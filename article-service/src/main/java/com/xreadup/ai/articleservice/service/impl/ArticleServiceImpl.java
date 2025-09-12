package com.xreadup.ai.articleservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisRequest;
import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisResponse;
import com.xreadup.ai.articleservice.mapper.ArticleMapper;
import com.xreadup.ai.articleservice.model.dto.ArticleQueryDTO;
import com.xreadup.ai.articleservice.model.dto.GnewsResponse;
import com.xreadup.ai.articleservice.model.dto.ManualDifficultyDTO;
import com.xreadup.ai.articleservice.model.entity.Article;
import com.xreadup.ai.articleservice.model.vo.ArticleDetailVO;
import com.xreadup.ai.articleservice.model.vo.ArticleVO;
import com.xreadup.ai.articleservice.service.ArticleService;
import com.xreadup.ai.articleservice.service.AiIntegrationService;
import com.xreadup.ai.articleservice.service.GnewsService;
import com.xreadup.ai.articleservice.util.DifficultyEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService {
    
    private final ArticleMapper articleMapper;
    private final GnewsService gnewsService;
    private final DifficultyEvaluator difficultyEvaluator;
    private final AiIntegrationService aiIntegrationService;
    
    @Override
    @Cacheable(value = "articlePage", key = "#query.toString()")
    public IPage<com.xreadup.ai.articleservice.model.vo.ArticleListVO> getArticlePage(ArticleQueryDTO query) {
        Page<Article> page = new Page<>(query.getPage(), query.getSize());
        IPage<Article> articlePage = articleMapper.selectArticlePage(page, query);
        
        List<com.xreadup.ai.articleservice.model.vo.ArticleListVO> articleVOList = articlePage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
        
        IPage<com.xreadup.ai.articleservice.model.vo.ArticleListVO> resultPage = new Page<>();
        resultPage.setRecords(articleVOList);
        resultPage.setTotal(articlePage.getTotal());
        resultPage.setCurrent(articlePage.getCurrent());
        resultPage.setSize(articlePage.getSize());
        
        return resultPage;
    }
    
    @Override
    @Cacheable(value = "articleDetail", key = "#id")
    public ArticleVO getArticleDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            return null;
        }
        return convertToVO(article);
    }

    @Override
    @CacheEvict(value = "articleDetail", key = "#id")
    public ArticleDetailVO deepDiveAnalysis(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            return null;
        }

        ArticleDetailVO detailVO = new ArticleDetailVO();
        detailVO.setArticle(convertToVO(article));
        
        // 强制重新分析文章
        ArticleAnalysisResponse aiAnalysis = aiIntegrationService.analyzeArticle(article);
        detailVO.setAiAnalysis(aiAnalysis);
        detailVO.setHasAiAnalysis(true);
        
        log.info("成功对文章进行AI分析: {} (ID: {})", article.getTitle(), id);
        return detailVO;
    }
    
    @Override
    @CacheEvict(value = {"articlePage", "articleDetail"}, allEntries = true)
    public boolean updateManualDifficulty(ManualDifficultyDTO dto) {
        Article article = new Article();
        article.setId(dto.getArticleId());
        article.setManualDifficulty(dto.getManualDifficulty());
        
        return articleMapper.updateById(article) > 0;
    }
    
    @Override
    @CacheEvict(value = {"articlePage"}, allEntries = true)
    public int refreshArticles(String category, Integer count) {
        try {
            log.info("Starting refresh with category: {} and count: {}", category, count);
            
            // 从gnews.io获取文章
            List<GnewsResponse.GnewsArticle> gnewsArticles = gnewsService.fetchArticlesByCategory(category, count);
            
            log.info("GNews API returned {} articles for category: {} with requested count: {}", 
                    gnewsArticles.size(), category, count);
            
            return processAndSaveArticles(gnewsArticles);
            
        } catch (Exception e) {
            log.error("Error refreshing articles from gnews.io", e);
            return 0;
        }
    }
    
    @Override
    @CacheEvict(value = {"articlePage"}, allEntries = true)
    public int refreshTopHeadlines(Integer count) {
        try {
            log.info("Starting refresh top headlines with count: {}", count);
            
            // 从gnews.io获取头条新闻
            List<GnewsResponse.GnewsArticle> gnewsArticles = gnewsService.fetchTopHeadlines(count);
            
            log.info("GNews API returned {} top headlines", gnewsArticles.size());
            
            return processAndSaveArticles(gnewsArticles);
            
        } catch (Exception e) {
            log.error("Error refreshing top headlines from gnews.io", e);
            return 0;
        }
    }
    
    private int processAndSaveArticles(List<GnewsResponse.GnewsArticle> gnewsArticles) {
        if (gnewsArticles.isEmpty()) {
            log.warn("No articles fetched from gnews.io");
            return 0;
        }
        
        int savedCount = 0;
        int duplicateCount = 0;
        
        for (GnewsResponse.GnewsArticle gnewsArticle : gnewsArticles) {
            try {
                // 检查文章是否已存在（根据URL去重）
                LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Article::getUrl, gnewsArticle.getUrl());
                Article existingArticle = articleMapper.selectOne(wrapper);
                if (existingArticle != null) {
                    log.debug("Article already exists: {}", gnewsArticle.getUrl());
                    duplicateCount++;
                    continue;
                }
                
                // 创建新文章实体
                Article article = new Article();
                article.setTitle(gnewsArticle.getTitle());
                article.setDescription(gnewsArticle.getDescription());
                article.setContentEn(gnewsArticle.getContent());
                article.setContentCn(""); // 中文翻译可后续通过AI服务生成
                article.setUrl(gnewsArticle.getUrl());
                article.setImage(gnewsArticle.getImage());
                article.setPublishedAt(gnewsArticle.getPublishedAt());
                article.setSource(gnewsArticle.getSource().getName());
                article.setCategory(mapToCategory(gnewsArticle.getTitle(), gnewsArticle.getDescription()));
                article.setDifficultyLevel(difficultyEvaluator.evaluateDifficulty(gnewsArticle.getContent()));
                article.setWordCount(gnewsArticle.getContent() != null ? 
                                   gnewsArticle.getContent().split("\\s+").length : 0);
                article.setReadCount(0);
                article.setLikeCount(0);
                article.setIsFeatured(false);
                article.setCreateTime(LocalDateTime.now());
                article.setUpdateTime(LocalDateTime.now());
                
                // 保存到数据库
                articleMapper.insert(article);
                savedCount++;
                
                log.info("Successfully saved article: {} (difficulty: {})", 
                        article.getTitle(), article.getDifficultyLevel());
                
            } catch (Exception e) {
                log.error("Error saving article: {}", gnewsArticle.getTitle(), e);
            }
        }
        
        log.info("Refresh completed: {} new articles saved, {} duplicates skipped from {} total articles", 
                savedCount, duplicateCount, gnewsArticles.size());
        return savedCount;
    }
    
    @Override
    @CacheEvict(value = "articleDetail", key = "#articleId")
    public void incrementReadCount(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setReadCount(article.getReadCount() + 1);
            articleMapper.updateById(article);
        }
    }
    
    private ArticleVO convertToVO(Article article) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }
    
    private com.xreadup.ai.articleservice.model.vo.ArticleListVO convertToListVO(Article article) {
        com.xreadup.ai.articleservice.model.vo.ArticleListVO vo = new com.xreadup.ai.articleservice.model.vo.ArticleListVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }
    
    private String mapToCategory(String title, String description) {
        String combinedText = (title + " " + description).toLowerCase();
        
        if (combinedText.contains("technology") || combinedText.contains("tech") || 
            combinedText.contains("ai") || combinedText.contains("software")) {
            return "technology";
        }
        if (combinedText.contains("business") || combinedText.contains("finance") || 
            combinedText.contains("market") || combinedText.contains("economic")) {
            return "business";
        }
        if (combinedText.contains("health") || combinedText.contains("medical") || 
            combinedText.contains("doctor") || combinedText.contains("disease")) {
            return "health";
        }
        if (combinedText.contains("science") || combinedText.contains("research") || 
            combinedText.contains("study") || combinedText.contains("scientist")) {
            return "science";
        }
        if (combinedText.contains("sports") || combinedText.contains("game") || 
            combinedText.contains("player") || combinedText.contains("team")) {
            return "sports";
        }
        if (combinedText.contains("education") || combinedText.contains("school") || 
            combinedText.contains("university") || combinedText.contains("learning")) {
            return "education";
        }
        if (combinedText.contains("environment") || combinedText.contains("climate") || 
            combinedText.contains("pollution") || combinedText.contains("green")) {
            return "environment";
        }
        
        return "general";
    }
    
    @Override
    @CacheEvict(value = {"articleDetail", "articlePage"}, allEntries = true)
    public String translate(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            return null;
        }
        
        try {
            ArticleAnalysisResponse response = aiIntegrationService.analyzeArticle(article);
            String translation = response.getChineseTranslation();
            
            // 将翻译结果保存到数据库
            if (translation != null && !"翻译服务暂时不可用".equals(translation)) {
                article.setContentCn(translation);
                article.setUpdateTime(LocalDateTime.now());
                articleMapper.updateById(article);
                log.info("文章翻译已保存到数据库，文章ID: {}", id);
            }
            
            return translation;
        } catch (Exception e) {
            log.error("文章翻译失败，文章ID: {}", id, e);
            return "翻译服务暂时不可用";
        }
    }
    
    @Override
    public String quickRead(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            return null;
        }
        
        try {
            ArticleAnalysisResponse response = aiIntegrationService.analyzeArticle(article);
            return response.getSummary();
        } catch (Exception e) {
            log.error("文章摘要失败，文章ID: {}", id, e);
            return "摘要服务暂时不可用";
        }
    }
    
    @Override
    public List<String> keyPoints(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            return null;
        }
        
        try {
            ArticleAnalysisResponse response = aiIntegrationService.analyzeArticle(article);
            return response.getKeywords();
        } catch (Exception e) {
            log.error("关键词提取失败，文章ID: {}", id, e);
            return List.of("服务暂时不可用");
        }
    }
    
    @Override
    public ArticleDetailVO microStudy(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            return null;
        }
        
        ArticleDetailVO detailVO = new ArticleDetailVO();
        detailVO.setArticle(convertToVO(article));
        
        try {
            ArticleAnalysisResponse aiAnalysis = aiIntegrationService.analyzeArticle(article);
            detailVO.setAiAnalysis(aiAnalysis);
            detailVO.setHasAiAnalysis(true);
        } catch (Exception e) {
            log.error("短文精学分析失败，文章ID: {}", id, e);
            detailVO.setHasAiAnalysis(false);
        }
        
        return detailVO;
    }
}
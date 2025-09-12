package com.xreadup.ai.articleservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xreadup.ai.articleservice.mapper.ArticleMapper;
import com.xreadup.ai.articleservice.model.common.PageResult;
import com.xreadup.ai.articleservice.model.dto.ArticleQueryDTO;
import com.xreadup.ai.articleservice.model.dto.ManualDifficultyDTO;
import com.xreadup.ai.articleservice.model.entity.Article;
import com.xreadup.ai.articleservice.model.vo.ArticleDetailVO;
import com.xreadup.ai.articleservice.model.vo.ArticleVO;
import com.xreadup.ai.articleservice.service.ArticleService;
import com.xreadup.ai.articleservice.service.AiIntegrationService;
import com.xreadup.ai.articleservice.service.GnewsService;
import com.xreadup.ai.articleservice.model.dto.ArticleAnalysisRequest;
import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisResponse;
import com.xreadup.ai.articleservice.model.vo.ArticleListVO;
import com.xreadup.ai.articleservice.model.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final AiIntegrationService aiIntegrationService;
    private final GnewsService gnewsService;

    @Override
    public IPage<ArticleVO> getArticlePage(ArticleQueryDTO query) {
        Page<Article> page = new Page<>(query.getPage(), query.getSize());
        
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getPublishedAt);
        
        if (query.getCategory() != null && !query.getCategory().isEmpty()) {
            wrapper.eq(Article::getCategory, query.getCategory());
        }
        
        if (query.getDifficultyLevel() != null) {
            wrapper.eq(Article::getDifficultyLevel, query.getDifficultyLevel());
        }
        
        IPage<Article> articlePage = articleMapper.selectPage(page, wrapper);
        
        return articlePage.convert(article -> {
            ArticleVO vo = new ArticleVO();
             vo.setId(article.getId());
             vo.setTitle(article.getTitle());
             vo.setContentEn(article.getContentEn());
             vo.setDescription(article.getDescription());
             vo.setUrl(article.getUrl());
             vo.setImage(article.getImage());
             vo.setCategory(article.getCategory());
             vo.setPublishedAt(article.getPublishedAt());
             vo.setSource(article.getSource());
             vo.setReadCount(article.getReadCount());
             vo.setDifficultyLevel(article.getDifficultyLevel());
             vo.setWordCount(article.getWordCount());
            return vo;
        });
    }

    @Override
    public ArticleVO getArticleDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            return null;
        }
        
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContentEn(article.getContentEn());
        vo.setContentCn(article.getContentCn());
        vo.setDescription(article.getDescription());
        vo.setUrl(article.getUrl());
        vo.setImage(article.getImage());
        vo.setCategory(article.getCategory());
        vo.setPublishedAt(article.getPublishedAt());
        vo.setSource(article.getSource());
        vo.setReadCount(article.getReadCount());
        vo.setDifficultyLevel(article.getDifficultyLevel());
        vo.setWordCount(article.getWordCount());
        return vo;
    }

    @Override
    public ApiResponse<ArticleDetailVO> deepDiveAnalysis(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            return null;
        }
        
        try {
            // 调用AI服务获取完整分析并保存结果
            var response = aiIntegrationService.analyzeAndSaveArticle(article);
            
            ArticleDetailVO vo = new ArticleDetailVO();
            
            // 创建ArticleVO对象
            ArticleVO articleVO = new ArticleVO();
            articleVO.setId(article.getId());
            articleVO.setTitle(article.getTitle());
            articleVO.setContentEn(article.getContentEn());
            articleVO.setContentCn(article.getContentCn());
            articleVO.setDescription(article.getDescription());
            articleVO.setUrl(article.getUrl());
            articleVO.setImage(article.getImage());
            articleVO.setCategory(article.getCategory());
            articleVO.setPublishedAt(article.getPublishedAt());
            articleVO.setSource(article.getSource());
            articleVO.setReadCount(article.getReadCount());
            articleVO.setDifficultyLevel(article.getDifficultyLevel());
            articleVO.setWordCount(article.getWordCount());
            
            vo.setArticle(articleVO);
            vo.setAiAnalysis(response);
            vo.setHasAiAnalysis(true);
            
            return ApiResponse.success(vo);
            
        } catch (Exception e) {
            log.error("AI分析失败，文章ID: {}", id, e);
            return ApiResponse.error("AI分析失败");
        }
    }

    @Override
    @Transactional
    public ApiResponse<Boolean> updateManualDifficulty(ManualDifficultyDTO dto) {
        Article article = articleMapper.selectById(dto.getArticleId());
        if (article == null) {
            return ApiResponse.error("文章不存在");
        }
        
        // 修复：使用正确的setDifficultyLevel()而不是setDifficulty()
        article.setDifficultyLevel(dto.getManualDifficulty());
        article.setManualDifficulty(dto.getManualDifficulty());
        
        boolean updated = articleMapper.updateById(article) > 0;
        return ApiResponse.success(updated);
    }

    @Override
    public List<ArticleVO> refreshArticles(String category, int limit) {
        try {
            return gnewsService.fetchArticlesByCategory(category, limit).stream()
                .map(article -> {
                    ArticleVO vo = new ArticleVO();
                    vo.setTitle(article.getTitle());
                    vo.setDescription(article.getDescription());
                    vo.setContentEn(article.getContent());
                    vo.setContentCn("");
                    vo.setUrl(article.getUrl());
                    vo.setImage(article.getImage());
                    vo.setPublishedAt(article.getPublishedAt());
                    vo.setSource(article.getSource() != null ? article.getSource().getName() : "Unknown");
                    vo.setCategory(category);
                    return vo;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取分类文章失败，分类: {}", category, e);
            return List.of();
        }
    }

    @Override
    public List<ArticleVO> refreshTopHeadlines(int limit) {
        try {
            return gnewsService.fetchTopHeadlines(limit).stream()
                .map(article -> {
                    ArticleVO vo = new ArticleVO();
                    vo.setTitle(article.getTitle());
                    vo.setDescription(article.getDescription());
                    vo.setContentEn(article.getContent());
                    vo.setContentCn("");
                    vo.setUrl(article.getUrl());
                    vo.setImage(article.getImage());
                    vo.setPublishedAt(article.getPublishedAt());
                    vo.setSource(article.getSource() != null ? article.getSource().getName() : "Unknown");
                    vo.setCategory("general");
                    return vo;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取热点文章失败", e);
            return List.of();
        }
    }

    @Override
    public void incrementReadCount(Long id) {
        Article article = articleMapper.selectById(id);
        if (article != null) {
            article.setReadCount(article.getReadCount() + 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    public ApiResponse<PageResult<ArticleListVO>> exploreArticles(ArticleQueryDTO query) {
        Page<Article> page = new Page<>(query.getPage(), query.getSize());
        
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        
        if (query.getCategory() != null && !query.getCategory().isEmpty()) {
            wrapper.eq(Article::getCategory, query.getCategory());
        }
        
        // 修复：使用正确的getDifficultyLevel()而不是getDifficulty()
        if (query.getDifficultyLevel() != null && !query.getDifficultyLevel().isEmpty()) {
            wrapper.eq(Article::getDifficultyLevel, query.getDifficultyLevel());
        }
        
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(Article::getTitle, query.getKeyword());
        }
        
        wrapper.orderByDesc(Article::getPublishedAt);
        
        Page<Article> articlePage = articleMapper.selectPage(page, wrapper);
        
        List<ArticleListVO> voList = articlePage.getRecords().stream()
                .map(article -> {
                    ArticleListVO vo = new ArticleListVO();
                    vo.setId(article.getId());
                    vo.setTitle(article.getTitle());
                    vo.setDescription(article.getDescription());
                    vo.setUrl(article.getUrl());
                    vo.setImage(article.getImage());
                    vo.setCategory(article.getCategory());
                    vo.setPublishedAt(article.getPublishedAt());
                    vo.setSource(article.getSource());
                    vo.setReadCount(article.getReadCount());
                    vo.setDifficultyLevel(article.getDifficultyLevel());
                    vo.setWordCount(article.getWordCount());
                    return vo;
                })
                .collect(Collectors.toList());
        
        PageResult<ArticleListVO> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(articlePage.getTotal());
        result.setPages(articlePage.getPages());
        result.setCurrent(articlePage.getCurrent());
        result.setSize(articlePage.getSize());
        
        return ApiResponse.success(result);
    }
    
    @Override
    public ApiResponse<ArticleDetailVO> readArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            return ApiResponse.error("文章不存在");
        }
        
        // 增加阅读次数
        article.setReadCount(article.getReadCount() + 1);
        articleMapper.updateById(article);
        
        ArticleDetailVO vo = new ArticleDetailVO();
        
        // 创建ArticleVO对象
        ArticleVO articleVO = new ArticleVO();
        articleVO.setId(article.getId());
        articleVO.setTitle(article.getTitle());
        articleVO.setContentEn(article.getContentEn());
        articleVO.setContentCn(article.getContentCn());
        articleVO.setDescription(article.getDescription());
        articleVO.setUrl(article.getUrl());
        articleVO.setImage(article.getImage());
        articleVO.setCategory(article.getCategory());
        articleVO.setPublishedAt(article.getPublishedAt());
        articleVO.setSource(article.getSource());
        articleVO.setReadCount(article.getReadCount());
        articleVO.setDifficultyLevel(article.getDifficultyLevel());
        articleVO.setWordCount(article.getWordCount());
        
        vo.setArticle(articleVO);
        vo.setHasAiAnalysis(false);
        
        return ApiResponse.success(vo);
    }
    

}
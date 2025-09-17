package com.xreadup.ai.articleservice.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
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
import com.xreadup.ai.articleservice.model.dto.GnewsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xreadup.ai.articleservice.service.ScraperService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final AiIntegrationService aiIntegrationService;
    private final GnewsService gnewsService;
    private final ScraperService scraperService;
    
    @Override
    public ApiResponse<ArticleDetailVO> readArticle(Long id) {
        try {
            Article article = articleMapper.selectById(id);
            if (article == null) {
                return ApiResponse.error("文章不存在");
            }
            
            // 增加阅读次数
            incrementReadCount(id);
            
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
            
            // 创建ArticleDetailVO对象
            ArticleDetailVO detailVO = new ArticleDetailVO();
            detailVO.setArticle(articleVO);
            detailVO.setHasAiAnalysis(!article.getContentCn().isEmpty());
            
            return ApiResponse.success(detailVO);
        } catch (Exception e) {
            log.error("读取文章失败，文章ID: {}", id, e);
            return ApiResponse.error("读取文章失败");
        }
    }
    
    @Override
    public ApiResponse<PageResult<ArticleListVO>> exploreArticles(ArticleQueryDTO query) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
            
            // 根据关键词模糊查询
            if (StringUtils.isNotBlank(query.getKeyword())) {
                queryWrapper.like(Article::getTitle, query.getKeyword());
            }
            
            // 根据分类筛选
            if (StringUtils.isNotBlank(query.getCategory())) {
                queryWrapper.eq(Article::getCategory, query.getCategory());
            }
            
            // 根据难度级别筛选
            if (StringUtils.isNotBlank(query.getDifficultyLevel())) {
                queryWrapper.eq(Article::getDifficultyLevel, query.getDifficultyLevel());
            }
            
            // 根据发布时间排序
            queryWrapper.orderByDesc(Article::getPublishedAt);
            
            // 执行分页查询
            Page<Article> page = new Page<>(query.getPage(), query.getSize());
            IPage<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
            
            // 转换为ArticleListVO对象列表
            List<ArticleListVO> listVOs = articlePage.getRecords().stream().map(article -> {
                ArticleListVO listVO = new ArticleListVO();
                listVO.setId(article.getId());
                listVO.setTitle(article.getTitle());
                listVO.setDescription(article.getDescription());
                listVO.setImage(article.getImage());
                listVO.setCategory(article.getCategory());
                listVO.setPublishedAt(article.getPublishedAt());
                listVO.setReadCount(article.getReadCount());
                listVO.setDifficultyLevel(article.getDifficultyLevel());
                listVO.setWordCount(article.getWordCount());
                return listVO;
            }).collect(Collectors.toList());
            
            // 构建PageResult对象
            PageResult<ArticleListVO> pageResult = new PageResult<>();
            pageResult.setList(listVOs);
            pageResult.setTotal(articlePage.getTotal());
            pageResult.setCurrent(articlePage.getCurrent());
            pageResult.setSize(articlePage.getSize());
            
            return ApiResponse.success(pageResult);
        } catch (Exception e) {
            log.error("探索文章列表失败", e);
            return ApiResponse.error("探索文章列表失败");
        }
    }

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
            // 确保请求数量为非负值
            int safeLimit = Math.max(0, limit);
            log.info("开始刷新{}分类文章，数量：{}", category, safeLimit);
            
            // 调用fetchAndSaveArticles获取并保存完整内容
            int savedCount = fetchAndSaveArticles(category, safeLimit);
            log.info("成功保存 {} 篇新的{}分类文章到数据库", savedCount, category);
            
            // 从数据库获取分类文章并返回
            LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Article::getCategory, category)
                       .orderByDesc(Article::getPublishedAt)
                       .last("LIMIT " + safeLimit);
            
            List<Article> articles = articleMapper.selectList(queryWrapper);
            log.info("从数据库获取到{}篇{}分类文章", articles.size(), category);
            
            return articles.stream().map(article -> {
                ArticleVO vo = new ArticleVO();
                vo.setId(article.getId());
                vo.setTitle(article.getTitle());
                vo.setDescription(article.getDescription());
                vo.setUrl(article.getUrl());
                vo.setImage(article.getImage());
                vo.setSource(article.getSource());
                vo.setPublishedAt(article.getPublishedAt());
                vo.setCategory(category);
                vo.setContentEn(article.getContentEn()); // 返回完整内容
                log.debug("文章ID: {}, 标题: {}, 内容长度: {}", article.getId(), article.getTitle(), 
                          article.getContentEn() != null ? article.getContentEn().length() : 0);
                return vo;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取和保存分类文章失败，分类: {}", category, e);
            // 即使出现异常，也尝试从数据库获取已有的文章
            try {
                LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Article::getCategory, category)
                           .orderByDesc(Article::getPublishedAt)
                           .last("LIMIT " + Math.max(0, limit));
                
                List<Article> existingArticles = articleMapper.selectList(queryWrapper);
                log.info("异常后从数据库获取到{}篇已有{}分类文章", existingArticles.size(), category);
                return existingArticles.stream().map(article -> {
                    ArticleVO vo = new ArticleVO();
                    vo.setId(article.getId());
                    vo.setTitle(article.getTitle());
                    vo.setDescription(article.getDescription());
                    vo.setUrl(article.getUrl());
                    vo.setImage(article.getImage());
                    vo.setSource(article.getSource());
                    vo.setPublishedAt(article.getPublishedAt());
                    vo.setCategory(category);
                    vo.setContentEn(article.getContentEn());
                    return vo;
                }).collect(Collectors.toList());
            } catch (Exception innerE) {
                log.error("获取已有文章也失败", innerE);
                return List.of();
            }
        }
    }

    @Override
    public List<ArticleVO> refreshTopHeadlines(int limit) {
        try {
            // 确保请求数量为非负值
            int safeLimit = Math.max(0, limit);
            log.info("开始刷新热点文章，请求数量：{}", safeLimit);
            
            // 直接调用GNews API获取热点文章
            List<GnewsResponse.GnewsArticle> gnewsArticles = gnewsService.fetchTopHeadlines(safeLimit);
            log.info("从GNews API获取到{}篇热点文章，将全部处理", gnewsArticles.size());
            
            int processedCount = 0;
            int savedCount = 0;
            int existingCount = 0;
            int failedScrapeCount = 0;
            // 遍历热点文章并处理每一篇
            for (GnewsResponse.GnewsArticle gnewsArticle : gnewsArticles) {
                processedCount++;
                String url = gnewsArticle.getUrl();
                String title = gnewsArticle.getTitle();
                log.info("处理第{}篇热点文章: {} - {}", processedCount, url, title);
                
                // 检查数据库是否已存在该文章
                if (isArticleExists(url)) {
                    existingCount++;
                    log.info("热点文章已存在，跳过({}/{}): {} - {}", processedCount, gnewsArticles.size(), title, url);
                    continue;
                }
                
                // 使用Readability4J获取文章全文
                log.info("尝试使用Readability4J获取文章全文: {}", url);
                Optional<String> fullContentOptional = scraperService.scrapeArticleContent(url);
                
                if (fullContentOptional.isPresent()) {
                    String fullContent = fullContentOptional.get();
                    log.info("成功获取文章全文，长度: {} 字符", fullContent.length());
                    
                    // 封装Article实体
                    Article article = new Article();
                    article.setTitle(title);
                    article.setDescription(gnewsArticle.getDescription());
                    article.setUrl(url);
                    article.setImage(gnewsArticle.getImage());
                    article.setPublishedAt(gnewsArticle.getPublishedAt());
                    article.setSource(gnewsArticle.getSource().getName());
                    article.setCategory("general");
                    article.setContentEn(fullContent);
                    article.setContentCn("");
                    article.setWordCount(countWords(fullContent));
                    article.setDifficultyLevel("B1");
                    
                    // 插入数据库
                    int insertResult = articleMapper.insert(article);
                    if (insertResult > 0) {
                        savedCount++;
                        log.info("成功存储热点文章({}/{}): {} (ID: {})", savedCount, gnewsArticles.size(), title, article.getId());
                    } else {
                        log.error("数据库插入失败，文章: {}", title);
                    }
                } else {
                    failedScrapeCount++;
                    log.warn("未能获取热点文章全文，跳过存储({}/{}): {} - {}", processedCount, gnewsArticles.size(), title, url);
                }
            }
            
            log.info("热点文章处理完成，统计信息：总获取={}篇，成功存储={}篇，已存在={}篇，抓取失败={}篇", 
                     gnewsArticles.size(), savedCount, existingCount, failedScrapeCount);
            
            // 从数据库获取热点文章并返回
            LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Article::getCategory, "general")
                       .orderByDesc(Article::getPublishedAt)
                       .last("LIMIT " + safeLimit);
            
            List<Article> articles = articleMapper.selectList(queryWrapper);
            log.info("从数据库获取到{}篇热点文章并返回", articles.size());
            
            return articles.stream().map(article -> {
                ArticleVO vo = new ArticleVO();
                vo.setId(article.getId());
                vo.setTitle(article.getTitle());
                vo.setDescription(article.getDescription());
                vo.setUrl(article.getUrl());
                vo.setImage(article.getImage());
                vo.setSource(article.getSource());
                vo.setPublishedAt(article.getPublishedAt());
                vo.setCategory("general");
                vo.setContentEn(article.getContentEn()); // 返回完整内容
                log.debug("热点文章ID: {}, 标题: {}, 内容长度: {}", article.getId(), article.getTitle(), 
                          article.getContentEn() != null ? article.getContentEn().length() : 0);
                return vo;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取和保存热点文章失败", e);
            // 即使出现异常，也尝试从数据库获取已有的热点文章
            try {
                LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Article::getCategory, "general")
                           .orderByDesc(Article::getPublishedAt)
                           .last("LIMIT " + Math.max(0, limit));
                
                List<Article> existingArticles = articleMapper.selectList(queryWrapper);
                log.info("异常后从数据库获取到{}篇已有热点文章", existingArticles.size());
                return existingArticles.stream().map(article -> {
                    ArticleVO vo = new ArticleVO();
                    vo.setId(article.getId());
                    vo.setTitle(article.getTitle());
                    vo.setDescription(article.getDescription());
                    vo.setUrl(article.getUrl());
                    vo.setImage(article.getImage());
                    vo.setSource(article.getSource());
                    vo.setPublishedAt(article.getPublishedAt());
                    vo.setCategory("general");
                    vo.setContentEn(article.getContentEn());
                    return vo;
                }).collect(Collectors.toList());
            } catch (Exception innerE) {
                log.error("获取已有热点文章也失败", innerE);
                return List.of();
            }
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
    public boolean testApiConnection() {
        try {
            // 测试API连接，不直接返回列表内容
            gnewsService.fetchArticlesByCategory("technology", 1);
            // 如果没有抛出异常，则连接成功
            return true;
        } catch (Exception e) {
            log.error("API连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ApiResponse<Boolean> updateContentCn(String contentEn, String contentCn) {
        try {
            log.info("更新文章中文内容，英文内容长度: {}", contentEn.length());
            
            // 优化查找方式，使用内容片段而不是完整内容来查找文章
            // 当内容很长时，完整内容匹配会导致性能问题和匹配失败
            LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
            
            // 提取内容前200个字符和后200个字符作为识别依据
            String contentStart = contentEn.length() > 200 ? contentEn.substring(0, 200) : contentEn;
            String contentEnd = contentEn.length() > 200 ? 
                contentEn.substring(contentEn.length() - 200) : 
                contentEn.substring(0, Math.min(50, contentEn.length()));
            
            queryWrapper.likeRight(Article::getContentEn, contentStart)
                        .likeLeft(Article::getContentEn, contentEnd)
                        .last("LIMIT 1");
            
            Article article = articleMapper.selectOne(queryWrapper);
            
            if (article == null) {
                // 如果基于内容片段的搜索失败，记录错误并尝试通过文章标题查找（如果能从内容中提取）
                log.warn("未找到匹配的文章，尝试其他方式查找");
                return ApiResponse.success(false); // 返回成功但更新状态为false，避免影响主流程
            }
            
            // 更新中文内容
            article.setContentCn(contentCn);
            int updateResult = articleMapper.updateById(article);
            
            if (updateResult > 0) {
                log.info("成功更新文章中文内容，文章ID: {}", article.getId());
                return ApiResponse.success(true);
            } else {
                log.warn("更新文章中文内容失败，文章ID: {}", article.getId());
                return ApiResponse.success(false); // 返回成功但更新状态为false
            }
        } catch (Exception e) {
            log.error("更新文章中文内容时发生异常", e);
            // 返回成功但更新状态为false，避免影响翻译功能主流程
            return ApiResponse.success(false);
        }
    }
    
    @Override
    @Transactional
    public int fetchAndSaveArticles(String category, int limit) {
        int savedCount = 0;
        int totalProcessed = 0;
        int existingCount = 0;
        int failedScrapeCount = 0;
        int failedInsertCount = 0;
        try {
            // 确保请求数量为非负值
            int safeLimit = Math.max(0, limit);
            
            // 1. 调用 GNews API 获取文章列表
            List<GnewsResponse.GnewsArticle> gnewsArticles = gnewsService.fetchArticlesByCategory(category, safeLimit);
            
            log.info("从GNews API获取到 {} 篇关于 {} 分类的文章", gnewsArticles.size(), category);
            
            // 2. 遍历每篇文章
            for (GnewsResponse.GnewsArticle gnewsArticle : gnewsArticles) {
                totalProcessed++;
                String url = gnewsArticle.getUrl();
                String title = gnewsArticle.getTitle();
                
                log.info("处理第{}篇文章: {} - {}", totalProcessed, url, title);
                
                // 3. 检查数据库是否已存在该文章
                boolean exists = isArticleExists(url);
                if (exists) {
                    existingCount++;
                    log.info("文章已存在，跳过({}/{}): {} - {}", totalProcessed, gnewsArticles.size(), title, url);
                    continue;
                }
                
                // 4. 使用 Readability4J 爬虫服务获取文章全文
                log.info("尝试使用Readability4J获取文章全文: {}", url);
                Optional<String> fullContentOptional = scraperService.scrapeArticleContent(url);
                
                if (fullContentOptional.isEmpty()) {
                    failedScrapeCount++;
                    log.warn("未能获取文章全文，跳过存储({}/{}): {} - {}", totalProcessed, gnewsArticles.size(), title, url);
                    continue;
                }
                
                String fullContent = fullContentOptional.get();
                log.info("成功获取文章全文，长度: {} 字符", fullContent.length());
                
                // 5. 封装 Article 实体
                Article article = new Article();
                article.setTitle(title);
                article.setDescription(gnewsArticle.getDescription());
                article.setUrl(url);
                article.setImage(gnewsArticle.getImage());
                article.setPublishedAt(gnewsArticle.getPublishedAt());
                article.setSource(gnewsArticle.getSource().getName());
                article.setCategory(category);
                article.setContentEn(fullContent); // 存储完整内容
                article.setContentCn(""); // 初始为空
                article.setWordCount(countWords(fullContent)); // 计算单词数
                article.setDifficultyLevel("B1"); // 默认难度级别，可后续通过AI分析更新
                article.setReadCount(0);
                article.setLikeCount(0);
                
                // 6. 插入数据库
                try {
                    int insertResult = articleMapper.insert(article);
                    if (insertResult > 0) {
                        savedCount++;
                        log.info("成功存储文章({}/{}): {} (ID: {})", savedCount, gnewsArticles.size(), title, article.getId());
                    } else {
                        failedInsertCount++;
                        log.warn("数据库插入失败({}/{}): {}", totalProcessed, gnewsArticles.size(), title);
                    }
                } catch (Exception e) {
                    failedInsertCount++;
                    log.error("数据库插入异常({}/{}): {}", totalProcessed, gnewsArticles.size(), title, e);
                }
                
                // 礼貌延迟，避免被封，每篇文章抓取后延迟3~5秒
                try {
                    Thread.sleep(3000 + (long)(Math.random() * 2000)); // 3~5秒随机延迟
                    log.debug("延迟完成，继续处理下一篇文章");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("延迟线程被中断");
                }
            }
            
            log.info("文章处理完成，统计信息：总获取={}篇，成功存储={}篇，已存在={}篇，抓取失败={}篇，插入失败={}篇", 
                     gnewsArticles.size(), savedCount, existingCount, failedScrapeCount, failedInsertCount);
            return savedCount;
        } catch (Exception e) {
            log.error("获取和存储文章失败, 当前进度：已处理={}篇, 已保存={}篇", totalProcessed, savedCount, e);
            throw new RuntimeException("获取和存储文章失败", e);
        }
    }
    
    /**
     * 检查文章是否已存在于数据库
     */
    private boolean isArticleExists(String url) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getUrl, url);
        return articleMapper.selectCount(queryWrapper) > 0;
    }
    
    /**
     * 计算文本中的单词数
     */
    private int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        // 使用正则表达式分割文本为单词
        Pattern pattern = Pattern.compile("\\b\\w+\\b");
        Matcher matcher = pattern.matcher(text);
        
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        
        return count;
    }
}
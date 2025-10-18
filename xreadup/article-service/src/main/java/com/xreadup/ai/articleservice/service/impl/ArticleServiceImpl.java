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
import com.xreadup.ai.articleservice.model.vo.ArticleListVO;
import com.xreadup.ai.articleservice.model.common.ApiResponse;
import com.xreadup.ai.articleservice.model.dto.GnewsResponse;
import com.xreadup.ai.articleservice.service.ScraperService;
import com.xreadup.ai.articleservice.util.DifficultyEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final AiIntegrationService aiIntegrationService;
    private final GnewsService gnewsService;
    private final ScraperService scraperService;
    private final DifficultyEvaluator difficultyEvaluator;
    
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
                vo.setDifficultyLevel(article.getDifficultyLevel()); // 设置难度等级
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
                    vo.setDifficultyLevel(article.getDifficultyLevel()); // 设置难度等级
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
                    // 使用DifficultyEvaluator评估文章难度
                    String difficultyLevel = difficultyEvaluator.evaluateDifficulty(fullContent);
                    article.setDifficultyLevel(difficultyLevel);
                    log.info("热点文章ID: {}, 标题: {}, 评估难度等级: {}", article.getId(), article.getTitle(), difficultyLevel);
                    
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
                vo.setDifficultyLevel(article.getDifficultyLevel()); // 设置难度等级
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
                    vo.setDifficultyLevel(article.getDifficultyLevel()); // 设置难度等级
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
            
            // 检查英文内容是否包含分段标记
            boolean hasParagraphs = contentEn.contains("\n\n") || contentEn.contains("\r\n\r\n");
            log.info("文章ID: {}，英文内容分段标记检查结果: {}, 包含\n\n: {}, 包含\r\n\r\n: {}", 
                article.getId(), hasParagraphs, contentEn.contains("\n\n"), contentEn.contains("\r\n\r\n"));
            
            // 如果英文内容包含分段标记，但中文翻译不包含，尝试智能分段
            boolean cnHasParagraphs = contentCn.contains("\n\n") || contentCn.contains("\r\n\r\n");
            log.info("文章ID: {}，中文翻译分段标记检查结果: {}, 包含\n\n: {}, 包含\r\n\r\n: {}", 
                article.getId(), cnHasParagraphs, contentCn.contains("\n\n"), contentCn.contains("\r\n\r\n"));
            
            if (hasParagraphs && !cnHasParagraphs) {
                contentCn = segmentChineseTranslation(contentCn, contentEn);
                log.info("文章ID: {}，已触发智能分段处理，处理前中文长度: {}，处理后中文长度: {}", 
                    article.getId(), contentCn.length(), contentCn.length());
            } else if (!hasParagraphs) {
                log.info("文章ID: {}，未触发智能分段处理 - 英文内容不包含分段标记", article.getId());
            } else if (cnHasParagraphs) {
                log.info("文章ID: {}，未触发智能分段处理 - 中文翻译已包含分段标记", article.getId());
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
    
    // 定义中文标点符号数组，用于智能分段
    private static final char[] punctuationMarks = { '。', '？', '！', '.', '?', '!', '；', '…' };
    
    /**
     * 智能分段中文翻译内容
     * 尝试根据英文原文的段落结构对中文翻译进行分段
     */
    private String segmentChineseTranslation(String chineseTranslation, String englishContent) {
        if (chineseTranslation == null || chineseTranslation.isEmpty() || englishContent == null || englishContent.isEmpty()) {
            log.info("智能分段 - 跳过处理：输入内容为空");
            return chineseTranslation;
        }
        
        // 首先检查中文翻译是否已经包含分段标记
        boolean cnHasDoubleNewline = chineseTranslation.contains("\n\n") || chineseTranslation.contains("\r\n\r\n");
        if (cnHasDoubleNewline) {
            log.info("智能分段 - 跳过处理：中文翻译已包含分段标记");
            
            // 对已有的分段进行优化，合并短段落
            String[] paragraphs;
            if (chineseTranslation.contains("\r\n\r\n")) {
                paragraphs = chineseTranslation.split("\\r\\n\\r\\n");
            } else {
                paragraphs = chineseTranslation.split("\\n\\n");
            }
            
            // 过滤空段落并合并短段落
            List<String> filteredParagraphs = new ArrayList<>();
            for (int i = 0; i < paragraphs.length; i++) {
                String paragraph = paragraphs[i].trim();
                if (paragraph.isEmpty()) {
                    continue;
                }
                
                // 提高短段落合并阈值，从20字符增加到50字符
                if (paragraph.length() < 50 && !filteredParagraphs.isEmpty()) {
                    String lastParagraph = filteredParagraphs.remove(filteredParagraphs.size() - 1);
                    filteredParagraphs.add(lastParagraph + paragraph);
                } else {
                    filteredParagraphs.add(paragraph);
                }
            }
            
            // 重新组合为带有双换行符的完整内容
            return String.join("\n\n", filteredParagraphs);
        }
        
        // 分割英文原文为段落
        String[] englishParagraphs;
        if (englishContent.contains("\r\n\r\n")) {
            englishParagraphs = englishContent.split("\\r\\n\\r\\n");
        } else {
            englishParagraphs = englishContent.split("\\n\\n");
        }
        
        // 过滤英文空段落
        List<String> nonEmptyEnglishParagraphs = new ArrayList<>();
        for (String paragraph : englishParagraphs) {
            if (!paragraph.trim().isEmpty()) {
                nonEmptyEnglishParagraphs.add(paragraph);
            }
        }
        englishParagraphs = nonEmptyEnglishParagraphs.toArray(new String[0]);
        
        log.info("智能分段 - 英文段落数量：{}", englishParagraphs.length);
        for (int i = 0; i < Math.min(3, englishParagraphs.length); i++) {
            log.debug("智能分段 - 英文段落{}长度：{}", i+1, englishParagraphs[i].length());
        }
        
        // 如果英文段落少于2个，不进行分段
        if (englishParagraphs.length < 2) {
            log.info("智能分段 - 跳过处理：英文段落少于2个");
            return chineseTranslation;
        }
        
        // 计算每段英文的平均长度
        int totalEnglishLength = 0;
        for (String paragraph : englishParagraphs) {
            totalEnglishLength += paragraph.length();
        }
        double avgParagraphLength = (double) totalEnglishLength / englishParagraphs.length;
        log.info("智能分段 - 英文总长度：{}, 平均段落长度：{}", totalEnglishLength, avgParagraphLength);
        
        // 开始对中文翻译进行分段
        StringBuilder segmentedChinese = new StringBuilder();
        int currentPosition = 0;
        
        // 找出所有可能的自然分段点
        List<Integer> naturalBreaks = new ArrayList<>();
        for (int i = 0; i < chineseTranslation.length(); i++) {
            char c = chineseTranslation.charAt(i);
            for (char mark : punctuationMarks) {
                if (c == mark) {
                    // 改进自然分段点识别：不仅检查换行符和空格，还检查其他可能的段落起始标志
                    if (i + 1 < chineseTranslation.length()) {
                        char nextChar = chineseTranslation.charAt(i + 1);
                        // 段落结束的情况：句号后接换行符、空格、数字或大写字母
                        if (nextChar == '\n' || nextChar == '\r' || 
                            Character.isWhitespace(nextChar) || 
                            Character.isDigit(nextChar) || 
                            Character.isUpperCase(nextChar)) {
                            // 进一步检查，避免在句子中间分段
                            int distanceToNext = findNextPunctuation(chineseTranslation, i + 1);
                            if (distanceToNext < 0 || distanceToNext > 50) {
                                // 如果下一个标点符号距离较远，认为这是一个段落结束
                                naturalBreaks.add(i + 1);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        // 对除最后一段外的每一段进行处理
        for (int i = 0; i < englishParagraphs.length - 1; i++) {
            // 根据英文段落长度计算中文段落的预估长度
            double ratio = (double) englishParagraphs[i].length() / totalEnglishLength;
            int estimatedChineseLength = (int) (chineseTranslation.length() * ratio);
            
            // 计算实际分段位置
            int segmentEnd = currentPosition + estimatedChineseLength;
            
            // 确保不越界
            segmentEnd = Math.min(segmentEnd, chineseTranslation.length() - 1);
            
            log.debug("智能分段 - 段落{}：预估中文长度={}, 预估位置={}", 
                i+1, estimatedChineseLength, segmentEnd);
            
            // 寻找合适的中文标点符号作为分段点，以保持语义完整
            int punctuationIndex = -1;
            
            // 扩大搜索范围，确保找到合适的分割点
            int searchStart = Math.max(currentPosition, segmentEnd - 200);
            int searchEnd = Math.min(chineseTranslation.length() - 1, segmentEnd + 200);
            
            // 优先查找自然分段点
            if (!naturalBreaks.isEmpty()) {
                for (int j = 0; j < naturalBreaks.size(); j++) {
                    int breakPos = naturalBreaks.get(j);
                    if (breakPos >= searchStart && breakPos <= searchEnd) {
                        // 检查分段点前后的文本长度，避免过短的段落
                        int segmentLength = breakPos - currentPosition;
                        if (segmentLength >= 30) { // 确保分段后的段落长度至少30个字符
                            punctuationIndex = breakPos - 1; // 指向标点符号位置
                            break;
                        }
                    }
                }
            }
            
            // 如果没有找到自然分段点，手动查找标点符号
            if (punctuationIndex == -1) {
                for (int j = searchEnd; j >= searchStart; j--) {
                    char c = chineseTranslation.charAt(j);
                    for (char mark : punctuationMarks) {
                        if (c == mark) {
                            // 确保分段后的段落长度合理
                            int segmentLength = j - currentPosition + 1;
                            if (segmentLength >= 30) {
                                punctuationIndex = j;
                                break;
                            }
                        }
                    }
                    if (punctuationIndex != -1) {
                        break;
                    }
                }
            }
            
            // 如果找到了合适的标点符号，就在该位置分段
            if (punctuationIndex != -1) {
                segmentEnd = punctuationIndex + 1; // 包含标点符号
                log.debug("智能分段 - 段落{}：找到标点符号 '{}' 在位置 {}, 实际分段位置={}", 
                    i+1, chineseTranslation.charAt(punctuationIndex), punctuationIndex, segmentEnd);
            } else {
                log.debug("智能分段 - 段落{}：未找到合适的标点符号，使用预估位置={}", i+1, segmentEnd);
            }
            
            // 添加当前段落
            if (segmentEnd > currentPosition) {
                segmentedChinese.append(chineseTranslation.substring(currentPosition, segmentEnd));
                segmentedChinese.append("\n\n"); // 添加分段标记
                log.debug("智能分段 - 段落{}：已添加，长度={}", 
                    i+1, segmentEnd - currentPosition);
                currentPosition = segmentEnd;
            }
        }
        
        // 添加最后一段
        if (currentPosition < chineseTranslation.length()) {
            segmentedChinese.append(chineseTranslation.substring(currentPosition));
            log.debug("智能分段 - 最后一段：长度={}", chineseTranslation.length() - currentPosition);
        }
        
        String result = segmentedChinese.toString().trim();
        
        // 最终检查：合并短段落和优化长句单独成段问题
        if (result.contains("\n\n")) {
            String[] paragraphs = result.split("\n\n");
            List<String> mergedParagraphs = new ArrayList<>();
            
            for (int i = 0; i < paragraphs.length; i++) {
                String paragraph = paragraphs[i].trim();
                if (paragraph.isEmpty()) {
                    continue;
                }
                
                // 策略1：合并短段落（少于50个字符）
                if (paragraph.length() < 50) {
                    if (!mergedParagraphs.isEmpty() && i < paragraphs.length - 1) {
                        // 如果前后都有段落，选择较短的一边合并
                        String lastParagraph = mergedParagraphs.get(mergedParagraphs.size() - 1);
                        String nextParagraph = paragraphs[i + 1].trim();
                        
                        if (lastParagraph.length() <= nextParagraph.length()) {
                            // 合并到前一段
                            mergedParagraphs.remove(mergedParagraphs.size() - 1);
                            mergedParagraphs.add(lastParagraph + paragraph);
                        } else {
                            // 暂时保留，后面合并到下一段
                            mergedParagraphs.add(paragraph);
                        }
                    } else if (!mergedParagraphs.isEmpty()) {
                        // 只有前一段，合并到前一段
                        String lastParagraph = mergedParagraphs.remove(mergedParagraphs.size() - 1);
                        mergedParagraphs.add(lastParagraph + paragraph);
                    } else if (i < paragraphs.length - 1) {
                        // 只有后一段，暂时保留，后面合并到下一段
                        mergedParagraphs.add(paragraph);
                    } else {
                        // 只有一段，无法合并
                        mergedParagraphs.add(paragraph);
                    }
                } 
                // 策略2：处理可能的长句单独成段问题
                else if (isPotentialSingleSentenceParagraphCn(paragraph)) {
                    // 如果当前段落可能只是一个长句，尝试与前后段落合并
                    if (!mergedParagraphs.isEmpty()) {
                        // 先检查前一段是否也是长句
                        String lastParagraph = mergedParagraphs.get(mergedParagraphs.size() - 1);
                        if (isPotentialSingleSentenceParagraphCn(lastParagraph)) {
                            // 前一段也是长句，合并到前一段
                            mergedParagraphs.remove(mergedParagraphs.size() - 1);
                            mergedParagraphs.add(lastParagraph + paragraph);
                        } else {
                            mergedParagraphs.add(paragraph);
                        }
                    } else if (i < paragraphs.length - 1) {
                        // 检查下一段是否也是长句
                        String nextParagraph = paragraphs[i + 1].trim();
                        if (!nextParagraph.isEmpty() && isPotentialSingleSentenceParagraphCn(nextParagraph)) {
                            // 下一段也是长句，暂时保留，后面合并
                            mergedParagraphs.add(paragraph);
                        } else {
                            mergedParagraphs.add(paragraph);
                        }
                    } else {
                        mergedParagraphs.add(paragraph);
                    }
                } else {
                    // 检查是否有之前保留的短段落需要合并到当前段落
                    if (!mergedParagraphs.isEmpty()) {
                        String lastParagraph = mergedParagraphs.get(mergedParagraphs.size() - 1);
                        if (lastParagraph.length() < 50) {
                            mergedParagraphs.remove(mergedParagraphs.size() - 1);
                            mergedParagraphs.add(lastParagraph + paragraph);
                        } else {
                            mergedParagraphs.add(paragraph);
                        }
                    } else {
                        mergedParagraphs.add(paragraph);
                    }
                }
            }
            
            // 重新组合段落
            result = String.join("\n\n", mergedParagraphs);
        }
        
        int paragraphCount = countParagraphs(result);
        log.info("智能分段 - 完成，中文翻译原始长度：{}, 处理后长度：{}, 生成段落数量：{}", 
            chineseTranslation.length(), result.length(), paragraphCount);
        
        return result;
    }
    
    /**
     * 查找下一个标点符号的位置
     */
    private int findNextPunctuation(String text, int startIndex) {
        if (text == null || startIndex >= text.length()) {
            return -1;
        }
        
        for (int i = startIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            for (char mark : punctuationMarks) {
                if (c == mark) {
                    return i - startIndex;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * 判断中文段落是否可能只是一个长句
     * 基于标点符号数量、段落长度等多维度分析
     */
    private boolean isPotentialSingleSentenceParagraphCn(String paragraph) {
        if (paragraph == null || paragraph.isEmpty()) {
            return false;
        }
        
        // 计算段落中的句子结束符数量
        int sentenceEndersCount = 0;
        for (char c : paragraph.toCharArray()) {
            if (c == '。' || c == '？' || c == '！' || c == '.' || c == '?' || c == '!') {
                sentenceEndersCount++;
            }
        }
        
        // 计算段落长度（字符数）
        int length = paragraph.length();
        
        // 中文特有判断：考虑逗号数量，中文句子通常有更多的逗号分割
        int commaCount = 0;
        for (char c : paragraph.toCharArray()) {
            if (c == '，' || c == ',') {
                commaCount++;
            }
        }
        
        // 多维度判断：
        // 1. 如果段落中只有一个或没有句子结束符，且长度不是特别长
        // 2. 句子结束符与段落长度的比例很低（每个句子平均长度过长）
        // 3. 逗号数量很多但句子结束符很少（表明句子被逗号分割得很细但缺少句号）
        boolean hasFewSentences = sentenceEndersCount <= 2;
        boolean highCommaToSentenceRatio = sentenceEndersCount > 0 && commaCount / sentenceEndersCount > 5;
        boolean isLongParagraphButFewSentences = length > 150 && sentenceEndersCount <= 2;
        
        return (sentenceEndersCount <= 1 && length < 250) || 
               (sentenceEndersCount > 0 && length / sentenceEndersCount > 120) ||
               (hasFewSentences && highCommaToSentenceRatio) ||
               isLongParagraphButFewSentences;
    }
    
    /**
     * 计算文本中的段落数量
     */
    private int countParagraphs(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        String[] paragraphs;
        if (text.contains("\r\n\r\n")) {
            paragraphs = text.split("\\r\\n\\r\\n");
        } else {
            paragraphs = text.split("\\n\\n");
        }
        
        // 过滤空段落
        int count = 0;
        for (String paragraph : paragraphs) {
            if (!paragraph.trim().isEmpty()) {
                count++;
            }
        }
        
        return count;
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
                  
                // 检查英文内容分段情况
                boolean hasDoubleNewline = fullContent.contains("\n\n");
                boolean hasCarriageReturnDoubleNewline = fullContent.contains("\r\n\r\n");
                boolean hasParagraphs = hasDoubleNewline || hasCarriageReturnDoubleNewline;
                int paragraphCount = 0;
                if (hasParagraphs) {
                    paragraphCount = countParagraphs(fullContent);
                }
                log.info("英文内容分段检查 - 包含\n\n: {}, 包含\r\n\r\n: {}, 总段落数: {}", 
                    hasDoubleNewline, hasCarriageReturnDoubleNewline, paragraphCount);
                  
                // 英文内容分段详细分析
                if (hasParagraphs) {
                    // 分割英文内容为段落
                    String[] englishParagraphs;
                    if (hasCarriageReturnDoubleNewline) {
                        englishParagraphs = fullContent.split("\\r\\n\\r\\n");
                    } else {
                        englishParagraphs = fullContent.split("\\n\\n");
                    }
                    
                    // 过滤空段落
                    List<String> nonEmptyParagraphs = new ArrayList<>();
                    for (String paragraph : englishParagraphs) {
                        if (!paragraph.trim().isEmpty()) {
                            nonEmptyParagraphs.add(paragraph);
                        }
                    }
                    
                    log.info("英文内容分段详细信息 - 原始段落数: {}, 非空段落数: {}", 
                        englishParagraphs.length, nonEmptyParagraphs.size());
                    
                    // 记录前3个段落的长度信息
                    for (int i = 0; i < Math.min(3, nonEmptyParagraphs.size()); i++) {
                        log.debug("英文段落{}长度: {}字符", i + 1, nonEmptyParagraphs.get(i).length());
                    }
                }
                  
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
                // 使用DifficultyEvaluator评估文章难度
                String difficultyLevel = difficultyEvaluator.evaluateDifficulty(fullContent);
                article.setDifficultyLevel(difficultyLevel);
                log.info("文章ID: {}, 标题: {}, 评估难度等级: {}", article.getId(), article.getTitle(), difficultyLevel);
                article.setReadCount(0);
                article.setLikeCount(0);
                
                // 6. 插入数据库
                try {
                    int insertResult = articleMapper.insert(article);
                    if (insertResult > 0) {
                        savedCount++;
                        log.info("成功存储文章({}/{}): {} (ID: {})", savedCount, gnewsArticles.size(), title, article.getId());
                        log.info("存储的文章内容信息 - 长度: {}字符, 单词数: {}, 段落数: {}", 
                            fullContent.length(), article.getWordCount(), paragraphCount);
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
    
    @Override
    public long getTotalArticleCount() {
        return articleMapper.selectCount(null);
    }
    
    @Override
    public long getPublishedArticleCount() {
        // Article实体没有status字段，所有文章都视为已发布
        // 可以通过publishedAt字段判断是否有发布时间
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Article::getPublishedAt);
        return articleMapper.selectCount(wrapper);
    }
    
    @Override
    public long getNewArticlesTodayCount() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Article::getCreateTime, LocalDate.now().atStartOfDay());
        return articleMapper.selectCount(wrapper);
    }
    
    @Override
    public List<ArticleVO> searchArticlesByKeyword(String keyword, int limit) {
        try {
            log.info("开始搜索文章，关键词: {}, 数量限制: {}", keyword, limit);
            
            // 使用GnewsService搜索文章
            List<GnewsResponse.GnewsArticle> gnewsArticles = gnewsService.searchArticlesByKeyword(keyword, limit);
            
            if (gnewsArticles.isEmpty()) {
                log.warn("GNews API未返回任何文章，关键词: {}", keyword);
                return Collections.emptyList();
            }
            
            log.info("从GNews API获取到 {} 篇关于 '{}' 的文章", gnewsArticles.size(), keyword);
            
            // 转换并保存文章
            List<ArticleVO> articles = new ArrayList<>();
            for (GnewsResponse.GnewsArticle gnewsArticle : gnewsArticles) {
                try {
                    // 检查文章是否已存在
                    LambdaQueryWrapper<Article> existingWrapper = new LambdaQueryWrapper<>();
                    existingWrapper.eq(Article::getUrl, gnewsArticle.getUrl());
                    Article existingArticle = articleMapper.selectOne(existingWrapper);
                    
                    if (existingArticle != null) {
                        // 文章已存在，直接转换
                        ArticleVO vo = convertToArticleVO(existingArticle);
                        articles.add(vo);
                        continue;
                    }
                    
                    // 使用Readability4J获取文章全文
                    log.info("尝试使用Readability4J获取搜索文章全文: {}", gnewsArticle.getUrl());
                    Optional<String> fullContentOptional = scraperService.scrapeArticleContent(gnewsArticle.getUrl());
                    
                    if (fullContentOptional.isEmpty()) {
                        log.warn("未能获取搜索文章全文，跳过存储: {} - {}", gnewsArticle.getTitle(), gnewsArticle.getUrl());
                        continue;
                    }
                    
                    String fullContent = fullContentOptional.get();
                    log.info("成功获取搜索文章全文，长度: {} 字符", fullContent.length());
                    
                    // 创建新文章
                    Article article = new Article();
                    // 限制title长度，避免数据库字段超限
                    String title = gnewsArticle.getTitle();
                    if (title != null && title.length() > 200) {
                        title = title.substring(0, 197) + "...";
                    }
                    article.setTitle(title);
                    // 限制description长度，避免数据库字段超限
                    String description = gnewsArticle.getDescription();
                    if (description != null && description.length() > 500) {
                        description = description.substring(0, 497) + "...";
                    }
                    article.setDescription(description);
                    article.setUrl(gnewsArticle.getUrl());
                    article.setImage(gnewsArticle.getImage());
                    article.setSource(gnewsArticle.getSource().getName());
                    article.setPublishedAt(gnewsArticle.getPublishedAt());
                    article.setCategory(keyword); // 使用关键词作为分类
                    article.setContentEn(fullContent); // 使用Readability4J获取的全文
                    article.setContentCn(""); // 初始为空，后续通过AI服务填充
                    article.setWordCount(countWords(fullContent)); // 计算单词数
                    article.setDifficultyLevel(difficultyEvaluator.evaluateDifficulty(fullContent)); // 评估难度
                    article.setReadCount(0);
                    article.setCreateTime(LocalDateTime.now());
                    article.setUpdateTime(LocalDateTime.now());
                    
                    // 保存到数据库
                    articleMapper.insert(article);
                    
                    // 转换为VO
                    ArticleVO vo = convertToArticleVO(article);
                    articles.add(vo);
                    
                    log.info("成功保存自定义主题文章: {}", article.getTitle());
                    
                } catch (Exception e) {
                    log.error("处理文章时出错: {}", gnewsArticle.getTitle(), e);
                }
            }
            
            log.info("搜索完成，共处理 {} 篇文章", articles.size());
            return articles;
            
        } catch (Exception e) {
            log.error("搜索文章失败，关键词: {}", keyword, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 将Article实体转换为ArticleVO
     */
    private ArticleVO convertToArticleVO(Article article) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setDescription(article.getDescription());
        vo.setUrl(article.getUrl());
        vo.setImage(article.getImage());
        vo.setSource(article.getSource());
        vo.setPublishedAt(article.getPublishedAt());
        vo.setCategory(article.getCategory());
        vo.setContentEn(article.getContentEn());
        vo.setContentCn(article.getContentCn());
        vo.setWordCount(article.getWordCount());
        vo.setDifficultyLevel(article.getDifficultyLevel());
        vo.setReadCount(article.getReadCount());
        vo.setCreateTime(article.getCreateTime());
        return vo;
    }
    
    @Override
    public List<ArticleVO> searchArticlesByKeyword(String keyword, int limit, String language, 
            String country, String fromDate, String toDate, String sortBy) {
        try {
            log.info("开始增强搜索文章，关键词: {}, 语言: {}, 国家: {}, 时间范围: {} - {}, 排序: {}", 
                    keyword, language, country, fromDate, toDate, sortBy);
            
            // 使用GnewsService增强搜索
            List<GnewsResponse.GnewsArticle> gnewsArticles = gnewsService.searchArticlesByKeyword(
                    keyword, limit, language, country, fromDate, toDate, sortBy);
            
            if (gnewsArticles.isEmpty()) {
                log.warn("GNews API未返回任何文章，关键词: {}", keyword);
                return Collections.emptyList();
            }
            
            log.info("从GNews API获取到 {} 篇关于 '{}' 的文章", gnewsArticles.size(), keyword);
            
            // 转换并保存文章
            List<ArticleVO> articles = new ArrayList<>();
            int processedCount = 0;
            int existingCount = 0;
            int failedScrapeCount = 0;
            
            for (GnewsResponse.GnewsArticle gnewsArticle : gnewsArticles) {
                processedCount++;
                String url = gnewsArticle.getUrl();
                String title = gnewsArticle.getTitle();
                
                log.info("处理第{}篇增强搜索文章: {} - {}", processedCount, url, title);
                
                try {
                    // 检查文章是否已存在
                    LambdaQueryWrapper<Article> existingWrapper = new LambdaQueryWrapper<>();
                    existingWrapper.eq(Article::getUrl, gnewsArticle.getUrl());
                    Article existingArticle = articleMapper.selectOne(existingWrapper);
                    
                    if (existingArticle != null) {
                        // 文章已存在，直接转换
                        existingCount++;
                        ArticleVO vo = convertToArticleVO(existingArticle);
                        articles.add(vo);
                        log.info("增强搜索文章已存在，跳过({}/{}): {} - {}", processedCount, gnewsArticles.size(), title, url);
                        continue;
                    }
                    
                    // 使用Readability4J获取文章全文
                    log.info("尝试使用Readability4J获取增强搜索文章全文: {}", url);
                    Optional<String> fullContentOptional = scraperService.scrapeArticleContent(url);
                    
                    if (fullContentOptional.isEmpty()) {
                        failedScrapeCount++;
                        log.warn("未能获取增强搜索文章全文，跳过存储({}/{}): {} - {}", processedCount, gnewsArticles.size(), title, url);
                        continue;
                    }
                    
                    String fullContent = fullContentOptional.get();
                    log.info("成功获取增强搜索文章全文，长度: {} 字符", fullContent.length());
                    
                    // 创建新文章
                    Article article = new Article();
                    article.setTitle(gnewsArticle.getTitle());
                    article.setDescription(gnewsArticle.getDescription());
                    article.setUrl(gnewsArticle.getUrl());
                    article.setImage(gnewsArticle.getImage());
                    article.setSource(gnewsArticle.getSource().getName());
                    article.setPublishedAt(gnewsArticle.getPublishedAt());
                    article.setCategory(keyword); // 使用关键词作为分类
                    article.setContentEn(fullContent); // 使用Readability4J获取的全文
                    article.setContentCn(""); // 初始为空，后续通过AI服务填充
                    article.setWordCount(0); // 初始为0，后续通过AI服务计算
                    article.setDifficultyLevel(""); // 初始为空，后续通过AI服务评估
                    article.setReadCount(0);
                    article.setCreateTime(LocalDateTime.now());
                    article.setUpdateTime(LocalDateTime.now());
                    
                    // 保存到数据库
                    articleMapper.insert(article);
                    
                    // 转换为VO
                    ArticleVO vo = convertToArticleVO(article);
                    articles.add(vo);
                    
                    log.info("成功保存增强搜索文章: {}", article.getTitle());
                    
                } catch (Exception e) {
                    log.error("处理增强搜索文章时出错: {}", gnewsArticle.getTitle(), e);
                }
            }
            
            log.info("增强搜索完成，共处理 {} 篇文章，已存在 {} 篇，爬取失败 {} 篇", 
                    articles.size(), existingCount, failedScrapeCount);
            
            log.info("增强搜索完成，共处理 {} 篇文章", articles.size());
            return articles;
            
        } catch (Exception e) {
            log.error("增强搜索文章失败，关键词: {}", keyword, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<ArticleVO> getArticlesByCategory(String category, int limit, String language, 
            String country, String fromDate, String toDate, String sortBy) {
        try {
            log.info("开始增强分类文章，分类: {}, 语言: {}, 国家: {}, 时间范围: {} - {}, 排序: {}", 
                    category, language, country, fromDate, toDate, sortBy);
            
            // 使用GnewsService增强分类获取
            List<GnewsResponse.GnewsArticle> gnewsArticles = gnewsService.fetchArticlesByCategory(
                    category, limit, language, country, fromDate, toDate, sortBy);
            
            if (gnewsArticles.isEmpty()) {
                log.warn("GNews API未返回任何文章，分类: {}", category);
                return Collections.emptyList();
            }
            
            log.info("从GNews API获取到 {} 篇 {} 分类文章", gnewsArticles.size(), category);
            
            // 转换并保存文章
            List<ArticleVO> articles = new ArrayList<>();
            int processedCount = 0;
            int existingCount = 0;
            int failedScrapeCount = 0;
            
            for (GnewsResponse.GnewsArticle gnewsArticle : gnewsArticles) {
                processedCount++;
                String url = gnewsArticle.getUrl();
                String title = gnewsArticle.getTitle();
                
                log.info("处理第{}篇增强分类文章: {} - {}", processedCount, url, title);
                
                try {
                    // 检查文章是否已存在
                    LambdaQueryWrapper<Article> existingWrapper = new LambdaQueryWrapper<>();
                    existingWrapper.eq(Article::getUrl, gnewsArticle.getUrl());
                    Article existingArticle = articleMapper.selectOne(existingWrapper);
                    
                    if (existingArticle != null) {
                        // 文章已存在，直接转换
                        existingCount++;
                        ArticleVO vo = convertToArticleVO(existingArticle);
                        articles.add(vo);
                        log.info("增强分类文章已存在，跳过({}/{}): {} - {}", processedCount, gnewsArticles.size(), title, url);
                        continue;
                    }
                    
                    // 使用Readability4J获取文章全文
                    log.info("尝试使用Readability4J获取增强分类文章全文: {}", url);
                    Optional<String> fullContentOptional = scraperService.scrapeArticleContent(url);
                    
                    if (fullContentOptional.isEmpty()) {
                        failedScrapeCount++;
                        log.warn("未能获取增强分类文章全文，跳过存储({}/{}): {} - {}", processedCount, gnewsArticles.size(), title, url);
                        continue;
                    }
                    
                    String fullContent = fullContentOptional.get();
                    log.info("成功获取增强分类文章全文，长度: {} 字符", fullContent.length());
                    
                    // 创建新文章
                    Article article = new Article();
                    article.setTitle(gnewsArticle.getTitle());
                    article.setDescription(gnewsArticle.getDescription());
                    article.setUrl(gnewsArticle.getUrl());
                    article.setImage(gnewsArticle.getImage());
                    article.setSource(gnewsArticle.getSource().getName());
                    article.setPublishedAt(gnewsArticle.getPublishedAt());
                    article.setCategory(category); // 使用实际分类
                    article.setContentEn(fullContent); // 使用Readability4J获取的全文
                    article.setContentCn(""); // 初始为空，后续通过AI服务填充
                    article.setWordCount(0); // 初始为0，后续通过AI服务计算
                    article.setDifficultyLevel(""); // 初始为空，后续通过AI服务评估
                    article.setReadCount(0);
                    article.setCreateTime(LocalDateTime.now());
                    article.setUpdateTime(LocalDateTime.now());
                    
                    // 保存到数据库
                    articleMapper.insert(article);
                    
                    // 转换为VO
                    ArticleVO vo = convertToArticleVO(article);
                    articles.add(vo);
                    
                    log.info("成功保存增强分类文章: {}", article.getTitle());
                    
                } catch (Exception e) {
                    log.error("处理增强分类文章时出错: {}", gnewsArticle.getTitle(), e);
                }
            }
            
            log.info("增强分类文章完成，共处理 {} 篇文章，已存在 {} 篇，爬取失败 {} 篇", 
                    articles.size(), existingCount, failedScrapeCount);
            
            log.info("增强分类文章完成，共处理 {} 篇文章", articles.size());
            return articles;
            
        } catch (Exception e) {
            log.error("增强分类文章失败，分类: {}", category, e);
            return Collections.emptyList();
        }
    }
}
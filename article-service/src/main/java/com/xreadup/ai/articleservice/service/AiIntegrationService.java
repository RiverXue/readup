package com.xreadup.ai.articleservice.service;

import com.xreadup.ai.articleservice.client.AiServiceClient;
import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisRequest;
import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisResponse;
import com.xreadup.ai.articleservice.model.entity.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI集成服务
 * 负责文章服务与AI服务的集成调用
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiIntegrationService {

    private final AiServiceClient aiServiceClient;

    /**
     * 分析文章内容并获取AI分析结果
     * @param article 文章实体
     * @return AI分析结果
     */
    public ArticleAnalysisResponse analyzeArticle(Article article) {
        try {
            ArticleAnalysisRequest request = buildAnalysisRequest(article);
            log.info("开始分析文章: {} (ID: {})", article.getTitle(), article.getId());
            
            // 根据文章长度选择合适的分析策略
            if (article.getWordCount() > 2000) {
                // 长文章使用分段分析
                log.info("文章较长({}字)，使用分段分析", article.getWordCount());
                return aiServiceClient.chunkedAnalyze(request);
            } else if (article.getWordCount() > 1000) {
                // 中等长度文章使用快速分析
                log.info("文章中等长度({}字)，使用快速分析", article.getWordCount());
                return aiServiceClient.quickAnalyze(request);
            } else {
                // 短文章使用完整分析
                log.info("文章较短({}字)，使用完整分析", article.getWordCount());
                return aiServiceClient.analyzeArticle(request);
            }
        } catch (Exception e) {
            log.error("AI分析文章失败: {} (ID: {})", article.getTitle(), article.getId(), e);
            return createFallbackResponse();
        }
    }

    /**
     * 构建文章分析请求
     * @param article 文章实体
     * @return 分析请求对象
     */
    private ArticleAnalysisRequest buildAnalysisRequest(Article article) {
        return new ArticleAnalysisRequest(
            article.getTitle(),
            article.getContentEn(),
            article.getCategory(),
            article.getWordCount()
        );
    }

    /**
     * 创建降级响应，当AI服务不可用时使用
     * @return 默认分析结果
     */
    private ArticleAnalysisResponse createFallbackResponse() {
        ArticleAnalysisResponse response = new ArticleAnalysisResponse();
        response.setDifficultyLevel("B2");
        response.setSummary("AI分析暂时不可用，请稍后重试");
        response.setChineseTranslation("翻译服务暂时不可用");
        response.setSimplifiedContent("Analysis temporarily unavailable");
        response.setReadabilityScore(70.0);
        return response;
    }
}
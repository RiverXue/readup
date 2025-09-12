package com.xreadup.ai.articleservice.service;

import com.xreadup.ai.articleservice.client.AiServiceClient;
import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisRequest;
import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisResponse;
import com.xreadup.ai.articleservice.model.entity.Article;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI集成服务 - 重构版
 * 适配新的统一API接口，移除冗余调用
 * 
 * @version 3.0.0
 */
@Service
@Slf4j
public class AiIntegrationService {

    @Autowired
    private AiServiceClient aiServiceClient;

    /**
     * 【统一分析】智能选择分析策略
     * 自动根据文章长度选择最佳分析方式
     */
    public ArticleAnalysisResponse analyzeArticle(Article article, boolean save, boolean forceRegenerate) {
        log.info("开始分析文章: {}, 保存: {}, 强制重新生成: {}", 
                article.getId(), save, forceRegenerate);
        
        ArticleAnalysisRequest request = buildAnalysisRequest(article);
        
        return aiServiceClient.analyzeArticle(request, save, forceRegenerate);
    }

    /**
     * 【传统模式】仅分析不保存
     */
    public ArticleAnalysisResponse analyzeArticle(Article article) {
        return analyzeArticle(article, false, false);
    }

    /**
     * 【增强模式】分析并保存
     */
    public ArticleAnalysisResponse analyzeAndSaveArticle(Article article) {
        return analyzeArticle(article, true, false);
    }

    /**
     * 【强制模式】强制重新分析并保存
     */
    public ArticleAnalysisResponse forceRegenerateAnalysis(Article article) {
        return analyzeArticle(article, true, true);
    }

    /**
     * 检查文章分析状态
     */
    public boolean isArticleAnalyzed(Long articleId) {
        try {
            var status = aiServiceClient.checkAnalysisStatus(articleId);
            return Boolean.TRUE.equals(status.get("analyzed"));
        } catch (Exception e) {
            log.error("检查文章分析状态失败: {}", articleId, e);
            return false;
        }
    }

    /**
     * 获取已保存的分析结果
     */
    public ArticleAnalysisResponse getAnalysisResult(Long articleId) {
        try {
            return aiServiceClient.getAnalysisResult(articleId);
        } catch (Exception e) {
            log.error("获取分析结果失败: {}", articleId, e);
            return null;
        }
    }

    /**
     * 删除已保存的分析结果
     */
    public boolean deleteAnalysisResult(Long articleId) {
        try {
            var result = aiServiceClient.deleteAnalysisResult(articleId);
            return Boolean.TRUE.equals(result.get("deleted"));
        } catch (Exception e) {
            log.error("删除分析结果失败: {}", articleId, e);
            return false;
        }
    }

    /**
     * 构建分析请求
     */
    private ArticleAnalysisRequest buildAnalysisRequest(Article article) {
        return new ArticleAnalysisRequest(
            article.getId(),
            article.getTitle(),
            article.getContentEn(),
            article.getCategory(),
            article.getContentEn() != null ? article.getContentEn().length() : 0
        );
    }
}
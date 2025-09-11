package com.xreadup.ai.articleservice.client;

import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisRequest;
import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI服务Feign客户端
 * 用于文章服务调用AI服务的文章分析接口
 */
@FeignClient(name = "ai-service", path = "/api/ai")
public interface AiServiceClient {

    /**
     * 文章全面分析
     * @param request 文章分析请求
     * @return AI分析结果
     */
    @PostMapping("/analyze")
    ArticleAnalysisResponse analyzeArticle(@RequestBody ArticleAnalysisRequest request);

    /**
     * 快速文章分析（节省70% token）
     * @param request 文章分析请求
     * @return AI分析结果
     */
    @PostMapping("/quick-analyze")
    ArticleAnalysisResponse quickAnalyze(@RequestBody ArticleAnalysisRequest request);

    /**
     * 分段文章分析（节省65% token）
     * @param request 文章分析请求
     * @return AI分析结果
     */
    @PostMapping("/chunked-analyze")
    ArticleAnalysisResponse chunkedAnalyze(@RequestBody ArticleAnalysisRequest request);
}
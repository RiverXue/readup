package com.xreadup.ai.articleservice.client;

import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisRequest;
import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI服务Feign客户端 - 重构版
 * 从9个API精简为3个核心API，消除冗余
 * 
 * @version 3.0.0
 */
@FeignClient(name = "ai-service", path = "/api/ai")
public interface AiServiceClient {

    /**
     * 【统一分析API】智能选择分析策略
     * 整合：深度学习、快速分析、分段分析
     * 
     * @param request 文章分析请求
     * @param save 是否保存分析结果到数据库
     * @param forceRegenerate 是否强制重新生成
     * @return AI分析结果
     */
    @PostMapping("/analyze")
    ArticleAnalysisResponse analyzeArticle(
            @RequestBody ArticleAnalysisRequest request,
            @RequestParam(value = "save", defaultValue = "false") boolean save,
            @RequestParam(value = "forceRegenerate", defaultValue = "false") boolean forceRegenerate);

    /**
     * 【状态检查API】检查文章分析状态
     * 
     * @param articleId 文章ID
     * @return 分析状态信息
     */
    @GetMapping("/status/{articleId}")
    Map<String, Object> checkAnalysisStatus(@PathVariable("articleId") Long articleId);

    /**
     * 【结果获取API】获取已保存的分析结果
     * 
     * @param articleId 文章ID
     * @return 文章分析结果
     */
    @GetMapping("/result/{articleId}")
    ArticleAnalysisResponse getAnalysisResult(@PathVariable("articleId") Long articleId);

    /**
     * 【结果删除API】删除已保存的分析结果
     * 
     * @param articleId 文章ID
     * @return 删除结果信息
     */
    @DeleteMapping("/result/{articleId}")
    Map<String, Object> deleteAnalysisResult(@PathVariable("articleId") Long articleId);
}
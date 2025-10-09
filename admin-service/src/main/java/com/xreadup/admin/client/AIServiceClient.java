package com.xreadup.admin.client;

import com.xreadup.admin.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * AI服务Feign客户端
 * 用于调用ai-service提供的API接口
 */
@FeignClient(name = "ai-service")
public interface AIServiceClient {

    /**
     * 获取AI分析结果
     * @param articleId 文章ID
     * @return AI分析结果
     */
    @GetMapping("/api/ai/smart/analyze")
    ApiResponse<AIAnalysisDTO> analyzeArticle(@RequestParam("articleId") Long articleId);

    /**
     * 获取AI助手响应
     * @param userId 用户ID
     * @param query 查询内容
     * @param articleId 文章ID（可选）
     * @return AI助手响应
     */
    @GetMapping("/assistant")
    ApiResponse<AssistantResponseDTO> getAssistantResponse(
            @RequestParam("userId") Long userId,
            @RequestParam("query") String query,
            @RequestParam(required = false) Long articleId);

    /**
     * 获取AI服务状态
     * @return 服务状态
     */
    @GetMapping("/api/ai/health")
    ApiResponse<String> healthCheck();

    /**
     * AI分析结果DTO接口定义
     */
    interface AIAnalysisDTO {
        Long getId();
        Long getArticleId();
        String getTitle();
        String getDifficultyLevel();
        Double getReadabilityScore();
        String getCreatedAt();
        String getUpdatedAt();
    }

    /**
     * AI助手响应DTO接口定义
     */
    interface AssistantResponseDTO {
        String getResponse();
        String getType();
        String getCreatedAt();
    }
}
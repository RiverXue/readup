package com.xreadup.ai.articleservice.controller;

import com.xreadup.ai.articleservice.client.AiServiceClient;
import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisRequest;
import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisResponse;
import com.xreadup.ai.articleservice.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@Tag(name = "AI集成测试", description = "测试AI服务调用的API")
@RequiredArgsConstructor
public class AiIntegrationTestController {

    private final AiServiceClient aiServiceClient;

    @PostMapping("/ai/quick")
    @Operation(summary = "测试快速AI分析", description = "测试调用AI服务的快速分析接口")
    public ApiResponse<Object> testQuickAnalysis(@RequestBody String content) {
        try {
            ArticleAnalysisRequest request = new ArticleAnalysisRequest();
            request.setTitle("测试文章");
            request.setContent(content);
            request.setCategory("测试");
            request.setWordCount(content.split("\\s+").length);

            ArticleAnalysisResponse response = aiServiceClient.quickAnalyze(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(500, "AI服务调用失败: " + e.getMessage());
        }
    }

    @PostMapping("/ai/full")
    @Operation(summary = "测试完整AI分析", description = "测试调用AI服务的完整分析接口")
    public ApiResponse<Object> testFullAnalysis(@RequestBody String content) {
        try {
            ArticleAnalysisRequest request = new ArticleAnalysisRequest();
            request.setTitle("测试文章");
            request.setContent(content);
            request.setCategory("测试");
            request.setWordCount(content.split("\\s+").length);

            ArticleAnalysisResponse response = aiServiceClient.analyzeArticle(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(500, "AI服务调用失败: " + e.getMessage());
        }
    }

    @GetMapping("/ai/health")
    @Operation(summary = "检查AI服务健康状态", description = "检查AI服务是否可用")
    public ApiResponse<Object> checkAiHealth() {
        try {
            // 使用一个简单的请求测试AI服务连通性
            ArticleAnalysisRequest request = new ArticleAnalysisRequest();
            request.setTitle("Health Check");
            request.setContent("This is a simple health check request.");
            request.setCategory("Health");
            request.setWordCount(6);

            ArticleAnalysisResponse response = aiServiceClient.quickAnalyze(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(503, "AI服务不可用: " + e.getMessage());
        }
    }
}
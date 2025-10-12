package com.xreadup.admin.client;

import com.xreadup.admin.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

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
     * 获取AI分析结果列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param articleTitle 文章标题搜索关键字
     * @param analysisType 分析类型
     * @param status 分析状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return AI分析结果列表
     */
    @GetMapping("/api/ai/analysis")
    ApiResponse<Map<String, Object>> getAIAnalysisList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String articleTitle,
            @RequestParam(required = false) String analysisType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate);

    /**
     * 获取AI分析详情
     * @param analysisId 分析ID
     * @return AI分析详情
     */
    @GetMapping("/api/ai/analysis/{analysisId}")
    ApiResponse<Map<String, Object>> getAIAnalysisDetail(@PathVariable("analysisId") Long analysisId);

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

    /**
     * AI分析结果列表DTO接口定义
     */
    interface AIAnalysisListDTO {
        List<AIAnalysisDTO> getAnalysisList();
        Integer getTotal();
        Integer getPage();
        Integer getPageSize();
    }

    /**
     * AI分析详情DTO接口定义 - 与init.sql中的ai_analysis表字段完全一致
     */
    interface AIAnalysisDetailDTO {
        Long getId();
        Long getArticleId();
        String getTitle();
        String getDifficultyLevel();
        String getKeywords(); // JSON格式存储
        String getSummary();
        String getChineseTranslation();
        String getSimplifiedContent();
        String getKeyPhrases(); // JSON格式存储
        Double getReadabilityScore();
        String getWordTranslations();
        String getSentenceParseResults();
        String getQuizQuestions();
        String getLearningTips();
        String getAnalysisMetadata();
        String getLastAnalysisType();
        String getCreatedAt();
        String getUpdatedAt();
    }
}
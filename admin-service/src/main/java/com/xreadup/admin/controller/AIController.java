package com.xreadup.admin.controller;

import com.xreadup.admin.client.AIServiceClient;
import com.xreadup.admin.client.AIServiceClient.AssistantResponseDTO;
import com.xreadup.admin.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * AI服务管理控制器
 * 提供后台管理系统对AI分析和学习助手功能的管理
 */
@RestController
@RequestMapping("/api/admin/ai")
@Tag(name = "AI服务管理", description = "AI分析和学习助手相关的后台管理API")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Autowired
    private AIServiceClient aiServiceClient;

    /**
     * 分析文章内容
     * @param articleId 文章ID
     * @param content 文章内容
     * @param difficulty 目标难度
     * @return 分析结果
     */
    @PostMapping("/analyze")
    @Operation(summary = "分析文章内容", description = "使用AI分析文章内容并返回分析结果")
    public ApiResponse<String> analyzeArticle(
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String difficulty) {
        try {
            logger.info("分析文章内容，articleId: {}, difficulty: {}", articleId, difficulty);
            
            // 由于AIServiceClient接口只接受articleId参数，我们只传递这个参数
            ApiResponse<AIServiceClient.AIAnalysisDTO> response = aiServiceClient.analyzeArticle(articleId);
            
            // 转换为符合当前API返回类型的结果
            if (response != null && response.isSuccess() && response.getData() != null) {
                return ApiResponse.success("文章分析成功");
            } else {
                return ApiResponse.fail(500, "分析文章内容失败");
            }
        } catch (Exception e) {
            logger.error("分析文章内容失败", e);
            return ApiResponse.fail(500, "分析文章内容失败");
        }
    }

    /**
     * 获取AI学习助手回答
     * @param question 用户问题
     * @param context 上下文信息
     * @return AI助手回答
     */
    @PostMapping("/assistant")
    @Operation(summary = "获取AI学习助手回答", description = "向AI学习助手提问并获取回答")
    public ApiResponse<AssistantResponseDTO> getAssistantResponse(
            @RequestParam @NotNull(message = "问题不能为空") String question,
            @RequestParam(required = false) String context) {
        try {
            logger.info("获取AI学习助手回答");
            
            // 为了符合AIServiceClient接口，我们需要传递userId和query参数
            // 这里使用默认的userId=0作为示例
            Long userId = 0L;
            return aiServiceClient.getAssistantResponse(userId, question, null);
        } catch (Exception e) {
            logger.error("获取AI学习助手回答失败", e);
            return ApiResponse.fail(500, "获取支持的难度等级列表失败");
        }
    }

    /**
     * 检查AI服务状态
     * @return AI服务状态
     */
    @GetMapping("/health")
    @Operation(summary = "检查AI服务状态", description = "检查AI服务是否正常运行")
    public ApiResponse<String> healthCheck() {
        try {
            logger.info("检查AI服务状态");
            return aiServiceClient.healthCheck();
        } catch (Exception e) {
            logger.error("检查AI服务状态失败", e);
            return ApiResponse.fail(500, "AI服务可能不可用");
        }
    }
}
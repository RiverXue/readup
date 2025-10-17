package com.xreadup.ai.controller;

import com.xreadup.ai.model.dto.*;
import com.xreadup.ai.service.EnhancedAiAnalysisService;
import com.xreadup.ai.service.AiConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * DeepSeek AI控制器 - 进阶AI功能
 * 
 * 作为双引擎策略中的进阶AI引擎，提供智能分析服务
 * 主要处理：
 * 1. AI摘要（DeepSeek）
 * 2. 长句解析（DeepSeek）
 * 3. 读后测验（DeepSeek，Structured Outputs）
 * 4. 学习建议（DeepSeek）
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@Tag(name = "DeepSeek AI", description = "进阶AI分析引擎API")
@RequiredArgsConstructor
public class DeepSeekController {

    private final EnhancedAiAnalysisService enhancedAiAnalysisService;
    private final AiConfigService aiConfigService;

    /**
     * AI摘要（DeepSeek）
     * 对文章进行智能摘要提取并保存到数据库
     */
    @PostMapping("/summary")
    @Operation(summary = "AI文章摘要", description = "使用DeepSeek对文章进行智能摘要并持久化存储")
    public ApiResponse<String> generateSummary(@RequestBody AiSummaryRequest request) {
        try {
            // 检查AI摘要生成功能是否启用
            if (!aiConfigService.isAiSummaryGenerationEnabled()) {
                log.warn("AI摘要生成功能已禁用，拒绝摘要请求: {}", request.getArticleId());
                return ApiResponse.error("AI摘要生成功能当前不可用");
            }
            
            // 检查系统维护模式
            if (aiConfigService.isMaintenanceMode()) {
                log.warn("系统处于维护模式，拒绝摘要请求: {}", request.getArticleId());
                return ApiResponse.error("系统正在维护中，请稍后重试");
            }
            
            if (request == null) {
                log.error("摘要请求为空");
                return ApiResponse.error("摘要请求不能为空");
            }
            
            if (request.getArticleId() == null) {
                log.error("文章ID为空");
                return ApiResponse.error("文章ID不能为空");
            }
            
            if (request.getText() == null || request.getText().trim().isEmpty()) {
                log.error("摘要文本内容为空");
                return ApiResponse.error("摘要文本内容不能为空");
            }
            
            log.info("开始生成文章摘要: 文章ID={}", request.getArticleId());
            String summary = enhancedAiAnalysisService.generateSummary(request.getText());
            
            // 保存摘要到数据库
            enhancedAiAnalysisService.saveDeepSeekSummary(request.getArticleId(), summary);
            
            log.info("文章摘要完成并保存: 摘要长度={}字符", summary.length());
            return ApiResponse.success(summary);
        } catch (Exception e) {
            String articleId = (request != null && request.getArticleId() != null) ? request.getArticleId().toString() : "null";
            log.error("生成文章摘要失败: {}", articleId, e);
            return ApiResponse.error("生成摘要失败: " + e.getMessage());
        }
    }

    /**
     * 长句解析（DeepSeek）
     * 对复杂句子进行语法和语义分析并保存到数据库
     * 支持共享缓存机制：相同句子的解析结果在不同用户间共享
     */
    @PostMapping("/parse")
    @Operation(summary = "长句解析", description = "使用DeepSeek对复杂句子进行语法和语义分析，支持缓存共享")
    public ApiResponse<SentenceParseResponse> parseSentence(@RequestBody SentenceParseRequest request) {
        try {
            // 检查AI句子解析功能是否启用
            if (!aiConfigService.isAiSentenceParsingEnabled()) {
                log.warn("AI句子解析功能已禁用，拒绝解析请求");
                return ApiResponse.error("AI句子解析功能当前不可用");
            }
            
            // 检查系统维护模式
            if (aiConfigService.isMaintenanceMode()) {
                log.warn("系统处于维护模式，拒绝解析请求");
                return ApiResponse.error("系统正在维护中，请稍后重试");
            }
            
            log.info("开始句子解析: 句子长度={}字符", request.getSentence().length());
            
            // 使用带缓存的句子解析方法，传递来源文章ID
            SentenceParseResponse response = enhancedAiAnalysisService.parseSentenceWithCache(request.getSentence(), request.getArticleId());
            
            log.info("句子解析完成: 语法分析使用了缓存共享机制");
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("句子解析失败", e);
            return ApiResponse.error("句子解析失败: " + e.getMessage());
        }
    }

    // ===== 以下方法已删除（未使用） =====
    // - generateQuiz() - 前端使用 assistantGenerateQuiz() 代替
    // - generateLearningTip() - 未使用
    // - comprehensiveAnalysis() - 未使用
}
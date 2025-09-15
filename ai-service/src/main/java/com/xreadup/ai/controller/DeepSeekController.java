package com.xreadup.ai.controller;

import com.xreadup.ai.model.dto.*;
import com.xreadup.ai.service.EnhancedAiAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * AI摘要（DeepSeek）
     * 对文章进行智能摘要提取并保存到数据库
     */
    @PostMapping("/summary")
    @Operation(summary = "AI文章摘要", description = "使用DeepSeek对文章进行智能摘要并持久化存储")
    public ApiResponse<String> generateSummary(@RequestBody AiSummaryRequest request) {
        try {
            log.info("开始生成文章摘要: 文章ID={}", request.getArticleId());
            String summary = enhancedAiAnalysisService.generateSummary(request.getText());
            
            // 保存摘要到数据库
            enhancedAiAnalysisService.saveDeepSeekSummary(request.getArticleId(), summary);
            
            log.info("文章摘要完成并保存: 摘要长度={}字符", summary.length());
            return ApiResponse.success(summary);
        } catch (Exception e) {
            log.error("生成文章摘要失败: {}", request.getArticleId(), e);
            return ApiResponse.error("生成摘要失败: " + e.getMessage());
        }
    }

    /**
     * 长句解析（DeepSeek）
     * 对复杂句子进行语法和语义分析并保存到数据库
     */
    @PostMapping("/parse")
    @Operation(summary = "长句解析", description = "使用DeepSeek对复杂句子进行语法和语义分析并持久化存储")
    public ApiResponse<SentenceParseResponse> parseSentence(@RequestBody SentenceParseRequest request) {
        try {
            log.info("开始句子解析: 句子长度={}字符", request.getSentence().length());
            SentenceParseResponse response = enhancedAiAnalysisService.parseSentence(request.getSentence());
            
            // 保存解析结果到数据库
            enhancedAiAnalysisService.saveSentenceParseResults(request.getArticleId(), response);
            
            log.info("句子解析完成并保存: 语法分析完成");
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("句子解析失败", e);
            return ApiResponse.error("句子解析失败: " + e.getMessage());
        }
    }

    /**
     * 生成测验题（DeepSeek）
     * 基于文章内容生成阅读理解测验题并保存到数据库
     */
    @PostMapping("/quiz")
    @Operation(summary = "生成测验题", description = "基于文章内容生成阅读理解测验题并持久化存储")
    public ApiResponse<List<QuizQuestion>> generateQuiz(@RequestBody QuizGenerationRequest request) {
        try {
            log.info("开始生成测验题: 文章ID={}", request.getArticleId());
            List<QuizQuestion> questions = enhancedAiAnalysisService.generateQuiz(request.getText(), request.getQuestionCount());
            
            // 保存测验题到数据库
            enhancedAiAnalysisService.saveQuizQuestions(request.getArticleId(), questions);
            
            log.info("测验题生成完成并保存: 生成题目数量={}", questions.size());
            return ApiResponse.success(questions);
        } catch (Exception e) {
            log.error("生成测验题失败: {}", request.getArticleId(), e);
            return ApiResponse.error("生成测验题失败: " + e.getMessage());
        }
    }

    /**
     * 学习建议（DeepSeek）
     * 基于用户学习数据生成个性化学习建议并保存到数据库
     */
    @PostMapping("/tip")
    @Operation(summary = "学习建议", description = "基于用户学习数据生成个性化学习建议并持久化存储")
    public ApiResponse<String> generateLearningTip(@RequestBody LearningTipRequest request) {
        try {
            log.info("开始生成学习建议: 用户ID={}", request.getUserId());
            String tip = enhancedAiAnalysisService.generateLearningTip(
                request.getArticleCount(), 
                request.getWordCount(),
                request.getLearningDays()
            );
            
            // 保存学习建议到数据库（关联到文章ID）
            enhancedAiAnalysisService.saveLearningTip(request.getArticleId(), request.getUserId(), tip);
            
            log.info("学习建议生成完成并保存: 建议长度={}字符", tip.length());
            return ApiResponse.success(tip);
        } catch (Exception e) {
            log.error("生成学习建议失败: {}", request.getUserId(), e);
            return ApiResponse.error("生成学习建议失败: " + e.getMessage());
        }
    }

    /**
     * 综合AI分析（DeepSeek）
     * 提供全面的文章分析和学习建议并保存到数据库
     */
    @PostMapping("/comprehensive")
    @Operation(summary = "综合AI分析", description = "提供全面的文章分析和学习建议并持久化存储所有结果")
    public ApiResponse<ComprehensiveAnalysisResponse> comprehensiveAnalysis(@RequestBody ComprehensiveAnalysisRequest request) {
        try {
            log.info("开始综合AI分析: 文章ID={}", request.getArticleId());
            ComprehensiveAnalysisResponse response = enhancedAiAnalysisService.comprehensiveAnalysis(request);
            
            // 保存综合分析结果到数据库
            enhancedAiAnalysisService.saveComprehensiveAnalysis(request.getArticleId(), request.getUserId(), response);
            
            log.info("综合AI分析完成并保存: 文章ID={}", request.getArticleId());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("综合AI分析失败: {}", request.getArticleId(), e);
            return ApiResponse.error("综合AI分析失败: " + e.getMessage());
        }
    }
}
package com.xreadup.ai.controller;

import com.xreadup.ai.model.dto.*;
import com.xreadup.ai.service.AiAnalysisService;
import com.xreadup.ai.service.EnhancedAiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI分析服务控制器
 * 
 * 提供文章AI分析相关API接口
 * 所有接口都保存分析结果到数据库
 * 
 * @author xreadup
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiAnalysisService aiAnalysisService;
    private final EnhancedAiAnalysisService enhancedAiAnalysisService;

    /**
     * 文章AI分析 - 保存分析结果
     * 对单篇文章进行AI分析并保存结果到数据库
     * 
     * @param request 分析请求
     * @return 分析结果
     */
    @PostMapping("/analyze")
    public ApiResponse<ArticleAnalysisResponse> analyzeArticle(@RequestBody ArticleAnalysisRequest request) {
        try {
            log.info("开始分析文章: {}", request.getArticleId());
            ArticleAnalysisResponse response = enhancedAiAnalysisService.analyzeAndSaveArticle(request, false);
            log.info("文章分析完成: {}", request.getArticleId());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("文章分析失败: {}", request.getArticleId(), e);
            return ApiResponse.error("文章分析失败: " + e.getMessage());
        }
    }

    /**
     * 批量文章AI分析 - 保存分析结果
     * 对多篇文章进行AI分析并保存结果到数据库
     * 
     * @param request 批量分析请求
     * @return 分析结果列表
     */
    @PostMapping("/analyze/batch")
    public ApiResponse<List<ArticleAnalysisResponse>> batchAnalyzeArticles(@RequestBody BatchAiAnalysisRequest request) {
        try {
            log.info("开始批量分析文章: {} 篇", request.getArticleIds().size());
            List<ArticleAnalysisResponse> responses = enhancedAiAnalysisService.batchAnalyzeArticles(request);
            log.info("批量文章分析完成: {} 篇", responses.size());
            return ApiResponse.success(responses);
        } catch (Exception e) {
            log.error("批量文章分析失败", e);
            return ApiResponse.error("批量文章分析失败: " + e.getMessage());
        }
    }

    /**
     * 全文翻译 - 保存翻译结果
     * 将英文内容完整翻译成中文并保存到数据库
     * 
     * @param request 翻译请求
     * @return 翻译结果
     */
    @PostMapping("/translate/fulltext")
    public ApiResponse<TranslationResponse> translateFullText(@RequestBody TranslationRequest request) {
        try {
            log.info("开始全文翻译: {} 字符", request.getContent().length());
            String translation = aiAnalysisService.translateFullText(request.getContent());
            
            // 保存翻译结果
            TranslationResponse response = new TranslationResponse();
            response.setOriginalText(request.getContent());
            response.setTranslatedText(translation);
            response.setLanguage("zh-CN");
            response.setArticleId(request.getArticleId());
            
            enhancedAiAnalysisService.saveTranslation(request.getArticleId(), translation);
            
            log.info("全文翻译完成: {} 字符", translation.length());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("全文翻译失败", e);
            return ApiResponse.error("全文翻译失败: " + e.getMessage());
        }
    }

    /**
     * 选词翻译 - 保存翻译结果
     * 提供单词的详细翻译和解释，并保存到数据库
     * 
     * @param request 选词翻译请求
     * @return 详细翻译信息
     */
    @PostMapping("/translate/word")
    public ApiResponse<WordTranslationResponse> translateWord(@RequestBody WordTranslationRequest request) {
        try {
            log.info("开始选词翻译: {} - 文章: {}", request.getWord(), request.getArticleId());
            WordTranslationResponse response = aiAnalysisService.translateWord(request);
            
            // 保存选词翻译结果
            enhancedAiAnalysisService.saveWordTranslation(request, response);
            
            log.info("选词翻译完成: {} -> {}", request.getWord(), response.getChinese());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("选词翻译失败", e);
            return ApiResponse.error("选词翻译失败: " + e.getMessage());
        }
    }

    // 删除获取和删除分析结果API - 产品经理决策：低频使用，无商业价值

    /**
     * 健康检查
     * 检查AI服务状态
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("AI服务运行正常");
    }
}
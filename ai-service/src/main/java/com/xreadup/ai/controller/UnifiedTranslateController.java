package com.xreadup.ai.controller;

import com.xreadup.ai.model.dto.TencentTranslateRequestDTO;
import com.xreadup.ai.model.dto.TencentTranslateResponseDTO;
import com.xreadup.ai.model.dto.ApiResponse;
import com.xreadup.ai.service.TencentTranslateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * 统一智能翻译控制器 - 产品经理优化版
 * 
 * 合并腾讯云翻译所有API为1个智能翻译接口，基于用户场景设计：
 * 1. 智能语言检测（可选）
 * 2. 一键翻译（中英文自动识别）
 * 3. 批量翻译（保持原有功能）
 * 
 * 产品经理决策：简化用户认知，从5个API合并为1个核心API
 * 
 * @author XReadUp Product Team
 * @version 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/translate")
@Tag(name = "智能翻译", description = "统一智能翻译API - 产品经理优化版")
@RequiredArgsConstructor
public class UnifiedTranslateController {

    private final TencentTranslateService tencentTranslateService;

    /**
     * 智能翻译 - 统一入口
     * 
     * 产品经理优化：
     * - 合并原5个翻译API为1个智能接口
     * - 自动语言检测，用户无需指定语言
     * - 支持一键翻译和批量翻译
     * - 保持腾讯云翻译引擎的高性能
     */
    @PostMapping("/smart")
    @Operation(summary = "智能翻译", description = "一键智能翻译，自动识别语言，支持批量翻译")
    public ApiResponse<TencentTranslateResponseDTO> smartTranslate(@Valid @RequestBody SmartTranslateRequest request) {
        long startTime = Instant.now().toEpochMilli();
        
        try {
            // 智能语言检测
            String detectedSourceLang = detectLanguage(request.getText());
            String targetLang = determineTargetLanguage(detectedSourceLang, request.getTargetLang());
            
            log.info("智能翻译请求: {} -> {}, 文本长度: {}", detectedSourceLang, targetLang, request.getText().length());
            
            // 执行翻译
            String translatedText = tencentTranslateService.translateText(
                request.getText(), 
                detectedSourceLang, 
                targetLang
            );
            
            TencentTranslateResponseDTO response = new TencentTranslateResponseDTO();
            response.setTranslatedText(translatedText);
            response.setSourceLang(detectedSourceLang);
            response.setTargetLang(targetLang);
            response.setOriginalText(request.getText());
            response.setTranslateTime(Instant.now().toEpochMilli() - startTime);
            response.setProvider("Tencent Cloud + AI");
            response.setStatus("success");
            response.setSmartFeatures(getSmartFeatures(request.getText(), translatedText));
            
            log.info("智能翻译完成: 耗时 {}ms", response.getTranslateTime());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("智能翻译失败", e);
            TencentTranslateResponseDTO errorResponse = new TencentTranslateResponseDTO();
            errorResponse.setOriginalText(request.getText());
            errorResponse.setStatus("error");
            errorResponse.setError(e.getMessage());
            return ApiResponse.error("翻译失败: " + e.getMessage());
        }
    }

    /**
     * 批量智能翻译
     */
    @PostMapping("/batch")
    @Operation(summary = "批量智能翻译", description = "批量文本智能翻译")
    public ApiResponse<BatchTranslateResponse> batchTranslate(@Valid @RequestBody BatchTranslateRequest request) {
        long startTime = Instant.now().toEpochMilli();
        
        try {
            log.info("批量智能翻译: 文本数量: {}", request.getTexts().size());
            
            List<TranslatedItem> results = request.getTexts().stream()
                .map(text -> {
                    String sourceLang = detectLanguage(text);
                    String targetLang = determineTargetLanguage(sourceLang, request.getTargetLang());
                    
                    try {
                        String translated = tencentTranslateService.translateText(
                            text, sourceLang, targetLang
                        );
                        
                        TranslatedItem item = new TranslatedItem();
                        item.setOriginalText(text);
                        item.setTranslatedText(translated);
                        item.setSourceLang(sourceLang);
                        item.setTargetLang(targetLang);
                        return item;
                        
                    } catch (Exception e) {
                        log.error("批量翻译单项失败: {}", text, e);
                        TranslatedItem errorItem = new TranslatedItem();
                        errorItem.setOriginalText(text);
                        errorItem.setError(e.getMessage());
                        return errorItem;
                    }
                })
                .toList();
            
            BatchTranslateResponse response = new BatchTranslateResponse();
            response.setResults(results);
            response.setTotalCount(results.size());
            response.setSuccessCount((int) results.stream().filter(r -> r.getError() == null).count());
            response.setTranslateTime(Instant.now().toEpochMilli() - startTime);
            
            log.info("批量翻译完成: 成功 {} / 总 {}", response.getSuccessCount(), response.getTotalCount());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("批量智能翻译失败", e);
            return ApiResponse.error("批量翻译失败: " + e.getMessage());
        }
    }

    // 智能语言检测（简化版）
    private String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "en";
        }
        
        // 简单的中英文检测逻辑
        long chineseChars = text.chars()
            .filter(c -> c >= 0x4E00 && c <= 0x9FFF)
            .count();
        
        return chineseChars > (text.length() / 2) ? "zh" : "en";
    }

    // 智能目标语言确定
    private String determineTargetLanguage(String sourceLang, String requestedTarget) {
        if (requestedTarget != null && !requestedTarget.isEmpty()) {
            return requestedTarget;
        }
        return "zh".equals(sourceLang) ? "en" : "zh";
    }

    // 智能特性分析
    private SmartFeatures getSmartFeatures(String original, String translated) {
        SmartFeatures features = new SmartFeatures();
        features.setWordCount(original.split("\\s+").length);
        features.setDetectedComplexity(detectComplexity(original));
        features.setRecommendedAction(getRecommendedAction(original, translated));
        return features;
    }

    private String detectComplexity(String text) {
        int wordCount = text.split("\\s+").length;
        if (wordCount < 10) return "simple";
        if (wordCount < 50) return "medium";
        return "complex";
    }

    private String getRecommendedAction(String original, String translated) {
        if (original.length() > 200) {
            return "建议分段学习";
        }
        return "适合直接阅读";
    }

    // 内部DTO类
    public static class SmartTranslateRequest {
        private String text;
        private String targetLang; // 可选，自动检测
        
        // getters and setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getTargetLang() { return targetLang; }
        public void setTargetLang(String targetLang) { this.targetLang = targetLang; }
    }

    public static class BatchTranslateRequest {
        private List<String> texts;
        private String targetLang; // 可选
        
        // getters and setters
        public List<String> getTexts() { return texts; }
        public void setTexts(List<String> texts) { this.texts = texts; }
        public String getTargetLang() { return targetLang; }
        public void setTargetLang(String targetLang) { this.targetLang = targetLang; }
    }

    public static class BatchTranslateResponse {
        private List<TranslatedItem> results;
        private int totalCount;
        private int successCount;
        private long translateTime;
        
        // getters and setters
        public List<TranslatedItem> getResults() { return results; }
        public void setResults(List<TranslatedItem> results) { this.results = results; }
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public long getTranslateTime() { return translateTime; }
        public void setTranslateTime(long translateTime) { this.translateTime = translateTime; }
    }

    public static class TranslatedItem {
        private String originalText;
        private String translatedText;
        private String sourceLang;
        private String targetLang;
        private String error;
        
        // getters and setters
        public String getOriginalText() { return originalText; }
        public void setOriginalText(String originalText) { this.originalText = originalText; }
        public String getTranslatedText() { return translatedText; }
        public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }
        public String getSourceLang() { return sourceLang; }
        public void setSourceLang(String sourceLang) { this.sourceLang = sourceLang; }
        public String getTargetLang() { return targetLang; }
        public void setTargetLang(String targetLang) { this.targetLang = targetLang; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class SmartFeatures {
        private int wordCount;
        private String detectedComplexity;
        private String recommendedAction;
        
        // getters and setters
        public int getWordCount() { return wordCount; }
        public void setWordCount(int wordCount) { this.wordCount = wordCount; }
        public String getDetectedComplexity() { return detectedComplexity; }
        public void setDetectedComplexity(String detectedComplexity) { this.detectedComplexity = detectedComplexity; }
        public String getRecommendedAction() { return recommendedAction; }
        public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    }
}
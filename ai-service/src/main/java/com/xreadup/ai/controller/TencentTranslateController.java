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

/**
 * 腾讯云翻译控制器 - 双引擎基础翻译引擎
 * 
 * 作为双引擎策略中的基础翻译引擎，提供快速、稳定的翻译服务
 * 主要处理：
 * 1. 基础文本翻译（英文↔中文）
 * 2. 批量翻译
 * 3. 支持多种语言对
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/tencent-translate")
@Tag(name = "腾讯云翻译", description = "腾讯云基础翻译引擎API")
@RequiredArgsConstructor
public class TencentTranslateController {

    private final TencentTranslateService tencentTranslateService;

    /**
     * 通用文本翻译
     * 支持任意语言对的翻译，作为双引擎的基础翻译引擎
     */
    @PostMapping("/text")
    @Operation(summary = "通用文本翻译", description = "使用腾讯云基础翻译引擎进行文本翻译")
    public ApiResponse<TencentTranslateResponseDTO> translateText(@Valid @RequestBody TencentTranslateRequestDTO request) {
        long startTime = Instant.now().toEpochMilli();
        
        try {
            log.info("腾讯云翻译请求: {} -> {}, 文本长度: {}", 
                request.getSourceLang(), request.getTargetLang(), request.getText().length());
            
            String translatedText = tencentTranslateService.translateText(
                request.getText(), 
                request.getSourceLang(), 
                request.getTargetLang()
            );
            
            TencentTranslateResponseDTO response = new TencentTranslateResponseDTO();
            response.setTranslatedText(translatedText);
            response.setSourceLang(request.getSourceLang());
            response.setTargetLang(request.getTargetLang());
            response.setOriginalText(request.getText());
            response.setTranslateTime(Instant.now().toEpochMilli() - startTime);
            response.setProvider("Tencent Cloud");
            response.setStatus("success");
            
            log.info("腾讯云翻译完成: 耗时 {}ms", response.getTranslateTime());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("腾讯云翻译失败", e);
            TencentTranslateResponseDTO errorResponse = new TencentTranslateResponseDTO();
            errorResponse.setOriginalText(request.getText());
            errorResponse.setStatus("error");
            errorResponse.setError(e.getMessage());
            return ApiResponse.error("翻译失败: " + e.getMessage());
        }
    }

    /**
     * 英文到中文翻译
     * 专门优化英文文章翻译到中文
     */
    @GetMapping("/en-to-zh")
    @Operation(summary = "英文到中文", description = "将英文内容翻译成中文")
    public ApiResponse<TencentTranslateResponseDTO> translateEnToZh(@RequestParam String text) {
        long startTime = Instant.now().toEpochMilli();
        
        try {
            log.info("腾讯云英译中请求: 文本长度: {}", text.length());
            
            String translatedText = tencentTranslateService.translateEnglishToChinese(text);
            
            TencentTranslateResponseDTO response = new TencentTranslateResponseDTO();
            response.setTranslatedText(translatedText);
            response.setSourceLang("en");
            response.setTargetLang("zh");
            response.setOriginalText(text);
            response.setTranslateTime(Instant.now().toEpochMilli() - startTime);
            response.setProvider("Tencent Cloud");
            response.setStatus("success");
            
            log.info("腾讯云英译中完成: 耗时 {}ms", response.getTranslateTime());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("腾讯云英译中失败", e);
            return ApiResponse.error("翻译失败: " + e.getMessage());
        }
    }

    /**
     * 中文到英文翻译
     * 专门优化中文内容翻译到英文
     */
    @GetMapping("/zh-to-en")
    @Operation(summary = "中文到英文", description = "将中文内容翻译成英文")
    public ApiResponse<TencentTranslateResponseDTO> translateZhToEn(@RequestParam String text) {
        long startTime = Instant.now().toEpochMilli();
        
        try {
            log.info("腾讯云中译英请求: 文本长度: {}", text.length());
            
            String translatedText = tencentTranslateService.translateChineseToEnglish(text);
            
            TencentTranslateResponseDTO response = new TencentTranslateResponseDTO();
            response.setTranslatedText(translatedText);
            response.setSourceLang("zh");
            response.setTargetLang("en");
            response.setOriginalText(text);
            response.setTranslateTime(Instant.now().toEpochMilli() - startTime);
            response.setProvider("Tencent Cloud");
            response.setStatus("success");
            
            log.info("腾讯云中译英完成: 耗时 {}ms", response.getTranslateTime());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("腾讯云中译英失败", e);
            return ApiResponse.error("翻译失败: " + e.getMessage());
        }
    }

    // 删除获取支持语言API - 产品经理决策：技术导向，用户无感知，建议前端硬编码

    /**
     * 批量翻译
     * 支持一次翻译多个文本片段
     */
    @PostMapping("/batch")
    @Operation(summary = "批量翻译", description = "批量翻译多个文本片段")
    public ApiResponse<TencentTranslateResponseDTO> translateBatch(
            @RequestParam String[] texts,
            @RequestParam String sourceLang,
            @RequestParam String targetLang) {
        long startTime = Instant.now().toEpochMilli();
        
        try {
            log.info("腾讯云批量翻译请求: {} -> {}, 文本数量: {}", sourceLang, targetLang, texts.length);
            
            String translatedText = tencentTranslateService.translateBatch(texts, sourceLang, targetLang);
            
            TencentTranslateResponseDTO response = new TencentTranslateResponseDTO();
            response.setTranslatedText(translatedText);
            response.setSourceLang(sourceLang);
            response.setTargetLang(targetLang);
            response.setOriginalText(String.join("\n", texts));
            response.setTranslateTime(Instant.now().toEpochMilli() - startTime);
            response.setProvider("Tencent Cloud");
            response.setStatus("success");
            
            log.info("腾讯云批量翻译完成: 耗时 {}ms", response.getTranslateTime());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("腾讯云批量翻译失败", e);
            return ApiResponse.error("批量翻译失败: " + e.getMessage());
        }
    }
}
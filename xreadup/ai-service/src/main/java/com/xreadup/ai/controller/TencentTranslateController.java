package com.xreadup.ai.controller;

import com.xreadup.ai.client.ArticleServiceClient;
import com.xreadup.ai.model.dto.TencentTranslateResponseDTO;
import com.xreadup.ai.model.dto.ApiResponse;
import com.xreadup.ai.service.TencentTranslateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    private final ArticleServiceClient articleServiceClient;

    // ===== translateText() 方法已删除（未使用） =====

    /**
     * 英文到中文翻译
     * 专门优化英文文章翻译到中文
     */
    @PostMapping("/en-to-zh")
    @Operation(summary = "英文到中文", description = "将英文内容翻译成中文")
    public ApiResponse<TencentTranslateResponseDTO> translateEnToZh(@RequestBody TranslationRequest request) {
        long startTime = Instant.now().toEpochMilli();
        
        try {
            log.info("腾讯云英译中请求: 文本长度: {}", request.text.length());
            
            String translatedText = tencentTranslateService.translateEnglishToChinese(request.text);
            
            TencentTranslateResponseDTO response = new TencentTranslateResponseDTO();
            response.setTranslatedText(translatedText);
            response.setSourceLang("en");
            response.setTargetLang("zh");
            response.setOriginalText(request.text);
            response.setTranslateTime(Instant.now().toEpochMilli() - startTime);
            response.setProvider("Tencent Cloud");
            response.setStatus("success");
            
            // 尝试将翻译结果保存到数据库
            try {
                if (translatedText != null && !translatedText.isEmpty()) {
                    // 创建内部类实例
                    class UpdateContentCnRequestImpl implements ArticleServiceClient.UpdateContentCnRequest {
                        private final String contentEn;
                        private final String contentCn;
                        
                        public UpdateContentCnRequestImpl(String contentEn, String contentCn) {
                            this.contentEn = contentEn;
                            this.contentCn = contentCn;
                        }
                        
                        @Override
                        public String getContentEn() {
                            return contentEn;
                        }
                        
                        @Override
                        public String getContentCn() {
                            return contentCn;
                        }
                    }
                    
                    ArticleServiceClient.UpdateContentCnRequest updateRequest = new UpdateContentCnRequestImpl(request.text, translatedText);
                    ArticleServiceClient.ApiResponse<Boolean> updateResponse = articleServiceClient.updateContentCn(updateRequest);
                    
                    if (updateResponse != null && updateResponse.isSuccess()) {
                        log.info("成功将翻译结果保存到数据库");
                    } else {
                        log.warn("保存翻译结果到数据库失败: {}", updateResponse != null ? updateResponse.getMessage() : "未知错误");
                    }
                }
            } catch (Exception e) {
                // 记录异常但不影响翻译功能
                log.error("保存翻译结果到数据库时发生异常", e);
            }
            
            log.info("腾讯云英译中完成: 耗时 {}ms", response.getTranslateTime());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("腾讯云英译中失败", e);
            return ApiResponse.error("翻译失败: " + e.getMessage());
        }
    }

    // ===== 以下方法已删除（未使用） =====
    // - translateZhToEn() - 未使用
    // - translateBatch() - 未使用
    
    // 简单的翻译请求类
    public static class TranslationRequest {
        public String text;
    }
}
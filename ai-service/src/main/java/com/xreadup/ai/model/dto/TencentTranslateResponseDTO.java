package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 腾讯云翻译响应DTO
 * 用于双引擎策略中的基础翻译引擎响应
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Schema(description = "腾讯云翻译响应")
public class TencentTranslateResponseDTO {

    @Schema(description = "翻译后的文本", example = "你好，世界！")
    private String translatedText;

    @Schema(description = "源语言", example = "en")
    private String sourceLang;

    @Schema(description = "目标语言", example = "zh")
    private String targetLang;

    @Schema(description = "原文本", example = "Hello, world!")
    private String originalText;

    @Schema(description = "翻译耗时（毫秒）", example = "150")
    private Long translateTime;

    @Schema(description = "翻译服务提供商", example = "Tencent Cloud")
    private String provider = "Tencent Cloud";

    @Schema(description = "翻译状态", example = "success")
    private String status = "success";

    @Schema(description = "错误信息（如果有）", example = "")
    private String error;

    @Schema(description = "智能特性配置", example = "{\"enableSmartCorrection\": true, \"enableContextAware\": true}")
    private Object smartFeatures;

    public void setSmartFeatures(Object smartFeatures) {
        this.smartFeatures = smartFeatures;
    }
}
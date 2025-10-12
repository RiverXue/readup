package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 腾讯云翻译请求DTO
 * 用于双引擎策略中的基础翻译引擎请求
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Schema(description = "腾讯云翻译请求")
public class TencentTranslateRequestDTO {

    @Schema(description = "待翻译文本", example = "Hello, world!")
    @NotBlank(message = "待翻译文本不能为空")
    private String text;

    @Schema(description = "源语言代码", example = "en", allowableValues = {"auto", "en", "zh", "ja", "ko", "fr", "de", "es", "ru", "ar"})
    @NotBlank(message = "源语言不能为空")
    private String sourceLang;

    @Schema(description = "目标语言代码", example = "zh", allowableValues = {"zh", "en", "ja", "ko", "fr", "de", "es", "ru", "ar"})
    @NotBlank(message = "目标语言不能为空")
    private String targetLang;

    @Schema(description = "文章ID（可选）", example = "1")
    private Long articleId;
}
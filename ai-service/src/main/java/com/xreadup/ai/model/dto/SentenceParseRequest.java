package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 句子解析请求DTO
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Schema(description = "句子解析请求")
public class SentenceParseRequest {
    
    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", example = "123")
    private Long articleId;

    @NotBlank(message = "句子内容不能为空")
    @Schema(description = "需要解析的句子", example = "Despite the heavy rain, the outdoor concert continued as planned.")
    private String sentence;

    @Schema(description = "解析深度：basic-基础解析，advanced-深度解析", example = "advanced", defaultValue = "advanced")
    private String parseLevel = "advanced";
}
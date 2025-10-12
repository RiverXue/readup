package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 选词翻译请求DTO
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Schema(description = "选词翻译请求")
public class WordTranslationRequest {
    
    @NotBlank(message = "单词不能为空")
    @Schema(description = "需要翻译的英文单词", example = "technology", required = true)
    private String word;
    
    @NotBlank(message = "上下文不能为空")
    @Schema(description = "单词所在的上下文句子", example = "Technology is changing our lives.", required = true)
    private String context;
    
    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID，用于关联上下文", example = "123", required = true)
    private Long articleId;
}
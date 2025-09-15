package com.xreadup.ai.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 全文翻译请求DTO
 */
@Data
public class TranslationRequest {
    
    @NotBlank(message = "翻译内容不能为空")
    private String content;
    
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
}
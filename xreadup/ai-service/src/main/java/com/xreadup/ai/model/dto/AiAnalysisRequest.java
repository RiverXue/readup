package com.xreadup.ai.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AI分析请求DTO
 * 
 * @author xreadup
 * @version 1.0
 */
@Data
public class AiAnalysisRequest {
    
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    @NotBlank(message = "文章内容不能为空")
    private String content;
}
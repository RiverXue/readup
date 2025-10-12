package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI摘要请求DTO
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Schema(description = "AI摘要请求")
public class AiSummaryRequest {
    
    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", example = "123")
    private Long articleId;
    
    @NotBlank(message = "文本内容不能为空")
    @Schema(description = "需要摘要的文本内容", example = "This is a sample article content...")
    private String text;
    
    @Schema(description = "摘要最大长度（字符数）", example = "200", defaultValue = "200")
    private Integer maxLength = 200;
}
package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 测验题生成请求DTO
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Schema(description = "测验题生成请求")
public class QuizGenerationRequest {
    
    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", example = "123")
    private Long articleId;
    
    @NotBlank(message = "文本内容不能为空")
    @Schema(description = "文章内容", example = "This is a sample article content...")
    private String text;
    
    @Min(value = 1, message = "题目数量必须大于0")
    @Schema(description = "题目数量", example = "5", defaultValue = "5")
    private Integer questionCount = 5;
    
    @Schema(description = "题目类型：comprehension-阅读理解，vocabulary-词汇理解", example = "comprehension", defaultValue = "comprehension")
    private String questionType = "comprehension";
    
    @Schema(description = "难度级别：easy-简单，medium-中等，hard-困难", example = "medium", defaultValue = "medium")
    private String difficulty = "medium";
}
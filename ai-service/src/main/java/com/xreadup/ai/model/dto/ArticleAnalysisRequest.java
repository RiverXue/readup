package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "文章分析请求DTO")
public class ArticleAnalysisRequest {
    
    @Schema(description = "文章标题", example = "AI Revolution in Healthcare")
    @NotBlank(message = "文章标题不能为空")
    private String title;
    
    @Schema(description = "文章内容", example = "Artificial intelligence is transforming healthcare...")
    @NotBlank(message = "文章内容不能为空")
    private String content;
    
    @Schema(description = "文章分类", example = "科技")
    private String category;
    
    @Schema(description = "文章字数", example = "500")
    @NotNull(message = "文章字数不能为空")
    private Integer wordCount;
}
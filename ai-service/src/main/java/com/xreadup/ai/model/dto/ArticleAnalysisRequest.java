package com.xreadup.ai.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 文章分析请求DTO
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
public class ArticleAnalysisRequest {
    
    /**
     * 文章ID
     */
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    private String title;
    
    /**
     * 文章内容
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;
    
    /**
     * 文章分类
     */
    @NotBlank(message = "文章分类不能为空")
    private String category;
    
    /**
     * 文章字数
     */
    @NotNull(message = "文章字数不能为空")
    private Integer wordCount;
}
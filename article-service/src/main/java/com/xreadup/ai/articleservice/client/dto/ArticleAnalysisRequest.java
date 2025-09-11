package com.xreadup.ai.articleservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章分析请求DTO
 * 用于文章服务向AI服务发送分析请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleAnalysisRequest {
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章内容
     */
    private String content;
    
    /**
     * 文章分类
     */
    private String category;
    
    /**
     * 文章字数
     */
    private Integer wordCount;
}
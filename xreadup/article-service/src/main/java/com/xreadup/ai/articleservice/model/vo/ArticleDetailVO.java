package com.xreadup.ai.articleservice.model.vo;

import com.xreadup.ai.articleservice.client.dto.ArticleAnalysisResponse;
import com.xreadup.ai.articleservice.model.entity.Article;
import lombok.Data;

/**
 * 文章详情VO，包含AI分析结果
 */
@Data
public class ArticleDetailVO {
    /**
     * 文章基本信息
     */
    private ArticleVO article;
    
    /**
     * AI分析结果
     */
    private ArticleAnalysisResponse aiAnalysis;
    
    /**
     * 是否已进行AI分析
     */
    private Boolean hasAiAnalysis;
}
package com.xreadup.ai.articleservice.client.dto;

import lombok.Data;

import java.util.List;

/**
 * 文章分析响应DTO
 * AI服务返回的分析结果
 */
@Data
public class ArticleAnalysisResponse {
    /**
     * CEFR难度等级 (A1, A2, B1, B2, C1, C2)
     */
    private String difficultyLevel;
    
    /**
     * 关键词列表
     */
    private List<String> keywords;
    
    /**
     * 中文摘要
     */
    private String summary;
    
    /**
     * 中文翻译
     */
    private String chineseTranslation;
    
    /**
     * 简化版英文内容
     */
    private String simplifiedContent;
    
    /**
     * 关键短语列表
     */
    private List<String> keyPhrases;
    
    /**
     * 可读性评分 (0-100)
     */
    private Double readabilityScore;
}
package com.xreadup.ai.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI分析响应DTO
 * 
 * @author xreadup
 * @version 1.0
 */
@Data
public class AiAnalysisResponse {
    
    /**
     * 文章ID
     */
    private Long articleId;
    
    /**
     * 中文翻译
     */
    private String chineseTranslation;
    
    /**
     * 中文摘要
     */
    private String chineseSummary;
    
    /**
     * 关键词列表
     */
    private String keywords;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
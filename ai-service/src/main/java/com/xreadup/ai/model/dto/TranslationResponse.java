package com.xreadup.ai.model.dto;

import lombok.Data;

/**
 * 全文翻译响应DTO
 * 
 * @author xreadup
 * @version 1.0
 */
@Data
public class TranslationResponse {
    
    /**
     * 原文内容
     */
    private String originalText;
    
    /**
     * 翻译后的中文内容
     */
    private String translatedText;
    
    /**
     * 目标语言代码
     */
    private String language;
    
    /**
     * 关联的文章ID
     */
    private Long articleId;
}
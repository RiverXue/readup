package com.xreadup.ai.articleservice.model.dto;

import lombok.Data;

@Data
public class ArticleAnalysisRequest {
    private String title;
    private String content;
    private String url;
}
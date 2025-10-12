package com.xreadup.ai.articleservice.model.dto;

import lombok.Data;

@Data
public class ArticleQueryDTO {
    private String category;
    private String difficultyLevel;
    private String keyword;
    private Integer page = 1;
    private Integer size = 10;
    private String sortBy = "publishedAt";
    private Boolean ascending = false;
}
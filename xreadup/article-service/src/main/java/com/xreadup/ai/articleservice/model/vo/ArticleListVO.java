package com.xreadup.ai.articleservice.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleListVO {
    private Long id;
    private String title;
    private String description;
    private String url;
    private String image;
    private String source;
    private String category;
    private String difficultyLevel;
    private String manualDifficulty;
    private Integer wordCount;
    private Integer readCount;
    private Integer likeCount;
    private Boolean isFeatured;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
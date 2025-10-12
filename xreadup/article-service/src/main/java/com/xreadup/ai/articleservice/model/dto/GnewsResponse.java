package com.xreadup.ai.articleservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GnewsResponse {
    private Integer totalArticles;
    private List<GnewsArticle> articles;
    
    @Data
    public static class GnewsArticle {
        private String title;
        private String description;
        private String content;
        private String url;
        private String image;
        
        @JsonProperty("publishedAt")
        private LocalDateTime publishedAt;
        
        @JsonProperty("source")
        private Source source;
        
        @Data
        public static class Source {
            private String name;
            private String url;
        }
    }
}
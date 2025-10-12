package com.xreadup.ai.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 文章服务客户端
 * 
 * 用于AI服务调用文章服务的接口
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@FeignClient(name = "article-service")
public interface ArticleServiceClient {
    
    /**
     * 获取文章详情
     * 
     * @param articleId 文章ID
     * @return 文章详情
     */
    @GetMapping("/api/article/read/{articleId}")
    ApiResponse<ArticleDetail> getArticleDetail(@PathVariable Long articleId);
    
    /**
     * 更新文章的中文翻译内容
     * 
     * @param request 更新中文内容请求
     * @return 更新结果
     */
    @PostMapping("/api/article/update-content-cn")
    ApiResponse<Boolean> updateContentCn(@RequestBody UpdateContentCnRequest request);
    
    /**
     * API响应通用格式 - 与article-service中的实现保持一致
     */
    @Data
    class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
    }
    
    /**
     * 文章详情
     */
    @Data
    class ArticleDetail {
        private ArticleInfo article;
        private Boolean hasAiAnalysis;
    }
    
    /**
     * 文章信息
     */
    @Data
    class ArticleInfo {
        private Long id;
        private String title;
        private String contentEn;
        private String contentCn;
        private String category;
        private String difficultyLevel;
        private String[] tags;
        private Integer readCount;
    }
    
    /**
     * 更新中文内容请求
     */
    interface UpdateContentCnRequest {
        String getContentEn();
        String getContentCn();
    }
}
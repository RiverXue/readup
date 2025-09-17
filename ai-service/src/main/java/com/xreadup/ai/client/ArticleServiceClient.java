package com.xreadup.ai.client;

import org.springframework.cloud.openfeign.FeignClient;
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
     * 更新文章的中文翻译内容
     * 
     * @param request 更新中文内容请求
     * @return 更新结果
     */
    @PostMapping("/api/article/update-content-cn")
    ApiResponse<Boolean> updateContentCn(@RequestBody UpdateContentCnRequest request);
    
    /**
     * API响应通用格式
     */
    interface ApiResponse<T> {
        int getCode();
        String getMessage();
        T getData();
        boolean isSuccess();
    }
    
    /**
     * 更新中文内容请求
     */
    interface UpdateContentCnRequest {
        String getContentEn();
        String getContentCn();
    }
}
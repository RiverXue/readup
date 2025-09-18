package com.xreadup.ai.userservice.client;

import com.xreadup.ai.model.dto.WordInfo;
import com.xreadup.ai.userservice.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * AI服务Feign客户端
 * 用于user-service调用ai-service的接口
 */
@FeignClient(name = "ai-service", path = "/api/ai/assistant")
public interface AiServiceClient {

    /**
     * 查询单词信息
     * 
     * @param word 要查询的单词
     * @return 包含单词详细信息的API响应
     */
    @GetMapping("/word/{word}")
    ApiResponse<WordInfo> lookupWord(@PathVariable("word") String word);
}
package com.xreadup.ai.controller;

import com.xreadup.ai.model.dto.SimpleAiTutorRequest;
import com.xreadup.ai.model.dto.SimpleAiTutorResponse;
import com.xreadup.ai.service.SimpleAiTutorService;
import com.xreadup.ai.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 简配版AI学导控制器
 * 专门为SimpleAITutor组件设计，极度节省token
 */
@RestController
@RequestMapping("/api/ai/simple-tutor")
@Tag(name = "简配版AI学导", description = "专门为文章阅读页面设计的轻量级AI学导")
@Slf4j
public class SimpleAiTutorController {

    @Autowired
    private SimpleAiTutorService simpleAiTutorService;

    /**
     * 简配版AI学导对话接口
     * 专门优化token使用，只传递必要信息
     */
    @PostMapping("/chat")
    @Operation(summary = "简配版AI学导对话", description = "为文章阅读页面提供轻量级AI学习指导")
    public ApiResponse<SimpleAiTutorResponse> chat(@RequestBody SimpleAiTutorRequest request) {
        try {
            log.info("简配版AI学导请求 - 用户: {}, 问题: {}, 文章: {}", 
                request.getUserId(), request.getQuestion(), request.getArticleTitle());
            
            SimpleAiTutorResponse response = simpleAiTutorService.chat(request);
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("简配版AI学导对话失败", e);
            return ApiResponse.error("AI学导暂时无法回答，请稍后再试");
        }
    }
}

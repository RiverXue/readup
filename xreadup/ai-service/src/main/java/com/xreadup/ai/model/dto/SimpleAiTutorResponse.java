package com.xreadup.ai.model.dto;

import lombok.Data;

/**
 * 简配版AI学导响应DTO
 * 精简响应结构，节省token
 */
@Data
public class SimpleAiTutorResponse {
    private String answer;
    private String followUpQuestion;
}

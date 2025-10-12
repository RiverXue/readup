package com.xreadup.ai.model.dto;

import lombok.Data;

/**
 * AI对话请求DTO
 */
@Data
public class AiChatRequest {
    private String question;
    private String articleContext;
    private Long userId;
    private Long articleId;
}
package com.xreadup.ai.userservice.dto;

import lombok.Data;

/**
 * 添加生词请求DTO
 */
@Data
public class AddWordRequest {
    private Long userId;
    private String word;
    private String meaning;
    private String context; // 上下文（如：金融/科技/地理）
    private Long sourceArticleId;
}
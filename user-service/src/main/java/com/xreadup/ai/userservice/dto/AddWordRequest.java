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
    private Long sourceArticleId;
}
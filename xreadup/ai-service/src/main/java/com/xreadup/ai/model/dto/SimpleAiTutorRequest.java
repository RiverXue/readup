package com.xreadup.ai.model.dto;

import lombok.Data;

/**
 * 简配版AI学导请求DTO
 * 只包含必要字段，节省token
 */
@Data
public class SimpleAiTutorRequest {
    private String question;
    private Long userId;
    private String articleTitle;
    private String articleCategory;
    private String articleDifficulty;
    private String articleDescription; // 只传递文章描述，不传递完整内容
}

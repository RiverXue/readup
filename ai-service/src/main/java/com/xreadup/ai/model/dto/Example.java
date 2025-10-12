package com.xreadup.ai.model.dto;

import lombok.Data;

/**
 * 例句DTO类
 * 包含英文例句和中文翻译
 */
@Data
public class Example {
    private String english;
    private String chinese;
}
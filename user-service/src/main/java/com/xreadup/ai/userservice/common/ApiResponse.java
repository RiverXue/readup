package com.xreadup.ai.userservice.common;

import lombok.Data;

/**
 * 通用API响应类
 * 用于处理Feign客户端调用ai-service返回的响应
 */
@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private boolean success;
}
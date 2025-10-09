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

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        response.setSuccess(true);
        return response;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        response.setSuccess(false);
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }
}
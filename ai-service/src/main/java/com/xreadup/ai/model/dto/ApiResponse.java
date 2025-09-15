package com.xreadup.ai.model.dto;

import lombok.Data;

/**
 * 统一API响应DTO
 * 
 * @author xreadup
 * @version 1.0
 */
@Data
public class ApiResponse<T> {
    
    /**
     * 响应码
     */
    private int code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        response.setSuccess(true);
        return response;
    }
    
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(500);
        response.setMessage(message);
        response.setData(null);
        response.setSuccess(false);
        return response;
    }
}
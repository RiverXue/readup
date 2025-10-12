package com.xreadup.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一API响应封装类
 * @param <T> 响应数据类型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;
    
    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        return response;
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
    
    /**
     * 错误响应（带数据）
     */
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    /**
     * 参数错误
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(400, message);
    }
    
    /**
     * 未找到资源
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return error(404, message);
    }
    
    /**
     * 服务器错误
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return error(500, message);
    }
}
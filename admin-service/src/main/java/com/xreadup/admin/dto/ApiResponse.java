package com.xreadup.admin.dto;

import lombok.Data;

/**
 * 通用API响应包装类
 * 统一接口返回格式
 */
@Data
public class ApiResponse<T> {
    
    /**
     * 响应状态码
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
    
    /**
     * 私有构造函数
     */
    private ApiResponse() {
    }
    
    /**
     * 成功响应构造方法
     * @param data 响应数据
     */
    private ApiResponse(T data) {
        this.code = 200;
        this.message = "success";
        this.data = data;
        this.success = true;
    }
    
    /**
     * 成功响应构造方法
     * @param data 响应数据
     * @param message 响应消息
     */
    private ApiResponse(T data, String message) {
        this.code = 200;
        this.message = message;
        this.data = data;
        this.success = true;
    }
    
    /**
     * 失败响应构造方法
     * @param code 状态码
     * @param message 错误消息
     */
    private ApiResponse(int code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
        this.success = false;
    }
    
    /**
     * 创建成功响应
     * @param data 响应数据
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }
    
    /**
     * 创建成功响应
     * @param data 响应数据
     * @param message 响应消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message);
    }
    
    /**
     * 创建无数据的成功响应
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(null);
    }
    
    /**
     * 创建失败响应
     * @param code 状态码
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message);
    }
    
    /**
     * 创建400错误响应
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message);
    }
    
    /**
     * 创建401错误响应
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message);
    }
    
    /**
     * 创建403错误响应
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, message);
    }
    
    /**
     * 创建404错误响应
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message);
    }
    
    /**
     * 创建500错误响应
     * @param message 错误消息
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> internalError(String message) {
        return new ApiResponse<>(500, message);
    }
}
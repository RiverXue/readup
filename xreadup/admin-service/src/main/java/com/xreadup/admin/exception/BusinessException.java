package com.xreadup.admin.exception;

/**
 * 业务异常类
 * 用于表示业务逻辑相关的异常
 */
public class BusinessException extends RuntimeException {
    
    private String errorCode;
    
    /**
     * 构造函数
     * @param message 异常消息
     */
    public BusinessException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * @param message 异常消息
     * @param errorCode 错误码
     */
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 构造函数
     * @param message 异常消息
     * @param cause 异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造函数
     * @param message 异常消息
     * @param errorCode 错误码
     * @param cause 异常原因
     */
    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * 获取错误码
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 设置错误码
     * @param errorCode 错误码
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
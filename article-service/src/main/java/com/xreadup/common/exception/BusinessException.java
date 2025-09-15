package com.xreadup.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于处理业务逻辑相关的异常情况
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final int code;
    
    /**
     * 构造业务异常
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 构造业务异常（默认400错误码）
     * @param message 错误消息
     */
    public BusinessException(String message) {
        this(400, message);
    }
    
    /**
     * 构造业务异常（带原始异常）
     * @param code 错误码
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
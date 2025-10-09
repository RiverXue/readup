package com.xreadup.admin.exception;

/**
 * 管理员不存在异常
 * 当请求的管理员用户不存在时抛出
 */
public class AdminNotFoundException extends BusinessException {

    /**
     * 构造函数
     * @param message 异常消息
     */
    public AdminNotFoundException(String message) {
        super(message);
    }

    /**
     * 构造函数
     * @param message 异常消息
     * @param errorCode 错误码
     */
    public AdminNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }

    /**
     * 构造函数
     * @param message 异常消息
     * @param cause 异常原因
     */
    public AdminNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     * @param message 异常消息
     * @param errorCode 错误码
     * @param cause 异常原因
     */
    public AdminNotFoundException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
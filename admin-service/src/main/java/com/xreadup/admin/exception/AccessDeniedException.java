package com.xreadup.admin.exception;

/**
 * 访问拒绝异常
 * 当用户没有足够权限执行操作时抛出
 */
public class AccessDeniedException extends BusinessException {

    /**
     * 构造函数
     * @param message 异常消息
     */
    public AccessDeniedException(String message) {
        super(message);
    }

    /**
     * 构造函数
     * @param message 异常消息
     * @param errorCode 错误码
     */
    public AccessDeniedException(String message, String errorCode) {
        super(message, errorCode);
    }

    /**
     * 构造函数
     * @param message 异常消息
     * @param cause 异常原因
     */
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     * @param message 异常消息
     * @param errorCode 错误码
     * @param cause 异常原因
     */
    public AccessDeniedException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
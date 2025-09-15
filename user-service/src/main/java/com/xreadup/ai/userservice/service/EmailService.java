package com.xreadup.ai.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 简化邮箱服务 - 处理用户注册验证（开发环境版本，不实际发送邮件）
 */
@Service
@Slf4j
public class EmailService {
    
    // 存储验证码（生产环境应使用Redis）
    private final ConcurrentMap<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();

    /**
     * 发送验证邮件（简化版本，仅记录日志不实际发送）
     */
    public void sendVerificationEmail(String toEmail, String username) {
        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        
        // 模拟发送，实际只记录日志
        log.info("模拟发送验证邮件至: {}，验证码: {}", toEmail, code);
        verificationCodes.put(toEmail, new VerificationCode(code, System.currentTimeMillis() + 15 * 60 * 1000));
        
        // 开发环境直接在控制台输出验证码
        log.warn("开发环境验证码：邮箱 {} 的验证码是 {}", toEmail, code);
    }

    /**
     * 验证邮箱验证码
     */
    public boolean verifyEmail(String email, String code) {
        VerificationCode storedCode = verificationCodes.get(email);
        if (storedCode == null) {
            return false;
        }
        
        if (System.currentTimeMillis() > storedCode.expiryTime) {
            verificationCodes.remove(email);
            return false;
        }
        
        boolean valid = storedCode.code.equalsIgnoreCase(code);
        if (valid) {
            verificationCodes.remove(email);
        }
        
        return valid;
    }

    /**
     * 检查邮箱是否已发送验证码
     */
    public boolean hasPendingVerification(String email) {
        return verificationCodes.containsKey(email);
    }

    private String buildEmailContent(String username, String code) {
        return String.format("""
            亲爱的 %s，
            
            感谢您注册 ReadUp AI！
            
            您的邮箱验证码是：%s
            
            请在15分钟内完成验证。如未收到邮件，请检查垃圾邮件箱。
            
            如果这不是您的操作，请忽略此邮件。
            
            ReadUp AI 团队
            官方网站：https://xreadup.com
            """, username, code);
    }

    /**
     * 验证码内部类
     */
    private static class VerificationCode {
        String code;
        long expiryTime;
        
        VerificationCode(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }
}
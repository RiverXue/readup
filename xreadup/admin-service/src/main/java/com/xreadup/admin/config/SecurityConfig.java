package com.xreadup.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全配置类
 * 配置管理员服务的安全相关组件
 */
@Configuration
public class SecurityConfig {

    /**
     * 密码编码器配置
     * 使用BCrypt密码加密算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 可以在这里添加其他安全相关的配置
     * 如: 安全过滤器链、会话管理等
     */
}
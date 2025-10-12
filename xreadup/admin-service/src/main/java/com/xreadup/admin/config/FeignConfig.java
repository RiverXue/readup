package com.xreadup.admin.config;

import feign.Logger;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign配置类
 * 配置OpenFeign的日志级别、重试策略等
 */
@Configuration
public class FeignConfig {
    
    /**
     * 配置Feign的日志级别
     * NONE: 不记录任何日志
     * BASIC: 仅记录请求方法、URL、响应状态码和执行时间
     * HEADERS: 记录BASIC级别的信息，以及请求和响应的头信息
     * FULL: 记录请求和响应的头信息、正文和元数据
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    /**
     * 配置Feign的重试策略
     */
    @Bean
    public Retryer feignRetryer() {
        // 第一个参数：初始间隔时间
        // 第二个参数：最大间隔时间
        // 第三个参数：最大尝试次数
        return new Retryer.Default(100, 1000, 3);
    }
}
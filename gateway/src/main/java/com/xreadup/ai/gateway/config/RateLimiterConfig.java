package com.xreadup.ai.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * 网关限流配置类
 * 配置RequestRateLimiter所需的KeyResolver
 */
@Configuration
public class RateLimiterConfig {

    /**
     * 基于IP地址的KeyResolver
     * 用于RequestRateLimiter限流过滤器
     */
    @Bean
    @Primary
    public KeyResolver remoteAddrKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

    /**
     * 基于用户ID的KeyResolver（备用）
     * 可以根据需要从请求头或参数中获取用户ID
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // 从请求头获取用户ID
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            if (userId != null) {
                return Mono.just(userId);
            }
            
            // 从查询参数获取用户ID
            userId = exchange.getRequest().getQueryParams().getFirst("userId");
            if (userId != null) {
                return Mono.just(userId);
            }
            
            // 默认使用IP地址
            return Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
            );
        };
    }

    /**
     * 基于请求路径的KeyResolver（备用）
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest().getPath().value()
        );
    }
}
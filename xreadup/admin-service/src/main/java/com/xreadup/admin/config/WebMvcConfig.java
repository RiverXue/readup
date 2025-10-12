package com.xreadup.admin.config;

import com.xreadup.admin.interceptor.AdminAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 用于配置Spring MVC相关功能，包括拦截器注册
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册管理员认证拦截器，拦截所有以/api/admin开头的请求
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                // 排除公开接口，避免拦截登录、检查、登出等不需要认证的接口
                .excludePathPatterns("/api/admin/login")
                .excludePathPatterns("/api/admin/check")
                .excludePathPatterns("/api/admin/logout")
                .excludePathPatterns("/api/admin/extend-session");
    }
}
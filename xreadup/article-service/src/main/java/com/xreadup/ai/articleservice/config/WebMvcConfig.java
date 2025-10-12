package com.xreadup.ai.articleservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 用于优化静态资源处理，包括favicon.ico
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置favicon.ico的静态资源映射
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false);
        
        // 配置所有静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false);
    }
}
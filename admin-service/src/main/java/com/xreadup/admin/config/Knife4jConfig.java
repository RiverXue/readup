package com.xreadup.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j配置类
 * 配置API文档相关信息
 */
@Configuration
public class Knife4jConfig {
    
    /**
     * 配置OpenAPI文档信息
     */
    @Bean
    public OpenAPI adminServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Admin Service API")
                        .description("管理员服务API文档")
                        .version("1.0.0")
                        .termsOfService("https://example.com/terms")
                );
    }
}
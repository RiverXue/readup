package com.xreadup.ai.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j配置类
 * 用于配置Swagger接口文档
 */
@Configuration
public class Knife4jConfig {

    /**
     * 创建用户服务API分组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user-service")
                .pathsToMatch("/api/user/**")
                .build();
    }

    /**
     * 配置OpenAPI信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("XReadUp用户服务API")
                        .description("XReadUp AI阅读助手 - 用户服务接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("XReadUp Team")
                                .email("support@xreadup.com")
                                .url("https://xreadup.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
package com.xreadup.ai.articleservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.xreadup.ai.articleservice.mapper")
public class ArticleServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArticleServiceApplication.class, args);
    }
}
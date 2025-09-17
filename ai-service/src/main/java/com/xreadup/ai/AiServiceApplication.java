package com.xreadup.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.xreadup.ai.client"})
public class AiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
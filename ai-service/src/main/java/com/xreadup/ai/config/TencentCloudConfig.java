package com.xreadup.ai.config;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云翻译服务配置 - 双引擎基础翻译引擎
 * 基于官方文档：https://cloud.tencent.com/document/product/551/15612
 * 
 * 为AI服务提供腾讯云基础翻译能力
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Configuration
public class TencentCloudConfig {

    @Value("${tencent.cloud.translate.secret-id:}")
    private String secretId;

    @Value("${tencent.cloud.translate.secret-key:}")
    private String secretKey;

    @Value("${tencent.cloud.translate.region:ap-beijing}")
    private String region;

    @Value("${tencent.cloud.translate.endpoint:tmt.tencentcloudapi.com}")
    private String endpoint;

    /**
     * 创建腾讯云翻译客户端 - 基础翻译引擎
     */
    @Bean
    public TmtClient tmtClient() {
        // 实例化认证对象
        Credential cred = new Credential(secretId, secretKey);
        
        // 实例化HTTP选项
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(endpoint);
        httpProfile.setReqMethod("POST");
        httpProfile.setConnTimeout(60);
        httpProfile.setReadTimeout(60);
        
        // 实例化客户端选项
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        clientProfile.setSignMethod("HmacSHA256");
        
        // 返回翻译客户端
        return new TmtClient(cred, region, clientProfile);
    }
}
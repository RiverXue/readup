package com.xreadup.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 系统配置服务客户端
 * 用于各微服务获取系统配置
 * 
 * @author XReadUp
 * @since 2025-10-12
 */
@FeignClient(name = "admin-service", url = "${admin.service.url:http://localhost:8085}")
public interface SystemConfigClient {
    
    /**
     * 获取配置值
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    @GetMapping("/api/admin/system-config/value/{configKey}")
    String getConfigValue(@PathVariable("configKey") String configKey);
    
    /**
     * 获取配置值（带默认值）
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    @GetMapping("/api/admin/system-config/value/{configKey}")
    String getConfigValue(@PathVariable("configKey") String configKey, 
                         @RequestParam("defaultValue") String defaultValue);
    
    /**
     * 检查功能是否启用
     * 
     * @param featureKey 功能键
     * @return 是否启用
     */
    @GetMapping("/api/admin/system-config/feature/{featureKey}/enabled")
    Boolean isFeatureEnabled(@PathVariable("featureKey") String featureKey);
    
    /**
     * 获取业务限制值
     * 
     * @param limitKey 限制键
     * @param defaultValue 默认值
     * @return 限制值
     */
    @GetMapping("/api/admin/system-config/limit/{limitKey}")
    Integer getLimitValue(@PathVariable("limitKey") String limitKey, 
                         @RequestParam("defaultValue") Integer defaultValue);
    
    /**
     * 检查系统维护状态
     * 
     * @return 维护状态
     */
    @GetMapping("/api/admin/system-config/maintenance/status")
    Object getMaintenanceStatus();
}

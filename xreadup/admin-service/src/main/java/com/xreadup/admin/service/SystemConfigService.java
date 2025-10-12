package com.xreadup.admin.service;

import com.xreadup.admin.dto.SystemConfigDTO;
import com.xreadup.admin.model.entity.SystemConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 * 
 * @author XReadUp
 * @since 2025-10-12
 */
public interface SystemConfigService {
    
    /**
     * 获取所有配置
     * 
     * @return 配置列表
     */
    List<SystemConfigDTO> getAllConfigs();
    
    /**
     * 根据分类获取配置
     * 
     * @param category 配置分类
     * @return 配置列表
     */
    List<SystemConfigDTO> getConfigsByCategory(String category);
    
    /**
     * 根据配置键获取配置值
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 根据配置键获取配置值（带默认值）
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 获取布尔类型配置值
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 布尔值
     */
    Boolean getBooleanConfigValue(String configKey, Boolean defaultValue);
    
    /**
     * 获取数字类型配置值
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 数字值
     */
    Integer getIntegerConfigValue(String configKey, Integer defaultValue);
    
    /**
     * 更新单个配置
     * 
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否成功
     */
    boolean updateConfig(String configKey, String configValue);
    
    /**
     * 批量更新配置
     * 
     * @param configs 配置Map
     * @return 是否成功
     */
    boolean batchUpdateConfigs(Map<String, String> configs);
    
    /**
     * 重置配置为默认值
     * 
     * @param configKey 配置键
     * @return 是否成功
     */
    boolean resetConfigToDefault(String configKey);
    
    /**
     * 获取配置分类列表
     * 
     * @return 分类列表
     */
    List<String> getConfigCategories();
    
    /**
     * 获取系统信息
     * 
     * @return 系统信息Map
     */
    Map<String, Object> getSystemInfo();
    
    /**
     * 检查功能是否启用
     * 
     * @param featureKey 功能键
     * @return 是否启用
     */
    boolean isFeatureEnabled(String featureKey);
    
    /**
     * 获取业务限制值
     * 
     * @param limitKey 限制键
     * @param defaultValue 默认值
     * @return 限制值
     */
    Integer getLimitValue(String limitKey, Integer defaultValue);
    
    /**
     * 检查系统是否处于维护模式
     * 
     * @return 是否维护模式
     */
    boolean isMaintenanceMode();
    
    /**
     * 获取维护模式消息
     * 
     * @return 维护消息
     */
    String getMaintenanceMessage();
}

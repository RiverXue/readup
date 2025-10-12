package com.xreadup.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xreadup.admin.dto.SystemConfigDTO;
import com.xreadup.admin.mapper.SystemConfigMapper;
import com.xreadup.admin.model.entity.SystemConfig;
import com.xreadup.admin.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现类
 * 
 * @author XReadUp
 * @since 2025-10-12
 */
@Service
public class SystemConfigServiceImpl implements SystemConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemConfigServiceImpl.class);
    
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    
    @Override
    public List<SystemConfigDTO> getAllConfigs() {
        try {
            List<SystemConfig> configs = systemConfigMapper.selectList(null);
            return configs.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取所有配置失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<SystemConfigDTO> getConfigsByCategory(String category) {
        try {
            LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemConfig::getCategory, category);
            queryWrapper.orderByAsc(SystemConfig::getConfigKey);
            
            List<SystemConfig> configs = systemConfigMapper.selectList(queryWrapper);
            return configs.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("根据分类获取配置失败: category={}", category, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public String getConfigValue(String configKey) {
        return getConfigValue(configKey, null);
    }
    
    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        try {
            SystemConfig config = systemConfigMapper.selectByConfigKey(configKey);
            return config != null ? config.getConfigValue() : defaultValue;
        } catch (Exception e) {
            logger.error("获取配置值失败: configKey={}", configKey, e);
            return defaultValue;
        }
    }
    
    @Override
    public Boolean getBooleanConfigValue(String configKey, Boolean defaultValue) {
        try {
            String value = getConfigValue(configKey);
            if (value == null) {
                return defaultValue;
            }
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            logger.error("获取布尔配置值失败: configKey={}", configKey, e);
            return defaultValue;
        }
    }
    
    @Override
    public Integer getIntegerConfigValue(String configKey, Integer defaultValue) {
        try {
            String value = getConfigValue(configKey);
            if (value == null) {
                return defaultValue;
            }
            return Integer.parseInt(value);
        } catch (Exception e) {
            logger.error("获取数字配置值失败: configKey={}", configKey, e);
            return defaultValue;
        }
    }
    
    @Override
    @Transactional
    public boolean updateConfig(String configKey, String configValue) {
        try {
            int result = systemConfigMapper.updateValueByKey(configKey, configValue);
            logger.info("更新配置成功: configKey={}, configValue={}", configKey, configValue);
            return result > 0;
        } catch (Exception e) {
            logger.error("更新配置失败: configKey={}, configValue={}", configKey, configValue, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean batchUpdateConfigs(Map<String, String> configs) {
        try {
            for (Map.Entry<String, String> entry : configs.entrySet()) {
                updateConfig(entry.getKey(), entry.getValue());
            }
            logger.info("批量更新配置成功: 共更新{}个配置", configs.size());
            return true;
        } catch (Exception e) {
            logger.error("批量更新配置失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean resetConfigToDefault(String configKey) {
        try {
            // 这里可以根据需要实现重置逻辑
            // 暂时返回false，表示不支持重置
            logger.warn("重置配置功能暂未实现: configKey={}", configKey);
            return false;
        } catch (Exception e) {
            logger.error("重置配置失败: configKey={}", configKey, e);
            return false;
        }
    }
    
    @Override
    public List<String> getConfigCategories() {
        try {
            LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(SystemConfig::getCategory);
            queryWrapper.groupBy(SystemConfig::getCategory);
            queryWrapper.orderByAsc(SystemConfig::getCategory);
            
            List<SystemConfig> configs = systemConfigMapper.selectList(queryWrapper);
            return configs.stream()
                    .map(SystemConfig::getCategory)
                    .map(Enum::name)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取配置分类失败", e);
            // 返回新的配置分类
            return Arrays.asList("MAINTENANCE", "FEATURES", "LIMITS", "GENERAL");
        }
    }
    
    /**
     * 转换为DTO
     */
    private SystemConfigDTO convertToDTO(SystemConfig config) {
        SystemConfigDTO dto = new SystemConfigDTO();
        dto.setId(config.getId());
        dto.setConfigKey(config.getConfigKey());
        dto.setConfigValue(config.getConfigValue());
        dto.setConfigType(config.getConfigType().name());
        dto.setDescription(config.getDescription());
        dto.setCategory(config.getCategory().name());
        dto.setIsSystem(config.getIsSystem());
        dto.setCreatedAt(config.getCreatedAt());
        dto.setUpdatedAt(config.getUpdatedAt());
        return dto;
    }
    
    @Override
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        try {
            // 获取系统基本信息
            systemInfo.put("systemName", getConfigValue("system.name", "XReadUp"));
            systemInfo.put("systemVersion", getConfigValue("system.version", "1.0.0"));
            systemInfo.put("systemDescription", getConfigValue("system.description", "智能英语阅读学习平台"));
            
            // 获取功能状态
            systemInfo.put("aiTranslationEnabled", isFeatureEnabled("features.ai_translation"));
            systemInfo.put("vocabularyEnabled", isFeatureEnabled("features.vocabulary"));
            systemInfo.put("subscriptionEnabled", isFeatureEnabled("features.subscription"));
            
            // 获取维护状态
            systemInfo.put("maintenanceMode", isMaintenanceMode());
            systemInfo.put("maintenanceMessage", getMaintenanceMessage());
            
            logger.info("获取系统信息成功");
        } catch (Exception e) {
            logger.error("获取系统信息失败", e);
        }
        return systemInfo;
    }
    
    @Override
    public boolean isFeatureEnabled(String featureKey) {
        return getBooleanConfigValue(featureKey, false);
    }
    
    @Override
    public Integer getLimitValue(String limitKey, Integer defaultValue) {
        return getIntegerConfigValue(limitKey, defaultValue);
    }
    
    @Override
    public boolean isMaintenanceMode() {
        return getBooleanConfigValue("maintenance.enabled", false);
    }
    
    @Override
    public String getMaintenanceMessage() {
        return getConfigValue("maintenance.message", "系统正在维护中，请稍后再试");
    }
}

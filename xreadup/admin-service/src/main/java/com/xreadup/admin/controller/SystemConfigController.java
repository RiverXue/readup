package com.xreadup.admin.controller;

import com.xreadup.admin.dto.ApiResponse;
import com.xreadup.admin.dto.SystemConfigDTO;
import com.xreadup.admin.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 * 
 * @author XReadUp
 * @since 2025-10-12
 */
@RestController
@RequestMapping("/api/admin/system-config")
@Tag(name = "系统配置管理", description = "系统配置相关的操作接口")
public class SystemConfigController {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemConfigController.class);
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * 获取所有系统配置
     * 
     * @return 配置列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有系统配置", description = "获取系统中所有的配置项")
    public ApiResponse<List<SystemConfigDTO>> getAllConfigs() {
        try {
            logger.info("获取所有系统配置");
            List<SystemConfigDTO> configs = systemConfigService.getAllConfigs();
            return ApiResponse.success(configs);
        } catch (Exception e) {
            logger.error("获取所有系统配置失败", e);
            return ApiResponse.fail(500, "获取系统配置失败");
        }
    }
    
    /**
     * 根据分类获取系统配置
     * 
     * @param category 配置分类
     * @return 配置列表
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "根据分类获取系统配置", description = "根据配置分类获取对应的配置项")
    public ApiResponse<List<SystemConfigDTO>> getConfigsByCategory(
            @Parameter(description = "配置分类", required = true)
            @PathVariable String category) {
        try {
            logger.info("根据分类获取系统配置: category={}", category);
            List<SystemConfigDTO> configs = systemConfigService.getConfigsByCategory(category);
            return ApiResponse.success(configs);
        } catch (Exception e) {
            logger.error("根据分类获取系统配置失败: category={}", category, e);
            return ApiResponse.fail(500, "获取系统配置失败");
        }
    }
    
    /**
     * 获取配置分类列表
     * 
     * @return 分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取配置分类列表", description = "获取系统中所有的配置分类")
    public ApiResponse<List<String>> getConfigCategories() {
        try {
            logger.info("获取配置分类列表");
            List<String> categories = systemConfigService.getConfigCategories();
            return ApiResponse.success(categories);
        } catch (Exception e) {
            logger.error("获取配置分类列表失败", e);
            return ApiResponse.fail(500, "获取配置分类失败");
        }
    }
    
    /**
     * 根据配置键获取配置值
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    @GetMapping("/value/{configKey}")
    @Operation(summary = "根据配置键获取配置值", description = "根据配置键获取对应的配置值")
    public ApiResponse<String> getConfigValue(
            @Parameter(description = "配置键", required = true)
            @PathVariable String configKey) {
        try {
            logger.info("根据配置键获取配置值: configKey={}", configKey);
            String value = systemConfigService.getConfigValue(configKey);
            return ApiResponse.success(value);
        } catch (Exception e) {
            logger.error("根据配置键获取配置值失败: configKey={}", configKey, e);
            return ApiResponse.fail(500, "获取配置值失败");
        }
    }
    
    /**
     * 更新单个配置
     * 
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 更新结果
     */
    @PutMapping("/update/{configKey}")
    @Operation(summary = "更新单个配置", description = "更新指定配置键的配置值")
    public ApiResponse<Boolean> updateConfig(
            @Parameter(description = "配置键", required = true)
            @PathVariable String configKey,
            @Parameter(description = "配置值", required = true)
            @RequestParam String configValue) {
        try {
            logger.info("更新单个配置: configKey={}, configValue={}", configKey, configValue);
            boolean success = systemConfigService.updateConfig(configKey, configValue);
            if (success) {
                return ApiResponse.success(true);
            } else {
                return ApiResponse.fail(400, "更新配置失败");
            }
        } catch (Exception e) {
            logger.error("更新单个配置失败: configKey={}, configValue={}", configKey, configValue, e);
            return ApiResponse.fail(500, "更新配置失败");
        }
    }
    
    /**
     * 批量更新配置
     * 
     * @param configs 配置Map
     * @return 更新结果
     */
    @PutMapping("/batch-update")
    @Operation(summary = "批量更新配置", description = "批量更新多个配置项")
    public ApiResponse<Boolean> batchUpdateConfigs(
            @Parameter(description = "配置Map", required = true)
            @RequestBody Map<String, String> configs) {
        try {
            logger.info("批量更新配置: 共{}个配置", configs.size());
            boolean success = systemConfigService.batchUpdateConfigs(configs);
            if (success) {
                return ApiResponse.success(true);
            } else {
                return ApiResponse.fail(400, "批量更新配置失败");
            }
        } catch (Exception e) {
            logger.error("批量更新配置失败", e);
            return ApiResponse.fail(500, "批量更新配置失败");
        }
    }
    
    /**
     * 重置配置为默认值
     * 
     * @param configKey 配置键
     * @return 重置结果
     */
    @PutMapping("/reset/{configKey}")
    @Operation(summary = "重置配置为默认值", description = "将指定配置重置为默认值")
    public ApiResponse<Boolean> resetConfigToDefault(
            @Parameter(description = "配置键", required = true)
            @PathVariable String configKey) {
        try {
            logger.info("重置配置为默认值: configKey={}", configKey);
            boolean success = systemConfigService.resetConfigToDefault(configKey);
            if (success) {
                return ApiResponse.success(true);
            } else {
                return ApiResponse.fail(400, "重置配置失败");
            }
        } catch (Exception e) {
            logger.error("重置配置失败: configKey={}", configKey, e);
            return ApiResponse.fail(500, "重置配置失败");
        }
    }
    
    /**
     * 获取系统信息
     * 
     * @return 系统信息
     */
    @GetMapping("/system-info")
    @Operation(summary = "获取系统信息", description = "获取系统的基本信息")
    public ApiResponse<Map<String, Object>> getSystemInfo() {
        try {
            logger.info("获取系统信息");
            Map<String, Object> systemInfo = systemConfigService.getSystemInfo();
            return ApiResponse.success(systemInfo);
        } catch (Exception e) {
            logger.error("获取系统信息失败", e);
            return ApiResponse.fail(500, "获取系统信息失败");
        }
    }
    
    /**
     * 检查功能是否启用
     * 
     * @param featureKey 功能键
     * @return 功能状态
     */
    @GetMapping("/feature/{featureKey}/enabled")
    @Operation(summary = "检查功能是否启用", description = "检查指定功能是否启用")
    public ApiResponse<Boolean> isFeatureEnabled(
            @Parameter(description = "功能键", required = true)
            @PathVariable String featureKey) {
        try {
            logger.info("检查功能是否启用: featureKey={}", featureKey);
            boolean enabled = systemConfigService.isFeatureEnabled(featureKey);
            return ApiResponse.success(enabled);
        } catch (Exception e) {
            logger.error("检查功能状态失败: featureKey={}", featureKey, e);
            return ApiResponse.fail(500, "检查功能状态失败");
        }
    }
    
    /**
     * 获取业务限制值
     * 
     * @param limitKey 限制键
     * @return 限制值
     */
    @GetMapping("/limit/{limitKey}")
    @Operation(summary = "获取业务限制值", description = "获取指定业务限制的值")
    public ApiResponse<Integer> getLimitValue(
            @Parameter(description = "限制键", required = true)
            @PathVariable String limitKey,
            @Parameter(description = "默认值")
            @RequestParam(required = false, defaultValue = "0") Integer defaultValue) {
        try {
            logger.info("获取业务限制值: limitKey={}, defaultValue={}", limitKey, defaultValue);
            Integer value = systemConfigService.getLimitValue(limitKey, defaultValue);
            return ApiResponse.success(value);
        } catch (Exception e) {
            logger.error("获取业务限制值失败: limitKey={}", limitKey, e);
            return ApiResponse.fail(500, "获取业务限制值失败");
        }
    }
    
    /**
     * 检查系统维护状态
     * 
     * @return 维护状态
     */
    @GetMapping("/maintenance/status")
    @Operation(summary = "检查系统维护状态", description = "检查系统是否处于维护模式")
    public ApiResponse<Map<String, Object>> getMaintenanceStatus() {
        try {
            logger.info("检查系统维护状态");
            Map<String, Object> status = new HashMap<>();
            status.put("maintenanceMode", systemConfigService.isMaintenanceMode());
            status.put("maintenanceMessage", systemConfigService.getMaintenanceMessage());
            return ApiResponse.success(status);
        } catch (Exception e) {
            logger.error("检查系统维护状态失败", e);
            return ApiResponse.fail(500, "检查系统维护状态失败");
        }
    }
    
    /**
     * 内部接口：检查系统维护状态（无需认证）
     * 供网关等内部服务调用
     * 
     * @return 维护状态
     */
    @GetMapping("/internal/maintenance/status")
    @Operation(summary = "内部接口：检查系统维护状态", description = "供网关等内部服务调用，无需认证")
    public ApiResponse<Map<String, Object>> getMaintenanceStatusInternal() {
        try {
            logger.debug("内部调用：检查系统维护状态");
            Map<String, Object> status = new HashMap<>();
            status.put("maintenanceMode", systemConfigService.isMaintenanceMode());
            status.put("maintenanceMessage", systemConfigService.getMaintenanceMessage());
            return ApiResponse.success(status);
        } catch (Exception e) {
            logger.error("内部调用：检查系统维护状态失败", e);
            return ApiResponse.fail(500, "检查系统维护状态失败");
        }
    }
}

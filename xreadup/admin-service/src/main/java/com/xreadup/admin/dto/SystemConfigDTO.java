package com.xreadup.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置DTO
 * 
 * @author XReadUp
 * @since 2025-10-12
 */
@Data
public class SystemConfigDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置类型
     */
    private String configType;
    
    /**
     * 配置描述
     */
    private String description;
    
    /**
     * 配置分类
     */
    private String category;
    
    /**
     * 是否为系统配置
     */
    private Boolean isSystem;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

/**
 * 系统配置更新请求DTO
 */
@Data
class SystemConfigUpdateDTO {
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
}

/**
 * 系统配置批量更新请求DTO
 */
@Data
class SystemConfigBatchUpdateDTO {
    
    /**
     * 配置列表
     */
    private java.util.List<SystemConfigUpdateDTO> configs;
}

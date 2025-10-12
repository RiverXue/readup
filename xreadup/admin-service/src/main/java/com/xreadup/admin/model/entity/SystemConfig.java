package com.xreadup.admin.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 
 * @author XReadUp
 * @since 2025-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("system_config")
public class SystemConfig {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 配置键
     */
    @TableField("config_key")
    private String configKey;
    
    /**
     * 配置值
     */
    @TableField(value = "config_value", typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    private String configValue;
    
    /**
     * 配置类型
     */
    @TableField("config_type")
    private ConfigType configType;
    
    /**
     * 配置描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 配置分类
     */
    @TableField("category")
    private ConfigCategory category;
    
    /**
     * 是否为系统配置（不可删除）
     */
    @TableField("is_system")
    private Boolean isSystem;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 配置类型枚举
     */
    public enum ConfigType {
        STRING, NUMBER, BOOLEAN, JSON
    }
    
    /**
     * 配置分类枚举 - 重新设计
     */
    public enum ConfigCategory {
        MAINTENANCE("系统维护"),
        FEATURES("功能开关"),
        LIMITS("业务限制"),
        GENERAL("通用设置");
        
        private final String description;
        
        ConfigCategory(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}

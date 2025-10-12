package com.xreadup.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xreadup.admin.model.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统配置Mapper接口
 * 
 * @author XReadUp
 * @since 2025-10-12
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    
    /**
     * 根据配置键查询配置
     * 
     * @param configKey 配置键
     * @return 系统配置
     */
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);
    
    /**
     * 根据分类查询配置列表
     * 
     * @param category 配置分类
     * @return 配置列表
     */
    List<SystemConfig> selectByCategory(@Param("category") String category);
    
    /**
     * 根据配置键更新配置值
     * 
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 更新行数
     */
    int updateValueByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
    
    /**
     * 批量更新配置
     * 
     * @param configs 配置列表
     * @return 更新行数
     */
    int batchUpdateConfigs(@Param("configs") List<SystemConfig> configs);
}

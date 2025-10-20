package com.xreadup.ai.articleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xreadup.ai.articleservice.model.entity.ContentFilterLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 内容过滤日志Mapper接口
 */
@Mapper
public interface ContentFilterLogMapper extends BaseMapper<ContentFilterLog> {
}

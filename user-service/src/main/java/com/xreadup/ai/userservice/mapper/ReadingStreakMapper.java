package com.xreadup.ai.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xreadup.ai.userservice.entity.ReadingStreak;
import org.apache.ibatis.annotations.Mapper;

/**
 * 阅读打卡Mapper接口
 */
@Mapper
public interface ReadingStreakMapper extends BaseMapper<ReadingStreak> {
}
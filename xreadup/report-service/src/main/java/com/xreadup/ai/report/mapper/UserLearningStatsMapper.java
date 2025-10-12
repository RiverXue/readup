package com.xreadup.ai.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xreadup.ai.report.entity.UserLearningStats;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户学习统计Mapper接口
 */
@Mapper
public interface UserLearningStatsMapper extends BaseMapper<UserLearningStats> {
}
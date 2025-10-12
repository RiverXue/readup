package com.xreadup.ai.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xreadup.ai.userservice.entity.Subscription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订阅数据访问层
 */
@Mapper
public interface SubscriptionMapper extends BaseMapper<Subscription> {
}
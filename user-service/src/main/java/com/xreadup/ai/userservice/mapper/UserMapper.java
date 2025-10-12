package com.xreadup.ai.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xreadup.ai.userservice.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
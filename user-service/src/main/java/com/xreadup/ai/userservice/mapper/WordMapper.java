package com.xreadup.ai.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xreadup.ai.userservice.entity.Word;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 生词Mapper接口
 */
@Mapper
public interface WordMapper extends BaseMapper<Word> {
    List<Word> findByUserId(@Param("userId") Long userId);
}
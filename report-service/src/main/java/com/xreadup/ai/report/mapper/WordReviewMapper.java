package com.xreadup.ai.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xreadup.ai.report.entity.WordReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 单词复习记录Mapper接口
 */
@Mapper
public interface WordReviewMapper extends BaseMapper<WordReview> {

    /**
     * 获取用户复习次数
     */
    int getReviewCount(@Param("userId") Long userId, @Param("wordId") Long wordId);

    /**
     * 获取指定日期范围的复习次数
     */
    int getReviewCountInRange(@Param("userId") Long userId, 
                            @Param("startDate") LocalDate startDate, 
                            @Param("endDate") LocalDate endDate);

    /**
     * 获取复习成功率
     */
    double getSuccessRate(@Param("userId") Long userId);

    /**
     * 获取复习日历数据
     */
    List<Map<String, Object>> getReviewCalendar(@Param("userId") Long userId, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
}
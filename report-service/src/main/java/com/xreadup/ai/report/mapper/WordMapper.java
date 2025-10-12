package com.xreadup.ai.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xreadup.ai.report.entity.UserLearningStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface WordMapper extends BaseMapper<UserLearningStats> {

    @Select("SELECT DATE(added_at) as date, COUNT(*) as count FROM word WHERE user_ids LIKE CONCAT('%', #{userId}, '%') AND DATE(added_at) >= #{startDate} AND DATE(added_at) <= #{endDate} GROUP BY DATE(added_at) ORDER BY DATE(added_at)")
    List<Map<String, Object>> getDailyWordCount(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COUNT(*) FROM word WHERE user_ids LIKE CONCAT('%', #{userId}, '%')")
    int countUserWords(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM word WHERE user_ids LIKE CONCAT('%', #{userId}, '%') AND DATE(added_at) >= #{date}")
    int countWordsAfterDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT review_status as difficulty, COUNT(*) as count FROM word WHERE user_ids LIKE CONCAT('%', #{userId}, '%') GROUP BY review_status")
    List<Map<String, Object>> getDifficultyDistribution(@Param("userId") Long userId);

    @Select("SELECT DATE(added_at) as date, COUNT(*) as count FROM word WHERE user_ids LIKE CONCAT('%', #{userId}, '%') AND DATE(added_at) >= #{startDate} GROUP BY DATE(added_at) ORDER BY DATE(added_at)")
    List<Map<String, Object>> getDailyWordCounts(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}
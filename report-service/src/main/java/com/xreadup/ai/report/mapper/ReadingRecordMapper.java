package com.xreadup.ai.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xreadup.ai.report.dto.ReadingTimeData;
import com.xreadup.ai.report.entity.ReadingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReadingRecordMapper extends BaseMapper<ReadingRecord> {

    @Select("SELECT COALESCE(SUM(read_time_sec)/60, 0) FROM reading_log WHERE user_id = #{userId} AND DATE(finished_at) = #{date}")
    Integer getTodayReadingMinutes(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT COALESCE(SUM(read_time_sec)/60, 0) FROM reading_log WHERE user_id = #{userId} AND DATE(finished_at) >= #{startDate}")
    Integer getTotalReadingMinutesSince(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Select("SELECT COALESCE(AVG(read_time_sec)/60, 0) FROM reading_log WHERE user_id = #{userId} AND DATE(finished_at) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
    Integer getWeeklyAverageMinutes(@Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(read_time_sec)/60, 0) FROM reading_log WHERE user_id = #{userId}")
    Integer getTotalReadingMinutes(@Param("userId") Long userId);

    @Select("SELECT COALESCE(COUNT(*), 0) FROM reading_log WHERE user_id = #{userId}")
    Integer getTotalArticlesRead(@Param("userId") Long userId);

    @Select("SELECT DATE(finished_at) as date, COALESCE(SUM(read_time_sec)/60, 0) as minutes, COALESCE(COUNT(*), 0) as articles FROM reading_log WHERE user_id = #{userId} AND DATE(finished_at) >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) GROUP BY DATE(finished_at) ORDER BY DATE(finished_at)")
    List<ReadingTimeData.DailyReading> getDailyReadings(@Param("userId") Long userId, @Param("days") int days);

    @Select("SELECT COALESCE(COUNT(*), 0) as count, COALESCE(SUM(read_time_sec)/60, 0) as totalMinutes FROM reading_log WHERE user_id = #{userId}")
    List<ReadingTimeData.DifficultyStats> getDifficultyStats(@Param("userId") Long userId);

    @Select("SELECT id, user_id, article_id, read_time_sec, finished_at FROM reading_log WHERE user_id = #{userId} AND read_date BETWEEN #{startDate} AND #{endDate} ORDER BY read_date")
    List<ReadingRecord> getReadingRecordsByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
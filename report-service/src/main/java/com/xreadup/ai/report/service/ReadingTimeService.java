package com.xreadup.ai.report.service;

import com.xreadup.ai.report.dto.ReadingTimeData;
import com.xreadup.ai.report.entity.ReadingRecord;
import com.xreadup.ai.report.mapper.ReadingRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 阅读时长统计服务
 * 统计用户的阅读时长和学习数据
 */
@Service
public class ReadingTimeService {

    private static final Logger logger = Logger.getLogger(ReadingTimeService.class.getName());

    @Autowired
    private ReadingRecordMapper readingRecordMapper;

    /**
     * 获取用户阅读时长统计
     * @param userId 用户ID
     * @return 阅读时长统计信息
     */
    public ReadingTimeData getReadingStats(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正数");
        }
        
        try {
            ReadingTimeData data = new ReadingTimeData();
            
            // 获取今日阅读时长
            Integer todayMinutes = readingRecordMapper.getTodayReadingMinutes(userId, LocalDate.now());
            data.setTodayMinutes(todayMinutes != null ? todayMinutes : 0);
            
            // 获取本周平均阅读时长
            Integer weeklyAverage = readingRecordMapper.getWeeklyAverageMinutes(userId);
            data.setWeeklyAverageMinutes(weeklyAverage != null ? weeklyAverage : 0);
            
            // 获取总阅读时长
            Integer totalMinutes = readingRecordMapper.getTotalReadingMinutes(userId);
            data.setTotalMinutes(totalMinutes != null ? totalMinutes : 0);
            
            // 获取总阅读文章数
            Integer totalArticles = readingRecordMapper.getTotalArticlesRead(userId);
            data.setTotalArticles(totalArticles != null ? totalArticles : 0);
            
            // 获取每日阅读数据
            List<ReadingTimeData.DailyReading> dailyReadings = readingRecordMapper.getDailyReadings(userId, 7);
            data.setDailyReadings(dailyReadings != null ? dailyReadings : new ArrayList<>());
            
            // 获取难度分布统计
            List<ReadingTimeData.DifficultyStats> difficultyStats = readingRecordMapper.getDifficultyStats(userId);
            data.setDifficultyStats(difficultyStats != null ? difficultyStats : new ArrayList<>());
            
            logger.info("用户ID: " + userId + " 阅读统计: " + data);
            return data;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "获取阅读统计失败，用户ID: " + userId, e);
            throw new RuntimeException("获取阅读统计失败，请稍后重试", e);
        }
    }

    /**
     * 获取用户阅读趋势
     * @param userId 用户ID
     * @param days 统计天数
     * @return 阅读趋势数据
     */
    public Map<String, Object> getReadingTrend(Long userId, int days) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正数");
        }
        if (days <= 0 || days > 365) {
            throw new IllegalArgumentException("天数必须在1-365之间");
        }
        
        try {
            Map<String, Object> trend = new HashMap<>();
            
            // 获取指定天数内的每日阅读数据
            List<ReadingTimeData.DailyReading> dailyReadings = readingRecordMapper.getDailyReadings(userId, days);
            
            // 计算总阅读时长和平均阅读时长
            int totalMinutes = dailyReadings.stream()
                    .mapToInt(ReadingTimeData.DailyReading::getMinutes)
                    .sum();
            
            double averagePerDay = days > 0 ? (double) totalMinutes / days : 0.0;
            
            trend.put("dailyReadings", dailyReadings != null ? dailyReadings : new ArrayList<>());
            trend.put("totalMinutes", totalMinutes);
            trend.put("averagePerDay", Math.round(averagePerDay * 100) / 100.0);
            
            logger.info("用户ID: " + userId + " 阅读趋势: " + trend);
            return trend;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "获取阅读趋势失败，用户ID: " + userId, e);
            throw new RuntimeException("获取阅读趋势失败，请稍后重试", e);
        }
    }

    /**
     * 记录用户阅读行为
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param minutesRead 阅读时长（分钟）
     */
    public void recordReading(Long userId, Long articleId, int minutesRead) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正数");
        }
        if (articleId == null || articleId <= 0) {
            throw new IllegalArgumentException("文章ID必须为正数");
        }
        if (minutesRead < 0) {
            throw new IllegalArgumentException("阅读时长不能为负数");
        }
        
        try {
            ReadingRecord record = new ReadingRecord();
            record.setUserId(userId);
            record.setArticleId(articleId);
            record.setMinutesRead(minutesRead);
            record.setReadDate(LocalDate.now());
            record.setCreatedAt(LocalDateTime.now());
            
            readingRecordMapper.insert(record);
            logger.info("记录阅读行为成功 - 用户ID: " + userId + ", 文章ID: " + articleId + ", 时长: " + minutesRead + "分钟");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "记录阅读行为失败 - 用户ID: " + userId + ", 文章ID: " + articleId, e);
            throw new RuntimeException("记录阅读行为失败，请稍后重试", e);
        }
    }
}
package com.xreadup.ai.report.service;

import com.xreadup.ai.report.dto.VocabularyGrowthData;
import com.xreadup.ai.report.mapper.WordMapper;
import com.xreadup.ai.report.mapper.UserLearningStatsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 词汇增长曲线服务
 * 统计用户的词汇学习增长趋势
 */
@Service
public class VocabularyGrowthService {

    private static final Logger logger = Logger.getLogger(VocabularyGrowthService.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");

    @Autowired
    private WordMapper wordMapper;
    
    @Autowired
    private UserLearningStatsMapper userLearningStatsMapper;

    /**
     * 获取用户词汇增长曲线
     * @param userId 用户ID
     * @param days 统计天数
     * @return 包含日期和词汇数量的VocabularyGrowthData
     */
    public VocabularyGrowthData getGrowthCurve(Long userId, int days) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正数");
        }
        if (days <= 0 || days > 365) {
            throw new IllegalArgumentException("天数必须在1-365之间");
        }
        
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        // 获取过去N天的词汇增长数据
        List<Map<String, Object>> dailyData = wordMapper.getDailyWordCount(userId, startDate, endDate);
        
        // 构建日期和计数列表
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = endDate.minusDays(i);
            dates.add(date.format(DATE_FORMATTER));
            
            // 从查询结果中查找对应日期的计数
            int count = 0;
            for (Map<String, Object> data : dailyData) {
                Object dateObj = data.get("date");
                if (dateObj != null) {
                    try {
                        LocalDate dataDate = parseDate(dateObj);
                        if (dataDate != null && dataDate.equals(date)) {
                            Object countObj = data.get("count");
                            if (countObj instanceof Number) {
                                count = ((Number) countObj).intValue();
                            }
                            break;
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "日期解析失败: " + dateObj, e);
                        continue;
                    }
                }
            }
            counts.add(count);
        }
        
        // 获取总词汇量和本周新词汇
        Integer totalWords = wordMapper.countUserWords(userId);
        Integer weeklyNewWords = wordMapper.countWordsAfterDate(userId, LocalDate.now().minusDays(7));
        
        // 计算日均新增
        Double dailyAverage = days > 0 ? weeklyNewWords.doubleValue() / 7 : 0.0;
        
        return new VocabularyGrowthData(dates, counts, 
            totalWords != null ? totalWords : 0, 
            weeklyNewWords != null ? weeklyNewWords : 0, 
            dailyAverage);
    }

    /**
     * 统一日期解析方法
     * @param dateObj 日期对象
     * @return LocalDate实例
     */
    private LocalDate parseDate(Object dateObj) {
        if (dateObj instanceof String) {
            return LocalDate.parse((String) dateObj);
        } else if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate();
        } else if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        }
        return null;
    }

    /**
     * 获取用户词汇学习统计
     * @param userId 用户ID
     * @return 词汇学习统计信息
     */
    public Map<String, Object> getVocabularyStats(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正数");
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取总词汇量
            Integer totalWords = wordMapper.countUserWords(userId);
            
            // 获取今日新增词汇
            LocalDate today = LocalDate.now();
            Integer dailyNewWords = 0;
            try {
                List<Map<String, Object>> todayData = wordMapper.getDailyWordCount(userId, today, today);
                if (todayData != null && !todayData.isEmpty()) {
                    Object countObj = todayData.get(0).get("count");
                    if (countObj instanceof Number) {
                        dailyNewWords = ((Number) countObj).intValue();
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "获取今日新增词汇失败", e);
                dailyNewWords = 0;
            }
            
            // 获取本周新增词汇
            Integer weeklyNewWords = 0;
            try {
                weeklyNewWords = wordMapper.countWordsAfterDate(userId, LocalDate.now().minusDays(7));
            } catch (Exception e) {
                logger.log(Level.WARNING, "获取本周新增词汇失败", e);
                weeklyNewWords = 0;
            }
            
            // 获取本月新增词汇
            Integer monthlyNewWords = 0;
            try {
                monthlyNewWords = wordMapper.countWordsAfterDate(userId, LocalDate.now().minusDays(30));
            } catch (Exception e) {
                logger.log(Level.WARNING, "获取本月新增词汇失败", e);
                monthlyNewWords = 0;
            }
            
            // 获取难度分布
            Map<String, Integer> difficultyDistribution = new HashMap<>();
            try {
                List<Map<String, Object>> rawDistribution = wordMapper.getDifficultyDistribution(userId);
                if (rawDistribution != null) {
                    for (Map<String, Object> item : rawDistribution) {
                        String difficulty = (String) item.get("difficulty");
                        Object countObj = item.get("count");
                        if (difficulty != null && countObj instanceof Number) {
                            difficultyDistribution.put(difficulty, ((Number) countObj).intValue());
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "获取难度分布失败", e);
                difficultyDistribution = new HashMap<>();
            }
            
            stats.put("totalWords", totalWords != null ? totalWords : 0);
            stats.put("dailyNewWords", dailyNewWords);
            stats.put("weeklyNewWords", weeklyNewWords != null ? weeklyNewWords : 0);
            stats.put("monthlyNewWords", monthlyNewWords != null ? monthlyNewWords : 0);
            stats.put("difficultyDistribution", difficultyDistribution);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "获取词汇统计失败，用户ID: " + userId, e);
            // 返回实际数据而不是默认值
            stats.put("totalWords", 0);
            stats.put("dailyNewWords", 0);
            stats.put("weeklyNewWords", 0);
            stats.put("monthlyNewWords", 0);
            stats.put("difficultyDistribution", new HashMap<>());
        }
        
        return stats;
    }
}
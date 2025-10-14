package com.xreadup.ai.report.service;

import com.xreadup.ai.report.dto.ReviewWordDto;
import com.xreadup.ai.report.dto.VocabularyGrowthData;
import com.xreadup.ai.report.entity.UserVocabulary;
import com.xreadup.ai.report.mapper.UserVocabularyMapper;
import com.xreadup.ai.report.mapper.WordMapper;
import com.xreadup.ai.report.mapper.UserLearningStatsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    
    @Autowired
    private UserVocabularyMapper userVocabularyMapper;


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
     * 获取用户今日待复习单词列表
     * @param userId 用户ID
     * @return 待复习单词列表
     */
    public List<ReviewWordDto> getTodayReviewWords(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正数");
        }
        
        try {
            return userVocabularyMapper.getWordsForReview(userId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "获取待复习单词失败，用户ID: " + userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 计算复习进度百分比
     * @param nextReviewTime 下次复习时间
     * @return 复习进度百分比
     */
    public int calculateReviewProgress(String nextReviewTime) {
        try {
            if (nextReviewTime == null || nextReviewTime.trim().isEmpty()) {
                return 50;
            }
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reviewDateTime;
            
            // 处理不同格式的日期
            if (nextReviewTime.contains("/")) {
                // 处理YYYY/MM/DD HH:MM:SS格式
                String[] parts = nextReviewTime.split(" ");
                String datePart = parts[0];
                String timePart = parts.length > 1 ? parts[1] : "00:00:00";
                
                String[] dateComponents = datePart.split("/");
                String[] timeComponents = timePart.split(":");
                
                int year = Integer.parseInt(dateComponents[0]);
                int month = Integer.parseInt(dateComponents[1]) - 1; // 月份从0开始
                int day = Integer.parseInt(dateComponents[2]);
                int hour = timeComponents.length > 0 ? Integer.parseInt(timeComponents[0]) : 0;
                int minute = timeComponents.length > 1 ? Integer.parseInt(timeComponents[1]) : 0;
                int second = timeComponents.length > 2 ? Integer.parseInt(timeComponents[2]) : 0;
                
                reviewDateTime = LocalDateTime.of(year, month, day, hour, minute, second);
            } else {
                // 处理标准日期格式
                reviewDateTime = LocalDateTime.parse(nextReviewTime);
            }
            
            // 计算时间差（毫秒）
            long timeDiff = java.time.Duration.between(now, reviewDateTime).toMillis();
            long dayInMs = 24 * 60 * 60 * 1000;
            
            // 如果已经过了复习时间，进度条显示100%
            if (timeDiff <= 0) {
                return 100;
            }
            
            // 计算剩余时间占一天的百分比
            int progress = Math.min(100, Math.max(0, (int)((dayInMs - timeDiff) * 100 / dayInMs)));
            return Math.round(progress);
        } catch (Exception e) {
            logger.log(Level.WARNING, "计算复习进度失败: " + nextReviewTime, e);
            return 50;
        }
    }
    
    /**
     * 格式化下次复习时间
     * @param nextReviewTime 下次复习时间
     * @return 格式化后的时间字符串
     */
    public String formatNextReviewTime(String nextReviewTime) {
        try {
            if (nextReviewTime == null || nextReviewTime.trim().isEmpty()) {
                return "时间格式无效";
            }
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reviewDateTime;
            
            // 处理不同格式的日期
            if (nextReviewTime.contains("/")) {
                // 处理YYYY/MM/DD HH:MM:SS格式
                String[] parts = nextReviewTime.split(" ");
                String datePart = parts[0];
                String timePart = parts.length > 1 ? parts[1] : "00:00:00";
                
                String[] dateComponents = datePart.split("/");
                String[] timeComponents = timePart.split(":");
                
                int year = Integer.parseInt(dateComponents[0]);
                int month = Integer.parseInt(dateComponents[1]) - 1; // 月份从0开始
                int day = Integer.parseInt(dateComponents[2]);
                int hour = timeComponents.length > 0 ? Integer.parseInt(timeComponents[0]) : 0;
                int minute = timeComponents.length > 1 ? Integer.parseInt(timeComponents[1]) : 0;
                int second = timeComponents.length > 2 ? Integer.parseInt(timeComponents[2]) : 0;
                
                reviewDateTime = LocalDateTime.of(year, month, day, hour, minute, second);
            } else {
                // 处理标准日期格式
                reviewDateTime = LocalDateTime.parse(nextReviewTime);
            }
            
            // 计算时间差（小时）
            long diffHours = java.time.Duration.between(now, reviewDateTime).toHours();
            
            // 处理负时间情况（复习时间已过）
            if (diffHours < 0) {
                return "已逾期" + Math.abs(diffHours) + "小时";
            } else if (diffHours < 24) {
                return diffHours + "小时后复习";
            } else {
                long diffDays = diffHours / 24;
                return diffDays + "天后复习";
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "格式化复习时间失败: " + nextReviewTime, e);
            return "时间格式无效";
        }
    }
    
    /**
     * 记录单词复习结果
     * @param userId 用户ID
     * @param wordId 单词ID
     * @param isRemembered 是否记住
     * @return 是否操作成功
     */
    public boolean recordReviewResult(Long userId, Long wordId, boolean isRemembered) {
        try {
            if (userId == null || userId <= 0 || wordId == null || wordId <= 0) {
                throw new IllegalArgumentException("用户ID和单词ID必须为正数");
            }
            
            // 获取当前单词状态
            String currentStatus = getUserWordStatus(wordId);
            
            // 根据当前状态和复习结果确定新的状态
            String newStatus = determineNewBackendStatus(currentStatus, isRemembered);
            
            // 计算下次复习日期（基于艾宾浩斯记忆曲线）
            LocalDate nextReviewDate = calculateNextReviewDate(newStatus);
            
            // 更新单词的复习状态和下次复习时间
            int rowsAffected = userVocabularyMapper.updateReviewStatusAndStatus(wordId, nextReviewDate, newStatus);
            
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "记录复习结果失败，用户ID: " + userId + "，单词ID: " + wordId, e);
            return false;
        }
    }
    
    /**
     * 获取用户单词状态
     * @param wordId 单词ID
     * @return 单词状态
     */
    private String getUserWordStatus(Long wordId) {
        try {
            // 查询数据库获取单词当前状态
            UserVocabulary userVocabulary = userVocabularyMapper.selectById(wordId);
            if (userVocabulary != null && userVocabulary.getReviewStatus() != null) {
                return userVocabulary.getReviewStatus();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "获取单词状态失败，单词ID: " + wordId, e);
        }
        // 默认返回new状态
        return "new";
    }
    
    /**
     * 根据当前状态和复习结果确定新的后端状态
     * @param currentStatus 当前状态
     * @param reviewResult 复习结果
     * @return 新状态
     */
    private String determineNewBackendStatus(String currentStatus, boolean reviewResult) {
        if (reviewResult) {
            // 复习成功
            if ("new".equals(currentStatus)) {
                return "learning";
            } else if ("learning".equals(currentStatus)) {
                return "mastered";
            } else {
                // 已掌握的单词，复习成功后保持已掌握状态
                return "mastered";
            }
        } else {
            // 复习失败
            if ("mastered".equals(currentStatus)) {
                return "learning";
            } else if ("learning".equals(currentStatus)) {
                return "new";
            } else {
                // 新单词，复习失败后保持新单词状态
                return "new";
            }
        }
    }
    
    /**
     * 根据复习结果计算下次复习日期（基于艾宾浩斯记忆曲线）
     * @param newStatus 新状态
     * @return 下次复习日期
     */
    private LocalDate calculateNextReviewDate(String newStatus) {
        LocalDate today = LocalDate.now();
        
        // 根据艾宾浩斯记忆曲线和单词状态确定复习间隔
        if ("new".equals(newStatus)) {
            // 新单词：当天复习
            return today;
        } else if ("learning".equals(newStatus)) {
            // 学习中：1天后复习
            return today.plusDays(1);
        } else if ("mastered".equals(newStatus)) {
            // 已掌握：3天后复习（统一使用3天，与user-service保持一致）
            return today.plusDays(3);
        }
        
        // 默认情况：1天后复习
        return today.plusDays(1);
    }
    
    /**
     * 设置单词为不再巩固
     * @param wordId 单词ID
     * @return 是否操作成功
     */
    public boolean setWordAsNoLongerReview(Long wordId) {
        try {
            if (wordId == null || wordId <= 0) {
                throw new IllegalArgumentException("单词ID必须为正数");
            }
            
            // 设置nextReviewTime为100年后，这样单词就不会再进入复习流程
            LocalDate farFutureDate = LocalDate.now().plusYears(100);
            
            // 更新单词的复习状态
            int rowsAffected = userVocabularyMapper.updateReviewStatus(wordId, farFutureDate, true);
            
            // 同时更新单词状态为已掌握
            userVocabularyMapper.updateReviewStatusAndStatus(wordId, farFutureDate, "mastered");
            
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "设置单词为不再巩固失败，单词ID: " + wordId, e);
            return false;
        }
    }

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
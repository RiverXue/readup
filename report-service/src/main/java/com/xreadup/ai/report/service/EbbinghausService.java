package com.xreadup.ai.report.service;

import com.xreadup.ai.report.dto.ReviewWordDto;
import com.xreadup.ai.report.entity.UserVocabulary;
import com.xreadup.ai.report.mapper.UserVocabularyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EbbinghausService {

    private static final Logger logger = Logger.getLogger(EbbinghausService.class.getName());

    @Autowired
    private UserVocabularyMapper vocabularyMapper;

    public List<ReviewWordDto> getTodayReviewWords(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正数");
        }
        
        try {
            List<ReviewWordDto> words = vocabularyMapper.getWordsForReview(userId);
            logger.info("用户ID: " + userId + " 获取今日复习单词 " + (words != null ? words.size() : 0) + " 个");
            return words;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "获取今日复习单词失败，用户ID: " + userId, e);
            throw new RuntimeException("获取复习单词失败，请稍后重试", e);
        }
    }

    public void recordReview(Long userId, Long wordId, boolean result) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正数");
        }
        if (wordId == null || wordId <= 0) {
            throw new IllegalArgumentException("单词ID必须为正数");
        }
        
        try {
            UserVocabulary word = vocabularyMapper.selectById(wordId);
            if (word != null) {
                Integer currentCount = word.getReviewCount() != null ? word.getReviewCount() : 0;
                LocalDate nextReviewDate = calculateNextReviewDate(currentCount + 1, result);
                vocabularyMapper.updateReviewStatus(wordId, nextReviewDate, result);
                logger.info("用户ID: " + userId + " 复习单词ID: " + wordId + " 结果: " + result + " 下次复习: " + nextReviewDate);
            } else {
                logger.warning("未找到单词ID: " + wordId + " 用户ID: " + userId);
                throw new RuntimeException("未找到指定单词");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "记录复习失败，用户ID: " + userId + " 单词ID: " + wordId, e);
            throw new RuntimeException("记录复习失败，请稍后重试", e);
        }
    }

    public LocalDate calculateNextReviewDate(int reviewCount, boolean lastResult) {
        if (!lastResult) {
            return LocalDate.now().plusDays(1);
        }
        
        int[] intervals = {1, 3, 7, 15, 30, 60, 120, 240};
        int index = Math.min(reviewCount - 1, intervals.length - 1);
        LocalDate nextDate = LocalDate.now().plusDays(intervals[index]);
        logger.fine("计算下次复习日期: reviewCount=" + reviewCount + ", lastResult=" + lastResult + ", nextDate=" + nextDate);
        return nextDate;
    }

    public Map<String, Object> getReviewStats(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正数");
        }
        
        try {
            int totalWords = vocabularyMapper.countTotalWords(userId);
            int reviewedWords = vocabularyMapper.countReviewedWords(userId);
            int successfulReviews = vocabularyMapper.countSuccessfulReviews(userId);
            int todayReviews = vocabularyMapper.countWordsForReview(userId);

            double successRate = reviewedWords > 0 ? (double) successfulReviews / reviewedWords * 100 : 0;
            
            Map<String, Object> stats = Map.of(
                "totalWords", totalWords,
                "reviewedWords", reviewedWords,
                "successfulReviews", successfulReviews,
                "todayReviews", todayReviews,
                "successRate", Math.round(successRate * 100) / 100.0
            );
            
            logger.info("用户ID: " + userId + " 复习统计: " + stats);
            return stats;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "获取复习统计失败，用户ID: " + userId, e);
            throw new RuntimeException("获取复习统计失败，请稍后重试", e);
        }
    }

    public void updateAllUsersReviewSchedule() {
        logger.info("开始更新所有用户的复习计划");
        try {
            // 这里可以添加实际的批量更新逻辑
            // 例如：查询所有需要更新复习计划的用户并逐个处理
            logger.info("完成更新所有用户的复习计划 - 处理了0个用户（待实现）");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "更新复习计划失败", e);
            throw new RuntimeException("更新复习计划失败", e);
        }
    }

    public void cleanupExpiredReviews() {
        logger.info("开始清理过期复习记录");
        try {
            // 这里可以添加实际的清理逻辑
            // 例如：删除30天前的已复习记录
            logger.info("完成清理过期复习记录 - 清理了0条记录（待实现）");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "清理过期复习记录失败", e);
            throw new RuntimeException("清理过期复习记录失败", e);
        }
    }

    public void generateMonthlyReport() {
        logger.info("开始生成月度学习报告");
        try {
            // 这里可以添加实际的月度报告生成逻辑
            // 例如：统计过去一个月的学习数据并生成报告
            logger.info("完成生成月度学习报告 - 生成了0个报告（待实现）");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "生成月度报告失败", e);
            throw new RuntimeException("生成月度报告失败", e);
        }
    }
}
package com.xreadup.ai.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户学习统计实体
 */
@Data
@TableName("user_learning_stats")
public class UserLearningStats {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private LocalDate date;
    private Integer articlesRead;
    private Integer wordsLearned;
    private Integer studyTimeMinutes;
    private String difficultyDistribution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package com.xreadup.ai.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 单词复习记录实体
 */
@Data
@TableName("word_review")
public class WordReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Long wordId;
    private LocalDate reviewDate;
    private Boolean success;
    private Integer reviewCount;
    private LocalDate nextReviewDate;
    private LocalDateTime createdAt;
}
package com.xreadup.ai.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("word")
public class UserVocabulary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String word;
    private String meaning;
    private Long sourceArticleId;
    private String reviewStatus;
    private Integer reviewCount;
    private LocalDate lastReviewedAt;
    private LocalDate nextReviewAt;
    private LocalDateTime addedAt;
}
package com.xreadup.ai.userservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生词实体类
 */
@Data
@TableName("word")
public class Word {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String word;
    
    private String meaning;
    
    private Long sourceArticleId;
    
    private String reviewStatus;
    
    private LocalDateTime lastReviewedAt;
    
    private LocalDateTime nextReviewAt;
    
    private LocalDateTime addedAt;
}
package com.xreadup.ai.userservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户阅读打卡记录实体类
 */
@Data
@TableName("reading_streak")
public class ReadingStreak {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Integer streakDays;
    
    private LocalDate lastReadDate;
    
    private LocalDateTime updatedAt;
}
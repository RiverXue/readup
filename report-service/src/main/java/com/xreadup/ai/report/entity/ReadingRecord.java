package com.xreadup.ai.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 阅读记录实体
 */
@Data
@TableName("reading_log")
public class ReadingRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Long articleId;
    private Integer readTimeSec;
    private Integer minutesRead;
    private LocalDate readDate;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
}
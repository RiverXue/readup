package com.xreadup.ai.articleservice.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容过滤日志实体类
 * 用于记录敏感词拦截、内容审核等过滤操作
 */
@Data
@TableName("content_filter_log")
public class ContentFilterLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 文章ID
     */
    @TableField("article_id")
    private Long articleId;
    
    /**
     * 过滤类型：sensitive_word, inappropriate_content, spam等
     */
    @TableField("filter_type")
    private String filterType;
    
    /**
     * 匹配到的敏感内容
     */
    @TableField("matched_content")
    private String matchedContent;
    
    /**
     * 过滤原因
     */
    @TableField("filter_reason")
    private String filterReason;
    
    /**
     * 严重程度：low, medium, high
     */
    @TableField("severity_level")
    private String severityLevel;
    
    /**
     * 采取的行动：blocked, warned, allowed
     */
    @TableField("action_taken")
    private String actionTaken;
    
    /**
     * 处理的管理员ID
     */
    @TableField("admin_id")
    private Long adminId;
    
    /**
     * 状态：active, resolved, ignored
     */
    private String status;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

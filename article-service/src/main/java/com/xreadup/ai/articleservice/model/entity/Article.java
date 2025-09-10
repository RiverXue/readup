package com.xreadup.ai.articleservice.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    @TableField("content_en")
    private String contentEn;
    
    @TableField("content_cn")
    private String contentCn;
    
    private String description;
    
    private String url;
    
    private String image;
    
    @TableField("published_at")
    private LocalDateTime publishedAt;
    
    private String source;
    
    private String category;
    
    @TableField("difficulty_level")
    private String difficultyLevel; // A1, A2, B1, B2, C1, C2
    
    @TableField("manual_difficulty")
    private String manualDifficulty; // 用户手动标注的难度
    
    @TableField("word_count")
    private Integer wordCount;
    
    @TableField("read_count")
    private Integer readCount = 0;
    
    @TableField("like_count")
    private Integer likeCount = 0;
    
    @TableField("is_featured")
    private Boolean isFeatured = false;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
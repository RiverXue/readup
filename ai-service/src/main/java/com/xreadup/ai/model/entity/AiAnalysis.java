package com.xreadup.ai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI分析结果实体类
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_analysis")
public class AiAnalysis {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 文章ID
     */
    private Long articleId;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * CEFR难度等级
     */
    private String difficultyLevel;
    
    /**
     * 关键词（JSON格式存储）
     */
    private String keywords;
    
    /**
     * 中文摘要
     */
    private String summary;
    
    /**
     * 完整中文翻译
     */
    private String chineseTranslation;
    
    /**
     * 简化版英文内容
     */
    private String simplifiedContent;
    
    /**
     * 重要短语（JSON格式存储）
     */
    private String keyPhrases;
    
    /**
     * 可读性评分
     */
    private Double readabilityScore;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
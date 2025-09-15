package com.xreadup.ai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI分析实体类
 * <p>
 * 存储文章AI分析结果的实体类，包含文章难度分析、关键词提取、
 * 中文翻译、简化内容等分析结果，以及选词翻译功能的支持
 * </p>
 * 
 * @author XReadUp Team
 * @version 2.0.0
 * @since 2024-01-01
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
    @TableField("article_id")
    private Long articleId;

    /**
     * 文章标题
     */
    @TableField("title")
    private String title;

    /**
     * 难度等级
     */
    @TableField("difficulty_level")
    private String difficultyLevel;

    /**
     * 关键词
     */
    @TableField("keywords")
    private String keywords;

    /**
     * 摘要
     */
    @TableField("summary")
    private String summary;

    /**
     * 中文翻译
     */
    @TableField("chinese_translation")
    private String chineseTranslation;

    /**
     * 简化内容
     */
    @TableField("simplified_content")
    private String simplifiedContent;

    /**
     * 关键短语
     */
    @TableField("key_phrases")
    private String keyPhrases;

    /**
     * 可读性评分
     */
    @TableField("readability_score")
    private Double readabilityScore;

    /**
     * 选词翻译结果
     */
    @TableField("word_translations")
    private String wordTranslations;

    /**
     * DeepSeek生成的文章摘要
     */
    @TableField("deepseek_summary")
    private String deepseekSummary;

    /**
     * 句子解析结果（JSON格式存储）
     */
    @TableField("sentence_parse_results")
    private String sentenceParseResults;

    /**
     * 测验题列表（JSON格式存储）
     */
    @TableField("quiz_questions")
    private String quizQuestions;

    /**
     * 个性化学习建议
     */
    @TableField("learning_tips")
    private String learningTips;

    /**
     * 分析元数据（JSON格式存储）
     */
    @TableField("analysis_metadata")
    private String analysisMetadata;

    /**
     * 最后一次分析类型
     */
    @TableField("last_analysis_type")
    private String lastAnalysisType;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 获取中文摘要（截取前200字符）
     * 
     * @return 中文摘要
     */
    public String getChineseSummary() {
        if (chineseTranslation == null || chineseTranslation.isEmpty()) {
            return "";
        }
        return chineseTranslation.length() > 200 
            ? chineseTranslation.substring(0, 200) + "..." 
            : chineseTranslation;
    }
}
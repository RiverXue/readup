package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 句子解析响应DTO
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Schema(description = "句子解析结果")
public class SentenceParseResponse {
    
    @Schema(description = "原句", example = "Despite the heavy rain, the outdoor concert continued as planned.")
    private String originalSentence;
    
    @Schema(description = "句子结构", example = "复合句")
    private String sentenceStructure;
    
    @Schema(description = "语法分析", example = "让步状语从句 + 主句")
    private String grammar;
    
    @Schema(description = "核心含义", example = "尽管下大雨，音乐会仍按计划进行")
    private String meaning;
    
    @Schema(description = "关键词汇", example = "[\"despite\", \"heavy rain\", \"outdoor concert\"]")
    private List<String> keyVocabulary;
    
    @Schema(description = "语法要点", example = "despite作让步状语，表示'尽管'")
    private List<String> grammarPoints;
    
    @Schema(description = "学习建议", example = "掌握despite引导让步状语从句的用法")
    private String learningTip;
}
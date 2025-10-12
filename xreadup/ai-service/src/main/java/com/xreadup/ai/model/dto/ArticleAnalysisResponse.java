package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "文章分析响应DTO")
public class ArticleAnalysisResponse {
    
    @Schema(description = "CEFR难度等级", example = "B2", allowableValues = {"A1", "A2", "B1", "B2", "C1", "C2"})
    private String difficultyLevel;
    
    @Schema(description = "核心关键词列表", example = "[\"technology\", \"innovation\", \"development\"]")
    private List<String> keywords;
    
    @Schema(description = "中文摘要", example = "这是一篇关于技术创新的重要文章...")
    private String summary;
    
    @Schema(description = "完整中文翻译", example = "人工智能正在改变医疗行业...")
    private String chineseTranslation;
    
    @Schema(description = "简化版英文内容", example = "This article explains how AI helps doctors...")
    private String simplifiedContent;
    
    @Schema(description = "重要短语列表", example = "[\"cutting-edge technology\", \"breakthrough innovation\"]")
    private List<String> keyPhrases;
    
    @Schema(description = "可读性评分(0-100)", example = "75.5", minimum = "0", maximum = "100")
    private Double readabilityScore;
}
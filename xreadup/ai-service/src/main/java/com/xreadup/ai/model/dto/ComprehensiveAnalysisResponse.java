package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 综合AI分析响应DTO
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Schema(description = "综合AI分析结果")
public class ComprehensiveAnalysisResponse {
    
    @Schema(description = "文章摘要")
    private String summary;
    
    @Schema(description = "难句解析列表")
    private List<SentenceParseResponse> difficultSentences;
    
    @Schema(description = "测验题列表")
    private List<QuizQuestion> quizQuestions;
    
    @Schema(description = "学习建议")
    private String learningTip;
    
    @Schema(description = "分析耗时（毫秒）")
    private Long analysisTime;
    
    @Schema(description = "文章ID")
    private Long articleId;
}
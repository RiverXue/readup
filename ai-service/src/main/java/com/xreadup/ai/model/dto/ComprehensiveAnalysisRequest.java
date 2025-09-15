package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 综合AI分析请求DTO
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Schema(description = "综合AI分析请求")
public class ComprehensiveAnalysisRequest {
    
    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", example = "123")
    private Long articleId;
    
    @NotBlank(message = "文本内容不能为空")
    @Schema(description = "文章内容", example = "This is a sample article content...")
    private String text;
    
    @Schema(description = "是否包含摘要", example = "true", defaultValue = "true")
    private Boolean includeSummary = true;
    
    @Schema(description = "是否包含难句解析", example = "true", defaultValue = "true")
    private Boolean includeParse = true;
    
    @Schema(description = "是否生成测验题", example = "true", defaultValue = "true")
    private Boolean includeQuiz = true;
    
    @Schema(description = "测验题数量", example = "5", defaultValue = "5")
    private Integer quizCount = 5;
    
    @Schema(description = "用户ID（用于个性化建议）", example = "123")
    private Long userId;
}
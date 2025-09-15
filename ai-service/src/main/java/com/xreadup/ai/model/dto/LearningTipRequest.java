package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 学习建议请求DTO
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Schema(description = "学习建议请求")
public class LearningTipRequest {
    
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "123")
    private Long userId;

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", example = "123")
    private Long articleId;

    @Min(value = 0, message = "文章数量不能为负数")
    @Schema(description = "阅读文章数量", example = "15")
    private Integer articleCount;
    
    @Min(value = 0, message = "词汇数量不能为负数")
    @Schema(description = "已学词汇数量", example = "120")
    private Integer wordCount;
    
    @Min(value = 0, message = "学习天数不能为负数")
    @Schema(description = "连续学习天数", example = "7")
    private Integer learningDays;
    
    @Min(value = 0, message = "阅读时长不能为负数")
    @Schema(description = "总阅读时长（分钟）", example = "300")
    private Integer totalReadingMinutes;
    
    @Schema(description = "用户身份标签：kaoyan-考研，cet4-四级，cet6-六级，workplace-职场", example = "kaoyan")
    private String identityTag;
}
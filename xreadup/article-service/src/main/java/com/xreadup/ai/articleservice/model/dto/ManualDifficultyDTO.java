package com.xreadup.ai.articleservice.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class ManualDifficultyDTO {
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    @NotNull(message = "难度等级不能为空")
    private String manualDifficulty; // A1, A2, B1, B2, C1, C2
}
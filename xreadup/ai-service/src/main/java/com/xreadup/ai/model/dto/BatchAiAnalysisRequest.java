package com.xreadup.ai.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量AI分析请求DTO
 */
@Data
public class BatchAiAnalysisRequest {
    
    @NotEmpty(message = "文章ID列表不能为空")
    private List<Long> articleIds;
    
    private boolean forceRegenerate = false;
}
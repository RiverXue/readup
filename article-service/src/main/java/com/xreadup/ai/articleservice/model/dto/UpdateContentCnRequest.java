package com.xreadup.ai.articleservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新文章中文内容请求
 * 
 * 用于接收AI服务翻译完成后，需要更新到文章的中文内容
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
public class UpdateContentCnRequest {
    
    /**
     * 文章的英文内容
     * 用于查找匹配的文章
     */
    @NotBlank(message = "英文内容不能为空")
    private String contentEn;
    
    /**
     * 文章的中文翻译内容
     * 需要更新到数据库的内容
     */
    @NotBlank(message = "中文内容不能为空")
    private String contentCn;
}
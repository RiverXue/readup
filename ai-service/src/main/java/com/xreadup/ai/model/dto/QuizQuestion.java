package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 测验题DTO
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Schema(description = "阅读理解测验题")
public class QuizQuestion {
    
    @Schema(description = "题目ID", example = "q1")
    private String id;
    
    @Schema(description = "题目内容", example = "What is the main idea of this passage?")
    private String question;
    
    @Schema(description = "选项列表", example = "[\"A. ...\", \"B. ...\", \"C. ...\", \"D. ...\"]")
    private List<String> options;
    
    @Schema(description = "正确答案索引", example = "2")
    private Integer correctAnswer;
    
    @Schema(description = "正确答案", example = "A")
    private String answer;
    
    @Schema(description = "正确答案文本", example = "C. The importance of environmental protection")
    private String correctAnswerText;
    
    @Schema(description = "解析说明", example = "文章主要讲述了环境保护的重要性...")
    private String explanation;
    
    @Schema(description = "题目类型：main-idea-主旨题，detail-细节题，inference-推理题", example = "main-idea")
    private String questionType;
    
    @Schema(description = "难度等级", example = "medium")
    private String difficulty;
}
package com.xreadup.ai.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 统一的测验题DTO
 * 在不同的服务和API之间共享
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
public class QuizQuestionDTO {
    
    /**
     * 题目ID
     */
    private String id;
    
    /**
     * 问题内容
     */
    private String question;
    
    /**
     * 选项列表 (包含A、B、C、D等选项)
     */
    private List<String> options;
    
    /**
     * 正确答案 (例如: "A", "B", "C", "D")
     */
    private String answer;
    
    /**
     * 正确答案的完整文本
     */
    private String correctAnswer;
    
    /**
     * 正确答案文本（向后兼容）
     */
    private String correctAnswerText;
    
    /**
     * 答案解析说明
     */
    private String explanation;
    
    /**
     * 题目类型 (如: "comprehension", "vocabulary", "grammar")
     */
    private String questionType;
    
    /**
     * 难度等级 (如: "easy", "medium", "hard")
     */
    private String difficulty;
    
    /**
     * 构造函数
     */
    public QuizQuestionDTO() {}
    
    /**
     * 便捷构造函数
     */
    public QuizQuestionDTO(String question, List<String> options, String answer, String explanation) {
        this.question = question;
        this.options = options;
        this.answer = answer;
        this.explanation = explanation;
        this.questionType = "comprehension";
        this.difficulty = "medium";
    }
}
package com.xreadup.ai.controller;

import com.xreadup.ai.model.dto.AiChatRequest;
import com.xreadup.ai.model.dto.AiChatResponse;
import com.xreadup.ai.model.dto.QuizQuestionDTO;
import com.xreadup.ai.model.dto.QuizQuestion;
import com.xreadup.ai.model.dto.WordInfo;
import com.xreadup.ai.service.AiReadingAssistantService;
import com.xreadup.ai.service.EnhancedAiAnalysisService;
import com.xreadup.ai.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

/**
 * AI阅读助手控制器
 * 提供AI对话和Function Calling功能
 */
@RestController
@RequestMapping("/api/ai/assistant")
@Tag(name = "AI阅读助手", description = "AI对话助手和Function Calling接口")
@Slf4j
public class AiReadingAssistantController {

    @Autowired
    private AiReadingAssistantService aiReadingAssistantService;

    @Autowired
    private EnhancedAiAnalysisService enhancedAiAnalysisService;

    /**
     * AI对话接口
     */
    @PostMapping("/chat")
    @Operation(summary = "AI对话", description = "与AI助手进行对话，支持Function Calling")
    public ApiResponse<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        try {
            log.info("AI对话请求 - 用户: {}, 问题: {}", 
                request.getUserId(), request.getQuestion());
            
            AiChatResponse response = aiReadingAssistantService.chatWithAssistant(request);
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("AI对话失败", e);
            return ApiResponse.error("AI对话失败: " + e.getMessage());
        }
    }

    /**
     * 查询单词信息
     */
    @GetMapping("/word/{word}")
    @Operation(summary = "查询单词", description = "查询单词的详细信息，包括释义、例句等")
    public ApiResponse<WordInfo> lookupWord(@PathVariable String word) {
        try {
            log.info("查询单词请求: {}", word);
            WordInfo wordInfo = aiReadingAssistantService.lookupWord(word);
            return ApiResponse.success(wordInfo);
        } catch (Exception e) {
            log.error("查询单词失败: {}", word, e);
            return ApiResponse.error("查询单词失败: " + e.getMessage());
        }
    }

    // ===== 以下方法已删除（未使用） =====
    // - translate() - 前端使用分层翻译策略代替

    /**
     * 获取已保存的测验题
     */
    @GetMapping("/quiz/{articleId}")
    @Operation(summary = "获取已保存测验", description = "获取指定文章已保存的测验题")
    public ApiResponse<List<QuizQuestion>> getSavedQuiz(@PathVariable Long articleId) {
        try {
            log.info("获取已保存测验 - 文章ID: {}", articleId);
            List<QuizQuestion> questions = enhancedAiAnalysisService.getQuizQuestions(articleId);
            
            if (questions == null || questions.isEmpty()) {
                return ApiResponse.success(new ArrayList<>());
            }
            
            return ApiResponse.success(questions);
        } catch (Exception e) {
            log.error("获取已保存测验失败", e);
            return ApiResponse.error("获取已保存测验失败");
        }
    }

    /**
     * 生成学习测验
     */
    @PostMapping("/quiz")
    @Operation(summary = "生成测验", description = "根据文章内容生成学习测验题")
    public ApiResponse<List<QuizQuestionDTO>> generateQuiz(@RequestBody QuizRequest request) {
        try {
            log.info("生成测验请求 - 文章ID: {}", request.getArticleId());
            List<QuizQuestionDTO> questions = aiReadingAssistantService.generateQuizEnhanced(request.getArticleContent());
            
            // 保存测验题到数据库
            if (request.getArticleId() != null && !questions.isEmpty()) {
                // 转换QuizQuestionDTO为QuizQuestion
                List<QuizQuestion> quizQuestions = questions.stream()
                    .map(dto -> {
                        QuizQuestion quiz = new QuizQuestion();
                        quiz.setId(dto.getId());
                        quiz.setQuestion(dto.getQuestion());
                        quiz.setOptions(dto.getOptions());
                        quiz.setAnswer(dto.getAnswer());
                        quiz.setCorrectAnswerText(dto.getCorrectAnswerText());
                        quiz.setExplanation(dto.getExplanation());
                        quiz.setQuestionType(dto.getQuestionType());
                        quiz.setDifficulty(dto.getDifficulty());
                        return quiz;
                    })
                    .collect(java.util.stream.Collectors.toList());
                
                enhancedAiAnalysisService.saveQuizQuestions(request.getArticleId(), quizQuestions);
            }
            
            return ApiResponse.success(questions);
        } catch (Exception e) {
            log.error("生成测验失败", e);
            return ApiResponse.error("生成测验失败");
        }
    }

    /**
     * 测验请求DTO
     */
    public static class QuizRequest {
        private String articleContent;
        private Long articleId;

        public String getArticleContent() { return articleContent; }
        public void setArticleContent(String articleContent) { this.articleContent = articleContent; }
        public Long getArticleId() { return articleId; }
        public void setArticleId(Long articleId) { this.articleId = articleId; }
    }
}
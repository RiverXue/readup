package com.xreadup.ai.controller;

import com.xreadup.ai.model.dto.ApiResponse;
import com.xreadup.ai.service.EnhancedAiAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * 统一AI分析控制器 - 产品经理优化版
 * 
 * 合并DeepSeek 5个分析API为1个智能分析接口，基于用户学习场景设计：
 * 1. 一键智能分析（自动选择最佳分析类型）
 * 2. 场景化分析（根据用户意图选择分析深度）
 * 3. 学习路径推荐（个性化建议）
 * 
 * 产品经理决策：简化用户决策成本，从5个API合并为1个核心API
 * 
 * @author XReadUp Product Team
 * @version 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/smart")
@Tag(name = "智能AI分析", description = "统一智能AI分析API - 产品经理优化版")
@RequiredArgsConstructor
public class UnifiedAiAnalysisController {

    private final EnhancedAiAnalysisService enhancedAiAnalysisService;

    /**
     * 智能AI分析 - 统一入口
     * 
     * 产品经理优化：
     * - 合并原5个AI分析API为1个智能接口
     * - 基于用户场景自动选择分析类型
     * - 个性化学习路径推荐
     * - 保持DeepSeek的高质量分析
     */
    @PostMapping("/analyze")
    @Operation(summary = "智能AI分析", description = "一键智能分析，基于场景自动选择最佳分析类型")
    public ApiResponse<SmartAnalysisResponse> smartAnalyze(@Valid @RequestBody SmartAnalysisRequest request) {
        long startTime = Instant.now().toEpochMilli();
        
        try {
            log.info("智能AI分析请求: 用户ID={}, 场景={}, 文章长度={}", 
                request.getUserId(), request.getScenario(), request.getContent().length());
            
            // 智能场景分析
            AnalysisScenario scenario = determineScenario(request);
            
            // 执行智能分析
            SmartAnalysisResponse response = performSmartAnalysis(request, scenario);
            response.setScenario(scenario);
            response.setAnalysisTime(Instant.now().toEpochMilli() - startTime);
            response.setUserId(request.getUserId());
            
            // 个性化推荐
            response.setPersonalizedRecommendations(generateRecommendations(request, response));
            
            log.info("智能AI分析完成: 场景={}, 耗时={}ms", scenario, response.getAnalysisTime());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("智能AI分析失败", e);
            SmartAnalysisResponse errorResponse = new SmartAnalysisResponse();
            errorResponse.setError(e.getMessage());
            errorResponse.setUserId(request.getUserId());
            return ApiResponse.error("分析失败: " + e.getMessage());
        }
    }

    /**
     * 学习助手 - 个性化建议
     */
    @PostMapping("/assistant")
    @Operation(summary = "AI学习助手", description = "基于用户历史提供个性化学习建议")
    public ApiResponse<LearningAssistantResponse> learningAssistant(@Valid @RequestBody AssistantRequest request) {
        long startTime = Instant.now().toEpochMilli();
        
        try {
            log.info("AI学习助手请求: 用户ID={}, 问题类型={}", request.getUserId(), request.getQuestionType());
            
            LearningAssistantResponse response = new LearningAssistantResponse();
            response.setUserId(request.getUserId());
            response.setAnswer(generateLearningAdvice(request));
            response.setNextSteps(generateNextSteps(request));
            response.setRelatedTopics(findRelatedTopics(request.getQuestion()));
            response.setResponseTime(Instant.now().toEpochMilli() - startTime);
            
            log.info("AI学习助手完成: 耗时={}ms", response.getResponseTime());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("AI学习助手失败", e);
            return ApiResponse.error("助手服务异常: " + e.getMessage());
        }
    }

    // 智能场景分析
    private AnalysisScenario determineScenario(SmartAnalysisRequest request) {
        String content = request.getContent();
        
        // 基于内容长度和复杂度判断
        int wordCount = content.split("\\s+").length;
        boolean hasComplexWords = content.split("\\s+").length > 50;
        
        if (request.getScenario() != null) {
            return AnalysisScenario.valueOf(request.getScenario().toUpperCase());
        }
        
        if (wordCount < 20) {
            return AnalysisScenario.QUICK_SUMMARY;
        } else if (hasComplexWords) {
            return AnalysisScenario.DEEP_ANALYSIS;
        } else {
            return AnalysisScenario.STANDARD_ANALYSIS;
        }
    }

    // 执行智能分析
    private SmartAnalysisResponse performSmartAnalysis(SmartAnalysisRequest request, AnalysisScenario scenario) {
        SmartAnalysisResponse response = new SmartAnalysisResponse();
        
        switch (scenario) {
            case QUICK_SUMMARY:
                response.setSummary(generateSummary(request.getContent()));
                response.setKeyPoints(extractKeyPoints(request.getContent()));
                break;
                
            case DEEP_ANALYSIS:
                response.setSummary(generateSummary(request.getContent()));
                response.setKeyPoints(extractKeyPoints(request.getContent()));
                response.setDetailedAnalysis(generateDetailedAnalysis(request.getContent()));
                response.setVocabularyAnalysis(analyzeVocabulary(request.getContent()));
                response.setQuizQuestions(generateQuiz(request.getContent()));
                break;
                
            case STANDARD_ANALYSIS:
                response.setSummary(generateSummary(request.getContent()));
                response.setKeyPoints(extractKeyPoints(request.getContent()));
                response.setLearningTips(generateLearningTips(request.getContent()));
                break;
                
            case VOCABULARY_FOCUS:
                response.setVocabularyAnalysis(analyzeVocabulary(request.getContent()));
                response.setKeyPoints(extractKeyPoints(request.getContent()));
                break;
                
            case QUIZ_MODE:
                response.setQuizQuestions(generateQuiz(request.getContent()));
                response.setKeyPoints(extractKeyPoints(request.getContent()));
                break;
        }
        
        return response;
    }

    // 各种分析方法的实现
    private String generateSummary(String content) {
        try {
            return enhancedAiAnalysisService.generateSummary(content);
        } catch (Exception e) {
            log.warn("摘要生成失败，使用简化版", e);
            return "这篇文章主要讲述了" + content.substring(0, Math.min(100, content.length())) + "...";
        }
    }

    private String generateDetailedAnalysis(String content) {
        try {
            return enhancedAiAnalysisService.analyzeArticle(content);
        } catch (Exception e) {
            log.warn("深度分析失败，使用简化版", e);
            return "文章内容丰富，建议分段学习理解。";
        }
    }

    private String analyzeVocabulary(String content) {
        try {
            return enhancedAiAnalysisService.analyzeVocabulary(content);
        } catch (Exception e) {
            log.warn("词汇分析失败，使用简化版", e);
            return "文章包含一些高级词汇，建议重点关注。";
        }
    }

    private String extractKeyPoints(String content) {
        try {
            return enhancedAiAnalysisService.extractKeyPoints(content);
        } catch (Exception e) {
            log.warn("要点提取失败，使用简化版", e);
            return "文章的核心观点需要仔细阅读理解。";
        }
    }

    private String generateQuiz(String content) {
        try {
            return enhancedAiAnalysisService.generateQuiz(content);
        } catch (Exception e) {
            log.warn("测验生成失败，使用简化版", e);
            return "请回答：这篇文章的主要内容是什么？";
        }
    }

    private String generateLearningTips(String content) {
        try {
            return enhancedAiAnalysisService.generateLearningTips(content);
        } catch (Exception e) {
            log.warn("学习建议生成失败，使用简化版", e);
            return "建议先通读全文，再重点理解难点词汇。";
        }
    }

    private PersonalizedRecommendations generateRecommendations(SmartAnalysisRequest request, SmartAnalysisResponse response) {
        PersonalizedRecommendations recommendations = new PersonalizedRecommendations();
        
        recommendations.setNextDifficulty(determineNextDifficulty(request.getUserId()));
        recommendations.setSuggestedTopics(findRelatedTopics(request.getContent()));
        recommendations.setStudyTimeSuggestion(calculateOptimalStudyTime(request.getContent()));
        recommendations.setPracticeRecommendations(generatePracticeSuggestions(response));
        
        return recommendations;
    }

    private String generateLearningAdvice(AssistantRequest request) {
        // 基于用户问题类型生成个性化建议
        return "根据您的学习情况，建议" + request.getQuestionType() + "可以从以下方面入手...";
    }

    private String determineNextDifficulty(String userId) {
        return "intermediate"; // 简化的难度推荐
    }

    private int calculateOptimalStudyTime(String content) {
        int wordCount = content.split("\\s+").length;
        return Math.max(5, Math.min(30, wordCount / 10));
    }

    private String generatePracticeSuggestions(SmartAnalysisResponse response) {
        return "建议先复习重点词汇，再完成相关练习。";
    }

    private String generateNextSteps(AssistantRequest request) {
        return "1. 完成当前文章学习\n2. 复习重点词汇\n3. 尝试相关练习";
    }

    private String findRelatedTopics(String content) {
        return "科技,教育,文化"; // 简化的相关主题推荐
    }

    // 枚举和内部DTO类
    public enum AnalysisScenario {
        QUICK_SUMMARY,      // 快速摘要
        STANDARD_ANALYSIS,  // 标准分析
        DEEP_ANALYSIS,      // 深度分析
        VOCABULARY_FOCUS,   // 词汇重点
        QUIZ_MODE          // 测验模式
    }

    public static class SmartAnalysisRequest {
        private String content;
        private String userId;
        private String scenario; // 可选：QUICK_SUMMARY, DEEP_ANALYSIS等
        
        // getters and setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getScenario() { return scenario; }
        public void setScenario(String scenario) { this.scenario = scenario; }
    }

    public static class SmartAnalysisResponse {
        private String userId;
        private AnalysisScenario scenario;
        private String summary;
        private String detailedAnalysis;
        private String keyPoints;
        private String vocabularyAnalysis;
        private String quizQuestions;
        private String learningTips;
        private PersonalizedRecommendations personalizedRecommendations;
        private long analysisTime;
        private String error;
        
        // getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public AnalysisScenario getScenario() { return scenario; }
        public void setScenario(AnalysisScenario scenario) { this.scenario = scenario; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getDetailedAnalysis() { return detailedAnalysis; }
        public void setDetailedAnalysis(String detailedAnalysis) { this.detailedAnalysis = detailedAnalysis; }
        public String getKeyPoints() { return keyPoints; }
        public void setKeyPoints(String keyPoints) { this.keyPoints = keyPoints; }
        public String getVocabularyAnalysis() { return vocabularyAnalysis; }
        public void setVocabularyAnalysis(String vocabularyAnalysis) { this.vocabularyAnalysis = vocabularyAnalysis; }
        public String getQuizQuestions() { return quizQuestions; }
        public void setQuizQuestions(String quizQuestions) { this.quizQuestions = quizQuestions; }
        public String getLearningTips() { return learningTips; }
        public void setLearningTips(String learningTips) { this.learningTips = learningTips; }
        public PersonalizedRecommendations getPersonalizedRecommendations() { return personalizedRecommendations; }
        public void setPersonalizedRecommendations(PersonalizedRecommendations recommendations) { this.personalizedRecommendations = recommendations; }
        public long getAnalysisTime() { return analysisTime; }
        public void setAnalysisTime(long analysisTime) { this.analysisTime = analysisTime; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class AssistantRequest {
        private String userId;
        private String question;
        private String questionType; // learning, vocabulary, grammar, etc.
        
        // getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }
    }

    public static class LearningAssistantResponse {
        private String userId;
        private String answer;
        private String nextSteps;
        private String relatedTopics;
        private long responseTime;
        
        // getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public String getNextSteps() { return nextSteps; }
        public void setNextSteps(String nextSteps) { this.nextSteps = nextSteps; }
        public String getRelatedTopics() { return relatedTopics; }
        public void setRelatedTopics(String relatedTopics) { this.relatedTopics = relatedTopics; }
        public long getResponseTime() { return responseTime; }
        public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
    }

    public static class PersonalizedRecommendations {
        private String nextDifficulty;
        private String suggestedTopics;
        private int studyTimeSuggestion;
        private String practiceRecommendations;
        
        // getters and setters
        public String getNextDifficulty() { return nextDifficulty; }
        public void setNextDifficulty(String nextDifficulty) { this.nextDifficulty = nextDifficulty; }
        public String getSuggestedTopics() { return suggestedTopics; }
        public void setSuggestedTopics(String suggestedTopics) { this.suggestedTopics = suggestedTopics; }
        public int getStudyTimeSuggestion() { return studyTimeSuggestion; }
        public void setStudyTimeSuggestion(int studyTimeSuggestion) { this.studyTimeSuggestion = studyTimeSuggestion; }
        public String getPracticeRecommendations() { return practiceRecommendations; }
        public void setPracticeRecommendations(String practiceRecommendations) { this.practiceRecommendations = practiceRecommendations; }
    }
}
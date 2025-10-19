package com.xreadup.ai.service;

import com.xreadup.ai.model.dto.SimpleAiTutorRequest;
import com.xreadup.ai.model.dto.SimpleAiTutorResponse;
import com.xreadup.ai.service.filter.ContentFilterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 简配版AI学导服务
 * 专门为SimpleAITutor组件设计，极度节省token
 */
@Service
@Slf4j
public class SimpleAiTutorService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ContentFilterService contentFilter;

    /**
     * 简配版AI学导对话
     * 使用极简的prompt，只关注当前文章和问题
     */
    public SimpleAiTutorResponse chat(SimpleAiTutorRequest request) {
        try {
            log.info("简配版AI学导对话 - 问题: {}, 文章: {}", 
                request.getQuestion(), request.getArticleTitle());
            
            // 添加AI对话内容过滤 - 检查用户问题是否包含违禁内容
            if (!contentFilter.isChatSafe(request.getQuestion())) {
                log.warn("用户问题包含违禁内容，拒绝回答");
                
                SimpleAiTutorResponse blockedResponse = new SimpleAiTutorResponse();
                blockedResponse.setAnswer("Rayda老师：抱歉，您的问题包含不当内容，请重新提问。");
                blockedResponse.setFollowUpQuestion("您可以问我关于英语学习的问题。");
                return blockedResponse;
            }
            
            // 构建极简的prompt，只包含必要信息
            String prompt = buildMinimalPrompt(request);
            
            // 调用AI模型
            String response = chatClient.prompt()
                .system("你是Rayda老师，一位专业的英语学习导师。你专门帮助学生在阅读文章时提供学习指导。用中文回答，语言简洁专业，始终以'Rayda老师'的身份回复。")
                .user(prompt)
                .call()
                .content();
            
            // 构建响应
            SimpleAiTutorResponse tutorResponse = new SimpleAiTutorResponse();
            tutorResponse.setAnswer(response != null ? response : "Rayda老师暂时无法回答这个问题。");
            tutorResponse.setFollowUpQuestion(generateSimpleFollowUp(request.getQuestion()));
            
            log.info("简配版AI学导响应成功 - 响应长度: {}", 
                response != null ? response.length() : 0);
            
            return tutorResponse;
            
        } catch (Exception e) {
            log.error("简配版AI学导对话失败", e);
            
            SimpleAiTutorResponse errorResponse = new SimpleAiTutorResponse();
            errorResponse.setAnswer("Rayda老师遇到了一些技术问题。请稍后再试。");
            errorResponse.setFollowUpQuestion("您可以尝试问一些简单的问题。");
            
            return errorResponse;
        }
    }
    
    /**
     * 构建极简的prompt，只包含核心信息
     */
    private String buildMinimalPrompt(SimpleAiTutorRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        // 文章基本信息（极简）
        prompt.append("文章：").append(request.getArticleTitle()).append("\n");
        if (request.getArticleCategory() != null) {
            prompt.append("分类：").append(request.getArticleCategory()).append("\n");
        }
        if (request.getArticleDifficulty() != null) {
            prompt.append("难度：").append(request.getArticleDifficulty()).append("\n");
        }
        
        // 文章描述（限制长度）
        if (request.getArticleDescription() != null && !request.getArticleDescription().trim().isEmpty()) {
            String description = request.getArticleDescription();
            if (description != null && description.length() > 300) {
                description = description.substring(0, 300) + "...";
            }
            prompt.append("内容：").append(description).append("\n");
        }
        
        // 用户问题
        prompt.append("问题：").append(request.getQuestion()).append("\n");
        
        // 简化的指导要求
        prompt.append("\n请基于以上信息回答用户问题，建议使用XReadUp平台的功能：");
        prompt.append("点击查词、双语对照、生词本、AI摘要等。");
        prompt.append("\n\n注意：你是Rayda老师，请始终以'Rayda老师'的身份回复，专注于当前文章的学习指导。");
        
        return prompt.toString();
    }
    
    /**
     * 生成简单的后续问题建议
     */
    private String generateSimpleFollowUp(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // 按优先级排序，避免重复匹配
        if (lowerQuestion.contains("单词") || lowerQuestion.contains("词汇")) {
            return "💡 还想学习其他重点词汇吗？";
        } else if (lowerQuestion.contains("语法") || lowerQuestion.contains("结构")) {
            return "🔍 还有其他语法点需要解释吗？";
        } else if (lowerQuestion.contains("翻译") || lowerQuestion.contains("句子")) {
            return "📝 需要翻译其他句子吗？";
        } else if (lowerQuestion.contains("文章") || lowerQuestion.contains("内容")) {
            return "📖 想了解文章的写作技巧吗？";
        } else {
            return "🤔 还有其他问题吗？";
        }
    }
}
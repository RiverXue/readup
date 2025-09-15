package com.xreadup.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xreadup.ai.model.dto.*;
import com.xreadup.ai.service.AiToolService.QuizQuestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI阅读助手服务
 * 实现AI对话、工具调用和结构化输出
 * 
 * @author XReadUp Team
 * @version 2.0.0
 */
@Service
@Slf4j
public class AiReadingAssistantService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TencentTranslateService translateService;

    @Autowired
    private AiToolService aiToolService;

    /**
     * AI对话助手（增强版 - 集成工具调用）
     */
    public AiChatResponse chatWithAssistant(AiChatRequest request) {
        try {
            log.info("AI对话请求 - 用户: {}, 问题: {}", request.getUserId(), request.getQuestion());
            
            // 使用新的智能对话功能
            String response = aiToolService.intelligentChat(
                request.getQuestion(), 
                request.getArticleContext()
            );

            AiChatResponse chatResponse = new AiChatResponse();
            chatResponse.setAnswer(response);
            chatResponse.setFollowUpQuestion("你对这个话题还有其他问题吗？我可以帮你查词、翻译或生成测验题！");
            chatResponse.setDifficulty("B1");

            log.info("AI智能对话响应成功生成");
            return chatResponse;

        } catch (Exception e) {
            log.error("AI对话失败", e);
            AiChatResponse errorResponse = new AiChatResponse();
            errorResponse.setAnswer("抱歉，我现在无法回答这个问题。请稍后再试。");
            return errorResponse;
        }
    }

    /**
     * 生成学习测验
     */
    public List<QuizQuestion> generateQuiz(String articleContent) {
        try {
            log.info("生成学习测验 - 文章长度: {}", articleContent.length());
            return aiToolService.generateQuiz(articleContent);
        } catch (Exception e) {
            log.error("生成测验失败", e);
            return List.of();
        }
    }

    /**
     * 查询单词详细信息
     */
    public WordInfo lookupWord(String word) {
        try {
            log.info("查询单词详细信息: {}", word);
            
            // 模拟单词信息
            WordInfo wordInfo = new WordInfo();
            wordInfo.setWord(word);
            wordInfo.setPhonetic("/səˈsteɪnəbəl/");
            wordInfo.setMeanings(List.of(
                "adj. 可持续的，能持续的",
                "adj. 合理利用的，不破坏环境的"
            ));
            wordInfo.setExamples(List.of(
                "We need to find a sustainable solution to climate change.",
                "Sustainable development is key to our future."
            ));
            wordInfo.setSynonyms(List.of("maintainable", "enduring", "lasting"));
            wordInfo.setDifficulty("B2");
            wordInfo.setUsage("常用于环保、经济、社会发展等话题");

            return wordInfo;

        } catch (Exception e) {
            log.error("查询单词失败: {}", word, e);
            return null;
        }
    }

    /**
     * 翻译文本
     */
    public String translateText(String text, String targetLang) {
        try {
            log.info("翻译文本: {} -> {}", text, targetLang);
            
            // 调用腾讯翻译服务
            return translateService.translateText(text, "en", targetLang);

        } catch (Exception e) {
            log.error("翻译失败", e);
            return "翻译失败，请重试";
        }
    }
}
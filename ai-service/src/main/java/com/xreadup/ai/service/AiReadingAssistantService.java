package com.xreadup.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xreadup.ai.model.dto.*;
import com.xreadup.ai.service.AiToolService.QuizQuestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * 查询单词详细信息 - 使用真实AI模型实现
     */
    public WordInfo lookupWord(String word) {
        try {
            log.info("查询单词详细信息: {}", word);
            
            // 使用AI模型查询单词信息
            String prompt = String.format("请提供单词'%s'的详细信息，包括：\n" +
                    "1. 音标 (phonetic)\n" +
                    "2. 释义列表 (meanings) - 请使用中文，并包含词性\n" +
                    "3. 例句 (example) - 请使用英文，并包含中文翻译（只需一条例句）\n" +
                    "4. 语境 (context) - 提供该单词在句子中的语境类型，使用简短类别描述（如：日常对话/学术/科技/金融/文学等）\n" +
                    "5. 难度等级 (difficulty) - 请使用CEFR标准 (A1, A2, B1, B2, C1, C2)\n" +
                    "6. 用法说明 (usage) - 提供该单词的常见用法说明\n" +
                    "请严格按照JSON格式返回，不要添加任何额外的解释或说明。", word);
            
            log.info("发送AI请求: {}", prompt);
            
            // 调用AI模型获取响应
            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            log.info("AI响应: {}", aiResponse);
            
            // 解析AI返回的JSON响应
            WordInfo wordInfo = parseWordInfoFromJson(aiResponse, word);
            
            return wordInfo;

        } catch (Exception e) {
            log.error("查询单词失败: {}", word, e);
            
            // 降级处理：返回基础的单词信息
            WordInfo fallbackWordInfo = new WordInfo();
            fallbackWordInfo.setWord(word);
            fallbackWordInfo.setMeanings(List.of("查询单词信息时发生错误，请稍后再试。"));
            
            Example fallbackExample = new Example();
            fallbackExample.setEnglish(String.format("Example sentence for '%s' could not be generated.", word));
            fallbackExample.setChinese("无法生成例句");
            fallbackWordInfo.setExamples(List.of(fallbackExample));
            
            fallbackWordInfo.setSynonyms(List.of());
            fallbackWordInfo.setDifficulty("unknown");
            fallbackWordInfo.setUsage("无法获取用法信息");
            
            return fallbackWordInfo;
        }
    }
    
    /**
     * 从JSON字符串解析WordInfo对象
     */
    private WordInfo parseWordInfoFromJson(String jsonResponse, String word) {
        WordInfo wordInfo = new WordInfo();
        wordInfo.setWord(word);
        
        try {
            // 清理JSON响应，移除可能的Markdown格式
            String cleanResponse = jsonResponse.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();
            
            // 解析JSON响应
            Map<String, Object> responseMap = objectMapper.readValue(cleanResponse, Map.class);
            
            // 提取并设置各个字段
            if (responseMap.containsKey("phonetic")) {
                wordInfo.setPhonetic(responseMap.get("phonetic").toString());
            } else {
                wordInfo.setPhonetic("/未知音标/");
            }
            
            // 处理meanings字段（嵌套对象数组）
            if (responseMap.containsKey("meanings")) {
                List<String> meanings = new ArrayList<>();
                Object meaningsObj = responseMap.get("meanings");
                if (meaningsObj instanceof List) {
                    for (Object meaningObj : (List<Object>) meaningsObj) {
                        if (meaningObj instanceof Map) {
                            Map<?, ?> meaningMap = (Map<?, ?>) meaningObj;
                            String partOfSpeech = meaningMap.containsKey("partOfSpeech") ? meaningMap.get("partOfSpeech").toString() : "未知词性";
                            // 同时支持meaning和definition两种键名
                            String definition = meaningMap.containsKey("meaning") ? meaningMap.get("meaning").toString() : 
                                              (meaningMap.containsKey("definition") ? meaningMap.get("definition").toString() : "");
                            
                            // 处理空释义的情况
                            if (definition == null || definition.trim().isEmpty()) {
                                meanings.add(partOfSpeech + ": 未提供详细释义");
                            } else {
                                meanings.add(partOfSpeech + ": " + definition);
                            }
                        } else {
                            meanings.add(meaningObj.toString());
                        }
                    }
                }
                if (!meanings.isEmpty()) {
                    wordInfo.setMeanings(meanings);
                } else {
                    wordInfo.setMeanings(List.of("暂无释义信息"));
                }
            } else {
                wordInfo.setMeanings(List.of("暂无释义信息"));
            }
            
            // 处理example字段（单个例句对象）
            List<Example> examples = new ArrayList<>();
            if (responseMap.containsKey("example")) {
                Object exampleObj = responseMap.get("example");
                if (exampleObj instanceof Map) {
                    Map<?, ?> exampleMap = (Map<?, ?>) exampleObj;
                    // 同时支持en/english和zh/chinese/cn三种键名
                    String enExample = exampleMap.containsKey("english") ? exampleMap.get("english").toString() : 
                                      (exampleMap.containsKey("en") ? exampleMap.get("en").toString() : "");
                    String zhExample = exampleMap.containsKey("chinese") ? exampleMap.get("chinese").toString() : 
                                      (exampleMap.containsKey("zh") ? exampleMap.get("zh").toString() : 
                                      (exampleMap.containsKey("cn") ? exampleMap.get("cn").toString() : ""));
                    
                    if (!enExample.isEmpty() || !zhExample.isEmpty()) {
                        Example example = new Example();
                        example.setEnglish(enExample);
                        example.setChinese(zhExample);
                        examples.add(example);
                    }
                } else if (exampleObj != null) {
                    // 如果不是Map对象，尝试作为字符串处理
                    Example example = new Example();
                    example.setEnglish(exampleObj.toString());
                    example.setChinese("暂无翻译");
                    examples.add(example);
                }
            }
            
            // 如果没有获取到有效的例句，使用默认例句
            if (examples.isEmpty()) {
                Example defaultExample = new Example();
                defaultExample.setEnglish(String.format("This is an example sentence using '%s'.", word));
                defaultExample.setChinese(String.format("这是使用'%s'的例句。", word));
                examples.add(defaultExample);
            }
            
            wordInfo.setExamples(examples);
            
            // 设置默认空同义词列表
            wordInfo.setSynonyms(List.of());
            
            if (responseMap.containsKey("context")) {
                wordInfo.setContext(responseMap.get("context").toString());
            } else {
                wordInfo.setContext("general");
            }

            if (responseMap.containsKey("difficulty")) {
                wordInfo.setDifficulty(responseMap.get("difficulty").toString());
            } else {
                wordInfo.setDifficulty("unknown");
            }

            if (responseMap.containsKey("usage")) {
                wordInfo.setUsage(responseMap.get("usage").toString());
            } else {
                wordInfo.setUsage("暂无用法说明");
            }
            
        } catch (Exception e) {
            log.error("解析WordInfo失败: {}", jsonResponse, e);
            // 发生解析错误时，设置基础信息
            wordInfo.setPhonetic("/无法解析音标/");
            wordInfo.setMeanings(List.of("无法解析单词释义"));
            
            Example errorExample = new Example();
            errorExample.setEnglish(String.format("无法解析例句 for '%s'.", word));
            errorExample.setChinese("无法解析例句");
            wordInfo.setExamples(List.of(errorExample));
            
            // 设置默认空同义词列表
            wordInfo.setSynonyms(List.of());
            wordInfo.setDifficulty("unknown");
            wordInfo.setUsage("无法解析用法信息");
        }
        
        return wordInfo;
    }
    
    /**
     * 将对象转换为字符串列表
     */
    @SuppressWarnings("unchecked")
    private List<String> convertToList(Object obj) {
        if (obj instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<Object>) obj) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        } else if (obj != null) {
            return List.of(obj.toString());
        }
        return List.of();
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
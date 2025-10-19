package com.xreadup.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xreadup.ai.model.dto.*;
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
     * AI对话助手（增强版 - 简化工具调用）
     */
    public AiChatResponse chatWithAssistant(AiChatRequest request) {
        try {
            log.info("AI对话请求 - 用户: {}, 问题: {}", request.getUserId(), request.getQuestion());
            
            // 验证输入参数
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                log.warn("AI对话请求问题为空");
                AiChatResponse emptyResponse = new AiChatResponse();
                emptyResponse.setAnswer("请提出一个具体的问题，我会尽力帮助您！");
                emptyResponse.setFollowUpQuestion("您可以问我关于文章内容、单词解释、语法问题等。");
                emptyResponse.setDifficulty("B1");
                return emptyResponse;
            }
            
            // 使用简化的智能对话功能
            String response = aiToolService.intelligentChat(
                request.getQuestion().trim(), 
                request.getArticleContext()
            );

            // 验证AI响应
            if (response == null || response.trim().isEmpty()) {
                log.warn("AI返回空响应");
                response = "抱歉，我暂时无法回答这个问题。请尝试换个方式提问，或者稍后再试。";
            }

            AiChatResponse chatResponse = new AiChatResponse();
            chatResponse.setAnswer(response);
            
            // 根据问题类型生成不同的后续问题建议
            String followUpQuestion = generateFollowUpQuestion(request.getQuestion());
            chatResponse.setFollowUpQuestion(followUpQuestion);
            chatResponse.setDifficulty("B1");

            log.info("AI智能对话响应成功生成 - 响应长度: {}", response.length());
            return chatResponse;

        } catch (Exception e) {
            log.error("AI对话失败", e);
            AiChatResponse errorResponse = new AiChatResponse();
            errorResponse.setAnswer("抱歉，我遇到了一些技术问题。请稍后再试，或者尝试重新提问。如果问题持续存在，请联系技术支持。");
            errorResponse.setFollowUpQuestion("您可以尝试问一些简单的问题，比如'这篇文章讲了什么？'或'这个单词是什么意思？'");
            errorResponse.setDifficulty("B1");
            return errorResponse;
        }
    }
    
    /**
     * 根据用户问题生成合适的后续问题建议（教育导向版）
     */
    private String generateFollowUpQuestion(String question) {
        String lowerQuestion = question.toLowerCase();
        
        if (lowerQuestion.contains("单词") || lowerQuestion.contains("word") || lowerQuestion.contains("意思") || lowerQuestion.contains("词汇")) {
            return "💡 您还想学习这篇文章中的其他重点词汇吗？我可以帮您分析词汇的用法和搭配。";
        } else if (lowerQuestion.contains("翻译") || lowerQuestion.contains("translate") || lowerQuestion.contains("句子")) {
            return "📝 需要我帮您翻译其他句子吗？或者分析句子的语法结构？";
        } else if (lowerQuestion.contains("语法") || lowerQuestion.contains("grammar") || lowerQuestion.contains("结构")) {
            return "🔍 还有其他语法点需要解释吗？我可以帮您分析更复杂的语法现象。";
        } else if (lowerQuestion.contains("文章") || lowerQuestion.contains("article") || lowerQuestion.contains("内容") || lowerQuestion.contains("主题")) {
            return "📚 想深入了解这篇文章的写作技巧、作者观点或相关话题吗？";
        } else if (lowerQuestion.contains("理解") || lowerQuestion.contains("理解") || lowerQuestion.contains("意思")) {
            return "🎯 需要我帮您分析文章的深层含义或写作手法吗？";
        } else {
            return "🤔 还有其他关于这篇文章的问题吗？我很乐意继续帮助您学习！";
        }
    }

    /**
     * 生成学习测验（增强版）
     * 使用简化的智能对话功能，提供高效的学习指导
     */
    public List<QuizQuestionDTO> generateQuizEnhanced(String articleContent) {
        try {
            log.info("生成学习测验（增强版） - 文章长度: {}", articleContent.length());
            
            // 直接使用ChatClient生成测验题，提供高效的测验生成
            String prompt = String.format("""
                作为英语阅读理解专家，请基于以下文章内容生成高质量的阅读理解选择题。
                
                文章内容：
                %s
                
                要求：
                1. 生成完整的3道选择题
                2. 每道题包含：问题、四个选项（A、B、C、D）、正确答案、解析
                3. 题目难度适中，考查对文章的理解
                4. 选项要有迷惑性，避免过于明显
                5. 必须以纯正JSON格式返回，不要添加任何其他说明文字
                
                JSON格式示例：
                [
                  {
                    "question": "问题内容",
                    "options": ["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"],
                    "answer": "A",
                    "explanation": "正确答案的解析说明"
                  }
                ]
                
                请直接返回JSON数组，不要包装在```json```代码块中。
                """, articleContent);
            
            String jsonResponse = chatClient.prompt()
                .system("你是一个专业的英语阅读理解出题专家，请生成高质量的测验题。返回结果必须是正确JSON格式。")
                .user(prompt)
                .call()
                .content();
            
            log.info("测验题AI返回结果: {}", jsonResponse);
            
            // 解析JSON响应
            List<QuizQuestionDTO> questions = parseJsonToQuizQuestions(jsonResponse);
            
            if (questions.isEmpty()) {
                log.warn("解析的测验题为空，返回错误时的备用测验题");
                return createFallbackQuizQuestions();
            }
            
            return questions;
            
        } catch (Exception e) {
            log.error("生成测验失败（增强版）", e);
            return createFallbackQuizQuestions();
        }
    }
    
    /**
     * 解析JSON响应为QuizQuestionDTO列表
     */
    private List<QuizQuestionDTO> parseJsonToQuizQuestions(String jsonResponse) {
        try {
            // 清理JSON响应
            String cleanJson = jsonResponse.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();
            
            log.info("清理后的JSON: {}", cleanJson);
            
            // 使用ObjectMapper解析JSON
            List<Map<String, Object>> questionMaps = objectMapper.readValue(cleanJson, List.class);
            
            List<QuizQuestionDTO> questions = new ArrayList<>();
            for (int i = 0; i < questionMaps.size(); i++) {
                Map<String, Object> questionMap = questionMaps.get(i);
                QuizQuestionDTO question = new QuizQuestionDTO();
                
                question.setId(String.valueOf(i + 1));
                question.setQuestion((String) questionMap.get("question"));
                
                // 处理选项
                Object optionsObj = questionMap.get("options");
                if (optionsObj instanceof List) {
                    question.setOptions((List<String>) optionsObj);
                } else {
                    question.setOptions(new ArrayList<>());
                }
                
                // 处理答案 - 支持多种字段名
                String answer = (String) questionMap.get("answer");
                if (answer == null) {
                    answer = (String) questionMap.get("correctAnswer");
                }
                question.setAnswer(answer);
                question.setCorrectAnswer(answer);
                question.setCorrectAnswerText(answer);
                
                question.setExplanation((String) questionMap.get("explanation"));
                question.setQuestionType("comprehension");
                question.setDifficulty("medium");
                
                questions.add(question);
            }
            
            log.info("成功解析测验题数量: {}", questions.size());
            return questions;
            
        } catch (Exception e) {
            log.error("解析测验题JSON失败: {}", jsonResponse, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 创建错误时的备用测验题
     */
    private List<QuizQuestionDTO> createFallbackQuizQuestions() {
        QuizQuestionDTO fallback = new QuizQuestionDTO(
            "基于文章内容，请选择正确答案：",
            List.of("A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"),
            "A",
            "测验题生成服务暂时不可用，请稍后再试"
        );
        
        fallback.setId("1");
        fallback.setCorrectAnswer("A");
        fallback.setCorrectAnswerText("A");
        
        return List.of(fallback);
    }

    /**
     * 查询单词详细信息 - 使用真实AI模型实现
     */
    public WordInfo lookupWord(String word) {
        try {
            log.info("查询单词详细信息: {}", word);
            
            // 使用AI模型查询单词信息
            String prompt = String.format("请提供单词'%s'的详细信息，严格按照以下JSON格式返回，不要添加任何额外的解释或说明：\n{\n" +
                    "  \"phonetic\": \"音标\",\n" +
                    "  \"meanings\": [\n" +
                    "    {\n" +
                    "      \"partOfSpeech\": \"中文词性（请使用中文，如：名词、动词、形容词等，不要使用英文缩写）\",\n" +
                    "      \"definition\": \"中文释义\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"example\": {\n" +
                    "    \"english\": \"英文例句\",\n" +
                    "    \"chinese\": \"中文翻译\"\n" +
                    "  },\n" +
                    "  \"context\": [\"语境类型1\", \"语境类型2\"],\n" +
                    "  \"difficulty\": \"CEFR难度等级（A1, A2, B1, B2, C1, C2）\",\n" +
                    "  \"usage\": \"用法说明\"\n" +
                    "}", word);
            
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
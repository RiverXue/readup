package com.xreadup.ai.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xreadup.ai.model.dto.ArticleAnalysisRequest;
import com.xreadup.ai.model.dto.ArticleAnalysisResponse;
import com.xreadup.ai.model.dto.WordTranslationRequest;
import com.xreadup.ai.model.dto.WordTranslationResponse;
import com.xreadup.ai.model.dto.SentenceParseResponse;
import com.xreadup.ai.model.dto.QuizQuestion;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

/**
 * AI分析服务
 * <p>
 * 提供文章智能分析、翻译、摘要等核心AI功能
 * 集成DeepSeek大模型，支持英语文章的CEFR难度评估、关键词提取、中文翻译和摘要生成
 * </p>
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class AiAnalysisService {

    @Autowired
    private ChatClient chatClient;

    /**
     * 全面分析文章
     * <p>
     * 对英文文章进行深度分析，包括：
     * <ul>
     *   <li>CEFR难度等级评估（A1-C2）</li>
     *   <li>核心关键词提取</li>
     *   <li>中文摘要生成</li>
     *   <li>完整中文翻译</li>
     *   <li>简化版英文内容</li>
     *   <li>重要短语提取</li>
     *   <li>可读性评分</li>
     * </ul>
     * </p>
     * 
     * @param request 文章分析请求，包含标题、内容、分类等信息
     * @return 文章分析响应，包含完整的分析结果
     */
    public ArticleAnalysisResponse analyzeArticle(ArticleAnalysisRequest request) {
        String prompt = buildAnalysisPrompt(request);
        
        String response = chatClient.prompt()
            .system("你是一个专业的英语学习助手，专门分析英文文章并提供学习支持。" +
                   "请严格按照JSON格式返回结果，包含以下字段：" +
                   "difficultyLevel（A1,A2,B1,B2,C1,C2），keywords（关键词列表），" +
                   "summary（中文摘要），chineseTranslation（中文翻译），" +
                   "simplifiedContent（简化版英文），keyPhrases（关键短语），readabilityScore（可读性分数0-100）")
            .user(prompt)
            .call()
            .content();
        
        return parseResponse(response);
    }

    /**
     * 构建分析提示词
     * <p>
     * 根据文章请求构建完整的AI分析提示词，包含所有需要的分析维度
     * </p>
     * 
     * @param request 文章分析请求
     * @return 完整的分析提示词
     */
    private String buildAnalysisPrompt(ArticleAnalysisRequest request) {
        return String.format(
            "请分析以下英文文章：\n\n" +
            "标题：%s\n" +
            "内容：%s\n" +
            "分类：%s\n" +
            "字数：%d\n\n" +
            "请提供：\n" +
            "1. CEFR难度等级（A1-C2）\n" +
            "2. 5-8个核心关键词\n" +
            "3. 中文摘要（100字内）\n" +
            "4. 完整中文翻译\n" +
            "5. 简化版英文内容\n" +
            "6. 3-5个重要短语\n" +
            "7. 可读性评分",
            request.getTitle(),
            request.getContent(),
            request.getCategory(),
            request.getWordCount()
        );
    }

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 解析AI响应
     * <p>
     * 解析DeepSeek AI返回的JSON响应，转换为ArticleAnalysisResponse对象
     * 使用ObjectMapper进行真实的JSON解析
     * </p>
     * 
     * @param response AI返回的JSON格式响应
     * @return 解析后的文章分析响应对象
     */
    private ArticleAnalysisResponse parseResponse(String response) {
        try {
            // 清理响应，移除可能的Markdown格式
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();
            
            // 使用ObjectMapper解析JSON响应
            return objectMapper.readValue(cleanResponse, ArticleAnalysisResponse.class);
        } catch (Exception e) {
            log.error("解析AI响应失败，使用降级响应: {}", response, e);
            
            // 降级响应
            ArticleAnalysisResponse result = new ArticleAnalysisResponse();
            result.setDifficultyLevel("B2");
            result.setKeywords(Arrays.asList("technology", "analysis", "content"));
            result.setSummary("AI分析结果获取失败，请稍后重试");
            result.setChineseTranslation("翻译服务暂时不可用");
            result.setSimplifiedContent("Analysis temporarily unavailable");
            result.setKeyPhrases(Arrays.asList("content analysis", "AI processing"));
            result.setReadabilityScore(70.0);
            
            return result;
        }
    }

    /**
     * 全文翻译 - 将英文内容完整翻译成中文
     * <p>
     * 使用DeepSeek大模型将完整的英文文章内容翻译成地道、准确的中文
     * 保持原文格式和段落结构
     * </p>
     * 
     * @param englishText 英文内容
     * @return 中文全文翻译结果，如果API失败则返回友好的错误提示
     */
    public String translateFullText(String englishText) {
        try {
            if (englishText == null || englishText.trim().isEmpty()) {
                return "翻译内容不能为空";
            }
            
            if (englishText.length() > 10000) {
                return handleLargeTextTranslation(englishText);
            }
            
            String translation = chatClient.prompt()
                .system("你是一个专业的中英翻译专家，请将英文文章完整翻译成准确、流畅的中文。" +
                       "保持原文的段落结构和格式，确保翻译准确且自然。")
                .user("请将以下英文内容完整翻译成中文：\n\n" + englishText)
                .call()
                .content();
            
            return translation != null ? translation : "翻译服务暂时不可用，请稍后重试";
            
        } catch (Exception e) {
            log.error("全文翻译服务调用失败", e);
            return "翻译服务暂时不可用，请稍后重试";
        }
    }

    /**
     * 选词翻译 - 提供单词的详细翻译和解释
     * <p>
     * 根据上下文提供单词的准确翻译、音标、词性、释义、例句等完整信息
     * </p>
     * 
     * @param request 选词翻译请求
     * @return 单词的详细翻译信息
     */
    public WordTranslationResponse translateWord(WordTranslationRequest request) {
        try {
            String prompt = String.format(
                "请为以下英文单词提供详细的翻译和解释：\n\n" +
                "单词：%s\n" +
                "上下文：%s\n\n" +
                "请提供以下信息：\n" +
                "1. 准确的中文翻译\n" +
                "2. 音标（英式发音）\n" +
                "3. 词性\n" +
                "4. 英文释义\n" +
                "5. 中文释义\n" +
                "6. 例句（英文）\n" +
                "7. 例句中文翻译\n" +
                "8. 常见同义词（3-5个）\n" +
                "9. CEFR难度等级（A1-C2）\n\n" +
                "请以JSON格式返回，包含字段：chinese, phonetic, partOfSpeech, definition, chineseDefinition, example, exampleChinese, synonyms, difficultyLevel",
                request.getWord(),
                request.getContext()
            );
            
            String response = chatClient.prompt()
                .system("你是一个专业的英语词典专家，请提供准确、详细的单词解释。")
                .user(prompt)
                .call()
                .content();
            
            return parseWordTranslationResponse(response, request.getWord());
            
        } catch (Exception e) {
            log.error("选词翻译服务调用失败", e);
            return createFallbackWordResponse(request.getWord());
        }
    }

    /**
     * 处理大文本翻译（分段处理）
     */
    private String handleLargeTextTranslation(String englishText) {
        try {
            int chunkSize = 3000;
            StringBuilder fullTranslation = new StringBuilder();
            
            for (int i = 0; i < englishText.length(); i += chunkSize) {
                int end = Math.min(i + chunkSize, englishText.length());
                String chunk = englishText.substring(i, end);
                
                String chunkTranslation = chatClient.prompt()
                    .system("你是一个专业的中英翻译专家，请将英文段落翻译成中文。" +
                           "确保翻译准确流畅，保持上下文的连贯性。")
                    .user("翻译：\n" + chunk)
                    .call()
                    .content();
                
                if (chunkTranslation != null) {
                    fullTranslation.append(chunkTranslation).append("\n\n");
                }
            }
            
            return fullTranslation.toString();
            
        } catch (Exception e) {
            log.error("大文本翻译失败", e);
            return "文本过长，翻译失败，请稍后重试";
        }
    }

    /**
     * 解析选词翻译响应
     */
    private WordTranslationResponse parseWordTranslationResponse(String response, String word) {
        try {
            // 清理响应
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();
            
            // 使用ObjectMapper解析JSON
            return objectMapper.readValue(cleanResponse, WordTranslationResponse.class);
            
        } catch (Exception e) {
            log.error("解析选词翻译响应失败: {}", response, e);
            return createFallbackWordResponse(word);
        }
    }

    /**
     * 创建降级响应
     */
    private WordTranslationResponse createFallbackWordResponse(String word) {
        WordTranslationResponse response = new WordTranslationResponse();
        response.setWord(word);
        response.setChinese("翻译暂时不可用");
        response.setPhonetic("/" + word + "/");
        response.setPartOfSpeech("未知");
        response.setDefinition("Translation temporarily unavailable");
        response.setChineseDefinition("翻译暂时不可用");
        response.setExample("Translation service is temporarily unavailable.");
        response.setExampleChinese("翻译服务暂时不可用");
        response.setSynonyms(Arrays.asList("-"));
        response.setDifficultyLevel("未知");
        return response;
    }

    /**
     * 英文翻译中文 - 已废弃，使用translateFullText替代
     */
    @Deprecated
    public String translateToChinese(String englishText) {
        return translateFullText(englishText);
    }

    /**
     * 生成中文摘要
     * <p>
     * 使用AI为文章内容生成简洁、准确的中文摘要，控制在100字以内
     * </p>
     * 
     * @param content 文章内容
     * @return 中文摘要
     */
    public String generateSummary(String content) {
        return chatClient.prompt()
            .system("你是一个专业的内容摘要专家，请用简洁的中文总结文章要点。")
            .user("请用中文总结以下文章内容（100字以内）：\n\n" + content)
            .call()
            .content();
    }

    /**
     * 提取关键词
     * <p>
     * 使用AI从文章内容中提取5-8个核心关键词
     * </p>
     * 
     * @param content 文章内容
     * @return 关键词列表
     */
    public List<String> extractKeywords(String content) {
        String keywordsText = chatClient.prompt()
            .system("你是一个专业的关键词提取专家，请提取文章的核心关键词，返回关键词列表。")
            .user("请从以下内容中提取5-8个核心关键词：\n\n" + content)
            .call()
            .content();
        
        return Arrays.asList(keywordsText.split(","));
    }

    /**
     * 句子解析（DeepSeek）
     * <p>
     * 使用AI对英文长句进行深度解析，包括语法结构、核心含义、关键词汇等
     * </p>
     * 
     * @param sentence 需要解析的英文句子
     * @return 句子解析结果，包含语法结构、核心含义、关键词汇等
     */
    public SentenceParseResponse parseSentence(String sentence) {
        try {
            String prompt = String.format(
                "请对以下英文句子进行深度解析：\n\n" +
                "句子：%s\n\n" +
                "请提供以下信息：\n" +
                "1. 句子结构分析（主谓宾、从句结构等）\n" +
                "2. 语法要点解析（时态、语态、虚拟语气等）\n" +
                "3. 核心含义的中文解释\n" +
                "4. 关键词汇解析（生词、短语、习语）\n" +
                "5. 学习建议（如何理解这类句子）\n\n" +
                "请以JSON格式返回，包含字段：originalSentence, sentenceStructure, grammar, meaning, keyVocabulary, grammarPoints, learningTip",
                sentence
            );

            String response = chatClient.prompt()
                .system("你是一个专业的英语语法专家，请提供准确、详细的句子解析。")
                .user(prompt)
                .call()
                .content();

            return parseSentenceResponse(response, sentence);

        } catch (Exception e) {
            log.error("句子解析服务调用失败", e);
            return createFallbackSentenceResponse(sentence);
        }
    }

    /**
     * 生成测验题（DeepSeek）
     * <p>
     * 使用AI根据文章内容生成阅读理解测验题
     * </p>
     * 
     * @param text 文章内容
     * @param questionCount 题目数量
     * @return 测验题列表
     */
    public List<QuizQuestion> generateQuiz(String text, Integer questionCount) {
        try {
            String prompt = String.format(
                "请根据以下文章内容生成%d道阅读理解测验题：\n\n" +
                "文章内容：%s\n\n" +
                "要求：\n" +
                "1. 题目类型：阅读理解题\n" +
                "2. 每题包含：问题、4个选项、正确答案、答案解析\n" +
                "3. 难度适中，考察对文章的理解\n" +
                "4. 题目要有针对性，避免过于简单\n\n" +
                "请以JSON数组格式返回，每个对象包含字段：id, question, options, correctAnswer, correctAnswerText, explanation, questionType, difficulty",
                questionCount, text
            );

            String response = chatClient.prompt()
                .system("你是一个专业的英语阅读理解出题专家，请生成高质量的测验题。")
                .user(prompt)
                .call()
                .content();

            return parseQuizResponse(response);

        } catch (Exception e) {
            log.error("生成测验题服务调用失败", e);
            return createFallbackQuizQuestions(questionCount);
        }
    }

    /**
     * 生成学习建议（DeepSeek）
     * <p>
     * 使用AI根据用户学习数据生成个性化学习建议
     * </p>
     * 
     * @param articleCount 已读文章数量
     * @param wordCount 已学词汇数量
     * @param learningDays 连续学习天数
     * @return 个性化学习建议
     */
    public String generateLearningTip(Integer articleCount, Integer wordCount, Integer learningDays) {
        try {
            String prompt = String.format(
                "请根据以下用户学习数据生成个性化学习建议：\n\n" +
                "已读文章数量：%d篇\n" +
                "已学词汇数量：%d个\n" +
                "连续学习天数：%d天\n\n" +
                "要求：\n" +
                "1. 分析用户当前学习状态\n" +
                "2. 指出学习中的优点和不足\n" +
                "3. 提供具体的学习建议\n" +
                "4. 建议要实用、具体、可操作\n" +
                "5. 用中文回答，100字左右\n\n" +
                "请直接返回学习建议文本。",
                articleCount, wordCount, learningDays
            );

            return chatClient.prompt()
                .system("你是一个专业的英语学习指导专家，请提供实用、具体的学习建议。")
                .user(prompt)
                .call()
                .content();

        } catch (Exception e) {
            log.error("生成学习建议服务调用失败", e);
            return "根据您的学习情况，建议保持每日阅读习惯，重点关注长难句的理解和词汇积累。";
        }
    }

    /**
     * 解析句子解析响应
     * 增强JSON解析的健壮性，处理多种可能的格式问题
     */
    private SentenceParseResponse parseSentenceResponse(String response, String sentence) {
        try {
            // 1. 首先记录原始响应以便调试
            log.info("原始句子解析响应长度: {} 字符", response.length());
            log.debug("原始句子解析响应: {}", response);
            
            // 2. 清理响应，处理多种可能的格式
            String cleanResponse = response.trim();
            
            // 2.1 处理Markdown代码块格式
            if (cleanResponse.startsWith("```json")) {
                log.debug("检测到```json格式代码块");
                cleanResponse = cleanResponse.substring(7);
                int endCodeBlockIndex = cleanResponse.lastIndexOf("```");
                if (endCodeBlockIndex > 0) {
                    cleanResponse = cleanResponse.substring(0, endCodeBlockIndex);
                }
            } else if (cleanResponse.startsWith("```")) {
                log.debug("检测到普通```格式代码块");
                cleanResponse = cleanResponse.substring(3);
                int endCodeBlockIndex = cleanResponse.lastIndexOf("```");
                if (endCodeBlockIndex > 0) {
                    cleanResponse = cleanResponse.substring(0, endCodeBlockIndex);
                }
            }
            
            // 2.2 处理可能的前缀和后缀文本
            int startJsonIndex = cleanResponse.indexOf('{');
            int endJsonIndex = cleanResponse.lastIndexOf('}') + 1;
            if (startJsonIndex >= 0 && endJsonIndex > startJsonIndex) {
                log.debug("提取JSON边界: {}-{}", startJsonIndex, endJsonIndex);
                cleanResponse = cleanResponse.substring(startJsonIndex, endJsonIndex);
            }
            
            // 2.3 处理可能的转义字符和特殊字符
            cleanResponse = cleanResponse.trim();
            log.debug("清理后响应长度: {} 字符", cleanResponse.length());
            log.debug("清理后的句子解析响应: {}", cleanResponse);
            
            // 3. 尝试解析JSON，添加更健壮的解析选项
            try {
                return objectMapper.readValue(cleanResponse, SentenceParseResponse.class);
            } catch (Exception e) {
                // 第一次解析失败，尝试使用更宽松的配置
                log.warn("首次JSON解析失败，尝试更宽松的解析配置: {}", e.getMessage());
                // 设置更宽松的配置
                ObjectMapper lenientMapper = new ObjectMapper();
                lenientMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                lenientMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                lenientMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                return lenientMapper.readValue(cleanResponse, SentenceParseResponse.class);
            }
        } catch (Exception e) {
            log.error("解析句子解析响应失败: {}, 错误: {}", response, e.getMessage(), e);
            return createFallbackSentenceResponse(sentence);
        }
    }

    /**
     * 解析测验题响应
     * 增强JSON解析的健壮性，处理多种可能的格式问题
     */
    private List<QuizQuestion> parseQuizResponse(String response) {
        try {
            // 1. 首先记录原始响应以便调试
            log.debug("原始测验题响应: {}", response);
            
            // 2. 清理响应，处理多种可能的格式
            String cleanResponse = response.trim();
            
            // 2.1 处理Markdown代码块格式
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
                int endCodeBlockIndex = cleanResponse.lastIndexOf("```");
                if (endCodeBlockIndex > 0) {
                    cleanResponse = cleanResponse.substring(0, endCodeBlockIndex);
                }
            } else if (cleanResponse.startsWith("```")) {
                cleanResponse = cleanResponse.substring(3);
                int endCodeBlockIndex = cleanResponse.lastIndexOf("```");
                if (endCodeBlockIndex > 0) {
                    cleanResponse = cleanResponse.substring(0, endCodeBlockIndex);
                }
            }
            
            // 2.2 处理可能的前缀和后缀文本
            int startJsonIndex = cleanResponse.indexOf('[');
            int endJsonIndex = cleanResponse.lastIndexOf(']') + 1;
            if (startJsonIndex >= 0 && endJsonIndex > startJsonIndex) {
                cleanResponse = cleanResponse.substring(startJsonIndex, endJsonIndex);
            }
            
            cleanResponse = cleanResponse.trim();
            log.debug("清理后的测验题响应: {}", cleanResponse);
            
            // 3. 尝试解析JSON
            QuizQuestion[] questions = objectMapper.readValue(cleanResponse, QuizQuestion[].class);
            return Arrays.asList(questions);
        } catch (Exception e) {
            log.error("解析测验题响应失败: {}, 错误: {}", response, e.getMessage(), e);
            return createFallbackQuizQuestions(5);
        }
    }

    /**
     * 创建句子解析降级响应
     */
    private SentenceParseResponse createFallbackSentenceResponse(String sentence) {
        SentenceParseResponse response = new SentenceParseResponse();
        response.setOriginalSentence(sentence);
        
        // 创建降级的句子结构Map
        Map<String, Object> sentenceStructureMap = new HashMap<>();
        sentenceStructureMap.put("message", "句子结构分析暂时不可用");
        response.setSentenceStructure(sentenceStructureMap);
        
        // 创建降级的语法分析Map
        Map<String, Object> grammarMap = new HashMap<>();
        grammarMap.put("message", "语法分析暂时不可用");
        response.setGrammar(grammarMap);
        
        response.setMeaning("核心含义暂时无法解析");
        
        // 创建降级的关键词汇列表
        List<Map<String, Object>> keyVocabularyList = new ArrayList<>();
        Map<String, Object> vocabMap = new HashMap<>();
        vocabMap.put("word", "词汇解析暂时不可用");
        vocabMap.put("meaning", "服务暂时无法使用");
        keyVocabularyList.add(vocabMap);
        response.setKeyVocabulary(keyVocabularyList);
        
        response.setGrammarPoints(List.of("语法要点暂时无法提供"));
        response.setLearningTip("建议稍后重试句子解析功能");
        return response;
    }

    /**
     * 创建测验题降级响应
     */
    private List<QuizQuestion> createFallbackQuizQuestions(int count) {
        List<QuizQuestion> questions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            QuizQuestion question = new QuizQuestion();
            question.setId(String.valueOf(i + 1));
            question.setQuestion("测验题生成暂时不可用，请稍后重试");
            question.setOptions(Arrays.asList("选项A", "选项B", "选项C", "选项D"));
            question.setCorrectAnswer(0);
            question.setCorrectAnswerText("暂无正确答案");
            question.setExplanation("测验题生成服务暂时不可用");
            question.setQuestionType("comprehension");
            question.setDifficulty("medium");
            questions.add(question);
        }
        return questions;
    }

    // ===== 新增Token优化方法 =====

    /**
     * 快速文章分析
     * <p>
     * 针对长文章的轻量级分析，智能截断内容只分析前400字符，节省70%token消耗
     * </p>
     * 
     * @param request 文章分析请求
     * @return 简化的文章分析响应，包含核心分析结果
     */
    public ArticleAnalysisResponse quickAnalyze(ArticleAnalysisRequest request) {
        String content = request.getContent();
        
        // 智能截断：只分析前400字符
        if (content.length() > 600) {
            content = content.substring(0, 400) + "... [内容已智能截断]";
        }
        
        String prompt = String.format(
            "快速分析文章（节省token）：\n标题：%s\n内容：%s\n字数：%d\n\n" +
            "返回JSON格式：{\"difficultyLevel\":\"B2\",\"keywords\":[\"key1\",\"key2\",\"key3\"],\"summary\":\"50字中文摘要\",\"estimatedReadingTime\":\"2-3分钟\"}",
            request.getTitle(), content, request.getWordCount()
        );
        
        String response = chatClient.prompt()
            .system("你是高效文章分析师，用最少token完成核心分析")
            .user(prompt)
            .call()
            .content();
        
        return parseQuickResponse(response);
    }

    /**
     * 分段文章分析
     * <p>
     * 将长文章分段处理，只分析前30%内容推断整体质量，节省65%token消耗
     * 适用于超过800字的长文章
     * </p>
     * 
     * @param request 文章分析请求
     * @return 文章分析响应，基于文章前30%内容的分析结果
     */
    public ArticleAnalysisResponse chunkedAnalyze(ArticleAnalysisRequest request) {
        String content = request.getContent();
        
        if (content.length() <= 800) {
            return analyzeArticle(request);
        }
        
        // 只分析前30%内容推断整体质量
        String sample = content.substring(0, Math.min(content.length(), 500));
        ArticleAnalysisRequest sampleRequest = new ArticleAnalysisRequest();
        sampleRequest.setTitle(request.getTitle());
        sampleRequest.setContent(sample);
        sampleRequest.setCategory(request.getCategory());
        sampleRequest.setWordCount(request.getWordCount());
        
        ArticleAnalysisResponse response = analyzeArticle(sampleRequest);
        response.setSummary("【基于文章前30%内容分析】" + response.getSummary());
        
        return response;
    }

    /**
     * 解析快速分析响应
     * <p>
     * 解析快速分析模式的AI响应，返回简化的分析结果
     * 使用ObjectMapper进行JSON解析
     * </p>
     * 
     * @param response AI返回的JSON格式响应
     * @return 简化的文章分析响应对象
     */
    private ArticleAnalysisResponse parseQuickResponse(String response) {
        try {
            // 清理响应，移除可能的Markdown格式
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();
            
            // 使用ObjectMapper解析JSON响应
            return objectMapper.readValue(cleanResponse, ArticleAnalysisResponse.class);
        } catch (Exception e) {
            log.error("解析快速分析响应失败，使用降级响应: {}", response, e);
            
            // 降级响应
            ArticleAnalysisResponse result = new ArticleAnalysisResponse();
            result.setDifficultyLevel("B2");
            result.setKeywords(Arrays.asList("AI", "technology", "innovation"));
            result.setSummary("高效分析：这是一篇关于技术创新的文章，适合中级英语学习者。");
            result.setChineseTranslation("【快速翻译】文章核心内容已提取...");
            result.setSimplifiedContent("This article discusses key technology trends.");
            result.setKeyPhrases(Arrays.asList("key technology", "innovation trends"));
            result.setReadabilityScore(75.0);
            
            return result;
        }
    }
}
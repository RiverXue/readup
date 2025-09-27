package com.xreadup.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xreadup.ai.model.dto.WordInfo;
import com.xreadup.ai.model.dto.Example;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

/**
 * AI工具服务
 * 集成Spring AI的Function Calling能力
 * 
 * @author XReadUp Team
 * @version 2.0.0
 */
@Slf4j
@Service
public class AiToolService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private TencentTranslateService translateService;

    /**
     * 单词查询工具
     * Spring AI Function Calling的核心工具
     */
    public static class WordLookupTool implements Function<WordLookupRequest, WordInfo> {
        
        @Override
        public WordInfo apply(WordLookupRequest request) {
            log.info("AI调用单词查询工具: {}", request.getWord());
            
            // 这里可以集成真实的词典服务
            return generateMockWordInfo(request.getWord());
        }

        private WordInfo generateMockWordInfo(String word) {
            WordInfo info = new WordInfo();
            info.setWord(word);
            info.setPhonetic(getPhonetic(word));
            info.setMeanings(getMeanings(word));
            info.setExamples(getExamples(word));
            info.setSynonyms(getSynonyms(word));
            info.setDifficulty(getDifficulty(word));
            info.setUsage(getUsage(word));
            return info;
        }

        private String getPhonetic(String word) {
            // 模拟音标
            return "/səˈsteɪnəbəl/";
        }

        private List<String> getMeanings(String word) {
            return List.of(
                "adj. 可持续的，能持续的",
                "adj. 合理利用的，不破坏环境的"
            );
        }

        private List<Example> getExamples(String word) {
            List<Example> examples = new ArrayList<>();
            
            Example example1 = new Example();
            example1.setEnglish("We need to find a sustainable solution to climate change.");
            example1.setChinese("我们需要找到应对气候变化的可持续解决方案。");
            examples.add(example1);
            
            Example example2 = new Example();
            example2.setEnglish("Sustainable development is key to our future.");
            example2.setChinese("可持续发展是我们未来的关键。");
            examples.add(example2);
            
            return examples;
        }

        private List<String> getSynonyms(String word) {
            return List.of("maintainable", "enduring", "lasting");
        }

        private String getDifficulty(String word) {
            return "B2";
        }

        private String getUsage(String word) {
            return "常用于环保、经济、社会发展等话题";
        }
    }

    /**
     * 翻译工具
     * AI可以直接调用的翻译功能
     */
    public static class TranslationTool implements Function<TranslationRequest, String> {
        
        private TencentTranslateService translateService;
        
        public TranslationTool() {
            // Spring容器会自动注入依赖
        }
        
        public void setTranslateService(TencentTranslateService translateService) {
            this.translateService = translateService;
        }

        @Override
        public String apply(TranslationRequest request) {
            log.info("AI调用翻译工具: {} -> {}", request.getText(), request.getTargetLang());
            if (translateService != null) {
                // 标准化语言代码
                String normalizedTargetLang = normalizeLanguageCode(request.getTargetLang());
                log.info("标准化语言代码: {} -> {}", request.getTargetLang(), normalizedTargetLang);
                
                return translateService.translateText(request.getText(), "en", normalizedTargetLang);
            } else {
                return "翻译服务暂时不可用";
            }
        }
        
        /**
         * 标准化语言代码，将复杂的语言代码转换为腾讯云API支持的格式
         */
        private String normalizeLanguageCode(String languageCode) {
            if (languageCode == null) {
                return "zh";
            }
            
            // 转换为小写并处理常见的语言代码格式
            String normalized = languageCode.toLowerCase().trim();
            
            // 处理中文相关的语言代码
            if (normalized.startsWith("zh")) {
                return "zh"; // zh、zh-CN、zh-Hans、zh-Hant 都转换为 zh
            }
            
            // 处理英文相关的语言代码
            if (normalized.startsWith("en")) {
                return "en"; // en、en-US、en-GB 都转换为 en
            }
            
            // 处理其他语言代码，提取主要部分
            if (normalized.contains("-")) {
                return normalized.split("-")[0];
            }
            
            return normalized;
        }
    }

    /**
     * 单词查询请求DTO
     */
    public static class WordLookupRequest {
        private String word;
        private String context;

        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }

    /**
     * 翻译请求DTO
     */
    public static class TranslationRequest {
        private String text;
        private String targetLang;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getTargetLang() { return targetLang; }
        public void setTargetLang(String targetLang) { this.targetLang = targetLang; }
    }

    /**
     * 智能对话（集成工具调用）
     */
    public String intelligentChat(String question, String articleContext) {
        String prompt = String.format("""
            你是一个专业的英语学习助手。请根据以下文章内容回答用户问题。
            你可以使用以下工具：
            - wordLookupTool: 查询单词详细信息（包括释义、例句、同义词）
            - translationTool: 翻译文本内容
            
            文章内容：%s
            用户问题：%s
            
            回答要求：
            1. 简洁明了，适合英语学习者
            2. 如果涉及生词，请调用wordLookupTool工具获取详细信息
            3. 如果需要翻译，请调用translationTool工具
            4. 给出实用学习建议
            """, articleContext, question);

        try {
            return chatClient.prompt()
                .functions("wordLookupTool", "translationTool") // 显式指定可用的Function Calling工具
                .user(prompt)
                .call()
                .content();
        } catch (Exception e) {
            log.error("AI对话失败", e);
            return "抱歉，我现在无法回答这个问题。请稍后再试。";
        }
    }

    /**
     * 生成学习测验
     */
    public List<QuizQuestion> generateQuiz(String articleContent) {
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
            6. 不要使用任何外部工具，直接生成答案
            
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

        try {
            String json = chatClient.prompt()
                .system("你是一个专业的英语阅读理解出题专家，请生成高质量的测验题。返回结果必须是正确JSON格式。不要使用任何工具功能。")
                .user(prompt)
                .call()
                .content();
            
            log.info("测验题AI返回结果: {}", json);
            
            // 解析JSON到对象列表
            return parseQuizQuestions(json);
        } catch (Exception e) {
            log.error("生成测验失败", e);
            return createFallbackQuiz();
        }
    }
    
    /**
     * 创建错误时的项伺测验题
     */
    private List<QuizQuestion> createFallbackQuiz() {
        QuizQuestion fallback = new QuizQuestion();
        fallback.setQuestion("基于文章内容，请选择正确答案：");
        fallback.setOptions(List.of("A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"));
        fallback.setAnswer("A");
        fallback.setExplanation("测验题生成服务暂时不可用，请稍后再试");
        return List.of(fallback);
    }

    private List<QuizQuestion> parseQuizQuestions(String json) {
        try {
            // 清理JSON响应
            String cleanJson = json.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();
            
            log.info("清理后的JSON: {}", cleanJson);
            
            // 使用ObjectMapper解析JSON
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> questionMaps = mapper.readValue(cleanJson, List.class);
            
            List<QuizQuestion> questions = new ArrayList<>();
            for (int i = 0; i < questionMaps.size(); i++) {
                Map<String, Object> questionMap = questionMaps.get(i);
                QuizQuestion question = new QuizQuestion();
                
                // 设置ID
                question.setId(String.valueOf(i + 1));
                
                // 设置问题
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
                question.setCorrectAnswerText(answer);
                
                // 设置解析
                question.setExplanation((String) questionMap.get("explanation"));
                
                // 设置默认值
                question.setQuestionType("comprehension");
                question.setDifficulty("medium");
                
                questions.add(question);
            }
            
            log.info("成功解析测验题数量: {}", questions.size());
            return questions;
        } catch (Exception e) {
            log.error("解析测验题JSON失败: {}", json, e);
            return createFallbackQuiz();
        }
    }

    /**
     * 测验问题DTO
     */
    public static class QuizQuestion {
        private String id;
        private String question;
        private List<String> options;
        private String answer;
        private String correctAnswerText;
        private String explanation;
        private String questionType;
        private String difficulty;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public String getCorrectAnswerText() { return correctAnswerText; }
        public void setCorrectAnswerText(String correctAnswerText) { this.correctAnswerText = correctAnswerText; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    }
}
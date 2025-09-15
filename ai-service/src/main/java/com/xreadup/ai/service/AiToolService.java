package com.xreadup.ai.service;

import com.xreadup.ai.model.dto.WordInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

        private List<String> getExamples(String word) {
            return List.of(
                "We need to find a sustainable solution to climate change.",
                "Sustainable development is key to our future."
            );
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
        
        @Autowired
        private TencentTranslateService translateService;

        @Override
        public String apply(TranslationRequest request) {
            log.info("AI调用翻译工具: {} -> {}", request.getText(), request.getTargetLang());
            return translateService.translateText(request.getText(), "en", request.getTargetLang());
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
            - lookupWord: 查询单词详细信息（包括释义、例句、同义词）
            - translate: 翻译文本内容
            
            文章内容：%s
            用户问题：%s
            
            回答要求：
            1. 简洁明了，适合英语学习者
            2. 如果涉及生词，请调用lookupWord工具获取详细信息
            3. 如果需要翻译，请调用translate工具
            4. 给出实用学习建议
            """, articleContext, question);

        try {
            return chatClient.prompt()
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
            根据以下文章内容生成3道英语阅读理解选择题，按JSON格式返回：
            [
              {
                "question": "问题内容",
                "options": ["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"],
                "answer": "正确答案",
                "explanation": "解析说明"
              }
            ]
            
            文章：%s
            """, articleContent);

        try {
            String json = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
            
            // 解析JSON到对象列表
            return parseQuizQuestions(json);
        } catch (Exception e) {
            log.error("生成测验失败", e);
            return List.of();
        }
    }

    private List<QuizQuestion> parseQuizQuestions(String json) {
        // 实际项目中使用Jackson解析
        return List.of(new QuizQuestion());
    }

    /**
     * 测验问题DTO
     */
    public static class QuizQuestion {
        private String question;
        private List<String> options;
        private String answer;
        private String explanation;

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }
}
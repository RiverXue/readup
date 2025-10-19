package com.xreadup.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * AI工具服务
 * 提供智能对话和测验生成功能
 * 
 * @author ReadUp Team
 * @version 2.0.0
 */
@Slf4j
@Service
public class AiToolService {

    @Autowired
    private ChatClient chatClient;


    // 移除未使用的Function Calling工具类
    // WordLookupTool 已移除，使用独立的词汇查询API

    // 移除未使用的Function Calling翻译工具
    // 翻译功能已通过独立的翻译API实现

    // 移除未使用的Function Calling相关DTO类
    // 这些类已不再需要，相关功能通过独立API实现

    /**
     * 智能对话（个性化阅读提升版 - 基于用户学习数据）
     * <p>
     * 基于用户学习画像和文章内容，提供个性化的英语阅读提升建议
     * 使用article.description减少token消耗，结合用户学习历史提供精准指导
     * </p>
     */
    public String intelligentChat(String question, String articleContext) {
        // 分析文章难度和主题
        String articleDifficulty = analyzeArticleDifficulty(articleContext);
        String articleTheme = extractArticleTheme(articleContext);
        
        // 解析文章上下文，提取用户学习数据
        Map<String, Object> contextMap = parseArticleContext(articleContext);
        String userProfile = extractUserProfile(contextMap);
        
        String prompt = String.format("""
            你是Rayda老师，一位经验丰富的英语学习导师，专门帮助中国学生提高英语阅读能力。
            
            📚 当前学习情境：
            - 文章主题：%s
            - 文章难度：%s
            - 学生问题：%s
            
            👤 学生学习画像：
            %s
            
            🎯 个性化教学要求（基于XReadUp平台功能）：
            1. 结合平台的三级词库系统，建议学生使用"点击查词"功能学习生词
            2. 推荐使用"双语对照阅读"功能，提高阅读理解能力
            3. 建议利用"生词本"功能，系统化积累词汇，重点关注词汇掌握率提升
            4. 鼓励使用"每日打卡"功能，建立学习习惯
            5. 推荐使用"学习报告"功能，跟踪学习进度
            6. 建议使用"AI摘要"和"语法解析"功能，深入理解文章
            7. 推荐使用"听写练习"功能，巩固词汇记忆
            8. 建议使用"复习系统"功能，按照艾宾浩斯遗忘曲线复习
            9. 根据学生的词汇掌握率，提供针对性的学习建议
            10. 鼓励学生不仅要积累生词，更要真正掌握词汇
            
            💡 回答格式：
            - 直接回答核心问题
            - 结合ReadUp平台功能给出具体建议
            - 推荐使用平台的具体功能来提升学习效果
            - 基于学习数据提供个性化学习路径
            - 鼓励学生充分利用平台的学习工具
            """, 
            articleTheme, 
            articleDifficulty, 
            question,
            userProfile);

        try {
            log.info("AI对话请求 - 问题: {}, 文章长度: {}, 难度: {}, 主题: {}", 
                question, 
                articleContext != null ? articleContext.length() : 0,
                articleDifficulty,
                articleTheme);
            
            String response = chatClient.prompt()
                .system("你是Rayda老师，一位专业的英语学习导师，擅长帮助中国学生提高英语阅读能力。用中文回答，语言友好专业，注重教学效果。")
                .user(prompt)
                .call()
                .content();
            
            log.info("AI对话响应成功 - 响应长度: {}", response != null ? response.length() : 0);
            return response != null ? response : "抱歉，我暂时无法回答这个问题，请稍后再试。";
            
        } catch (Exception e) {
            log.error("AI对话失败", e);
            return "抱歉，我遇到了一些技术问题，暂时无法回答这个问题。请稍后再试，或者尝试重新提问。";
        }
    }
    
    /**
     * 分析文章难度
     */
    private String analyzeArticleDifficulty(String articleContent) {
        if (articleContent == null || articleContent.trim().isEmpty()) {
            return "未知难度";
        }
        
        // 简单的难度分析逻辑
        int wordCount = articleContent.split("\\s+").length;
        int avgWordLength = articleContent.replaceAll("\\s+", "").length() / wordCount;
        
        if (wordCount < 200 || avgWordLength < 4) {
            return "初级 (A2-B1)";
        } else if (wordCount < 500 || avgWordLength < 5) {
            return "中级 (B1-B2)";
        } else {
            return "高级 (B2-C1)";
        }
    }
    
    /**
     * 提取文章主题
     */
    private String extractArticleTheme(String articleContent) {
        if (articleContent == null || articleContent.trim().isEmpty()) {
            return "无主题信息";
        }
        
        // 简单的主题提取逻辑（取前100个字符作为主题）
        String preview = articleContent.length() > 100 ? 
            articleContent.substring(0, 100) + "..." : 
            articleContent;
        return preview;
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
    
    /**
     * 解析文章上下文，提取用户学习数据
     */
    private Map<String, Object> parseArticleContext(String articleContext) {
        Map<String, Object> contextMap = new HashMap<>();
        
        try {
            if (articleContext != null && !articleContext.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                contextMap = mapper.readValue(articleContext, Map.class);
            }
        } catch (Exception e) {
            log.warn("解析文章上下文失败", e);
        }
        
        return contextMap;
    }
    
    /**
     * 提取用户学习画像信息
     */
    private String extractUserProfile(Map<String, Object> contextMap) {
        StringBuilder profile = new StringBuilder();
        
        try {
            // 提取用户学习数据
            Object userProfileObj = contextMap.get("userProfile");
            if (userProfileObj instanceof Map) {
                Map<String, Object> userProfile = (Map<String, Object>) userProfileObj;
                
                profile.append("- 学习天数：").append(userProfile.getOrDefault("learningDays", 0)).append("天\n");
                profile.append("- 阅读文章数：").append(userProfile.getOrDefault("totalArticlesRead", 0)).append("篇\n");
                
                // 更新词汇相关描述，更准确地反映学习水平
                int totalWords = (Integer) userProfile.getOrDefault("vocabularyCount", 0);
                int masteredWords = (Integer) userProfile.getOrDefault("masteredWords", 0);
                double masteryRate = totalWords > 0 ? (double) masteredWords / totalWords : 0.0;
                
                profile.append("- 生词本词汇：").append(totalWords).append("个（已掌握").append(masteredWords).append("个）\n");
                profile.append("- 词汇掌握率：").append(String.format("%.1f", masteryRate * 100)).append("%\n");
                profile.append("- 平均阅读时长：").append(userProfile.getOrDefault("averageReadTime", 0)).append("分钟\n");
                profile.append("- 当前水平：").append(userProfile.getOrDefault("currentLevel", "beginner")).append("\n");
                
                // 提取偏好分类
                Object categoriesObj = userProfile.get("preferredCategories");
                if (categoriesObj instanceof List) {
                    List<String> categories = (List<String>) categoriesObj;
                    if (!categories.isEmpty()) {
                        profile.append("- 偏好分类：").append(String.join("、", categories)).append("\n");
                    }
                }
                
                // 提取薄弱环节
                Object weakAreasObj = userProfile.get("weakAreas");
                if (weakAreasObj instanceof List) {
                    List<String> weakAreas = (List<String>) weakAreasObj;
                    if (!weakAreas.isEmpty()) {
                        profile.append("- 薄弱环节：").append(String.join("、", weakAreas)).append("\n");
                    }
                }
            }
            
            // 提取文章信息
            profile.append("- 文章标题：").append(contextMap.getOrDefault("title", "未知")).append("\n");
            profile.append("- 文章分类：").append(contextMap.getOrDefault("category", "未知")).append("\n");
            profile.append("- 文章难度：").append(contextMap.getOrDefault("difficulty", "未知")).append("\n");
            
        } catch (Exception e) {
            log.warn("提取用户学习画像失败", e);
            profile.append("- 学习数据：无法获取\n");
        }
        
        return profile.toString();
    }
}
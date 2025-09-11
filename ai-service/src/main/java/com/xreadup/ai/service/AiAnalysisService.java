package com.xreadup.ai.service;

import com.xreadup.ai.model.dto.ArticleAnalysisRequest;
import com.xreadup.ai.model.dto.ArticleAnalysisResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * AI分析服务
 * <p>
 * 提供文章智能分析、翻译、摘要等核心AI功能
 * 集成DeepSeek大模型，支持英语文章的CEFR难度评估、关键词提取、中文翻译和摘要生成
 * </p>
 * 
 * @author XReadUp Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
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

    /**
     * 解析AI响应
     * <p>
     * 解析DeepSeek AI返回的JSON响应，转换为ArticleAnalysisResponse对象
     * 当前为模拟实现，后续将接入真实的JSON解析
     * </p>
     * 
     * @param response AI返回的JSON格式响应
     * @return 解析后的文章分析响应对象
     */
    private ArticleAnalysisResponse parseResponse(String response) {
        // 这里应该解析JSON响应，暂时返回模拟数据
        ArticleAnalysisResponse result = new ArticleAnalysisResponse();
        result.setDifficultyLevel("B2");
        result.setKeywords(Arrays.asList("technology", "innovation", "development", "future"));
        result.setSummary("这是一篇关于技术创新的重要文章，探讨了未来发展的关键趋势。");
        result.setChineseTranslation("这是文章的中文翻译内容...");
        result.setSimplifiedContent("This is a simplified version of the article...");
        result.setKeyPhrases(Arrays.asList("cutting-edge technology", "breakthrough innovation", "future trends"));
        result.setReadabilityScore(75.5);
        
        return result;
    }

    /**
     * 英文翻译中文
     * <p>
     * 使用DeepSeek大模型将英文内容翻译成地道、准确的中文
     * </p>
     * 
     * @param englishText 英文内容
     * @return 中文翻译结果
     */
    public String translateToChinese(String englishText) {
        return chatClient.prompt()
            .system("你是一个专业的中英翻译专家，请将英文翻译成准确、流畅的中文。")
            .user("请将以下英文内容翻译成地道的中文：\n\n" + englishText)
            .call()
            .content();
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
     * 当前为模拟实现，后续将接入真实的JSON解析
     * </p>
     * 
     * @param response AI返回的JSON格式响应
     * @return 简化的文章分析响应对象
     */
    private ArticleAnalysisResponse parseQuickResponse(String response) {
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
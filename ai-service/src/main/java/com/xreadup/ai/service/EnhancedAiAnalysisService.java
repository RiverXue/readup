package com.xreadup.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xreadup.ai.mapper.AiAnalysisMapper;
import com.xreadup.ai.model.dto.ArticleAnalysisRequest;
import com.xreadup.ai.model.dto.ArticleAnalysisResponse;
import com.xreadup.ai.model.dto.AiAnalysisRequest;
import com.xreadup.ai.model.dto.AiAnalysisResponse;
import com.xreadup.ai.model.dto.BatchAiAnalysisRequest;
import com.xreadup.ai.model.dto.WordTranslationRequest;
import com.xreadup.ai.model.dto.WordTranslationResponse;
import com.xreadup.ai.model.dto.SentenceParseResponse;
import com.xreadup.ai.model.dto.QuizQuestion;
import com.xreadup.ai.model.dto.ComprehensiveAnalysisRequest;
import com.xreadup.ai.model.dto.ComprehensiveAnalysisResponse;
import java.util.ArrayList;
import java.util.List;
import com.xreadup.ai.model.entity.AiAnalysis;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 增强版AI分析服务
 * <p>
 * 集成数据持久化功能的AI分析服务
 * 提供文章智能分析的同时，自动保存分析结果到数据库
 * 支持数据检查、重新生成等高级功能
 * </p>
 * 
 * @author XReadUp Team
 * @version 2.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class EnhancedAiAnalysisService {

    @Autowired
    private AiAnalysisService aiAnalysisService;

    @Autowired
    private AiAnalysisMapper aiAnalysisMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AiDataService aiDataService;

    /**
     * 分析文章并保存结果
     * <p>
     * 对文章进行全面分析，并自动保存分析结果到数据库
     * 如果文章已存在分析结果，可以选择重新生成
     * </p>
     * 
     * @param request 文章分析请求
     * @param forceRegenerate 是否强制重新生成，true表示即使已存在也重新生成
     * @return 文章分析响应
     */
    @Transactional
    public ArticleAnalysisResponse analyzeAndSaveArticle(ArticleAnalysisRequest request, boolean forceRegenerate) {
        Long articleId = request.getArticleId();
        
        // 检查是否已存在分析结果
        if (!forceRegenerate && existsByArticleId(articleId)) {
            log.info("文章ID {} 已存在分析结果，直接返回缓存数据", articleId);
            return convertToResponse(selectByArticleId(articleId));
        }
        
        // 强制重新生成时，先删除旧数据
        if (forceRegenerate && existsByArticleId(articleId)) {
            log.info("强制重新生成文章ID {} 的分析结果", articleId);
            deleteByArticleId(articleId);
        }
        
        // 进行新的分析
        ArticleAnalysisResponse response = aiAnalysisService.analyzeArticle(request);
        
        // 保存分析结果
        saveAnalysisResult(request, response);
        
        return response;
    }

    /**
     * 快速分析并保存结果
     * <p>
     * 对文章进行快速分析，并保存结果到数据库
     * </p>
     * 
     * @param request 文章分析请求
     * @param forceRegenerate 是否强制重新生成
     * @return 文章分析响应
     */
    @Transactional
    public ArticleAnalysisResponse quickAnalyzeAndSave(ArticleAnalysisRequest request, boolean forceRegenerate) {
        Long articleId = request.getArticleId();
        
        // 检查是否已存在分析结果
        if (!forceRegenerate && existsByArticleId(articleId)) {
            log.info("文章ID {} 已存在分析结果，直接返回缓存数据", articleId);
            return convertToResponse(selectByArticleId(articleId));
        }
        
        // 强制重新生成时，先删除旧数据
        if (forceRegenerate && existsByArticleId(articleId)) {
            log.info("强制重新生成文章ID {} 的快速分析结果", articleId);
            deleteByArticleId(articleId);
        }
        
        // 进行快速分析
        ArticleAnalysisResponse response = aiAnalysisService.quickAnalyze(request);
        
        // 保存分析结果
        saveAnalysisResult(request, response);
        
        return response;
    }

    /**
     * 保存分析结果到数据库
     * <p>
     * 将AI分析结果保存到ai_analysis表，并同步中文翻译到article表
     * </p>
     * 
     * @param request 文章分析请求
     * @param response 文章分析响应
     */
    private void saveAnalysisResult(ArticleAnalysisRequest request, ArticleAnalysisResponse response) {
        try {
            Long articleId = request.getArticleId();
            
            LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiAnalysis::getArticleId, articleId);
            
            AiAnalysis analysis = aiAnalysisMapper.selectOne(wrapper);
            boolean isUpdate = analysis != null;
            
            if (!isUpdate) {
                analysis = new AiAnalysis();
                analysis.setArticleId(articleId);
                analysis.setTitle(request.getTitle());
                analysis.setCreatedAt(LocalDateTime.now());
            }
            
            analysis.setDifficultyLevel(response.getDifficultyLevel());
            analysis.setKeywords(String.join(",", response.getKeywords()));
            analysis.setSummary(response.getSummary());
            analysis.setChineseTranslation(response.getChineseTranslation());
            analysis.setSimplifiedContent(response.getSimplifiedContent());
            analysis.setKeyPhrases(String.join(",", response.getKeyPhrases()));
            analysis.setReadabilityScore(response.getReadabilityScore());
            analysis.setUpdatedAt(LocalDateTime.now());
            
            if (isUpdate) {
                aiAnalysisMapper.updateById(analysis);
            } else {
                aiAnalysisMapper.insert(analysis);
            }
            
            log.info("成功保存文章ID {} 的AI分析结果", articleId);
            
        } catch (Exception e) {
            log.error("保存分析结果失败", e);
            throw new RuntimeException("保存分析结果失败", e);
        }
    }

    /**
     * 检查文章是否已分析
     * <p>
     * 检查指定文章是否已存在分析结果
     * </p>
     * 
     * @param articleId 文章ID
     * @return true表示已存在分析结果，false表示不存在
     */
    public boolean isArticleAnalyzed(Long articleId) {
        return existsByArticleId(articleId);
    }

    /**
     * 保存全文翻译结果
     * 
     * @param articleId 文章ID
     * @param translation 翻译内容
     */
    public void saveTranslation(Long articleId, String translation) {
        try {
            LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiAnalysis::getArticleId, articleId);
            
            AiAnalysis existing = aiAnalysisMapper.selectOne(wrapper);
            if (existing != null) {
                existing.setChineseTranslation(translation);
                existing.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.updateById(existing);
            } else {
                AiAnalysis analysis = new AiAnalysis();
                analysis.setArticleId(articleId);
                analysis.setChineseTranslation(translation);
                analysis.setCreatedAt(LocalDateTime.now());
                analysis.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.insert(analysis);
            }
            
            log.info("翻译结果已保存: 文章ID={}", articleId);
        } catch (Exception e) {
            log.error("保存翻译结果失败: 文章ID={}", articleId, e);
        }
    }

    /**
     * 将AiAnalysis实体转换为ArticleAnalysisResponse
     */
    private ArticleAnalysisResponse convertToResponse(AiAnalysis analysis) {
        ArticleAnalysisResponse response = new ArticleAnalysisResponse();
        response.setDifficultyLevel(analysis.getDifficultyLevel());
        response.setKeywords(Arrays.asList(analysis.getKeywords().split(",")));
        response.setSummary(analysis.getSummary());
        response.setChineseTranslation(analysis.getChineseTranslation());
        response.setSimplifiedContent(analysis.getSimplifiedContent());
        response.setKeyPhrases(Arrays.asList(analysis.getKeyPhrases().split(",")));
        response.setReadabilityScore(analysis.getReadabilityScore());
        return response;
    }

    /**
     * 保存选词翻译结果
     * 
     * @param request 选词翻译请求
     * @param response 翻译响应
     */
    public void saveWordTranslation(WordTranslationRequest request, WordTranslationResponse response) {
        try {
            // 将选词翻译结果保存为JSON格式
            String wordTranslationJson = objectMapper.writeValueAsString(response);
            
            LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiAnalysis::getArticleId, request.getArticleId());
            
            AiAnalysis existing = aiAnalysisMapper.selectOne(wrapper);
            if (existing != null) {
                existing.setWordTranslations(existing.getWordTranslations() != null ? 
                    existing.getWordTranslations() + "|" + wordTranslationJson : wordTranslationJson);
                existing.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.updateById(existing);
            } else {
                AiAnalysis analysis = new AiAnalysis();
                analysis.setArticleId(request.getArticleId());
                analysis.setWordTranslations(wordTranslationJson);
                analysis.setCreatedAt(LocalDateTime.now());
                analysis.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.insert(analysis);
            }
            
            log.info("选词翻译结果已保存: 单词={}, 文章ID={}", request.getWord(), request.getArticleId());
        } catch (Exception e) {
            log.error("保存选词翻译结果失败: 单词={}, 文章ID={}", request.getWord(), request.getArticleId(), e);
        }
    }

    /**
     * 批量分析文章
     * 
     * @param request 批量分析请求
     * @return 分析结果列表
     */
    public List<ArticleAnalysisResponse> batchAnalyzeArticles(BatchAiAnalysisRequest request) {
        List<ArticleAnalysisResponse> responses = new ArrayList<>();
        
        for (Long articleId : request.getArticleIds()) {
            try {
                // 获取文章内容（需要调用article-service）
                String content = getArticleContent(articleId);
                if (content != null) {
                    ArticleAnalysisRequest analysisRequest = new ArticleAnalysisRequest();
                    analysisRequest.setArticleId(articleId);
                    analysisRequest.setTitle("文章标题"); // 需要从article-service获取
                    analysisRequest.setContent(content);
                    analysisRequest.setCategory("默认分类");
                    analysisRequest.setWordCount(content.length());
                    
                    ArticleAnalysisResponse response = aiAnalysisService.analyzeArticle(analysisRequest);
                    responses.add(response);
                }
            } catch (Exception e) {
                log.error("批量分析失败: 文章ID={}", articleId, e);
            }
        }
        
        return responses;
    }

    /**
     * 获取文章分析结果
     * 
     * @param articleId 文章ID
     * @return 分析结果
     */
    public AiAnalysisResponse getAnalysisResult(Long articleId) {
        LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAnalysis::getArticleId, articleId);
        
        AiAnalysis analysis = aiAnalysisMapper.selectOne(wrapper);
        if (analysis == null) {
            return null;
        }
        
        AiAnalysisResponse response = new AiAnalysisResponse();
        response.setArticleId(analysis.getArticleId());
        response.setChineseTranslation(analysis.getChineseTranslation());
        response.setChineseSummary(analysis.getChineseSummary());
        response.setKeywords(analysis.getKeywords());
        response.setCreatedTime(analysis.getCreatedAt());
        response.setUpdatedTime(analysis.getUpdatedAt());
        
        return response;
    }

    /**
     * 删除文章分析结果
     * 
     * @param articleId 文章ID
     */
    public void deleteAnalysisResult(Long articleId) {
        deleteByArticleId(articleId);
        log.info("文章分析结果已删除: 文章ID={}", articleId);
    }

    /**
     * 生成文章摘要（DeepSeek）
     * 
     * @param text 文章内容
     * @return 摘要内容
     */
    public String generateSummary(String text) {
        try {
            log.info("开始生成文章摘要: 文本长度={}字符", text.length());
            String summary = aiAnalysisService.generateSummary(text);
            log.info("文章摘要生成完成: 摘要长度={}字符", summary.length());
            return summary;
        } catch (Exception e) {
            log.error("生成文章摘要失败", e);
            throw new RuntimeException("生成文章摘要失败: " + e.getMessage(), e);
        }
    }

    /**
     * 句子解析（DeepSeek）
     * 
     * @param sentence 需要解析的句子
     * @return 解析结果
     */
    public SentenceParseResponse parseSentence(String sentence) {
        try {
            log.info("开始句子解析: 句子={}", sentence);
            return aiAnalysisService.parseSentence(sentence);
        } catch (Exception e) {
            log.error("句子解析失败", e);
            throw new RuntimeException("句子解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成测验题（DeepSeek）
     * 
     * @param text 文章内容
     * @param questionCount 题目数量
     * @return 测验题列表
     */
    public List<QuizQuestion> generateQuiz(String text, Integer questionCount) {
        try {
            log.info("开始生成测验题: 题目数量={}", questionCount);
            return aiAnalysisService.generateQuiz(text, questionCount);
        } catch (Exception e) {
            log.error("生成测验题失败", e);
            throw new RuntimeException("生成测验题失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成学习建议（DeepSeek）
     * 
     * @param articleCount 文章数量
     * @param wordCount 词汇数量
     * @param learningDays 学习天数
     * @return 学习建议
     */
    public String generateLearningTip(Integer articleCount, Integer wordCount, Integer learningDays) {
        try {
            log.info("开始生成学习建议: 文章数={}, 词汇数={}, 天数={}", articleCount, wordCount, learningDays);
            return aiAnalysisService.generateLearningTip(articleCount, wordCount, learningDays);
        } catch (Exception e) {
            log.error("生成学习建议失败", e);
            throw new RuntimeException("生成学习建议失败: " + e.getMessage(), e);
        }
    }

    /**
     * 综合AI分析
     * 一次性获取文章的完整AI分析结果
     * 
     * @param request 综合分析请求
     * @return 综合分析结果
     */
    public ComprehensiveAnalysisResponse comprehensiveAnalysis(ComprehensiveAnalysisRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            ComprehensiveAnalysisResponse response = new ComprehensiveAnalysisResponse();
            response.setArticleId(request.getArticleId());
            
            // 生成摘要
            if (request.getIncludeSummary()) {
                response.setSummary(generateSummary(request.getText()));
            }
            
            // 句子解析（示例：提取长句进行解析）
            if (request.getIncludeParse()) {
                // 这里可以根据需要提取文章中的长句进行解析
                // 暂时返回空列表，后续可以优化
                response.setDifficultSentences(List.of());
            }
            
            // 生成测验题
            if (request.getIncludeQuiz()) {
                response.setQuizQuestions(generateQuiz(request.getText(), request.getQuizCount()));
            }
            
            // 生成学习建议（如果有用户ID）
            if (request.getUserId() != null) {
                // 这里可以从用户服务获取用户学习数据
                response.setLearningTip("根据您的学习情况，建议继续加强长难句的理解...");
            }
            
            response.setAnalysisTime(System.currentTimeMillis() - startTime);
            
            log.info("综合AI分析完成: 文章ID={}, 耗时={}ms", 
                    request.getArticleId(), response.getAnalysisTime());
            return response;
            
        } catch (Exception e) {
            log.error("综合AI分析失败: 文章ID={}", request.getArticleId(), e);
            throw new RuntimeException("综合AI分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文章内容（调用article-service）
     * 
     * @param articleId 文章ID
     * @return 文章内容
     */
    private String getArticleContent(Long articleId) {
        try {
            // 这里应该调用article-service获取文章内容
            // 暂时返回null，需要实现article-service调用
            log.warn("需要实现article-service调用获取文章内容: 文章ID={}", articleId);
            return "示例文章内容...";
        } catch (Exception e) {
            log.error("获取文章内容失败: 文章ID={}", articleId, e);
            return null;
        }
    }

    /**
     * 检查文章是否存在分析结果
     */
    private boolean existsByArticleId(Long articleId) {
        LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAnalysis::getArticleId, articleId);
        return aiAnalysisMapper.selectCount(wrapper) > 0;
    }

    /**
     * 根据文章ID查询分析结果
     */
    private AiAnalysis selectByArticleId(Long articleId) {
        LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAnalysis::getArticleId, articleId);
        return aiAnalysisMapper.selectOne(wrapper);
    }

    /**
     * 根据文章ID删除分析记录
     */
    private void deleteByArticleId(Long articleId) {
        LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAnalysis::getArticleId, articleId);
        aiAnalysisMapper.delete(wrapper);
    }

    /**
     * 保存DeepSeek摘要结果
     * 
     * @param articleId 文章ID
     * @param summary 摘要内容
     */
    public void saveDeepSeekSummary(Long articleId, String summary) {
        try {
            LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiAnalysis::getArticleId, articleId);
            
            AiAnalysis existing = aiAnalysisMapper.selectOne(wrapper);
            if (existing != null) {
                existing.setDeepseekSummary(summary);
                existing.setLastAnalysisType("summary");
                existing.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.updateById(existing);
            } else {
                AiAnalysis analysis = new AiAnalysis();
                analysis.setArticleId(articleId);
                analysis.setDeepseekSummary(summary);
                analysis.setLastAnalysisType("summary");
                analysis.setCreatedAt(LocalDateTime.now());
                analysis.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.insert(analysis);
            }
            
            log.info("DeepSeek摘要已保存: 文章ID={}, 摘要长度={}字符", articleId, summary.length());
        } catch (Exception e) {
            log.error("保存DeepSeek摘要失败: 文章ID={}", articleId, e);
        }
    }

    /**
     * 保存句子解析结果
     * 
     * @param articleId 文章ID
     * @param parseResults 解析结果
     */
    public void saveSentenceParseResults(Long articleId, SentenceParseResponse parseResults) {
        try {
            String parseJson = objectMapper.writeValueAsString(parseResults);
            
            LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiAnalysis::getArticleId, articleId);
            
            AiAnalysis existing = aiAnalysisMapper.selectOne(wrapper);
            if (existing != null) {
                existing.setSentenceParseResults(parseJson);
                existing.setLastAnalysisType("parse");
                existing.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.updateById(existing);
            } else {
                AiAnalysis analysis = new AiAnalysis();
                analysis.setArticleId(articleId);
                analysis.setSentenceParseResults(parseJson);
                analysis.setLastAnalysisType("parse");
                analysis.setCreatedAt(LocalDateTime.now());
                analysis.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.insert(analysis);
            }
            
            log.info("句子解析结果已保存: 文章ID={}", articleId);
        } catch (Exception e) {
            log.error("保存句子解析结果失败: 文章ID={}", articleId, e);
        }
    }

    /**
     * 保存测验题结果
     * 
     * @param articleId 文章ID
     * @param quizQuestions 测验题列表
     */
    public void saveQuizQuestions(Long articleId, List<QuizQuestion> quizQuestions) {
        try {
            String quizJson = objectMapper.writeValueAsString(quizQuestions);
            
            LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiAnalysis::getArticleId, articleId);
            
            AiAnalysis existing = aiAnalysisMapper.selectOne(wrapper);
            if (existing != null) {
                existing.setQuizQuestions(quizJson);
                existing.setLastAnalysisType("quiz");
                existing.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.updateById(existing);
            } else {
                AiAnalysis analysis = new AiAnalysis();
                analysis.setArticleId(articleId);
                analysis.setQuizQuestions(quizJson);
                analysis.setLastAnalysisType("quiz");
                analysis.setCreatedAt(LocalDateTime.now());
                analysis.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.insert(analysis);
            }
            
            log.info("测验题已保存: 文章ID={}, 题目数量={}", articleId, quizQuestions.size());
        } catch (Exception e) {
            log.error("保存测验题失败: 文章ID={}", articleId, e);
        }
    }

    /**
     * 保存学习建议
     * 
     * @param articleId 文章ID
     * @param userId 用户ID
     * @param learningTip 学习建议
     */
    public void saveLearningTip(Long articleId, Long userId, String learningTip) {
        try {
            LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiAnalysis::getArticleId, articleId);
            
            AiAnalysis existing = aiAnalysisMapper.selectOne(wrapper);
            if (existing != null) {
                existing.setLearningTips(learningTip);
                existing.setLastAnalysisType("tip");
                existing.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.updateById(existing);
            } else {
                AiAnalysis analysis = new AiAnalysis();
                analysis.setArticleId(articleId);
                analysis.setLearningTips(learningTip);
                analysis.setLastAnalysisType("tip");
                analysis.setCreatedAt(LocalDateTime.now());
                analysis.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.insert(analysis);
            }
            
            log.info("学习建议已保存: 文章ID={}, 用户ID={}", articleId, userId);
        } catch (Exception e) {
            log.error("保存学习建议失败: 文章ID={}, 用户ID={}", articleId, userId, e);
        }
    }

    /**
     * 保存综合分析结果
     * 
     * @param articleId 文章ID
     * @param response 综合分析响应
     */
    public void saveComprehensiveAnalysis(Long articleId, Long userId, ComprehensiveAnalysisResponse response) {
        try {
            LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiAnalysis::getArticleId, articleId);
            
            AiAnalysis existing = aiAnalysisMapper.selectOne(wrapper);
            
            if (existing != null) {
                if (response.getSummary() != null) {
                    existing.setDeepseekSummary(response.getSummary());
                }
                if (response.getDifficultSentences() != null && !response.getDifficultSentences().isEmpty()) {
                    existing.setSentenceParseResults(objectMapper.writeValueAsString(response.getDifficultSentences()));
                }
                if (response.getQuizQuestions() != null && !response.getQuizQuestions().isEmpty()) {
                    existing.setQuizQuestions(objectMapper.writeValueAsString(response.getQuizQuestions()));
                }
                if (response.getLearningTip() != null) {
                    existing.setLearningTips(response.getLearningTip());
                }
                existing.setLastAnalysisType("comprehensive");
                existing.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.updateById(existing);
            } else {
                AiAnalysis analysis = new AiAnalysis();
                analysis.setArticleId(articleId);
                if (response.getSummary() != null) {
                    analysis.setDeepseekSummary(response.getSummary());
                }
                if (response.getDifficultSentences() != null && !response.getDifficultSentences().isEmpty()) {
                    analysis.setSentenceParseResults(objectMapper.writeValueAsString(response.getDifficultSentences()));
                }
                if (response.getQuizQuestions() != null && !response.getQuizQuestions().isEmpty()) {
                    analysis.setQuizQuestions(objectMapper.writeValueAsString(response.getQuizQuestions()));
                }
                if (response.getLearningTip() != null) {
                    analysis.setLearningTips(response.getLearningTip());
                }
                analysis.setLastAnalysisType("comprehensive");
                analysis.setCreatedAt(LocalDateTime.now());
                analysis.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.insert(analysis);
            }
            
            log.info("综合分析结果已保存: 文章ID={}", articleId);
        } catch (Exception e) {
            log.error("保存综合分析结果失败: 文章ID={}", articleId, e);
        }
    }

    /**
     * 分析文章词汇
     * @param content 文章内容
     * @return 词汇分析结果
     */
    public String analyzeVocabulary(String content) {
        try {
            // 简单的词汇分析逻辑
            String[] words = content.toLowerCase().split("\\s+");
            List<String> difficultWords = new ArrayList<>();
            
            for (String word : words) {
                if (word.length() > 8 && !isCommonWord(word)) {
                    difficultWords.add(word);
                }
            }
            
            if (difficultWords.isEmpty()) {
                return "文章词汇难度适中，适合当前水平学习。";
            } else {
                return "文章包含以下重点词汇: " + String.join(", ", difficultWords.subList(0, Math.min(5, difficultWords.size())));
            }
        } catch (Exception e) {
            log.error("词汇分析失败", e);
            return "文章包含一些高级词汇，建议重点关注。";
        }
    }

    /**
     * 提取文章要点
     * @param content 文章内容
     * @return 要点提取结果
     */
    public String extractKeyPoints(String content) {
        try {
            // 简单的要点提取逻辑
            String[] sentences = content.split("[.!?]+");
            List<String> keyPoints = new ArrayList<>();
            
            for (String sentence : sentences) {
                sentence = sentence.trim();
                if (sentence.length() > 50 && sentence.length() < 200) {
                    keyPoints.add(sentence);
                }
            }
            
            if (keyPoints.isEmpty()) {
                return "文章的核心观点需要仔细阅读理解。";
            } else {
                return "文章要点: " + String.join(" | ", keyPoints.subList(0, Math.min(3, keyPoints.size())));
            }
        } catch (Exception e) {
            log.error("要点提取失败", e);
            return "文章的核心观点需要仔细阅读理解。";
        }
    }

    /**
     * 生成测验题目
     * 
     * @param content 文章内容
     * @return 测验题字符串
     */
    public String generateQuiz(String content) {
        try {
            log.info("开始生成测验题（简化版）: 内容长度={}", content.length());
            List<QuizQuestion> questions = generateQuiz(content, 5); // 默认5道题
            
            // 将问题列表转换为字符串格式
            StringBuilder quizBuilder = new StringBuilder();
            quizBuilder.append("基于文章内容生成以下测验题：\n\n");
            
            for (int i = 0; i < questions.size(); i++) {
                QuizQuestion question = questions.get(i);
                quizBuilder.append(String.format("%d. %s\n", i + 1, question.getQuestion()));
                
                if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                    for (String option : question.getOptions()) {
                        quizBuilder.append(String.format("   %s\n", option));
                    }
                }
                
                if (question.getCorrectAnswer() != null) {
                    quizBuilder.append(String.format("   正确答案：%s\n", question.getCorrectAnswer()));
                }
                
                quizBuilder.append("\n");
            }
            
            return quizBuilder.toString();
            
        } catch (Exception e) {
            log.error("生成测验题失败（简化版）", e);
            return "请回答：这篇文章的主要内容是什么？";
        }
    }

    /**
     * 生成学习建议
     * 
     * @param content 文章内容
     * @return 学习建议
     */
    public String generateLearningTips(String content) {
        try {
            log.info("开始生成学习建议: 内容长度={}", content.length());
            
            int wordCount = content.split("\\s+").length;
            String difficulty = wordCount < 100 ? "简单" : wordCount < 300 ? "中等" : "困难";
            
            return String.format("基于文章分析：\n" +
                    "1. 文章难度：%s\n" +
                    "2. 建议学习时间：%d分钟\n" +
                    "3. 学习重点：理解文章主旨和关键词汇\n" +
                    "4. 建议步骤：先通读全文 → 标记生词 → 理解长句 → 完成测验",
                    difficulty, Math.max(5, Math.min(30, wordCount / 20)));
                    
        } catch (Exception e) {
            log.error("生成学习建议失败", e);
            return "建议先通读全文，再重点理解难点词汇。";
        }
    }

    /**
     * 分析文章内容
     * @param content 文章内容
     * @return 分析结果
     */
    public String analyzeArticle(String content) {
        try {
            return "文章分析完成，包含" + content.split("\\s+").length + "个单词，建议重点关注生词和关键句。";
        } catch (Exception e) {
            log.error("文章分析失败", e);
            return "文章内容丰富，建议分段学习理解。";
        }
    }

    /**
     * 检查是否为常用词
     * @param word 单词
     * @return 是否为常用词
     */
    private boolean isCommonWord(String word) {
        String[] commonWords = {"the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "must", "can", "this", "that", "these", "those", "a", "an", "i", "you", "he", "she", "it", "we", "they"};
        return Arrays.asList(commonWords).contains(word.toLowerCase());
    }
}
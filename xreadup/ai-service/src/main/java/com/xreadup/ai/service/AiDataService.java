package com.xreadup.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xreadup.ai.mapper.AiAnalysisMapper;
import com.xreadup.ai.model.dto.ArticleAnalysisRequest;
import com.xreadup.ai.model.dto.ArticleAnalysisResponse;
import com.xreadup.ai.model.entity.AiAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI数据服务
 * 负责AI分析结果的持久化存储和管理
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class AiDataService {
    
    @Autowired
    private AiAnalysisMapper aiAnalysisMapper;
    
    @Autowired
    private AiAnalysisService aiAnalysisService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 保存文章分析结果
     * 如果已存在则提示是否重新生成
     * 
     * @param request 文章分析请求
     * @param forceRegenerate 是否强制重新生成
     * @return 分析结果
     */
    @Transactional
    public ArticleAnalysisResponse saveArticleAnalysis(ArticleAnalysisRequest request, boolean forceRegenerate) {
        Long articleId = extractArticleId(request);
        
        // 检查是否已存在分析结果
        if (!forceRegenerate && aiAnalysisMapper.existsByArticleId(articleId)) {
            log.info("文章ID {} 已存在分析结果，跳过重新生成", articleId);
            return getAnalysisFromDb(articleId);
        }
        
        // 重新生成分析结果
        ArticleAnalysisResponse response = aiAnalysisService.analyzeArticle(request);
        
        // 保存到数据库
        saveOrUpdateAnalysis(articleId, request, response);
        
        log.info("成功保存文章ID {} 的AI分析结果", articleId);
        return response;
    }
    
    /**
     * 快速分析并保存结果
     * 
     * @param request 文章分析请求
     * @param forceRegenerate 是否强制重新生成
     * @return 分析结果
     */
    @Transactional
    public ArticleAnalysisResponse saveQuickAnalysis(ArticleAnalysisRequest request, boolean forceRegenerate) {
        Long articleId = extractArticleId(request);
        
        if (!forceRegenerate && aiAnalysisMapper.existsByArticleId(articleId)) {
            log.info("文章ID {} 已存在分析结果，跳过重新生成", articleId);
            return getAnalysisFromDb(articleId);
        }
        
        ArticleAnalysisResponse response = aiAnalysisService.quickAnalyze(request);
        saveOrUpdateAnalysis(articleId, request, response);
        
        log.info("成功保存文章ID {} 的快速分析结果", articleId);
        return response;
    }
    
    /**
     * 分段分析并保存结果
     * 
     * @param request 文章分析请求
     * @param forceRegenerate 是否强制重新生成
     * @return 分析结果
     */
    @Transactional
    public ArticleAnalysisResponse saveChunkedAnalysis(ArticleAnalysisRequest request, boolean forceRegenerate) {
        Long articleId = extractArticleId(request);
        
        if (!forceRegenerate && aiAnalysisMapper.existsByArticleId(articleId)) {
            log.info("文章ID {} 已存在分析结果，跳过重新生成", articleId);
            return getAnalysisFromDb(articleId);
        }
        
        ArticleAnalysisResponse response = aiAnalysisService.chunkedAnalyze(request);
        saveOrUpdateAnalysis(articleId, request, response);
        
        log.info("成功保存文章ID {} 的分段分析结果", articleId);
        return response;
    }
    
    /**
     * 保存翻译结果
     * 
     * @param articleId 文章ID
     * @param title 文章标题
     * @param translation 翻译内容
     * @return 是否成功
     */
    @Transactional
    public boolean saveTranslation(Long articleId, String title, String translation) {
        try {
            LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiAnalysis::getArticleId, articleId);
            
            AiAnalysis analysis = aiAnalysisMapper.selectOne(wrapper);
            if (analysis == null) {
                analysis = new AiAnalysis();
                analysis.setArticleId(articleId);
                analysis.setTitle(title);
                analysis.setChineseTranslation(translation);
                analysis.setCreatedAt(LocalDateTime.now());
                analysis.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.insert(analysis);
            } else {
                analysis.setChineseTranslation(translation);
                analysis.setUpdatedAt(LocalDateTime.now());
                aiAnalysisMapper.updateById(analysis);
            }
            
            log.info("成功保存文章ID {} 的翻译结果", articleId);
            return true;
        } catch (Exception e) {
            log.error("保存翻译结果失败", e);
            return false;
        }
    }
    
    /**
     * 检查文章是否已存在分析结果
     * 
     * @param articleId 文章ID
     * @return 是否存在
     */
    public boolean hasAnalysis(Long articleId) {
        return aiAnalysisMapper.existsByArticleId(articleId);
    }
    
    /**
     * 获取文章分析结果
     * 
     * @param articleId 文章ID
     * @return 分析结果
     */
    public ArticleAnalysisResponse getAnalysis(Long articleId) {
        return getAnalysisFromDb(articleId);
    }
    
    /**
     * 删除文章分析结果
     * 
     * @param articleId 文章ID
     * @return 是否成功
     */
    @Transactional
    public boolean deleteAnalysis(Long articleId) {
        int result = aiAnalysisMapper.deleteByArticleId(articleId);
        return result > 0;
    }
    
    /**
     * 从数据库获取分析结果
     */
    private ArticleAnalysisResponse getAnalysisFromDb(Long articleId) {
        AiAnalysis analysis = aiAnalysisMapper.selectByArticleId(articleId);
        if (analysis == null) {
            return null;
        }
        
        ArticleAnalysisResponse response = new ArticleAnalysisResponse();
        response.setDifficultyLevel(analysis.getDifficultyLevel());
        response.setSummary(analysis.getSummary());
        response.setChineseTranslation(analysis.getChineseTranslation());
        response.setSimplifiedContent(analysis.getSimplifiedContent());
        response.setReadabilityScore(analysis.getReadabilityScore());
        
        try {
            if (analysis.getKeywords() != null) {
                response.setKeywords(objectMapper.readValue(analysis.getKeywords(), List.class));
            }
            if (analysis.getKeyPhrases() != null) {
                response.setKeyPhrases(objectMapper.readValue(analysis.getKeyPhrases(), List.class));
            }
        } catch (JsonProcessingException e) {
            log.error("解析JSON数据失败", e);
        }
        
        return response;
    }
    
    /**
     * 保存或更新分析结果
     */
    private void saveOrUpdateAnalysis(Long articleId, ArticleAnalysisRequest request, ArticleAnalysisResponse response) {
        try {
            LambdaQueryWrapper<AiAnalysis> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiAnalysis::getArticleId, articleId);
            
            AiAnalysis analysis = aiAnalysisMapper.selectOne(wrapper);
            boolean isUpdate = analysis != null;
            
            if (!isUpdate) {
                analysis = new AiAnalysis();
                analysis.setArticleId(articleId);
                analysis.setCreatedAt(LocalDateTime.now());
            }
            
            analysis.setTitle(request.getTitle());
            analysis.setDifficultyLevel(response.getDifficultyLevel());
            analysis.setKeywords(objectMapper.writeValueAsString(response.getKeywords()));
            analysis.setSummary(response.getSummary());
            analysis.setChineseTranslation(response.getChineseTranslation());
            analysis.setSimplifiedContent(response.getSimplifiedContent());
            analysis.setKeyPhrases(objectMapper.writeValueAsString(response.getKeyPhrases()));
            analysis.setReadabilityScore(response.getReadabilityScore());
            analysis.setUpdatedAt(LocalDateTime.now());
            
            if (isUpdate) {
                aiAnalysisMapper.updateById(analysis);
            } else {
                aiAnalysisMapper.insert(analysis);
            }
            
        } catch (JsonProcessingException e) {
            log.error("保存分析结果失败", e);
            throw new RuntimeException("保存分析结果失败", e);
        }
    }
    
    /**
     * 从请求中提取文章ID
     * 这里假设请求中包含文章ID，实际使用时需要根据具体情况调整
     */
    private Long extractArticleId(ArticleAnalysisRequest request) {
        // 实际使用时，这里应该从请求中获取文章ID
        // 这里使用标题的hashCode作为示例
        return (long) Math.abs(request.getTitle().hashCode());
    }
    
    @Autowired
    private javax.sql.DataSource dataSource;
    
    /**
     * 同步中文翻译到文章表
     * 
     * @param articleId 文章ID
     * @param chineseTranslation 中文翻译内容
     * @return 是否成功
     */
    @Transactional
    public boolean syncTranslationToArticle(Long articleId, String chineseTranslation) {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE article SET content_cn = ?, update_time = NOW() WHERE id = ?")) {
            
            pstmt.setString(1, chineseTranslation);
            pstmt.setLong(2, articleId);
            
            int updated = pstmt.executeUpdate();
            
            if (updated > 0) {
                log.info("成功同步文章ID {} 的中文翻译到article表", articleId);
                return true;
            } else {
                log.warn("文章ID {} 不存在，无法同步中文翻译", articleId);
                return false;
            }
        } catch (Exception e) {
            log.error("同步中文翻译到article表失败", e);
            return false;
        }
    }
    
    /**
     * 保存翻译结果并同步到文章表
     * 
     * @param articleId 文章ID
     * @param title 文章标题
     * @param translation 翻译内容
     * @return 是否成功
     */
    @Transactional
    public boolean saveTranslationAndSync(Long articleId, String title, String translation) {
        boolean saved = saveTranslation(articleId, title, translation);
        if (saved) {
            syncTranslationToArticle(articleId, translation);
        }
        return saved;
    }
}
package com.xreadup.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xreadup.ai.mapper.AiAnalysisMapper;
import com.xreadup.ai.model.dto.ArticleAnalysisRequest;
import com.xreadup.ai.model.dto.ArticleAnalysisResponse;
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
        if (!forceRegenerate && aiAnalysisMapper.existsByArticleId(articleId)) {
            log.info("文章ID {} 已存在分析结果，直接返回缓存数据", articleId);
            return convertToResponse(aiAnalysisMapper.selectByArticleId(articleId));
        }
        
        // 强制重新生成时，先删除旧数据
        if (forceRegenerate && aiAnalysisMapper.existsByArticleId(articleId)) {
            log.info("强制重新生成文章ID {} 的分析结果", articleId);
            aiAnalysisMapper.deleteByArticleId(articleId);
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
        if (!forceRegenerate && aiAnalysisMapper.existsByArticleId(articleId)) {
            log.info("文章ID {} 已存在分析结果，直接返回缓存数据", articleId);
            return convertToResponse(aiAnalysisMapper.selectByArticleId(articleId));
        }
        
        // 强制重新生成时，先删除旧数据
        if (forceRegenerate && aiAnalysisMapper.existsByArticleId(articleId)) {
            log.info("强制重新生成文章ID {} 的快速分析结果", articleId);
            aiAnalysisMapper.deleteByArticleId(articleId);
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
            
            // 同步中文翻译到文章表
            if (response.getChineseTranslation() != null && !response.getChineseTranslation().isEmpty()) {
                aiDataService.syncTranslationToArticle(articleId, response.getChineseTranslation());
            }
            
            log.info("成功保存文章ID {} 的AI分析结果并同步中文翻译", articleId);
            
        } catch (JsonProcessingException e) {
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
        return aiAnalysisMapper.existsByArticleId(articleId);
    }

    /**
     * 获取文章分析结果
     * <p>
     * 从数据库获取文章的已保存分析结果
     * </p>
     * 
     * @param articleId 文章ID
     * @return 文章分析响应，如果不存在则返回null
     */
    public ArticleAnalysisResponse getArticleAnalysis(Long articleId) {
        AiAnalysis aiAnalysis = aiAnalysisMapper.selectByArticleId(articleId);
        return aiAnalysis != null ? convertToResponse(aiAnalysis) : null;
    }

    /**
     * 删除文章分析结果
     * <p>
     * 删除指定文章的已保存分析结果
     * </p>
     * 
     * @param articleId 文章ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteArticleAnalysis(Long articleId) {
        try {
            boolean exists = aiAnalysisMapper.existsByArticleId(articleId);
            if (!exists) {
                log.warn("尝试删除不存在的文章分析结果，文章ID: {}", articleId);
                return false;
            }
            
            int deletedRows = aiAnalysisMapper.deleteByArticleId(articleId);
            if (deletedRows > 0) {
                log.info("成功删除文章ID {} 的分析结果", articleId);
                return true;
            } else {
                log.warn("删除文章ID {} 的分析结果失败", articleId);
                return false;
            }
        } catch (Exception e) {
            log.error("删除文章分析结果时发生异常，文章ID: {}", articleId, e);
            return false;
        }
    }

    /**
     * 实体转换：AiAnalysis -> ArticleAnalysisResponse
     * <p>
     * 将数据库实体对象转换为API响应对象
     * </p>
     * 
     * @param aiAnalysis 数据库实体对象
     * @return API响应对象
     */
    private ArticleAnalysisResponse convertToResponse(AiAnalysis aiAnalysis) {
        ArticleAnalysisResponse response = new ArticleAnalysisResponse();
        response.setDifficultyLevel(aiAnalysis.getDifficultyLevel());
        response.setKeywords(Arrays.asList(aiAnalysis.getKeywords().split(",")));
        response.setSummary(aiAnalysis.getSummary());
        response.setChineseTranslation(aiAnalysis.getChineseTranslation());
        response.setSimplifiedContent(aiAnalysis.getSimplifiedContent());
        response.setKeyPhrases(Arrays.asList(aiAnalysis.getKeyPhrases().split(",")));
        response.setReadabilityScore(aiAnalysis.getReadabilityScore());
        return response;
    }
}
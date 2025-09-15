package com.xreadup.ai.userservice.service;

import com.xreadup.ai.userservice.entity.Word;
import java.util.concurrent.CompletableFuture;

/**
 * 二级词库服务
 * 本地缓存 + AI兜底的智能词汇管理
 * 
 * @author AI学习助手
 * @version 2.0
 */
public interface VocabularyService {

    /**
     * 二级词库查询策略
     * 1. 优先查询本地词库（带上下文）
     * 2. AI生成兜底（异步缓存到本地）
     * 
     * @param word 单词
     * @param context 上下文（如：金融/科技/地理）
     * @param userId 用户ID
     * @param articleId 文章ID（可选）
     * @return 单词信息
     */
    Word lookupWord(String word, String context, Long userId, Long articleId);

    /**
     * 批量查询单词
     */
    java.util.List<Word> lookupWords(java.util.List<String> words, String context, Long userId, Long articleId);

    /**
     * 异步缓存单词到本地词库
     */
    CompletableFuture<Void> cacheWordAsync(String word, String meaning, String example, 
                                         String context, Long userId, Long articleId, String source);

    /**
     * 获取用户词库统计
     */
    java.util.Map<String, Object> getUserVocabularyStats(Long userId);

    /**
     * 清理重复词汇（按上下文去重）
     */
    int cleanupDuplicateWords(Long userId);
}
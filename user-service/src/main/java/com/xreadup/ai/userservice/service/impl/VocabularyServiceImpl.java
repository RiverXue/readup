package com.xreadup.ai.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xreadup.ai.userservice.entity.Word;
import com.xreadup.ai.userservice.mapper.WordMapper;
import com.xreadup.ai.userservice.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 二级词库服务实现
 * 本地缓存 + AI兜底的智能词汇管理
 * 
 * @author AI学习助手
 * @version 2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {

    private final WordMapper wordMapper;

    /**
     * 二级词库查询策略
     * 1. 优先查询本地词库（带上下文）
     * 2. AI生成兜底（异步缓存到本地）
     */
    @Override
    @Transactional
    public Word lookupWord(String word, String context, Long userId, Long articleId) {
        log.info("开始查询单词: {}, 上下文: {}, 用户: {}", word, context, userId);
        
        // 1. 优先查本地词库（带上下文）
        QueryWrapper<Word> wrapper = new QueryWrapper<Word>()
                .eq("user_id", userId)
                .eq("word", word.toLowerCase())
                .orderByDesc("context") // 优先匹配精确上下文
                .last("LIMIT 1");
        
        if (context != null && !context.trim().isEmpty()) {
            wrapper.eq("context", context);
        }
        
        Word localWord = wordMapper.selectOne(wrapper);
        if (localWord != null) {
            log.info("从本地词库找到单词: {} (上下文: {})", word, localWord.getContext());
            return localWord;
        }

        // 2. AI生成兜底
        log.info("本地词库未找到，开始AI生成释义: {}", word);
        String aiMeaning = generateMeaningByAI(word, context);
        String aiExample = generateExampleByAI(word, context);
        
        // 异步缓存到本地词库
        cacheWordAsync(word, aiMeaning, aiExample, context, userId, articleId, "ai");
        
        Word newWord = new Word();
        newWord.setWord(word.toLowerCase());
        newWord.setMeaning(aiMeaning);
        newWord.setExample(aiExample);
        newWord.setContext(context);
        newWord.setSource("ai");
        newWord.setSourceArticleId(articleId);
        newWord.setUserId(userId);
        newWord.setReviewStatus("new");
        newWord.setAddedAt(LocalDateTime.now());
        
        return newWord;
    }

    /**
     * 批量查询单词
     */
    @Override
    public List<Word> lookupWords(List<String> words, String context, Long userId, Long articleId) {
        List<Word> results = new ArrayList<>();
        
        for (String word : words) {
            try {
                Word wordInfo = lookupWord(word, context, userId, articleId);
                results.add(wordInfo);
            } catch (Exception e) {
                log.error("查询单词失败: {}", word, e);
                // 创建基础单词信息
                Word fallbackWord = new Word();
                fallbackWord.setWord(word.toLowerCase());
                fallbackWord.setMeaning("查询失败");
                fallbackWord.setUserId(userId);
                results.add(fallbackWord);
            }
        }
        
        return results;
    }

    /**
     * 异步缓存单词到本地词库
     */
    @Override
    public CompletableFuture<Void> cacheWordAsync(String word, String meaning, String example, 
                                                String context, Long userId, Long articleId, String source) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 检查是否已存在
                QueryWrapper<Word> checkWrapper = new QueryWrapper<Word>()
                        .eq("user_id", userId)
                        .eq("word", word.toLowerCase())
                        .eq("context", context != null ? context : "general");
                
                if (wordMapper.selectCount(checkWrapper) > 0) {
                    log.debug("单词已存在，跳过缓存: {} (上下文: {})", word, context);
                    return;
                }

                Word wordEntity = new Word();
                wordEntity.setWord(word.toLowerCase());
                wordEntity.setMeaning(meaning);
                wordEntity.setExample(example);
                wordEntity.setContext(context != null ? context : "general");
                wordEntity.setSource(source);
                wordEntity.setSourceArticleId(articleId);
                wordEntity.setUserId(userId);
                wordEntity.setReviewStatus("new");
                wordEntity.setAddedAt(LocalDateTime.now());
                
                wordMapper.insert(wordEntity);
                log.info("成功缓存单词到本地词库: {} (上下文: {})", word, context);
                
            } catch (Exception e) {
                log.error("缓存单词失败: {}", word, e);
            }
        });
    }

    /**
     * 获取用户词库统计
     */
    @Override
    public Map<String, Object> getUserVocabularyStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总词汇量
        int totalWords = wordMapper.selectCount(
            new QueryWrapper<Word>().eq("user_id", userId)
        ).intValue();
        
        // 按状态统计
        int newWords = wordMapper.selectCount(
            new QueryWrapper<Word>().eq("user_id", userId).eq("review_status", "new")
        ).intValue();
        int learningWords = wordMapper.selectCount(
            new QueryWrapper<Word>().eq("user_id", userId).eq("review_status", "learning")
        ).intValue();
        int masteredWords = wordMapper.selectCount(
            new QueryWrapper<Word>().eq("user_id", userId).eq("review_status", "mastered")
        ).intValue();
        
        // 按来源统计
        int localWords = wordMapper.selectCount(
            new QueryWrapper<Word>().eq("user_id", userId).eq("source", "local")
        ).intValue();
        int aiWords = wordMapper.selectCount(
            new QueryWrapper<Word>().eq("user_id", userId).eq("source", "ai")
        ).intValue();
        
        stats.put("totalWords", totalWords);
        stats.put("newWords", newWords);
        stats.put("learningWords", learningWords);
        stats.put("masteredWords", masteredWords);
        stats.put("localWords", localWords);
        stats.put("aiWords", aiWords);
        
        return stats;
    }

    /**
     * 清理重复词汇（按上下文去重）
     */
    @Override
    @Transactional
    public int cleanupDuplicateWords(Long userId) {
        // 查找重复词汇
        List<Word> duplicates = wordMapper.selectList(
            new QueryWrapper<Word>()
                .eq("user_id", userId)
                .groupBy("word, context")
                .having("count(*) > 1")
        );
        
        int cleaned = 0;
        for (Word duplicate : duplicates) {
            // 保留最新的记录，删除旧的
            QueryWrapper<Word> deleteWrapper = new QueryWrapper<Word>()
                    .eq("user_id", userId)
                    .eq("word", duplicate.getWord())
                    .eq("context", duplicate.getContext())
                    .orderByAsc("added_at")
                    .last("LIMIT 1");
            
            cleaned += wordMapper.delete(deleteWrapper);
        }
        
        log.info("清理重复词汇完成，删除 {} 条记录", cleaned);
        return cleaned;
    }

    /**
     * AI生成单词释义
     */
    private String generateMeaningByAI(String word, String context) {
        // 这里可以调用AI服务生成释义
        // 暂时返回模拟数据
        if (context != null && !context.trim().isEmpty()) {
            return String.format("【%s场景】%s 的释义...", context, word);
        }
        return String.format("%s 的基本释义...", word);
    }

    /**
     * AI生成例句
     */
    private String generateExampleByAI(String word, String context) {
        // 这里可以调用AI服务生成例句
        // 暂时返回模拟数据
        if (context != null && !context.trim().isEmpty()) {
            return String.format("This is an example sentence using '%s' in %s context.", word, context);
        }
        return String.format("This is an example sentence using '%s'.", word);
    }
}
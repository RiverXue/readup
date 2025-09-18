package com.xreadup.ai.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xreadup.ai.model.dto.WordInfo;
import com.xreadup.ai.userservice.common.ApiResponse;
import com.xreadup.ai.userservice.client.AiServiceClient;
import com.xreadup.ai.userservice.entity.Word;
import com.xreadup.ai.userservice.mapper.WordMapper;
import com.xreadup.ai.userservice.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    private final AiServiceClient aiServiceClient;

    /**
     * 二级词库查询策略
     * 1. 优先查询本地词库（带上下文）
     * 2. AI生成兜底（异步缓存到本地）
     */
    @Override
    @Transactional
    public Word lookupWord(String word, String context, Long userId, Long articleId) {
        log.info("开始查询单词: {}, 上下文: {}, 用户: {}", word, context, userId);
        
        // 1. 优先查本地词库（不匹配上下文，只按单词和用户ID查询）
        QueryWrapper<Word> wrapper = new QueryWrapper<Word>()
                .eq("user_id", userId)
                .eq("word", word.toLowerCase())
                .last("LIMIT 1");
        
        Word localWord = wordMapper.selectOne(wrapper);
        if (localWord != null) {
            log.info("从本地词库找到单词: {} (上下文: {})", word, localWord.getContext());
            return localWord;
        }

        // 2. AI生成兜底
        log.info("本地词库未找到，开始AI生成释义: {}", word);
        
        // 只调用一次AI服务，获取完整的单词信息
        WordInfo wordInfo = null;
        try {
            log.info("调用AI服务查询单词: {}, 上下文: {}", word, context);
            ApiResponse<WordInfo> apiResponse = aiServiceClient.lookupWord(word);
            
            // 验证响应
            if (apiResponse == null || !apiResponse.isSuccess() || apiResponse.getData() == null) {
                log.warn("AI服务返回无效响应: {}", apiResponse);
                throw new RuntimeException("AI服务返回无效响应");
            }
            
            wordInfo = apiResponse.getData();
            
            log.info("AI服务返回: wordInfo对象是否为空: {}", wordInfo == null);
            if (wordInfo != null) {
                log.info("AI服务返回的meanings列表: {}", wordInfo.getMeanings());
                // 格式化examples列表以便日志显示
                if (wordInfo.getExamples() != null) {
                    List<String> formattedExamples = wordInfo.getExamples().stream()
                            .map(example -> example.getEnglish() + " 【" + example.getChinese() + "】")
                            .collect(java.util.stream.Collectors.toList());
                    log.info("AI服务返回的examples列表: {}", formattedExamples);
                } else {
                    log.info("AI服务返回的examples列表: null");
                }
            }
        } catch (Exception e) {
            log.error("调用AI服务异常: {}, 详细信息: {}", e.getMessage(), e);
        }
        
        // 提取释义和例句
        String aiMeaning = extractMeaningFromWordInfo(wordInfo, word, context);
        String aiExample = extractExampleFromWordInfo(wordInfo, word, context);
        
        // 提取音标、难度等级和语境
        String phonetic = wordInfo != null ? wordInfo.getPhonetic() : null;
        String difficulty = wordInfo != null ? wordInfo.getDifficulty() : null;
        // 优先使用AI返回的语境类型，如果没有则使用传入的上下文
        String aiContext = wordInfo != null && wordInfo.getContext() != null ? wordInfo.getContext() : context;
        
        // 异步缓存到本地词库
        cacheWordAsync(word, aiMeaning, aiExample, aiContext, userId, articleId, "ai", phonetic, difficulty);
        
        Word newWord = new Word();
        newWord.setWord(word.toLowerCase());
        newWord.setMeaning(aiMeaning);
        newWord.setExample(aiExample);
        newWord.setContext(aiContext);
        newWord.setSource("ai");
        newWord.setSourceArticleId(articleId);
        newWord.setUserId(userId);
        newWord.setReviewStatus("new");
        newWord.setAddedAt(LocalDateTime.now());
        newWord.setPhonetic(phonetic);
        newWord.setDifficulty(difficulty);
        
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
        return cacheWordAsync(word, meaning, example, context, userId, articleId, source, null, null);
    }
    
    /**
     * 异步缓存单词到本地词库（包含音标和难度等级）
     */
    public CompletableFuture<Void> cacheWordAsync(String word, String meaning, String example, 
                                                String context, Long userId, Long articleId, String source, 
                                                String phonetic, String difficulty) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 限制上下文长度，防止数据库字段超长
                String limitedContext = limitContextLength(context);
                
                // 检查是否已存在
                QueryWrapper<Word> checkWrapper = new QueryWrapper<Word>()
                        .eq("user_id", userId)
                        .eq("word", word.toLowerCase())
                        .eq("context", limitedContext);
                
                if (wordMapper.selectCount(checkWrapper) > 0) {
                    log.debug("单词已存在，跳过缓存: {} (上下文: {})", word, limitedContext);
                    return;
                }

                Word wordEntity = new Word();
                wordEntity.setWord(word.toLowerCase());
                wordEntity.setMeaning(meaning);
                wordEntity.setExample(example);
                wordEntity.setContext(limitedContext);
                wordEntity.setSource(source);
                wordEntity.setSourceArticleId(articleId);
                wordEntity.setUserId(userId);
                wordEntity.setReviewStatus("new");
                wordEntity.setAddedAt(LocalDateTime.now());
                wordEntity.setPhonetic(phonetic);
                wordEntity.setDifficulty(difficulty);
                
                wordMapper.insert(wordEntity);
                log.info("成功缓存单词到本地词库: {} (上下文: {})", word, limitedContext);
                
            } catch (Exception e) {
                log.error("缓存单词失败: {}", word, e);
            }
        });
    }
    
    /**
     * 限制上下文长度，防止数据库字段超长
     */
    private String limitContextLength(String context) {
        if (context == null || context.trim().isEmpty()) {
            return "general";
        }
        
        // 根据实体类注释，上下文应该是简短的类别（如：金融/科技/地理）
        // 如果输入是长文本，提取前50个字符
        final int MAX_CONTEXT_LENGTH = 50;
        if (context.length() > MAX_CONTEXT_LENGTH) {
            return context.substring(0, MAX_CONTEXT_LENGTH);
        }
        return context;
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
     * 从WordInfo中提取单词释义
     */
    private String extractMeaningFromWordInfo(WordInfo wordInfo, String word, String context) {
        try {
            if (wordInfo != null && wordInfo.getMeanings() != null) {
                log.info("Meanings列表大小: {}", wordInfo.getMeanings().size());
                
                // 过滤空字符串并保留有效释义
                List<String> validMeanings = new ArrayList<>();
                
                // 处理可能的两种情况：1.List<String> 2.包含对象的字符串
                for (String meaningStr : wordInfo.getMeanings()) {
                    if (meaningStr != null && !meaningStr.trim().isEmpty() && !meaningStr.contains("暂无释义信息")) {
                        // 检查是否包含完整的词性和释义
                        if (meaningStr.contains(": ")) {
                            String[] parts = meaningStr.split(": ", 2);
                            if (parts.length == 2 && !parts[1].trim().isEmpty()) {
                                validMeanings.add(meaningStr);
                            } else {
                                // 只有词性，没有释义的情况
                                validMeanings.add(meaningStr + " 未提供详细释义");
                            }
                        } else {
                            // 其他格式的释义
                            validMeanings.add(meaningStr);
                        }
                    }
                }
                
                log.info("有效释义数量: {}", validMeanings.size());
                if (!validMeanings.isEmpty()) {
                    return String.join(", ", validMeanings);
                }
            }
            log.warn("AI服务返回为空或没有有效释义");
            return "暂无释义信息";
        } catch (Exception e) {
            log.error("提取释义失败: {}, 详细异常: {}", e.getMessage(), e);
            // 降级处理：返回简单的模拟数据
            if (context != null && !context.trim().isEmpty()) {
                return String.format("【%s场景】%s 的释义", context, word);
            }
            return String.format("%s 的基本释义", word);
        }
    }

    /**
     * 从WordInfo中提取例句
     */
    private String extractExampleFromWordInfo(WordInfo wordInfo, String word, String context) {
        try {
            if (wordInfo != null && wordInfo.getExamples() != null) {
                log.info("Examples列表大小: {}", wordInfo.getExamples().size());
                
                // 过滤空字符串并保留有效例句
                List<String> validExamples = wordInfo.getExamples().stream()
                        .filter(example -> example != null && example.getEnglish() != null && !example.getEnglish().trim().isEmpty())
                        .map(example -> example.getEnglish() + " 【" + example.getChinese() + "】")
                        .collect(java.util.stream.Collectors.toList());
                
                log.info("有效例句数量: {}", validExamples.size());
                if (!validExamples.isEmpty()) {
                    return validExamples.get(0);
                }
            }
            log.warn("AI服务返回为空或没有有效例句");
            return "暂无例句信息";
        } catch (Exception e) {
            log.error("提取例句失败: {}, 详细异常: {}", e.getMessage(), e);
            if (context != null && !context.trim().isEmpty()) {
                return String.format("This is an example sentence using '%s' in %s context.", word, context);
            }
            return String.format("This is an example sentence using '%s'.", word);
        }
    }
}
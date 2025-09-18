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
import java.util.stream.Collectors;

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
     * 1. 优先查询当前用户的词库
     * 2. 查询其他用户共享的词库
     * 3. AI生成兜底（异步缓存到本地）
     */
    @Override
    @Transactional
    public Word lookupWord(String word, String context, Long userId, Long articleId) {
        log.info("开始查询单词: {}, 上下文: {}, 用户: {}", word, context, userId);
        
        // 1. 优先查当前用户的词库
        Word userWord = wordMapper.findByWordAndUserId(word.toLowerCase(), userId);
        if (userWord != null) {
            log.info("从当前用户词库找到单词: {} (上下文: {})", word, userWord.getContext());
            return userWord;
        }

        // 2. 查询其他用户共享的词库
        Word sharedWord = wordMapper.findByWord(word.toLowerCase());
        if (sharedWord != null) {
            log.info("从其他用户词库找到单词: {} (上下文: {})", word, sharedWord.getContext());
            
            // 将当前用户ID添加到该单词的用户列表中
            sharedWord.addUserId(userId);
            wordMapper.updateUserIds(sharedWord.getId(), sharedWord.getUserIds());
            
            // 设置当前用户的复习状态为新单词
            sharedWord.setReviewStatus("new");
            sharedWord.setLastReviewedAt(null);
            sharedWord.setNextReviewAt(null);
            sharedWord.setAddedAt(LocalDateTime.now());
            
            return sharedWord;
        }

        // 3. AI生成兜底
        log.info("所有词库未找到，开始AI生成释义: {}", word);
        
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
        newWord.addUserId(userId);
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
                fallbackWord.addUserId(userId); // 使用addUserId方法添加用户ID
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
                                                String context, Long addUserId, Long articleId, String source, 
                                                String phonetic, String difficulty) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 限制上下文长度，防止数据库字段超长
                String limitedContext = limitContextLength(context);
                
                // 检查该单词是否已存在于系统中
                Word existingWord = wordMapper.findByWord(word.toLowerCase());
                
                if (existingWord != null) {
                    // 如果单词已存在，检查当前用户是否已添加该单词
                    if (!existingWord.containsUserId(addUserId)) {
                        // 如果当前用户未添加，将用户ID添加到现有单词
                        existingWord.addUserId(addUserId);
                        wordMapper.updateUserIds(existingWord.getId(), existingWord.getUserIds());
                        log.info("单词已存在，已将当前用户添加到单词共享列表: {} (上下文: {})", word, limitedContext);
                    }
                    return;
                }

                // 如果单词不存在，创建新单词并添加当前用户ID
                Word wordEntity = new Word();
                wordEntity.setWord(word.toLowerCase());
                wordEntity.setMeaning(meaning);
                wordEntity.setExample(example);
                wordEntity.setContext(limitedContext);
                wordEntity.setSource(source);
                wordEntity.setSourceArticleId(articleId);
                wordEntity.addUserId(addUserId); // 使用addUserId方法添加用户ID
                wordEntity.setReviewStatus("new");
                wordEntity.setAddedAt(LocalDateTime.now());
                wordEntity.setPhonetic(phonetic);
                wordEntity.setDifficulty(difficulty);
                
                wordMapper.insert(wordEntity);
                log.info("成功创建并缓存单词到词库: {} (上下文: {})", word, limitedContext);
                
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
        // 使用自定义方法查询用户的单词列表，然后统计数量
        List<Word> userWords = wordMapper.findByUserId(userId);
        int totalWords = userWords.size();
        
        // 按状态统计
        // 过滤用户单词列表来统计不同状态的单词数量
        int newWords = (int) userWords.stream().filter(w -> "new".equals(w.getReviewStatus())).count();
        int learningWords = (int) userWords.stream().filter(w -> "learning".equals(w.getReviewStatus())).count();
        int masteredWords = (int) userWords.stream().filter(w -> "mastered".equals(w.getReviewStatus())).count();
        
        // 按来源统计
        int localWords = (int) userWords.stream().filter(w -> "local".equals(w.getSource())).count();
        int aiWords = (int) userWords.stream().filter(w -> "ai".equals(w.getSource())).count();
        
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
     * 在多用户共享模式下，清理当前用户视角下的重复词汇
     */
    @Override
    @Transactional
    public int cleanupDuplicateWords(Long userId) {
        // 获取用户的所有单词
        List<Word> userWords = wordMapper.findByUserId(userId);
        
        // 用于记录已经处理过的单词和上下文组合
        Map<String, Word> processedWords = new HashMap<>();
        int cleaned = 0;
        
        // 按照添加时间排序，保留最新的记录
        userWords.sort(Comparator.comparing(Word::getAddedAt).reversed());
        
        for (Word word : userWords) {
            String key = word.getWord() + ":" + word.getContext();
            
            if (processedWords.containsKey(key)) {
                // 找到重复的单词，从用户列表中移除
                if (word.removeUserId(userId)) {
                    if (word.getUserIdSet().isEmpty()) {
                        // 如果没有其他用户使用，删除整个单词
                        wordMapper.deleteById(word.getId());
                    } else {
                        // 否则更新用户列表
                        wordMapper.updateUserIds(word.getId(), word.getUserIds());
                    }
                    cleaned++;
                }
            } else {
                // 第一次遇到该单词和上下文组合，记录下来
                processedWords.put(key, word);
            }
        }
        
        log.info("清理重复词汇完成，清理 {} 条记录", cleaned);
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

    /**
     * 复习单词
     * 更新单词的复习状态
     */
    @Override
    @Transactional
    public boolean reviewWord(Long wordId, Long userId, String reviewStatus) {
        log.info("开始复习单词: {}, 用户: {}, 新状态: {}", wordId, userId, reviewStatus);
        
        // 检查单词是否存在且用户是否在共享列表中
        Word word = wordMapper.selectById(wordId);
        if (word == null || !word.containsUserId(userId)) {
            log.warn("单词不存在或用户没有访问权限: {}, 用户: {}", wordId, userId);
            return false;
        }
        
        // 更新复习状态和时间
        Word updateWord = new Word();
        updateWord.setId(wordId);
        updateWord.setReviewStatus(reviewStatus);
        updateWord.setLastReviewedAt(LocalDateTime.now());
        
        // 根据复习状态设置下次复习时间
        if ("mastered".equals(reviewStatus)) {
            // 掌握的单词，下次复习时间设置为一周后
            updateWord.setNextReviewAt(LocalDateTime.now().plusDays(7));
        } else if ("learning".equals(reviewStatus)) {
            // 学习中的单词，下次复习时间设置为明天
            updateWord.setNextReviewAt(LocalDateTime.now().plusDays(1));
        } else if ("new".equals(reviewStatus)) {
            // 新单词，下次复习时间设置为今天
            updateWord.setNextReviewAt(LocalDateTime.now());
        }
        
        int result = wordMapper.updateById(updateWord);
        boolean success = result > 0;
        
        if (success) {
            log.info("复习单词成功: {}, 用户: {}, 新状态: {}", wordId, userId, reviewStatus);
        } else {
            log.error("复习单词失败: {}, 用户: {}, 新状态: {}", wordId, userId, reviewStatus);
        }
        
        return success;
    }
    
    /**
     * 删除单词
     * 从用户的词库列表中移除单词，但保留单词本身供其他用户使用
     */
    @Override
    @Transactional
    public boolean deleteWord(Long wordId, Long userId) {
        log.info("开始删除单词: {}, 用户: {}", wordId, userId);
        
        // 检查单词是否存在
        Word word = wordMapper.selectById(wordId);
        if (word == null) {
            log.warn("单词不存在: {}", wordId);
            return false;
        }
        
        // 检查当前用户是否使用该单词
        if (!word.containsUserId(userId)) {
            log.warn("单词不属于当前用户: {}, 用户: {}", wordId, userId);
            return false;
        }
        
        // 从用户列表中移除当前用户
        boolean removed = word.removeUserId(userId);
        
        if (removed) {
            // 如果还有其他用户使用该单词，只更新用户列表
            if (!word.getUserIdSet().isEmpty()) {
                int updateResult = wordMapper.updateUserIds(word.getId(), word.getUserIds());
                if (updateResult > 0) {
                    log.info("成功从单词用户列表中移除用户: {}, 单词: {}", userId, wordId);
                    return true;
                } else {
                    log.error("更新单词用户列表失败: {}, 用户: {}", wordId, userId);
                    return false;
                }
            } else {
                // 如果没有其他用户使用该单词，删除整个单词记录
                int deleteResult = wordMapper.deleteById(wordId);
                if (deleteResult > 0) {
                    log.info("成功删除单词(无其他用户使用): {}, 用户: {}", wordId, userId);
                    return true;
                } else {
                    log.error("删除单词失败: {}, 用户: {}", wordId, userId);
                    return false;
                }
            }
        }
        
        log.error("从单词用户列表中移除用户失败: {}, 用户: {}", wordId, userId);
        return false;
    }
}
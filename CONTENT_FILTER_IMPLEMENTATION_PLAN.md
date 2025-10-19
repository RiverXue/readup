# 专业级违禁词过滤系统实施方案

## 📋 项目背景

**项目名称**: ReadUp 智能英语学习平台  
**技术栈**: Spring Boot 3.4.1 + Vue 3 + MySQL 8.0 + Redis  
**架构模式**: 微服务架构（6个服务）  
**实施目标**: 构建专业级违禁词过滤系统，学习企业级技术，适合本科毕设到企业入门级

## 🎯 学习目标

- **技术深度**: 学习Trie树、AC自动机、缓存策略等专业算法
- **工程实践**: 掌握配置管理、监控日志、性能优化等企业技能
- **架构设计**: 理解分层架构、微服务集成、数据一致性等设计模式
- **实用价值**: 既能完成毕设，又能在企业实际应用

## 🏗️ 专业级系统架构

### 整体架构图
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端检测层     │    │   后端过滤层     │    │   词库管理层     │
│  (实时拦截)     │───▶│  (Trie树匹配)   │───▶│  (配置管理)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   规则引擎层     │    │   缓存策略层     │    │   监控日志层     │
│  (策略管理)     │    │  (Redis缓存)    │    │  (性能监控)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 微服务集成架构
```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway (8080)                      │
└─────────────────────┬───────────────────────────────────────────┘
                      │
    ┌─────────────────┼─────────────────┐
    │                 │                 │
┌───▼───┐        ┌────▼────┐        ┌───▼───┐
│User   │        │Article  │        │AI     │
│Service│        │Service  │        │Service│
│(8081) │        │(8082)   │        │(8084) │
└───┬───┘        └────┬────┘        └───┬───┘
    │                 │                 │
    └─────────────────┼─────────────────┘
                      │
            ┌─────────▼─────────┐
            │  Content Filter   │
            │   (集成到各服务)   │
            └─────────┬─────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
    ┌───▼───┐    ┌────▼────┐    ┌───▼───┐
    │MySQL  │    │ Redis   │    │Nacos  │
    │词库   │    │缓存     │    │配置   │
    └───────┘    └─────────┘    └───────┘
```

## 📊 内容风险分析

| 内容类型 | 风险等级 | 处理策略 | 集成位置 |
|---------|----------|----------|----------|
| **文章内容** | 🔴 高 | 拦截/替换 | ScraperServiceImpl |
| **AI对话** | 🟡 中 | 拦截/替换 | AiReadingAssistantService |
| **用户生词** | 🟡 中 | 拦截/替换 | VocabularyController |
| **翻译内容** | 🟡 中 | 拦截/替换 | 翻译服务 |
| **学习记录** | 🟢 低 | 标记记录 | ReportService |

## 🛠️ 专业级技术实现方案

### 1. 词库管理系统（学习重点）

#### 1.1 简化的数据库设计

```sql
-- 违禁词主表（简化版，但包含企业级特性）
CREATE TABLE sensitive_words (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(255) NOT NULL COMMENT '违禁词',
    category VARCHAR(50) NOT NULL COMMENT '分类：政治、暴力、色情等',
    risk_level TINYINT NOT NULL COMMENT '风险等级 1-3',
    action_type ENUM('BLOCK', 'REPLACE') NOT NULL COMMENT '处理方式',
    replacement VARCHAR(255) COMMENT '替换词',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_word (word),
    INDEX idx_category (category),
    INDEX idx_risk_level (risk_level),
    INDEX idx_enabled (enabled)
);

-- 过滤日志表（学习企业级日志设计）
CREATE TABLE filter_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content_type VARCHAR(50) NOT NULL COMMENT '内容类型',
    user_id BIGINT COMMENT '用户ID',
    original_content TEXT COMMENT '原始内容',
    filtered_content TEXT COMMENT '过滤后内容',
    filter_result ENUM('PASS', 'BLOCK', 'REPLACE') NOT NULL COMMENT '过滤结果',
    matched_words JSON COMMENT '匹配到的违禁词',
    processing_time_ms INT COMMENT '处理耗时(毫秒)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_content_type (content_type),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);
```

#### 1.2 专业级词库管理服务

```java
@Service
@Slf4j
public class ProfessionalWordLibraryService {
    
    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private TrieMatcher trieMatcher;
    
    private static final String WORD_CACHE_KEY = "sensitive_words:";
    private static final String TRIE_CACHE_KEY = "trie_matcher:";
    
    /**
     * 动态加载词库到内存
     */
    @PostConstruct
    public void loadWordLibrary() {
        try {
            // 1. 从数据库加载所有启用的违禁词
            List<SensitiveWord> words = sensitiveWordMapper.findEnabledWords();
            log.info("加载违禁词库，共 {} 个词汇", words.size());
            
            // 2. 构建Trie树
            TrieNode root = buildTrieTree(words);
            
            // 3. 缓存到Redis
            cacheWordLibrary(root, words);
            
            // 4. 更新本地Trie匹配器
            trieMatcher.updateTrie(root);
            
            log.info("词库加载完成，Trie树节点数: {}", countTrieNodes(root));
            
        } catch (Exception e) {
            log.error("词库加载失败", e);
            throw new RuntimeException("词库加载失败", e);
        }
    }
    
    /**
     * 构建高性能Trie树
     */
    private TrieNode buildTrieTree(List<SensitiveWord> words) {
        TrieNode root = new TrieNode();
        
        for (SensitiveWord word : words) {
            insertWord(root, word);
        }
        
        // 构建失败指针（AC自动机）
        buildFailureLinks(root);
        
        return root;
    }
    
    /**
     * 插入词汇到Trie树
     */
    private void insertWord(TrieNode root, SensitiveWord word) {
        TrieNode current = root;
        
        for (char c : word.getWord().toCharArray()) {
            current = current.getChildren().computeIfAbsent(c, k -> new TrieNode());
        }
        
        current.setEndOfWord(true);
        current.setSensitiveWord(word);
        current.setRiskLevel(word.getRiskLevel());
        current.setActionType(word.getActionType());
        current.setReplacement(word.getReplacement());
    }
    
    /**
     * 构建AC自动机失败指针
     */
    private void buildFailureLinks(TrieNode root) {
        Queue<TrieNode> queue = new LinkedList<>();
        
        // 第一层节点的失败指针都指向根节点
        for (TrieNode child : root.getChildren().values()) {
            child.setFailure(root);
            queue.offer(child);
        }
        
        // BFS构建失败指针
        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();
            
            for (Map.Entry<Character, TrieNode> entry : current.getChildren().entrySet()) {
                char c = entry.getKey();
                TrieNode child = entry.getValue();
                
                // 找到当前节点的失败指针
                TrieNode failure = current.getFailure();
                
                // 如果失败指针有对应字符的子节点，则设置为失败指针
                while (failure != root && !failure.getChildren().containsKey(c)) {
                    failure = failure.getFailure();
                }
                
                if (failure.getChildren().containsKey(c)) {
                    child.setFailure(failure.getChildren().get(c));
                } else {
                    child.setFailure(root);
                }
                
                queue.offer(child);
            }
        }
    }
    
    /**
     * 高性能内容过滤
     */
    public FilterResult filterContent(String content, ContentType contentType) {
        if (content == null || content.trim().isEmpty()) {
            return FilterResult.pass();
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 使用Trie树进行快速匹配
            List<MatchResult> matches = trieMatcher.findMatches(content);
            
            // 2. 应用过滤规则
            FilterResult result = applyFilterRules(content, matches, contentType);
            
            // 3. 记录处理时间
            long processingTime = System.currentTimeMillis() - startTime;
            result.setProcessingTime(processingTime);
            
            // 4. 记录过滤日志
            logFilterResult(content, result, contentType);
            
            return result;
            
        } catch (Exception e) {
            log.error("内容过滤失败", e);
            return FilterResult.error("过滤处理失败");
        }
    }
    
    /**
     * 应用过滤规则
     */
    private FilterResult applyFilterRules(String content, List<MatchResult> matches, ContentType contentType) {
        if (matches.isEmpty()) {
            return FilterResult.pass();
        }
        
        // 按风险等级排序
        matches.sort((a, b) -> Integer.compare(b.getRiskLevel(), a.getRiskLevel()));
        
        // 检查是否有高风险词汇需要直接拦截
        for (MatchResult match : matches) {
            if (match.getRiskLevel() >= 4) { // 高风险
                return FilterResult.block("内容包含高风险违禁词: " + match.getWord());
            }
        }
        
        // 应用替换规则
        String filteredContent = content;
        List<String> replacedWords = new ArrayList<>();
        
        for (MatchResult match : matches) {
            if (match.getActionType() == ActionType.REPLACE) {
                String replacement = match.getReplacement() != null ? 
                    match.getReplacement() : generateReplacement(match.getWord());
                filteredContent = filteredContent.replace(match.getWord(), replacement);
                replacedWords.add(match.getWord());
            }
        }
        
        if (!replacedWords.isEmpty()) {
            return FilterResult.replace(filteredContent, replacedWords);
        }
        
        return FilterResult.pass();
    }
    
    /**
     * 智能生成替换词
     */
    private String generateReplacement(String word) {
        // 根据词汇长度和类型生成不同的替换词
        if (word.length() <= 2) {
            return "**";
        } else if (word.length() <= 4) {
            return "***";
        } else {
            return "****";
        }
    }
    
    /**
     * 动态更新词库
     */
    @Transactional
    public void updateWordLibrary(SensitiveWord word) {
        try {
            // 1. 更新数据库
            if (word.getId() == null) {
                sensitiveWordMapper.insert(word);
            } else {
                sensitiveWordMapper.updateById(word);
            }
            
            // 2. 重新加载词库
            loadWordLibrary();
            
            // 3. 发送词库更新通知
            publishWordLibraryUpdateEvent(word);
            
            log.info("词库更新成功: {}", word.getWord());
            
        } catch (Exception e) {
            log.error("词库更新失败", e);
            throw new RuntimeException("词库更新失败", e);
        }
    }
    
    /**
     * 批量导入词库
     */
    @Transactional
    public void importWordLibrary(List<SensitiveWord> words) {
        try {
            // 1. 批量插入数据库
            sensitiveWordMapper.batchInsert(words);
            
            // 2. 重新加载词库
            loadWordLibrary();
            
            log.info("批量导入词库成功，共 {} 个词汇", words.size());
            
        } catch (Exception e) {
            log.error("批量导入词库失败", e);
            throw new RuntimeException("批量导入词库失败", e);
        }
    }
    
    /**
     * 词库统计分析
     */
    public WordLibraryStats getWordLibraryStats() {
        WordLibraryStats stats = new WordLibraryStats();
        
        // 总词数
        stats.setTotalWords(sensitiveWordMapper.countEnabledWords());
        
        // 按分类统计
        stats.setCategoryStats(sensitiveWordMapper.countByCategory());
        
        // 按风险等级统计
        stats.setRiskLevelStats(sensitiveWordMapper.countByRiskLevel());
        
        // 按处理方式统计
        stats.setActionTypeStats(sensitiveWordMapper.countByActionType());
        
        return stats;
    }
}
```

#### 1.3 Trie树匹配器

```java
@Component
@Slf4j
public class TrieMatcher {
    
    private volatile TrieNode root;
    private final Object lock = new Object();
    
    /**
     * 更新Trie树
     */
    public void updateTrie(TrieNode newRoot) {
        synchronized (lock) {
            this.root = newRoot;
        }
    }
    
    /**
     * 查找匹配的违禁词
     */
    public List<MatchResult> findMatches(String content) {
        if (root == null || content == null || content.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<MatchResult> matches = new ArrayList<>();
        TrieNode current = root;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            // 沿着Trie树查找
            while (current != root && !current.getChildren().containsKey(c)) {
                current = current.getFailure();
            }
            
            if (current.getChildren().containsKey(c)) {
                current = current.getChildren().get(c);
                
                // 检查是否匹配到违禁词
                if (current.isEndOfWord()) {
                    SensitiveWord word = current.getSensitiveWord();
                    MatchResult match = new MatchResult();
                    match.setWord(word.getWord());
                    match.setStartIndex(i - word.getWord().length() + 1);
                    match.setEndIndex(i + 1);
                    match.setRiskLevel(word.getRiskLevel());
                    match.setActionType(word.getActionType());
                    match.setReplacement(word.getReplacement());
                    matches.add(match);
                }
            }
        }
        
        return matches;
    }
}

/**
 * Trie树节点
 */
@Data
public class TrieNode {
    private Map<Character, TrieNode> children = new HashMap<>();
    private boolean endOfWord = false;
    private SensitiveWord sensitiveWord;
    private int riskLevel;
    private ActionType actionType;
    private String replacement;
    private TrieNode failure; // AC自动机失败指针
}

/**
 * 匹配结果
 */
@Data
public class MatchResult {
    private String word;
    private int startIndex;
    private int endIndex;
    private int riskLevel;
    private ActionType actionType;
    private String replacement;
}
```

### 2. 后端实现

**文件位置**: `xreadup/common/src/main/java/com/xreadup/common/filter/SimpleContentFilter.java`

```java
package com.xreadup.common.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SimpleContentFilter {
    
    // 违禁词库 - 可配置
    private static final Set<String> SENSITIVE_WORDS = ConcurrentHashMap.newKeySet();
    
    // 高风险词汇 - 直接拦截
    private static final Set<String> HIGH_RISK_WORDS = ConcurrentHashMap.newKeySet();
    
    // 替换词映射
    private static final Map<String, String> REPLACEMENT_MAP = new ConcurrentHashMap<>();
    
    // 统计信息
    private final Map<String, Long> filterStats = new ConcurrentHashMap<>();
    
    static {
        // 初始化违禁词库
        initializeSensitiveWords();
    }
    
    /**
     * 主过滤方法
     */
    public FilterResult filter(String content, ContentType contentType) {
        if (content == null || content.trim().isEmpty()) {
            return FilterResult.pass();
        }
        
        String filteredContent = content;
        List<String> matchedWords = new ArrayList<>();
        
        // 检查高风险词汇
        for (String highRiskWord : HIGH_RISK_WORDS) {
            if (content.contains(highRiskWord)) {
                log.warn("检测到高风险违禁词: {} | 内容类型: {}", highRiskWord, contentType);
                recordFilterStats("high_risk_blocked", contentType);
                return FilterResult.block("内容包含不当信息，请重新输入");
            }
        }
        
        // 检查一般违禁词
        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (content.contains(sensitiveWord)) {
                matchedWords.add(sensitiveWord);
                String replacement = REPLACEMENT_MAP.getOrDefault(sensitiveWord, "***");
                filteredContent = filteredContent.replace(sensitiveWord, replacement);
            }
        }
        
        if (!matchedWords.isEmpty()) {
            log.info("内容已过滤 | 匹配词汇: {} | 内容类型: {}", matchedWords, contentType);
            recordFilterStats("filtered", contentType);
            return FilterResult.replace(filteredContent, matchedWords);
        }
        
        return FilterResult.pass();
    }
    
    /**
     * 初始化违禁词库
     */
    private static void initializeSensitiveWords() {
        // 政治敏感词
        HIGH_RISK_WORDS.addAll(Arrays.asList(
            "法轮功", "六四", "天安门", "达赖", "台独", "港独", "疆独"
        ));
        
        // 一般违禁词
        SENSITIVE_WORDS.addAll(Arrays.asList(
            "恐怖主义", "爆炸", "枪击", "暴力", "色情", "成人", "性爱",
            "赌博", "毒品", "自杀", "邪教", "传销", "诈骗"
        ));
        
        // 替换词映射
        REPLACEMENT_MAP.put("恐怖主义", "***");
        REPLACEMENT_MAP.put("爆炸", "***");
        REPLACEMENT_MAP.put("枪击", "***");
        REPLACEMENT_MAP.put("暴力", "***");
        REPLACEMENT_MAP.put("色情", "***");
        REPLACEMENT_MAP.put("成人", "***");
        REPLACEMENT_MAP.put("赌博", "***");
        REPLACEMENT_MAP.put("毒品", "***");
    }
    
    /**
     * 记录过滤统计
     */
    private void recordFilterStats(String action, ContentType contentType) {
        String key = action + "_" + contentType.name().toLowerCase();
        filterStats.merge(key, 1L, Long::sum);
    }
    
    /**
     * 获取过滤统计
     */
    public Map<String, Long> getFilterStats() {
        return new HashMap<>(filterStats);
    }
    
    /**
     * 内容类型枚举
     */
    public enum ContentType {
        ARTICLE,    // 文章内容
        CHAT,       // AI对话
        VOCABULARY, // 用户生词
        TRANSLATION, // 翻译内容
        COMMENT     // 用户评论
    }
    
    /**
     * 过滤结果类
     */
    public static class FilterResult {
        private final boolean passed;
        private final String filteredContent;
        private final List<String> matchedWords;
        private final String message;
        
        private FilterResult(boolean passed, String filteredContent, List<String> matchedWords, String message) {
            this.passed = passed;
            this.filteredContent = filteredContent;
            this.matchedWords = matchedWords;
            this.message = message;
        }
        
        public static FilterResult pass() {
            return new FilterResult(true, null, Collections.emptyList(), null);
        }
        
        public static FilterResult replace(String content, List<String> words) {
            return new FilterResult(true, content, words, "内容已过滤");
        }
        
        public static FilterResult block(String message) {
            return new FilterResult(false, null, Collections.emptyList(), message);
        }
        
        // Getters
        public boolean isPassed() { return passed; }
        public String getFilteredContent() { return filteredContent; }
        public List<String> getMatchedWords() { return matchedWords; }
        public String getMessage() { return message; }
    }
}
```

#### 1.2 集成到文章服务

**修改文件**: `xreadup/article-service/src/main/java/com/xreadup/ai/articleservice/service/impl/ScraperServiceImpl.java`

```java
// 在类中添加依赖注入
@Autowired
private SimpleContentFilter contentFilter;

// 在 scrapeArticleContent 方法中添加过滤逻辑
@Override
public Optional<String> scrapeArticleContent(String url) {
    try {
        // ... 现有代码 ...
        
        if (article != null) {
            String textContent = article.getTextContent();
            if (textContent != null) {
                // 添加内容过滤
                SimpleContentFilter.FilterResult filterResult = contentFilter.filter(
                    textContent, 
                    SimpleContentFilter.ContentType.ARTICLE
                );
                
                if (!filterResult.isPassed()) {
                    log.warn("文章内容被过滤拦截: {} | 原因: {}", url, filterResult.getMessage());
                    return Optional.empty();
                }
                
                // 使用过滤后的内容
                if (filterResult.getFilteredContent() != null) {
                    textContent = filterResult.getFilteredContent();
                    log.info("文章内容已过滤 | 匹配词汇: {}", filterResult.getMatchedWords());
                }
                
                // ... 后续处理逻辑保持不变 ...
            }
        }
    } catch (Exception e) {
        // ... 错误处理 ...
    }
}
```

#### 1.3 集成到AI服务

**修改文件**: `xreadup/ai-service/src/main/java/com/xreadup/ai/service/AiReadingAssistantService.java`

```java
// 在类中添加依赖注入
@Autowired
private SimpleContentFilter contentFilter;

// 在 chatWithAssistant 方法中添加过滤逻辑
public AiChatResponse chatWithAssistant(AiChatRequest request) {
    try {
        // 过滤用户问题
        SimpleContentFilter.FilterResult questionFilter = contentFilter.filter(
            request.getQuestion(), 
            SimpleContentFilter.ContentType.CHAT
        );
        
        if (!questionFilter.isPassed()) {
            log.warn("用户问题被过滤拦截 | 用户: {} | 原因: {}", 
                request.getUserId(), questionFilter.getMessage());
            
            AiChatResponse response = new AiChatResponse();
            response.setAnswer("抱歉，您的问题包含不当内容，请重新提问。");
            response.setFollowUpQuestion("您可以问我关于英语学习的问题。");
            response.setDifficulty("B1");
            return response;
        }
        
        // 使用过滤后的问题
        if (questionFilter.getFilteredContent() != null) {
            request.setQuestion(questionFilter.getFilteredContent());
            log.info("用户问题已过滤 | 匹配词汇: {}", questionFilter.getMatchedWords());
        }
        
        // ... 现有AI处理逻辑 ...
        
        // 过滤AI回答
        SimpleContentFilter.FilterResult answerFilter = contentFilter.filter(
            response.getAnswer(), 
            SimpleContentFilter.ContentType.CHAT
        );
        
        if (!answerFilter.isPassed()) {
            log.warn("AI回答被过滤拦截 | 用户: {}", request.getUserId());
            response.setAnswer("抱歉，我无法回答这个问题。");
        } else if (answerFilter.getFilteredContent() != null) {
            response.setAnswer(answerFilter.getFilteredContent());
            log.info("AI回答已过滤 | 匹配词汇: {}", answerFilter.getMatchedWords());
        }
        
        return response;
    } catch (Exception e) {
        // ... 错误处理 ...
    }
}
```

#### 1.4 集成到用户服务

**修改文件**: `xreadup/user-service/src/main/java/com/xreadup/ai/userservice/controller/VocabularyController.java`

```java
// 在添加生词的方法中添加过滤
@PostMapping("/add")
public ApiResponse<Boolean> addWord(@RequestBody AddWordRequest request) {
    try {
        // 过滤英文单词
        SimpleContentFilter.FilterResult wordFilter = contentFilter.filter(
            request.getWord(), 
            SimpleContentFilter.ContentType.VOCABULARY
        );
        
        if (!wordFilter.isPassed()) {
            return ApiResponse.error("单词包含不当内容，请重新输入");
        }
        
        // 过滤中文翻译
        SimpleContentFilter.FilterResult translationFilter = contentFilter.filter(
            request.getTranslation(), 
            SimpleContentFilter.ContentType.VOCABULARY
        );
        
        if (!translationFilter.isPassed()) {
            return ApiResponse.error("翻译包含不当内容，请重新输入");
        }
        
        // 使用过滤后的内容
        if (wordFilter.getFilteredContent() != null) {
            request.setWord(wordFilter.getFilteredContent());
        }
        if (translationFilter.getFilteredContent() != null) {
            request.setTranslation(translationFilter.getFilteredContent());
        }
        
        // ... 现有业务逻辑 ...
    } catch (Exception e) {
        // ... 错误处理 ...
    }
}
```

### 2. 前端实现

#### 2.1 创建内容过滤器工具类

**文件位置**: `xreadup-frontend/src/utils/contentFilter.ts`

```typescript
export interface FilterResult {
  passed: boolean;
  filteredContent: string;
  matchedWords: string[];
  message?: string;
}

export class ContentFilter {
  private static instance: ContentFilter;
  private static sensitiveWords = [
    '法轮功', '六四', '天安门', '达赖', '台独', '港独', '疆独',
    '恐怖主义', '爆炸', '枪击', '暴力', '色情', '成人', '性爱',
    '赌博', '毒品', '自杀', '邪教', '传销', '诈骗'
  ];
  
  private static highRiskWords = [
    '法轮功', '六四', '天安门', '达赖', '台独', '港独', '疆独'
  ];
  
  private static replacementMap = new Map([
    ['恐怖主义', '***'],
    ['爆炸', '***'],
    ['枪击', '***'],
    ['暴力', '***'],
    ['色情', '***'],
    ['成人', '***'],
    ['赌博', '***'],
    ['毒品', '***']
  ]);
  
  static getInstance(): ContentFilter {
    if (!ContentFilter.instance) {
      ContentFilter.instance = new ContentFilter();
    }
    return ContentFilter.instance;
  }
  
  /**
   * 过滤内容
   */
  filter(content: string, contentType: 'article' | 'chat' | 'vocabulary' | 'translation' = 'article'): FilterResult {
    if (!content || content.trim() === '') {
      return { passed: true, filteredContent: content, matchedWords: [] };
    }
    
    let filteredContent = content;
    const matchedWords: string[] = [];
    
    // 检查高风险词汇
    for (const word of ContentFilter.highRiskWords) {
      if (content.includes(word)) {
        console.warn(`检测到高风险违禁词: ${word} | 内容类型: ${contentType}`);
        return { 
          passed: false, 
          filteredContent: '', 
          matchedWords: [word], 
          message: '内容包含不当信息，请重新输入' 
        };
      }
    }
    
    // 检查一般违禁词
    for (const word of ContentFilter.sensitiveWords) {
      if (content.includes(word)) {
        matchedWords.push(word);
        const replacement = ContentFilter.replacementMap.get(word) || '***';
        filteredContent = filteredContent.replace(new RegExp(word, 'g'), replacement);
      }
    }
    
    if (matchedWords.length > 0) {
      console.info(`内容已过滤 | 匹配词汇: ${matchedWords.join(', ')} | 内容类型: ${contentType}`);
      return { 
        passed: true, 
        filteredContent, 
        matchedWords, 
        message: `已过滤 ${matchedWords.length} 个敏感词` 
      };
    }
    
    return { passed: true, filteredContent: content, matchedWords: [] };
  }
  
  /**
   * 实时过滤输入
   */
  filterInput(input: string, contentType: 'article' | 'chat' | 'vocabulary' | 'translation' = 'article'): {
    value: string;
    warning: string;
    blocked: boolean;
  } {
    const result = this.filter(input, contentType);
    
    return {
      value: result.filteredContent,
      warning: result.message || '',
      blocked: !result.passed
    };
  }
}
```

#### 2.2 创建过滤输入组件

**文件位置**: `xreadup-frontend/src/components/common/FilteredInput.vue`

```vue
<template>
  <div class="filtered-input">
    <el-input
      v-model="inputValue"
      :type="type"
      :rows="rows"
      :placeholder="placeholder"
      :disabled="disabled"
      @input="handleInput"
      @blur="handleBlur"
      :class="{ 'has-warning': showWarning, 'is-blocked': isBlocked }"
    />
    
    <!-- 过滤提示 -->
    <div v-if="showWarning" class="filter-warning">
      <el-alert
        :title="warningMessage"
        :type="isBlocked ? 'error' : 'warning'"
        :closable="false"
        show-icon
        size="small"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { ContentFilter } from '@/utils/contentFilter';

const props = defineProps({
  modelValue: String,
  type: { type: String, default: 'text' },
  rows: Number,
  placeholder: String,
  disabled: Boolean,
  contentType: { type: String, default: 'article' }
});

const emit = defineEmits(['update:modelValue', 'filter-result']);

const inputValue = ref(props.modelValue || '');
const warningMessage = ref('');
const isBlocked = ref(false);

const showWarning = computed(() => warningMessage.value !== '');

const contentFilter = ContentFilter.getInstance();

const handleInput = (value: string) => {
  const result = contentFilter.filterInput(value, props.contentType as any);
  
  inputValue.value = result.value;
  warningMessage.value = result.warning;
  isBlocked.value = result.blocked;
  
  emit('update:modelValue', result.value);
  emit('filter-result', {
    passed: !result.blocked,
    filteredContent: result.value,
    warning: result.warning
  });
};

const handleBlur = () => {
  // 失焦时进行最终检查
  if (inputValue.value.trim()) {
    const result = contentFilter.filter(inputValue.value, props.contentType as any);
    if (!result.passed) {
      inputValue.value = '';
      emit('update:modelValue', '');
    }
  }
};

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  inputValue.value = newValue || '';
});
</script>

<style scoped>
.filtered-input {
  position: relative;
}

.filter-warning {
  margin-top: 8px;
}

.has-warning .el-input__inner {
  border-color: #e6a23c;
}

.is-blocked .el-input__inner {
  border-color: #f56c6c;
}
</style>
```

#### 2.3 集成到现有页面

**修改文件**: `xreadup-frontend/src/views/VocabularyPage.vue`

```vue
<template>
  <!-- 在生词添加表单中使用过滤组件 -->
  <el-form-item label="英文单词">
    <FilteredInput
      v-model="newWord.word"
      placeholder="输入英文单词"
      content-type="vocabulary"
      @filter-result="handleWordFilter"
    />
  </el-form-item>
  
  <el-form-item label="中文翻译">
    <FilteredInput
      v-model="newWord.translation"
      placeholder="输入中文翻译"
      content-type="vocabulary"
      @filter-result="handleTranslationFilter"
    />
  </el-form-item>
</template>

<script setup lang="ts">
import FilteredInput from '@/components/common/FilteredInput.vue';

// 处理过滤结果
const handleWordFilter = (result: any) => {
  if (!result.passed) {
    ElMessage.error('单词包含不当内容，请重新输入');
  } else if (result.warning) {
    ElMessage.warning(result.warning);
  }
};

const handleTranslationFilter = (result: any) => {
  if (!result.passed) {
    ElMessage.error('翻译包含不当内容，请重新输入');
  } else if (result.warning) {
    ElMessage.warning(result.warning);
  }
};
</script>
```

### 3. 配置管理

#### 3.1 应用配置

**文件位置**: `xreadup/common/src/main/resources/application-filter.yml`

```yaml
content-filter:
  enabled: true
  sensitive-words:
    - "法轮功"
    - "六四"
    - "天安门"
    - "达赖"
    - "台独"
    - "港独"
    - "疆独"
    - "恐怖主义"
    - "爆炸"
    - "枪击"
    - "暴力"
    - "色情"
    - "成人"
    - "性爱"
    - "赌博"
    - "毒品"
    - "自杀"
    - "邪教"
    - "传销"
    - "诈骗"
  high-risk-words:
    - "法轮功"
    - "六四"
    - "天安门"
    - "达赖"
    - "台独"
    - "港独"
    - "疆独"
  replacement: "***"
  log-level: INFO
```

#### 3.2 配置类

**文件位置**: `xreadup/common/src/main/java/com/xreadup/common/config/ContentFilterConfig.java`

```java
@ConfigurationProperties(prefix = "content-filter")
@Data
@Component
public class ContentFilterConfig {
    private boolean enabled = true;
    private List<String> sensitiveWords = new ArrayList<>();
    private List<String> highRiskWords = new ArrayList<>();
    private String replacement = "***";
    private String logLevel = "INFO";
}
```

## 📋 实施步骤

### 阶段一：基础建设 (第1-2天)

1. **创建公共模块**
   ```bash
   # 在 xreadup/common 中创建过滤相关类
   mkdir -p xreadup/common/src/main/java/com/xreadup/common/filter
   mkdir -p xreadup/common/src/main/java/com/xreadup/common/config
   ```

2. **实现核心过滤服务**
   - 创建 `SimpleContentFilter.java`
   - 创建 `ContentFilterConfig.java`
   - 添加配置文件

3. **测试基础功能**
   ```java
   @Test
   public void testContentFilter() {
       SimpleContentFilter filter = new SimpleContentFilter();
       
       // 测试高风险词汇
       FilterResult result1 = filter.filter("法轮功", ContentType.ARTICLE);
       assertFalse(result1.isPassed());
       
       // 测试一般违禁词
       FilterResult result2 = filter.filter("暴力", ContentType.ARTICLE);
       assertTrue(result2.isPassed());
       assertEquals("***", result2.getFilteredContent());
   }
   ```

### 阶段二：服务集成 (第3-4天)

1. **集成文章服务**
   - 修改 `ScraperServiceImpl.java`
   - 添加依赖注入
   - 测试文章过滤功能

2. **集成AI服务**
   - 修改 `AiReadingAssistantService.java`
   - 添加对话过滤逻辑
   - 测试AI对话过滤

3. **集成用户服务**
   - 修改 `VocabularyController.java`
   - 添加生词过滤逻辑
   - 测试用户内容过滤

### 阶段三：前端实现 (第5-6天)

1. **创建前端工具类**
   - 创建 `contentFilter.ts`
   - 实现前端过滤逻辑
   - 添加类型定义

2. **创建过滤组件**
   - 创建 `FilteredInput.vue`
   - 实现实时过滤功能
   - 添加用户提示

3. **集成到现有页面**
   - 修改 `VocabularyPage.vue`
   - 修改 `ArticleReader.vue`
   - 修改 `AIAssistantView.vue`

### 阶段四：测试优化 (第7天)

1. **功能测试**
   - 测试各种内容类型的过滤
   - 验证高风险词汇拦截
   - 验证一般违禁词替换

2. **性能测试**
   - 测试过滤性能
   - 优化过滤算法
   - 添加缓存机制

3. **用户体验测试**
   - 测试用户交互流程
   - 优化提示信息
   - 调整过滤策略

## 🚀 部署方案

### 1. 开发环境部署

```bash
# 1. 启动基础服务
cd xreadup
docker-compose up -d mysql redis nacos

# 2. 启动微服务
mvn clean install
java -jar user-service/target/user-service-1.0.0.jar
java -jar article-service/target/article-service-1.0.0.jar
java -jar ai-service/target/ai-service-1.0.0.jar

# 3. 启动前端
cd xreadup-frontend
npm install
npm run dev
```

### 2. 生产环境部署

```bash
# 1. 构建镜像
docker build -t xreadup-content-filter .

# 2. 更新服务配置
# 在 nacos 中添加 content-filter 配置

# 3. 滚动更新服务
kubectl rollout restart deployment/user-service
kubectl rollout restart deployment/article-service
kubectl rollout restart deployment/ai-service
```

## 📊 监控和维护

### 1. 日志监控

```java
// 在过滤方法中添加详细日志
log.info("内容过滤 | 类型: {} | 原始长度: {} | 过滤后长度: {} | 匹配词汇: {}", 
    contentType, content.length(), filteredContent.length(), matchedWords);
```

### 2. 统计面板

```java
// 创建简单的统计接口
@RestController
@RequestMapping("/api/admin/filter-stats")
public class FilterStatsController {
    
    @Autowired
    private SimpleContentFilter contentFilter;
    
    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> getFilterStats() {
        return ApiResponse.success(contentFilter.getFilterStats());
    }
}
```

### 3. 词库维护

```java
// 提供词库管理接口
@RestController
@RequestMapping("/api/admin/filter-words")
public class FilterWordsController {
    
    @PostMapping("/add")
    public ApiResponse<Void> addSensitiveWord(@RequestBody String word) {
        // 添加违禁词到词库
        return ApiResponse.success();
    }
    
    @DeleteMapping("/remove")
    public ApiResponse<Void> removeSensitiveWord(@RequestBody String word) {
        // 从词库中移除违禁词
        return ApiResponse.success();
    }
}
```

## 🔧 故障排查

### 常见问题

1. **过滤不生效**
   - 检查依赖注入是否正确
   - 检查配置文件是否加载
   - 检查日志输出

2. **性能问题**
   - 检查词库大小
   - 优化匹配算法
   - 添加缓存机制

3. **误拦截**
   - 调整词库内容
   - 优化匹配规则
   - 添加白名单机制

### 调试工具

```java
// 添加调试接口
@GetMapping("/debug/filter")
public ApiResponse<FilterResult> debugFilter(@RequestParam String content) {
    FilterResult result = contentFilter.filter(content, ContentType.ARTICLE);
    return ApiResponse.success(result);
}
```

## 📈 后续优化

### 短期优化 (1-2周)
- 添加更多违禁词
- 优化过滤性能
- 完善用户提示

### 中期优化 (1-2月)
- 添加机器学习过滤
- 实现动态词库更新
- 添加过滤统计面板

### 长期优化 (3-6月)
- 集成第三方过滤服务
- 实现智能替换算法
- 添加内容审核工作流

## ✅ 验收标准

1. **功能完整性**
   - [ ] 文章内容过滤正常
   - [ ] AI对话过滤正常
   - [ ] 用户生词过滤正常
   - [ ] 高风险词汇正确拦截
   - [ ] 一般违禁词正确替换

2. **性能指标**
   - [ ] 过滤响应时间 < 100ms
   - [ ] 内存占用 < 50MB
   - [ ] CPU占用 < 5%

3. **用户体验**
   - [ ] 过滤提示清晰明确
   - [ ] 不影响正常学习流程
   - [ ] 错误处理友好

4. **代码质量**
   - [ ] 代码注释完整
   - [ ] 单元测试覆盖 > 80%
   - [ ] 符合项目编码规范

## 📞 技术支持

- **开发团队**: ReadUp 开发组
- **技术栈**: Spring Boot + Vue 3
- **文档更新**: 2024年12月
- **版本**: v1.0.0

---

**注意**: 本方案基于项目实际情况制定，实施过程中可根据具体需求进行调整。建议分阶段实施，确保系统稳定性。

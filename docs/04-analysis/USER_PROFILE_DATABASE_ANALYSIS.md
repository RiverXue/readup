# 用户画像与AI助手数据库支持分析报告

## 📊 数据库结构分析（readup_ai库）

### 1. 用户相关表

#### user表（用户基础信息）
```sql
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20),
    `interest_tag` VARCHAR(50) COMMENT '兴趣标签：tech/business/culture',
    `identity_tag` VARCHAR(50) COMMENT '身份标签：考研/四六级/职场/留学',
    `learning_goal_words` INT DEFAULT 0 COMMENT '目标词汇量',
    `target_reading_time` INT DEFAULT 0 COMMENT '每日目标阅读时长（分钟）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
)
```

**支持的用户画像字段：**
- ✅ 兴趣标签（interest_tag）
- ✅ 身份标签（identity_tag）
- ✅ 学习目标词汇量（learning_goal_words）
- ✅ 每日目标阅读时长（target_reading_time）
- ✅ 注册时间（created_at）- 可计算学习天数

### 2. 学习数据相关表

#### word表（词汇学习数据）
```sql
CREATE TABLE `word` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `word` VARCHAR(100) NOT NULL,
    `meaning` VARCHAR(500),
    `example` TEXT,
    `context` VARCHAR(100) COMMENT '上下文（如：金融/科技/地理）',
    `source` VARCHAR(50) COMMENT '来源：local/ai',
    `source_article_id` BIGINT COMMENT '来源文章ID',
    `review_status` VARCHAR(20) DEFAULT 'new' COMMENT '复习状态：new/learning/mastered',
    `last_reviewed_at` DATETIME NULL,
    `next_review_at` DATETIME NULL,
    `added_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `phonetic` VARCHAR(50),
    `difficulty` VARCHAR(10) COMMENT '难度等级：A1/A2/B1/B2/C1/C2'
)
```

**支持的词汇统计：**
- ✅ 总词汇量（COUNT(*)）
- ✅ 新词数量（review_status = 'new'）
- ✅ 学习中词汇（review_status = 'learning'）
- ✅ 已掌握词汇（review_status = 'mastered'）
- ✅ 薄弱环节识别（需要复习的词汇）
- ✅ 词汇来源分析（source_article_id）

#### reading_log表（阅读记录）
```sql
CREATE TABLE `reading_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `article_id` BIGINT NOT NULL,
    `read_time_sec` INT COMMENT '阅读时长（秒）',
    `finished_at` DATETIME DEFAULT CURRENT_TIMESTAMP
)
```

**支持的阅读统计：**
- ✅ 总阅读文章数（COUNT(DISTINCT article_id)）
- ✅ 总阅读时长（SUM(read_time_sec)）
- ✅ 平均阅读时长（AVG(read_time_sec)）
- ✅ 阅读历史记录

#### reading_streak表（阅读打卡数据）
```sql
CREATE TABLE `reading_streak` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `streak_days` INT DEFAULT 0 COMMENT '连续阅读天数',
    `last_read_date` DATE COMMENT '最后阅读日期',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_id` (`user_id`)
)
```

**支持的打卡统计：**
- ✅ 连续打卡天数（streak_days）
- ✅ 最后阅读日期（last_read_date）
- ✅ 学习连贯性分析

### 3. 文章相关表

#### article表（文章信息）
```sql
CREATE TABLE `article` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content_en` LONGTEXT NOT NULL,
    `content_cn` LONGTEXT NOT NULL,
    `description` VARCHAR(500),
    `category` VARCHAR(50) COMMENT 'AI自动分类',
    `difficulty_level` VARCHAR(10) COMMENT 'AI自动难度等级：A1/A2/B1/B2/C1/C2',
    `word_count` INT DEFAULT 0,
    `read_count` INT DEFAULT 0,
    ...
)
```

**支持的文章分析：**
- ✅ 文章难度级别（difficulty_level）
- ✅ 文章分类（category）
- ✅ 文章描述（description）- 用于AI上下文
- ✅ 阅读次数统计（read_count）

#### ai_analysis表（AI分析结果）
```sql
CREATE TABLE `ai_analysis` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL,
    `difficulty_level` VARCHAR(10),
    `keywords` TEXT/JSON,
    `summary` TEXT,
    `chinese_translation` LONGTEXT,
    `key_phrases` TEXT/JSON,
    `readability_score` DECIMAL(5,2),
    `word_translations` LONGTEXT,
    `sentence_parse_results` LONGTEXT,
    `quiz_questions` LONGTEXT,
    `learning_tips` TEXT COMMENT '个性化学习建议',
    `analysis_metadata` TEXT,
    ...
)
```

**支持的AI分析功能：**
- ✅ 文章摘要（summary）
- ✅ 难度评估（difficulty_level）
- ✅ 关键词提取（keywords）
- ✅ 学习建议（learning_tips）
- ✅ 测验题目（quiz_questions）

### 4. 订阅相关表

#### subscription表（用户订阅）
```sql
CREATE TABLE subscription (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `plan_type` VARCHAR(20) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `ai_features_enabled` BOOLEAN DEFAULT FALSE,
    ...
)
```

**支持的订阅管理：**
- ✅ 用户套餐类型（plan_type）
- ✅ AI功能权限（ai_features_enabled）
- ✅ 订阅状态（status）

## 🎯 后端AI助手实现分析

### 1. AiToolService实现（ai-service）

#### intelligentChat方法
```java
public String intelligentChat(String question, String articleContext) {
    // 1. 解析文章上下文
    Map<String, Object> contextMap = parseArticleContext(articleContext);
    
    // 2. 提取用户学习画像
    String userProfile = extractUserProfile(contextMap);
    
    // 3. 生成个性化提示词
    String prompt = String.format("""
        👤 学生学习画像：
        %s
        
        🎯 个性化教学要求：
        1. 基于学生的学习历史提供个性化建议
        2. 结合学生的薄弱环节进行针对性指导
        3. 根据学生的学习水平调整回答深度
        ...
        """, userProfile);
    
    // 4. 调用AI模型
    return callAIModel(prompt);
}
```

#### extractUserProfile方法
```java
private String extractUserProfile(Map<String, Object> contextMap) {
    StringBuilder profile = new StringBuilder();
    
    // 提取用户学习数据
    Object userProfileObj = contextMap.get("userProfile");
    if (userProfileObj instanceof Map) {
        Map<String, Object> userProfile = (Map<String, Object>) userProfileObj;
        
        profile.append("- 学习天数：").append(userProfile.get("learningDays")).append("\n");
        profile.append("- 已读文章：").append(userProfile.get("totalArticlesRead")).append("\n");
        profile.append("- 词汇量：").append(userProfile.get("vocabularyCount")).append("\n");
        profile.append("- 平均阅读时长：").append(userProfile.get("averageReadTime")).append("分钟\n");
        profile.append("- 当前水平：").append(userProfile.get("currentLevel")).append("\n");
        profile.append("- 薄弱环节：").append(userProfile.get("weakAreas")).append("\n");
        ...
    }
    
    return profile.toString();
}
```

### 2. 前端传递的用户画像数据结构

```typescript
interface UserProfile {
  learningDays: number          // 学习天数
  totalArticlesRead: number     // 已读文章数
  vocabularyCount: number       // 词汇量
  averageReadTime: number       // 平均阅读时长
  totalReadTime: number         // 总阅读时长
  readingStreak: number         // 连续打卡天数
  preferredCategories: string[] // 偏好分类
  currentLevel: string          // 当前水平
  weakAreas: string[]           // 薄弱环节
  newWords: number              // 新词数量
  learningWords: number         // 学习中词汇
  masteredWords: number         // 已掌握词汇
  averageDifficulty: string     // 平均难度
}
```

## ✅ 数据库支持情况总结

### 完全支持的功能
1. ✅ **学习天数统计**：通过user.created_at计算或reading_streak.streak_days
2. ✅ **文章阅读统计**：通过reading_log表统计
3. ✅ **词汇学习统计**：通过word表统计（总量、新词、学习中、已掌握）
4. ✅ **阅读时长统计**：通过reading_log.read_time_sec统计
5. ✅ **连续打卡统计**：通过reading_streak表
6. ✅ **薄弱环节识别**：通过word.review_status分析
7. ✅ **学习水平评估**：基于多维度数据综合评估
8. ✅ **偏好分类分析**：通过reading_log关联article统计

### 需要增强的功能（可选）

#### 1. 用户学习画像表（建议新增）
```sql
CREATE TABLE `user_learning_profile` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL UNIQUE,
    `current_level` VARCHAR(20) COMMENT '当前水平：beginner/intermediate/advanced/expert',
    `preferred_categories` JSON COMMENT '偏好分类',
    `weak_areas` JSON COMMENT '薄弱环节',
    `learning_goals` JSON COMMENT '学习目标',
    `last_updated` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) COMMENT='用户学习画像表';
```

#### 2. 学习统计缓存表（优化性能）
```sql
CREATE TABLE `user_stats_cache` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL UNIQUE,
    `total_articles_read` INT DEFAULT 0,
    `total_vocabulary` INT DEFAULT 0,
    `total_reading_time` INT DEFAULT 0,
    `average_read_time` INT DEFAULT 0,
    `last_calculated` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) COMMENT='用户统计缓存表';
```

## 🚀 当前实现方案

### 数据获取流程
1. **前端AIAssistantView.vue**
   - 调用report-service获取阅读统计
   - 调用user-service获取词汇统计
   - 调用learningApi.dailyCheckIn获取打卡天数
   - 本地计算学习水平和薄弱环节

2. **后端ai-service**
   - 接收前端传递的userProfile数据
   - 解析并格式化为学习画像文本
   - 生成个性化AI提示词
   - 返回个性化学习建议

### API调用链路
```
前端 -> report-service -> MySQL (reading_log)
前端 -> user-service -> MySQL (word)
前端 -> user-service -> MySQL (reading_streak)
前端 -> ai-service -> AI模型（带用户画像）
```

## 📝 优化建议

### 1. 性能优化
- ✅ 添加统计缓存表，减少实时计算
- ✅ 使用Redis缓存热点数据
- ✅ 定期更新用户学习画像

### 2. 功能增强
- ✅ 添加学习目标管理
- ✅ 添加学习进度跟踪
- ✅ 添加个性化推荐算法

### 3. 数据完整性
- ✅ 添加数据备份机制
- ✅ 添加数据一致性校验
- ✅ 添加数据恢复功能

## 🎯 结论

**当前数据库结构完全支持AI助手的用户画像功能！**

- ✅ 所有必需的用户学习数据都有对应的数据库表
- ✅ 后端AI助手实现支持接收和处理用户画像数据
- ✅ 前端已实现完整的数据获取和传递逻辑
- ✅ AI提示词已优化为个性化教学模式

**建议：**
可以考虑添加用户学习画像缓存表来优化性能，但当前实现已经完全可用。

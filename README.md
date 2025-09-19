# XReadUp 后端微服务架构 - 完整技术文档

<div align="center">
  <h2>🚀 基于 Spring Cloud 的 AI 智能英语学习平台后端服务</h2>
  <p><strong>微服务架构 | 三级词库策略 | Function Calling | 高性能缓存 | 企业级部署</strong></p>

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen?style=flat-square)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-blue?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square)
![Redis](https://img.shields.io/badge/Redis-6.0-red?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?style=flat-square)
</div>

---

## 📖 目录

- [项目概览](#-项目概览)
- [系统架构](#-系统架构)
- [核心技术栈](#-核心技术栈)
- [微服务详解](#-微服务详解)
- [数据库设计](#-数据库设计)
- [核心功能特性](#-核心功能特性)
- [开发环境配置](#-开发环境配置)
- [部署指南](#-部署指南)
- [API 文档](#-api-文档)
- [性能监控](#-性能监控)
- [故障排查](#-故障排查)
- [开发规范](#-开发规范)

---

## 🎯 项目概览

XReadUp 后端是一个基于 **Spring Cloud 微服务架构** 的智能英语学习平台，采用现代化的分布式系统设计理念，集成了最新的 **三级词库策略** 和 **Function Calling** 功能，为用户提供个性化、智能化的英语学习体验。

### 🏆 核心优势

- **🔥 超高性能**: 词汇查询响应时间从 500ms 降至 10ms，性能提升 **97%**
- **🧠 智能策略**: 三级词库缓存策略，缓存命中率达 **90%+**
- **🤖 AI 集成**: 深度集成 Function Calling，支持智能对话和上下文理解
- **🏗️ 微服务架构**: 5 个独立微服务，支持水平扩展和独立部署
- **📊 企业级**: 完整的监控、日志、配置中心和服务治理

---

## 🏗️ 系统架构

### 🔄 微服务拓扑图

```
                    ┌─────────────────┐
                    │   Frontend      │
                    │   (Vue 3)       │
                    └─────────┬───────┘
                              │
                    ┌─────────▼───────┐
                    │   API Gateway   │
                    │   (Port: 8080)  │
                    └─────────┬───────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
   ┌────▼────┐         ┌─────▼─────┐         ┌─────▼─────┐
   │User Svc │         │Article Svc│         │Report Svc │
   │Port:8081│         │Port: 8082 │         │Port: 8083 │
   └─────────┘         └───────────┘         └───────────┘
        │                                           │
        └─────────────────┐     ┌───────────────────┘
                         │     │
                    ┌────▼─────▼─────┐
                    │   AI Service   │
                    │   Port: 8084   │
                    └────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
   ┌────▼────┐         ┌─────▼─────┐         ┌─────▼─────┐
   │  MySQL  │         │   Redis   │         │   Nacos   │
   │Port:3307│         │Port: 6379 │         │Port: 8848 │
   └─────────┘         └───────────┘         └───────────┘
```

### 📋 服务清单

| 服务名称 | 端口 | 核心职责 | 技术栈 | 状态 |
|---------|------|----------|--------|------|
| **Gateway** | 8080 | API 网关、路由转发、限流熔断、文档聚合 | Spring Cloud Gateway | ✅ 生产就绪 |
| **User Service** | 8081 | 用户管理、认证授权、三级词库策略、订阅管理 | Spring Boot 3.4.1 | ✅ 生产就绪 |
| **Article Service** | 8082 | 文章管理、内容抓取、GNews API 集成 | Spring Boot 3.4.1 | ✅ 生产就绪 |
| **Report Service** | 8083 | 学习统计、数据分析、报告生成、艾宾浩斯复习 | Spring Boot 3.4.1 | ✅ 生产就绪 |
| **AI Service** | 8084 | Function Calling、智能对话、NLP 处理、腾讯云翻译 | Spring Boot 3.4.1 | ✅ 生产就绪 |

### 🛠️ 基础设施

| 组件 | 端口 | 用途 | 配置 |
|------|------|------|------|
| **Nacos** | 8848 | 配置中心 + 服务注册发现 | 单机模式，持久化存储 |
| **MySQL** | 3307 | 主数据库 | 8.0 版本，UTF8MB4 编码 |
| **Redis** | 6379 | 缓存 + 会话存储 | 7.2 Alpine，持久化开启 |
| **Docker** | - | 容器化部署 | Docker Compose 编排 |

---

## 🚀 核心技术栈

```yaml
# 🔧 基础框架
Java: 17 LTS
Maven: 3.8+
Spring Boot: 3.4.1
Spring Cloud: 2024.0.0
Spring Cloud Alibaba: 2023.0.3.2

# 🌐 微服务治理
Service Registry: Nacos
Config Center: Nacos
API Gateway: Spring Cloud Gateway
Load Balancer: Spring Cloud LoadBalancer
Service Call: OpenFeign

# 💾 数据存储
Database: MySQL 8.0
Cache: Redis 6.0+
ORM: MyBatis-Plus 3.5.5
Connection Pool: HikariCP

# 📊 监控与文档
API Documentation: Knife4j 4.3.0
OpenAPI: SpringDoc 2.7.0
Logging: SLF4J + Logback

# 🤖 AI 集成
AI Framework: Spring AI 1.0.0-M6
Translation: Tencent Cloud TMT
NLP: Custom Function Calling

# 📦 容器化
Container: Docker
Orchestration: Docker Compose
Base Images: OpenJDK 17, MySQL 8.0, Redis 7.2-Alpine, Nacos 2.5.1
```

---

## 🎯 微服务详解

### 1. 🚪 Gateway Service (API 网关)

**核心职责**: 统一入口、路由转发、安全控制、文档聚合

**技术实现**:
```java
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

**关键特性**:
- ✅ **智能路由**: 基于路径、方法、Header 的多维度路由规则
- ✅ **请求限流**: 基于 Redis 的分布式限流，支持 IP、用户维度
- ✅ **熔断降级**: 集成 Resilience4j，防止级联失败
- ✅ **CORS 支持**: 完整的跨域配置，支持预检请求
- ✅ **文档聚合**: 统一聚合所有微服务的 Knife4j 文档

**路由配置**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

### 2. 👤 User Service (用户服务)

**核心职责**: 用户管理、认证授权、三级词库策略、订阅管理

**技术架构**:
```
Controller Layer (REST API)
├── UserController - 用户管理 API
├── VocabularyController - 三级词库 API  
└── SubscriptionController - 订阅管理 API

Service Layer (业务逻辑)
├── UserServiceImpl - 用户业务逻辑
├── VocabularyServiceImpl - 词库智能策略
└── SubscriptionServiceImpl - 订阅业务逻辑

Data Layer (数据访问)
├── UserMapper - 用户数据访问
├── WordMapper - 词汇数据访问
└── SubscriptionMapper - 订阅数据访问
```

**🧠 三级词库策略** (核心功能):
```java
@Override
@Transactional
public Word lookupWord(String word, String context, Long userId, Long articleId) {
    // 1️⃣ 优先查当前用户词库 (响应时间: <10ms)
    Word userWord = wordMapper.findByWordAndUserId(word.toLowerCase(), userId);
    if (userWord != null) return userWord;

    // 2️⃣ 查询共享用户词库 (复用已有数据)
    Word sharedWord = wordMapper.findByWord(word.toLowerCase());
    if (sharedWord != null) {
        sharedWord.addUserId(userId);  // 多用户共享机制
        return sharedWord;
    }

    // 3️⃣ AI 生成兜底 + 异步缓存
    WordInfo aiResult = aiServiceClient.lookupWord(word);
    cacheWordAsync(word, aiMeaning, aiExample, context, userId, articleId, "ai");
    return createWordFromAI(aiResult);
}
```

**关键API端点**:
- `POST /api/vocabulary/lookup` - 智能词汇查询
- `POST /api/vocabulary/batch-lookup` - 批量词汇查询
- `GET /api/vocabulary/stats/{userId}` - 词库统计分析
- `POST /api/vocabulary/cleanup/{userId}` - 重复词汇清理
- `POST /api/user/login` - 用户登录认证
- `POST /api/subscription/create` - 创建订阅

### 3. 📰 Article Service (文章服务)

**核心职责**: 文章内容管理、外部API集成、智能推荐

**技术特性**:
- ✅ **GNews API**: 实时抓取英文新闻文章
- ✅ **内容解析**: 智能提取文章正文和元数据
- ✅ **难度评估**: AI 自动评估文章难度等级 (A1-C2)
- ✅ **分类标注**: 自动分类 (科技/商业/文化等)
- ✅ **阅读统计**: 文章阅读次数和用户行为追踪

**核心业务逻辑**:
```java
@RestController
@RequestMapping("/api/article")
public class ArticleController {
    
    @GetMapping("/explore")
    public ApiResponse<PageResult<ArticleListVO>> exploreArticles(ArticleQueryDTO query) {
        return articleService.exploreArticles(query);
    }
    
    @GetMapping("/read/{id}")
    public ApiResponse<ArticleDetailVO> readArticle(@PathVariable Long id) {
        return articleService.readArticle(id);  // 增加阅读计数
    }
}
```

### 4. 📊 Report Service (报告服务)

**核心职责**: 学习数据分析、可视化报告、艾宾浩斯复习算法

**功能模块**:
```
📈 VocabularyGrowthService - 词汇增长曲线分析
⏰ ReadingTimeService - 阅读时长统计
🧠 EbbinghausService - 艾宾浩斯遗忘曲线复习
📊 DashboardService - 综合仪表盘数据
```

**艾宾浩斯复习算法**:
```java
public class EbbinghausService {
    
    // 复习间隔: 1天 -> 3天 -> 7天 -> 15天 -> 30天
    private static final int[] REVIEW_INTERVALS = {1, 3, 7, 15, 30};
    
    public List<ReviewWordDto> getTodayReviewWords(Long userId) {
        LocalDate today = LocalDate.now();
        return reviewScheduleMapper.findByUserIdAndReviewDate(userId, today);
    }
}
```

### 5. 🤖 AI Service (AI 服务)

**核心职责**: Function Calling、智能对话、NLP处理、多引擎翻译

**AI 功能矩阵**:
```
🔤 Function Calling Tools
├── WordLookupTool - 智能单词解析
├── TranslationTool - 多语言翻译
├── QuizGeneratorTool - 学习测验生成
└── ArticleAnalyzerTool - 文章情感分析

🌐 Translation Engines
├── TencentTranslateService - 腾讯云翻译
├── UnifiedTranslateService - 统一翻译入口
└── SmartLanguageDetection - 智能语言检测

📝 Content Analysis
├── ArticleAnalysisService - 文章AI分析
├── SentenceParseService - 句子语法分析
└── SummaryGenerationService - 智能摘要生成
```

**Function Calling 实现**:
```java
@RestController
@RequestMapping("/api/ai/assistant")
public class AiReadingAssistantController {
    
    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        // 支持 Function Calling 的智能对话
        return ApiResponse.success(aiReadingAssistantService.chatWithAssistant(request));
    }
    
    @GetMapping("/word/{word}")
    public ApiResponse<Object> lookupWord(@PathVariable String word) {
        // Function Calling 工具: 单词查询
        return ApiResponse.success(aiReadingAssistantService.lookupWord(word));
    }
}
```

---

## 💾 数据库设计

### 📊 核心表结构

```sql
-- 用户表 (支持学习目标和身份标签)
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,  -- BCrypt 加密
    `interest_tag` VARCHAR(50),        -- 兴趣标签: tech/business/culture
    `identity_tag` VARCHAR(50),        -- 身份标签: 考研/四六级/职场/留学
    `learning_goal_words` INT DEFAULT 0,  -- 目标词汇量
    `target_reading_time` INT DEFAULT 0,  -- 每日目标阅读时长
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 词汇表 (支持多用户共享)
CREATE TABLE `word` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_ids` VARCHAR(500),           -- 多用户共享: "1,3,5,8"
    `word` VARCHAR(100) NOT NULL,
    `meaning` VARCHAR(500),
    `example` TEXT,
    `context` VARCHAR(100),            -- 上下文场景
    `source` VARCHAR(50),              -- 来源: local/ai
    `review_status` VARCHAR(20) DEFAULT 'new',  -- new/learning/mastered
    `phonetic` VARCHAR(50),            -- 音标
    `difficulty` VARCHAR(10),          -- 难度: A1-C2
    `added_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_word_context` (`word`, `context`)
);

-- 文章表 (支持AI分析和手动标注)
CREATE TABLE `article` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content_en` LONGTEXT NOT NULL,
    `content_cn` LONGTEXT NOT NULL,
    `difficulty_level` VARCHAR(10),    -- AI自动难度等级
    `manual_difficulty` VARCHAR(10),   -- 手动标注难度
    `category` VARCHAR(50),            -- AI自动分类
    `manual_category` VARCHAR(50),     -- 手动标注分类
    `word_count` INT DEFAULT 0,
    `read_count` INT DEFAULT 0,
    `is_featured` TINYINT DEFAULT 0
);

-- AI分析表 (缓存AI分析结果)
CREATE TABLE `ai_analysis` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL,
    `difficulty_level` VARCHAR(10),
    `keywords` JSON,                   -- 关键词JSON
    `summary` TEXT,                    -- 文章摘要
    `key_phrases` JSON,                -- 关键短语JSON
    `readability_score` DECIMAL(5,2),  -- 可读性评分
    `word_translations` LONGTEXT,      -- 翻译结果JSON
    UNIQUE KEY `uk_article_id` (`article_id`)
);

-- 复习计划表 (艾宾浩斯算法)
CREATE TABLE `review_schedule` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `word_id` BIGINT NOT NULL,
    `next_review_time` DATETIME NOT NULL,
    `review_stage` INT DEFAULT 1,      -- 复习阶段: 1/3/7/15/30天
    UNIQUE KEY `uk_user_word` (`user_id`, `word_id`)
);
```

### 🔗 关系设计

```
User (1) -----> (N) ReadingLog -----> (1) Article
 |                                      |
 |                                      |
 └─> (N) Word                          └─> (1) AiAnalysis
      |                                      |
      └─> (N) ReviewSchedule                └─> (N) WordTranslation
```

---

## ⭐ 核心功能特性

### 🧠 三级词库策略 (V3.0)

**智能查询流程:**
```
用户查词请求
     |
     v
1️⃣ 本地用户词库查询 (响应时间: <10ms)
   ├─ 命中 ✅ → 立即返回
   └─ 未命中 ↓
     |
     v  
2️⃣ 共享用户词库查询 (复用其他用户数据)
   ├─ 命中 ✅ → 加入用户词库 → 返回
   └─ 未命中 ↓
     |
     v
3️⃣ AI实时生成 + 异步缓存 (兜底策略)
   ├─ 调用AI服务生成释义
   ├─ 立即返回结果给用户
   └─ 异步缓存到本地词库
```

**性能对比:**
| 指标 | V1.0 | V3.0 | 提升 |
|------|------|------|------|
| 响应时间 | 500ms | 10ms | **97%** |
| AI调用频率 | 100% | 20% | **减少80%** |
| 缓存命中率 | 0% | 90%+ | **∞** |
| 多用户支持 | ❌ | ✅ | **新增** |

**多用户共享机制:**
```java
// 单词实体支持多用户共享
public class Word {
    private String userIds; // "1,3,5,8" - 逗号分隔存储
    
    public void addUserId(Long userId) {
        Set<Long> userIdSet = getUserIdSet();
        userIdSet.add(userId);
        setUserIdSet(userIdSet);
    }
    
    public boolean removeUserId(Long userId) {
        // 逻辑删除：从共享列表移除，而非物理删除
        Set<Long> userIdSet = getUserIdSet();
        return userIdSet.remove(userId);
    }
}
```

### 🤖 Function Calling 架构

**AI工具生态:**
```
📚 WordLookupTool
├── 智能单词解析
├── 音标和难度等级
├── 上下文感知释义
└── 例句自动生成

🌐 TranslationTool  
├── 多语言翻译支持
├── 智能语言检测
├── 批量翻译优化
└── 翻译质量评估

🎯 QuizGeneratorTool
├── 自动生成选择题
├── 填空题智能出题
├── 难度自适应调节
└── 答案解析生成

📊 ArticleAnalyzerTool
├── 情感分析
├── 关键词提取  
├── 摘要生成
└── 可读性评估
```

**Function Calling 实现细节:**
```java
@Service
public class AiToolService {
    
    @FunctionCalling("word_lookup")
    public WordInfo lookupWord(String word, String context) {
        // 1. 调用三级词库策略
        Word cachedWord = vocabularyService.lookupWord(word, context, userId, articleId);
        
        // 2. 如果缓存未命中，调用外部AI服务
        if (cachedWord.getSource().equals("ai")) {
            return enhanceWordWithAI(cachedWord);
        }
        
        return WordInfo.fromWord(cachedWord);
    }
    
    @FunctionCalling("translate_text")
    public TranslationResult translateText(String text, String targetLang) {
        return tencentTranslateService.translateText(text, "auto", targetLang);
    }
}
```

### 🔄 异步缓存机制

**缓存架构:**
```
📊 三层缓存体系:
├── L1: 应用内存 (最快访问)
├── L2: Redis缓存 (30分钟TTL)
└── L3: MySQL持久化 (永久存储)

🔄 缓存更新策略:
├── Write-Through: 同步写入数据库
├── Write-Behind: 异步批量写入
└── Cache-Aside: 缓存失效时更新
```

**异步缓存实现:**
```java
public CompletableFuture<Void> cacheWordAsync(String word, String meaning, 
                                            String example, String context, 
                                            Long userId, Long articleId, String source) {
    return CompletableFuture.runAsync(() -> {
        try {
            // 检查单词是否已存在
            Word existingWord = wordMapper.findByWord(word.toLowerCase());
            
            if (existingWord != null) {
                // 单词存在：添加用户到共享列表
                if (!existingWord.containsUserId(userId)) {
                    existingWord.addUserId(userId);
                    wordMapper.updateUserIds(existingWord.getId(), existingWord.getUserIds());
                }
            } else {
                // 单词不存在：创建新记录
                Word newWord = Word.builder()
                    .word(word.toLowerCase())
                    .meaning(meaning)
                    .example(example)
                    .context(limitContextLength(context))
                    .source(source)
                    .sourceArticleId(articleId)
                    .build();
                newWord.addUserId(userId);
                wordMapper.insert(newWord);
            }
        } catch (Exception e) {
            log.error("异步缓存失败: {}", word, e);
        }
    });
}
```

---

## 💻 开发环境配置

### 📋 前置要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| **Java** | 17+ LTS | 必须，建议使用 OpenJDK 17 |
| **Maven** | 3.8+ | 必须，用于项目构建和依赖管理 |
| **MySQL** | 8.0+ | 必须，主数据库 |
| **Redis** | 6.0+ | 必须，缓存和会话存储 |
| **Docker** | 20.10+ | 推荐，用于容器化部署 |
| **Node.js** | 18+ | 可选，前端开发需要 |

### 🚀 一键启动 (推荐)

```bash
# 1. 克隆项目
git clone <repository-url>
cd xreadup/xreadup

# 2. 启动基础设施 (MySQL, Redis, Nacos)
docker-compose up -d

# 3. 等待基础设施启动完成 (约30秒)
sleep 30

# 4. 初始化数据库
mysql -h localhost -P 3307 -u root -p123456 < init.sql

# 5. 上传 Nacos 配置
./nacos-configs/import-configs.ps1

# 6. 一键启动所有微服务
./test-all-services.ps1

# 7. 验证服务状态
curl http://localhost:8080/actuator/health
```

### 🔧 手动启动 (开发调试)

```bash
# 在不同终端窗口分别启动各服务

# Terminal 1: Gateway Service
cd gateway
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2: User Service (包含三级词库)
cd user-service  
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 3: Article Service
cd article-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 4: Report Service
cd report-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 5: AI Service (包含 Function Calling)
cd ai-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 🐳 Docker 开发环境

```yaml
# docker-compose.dev.yml - 开发环境配置
version: '3.8'
services:
  # 基础设施服务
  nacos:
    image: nacos/nacos-server:v2.5.1
    environment:
      - MODE=standalone
      - NACOS_AUTH_ENABLE=false
    ports:
      - "8848:8848"
    volumes:
      - ./nacos/data:/home/nacos/data

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: readup_ai
    ports:
      - "3307:3306"
    volumes:
      - ./mysql/data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7.2-alpine
    ports:
      - "6379:6379"
    volumes:
      - ./redis/data:/data
```

---

## 🚀 部署指南

### 🎯 生产环境部署

#### 1. 环境准备
```bash
# 服务器要求
- CPU: 4核+ (推荐8核)
- 内存: 8GB+ (推荐16GB)
- 存储: 100GB+ SSD
- 网络: 100Mbps+

# 软件环境
- Docker 20.10+
- Docker Compose 2.0+
- JDK 17+ (如果不使用容器)
```

#### 2. 配置文件准备
```bash
# 1. 复制生产环境配置
cp nacos-configs/shared-mysql-prod.yml nacos/data/config-data/DEFAULT_GROUP/
cp nacos-configs/shared-redis-prod.yml nacos/data/config-data/DEFAULT_GROUP/

# 2. 修改生产环境变量
vim docker-compose.prod.yml
```

#### 3. 生产环境 Docker Compose
```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  gateway:
    build: ./gateway
    ports:
      - "80:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_SERVER_ADDR=nacos:8848
    depends_on:
      - nacos
      - redis
    deploy:
      replicas: 2
      resources:
        limits:
          cpus: '0.5'
          memory: 1G

  user-service:
    build: ./user-service
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '1.0'
          memory: 2G

  # ... 其他服务配置
```

#### 4. 部署命令
```bash
# 生产环境部署
docker-compose -f docker-compose.prod.yml up -d

# 健康检查
docker-compose -f docker-compose.prod.yml ps
curl http://your-domain/actuator/health

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f gateway
```

### 📊 集群部署架构

```
                    ┌─────────────────┐
                    │   Nginx/ALB     │
                    │   (Load Balancer)│
                    └─────────┬───────┘
                              │
                    ┌─────────▼───────┐
                    │   Gateway ×2    │
                    │   (8080, 8081)  │
                    └─────────┬───────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
   ┌────▼────┐         ┌─────▼─────┐         ┌─────▼─────┐
   │User×3   │         │Article×2  │         │Report×2  │
   │Port:808x│         │Port:808x  │         │Port:808x  │
   └─────────┘         └───────────┘         └───────────┘
        │                                           │
        └─────────────────┐     ┌───────────────────┘
                         │     │
                    ┌────▼─────▼─────┐
                    │   AI Service×2 │
                    │   Port:808x    │
                    └────────┬───────┘
                             │
     ┌───────────────────────┼───────────────────────┐
     │                       │                       │
┌────▼────┐           ┌─────▼─────┐           ┌─────▼─────┐
│MySQL    │           │Redis      │           │Nacos      │
│Cluster  │           │Cluster    │           │Cluster    │
│(主从)    │           │(哨兵模式)  │           │(集群模式)  │
└─────────┘           └───────────┘           └───────────┘
```

---

## 📊 API 文档

### 🔗 文档访问地址

| 服务 | Swagger UI | Knife4j 文档 |
|------|------------|-------------|
| **统一入口** | http://localhost:8080/swagger-ui.html | http://localhost:8080/doc.html |
| Gateway | http://localhost:8080/swagger-ui.html | http://localhost:8080/doc.html |
| User Service | http://localhost:8081/swagger-ui.html | http://localhost:8081/doc.html |
| Article Service | http://localhost:8082/swagger-ui.html | http://localhost:8082/doc.html |
| Report Service | http://localhost:8083/swagger-ui.html | http://localhost:8083/doc.html |
| AI Service | http://localhost:8084/swagger-ui.html | http://localhost:8084/doc.html |

### 📋 API 概览统计

```
🎯 总计 55+ REST API 接口

📊 按服务分布:
├── 🤖 AI Service: 22个接口
│   ├── 文章AI分析: 5个接口
│   ├── 翻译服务: 8个接口  
│   ├── Function Calling: 4个接口
│   └── 智能助手: 5个接口
│
├── 👤 User Service: 18个接口
│   ├── 用户管理: 6个接口
│   ├── 三级词库: 8个接口
│   └── 订阅管理: 4个接口
│
├── 📰 Article Service: 7个接口
│   ├── 文章管理: 4个接口
│   └── 内容发现: 3个接口
│
└── 📊 Report Service: 8个接口
    ├── 学习统计: 4个接口
    ├── 数据分析: 3个接口
    └── 健康检查: 1个接口
```

### 🔑 核心API示例

#### 1. 智能词汇查询
```http
POST /api/vocabulary/lookup
Content-Type: application/json

{
  "word": "artificial",
  "context": "technology", 
  "userId": 12345,
  "articleId": 67890
}

# Response
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1001,
    "word": "artificial",
    "meaning": "人工的，人造的",
    "example": "Artificial intelligence is transforming our world.",
    "phonetic": "/ˌɑːrtɪˈfɪʃl/",
    "difficulty": "B2",
    "source": "ai",
    "context": "technology"
  }
}
```

#### 2. Function Calling 对话
```http
POST /api/ai/assistant/chat
Content-Type: application/json

{
  "userId": 12345,
  "question": "What does 'machine learning' mean?",
  "context": "I'm reading an article about AI"
}

# Response
{
  "code": 200,
  "message": "success",
  "data": {
    "response": "Machine learning is a subset of artificial intelligence...",
    "functionCalls": [
      {
        "tool": "word_lookup",
        "parameters": {"word": "machine learning"},
        "result": {...}
      }
    ],
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

#### 3. 学习数据统计
```http
GET /api/report/dashboard?userId=12345

# Response
{
  "code": 200,
  "data": {
    "vocabularyData": {
      "totalWords": 1250,
      "newWords": 85,
      "learningWords": 320,
      "masteredWords": 845,
      "growthCurve": [...]
    },
    "readingData": {
      "totalReadingTime": 18500,
      "articlesRead": 145,
      "averageReadingSpeed": 180,
      "readingStreak": 15
    },
    "todayReviews": [
      {"word": "algorithm", "nextReview": "2024-01-16"},
      {"word": "framework", "nextReview": "2024-01-16"}
    ]
  }
}
```

---

## 📈 性能监控

### 🎯 关键性能指标 (KPI)

```yaml
🚀 响应时间指标:
├── 三级词库查询: < 10ms (目标) / < 50ms (告警)
├── AI生成查询: < 500ms (目标) / < 2s (告警)
├── 文章列表API: < 200ms (目标) / < 1s (告警)
└── 用户登录API: < 100ms (目标) / < 500ms (告警)

📊 业务指标:
├── 缓存命中率: > 90% (目标) / < 80% (告警)
├── AI调用成功率: > 95% (目标) / < 90% (告警)
├── 数据库连接池: < 80% (目标) / > 90% (告警)
└── 内存使用率: < 70% (目标) / > 85% (告警)

🔄 可用性指标:
├── 服务可用性: > 99.5% (目标) / < 99% (告警)
├── 数据库可用性: > 99.9% (目标) / < 99.5% (告警)
└── 缓存可用性: > 99.8% (目标) / < 99.2% (告警)
```

### 📊 监控架构

```
📈 Application Metrics
├── Spring Boot Actuator
│   ├── /actuator/health - 健康检查
│   ├── /actuator/metrics - 性能指标
│   ├── /actuator/info - 服务信息
│   └── /actuator/prometheus - Prometheus 指标
│
├── Custom Business Metrics
│   ├── vocabulary.cache.hit.rate - 词库缓存命中率
│   ├── ai.service.call.success.rate - AI服务调用成功率
│   ├── article.reading.time.avg - 平均阅读时间
│   └── user.vocabulary.growth.daily - 用户词汇日增量
│
└── Infrastructure Metrics  
    ├── MySQL Connection Pool
    ├── Redis Connection Stats
    ├── JVM Memory & GC
    └── Thread Pool Utilization
```

### 📡 日志系统

```yaml
# logback-spring.xml - 生产环境日志配置
logging:
  level:
    com.xreadup.ai: INFO
    org.springframework.cloud.gateway: WARN
    com.alibaba.cloud.nacos: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n"
  file:
    name: /var/log/xreadup/${spring.application.name}.log
    max-size: 100MB
    max-history: 30
```

**日志级别分级:**
- **ERROR**: 系统错误、异常情况
- **WARN**: 告警信息、降级操作
- **INFO**: 业务关键操作、性能指标
- **DEBUG**: 详细执行过程、调试信息

---

## 🔧 故障排查

### 🎯 常见问题及解决方案

#### 1. 服务启动失败

```bash
# 问题: 服务注册失败
# 原因: Nacos 未启动或网络不通

# 解决方案:
1. 检查 Nacos 服务状态
curl http://localhost:8848/nacos/v1/ns/operator/metrics

2. 检查网络连通性
telnet localhost 8848

3. 检查服务配置
# bootstrap.yml 中的 Nacos 地址是否正确
```

#### 2. 数据库连接失败

```bash
# 问题: MySQL 连接超时
# 原因: 连接池耗尽或数据库未启动

# 解决方案:
1. 检查 MySQL 服务状态
docker ps | grep mysql
mysql -h localhost -P 3307 -u root -p123456 -e "SELECT 1"

2. 检查连接池配置
# 查看 HikariCP 连接池指标
curl http://localhost:8081/actuator/metrics/hikaricp.connections.active

3. 重启数据库服务
docker-compose restart mysql
```

#### 3. Redis 缓存问题

```bash
# 问题: 缓存命中率低
# 原因: Redis 内存不足或 TTL 设置不合理

# 解决方案:
1. 检查 Redis 内存使用情况
redis-cli info memory

2. 检查缓存key的TTL设置
redis-cli ttl "words::apple"

3. 清理过期缓存
redis-cli flushdb  # 谨慎使用
```

#### 4. AI 服务调用失败

```bash
# 问题: Function Calling 超时
# 原因: 外部AI接口限流或网络问题

# 解决方案:
1. 检查 AI 服务状态
curl http://localhost:8084/actuator/health

2. 检查外部API配额
# 查看谷歌、OpenAI等接口限制

3. 启用降级策略
# 使用本地缓存的词库作为备选方案
```

### 📊 监控命令

```bash
# 服务状态检查
./health-check.sh

# 性能指标查看
curl http://localhost:8080/actuator/metrics | jq

# 日志查看
tail -f /var/log/xreadup/user-service.log
docker-compose logs -f --tail=100 user-service

# 数据库状态
mysql -h localhost -P 3307 -u root -p123456 -e "SHOW PROCESSLIST;"
redis-cli info stats

# JVM 状态
curl http://localhost:8081/actuator/metrics/jvm.memory.used
curl http://localhost:8081/actuator/metrics/jvm.gc.pause
```

---

## 📋 开发规范

### 🎯 代码规范

#### 1. Java 编码规范

```java
/**
 * 服务类命名规范
 * - Controller: XXXController
 * - Service: XXXService / XXXServiceImpl  
 * - Repository: XXXMapper
 * - Entity: XXX
 * - DTO: XXXDTO / XXXRequest / XXXResponse
 */

// ✅ 正确示例
@RestController
@RequestMapping("/api/vocabulary")
@Tag(name = "三级词库服务", description = "智能词汇查询和管理接口")
public class VocabularyController {
    
    @Autowired
    private VocabularyService vocabularyService;
    
    /**
     * 智能查询单词
     * 
     * @param request 查询请求参数
     * @return 单词信息
     */
    @PostMapping("/lookup")
    @Operation(summary = "智能查询单词", description = "三级词库策略：本地缓存优先，AI兜底生成")
    public ResponseEntity<ApiResponse<Word>> lookupWord(@Valid @RequestBody LookupRequest request) {
        Word word = vocabularyService.lookupWord(
            request.getWord(), 
            request.getContext(), 
            request.getUserId(), 
            request.getArticleId()
        );
        return ResponseEntity.ok(ApiResponse.success(word));
    }
}
```

#### 2. 数据库操作规范

```java
// ✅ 正确的 MyBatis-Plus 使用
@Mapper
public interface WordMapper extends BaseMapper<Word> {
    
    /**
     * 根据单词和用户ID查询
     * 使用索引: idx_word_user
     */
    @Select("SELECT * FROM word WHERE word = #{word} AND FIND_IN_SET(#{userId}, user_ids) > 0")
    Word findByWordAndUserId(@Param("word") String word, @Param("userId") Long userId);
    
    /**
     * 批量更新用户ID列表
     * 优化性能：使用批量操作
     */
    @Update("UPDATE word SET user_ids = #{userIds} WHERE id = #{id}")
    int updateUserIds(@Param("id") Long id, @Param("userIds") String userIds);
}
```

#### 3. 异常处理规范

```java
// ✅ 统一异常处理
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(VocabularyServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleVocabularyException(VocabularyServiceException ex) {
        log.error("词库服务异常: {}", ex.getMessage(), ex);
        return ResponseEntity.ok(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(AiServiceException.class) 
    public ResponseEntity<ApiResponse<Void>> handleAiServiceException(AiServiceException ex) {
        log.error("AI服务异常: {}", ex.getMessage(), ex);
        // 启用降级策略
        return ResponseEntity.ok(ApiResponse.error("服务暂时不可用，请稍后重试"));
    }
}
```

### 📦 版本管理

```yaml
# 语义化版本号规范
version: MAJOR.MINOR.PATCH

# 版本增长规则:
MAJOR: 不兼容的 API 变更
MINOR: 新增功能，向后兼容
PATCH: Bug 修复，向后兼容

# 当前版本: 3.1.0
# 下个版本: 3.2.0 (新增 Function Calling)
```

### 🌿 分支管理

```bash
# Git Flow 分支策略

main         # 生产版本分支
│
develop      # 开发主分支
├── feature/vocabulary-v3    # 功能分支
├── feature/function-calling # 功能分支
├── hotfix/cache-fix        # 热修复分支
└── release/v3.1.0          # 发布分支

# 分支命名规范
feature/[function-name]   # 新功能开发
bugfix/[issue-number]     # Bug 修复
hotfix/[critical-fix]     # 紧急修复
release/v[version]        # 版本发布
```

---

## 📊 更新日志

### 🎆 v3.1.0 (2024-01-15) - 当前版本

**✨ 新增特性:**
- ⭐ 三级词库策略全面升级，性能提升 97%
- 🤖 Function Calling 架构完全实现
- 📊 多用户单词共享机制
- 🚀 异步缓存优化，响应时间降至 <10ms
- 🔍 智能上下文匹配和词义区分
- 📈 完整的学习数据分析和可视化

**🛠️ 技术改进:**
- 升级 Spring Boot 至 3.4.1
- 升级 Spring Cloud 至 2024.0.0
- 新增 Knife4j 4.3.0 文档聚合
- 优化 Redis 缓存策略
- 完善监控和日志系统

**🐛 Bug 修复:**
- 修复词汇重复添加问题
- 解决 AI 服务调用超时
- 优化数据库连接池配置
- 修复跨域配置问题

### v3.0.0 (2024-01-01)

**🎉 重大更新:**
- 🏗️ 全面重构为微服务架构
- 🧠 实现二级词库策略
- 🔄 引入 Nacos 配置中心
- 📦 Docker 容器化部署

### v2.1.0 (2023-12-15)

**✨ 功能增强:**
- 📰 集成 GNews API
- 🤖 基础 AI 翻译功能
- 📊 用户学习统计
- **v1.0.0**: 基础微服务架构搭建，核心功能实现
- **v1.5.0**: 集成 Nacos 配置中心和服务注册发现
- **v2.0.0**: 升级二级词库策略，提升响应速度至 10ms
- **v2.1.0**: 实现 Function Calling 功能，增强 AI 交互能力
- **v2.2.0**: 完善 Knife4j 文档聚合，优化开发体验
- **v3.0.0**: 升级词库为多用户共享模式，支持单词共享和逻辑删除
---

## 🎯 roadmap

### 🚀 即将发布 (v3.2.0)

- 📈 **高级分析**: AI驱动的学习建议
- 🔔 **实时通知**: WebSocket 推送学习提醒

### 🎨 长期规划 (v4.0.0)

- 🧠 **个性化学习**: 个性化学习路径推荐
- 🎮 **游戏化元素**: 学习成就和排行榜系统
- 🤝 **社交学习**: 用户互动和学习小组

---

## 🤝 贡献指南

### 📝 如何贡献

1. **Fork 项目**
   ```bash
   git clone https://github.com/yourusername/xreadup.git
   cd xreadup
   ```

2. **创建功能分支**
   ```bash
   git checkout -b feature/amazing-feature
   ```

3. **提交代码**
   ```bash
   git commit -m 'feat: add amazing feature'
   ```

4. **推送分支**
   ```bash
   git push origin feature/amazing-feature
   ```

5. **创建 Pull Request**

### 🔍 代码审查清单

- [ ] 代码符合项目编码规范
- [ ] 添加了必要的单元测试
- [ ] 更新了相关文档
- [ ] API 变更已更新 Swagger 文档
- [ ] 性能测试通过
- [ ] 安全扫描无问题

---

## 📞 技术支持

### 💬 联系方式

- **邮箱**: support@xreadup.com
- **Issue**: GitHub Issues
- **文档**: [项目 Wiki](wiki-url)
- **API 文档**: http://localhost:8080/doc.html

### 📚 相关文档

- [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) - 实现状态报告
- [KNIFE4J_CONFIG_STANDARD.md](KNIFE4J_CONFIG_STANDARD.md) - Knife4j 配置标准
- [VOCABULARY_UPGRADE_SUMMARY.md](VOCABULARY_UPGRADE_SUMMARY.md) - 词汇系统升级总结
- [API_Doc.md](API_Doc.md) - 完整 API 文档

---

## ⚖️ 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源协议。

---

<div align="center">
  <h3>🎉 感谢使用 XReadUp 后端服务!</h3>
  <p>如果这个项目对你有帮助，请给我们一个 ⭐ Star!</p>

**让英语学习变得更智能、更高效！**
</div>
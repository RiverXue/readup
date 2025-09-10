### 📘 ReadUp AI — 基于 Spring AI + DeepSeek 的智能英语外刊阅读系统

🎓 本科毕业设计项目 | 技术栈：Spring Cloud Alibaba + Vue3 + DeepSeek API + Redis + MySQL + Nacos + LoadBalancer

#### 🌟 项目简介

一个帮助大学生/职场人高效阅读英文外刊的AI助手，支持：

- 📖 双语阅读 + 生词收藏（点击自动收录）
- 🤖 AI智能摘要 + 长句解析 + 读后测验（调用 DeepSeek 大模型，Spring AI 集成）
- 📊 词汇量统计 + 阅读时长报告 + 成长曲线
- 🔁 艾宾浩斯复习提醒（自动规划复习计划）
- 🏆 连续打卡激励 + 学习小组（可选）
- 👨‍💻 后台文章管理 + AI Prompt模板配置 + 数据看板

### 🛠️ 技术架构

```
[Vue3 前端]
↓ HTTP (localhost:5173)
[SpringCloud Gateway] ← 注册 → [Nacos Server] (服务发现+配置中心)
↓ 路由 + 负载均衡 (LoadBalancer)
[用户服务] [文章服务] [AI服务] [报告服务] ← 均注册到Nacos
↘     ↓     ↙
[MySQL 8.0]   [Redis 7.x]（缓存AI结果/会话）
↓
[DeepSeek API] ← Spring AI Client 调用
```

- ✅ 网关使用 Spring Cloud Gateway + LoadBalancer（替代Ribbon）
- ✅ 服务注册与发现使用 Nacos 2.x（动态服务管理）
- ✅ AI模块使用 Spring AI + DeepSeek（零训练，直接调用API，支持结构化输出与Function Calling）
- ✅ 数据库使用 MySQL 8.0（完整支持生词本+复习计划+打卡记录）
- ✅ 缓存使用 Redis（会话+AI结果缓存，提升响应速度）

### 🚀 快速启动指南

#### 1. 前置依赖

- ✅ JDK 17
- ✅ Maven 3.8+
- ✅ MySQL 8.0+
- ✅ Redis 7.x
- ✅ Node.js 18+（前端用）
- ✅ Nacos 2.3.0+ （单机模式）

#### 2. 启动 Nacos（服务注册中心）

```bash
# 下载后解压，进入 bin 目录
sh startup.sh -m standalone  # Mac/Linux
# 或
startup.cmd -m standalone    # Windows
```

访问：http://localhost:8848/nacos（账号密码：nacos/nacos）

#### 3. 启动后端服务（按顺序）

##### 3.1 修改各服务的 `application.yml`

📌 所有服务都需要配置 Nacos 地址：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
```

##### 3.2 启动顺序

```bash
# 1. 用户服务（端口 8081）→ 包含生词本管理
cd user-service
mvn spring-boot:run

# 2. 文章服务（端口 8082）
cd ../article-service
mvn spring-boot:run

# 3. AI服务（端口 8083）🌟核心
cd ../ai-service
mvn spring-boot:run

# 4. 报告服务（端口 8084）→ 包含复习提醒定时任务
cd ../report-service
mvn spring-boot:run

# 5. 网关服务（端口 8080）
cd ../gateway
mvn spring-boot:run
```

✅ 网关已集成 LoadBalancer，自动负载均衡调用下游服务

#### 4. 配置 DeepSeek API Key（在 `ai-service` 中）

在 `ai-service/src/main/resources/application.yml` 中：

```yaml
spring:
  ai:
    openai:
      api-key: sk-你的DeepSeek-API-Key-在这里
      base-url: https://api.deepseek.com/v1
    chat:
      options:
        model: deepseek-chat
        temperature: 0.3
```

🔑 申请地址：https://platform.deepseek.com
💡 DeepSeek-V3.1 已支持更强 Agent 能力，适合复杂Prompt场景

#### 5. 初始化数据库（含生词本+复习计划表）

在 MySQL 中执行：

```sql
CREATE DATABASE IF NOT EXISTS readup_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE readup_ai;
```

执行下方完整初始化脚本 👇

### 📄 数据库脚本（MySQL）— 含生词本+复习模块

```sql
-- 用户表（新增身份标签+学习目标）
CREATE TABLE `user`
(
    `id`                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username`            VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password`            VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `phone`               VARCHAR(20) COMMENT '手机号',
    `interest_tag`        VARCHAR(50) COMMENT '兴趣标签：tech/business/culture',
    `identity_tag`        VARCHAR(50) COMMENT '身份标签：考研/四六级/职场/留学',
    `learning_goal_words` INT      DEFAULT 0 COMMENT '目标词汇量',
    `target_reading_time` INT      DEFAULT 0 COMMENT '每日目标阅读时长（分钟）',
    `created_at`          DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 文章表（新增手动标注字段）
CREATE TABLE `article`
(
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title`             VARCHAR(200) NOT NULL COMMENT '标题',
    `en_content`        LONGTEXT     NOT NULL COMMENT '英文原文',
    `cn_content`        LONGTEXT     NOT NULL COMMENT '中文翻译',
    `difficulty`        VARCHAR(10) COMMENT 'AI自动难度',
    `category`          VARCHAR(50) COMMENT 'AI自动分类',
    `manual_difficulty` VARCHAR(10) COMMENT '手动标注难度：A2/B1/B2/C1',
    `manual_category`   VARCHAR(50) COMMENT '手动标注分类：科技/商业/文化',
    `read_count`        INT      DEFAULT 0 COMMENT '阅读次数',
    `created_at`        DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 生词表（新增复习状态字段）
CREATE TABLE `word`
(
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`           BIGINT       NOT NULL,
    `word`              VARCHAR(100) NOT NULL COMMENT '单词',
    `meaning`           VARCHAR(500) COMMENT '释义',
    `source_article_id` BIGINT COMMENT '来源文章ID',
    `review_status`     VARCHAR(20) DEFAULT 'new' COMMENT '复习状态：new/learning/mastered',
    `last_reviewed_at`  DATETIME     NULL COMMENT '上次复习时间',
    `next_review_at`    DATETIME     NULL COMMENT '下次复习时间',
    `added_at`          DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_word` (`user_id`, `word`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 阅读记录表
CREATE TABLE `reading_log`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`       BIGINT NOT NULL,
    `article_id`    BIGINT NOT NULL,
    `read_time_sec` INT COMMENT '阅读时长（秒）',
    `finished_at`   DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- AI缓存表（提升响应速度）
CREATE TABLE `ai_cache`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `input_text`  TEXT COMMENT '输入原文',
    `ai_type`     VARCHAR(50) COMMENT '类型：summary/parse/question',
    `output_text` TEXT COMMENT 'AI输出结果',
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 复习计划表（艾宾浩斯核心）
CREATE TABLE `review_schedule`
(
    `id`               BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`          BIGINT   NOT NULL,
    `word_id`          BIGINT   NOT NULL,
    `next_review_time` DATETIME NOT NULL COMMENT '下次复习时间',
    `review_stage`     INT      DEFAULT 1 COMMENT '复习阶段：1/2/4/7/15（天）',
    `created_at`       DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_next_review` (`user_id`, `next_review_time`),
    UNIQUE KEY `uk_user_word` (`user_id`, `word_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户单词复习计划表';

-- 打卡记录表（激励系统）
CREATE TABLE `reading_streak`
(
    `id`             BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`        BIGINT NOT NULL,
    `streak_days`    INT      DEFAULT 0 COMMENT '连续阅读天数',
    `last_read_date` DATE COMMENT '最后阅读日期',
    `updated_at`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户阅读打卡记录';
```

#### 6. 启动前端（Vue3）

```bash
cd vue3-frontend
npm install
npm run dev
```

访问：http://localhost:5173

### 📂 项目结构

```
readup-ai-parent
├── user-service        # 用户管理 + 生词本CRUD + 打卡记录
├── article-service     # 文章管理 + 手动标注
├── ai-service          # 🌟 AI核心（摘要/解析/提问/错题归档）
├── report-service      # 📊 数据报告 + 复习提醒定时任务 + 成长曲线
├── gateway             # 网关（路由 + LoadBalancer + 跨域）
├── vue3-frontend       # 前端（Vue3 + Pinia + Element Plus）
└── README.md           # 本文件
```

### ⚙️ 网关配置说明（Nacos + LoadBalancer）

`gateway/pom.xml` 核心依赖：

```xml

<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
</dependencies>
```

`gateway/src/main/resources/application.yml`：

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=1

        - id: article-service
          uri: lb://article-service
          predicates:
            - Path=/api/article/**

        - id: ai-service
          uri: lb://ai-service
          predicates:
            - Path=/api/ai/**

        - id: report-service
          uri: lb://report-service
          predicates:
            - Path=/api/report/**

  spring.webflux.cors:
    allowed-origins: "http://localhost:5173"
    allowed-methods: "*"
    allowed-headers: "*"
```

### 🧪 API 测试示例（Postman / 浏览器）

1. 测试网关路由 + LoadBalancer

```
GET http://localhost:8080/api/user/health
→ 应返回：{"status":"UP", "service":"user-service"}
```

2. 测试 AI 服务（需先启动 `ai-service`）

```bash
curl -X POST http://localhost:8080/api/ai/summary \
-H "Content-Type: application/json" \
-d '{"text": "DeepSeek-V3.1 supports stronger agent capabilities."}'
```

3. 测试生词本添加（需先启动 `user-service`）

```bash
curl -X POST http://localhost:8080/api/word/add \
-H "Content-Type: application/json" \
-d '{"userId": 1, "word": "agent", "meaning": "代理", "sourceArticleId": 101}'
```

### 🤝 作者与致谢

作者：[你的姓名]
学号：[你的学号]
指导老师：[老师姓名]

特别感谢：

- DeepSeek 提供免费大模型API支持
- Spring AI 项目提供标准化AI集成方案
- Spring Cloud Alibaba 提供稳定微服务基础设施


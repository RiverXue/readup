# 📘 ReadUp AI — 基于 Spring AI + DeepSeek 的智能英语外刊阅读系统

> 🎓 本科毕业设计项目 | 技术栈：Spring Cloud Alibaba + Vue3 + DeepSeek API + Redis + MySQL + Nacos + LoadBalancer

---

## 🌟 项目简介

一个帮助大学生/职场人高效阅读英文外刊的AI助手，支持：

- 📖 双语阅读 + 生词收藏
- 🤖 **AI智能摘要 + 长句解析**（调用 DeepSeek 大模型，Spring AI 集成）
- 📊 词汇量统计 + 学习报告
- 🔁 艾宾浩斯复习提醒
- 👨‍💻 后台文章管理 + 数据看板

---

## 🛠️ 技术架构

```
[Vue3 前端] 
     ↓ HTTP (localhost:5173)
[SpringCloud Gateway] ← 注册 → [Nacos Server] (服务发现+配置中心)
     ↓ 路由 + 负载均衡 (LoadBalancer)
[用户服务] [文章服务] [AI服务] [报告服务] ← 均注册到Nacos
     ↘     ↓     ↙
     [MySQL 8.0]   [Redis 7.x]
          ↓
   [DeepSeek API] ← Spring AI Client 调用
```

> ✅ 网关使用 **Spring Cloud Gateway + LoadBalancer**（替代Ribbon）  
> ✅ 服务注册与发现使用 **Nacos 2.x**  
> ✅ AI模块使用 **Spring AI + DeepSeek**（无需训练模型，直接调用API）

---

## 🚀 快速启动指南

### 1. 前置依赖

- ✅ JDK 17
- ✅ Maven 3.8+
- ✅ MySQL 8.0+
- ✅ Redis 7.x
- ✅ Node.js 18+（前端用）
- ✅ [Nacos 2.3.0+](https://github.com/alibaba/nacos/releases)（单机模式）

---

### 2. 启动 Nacos（服务注册中心）

```bash
# 下载后解压，进入 bin 目录
sh startup.sh -m standalone  # Mac/Linux
# 或
startup.cmd -m standalone    # Windows
```

访问：http://localhost:8848/nacos（账号密码：nacos/nacos）

---

### 3. 启动后端服务（按顺序）

#### 3.1 修改各服务的 `application.yml`

> 📌 所有服务都需要配置 Nacos 地址：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
```

#### 3.2 启动顺序

```bash
# 1. 用户服务（端口 8081）
cd user-service
mvn spring-boot:run

# 2. 文章服务（端口 8082）
cd ../article-service
mvn spring-boot:run

# 3. AI服务（端口 8083）🌟核心
cd ../ai-service
mvn spring-boot:run

# 4. 报告服务（端口 8084）
cd ../report-service
mvn spring-boot:run

# 5. 网关服务（端口 8080）
cd ../gateway
mvn spring-boot:run
```

> ✅ 网关已集成 **LoadBalancer**，自动负载均衡调用下游服务

---

### 4. 配置 DeepSeek API Key（在 `ai-service` 中）

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

> 🔑 申请地址：https://platform.deepseek.com

---

### 5. 初始化数据库

在 MySQL 中创建数据库：

```sql
CREATE DATABASE readup_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行初始化脚本（见下方 `📄 数据库脚本` 章节）

---

### 6. 启动前端（Vue3）

```bash
cd vue3-frontend
npm install
npm run dev
```

访问：http://localhost:5173

---

## 📂 项目结构

```
readup-ai-parent
├── user-service        # 用户管理（注册/登录/JWT）
├── article-service     # 文章管理（CRUD/标签/难度）
├── ai-service          # 🌟 AI核心（Spring AI + DeepSeek）
├── report-service      # 数据报告（统计/图表/AI建议）
├── gateway             # 网关（路由 + LoadBalancer + 跨域）
├── vue3-frontend       # 前端（Vue3 + Pinia + Element Plus）
└── README.md           # 本文件
```

---

## ⚙️ 网关配置说明（Nacos + LoadBalancer）

### `gateway/pom.xml` 核心依赖：

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
    <!-- LoadBalancer 替代 Ribbon -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
</dependencies>
```

### `gateway/src/main/resources/application.yml`

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
          uri: lb://user-service    # lb = loadbalancer
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

# 允许跨域（前端开发用）
spring.webflux.cors:
  allowed-origins: "http://localhost:5173"
  allowed-methods: "*"
  allowed-headers: "*"
```

> ✅ `lb://service-name` 表示使用 LoadBalancer 自动负载均衡  
> ✅ 自动从 Nacos 获取服务实例列表，无需硬编码IP

---

## 📄 数据库脚本（MySQL）

```sql
-- 用户表
CREATE TABLE `user`
(
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username`     VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password`     VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `phone`        VARCHAR(20) COMMENT '手机号',
    `interest_tag` VARCHAR(50) COMMENT '兴趣标签：tech/business/culture',
    `target_words` INT      DEFAULT 0 COMMENT '目标词汇量',
    `created_at`   DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 文章表
CREATE TABLE `article`
(
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title`      VARCHAR(200) NOT NULL COMMENT '标题',
    `en_content` LONGTEXT     NOT NULL COMMENT '英文原文',
    `cn_content` LONGTEXT     NOT NULL COMMENT '中文翻译',
    `difficulty` VARCHAR(10) COMMENT '难度：A2/B1/B2/C1',
    `category`   VARCHAR(50) COMMENT '分类：科技/商业/文化',
    `read_count` INT      DEFAULT 0 COMMENT '阅读次数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 生词表
CREATE TABLE `word`
(
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`           BIGINT       NOT NULL,
    `word`              VARCHAR(100) NOT NULL COMMENT '单词',
    `meaning`           VARCHAR(500) COMMENT '释义',
    `source_article_id` BIGINT COMMENT '来源文章ID',
    `added_at`          DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_word` (`user_id`, `word`) -- 防止重复添加
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

-- AI缓存表（可选）
CREATE TABLE `ai_cache`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `input_text`  TEXT COMMENT '输入原文',
    `ai_type`     VARCHAR(50) COMMENT '类型：summary/parse/question',
    `output_text` TEXT COMMENT 'AI输出结果',
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```

---

## 🧪 API 测试示例（Postman / 浏览器）

### 1. 测试网关路由 + LoadBalancer

```
GET http://localhost:8080/api/user/health
→ 应返回：{"status":"UP", "service":"user-service"}

GET http://localhost:8080/api/ai/test
→ 应返回 DeepSeek 翻译结果
```

### 2. 测试 AI 服务（需先启动 ai-service）

```bash
# 测试翻译
curl -X POST http://localhost:8080/api/ai/summary \
  -H "Content-Type: application/json" \
  -d '{"text": "Artificial intelligence is transforming education."}'
```

---

## 🤝 作者与致谢

- **作者**：[你的姓名]
- **学号**：[你的学号]
- **指导老师**：[老师姓名]
- **特别感谢**：DeepSeek 提供免费大模型API支持

---

## 📚 参考资料

- Spring AI GitHub：https://github.com/spring-projects/spring-ai
- Spring AI 官方文档（最新）：https://docs.spring.io/spring-ai/reference/index.html
- DeepSeek API 控制台：https://platform.deepseek.com（登录后查看 API Key 与调用示例）
- Spring Cloud Alibaba：https://sca.aliyun.com
- Nacos：https://nacos.io

---

## ❓ 常见问题（FAQ）

### Q1: 启动报错 `Invalid API key`？

→ 检查 `ai-service` 的 `application.yml` 中 `api-key` 是否复制完整，前后无空格；确认 API Key 未过期（DeepSeek 免费 Key
有有效期，过期需重新申请）。

### Q2: 服务注册不到 Nacos？

→ 1. 检查 Nacos 服务是否正常启动（访问 http://localhost:8848/nacos 能登录则正常）；2. 检查各服务 `application.yml` 中
`spring.cloud.nacos.discovery.server-addr` 配置是否为 `localhost:8848`；3. 确保服务启动时未报错，查看控制台日志是否有
Nacos 连接失败信息。

### Q3: 前端调用报跨域？

→ 1. 确保网关服务已配置 `spring.webflux.cors` 跨域规则，`allowed-origins` 与前端启动地址（http://localhost:5173）一致；2.
重启网关服务使跨域配置生效；3. 开发阶段若仍有问题，可临时在浏览器安装「CORS 解锁」插件（仅开发环境使用，生产环境需通过网关正规跨域配置）。

### Q4: DeepSeek API 响应慢或报「速率限制」？

→ 1. DeepSeek 免费 API 有每秒调用次数限制，可在 `ai-service` 中添加本地缓存（使用 `ai_cache` 表或 Redis），相同文本的 AI
请求优先从缓存获取结果，避免重复调用；2. 优化请求文本长度，避免单次传入过长的外刊内容（可按段落拆分请求）。

### Q5: 数据库初始化报错「表已存在」？

→ 执行 `CREATE DATABASE` 语句前，先检查 MySQL 中是否已存在 `readup_ai` 数据库，若存在可先执行
`DROP DATABASE IF EXISTS readup_ai;` 删除旧库后再重新创建。
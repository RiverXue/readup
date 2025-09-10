# 📘 ReadUp AI

> 🎓 技术栈：Spring Cloud Alibaba + Vue3 + DeepSeek API + Redis + MySQL + Nacos + LoadBalancer

---

## 🌟 项目简介

**ReadUp AI** 是一个基于大语言模型的智能英语外刊阅读辅助系统，面向大学生与职场人群，解决“外刊读不懂、记不住、无反馈”三大痛点。

### ✅ 核心能力

| 能力         | 技术实现                                   | 亮点                                                           |
|------------|----------------------------------------|--------------------------------------------------------------|
| 📖 智能双语阅读  | 原文+翻译并排，点击查词                           | 支持生词自动收录                                                     |
| 🤖 AI 深度解析 | Spring AI + DeepSeek-V3.1              | 支持 **Structured Outputs** 映射到 POJO，**Function Calling** 扩展能力 |
| 🔁 艾宾浩斯复习  | 定时任务 + 复习计划表                           | 自动推送复习提醒                                                     |
| 📊 学习数据看板  | ECharts + 动态统计                         | 词汇增长曲线、阅读热力图                                                 |
| 🛡️ 微服务架构  | Spring Cloud Alibaba + Nacos + Gateway | 阿里双十一验证，生产级稳定                                                |
| ⚡ 高性能缓存    | Redis 缓存 AI 结果 + 会话                    | 响应 < 500ms                                                   |

---

## 🏗️ 系统架构图

```
┌─────────────┐    ┌──────────────────┐
│  Vue3前端    │    │   Spring Gateway │
│ (localhost:5173)├──▶ (LoadBalancer)  │
└──────┬──────┘    └────────┬─────────┘
       │                   │ 注册/发现
       │             ┌─────▼─────┐
       │             │  Nacos    │
       │             │ (localhost:8848)
       │             └─────┬─────┘
       │                   │
       │     ┌─────────────▼─────────────┐
       │     │   user-service            │
       ├─────▶   (生词本+用户管理)       │
       │     └─────────────┬─────────────┘
       │                   │
       │     ┌─────────────▼─────────────┐
       │     │   article-service         │
       ├─────▶   (文章+手动标注)          │
       │     └─────────────┬─────────────┘
       │                   │
       │     ┌─────────────▼─────────────┐
       │     │   ai-service              │
       ├─────▶   (DeepSeek AI集成)        │
       │     └─────────────┬─────────────┘
       │                   │
       │     ┌─────────────▼─────────────┐
       │     │   report-service          │
       └─────▶   (报告+复习提醒+统计)      │
             └─────────────┬─────────────┘
                           │
             ┌─────────────▼─────────────┐
             │   MySQL + Redis           │
             │   (数据持久化+缓存加速)     │
             └─────────────┬─────────────┘
                           │
             ┌─────────────▼─────────────┐
             │   DeepSeek API            │
             │   (大模型推理服务)         │
             └───────────────────────────┘
```

> ✅ 架构说明：
> - 前端 → Gateway → 微服务 → 数据库/缓存 → AI 服务
> - 所有服务注册到 Nacos，Gateway 通过 LoadBalancer 路由
> - AI 服务异步调用 DeepSeek，结果可缓存至 Redis
> - Report 服务定时扫描 `review_schedule` 表，触发复习提醒

---

## 📡 API 接口规划（按模块标准化）

> ✅ 所有接口通过 Gateway 访问：`http://localhost:8080/api/{模块}/{路径}`  
> ✅ 所有响应格式统一：`{ "code": 200, "message": "success", "data": {...} }`

---

### 1️⃣ 用户服务 `user-service`（`/api/user/**`）

| 端点               | 方法   | 描述    | 请求示例                                                              | 响应示例                                                              |
|------------------|------|-------|-------------------------------------------------------------------|-------------------------------------------------------------------|
| `/register`      | POST | 用户注册  | `{ "username": "alice", "password": "123", "identityTag": "考研" }` | `{ "code": 200, "message": "注册成功" }`                              |
| `/login`         | POST | 用户登录  | `{ "username": "alice", "password": "123" }`                      | `{ "code": 200, "data": { "token": "xxx" } }`                     |
| `/word/add`      | POST | 添加生词  | `{ "userId": 1, "word": "agent", "meaning": "代理" }`               | `{ "code": 200, "message": "已加入生词本" }`                            |
| `/word/list`     | GET  | 获取生词本 | `?userId=1`                                                       | `{ "code": 200, "data": [{ "word": "agent", "status": "new" }] }` |
| `/streak/update` | POST | 更新打卡  | `{ "userId": 1 }`                                                 | `{ "code": 200, "data": { "days": 5 } }`                          |

---

### 2️⃣ 文章服务 `article-service`（`/api/article/**`）

| 端点               | 方法   | 描述   | 请求示例                                             | 响应示例                                                      |
|------------------|------|------|--------------------------------------------------|-----------------------------------------------------------|
| `/list`          | GET  | 文章列表 | `?category=科技&difficulty=B1`                     | `{ "code": 200, "data": [{ "title": "AI Revolution" }] }` |
| `/detail/{id}`   | GET  | 文章详情 | `/detail/101`                                    | `{ "code": 200, "data": { "en": "...", "cn": "..." } }`   |
| `/update-manual` | POST | 手动标注 | `{ "articleId": 101, "manualDifficulty": "B2" }` | `{ "code": 200, "message": "更新成功" }`                      |

---

### 3️⃣ AI 服务 `ai-service`（`/api/ai/**`）🌟 核心

| 端点         | 方法   | 描述    | 请求示例                                                                        | 响应示例                                                                                                   |
|------------|------|-------|-----------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| `/summary` | POST | AI 摘要 | `{ "text": "DeepSeek-V3.1 supports agent capabilities." }`                  | `{ "code": 200, "data": "DeepSeek-V3.1支持更强的代理能力。" }`                                                   |
| `/parse`   | POST | 长句解析  | `{ "sentence": "Although AI is powerful, it still needs human guidance." }` | `{ "code": 200, "data": { "grammar": "让步状语从句", "meaning": "尽管AI强大，仍需人类指导" } }`                         |
| `/quiz`    | POST | 生成测验  | `{ "text": "文章内容..." }`                                                     | `{ "code": 200, "data": [{ "question": "What is the main idea?", "options": [...], "answer": "A" }] }` |
| `/tip`     | POST | 学习建议  | `{ "articleCount": 5, "wordCount": 120 }`                                   | `{ "code": 200, "data": "你已掌握120词，继续加油！" }`                                                            |

> ✅ **技术亮点**：
> - 使用 Spring AI `ChatClient` + `Prompt` 调用 DeepSeek
> - 支持 **Structured Outputs** → 直接映射到 `QuizQuestionVO` 等 POJO
> - 支持 **Function Calling** → 未来可扩展“查词典”“搜例句”等工具

---

### 4️⃣ 报告服务 `report-service`（`/api/report/**`）

| 端点              | 方法  | 描述     | 请求示例               | 响应示例                                                                  |
|-----------------|-----|--------|--------------------|-----------------------------------------------------------------------|
| `/growth`       | GET | 词汇增长曲线 | `?userId=1&days=7` | `{ "code": 200, "data": { "dates": [...], "counts": [...] } }`        |
| `/time-stats`   | GET | 阅读时长统计 | `?userId=1`        | `{ "code": 200, "data": { "today": 15, "weekAvg": 25 } }`             |
| `/review-today` | GET | 今日待复习  | `?userId=1`        | `{ "code": 200, "data": [{ "word": "agent", "due": "2025-04-05" }] }` |

> ✅ **定时任务**：`@Scheduled(cron = "0 0 9 * * ?")` 每天9点扫描 `review_schedule` 表

---

## 🔐 安全与可观测性（专业化）

### ✅ 安全

- JWT Token 鉴权（`Authorization: Bearer xxx`）
- 网关层统一 CORS 配置
- 敏感数据加密（BCrypt 加密密码）

### ✅ 可观测性

- **日志**：Logback + 控制台输出
- **监控**：Spring Boot Actuator（`/actuator/health`, `/actuator/metrics`）
- **链路追踪**：可集成 SkyWalking（需额外配置）
- **指标看板**：Grafana + Prometheus（展示 QPS、响应时间、错误率）

---

## 📚 技术选型依据（引用官方文档）

| 技术                       | 选型理由                                                        | 官方文档引用                                                                                                                                                                       |
|--------------------------|-------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Spring AI**            | 提供标准化 AI 集成，支持 Structured Outputs 和 Function Calling，避免厂商锁定 | [Spring AI Docs](https://docs.spring.io/spring-ai/reference/)：“Supports mapping of AI Model output to POJOs” & “Permits the model to request execution of client-side tools” |
| **DeepSeek-V3.1**        | 免费、中文优化、支持 Agent 能力，适合复杂 Prompt 场景                          | [DeepSeek Platform](https://platform.deepseek.com)：“更强的 agent 能力”                                                                                                            |
| **Spring Cloud Alibaba** | 阿里生产级验证，支持服务发现、限流、配置管理                                      | [SCA Docs](https://sca.aliyun.com)：“核心组件都经过过阿里巴巴多年双十一洪峰考验”                                                                                                                   |
| **Nacos**                | 动态服务发现与配置管理，支持健康检查                                          | [Nacos Docs](https://nacos.io)：“实时健康检查，防止请求发往不健康实例”                                                                                                                          |

---

## 🤝 作者与致谢

- **作者**：[你的姓名]
- **学号**：[你的学号]
- **指导老师**：[老师姓名]

**特别感谢**：

- DeepSeek 提供免费大模型API支持
- Spring AI 项目提供标准化AI集成方案
- Spring Cloud Alibaba 提供稳定微服务基础设施

---
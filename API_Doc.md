# XReadUp AI 项目 API 文档

## 📋 文档概述
本文档包含XReadUp AI项目所有微服务的完整REST API接口定义，按照服务模块进行分类组织。

## 🚀 微服务架构
XReadUp AI采用Spring Cloud微服务架构，包含以下核心服务：
- **AI服务**：提供智能分析、翻译和AI助手功能
- **文章服务**：管理文章内容、分类和阅读功能
- **用户服务**：用户管理、认证和词汇学习
- **报告服务**：学习统计、报表生成和学习建议
- **网关服务**：统一入口，请求路由和负载均衡

## 🔧 API 访问方式
所有API通过网关服务统一访问，实际部署时可通过`http://网关地址/api/[服务模块]/[接口路径]`进行调用。

## 📊 API 概览
目前系统共包含 **53个** REST API接口，分布如下：
- **AI服务**：22个接口（智能分析、翻译、AI助手）
- **文章服务**：7个接口（文章管理、阅读、内容提取）
- **用户服务**：16个接口（用户管理、订阅、词汇学习）
- **报告服务**：8个接口（学习统计、报表、健康检查）

---

## 🧠 AI 服务 (ai-service) - 22个接口
提供智能分析、翻译和AI助手功能。

### 1. DeepSeek AI - 进阶AI分析引擎

#### 文章摘要
```http
POST /api/ai/summary
```
- **描述**：使用DeepSeek对文章进行智能摘要并持久化存储
- **请求体**：`AiSummaryRequest` (包含articleId和text字段)
- **响应**：`ApiResponse<String>` (摘要文本)

#### 长句解析
```http
POST /api/ai/parse
```
- **描述**：使用DeepSeek对复杂句子进行语法和语义分析并持久化存储
- **请求体**：`SentenceParseRequest` (包含articleId和sentence字段)
- **响应**：`ApiResponse<SentenceParseResponse>` (解析结果)

#### 生成测验题
```http
POST /api/ai/quiz
```
- **描述**：基于文章内容生成阅读理解测验题并持久化存储
- **请求体**：`QuizGenerationRequest` (包含articleId、text和questionCount字段)
- **响应**：`ApiResponse<List<QuizQuestion>>` (测验题列表)

#### 学习建议
```http
POST /api/ai/tip
```
- **描述**：基于用户学习数据生成个性化学习建议并持久化存储
- **请求体**：`LearningTipRequest` (包含userId、articleId、articleCount、wordCount和learningDays字段)
- **响应**：`ApiResponse<String>` (学习建议文本)

#### 综合AI分析
```http
POST /api/ai/comprehensive
```
- **描述**：提供全面的文章分析和学习建议并持久化存储所有结果
- **请求体**：`ComprehensiveAnalysisRequest` (包含articleId、userId和text字段)
- **响应**：`ApiResponse<ComprehensiveAnalysisResponse>` (综合分析结果)

### 2. 智能AI分析 - 产品经理优化版

#### 智能AI分析
```http
POST /api/ai/smart/analyze
```
- **描述**：一键智能分析，基于场景自动选择最佳分析类型
- **请求体**：`SmartAnalysisRequest` (包含content、userId和可选的scenario字段)
- **响应**：`ApiResponse<SmartAnalysisResponse>` (分析结果，根据场景不同包含摘要、关键点、详细分析等)

#### AI学习助手
```http
POST /api/ai/smart/assistant
```
- **描述**：基于用户历史提供个性化学习建议
- **请求体**：`AssistantRequest` (包含userId、question和questionType字段)
- **响应**：`ApiResponse<LearningAssistantResponse>` (助手回答和学习建议)

### 3. AI阅读助手 - 支持Function Calling

#### AI对话
```http
POST /api/ai/assistant/chat
```
- **描述**：与AI助手进行对话，支持Function Calling
- **请求体**：`AiChatRequest` (包含userId和question字段)
- **响应**：`ApiResponse<AiChatResponse>` (AI助手回复)

#### 查询单词
```http
GET /api/ai/assistant/word/{word}
```
- **描述**：查询单词的详细信息（Function Calling工具）
- **路径参数**：word (要查询的单词)
- **响应**：`ApiResponse<Object>` (单词详细信息)

#### 翻译文本
```http
POST /api/ai/assistant/translate
```
- **描述**：翻译文本内容（Function Calling工具）
- **请求参数**：text (要翻译的文本)、targetLang (目标语言，默认zh)
- **响应**：`ApiResponse<String>` (翻译结果)

#### 生成测验
```http
POST /api/ai/assistant/quiz
```
- **描述**：根据文章内容生成学习测验题
- **请求体**：`QuizRequest` (包含articleContent和articleId字段)
- **响应**：`ApiResponse<List<QuizQuestion>>` (测验题列表)

### 4. 智能翻译 - 产品经理优化版

#### 智能翻译
```http
POST /api/ai/translate/smart
```
- **描述**：一键智能翻译，自动识别语言，支持批量翻译
- **请求体**：`SmartTranslateRequest` (包含text和可选的targetLang字段)
- **响应**：`ApiResponse<TencentTranslateResponseDTO>` (翻译结果)

#### 批量智能翻译
```http
POST /api/ai/translate/batch
```
- **描述**：批量文本智能翻译
- **请求体**：`BatchTranslateRequest` (包含texts列表字段)
- **响应**：`ApiResponse<BatchTranslateResponse>` (批量翻译结果)

### 5. 腾讯云翻译 - 基础翻译引擎

#### 通用文本翻译
```http
POST /api/ai/tencent-translate/text
```
- **描述**：使用腾讯云基础翻译引擎进行文本翻译
- **请求体**：`TencentTranslateRequestDTO` (包含text、sourceLang和targetLang字段)
- **响应**：`ApiResponse<TencentTranslateResponseDTO>` (翻译结果)

#### 英文到中文翻译
```http
GET /api/ai/tencent-translate/en-to-zh
```
- **描述**：将英文内容翻译成中文
- **请求参数**：text (要翻译的英文文本)
- **响应**：`ApiResponse<TencentTranslateResponseDTO>` (翻译结果)

#### 中文到英文翻译
```http
GET /api/ai/tencent-translate/zh-to-en
```
- **描述**：将中文内容翻译成英文
- **请求参数**：text (要翻译的中文文本)
- **响应**：`ApiResponse<TencentTranslateResponseDTO>` (翻译结果)

#### 批量翻译
```http
POST /api/ai/tencent-translate/batch
```
- **描述**：批量翻译多个文本片段
- **请求体**：批量翻译请求对象
- **响应**：`ApiResponse<TencentTranslateResponseDTO>` (翻译结果)

### 6. AI分析服务 - 基础接口

#### 文章AI分析
```http
POST /api/ai/analyze
```
- **描述**：对单篇文章进行AI分析并保存结果到数据库
- **请求体**：`ArticleAnalysisRequest` (包含articleId和content字段)
- **响应**：`ApiResponse<ArticleAnalysisResponse>` (分析结果)

#### 批量文章AI分析
```http
POST /api/ai/analyze/batch
```
- **描述**：对多篇文章进行AI分析并保存结果到数据库
- **请求体**：`BatchAiAnalysisRequest` (包含articleIds列表字段)
- **响应**：`ApiResponse<List<ArticleAnalysisResponse>>` (分析结果列表)

#### 全文翻译
```http
POST /api/ai/translate/fulltext
```
- **描述**：将英文内容完整翻译成中文并保存到数据库
- **请求体**：`TranslationRequest` (包含articleId和content字段)
- **响应**：`ApiResponse<TranslationResponse>` (翻译结果)

#### 选词翻译
```http
POST /api/ai/translate/word
```
- **描述**：提供单词的详细翻译和解释，并保存到数据库
- **请求体**：`WordTranslationRequest` (包含word和articleId字段)
- **响应**：`ApiResponse<WordTranslationResponse>` (详细翻译信息)

#### 健康检查
```http
GET /api/ai/health
```
- **描述**：检查AI服务状态
- **响应**：`ApiResponse<String>` (服务状态)

---

## 📚 文章服务 (article-service) - 7个接口
提供文章管理、发现和阅读功能。

### 1. 文章管理

#### 探索文章列表
```http
GET /api/article/explore
```
- **描述**：根据条件筛选和分页获取文章列表
- **请求参数**：`ArticleQueryDTO` (包含分类、难度、分页等筛选条件)
- **响应**：`ApiResponse<PageResult<ArticleListVO>>` (文章列表)

#### 获取文章详情
```http
GET /api/article/read/{id}
```
- **描述**：获取文章详细信息并增加阅读次数
- **路径参数**：id (文章ID)
- **响应**：`ApiResponse<ArticleDetailVO>` (文章详情)

#### AI深度分析
```http
GET /api/article/{id}/deep-dive
```
- **描述**：对文章进行AI深度分析，包括难度评估、关键词提取、摘要生成等
- **路径参数**：id (文章ID)
- **响应**：`ApiResponse<ArticleDetailVO>` (增强的文章详情)

#### 手动标注文章难度
```http
POST /api/article/feedback/difficulty
```
- **描述**：允许用户手动标注文章难度等级
- **请求体**：`ManualDifficultyDTO` (包含articleId和difficulty字段)
- **响应**：`ApiResponse<Boolean>` (操作结果)

#### 按主题获取文章
```http
POST /api/article/discover/category
```
- **描述**：根据指定主题获取最新文章
- **请求参数**：category (主题名称)、limit (数量限制，默认10)
- **响应**：`ApiResponse<Object>` (主题文章列表)

#### 获取热点文章
```http
POST /api/article/discover/trending
```
- **描述**：获取当前热点新闻文章
- **请求参数**：limit (数量限制，默认10)
- **响应**：`ApiResponse<Object>` (热点文章列表)

#### 从URL提取可读内容
```http
GET /api/article/extract-content
```
- **描述**：使用Readability4J从指定URL提取文章的可读内容
- **请求参数**：url (文章URL)
- **响应**：`ApiResponse<String>` (提取的文章内容)

---

## 👤 用户服务 (user-service) - 16个接口
提供用户管理、认证和词汇学习功能。

### 1. 用户管理

#### 用户注册
```http
POST /api/user/register
```
- **描述**：新用户注册接口
- **请求体**：`UserRegisterRequest` (包含username和password字段)
- **响应**：注册结果和用户信息

#### 用户登录
```http
POST /api/user/login
```
- **描述**：用户登录验证接口
- **请求体**：`UserLoginRequest` (包含username和password字段)
- **响应**：登录结果和JWT token

#### 加入生词本
```http
POST /api/user/vocabulary/add
```
- **描述**：一键收藏难词，打造专属词汇库
- **请求体**：`AddWordRequest` (包含userId和word字段)
- **响应**：操作结果

#### 查看生词本
```http
GET /api/user/vocabulary/my-words
```
- **描述**：回顾你的词汇收藏，温故而知新
- **请求参数**：userId (用户ID)
- **响应**：词汇列表

#### 每日打卡
```http
POST /api/user/progress/check-in
```
- **描述**：每日阅读打卡，积累学习成就
- **请求参数**：userId (用户ID)
- **响应**：当前连续打卡天数

### 2. 订阅管理 - 商业化功能

#### 创建订阅
```http
POST /api/subscription/create
```
- **描述**：创建新的订阅套餐
- **请求参数**：userId (用户ID)、planType (套餐类型)、paymentMethod (支付方式)
- **响应**：订阅创建结果

#### 获取当前订阅
```http
GET /api/subscription/current/{userId}
```
- **描述**：获取用户当前激活的订阅信息
- **路径参数**：userId (用户ID)
- **响应**：当前订阅信息

#### 获取订阅历史
```http
GET /api/subscription/history/{userId}
```
- **描述**：获取用户的订阅历史记录
- **路径参数**：userId (用户ID)
- **响应**：订阅历史列表

#### 取消订阅
```http
POST /api/subscription/cancel/{subscriptionId}
```
- **描述**：取消用户的订阅
- **路径参数**：subscriptionId (订阅ID)
- **响应**：操作结果

#### 检查使用限制
```http
GET /api/subscription/check-limit/{userId}
```
- **描述**：检查用户是否超出使用限制
- **路径参数**：userId (用户ID)
- **请求参数**：articlesCount (文章数量)、wordsCount (单词数量)
- **响应**：是否在限制范围内

#### 获取剩余额度
```http
GET /api/subscription/quota/{userId}
```
- **描述**：获取用户剩余的使用额度
- **路径参数**：userId (用户ID)
- **响应**：剩余额度信息

### 3. 二级词库服务 - 智能词汇查询

#### 智能查询单词
```http
POST /api/vocabulary/lookup
```
- **描述**：二级词库策略：本地缓存优先，AI兜底生成
- **请求体**：`LookupRequest` (包含word、context、userId和articleId字段)
- **响应**：单词详细信息

#### 批量查询单词
```http
POST /api/vocabulary/batch-lookup
```
- **描述**：批量智能查询多个单词
- **请求体**：`BatchLookupRequest` (包含words列表、context、userId和articleId字段)
- **响应**：单词列表信息

#### 获取词库统计
```http
GET /api/vocabulary/stats/{userId}
```
- **描述**：获取用户的词汇学习统计信息
- **路径参数**：userId (用户ID)
- **响应**：统计数据

#### 清理重复词汇
```http
POST /api/vocabulary/cleanup/{userId}
```
- **描述**：清理用户词库中的重复词汇
- **路径参数**：userId (用户ID)
- **响应**：清理结果

#### 智能添加单词
```http
POST /api/vocabulary/add
```
- **描述**：智能添加单词到生词本，支持上下文
- **请求体**：`AddWordRequest` (包含word、context、userId和sourceArticleId字段)
- **响应**：添加的单词信息

---

## 📊 报告服务 (report-service) - 8个接口
提供学习统计、报表生成和学习建议功能。

### 1. 学习统计与报表

#### 词汇增长曲线
```http
GET /api/report/growth-curve
```
- **描述**：追踪你的词汇量成长轨迹
- **请求参数**：userId (用户ID)、days (统计天数，默认7)
- **响应**：`ApiResponse<VocabularyGrowthData>` (词汇增长数据)

#### 阅读时长统计
```http
GET /api/report/reading-time
```
- **描述**：深度分析你的阅读习惯
- **请求参数**：userId (用户ID)
- **响应**：`ApiResponse<ReadingTimeData>` (阅读时长数据)

#### 艾宾浩斯智能复习提醒
```http
GET /api/report/review-today
```
- **描述**：艾宾浩斯智能复习提醒
- **请求参数**：userId (用户ID)
- **响应**：`ApiResponse<List<ReviewWordDto>>` (今日待复习单词列表)

#### 今日学习日报
```http
GET /api/report/today/summary
```
- **描述**：今日阅读成果一目了然
- **请求参数**：userId (用户ID)
- **响应**：今日学习统计数据

#### 一周学习周报
```http
GET /api/report/weekly/insights
```
- **描述**：本周学习成果深度分析
- **请求参数**：userId (用户ID)
- **响应**：一周学习趋势数据

#### 学习成就统计
```http
GET /api/report/streak/achievement
```
- **描述**：见证你的坚持，记录每一个学习里程碑
- **请求参数**：userId (用户ID)
- **响应**：学习成就数据

#### 学习仪表盘
```http
GET /api/report/dashboard
```
- **描述**：一站式查看所有学习数据
- **请求参数**：userId (用户ID)
- **响应**：`ApiResponse<DashboardData>` (综合学习数据仪表盘)

#### 健康检查
```http
GET /api/report/health
```
- **描述**：检查报表服务状态
- **响应**：`ApiResponse<String>` (服务健康状态)

---

## 🚦 API 访问地址
各服务的Swagger文档访问地址：

| 服务名称 | 访问地址 | 服务说明 |
|---------|---------|---------|
| AI服务 | http://localhost:8084/swagger-ui.html | 提供智能分析、翻译和AI助手功能 |
| 文章服务 | http://localhost:8082/swagger-ui.html | 管理文章内容、分类和阅读功能 |
| 报告服务 | http://localhost:8083/swagger-ui.html | 学习统计、报表生成和学习建议 |
| 用户服务 | http://localhost:8081/swagger-ui.html | 用户管理、认证和词汇学习 |

---

## 🔍 常见问题

### Q: API请求返回401错误怎么办？
**A:** 401错误表示未授权，请确保在请求头中包含有效的JWT token，格式为 `Authorization: Bearer {your_token}`。

### Q: 如何获取API请求所需的参数示例？
**A:** 请访问各服务的Swagger文档页面，其中包含详细的参数说明和请求示例。

### Q: API响应格式是什么样的？
**A:** 大多数API响应采用统一的`ApiResponse`格式，包含code、message和data三个字段：
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

---

## 📝 更新日志

- **v1.0.0**：基础API接口实现，核心功能上线
- **v1.5.0**：整合Nacos服务注册发现，优化API路由
- **v2.0.0**：升级二级词库策略，重构部分API接口
- **v2.1.0**：实现Function Calling功能，新增AI助手API
- **v2.2.0**：统一Knife4j文档配置，完善API文档

---

## 📧 联系我们
如有任何问题或建议，请联系项目维护团队。
# XReadUp AI 项目 API 文档

## 📋 文档概述

本文档包含XReadUp AI项目所有微服务的完整REST API接口定义，按照服务模块进行分类组织。基于实际代码实现，详细记录每个接口的使用场景、业务逻辑、参数说明和响应格式。

## 🚀 微服务架构

XReadUp AI采用Spring Cloud微服务架构，包含以下核心服务：

- **网关服务** (端口:8080)：统一入口，请求路由和负载均衡
- **用户服务** (端口:8081)：用户管理、认证和三级词库策略
- **文章服务** (端口:8082)：管理文章内容、分类和阅读功能  
- **报告服务** (端口:8083)：学习统计、报表生成和学习建议
- **AI服务** (端口:8084)：提供智能分析、翻译和AI助手功能
- **管理员服务** (端口:8085)：系统配置管理、管理员权限控制

## 🔧 API 访问方式

### 开发环境

- **直接访问**：`http://localhost:[服务端口]/api/[接口路径]`
- **网关访问**：`http://localhost:8080/api/[服务模块]/[接口路径]`

### 生产环境

- **统一网关**：`http://网关地址/api/[服务模块]/[接口路径]`

## 📊 API 概览

目前系统共包含 **60+个** REST API接口，分布如下：

- **AI服务**：22个接口（DeepSeek智能分析、腾讯云翻译、AI助手）
- **文章服务**：7个接口（文章发现、阅读管理、内容提取）
- **用户服务**：18个接口（用户认证、三级词库、订阅管理）
- **报告服务**：9个接口（学习统计、艾宾浩斯复习、数据仪表盘）
- **管理员服务**：5个接口（系统配置管理、管理员权限控制）

## 🔐 认证说明

大部分API需要JWT Token认证，请在请求头中添加：

```http
Authorization: Bearer {your_jwt_token}
```

---

## 🧠 AI 服务 (ai-service) - 22个接口

提供智能分析、翻译和AI助手功能。服务采用双引擎策略：DeepSeek（进阶AI）+ 腾讯云（基础翻译）。

### 1. DeepSeek AI - 进阶AI分析引擎

> **使用场景**：适用于需要深度分析的学习场景，如考研阅读、四六级备考等。

#### 文章摘要

```http
POST /api/ai/summary
```

- **使用场景**：快速获取文章核心内容，提高阅读效率
- **业务逻辑**：使用DeepSeek进行智能摘要提取，结果持久化存储到数据库
- **请求体**：`AiSummaryRequest`
  
  ```json
  {
    "articleId": 123,
    "text": "文章内容..."
  }
  ```
- **响应**：`ApiResponse<String>`
  
  ```json
  {
    "code": 200,
    "message": "success",
    "success": true,
    "data": "文章摘要内容..."
  }
  ```
- **实现特点**：
  - 自动保存到 `ai_analysis.deepseek_summary` 字段
  - 支持强制重新生成
  - 支持中英文混合内容

#### 长句解析

```http
POST /api/ai/parse
```

- **使用场景**：针对复杂句子的语法和语义分析，提升语法理解能力
- **业务逻辑**：解析句子结构、语法要点、核心词汇，结果持久化
- **请求体**：`SentenceParseRequest`
  
  ```json
  {
    "articleId": 123,
    "sentence": "Despite the heavy rain, the outdoor concert continued as planned.",
    "parseLevel": "advanced"
  }
  ```
- **响应**：`ApiResponse<SentenceParseResponse>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "originalSentence": "原句子",
      "sentenceStructure": {
        "mainClause": ["主句分析"],
        "subordinateClause": ["从句分析"],
        "sentenceType": "句子类型"
      },
      "grammar": {
        "tense": "时态",
        "voice": "语态",
        "mood": "语气"
      },
      "meaning": "核心含义",
      "keyVocabulary": [关键词汇列表],
      "grammarPoints": ["语法要点列表"],
      "learningTip": "学习建议"
    }
  }
  ```

#### 生成测验题

```http
POST /api/ai/quiz
```

- **使用场景**：基于文章内容生成阅读理解测验，检验学习效果
- **业务逻辑**：使用DeepSeek Structured Outputs生成标准化题目，支持多种题型
- **请求体**：`QuizGenerationRequest`
  
  ```json
  {
    "articleId": 123,
    "text": "文章内容...",
    "questionCount": 5,
    "questionType": "comprehension",
    "difficulty": "medium"
  }
  ```
- **响应**：`ApiResponse<List<QuizQuestion>>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": [
      {
        "question": "题目内容",
        "options": ["A选项", "B选项", "C选项", "D选项"],
        "correctAnswer": "A",
        "explanation": "解释说明",
        "difficulty": "medium",
        "type": "multiple_choice"
      }
    ]
  }
  ```

#### 学习建议

```http
POST /api/ai/tip
```

- **使用场景**：基于用户学习数据生成个性化学习建议
- **业务逻辑**：综合分析用户学习进度、词汇量、学习天数等数据
- **请求体**：`LearningTipRequest`
  
  ```json
  {
    "userId": 123,
    "articleId": 456,
    "articleCount": 15,
    "wordCount": 120,
    "learningDays": 7,
    "totalReadingMinutes": 300,
    "identityTag": "kaoyan"
  }
  ```
- **响应**：`ApiResponse<String>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": "根据您的7天学习情况，建议..."
  }
  ```

#### 综合AI分析

```http
POST /api/ai/comprehensive
```

- **使用场景**：一键获取文章的全面分析结果，适合深度学习
- **业务逻辑**：整合摘要、解析、测验、建议等多项分析结果
- **请求体**：`ComprehensiveAnalysisRequest`
  
  ```json
  {
    "articleId": 123,
    "userId": 456,
    "text": "文章内容..."
  }
  ```
- **响应**：`ApiResponse<ComprehensiveAnalysisResponse>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "summary": "文章摘要",
      "keyPoints": "关键要点",
      "difficulty": "B2",
      "quiz": [测验题列表],
      "learningTips": "学习建议",
      "analysisTime": 1500
    }
  }
  ```

### 2. 统一智能分析 - 产品经理优化版

> **使用场景**：适合一般用户快速获取AI分析结果，无需复杂配置。

#### 智能AI分析

```http
POST /api/ai/smart/analyze
```

- **使用场景**：一键智能分析，基于场景自动选择最佳分析类型
- **业务逻辑**：
  - 自动识别内容类型和复杂度
  - 智能匹配分析场景（快速摘要、标准分析、深度分析、词汇焦点、测验模式）
  - 提供个性化学习路径推荐
- **请求体**：`SmartAnalysisRequest`
  
  ```json
  {
    "userId": 123,
    "content": "文章内容...",
    "scenario": "deep_analysis" // 可选: quick_summary, standard_analysis, deep_analysis, vocabulary_focus, quiz_mode
  }
  ```
- **响应**：`ApiResponse<SmartAnalysisResponse>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "scenario": "deep_analysis",
      "summary": "摘要内容",
      "keyPoints": "关键要点",
      "detailedAnalysis": "详细分析",
      "vocabularyAnalysis": "词汇分析",
      "quizQuestions": [测验题列表],
      "personalizedRecommendations": [个性化推荐],
      "analysisTime": 1200,
      "userId": 123
    }
  }
  ```

#### AI学习助手

```http
POST /api/ai/smart/assistant
```

- **使用场景**：基于用户历史提供个性化学习建议
- **业务逻辑**：综合用户学习历史、反馈和目标生成智能建议
- **请求体**：`AssistantRequest`
  
  ```json
  {
    "userId": 123,
    "question": "如何提高阅读理解能力？",
    "questionType": "learning_advice" // 可选: learning_advice, vocabulary_help, reading_strategy
  }
  ```
- **响应**：`ApiResponse<LearningAssistantResponse>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "userId": 123,
      "answer": "基于您的学习情况...",
      "nextSteps": ["下一步建议列表"],
      "relatedTopics": ["相关话题"],
      "responseTime": 800
    }
  }
  ```

### 3. AI阅读助手 - 支持Function Calling

> **使用场景**：互动式AI对话，支持动态调用工具函数，适合智能问答场景。

#### AI对话

```http
POST /api/ai/assistant/chat
```

- **使用场景**：与AI助手进行对话，支持动态调用各种工具函数
- **业务逻辑**：
  - 支持Function Calling，能够自动调用单词查询、翻译、测验生成等工具
  - 上下文感知，能理解文章内容和用户问题
  - 提供个性化回答和学习建议
- **请求体**：`AiChatRequest`
  
  ```json
  {
    "userId": 123,
    "question": "请解释一下technology这个单词",
    "articleContext": "相关文章内容",
    "articleId": 456
  }
  ```
- **响应**：`ApiResponse<AiChatResponse>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "answer": "AI助手的回答",
      "referencedWords": [
        {
          "word": "technology",
          "meaning": "技术",
          "phonetic": "/tekˈnɒlədʒi/"
        }
      ],
      "followUpQuestion": "后续问题建议",
      "difficulty": "B2"
    }
  }
  ```

#### 查询单词

```http
GET /api/ai/assistant/word/{word}
```

- **使用场景**：Function Calling工具函数，为AI对话提供关联单词信息
- **路径参数**：`word` - 要查询的单词
- **响应**：`ApiResponse<Object>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "word": "technology",
      "meaning": "技术",
      "phonetic": "/tekˈnɒlədʒi/",
      "examples": ["使用示例"]
    }
  }
  ```

#### 翻译文本

```http
POST /api/ai/assistant/translate
```

- **使用场景**：Function Calling工具函数，为AI对话提供翻译服务
- **请求参数**：
  - `text` - 要翻译的文本
  - `targetLang` - 目标语言(默认zh)
- **响应**：`ApiResponse<String>`

#### 生成测验

```http
POST /api/ai/assistant/quiz
```

- **使用场景**：根据文章内容生成学习测验题
- **请求体**：`QuizRequest`
  
  ```json
  {
    "articleContent": "文章内容",
    "articleId": 123
  }
  ```
- **响应**：`ApiResponse<List<QuizQuestion>>`

### 4. 智能翻译 - 产品经理优化版

> **使用场景**：适合日常翻译需求，支持中英文自动识别和批量翻译。

#### 智能翻译

```http
POST /api/ai/translate/smart
```

- **使用场景**：一键智能翻译，自动识别语言，支持批量翻译
- **业务逻辑**：
  - 自动语言检测（中文/英文）
  - 智能选择翻译引擎（腾讯云）
  - 翻译质量优化和后处理
- **请求体**：`SmartTranslateRequest`
  
  ```json
  {
    "text": "Hello, world!",
    "targetLang": "zh" // 可选，默认为自动检测
  }
  ```
- **响应**：`ApiResponse<TencentTranslateResponseDTO>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "translatedText": "你好，世界！",
      "sourceLang": "en",
      "targetLang": "zh",
      "originalText": "Hello, world!",
      "translateTime": 150,
      "provider": "Tencent Cloud",
      "status": "success"
    }
  }
  ```

#### 批量智能翻译

```http
POST /api/ai/translate/batch
```

- **使用场景**：批量文本智能翻译，适合大量内容翻译
- **业务逻辑**：分批处理、并发控制、结果合并
- **请求体**：`BatchTranslateRequest`
  
  ```json
  {
    "texts": [
      "Hello, world!",
      "How are you?",
      "Nice to meet you."
    ]
  }
  ```
- **响应**：`ApiResponse<BatchTranslateResponse>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "results": [
        {
          "originalText": "Hello, world!",
          "translatedText": "你好，世界！",
          "success": true
        }
      ],
      "totalCount": 3,
      "successCount": 3,
      "failedCount": 0,
      "totalTime": 450
    }
  }
  ```

### 5. 腾讯云翻译 - 基础翻译引擎

> **使用场景**：适合需要精确控制翻译参数的场景，支持多语言。

#### 通用文本翻译

```http
POST /api/ai/tencent-translate/text
```

- **使用场景**：使用腾讯云基础翻译引擎进行文本翻译
- **请求体**：`TencentTranslateRequestDTO`
  
  ```json
  {
    "text": "Hello, world!",
    "sourceLang": "en",
    "targetLang": "zh",
    "articleId": 123
  }
  ```
- **响应**：`ApiResponse<TencentTranslateResponseDTO>`

#### 英文到中文翻译

```http
GET /api/ai/tencent-translate/en-to-zh?text={text}
```

- **使用场景**：英文内容快速翻译成中文
- **请求参数**：`text` - 要翻译的英文文本
- **响应**：`ApiResponse<TencentTranslateResponseDTO>`

#### 中文到英文翻译

```http
GET /api/ai/tencent-translate/zh-to-en?text={text}
```

- **使用场景**：中文内容快速翻译成英文
- **请求参数**：`text` - 要翻译的中文文本
- **响应**：`ApiResponse<TencentTranslateResponseDTO>`

#### 批量翻译

```http
POST /api/ai/tencent-translate/batch
```

- **使用场景**：批量翻译多个文本片段
- **请求参数**：
  - `texts[]` - 文本数组
  - `sourceLang` - 源语言
  - `targetLang` - 目标语言
- **响应**：`ApiResponse<TencentTranslateResponseDTO>`

### 6. AI分析服务 - 基础接口

> **使用场景**：适合系统集成和自动化处理场景。

#### 文章AI分析

```http
POST /api/ai/analyze
```

- **使用场景**：对单篇文章进行AI分析并保存结果到数据库
- **业务逻辑**：
  - 文章难度评估
  - 关键词提取
  - 中文翻译和摘要生成
  - 结果持久化存储
- **请求体**：`ArticleAnalysisRequest`
  
  ```json
  {
    "articleId": 123,
    "title": "文章标题",
    "content": "文章内容...",
    "category": "technology",
    "wordCount": 500
  }
  ```
- **响应**：`ApiResponse<ArticleAnalysisResponse>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "articleId": 123,
      "chineseTranslation": "中文翻译",
      "chineseSummary": "中文摘要",
      "keywords": "关键词1,关键词2",
      "createdTime": "2024-01-01 12:00:00",
      "updatedTime": "2024-01-01 12:00:00"
    }
  }
  ```

#### 批量AI分析

```http
POST /api/ai/analyze/batch
```

- **使用场景**：对多篇文章进行AI分析并保存结果到数据库
- **请求体**：`BatchAiAnalysisRequest`
  
  ```json
  {
    "articleIds": [123, 456, 789],
    "forceRegenerate": false
  }
  ```
- **响应**：`ApiResponse<List<ArticleAnalysisResponse>>`

#### 全文翻译

```http
POST /api/ai/translate/fulltext
```

- **使用场景**：英文内容完整翻译成中文并保存到数据库
- **请求体**：`TranslationRequest`
  
  ```json
  {
    "content": "文章内容...",
    "articleId": 123
  }
  ```
- **响应**：`ApiResponse<TranslationResponse>`

#### 选词翻译

```http
POST /api/ai/translate/word
```

- **使用场景**：提供单词的详细翻译和解释，并保存到数据库
- **请求体**：`WordTranslationRequest`
  
  ```json
  {
    "word": "technology",
    "context": "Technology is changing our lives.",
    "articleId": 123
  }
  ```
- **响应**：`ApiResponse<WordTranslationResponse>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "word": "technology",
      "chinese": "技术",
      "phonetic": "/tekˈnɒlədʒi/",
      "partOfSpeech": "名词",
      "definition": "the application of scientific knowledge",
      "chineseDefinition": "科学知识的应用",
      "example": "Modern technology has revolutionized communication.",
      "exampleChinese": "现代技术彻底改变了通讯方式。",
      "synonyms": ["technique", "method", "system"],
      "difficultyLevel": "B2"
    }
  }
  ```

#### 健康检查

```http
GET /api/ai/health
```

- **使用场景**：检查AI服务状态
- **响应**：`ApiResponse<String>`
  ```json
  {
    "code": 200,
    "success": true,
    "data": "AI服务运行正常✅"
  }
  ```体**：`WordTranslationRequest` (包含word和articleId字段)
- **响应**：`ApiResponse<WordTranslationResponse>` (详细翻译信息)

#### 健康检查

```http
GET /api/ai/health
```

- **描述**：检查AI服务状态
- **响应**：`ApiResponse<String>` (服务状态)

---

## 📚 文章服务 (article-service) - 7个接口

提供文章管理、发现和阅读功能。集成Gnews API和Readability4J实现内容提取。

### 1. 文章管理与阅读

> **使用场景**：适用于日常英语阅读学习，支持按难度、主题筛选。

#### 探索文章列表

```http
GET /api/article/explore
```

- **使用场景**：根据条件筛选和分页获取文章列表
- **业务逻辑**：
  - 支持多维度筛选（分类、难度、关键词）
  - 分页查询优化性能
  - 排序支持（时间、热度、难度）
- **请求参数**：`ArticleQueryDTO`
  
  ```http
  GET /api/article/explore?category=technology&difficulty=B2&page=1&size=10&sortBy=publishedAt&sortOrder=desc
  ```
- **响应**：`ApiResponse<PageResult<ArticleListVO>>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "records": [
        {
          "id": 123,
          "title": "文章标题",
          "description": "文章描述",
          "category": "technology",
          "difficultyLevel": "B2",
          "wordCount": 500,
          "readCount": 150,
          "publishedAt": "2024-01-01 10:00:00",
          "isFeatured": false
        }
      ],
      "total": 100,
      "current": 1,
      "size": 10,
      "pages": 10
    }
  }
  ```

#### 获取文章详情

```http
GET /api/article/read/{id}
```

- **使用场景**：获取文章详细信息并增加阅读次数
- **业务逻辑**：
  - 自动增加阅读计数
  - 返回完整文章内容（中英文对照）
  - 记录用户阅读行为
- **路径参数**：`id` - 文章ID
- **响应**：`ApiResponse<ArticleDetailVO>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "id": 123,
      "title": "文章标题",
      "contentEn": "英文内容...",
      "contentCn": "中文翻译...",
      "category": "technology",
      "difficultyLevel": "B2",
      "manualDifficulty": "B1",
      "wordCount": 500,
      "readCount": 151,
      "publishedAt": "2024-01-01 10:00:00",
      "source": "BBC News",
      "url": "https://example.com/article"
    }
  }
  ```

#### AI深度分析

```http
GET /api/article/{id}/deep-dive
```

- **使用场景**：对文章进行AI深度分析，包括难度评估、关键词提取、摘要生成等
- **业务逻辑**：
  - 调用AI服务进行综合分析
  - 生成增强文章详情
  - 缓存分析结果
- **路径参数**：`id` - 文章ID
- **响应**：`ApiResponse<ArticleDetailVO>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "id": 123,
      "title": "文章标题",
      "contentEn": "英文内容...",
      "contentCn": "中文翻译...",
      "aiAnalysis": {
        "summary": "AI生成的摘要",
        "keywords": "关键词1,关键词2",
        "difficultyLevel": "B2",
        "readabilityScore": 85.5
      },
      "enhancedFeatures": {
        "vocabularyHighlights": [重点词汇],
        "grammarPoints": [语法要点],
        "culturalNotes": [文化背景]
      }
    }
  }
  ```

#### 手动标注文章难度

```http
POST /api/article/feedback/difficulty
```

- **使用场景**：允许用户手动标注文章难度等级，优化AI难度评估
- **业务逻辑**：
  - 收集用户反馈数据
  - 优化难度评估算法
  - 支持A1-C2欧洲标准
- **请求体**：`ManualDifficultyDTO`
  
  ```json
  {
    "articleId": 123,
    "manualDifficulty": "B1" // A1, A2, B1, B2, C1, C2
  }
  ```
- **响应**：`ApiResponse<Boolean>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": true,
    "message": "难度标注成功"
  }
  ```

### 2. 内容发现与获取

> **使用场景**：内容的自动获取和管理，支持实时新闻和主题探索。

#### 按主题获取文章

```http
POST /api/article/discover/category
```

- **使用场景**：根据指定主题获取最新文章
- **业务逻辑**：
  - 调用Gnews API获取实时新闻
  - 自动解析和存储文章内容
  - 支持多种主题分类
- **请求参数**：
  - `category` - 主题名称
  - `limit` - 数量限制(默认10)
- **响应**：`ApiResponse<Object>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "category": "technology",
      "articles": [
        {
          "title": "新闻标题",
          "url": "https://example.com/article",
          "publishedAt": "2024-01-01 10:00:00",
          "source": "BBC News",
          "image": "https://example.com/image.jpg"
        }
      ],
      "totalFound": 15,
      "timestamp": "2024-01-01 12:00:00"
    }
  }
  ```

#### 获取热点文章

```http
POST /api/article/discover/trending
```

- **使用场景**：获取当前热点新闻文章
- **业务逻辑**：
  - 获取全球热点新闻
  - 按阅读量和热度排序
  - 自动筛选适合学习的内容
- **请求参数**：`limit` - 数量限制(默认10)
- **响应**：`ApiResponse<Object>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "trending": [
        {
          "title": "热点新闻标题",
          "url": "https://example.com/trending",
          "popularity": 95,
          "category": "world",
          "readingLevel": "intermediate"
        }
      ],
      "lastUpdated": "2024-01-01 12:00:00"
    }
  }
  ```

#### 从URL提取可读内容

```http
GET /api/article/extract-content?url={url}
```

- **使用场景**：使用Readability4J从URL提取文章的可读内容
- **业务逻辑**：
  - 自动解析网页结构
  - 提取正文内容
  - 过滤广告和无关信息
  - 支持多种网站结构
- **请求参数**：`url` - 文章URL
- **响应**：`ApiResponse<String>`
  ```json
  {
    "code": 200,
    "success": true,
    "data": "提取的文章可读内容...",
    "metadata": {
  
      "title": "文章标题",
      "author": "作者",
      "publishDate": "2024-01-01",
      "wordCount": 850,
      "readingTime": "3-4 minutes"
  
    }
  }
  ```difficulty字段)
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

## 👤 用户服务 (user-service) - 18个接口

提供用户管理、认证和三级词库功能。采用模块化设计，包括用户模块、订阅模块和词汇模块。

### 1. 用户认证与管理

> **使用场景**：用户注册登录、个人信息管理、学习进度跟踪。

#### 用户注册

```http
POST /api/user/register
```

- **使用场景**：新用户注册接口，支持个性化信息配置
- **业务逻辑**：
  - 用户名唯一性校验
  - 密码加密存储(BCrypt)
  - 自动创建用户档案
  - 初始化学习目标
- **请求体**：`UserRegisterRequest`
  
  ```json
  {
    "username": "learner123",
    "password": "securePassword123",
    "phone": "+86-13800138000",
    "interestTag": "technology,science", // 兴趣标签
    "identityTag": "kaoyan", // kaoyan-考研, cet4-四级, cet6-六级, workplace-职场
    "learningGoalWords": 3000, // 学习目标词汇量
    "targetReadingTime": 30 // 每日阅读目标时间(分钟)
  }
  ```
- **响应**：注册结果和用户信息
  
  ```json
  {
    "success": true,
    "message": "注册成功",
    "data": {
      "userId": 123,
      "username": "learner123"
    }
  }
  ```
- **验证规则**：
  - 用户名: 3-20字符，不能为空
  - 密码: 8-32字符，不能为空

#### 用户登录

```http
POST /api/user/login
```

- **使用场景**：用户登录验证接口，返回JWT Token
- **业务逻辑**：
  - 用户名和密码验证
  - 生成JWT Token
  - 记录登录时间和状态
  - 返回用户基本信息
- **请求体**：`UserLoginRequest`
  
  ```json
  {
    "username": "learner123",
    "password": "securePassword123"
  }
  ```
- **响应**：登录结果和JWT token
  
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "userInfo": {
        "id": 123,
        "username": "learner123",
        "phone": "+86-13800138000",
        "interestTag": "technology,science",
        "identityTag": "kaoyan",
        "learningGoalWords": 3000,
        "targetReadingTime": 30
      }
    }
  }
  ```

#### 加入生词本

```http
POST /api/user/vocabulary/add
```

- **使用场景**：一键收藏难词，打造专属词汇库
- **业务逻辑**：
  - 调用三级词库服务查询单词
  - 自动生成词汇信息(音标、释义、例句)
  - 支持上下文关联
  - 初始化复习计划
- **请求体**：`AddWordRequest`
  
  ```json
  {
    "userId": 123,
    "word": "technology",
    "context": "Technology is changing our lives.",
    "sourceArticleId": 456
  }
  ```
- **响应**：操作结果
  
  ```json
  {
    "code": 200,
    "message": "已加入生词本",
    "data": {
      "id": 789,
      "word": "technology",
      "meaning": "技术",
      "phonetic": "/tekˈnɒlədʒi/",
      "example": "Modern technology has revolutionized communication.",
      "difficulty": "B2",
      "reviewStatus": "new"
    }
  }
  ```

#### 查看生词本

```http
GET /api/user/vocabulary/my-words?userId={userId}
```

- **使用场景**：回顾你的词汇收藏，温故而知新
- **业务逻辑**：
  - 获取用户所有收藏词汇
  - 按添加时间或复习状态排序
  - 包含复习进度和下次复习时间
- **请求参数**：`userId` - 用户ID
- **响应**：词汇列表
  
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 789,
        "word": "technology",
        "meaning": "技术",
        "phonetic": "/tekˈnɒlədʒi/",
        "example": "Modern technology has revolutionized communication.",
        "context": "Technology is changing our lives.",
        "difficulty": "B2",
        "reviewStatus": "learning", // new, learning, mastered
        "lastReviewedAt": "2024-01-01 10:00:00",
        "nextReviewAt": "2024-01-03 10:00:00",
        "addedAt": "2024-01-01 08:00:00",
        "sourceArticleId": 456
      }
    ]
  }
  ```

#### 每日打卡

```http
POST /api/user/progress/check-in?userId={userId}
```

- **使用场景**：每日阅读打卡，积累学习成就
- **业务逻辑**：
  - 记录每日学习打卡
  - 计算连续学习天数
  - 更新学习成就统计
  - 支持断续后重新开始
- **请求参数**：`userId` - 用户ID
- **响应**：当前连续打卡天数
  
  ```json
  {
    "code": 200,
    "message": "打卡成功",
    "data": 7 // 连续7天学习
  }
  ```

### 2. 订阅管理 - 商业化功能

> **使用场景**：支持多级订阅套餐，提供差异化服务和使用限制。

#### 创建订阅

```http
POST /api/subscription/create
```

- **使用场景**：用户购买订阅套餐，升级会员服务
- **业务逻辑**：
  - 支持多种套餐类型：BASIC、PRO、ENTERPRISE
  - 自动计算价格和权益
  - 支持多种支付方式
  - 自动设置续费策略
- **请求参数**：
  - `userId` - 用户ID
  - `planType` - 套餐类型(BASIC/PRO/ENTERPRISE)
  - `paymentMethod` - 支付方式(ALIPAY/WECHAT/CREDIT_CARD)
- **响应**：订阅创建结果
  
  ```json
  {
    "success": true,
    "message": "订阅创建成功",
    "data": {
      "id": 456,
      "planType": "PRO",
      "price": 19.99,
      "currency": "USD",
      "status": "ACTIVE",
      "startDate": "2024-01-01 12:00:00",
      "endDate": "2024-02-01 12:00:00",
      "maxArticlesPerMonth": 50,
      "maxWordsPerArticle": 5000,
      "aiFeaturesEnabled": true,
      "autoRenew": true
    }
  }
  ```
- **套餐详情**：
  - **BASIC** ($9.99/月): 10篇文章, 1000词/篇, 基础功能
  - **PRO** ($19.99/月): 50篇文章, 5000词/篇, AI功能开启
  - **ENTERPRISE** ($49.99/月): 200篇文章, 20000词/篇, 全部功能+优先支持

#### 获取当前订阅

```http
GET /api/subscription/current/{userId}
```

- **使用场景**：获取用户当前激活的订阅信息
- **路径参数**：`userId` - 用户ID
- **响应**：当前订阅信息
  
  ```json
  {
    "success": true,
    "data": {
      "id": 456,
      "planType": "PRO",
      "status": "ACTIVE",
      "remainingDays": 15,
      "maxArticlesPerMonth": 50,
      "usedArticlesThisMonth": 23,
      "maxWordsPerArticle": 5000,
      "aiFeaturesEnabled": true,
      "nextBillingDate": "2024-02-01"
    }
  }
  ```

#### 获取订阅历史

```http
GET /api/subscription/history/{userId}
```

- **使用场景**：获取用户的订阅历史记录
- **路径参数**：`userId` - 用户ID
- **响应**：订阅历史列表
  
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 456,
        "planType": "PRO",
        "status": "ACTIVE",
        "startDate": "2024-01-01",
        "endDate": "2024-02-01",
        "price": 19.99,
        "paymentMethod": "ALIPAY"
      },
      {
        "id": 455,
        "planType": "BASIC",
        "status": "EXPIRED",
        "startDate": "2023-12-01",
        "endDate": "2024-01-01",
        "price": 9.99,
        "paymentMethod": "WECHAT"
      }
    ]
  }
  ```

#### 取消订阅

```http
POST /api/subscription/cancel/{subscriptionId}
```

- **使用场景**：取消用户的订阅
- **路径参数**：`subscriptionId` - 订阅ID
- **响应**：操作结果
  
  ```json
  {
    "success": true,
    "message": "订阅已取消"
  }
  ```

#### 检查使用限制

```http
GET /api/subscription/check-limit/{userId}?articlesCount={count}&wordsCount={words}
```

- **使用场景**：检查用户是否超出使用限制
- **路径参数**：`userId` - 用户ID
- **请求参数**：
  - `articlesCount` - 文章数量
  - `wordsCount` - 单词数量
- **响应**：是否在限制范围内
  
  ```json
  {
    "success": true,
    "withinLimit": true,
    "limits": {
      "maxArticlesPerMonth": 50,
      "currentArticlesCount": 23,
      "maxWordsPerArticle": 5000,
      "currentWordsCount": 1200
    }
  }
  ```

#### 获取剩余额度

```http
GET /api/subscription/quota/{userId}
```

- **使用场景**：获取用户剩余的使用额度
- **路径参数**：`userId` - 用户ID
- **响应**：剩余额度信息
  
  ```json
  {
    "success": true,
    "data": {
      "articlesQuota": {
        "total": 50,
        "used": 23,
        "remaining": 27,
        "resetDate": "2024-02-01"
      },
      "wordsQuota": {
        "perArticleLimit": 5000,
        "averageWordsUsed": 1200
      },
      "aiQuota": {
        "enabled": true,
        "unlimited": true
      }
    }
  }
  ```

### 3. 三级词库服务 - 智能词汇查询

> **使用场景**：项目核心功能，采用"本地缓存 + AI兴底"策略，实现智能词汇管理。

#### 三级词库策略原理

策略设计：

1. **第一级**：优先查当前用户词库 (响应时间: <10ms)
2. **第二级**：查询共享用户词库 (复用已有数据)
3. **第三级**：AI 生成兴底 + 异步缓存

#### 智能查询单词

```http
POST /api/vocabulary/lookup
```

- **使用场景**：三级词库策略：本地缓存优先，AI兴底生成，支持多用户共享
- **业务逻辑**：
  - 首先查询用户个人词库
  - 未命中则查询共享词库
  - 都未命中则调用AI服务生成
  - 异步缓存结果到本地词库
  - 支持多用户共享单词机制
- **请求体**：`LookupRequest`
  
  ```json
  {
    "word": "technology",
    "context": "Technology is changing our lives.",
    "userId": 123,
    "articleId": 456
  }
  ```
- **响应**：单词详细信息
  
  ```json
  {
    "code": 200,
    "message": "查询成功",
    "data": {
      "id": 789,
      "word": "technology",
      "meaning": "技术",
      "phonetic": "/tekˈnɒlədʒi/",
      "example": "Modern technology has revolutionized communication.",
      "context": "Technology is changing our lives.",
      "source": "ai", // local, ai, shared
      "difficulty": "B2",
      "reviewStatus": "new",
      "addedAt": "2024-01-01 12:00:00",
      "cacheHit": false, // 是否命中缓存
      "responseTime": 150
    }
  }
  ```

#### 批量智能查询单词

```http
POST /api/vocabulary/batch-lookup
```

- **使用场景**：批量智能查询多个单词，支持多用户共享词汇
- **业务逻辑**：
  - 并发查询多个单词
  - 批量AI生成优化
  - 统一缓存策略
- **请求体**：`BatchLookupRequest`
  
  ```json
  {
    "words": ["technology", "innovation", "artificial"],
    "context": "Technology and innovation drive artificial intelligence.",
    "userId": 123,
    "articleId": 456
  }
  ```
- **响应**：单词列表信息
  
  ```json
  {
    "code": 200,
    "message": "批量查询成功",
    "data": [
      {
        "word": "technology",
        "meaning": "技术",
        "cacheHit": true
      },
      {
        "word": "innovation",
        "meaning": "创新",
        "cacheHit": false
      }
    ],
    "totalCount": 3,
    "cacheHitRate": 0.67,
    "totalTime": 280
  }
  ```
  
  ```http
  POST /api/vocabulary/batch-lookup
  ```
- **描述**：批量智能查询多个单词，支持多用户共享词汇
- **请求体**：`BatchLookupRequest` (包含words列表、context、userId和articleId字段)
- **响应**：单词列表信息

#### 获取词库统计

```http
GET /api/vocabulary/stats/{userId}
```

- **描述**：获取用户的词汇学习统计信息，支持多用户共享单词的智能统计
- **路径参数**：userId (用户ID)
- **响应**：统计数据

#### 清理重复词汇

```http
POST /api/vocabulary/cleanup/{userId}
```

- **描述**：清理用户词库中的重复词汇，支持多用户共享单词的智能清理
- **路径参数**：userId (用户ID)
- **响应**：清理结果

#### 智能添加单词

```http
POST /api/vocabulary/add
```

- **描述**：智能添加单词到生词本，支持上下文
- **请求体**：`AddWordRequest` (包含word、context、userId和sourceArticleId字段)
- **响应**：添加的单词信息

#### 复习单词

```http
POST /api/vocabulary/review
```

- **描述**：更新单词的复习状态，支持多用户共享单词的权限验证
- **请求体**：`ReviewWordRequest` (包含wordId、userId和reviewStatus字段)
- **响应**：复习操作结果

#### 删除单词

```http
DELETE /api/vocabulary/{wordId}
```

- **描述**：从用户词库中删除单词（逻辑删除，从共享列表移除）
- **路径参数**：wordId (单词ID)
- **请求参数**：userId (用户ID)
- **响应**：删除操作结果

---

## 📊 报告服务 (report-service) - 9个接口

提供学习统计、报表生成和学习建议功能。采用艾宾浩斯复习算法，提供科学的学习进度跟踪。

### 1. 学习统计与报表

#### 词汇增长曲线

```http
GET /api/report/growth-curve?userId={userId}&days={days}
```

- **使用场景**：追踪你的词汇量成长轨迹，直观展示学习成果
- **业务逻辑**：按日统计新增词汇数量、分析难度分布趋势、计算学习速度
- **请求参数**：`userId` (用户ID)、`days` (统计天数，默认7)
- **响应**：`ApiResponse<VocabularyGrowthData>` (词汇增长数据)

#### 阅读时长统计

```http
GET /api/report/reading-time?userId={userId}
```

- **使用场景**：深度分析你的阅读习惯，提供个性化学习建议
- **业务逻辑**：统计阅读时长、分析习惯模式、计算阅读效率
- **请求参数**：`userId` (用户ID)
- **响应**：`ApiResponse<ReadingTimeData>` (阅读时长数据)

#### 记录阅读时长

```http
POST /api/report/reading-record
```

- **使用场景**：记录用户阅读文章的时长数据
- **业务逻辑**：保存用户阅读记录、计算阅读统计数据
- **请求体**：`ReadingRecordRequest`
  
  ```json
  {
    "userId": 123,
    "articleId": 456,
    "readTimeSec": 300
  }
  ```
- **响应**：`ApiResponse<String>`
  
  ```json
  {
    "code": 200,
    "message": "success",
    "success": true,
    "data": "阅读记录保存成功"
  }
  ```

#### 艾宾浩斯智能复习提醒

```http
GET /api/report/review-today?userId={userId}
```

- **使用场景**：艾宾洩斯智能复习提醒，科学安排复习计划
- **业务逻辑**：根据遗忘曲线计算复习时机、智能调整难度和频率
- **请求参数**：`userId` (用户ID)
- **响应**：`ApiResponse<List<ReviewWordDto>>` (今日待复习单词列表)

#### 今日学习日报

```http
GET /api/report/today/summary?userId={userId}
```

- **使用场景**：今日阅读成果一目了然，快速了解当日学习情况
- **请求参数**：`userId` (用户ID)
- **响应**：今日学习统计数据

#### 一周学习周报

```http
GET /api/report/weekly/insights?userId={userId}
```

- **使用场景**：本周学习成果深度分析，发现学习规律和问题
- **请求参数**：`userId` (用户ID)
- **响应**：一周学习趋势数据

#### 学习成就统计

```http
GET /api/report/streak/achievement?userId={userId}
```

- **使用场景**：见证你的坚持，记录每一个学习里程碑
- **请求参数**：`userId` (用户ID)
- **响应**：学习成就数据

#### 学习仪表盘

```http
GET /api/report/dashboard?userId={userId}
```

- **使用场景**：一站式查看所有学习数据，综合展示学习状态
- **业务逻辑**：整合词汇增长、阅读时长、复习计划等多维度数据
- **请求参数**：`userId` (用户ID)
- **响应**：`ApiResponse<DashboardData>` (综合学习数据仪表盘)

#### 健康检查

```http
GET /api/report/health
```

- **使用场景**：检查报表服务状态
- **响应**：`ApiResponse<String>` (服务健康状态)

---

## 🚦 API 访问地址

各服务的Swagger文档访问地址：

| 服务名称 | 访问地址                                  | 服务说明             |
| ---- | ------------------------------------- | ---------------- |
| AI服务 | http://localhost:8084/swagger-ui.html | 提供智能分析、翻译和AI助手功能 |
| 文章服务 | http://localhost:8082/swagger-ui.html | 管理文章内容、分类和阅读功能   |
| 报告服务 | http://localhost:8083/swagger-ui.html | 学习统计、报表生成和学习建议   |
| 用户服务 | http://localhost:8081/swagger-ui.html | 用户管理、认证和词汇学习     |

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

## 📊 技术架构与实现特点

### 微服务架构特点

- **服务注册与发现**：采用Nacos实现服务注册与配置管理
- **API网关**：使用Spring Cloud Gateway实现统一入口和负载均衡
- **数据一致性**：支持分布式事务，保证数据一致性
- **缓存策略**：采用Redis实现多级缓存，提高响应性能

### 核心功能亮点

1. **三级词库策略**：本地缓存 + 共享词库 + AI兜底，实现<10ms响应
2. **双引擎翻译**：DeepSeek(进阶) + 腾讯云(基础)，支持智能切换
3. **Function Calling**：AI助手支持动态调用工具函数
4. **艾宾浩斯复习**：科学复习算法，提高记忆效率
5. **商业化支持**：多级订阅套餐，灵活的使用限制

### 性能指标

- **响应时间**：本地词库查询 < 10ms，AI生成 < 200ms
- **缓存命中率**：目标 ≥ 90%，实际表现优秀
- **并发处理**：支持高并发请求，水平扩展
- **数据安全**：JWT认证 + 数据加密，保障用户隐私

---

## 🚀 开发指南

### 环境搭建

1. **基础环境**：Java 17+, Maven 3.8+, Node.js 18+
2. **数据库**：MySQL 8.0, Redis 6.0
3. **服务发现**：Nacos 2.0+
4. **开发工具**：IntelliJ IDEA, VS Code, Postman

### 快速启动

```bash
# 1. 启动基础设施
docker-compose up -d

# 2. 启动后端服务
.\test-all-services.ps1  # Windows
# 或手动启动各个服务

# 3. 启动前端
cd xreadup-frontend
npm install
npm run dev
```

### API调用示例

```javascript
// 用户登录
const loginResponse = await fetch('/api/user/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'learner123',
    password: 'securePassword123'
  })
});
const { token } = await loginResponse.json();

// 查询单词(三级词库)
const wordResponse = await fetch('/api/vocabulary/lookup', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    word: 'technology',
    context: 'Technology is changing our lives.',
    userId: 123,
    articleId: 456
  })
});
```

---

## 📝 最佳实践

### API调用建议

1. **限流控制**：高频调用建议加入本地限流
2. **错误处理**：始终检查`success`字段，合理处理异常
3. **参数验证**：前端验证 + 后端验证，双重保障
4. **缓存优化**：合理使用缓存，减少不必要的API调用

### 安全注意事项

- **Token管理**：定期刷新JWT Token，避免过期
- **输入验证**：严格验证用户输入，防止注入攻击
- **数据脱敏**：不要在日志中记录敏感信息
- **权限控制**：严格遵循最小权限原则

---

## ❓ 常见问题解答

### Q: 为什么要采用三级词库策略？

**A:** 三级词库策略的优势：

- **性能优化**：本地查询 < 10ms，显著提升用户体验
- **成本控制**：减少AI调用次数，降低运营成本
- **数据共享**：多用户共享单词数据，提高数据价值
- **可靠性**：离线可用，不依赖外部API服务

### Q: Function Calling如何工作？

**A:** Function Calling实现原理：

1. 用户提问"请解释单词technology"
2. AI检测到需要查询单词，调用`lookup_word`函数
3. 系统自动执行对应的API接口
4. 将结果返回AI，生成综合回答

### Q: 艾宾浩斯复习算法的复习间隔是怎样计算的？

**A:** 复习间隔计算公式：

- **初次学习**：1天后复习
- **第二次**：3天后复习  
- **第三次**：7天后复习
- **第四次**：15天后复习
- **第五次**：31天后复习
- 根据复习效果动态调整间隔

### Q: 如何进行性能优化？

**A:** 性能优化建议：

- **批量处理**：使用`batch-lookup`接口批量查询单词
- **缓存利用**：合理设置缓存时间，提高命中率
- **异步处理**：非关键操作采用异步处理
- **连接池**：复用HTTP连接，减少连接开销

---

## 📈 版本更新记录

### v2.2.0 (2024-01-07) - 当前版本

- ✨ **新增功能**：完成三级词库策略升级
- ✨ **新增功能**：Function Calling支持，增强AI交互能力
- ✨ **新增功能**：艾宾浩斯智能复习系统
- 📝 **文档优化**：统一Knife4j文档配置，完善API文档
- 🚀 **性能优化**：词库查询响应时间优化至 < 10ms

### v2.1.0 (2023-12-15)

- ✨ **新增功能**：DeepSeek AI集成，双引擎翻译策略
- ✨ **新增功能**：商业化订阅系统，支持多级套餐
- 🐛 **问题修复**：修复多用户并发词库查询问题

### v2.0.0 (2023-11-20)

- ✨ **重大更新**：升级为三级词库策略架构
- ✨ **新增功能**：报告服务，学习数据可视化
- 🔧 **架构优化**：引入Spring Cloud微服务架构

### v1.5.0 (2023-10-15)

- ✨ **新增功能**：整合Nacos服务注册发现
- 🚀 **性能优化**：优化API路由和负载均衡

### v1.0.0 (2023-09-01)

- 🎉 **首次发布**：基础API接口实现，核心功能上线
- ✨ **核心功能**：用户认证、文章管理、词汇学习基础功能

---

## 🔧 管理员服务 (admin-service) - 5个接口

提供系统配置管理、管理员权限控制和后台管理功能。支持动态配置更新和权限验证。

### 1. 管理员权限管理

> **使用场景**：管理员身份验证、权限检查和用户管理。

#### 检查管理员权限

```http
GET /api/admin/check?userId={userId}
```

- **使用场景**：验证用户是否具有管理员权限
- **业务逻辑**：检查用户ID是否在管理员列表中，返回权限级别
- **请求参数**：`userId` - 用户ID
- **响应**：`ApiResponse<AdminCheckDTO>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": {
      "admin": true,
      "role": "SUPER_ADMIN",
      "userId": 123
    }
  }
  ```

#### 获取管理员详情

```http
GET /api/admin/detail?userId={userId}
```

- **使用场景**：获取管理员用户的详细信息
- **请求参数**：`userId` - 用户ID
- **响应**：`ApiResponse<AdminUserDTO>`

### 2. 系统配置管理

> **使用场景**：动态配置系统参数、功能开关和业务限制。

#### 获取所有系统配置

```http
GET /api/admin/system-config/all
```

- **使用场景**：获取系统中所有配置项
- **业务逻辑**：返回按分类组织的配置列表
- **响应**：`ApiResponse<List<SystemConfigDTO>>`
  
  ```json
  {
    "code": 200,
    "success": true,
    "data": [
      {
        "configKey": "maintenance.enabled",
        "configValue": "false",
        "configType": "BOOLEAN",
        "description": "系统维护模式开关",
        "category": "MAINTENANCE"
      }
    ]
  }
  ```

#### 更新系统配置

```http
POST /api/admin/system-config/update
```

- **使用场景**：更新系统配置参数
- **请求体**：`SystemConfigUpdateRequest`
  
  ```json
  {
    "configKey": "maintenance.enabled",
    "configValue": "true"
  }
  ```
- **响应**：`ApiResponse<Boolean>`

#### 获取配置值

```http
GET /api/admin/system-config/value/{configKey}
```

- **使用场景**：获取指定配置项的值
- **路径参数**：`configKey` - 配置键
- **响应**：`ApiResponse<String>`

### 3. 用户管理

> **使用场景**：管理员查看和管理普通用户。

#### 获取用户列表

```http
GET /api/admin/users/list?page={page}&pageSize={pageSize}&keyword={keyword}
```

- **使用场景**：管理员查看用户列表
- **请求参数**：
  - `page` - 页码（默认1）
  - `pageSize` - 每页大小（默认10）
  - `keyword` - 搜索关键字（可选）
- **响应**：`ApiResponse<List<UserDTO>>`

### 4. 管理员用户管理

> **使用场景**：超级管理员管理其他管理员用户。

#### 添加管理员用户

```http
POST /api/admin/admins/add
```

- **使用场景**：将普通用户提升为管理员
- **请求体**：`AdminUserCreateDTO`
  
  ```json
  {
    "userId": 123,
    "role": "ADMIN",
    "phone": "+86-13800138000"
  }
  ```
- **响应**：`ApiResponse<Boolean>`

### 5. 健康检查

```http
GET /api/admin/health
```

- **使用场景**：检查管理员服务状态
- **响应**：`ApiResponse<String>`
  ```json
  {
    "code": 200,
    "success": true,
    "data": "管理员服务运行正常✅"
  }
  ```

---

## 📧 技术支持与联系方式

如有任何技术问题或业务合作需求，请通过以下方式联系我们：

- **项目地址**：[GitHub Repository](https://github.com/xreadup/xreadup-ai)
- **技术文档**：完整的开发文档和部署指南
- **问题反馈**：通过GitHub Issues提交问题和建议
- **社区讨论**：加入我们的开发者社区，参与技术讨论

**XReadUp团队** - 专注于AI驱动的英语学习解决方案

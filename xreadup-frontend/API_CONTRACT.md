# XReadUp Frontend API 合约文档

<div align="center">

![API Contract](https://img.shields.io/badge/API_Contract-Frontend_Backend-blue.svg)
![Version](https://img.shields.io/badge/Version-1.0.43-green.svg)
![Status](https://img.shields.io/badge/Status-Production_Ready-success.svg)

**前端与后端服务API接口合约规范**

</div>

## 📋 文档概述

本文档详细描述了 XReadUp 前端与后端服务之间的 API 接口合约，基于前端 `src/utils/api.ts` 和 `src/api/admin/adminApi.ts` 文件的实际实现。本合约定义了前端调用的所有 API 端点、请求参数、响应格式以及错误处理机制，旨在确保前后端协作的一致性和可预测性。

## 🔧 API 基础规范

### 通信协议
- **协议**: HTTP/HTTPS
- **数据格式**: JSON
- **字符编码**: UTF-8
- **基础路径**: `/` (通过Vite代理转发)

### 请求规范
- **请求头**: `Content-Type: application/json`
- **认证方式**: JWT Token (`Authorization: Bearer {token}`)
- **超时设置**: 60秒 (600000毫秒)
- **请求方法**: GET, POST, PUT, DELETE

### 响应规范
- **统一响应格式**: `ApiResponse<T>`
- **响应拦截**: 自动提取 `response.data`
- **成功响应**: 包含 `code`, `success`, `data` 字段
- **失败响应**: 包含 `code`, `success`, `message` 字段

### 认证与授权
- **用户认证**: JWT Token存储在localStorage
- **管理员认证**: 独立的管理员Token管理
- **Token验证**: 请求拦截器自动检查Token有效性
- **自动重定向**: Token过期时自动跳转到登录页

### 错误处理
- **统一错误处理**: 请求/响应拦截器
- **状态码处理**: 401自动重定向，403权限提示
- **用户友好提示**: Element Plus Message组件
- **错误日志**: 详细错误信息记录

## 🎯 核心API模块

### 1. 用户端API模块

#### 1.1 文章服务 (articleApi)

**获取文章列表**
```typescript
GET /api/article/explore
Parameters: {
  pageNum?: number,      // 页码，默认1
  pageSize?: number,     // 每页数量，默认10
  category?: string,     // 文章分类
  difficulty?: string,   // 难度等级
  keyword?: string       // 搜索关键词
}
Response: {
  code: 200,
  success: true,
  data: {
    articles: Article[],
    total: number,
    pageNum: number,
    pageSize: number
  }
}
```

**获取文章详情**
```typescript
GET /api/article/read/{id}
Parameters: {
  id: string            // 文章ID
}
Response: {
  code: 200,
  success: true,
  data: {
    id: string,
    title: string,
    content: string,
    translation: string,
    difficulty: string,
    category: string,
    publishDate: string,
    readingTime: number,
    wordCount: number
  }
}
```

**获取分类文章**
```typescript
GET /api/article/discover/category/{category}
Parameters: {
  category: string      // 文章分类
}
Response: {
  code: 200,
  success: true,
  data: Article[]
}
```

**获取热点文章**
```typescript
GET /api/article/discover/trending
Response: {
  code: 200,
  success: true,
  data: Article[]
}
```

#### 1.2 AI功能服务 (aiApi)

**全文翻译**
```typescript
POST /api/ai/translate
Request: {
  text: string,           // 要翻译的文本
  targetLanguage: string  // 目标语言，默认'zh'
}
Response: {
  code: 200,
  success: true,
  data: {
    originalText: string,
    translatedText: string,
    confidence: number,
    translationMethod: string
  }
}
```

**单词翻译**
```typescript
POST /api/ai/translate/word
Request: {
  word: string           // 要翻译的单词
}
Response: {
  code: 200,
  success: true,
  data: {
    word: string,
    meaning: string,
    phonetic: string,
    difficulty: string,
    example: string
  }
}
```

**生成文章摘要**
```typescript
POST /api/ai/summary
Request: {
  articleId: string      // 文章ID
}
Response: {
  code: 200,
  success: true,
  data: {
    summary: string,
    keyPoints: string[],
    difficulty: string
  }
}
```

**AI助手对话**
```typescript
POST /api/ai/assistant/chat
Request: {
  question: string,      // 用户问题
  context?: string,      // 上下文信息
  userId: number         // 用户ID
}
Response: {
  code: 200,
  success: true,
  data: {
    response: string,
    functionCalls?: FunctionCall[],
    timestamp: string
  }
}
```

**生成测验题目**
```typescript
POST /api/ai/quiz/generate
Request: {
  articleId: string      // 文章ID
}
Response: {
  code: 200,
  success: true,
  data: {
    questions: QuizQuestion[],
    difficulty: string,
    estimatedTime: number
  }
}
```

#### 1.3 词汇管理服务 (vocabularyApi)

**添加生词**
```typescript
POST /api/user/word/add
Request: {
  word: string,         // 单词
  meaning: string,      // 释义
  userId: number        // 用户ID
}
Response: {
  code: 200,
  success: true,
  data: {
    id: number,
    word: string,
    meaning: string,
    addedAt: string
  }
}
```

**获取用户生词列表**
```typescript
GET /api/user/word/list/{userId}
Parameters: {
  userId: number,       // 用户ID
  pageNum?: number,     // 页码
  pageSize?: number,    // 每页数量
  status?: string       // 学习状态
}
Response: {
  code: 200,
  success: true,
  data: {
    words: UserWord[],
    total: number,
    pageNum: number,
    pageSize: number
  }
}
```

**批量词汇查询**
```typescript
POST /api/vocabulary/batch-lookup
Request: {
  words: string[]       // 单词列表
}
Response: {
  code: 200,
  success: true,
  data: VocabularyItem[]
}
```

**智能词汇查询**
```typescript
POST /api/vocabulary/lookup
Request: {
  word: string,         // 单词
  context?: string,      // 上下文
  userId: number,       // 用户ID
  articleId?: number     // 文章ID
}
Response: {
  code: 200,
  success: true,
  data: {
    id: number,
    word: string,
    meaning: string,
    example: string,
    phonetic: string,
    difficulty: string,
    source: string,
    context: string
  }
}
```

#### 1.4 学习记录服务 (learningApi)

**记录阅读时长**
```typescript
POST /api/learning/reading-time
Request: {
  articleId: string,     // 文章ID
  readingTime: number   // 阅读时长(分钟)
}
Response: {
  code: 200,
  success: true,
  data: {
    recorded: boolean,
    totalTime: number
  }
}
```

**每日打卡**
```typescript
POST /api/learning/check-in
Response: {
  code: 200,
  success: true,
  data: {
    checkedIn: boolean,
    streakDays: number,
    totalDays: number
  }
}
```

**获取今日学习小结**
```typescript
GET /api/learning/today/summary
Response: {
  code: 200,
  success: true,
  data: {
    studyTime: number,      // 学习时长(分钟)
    wordsLearned: number,   // 学习单词数
    articlesRead: number,   // 阅读文章数
    hasCheckedInToday: boolean, // 是否已打卡
    streakDays: number      // 连续打卡天数
  }
}
```

#### 1.5 学习报告服务 (reportApi)

**获取仪表盘数据**
```typescript
GET /api/report/dashboard
Response: {
  code: 200,
  success: true,
  data: {
    totalWords: number,     // 总词汇量
    totalReadingTime: number, // 总阅读时长
    streakDays: number,     // 连续打卡天数
    weeklyProgress: number, // 周进度
    monthlyProgress: number // 月进度
  }
}
```

**获取词汇增长曲线**
```typescript
GET /api/report/growth-curve
Parameters: {
  period?: string          // 时间周期: 'week', 'month', 'year'
}
Response: {
  code: 200,
  success: true,
  data: {
    dates: string[],
    vocabularyCounts: number[],
    growthRate: number
  }
}
```

**获取阅读时长统计**
```typescript
GET /api/report/reading-time
Parameters: {
  period?: string          // 时间周期
}
Response: {
  code: 200,
  success: true,
  data: {
    dates: string[],
    readingTimes: number[],
    averageTime: number
  }
}
```

**获取今日学习小结**
```typescript
GET /api/report/today/summary
Response: {
  code: 200,
  success: true,
  data: {
    studyTime: number,
    wordsLearned: number,
    articlesRead: number,
    achievements: Achievement[]
  }
}
```

#### 1.6 订阅管理服务 (subscriptionApi)

**获取订阅计划**
```typescript
GET /api/subscription/plans
Response: {
  code: 200,
  success: true,
  data: {
    FREE: {
      priceCny: 0,
      maxArticlesPerMonth: 30,
      maxWordsPerArticle: 2000,
      aiFeaturesEnabled: false,
      prioritySupport: false
    },
    BASIC: {
      priceCny: 29,
      maxArticlesPerMonth: 100,
      maxWordsPerArticle: 5000,
      aiFeaturesEnabled: true,
      prioritySupport: false
    },
    PRO: {
      priceCny: 59,
      maxArticlesPerMonth: 300,
      maxWordsPerArticle: 10000,
      aiFeaturesEnabled: true,
      prioritySupport: true
    },
    ENTERPRISE: {
      priceCny: 199,
      maxArticlesPerMonth: 1000,
      maxWordsPerArticle: 20000,
      aiFeaturesEnabled: true,
      prioritySupport: true
    }
  }
}
```

**获取用户订阅信息**
```typescript
GET /api/user/subscription
Response: {
  code: 200,
  success: true,
  data: {
    planType: string,      // 'FREE', 'BASIC', 'PRO', 'ENTERPRISE'
    status: string,        // 'ACTIVE', 'CANCELLED', 'EXPIRED'
    startDate: string,
    endDate: string,
    price: number,
    maxArticlesPerMonth: number
  }
}
```

**获取用户使用情况**
```typescript
GET /api/user/usage
Response: {
  code: 200,
  success: true,
  data: {
    articlesQuota: {
      used: number,
      total: number,
      resetDate: string
    },
    wordsQuota: {
      used: number,
      total: number
    },
    aiCalls: {
      used: number,
      total: number
    }
  }
}
```

### 2. 管理员端API模块

#### 2.1 管理员认证服务

**管理员登录**
```typescript
POST /api/admin/login
Request: {
  username: string,      // 管理员用户名
  password: string       // 密码
}
Response: {
  code: 200,
  success: true,
  data: {
    token: string,
    userId: number,
    role: string,        // 'ADMIN', 'SUPER_ADMIN'
    permissions: string[],
    sessionExpiredAt: string
  }
}
```

**检查管理员状态**
```typescript
GET /api/admin/check
Parameters: {
  userId?: string | number  // 用户ID
}
Response: {
  code: 200,
  success: true,
  data: {
    isAdmin: boolean,
    admin?: boolean,
    role?: string,
    userId?: string | number
  }
}
```

#### 2.2 用户管理服务

**获取用户列表**
```typescript
GET /api/admin/users
Parameters: {
  pageNum?: number,      // 页码
  pageSize?: number,     // 每页数量
  keyword?: string,      // 搜索关键词
  status?: string        // 用户状态
}
Response: {
  code: 200,
  success: true,
  data: {
    users: AdminUser[],
    total: number,
    pageNum: number,
    pageSize: number
  }
}
```

**更新用户信息**
```typescript
PUT /api/admin/users/{userId}
Request: {
  username?: string,
  email?: string,
  nickname?: string,
  status?: string
}
Response: {
  code: 200,
  success: true,
  data: {
    updated: boolean,
    user: AdminUser
  }
}
```

**禁用用户**
```typescript
PUT /api/admin/users/{userId}/disable
Response: {
  code: 200,
  success: true,
  data: {
    disabled: boolean,
    message: string
  }
}
```

**启用用户**
```typescript
PUT /api/admin/users/{userId}/enable
Response: {
  code: 200,
  success: true,
  data: {
    enabled: boolean,
    message: string
  }
}
```

#### 2.3 文章管理服务

**获取文章列表**
```typescript
GET /api/admin/articles
Parameters: {
  pageNum?: number,
  pageSize?: number,
  keyword?: string,
  status?: string,
  category?: string
}
Response: {
  code: 200,
  success: true,
  data: {
    articles: AdminArticle[],
    total: number,
    pageNum: number,
    pageSize: number
  }
}
```

**获取文章详情**
```typescript
GET /api/admin/articles/{articleId}
Response: {
  code: 200,
  success: true,
  data: {
    id: string,
    title: string,
    content: string,
    translation: string,
    status: string,
    publishDate: string,
    viewCount: number,
    aiAnalysis: any
  }
}
```

**删除文章**
```typescript
DELETE /api/admin/articles/{articleId}
Response: {
  code: 200,
  success: true,
  data: {
    deleted: boolean,
    message: string
  }
}
```

**发布文章**
```typescript
PUT /api/admin/articles/{articleId}/publish
Response: {
  code: 200,
  success: true,
  data: {
    published: boolean,
    message: string
  }
}
```

#### 2.4 订阅管理服务

**获取订阅计划列表**
```typescript
GET /api/admin/subscription/plans
Response: {
  code: 200,
  success: true,
  data: SubscriptionPlan[]
}
```

**创建订阅计划**
```typescript
POST /api/admin/subscription/plans
Request: {
  planType: string,
  name: string,
  price: number,
  currency: string,
  duration: string,
  maxArticles: number,
  maxWords: number,
  aiFeatures: boolean,
  prioritySupport: boolean
}
Response: {
  code: 200,
  success: true,
  data: {
    created: boolean,
    plan: SubscriptionPlan
  }
}
```

**更新订阅计划**
```typescript
PUT /api/admin/subscription/plans/{planId}
Request: {
  name?: string,
  price?: number,
  maxArticles?: number,
  maxWords?: number,
  aiFeatures?: boolean,
  prioritySupport?: boolean
}
Response: {
  code: 200,
  success: true,
  data: {
    updated: boolean,
    plan: SubscriptionPlan
  }
}
```

**获取用户订阅列表**
```typescript
GET /api/admin/subscription/users
Parameters: {
  pageNum?: number,
  pageSize?: number,
  planType?: string,
  status?: string
}
Response: {
  code: 200,
  success: true,
  data: {
    subscriptions: UserSubscription[],
    total: number,
    pageNum: number,
    pageSize: number
  }
}
```

**更新用户订阅状态**
```typescript
PUT /api/admin/subscription/users/{subscriptionId}/status
Request: {
  status: string        // 'ACTIVE', 'CANCELLED', 'EXPIRED'
}
Response: {
  code: 200,
  success: true,
  data: {
    updated: boolean,
    subscription: UserSubscription
  }
}
```

#### 2.5 系统配置服务

**获取系统配置**
```typescript
GET /api/admin/system-config/all
Response: {
  code: 200,
  success: true,
  data: {
    MAINTENANCE: {
      'system.maintenance.mode': string,
      'system.maintenance.message': string
    },
    FEATURE: {
      'feature.ai.enabled': string,
      'feature.tts.enabled': string,
      'feature.quiz.enabled': string
    },
    LIMIT: {
      'limit.free.articles': string,
      'limit.free.words': string,
      'limit.basic.articles': string,
      'limit.basic.words': string
    }
  }
}
```

**更新系统配置**
```typescript
PUT /api/admin/system-config
Request: {
  configKey: string,     // 配置键
  configValue: string    // 配置值
}
Response: {
  code: 200,
  success: true,
  data: {
    updated: boolean,
    message: string
  }
}
```

**批量更新系统配置**
```typescript
PUT /api/admin/system-config/batch
Request: {
  configs: Record<string, string>  // 配置键值对
}
Response: {
  code: 200,
  success: true,
  data: {
    updated: boolean,
    updatedCount: number,
    message: string
  }
}
```

#### 2.6 AI分析服务

**获取AI分析列表**
```typescript
GET /api/admin/ai-analysis
Parameters: {
  pageNum?: number,
  pageSize?: number,
  type?: string,
  status?: string
}
Response: {
  code: 200,
  success: true,
  data: {
    analyses: AIAnalysis[],
    total: number,
    pageNum: number,
    pageSize: number
  }
}
```

**获取AI分析详情**
```typescript
GET /api/admin/ai-analysis/{analysisId}
Response: {
  code: 200,
  success: true,
  data: {
    id: string,
    type: string,
    input: string,
    output: string,
    status: string,
    createdAt: string,
    processingTime: number
  }
}
```

#### 2.7 管理员管理服务

**获取管理员列表**
```typescript
GET /api/admin/admins
Parameters: {
  pageNum?: number,
  pageSize?: number,
  role?: string
}
Response: {
  code: 200,
  success: true,
  data: {
    admins: AdminUser[],
    total: number,
    pageNum: number,
    pageSize: number
  }
}
```

**添加管理员**
```typescript
POST /api/admin/admins
Request: {
  username: string,
  password: string,
  role: string,         // 'ADMIN', 'SUPER_ADMIN'
  permissions: string[]
}
Response: {
  code: 200,
  success: true,
  data: {
    created: boolean,
    admin: AdminUser
  }
}
```

**更新管理员角色**
```typescript
PUT /api/admin/admins/{userId}/role
Request: {
  role: string          // 'ADMIN', 'SUPER_ADMIN'
}
Response: {
  code: 200,
  success: true,
  data: {
    updated: boolean,
    admin: AdminUser
  }
}
```

**删除管理员**
```typescript
DELETE /api/admin/admins/{userId}
Response: {
  code: 200,
  success: true,
  data: {
    deleted: boolean,
    message: string
  }
}
```

#### 2.8 系统监控服务

**获取管理员统计**
```typescript
GET /api/admin/stats
Response: {
  code: 200,
  success: true,
  data: {
    totalUsers: number,
    totalArticles: number,
    totalSubscriptions: number,
    totalAICalls: number,
    activeUsers: number,
    newUsersToday: number
  }
}
```

**获取数据趋势**
```typescript
GET /api/admin/trends
Parameters: {
  period?: string       // 'week', 'month', 'year'
}
Response: {
  code: 200,
  success: true,
  data: {
    userGrowth: TrendData[],
    articleViews: TrendData[],
    aiUsage: TrendData[],
    subscriptionRevenue: TrendData[]
  }
}
```

**获取最近活动**
```typescript
GET /api/admin/activities
Parameters: {
  limit?: number        // 限制数量
}
Response: {
  code: 200,
  success: true,
  data: {
    activities: Activity[],
    total: number
  }
}
```

## 🔄 智能Token管理

### 双Token系统

#### 用户Token管理
```typescript
// 用户Token存储
const userToken = localStorage.getItem('token')
const tokenExpiry = localStorage.getItem('tokenExpiry')

// Token有效性检查
const isTokenValid = (): boolean => {
  const token = localStorage.getItem('token')
  const tokenExpiry = localStorage.getItem('tokenExpiry')
  const currentTime = Date.now()
  
  return token && tokenExpiry && parseInt(tokenExpiry) > currentTime
}

// 自动Token清理
const clearExpiredToken = (): void => {
  const tokenExpiry = localStorage.getItem('tokenExpiry')
  const currentTime = Date.now()
  
  if (tokenExpiry && parseInt(tokenExpiry) < currentTime) {
    localStorage.removeItem('token')
    localStorage.removeItem('tokenExpiry')
    localStorage.removeItem('user')
  }
}
```

#### 管理员Token管理
```typescript
// 管理员Token存储
const adminToken = localStorage.getItem('admin_token')
const adminSession = localStorage.getItem('adminSession')

// 管理员会话检查
const isAdminSessionValid = (): boolean => {
  const adminSession = localStorage.getItem('adminSession')
  if (!adminSession) return false
  
  const session = JSON.parse(adminSession)
  const currentTime = Date.now()
  
  return session.expiredAt && new Date(session.expiredAt).getTime() > currentTime
}

// 管理员会话延长
const extendAdminSession = (): void => {
  const adminSession = localStorage.getItem('adminSession')
  if (adminSession) {
    const session = JSON.parse(adminSession)
    session.expiredAt = new Date(Date.now() + 4 * 60 * 60 * 1000).toISOString()
    localStorage.setItem('adminSession', JSON.stringify(session))
  }
}
```

### 请求拦截器

```typescript
// 智能Token管理请求拦截器
api.interceptors.request.use(
  (config) => {
    // 判断是否是管理员API请求
    const isAdminApi = config.url?.startsWith('/api/admin/') || 
                      config.url?.startsWith('/api/user/login') && config.data?.isAdminLogin
    
    // 检查是否需要跳过token验证
    const shouldSkipToken = config.url === '/api/admin/check'
    
    if (isAdminApi && !shouldSkipToken) {
      // 管理员API：使用管理员token
      const adminToken = localStorage.getItem('admin_token')
      if (adminToken) {
        config.headers.Authorization = `Bearer ${adminToken}`
      }
    } else {
      // 普通用户API：使用普通token
      const token = localStorage.getItem('token')
      const tokenExpiry = localStorage.getItem('tokenExpiry')
      const currentTime = Date.now()

      // 检查token是否已过期
      if (token && tokenExpiry && parseInt(tokenExpiry) < currentTime) {
        console.warn('Token已过期，将自动清除登录状态')
        localStorage.removeItem('token')
        localStorage.removeItem('tokenExpiry')
        localStorage.removeItem('user')
      } else if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)
```

## ⚠️ 错误处理机制

### 响应拦截器

```typescript
// 智能错误处理响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    // 处理401未授权错误
    if (error.response?.status === 401) {
      // 判断是管理员API还是普通用户API
      const isAdminApi = error.config?.url?.startsWith('/api/admin/')
      
      if (isAdminApi) {
        // 管理员API：清除管理员登录信息，重定向到管理员登录页
        localStorage.removeItem('adminSession')
        localStorage.removeItem('admin_token')
        if (window.location.pathname !== '/admin/login') {
          window.location.href = '/admin/login'
        }
      } else {
        // 普通用户API：清除用户登录信息，重定向到用户登录页
        localStorage.removeItem('token')
        localStorage.removeItem('tokenExpiry')
        localStorage.removeItem('user')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
    }
    
    // 处理403禁止访问错误
    if (error.response?.status === 403) {
      ElMessage.error('权限不足，无法访问此功能')
    }
    
    // 处理500服务器错误
    if (error.response?.status === 500) {
      ElMessage.error('服务器错误，请稍后重试')
    }
    
    // 处理网络错误
    if (!error.response) {
      ElMessage.error('网络连接失败，请检查网络设置')
    }
    
    return Promise.reject(error)
  }
)
```

### 错误类型定义

```typescript
// API错误类型
interface ApiError {
  code: number
  message: string
  details?: any
  timestamp: string
}

// 网络错误类型
interface NetworkError {
  message: string
  status?: number
  config?: any
}

// 业务错误类型
interface BusinessError {
  code: string
  message: string
  field?: string
  value?: any
}
```

## 📊 数据类型定义

### 基础数据类型

```typescript
// 用户信息
interface User {
  id: number
  username: string
  email?: string
  nickname?: string
  avatar?: string
  createdAt: string
  updatedAt: string
  status: 'ACTIVE' | 'INACTIVE' | 'BANNED'
}

// 文章信息
interface Article {
  id: string
  title: string
  content: string
  translation: string
  difficulty: 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2'
  category: string
  publishDate: string
  readingTime: number
  wordCount: number
  viewCount: number
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
}

// 词汇信息
interface VocabularyItem {
  id: number
  word: string
  meaning: string
  phonetic: string
  difficulty: string
  example: string
  source: 'ai' | 'dictionary' | 'user'
  context?: string
}

// 订阅信息
interface Subscription {
  id: string
  userId: number
  planType: 'FREE' | 'BASIC' | 'PRO' | 'ENTERPRISE'
  status: 'ACTIVE' | 'CANCELLED' | 'EXPIRED'
  startDate: string
  endDate: string
  price: number
  maxArticlesPerMonth: number
  maxWordsPerArticle: number
  aiFeaturesEnabled: boolean
  prioritySupport: boolean
}

// 学习报告
interface LearningReport {
  totalWords: number
  totalReadingTime: number
  streakDays: number
  weeklyProgress: number
  monthlyProgress: number
  achievements: Achievement[]
}

// 成就信息
interface Achievement {
  id: string
  name: string
  description: string
  icon: string
  unlockedAt: string
  progress: number
  maxProgress: number
}
```

### 管理员数据类型

```typescript
// 管理员用户
interface AdminUser {
  id: number
  username: string
  email?: string
  role: 'ADMIN' | 'SUPER_ADMIN'
  permissions: AdminPermission[]
  status: 'ACTIVE' | 'INACTIVE'
  createdAt: string
  lastLoginAt: string
}

// 管理员权限
type AdminPermission = 
  | 'USER_MANAGE'
  | 'ARTICLE_MANAGE'
  | 'SUBSCRIPTION_MANAGE'
  | 'AI_RESULT_MANAGE'
  | 'ADMIN_MANAGE'
  | 'SYSTEM_CONFIG'

// 管理员会话
interface AdminSession {
  userId: number
  username: string
  role: string
  permissions: string[]
  expiredAt: string
  lastLoginTime: string
}

// 系统配置
interface SystemConfig {
  category: string
  configs: Record<string, string>
}

// AI分析结果
interface AIAnalysis {
  id: string
  type: 'TRANSLATION' | 'SUMMARY' | 'QUIZ' | 'CHAT'
  input: string
  output: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED'
  createdAt: string
  processingTime: number
  userId: number
}

// 活动记录
interface Activity {
  id: string
  type: 'USER_LOGIN' | 'USER_REGISTER' | 'ARTICLE_READ' | 'WORD_LEARNED' | 'SUBSCRIPTION_CREATED'
  userId: number
  description: string
  metadata: any
  createdAt: string
}
```

## 🔧 API使用示例

### 用户端API使用

```typescript
// 文章阅读流程
const readArticle = async (articleId: string) => {
  try {
    // 1. 获取文章详情
    const article = await articleApi.getArticle(articleId)
    
    // 2. 记录阅读开始时间
    const startTime = Date.now()
    
    // 3. 用户阅读文章...
    
    // 4. 记录阅读时长
    const readingTime = Math.floor((Date.now() - startTime) / 60000) // 分钟
    await learningApi.recordReadingTime(articleId, readingTime)
    
    // 5. 获取学习小结
    const summary = await learningApi.getTodaySummary()
    
    return { article, summary }
  } catch (error) {
    console.error('阅读文章失败:', error)
    throw error
  }
}

// 智能查词流程
const lookupWord = async (word: string, context?: string) => {
  try {
    // 1. 调用智能词汇查询API
    const result = await vocabularyApi.lookupWord(word, context)
    
    // 2. 添加到生词本
    if (result.data) {
      await vocabularyApi.addWord(
        result.data.word,
        result.data.meaning,
        userStore.user?.id || 0
      )
    }
    
    return result.data
  } catch (error) {
    console.error('查词失败:', error)
    throw error
  }
}

// AI功能使用流程
const useAIFeatures = async (articleId: string) => {
  try {
    // 1. 检查AI权限
    if (!userStore.hasAIFeatures) {
      throw new Error('当前订阅不支持AI功能')
    }
    
    // 2. 生成文章摘要
    const summary = await aiApi.generateSummary(articleId)
    
    // 3. 生成测验题目
    const quiz = await aiApi.generateQuiz(articleId)
    
    return { summary: summary.data, quiz: quiz.data }
  } catch (error) {
    console.error('AI功能使用失败:', error)
    throw error
  }
}
```

### 管理员端API使用

```typescript
// 管理员登录流程
const adminLogin = async (username: string, password: string) => {
  try {
    // 1. 调用管理员登录API
    const result = await adminApi.login({ username, password })
    
    // 2. 保存管理员信息
    if (result.success) {
      localStorage.setItem('admin_token', result.data.token)
      localStorage.setItem('adminSession', JSON.stringify({
        userId: result.data.userId,
        username: username,
        role: result.data.role,
        permissions: result.data.permissions,
        expiredAt: result.data.sessionExpiredAt,
        lastLoginTime: new Date().toISOString()
      }))
      
      // 3. 更新管理员状态
      adminStore.setAdminState({
        isAdmin: true,
        isSuperAdmin: result.data.role === 'SUPER_ADMIN',
        role: result.data.role,
        token: result.data.token,
        userId: result.data.userId
      })
    }
    
    return result
  } catch (error) {
    console.error('管理员登录失败:', error)
    throw error
  }
}

// 用户管理流程
const manageUser = async (userId: number, action: 'enable' | 'disable') => {
  try {
    let result
    
    if (action === 'enable') {
      result = await adminApi.enableUser(userId)
    } else {
      result = await adminApi.disableUser(userId)
    }
    
    if (result.success) {
      ElMessage.success(`用户${action === 'enable' ? '启用' : '禁用'}成功`)
      // 刷新用户列表
      await loadUserList()
    }
    
    return result
  } catch (error) {
    console.error('用户管理失败:', error)
    ElMessage.error('操作失败，请稍后重试')
    throw error
  }
}

// 系统配置管理流程
const updateSystemConfig = async (configs: Record<string, string>) => {
  try {
    // 1. 批量更新配置
    const result = await adminApi.batchUpdateSystemConfigs(configs)
    
    if (result.success) {
      ElMessage.success('配置更新成功')
      
      // 2. 刷新配置列表
      await loadSystemConfigs()
      
      // 3. 通知前端更新
      window.dispatchEvent(new CustomEvent('configUpdated', { 
        detail: { configs } 
      }))
    }
    
    return result
  } catch (error) {
    console.error('配置更新失败:', error)
    ElMessage.error('配置更新失败')
    throw error
  }
}
```

## 📈 性能优化

### 请求优化

```typescript
// 请求去重
const requestCache = new Map<string, Promise<any>>()

const deduplicateRequest = <T>(key: string, requestFn: () => Promise<T>): Promise<T> => {
  if (requestCache.has(key)) {
    return requestCache.get(key)!
  }
  
  const promise = requestFn().finally(() => {
    requestCache.delete(key)
  })
  
  requestCache.set(key, promise)
  return promise
}

// 请求节流
const throttleRequest = (fn: Function, delay: number) => {
  let timeoutId: NodeJS.Timeout | null = null
  
  return (...args: any[]) => {
    if (timeoutId) return
    
    timeoutId = setTimeout(() => {
      fn(...args)
      timeoutId = null
    }, delay)
  }
}

// 请求重试
const retryRequest = async <T>(
  requestFn: () => Promise<T>,
  maxRetries: number = 3,
  delay: number = 1000
): Promise<T> => {
  let lastError: Error
  
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await requestFn()
    } catch (error) {
      lastError = error as Error
      
      if (i < maxRetries - 1) {
        await new Promise(resolve => setTimeout(resolve, delay * Math.pow(2, i)))
      }
    }
  }
  
  throw lastError!
}
```

### 缓存策略

```typescript
// 内存缓存
class MemoryCache {
  private cache = new Map<string, { data: any, expiry: number }>()
  
  set(key: string, data: any, ttl: number = 300000): void { // 默认5分钟
    this.cache.set(key, {
      data,
      expiry: Date.now() + ttl
    })
  }
  
  get(key: string): any | null {
    const item = this.cache.get(key)
    
    if (!item) return null
    
    if (Date.now() > item.expiry) {
      this.cache.delete(key)
      return null
    }
    
    return item.data
  }
  
  clear(): void {
    this.cache.clear()
  }
}

// API缓存装饰器
const withCache = (cacheKey: string, ttl: number = 300000) => {
  return (target: any, propertyName: string, descriptor: PropertyDescriptor) => {
    const method = descriptor.value
    
    descriptor.value = async function(...args: any[]) {
      const key = `${cacheKey}:${JSON.stringify(args)}`
      const cached = memoryCache.get(key)
      
      if (cached) {
        return cached
      }
      
      const result = await method.apply(this, args)
      memoryCache.set(key, result, ttl)
      
      return result
    }
  }
}
```

## 🔍 调试与监控

### API调试工具

```typescript
// API请求日志
const apiLogger = {
  logRequest: (config: any) => {
    console.group(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`)
    console.log('Headers:', config.headers)
    console.log('Data:', config.data)
    console.log('Params:', config.params)
    console.groupEnd()
  },
  
  logResponse: (response: any) => {
    console.group(`✅ API Response: ${response.status}`)
    console.log('Data:', response.data)
    console.log('Headers:', response.headers)
    console.groupEnd()
  },
  
  logError: (error: any) => {
    console.group(`❌ API Error: ${error.response?.status || 'Network Error'}`)
    console.log('URL:', error.config?.url)
    console.log('Method:', error.config?.method)
    console.log('Error:', error.message)
    console.log('Response:', error.response?.data)
    console.groupEnd()
  }
}

// 启用API日志
if (import.meta.env.DEV) {
  api.interceptors.request.use(
    (config) => {
      apiLogger.logRequest(config)
      return config
    },
    (error) => {
      apiLogger.logError(error)
      return Promise.reject(error)
    }
  )
  
  api.interceptors.response.use(
    (response) => {
      apiLogger.logResponse(response)
      return response
    },
    (error) => {
      apiLogger.logError(error)
      return Promise.reject(error)
    }
  )
}
```

### 性能监控

```typescript
// API性能监控
const performanceMonitor = {
  startTime: 0,
  
  start: () => {
    performanceMonitor.startTime = performance.now()
  },
  
  end: (url: string) => {
    const duration = performance.now() - performanceMonitor.startTime
    console.log(`⏱️ API ${url} took ${duration.toFixed(2)}ms`)
    
    // 发送性能数据到监控服务
    if (duration > 5000) { // 超过5秒的请求
      console.warn(`🐌 Slow API request: ${url} (${duration.toFixed(2)}ms)`)
    }
  }
}

// 请求性能监控
api.interceptors.request.use(
  (config) => {
    performanceMonitor.start()
    return config
  }
)

api.interceptors.response.use(
  (response) => {
    performanceMonitor.end(response.config.url || 'unknown')
    return response
  },
  (error) => {
    performanceMonitor.end(error.config?.url || 'unknown')
    return Promise.reject(error)
  }
)
```

## 📚 总结

XReadUp Frontend API 合约文档提供了完整的前后端接口规范，包括：

### 🎯 核心特性

1. **双API系统**：用户端和管理员端API完全分离
2. **智能Token管理**：自动识别和管理不同类型的Token
3. **统一错误处理**：完善的错误处理和用户提示机制
4. **类型安全**：完整的TypeScript类型定义
5. **性能优化**：请求去重、缓存、重试等优化策略

### 📊 API统计

- **用户端API**: 25个接口
- **管理员端API**: 35个接口
- **总计**: 60个API接口
- **覆盖功能**: 认证、文章、AI、词汇、学习、报告、订阅、管理

### 🔧 技术特色

- **智能认证**：双Token系统，自动权限管理
- **错误处理**：统一错误处理，用户友好提示
- **性能监控**：请求性能监控和优化
- **调试支持**：完整的调试和日志系统
- **类型安全**：TypeScript类型定义，编译时检查

这个API合约为前后端协作提供了清晰的规范，确保系统的稳定性和可维护性。

---

<div align="center">

**🔌 构建稳定的API接口，确保前后端协作顺畅**

*设计于 ❤️ 与最佳实践*

</div>
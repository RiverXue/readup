// API服务封装
import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '../types/apiResponse'
import type { ReadingRecordRequest, ReadingTimeData } from '../types/report'

// 创建axios实例
const api = axios.create({
  baseURL: '/', // 使用相对路径，利用Vite代理
  timeout: 600000, // 增加timeout至60秒以适应热点文章获取等耗时操作
  headers: {
    'Content-Type': 'application/json'
  }
})

// 定义一个标志来防止多次重定向到登录页
let isRefreshing = false

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 判断是否是管理员API请求
    const isAdminApi = config.url?.startsWith('/api/admin/') || 
                      config.url?.startsWith('/api/user/login') && config.data?.isAdminLogin
    
    // 检查是否需要跳过token验证 - 为check接口添加例外
    const shouldSkipToken = config.url === '/api/admin/check'
    
    if (isAdminApi && !shouldSkipToken) {
      // 管理员API：使用管理员token
      const adminToken = localStorage.getItem('admin_token')
      if (adminToken) {
        config.headers.Authorization = `Bearer ${adminToken}`
      }
    } else {
      // 普通用户API：使用普通token
      // 直接从localStorage获取token，避免在拦截器中使用store
      const token = localStorage.getItem('token')
      const tokenExpiry = localStorage.getItem('tokenExpiry')
      const currentTime = Date.now()

      // 检查token是否已过期
      if (token && tokenExpiry && parseInt(tokenExpiry) < currentTime) {
        console.warn('Token已过期，将自动清除登录状态')
        // 清除过期的token和用户信息
        localStorage.removeItem('token')
        localStorage.removeItem('tokenExpiry')
        localStorage.removeItem('user')
      } else if (token) {
        // 只有有效的token才添加到请求头
        config.headers.Authorization = `Bearer ${token}`
      }
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    // 添加详细的错误日志，便于调试
    console.log('API请求错误详情:', {
      url: error.config?.url,
      method: error.config?.method,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      message: error.message
    })

    // 对于登录请求，不显示错误消息，让调用方自己处理
    // 这样可以避免登录页面同时显示拦截器和组件自己的错误消息
    // 注意：这里使用更精确的路径判断
    const isAdminLoginRequest = error.config?.url === '/api/user/login'
    if (!isAdminLoginRequest) {
      // 特别处理400错误，提供更友好的错误信息
      if (error.response?.status === 400) {
        const message = error.response?.data?.message || '请求参数错误'
        ElMessage.error(message)
      } else if (error.response?.status !== 401) {
        // 401错误由专门的处理逻辑处理，避免重复显示错误消息
        const message = error.response?.data?.message || error.message || '请求失败'
        ElMessage.error(message)
      }
    }

    if (error.response?.status === 401) {
      // 避免重复触发重定向
      if (!isRefreshing) {
        isRefreshing = true

        try {
          // 判断是管理员API还是普通用户API
          const isAdminApi = error.config?.url?.startsWith('/api/admin/')
          
          if (isAdminApi) {
            // 管理员API：清除管理员登录信息，重定向到管理员登录页
            localStorage.removeItem('adminSession')
            
            if (window.location.pathname !== '/admin/login') {
              setTimeout(() => {
                window.location.href = '/admin/login'
              }, 100)
            }
          } else {
            // 普通用户API：清除用户登录信息，重定向到用户登录页
            localStorage.removeItem('token')
            localStorage.removeItem('tokenExpiry')
            localStorage.removeItem('user')
            
            if (window.location.pathname !== '/login') {
              setTimeout(() => {
                window.location.href = '/login'
              }, 100)
            }
          }
        } catch (redirectError) {
          console.error('重定向登录页失败:', redirectError)
        } finally {
          // 在适当的时机重置标志
          setTimeout(() => {
            isRefreshing = false
          }, 1000)
        }
      }
    }

    return Promise.reject(error)
  }
)

// 导出原始axios实例，用于需要完整response对象的场景
export const request = api

// 文章相关API
export const articleApi = {
  // 获取文章列表
  getArticles: (params?: any) => api.get('/api/article/explore', { params }),

  // 获取文章详情
  getArticle: (id: string) => api.get(`/api/article/${id}`),

  // 阅读文章（获取带阅读统计的文章）
  readArticle: (id: string) => api.get(`/api/article/read/${id}`),

  // 搜索文章
  searchArticles: (keyword: string) => api.get('/api/article/search', { params: { keyword } }),

  // 获取热点文章
  getTrendingArticles: (limit: number) => api.post('/api/article/discover/trending', {}, { params: { limit } }),

  // 获取分类文章
  getArticlesByCategory: (category: string, limit: number) => api.post('/api/article/discover/category', {}, { params: { category, limit } })
}



// AI服务API
export const aiApi = {
  // 分层翻译策略：根据用户等级选择翻译接口
  translate: async (text: string, userId?: number) => {
    // 检查用户等级和AI权限
    if (userId) {
      try {
        const quotaRes: any = await api.get(`/api/subscription/quota/${userId}`);
        const hasAIFeatures = quotaRes?.data?.aiQuota?.enabled;

        if (hasAIFeatures) {
          // 付费用户：智能翻译
          return api.post('/api/ai/translate/smart', { text });
        }
      } catch (error) {
        console.warn('检查用户权限失败，使用基础翻译:', error);
      }
    }

    // 免费用户或权限检查失败：基础翻译
    return api.post('/api/ai/tencent-translate/en-to-zh', { text });
  },

  // 增值翻译服务（仅付费用户）
  smartTranslate: (text: string, targetLang?: string) =>
    api.post('/api/ai/translate/smart', { text, targetLang }),

  // 全文翻译
  translateFullText: (content: string, articleId?: string) =>
    api.post('/api/ai/translate/fulltext', { content, articleId }),

  // 腾讯云英文到中文翻译
  tencentTranslateEnToZh: (text: string) =>
    api.post('/api/ai/tencent-translate/en-to-zh', { text }),

  // 腾讯云中文到英文翻译
  tencentTranslateZhToEn: (text: string) =>
    api.post('/api/ai/tencent-translate/zh-to-en', { text }),

  // 腾讯云批量翻译
  tencentTranslateBatch: (data: any) =>
    api.post('/api/ai/tencent-translate/batch', data),

  // AI摘要
  generateSummary: (text: string, articleId: string | number) =>
    api.post('/api/ai/summary', { text, articleId }),

  // AI语法解析
  parseSentence: (sentence: string, articleId: string | number) =>
    api.post('/api/ai/parse', { sentence, articleId }),

  // 词汇翻译
  translateWord: (word: string, context: string) =>
    api.post('/api/ai/translate/word', { word, context }),

  // 获取阅读建议
  getReadingSuggestions: (articleId: string) =>
    api.get(`/api/ai/assistant/${articleId}/suggestions`),

  // AI对话（支持Function Calling）
  chat: (question: string, userId: number) => {
    return api.post('/api/ai/assistant/chat', {
      question,
      userId
    });
  },

  // 查询单词（调用二级词库）
  lookupWord: (word: string, context: string, userId: number) => {
    return api.post('/api/vocabulary/lookup', {
      word,
      context,
      userId
    });
  },

  // 生成测验题（Structured Outputs）
  generateQuiz: (text: string, articleId: number) => {
    return api.post('/api/ai/quiz', {
      text,
      articleId,
      questionCount: 3
    });
  },

  // 智能AI分析（推荐使用）
  smartAnalyze: (data: {
    userId: number;
    content: string;
    scenario?: string;
  }) => {
    return api.post('/api/ai/smart/analyze', data);
  },

  // Function Calling 生成测验（推荐使用）
  assistantGenerateQuiz: (data: {
    articleContent: string;
    articleId: number;
  }) => {
    return api.post('/api/ai/assistant/quiz', data);
  },

  // 综合AI分析
  comprehensiveAnalysis: (data: {
    articleId: number;
    userId?: number;
    text: string;
  }) => {
    return api.post('/api/ai/comprehensive', data);
  }
}

// 词汇相关API
export const vocabularyApi = {
  // 添加生词
  addWord: (data: { word: string; translation: string; context: string; articleId: number; userId: number }) =>
    api.post('/api/vocabulary/add', data),

  // 智能查询单词
  lookupWord: (data: { word: string; context: string; userId: number; articleId?: number }) =>
    api.post('/api/vocabulary/lookup', data),

  // 批量查询单词
  batchLookup: (data: { words: string[]; context: string; userId: number; articleId?: number }) =>
    api.post('/api/vocabulary/batch-lookup', data),

  // 获取词汇统计
  getVocabularyStats: (userId: string) => api.get(`/api/vocabulary/stats/${userId}`),

  // 清理重复词汇
  cleanupDuplicates: (userId: string) => api.post(`/api/vocabulary/cleanup/${userId}`),

  // 获取用户所有单词（根据API文档添加）
  getUserWords: (userId: string) => api.get(`/api/user/vocabulary/my-words?userId=${userId}`),

  // 复习单词 - 调用真实的后端API
  reviewWord: (userId: string, wordId: number, reviewStatus: string) => {
    return api.post('/api/vocabulary/review', {
      wordId,
      userId: parseInt(userId),
      reviewStatus
    });
  },

  // 删除单词 - 调用真实的后端API
  deleteWord: (userId: string, wordId: number) => {
    return api.delete(`/api/vocabulary/${wordId}?userId=${userId}`);
  },

  // 听写答案校对 - 前端本地实现（不依赖后端API）
  checkDictation: (userId: string, word: string, userAnswer: string) => {
    // 模拟后端响应，直接在前端进行答案校对
    const isCorrect = userAnswer.trim().toLowerCase() === word.toLowerCase();
    return Promise.resolve({
      data: {
        correct: isCorrect,
        message: isCorrect ? '拼写正确！' : '拼写错误'
      }
    });
  }
}

// 学习记录API
export const learningApi = {

  // 每日打卡
  dailyCheckIn: (userId: string) => api.post(`/api/user/progress/check-in?userId=${userId}`),

  // 记录阅读时长
  recordReadingTime: (userId: number, articleId: number, readTimeSec: number) => {
    return api.post<ApiResponse<string>>(`/api/report/reading-record`, {
      userId,
      articleId,
      readTimeSec
    });
  },

  // 获取学习统计
  getReadingTimeStats: (userId: number) => {
    return api.get<ApiResponse<ReadingTimeData>>(`/api/report/reading-time`, {
      params: { userId }
    });
  },

  // 获取今日复习词汇
  getTodayReviewWords: (userId: string) => api.get(`/api/report/review-today?userId=${userId}`),

  // 获取学习周报
  getWeeklyInsights: (userId: string) => api.get(`/api/report/weekly/insights?userId=${userId}`),

  // 获取今日小结
  getTodaySummary: (userId: string) => api.get(`/api/report/today/summary?userId=${userId}`),

  // 获取连续打卡成就
  getStreakAchievement: (userId: string) => api.get(`/api/report/streak/achievement?userId=${userId}`)
}

// 报告相关API
export const reportApi = {
  // 获取仪表盘数据
  getDashboardData: (userId: string) => api.get(`/api/report/dashboard?userId=${userId}`),

  // 获取词汇增长曲线
  getGrowthCurve: (userId: string, days: number) => api.get(`/api/report/growth-curve?userId=${userId}&days=${days}`),

  // 获取阅读时长数据
  getReadingTime: (userId: string) => api.get(`/api/report/reading-time?userId=${userId}`),

  // 设置单词为不再巩固
  setWordAsNoLongerReview: (wordId: string | number) => {
    const numericWordId = typeof wordId === 'string' ? parseInt(wordId, 10) : wordId;
    if (isNaN(numericWordId)) {
      console.error(`API调用 - setWordAsNoLongerReview: 无效的wordId格式: ${wordId}`);
      return Promise.reject(new Error('无效的单词ID'));
    }
    return api.post(`/api/report/no-longer-review/${numericWordId}`);
  }
}

// 订阅相关API
export const subscriptionApi = {
  // 创建订阅 - 使用查询参数传递
  create: (userId: string | number, planType: string, paymentMethod: string) => {
    // 确保userId是数字类型
    const numericUserId = typeof userId === 'string' ? parseInt(userId, 10) : userId;
    if (isNaN(numericUserId)) {
      console.error(`API调用 - createSubscription: 无效的userId格式: ${userId}`);
      return Promise.reject(new Error('无效的用户ID'));
    }
    // 使用查询参数而不是请求体传递参数
    return api.post(`/api/subscription/create?userId=${numericUserId}&planType=${planType}&paymentMethod=${paymentMethod}`);
  },

  // 获取当前订阅 - 使用路径参数传递userId
  // 特别处理：确保userId是数字类型以匹配后端Long类型参数
  getCurrentSubscription: (userId: string | number) => {
    // 安全地将userId转换为整数，确保类型一致性
    const numericUserId = typeof userId === 'string' ? parseInt(userId, 10) : userId;
    // 验证转换结果是否有效
    if (isNaN(numericUserId)) {
      console.error(`API调用 - getCurrentSubscription: 无效的userId格式: ${userId}`);
      // 这里可以返回一个rejected Promise或默认值
      return Promise.reject(new Error('无效的用户ID'));
    }
    console.log(`API调用 - getCurrentSubscription: userId=${numericUserId}, 原始类型=${typeof userId}, 转换后类型=${typeof numericUserId}`);
    return api.get(`/api/subscription/current/${numericUserId}`);
  },

  // 获取订阅历史 - 修改userId为字符串类型
  // 注意：响应拦截器直接返回response.data
  getSubscriptionHistory: (userId: string | number) => {
    // 确保userId是数字类型
    const numericUserId = typeof userId === 'string' ? parseInt(userId, 10) : userId;
    return api.get(`/api/subscription/history/${numericUserId}`);
  },

  // 取消订阅
  cancelSubscription: (subscriptionId: number) =>
    api.post(`/api/subscription/cancel/${subscriptionId}`),

  // 检查使用限制 - 修改userId为字符串类型
  checkUsageLimit: (userId: string | number, articlesCount: number, wordsCount: number) => {
    // 确保userId是数字类型
    const numericUserId = typeof userId === 'string' ? parseInt(userId, 10) : userId;
    return api.get(`/api/subscription/check-limit/${numericUserId}?articlesCount=${articlesCount}&wordsCount=${wordsCount}`);
  },

  // 获取剩余额度 - 修改userId为字符串类型
  getRemainingQuota: (userId: string | number) => {
    // 确保userId是数字类型
    const numericUserId = typeof userId === 'string' ? parseInt(userId, 10) : userId;
    return api.get(`/api/subscription/quota/${numericUserId}`);
  },
  
  // 获取套餐价格配置
  getPlanPrices: () => {
    return api.get('/api/subscription/plan-prices');
  }
}

export default api

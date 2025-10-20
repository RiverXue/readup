import type { AdminUser, AdminRole } from '@/types/admin'
import { request } from '@/utils/api'

/**
 * 管理员相关API接口
 */

// 管理员登录 - 使用独立的管理员登录接口
export const login = async (params: { username: string, password: string }) => {
  try {
    // 使用独立的管理员登录接口，不再复用普通用户登录接口
    const response = await request.post('/api/admin/login', {
      username: params.username,
      password: params.password
    });

    // 如果登录成功，后端已经完成了身份认证和角色校验
    if (response && response.data) {
      // 更灵活地判断登录成功
      const isLoginSuccess = response.success !== undefined ? response.success : 
                            (response.code === 200 || response.code === 0);
      
      if (isLoginSuccess) {
        // 直接使用后端返回的管理员信息
        const userData = response.data;
        
        // 确保返回的数据包含必要的管理员标识
        userData.isAdmin = true;
        userData.isSuperAdmin = userData.role === 'SUPER_ADMIN';
        userData.adminRole = userData.role || 'ADMIN';
      }
    }

    return response;
  } catch (error) {
    console.error('管理员登录失败:', error);
    throw error;
  }
}

// 验证用户是否为管理员
export const checkAdmin = async (userId?: string | number): Promise<{ success: boolean, data?: { isAdmin: boolean, admin?: boolean, role?: string, userId?: string | number } }> => {
  // 参数验证
  if (!userId) {
    console.error('检查管理员权限失败: 缺少userId参数');
    return { success: false, data: { isAdmin: false } };
  }

  // 确保userId是字符串类型以便正确传递
  const userIdStr = String(userId);
  
  try {
    // 构造请求配置
    const requestConfig = {
      params: {
        userId: userIdStr
      },
      headers: {
        // 明确设置Content-Type
        'Content-Type': 'application/json'
      },
      timeout: 10000 // 设置10秒超时
    };

    console.log('发起管理员权限检查请求:', { url: '/api/admin/check', params: requestConfig.params });
    const response = await request.get('/api/admin/check', requestConfig);
    
    // 处理响应结果
    if (!response) {
      console.error('检查管理员权限失败: 未获取到响应');
      return { success: false, data: { isAdmin: false } };
    }

    // 解析响应结果 - 兼容不同的响应格式
    const isSuccess = response.success !== undefined ? response.success : 
                     (response.code === 200 || response.code === 0);
    
    if (!isSuccess) {
      console.error('检查管理员权限失败:', response.message || '未知错误');
      return { success: false, data: { isAdmin: false } };
    }

    // 处理响应数据
    if (!response.data) {
      console.error('检查管理员权限失败: 响应数据为空');
      return { success: false, data: { isAdmin: false } };
    }

    // 统一响应数据格式，确保isAdmin字段存在
    const result = {
      success: true,
      data: {
        // 优先使用admin字段，没有则使用isAdmin字段，默认false
        isAdmin: response.data.admin !== undefined ? response.data.admin : 
                (response.data.isAdmin || false),
        admin: response.data.admin, // 保留原始admin字段
        role: response.data.role,
        userId: response.data.userId || userIdStr
      }
    };

    console.log('管理员权限检查成功:', result);
    return result;
  } catch (error) {
    // 详细的错误日志
    console.error('检查管理员权限请求异常:', {
      error: error instanceof Error ? error.message : String(error),
      stack: error instanceof Error ? error.stack : undefined
    });
    
    // 网络错误或其他异常情况，返回默认失败结果
    return {
      success: false,
      data: {
        isAdmin: false,
        userId: userIdStr
      }
    };
  }
};

// ========== 管理员用户管理模块 ==========

// 获取管理员列表
export const getAdminUsersList = (params?: {
  page?: number
  pageSize?: number
  keyword?: string
  role?: string
}) => {
  return request.get('/api/admin/list', { params })
}

// 获取管理员详情
export const getAdminDetail = (adminId: string) => {
  return request.get(`/api/admin/detail`, {
    params: {
      userId: adminId
    }
  })
}

// 获取可选用户列表（用于添加新管理员）
export const getAvailableUsers = async (keyword?: string) => {
  try {
    const response = await request.get('/api/admin/available-users', {
      params: {
        keyword,
        pageSize: 50
      }
    });
    return response;
  } catch (error) {
    console.error('获取可选用户列表失败:', error);
    throw error;
  }
}

// 添加管理员
export const addAdminUser = (data: {
  userId: number
  role: AdminRole
}) => {
  return request.post('/api/admin/admins/add', {}, { params: data })
}

// 更新管理员角色
export const updateAdminUserRole = (userId: number, role: AdminRole) => {
  return request.put(`/api/admin/admins/update/${userId}`, {}, { params: { role } })
}

// 删除管理员
export const deleteAdminUser = (userId: number) => {
  return request.delete(`/api/admin/admins/delete/${userId}`)
}

// 更新管理员信息
export const updateAdmin = (adminId: string, data: any) => {
  return request.put(`/api/admin/update`, null, {
    params: {
      userId: adminId,
      role: data.role,
      phone: data.phone
    }
  })
}

// 添加管理员（新方法）
export const addAdmin = (data: any) => {
  return request.post('/api/admin/add', data)
}

// 删除管理员（新方法）
export const deleteAdmin = (adminId: string) => {
  return request.delete(`/api/admin/delete`, {
    params: {
      userId: adminId
    }
  })
}

// 重置管理员密码
export const resetAdminPassword = (adminId: string, data: any) => {
  return request.put(`/api/admin/admins/reset-password/${adminId}`, data)
}

// ========== 用户管理相关接口 ==========

// 获取用户列表
export const getUserList = (params?: {
  page?: number
  pageSize?: number
  username?: string
  phone?: string
  interestTag?: string
  identityTag?: string
  status?: string
}) => {
  return request.get('/api/admin/users/list', { params })
}



// ========== 文章管理相关接口 ==========

// 获取文章列表
export const getArticleList = (params?: {
  page?: number
  pageSize?: number
  title?: string
  source?: string
  status?: string
  difficulty?: string
  hasAI?: boolean
}) => {
  return request.get('/api/admin/articles/list', { params })
}

// 获取文章详情
export const getArticleDetail = (articleId: string) => {
  return request.get(`/api/admin/articles/detail/${articleId}`)
}

// 删除文章
export const deleteArticle = (articleId: string) => {
  return request.delete(`/api/admin/articles/delete/${articleId}`)
}

// 发布文章
export const publishArticle = (articleId: string) => {
  return request.put(`/api/admin/articles/publish/${articleId}`)
}

// 审核文章
export const auditArticle = (articleId: number, data: {
  approved: boolean
  reason?: string
}) => {
  return request.post(`/api/admin/article/audit/${articleId}`, data)
}

// 更新文章分类
export const updateArticleCategory = (articleId: string, category: string) => {
  return request.put(`/api/admin/articles/${articleId}/category`, null, {
    params: { category }
  })
}

// 更新文章难度
export const updateArticleDifficulty = (articleId: string, difficulty: string) => {
  return request.put(`/api/admin/articles/${articleId}/difficulty`, null, {
    params: { difficulty }
  })
}

// 标记精选文章
export const markArticleFeatured = (articleId: string, featured: boolean) => {
  return request.put(`/api/admin/articles/${articleId}/featured`, null, {
    params: { featured }
  })
}


// ========== 内容过滤管理相关接口 ==========

// 获取文章过滤记录
export const getArticleFilterLogs = (articleId: string) => {
  return request.get(`/api/admin/articles/${articleId}/filter-logs`)
}

// 获取过滤记录列表
export const getFilterLogsPage = (params?: {
  page?: number
  pageSize?: number
  filterType?: string
  status?: string
  startDate?: string
  endDate?: string
}) => {
  return request.get('/api/admin/articles/filter-logs', { params })
}

// 更新过滤记录状态
export const updateFilterLogStatus = (logId: string, status: string, adminId?: number) => {
  return request.put(`/api/admin/articles/filter-logs/${logId}/status`, null, {
    params: { status, adminId }
  })
}

// 删除过滤记录
export const deleteFilterLog = (logId: string) => {
  return request.delete(`/api/admin/articles/filter-logs/${logId}`)
}

// 获取过滤统计信息
export const getFilterStatistics = () => {
  return request.get('/api/admin/articles/filter-logs/statistics')
}

// 记录内容过滤日志
export const logContentFilter = (data: {
  articleId: number
  filterType: string
  matchedContent: string
  filterReason?: string
  severityLevel?: string
  actionTaken?: string
  adminId?: number
}) => {
  return request.post('/api/admin/articles/filter-logs', null, { params: data })
}

// ========== 订阅管理相关接口 ==========

// 获取订阅方案列表
export const getSubscriptionPlans = () => {
  return request.get('/api/admin/subscriptions/plans')
}

// 创建新的订阅方案
export const createSubscriptionPlan = (data: {
  planType: string
  price: number
  currency: string
  maxArticlesPerMonth: number
  maxWordsPerArticle: number
  aiFeaturesEnabled: boolean
  prioritySupport: boolean
}) => {
  return request.post('/api/admin/subscriptions/plans', data)
}

// 更新订阅方案
export const updateSubscriptionPlan = (planId: string, data: any) => {
  return request.put(`/api/admin/subscriptions/plans/${planId}`, data)
}

// 删除订阅方案
export const deleteSubscriptionPlan = (planId: string) => {
  return request.delete(`/api/admin/subscriptions/plans/${planId}`)
}

// 获取所有用户的订阅记录
export const getUserSubscriptionList = (params?: {
  page?: number
  pageSize?: number
  userId?: string
  status?: string
  planType?: string
}) => {
  return request.get('/api/admin/subscriptions/user', { params })
}

// 系统配置相关API
export const getSystemConfigs = () => {
  return request.get('/api/admin/system-config/all')
}

export const getSystemConfigsByCategory = (category: string) => {
  return request.get(`/api/admin/system-config/category/${category}`)
}

export const getSystemConfigCategories = () => {
  return request.get('/api/admin/system-config/categories')
}

export const getSystemConfigValue = (configKey: string) => {
  return request.get(`/api/admin/system-config/value/${configKey}`)
}

export const updateSystemConfig = (configKey: string, configValue: string) => {
  return request.put(`/api/admin/system-config/update/${configKey}`, null, {
    params: { configValue }
  })
}

export const batchUpdateSystemConfigs = (configs: Record<string, string>) => {
  return request.put('/api/admin/system-config/batch-update', configs)
}

export const resetSystemConfig = (configKey: string) => {
  return request.put(`/api/admin/system-config/reset/${configKey}`)
}

export const getSystemInfo = () => {
  return request.get('/api/admin/system-config/system-info')
}

// 更新用户信息
export const updateUser = (userId: number, data: any) => {
  return request.put(`/api/admin/users/update/${userId}`, data)
}

// 更新用户订阅状态
export const updateUserSubscriptionStatus = (subscriptionId: string, status: string) => {
  return request.put(`/api/admin/subscriptions/user/${subscriptionId}/status`, {}, {
    params: { status }
  })
}

// ========== AI分析结果相关接口 ==========

// 注意：根据后端实现，目前AI分析功能通过以下接口实现

// 分析文章内容
export const analyzeArticle = (params?: {
  articleId?: number
  content?: string
  difficulty?: string
}) => {
  return request.post('/api/admin/ai/analyze', params)
}

// 获取AI学习助手回答
export const getAIAssistantAnswer = (params: {
  question: string
  context?: string
}) => {
  return request.post('/api/admin/ai/assistant', params)
}

// 检查AI服务状态
export const checkAIServiceHealth = () => {
  return request.get('/api/admin/ai/health')
}

// ========== 管理员统计相关接口 ==========

// 获取管理员专属统计数据
export const getAdminStats = () => {
  return request.get('/api/admin/stats')
}

// ========== 用户状态管理接口 ==========

// 禁用用户
export const disableUser = (userId: number) => {
  return request.put(`/api/admin/users/disable/${userId}`)
}

// 启用用户
export const enableUser = (userId: number) => {
  return request.put(`/api/admin/users/enable/${userId}`)
}

// ========== 仪表盘相关接口 ==========

// 获取数据趋势
export const getDataTrends = (type: string, days: number) => {
  return request.get('/api/admin/dashboard/trends', {
    params: { type, days }
  })
}

// 获取最近活动
export const getRecentActivities = (limit: number = 20) => {
  return request.get('/api/admin/dashboard/recent-activities', {
    params: { limit }
  })
}

// ========== AI分析相关接口 ==========

// 获取AI分析结果列表
export const getAIAnalysisList = (params?: {
  page?: number
  pageSize?: number
  articleTitle?: string
  analysisType?: string
  status?: string
  startDate?: string
  endDate?: string
}) => {
  return request.get('/api/admin/ai/analysis', { params })
}

// 获取AI分析详情
export const getAIAnalysisDetail = (analysisId: string) => {
  return request.get(`/api/admin/ai/analysis/${analysisId}`)
}


// 统一导出对象
export const adminApi = {
  login,
  checkAdmin,
  getAdminUsersList,
  getAdminDetail,
  getAvailableUsers,
  addAdminUser,
  addAdmin,
  updateAdmin,
  updateAdminUserRole,
  deleteAdminUser,
  deleteAdmin,
  resetAdminPassword,
  getUserList,
  updateUser,
  getArticleList,
  getArticleDetail,
  deleteArticle,
  publishArticle,
  auditArticle,
  getSubscriptionPlans,
  createSubscriptionPlan,
  updateSubscriptionPlan,
  deleteSubscriptionPlan,
  getUserSubscriptionList,
  updateUserSubscriptionStatus,
  analyzeArticle,
  getAIAssistantAnswer,
  checkAIServiceHealth,
  getAdminStats,
  getDataTrends,
  getRecentActivities,
  getAIAnalysisList,
  getAIAnalysisDetail,
  disableUser,
  enableUser,
  // 系统配置相关
  getSystemConfigs,
  getSystemConfigsByCategory,
  getSystemConfigCategories,
  getSystemConfigValue,
  updateSystemConfig,
  batchUpdateSystemConfigs,
  resetSystemConfig,
  getSystemInfo
}
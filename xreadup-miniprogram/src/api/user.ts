// api/user.ts - 用户相关API
import request from '@/utils/request'

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar?: string
  email?: string
  phone?: string
  createdAt: string
}

export interface LoginRequest {
  code: string
  encryptedData: string
  iv: string
  signature: string
  rawData: string
}

export interface LoginResponse {
  user: UserInfo
  token: string
  expiresIn: number
}

export interface SubscriptionInfo {
  planType: 'FREE' | 'BASIC' | 'PRO' | 'ENTERPRISE'
  status: 'ACTIVE' | 'INACTIVE' | 'EXPIRED'
  price: number
  startDate: string
  endDate: string
  maxArticlesPerMonth: number
  maxWordsPerArticle: number
  aiFeaturesEnabled: boolean
}

export interface UsageInfo {
  articlesQuota: {
    used: number
    total: number
    resetDate: string
  }
  wordsQuota: {
    used: number
    total: number
  }
  aiCallsQuota: {
    used: number
    total: number
  }
}

export const userApi = {
  // 微信登录
  wechatLogin: (data: LoginRequest) => 
    request.post<LoginResponse>('/api/user/wechat-login', data),
  
  // 获取用户信息
  getUserInfo: () => 
    request.get<UserInfo>('/api/user/profile'),
  
  // 更新用户信息
  updateUserInfo: (data: Partial<UserInfo>) => 
    request.put<UserInfo>('/api/user/profile', data),
  
  // 获取用户订阅信息
  getUserSubscription: () => 
    request.get<SubscriptionInfo>('/api/user/subscription'),
  
  // 获取用户使用统计
  getUserUsage: () => 
    request.get<UsageInfo>('/api/user/usage'),
  
  // 每日打卡
  dailyCheckIn: () => 
    request.post<{ streakDays: number }>('/api/user/checkin'),
  
  // 获取今日学习统计
  getTodaySummary: () => 
    request.get<{
      hasCheckedInToday: boolean
      studyTime: number
      wordsLearned: number
      articlesRead: number
    }>('/api/user/today-summary')
}

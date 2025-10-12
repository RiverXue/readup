// 用户相关类型定义

// 用户基本信息
export interface User {
  id: number
  username: string
  email?: string
  avatar?: string
  role?: string
  createdAt?: string
  updatedAt?: string
}

// 用户注册请求
export interface RegisterData {
  username: string
  password: string
  email?: string
}

// 用户登录请求
export interface LoginCredentials {
  username: string
  password: string
}

// 用户登录响应
export interface LoginResponse {
  token: string
  user: User
  expiresIn?: number
}

// 用户更新个人资料请求
export interface UpdateProfileRequest {
  username?: string
  email?: string
  avatar?: string
}

// 订阅计划类型
export interface SubscriptionPlan {
  id: number
  name: string
  description: string
  price: number
  durationDays: number
  maxArticles: number
  maxWords: number
  features: string[]
}

// 用户订阅信息
export interface UserSubscription {
  id: number
  userId: number
  planId: number
  planName: string
  startDate: string
  endDate: string
  status: 'active' | 'expired' | 'cancelled'
  createdAt: string
  updatedAt: string
}

// 订阅创建请求
export interface CreateSubscriptionRequest {
  userId: number
  planType: string
  paymentMethod: string
}
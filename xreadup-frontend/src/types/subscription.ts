// 订阅相关类型定义
export interface Subscription {
  id: number
  userId: number
  planType: 'FREE' | 'BASIC' | 'PRO' | 'ENTERPRISE' | 'TRIAL'
  price: number
  currency: string
  status: 'ACTIVE' | 'CANCELLED' | 'EXPIRED'
  startDate: string
  endDate: string
  paymentMethod: 'ALIPAY' | 'WECHAT' | 'CREDIT_CARD' | 'TRIAL'
  maxArticlesPerMonth: number
  maxWordsPerArticle: number
  aiFeaturesEnabled: boolean
  autoRenew: boolean
  remainingDays?: number
  usedArticlesThisMonth?: number
  nextBillingDate?: string
  isTrial?: boolean
}

export interface SubscriptionPlan {
  type: 'FREE' | 'BASIC' | 'PRO' | 'ENTERPRISE'
  name: string
  price: number
  currency: string
  duration: string
  maxArticles: number
  maxWords: number
  aiFeatures: boolean
  prioritySupport: boolean
  features: string[]
  recommended?: boolean
}

export interface UsageQuota {
  articlesQuota: {
    total: number
    used: number
    remaining: number
    resetDate: string
  }
  wordsQuota: {
    perArticleLimit: number
    averageWordsUsed: number
  }
  aiQuota: {
    enabled: boolean
    unlimited: boolean
    dailyLimit?: number
    used?: number
  }
}

export interface PaymentMethod {
  type: 'ALIPAY' | 'WECHAT' | 'CREDIT_CARD'
  name: string
  icon: string
  description: string
}
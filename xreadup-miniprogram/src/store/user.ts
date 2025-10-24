// store/user.ts - 用户状态管理
import { defineStore } from 'pinia'
import { userApi, type UserInfo, type SubscriptionInfo, type UsageInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null as UserInfo | null,
    token: '',
    isLoggedIn: false,
    subscription: null as SubscriptionInfo | null,
    usage: null as UsageInfo | null,
    loading: false
  }),
  
  getters: {
    // 获取用户等级
    userLevel: (state) => {
      if (!state.user) return 'guest'
      
      // 根据订阅类型判断用户等级
      if (state.subscription?.planType === 'ENTERPRISE') return 'enterprise'
      if (state.subscription?.planType === 'PRO') return 'pro'
      if (state.subscription?.planType === 'BASIC') return 'basic'
      return 'free'
    },
    
    // 是否可以使用AI功能
    canUseAI: (state) => {
      return state.subscription?.aiFeaturesEnabled || false
    },
    
    // 是否已用完文章额度
    isArticleQuotaExceeded: (state) => {
      if (!state.usage) return false
      return state.usage.articlesQuota.used >= state.usage.articlesQuota.total
    },
    
    // 是否已用完单词额度
    isWordQuotaExceeded: (state) => {
      if (!state.usage) return false
      return state.usage.wordsQuota.used >= state.usage.wordsQuota.total
    }
  },
  
  actions: {
    // 微信登录
    async loginWithWeChat() {
      this.loading = true
      
      try {
        // 1. 获取微信登录凭证
        const loginRes = await uni.login({
          provider: 'weixin'
        })
        
        // 2. 获取用户信息
        const userInfoRes = await uni.getUserProfile({
          desc: '用于完善用户资料'
        })
        
        // 3. 调用后端API
        const response = await userApi.wechatLogin({
          code: loginRes.code,
          encryptedData: userInfoRes.encryptedData,
          iv: userInfoRes.iv,
          signature: userInfoRes.signature,
          rawData: userInfoRes.rawData
        })
        
        // 4. 保存用户信息
        this.user = response.data.user
        this.token = response.data.token
        this.isLoggedIn = true
        
        // 5. 持久化存储
        uni.setStorageSync('token', this.token)
        uni.setStorageSync('user', this.user)
        
        // 6. 获取订阅和使用信息
        await this.loadUserData()
        
        return response.data
      } catch (error) {
        console.error('微信登录失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },
    
    // 自动登录
    async autoLogin() {
      const token = uni.getStorageSync('token')
      const user = uni.getStorageSync('user')
      
      if (token && user) {
        this.token = token
        this.user = user
        this.isLoggedIn = true
        
        try {
          // 验证token有效性
          await userApi.getUserInfo()
          // 加载用户数据
          await this.loadUserData()
        } catch (error) {
          console.error('自动登录失败:', error)
          this.logout()
        }
      }
    },
    
    // 加载用户数据
    async loadUserData() {
      if (!this.isLoggedIn) return
      
      try {
        // 并行获取订阅和使用信息
        const [subscriptionRes, usageRes] = await Promise.all([
          userApi.getUserSubscription(),
          userApi.getUserUsage()
        ])
        
        this.subscription = subscriptionRes.data
        this.usage = usageRes.data
      } catch (error) {
        console.error('加载用户数据失败:', error)
      }
    },
    
    // 更新用户信息
    async updateUserInfo(data: Partial<UserInfo>) {
      if (!this.isLoggedIn) return
      
      try {
        const response = await userApi.updateUserInfo(data)
        this.user = response.data
        
        // 更新本地存储
        uni.setStorageSync('user', this.user)
        
        return response.data
      } catch (error) {
        console.error('更新用户信息失败:', error)
        throw error
      }
    },
    
    // 每日打卡
    async dailyCheckIn() {
      if (!this.isLoggedIn) return
      
      try {
        const response = await userApi.dailyCheckIn()
        
        // 更新今日学习统计
        await this.loadUserData()
        
        return response.data
      } catch (error) {
        console.error('打卡失败:', error)
        throw error
      }
    },
    
    // 获取今日学习统计
    async getTodaySummary() {
      if (!this.isLoggedIn) return null
      
      try {
        const response = await userApi.getTodaySummary()
        return response.data
      } catch (error) {
        console.error('获取今日统计失败:', error)
        return null
      }
    },
    
    // 退出登录
    logout() {
      this.user = null
      this.token = ''
      this.isLoggedIn = false
      this.subscription = null
      this.usage = null
      
      // 清除本地存储
      uni.removeStorageSync('token')
      uni.removeStorageSync('user')
      
      // 跳转到登录页
      uni.reLaunch({
        url: '/pages/login/login'
      })
    },
    
    // 检查登录状态
    checkLoginStatus() {
      if (!this.isLoggedIn) {
        uni.navigateTo({
          url: '/pages/login/login'
        })
        return false
      }
      return true
    }
  }
})

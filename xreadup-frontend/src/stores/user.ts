import { ElMessage } from 'element-plus'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api, { subscriptionApi } from '@/utils/api'
import type { Subscription } from '@/types/subscription'

export interface User {
  id: string
  username: string
  avatar?: string
  level?: number
  totalWords?: number
  totalReadingTime?: number
  streakDays?: number
  createdAt?: string
  subscription?: Subscription
  interestTag?: string
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const user = ref<User | null>(null)
  // 直接从localStorage获取token，确保状态一致
  const storedToken = localStorage.getItem('token')
  const token = ref<string>(storedToken || '')
  const loading = ref(false)
  const tier = ref<'free' | 'basic' | 'pro' | 'enterprise'>('free')
  const aiCalls = ref<number>(0)
  const subscription = ref<Subscription | null>(null)
  
  // 试用状态
  const isTrialActive = ref<boolean>(false)
  const hasUsedTrial = ref<boolean>(false)

  // 计算属性 - 只根据token判断是否登录，不依赖user对象
  const isLoggedIn = computed(() => !!token.value && token.value.trim() !== '')
  const userInfo = computed(() => user.value)

  // 用户阶段
  const userStage = computed(() => {
    if (!user.value || user.value.totalWords === undefined) return 'newbie';
    if (user.value.totalWords < 50) return 'newbie';
    if (user.value.totalWords < 200) return 'intermediate';
    return 'advanced';
  })

  // 用户等级计算 - 增强版，添加更详细的调试日志
  const userTier = computed(() => {
    try {
      // 防御性编程，确保subscription对象存在
      if (!subscription.value) {
        console.log('用户等级计算: subscription对象不存在，返回free')
        console.log('当前token状态:', !!token.value, '用户信息:', user.value?.id ? '存在' : '不存在')
        return 'free'
      }

      console.log('用户等级计算详细信息:', {
        subscriptionId: subscription.value.id,
        status: subscription.value.status,
        planType: subscription.value.planType,
        startDate: subscription.value.startDate,
        endDate: subscription.value.endDate,
        userId: user.value?.id
      })

      // 检查状态 - 添加大小写不敏感的比较
      const status = subscription.value.status || ''
      if (status.toLowerCase() !== 'active') {
        console.log(`用户等级计算: 订阅状态不是ACTIVE (${status})，返回free`)
        return 'free'
      }

      // 转换为小写以确保大小写不敏感
      const planType = (subscription.value.planType || '').toLowerCase()
      console.log(`用户等级计算: 计划类型为${planType}`)

      // 返回有效的用户等级
      if (planType === 'pro' || planType === 'trial') {
        console.log(`用户等级计算: 确认是${planType === 'trial' ? '试用' : '专业'}会员，返回pro`)
        return 'pro'
      } else if (planType === 'enterprise') {
        console.log('用户等级计算: 确认是企业会员，返回enterprise')
        return 'enterprise'
      } else if (planType === 'basic') {
        return 'basic'
      } else {
        console.log(`用户等级计算: 未知的计划类型${planType}，返回free`)
        return 'free'
      }
    } catch (error) {
      console.error('用户等级计算出错:', error)
      return 'free'
    }
  })

  // AI功能权限
  const hasAIFeatures = computed(() => {
    // 如果有试用权限，也允许使用AI功能
    if (isTrialActive.value) {
      return true
    }
    
    // 原有逻辑
    return subscription.value?.aiFeaturesEnabled || false
  })

  // 初始化用户状态 - 增强版，适配后端实际接口
  const initializeUser = async () => {
    try {
      loading.value = true

      // 获取token和过期时间
      const savedToken = localStorage.getItem('token')
      const tokenExpiry = localStorage.getItem('tokenExpiry')
      const currentTime = Date.now()

      // 如果token存在但已过期，清除所有存储
      if (savedToken && tokenExpiry && parseInt(tokenExpiry) < currentTime) {
        console.info('Token已过期，清除登录状态')
        logout()
        return
      }

      // 更新token状态
      token.value = savedToken || ''

      // 确保api实例的Authorization头已经设置
      if (token.value) {
        api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`

        // 尝试从localStorage获取用户信息
        await fetchUserProfileFromServer()

        // 无论用户信息是否从服务器获取到，都尝试获取订阅信息
        if (user.value?.id) {
          await fetchSubscription()
        }
      }
    } catch (error) {
      console.error('初始化用户状态失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 从服务器获取用户信息（内部辅助函数） - 适配后端实际接口
  const fetchUserProfileFromServer = async (): Promise<User | null> => {
    try {
      loading.value = true
      // 注意：根据错误信息，/api/user/profile接口不存在
      // 作为降级方案，我们尝试从localStorage获取用户信息
      const savedUser = localStorage.getItem('user')
      if (savedUser && savedUser !== 'undefined' && savedUser.trim() !== '') {
        try {
          user.value = JSON.parse(savedUser)
          // 在获取到用户信息后，尝试获取订阅信息
          if (user.value?.id) {
            await fetchSubscription()
          }
          return user.value
        } catch (parseError) {
          console.error('解析本地用户信息失败:', parseError)
          localStorage.removeItem('user')
        }
      }
      return null
    } catch (error) {
      console.error('获取用户信息过程中出现错误:', error)
      return null
    } finally {
      loading.value = false
    }
  }

  // 登录 - 增强版，适配后端实际响应格式
  const login = async (username: string, password: string) => {
    try {
      loading.value = true
      const response = await api.post('/api/user/login', { username, password })

      // 打印完整响应以便调试
      console.log('完整的登录响应:', response)

      // 提取token（根据实际响应格式）
      const responseData = response.data || {}
      // 尝试多种可能的token路径
      const tokenData = responseData.token || responseData.data?.token || responseData.result?.token
      if (!tokenData) {
        console.log('未能找到token，响应数据结构:', responseData)
        throw new Error('登录失败：未获取到token')
      }

      // 更新状态
      token.value = tokenData

      // 提取用户信息（尝试多种可能的路径）
      let userInfo = null
      if (responseData.userInfo) {
        userInfo = responseData.userInfo
      } else if (responseData.data?.userInfo) {
        userInfo = responseData.data.userInfo
      } else if (responseData.result?.userInfo) {
        userInfo = responseData.result.userInfo
      } else if (responseData.user) {
        userInfo = responseData.user
      } else if (responseData.data?.user) {
        userInfo = responseData.data.user
      }

      // 记录找到的用户信息
      console.log('提取到的用户信息:', userInfo)

      // 更新用户信息
      user.value = userInfo

      // 持久化存储 - 设置token过期时间（默认7天）
      const expiryTime = Date.now() + (7 * 24 * 60 * 60 * 1000) // 7天有效期
      localStorage.setItem('token', token.value)
      localStorage.setItem('tokenExpiry', expiryTime.toString())

      // 存储用户信息
      if (userInfo) {
        localStorage.setItem('user', JSON.stringify(userInfo))
      }

      // 设置api实例默认headers
      api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`

      // 获取订阅信息
      await fetchSubscription()

      ElMessage.success('登录成功')
      return { success: true, data: response.data }
    } catch (error: any) {
      const message = error.response?.data?.message || error.message || '登录失败'
      ElMessage.error(message)
      return {
        success: false,
        message
      }
    } finally {
      loading.value = false
    }
  }

  // 注册
  const register = async (username: string, password: string, _email?: string, nickname?: string, additionalInfo?: {
    phone?: string
    interestTag?: string
    identityTag?: string
    learningGoalWords?: number
    targetReadingTime?: number
  }) => {
    try {
      loading.value = true
      // 构建完整的注册数据
      const registerData = {
        username,
        password,
        ...(additionalInfo || {})
      }

      const response = await api.post('/api/user/register', registerData)

      ElMessage.success('注册成功')
      return { success: true, data: response.data }
    } catch (error: any) {
      const message = error.response?.data?.message || error.message || '注册失败'
      ElMessage.error(message)
      return {
        success: false,
        message
      }
    } finally {
      loading.value = false
    }
  }

  // 登出 - 增强版，完全清除所有用户状态相关数据
  const logout = () => {
    // 清除状态
    token.value = ''
    user.value = null
    loading.value = false
    tier.value = 'free'
    aiCalls.value = 0
    subscription.value = null

    // 清除所有相关的持久化存储
    localStorage.removeItem('token')
    localStorage.removeItem('tokenExpiry')
    localStorage.removeItem('user')

    // 清除api实例默认headers
    delete api.defaults.headers.common['Authorization']

    ElMessage.info('已登出')
  }

  // 获取用户信息 - 增强版，适配后端实际接口
  const fetchUserProfile = async (): Promise<User | null> => {
    try {
      loading.value = true

      // 直接调用我们已经修改过的fetchUserProfileFromServer函数
      // 该函数会优先从localStorage获取用户信息
      return await fetchUserProfileFromServer()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    } finally {
      loading.value = false
    }
  }

  // 更新用户信息 - 仅更新本地信息，后端暂不支持此功能
  const updateProfile = async (data: Partial<User>) => {
    try {
      if (user.value) {
        user.value = { ...user.value, ...data }
      } else if (data.id && data.username) {
        user.value = { ...data } as User
      }
      localStorage.setItem('user', JSON.stringify(user.value))
      ElMessage.success('个人信息已更新')
      return { success: true }
    } catch (error: any) {
      console.error('更新个人信息失败:', error)
      return {
        success: false,
        message: '更新个人信息失败'
      }
    }
  }

  // 检查AI调用配额
  const checkAiQuota = async () => {
    // 如果有有效订阅且启用AI功能，则无限制
    if (hasAIFeatures.value) {
      return true
    }

    // 免费用户每天可使甲10次AI功能
    if (tier.value === 'free' && aiCalls.value >= 10) {
      ElMessage.warning('今日AI调用次数已用完，明天可继续使用或升级会员')
      return false
    }

    aiCalls.value++
    return true
  }

  // 获取用户订阅信息 - 增强错误处理和日志记录
  const fetchSubscription = async () => {
    if (!user.value?.id) {
      console.warn('获取订阅信息失败: 用户ID不存在')
      return
    }

    try {
      const userId = user.value.id
      console.log('正在获取用户订阅信息，用户ID:', userId, '类型:', typeof userId)

      // 不再强制转换为数字，直接传递原始类型
      // 注意：响应拦截器直接返回response.data，所以不需要再访问.data属性
      const response = await subscriptionApi.getCurrentSubscription(userId)

      // 检查响应格式，如果包含success和data字段，则使用data
      let subscriptionData
      if (typeof response === 'object' && 'success' in response && 'data' in response) {
        subscriptionData = response.data
      } else {
        subscriptionData = response
      }

      if (subscriptionData) {
        console.log('成功获取订阅信息:', subscriptionData)
        subscription.value = subscriptionData
        // 试用用户应该被当作专业会员对待
        const planType = subscriptionData.planType?.toLowerCase()
        if (planType === 'trial') {
          tier.value = 'pro'
        } else {
          tier.value = planType as 'basic' | 'pro' | 'enterprise' || 'free'
        }
      } else {
        console.log('用户没有有效订阅')
        subscription.value = null
        tier.value = 'free'
      }
    } catch (error: any) {
      // 增强错误日志，记录完整错误信息
      console.error('获取订阅信息失败:', error)
      if (error.response) {
        console.error('API响应错误状态:', error.response.status)
        console.error('API响应错误数据:', error.response.data)
        console.error('API请求URL:', error.response.config.url)
      }
      subscription.value = null
      tier.value = 'free'
    }
  }

  return {
    user,
    token,
    loading,
    tier,
    aiCalls,
    subscription,
    isTrialActive,
    hasUsedTrial,
    isLoggedIn,
    userInfo,
    userStage,
    userTier,
    hasAIFeatures,
    login,
    register,
    logout,
    fetchUserProfile,
    updateProfile,
    checkAiQuota,
    fetchSubscription,
    initializeUser
  }
})

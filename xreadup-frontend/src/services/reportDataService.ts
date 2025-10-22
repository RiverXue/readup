// 统一的报告数据服务
import { reportApi } from '@/utils/api'
import { dataCache } from '@/utils/dataCache'
import { ElMessage } from 'element-plus'

export interface ReportDataResult {
  dashboard: any | null
  todaySummary: any | null
  weeklyInsights: any | null
  errors: string[]
}

export class ReportDataService {
  /**
   * 加载仪表盘数据
   */
  async loadDashboardData(userId: string): Promise<any> {
    try {
      const cacheKey = `dashboard_${userId}`
      const cached = dataCache.get(cacheKey)
      if (cached) return cached

      console.log('Loading dashboard data for userId:', userId)
      const response = await reportApi.getDashboardData(userId)
      console.log('Dashboard API response:', response)
      
      dataCache.set(cacheKey, response.data)
      return response.data
    } catch (error) {
      console.error('加载仪表盘数据失败:', error)
      throw new Error('仪表盘数据加载失败，请稍后重试')
    }
  }

  /**
   * 加载今日总结数据
   */
  async loadTodaySummary(userId: string): Promise<any> {
    try {
      const cacheKey = `today_${userId}_${new Date().toDateString()}`
      const cached = dataCache.get(cacheKey)
      if (cached) return cached

      const response = await reportApi.getTodaySummary(userId)
      dataCache.set(cacheKey, response.data)
      return response.data
    } catch (error) {
      console.error('加载今日总结失败:', error)
      throw new Error('今日总结加载失败，请稍后重试')
    }
  }

  /**
   * 加载周报数据
   */
  async loadWeeklyInsights(userId: string): Promise<any> {
    try {
      const weekKey = this.getWeekKey()
      const cacheKey = `weekly_${userId}_${weekKey}`
      const cached = dataCache.get(cacheKey)
      if (cached) return cached

      const response = await reportApi.getWeeklyInsights(userId)
      dataCache.set(cacheKey, response.data)
      return response.data
    } catch (error) {
      console.error('加载周报数据失败:', error)
      throw new Error('周报数据加载失败，请稍后重试')
    }
  }

  /**
   * 加载词汇增长数据
   */
  async loadVocabularyData(userId: string, days: number = 30): Promise<any> {
    try {
      const cacheKey = `vocabulary_${userId}_${days}`
      const cached = dataCache.get(cacheKey)
      if (cached) return cached

      const response = await reportApi.getGrowthCurve(userId, days)
      dataCache.set(cacheKey, response.data)
      return response.data
    } catch (error) {
      console.error('加载词汇数据失败:', error)
      throw new Error('词汇数据加载失败，请稍后重试')
    }
  }

  /**
   * 加载阅读时长数据
   */
  async loadReadingData(userId: string, days: number = 30): Promise<any> {
    try {
      const cacheKey = `reading_${userId}_${days}`
      const cached = dataCache.get(cacheKey)
      if (cached) return cached

      const response = await reportApi.getReadingTime(userId, days)
      dataCache.set(cacheKey, response.data)
      return response.data
    } catch (error) {
      console.error('加载阅读数据失败:', error)
      throw new Error('阅读数据加载失败，请稍后重试')
    }
  }

  /**
   * 加载历史数据
   */
  async loadHistoricalData(userId: string, startDate: string, endDate: string): Promise<any> {
    try {
      const cacheKey = `historical_${userId}_${startDate}_${endDate}`
      const cached = dataCache.get(cacheKey)
      if (cached) return cached

      const response = await reportApi.getHistoricalData(userId, startDate, endDate)
      dataCache.set(cacheKey, response.data)
      return response.data
    } catch (error) {
      console.error('加载历史数据失败:', error)
      throw new Error('历史数据加载失败，请稍后重试')
    }
  }

  /**
   * 并行加载所有数据
   */
  async loadAllData(userId: string): Promise<ReportDataResult> {
    try {
      const [dashboard, todaySummary, weeklyInsights] = await Promise.allSettled([
        this.loadDashboardData(userId),
        this.loadTodaySummary(userId),
        this.loadWeeklyInsights(userId)
      ])

      const errors: string[] = []
      
      // 处理失败的结果
      if (dashboard.status === 'rejected') {
        errors.push('仪表盘数据加载失败')
      }
      if (todaySummary.status === 'rejected') {
        errors.push('今日总结加载失败')
      }
      if (weeklyInsights.status === 'rejected') {
        errors.push('周报数据加载失败')
      }

      // 如果有部分数据加载失败，显示警告
      if (errors.length > 0 && errors.length < 3) {
        ElMessage.warning(`部分数据加载失败: ${errors.join(', ')}`)
      }

      return {
        dashboard: dashboard.status === 'fulfilled' ? dashboard.value : null,
        todaySummary: todaySummary.status === 'fulfilled' ? todaySummary.value : null,
        weeklyInsights: weeklyInsights.status === 'fulfilled' ? weeklyInsights.value : null,
        errors
      }
    } catch (error) {
      console.error('加载所有数据失败:', error)
      ElMessage.error('数据加载失败，请稍后重试')
      throw error
    }
  }

  /**
   * 清除用户相关缓存
   */
  clearUserCache(userId: string) {
    const keys = dataCache.getStats().keys
    keys.forEach(key => {
      if (key.includes(userId)) {
        dataCache.delete(key)
      }
    })
  }

  /**
   * 获取周键
   */
  private getWeekKey(): string {
    const now = new Date()
    const startOfWeek = new Date(now.setDate(now.getDate() - now.getDay()))
    return startOfWeek.toISOString().split('T')[0]
  }

  /**
   * 重试失败的请求
   */
  async retryFailedRequest<T>(
    requestFn: () => Promise<T>,
    maxRetries: number = 3,
    delay: number = 1000
  ): Promise<T> {
    let lastError: Error
    
    for (let i = 0; i < maxRetries; i++) {
      try {
        return await requestFn()
      } catch (error) {
        lastError = error as Error
        if (i < maxRetries - 1) {
          console.log(`请求失败，${delay}ms后重试 (${i + 1}/${maxRetries})`)
          await new Promise(resolve => setTimeout(resolve, delay))
          delay *= 2 // 指数退避
        }
      }
    }
    
    throw lastError!
  }
}

// 创建全局服务实例
export const reportDataService = new ReportDataService()

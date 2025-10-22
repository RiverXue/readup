<template>
  <div class="enhanced-report-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>📊 学习报告</h1>
        <div class="header-actions">
          <el-button @click="refreshData" :loading="loading.all" type="primary">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
          <el-button @click="showAchievements" type="success">
            <el-icon><Trophy /></el-icon>
            查看成就
          </el-button>
        </div>
        </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
       <!-- 数据概览 -->
       <DataOverviewSection 
         :dashboardData="dashboardData"
         :loading="loading.overview"
         @cardClick="(type: string) => handleCardClick({ type: type as any })"
       />
      
      <!-- 图表展示 -->
      <ChartsSection 
        :vocabularyData="vocabularyData"
        :readingData="readingData"
        :loading="loading.charts"
        @chartClick="handleChartClick"
      />
      
      <!-- 学习报告 -->
      <ReportsSection 
        :todaySummary="todaySummary"
        :weeklyInsights="weeklyInsights"
        :loading="loading.reports"
      />
      
       <!-- 数据对比 -->
       <ComparisonSection 
         :currentData="currentData"
         :historicalData="historicalData"
         :loading="loading.comparison"
         @comparisonChange="(type: string) => handleComparisonChange({ type: type as any })"
       />
      
      
      <!-- 学习成就区域 -->
      <AchievementSection 
        :achievementData="achievementData"
        :loading="loading.achievements"
        @achievementClick="handleAchievementClick"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { reportApi, learningApi } from '@/utils/api'
import { useUserStore } from '@/stores/user'
import { dataCache } from '@/utils/dataCache'
import { reportDataService } from '@/services/reportDataService'
import { 
  getRealAchievementDate, 
  getRealStreakDate, 
  getFirstLearningDate, 
  getFirstWeekDate, 
  getFirstMonthDate,
  getWeekKey 
} from '@/utils/dateUtils'
import type { 
  DashboardData, 
  VocabularyGrowthData, 
  ReadingTimeData, 
  TodaySummary, 
  WeeklyInsights, 
  AchievementData, 
  DailyReading, 
  LoadingState,
  ChartClickEvent,
  CardClickEvent,
  ComparisonChangeEvent,
  AchievementClickEvent
} from '@/types/report'

// 组件导入
import DataOverviewSection from './components/DataOverviewSection.vue'
import ChartsSection from './components/ChartsSection.vue'
import ReportsSection from './components/ReportsSection.vue'
import ComparisonSection from './components/ComparisonSection.vue'
import AchievementSection from './components/AchievementSection.vue'

const userStore = useUserStore()

// 响应式数据
const dashboardData = ref<DashboardData | null>(null)
const vocabularyData = ref<VocabularyGrowthData | null>(null)
const readingData = ref<ReadingTimeData | null>(null)
const todaySummary = ref<TodaySummary | null>(null)
const weeklyInsights = ref<WeeklyInsights | null>(null)
const currentData = ref<DailyReading[] | null>(null)
const historicalData = ref<DailyReading[] | null>(null)
const achievementData = ref<AchievementData | null>(null)

// 加载状态
const loading = ref<LoadingState>({
  all: false,
  overview: false,
  charts: false,
  reports: false,
  comparison: false,
  achievements: false
})

// 使用统一的数据缓存服务

// 计算属性
const userId = computed(() => {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    console.warn('用户未登录或用户信息缺失')
    return null
  }
  const id = userStore.userInfo.id.toString()
  console.log('userId computed:', id, 'userStore.userInfo:', userStore.userInfo, 'isLoggedIn:', userStore.isLoggedIn)
  return id
})


// 方法
const loadAllData = async () => {
  console.log('loadAllData called, userId:', userId.value, 'userStore.userInfo:', userStore.userInfo)
  if (!userId.value) {
    ElMessage.warning('请先登录后再查看学习报告')
    return
  }

  loading.value.all = true
  
  try {
    // 使用统一的数据服务加载所有数据
    const result = await reportDataService.loadAllData(userId.value)
    
    // 处理数据
    await processAllData(result)
    
    // 显示成功消息（如果没有错误）
    if (result.errors.length === 0) {
      ElMessage.success('数据加载完成')
    }
  } catch (error) {
    console.error('数据加载失败:', error)
    ElMessage.error('数据加载失败，请稍后重试')
  } finally {
    loading.value.all = false
  }
}

// 移除重复的加载方法，统一使用 reportDataService

const processAllData = async (result: any) => {
  try {
    // 使用nextTick确保DOM更新完成
    await nextTick()
    
    // 明确数据来源，确保数据完整性
    dashboardData.value = result.dashboard
    vocabularyData.value = result.dashboard?.vocabularyData || null
    readingData.value = result.dashboard?.readingData || null
    todaySummary.value = result.todaySummary
    weeklyInsights.value = result.weeklyInsights
    
    console.log('Processed data:', {
      dashboard: dashboardData.value,
      vocabulary: vocabularyData.value,
      reading: readingData.value,
      today: todaySummary.value,
      weekly: weeklyInsights.value
    })
    
    // 并行处理对比数据和成就数据，避免使用setTimeout
    await Promise.all([
      processComparisonData(),
      processAchievementData()
    ])
    
    // 确保DOM更新完成
    await nextTick()
  } catch (error) {
    console.error('处理数据时出错:', error)
    ElMessage.error('数据处理失败，请刷新页面重试')
  }
}

const processComparisonData = async () => {
  try {
    console.log('processComparisonData called, readingData:', readingData.value)
    
    if (readingData.value?.dailyReadings && readingData.value.dailyReadings.length > 0) {
      currentData.value = readingData.value.dailyReadings.slice(-7)
      console.log('currentData set:', currentData.value)
      
      // 获取真实的历史数据
      const endDate = new Date()
      endDate.setDate(endDate.getDate() - 7) // 上周结束日期
      const startDate = new Date()
      startDate.setDate(startDate.getDate() - 14) // 上周开始日期
      
      const startDateStr = startDate.toISOString().split('T')[0]
      const endDateStr = endDate.toISOString().split('T')[0]
      
      console.log('请求历史数据:', {
        userId: userId.value,
        startDate: startDateStr,
        endDate: endDateStr
      })
      
      const response = await reportApi.getHistoricalData(
        userId.value!.toString(),
        startDateStr,
        endDateStr
      )
      
      console.log('历史数据API响应:', response.data)
      
      if (response.data && response.data.dailyReadings && response.data.dailyReadings.length > 0) {
        // 处理历史数据格式
        historicalData.value = response.data.dailyReadings
        console.log('历史数据加载成功:', historicalData.value)
      } else {
        console.warn('历史数据为空或格式不正确，响应:', response.data)
        historicalData.value = []
      }
    } else {
      console.warn('readingData.dailyReadings为空或不存在')
      currentData.value = []
      historicalData.value = []
    }
  } catch (error) {
    console.error('处理对比数据失败:', error)
    currentData.value = []
    historicalData.value = []
  }
}

const processAchievementData = async () => {
  try {
    if (dashboardData.value) {
      achievementData.value = generateAchievementData()
    }
  } catch (error) {
    console.error('处理成就数据失败:', error)
  }
}

const generateAchievementData = () => {
  if (!dashboardData.value) return null
  
  const data = dashboardData.value
  const achievements = []
  const milestones = []
  
  // 基于真实数据生成成就
    if (data.currentStreak >= 7) {
      achievements.push({
        id: 'streak_7',
        title: '一周坚持',
        description: '连续学习7天',
        icon: '🔥',
        unlockedAt: getRealStreakDate(data.readingData?.dailyReadings, 7),
        type: 'streak' as const
      })
    }
  
  if (data.currentStreak >= 30) {
    achievements.push({
      id: 'streak_30',
      title: '月度坚持',
      description: '连续学习30天',
      icon: '💪',
      unlockedAt: getRealStreakDate(data.readingData?.dailyReadings, 30),
      type: 'streak' as const
    })
  }
  
  if (data.vocabularyData?.totalWords >= 50) {
    achievements.push({
      id: 'vocab_50',
      title: '词汇入门',
      description: '掌握50个词汇',
      icon: '📝',
      unlockedAt: getRealAchievementDate(data.vocabularyData?.dates, 'words', 50),
      type: 'vocabulary' as const
    })
  }
  
  if (data.vocabularyData?.totalWords >= 100) {
    achievements.push({
      id: 'vocab_100',
      title: '词汇达人',
      description: '掌握100个词汇',
      icon: '📚',
      unlockedAt: getRealAchievementDate(data.vocabularyData?.dates, 'words', 100),
      type: 'vocabulary' as const
    })
  }
  
  if (data.readingData?.totalArticles >= 5) {
    achievements.push({
      id: 'reader_5',
      title: '阅读新手',
      description: '阅读5篇文章',
      icon: '📖',
      unlockedAt: getRealAchievementDate(data.readingData?.dailyReadings, 'articles', 5),
      type: 'reading' as const
    })
  }
  
  if (data.readingData?.totalArticles >= 10) {
    achievements.push({
      id: 'reader_10',
      title: '阅读达人',
      description: '阅读10篇文章',
      icon: '📚',
      unlockedAt: getRealAchievementDate(data.readingData?.dailyReadings, 'articles', 10),
      type: 'reading' as const
    })
  }
  
  if (data.readingData?.totalMinutes >= 1000) {
    achievements.push({
      id: 'reading_1000',
      title: '阅读大师',
      description: '累计阅读1000分钟',
      icon: '⏰',
      unlockedAt: getRealAchievementDate(data.readingData?.dailyReadings, 'minutes', 1000),
      type: 'reading' as const
    })
  }
  
  // 生成里程碑数据
  milestones.push({
    id: 'first_day',
    title: '第一天',
    description: '开始学习之旅',
    date: getFirstLearningDate(data.readingData?.dailyReadings),
    achieved: data.totalDays >= 1,
    type: 'time' as const
  })
  
  milestones.push({
    id: 'first_week',
    title: '第一周',
    description: '坚持学习一周',
    date: getFirstWeekDate(data.readingData?.dailyReadings),
    achieved: data.totalDays >= 7,
      type: 'time' as const
  })
  
  milestones.push({
    id: 'first_month',
    title: '第一个月',
    description: '坚持学习一个月',
    date: getFirstMonthDate(data.readingData?.dailyReadings),
    achieved: data.totalDays >= 30,
      type: 'time' as const
  })
  
  milestones.push({
    id: 'vocab_milestone',
    title: '词汇里程碑',
    description: '掌握100个词汇',
    date: getRealAchievementDate(data.vocabularyData?.dates, 'words', 100),
    achieved: data.vocabularyData?.totalWords >= 100,
    type: 'vocabulary' as const
  })
  
  milestones.push({
    id: 'reading_milestone',
    title: '阅读里程碑',
    description: '阅读10篇文章',
    date: getRealAchievementDate(data.readingData?.dailyReadings, 'articles', 10),
    achieved: data.readingData?.totalArticles >= 10,
    type: 'reading' as const
  })
  
  // 按时间排序成就和里程碑
  achievements.sort((a: any, b: any) => new Date(a.unlockedAt).getTime() - new Date(b.unlockedAt).getTime())
  milestones.sort((a: any, b: any) => new Date(a.date).getTime() - new Date(b.date).getTime())
  
  return {
    achievements,
    milestones,
    totalAchievements: achievements.length,
    totalMilestones: milestones.length
  }
}

// 日期处理函数已移至 @/utils/dateUtils.ts

const showAchievements = () => {
  // 显示成就详情弹窗
  console.log('显示成就详情')
}

// 数据交互功能
const handleCardClick = (event: CardClickEvent) => {
  console.log('卡片点击:', event.type)
  
  switch (event.type) {
    case 'vocabulary':
      ElMessage.info('查看词汇详情')
      // 可以跳转到词汇详情页面或显示词汇弹窗
      break
    case 'reading':
      ElMessage.info('查看阅读详情')
      // 可以跳转到阅读详情页面或显示阅读弹窗
      break
    case 'articles':
      ElMessage.info('查看文章详情')
      // 可以跳转到文章列表页面
      break
    case 'streak':
      ElMessage.info('查看连续学习详情')
      // 可以显示连续学习历史
      break
    default:
      console.log('未知卡片类型:', event.type)
  }
}

const handleChartClick = (event: ChartClickEvent) => {
  console.log('图表点击:', event.type, event.data)
  
  switch (event.type) {
    case 'vocabulary':
      if (event.date) {
        ElMessage.info(`查看 ${event.date} 的词汇详情`)
        // 可以显示该日期的详细词汇数据
      }
      break
    case 'reading':
      if (event.date) {
        ElMessage.info(`查看 ${event.date} 的阅读详情`)
        // 可以显示该日期的详细阅读数据
      }
      break
    case 'difficulty':
      ElMessage.info(`查看 ${event.data.difficulty} 难度文章详情`)
      // 可以显示该难度级别的文章列表
      break
    case 'efficiency':
      ElMessage.info('查看学习效率详情')
      // 可以显示学习效率分析详情
      break
    default:
      console.log('未知图表类型:', event.type)
  }
}

const handleComparisonChange = (event: ComparisonChangeEvent) => {
  console.log('对比类型变更:', event.type)
  
  // 根据对比类型加载不同的历史数据
  const days = event.type === 'weekly' ? 7 : event.type === 'monthly' ? 30 : 365
  loadHistoricalData(days)
}

const handleAchievementClick = (event: AchievementClickEvent) => {
  console.log('成就点击:', event.achievement)
  ElMessage.success(`恭喜获得成就：${event.achievement.title}`)
  // 可以显示成就详情弹窗
}

// 加载历史数据用于对比
const loadHistoricalData = async (days: number) => {
  if (!userId.value) return
  
  loading.value.comparison = true
  try {
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(startDate.getDate() - days)
    
    const data = await reportDataService.loadHistoricalData(
      userId.value,
      startDate.toISOString().split('T')[0],
      endDate.toISOString().split('T')[0]
    )
    
    historicalData.value = data.dailyReadings || []
    ElMessage.success(`已加载最近${days}天的历史数据`)
  } catch (error) {
    console.error('加载历史数据失败:', error)
    ElMessage.error('历史数据加载失败')
  } finally {
    loading.value.comparison = false
  }
}

// 事件处理（已在上方定义，这里删除重复定义）

// 图表点击和对比变更方法已在上方定义

const refreshData = () => {
  if (userId.value) {
    reportDataService.clearUserCache(userId.value)
  }
  loadAllData()
}

// getWeekKey 函数已移至 @/utils/dateUtils.ts

// 生命周期
onMounted(() => {
  console.log('ReportPage mounted, userStore:', userStore)
  console.log('isLoggedIn:', userStore.isLoggedIn)
  console.log('userInfo:', userStore.userInfo)
  console.log('token:', userStore.token)
  
  if (userStore.isLoggedIn) {
    loadAllData()
  } else {
    ElMessage.warning('请先登录')
  }
})
</script>

<style scoped>
.enhanced-report-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  background: #f8f9fa;
  min-height: 100vh;
}

.page-header {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #2d3748;
  background: linear-gradient(135deg, #409eff, #67c23a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

@media (max-width: 768px) {
  .enhanced-report-container {
    padding: 16px;
  }
  
  .page-header {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .header-content h1 {
    font-size: 24px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .main-content {
    gap: 24px;
  }
}

@media (max-width: 480px) {
  .enhanced-report-container {
    padding: 12px;
  }
  
  .page-header {
    padding: 12px;
  }
  
  .header-content h1 {
    font-size: 20px;
  }
  
  .header-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .header-actions .el-button {
    width: 100%;
  }
}
</style>

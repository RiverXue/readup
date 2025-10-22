// 学习报告相关类型定义

// 基础API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  success: boolean
}

// 仪表盘数据类型
export interface DashboardData {
  vocabularyData: VocabularyGrowthData
  readingData: ReadingTimeData
  currentStreak: number
  totalDays: number
}

// 词汇增长数据类型
export interface VocabularyGrowthData {
  totalWords: number
  weeklyNewWords: number
  dates: string[]
  counts: number[]
}

// 阅读时长数据类型
export interface ReadingTimeData {
  totalMinutes: number
  totalArticles: number
  dailyReadings: DailyReading[]
  difficultyStats: DifficultyStat[]
  todayMinutes: number
  todayArticles: number
  averageMinutes: number
}

// 每日阅读数据类型
export interface DailyReading {
  date: string
  minutes: number
  articles: number
  newWords: number
}

// 难度统计类型
export interface DifficultyStat {
  difficulty: string
  count: number
}

// 今日总结类型
export interface TodaySummary {
  dailyNewWords: number
  todayMinutes: number
  todayArticles: number
  vocabularyCount: number
  readingStreak: number
}

// 周报洞察类型
export interface WeeklyInsights {
  totalMinutes: number
  totalArticles: number
  averageMinutes: number
  insights: Insight[]
}

// 洞察类型
export interface Insight {
  id: string
  type: 'positive' | 'suggestion' | 'achievement' | 'warning'
  title: string
  content: string
  action?: string
  actionText?: string
}

// 成就数据类型
export interface AchievementData {
  achievements: Achievement[]
  milestones: Milestone[]
  totalAchievements: number
  totalMilestones: number
}

// 成就类型
export interface Achievement {
  id: string
  title: string
  description: string
  icon: string
  unlockedAt: string
  type: 'streak' | 'vocabulary' | 'reading'
}

// 里程碑类型
export interface Milestone {
  id: string
  title: string
  description: string
  date: string
  achieved: boolean
  type: 'time' | 'vocabulary' | 'reading'
}

// 历史数据类型
export interface HistoricalData {
  dailyReadings: DailyReading[]
  totalMinutes: number
  averagePerDay: number
  vocabularyData: VocabularyGrowthData
}

// 图表配置类型
export interface ChartConfig {
  type: 'line' | 'bar' | 'pie' | 'radar'
  title: string
  data: any[]
  options?: any
}

// 用户偏好类型
export interface UserPreferences {
  chartType: 'line' | 'bar'
  timeRange: '7' | '30' | '90'
  theme: 'light' | 'dark'
  layout: 'grid' | 'list'
  language: 'zh' | 'en'
}

// 加载状态类型
export interface LoadingState {
  all: boolean
  overview: boolean
  charts: boolean
  reports: boolean
  comparison: boolean
  achievements: boolean
}

// 错误类型
export interface ReportError {
  type: 'network' | 'data' | 'permission' | 'unknown'
  message: string
  code?: string
  timestamp: number
}

// 数据服务结果类型
export interface ReportDataResult {
  dashboard: DashboardData | null
  todaySummary: TodaySummary | null
  weeklyInsights: WeeklyInsights | null
  errors: string[]
}

// 缓存统计类型
export interface CacheStats {
  size: number
  keys: string[]
  expiry: number
}

// 组件Props类型
export interface DataOverviewSectionProps {
  dashboardData: DashboardData | null
  loading: boolean
}

export interface ChartsSectionProps {
  vocabularyData: VocabularyGrowthData | null
  readingData: ReadingTimeData | null
  loading: boolean
}

export interface ReportsSectionProps {
  todaySummary: TodaySummary | null
  weeklyInsights: WeeklyInsights | null
  loading: boolean
}

export interface ComparisonSectionProps {
  currentData: DailyReading[] | null
  historicalData: DailyReading[] | null
  loading: boolean
}

export interface AchievementSectionProps {
  achievementData: AchievementData | null
  loading: boolean
}

// 事件类型
export interface ChartClickEvent {
  type: string
  data: any
  date?: string
}

export interface CardClickEvent {
  type: 'vocabulary' | 'reading' | 'articles' | 'streak'
}

export interface ComparisonChangeEvent {
  type: 'weekly' | 'monthly' | 'yearly'
}

export interface AchievementClickEvent {
  achievement: Achievement
}

// 工具函数类型
export type DateFormatter = (date: string) => string
export type NumberFormatter = (num: number) => string
export type ColorGenerator = (difficulty: string) => string
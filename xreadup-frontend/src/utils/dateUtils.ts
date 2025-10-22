// 统一的日期处理工具

/**
 * 格式化日期为 YYYY-MM-DD 格式
 */
export const formatDate = (date: Date | string): string => {
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toISOString().split('T')[0]
}

/**
 * 格式化日期为 MM/DD 格式
 */
export const formatDateShort = (date: Date | string): string => {
  const d = typeof date === 'string' ? new Date(date) : date
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')
  return `${month}/${day}`
}

/**
 * 获取周键（周一的日期）
 */
export const getWeekKey = (): string => {
  const now = new Date()
  const startOfWeek = new Date(now.setDate(now.getDate() - now.getDay()))
  return formatDate(startOfWeek)
}

/**
 * 获取指定天数前的日期
 */
export const getDateBefore = (days: number): string => {
  const date = new Date()
  date.setDate(date.getDate() - days)
  return formatDate(date)
}

/**
 * 获取日期范围
 */
export const getDateRange = (days: number): { startDate: string; endDate: string } => {
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - days)
  
  return {
    startDate: formatDate(startDate),
    endDate: formatDate(endDate)
  }
}

/**
 * 从数据中获取真实成就日期
 */
export const getRealAchievementDate = (data: any[], field: string, target: number): string => {
  if (!data || data.length === 0) return new Date().toISOString()
  
  let cumulative = 0
  for (let i = data.length - 1; i >= 0; i--) {
    const item = data[i]
    if (field === 'words' && item.count) {
      cumulative += item.count
    } else if (field === 'articles' && item.articles) {
      cumulative += item.articles
    } else if (field === 'minutes' && item.minutes) {
      cumulative += item.minutes
    }
    
    if (cumulative >= target) {
      return new Date(item.date || item).toISOString()
    }
  }
  
  return new Date().toISOString()
}

/**
 * 获取真实连续学习日期
 */
export const getRealStreakDate = (data: any[], streakDays: number): string => {
  if (!data || data.length === 0) return new Date().toISOString()
  
  let currentStreak = 0
  const today = new Date()
  
  for (let i = 0; i < streakDays; i++) {
    const checkDate = new Date(today)
    checkDate.setDate(today.getDate() - i)
    const dateStr = formatDate(checkDate)
    
    const hasData = data.some((item: any) => {
      const itemDate = formatDate(item.date)
      return itemDate === dateStr && (item.minutes > 0 || item.articles > 0)
    })
    
    if (hasData) {
      currentStreak++
    } else {
      break
    }
  }
  
  if (currentStreak >= streakDays) {
    const achievementDate = new Date(today)
    achievementDate.setDate(today.getDate() - (streakDays - 1))
    return achievementDate.toISOString()
  }
  
  return new Date().toISOString()
}

/**
 * 获取第一次学习日期
 */
export const getFirstLearningDate = (data: any[]): string => {
  if (!data || data.length === 0) return new Date().toISOString()
  
  const sortedData = [...data].sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())
  return new Date(sortedData[0].date).toISOString()
}

/**
 * 获取第一周学习日期
 */
export const getFirstWeekDate = (data: any[]): string => {
  if (!data || data.length === 0) return new Date().toISOString()
  
  const sortedData = [...data].sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())
  const firstDate = new Date(sortedData[0].date)
  firstDate.setDate(firstDate.getDate() + 6) // 第一周结束日期
  return firstDate.toISOString()
}

/**
 * 获取第一个月学习日期
 */
export const getFirstMonthDate = (data: any[]): string => {
  if (!data || data.length === 0) return new Date().toISOString()
  
  const sortedData = [...data].sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())
  const firstDate = new Date(sortedData[0].date)
  firstDate.setMonth(firstDate.getMonth() + 1) // 第一个月结束日期
  return firstDate.toISOString()
}

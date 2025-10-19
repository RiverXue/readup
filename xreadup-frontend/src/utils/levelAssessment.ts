/**
 * 用户学习等级评估工具
 * 统一的等级评估算法，确保整个应用的一致性
 */

export type UserLevel = 'beginner' | 'intermediate' | 'advanced' | 'expert'

/**
 * 评估用户当前学习水平
 * @param learningDays 学习天数
 * @param articlesRead 已读文章数
 * @param masteredWords 已掌握词汇数（从生词本中掌握的词汇）
 * @param totalWords 生词本总词汇数
 * @returns 用户学习等级
 */
export const assessUserLevel = (
  learningDays: number, 
  articlesRead: number, 
  masteredWords: number, 
  totalWords: number = 0
): UserLevel => {
  // 计算词汇掌握率（已掌握词汇 / 总生词数）
  const masteryRate = totalWords > 0 ? (masteredWords / totalWords) : 0
  
  // 计算阅读强度分数（阅读量权重）
  const readingIntensity = articlesRead / Math.max(learningDays, 1) // 每天平均阅读文章数
  
  // 专家级别：90天+学习，50+文章，已掌握500+词汇，掌握率80%+
  if (learningDays >= 90 && articlesRead >= 50 && masteredWords >= 500 && masteryRate >= 0.8) return 'expert'
  
  // 高级级别：60天+学习，30+文章，已掌握200+词汇，掌握率70%+
  if (learningDays >= 60 && articlesRead >= 30 && masteredWords >= 200 && masteryRate >= 0.7) return 'advanced'
  
  // 中级级别：更灵活的评估标准
  // 标准1：传统标准（30天+学习，15+文章，已掌握50+词汇，掌握率60%+）
  if (learningDays >= 30 && articlesRead >= 15 && masteredWords >= 50 && masteryRate >= 0.6) return 'intermediate'
  
  // 标准2：高阅读量用户（30天+学习，100+文章，已掌握30+词汇，掌握率30%+）
  if (learningDays >= 30 && articlesRead >= 100 && masteredWords >= 30 && masteryRate >= 0.3) return 'intermediate'
  
  // 标准3：极高阅读量用户（30天+学习，200+文章，已掌握20+词汇）
  if (learningDays >= 30 && articlesRead >= 200 && masteredWords >= 20) return 'intermediate'
  
  // 标准5：超级阅读量用户（30天+学习，500+文章，已掌握10+词汇）
  if (learningDays >= 30 && articlesRead >= 500 && masteredWords >= 10) return 'intermediate'
  
  // 标准6：极端阅读量用户（30天+学习，700+文章，已掌握5+词汇）
  if (learningDays >= 30 && articlesRead >= 700 && masteredWords >= 5) return 'intermediate'
  
  // 标准4：长期学习用户（60天+学习，50+文章，已掌握20+词汇）
  if (learningDays >= 60 && articlesRead >= 50 && masteredWords >= 20) return 'intermediate'
  
  // 初级级别：其他情况
  return 'beginner'
}

/**
 * 兼容旧版本的评估方法（仅基于总词汇数，不推荐使用）
 * @deprecated 请使用新的assessUserLevel方法
 */
export const assessUserLevelLegacy = (learningDays: number, articlesRead: number, vocabCount: number): UserLevel => {
  // 专家级别：90天+学习，50+文章，生词本1000+词汇（说明阅读量大，遇到生词多）
  if (learningDays >= 90 && articlesRead >= 50 && vocabCount >= 1000) return 'expert'
  
  // 高级级别：60天+学习，30+文章，生词本500+词汇
  if (learningDays >= 60 && articlesRead >= 30 && vocabCount >= 500) return 'advanced'
  
  // 中级级别：30天+学习，15+文章，生词本200+词汇
  if (learningDays >= 30 && articlesRead >= 15 && vocabCount >= 200) return 'intermediate'
  
  // 初级级别：其他情况
  return 'beginner'
}

/**
 * 获取等级显示名称
 * @param level 等级代码
 * @returns 中文显示名称
 */
export const getLevelDisplayName = (level: UserLevel): string => {
  const levelNames: Record<UserLevel, string> = {
    'beginner': '初学者',
    'intermediate': '中级',
    'advanced': '高级',
    'expert': '专家'
  }
  return levelNames[level] || '初学者'
}

/**
 * 获取等级详细描述
 * @param level 等级代码
 * @returns 等级详细描述
 */
export const getLevelDescription = (level: UserLevel): string => {
  const descriptions: Record<UserLevel, string> = {
    'beginner': '刚开始学习英语，正在建立基础',
    'intermediate': '有一定基础，可以理解日常英语内容',
    'advanced': '英语水平较高，可以处理复杂内容',
    'expert': '英语水平优秀，接近母语者水平'
  }
  return descriptions[level] || '刚开始学习英语，正在建立基础'
}

/**
 * 获取等级图标
 * @param level 等级代码
 * @returns 等级图标
 */
export const getLevelIcon = (level: UserLevel): string => {
  const icons: Record<UserLevel, string> = {
    'beginner': '🌱',
    'intermediate': '🌿',
    'advanced': '🌳',
    'expert': '🏆'
  }
  return icons[level] || '🌱'
}

/**
 * 获取等级进度百分比
 * @param level 当前等级
 * @returns 进度百分比 (0-100)
 */
export const getLevelProgress = (level: UserLevel): number => {
  const progressMap: Record<UserLevel, number> = {
    'beginner': 25,
    'intermediate': 50,
    'advanced': 75,
    'expert': 100
  }
  return progressMap[level] || 25
}

/**
 * 获取下一等级信息
 * @param currentLevel 当前等级
 * @returns 下一等级信息
 */
export const getNextLevelInfo = (currentLevel: UserLevel): { level: UserLevel | null, name: string } => {
  const nextLevelMap: Record<UserLevel, { level: UserLevel | null, name: string }> = {
    'beginner': { level: 'intermediate', name: '中级' },
    'intermediate': { level: 'advanced', name: '高级' },
    'advanced': { level: 'expert', name: '专家' },
    'expert': { level: null, name: '已达到最高等级' }
  }
  return nextLevelMap[currentLevel] || { level: null, name: '未知等级' }
}

/**
 * 获取等级颜色主题
 * @param level 等级
 * @returns CSS颜色值
 */
export const getLevelColor = (level: UserLevel): string => {
  const colorMap: Record<UserLevel, string> = {
    'beginner': '#409EFF',    // 蓝色
    'intermediate': '#E6A23C', // 橙色
    'advanced': '#67C23A',    // 绿色
    'expert': '#F56C6C'       // 红色
  }
  return colorMap[level] || '#409EFF'
}

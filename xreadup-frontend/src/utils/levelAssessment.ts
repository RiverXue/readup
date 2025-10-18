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
  
  // 专家级别：90天+学习，50+文章，已掌握500+词汇，掌握率80%+
  if (learningDays >= 90 && articlesRead >= 50 && masteredWords >= 500 && masteryRate >= 0.8) return 'expert'
  
  // 高级级别：60天+学习，30+文章，已掌握200+词汇，掌握率70%+
  if (learningDays >= 60 && articlesRead >= 30 && masteredWords >= 200 && masteryRate >= 0.7) return 'advanced'
  
  // 中级级别：30天+学习，15+文章，已掌握50+词汇，掌握率60%+
  if (learningDays >= 30 && articlesRead >= 15 && masteredWords >= 50 && masteryRate >= 0.6) return 'intermediate'
  
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

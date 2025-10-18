/**
 * 学习评估阈值配置
 * 统一管理薄弱环节识别和优势识别的阈值标准
 */

export const LEARNING_THRESHOLDS = {
  // 薄弱环节识别阈值
  WEAK: {
    learningDays: 14,        // 学习天数不足14天
    totalArticlesRead: 10,   // 阅读文章不足10篇
    masteredWords: 50,       // 已掌握词汇不足50个
    readingStreak: 5,        // 连续学习不足5天
    averageReadTime: 15,     // 平均阅读时长不足15分钟
    // 词汇状态比例阈值
    newWordRate: 0.3,        // 新词比例超过30%
    learningRate: 0.4,       // 学习中比例超过40%
    reviewRate: 0.2,         // 待复习比例超过20%
    masteryRate: 0.3         // 掌握率低于30%
  },
  
  // 优势识别阈值
  STRONG: {
    learningDays: 30,        // 学习天数超过30天
    totalArticlesRead: 20,   // 阅读文章超过20篇
    masteredWords: 100,      // 已掌握词汇超过100个
    readingStreak: 7,        // 连续学习超过7天
    averageReadTime: 20      // 平均阅读时长超过20分钟
  },
  
  // 等级评估阈值
  LEVEL: {
    expert: {
      learningDays: 90,
      articlesRead: 50,
      masteredWords: 500,
      masteryRate: 0.8
    },
    advanced: {
      learningDays: 60,
      articlesRead: 30,
      masteredWords: 200,
      masteryRate: 0.7
    },
    intermediate: {
      learningDays: 30,
      articlesRead: 15,
      masteredWords: 50,
      masteryRate: 0.6
    }
  }
} as const

/**
 * 检查是否达到优势标准
 */
export const isStrong = (profile: any, metric: keyof typeof LEARNING_THRESHOLDS.STRONG): boolean => {
  const threshold = LEARNING_THRESHOLDS.STRONG[metric]
  return profile[metric] >= threshold
}

/**
 * 检查是否为薄弱环节
 */
export const isWeak = (profile: any, metric: keyof typeof LEARNING_THRESHOLDS.WEAK): boolean => {
  const threshold = LEARNING_THRESHOLDS.WEAK[metric]
  return profile[metric] < threshold
}

/**
 * 获取学习状态描述
 */
export const getLearningStatus = (profile: any, metric: keyof typeof LEARNING_THRESHOLDS.STRONG): 'strong' | 'weak' | 'normal' => {
  if (isStrong(profile, metric)) return 'strong'
  if (isWeak(profile, metric)) return 'weak'
  return 'normal'
}

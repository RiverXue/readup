/**
 * 报告服务相关类型定义
 */

/**
 * 阅读记录请求参数
 */
export interface ReadingRecordRequest {
  userId: number;
  articleId: number;
  readTimeSec: number;
}

/**
 * 每日阅读数据
 */
export interface DailyReading {
  date: string;
  minutes: number;
  articles: number;
}

/**
 * 难度分布统计
 */
export interface DifficultyStats {
  difficulty: string;
  count: number;
  totalMinutes: number;
}

/**
 * 阅读时长统计数据
 */
export interface ReadingTimeData {
  todayMinutes: number;
  weeklyAverageMinutes: number;
  totalMinutes: number;
  totalArticles: number;
  dailyReadings: DailyReading[];
  difficultyStats: DifficultyStats[];
}

/**
 * 复习单词数据
 */
export interface ReviewWordDto {
  id: number;
  word: string;
  definition: string;
  partOfSpeech?: string;
  pronunciation: string;
  reviewCount: number;
  nextReviewTime: string;
  difficultyLevel: number;
}

/**
 * 今日学习日报数据
 */
export interface TodaySummary {
  totalReadingMinutes: number;
  totalWordsLearned: number;
  articlesRead: number;
  streaks: number;
}

/**
 * 一周学习周报数据
 */
export interface WeeklyInsights {
  weeklyReadingMinutes: number;
  weeklyWordsLearned: number;
  weeklyArticlesRead: number;
  completionRate: number;
  improvement: number;
}
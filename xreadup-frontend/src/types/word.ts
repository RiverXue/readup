/**
 * 单词相关类型定义
 */

/**
 * 单词项目接口
 * 统一整个应用中使用的单词数据结构
 */
export interface WordItem {
  /** 单词ID，支持数字和字符串类型以兼容不同数据源 */
  id: number | string;
  
  /** 单词文本 */
  word: string;
  
  /** 单词释义（别名：definition） */
  meaning: string;
  
  /** 单词翻译（用于听写组件显示） */
  translation: string;
  
  /** 单词音标（别名：pronunciation） */
  phonetic: string;
  
  /** 词性 */
  partOfSpeech?: string;
  
  /** 例句 */
  example?: string;
  
  /** 复习状态 */
  reviewStatus: 'unreviewed' | 'reviewing' | 'mastered' | 'overdue';
  
  /** 复习次数 */
  reviewCount: number;
  
  /** 下次复习时间 */
  nextReviewTime?: string;
  
  /** 难度等级 */
  difficultyLevel?: number;
  
  /** 是否不再复习 */
  noLongerReview: boolean;
  
  /** 是否需要复习（基于状态和时间计算） */
  needsReview: boolean;
  
  /** 创建时间 */
  createdAt?: string;
  
  /** 定义（与meaning同义，用于兼容旧数据） */
  definition?: string;
  
  /** 发音（与phonetic同义，用于兼容旧数据） */
  pronunciation?: string;
}
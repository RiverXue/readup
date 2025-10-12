// 词汇相关类型定义

export interface Word {
  id: number
  word: string
  translation: string
  phonetic?: string
  partOfSpeech?: string
  example?: string
  context?: string
  masteryLevel: number
  createdAt: string
  lastReviewedAt?: string
  nextReviewAt?: string
  articleId?: number
}

export interface WordDetail {
  word: string
  meaning: string
  phonetic?: string
  example?: string
  context?: string
  partOfSpeech?: string
  synonyms?: string[]
  antonyms?: string[]
  frequency?: number
  collocations?: string[]
}

export interface AddWordData {
  word: string
  translation?: string
  context?: string
  articleId?: number
}

export type MasteryLevel = 0 | 1 | 2 | 3 | 4 | 5

// 0: 不认识
// 1: 听说过
// 2: 基本认识
// 3: 会用
// 4: 熟练
// 5: 精通

export interface VocabularyStats {
  totalWords: number
  masteredWords: number
  learningWords: number
  newWordsToday: number
  reviewWordsToday: number
  growthRate: number
}

export interface VocabularyGrowthData {
  date: string
  count: number
  reviewed?: number
}
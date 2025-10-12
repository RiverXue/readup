// 文章类型定义
export interface Article {
  id: number
  title: string
  enContent: string
  cnContent: string
  category: string
  difficulty: string
  tags: string[]
  description?: string
  readCount?: number
  createdAt?: string
}

// AI功能类型
export interface AITranslation {
  original: string
  translated: string
  keywords: string[]
}

export interface AISummary {
  summary: string
  keyPoints: string[]
}

export interface AIParse {
  sentence: string
  grammar: string
  vocabulary: string
}

// 单词查询结果
export interface WordDetail {
  word: string
  meaning: string
  phonetic?: string
  example?: string
  context?: string
  partOfSpeech?: string
}
// api/vocabulary.ts - 生词本相关API
import request from '@/utils/request'

export interface Vocabulary {
  id: number
  word: string
  phonetic: string
  meaning: string
  example: string
  difficulty: string
  addedAt: string
  lastReviewedAt?: string
  reviewCount: number
  masteryLevel: number
  nextReviewAt: string
  isMastered: boolean
}

export interface VocabularyListParams {
  page: number
  pageSize: number
  difficulty?: string
  masteryLevel?: number
  keyword?: string
}

export interface VocabularyListResponse {
  list: Vocabulary[]
  total: number
  hasMore: boolean
}

export interface ReviewSession {
  id: number
  type: 'flashcard' | 'quick' | 'dictation'
  words: Vocabulary[]
  currentIndex: number
  totalWords: number
  correctCount: number
  wrongCount: number
  startTime: string
  endTime?: string
}

export interface ReviewResult {
  sessionId: number
  totalWords: number
  correctCount: number
  wrongCount: number
  accuracy: number
  timeSpent: number
  experienceGained: number
  newMasteredWords: number
}

export const vocabularyApi = {
  // 获取生词本列表
  getVocabularyList: (params: VocabularyListParams) => 
    request.get<VocabularyListResponse>('/api/vocabulary/list', params),
  
  // 添加单词到生词本
  addWord: (word: string) => 
    request.post<Vocabulary>('/api/vocabulary/add', { word }),
  
  // 从生词本删除单词
  removeWord: (id: number) => 
    request.delete(`/api/vocabulary/${id}`),
  
  // 更新单词信息
  updateWord: (id: number, data: Partial<Vocabulary>) => 
    request.put<Vocabulary>(`/api/vocabulary/${id}`, data),
  
  // 获取复习单词
  getReviewWords: (type: 'flashcard' | 'quick' | 'dictation', count: number = 20) => 
    request.get<Vocabulary[]>('/api/vocabulary/review', { type, count }),
  
  // 开始复习会话
  startReviewSession: (type: 'flashcard' | 'quick' | 'dictation', wordIds: number[]) => 
    request.post<ReviewSession>('/api/vocabulary/review/session', { type, wordIds }),
  
  // 提交复习结果
  submitReviewResult: (sessionId: number, wordId: number, isCorrect: boolean, timeSpent: number) => 
    request.post<ReviewResult>('/api/vocabulary/review/result', { sessionId, wordId, isCorrect, timeSpent }),
  
  // 结束复习会话
  endReviewSession: (sessionId: number) => 
    request.post<ReviewResult>('/api/vocabulary/review/end', { sessionId }),
  
  // 获取学习统计
  getLearningStats: () => 
    request.get<{
      totalWords: number
      masteredWords: number
      learningWords: number
      reviewWords: number
      streakDays: number
      totalStudyTime: number
      experiencePoints: number
    }>('/api/vocabulary/stats'),
  
  // 获取复习计划
  getReviewPlan: () => 
    request.get<{
      todayReview: number
      tomorrowReview: number
      thisWeekReview: number
      overdueReview: number
    }>('/api/vocabulary/review-plan'),
  
  // 搜索单词
  searchWords: (keyword: string) => 
    request.get<Vocabulary[]>('/api/vocabulary/search', { keyword }),
  
  // 批量操作
  batchAddWords: (words: string[]) => 
    request.post<Vocabulary[]>('/api/vocabulary/batch-add', { words }),
  
  batchRemoveWords: (ids: number[]) => 
    request.delete('/api/vocabulary/batch-remove', { ids }),
  
  // 获取单词详情
  getWordDetail: (id: number) => 
    request.get<Vocabulary>(`/api/vocabulary/${id}`),
  
  // 重置单词学习进度
  resetWordProgress: (id: number) => 
    request.post(`/api/vocabulary/${id}/reset`),
  
  // 标记单词为已掌握
  markAsMastered: (id: number) => 
    request.post(`/api/vocabulary/${id}/mastered`),
  
  // 获取学习历史
  getLearningHistory: (page: number, pageSize: number) => 
    request.get<{
      list: Array<{
        id: number
        word: string
        action: 'add' | 'review' | 'master' | 'remove'
        timestamp: string
        details?: string
      }>
      total: number
    }>('/api/vocabulary/history', { page, pageSize })
}

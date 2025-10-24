// api/ai.ts - AI助手相关API
import request from '@/utils/request'

export interface ChatMessage {
  id: string
  type: 'user' | 'assistant'
  content: string
  timestamp: string
  metadata?: {
    articleId?: number
    wordId?: number
    functionCall?: string
    parameters?: any
  }
}

export interface ChatSession {
  id: string
  title: string
  articleId?: number
  createdAt: string
  updatedAt: string
  messageCount: number
  lastMessage?: string
}

export interface ChatRequest {
  message: string
  sessionId?: string
  articleId?: number
  context?: {
    currentArticle?: any
    vocabularyList?: any[]
    learningHistory?: any[]
  }
}

export interface ChatResponse {
  message: ChatMessage
  sessionId: string
  suggestions?: string[]
  functionCalls?: Array<{
    name: string
    parameters: any
  }>
}

export interface ArticleSummary {
  summary: string
  keyPoints: string[]
  difficulty: string
  vocabulary: Array<{
    word: string
    meaning: string
    difficulty: string
  }>
  questions: Array<{
    question: string
    answer: string
    type: 'comprehension' | 'vocabulary' | 'grammar'
  }>
}

export interface SentenceAnalysis {
  original: string
  translation: string
  grammar: Array<{
    part: string
    explanation: string
  }>
  vocabulary: Array<{
    word: string
    meaning: string
    difficulty: string
  }>
  pronunciation: string
}

export interface PersonalizedQuestion {
  id: string
  question: string
  type: 'comprehension' | 'vocabulary' | 'grammar' | 'writing'
  difficulty: string
  options?: string[]
  correctAnswer?: string
  explanation: string
  articleId?: number
  wordId?: number
}

export interface LearningDiagnosis {
  overallLevel: string
  strengths: string[]
  weaknesses: string[]
  recommendations: string[]
  studyPlan: {
    dailyGoal: number
    weeklyGoal: number
    focusAreas: string[]
    suggestedActivities: string[]
  }
  progress: {
    vocabulary: number
    grammar: number
    comprehension: number
    writing: number
  }
}

export const aiApi = {
  // 发送聊天消息
  sendMessage: (data: ChatRequest) => 
    request.post<ChatResponse>('/api/ai/chat', data),
  
  // 获取聊天历史
  getChatHistory: (sessionId: string, page: number = 1, pageSize: number = 20) => 
    request.get<{
      messages: ChatMessage[]
      hasMore: boolean
    }>('/api/ai/chat/history', { sessionId, page, pageSize }),
  
  // 获取聊天会话列表
  getChatSessions: (page: number = 1, pageSize: number = 20) => 
    request.get<{
      sessions: ChatSession[]
      hasMore: boolean
    }>('/api/ai/chat/sessions', { page, pageSize }),
  
  // 创建新的聊天会话
  createChatSession: (title: string, articleId?: number) => 
    request.post<ChatSession>('/api/ai/chat/session', { title, articleId }),
  
  // 删除聊天会话
  deleteChatSession: (sessionId: string) => 
    request.delete(`/api/ai/chat/session/${sessionId}`),
  
  // 获取文章摘要
  getArticleSummary: (articleId: number) => 
    request.get<ArticleSummary>(`/api/ai/article/${articleId}/summary`),
  
  // 分析句子
  analyzeSentence: (sentence: string, articleId?: number) => 
    request.post<SentenceAnalysis>('/api/ai/sentence/analyze', { sentence, articleId }),
  
  // 获取个性化问题
  getPersonalizedQuestions: (count: number = 5, type?: string) => 
    request.get<PersonalizedQuestion[]>('/api/ai/questions/personalized', { count, type }),
  
  // 提交问题答案
  submitQuestionAnswer: (questionId: string, answer: string, timeSpent: number) => 
    request.post<{
      isCorrect: boolean
      score: number
      explanation: string
      nextQuestion?: PersonalizedQuestion
    }>('/api/ai/questions/answer', { questionId, answer, timeSpent }),
  
  // 获取学习诊断
  getLearningDiagnosis: () => 
    request.get<LearningDiagnosis>('/api/ai/diagnosis'),
  
  // 生成学习计划
  generateStudyPlan: (goals: string[], timeAvailable: number) => 
    request.post<{
      plan: {
        daily: any[]
        weekly: any[]
        monthly: any[]
      }
      recommendations: string[]
    }>('/api/ai/study-plan', { goals, timeAvailable }),
  
  // 获取学习建议
  getLearningSuggestions: (context: {
    currentLevel: string
    recentArticles: any[]
    vocabularyProgress: any[]
    weakAreas: string[]
  }) => 
    request.post<{
      suggestions: string[]
      resources: Array<{
        type: string
        title: string
        url: string
        description: string
      }>
    }>('/api/ai/suggestions', context),
  
  // 语音转文字
  speechToText: (audioData: string) => 
    request.post<{
      text: string
      confidence: number
    }>('/api/ai/speech-to-text', { audioData }),
  
  // 文字转语音
  textToSpeech: (text: string, voice: string = 'default') => 
    request.post<{
      audioUrl: string
      duration: number
    }>('/api/ai/text-to-speech', { text, voice }),
  
  // 获取AI功能状态
  getAIFeaturesStatus: () => 
    request.get<{
      chatEnabled: boolean
      summaryEnabled: boolean
      analysisEnabled: boolean
      questionsEnabled: boolean
      diagnosisEnabled: boolean
      ttsEnabled: boolean
      sttEnabled: boolean
    }>('/api/ai/features/status'),
  
  // 获取AI使用统计
  getAIUsageStats: () => 
    request.get<{
      totalCalls: number
      callsThisMonth: number
      remainingCalls: number
      lastUsed: string
      favoriteFeatures: string[]
    }>('/api/ai/usage/stats')
}

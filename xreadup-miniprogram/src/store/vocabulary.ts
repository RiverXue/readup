// store/vocabulary.ts - 生词本状态管理
import { defineStore } from 'pinia'
import { vocabularyApi, type Vocabulary, type ReviewSession, type ReviewResult } from '@/api/vocabulary'

export const useVocabularyStore = defineStore('vocabulary', {
  state: () => ({
    vocabularyList: [] as Vocabulary[],
    currentSession: null as ReviewSession | null,
    learningStats: {
      totalWords: 0,
      masteredWords: 0,
      learningWords: 0,
      reviewWords: 0,
      streakDays: 0,
      totalStudyTime: 0,
      experiencePoints: 0
    },
    reviewPlan: {
      todayReview: 0,
      tomorrowReview: 0,
      thisWeekReview: 0,
      overdueReview: 0
    },
    loading: false,
    currentPage: 1,
    hasMore: true
  }),
  
  getters: {
    // 获取需要复习的单词
    reviewWords: (state) => {
      return state.vocabularyList.filter(word => 
        !word.isMastered && new Date(word.nextReviewAt) <= new Date()
      )
    },
    
    // 获取已掌握的单词
    masteredWords: (state) => {
      return state.vocabularyList.filter(word => word.isMastered)
    },
    
    // 获取正在学习的单词
    learningWords: (state) => {
      return state.vocabularyList.filter(word => 
        !word.isMastered && new Date(word.nextReviewAt) > new Date()
      )
    },
    
    // 获取按难度分组的单词
    wordsByDifficulty: (state) => {
      const groups = {
        easy: [] as Vocabulary[],
        medium: [] as Vocabulary[],
        hard: [] as Vocabulary[]
      }
      
      state.vocabularyList.forEach(word => {
        if (groups[word.difficulty as keyof typeof groups]) {
          groups[word.difficulty as keyof typeof groups].push(word)
        }
      })
      
      return groups
    },
    
    // 获取学习进度
    learningProgress: (state) => {
      if (state.learningStats.totalWords === 0) return 0
      return Math.round((state.learningStats.masteredWords / state.learningStats.totalWords) * 100)
    }
  },
  
  actions: {
    // 加载生词本列表
    async loadVocabularyList(reset = true) {
      this.loading = true
      
      try {
        const response = await vocabularyApi.getVocabularyList({
          page: reset ? 1 : this.currentPage,
          pageSize: 20
        })
        
        if (reset) {
          this.vocabularyList = response.data.list
          this.currentPage = 1
        } else {
          this.vocabularyList.push(...response.data.list)
        }
        
        this.hasMore = response.data.hasMore
        this.currentPage++
        
      } catch (error) {
        console.error('加载生词本失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },
    
    // 添加单词
    async addWord(word: string) {
      try {
        const response = await vocabularyApi.addWord(word)
        this.vocabularyList.unshift(response.data)
        
        // 更新统计
        await this.loadLearningStats()
        
        return response.data
      } catch (error) {
        console.error('添加单词失败:', error)
        throw error
      }
    },
    
    // 删除单词
    async removeWord(id: number) {
      try {
        await vocabularyApi.removeWord(id)
        this.vocabularyList = this.vocabularyList.filter(word => word.id !== id)
        
        // 更新统计
        await this.loadLearningStats()
        
      } catch (error) {
        console.error('删除单词失败:', error)
        throw error
      }
    },
    
    // 更新单词信息
    async updateWord(id: number, data: Partial<Vocabulary>) {
      try {
        const response = await vocabularyApi.updateWord(id, data)
        const index = this.vocabularyList.findIndex(word => word.id === id)
        if (index !== -1) {
          this.vocabularyList[index] = response.data
        }
        
        return response.data
      } catch (error) {
        console.error('更新单词失败:', error)
        throw error
      }
    },
    
    // 开始复习会话
    async startReviewSession(type: 'flashcard' | 'quick' | 'dictation', wordIds?: number[]) {
      try {
        let words: Vocabulary[]
        
        if (wordIds && wordIds.length > 0) {
          words = this.vocabularyList.filter(word => wordIds.includes(word.id))
        } else {
          // 获取需要复习的单词
          const response = await vocabularyApi.getReviewWords(type, 20)
          words = response.data
        }
        
        if (words.length === 0) {
          throw new Error('没有需要复习的单词')
        }
        
        const response = await vocabularyApi.startReviewSession(type, words.map(w => w.id))
        this.currentSession = response.data
        
        return response.data
      } catch (error) {
        console.error('开始复习失败:', error)
        throw error
      }
    },
    
    // 提交复习结果
    async submitReviewResult(wordId: number, isCorrect: boolean, timeSpent: number) {
      if (!this.currentSession) return
        
      try {
        const response = await vocabularyApi.submitReviewResult(
          this.currentSession.id,
          wordId,
          isCorrect,
          timeSpent
        )
        
        // 更新当前会话
        this.currentSession.currentIndex++
        if (isCorrect) {
          this.currentSession.correctCount++
        } else {
          this.currentSession.wrongCount++
        }
        
        return response.data
      } catch (error) {
        console.error('提交复习结果失败:', error)
        throw error
      }
    },
    
    // 结束复习会话
    async endReviewSession() {
      if (!this.currentSession) return
        
      try {
        const response = await vocabularyApi.endReviewSession(this.currentSession.id)
        
        // 更新统计
        await this.loadLearningStats()
        
        // 清除当前会话
        this.currentSession = null
        
        return response.data
      } catch (error) {
        console.error('结束复习失败:', error)
        throw error
      }
    },
    
    // 加载学习统计
    async loadLearningStats() {
      try {
        const response = await vocabularyApi.getLearningStats()
        this.learningStats = response.data
      } catch (error) {
        console.error('加载学习统计失败:', error)
      }
    },
    
    // 加载复习计划
    async loadReviewPlan() {
      try {
        const response = await vocabularyApi.getReviewPlan()
        this.reviewPlan = response.data
      } catch (error) {
        console.error('加载复习计划失败:', error)
      }
    },
    
    // 搜索单词
    async searchWords(keyword: string) {
      try {
        const response = await vocabularyApi.searchWords(keyword)
        return response.data
      } catch (error) {
        console.error('搜索单词失败:', error)
        throw error
      }
    },
    
    // 批量添加单词
    async batchAddWords(words: string[]) {
      try {
        const response = await vocabularyApi.batchAddWords(words)
        this.vocabularyList.unshift(...response.data)
        
        // 更新统计
        await this.loadLearningStats()
        
        return response.data
      } catch (error) {
        console.error('批量添加单词失败:', error)
        throw error
      }
    },
    
    // 批量删除单词
    async batchRemoveWords(ids: number[]) {
      try {
        await vocabularyApi.batchRemoveWords(ids)
        this.vocabularyList = this.vocabularyList.filter(word => !ids.includes(word.id))
        
        // 更新统计
        await this.loadLearningStats()
        
      } catch (error) {
        console.error('批量删除单词失败:', error)
        throw error
      }
    },
    
    // 重置单词学习进度
    async resetWordProgress(id: number) {
      try {
        await vocabularyApi.resetWordProgress(id)
        
        // 更新本地数据
        const word = this.vocabularyList.find(w => w.id === id)
        if (word) {
          word.masteryLevel = 0
          word.reviewCount = 0
          word.isMastered = false
          word.nextReviewAt = new Date().toISOString()
        }
        
      } catch (error) {
        console.error('重置单词进度失败:', error)
        throw error
      }
    },
    
    // 标记单词为已掌握
    async markAsMastered(id: number) {
      try {
        await vocabularyApi.markAsMastered(id)
        
        // 更新本地数据
        const word = this.vocabularyList.find(w => w.id === id)
        if (word) {
          word.isMastered = true
          word.masteryLevel = 100
        }
        
        // 更新统计
        await this.loadLearningStats()
        
      } catch (error) {
        console.error('标记单词为已掌握失败:', error)
        throw error
      }
    },
    
    // 加载更多单词
    async loadMoreWords() {
      if (this.hasMore && !this.loading) {
        await this.loadVocabularyList(false)
      }
    },
    
    // 刷新数据
    async refreshData() {
      await Promise.all([
        this.loadVocabularyList(true),
        this.loadLearningStats(),
        this.loadReviewPlan()
      ])
    }
  }
})

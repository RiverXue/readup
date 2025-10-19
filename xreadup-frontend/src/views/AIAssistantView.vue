<template>
  <div class="ai-tutor-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <div class="tutor-avatar">
            <el-icon><Star /></el-icon>
          </div>
          <div class="header-info">
            <h1>🎓 Rayda老师</h1>
            <p>您的专属英语学习导师</p>
          </div>
        </div>
        <div class="header-actions">
          <el-button @click="goToHome" type="primary" plain>
            <el-icon><HomeFilled /></el-icon>
            返回首页
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧：用户信息和学习诊断 -->
      <div class="left-sidebar">
        <!-- 用户学习画像 -->
        <div class="user-profile-card">
          <div class="card-header">
            <h3>👤 学习画像</h3>
          </div>
          <div class="profile-content">
            <div class="user-info">
              <div class="user-avatar">
            <el-icon><User /></el-icon>
          </div>
              <div class="user-details">
                <h4>{{ userStore.userInfo?.username || '学习者' }}</h4>
                <div class="user-level-info">
                  <span class="level-icon">{{ getLevelIcon(userProfile.currentLevel as any) }}</span>
                  <span class="level-name">{{ getLevelDisplayName(userProfile.currentLevel as any) }}</span>
                </div>
                <p class="level-description">{{ getLevelDescription(userProfile.currentLevel as any) }}</p>
          </div>
        </div>
            <div class="learning-stats">
          <div class="stat-item">
            <div class="stat-value">{{ userProfile.learningDays || 0 }}</div>
            <div class="stat-label">学习天数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userProfile.totalArticlesRead || 0 }}</div>
            <div class="stat-label">已读文章</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userProfile.masteredWords || 0 }}</div>
            <div class="stat-label">已掌握词汇</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userProfile.readingStreak || 0 }}</div>
            <div class="stat-label">连续学习</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userProfile.averageReadTime || 0 }}min</div>
            <div class="stat-label">平均阅读时长</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userProfile.vocabularyCount || 0 }}</div>
            <div class="stat-label">生词本词汇</div>
          </div>
            </div>
          </div>
        </div>

        <!-- 学习诊断 -->
        <div class="learning-diagnosis-card" v-if="diagnosis">
          <div class="card-header">
            <h3>📊 学习诊断</h3>
          </div>
          <div class="diagnosis-content">
            <!-- 学习效率分析 -->
            <div class="efficiency-analysis">
              <h4>📈 学习效率分析</h4>
              <div class="efficiency-metrics">
                <div class="metric-item">
                  <span class="metric-label">阅读效率</span>
                  <span class="metric-value">{{ Math.round((userProfile.totalArticlesRead || 0) / Math.max(userProfile.learningDays || 1, 1)) }}篇/天</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">词汇掌握率</span>
                  <span class="metric-value">{{ userProfile.vocabularyCount > 0 ? Math.round((userProfile.masteredWords || 0) / userProfile.vocabularyCount * 100) : 0 }}%</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">学习坚持度</span>
                  <span class="metric-value">{{ userProfile.readingStreak > 7 ? '优秀' : userProfile.readingStreak > 3 ? '良好' : '需提升' }}</span>
                </div>
              </div>
            </div>
            
            <div class="strengths-weaknesses">
              <div class="strengths">
                <h4>✅ 学习优势</h4>
                <ul>
                  <li v-for="strength in diagnosis.strengths" :key="strength">{{ strength }}</li>
                </ul>
              </div>
              <div class="weaknesses">
                <h4>🎯 需要提升</h4>
                <ul>
                  <li v-for="weakness in userProfile.weakAreas" :key="weakness">{{ weakness }}</li>
                </ul>
              </div>
            </div>
            
            <!-- 学习建议 -->
            <div class="learning-recommendations" v-if="diagnosis.recommendations && diagnosis.recommendations.length > 0">
              <h4>💡 学习建议</h4>
              <ul>
                <li v-for="recommendation in diagnosis.recommendations" :key="recommendation">{{ recommendation }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- 当前文章信息 -->
      <div class="article-info-card" v-if="currentArticle">
        <div class="card-header">
            <h3>📖 当前文章</h3>
        </div>
          <div class="article-content">
        <div class="article-title">{{ currentArticle.title }}</div>
        <div class="article-tags">
              <el-tag :type="getDifficultyType(currentArticle.difficulty)" size="small">
            {{ currentArticle.difficulty || '未知难度' }}
          </el-tag>
              <el-tag type="info" size="small">
            {{ currentArticle.category || '未分类' }}
          </el-tag>
        </div>
      </div>
      </div>

      <!-- 右侧：AI对话区域 -->
      <div class="right-content">
        <!-- AI对话头部 -->
        <div class="chat-header">
          <div class="ai-status" :class="aiLoading ? 'loading' : 'ready'">
          <div class="status-dot"></div>
            <span>{{ aiLoading ? 'Rayda老师正在思考...' : 'Rayda老师在线指导中' }}</span>
        </div>
          <el-button @click="clearChat" :disabled="aiLoading" size="small" plain>
            <el-icon><Delete /></el-icon>
            清空对话
          </el-button>
      </div>

        <!-- 对话内容区域 -->
        <div class="chat-content">
          <!-- 个性化问题推荐 -->
          <div class="questions-section" v-if="chatHistory.length === 0">
            <div class="section-header">
                <h3>🎯 个性化学习指导</h3>
                <p>Rayda老师为您量身定制的学习建议</p>
            </div>
            <div class="questions-grid">
              <div 
                v-for="question in smartQuestions" 
                :key="question.id"
                @click="askSuggestedQuestion(question.text)"
                class="question-card"
                :class="question.type"
              >
                <div class="question-icon">{{ question.icon }}</div>
                <div class="question-content">
                  <div class="question-text">{{ question.text }}</div>
                  <div class="question-type">{{ getQuestionTypeLabel(question.type) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 对话历史 -->
          <div class="chat-section" v-else>
            <div class="chat-messages">
              <div 
                v-for="message in chatHistory" 
                :key="message.id" 
                class="message"
                :class="message.type"
              >
                <div class="message-avatar" v-if="message.type === 'ai'">
                  <el-icon><Star /></el-icon>
                </div>
                <div class="message-content">
                  <div class="message-text" v-html="formatMessage(message.content)"></div>
                  <div class="message-time">{{ formatTime(message.timestamp) }}</div>
                </div>
              </div>
            </div>
          </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
          <div class="input-container">
          <el-input
            v-model="aiQuestion"
            type="textarea"
            :rows="3"
              placeholder="向Rayda老师提问任何英语学习问题..."
              @keydown.ctrl.enter="submitAIQuestion"
            :disabled="aiLoading"
              class="question-input"
          />
          <div class="input-actions">
            <div class="input-tips">
                按 Ctrl+Enter 快速发送
            </div>
            <el-button 
              @click="submitAIQuestion" 
                type="primary"
              :loading="aiLoading"
                :disabled="!aiQuestion.trim()"
              class="send-btn"
            >
              发送
            </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { aiApi, articleApi, vocabularyApi, learningApi, request as api } from '@/utils/api'
import { useUserStore } from '@/stores/user'
import { assessUserLevel, getLevelDisplayName, getLevelProgress, getLevelDescription, getLevelIcon } from '@/utils/levelAssessment'
import { LEARNING_THRESHOLDS, isStrong, isWeak } from '@/utils/learningThresholds'
import { 
  Document, HomeFilled, CircleClose, Trophy, Star, StarFilled, 
  Reading, View, Clock, User, Delete, TrendCharts 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const aiQuestion = ref('')
const aiAnswer = ref('')
const aiLoading = ref(false)
const currentArticle = ref<any>(null)

// 对话历史
const chatHistory = ref<Array<{
  id: string
  type: 'user' | 'ai'
  content: string
  timestamp: number
}>>([])

// 用户学习画像
const userProfile = ref({
  learningDays: 0,
  totalArticlesRead: 0,
  vocabularyCount: 0,
  averageReadTime: 0,
  totalReadTime: 0,
  readingStreak: 0,
  preferredCategories: [] as string[],
  currentLevel: 'beginner',
  weakAreas: [] as string[],
  newWords: 0,
  learningWords: 0,
  masteredWords: 0,
  averageDifficulty: 'B1'
})

// 智能问题推荐
const smartQuestions = ref<Array<{
  id: number
  text: string
  icon: string
  type: string
}>>([])

// 学习诊断
const diagnosis = ref<{
  strengths: string[]
  weaknesses: string[]
  recommendations: string[]
} | null>(null)

// 报告服务API
const reportApi = {
  getDashboard: (userId: number) => api.get(`/api/report/dashboard?userId=${userId}`),
  getReadingTime: (userId: number) => api.get(`/api/report/reading-time?userId=${userId}`)
}

// 词汇统计API
const vocabularyStatsApi = {
  getStats: (userId: number) => api.get(`/api/vocabulary/stats/${userId}`),
  getMyWords: (userId: number) => api.get(`/api/user/vocabulary/my-words?userId=${userId}`)
}

// 获取难度类型
const getDifficultyType = (difficulty: string) => {
  const difficultyMap: Record<string, string> = {
    'A1': 'success',
    'A2': 'info', 
    'B1': 'warning',
    'B2': 'danger',
    'C1': 'danger',
    'C2': 'danger'
  }
  return difficultyMap[difficulty] || 'info'
}

// 获取问题类型标签
const getQuestionTypeLabel = (type: string) => {
  const typeLabels: Record<string, string> = {
    'personalized-progress': '个性化进度',
    'category-improvement': '分类提升',
    'vocabulary-expansion': '词汇扩展',
    'reading-efficiency': '阅读效率',
    'weakness-targeting': '薄弱提升',
    'next-learning-path': '学习路径',
    'achievement-based': '成就激励',
    'vocabulary-consolidation': '词汇巩固'
  }
  return typeLabels[type] || '阅读提升'
}

// 分析用户问题类型
const analyzeQuestionType = (question: string) => {
  const lowerQuestion = question.toLowerCase()
  if (lowerQuestion.includes('单词') || lowerQuestion.includes('词汇') || lowerQuestion.includes('vocabulary')) {
    return 'vocabulary-expansion'
  } else if (lowerQuestion.includes('语法') || lowerQuestion.includes('grammar')) {
    return 'category-improvement'
  } else if (lowerQuestion.includes('阅读') || lowerQuestion.includes('reading')) {
    return 'reading-efficiency'
  } else if (lowerQuestion.includes('薄弱') || lowerQuestion.includes('提升') || lowerQuestion.includes('改进')) {
    return 'weakness-targeting'
  } else if (lowerQuestion.includes('接下来') || lowerQuestion.includes('学习') || lowerQuestion.includes('路径')) {
    return 'next-learning-path'
  } else {
    return 'personalized-progress'
  }
}

// 获取用户学习数据
const loadUserProfile = async () => {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) return
  
  try {
    const learningDays = await getUserLearningDays()
    const readingStats = await getUserReadingStats()
    const vocabularyStats = await getUserVocabularyStats()
    const currentLevel = assessUserLevel(learningDays, readingStats.totalArticles, vocabularyStats.masteredWords, vocabularyStats.count)
    
    // 先创建基础profile对象
    const baseProfile = {
      learningDays,
      totalArticlesRead: readingStats.totalArticles || 0,
      vocabularyCount: vocabularyStats.count || 0,
      averageReadTime: readingStats.averageReadTime || 0,
      totalReadTime: readingStats.totalReadTime || 0,
      readingStreak: readingStats.readingStreak || 0,
      preferredCategories: readingStats.preferredCategories || [],
      newWords: vocabularyStats.newWords || 0,
      learningWords: vocabularyStats.learningWords || 0,
      masteredWords: vocabularyStats.masteredWords || 0,
      averageDifficulty: vocabularyStats.averageDifficulty || 'B1'
    }
    
    const weakAreas = identifyWeakAreas(vocabularyStats.reviewStatus, baseProfile)
    
    // 调试信息
    console.log('🔍 薄弱环节识别调试信息:', {
      reviewStatus: vocabularyStats.reviewStatus,
      baseProfile: baseProfile,
      weakAreas: weakAreas
    })
    
    userProfile.value = {
      learningDays,
      totalArticlesRead: readingStats.totalArticles || 0,
      vocabularyCount: vocabularyStats.count || 0,
      averageReadTime: readingStats.averageReadTime || 0,
      totalReadTime: readingStats.totalReadTime || 0,
      readingStreak: readingStats.readingStreak || 0,
      preferredCategories: readingStats.preferredCategories || [],
      currentLevel,
      weakAreas,
      newWords: vocabularyStats.newWords || 0,
      learningWords: vocabularyStats.learningWords || 0,
      masteredWords: vocabularyStats.masteredWords || 0,
      averageDifficulty: vocabularyStats.averageDifficulty || 'B1'
    }
    
    // 生成学习诊断
    diagnosis.value = generateLearningDiagnosis(userProfile.value)
    
    console.log('📊 用户学习画像加载完成:', userProfile.value)
    console.log('🔍 学习诊断生成完成:', diagnosis.value)
  } catch (error) {
    console.error('加载用户学习数据失败:', error)
  }
}

// 获取用户学习天数
const getUserLearningDays = async () => {
  if (!userStore.userInfo?.id) return 0
  
  try {
    const userId = userStore.userInfo.id.toString()
    const checkInResponse = await learningApi.dailyCheckIn(userId)
    
    if (checkInResponse.data !== undefined) {
      console.log('从打卡API获取学习天数:', checkInResponse.data)
      return checkInResponse.data
    }
    
    return 0
  } catch (error) {
    console.warn('获取学习天数失败:', error)
    return 0
  }
}

// 获取用户阅读统计
const getUserReadingStats = async () => {
  try {
    if (!userStore.userInfo?.id) {
      return { totalArticles: 0, averageReadTime: 0, preferredCategories: [], totalReadTime: 0, readingStreak: 0 }
    }
    
    const dashboardResponse = await reportApi.getDashboard(Number(userStore.userInfo.id))
    const readingTimeResponse = await reportApi.getReadingTime(Number(userStore.userInfo.id))
    
    if (dashboardResponse.data && readingTimeResponse.data) {
      const dashboard = dashboardResponse.data
      const readingTime = readingTimeResponse.data
      
    console.log('阅读统计数据:', { dashboard, readingTime })
    console.log('readingTime.averageReadTimeMinutes:', readingTime.averageReadTimeMinutes)
    console.log('dashboard.averageReadTimeMinutes:', dashboard.averageReadTimeMinutes)
    console.log('readingTime.weeklyAverageMinutes:', readingTime.weeklyAverageMinutes)
    console.log('readingTime.totalMinutes:', readingTime.totalMinutes)
    console.log('readingTime.totalArticles:', readingTime.totalArticles)
    
    // 计算平均阅读时长：总阅读时长 / 总文章数
    const totalMinutes = readingTime.totalMinutes || 0
    const totalArticles = readingTime.totalArticles || 0
    const calculatedAverageReadTime = totalArticles > 0 ? Math.round(totalMinutes / totalArticles) : 0
    
    const finalAverageReadTime = readingTime.averageReadTimeMinutes || 
                                dashboard.averageReadTimeMinutes || 
                                readingTime.weeklyAverageMinutes || 
                                calculatedAverageReadTime || 0
    
    console.log('计算的平均阅读时长:', calculatedAverageReadTime)
    console.log('最终平均阅读时长:', finalAverageReadTime)
    
    return {
      totalArticles: readingTime.totalArticles || dashboard.totalArticlesRead || 0,
      averageReadTime: finalAverageReadTime,
      preferredCategories: dashboard.preferredCategories || [],
      totalReadTime: readingTime.totalMinutes || 0,
      readingStreak: dashboard.currentStreak || 0
    }
    }
    
    // 备选方案
    try {
      console.log('尝试备选方案 - learningApi.getReadingTimeStats')
      const readingTimeRes = await learningApi.getReadingTimeStats(Number(userStore.userInfo.id))
      console.log('备选方案响应:', readingTimeRes)
      
      if (readingTimeRes?.data) {
        console.log('备选方案数据:', readingTimeRes.data)
        return {
          totalArticles: readingTimeRes.data.totalArticles || 0,
          averageReadTime: readingTimeRes.data.averageReadTimeMinutes || 0,
          preferredCategories: [],
          totalReadTime: readingTimeRes.data.totalReadTimeMinutes || 0,
          readingStreak: 0
        }
      }
    } catch (learningError) {
      console.warn('learningApi备选方案也失败:', learningError)
    }
    
    return { totalArticles: 0, averageReadTime: 0, preferredCategories: [], totalReadTime: 0, readingStreak: 0 }
  } catch (error) {
    console.warn('获取用户阅读统计失败:', error)
    return { totalArticles: 0, averageReadTime: 0, preferredCategories: [], totalReadTime: 0, readingStreak: 0 }
  }
}

// 获取用户词汇统计
const getUserVocabularyStats = async () => {
  try {
    if (!userStore.userInfo?.id) {
      return { count: 0, newWords: 0, learningWords: 0, masteredWords: 0, averageDifficulty: 'B1', reviewStatus: {} }
    }
    
    const statsResponse = await vocabularyStatsApi.getStats(Number(userStore.userInfo.id))
    const myWordsResponse = await vocabularyStatsApi.getMyWords(Number(userStore.userInfo.id))
    
    if (statsResponse.data && myWordsResponse.data) {
      const stats = statsResponse.data
      const myWords = myWordsResponse.data
      
      return {
        count: stats.totalWords || 0,
        newWords: stats.newWords || 0,
        learningWords: stats.learningWords || 0,
        masteredWords: stats.masteredWords || 0,
        averageDifficulty: stats.averageDifficulty || 'B1',
        reviewStatus: myWords.reduce((acc: any, word: any) => {
          acc[word.status] = (acc[word.status] || 0) + 1
          return acc
        }, {})
      }
    }
    
    return { count: 0, newWords: 0, learningWords: 0, masteredWords: 0, averageDifficulty: 'B1', reviewStatus: {} }
  } catch (error) {
    console.warn('获取用户词汇统计失败:', error)
    return { count: 0, newWords: 0, learningWords: 0, masteredWords: 0, averageDifficulty: 'B1', reviewStatus: {} }
  }
}


// 识别用户薄弱环节
const identifyWeakAreas = (reviewStatus: any, profile: any) => {
  const weakAreas = []
  
  // 1. 基于词汇复习状态识别薄弱环节（优先级最高）
  if (reviewStatus && Object.keys(reviewStatus).length > 0) {
    const total = Object.values(reviewStatus).reduce((sum: number, count: any) => sum + count, 0)
    
    if (total > 0) {
      // 词汇学习状态分析
      const newRate = (reviewStatus['new'] || 0) / total
      const learningRate = (reviewStatus['learning'] || 0) / total
      const reviewRate = (reviewStatus['review'] || 0) / total
      const masteryRate = (reviewStatus['mastered'] || 0) / total
      
      // 新词过多：说明学习速度过快，质量不够
      if (newRate > 0.3) weakAreas.push('新词掌握')
      
      // 学习中词汇过多：说明复习不够，进度缓慢
      if (learningRate > 0.4) weakAreas.push('词汇巩固')
      
      // 待复习词汇过多：说明复习频率不够
      if (reviewRate > 0.2) weakAreas.push('复习频率')
      
      // 掌握率过低：说明学习效果不好
      if (masteryRate < 0.3) weakAreas.push('词汇掌握率低')
      
      // 学习中词汇比例过高：说明学习进度缓慢
      if (learningRate > 0.5) weakAreas.push('学习进度缓慢')
    }
  }
  
  // 2. 基于整体学习数据识别薄弱环节
  if (isWeak(profile, 'learningDays')) weakAreas.push('学习坚持性')
  if (isWeak(profile, 'totalArticlesRead')) weakAreas.push('阅读练习')
  if (isWeak(profile, 'masteredWords')) weakAreas.push('词汇掌握')
  if (isWeak(profile, 'readingStreak')) weakAreas.push('学习习惯')
  if (isWeak(profile, 'averageReadTime')) weakAreas.push('阅读专注力')
  
  // 去重并返回
  return [...new Set(weakAreas)]
}

// 生成学习诊断
const generateLearningDiagnosis = (profile: any) => {
  const strengths = identifyStrengths(profile)
  const weaknesses = profile.weakAreas || []
  const recommendations = generateRecommendations(profile)
  
  // 如果没有薄弱环节，提供一些通用的提升建议
  if (weaknesses.length === 0) {
    weaknesses.push('可以尝试更高难度的内容')
    weaknesses.push('可以增加学习时长')
    weaknesses.push('可以探索新的学习领域')
  }
  
  return {
    strengths,
    weaknesses,
    recommendations
  }
}

// 识别学习优势
const identifyStrengths = (profile: any) => {
  const strengths = []
  
  if (isStrong(profile, 'learningDays')) strengths.push('学习坚持性')
  if (isStrong(profile, 'masteredWords')) strengths.push('词汇掌握')
  if (isStrong(profile, 'totalArticlesRead')) strengths.push('阅读能力')
  if (isStrong(profile, 'readingStreak')) strengths.push('学习习惯')
  if (isStrong(profile, 'averageReadTime')) strengths.push('专注力')
  
  // 如果没有明显的优势，给出鼓励性建议
  if (strengths.length === 0) {
    strengths.push('学习热情')
  }
  
  return strengths
}

// 生成学习建议
const generateRecommendations = (profile: any) => {
  const recommendations = []
  
  // 基于薄弱环节生成具体建议
  if (profile.weakAreas.includes('学习坚持性')) {
    recommendations.push('建议每天固定时间学习，建立学习习惯')
  }
  if (profile.weakAreas.includes('阅读练习')) {
    recommendations.push('建议每周阅读2-3篇文章，提高阅读理解能力')
  }
  if (profile.weakAreas.includes('词汇积累')) {
    recommendations.push('建议每天学习10-15个新单词，扩大词汇量')
  }
  if (profile.weakAreas.includes('学习习惯')) {
    recommendations.push('建议设置学习提醒，保持连续学习')
  }
  if (profile.weakAreas.includes('阅读专注力')) {
    recommendations.push('建议选择安静环境，延长单次阅读时间')
  }
  if (profile.weakAreas.includes('新词掌握')) {
    recommendations.push('建议使用记忆技巧，提高新词掌握效率')
  }
  if (profile.weakAreas.includes('词汇巩固')) {
    recommendations.push('建议增加词汇复习频率，巩固已学词汇')
  }
  if (profile.weakAreas.includes('复习频率')) {
    recommendations.push('建议制定复习计划，定期回顾已学内容')
  }
  if (profile.weakAreas.includes('词汇掌握率低')) {
    recommendations.push('建议放慢学习节奏，确保每个词汇都掌握')
  }
  if (profile.weakAreas.includes('学习进度缓慢')) {
    recommendations.push('建议调整学习方法，提高学习效率')
  }
  
  // 基于学习数据生成通用建议
  if (profile.averageReadTime < 10) {
    recommendations.push('建议延长单次阅读时间至15-20分钟')
  }
  if (profile.readingStreak < 3) {
    recommendations.push('建议保持连续学习习惯，避免中断')
  }
  if (profile.masteredWords < 50) {
    recommendations.push('建议提高词汇掌握率，重点巩固已学词汇')
  }
  if (profile.totalArticlesRead < 5) {
    recommendations.push('建议多阅读不同类型的文章，拓宽知识面')
  }
  
  // 如果没有薄弱环节，给出积极建议
  if (profile.weakAreas.length === 0) {
    recommendations.push('您的学习状态很好，建议继续保持')
    recommendations.push('可以尝试挑战更高难度的内容')
  }
  
  return recommendations
}

// 获取学习水平进度百分比（使用统一工具函数）
const getLevelProgressValue = () => {
  return getLevelProgress(userProfile.value.currentLevel as any)
}

// 生成个性化问题
const generatePersonalizedQuestions = () => {
  const questions = []
  
  // 基于学习天数的个性化问题
  if (userProfile.value.learningDays >= 7) {
    questions.push({
      id: 1,
      text: `您已经坚持学习${userProfile.value.learningDays}天了！如何保持这个良好的学习节奏？`,
      icon: '🔥',
      type: 'achievement-based'
    })
  }
  
  // 基于文章阅读量的个性化问题
  if (userProfile.value.totalArticlesRead > 0) {
    questions.push({
      id: 2,
      text: `您已阅读${userProfile.value.totalArticlesRead}篇文章，如何提高阅读效率？`,
      icon: '📖',
      type: 'reading-efficiency'
    })
  }
  
  // 基于词汇量的个性化问题
  if (userProfile.value.vocabularyCount > 0) {
    questions.push({
      id: 3,
      text: `您已学习${userProfile.value.vocabularyCount}个词汇，如何更好地巩固记忆？`,
      icon: '📚',
      type: 'vocabulary-consolidation'
    })
  }
  
  // 基于薄弱环节的个性化问题
  if (userProfile.value.weakAreas.length > 0) {
    questions.push({
      id: 4,
      text: `针对您的薄弱环节"${userProfile.value.weakAreas[0]}"，有什么提升建议？`,
      icon: '🎯',
      type: 'weakness-targeting'
    })
  }
  
  // 通用学习问题
    questions.push({
      id: 5,
    text: '如何制定适合自己的英语学习计划？',
    icon: '📋',
    type: 'next-learning-path'
  })
  
    questions.push({
    id: 6,
    text: '如何提高英语阅读理解能力？',
    icon: '🔍',
      type: 'reading-efficiency'
    })
  
  smartQuestions.value = questions
}

// 提交AI问题
const submitAIQuestion = async () => {
  if (!aiQuestion.value.trim()) return
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    ElMessage.warning('请先登录以使用AI助手功能')
    return
  }
  if (!userStore.checkAiQuota()) return

  aiLoading.value = true
  
  try {
    const articleContext = {
      title: currentArticle.value?.title || '',
      description: currentArticle.value?.description || '',
      userProfile: userProfile.value,
      questionType: analyzeQuestionType(aiQuestion.value)
    }
    
    const res = await aiApi.chat(aiQuestion.value, Number(userStore.userInfo?.id), JSON.stringify(articleContext))
    
    console.log('AI API响应:', res)
    console.log('响应数据:', res.data)
    
    // 检查响应结构 - 从调试信息看，res.data直接包含answer字段
    if (res.data && res.data.answer) {
      console.log('进入成功分支 - 直接包含answer字段')
      
      // 添加用户问题到对话历史
      chatHistory.value.push({
        id: Date.now().toString(),
        type: 'user',
        content: aiQuestion.value,
        timestamp: Date.now()
      })
      
      // 设置AI回答 - 直接使用res.data.answer
      const answerText = res.data.answer || '抱歉，我暂时无法回答这个问题。'
      console.log('AI回答文本:', answerText)
      
      // 添加AI回答到对话历史
      chatHistory.value.push({
        id: (Date.now() + 1).toString(),
        type: 'ai',
        content: answerText,
        timestamp: Date.now()
      })
      
      // 清空AI回答临时变量和输入框
      aiAnswer.value = ''
      aiQuestion.value = ''
    } else if (res.data && res.data.success && res.data.data) {
      console.log('进入成功分支 - 标准ApiResponse格式')
      const aiResponse = res.data.data
      console.log('aiResponse:', aiResponse)
      
      // 添加用户问题到对话历史
      chatHistory.value.push({
        id: Date.now().toString(),
        type: 'user',
        content: aiQuestion.value,
        timestamp: Date.now()
      })
      
      // 设置AI回答 - 从响应对象中提取answer字段
      const answerText = aiResponse?.answer || '抱歉，我暂时无法回答这个问题。'
      console.log('AI回答文本:', answerText)
      
      // 添加AI回答到对话历史
      chatHistory.value.push({
        id: (Date.now() + 1).toString(),
        type: 'ai',
        content: answerText,
        timestamp: Date.now()
      })
      
      // 清空AI回答临时变量和输入框
      aiAnswer.value = ''
      aiQuestion.value = ''
    } else {
      console.log('进入错误分支')
      console.log('res.data存在:', !!res.data)
      console.log('res.data.answer存在:', !!res.data?.answer)
      console.log('res.data.success:', res.data?.success)
      console.error('AI响应数据异常:', res)
      ElMessage.error(res.data?.message || 'AI助手暂时无法回答，请稍后再试')
    }
  } catch (error) {
    console.error('AI助手请求失败:', error)
    ElMessage.error('网络错误，请稍后再试')
  } finally {
    aiLoading.value = false
  }
}

// 获取当前文章信息
const getCurrentArticle = async () => {
  const articleId = route.query.articleId
  if (articleId) {
    try {
      const response = await articleApi.getArticle(String(articleId))
      if (response.data) {
        currentArticle.value = response.data
      }
    } catch (error) {
      console.warn('获取文章信息失败:', error)
    }
  }
}

// 点击推荐问题
const askSuggestedQuestion = (questionText: string) => {
  aiQuestion.value = questionText
  submitAIQuestion()
}

// 清空对话
const clearChat = () => {
  aiQuestion.value = ''
  aiAnswer.value = ''
  chatHistory.value = []
}

// 返回首页
const goToHome = () => {
  router.push('/')
}

// 格式化消息
const formatMessage = (content: string) => {
  return content.replace(/\n/g, '<br>')
}

// 格式化时间
const formatTime = (timestamp: number) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 组件挂载
onMounted(async () => {
  await loadUserProfile()
  await getCurrentArticle()
  generatePersonalizedQuestions()
})
</script>

<style scoped>
/* 页面整体布局 */
.ai-tutor-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f2f2f7 0%, #e5e5ea 100%);
  padding: 20px;
  display: flex;
  flex-direction: column;
}

/* 页面头部 */
.page-header {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  overflow: hidden;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 32px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.tutor-avatar {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.header-info h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-info p {
  margin: 4px 0 0 0;
  font-size: 16px;
  color: #718096;
}

/* 主要内容区域 */
.main-content {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 20px;
  max-width: 1400px;
  margin: 0 auto;
  align-items: start;
  flex: 1;
  height: 0;
}

/* 左侧边栏 */
.left-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: fit-content;
}

/* 卡片通用样式 */
.user-profile-card,
.learning-diagnosis-card,
.article-info-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.user-profile-card:hover,
.learning-diagnosis-card:hover,
.article-info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.card-header {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 16px 20px;
  border-bottom: 1px solid #e9ecef;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #495057;
}

/* 用户学习画像 */
.profile-content {
  padding: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.user-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.user-details h4 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.user-level-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 4px 0 6px 0;
}

.level-icon {
  font-size: 16px;
}

.level-name {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  text-transform: capitalize;
}

.level-description {
  margin: 0;
  font-size: 12px;
  color: #718096;
  line-height: 1.4;
}

.learning-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 12px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #718096;
}

/* 学习诊断 */
.diagnosis-content {
  padding: 20px;
}

/* 学习效率分析 */
.efficiency-analysis {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.efficiency-analysis h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.efficiency-metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.metric-label {
  font-size: 12px;
  color: #718096;
  margin-bottom: 4px;
}

.metric-value {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

/* 学习建议 */
.learning-recommendations {
  margin-top: 20px;
  padding: 16px;
  background: #e6f7ff;
  border-radius: 12px;
  border-left: 4px solid #007AFF;
}

.learning-recommendations h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.learning-recommendations ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.learning-recommendations li {
  font-size: 14px;
  color: #2d3748;
  margin-bottom: 8px;
  padding: 4px 0;
  position: relative;
  padding-left: 16px;
}

.learning-recommendations li:before {
  content: '💡';
  position: absolute;
  left: 0;
  top: 4px;
}

.level-indicator {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.current-level {
  margin-bottom: 16px;
}

.level-label {
  font-size: 14px;
  color: #718096;
  font-weight: 500;
  margin-bottom: 8px;
  display: block;
}

.level-display {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.level-display .level-icon {
  font-size: 20px;
}

.level-display .level-value {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  text-transform: capitalize;
}

.level-description {
  margin: 0;
  font-size: 13px;
  color: #718096;
  line-height: 1.4;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background: linear-gradient(90deg, #48bb78 0%, #38a169 100%);
  border-radius: 4px;
  transition: width 0.6s ease;
}

.strengths-weaknesses {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.strengths,
.weaknesses {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.strengths h4,
.weaknesses h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #495057;
}

.strengths ul,
.weaknesses ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.strengths li,
.weaknesses li {
  font-size: 13px;
  color: #718096;
  margin-bottom: 6px;
  padding: 4px 0;
}

.strengths li:last-child,
.weaknesses li:last-child {
  margin-bottom: 0;
}

/* 文章信息 */
.article-content {
  padding: 20px;
}

.article-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
  line-height: 1.4;
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 右侧对话区域 */
.right-content {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 1065px;
  max-height: 80vh;
}

/* 对话头部 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8f9fa;
}

.ai-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

.ai-status.ready {
  color: #48bb78;
}

.ai-status.loading {
  color: #ed8936;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 对话内容 */
.chat-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  min-height: 300px;
}

/* 欢迎内容 */
.welcome-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.welcome-message {
  text-align: center;
  max-width: 400px;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.welcome-message h3 {
  margin: 0 0 12px 0;
  font-size: 24px;
  font-weight: 600;
  color: #2d3748;
}

.welcome-message p {
  margin: 8px 0;
  font-size: 16px;
  color: #718096;
  line-height: 1.5;
}

/* 问题推荐区域 */
.questions-section {
  text-align: center;
}

.section-header h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 700;
  color: #2d3748;
}

.section-header p {
  margin: 0 0 20px 0;
  font-size: 14px;
  color: #718096;
}

.questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  max-width: 800px;
  margin: 0 auto;
}

.question-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.question-card:hover {
  border-color: #007AFF;
  background: #f8f9ff;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 122, 255, 0.15);
}

.question-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.question-text {
  font-size: 14px;
  font-weight: 500;
  color: #2d3748;
  margin-bottom: 6px;
  line-height: 1.4;
}

.question-type {
  font-size: 12px;
  color: #007AFF;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 对话历史 */
.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  color: white;
}

.message.ai .message-avatar {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
}

.message-content {
  flex: 1;
  max-width: 70%;
}

.message.user .message-content {
  text-align: right;
}

.message-text {
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  color: #2d3748;
  word-wrap: break-word;
}

.message.user .message-text {
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  color: white;
}

.message-time {
  font-size: 12px;
  color: #a0aec0;
  margin-top: 4px;
}

/* 输入区域 */
.chat-input {
  padding: 20px 24px;
  border-top: 1px solid #e2e8f0;
  background: #f8f9fa;
}

.input-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-input {
  border-radius: 12px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.input-tips {
  font-size: 12px;
  color: #a0aec0;
}

.send-btn {
  padding: 8px 24px;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 300px 1fr;
  }
  
  .right-content {
    min-height: 450px;
    max-height: 70vh;
  }
  
  .chat-content {
    min-height: 250px;
  }
}

@media (max-width: 768px) {
  .ai-tutor-page {
    padding: 10px;
  }
  
  .main-content {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .left-sidebar {
    order: 2;
  }
  
  .right-content {
    order: 1;
    min-height: 400px;
    max-height: 60vh;
  }
  
  .chat-content {
    min-height: 200px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .questions-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .question-card {
    padding: 12px;
  }
  
  .question-text {
    font-size: 13px;
  }
  
  .learning-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .strengths-weaknesses {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .learning-stats {
    grid-template-columns: 1fr;
  }
  
  .header-info h1 {
    font-size: 24px;
  }
  
  .tutor-avatar {
    width: 50px;
    height: 50px;
    font-size: 20px;
  }
}
</style>
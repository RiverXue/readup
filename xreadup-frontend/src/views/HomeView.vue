<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { articleApi, learningApi, vocabularyApi } from '@/utils/api'
import { Reading, Collection, TrendCharts, Calendar, Clock, Message, Search, Refresh } from '@element-plus/icons-vue'
import ArticleDiscovery from '@/components/ArticleDiscovery.vue'

const userStore = useUserStore()

// 学习统计数据
const learningStats = ref({
  streakDays: 0,
  totalReadingTime: 0,
  totalWordsLearned: 0,
  hasCheckedInToday: false,
  dueForReview: 0 // 设置为0，实际数据将从API获取
})
const statsLoading = ref(false)

// 获取学习统计数据
const fetchLearningStats = async () => {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) return

  statsLoading.value = true
  try {
    // 统一用户ID类型为字符串
    const userId = userStore.userInfo.id.toString()
    const numericUserId = parseInt(userId)
    
    // 先调用dailyCheckIn接口获取准确的连续打卡天数
    let checkInRes
    try {
      checkInRes = await learningApi.dailyCheckIn(userId)
      if (checkInRes?.data !== undefined) {
        // 注意：API返回的data直接就是连续打卡天数的数值，不是对象
        learningStats.value.streakDays = checkInRes.data
        // 由于API只返回连续天数，我们无法直接获取打卡状态，需要通过其他方式判断
        // 这里我们假设如果有连续天数，则说明已打卡
        if (learningStats.value.streakDays > 0) {
          learningStats.value.hasCheckedInToday = true
        }
        console.log('从dailyCheckIn接口获取连续打卡天数:', learningStats.value.streakDays)
      }
    } catch (checkInError) {
      console.warn('获取打卡数据失败，使用备用数据:', checkInError)
    }

    // 再调用其他统计数据接口
    try {
      const [summaryRes, timeRes, reviewWordsRes, userWordsRes] = await Promise.all([
        learningApi.getTodaySummary(userId), // 使用string类型参数
        learningApi.getReadingTimeStats(parseInt(userId)), // 使用number类型参数
        learningApi.getTodayReviewWords(userId), // 获取待复习单词列表
        vocabularyApi.getUserWords(userId) // 获取用户所有单词列表，用于本地计算待复习单词数
      ])

      if (summaryRes?.data) {
        // 如果dailyCheckIn接口没有返回天数，则使用summaryRes的数据作为备用
        if (learningStats.value.streakDays === 0) {
          learningStats.value.streakDays = summaryRes.data.streakDays || 0
        }
        // 如果dailyCheckIn接口没有返回打卡状态，则使用summaryRes的数据作为备用
        if (learningStats.value.hasCheckedInToday === false) {
          learningStats.value.hasCheckedInToday = summaryRes.data.hasCheckedIn || false
        }
        // 使用totalWords而不是totalWordsLearned，因为后端返回的是totalWords
        learningStats.value.totalWordsLearned = summaryRes.data.totalWords || 0
        
        // 初始化待复习单词数为0
        let dueForReviewCount = 0
        
        // 优先使用getTodayReviewWords的结果
        if (reviewWordsRes?.data && Array.isArray(reviewWordsRes.data)) {
          dueForReviewCount = reviewWordsRes.data.length || 0
          console.log('从getTodayReviewWords获取待复习单词数:', dueForReviewCount)
        }
        
        // 如果API返回的待复习单词数为0，尝试从本地单词列表中计算实际需要复习的单词数
        if (dueForReviewCount === 0) {
          // 获取本地存储的单词列表（与VocabularyPage.vue保持一致的数据源）
          const localWords = localStorage.getItem('userWords')
          let wordsList = []
          
          try {
            if (localWords) {
              wordsList = JSON.parse(localWords)
            }
          } catch (e) {
            console.warn('解析本地单词列表失败:', e)
          }
          
          // 如果本地有单词列表
          if (Array.isArray(wordsList) && wordsList.length > 0) {
            // 计算今天需要复习的单词（严格按照VocabularyPage.vue的主逻辑）
            const todayEnd = new Date(new Date().setHours(23, 59, 59, 999))
            
            // 添加详细日志记录，查看数据结构
            console.log('本地单词列表总数:', wordsList.length)
            const wordsWithNextReviewAt = wordsList.filter((word: any) => word.nextReviewAt).length
            const wordsWithNextReviewTime = wordsList.filter((word: any) => word.nextReviewTime).length
            console.log('含有nextReviewAt字段的单词数:', wordsWithNextReviewAt)
            console.log('含有nextReviewTime字段的单词数:', wordsWithNextReviewTime)
            
            // 先尝试使用VocabularyPage.vue主逻辑中的筛选条件，同时检查nextReviewAt和nextReviewTime
            const locallyDueForReview = wordsList.filter((word: any) => 
              (word.nextReviewTime || word.nextReviewAt) && 
              new Date(word.nextReviewTime || word.nextReviewAt) <= todayEnd
            ).length
            
            console.log('按主逻辑筛选的待复习单词数:', locallyDueForReview)
            
            // 如果主逻辑筛选结果为0，尝试使用错误处理中的逻辑（检查reviewStatus）
            if (locallyDueForReview === 0) {
              const errorHandlingDueForReview = wordsList.filter((word: any) => 
                word.reviewStatus === 'reviewing' && 
                (word.nextReviewTime || word.nextReviewAt) && 
                new Date(word.nextReviewTime || word.nextReviewAt) <= new Date()
              ).length
              
              console.log('按错误处理逻辑筛选的待复习单词数:', errorHandlingDueForReview)
              
              // 如果错误处理逻辑有结果，使用该结果
              if (errorHandlingDueForReview > 0) {
                dueForReviewCount = errorHandlingDueForReview
              }
            } else {
              // 如果主逻辑有结果，使用主逻辑的结果
              dueForReviewCount = locallyDueForReview
            }
            
            if (dueForReviewCount > 0) {
              console.log('从本地单词列表计算待复习单词数:', dueForReviewCount)
            }
          }
        }
        
        if (dueForReviewCount === 0 && userWordsRes?.data && Array.isArray(userWordsRes.data)) {
          // 计算今天需要复习的单词，同时检查nextReviewAt和nextReviewTime字段
          const todayEnd = new Date(new Date().setHours(23, 59, 59, 999))
          
          // 添加详细日志记录，查看数据结构
          console.log('用户单词列表总数:', userWordsRes.data.length)
          const wordsWithNextReviewAt = userWordsRes.data.filter((word: any) => word.nextReviewAt).length
          const wordsWithNextReviewTime = userWordsRes.data.filter((word: any) => word.nextReviewTime).length
          console.log('含有nextReviewAt字段的单词数:', wordsWithNextReviewAt)
          console.log('含有nextReviewTime字段的单词数:', wordsWithNextReviewTime)
          
          const locallyDueForReview = userWordsRes.data.filter((word: any) => 
            (word.nextReviewTime || word.nextReviewAt) && 
            new Date(word.nextReviewTime || word.nextReviewAt) <= todayEnd
          ).length
          
          console.log('符合条件的待复习单词数:', locallyDueForReview)
          
          if (locallyDueForReview > 0) {
            dueForReviewCount = locallyDueForReview
            console.log('从用户单词列表计算待复习单词数:', dueForReviewCount)
          }
        }
        
        learningStats.value.dueForReview = dueForReviewCount
      }

      if (timeRes?.data) {
        learningStats.value.totalReadingTime = timeRes.data.totalMinutes || 0
      }
    } catch (summaryError) {
      console.warn('获取学习统计数据失败，使用本地数据:', summaryError)
      // 发生错误时，使用本地存储的数据作为备选
      const localStreakDays = localStorage.getItem('streakDays')
      const localWordsLearned = localStorage.getItem('totalWordsLearned')
      const localReadingTime = localStorage.getItem('totalReadingTime')
      const localDueForReview = localStorage.getItem('dueForReview')
      
      if (localStreakDays) {
        learningStats.value.streakDays = parseInt(localStreakDays) || 0
      }
      if (localWordsLearned) {
        learningStats.value.totalWordsLearned = parseInt(localWordsLearned) || 0
      }
      if (localReadingTime) {
        learningStats.value.totalReadingTime = parseInt(localReadingTime) || 0
      }
      if (localDueForReview) {
        learningStats.value.dueForReview = parseInt(localDueForReview) || 0
      }
      console.log('使用本地存储的数据:', learningStats.value)
    }
  } catch (error) {
    console.error('获取学习统计数据失败:', error)
    // 如果发生严重错误，显示0表示没有数据
    learningStats.value.dueForReview = 0
    learningStats.value.totalWordsLearned = 0
  } finally {
    statsLoading.value = false
    
    console.log('学习统计数据加载完成:', learningStats.value)
    
    // 保存数据到本地存储，作为下次加载的备用
    if (learningStats.value.streakDays > 0) {
      localStorage.setItem('streakDays', learningStats.value.streakDays.toString())
    }
    if (learningStats.value.totalWordsLearned > 0) {
      localStorage.setItem('totalWordsLearned', learningStats.value.totalWordsLearned.toString())
    }
    if (learningStats.value.totalReadingTime > 0) {
      localStorage.setItem('totalReadingTime', learningStats.value.totalReadingTime.toString())
    }
    if (learningStats.value.dueForReview > 0) {
      localStorage.setItem('dueForReview', learningStats.value.dueForReview.toString())
    }
  }
}

// 格式化阅读时长
const formatReadingTime = computed(() => {
  const minutes = learningStats.value.totalReadingTime
  if (minutes < 60) {
    return `${minutes}分钟`
  } else {
    const hours = Math.floor(minutes / 60)
    const remainingMinutes = minutes % 60
    return `${hours}小时${remainingMinutes > 0 ? remainingMinutes + '分钟' : ''}`
  }
})

// 个性化欢迎语
const welcomeMessage = computed(() => {
  if (!userStore.userInfo) return ''
  const { username } = userStore.userInfo

  // 根据时间段生成不同的问候语
  const hour = new Date().getHours()
  let greeting = ''
  if (hour < 12) greeting = '早上好'
  else if (hour < 18) greeting = '下午好'
  else greeting = '晚上好'

  // 使用从API获取的连续学习天数
  const streakDays = learningStats.value.streakDays || 0
  let streakMessage = ''
  if (streakDays === 0) {
    streakMessage = '今天是个开始学习的好日子！'
  } else if (streakDays < 7) {
    streakMessage = `已坚持学习${streakDays}天，继续保持！`
  } else if (streakDays < 30) {
    streakMessage = `太棒了！连续学习${streakDays}天，好习惯正在养成中！`
  } else if (streakDays < 100) {
    streakMessage = `超级棒！连续学习${streakDays}天，你已经是学习达人了！`
  } else {
    streakMessage = `学习大神！已连续学习${streakDays}天，继续创造辉煌！`
  }

  return `${greeting}，${username}！${streakMessage}`
})

  // 场景化快捷入口 - 根据用户状态动态生成
const quickActions = computed(() => {
  // 基础操作
  const actions: Array<{ 
    title: string
    description: string
    icon: string
    path: string
    show: boolean
  }> = []

  // 如果未登录，显示基础引导
  if (!userStore.isLoggedIn) {
    actions.push(
      {
        title: '开始阅读',
        description: '体验AI驱动的双语阅读，点击单词即可查词',
        icon: 'reading',
        path: '/article/1',
        show: true
      },
      {
        title: '查看示例报告',
        description: '了解数据可视化如何追踪你的学习进度',
        icon: 'trend-charts',
        path: '/report',
        show: true
      },
      {
        title: '登录解锁全部功能',
        description: '登录后享受个性化学习体验',
        icon: 'message',
        path: '/login',
        show: true
      }
    )
    return actions
  }

  // 使用从API获取的已学习单词量来判断用户阶段
  const totalWords = learningStats.value.totalWordsLearned || 0
  
  // 新用户（单词量<50）
  if (totalWords < 50) {
    actions.push(
      {
        title: '开始你的第一篇阅读',
        description: '选择适合初学者的文章，开启学习之旅',
        icon: 'reading',  // 使用已导入的图标组件
        path: '/article/1',
        show: true
      },
      {
        title: 'AI翻译试用',
        description: '体验我们的AI翻译功能，提升阅读效率',
        icon: 'search',
        path: '/article/1?ai=1',
        show: true
      },
      {
        title: '创建学习计划',
        description: '设置每日学习目标，养成良好习惯',
        icon: 'calendar',
        path: '/subscription',
        show: true
      }
    )
  }
  // 活跃用户（单词量50-200）
  else if (totalWords < 200) {
    actions.push(
      { 
        title: `今日待复习单词 (${learningStats.value.dueForReview})`,
        description: '按时复习巩固记忆，提升学习效果',
        icon: 'refresh',
        path: '/vocabulary?type=review',
        show: learningStats.value.dueForReview > 0
      },
      {
        title: '生成个性化测验',
        description: '根据你的学习记录，生成定制化测验',
        icon: 'calendar',
        path: '/quiz',
        show: true
      },
      {
        title: '查看本周学习周报',
        description: '分析一周学习情况，优化学习策略',
        icon: 'trend-charts',
        path: '/report',
        show: true
      }
    )
  }
  // 高级用户（单词量>=200）
  else {
    actions.push(
      {
        title: '探索高级文章',
        description: '尝试更难的文章，挑战自我',
        icon: 'reading',  // 使用已导入的图标组件
        path: '/discovery?difficulty=B2',
        show: true
      },
      {
        title: '深度分析',
        description: '获取文章的深度解析和学习建议',
        icon: 'search',
        path: '/article/1?deep=1',
        show: true
      },
      {
        title: 'AI学习助手',
        description: '智能问答，解决你的学习疑问',
        icon: 'message',
        path: '/ai-assistant',
        show: true
      }
    )
  }

  // 添加通用的会员服务入口
  actions.push(
    {
      title: '💎 会员服务',
      description: '升级会员，解锁更多AI功能和优质内容',
      icon: 'calendar',
      path: '/subscription',
      show: true
    }
  )

  return actions.filter(action => action.show)
})

interface Article {
  id: number
  title: string
  description: string
  category: string
  difficultyLevel: string
  enContent?: string
}

const articles = ref<Article[]>([])
const loading = ref(false)

// 文章筛选功能
const filters = ref({
  category: '',
  difficulty: '',
  page: 1,
  size: 9
})

const totalArticles = ref(0)

const fetchArticles = async () => {
  loading.value = true
  try {
    // 将difficulty映射为difficultyLevel，确保与后端API字段一致
    const apiParams = {
      ...filters.value,
      difficultyLevel: filters.value.difficulty, // 映射字段名称
      difficulty: undefined // 删除原字段
    }
    const res = await articleApi.getArticles(apiParams)
    // 将difficultyLevel映射到difficulty属性，与ArticleReader.vue保持一致
    articles.value = (res.data.list || []).map((article: any) => ({
      ...article,
      difficulty: article.difficultyLevel || ''
    }))
    totalArticles.value = res.data.total || 0
  } catch (error) {
    console.error('获取文章失败:', error)
    articles.value = []
  } finally {
    loading.value = false
  }
}

// 分页处理函数
const handlePageChange = (page: number) => {
  filters.value.page = page
  fetchArticles()
}

onMounted(async () => {
  console.log('HomeView onMounted 开始加载数据')
  
  // 获取学习统计数据
  await fetchLearningStats()
  console.log('学习统计数据加载完成:', learningStats.value)
  
  // 获取文章列表
  await fetchArticles()
  console.log('文章列表加载完成:', articles.value.length, '篇')
  
  // 等待DOM更新完成
  await nextTick()
  console.log('DOM更新完成，应该可以看到待复习单词了')
})
</script>

<template>
  <div class="home-container">
    <!-- 顶部横幅 -->
    <div class="hero-section">
      <!-- 未登录状态 -->
      <div v-if="!userStore.isLoggedIn" class="hero-content">
        <h1>AI驱动的英语学习平台</h1>
        <p>用人工智能重新定义你的英语学习体验</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/article/1')">
            开始阅读
          </el-button>
          <el-button size="large" @click="$router.push('/login')">
            立即登录
          </el-button>
        </div>
      </div>
      
      <!-- 已登录状态 -->
      <div v-else class="hero-content logged-in">
        <div class="welcome-message">
          <span class="greeting-part">
            {{ new Date().getHours() < 12 ? '早上好' : (new Date().getHours() < 18 ? '下午好' : '晚上好') }},
            {{ userStore.userInfo?.username }}！
          </span>
          <span v-if="learningStats.streakDays === 0" class="info-part">今天是个开始学习的好日子！</span>
          <span v-else-if="learningStats.streakDays < 7" class="info-part">已坚持学习{{ learningStats.streakDays }}天，继续保持！</span>
          <span v-else-if="learningStats.streakDays < 30" class="info-part">太棒了！连续学习{{ learningStats.streakDays }}天，好习惯正在养成中！</span>
          <span v-else-if="learningStats.streakDays < 100" class="info-part">超级棒！连续学习{{ learningStats.streakDays }}天，你已经是学习达人了！</span>
          <span v-else class="info-part">学习大神！已连续学习{{ learningStats.streakDays }}天，继续创造辉煌！</span>
        </div>
        
        <!-- 学习统计数据，带加载状态 -->
        <div class="learning-summary" v-if="!statsLoading">
          <div class="summary-item">
            <Clock size="20" class="summary-icon time" />
            <span>总阅读时长：{{ formatReadingTime }}</span>
          </div>
          <div class="summary-item">
            <Reading size="20" class="summary-icon words" />
            <span>已学习单词：{{ learningStats.totalWordsLearned }}个</span>
          </div>
          <div class="summary-item">
            <Refresh size="20" class="summary-icon review" />
            <span>待复习单词：{{ learningStats.dueForReview }}个</span>
          </div>
          <div class="summary-item">
            <Calendar size="20" :class="['summary-icon', { 'streak': learningStats.streakDays > 0 }]" />
            <span>连续打卡：{{ learningStats.streakDays }}天</span>
          </div>
          <!-- 数据加载失败时显示提示 -->
          <div v-if="learningStats.streakDays === 0 && learningStats.totalWordsLearned === 0 && learningStats.totalReadingTime === 0 && learningStats.dueForReview === 0" 
               class="data-loading-error">
            <Message size="20" class="summary-icon" />
            <span>正在同步数据，请稍后刷新页面</span>
          </div>
        </div>
        
        <!-- 加载状态指示器 -->
        <div v-else class="learning-summary-loading">
          <el-skeleton :rows="3" animated class="skeleton-item" />
        </div>
        
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/vocabulary')">
            今日复习
          </el-button>
          <el-button size="large" @click="$router.push('/article/1')">
            继续阅读
          </el-button>
        </div>
      </div>
    </div>

    <!-- 场景化快捷入口 - 仅在未登录状态下显示 -->
    <div class="quick-actions-section" v-if="!userStore.isLoggedIn">
      <h2>今日推荐</h2>
      <div class="actions-grid">
        <el-card
          v-for="action in quickActions"
          :key="action.title"
          class="action-card"
          @click="$router.push(action.path)"
        >
          <div class="action-icon">
                  <el-icon :size="32">
                    <Reading v-if="action.icon === 'reading' || action.icon === 'book'" />
                    <Collection v-if="action.icon === 'collection'" />
                    <TrendCharts v-if="action.icon === 'trend-charts'" />
                    <Message v-if="action.icon === 'message'" />
                    <Calendar v-if="action.icon === 'calendar'" />
                    <Search v-if="action.icon === 'search'" />
                    <Refresh v-if="action.icon === 'refresh'" />
                  </el-icon>
                </div>
          <h3>{{ action.title }}</h3>
          <p>{{ action.description }}</p>
        </el-card>
      </div>
    </div>

    <!-- 智能文章发现中心 -->
    <div class="discovery-section">
      <h2>推荐文章</h2>
      
      <!-- 筛选栏 -->
      <div class="filters-bar">
        <el-select v-model="filters.category" placeholder="全部分类" size="small" @change="fetchArticles" style="min-width: 120px;">
          <el-option value="">全部分类</el-option>
          <el-option value="technology">科技</el-option>
          <el-option value="health">健康</el-option>
          <el-option value="business">商业</el-option>
          <el-option value="education">教育</el-option>
          <el-option value="entertainment">娱乐</el-option>
          <el-option value="sports">体育</el-option>
          <el-option value="travel">旅行</el-option>
          <el-option value="food">美食</el-option>
        </el-select>
        <el-select v-model="filters.difficulty" placeholder="难度" size="small" @change="fetchArticles" style="min-width: 100px;">
          <el-option value="">全难度</el-option>
          <el-option v-for="level in ['A1','A2','B1','B2','C1','C2']" :key="level" :label="level" :value="level" />
        </el-select>
        <el-button type="primary" @click="fetchArticles" size="small">
          筛选
        </el-button>
      </div>

      <!-- 文章列表 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="articles.length === 0" class="empty-container">
        <el-empty description="暂无文章数据" />
      </div>
      <div v-else class="articles-grid">
        <el-card
          v-for="article in articles"
          :key="article.id"
          class="article-card"
          @click="$router.push(`/article/${article.id}`)"
        >
          <div class="article-meta">
            <el-tag size="small">{{ article.category || '未分类' }}</el-tag>
            <el-tag size="small" type="info">{{ article.difficultyLevel || '未知' }}</el-tag>
          </div>
          <h3>{{ article.title || '无标题' }}</h3>
          <p>{{ article.description || article.enContent?.substring(0, 100) + '...' || '暂无描述' }}</p>
          <div class="article-action">
            <el-button type="text">开始阅读 →</el-button>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="totalArticles > 0"
        v-model:current-page="filters.page"
        :page-size="filters.size"
        :total="totalArticles"
        @current-change="handlePageChange"
        layout="prev, pager, next, total"
        class="pagination"
      />
    </div>

    <!-- 文章发现组件 -->
    <div class="article-discovery-section">
      <ArticleDiscovery></ArticleDiscovery>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  width: 100%;
  padding: 0;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-section {
  text-align: center;
  padding: 80px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px;
  margin-bottom: 50px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(102, 126, 234, 0.25);
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(255, 255, 255, 0.1) 0%, transparent 50%, rgba(255, 255, 255, 0.05) 100%);
  pointer-events: none;
}

.welcome-message {
      margin-bottom: 15px;
      text-align: center;
    }

.greeting-part {
  display: block;
  font-size: 2.4em;
  font-weight: bold;
  margin-bottom: 12px;
  color: #fff;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 2;
}

.info-part {
  display: block;
  font-size: 1.4em;
  font-weight: normal;
  color: rgba(255, 255, 255, 0.95);
  opacity: 0.95;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 2;
}

.hero-section p {
  font-size: 1.2em;
  margin-bottom: 30px;
  opacity: 0.9;
}

.hero-content.logged-in {
  border-radius: 15px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.hero-content.logged-in:hover {
  transform: translateY(-2px);
}

/* 移除旋转动画，保持静态背景纹理 */
.hero-section::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  pointer-events: none;
}

.hero-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 20px;
}

.learning-summary {
  display: flex;
  justify-content: center;
  gap: 25px;
  margin: 30px 0;
  flex-wrap: wrap;
  position: relative;
  z-index: 2;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  padding: 12px 20px;
  border-radius: 25px;
  font-size: 0.95em;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 8px 16px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.summary-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.summary-item:hover::before {
  left: 100%;
}

.summary-item:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.35);
  box-shadow: 
    0 12px 24px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.summary-icon {
  color: #fff;
}

.summary-icon.review {
  color: #ffcc00;
  animation: pulse 2s infinite;
}

.summary-icon.time {
      color: #409eff;
      animation: glowEffect 3s infinite;
    }

    .summary-icon.words {
      color: #67c23a;
      animation: glowEffect 3s infinite;
    }

    .summary-icon.streak {
      color: #ff6b6b;
      animation: glowEffect 3s infinite;
    }

    @keyframes glowEffect {
      0%, 100% {
        filter: brightness(1) drop-shadow(0 0 2px currentColor);
      }
      50% {
        filter: brightness(1.2) drop-shadow(0 0 6px currentColor);
      }
    }

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}

.quick-actions-section,
.discovery-section,
.article-discovery-section {
  margin-bottom: 60px;
  position: relative;
}

.quick-actions-section::before,
.discovery-section::before,
.article-discovery-section::before {
  content: '';
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #409eff, #67c23a);
  border-radius: 2px;
  opacity: 0.6;
}

.quick-actions-section h2,
.discovery-section h2,
.article-discovery-section h2 {
  text-align: center;
  margin-bottom: 40px;
  color: #303133;
  font-size: 2em;
  font-weight: 600;
  position: relative;
  padding-bottom: 15px;
}

.quick-actions-section h2::after,
.discovery-section h2::after,
.article-discovery-section h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #409eff, #67c23a);
  border-radius: 2px;
}

.loading-container,
.empty-container {
  text-align: center;
  padding: 40px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(200px, 1fr));
  gap: 25px;
  justify-items: center;
  padding: 20px 0;
}

@media (max-width: 1200px) {
  .actions-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 25px;
  padding: 20px 0;
}

.action-card,
.article-card {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
  border: 1px solid rgba(0, 0, 0, 0.05);
  background: linear-gradient(145deg, #ffffff 0%, #f8f9fa 100%);
  box-shadow: 
    0 4px 6px rgba(0, 0, 0, 0.05),
    0 1px 3px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  position: relative;
  overflow: hidden;
}

.action-card::before,
.article-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.6s ease;
}

.action-card:hover::before,
.article-card:hover::before {
  left: 100%;
}

.action-card:hover,
.article-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.12),
    0 8px 16px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border-color: rgba(64, 158, 255, 0.2);
}

.action-icon {
  margin-bottom: 15px;
  color: #409eff;
}

.action-card h3 {
  margin: 10px 0;
  color: #303133;
  font-size: 1.1em;
}
/* 加载状态样式 */
.learning-summary-loading {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin: 20px 0;
  flex-wrap: wrap;
}

.skeleton-item {
  width: 200px;
}

/* 数据加载失败样式 */
.data-loading-error {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 193, 7, 0.1);
  color: #ffc107;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9em;
  margin-top: 10px;
}

.filters-bar {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.article-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.article-card h3 {
  margin: 10px 0;
  color: #303133;
}

.article-card p {
  color: #606266;
  margin-bottom: 15px;
}

.pagination {
    margin-top: 30px;
    text-align: center;
    display: flex;
    justify-content: center;
  }

.user-section {
  margin-bottom: 40px;
}

.login-prompt {
  text-align: center;
  padding: 40px;
}

.login-prompt h3 {
  margin-bottom: 10px;
  color: #303133;
}

.login-prompt p {
  color: #606266;
  margin-bottom: 20px;
}

@media (max-width: 1200px) {
  .articles-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 900px) {
  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.hero-recommendations {
    background: rgba(255, 255, 255, 0.1);
    padding: 15px 20px;
    border-radius: 10px;
    margin: 20px auto;
    max-width: 800px;
  }
  
  .hero-quick-actions {
    display: flex;
    justify-content: center;
    gap: 20px;
    flex-wrap: wrap;
  }
  
  .hero-action-item {
    display: flex;
    align-items: center;
    gap: 8px;
    background: rgba(255, 255, 255, 0.2);
    padding: 10px 15px;
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.3s;
  }
  
  .hero-action-item:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: translateY(-2px);
  }
  
  .hero-action-icon {
    color: #fff;
  }
  
  @media (max-width: 768px) {
    .hero-section {
      padding: 40px 20px;
    }

    .hero-section h1 {
      font-size: 1.8em;
    }

    .hero-actions {
      flex-direction: column;
      align-items: center;
    }

    .learning-summary {
      flex-direction: column;
      gap: 10px;
    }

    .actions-grid,
    .articles-grid {
      grid-template-columns: 1fr;
    }

    .home-container {
      padding: 0;
    }

    .filters-bar {
      flex-direction: column;
      align-items: center;
    }

    .filters-bar .el-select {
      width: 100%;
      max-width: 200px;
    }
    
    .hero-quick-actions {
      flex-direction: column;
      align-items: center;
    }
    
    .hero-action-item {
      width: 100%;
      justify-content: center;
    }
  }
</style>




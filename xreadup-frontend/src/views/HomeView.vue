<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { articleApi, learningApi, vocabularyApi } from '@/utils/api'
import { Reading, Collection, TrendCharts, Calendar, Clock, Message, Search, Refresh } from '@element-plus/icons-vue'
import ArticleDiscovery from '@/components/ArticleDiscovery.vue'
import TactileButton from '@/components/common/TactileButton.vue'
import ArticleCard from '@/components/ArticleCard.vue'

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
    return `${hours}小时`
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

// 推荐文章相关
const recommendArticles = ref<Article[]>([])
const recommendLoading = ref(false)
const userInterestTags = ref<string[]>([])

// 文章筛选功能（保留用于探索文章）
const articles = ref<Article[]>([])
const loading = ref(false)

const filters = ref({
  category: '',
  difficulty: '',
  page: 1,
  size: 9
})

const totalArticles = ref(0)

// 分类中英文映射
const categoryMap: Record<string, string> = {
  'technology': '科技',

  'business': '商业',
  'culture': '文化',
  'education': '教育',
  'health': '健康',
  'sports': '体育',
  'entertainment': '娱乐',
  'travel': '旅行',
  'food': '美食'
}

// 创建反向映射（中文到英文）
const reverseCategoryMap: Record<string, string> = {}
Object.entries(categoryMap).forEach(([en, zh]) => {
  reverseCategoryMap[zh] = en
})

// 将兴趣标签转换为英文分类
const convertInterestTagsToEnglish = (tags: string[]): string[] => {
  return tags.map(tag => {
    const trimmedTag = tag.trim()
    // 如果是中文，转换为英文
    if (reverseCategoryMap[trimmedTag]) {
      return reverseCategoryMap[trimmedTag]
    }
    // 如果已经是英文，直接返回
    if (categoryMap[trimmedTag]) {
      return trimmedTag
    }
    // 如果都不匹配，尝试模糊匹配
    const fuzzyMatch = Object.keys(categoryMap).find(key => 
      key.toLowerCase().includes(trimmedTag.toLowerCase()) ||
      categoryMap[key].includes(trimmedTag)
    )
    return fuzzyMatch || trimmedTag
  }).filter(tag => categoryMap[tag]) // 只保留有效的分类
}

// 获取用户兴趣标签
const fetchUserInterestTags = async () => {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    console.log('用户未登录，使用默认兴趣标签')
    userInterestTags.value = ['technology', 'health', 'business'] // 默认标签
    return
  }

  try {
    // 从用户信息中获取兴趣标签
    const userInfo = userStore.userInfo as any
    if (userInfo?.interestTag) {
      // 如果interestTag是字符串，按逗号分割
      const rawTags = userInfo.interestTag.split(',').map((tag: string) => tag.trim())
      // 转换为英文分类
      userInterestTags.value = convertInterestTagsToEnglish(rawTags)
    } else {
      // 如果没有兴趣标签，使用默认标签
      userInterestTags.value = ['technology', 'health', 'business', 'education', 'entertainment']
    }
    console.log('用户兴趣标签（原始）:', userInfo?.interestTag)
    console.log('用户兴趣标签（转换后）:', userInterestTags.value)
  } catch (error) {
    console.warn('获取用户兴趣标签失败，使用默认标签:', error)
    userInterestTags.value = ['technology', 'health', 'business']
  }
}

// 获取推荐文章
const fetchRecommendArticles = async (isRefresh = false) => {
  recommendLoading.value = true
  try {
    // 确保有用户兴趣标签
    if (userInterestTags.value.length === 0) {
      await fetchUserInterestTags()
    }

    // 随机选择一个兴趣标签
    let randomTag
    if (isRefresh && userInterestTags.value.length > 1) {
      // 换一批时，如果用户有多个兴趣标签，随机选择不同的标签
      const currentTag = recommendArticles.value.length > 0 ? recommendArticles.value[0]?.category : null
      const availableTags = userInterestTags.value.filter(tag => tag !== currentTag)
      randomTag = availableTags[Math.floor(Math.random() * availableTags.length)] || userInterestTags.value[Math.floor(Math.random() * userInterestTags.value.length)]
    } else {
      randomTag = userInterestTags.value[Math.floor(Math.random() * userInterestTags.value.length)]
    }
    
    // 使用不同的排序方式确保获取不同内容
    const sortOptions = [
      { sortBy: 'publishedAt', sortOrder: 'desc' },
      { sortBy: 'publishedAt', sortOrder: 'asc' },
      { sortBy: 'readCount', sortOrder: 'desc' },
      { sortBy: 'readCount', sortOrder: 'asc' }
    ]
    
    // 如果是刷新，随机选择排序方式；否则使用默认排序
    const sortOption = isRefresh 
      ? sortOptions[Math.floor(Math.random() * sortOptions.length)]
      : sortOptions[0]
    
    // 如果是刷新，使用随机页码（1-3页）获取不同内容
    const page = isRefresh ? Math.floor(Math.random() * 3) + 1 : 1
    
    const response = await articleApi.getArticles({
      category: randomTag,
      page: page,
      size: 9,
      sortBy: sortOption.sortBy,
      sortOrder: sortOption.sortOrder
    })

    if (response?.data?.list) {
      recommendArticles.value = response.data.list.map((article: any) => ({
        ...article,
        difficulty: article.difficultyLevel || '',
        wordCount: article.wordCount || article.word_count || 0
      }))
    } else {
      recommendArticles.value = []
    }
    
    console.log(`获取推荐文章成功，基于标签: ${randomTag}，排序: ${sortOption.sortBy}-${sortOption.sortOrder}，页码: ${page}，共${recommendArticles.value.length}篇`)
  } catch (error) {
    console.error('获取推荐文章失败:', error)
    recommendArticles.value = []
  } finally {
    recommendLoading.value = false
  }
}

// 换一批推荐文章
const refreshRecommendArticles = async () => {
  await fetchRecommendArticles(true) // 传递true表示是刷新操作
}

// 处理文章点击
const handleArticleClick = (article: Article) => {
  // 这里可以添加点击统计或其他逻辑
  console.log('点击文章:', article.title)
}

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

  // 获取推荐文章
  await fetchRecommendArticles()
  console.log('推荐文章加载完成:', recommendArticles.value.length, '篇')

  // 获取文章列表（用于探索文章）
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
        <h1 class="hero-title">AI驱动的英语学习平台</h1>
        <p class="hero-subtitle">用人工智能重新定义你的英语学习体验</p>
        <div class="hero-actions">
          <TactileButton variant="primary" size="lg" @click="$router.push('/article/1')">
            <template #icon>
              <Reading size="20" />
            </template>
            开始阅读
          </TactileButton>
          <TactileButton variant="secondary" size="lg" @click="$router.push('/login')">
            <template #icon>
              <Message size="20" />
            </template>
            立即登录
          </TactileButton>
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
          <TactileButton variant="warning" size="lg" @click="$router.push('/vocabulary')">
            <template #icon>
              <Refresh size="20" />
            </template>
            今日复习
          </TactileButton>
          <TactileButton variant="primary" size="lg" @click="$router.push('/article/1')">
            <template #icon>
              <Reading size="20" />
            </template>
            继续阅读
          </TactileButton>
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

    <!-- 智能文章推荐中心 -->
    <div class="recommend-section">
      <div class="section-header">
        <h2>推荐文章</h2>
        <TactileButton variant="primary" size="md" @click="refreshRecommendArticles" class="refresh-button">
          <template #icon>
            <Refresh size="16" />
          </template>
          换一批
        </TactileButton>
      </div>

      <!-- 文章列表 -->
      <div v-if="recommendLoading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="recommendArticles.length === 0" class="empty-container">
        <el-empty description="暂无推荐文章" />
      </div>
      <div v-else class="recommend-articles-grid">
        <ArticleCard
          v-for="article in recommendArticles"
          :key="article.id"
          :article="article"
          @click="handleArticleClick(article)"
        />
      </div>
    </div>

    <!-- 文章发现组件 -->
    <div class="article-discovery-section">
      <ArticleDiscovery></ArticleDiscovery>
    </div>
  </div>
</template>

<style scoped>
@import '@/assets/design-system.css';

.home-container {
  width: 100%;
  padding: 0;
  animation: fadeInUp 0.8s ease-out;
  background: linear-gradient(135deg, 
    rgba(248, 250, 252, 0.3) 0%, 
    rgba(241, 245, 249, 0.2) 50%, 
    rgba(248, 250, 252, 0.3) 100%);
  border-radius: var(--radius-2xl);
  position: relative;
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

@keyframes liquidFlow {
  0%, 100% { 
    opacity: 0.1;
    transform: scale(1);
  }
  50% { 
    opacity: 0.2;
    transform: scale(1.02);
  }
}

.hero-section {
  text-align: center;
  padding: var(--space-8) var(--space-6);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%, 
    rgba(248, 250, 252, 0.9) 50%, 
    rgba(241, 245, 249, 0.95) 100%);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: var(--radius-3xl);
  margin-bottom: var(--space-12);
  position: relative;
  overflow: hidden;
  box-shadow: 
    0 12px 48px rgba(0, 0, 0, 0.15),
    0 4px 16px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    inset 0 -1px 0 rgba(0, 0, 0, 0.05);
  border: 3px solid rgba(255, 255, 255, 0.4);
  color: var(--text-primary);
  transition: all var(--transition-normal);
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.05) 0%, rgba(90, 200, 250, 0.03) 50%, rgba(0, 122, 255, 0.05) 100%);
  pointer-events: none;
  animation: liquidFlow 20s ease-in-out infinite;
}

.hero-section:hover {
  transform: translateY(-4px);
  box-shadow: 
    0 16px 64px rgba(0, 0, 0, 0.2),
    0 8px 24px rgba(0, 0, 0, 0.15),
    0 4px 12px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    inset 0 -1px 0 rgba(0, 0, 0, 0.08);
  border-color: rgba(0, 122, 255, 0.3);
}

.hero-section::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, 
    rgba(0, 122, 255, 0.1) 0%, 
    rgba(90, 200, 250, 0.05) 25%, 
    rgba(0, 122, 255, 0.1) 50%, 
    rgba(90, 200, 250, 0.05) 75%, 
    rgba(0, 122, 255, 0.1) 100%);
  border-radius: var(--radius-3xl);
  z-index: -1;
  opacity: 0;
  transition: opacity var(--transition-normal);
  pointer-events: none;
}

.hero-section:hover::after {
  opacity: 1;
}

.welcome-message {
  margin-bottom: var(--space-4);
  text-align: center;
}

.greeting-part {
  display: block;
  font-size: var(--text-4xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--space-3);
  color: var(--text-primary);
  text-shadow: none;
  position: relative;
  z-index: 2;
  font-family: var(--font-family-display);
  letter-spacing: -0.02em;
}

.info-part {
  display: block;
  font-size: var(--text-xl);
  font-weight: var(--font-weight-normal);
  color: var(--text-secondary);
  opacity: 1;
  text-shadow: none;
  position: relative;
  z-index: 2;
}

.hero-section p {
  font-size: var(--text-lg);
  margin-bottom: var(--space-8);
  color: var(--text-secondary);
  opacity: 1;
}

.hero-title {
  font-size: var(--text-4xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--space-4);
  color: var(--text-primary);
  font-family: var(--font-family-display);
  letter-spacing: -0.02em;
}

.hero-subtitle {
  font-size: var(--text-xl);
  color: var(--text-secondary);
  margin-bottom: var(--space-8);
  font-weight: var(--font-weight-normal);
}

.hero-content.logged-in {
  border-radius: var(--radius-2xl);
  transition: all var(--transition-normal);
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
  gap: var(--space-6);
  justify-content: center;
  margin-top: var(--space-8);
  position: relative;
  z-index: 3;
}

/* 去掉Hero区域内按钮的外发光效果 */
.hero-actions .tactile-button--primary {
  box-shadow: 
    0 4px 12px rgba(52, 199, 89, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.hero-actions .tactile-button--primary:hover:not(.tactile-button--disabled) {
  box-shadow: 
    0 6px 16px rgba(52, 199, 89, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.hero-actions .tactile-button--warning {
  box-shadow: 
    0 4px 12px rgba(255, 149, 0, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.hero-actions .tactile-button--warning:hover:not(.tactile-button--disabled) {
  box-shadow: 
    0 6px 16px rgba(255, 149, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.learning-summary {
  display: flex;
  justify-content: center;
  gap: var(--space-6);
  margin: var(--space-8) 0;
  flex-wrap: wrap;
  position: relative;
  z-index: 2;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.8) 0%, 
    rgba(248, 250, 252, 0.6) 50%, 
    rgba(241, 245, 249, 0.8) 100%);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  padding: var(--space-4) var(--space-6);
  border-radius: var(--radius-ios-large);
  font-size: var(--text-sm);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
  font-family: var(--font-family-primary);
  color: var(--text-primary);
}

.summary-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left var(--transition-slow);
}

.summary-item:hover::before {
  left: 100%;
}

.summary-item:hover {
  transform: translateY(-3px) scale(1.02);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.9) 0%, 
    rgba(248, 250, 252, 0.8) 50%, 
    rgba(241, 245, 249, 0.9) 100%);
  box-shadow: 
    0 8px 24px rgba(0, 0, 0, 0.12),
    0 2px 6px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border-color: var(--ios-blue);
}

.summary-icon {
  color: var(--ios-blue);
  animation: iconGlow 3s ease-in-out infinite;
}
.summary-icon.time { 
  color: #ba68c8; 
  animation: iconGlowTime 3s ease-in-out infinite;
}
.summary-icon.words { 
  color: #67c23a; 
  animation: iconGlowWords 3s ease-in-out infinite;
}
.summary-icon.review { 
  color: #ffcc00; 
  animation: iconGlowReview 3s ease-in-out infinite;
}
.summary-icon.streak { 
  color: #ff6b6b; 
  animation: iconGlowStreak 3s ease-in-out infinite;
}

@keyframes iconGlow {
  0%, 100% {
    filter: brightness(1) drop-shadow(0 0 4px rgba(0, 122, 255, 0.3));
  }
  50% {
    filter: brightness(1.2) drop-shadow(0 0 8px rgba(0, 122, 255, 0.6));
  }
}

@keyframes iconGlowTime {
  0%, 100% {
    filter: brightness(1) drop-shadow(0 0 4px rgba(186, 104, 200, 0.3));
  }
  50% {
    filter: brightness(1.2) drop-shadow(0 0 8px rgba(186, 104, 200, 0.6));
  }
}

@keyframes iconGlowWords {
  0%, 100% {
    filter: brightness(1) drop-shadow(0 0 4px rgba(103, 194, 58, 0.3));
  }
  50% {
    filter: brightness(1.2) drop-shadow(0 0 8px rgba(103, 194, 58, 0.6));
  }
}

@keyframes iconGlowReview {
  0%, 100% {
    filter: brightness(1) drop-shadow(0 0 4px rgba(255, 204, 0, 0.3));
  }
  50% {
    filter: brightness(1.2) drop-shadow(0 0 8px rgba(255, 204, 0, 0.6));
  }
}

@keyframes iconGlowStreak {
  0%, 100% {
    filter: brightness(1) drop-shadow(0 0 4px rgba(255, 107, 107, 0.3));
  }
  50% {
    filter: brightness(1.2) drop-shadow(0 0 8px rgba(255, 107, 107, 0.6));
  }
}

.quick-actions-section,
.discovery-section,
.article-discovery-section {
  margin-bottom: var(--space-16);
  position: relative;
}

.quick-actions-section::before,
.discovery-section::before,
.article-discovery-section::before {
  content: '';
  position: absolute;
  top: -var(--space-5);
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-500), var(--warm-orange));
  border-radius: var(--radius-sm);
  opacity: 0.6;
}

.quick-actions-section h2,
.discovery-section h2,
.article-discovery-section h2 {
  text-align: center;
  margin-bottom: var(--space-12);
  color: var(--text-primary);
  font-size: var(--text-4xl);
  font-weight: var(--font-weight-semibold);
  position: relative;
  padding-bottom: var(--space-4);
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
  background: linear-gradient(90deg, var(--primary-500), var(--warm-orange));
  border-radius: var(--radius-sm);
}

.loading-container,
.empty-container {
  text-align: center;
  padding: var(--space-12);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(200px, 1fr));
  gap: var(--space-6);
  justify-items: center;
  padding: var(--space-5) 0;
}

@media (max-width: 1200px) {
  .actions-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-6);
  padding: var(--space-5) 0;
}

.action-card,
.article-card {
  cursor: pointer;
  transition: all var(--transition-normal);
  text-align: center;
  border: 1px solid var(--glass-border);
  background: var(--bg-primary);
  box-shadow:
    0 4px 20px var(--glass-shadow),
    0 1px 3px rgba(0, 0, 0, 0.1);
  border-radius: var(--radius-ios-large);
  position: relative;
  overflow: hidden;
  font-family: var(--font-family-primary);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
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
  transition: left var(--transition-slow);
}

.action-card:hover::before,
.article-card:hover::before {
  left: 100%;
}

.action-card:hover,
.article-card:hover {
  transform: translateY(-8px) scale(1.03);
  box-shadow:
    0 20px 40px var(--glass-shadow-medium),
    0 4px 8px rgba(0, 0, 0, 0.08);
  border-color: var(--ios-blue);
  background: var(--glass-white);
}

.action-icon {
  margin-bottom: var(--space-4);
  color: var(--primary-500);
}

.action-card h3 {
  margin: var(--space-3) 0;
  color: var(--text-primary);
  font-size: var(--text-lg);
}
/* 加载状态样式 */
.learning-summary-loading {
  display: flex;
  justify-content: center;
  gap: var(--space-4);
  margin: var(--space-5) 0;
  flex-wrap: wrap;
}

.skeleton-item {
  width: 200px;
}

/* 数据加载失败样式 */
.data-loading-error {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  background: rgba(255, 193, 7, 0.1);
  color: var(--accent-warning);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-3xl);
  font-size: var(--text-sm);
  margin-top: var(--space-3);
}

.filters-bar {
  display: flex;
  gap: var(--space-6);
  justify-content: center;
  margin-bottom: var(--space-8);
  flex-wrap: wrap;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  align-items: center;
}

.filter-label {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-secondary);
  margin-bottom: var(--space-1);
}

.modern-select {
  min-width: 180px;
}

.custom-select-wrapper {
  min-width: 180px;
}

.custom-select-wrapper :deep(.el-input__wrapper) {
  background: var(--glass-white) !important;
  backdrop-filter: blur(16px) !important;
  -webkit-backdrop-filter: blur(16px) !important;
  border: 1px solid var(--glass-border) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: 
    0 4px 16px var(--glass-shadow),
    inset 0 1px 0 rgba(255, 255, 255, 0.1) !important;
  transition: all var(--transition-normal) !important;
}

.custom-select-wrapper :deep(.el-input__wrapper:hover) {
  border-color: var(--ios-blue) !important;
  box-shadow: 
    0 6px 20px var(--glass-shadow-medium),
    inset 0 1px 0 rgba(255, 255, 255, 0.15) !important;
}

.custom-select-wrapper :deep(.el-input__wrapper.is-focus) {
  border-color: var(--ios-blue) !important;
  box-shadow: 
    0 0 0 3px rgba(0, 122, 255, 0.1),
    0 6px 20px var(--glass-shadow-medium) !important;
}

.custom-select-wrapper :deep(.el-input__inner) {
  color: var(--text-primary) !important;
  font-size: 14px !important;
}

.custom-select-wrapper :deep(.el-select__caret) {
  color: var(--text-secondary) !important;
}

.filter-button {
  margin-top: var(--space-6);
}

/* 推荐文章部分样式 */
.recommend-section {
  margin-bottom: var(--space-12);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-8);
}

.section-header h2 {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
}

.refresh-button {
  margin-left: var(--space-4);
}

.recommend-articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--space-6);
  margin-bottom: var(--space-8);
}

@media (max-width: 768px) {
  .recommend-articles-grid {
    grid-template-columns: 1fr;
    gap: var(--space-4);
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-4);
  }
  
  .refresh-button {
    margin-left: 0;
  }
}

.article-meta {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.article-card h3 {
  margin: var(--space-3) 0;
  color: var(--text-primary);
}

.article-card p {
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
}

.pagination-container {
  margin-top: var(--space-8);
  display: flex;
  justify-content: center;
}

.modern-pagination :deep(.el-pagination) {
  background: var(--glass-white);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  padding: var(--space-3) var(--space-4);
  box-shadow: 
    0 4px 16px var(--glass-shadow),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.modern-pagination :deep(.el-pager li) {
  background: transparent;
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  margin: 0 var(--space-1);
  transition: all var(--transition-normal);
}

.modern-pagination :deep(.el-pager li:hover) {
  background: var(--glass-white-medium);
  border-color: var(--ios-blue);
  transform: translateY(-1px);
}

.modern-pagination :deep(.el-pager li.is-active) {
  background: var(--gradient-primary);
  border-color: var(--ios-blue);
  color: var(--text-inverse);
  box-shadow: var(--shadow-primary);
}

.user-section {
  margin-bottom: var(--space-12);
}

.login-prompt {
  text-align: center;
  padding: var(--space-12);
}

.login-prompt h3 {
  margin-bottom: var(--space-3);
  color: var(--text-primary);
}

.login-prompt p {
  color: var(--text-secondary);
  margin-bottom: var(--space-5);
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
  padding: var(--space-4) var(--space-5);
  border-radius: var(--radius-xl);
  margin: var(--space-5) auto;
  max-width: 800px;
}

.hero-quick-actions {
  display: flex;
  justify-content: center;
  gap: var(--space-5);
  flex-wrap: wrap;
}

.hero-action-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  background: rgba(255, 255, 255, 0.2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-3xl);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.hero-action-item:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.hero-action-icon {
  color: var(--text-inverse);
}

@media (max-width: 768px) {
  .hero-section {
    padding: var(--space-8) var(--space-4);
    margin-bottom: var(--space-8);
  }

  .hero-section h1 {
    font-size: var(--text-3xl);
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
    gap: var(--space-4);
  }

  .learning-summary {
    flex-direction: column;
    gap: var(--space-3);
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
    gap: var(--space-4);
  }

  .filter-group {
    width: 100%;
    max-width: 200px;
  }

  .modern-select {
    width: 100%;
  }

  .filter-button {
    margin-top: var(--space-4);
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




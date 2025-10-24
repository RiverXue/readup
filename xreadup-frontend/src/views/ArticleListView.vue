<template>
  <div class="article-list-container">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">阅读列表</h1>
        <p class="page-description">探索精选文章，提升英语阅读能力</p>
      </div>
      
      <!-- 用户阅读统计卡片 -->
      <div class="stats-card" v-if="userStore.isLoggedIn">
        <div class="stats-item">
          <el-icon><Clock /></el-icon>
          <div class="stats-content">
            <div class="stats-value">{{ formatReadingTime }}</div>
            <div class="stats-label">总阅读时长</div>
          </div>
        </div>
        <div class="stats-item">
          <el-icon><Document /></el-icon>
          <div class="stats-content">
            <div class="stats-value">{{ readArticleCount }}</div>
            <div class="stats-label">已读文章</div>
          </div>
        </div>
        <div class="stats-item">
          <el-icon><Collection /></el-icon>
          <div class="stats-content">
            <div class="stats-value">{{ learnedWordsCount }}</div>
            <div class="stats-label">学习词汇</div>
          </div>
        </div>
      </div>
    </div>


    <!-- 主体内容区 -->
    <div class="main-content">
      <!-- 左侧筛选面板 -->
      <div class="filter-panel" :class="{ 'filter-panel-collapsed': isFilterPanelCollapsed }">
        <div class="filter-panel-header">
          <h3>筛选</h3>
          <el-button type="text" @click="toggleFilterPanel">
            <el-icon>
              <component :is="isFilterPanelCollapsed ? 'ArrowRight' : 'ArrowLeft'" />
            </el-icon>
          </el-button>
        </div>
        
        <div class="filter-panel-content" v-show="!isFilterPanelCollapsed">
          <!-- 搜索框 -->
          <div class="filter-section">
            <div class="filter-header">
              <el-icon class="filter-icon"><Search /></el-icon>
              <h4>搜索文章</h4>
            </div>
            <el-input
              v-model="searchQuery"
              placeholder="输入关键词搜索..."
              prefix-icon="Search"
              clearable
              @input="handleSearch"
              class="search-input"
            />
          </div>
          
          <!-- 难度筛选 -->
          <div class="filter-section">
            <div class="filter-header">
              <el-icon class="filter-icon"><TrendCharts /></el-icon>
              <h4>难度等级</h4>
            </div>
            <div class="difficulty-tags">
              <div 
                class="difficulty-tag"
                :class="{ active: filters.difficulty === '' }"
                @click="filters.difficulty = ''; handleFilterChange()"
              >
                全部
              </div>
              <div 
                v-for="level in ['A1', 'A2', 'B1', 'B2', 'C1', 'C2']"
                :key="level"
                class="difficulty-tag"
                :class="{ 
                  active: filters.difficulty === level,
                  [`level-${level}`]: true
                }"
                @click="filters.difficulty = level; handleFilterChange()"
              >
                {{ level }}
              </div>
            </div>
          </div>
          
          <!-- 分类筛选 -->
          <div class="filter-section">
            <div class="filter-header">
              <el-icon class="filter-icon"><Collection /></el-icon>
              <h4>文章分类</h4>
            </div>
            <div class="category-tags">
              <div 
                class="category-tag"
                :class="{ active: filters.category === '' }"
                @click="filters.category = ''; handleFilterChange()"
              >
                全部
              </div>
              <div 
                v-for="option in getCategoryOptions()" 
                :key="option.value"
                class="category-tag"
                :class="{ active: filters.category === option.value }"
                @click="filters.category = option.value; handleFilterChange()"
              >
                {{ option.label }}
              </div>
            </div>
          </div>
          
          <!-- 排序方式 -->
          <div class="filter-section">
            <div class="filter-header">
              <el-icon class="filter-icon"><Sort /></el-icon>
              <h4>排序方式</h4>
            </div>
            <div class="sort-options">
              <div 
                class="sort-option"
                :class="{ active: sortBy === 'newest' }"
                @click="sortBy = 'newest'; handleSortChange()"
              >
                <el-icon><Clock /></el-icon>
                <span>最新</span>
              </div>
              <div 
                class="sort-option"
                :class="{ active: sortBy === 'popular' }"
                @click="sortBy = 'popular'; handleSortChange()"
              >
                <el-icon><Star /></el-icon>
                <span>热门</span>
              </div>
            </div>
          </div>

          <!-- 重置筛选按钮 -->
          <div class="filter-actions">
            <el-button 
              type="primary" 
              @click="resetFilters" 
              class="reset-button"
              :icon="Refresh"
            >
              重置筛选
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 右侧文章列表展示区 -->
      <div class="article-list-view">
        <!-- 视图切换 -->
        <div class="view-controls">
          <div class="left-space"></div>
          <div class="view-toggle">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button label="card">
                <el-icon><Grid /></el-icon>
              </el-radio-button>
              <el-radio-button label="list">
                <el-icon><Menu /></el-icon>
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>
        
        <!-- 文章列表 - 加载中 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :count="6" :loading="true" animated>
            <template #template>
              <div class="article-skeleton">
                <div class="skeleton-cover" />
                <div class="skeleton-title" />
                <div class="skeleton-desc" />
                <div class="skeleton-meta" />
              </div>
            </template>
          </el-skeleton>
        </div>
        
        <!-- 文章列表 - 无数据 -->
        <el-empty v-else-if="articles.length === 0" description="暂无符合条件的文章" />
        
        <!-- 文章列表 - 卡片视图 -->
        <div v-else-if="viewMode === 'card'" class="article-grid">
          <div v-for="article in articles" :key="article.id" class="article-card-wrapper">
            <article-card :article="article" @click="handleArticleClick(article.id)" />
          </div>
        </div>
        
        <!-- 文章列表 - 列表视图 -->
        <div v-else class="article-table">
          <el-table :data="articles" style="width: 100%">
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="scope">
                <div class="article-title-cell" @click="handleArticleClick(scope.row.id)">
                  <span class="article-title-text">{{ scope.row.title }}</span>
                  <el-tag v-if="scope.row.status === 'featured'" size="small" type="warning">精选</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="100">
              <template #default="scope">
                <el-tag size="small">{{ formatCategory(scope.row.category) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="difficulty" label="难度" width="80">
              <template #default="scope">
                <el-tag size="small" :type="getDifficultyTagType(scope.row.difficulty)">
                  {{ scope.row.difficulty }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="wordCount" label="词数" width="80" />
            <el-table-column prop="readTime" label="时长" width="80">
              <template #default="scope">
                <span>{{ scope.row.readTime || 0 }}分钟</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleArticleClick(scope.row.id)">
                  阅读
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 分页控件 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { articleApi, learningApi, vocabularyApi } from '@/utils/api'
import ArticleCard from '@/components/ArticleCard.vue'
import { 
  Clock, 
  Document, 
  Collection, 
  Grid, 
  Menu, 
  Search, 
  ArrowRight, 
  ArrowLeft,
  TrendCharts,
  Sort,
  Star,
  Refresh
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { debounce } from 'lodash-es'
import { getCategoryOptions } from '@/utils/categoryConfig'

const router = useRouter()
const userStore = useUserStore()

// 文章数据
const articles = ref<any[]>([])
const loading = ref(false)

// 用户统计数据
const readArticleCount = ref(0)
const learnedWordsCount = ref(0)
const totalReadingTime = ref(0)

// 格式化阅读时长
const formatReadingTime = computed(() => {
  const minutes = totalReadingTime.value
  if (minutes < 60) {
    return `${minutes}分钟`
  } else {
    const hours = Math.floor(minutes / 60)
    const remainingMinutes = minutes % 60
    return `${hours}小时${remainingMinutes > 0 ? remainingMinutes + '分钟' : ''}`
  }
})

// 视图模式
const viewMode = ref('card')

// 筛选面板折叠状态
const isFilterPanelCollapsed = ref(false)

// 排序方式
const sortBy = ref('newest')

// 搜索查询
const searchQuery = ref('')

// 筛选条件
const filters = reactive({
  difficulty: '',
  category: ''
})


// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 切换筛选面板
const toggleFilterPanel = () => {
  isFilterPanelCollapsed.value = !isFilterPanelCollapsed.value
}

// 处理筛选条件变更
const handleFilterChange = debounce(() => {
  pagination.currentPage = 1
  fetchArticles()
}, 300)

// 处理排序方式变更
const handleSortChange = () => {
  pagination.currentPage = 1
  fetchArticles()
}

// 处理搜索
const handleSearch = debounce(() => {
  pagination.currentPage = 1
  fetchArticles()
}, 500)

// 重置筛选条件
const resetFilters = () => {
  filters.difficulty = ''
  filters.category = ''
  sortBy.value = 'newest'
  searchQuery.value = ''
  
  pagination.currentPage = 1
  fetchArticles()
}

// 处理页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchArticles()
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchArticles()
}

// 处理文章点击
const handleArticleClick = (articleId: number | string) => {
  router.push(`/article/${articleId}`)
}

// 获取文章列表
const fetchArticles = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    
    // 添加筛选条件
    if (filters.difficulty) params.difficultyLevel = filters.difficulty
    if (filters.category) params.category = filters.category
    if (searchQuery.value) params.keyword = searchQuery.value
    
    // 添加排序方式
    if (sortBy.value === 'newest') params.sort = 'create_time,desc'
    else if (sortBy.value === 'popular') params.sort = 'read_count,desc'
    
    const res = await articleApi.getArticles(params)
    
    // 将difficultyLevel映射到difficulty属性，与ArticleReader.vue保持一致
    articles.value = (res.data.list || []).map((article: any) => ({
      ...article,
      difficulty: article.difficultyLevel || '',
      // 根据wordCount计算预计阅读时长，每分钟阅读200词
      readTime: article.wordCount ? Math.ceil(article.wordCount / 200) : 0
    }))
    
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('获取文章失败:', error)
    ElMessage.error('获取文章列表失败，请稍后重试')
    articles.value = []
  } finally {
    loading.value = false
  }
}

// 获取用户统计数据
const fetchUserStats = async () => {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) return
  
  try {
    const userId = userStore.userInfo.id.toString()
    
    // 尝试从词汇统计API获取数据（参考ReportPage.vue中的方式）
    let vocabularyStatsRes
    try {
      vocabularyStatsRes = await vocabularyApi.getVocabularyStats(userId)
      if (vocabularyStatsRes?.data) {
        // 使用词汇统计API中的数据作为生词量
        learnedWordsCount.value = vocabularyStatsRes.data.totalWords || 0
      }
    } catch (vocabError) {
      console.warn('获取词汇统计数据失败，使用备选方案:', vocabError)
    }
    
    // 尝试从学习摘要API获取数据（参考ReportPage.vue中的方式）
    let readingTimeRes
    try {
      readingTimeRes = await learningApi.getReadingTimeStats(Number(userStore.userInfo.id))
      if (readingTimeRes?.data) {
        totalReadingTime.value = readingTimeRes.data.totalMinutes || 0
        // 使用阅读时长API中的完成文章数
        readArticleCount.value = readingTimeRes.data.totalArticles || 0
      }
    } catch (readingError) {
      console.warn('获取阅读时长数据失败，使用备选方案:', readingError)
    }
    
    // 如果前面的API调用失败，尝试使用todaySummary作为备选
    if ((!learnedWordsCount.value && !readArticleCount.value)) {
      try {
        const summaryRes = await learningApi.getTodaySummary(userId)
        if (summaryRes?.data) {
          if (!learnedWordsCount.value) {
            learnedWordsCount.value = summaryRes.data.totalWordsLearned || 0
          }
          if (!readArticleCount.value) {
            readArticleCount.value = summaryRes.data.totalArticlesRead || 0
          }
        }
      } catch (summaryError) {
        console.warn('获取今日摘要数据失败:', summaryError)
      }
    }
  } catch (error) {
    console.error('获取用户统计数据失败:', error)
  }
}

// 格式化分类名称
const formatCategory = (category: string) => {
  const categoryMap: Record<string, string> = {
    'tech': '科技',
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
  
  return categoryMap[category] || category || '未分类'
}

// 获取难度标签类型
const getDifficultyTagType = (difficulty: string) => {
  const typeMap: Record<string, string> = {
    'A1': 'success',
    'A2': 'success',
    'B1': 'warning',
    'B2': 'warning',
    'C1': 'danger',
    'C2': 'danger'
  }
  
  return typeMap[difficulty] || 'info'
}

onMounted(() => {
  fetchArticles()
  fetchUserStats()
})
</script>

<style scoped>
@import '@/assets/design-system.css';

.article-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6);
  animation: fadeInUp 0.8s ease-out;
  background: var(--bg-secondary);
  border-radius: var(--radius-2xl);
  position: relative;
  min-height: 100vh;
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

/* 页面标题区域 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-8);
  padding: var(--space-8) var(--space-6);
  background: var(--bg-primary);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-3xl);
  position: relative;
  overflow: hidden;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all var(--transition-normal);
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.03) 0%, rgba(90, 200, 250, 0.02) 50%, rgba(0, 122, 255, 0.03) 100%);
  pointer-events: none;
  animation: liquidFlow 25s ease-in-out infinite;
}

.page-header:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 12px 40px rgba(0, 0, 0, 0.15),
    0 4px 12px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
  border-color: rgba(0, 122, 255, 0.2);
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

.header-content {
  flex: 1;
}

.page-title {
  font-size: var(--text-4xl);
  font-weight: var(--font-weight-bold);
  margin: 0 0 var(--space-2) 0;
  color: var(--text-primary);
}

.page-description {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  margin: 0;
}

/* 用户统计卡片 */
.stats-card {
  display: flex;
  gap: var(--space-6);
  background: var(--bg-primary);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: var(--radius-2xl);
  padding: var(--space-6) var(--space-8);
  border: 2px solid rgba(255, 255, 255, 0.4);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.08),
    0 1px 4px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.7),
    inset 0 -1px 0 rgba(0, 0, 0, 0.03);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.stats-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left var(--transition-slow);
}

.stats-card:hover::before {
  left: 100%;
}

.stats-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 
    0 12px 48px rgba(0, 0, 0, 0.18),
    0 4px 16px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    inset 0 -1px 0 rgba(0, 0, 0, 0.05);
  border-color: rgba(0, 122, 255, 0.3);
}

.stats-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.stats-item .el-icon {
  font-size: 24px;
  color: var(--ios-blue);
}

.stats-content {
  display: flex;
  flex-direction: column;
}

.stats-value {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.stats-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}


/* 主体内容区 */
.main-content {
  display: flex;
  gap: var(--space-8);
  margin-bottom: var(--space-8);
  padding: var(--space-6);
  background: var(--bg-primary);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-2xl);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.main-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.01) 0%, rgba(90, 200, 250, 0.005) 50%, rgba(0, 122, 255, 0.01) 100%);
  pointer-events: none;
  animation: liquidFlow 35s ease-in-out infinite;
}

.main-content:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 12px 40px rgba(0, 0, 0, 0.15),
    0 4px 12px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
  border-color: rgba(0, 122, 255, 0.2);
}

/* 左侧筛选面板 */
.filter-panel {
  width: 280px;
  padding: var(--space-6);
  background: var(--bg-primary);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.08),
    0 1px 4px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.filter-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.01) 0%, rgba(90, 200, 250, 0.005) 50%, rgba(0, 122, 255, 0.01) 100%);
  pointer-events: none;
  animation: liquidFlow 40s ease-in-out infinite;
}

.filter-panel-collapsed {
  width: 60px;
  padding: var(--space-4);
}

.filter-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  position: relative;
  z-index: 2;
}

.filter-panel-header h3 {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.filter-section {
  margin-bottom: var(--space-6);
  position: relative;
  z-index: 2;
}

.filter-section h4 {
  font-size: var(--text-sm);
  margin: 0 0 var(--space-4) 0;
  color: var(--text-primary);
  font-weight: var(--font-weight-semibold);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  position: relative;
}

.filter-section h4::after {
  content: '';
  position: absolute;
  bottom: -var(--space-2);
  left: 0;
  width: 30px;
  height: 2px;
  background: linear-gradient(90deg, var(--ios-blue), rgba(0, 122, 255, 0.3));
  border-radius: var(--radius-sm);
}

/* 筛选面板重新设计样式 */

/* 筛选区域标题 */
.filter-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-2);
  border-bottom: 1px solid rgba(0, 122, 255, 0.1);
}

.filter-icon {
  color: var(--ios-blue);
  font-size: 16px;
}

.filter-header h4 {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

/* 搜索框样式 */
.search-input {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-lg);
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(0, 122, 255, 0.2);
  transition: all var(--transition-normal);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(0, 122, 255, 0.4);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--ios-blue);
  box-shadow: 
    0 4px 12px rgba(0, 122, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

/* 难度标签样式 */
.difficulty-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.difficulty-tag {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-lg);
  font-size: var(--text-xs);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 2px solid transparent;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  color: var(--text-secondary);
  text-align: center;
  min-width: 40px;
  position: relative;
  overflow: hidden;
}

.difficulty-tag::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left var(--transition-slow);
}

.difficulty-tag:hover::before {
  left: 100%;
}

.difficulty-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.difficulty-tag.active {
  background: linear-gradient(135deg, var(--ios-blue) 0%, #5AC8FA 100%);
  color: var(--text-inverse);
  border-color: var(--ios-blue);
  box-shadow: 
    0 4px 12px rgba(0, 122, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

/* 难度等级颜色 */
.difficulty-tag.level-A1,
.difficulty-tag.level-A2 {
  border-color: #67c23a;
}

.difficulty-tag.level-A1.active,
.difficulty-tag.level-A2.active {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border-color: #67c23a;
}

.difficulty-tag.level-B1,
.difficulty-tag.level-B2 {
  border-color: #e6a23c;
}

.difficulty-tag.level-B1.active,
.difficulty-tag.level-B2.active {
  background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
  border-color: #e6a23c;
}

.difficulty-tag.level-C1,
.difficulty-tag.level-C2 {
  border-color: #f56c6c;
}

.difficulty-tag.level-C1.active,
.difficulty-tag.level-C2.active {
  background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);
  border-color: #f56c6c;
}

/* 分类标签样式 */
.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.category-tag {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-3xl);
  font-size: var(--text-xs);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 2px solid rgba(0, 122, 255, 0.2);
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  color: var(--text-secondary);
  text-align: center;
  position: relative;
  overflow: hidden;
}

.category-tag::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left var(--transition-slow);
}

.category-tag:hover::before {
  left: 100%;
}

.category-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(0, 122, 255, 0.4);
}

.category-tag.active {
  background: linear-gradient(135deg, var(--ios-blue) 0%, #5AC8FA 100%);
  color: var(--text-inverse);
  border-color: var(--ios-blue);
  box-shadow: 
    0 4px 12px rgba(0, 122, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

/* 排序选项样式 */
.sort-options {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.sort-option {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 2px solid rgba(0, 122, 255, 0.1);
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  color: var(--text-secondary);
  position: relative;
  overflow: hidden;
}

.sort-option::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left var(--transition-slow);
}

.sort-option:hover::before {
  left: 100%;
}

.sort-option:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(0, 122, 255, 0.3);
}

.sort-option.active {
  background: linear-gradient(135deg, var(--ios-blue) 0%, #5AC8FA 100%);
  color: var(--text-inverse);
  border-color: var(--ios-blue);
  box-shadow: 
    0 4px 12px rgba(0, 122, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.sort-option .el-icon {
  font-size: 16px;
}

/* 筛选操作按钮 */
.filter-actions {
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid rgba(0, 122, 255, 0.1);
}

.reset-button {
  width: 100%;
  background: linear-gradient(135deg, var(--ios-blue) 0%, #5AC8FA 100%);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-lg);
  padding: var(--space-3) var(--space-4);
  font-weight: var(--font-weight-medium);
  box-shadow: 
    0 4px 12px rgba(0, 122, 255, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.reset-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left var(--transition-slow);
}

.reset-button:hover::before {
  left: 100%;
}

.reset-button:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 6px 16px rgba(0, 122, 255, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.range-labels {
  display: flex;
  justify-content: space-between;
  margin-top: var(--space-2);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.reset-button {
  width: 100%;
  margin-top: var(--space-6);
  background: linear-gradient(135deg, var(--ios-blue) 0%, #5AC8FA 100%);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-lg);
  padding: var(--space-3) var(--space-4);
  font-weight: var(--font-weight-medium);
  box-shadow: 
    0 4px 12px rgba(0, 122, 255, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.reset-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left var(--transition-slow);
}

.reset-button:hover::before {
  left: 100%;
}

.reset-button:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 6px 16px rgba(0, 122, 255, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}


/* 右侧文章列表 */
.article-list-view {
  flex: 1;
  padding: var(--space-4);
  margin-bottom: 0;
  background: var(--bg-primary);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.06),
    0 1px 4px rgba(0, 0, 0, 0.03),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  position: relative;
  overflow: hidden;
}

.article-list-view::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.005) 0%, rgba(90, 200, 250, 0.002) 50%, rgba(0, 122, 255, 0.005) 100%);
  pointer-events: none;
  animation: liquidFlow 45s ease-in-out infinite;
}

/* 视图控制 */
.view-controls {
  display: flex;
  justify-content: flex-end;
  margin-bottom: var(--space-6);
  position: relative;
  z-index: 2;
}



/* 文章网格视图 */
.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-5);
  margin-bottom: var(--space-6);
}

.article-card-wrapper {
  height: 100%;
}

/* 文章列表视图 */
.article-table {
  margin-bottom: var(--space-6);
}

.article-title-cell {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
}

.article-title-text {
  color: var(--ios-blue);
  transition: color var(--transition-normal);
}

.article-title-text:hover {
  text-decoration: underline;
  color: var(--primary-600);
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: var(--space-8);
  position: relative;
  z-index: 2;
}

/* 加载状态 */
.loading-container {
  padding: var(--space-5) 0;
}

.article-skeleton {
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  background: var(--glass-white);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--glass-border);
  margin-bottom: var(--space-4);
}

.skeleton-cover {
  height: 160px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-3);
}

.skeleton-title {
  height: 24px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-3);
  width: 80%;
}

.skeleton-desc {
  height: 16px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-2);
  width: 100%;
}

.skeleton-meta {
  height: 16px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  width: 60%;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-4);
  }
  
  .stats-card {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .main-content {
    flex-direction: column;
  }
  
  .filter-panel {
    width: 100%;
    margin-bottom: var(--space-4);
  }
  
  .filter-panel-collapsed {
    width: 100%;
  }
  
  .search-box {
    width: 100%;
  }
  
  .view-controls {
    flex-direction: column;
    gap: var(--space-3);
  }
}
</style>
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

    <!-- 快捷筛选标签 -->
    <div class="quick-filter-tags">
      <div class="filter-group">
        <span class="filter-label">难度:</span>
        <el-radio-group v-model="filters.difficulty" size="small" @change="handleFilterChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="A1">A1</el-radio-button>
          <el-radio-button label="A2">A2</el-radio-button>
          <el-radio-button label="B1">B1</el-radio-button>
          <el-radio-button label="B2">B2</el-radio-button>
          <el-radio-button label="C1">C1</el-radio-button>
          <el-radio-button label="C2">C2</el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="filter-group">
        <span class="filter-label">分类:</span>
        <el-radio-group v-model="filters.category" size="small" @change="handleFilterChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="tech">科技</el-radio-button>
          <el-radio-button label="business">商业</el-radio-button>
          <el-radio-button label="culture">文化</el-radio-button>
          <el-radio-button label="education">教育</el-radio-button>
          <el-radio-button label="health">健康</el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="filter-group">
        <span class="filter-label">排序:</span>
        <el-radio-group v-model="sortBy" size="small" @change="handleSortChange">
          <el-radio-button label="newest">最新</el-radio-button>
          <el-radio-button label="popular">热门</el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索文章标题或内容"
          prefix-icon="Search"
          clearable
          @input="handleSearch"
          size="small"
        />
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="main-content">
      <!-- 左侧高级筛选面板 -->
      <div class="filter-panel" :class="{ 'filter-panel-collapsed': isFilterPanelCollapsed }">
        <div class="filter-panel-header">
          <h3>高级筛选</h3>
          <el-button type="text" @click="toggleFilterPanel">
            <el-icon>
              <component :is="isFilterPanelCollapsed ? 'ArrowRight' : 'ArrowLeft'" />
            </el-icon>
          </el-button>
        </div>
        
        <div class="filter-panel-content" v-show="!isFilterPanelCollapsed">
          <!-- 阅读状态筛选 -->
          <div class="filter-section">
            <h4>阅读状态</h4>
            <el-checkbox-group v-model="filters.readStatus" @change="handleFilterChange">
              <el-checkbox label="read">已读</el-checkbox>
              <el-checkbox label="unread">未读</el-checkbox>
            </el-checkbox-group>
          </div>
          
          <!-- 文章状态筛选 -->
          <div class="filter-section">
            <h4>文章状态</h4>
            <el-checkbox-group v-model="filters.articleStatus" @change="handleFilterChange">
              <el-checkbox label="normal">普通</el-checkbox>
              <el-checkbox label="featured">精选</el-checkbox>
            </el-checkbox-group>
          </div>
          
          <!-- 词汇量范围筛选 -->
          <div class="filter-section">
            <h4>词汇量范围</h4>
            <el-slider
              v-model="filters.wordCountRange"
              range
              :min="0"
              :max="2000"
              :step="100"
              @change="handleFilterChange"
            />
            <div class="range-labels">
              <span>{{ filters.wordCountRange[0] }}</span>
              <span>{{ filters.wordCountRange[1] }}</span>
            </div>
          </div>
          
          <!-- 阅读时长筛选 -->
          <div class="filter-section">
            <h4>预计阅读时长</h4>
            <el-slider
              v-model="filters.readTimeRange"
              range
              :min="0"
              :max="30"
              :step="5"
              @change="handleFilterChange"
            />
            <div class="range-labels">
              <span>{{ filters.readTimeRange[0] }}分钟</span>
              <span>{{ filters.readTimeRange[1] }}分钟</span>
            </div>
          </div>
          
          <!-- 重置筛选按钮 -->
          <el-button type="primary" @click="resetFilters" class="reset-button">重置筛选</el-button>
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
  ArrowLeft 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { debounce } from 'lodash-es'

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
  category: '',
  readStatus: [] as string[],
  articleStatus: [] as string[],
  wordCountRange: [0, 2000],
  readTimeRange: [0, 30]
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
  filters.readStatus = []
  filters.articleStatus = []
  filters.wordCountRange = [0, 2000]
  filters.readTimeRange = [0, 30]
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
    
    // 添加词汇量范围筛选
    params.minWordCount = filters.wordCountRange[0]
    params.maxWordCount = filters.wordCountRange[1]
    
    // 添加阅读时长筛选
    params.minReadTime = filters.readTimeRange[0]
    params.maxReadTime = filters.readTimeRange[1]
    
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
.article-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 页面标题区域 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #303133;
}

.page-description {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

/* 用户统计卡片 */
.stats-card {
  display: flex;
  gap: 24px;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 24px;
}

.stats-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stats-item .el-icon {
  font-size: 24px;
  color: #409eff;
}

.stats-content {
  display: flex;
  flex-direction: column;
}

.stats-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.stats-label {
  font-size: 12px;
  color: #909399;
}

/* 快捷筛选标签 */
.quick-filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 主体内容区 */
.main-content {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

/* 左侧筛选面板 */
.filter-panel {
  width: 260px;
  padding: 0;
  transition: all 0.3s ease;
}

.filter-panel-collapsed {
  width: 60px;
}

.filter-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.filter-panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-section h4 {
  font-size: 14px;
  margin: 0 0 12px 0;
  color: #606266;
}

.range-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.reset-button {
  width: 100%;
  margin-top: 16px;
}

/* 右侧文章列表 */
.article-list-view {
  flex: 1;
  padding: 0;
  margin-bottom: 0;
}

/* 视图控制 */
.view-controls {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.search-box {
  width: 250px; /* 稍微缩小宽度确保在一行显示 */
  flex-shrink: 1; /* 允许在空间不足时缩小 */
  min-width: 180px; /* 设置最小宽度 */
}

/* 确保快捷筛选标签区域内的元素不会换行 */
.quick-filter-tags {
  display: flex;
  flex-wrap: nowrap; /* 禁止换行 */
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow-x: auto; /* 在空间不足时显示水平滚动条 */
}

/* 文章网格视图 */
.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.article-card-wrapper {
  height: 100%;
}

/* 文章列表视图 */
.article-table {
  margin-bottom: 24px;
}

.article-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.article-title-text {
  color: #409eff;
}

.article-title-text:hover {
  text-decoration: underline;
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 加载状态 */
.loading-container {
  padding: 20px 0;
}

.article-skeleton {
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
  margin-bottom: 16px;
}

.skeleton-cover {
  height: 160px;
  background: #e4e7ed;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-title {
  height: 24px;
  background: #e4e7ed;
  border-radius: 4px;
  margin-bottom: 12px;
  width: 80%;
}

.skeleton-desc {
  height: 16px;
  background: #e4e7ed;
  border-radius: 4px;
  margin-bottom: 8px;
  width: 100%;
}

.skeleton-meta {
  height: 16px;
  background: #e4e7ed;
  border-radius: 4px;
  width: 60%;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
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
    margin-bottom: 16px;
  }
  
  .filter-panel-collapsed {
    width: 100%;
  }
  
  .search-box {
    width: 100%;
  }
  
  .view-controls {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
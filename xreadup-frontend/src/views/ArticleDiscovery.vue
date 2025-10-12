<template>
  <div class="article-discovery-container">
    <!-- 顶部导航栏 -->
    <Header />
    
    <main class="discovery-content">
      <div class="container">
        <!-- 页面标题和简介 -->
        <div class="page-header">
          <h1>📚 文章发现中心</h1>
          <p>探索最新热点和感兴趣的主题，扩展您的阅读视野</p>
          
          <!-- 订阅状态和使用额度显示 -->
          <div class="subscription-info" v-if="userStore.isLoggedIn">
            <div class="quota-badge">
              <span class="quota-text">
                本月剩余: {{ remainingArticles }} 篇文章
              </span>
              <span class="plan-name">当前方案: {{ subscriptionPlan || '免费' }}</span>
            </div>
            <el-button 
              type="primary" 
              size="small" 
              v-if="!hasActiveSubscription"
              @click="handleUpgradeSubscription"
              class="upgrade-button"
            >
              升级订阅
            </el-button>
          </div>
        </div>
        
        <!-- 文章发现工具区域 -->
        <div class="discovery-tools">
          <!-- 热点文章选项卡 -->
          <div class="tab-container">
            <el-tabs v-model="activeTab" @tab-change="handleTabChange" type="card">
              <el-tab-pane label="🔥 热点文章" name="trending">
                <div class="article-list-container">
                  <!-- 文章过滤和排序 -->
                  <div class="filter-section">
                    <div class="sort-options">
                      <span class="filter-label">排序方式:</span>
                      <el-select v-model="trendingSortBy" placeholder="选择排序" @change="loadTrendingArticles">
                        <el-option label="热度优先" value="popularity" />
                        <el-option label="最新发布" value="latest" />
                      </el-select>
                    </div>
                    <div class="article-count">
                      <el-select v-model="articleCount" placeholder="显示数量" @change="loadTrendingArticles">
                        <el-option label="10篇" value="10" />
                        <el-option label="20篇" value="20" />
                        <el-option label="50篇" value="50" />
                      </el-select>
                    </div>
                  </div>
                  
                  <!-- 热点文章列表 -->
                  <div class="article-list" v-if="!isLoadingTrending && trendingArticles.length > 0">
                    <el-card 
                      v-for="article in trendingArticles" 
                      :key="article.url"
                      class="article-card hover-lift"
                      :body-style="{ padding: '16px' }"
                    >
                      <div class="article-content-wrapper">
                        <div v-if="article.image" class="article-image">
                          <img :src="article.image" :alt="article.title" class="card-image">
                        </div>
                        <div class="article-content">
                          <h3 class="article-title">
                            <router-link :to="`/article/${article.id || '1'}`" class="title-link">
                              {{ article.title }}
                            </router-link>
                          </h3>
                          <div class="article-meta">
                            <span class="source">{{ article.source }}</span>
                            <span class="date">{{ formatDate(article.publishedAt) }}</span>
                            <span v-if="article.popularity" class="popularity">热度: {{ article.popularity }}%</span>
                            <span v-if="article.category" class="category">分类: {{ article.category }}</span>
                          </div>
                          <p class="article-desc" v-if="article.description">
                            {{ truncateText(article.description, 120) }}
                          </p>
                          <div class="article-actions">
                            <el-button 
                              type="primary" 
                              size="small"
                              :to="`/article/${article.id || '1'}`"
                              :disabled="!canAccessArticle"
                              @click="recordArticleAccess"
                            >
                              开始阅读
                            </el-button>
                          </div>
                        </div>
                      </div>
                    </el-card>
                  </div>
                  
                  <!-- 加载状态 -->
                  <div v-else-if="isLoadingTrending" class="loading-container">
                    <el-empty description="加载中...">
                      <template #image>
                        <el-icon><Loading /></el-icon>
                      </template>
                    </el-empty>
                  </div>
                  
                  <!-- 空状态 -->
                  <div v-else class="empty-container">
                    <el-empty description="暂无热点文章数据">
                      <template #image>
                        <el-icon><Document /></el-icon>
                      </template>
                    </el-empty>
                  </div>
                </div>
              </el-tab-pane>
              
              <!-- 主题文章选项卡 -->
              <el-tab-pane label="🎯 主题探索" name="categories">
                <div class="categories-container">
                  <!-- 主题选择 -->
                  <div class="category-selection">
                    <el-tag 
                      v-for="category in availableCategories" 
                      :key="category.value"
                      :type="selectedCategory === category.value ? 'primary' : ''"
                      closable 
                      :disable-transitions="false"
                      @click="selectCategory(category.value)"
                      @close="handleCategoryClose($event, category.value)"
                      class="category-tag clickable-tag"
                    >
                      {{ category.label }}
                    </el-tag>
                  </div>
                  
                  <!-- 主题文章列表 -->
                  <div class="article-list" v-if="!isLoadingCategory && categoryArticles.length > 0">
                    <el-card 
                      v-for="article in categoryArticles" 
                      :key="article.url"
                      class="article-card hover-lift"
                      :body-style="{ padding: '16px' }"
                    >
                      <div class="article-content-wrapper">
                        <div v-if="article.image" class="article-image">
                          <img :src="article.image" :alt="article.title" class="card-image">
                        </div>
                        <div class="article-content">
                          <h3 class="article-title">
                            <router-link :to="`/article/${article.id || '1'}`" class="title-link">
                              {{ article.title }}
                            </router-link>
                          </h3>
                          <div class="article-meta">
                            <span class="source">{{ article.source }}</span>
                            <span class="date">{{ formatDate(article.publishedAt) }}</span>
                            <span v-if="article.category" class="category">分类: {{ article.category }}</span>
                          </div>
                          <p class="article-desc" v-if="article.description">
                            {{ truncateText(article.description, 120) }}
                          </p>
                          <div class="article-actions">
                            <el-button 
                              type="primary" 
                              size="small"
                              :to="`/article/${article.id || '1'}`"
                              :disabled="!canAccessArticle"
                              @click="recordArticleAccess"
                            >
                              开始阅读
                            </el-button>
                          </div>
                        </div>
                      </div>
                    </el-card>
                  </div>
                  
                  <!-- 加载状态 -->
                  <div v-else-if="isLoadingCategory" class="loading-container">
                    <el-empty description="加载中...">
                      <template #image>
                        <el-icon><Loading /></el-icon>
                      </template>
                    </el-empty>
                  </div>
                  
                  <!-- 空状态或未选择主题 -->
                  <div v-else class="empty-container">
                    <el-empty :description="selectedCategory ? '暂无该主题文章数据' : '请选择感兴趣的主题'">
                      <template #image>
                        <el-icon><Document /></el-icon>
                      </template>
                    </el-empty>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
      </div>
    </main>
    
    <!-- 页脚 -->
    <Footer />
    
    <!-- 订阅升级提示对话框 -->
    <el-dialog v-model="showUpgradeDialog" title="升级您的订阅" width="500px" center>
      <div class="upgrade-content">
        <p>您当前的订阅方案限制了文章访问数量。升级到高级方案以获取更多优质内容！</p>
        <div class="plan-comparison">
          <div class="plan-feature">
            <span>免费方案:</span>
            <span>每月仅限 5 篇文章</span>
          </div>
          <div class="plan-feature">
            <span>BASIC ($9.99/月):</span>
            <span>每月 10 篇文章，每篇最多 1000 词</span>
          </div>
          <div class="plan-feature">
            <span>PRO ($19.99/月):</span>
            <span>每月 50 篇文章，每篇最多 5000 词</span>
          </div>
          <div class="plan-feature">
            <span>ENTERPRISE ($49.99/月):</span>
            <span>每月 200 篇文章，每篇最多 20000 词</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showUpgradeDialog = false">取消</el-button>
          <el-button type="primary" @click="goToSubscriptionPage">立即升级</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { articleApi, subscriptionApi } from '@/utils/api'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import { Document, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 标签页状态
const activeTab = ref('trending')
const articleCount = ref('10')
const trendingSortBy = ref('popularity')

// 主题相关状态
const availableCategories = ref([
  { label: '科技', value: 'technology' },
  { label: '商业', value: 'business' },
  { label: '健康', value: 'health' },
  { label: '科学', value: 'science' },
  { label: '体育', value: 'sports' },
  { label: '娱乐', value: 'entertainment' },
  { label: '世界', value: 'world' }
])
const selectedCategory = ref('')

// 文章数据状态
const trendingArticles = ref<any[]>([])
const categoryArticles = ref<any[]>([])
let isLoadingTrending = false
let isLoadingCategory = false

// 订阅相关状态
const subscriptionInfo = ref<any>(null)
const remainingArticles = ref(0)
const subscriptionPlan = ref('')
const hasActiveSubscription = ref(false)
const showUpgradeDialog = ref(false)
const canAccessArticle = ref(true)

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }).format(date)
}

// 截断文本
const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 加载热点文章
const loadTrendingArticles = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录以获取热点文章')
    return
  }

  // 检查订阅权限
  if (!await checkSubscriptionAccess()) {
    showUpgradeDialog.value = true
    return
  }

  isLoadingTrending = true
  try {
    const limit = parseInt(articleCount.value, 10) || 10
    const response = await articleApi.getTrendingArticles(limit)
    
    if (response?.data?.trending) {
      trendingArticles.value = response.data.trending
      // 按热度或日期排序
      if (trendingSortBy.value === 'latest') {
        trendingArticles.value.sort((a, b) => 
          new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime()
        )
      } else {
        trendingArticles.value.sort((a, b) => (b.popularity || 0) - (a.popularity || 0))
      }
    } else {
      trendingArticles.value = []
    }
  } catch (error) {
    console.error('获取热点文章失败:', error)
    ElMessage.error('获取热点文章失败，请稍后再试')
    trendingArticles.value = []
  } finally {
    isLoadingTrending = false
  }
}

// 加载主题文章
const loadCategoryArticles = async (category: string) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录以获取主题文章')
    return
  }

  // 检查订阅权限
  if (!await checkSubscriptionAccess()) {
    showUpgradeDialog.value = true
    return
  }

  isLoadingCategory = true
  try {
    const limit = parseInt(articleCount.value, 10) || 10
    const response = await articleApi.getArticlesByCategory(category, limit)
    
    if (response?.data?.articles) {
      categoryArticles.value = response.data.articles
      // 按日期排序
      categoryArticles.value.sort((a, b) => 
        new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime()
      )
    } else {
      categoryArticles.value = []
    }
  } catch (error) {
    console.error('获取主题文章失败:', error)
    ElMessage.error('获取主题文章失败，请稍后再试')
    categoryArticles.value = []
  } finally {
    isLoadingCategory = false
  }
}

// 选择主题
const selectCategory = (category: string) => {
  if (selectedCategory.value === category) {
    // 已经选择了该主题，再次点击取消选择
    selectedCategory.value = ''
    categoryArticles.value = []
  } else {
    selectedCategory.value = category
    loadCategoryArticles(category)
  }
}

// 处理主题标签关闭
const handleCategoryClose = (e: Event, category: string) => {
  e.stopPropagation()
  if (selectedCategory.value === category) {
    selectedCategory.value = ''
    categoryArticles.value = []
  }
}

// 处理标签页切换
const handleTabChange = (tabName: string) => {
  if (tabName === 'trending' && trendingArticles.value.length === 0) {
    loadTrendingArticles()
  }
}

// 检查订阅访问权限
const checkSubscriptionAccess = async (): Promise<boolean> => {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    return false
  }

  try {
    const response = await subscriptionApi.getRemainingQuota(userStore.userInfo.id)
    if (response?.data?.articlesQuota) {
      const quota = response.data.articlesQuota
      remainingArticles.value = quota.remaining || 0
      hasActiveSubscription.value = remainingArticles.value > 0 || quota.total > 0
      
      // 如果剩余文章数量为0，禁止访问
      if (remainingArticles.value <= 0) {
        canAccessArticle.value = false
        return false
      }
      
      canAccessArticle.value = true
      return true
    }
  } catch (error) {
    console.warn('检查订阅权限失败:', error)
  }
  
  // 默认允许访问（可能是免费用户）
  canAccessArticle.value = true
  return true
}

// 记录文章访问
const recordArticleAccess = async () => {
  // 这里可以记录用户的文章访问情况
  // 在实际实现中，应该调用后端API来更新用户的文章使用计数
  console.log('用户访问了文章')
}

// 处理升级订阅
const handleUpgradeSubscription = () => {
  showUpgradeDialog.value = true
}

// 前往订阅页面
const goToSubscriptionPage = () => {
  showUpgradeDialog.value = false
  router.push('/subscription')
}

// 监听用户登录状态变化
watch(() => userStore.isLoggedIn, async (newValue) => {
  if (newValue) {
    await checkSubscriptionAccess()
    if (activeTab.value === 'trending' && trendingArticles.value.length === 0) {
      loadTrendingArticles()
    }
  } else {
    trendingArticles.value = []
    categoryArticles.value = []
    selectedCategory.value = ''
  }
})

// 组件挂载时
onMounted(async () => {
  if (userStore.isLoggedIn) {
    await checkSubscriptionAccess()
    if (activeTab.value === 'trending') {
      loadTrendingArticles()
    }
  }
})
</script>

<style scoped lang="scss">
.article-discovery-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.discovery-content {
  flex: 1;
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  color: white;
}

.page-header h1 {
  font-size: 2.5rem;
  margin-bottom: 10px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.page-header p {
  font-size: 1.1rem;
  opacity: 0.9;
  margin-bottom: 20px;
}

.subscription-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.quota-badge {
  background: rgba(255, 255, 255, 0.2);
  padding: 10px 20px;
  border-radius: 30px;
  display: flex;
  align-items: center;
  gap: 15px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.quota-text {
  font-weight: 500;
}

.plan-name {
  font-size: 0.9rem;
  opacity: 0.9;
}

.upgrade-button {
  background: white;
  color: #667eea;
  border: none;
  transition: all 0.3s ease;
}

.upgrade-button:hover {
  background: #f0f0f0;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.discovery-tools {
  background: white;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
}

.tab-container {
  width: 100%;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-weight: 500;
  color: #333;
}

.article-count {
  flex-shrink: 0;
}

.category-selection {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.category-tag {
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-card {
  transition: all 0.3s ease;
  border: none;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
}

.article-content-wrapper {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.article-image {
  flex-shrink: 0;
  width: 200px;
  height: 140px;
  overflow: hidden;
  border-radius: 8px;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-card:hover .card-image {
  transform: scale(1.05);
}

.article-content {
  flex: 1;
  min-width: 0;
}

.article-title {
  font-size: 1.2rem;
  margin-bottom: 10px;
  line-height: 1.4;
}

.title-link {
  color: #333;
  text-decoration: none;
  transition: color 0.3s ease;
}

.title-link:hover {
  color: #667eea;
}

.article-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
  font-size: 0.9rem;
  color: #666;
  flex-wrap: wrap;
}

.article-desc {
  color: #555;
  line-height: 1.6;
  margin-bottom: 15px;
}

.article-actions {
  margin-top: auto;
}

.loading-container,
.empty-container {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.upgrade-content {
  text-align: left;
}

.upgrade-content p {
  margin-bottom: 20px;
  color: #333;
}

.plan-comparison {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan-feature {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.plan-feature span:first-child {
  font-weight: 500;
  color: #333;
}

.plan-feature span:last-child {
  color: #666;
  font-size: 0.95rem;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header h1 {
    font-size: 2rem;
  }
  
  .article-content-wrapper {
    flex-direction: column;
  }
  
  .article-image {
    width: 100%;
    height: 180px;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .subscription-info {
    flex-direction: column;
  }
  
  .quota-badge {
    flex-direction: column;
    text-align: center;
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.article-card {
  animation: fadeIn 0.5s ease-out;
}

/* 提升用户体验的微交互 */
.el-select .el-input__wrapper:hover {
  border-color: #667eea;
}

.el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

/* 滚动条样式优化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
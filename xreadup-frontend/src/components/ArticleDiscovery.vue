<template>
  <div class="article-discovery">
    <!-- 标题栏 -->
    <div class="discovery-header">
      <h2 class="section-title">探索文章</h2>
      <p class="section-description">发现更多适合你的优质内容</p>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-buttons">
      <!-- 热点文章按钮 -->
      <TactileButton
        :variant="isPremiumUser ? 'primary' : 'secondary'"
        :loading="isLoadingTrending"
        :disabled="!canFetchTrending || isLoadingTrending"
        @click="fetchTrendingArticles"
        class="discovery-button"
        size="lg"
      >
        <template #icon>
          <TrendChartsIcon />
        </template>
        获取热点文章
      </TactileButton>

      <!-- 主题文章选择器和按钮 -->
      <div class="category-selector">
        <el-select
          v-model="selectedCategory"
          placeholder="选择文章主题"
          size="large"
          style="width: 180px; margin-right: 10px;"
          :disabled="!canFetchCategory || isLoadingCategory"
          :tooltip="!canFetchCategory ? '升级基础会员解锁此功能' : ''"
        >
          <el-option label="科技" value="technology" />
          <el-option label="健康" value="health" />
          <el-option label="商业" value="business" />
          <el-option label="教育" value="education" />
          <el-option label="娱乐" value="entertainment" />
          <el-option label="体育" value="sports" />
          <el-option label="旅行" value="travel" />
          <el-option label="美食" value="food" />
        </el-select>
        <TactileButton
          :variant="userStore.userTier === 'basic' ? 'primary' : (isPremiumUser ? 'primary' : 'secondary')"
          :loading="isLoadingCategory"
          :disabled="!selectedCategory || !canFetchCategory || isLoadingCategory"
          @click="fetchCategoryArticles"
          class="discovery-button"
          size="lg"
        >
          <template #icon>
          <MagicStickIcon />
        </template>
          获取主题文章
          <!-- 为所有用户显示基础会员标签 -->
          <span class="basic-badge">基础会员</span>
        </TactileButton>
        <!-- 为非基础会员显示提示 -->
        <el-tooltip
          v-if="userStore.userTier === 'free'"
          effect="dark"
          content="升级到基础会员解锁固定主题探索功能"
          placement="top"
        >
          <div class="pro-feature-tag">基础会员功能</div>
        </el-tooltip>
        <!-- 基础会员的剩余次数已在下方的配额信息区域显示 -->
      </div>

      <!-- 自定义主题搜索（修改为所有用户可见，但仅高级会员可用） -->
      <div class="custom-search">
        <el-input
          v-model="customTopic"
          placeholder="输入自定义主题关键字"
          size="large"
          style="width: 220px; margin-right: 10px;"
          :disabled="isLoadingCustomTopic || !isProOrEnterpriseUser"
          :tooltip="!isProOrEnterpriseUser ? '升级高级会员解锁此功能' : ''"
        />
        <TactileButton
          variant="primary"
          :loading="isLoadingCustomTopic"
          :disabled="!customTopic || !canFetchCustomTopic || isLoadingCustomTopic"
          @click="fetchCustomTopicArticles"
          class="discovery-button"
          size="lg"
        >
          <template #icon>
            <SearchIcon />
          </template>
          搜索主题
          <span class="premium-badge">高级会员</span>
        </TactileButton>
        <!-- 为非高级会员显示提示 -->
        <el-tooltip
          v-if="!isProOrEnterpriseUser"
          effect="dark"
          content="升级到高级会员解锁自定义主题搜索功能"
          placement="top"
        >
          <div class="pro-feature-tag">高级会员功能</div>
        </el-tooltip>
      </div>
    </div>

    <!-- 剩余获取次数显示 -->
    <div class="quota-info" v-if="!isPremiumUser">
      <div class="quota-header">
        <span class="quota-title">今日可用次数</span>
        <el-button
          type="primary"
          size="small"
          class="upgrade-button"
          @click="navigateToSubscription"
        >
          {{ userStore.userTier === 'free' ? '升级基础会员' : '升级高级会员' }}
        </el-button>
      </div>

      <div class="quota-container">
        <!-- 免费用户显示热点文章配额 -->
        <div v-if="userStore.userTier === 'free'" class="quota-item">
          <el-tooltip content="升级会员可获得更多获取次数" placement="top">
            <div class="quota-card">
              <div class="quota-icon-wrapper">
                <ClockIcon class="quota-icon" />
              </div>
              <div class="quota-content">
                <span class="quota-label">热点文章</span>
                <div class="quota-progress">
                  <div class="quota-bar">
                    <div
                      class="quota-progress-fill"
                      :style="{ width: (remainingTrendingQuota / 5 * 100) + '%' }"
                    ></div>
                  </div>
                  <div class="quota-count">
                    <span class="quota-remaining">{{ remainingTrendingQuota }}</span>
                    <span class="quota-separator">/</span>
                    <span class="quota-total">5</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tooltip>
        </div>

        <!-- 基础会员显示两种配额 -->
        <template v-else-if="userStore.userTier === 'basic'">
          <div class="quota-item">
            <el-tooltip content="每日可获取10次热点文章" placement="top">
              <div class="quota-card">
                <div class="quota-icon-wrapper">
                  <TrendChartsIcon class="quota-icon" />
                </div>
                <div class="quota-content">
                  <span class="quota-label">热点文章</span>
                  <div class="quota-progress">
                    <div class="quota-bar">
                      <div
                        class="quota-progress-fill"
                        :style="{ width: (remainingTrendingQuota / 10 * 100) + '%' }"
                      ></div>
                    </div>
                    <div class="quota-count">
                      <span class="quota-remaining">{{ remainingTrendingQuota }}</span>
                      <span class="quota-separator">/</span>
                      <span class="quota-total">10</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-tooltip>
          </div>

          <div class="quota-item">
            <el-tooltip content="每日可获取10次主题文章" placement="top">
              <div class="quota-card">
                <div class="quota-icon-wrapper">
                  <MagicStickIcon class="quota-icon" />
                </div>
                <div class="quota-content">
                  <span class="quota-label">主题文章</span>
                  <div class="quota-progress">
                    <div class="quota-bar">
                      <div
                        class="quota-progress-fill"
                        :style="{ width: (remainingCategoryQuota / 10 * 100) + '%' }"
                      ></div>
                    </div>
                    <div class="quota-count">
                      <span class="quota-remaining">{{ remainingCategoryQuota }}</span>
                      <span class="quota-separator">/</span>
                      <span class="quota-total">10</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-tooltip>
          </div>
        </template>
      </div>
    </div>

    <!-- 文章结果展示区 -->
    <div class="articles-result" v-if="articles.length > 0">
      <h3 class="result-title">{{ resultTitle }}</h3>
      <div class="articles-grid">
        <DiscoveryArticleCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
          :discovery-type="getDiscoveryType()"
        />
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else-if="!isLoadingTrending && !isLoadingCategory && !isLoadingCustomTopic">
      <DocumentIcon class="empty-icon" />
      <p>暂无文章，点击按钮获取</p>
      <div v-if="!isPremiumUser" style="margin-top: 16px;">
        <el-button
          type="primary"
          @click="navigateToSubscription"
        >
          升级会员，解锁更多内容
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div class="loading-state" v-else>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElButton, ElSelect, ElOption, ElTooltip, ElSkeleton, ElMessage, ElInput } from 'element-plus'
import { TrendCharts, Document, MagicStick, Clock, Search } from '@element-plus/icons-vue'
import { articleApi } from '@/utils/api'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { watch } from 'vue'
import DiscoveryArticleCard from '@/components/DiscoveryArticleCard.vue'
import TactileButton from '@/components/common/TactileButton.vue'

// 重命名图标组件以避免命名冲突
const TrendChartsIcon = TrendCharts
const DocumentIcon = Document
const MagicStickIcon = MagicStick
const ClockIcon = Clock
const SearchIcon = Search

// 文章点击处理函数
const handleArticleClick = (articleId: string | number) => {
  router.push(`/article/${articleId}`)
}

// 状态管理
const userStore = useUserStore()
const router = useRouter()

// 导航到会员订阅页面并显示升级对话框
const navigateToSubscription = () => {
  // 使用query参数触发订阅页面显示升级对话框
  router.push({ path: '/subscription', query: { showUpgrade: 'true' } })
}

// 文章数据状态
const articles = ref<any[]>([])
const resultTitle = ref('')

// 加载状态
const isLoadingTrending = ref(false)
const isLoadingCategory = ref(false)
const isLoadingCustomTopic = ref(false)

// 主题选择
const selectedCategory = ref('')
const customTopic = ref('')

// 配额信息状态
const remainingTrendingQuota = ref(5) // 热点文章剩余次数
const remainingCategoryQuota = ref(10) // 主题文章剩余次数 (基础会员)

// LocalStorage 键名
const STORAGE_KEYS = {
  ARTICLES: 'discovery_articles',
  RESULT_TITLE: 'discovery_result_title',
  REMAINING_TRENDING_QUOTA: 'discovery_remaining_trending_quota',
  REMAINING_CATEGORY_QUOTA: 'discovery_remaining_category_quota',
  LAST_RESET_DATE: 'discovery_last_reset_date',
  LAST_USER_TIER: 'discovery_last_user_tier'
}

// 计算属性
const isPremiumUser = computed(() => {
  // 检查用户是否有高级会员权限
  return userStore.userTier === 'pro' || userStore.userTier === 'enterprise'
})

const isProOrEnterpriseUser = computed(() => {
  // 检查用户是否为PRO或ENTERPRISE会员
  return userStore.userTier === 'pro' || userStore.userTier === 'enterprise'
})

const canFetchTrending = computed(() => {
  // 付费用户无限制，免费用户检查配额
  return isPremiumUser.value || remainingTrendingQuota.value > 0
})

const canFetchCategory = computed(() => {
  // 免费会员不能使用，基础会员检查配额，高级会员无限制
  if (userStore.userTier === 'free') {
    return false
  }
  if (userStore.userTier === 'basic') {
    return remainingCategoryQuota.value > 0
  }
  // pro和enterprise用户无限制
  return true
})

const canFetchCustomTopic = computed(() => {
  // 只有PRO和ENTERPRISE会员可以使用自定义主题搜索
  return isProOrEnterpriseUser.value
})

// 获取当前发现类型
const getDiscoveryType = () => {
  if (resultTitle.value.includes('热点')) return 'trending'
  if (resultTitle.value.includes('主题')) return 'category'
  if (resultTitle.value.includes('自定义') || resultTitle.value.includes('搜索')) return 'custom'
  return 'trending' // 默认
}

// 初始化时加载用户配额和文章数据
onMounted(async () => {
  if (!userStore.isLoggedIn) return

  // 尝试手动刷新用户订阅信息，确保状态正确
  await userStore.fetchSubscription()

  await loadUserQuota()
  loadSavedArticles()
})

// 监听用户等级变化，当等级变化时自动刷新配额
watch(
  () => userStore.userTier,
  async (newTier, oldTier) => {
    if (newTier !== oldTier) {
      console.log('用户等级发生变化，从', oldTier, '变为', newTier, '，刷新配额')
      await loadUserQuota()
    }
  }
)

// 加载用户配额信息（带持久化和每日重置）
const loadUserQuota = async () => {
  try {
    if (userStore.userInfo?.id) {
      // 检查是否需要每日重置配额
      const lastResetDate = localStorage.getItem(STORAGE_KEYS.LAST_RESET_DATE)
      const today = new Date().toDateString()
      // 获取上次保存的用户等级
      const lastUserTier = localStorage.getItem(STORAGE_KEYS.LAST_USER_TIER)

      // 如果是新的一天、没有记录或用户等级发生变化，根据当前用户等级重置配额
      if (!lastResetDate || lastResetDate !== today || lastUserTier !== userStore.userTier) {
        // 热点文章配额：免费用户5次，基础会员10次
        remainingTrendingQuota.value = userStore.userTier === 'basic' ? 10 : 5

        // 主题文章配额：仅基础会员有10次
        remainingCategoryQuota.value = userStore.userTier === 'basic' ? 10 : 0

        // 更新最后重置日期和用户等级
        localStorage.setItem(STORAGE_KEYS.LAST_RESET_DATE, today)
        localStorage.setItem(STORAGE_KEYS.LAST_USER_TIER, userStore.userTier)

        // 保存重置后的配额到localStorage（修复每日不刷新问题）
        saveQuotaToStorage()
      } else {
        // 从localStorage加载保存的配额
        const savedTrendingQuota = localStorage.getItem(STORAGE_KEYS.REMAINING_TRENDING_QUOTA)
        const savedCategoryQuota = localStorage.getItem(STORAGE_KEYS.REMAINING_CATEGORY_QUOTA)

        if (savedTrendingQuota !== null) {
          remainingTrendingQuota.value = parseInt(savedTrendingQuota)
        }

        if (savedCategoryQuota !== null) {
          remainingCategoryQuota.value = parseInt(savedCategoryQuota)
        }
      }
    }
  } catch (error) {
    console.error('加载用户配额失败:', error)
  }
}

// 从localStorage加载保存的文章数据
const loadSavedArticles = () => {
  try {
    const savedArticles = localStorage.getItem(STORAGE_KEYS.ARTICLES)
    const savedTitle = localStorage.getItem(STORAGE_KEYS.RESULT_TITLE)

    console.log('从localStorage加载的原始文章数据:', savedArticles)

    if (savedArticles) {
      const parsedArticles = JSON.parse(savedArticles)
      console.log('解析后的文章数据:', parsedArticles)

      // 加载数据后，确保对difficultyLevel进行映射
      articles.value = parsedArticles.map((article: any) => {
        const mappedArticle = {
          ...article,
          difficulty: article.difficulty || article.difficultyLevel || ''
        }
        console.log('映射后的文章数据:', mappedArticle)
        return mappedArticle
      })
    }

    if (savedTitle) {
      resultTitle.value = savedTitle
    }
  } catch (error) {
    console.error('加载保存的文章数据失败:', error)
  }
}

// 保存文章数据到localStorage
const saveArticlesToStorage = () => {
  try {
    localStorage.setItem(STORAGE_KEYS.ARTICLES, JSON.stringify(articles.value))
    localStorage.setItem(STORAGE_KEYS.RESULT_TITLE, resultTitle.value)
  } catch (error) {
    console.error('保存文章数据失败:', error)
  }
}

// 保存配额到localStorage
const saveQuotaToStorage = () => {
  try {
    localStorage.setItem(STORAGE_KEYS.REMAINING_TRENDING_QUOTA, remainingTrendingQuota.value.toString())
    localStorage.setItem(STORAGE_KEYS.REMAINING_CATEGORY_QUOTA, remainingCategoryQuota.value.toString())
  } catch (error) {
    console.error('保存配额失败:', error)
  }
}

// 获取热点文章
const fetchTrendingArticles = async () => {
  if (!canFetchTrending.value || isLoadingTrending.value) return

  isLoadingTrending.value = true
  try {
      // 调用热点文章API
      const response = await articleApi.getTrendingArticles(9)
      // 将difficultyLevel映射到difficulty属性，并确保wordCount字段存在
      articles.value = (response.data || []).map((article: any) => ({
        ...article,
        difficulty: article.difficultyLevel || '',
        wordCount: article.wordCount || article.word_count || 0
      }))
      resultTitle.value = '🔥 热点文章'

    // 保存文章数据到localStorage
    saveArticlesToStorage()

    // 免费用户减少热点文章配额并保存
    if (!isPremiumUser.value) {
      remainingTrendingQuota.value--
      saveQuotaToStorage()
      ElMessage.success(`获取成功，热点文章剩余${remainingTrendingQuota.value}次`)
    }
  } catch (error) {
    console.error('获取热点文章失败:', error)
    ElMessage.error('获取热点文章失败，请稍后重试')
  } finally {
    isLoadingTrending.value = false
  }
}

// 获取主题文章
const fetchCategoryArticles = async () => {
  if (!selectedCategory.value || !canFetchCategory.value || isLoadingCategory.value) return

  isLoadingCategory.value = true
  try {
      // 调用主题文章API
      const response = await articleApi.getArticlesByCategory(selectedCategory.value, 9)
      // 将difficultyLevel映射到difficulty属性，并确保wordCount字段存在
      articles.value = (response.data || []).map((article: any) => ({
        ...article,
        difficulty: article.difficultyLevel || '',
        wordCount: article.wordCount || article.word_count || 0
      }))

    // 设置结果标题（根据选择的主题显示对应的中文名称）
    const categoryMap: Record<string, string> = {
      technology: '科技',
      health: '健康',
      business: '商业',
      education: '教育',
      entertainment: '娱乐',
      sports: '体育',
      travel: '旅行',
      food: '美食'
    }
    resultTitle.value = `${categoryMap[selectedCategory.value] || selectedCategory.value}主题文章`

    // 保存文章数据到localStorage
    saveArticlesToStorage()

    // 基础会员减少主题文章配额并保存
    if (userStore.userTier === 'basic') {
      remainingCategoryQuota.value--
      saveQuotaToStorage()
      ElMessage.success(`获取成功，主题文章剩余${remainingCategoryQuota.value}次`)
    }
  } catch (error: any) {
    console.error('获取主题文章失败:', error)
    // 输出更详细的错误信息，帮助诊断400 Bad Request问题
    if (error.response) {
      console.error('错误详情:', error.response.data)
      console.error('请求参数:', { category: selectedCategory.value, count: 6 })
    }
    ElMessage.error('获取主题文章失败，请稍后重试')
  } finally {
    isLoadingCategory.value = false
  }
}

// 获取自定义主题文章（仅对PRO和ENTERPRISE会员开放）
const fetchCustomTopicArticles = async () => {
  if (!customTopic.value || !canFetchCustomTopic.value || isLoadingCustomTopic.value) {
    return
  }

  isLoadingCustomTopic.value = true
  try {
      // 调用自定义主题文章API
      // 注意：这里使用了getArticlesByCategory作为临时实现，实际项目中可能需要一个专门的API
      const response = await articleApi.getArticlesByCategory(customTopic.value, 9)
      // 将difficultyLevel映射到difficulty属性，并确保wordCount字段存在
      articles.value = (response.data || []).map((article: any) => ({
        ...article,
        difficulty: article.difficultyLevel || '',
        wordCount: article.wordCount || article.word_count || 0
      }))
      resultTitle.value = `🔍 "${customTopic.value}" 主题文章`

    // 保存文章数据到localStorage
    saveArticlesToStorage()

    ElMessage.success(`"${customTopic.value}" 主题文章搜索成功`)
  } catch (error: any) {
    console.error('获取自定义主题文章失败:', error)
    ElMessage.error('获取自定义主题文章失败，请稍后重试')
  } finally {
    isLoadingCustomTopic.value = false
  }
}
</script>

<style scoped>
.article-discovery {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.discovery-header {
  text-align: center;
  margin-bottom: 30px;
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  position: relative;
  display: inline-block;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #409eff, #66b1ff);
  border-radius: 3px;
}

.section-description {
  color: #909399;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

/* 统一按钮样式 */
.discovery-button {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  min-width: 160px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.discovery-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 优化标签样式和位置 */
.premium-badge, .basic-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #ff6700;
  color: #fff;
  font-size: 10px;
  padding: 3px 8px;
  border-radius: 12px;
  font-weight: normal;
  white-space: nowrap;
}

.basic-badge {
  background: #409eff;
}

.basic-feature-tag {
  color: #409eff;
  font-size: 12px;
  margin-left: 8px;
  font-weight: 500;
  cursor: help;
  display: inline-flex;
  align-items: center;
}

/* 优化功能标签样式 */
.pro-feature-tag {
  color: #ff6700;
  font-size: 12px;
  margin-left: 8px;
  font-weight: 500;
  cursor: help;
  display: inline-flex;
  align-items: center;
}

/* 优化主题选择器布局 */
.category-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  background: #f8f9fa;
  padding: 10px;
  border-radius: 8px;
}

/* 统一输入框和选择器样式 */
.category-selector .el-select, .custom-search .el-input {
  flex-shrink: 0;
}

/* 优化配额信息样式 */
.quota-info {
  margin-bottom: 24px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.quota-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e9ecef;
}

.quota-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.upgrade-button {
  min-width: 100px;
  padding: 6px 16px;
  font-size: 12px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.upgrade-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

.quota-container {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.quota-item {
  flex: 1;
  min-width: 220px;
}

.quota-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.quota-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.quota-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #ecf5ff;
  border-radius: 50%;
  flex-shrink: 0;
}

.quota-icon {
  font-size: 18px;
  color: #409eff;
}

.quota-content {
  flex: 1;
  min-width: 0;
}

.quota-label {
  display: block;
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.quota-progress {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quota-bar {
  flex: 1;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
  position: relative;
}

.quota-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #66b1ff);
  border-radius: 3px;
  transition: width 0.3s ease;
  position: relative;
}

.quota-progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.quota-count {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  min-width: 50px;
  justify-content: flex-end;
}

.quota-remaining {
  color: #409eff;
  font-size: 16px;
}

.quota-separator {
  color: #dcdfe6;
  margin: 0 2px;
}

.quota-total {
  color: #909399;
  font-size: 14px;
  font-weight: normal;
}

/* 自定义搜索区域样式 */
.custom-search {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  background: #f8f9fa;
  padding: 10px;
  border-radius: 8px;
}

/* 文章结果区域样式 */
.articles-result {
  margin-top: 30px;
}

.result-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-top: 20px;
}

.article-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.article-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.article-meta .el-tag {
  cursor: default;
}

.article-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-card p {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-action {
  text-align: right;
  margin-top: 8px;
}

.article-action .el-button {
  color: #409eff;
  font-size: 14px;
}

.article-action .el-button:hover {
  color: #66b1ff;
}

/* 加载状态调整 */
.loading-state {
  margin-top: 20px;
}

.loading-state .el-skeleton {
  margin-bottom: 24px;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
  background: #f8f9fa;
  border-radius: 8px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.3;
}

/* 加载状态样式 */
.loading-state {
  padding: 40px 0;
}

.article-skeleton {
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
}

.skeleton-cover {
  width: 100%;
  height: 180px;
  background: #e4e7ed;
  border-radius: 4px;
  margin-bottom: 16px;
}

.skeleton-title {
  width: 80%;
  height: 20px;
  background: #e4e7ed;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-desc {
  width: 100%;
  height: 40px;
  background: #e4e7ed;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-meta {
  width: 40%;
  height: 16px;
  background: #e4e7ed;
  border-radius: 4px;
}

/* 响应式设计优化 */
@media (max-width: 768px) {
  .article-discovery {
    padding: 16px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 16px;
  }

  .category-selector, .custom-search {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .category-selector .el-select, .custom-search .el-input {
    width: 100% !important;
    margin: 0 !important;
  }

  .category-selector .el-button, .custom-search .el-button {
    width: 100%;
    min-width: auto;
  }

  .quota-info {
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }

  .upgrade-button {
    width: 100%;
  }

  .articles-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .section-title {
    font-size: 24px;
  }

  .discovery-button {
    padding: 10px 20px;
    font-size: 13px;
  }

  .premium-badge, .basic-badge {
    font-size: 9px;
    padding: 2px 6px;
    top: -6px;
    right: -6px;
  }
}
</style>

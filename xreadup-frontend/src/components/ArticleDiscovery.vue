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
      <div class="trending-selector">
        <div class="button-with-tag">
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
        </div>
      </div>

      <!-- 主题文章选择器和按钮 -->
      <div class="category-selector">
        <el-select
          v-model="selectedCategory"
          placeholder="选择文章主题"
          size="large"
          style="width: 180px; margin-right: 10px;"
          :disabled="!canFetchCategory || isLoadingCategory"
          :tooltip="!canFetchCategory ? '升级基础会员及以上解锁此功能' : ''"
        >
          <el-option
            v-for="option in getCategoryOptions()"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <div class="button-with-tag">
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
          </TactileButton>
          <!-- 会员等级标签 -->
          <span class="membership-tag basic">基础会员+</span>
        </div>
        <el-input
          v-model="customTopic"
          placeholder="输入自定义主题关键字"
          size="large"
          style="width: 220px; margin-right: 10px;"
          :disabled="isLoadingCustomTopic || !isProOrEnterpriseUser"
          :tooltip="!isProOrEnterpriseUser ? '升级专业会员及以上解锁此功能' : ''"
        />
        <div class="button-with-tag">
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
            自定义主题
          </TactileButton>
          <!-- 会员等级标签 -->
          <span class="membership-tag pro">专业会员+</span>
        </div>
      </div>

    </div>

    <!-- 高级筛选切换按钮 -->
    <div class="advanced-toggle" v-if="isProOrEnterpriseUser">
      <el-button
        type="text"
        @click="advancedFilters.useAdvanced = !advancedFilters.useAdvanced"
        :icon="advancedFilters.useAdvanced ? 'ArrowUp' : 'ArrowDown'"
      >
        {{ advancedFilters.useAdvanced ? '隐藏高级筛选' : '显示高级筛选' }}
      </el-button>
    </div>

    <!-- 高级筛选面板 -->
    <div class="advanced-filters" v-if="advancedFilters.useAdvanced && isProOrEnterpriseUser">
      <div class="filters-header">
        <div class="filters-title">
          <h4>高级筛选</h4>
          <div class="filter-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>注意：语言和国家不能同时使用，优先使用语言筛选</span>
          </div>
        </div>
        <el-button type="text" @click="advancedFilters.useAdvanced = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      <div class="filters-content">
        <div class="filter-row">
            <div class="filter-item">
              <label>
                语言
                <el-tooltip content="控制新闻的语言，如英语、中文、法语等。选择语言后将禁用国家筛选。" placement="top">
                  <el-icon class="help-icon"><QuestionFilled /></el-icon>
                </el-tooltip>
              </label>
              <el-select
                v-model="advancedFilters.language"
                placeholder="选择语言"
                size="small"
                clearable
                @change="handleLanguageChange"
              >
                <el-option
                  v-for="option in getLanguageOptions()"
                  :key="option.value"
                  :label="getLanguageLabel(option.value)"
                  :value="option.value"
                >
                  <div class="option-content">
                    <span class="option-flag">{{ option.flag }}</span>
                    <span class="option-label">{{ option.label }}</span>
                    <span class="option-desc">{{ option.description }}</span>
                  </div>
                </el-option>
              </el-select>
            </div>
            <div class="filter-item">
              <label>
                国家（可选）
                <el-tooltip content="可选：控制新闻来源的国家。选择国家后将禁用语言筛选。" placement="top">
                  <el-icon class="help-icon"><QuestionFilled /></el-icon>
                </el-tooltip>
              </label>
              <el-select
                v-model="advancedFilters.country"
                placeholder="不限国家"
                clearable
                size="small"
                :disabled="!!advancedFilters.language"
                @change="handleCountryChange"
              >
                <el-option
                  v-for="option in getCountryOptions()"
                  :key="option.value"
                  :label="getCountryLabel(option.value)"
                  :value="option.value"
                >
                  <div class="option-content">
                    <span class="option-flag">{{ option.flag }}</span>
                    <span class="option-label">{{ option.label }}</span>
                    <span class="option-desc">{{ option.description }}</span>
                  </div>
                </el-option>
              </el-select>
            </div>
          <div class="filter-item">
            <label>排序</label>
            <el-select v-model="advancedFilters.sortBy" placeholder="选择排序" size="small">
              <el-option
                v-for="option in getSortOptions()"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </div>
        </div>
        <div class="filter-row">
          <div class="filter-item">
            <label>开始日期</label>
            <el-date-picker
              v-model="advancedFilters.fromDate"
              type="datetime"
              placeholder="选择开始日期"
              size="small"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ssZ"
            />
          </div>
          <div class="filter-item">
            <label>结束日期</label>
            <el-date-picker
              v-model="advancedFilters.toDate"
              type="datetime"
              placeholder="选择结束日期"
              size="small"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ssZ"
            />
          </div>
        </div>

        <!-- 筛选条件预览 -->
        <div class="filter-preview" v-if="hasActiveFilters">
          <div class="preview-title">
            <el-icon><Filter /></el-icon>
            <span>当前筛选条件</span>
          </div>
          <div class="preview-tags">
            <el-tag
              v-if="advancedFilters.language"
              type="primary"
              closable
              @close="advancedFilters.language = ''"
            >
              语言: {{ getLanguageLabel(advancedFilters.language) }}
            </el-tag>
            <el-tag
              v-if="advancedFilters.country"
              type="success"
              closable
              @close="advancedFilters.country = ''"
            >
              国家: {{ getCountryLabel(advancedFilters.country) }}
            </el-tag>
            <el-tag
              v-if="advancedFilters.sortBy"
              type="info"
              closable
              @close="advancedFilters.sortBy = 'publishedAt'"
            >
              排序: {{ getSortLabel(advancedFilters.sortBy) }}
            </el-tag>
            <el-tag
              v-if="advancedFilters.fromDate"
              type="warning"
              closable
              @close="advancedFilters.fromDate = ''"
            >
              开始: {{ formatDatePreview(advancedFilters.fromDate) }}
            </el-tag>
            <el-tag
              v-if="advancedFilters.toDate"
              type="warning"
              closable
              @close="advancedFilters.toDate = ''"
            >
              结束: {{ formatDatePreview(advancedFilters.toDate) }}
            </el-tag>
          </div>
        </div>
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
          {{ userStore.userTier === 'free' ? '升级基础会员+' : '升级专业会员+' }}
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

    <!-- 内容展示区 -->
    <div class="content-area">
      <!-- 文章结果展示区 -->
      <div class="articles-result" v-if="articles.length > 0">
        <h3 class="result-title">{{ resultTitle }}</h3>
        <div class="articles-grid">
          <ArticleCard
            v-for="article in articles"
            :key="article.id"
            :article="article"
            :show-discovery-badge="true"
            :discovery-type="getDiscoveryType()"
          />
        </div>
      </div>

      <!-- 加载状态 -->
      <div class="loading-state" v-else-if="isLoadingTrending || isLoadingCategory || isLoadingCustomTopic">
        <div class="loading-content">
          <!-- 加载指示器 -->
          <div class="loading-indicator">
            <div class="loading-spinner">
              <div class="spinner-ring"></div>
              <div class="spinner-ring"></div>
              <div class="spinner-ring"></div>
            </div>
            <div class="loading-text">
              <h3 class="loading-title">正在获取精彩文章...</h3>
              <p class="loading-subtitle">请稍候，我们正在为您精心挑选优质内容</p>
            </div>
            <div class="loading-progress">
              <div class="progress-bar">
                <div class="progress-fill"></div>
              </div>
              <div class="progress-dots">
                <span class="dot" v-for="n in 3" :key="n" :style="{ animationDelay: (n - 1) * 0.2 + 's' }"></span>
              </div>
            </div>
          </div>

          <!-- 加载文章网格 -->
          <div class="loading-grid">
            <div class="loading-card" v-for="n in 6" :key="n" :style="{ animationDelay: (n - 1) * 0.1 + 's' }">
              <!-- 图片区域骨架 -->
              <div class="loading-image"></div>

              <!-- 卡片内容骨架 -->
              <div class="loading-card-content">
                <!-- 顶部信息栏骨架 -->
                <div class="loading-header">
                  <div class="loading-source-info">
                    <div class="loading-source-name"></div>
                    <div class="loading-publish-time"></div>
                  </div>
                  <div class="loading-category-tags">
                    <div class="loading-category-tag"></div>
                    <div class="loading-difficulty-tag"></div>
                  </div>
                </div>

                <!-- 标题骨架 -->
                <div class="loading-card-title"></div>
                <div class="loading-card-title short"></div>

                <!-- 描述骨架 -->
                <div class="loading-card-desc"></div>
                <div class="loading-card-desc"></div>
                <div class="loading-card-desc short"></div>

                <!-- 底部信息骨架 -->
                <div class="loading-footer">
                  <div class="loading-reading-info">
                    <div class="loading-read-time"></div>
                    <div class="loading-word-count"></div>
                    <div class="loading-discovery-type"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 错误状态 -->
      <div class="error-state" v-else-if="hasError">
        <div class="error-icon-wrapper">
          <el-icon class="error-icon"><WarningFilled /></el-icon>
        </div>
        <h3 class="error-title">获取文章失败</h3>
        <p class="error-message">{{ errorMessage }}</p>
        <div class="error-suggestions">
          <div class="suggestion-item" v-if="advancedFilters.useAdvanced && isProOrEnterpriseUser">
            <el-icon><InfoFilled /></el-icon>
            <span>GNews API限制：语言和国家不能同时使用，已自动优先使用语言筛选</span>
          </div>
          <div class="suggestion-item">
            <el-icon><InfoFilled /></el-icon>
            <span>检查网络连接是否正常</span>
          </div>
          <div class="suggestion-item">
            <el-icon><InfoFilled /></el-icon>
            <span>尝试选择其他分类或关键词</span>
          </div>
        </div>
        <div class="error-actions">
          <el-button type="primary" @click="retryLastAction">
            <el-icon><RefreshRight /></el-icon>
            重试
          </el-button>
          <el-button @click="clearError">返回</el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-else>
        <div class="empty-content">
          <div class="empty-icon-wrapper">
            <el-icon class="empty-icon"><Document /></el-icon>
          </div>
          <h3 class="empty-title">暂无文章</h3>
          <p class="empty-description">选择分类或输入关键词开始探索精彩内容</p>
          <div class="empty-actions">
            <el-button type="primary" size="large" @click="fetchTrendingArticles">
              <el-icon><TrendCharts /></el-icon>
              获取热点文章
            </el-button>
            <el-button size="large" @click="() => { selectedCategory = 'general'; fetchCategoryArticles(); }">
              <el-icon><Menu /></el-icon>
              浏览分类文章
            </el-button>
          </div>
          <div v-if="!isPremiumUser" class="upgrade-prompt">
            <el-divider>
              <span class="divider-text">升级解锁更多功能</span>
            </el-divider>
            <el-button
              type="success"
              plain
              @click="navigateToSubscription"
            >
              <el-icon><Star /></el-icon>
              升级会员，解锁更多内容
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElButton, ElSelect, ElOption, ElTooltip, ElSkeleton, ElMessage, ElInput } from 'element-plus'
import { TrendCharts, Document, MagicStick, Clock, Search, Close, ArrowUp, ArrowDown, InfoFilled, QuestionFilled, WarningFilled, RefreshRight, Menu, Star, Filter } from '@element-plus/icons-vue'
import { articleApi } from '@/utils/api'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { watch } from 'vue'
import ArticleCard from '@/components/ArticleCard.vue'
import TactileButton from '@/components/common/TactileButton.vue'
import { CATEGORY_MAP, getCategoryOptions } from '@/utils/categoryConfig'
import { getLanguageOptions, getCountryOptions, getSortOptions, getLanguageLabel, getCountryLabel, getSortLabel, getLanguageDescription, getCountryDescription } from '@/utils/gnewsConfig'

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

// 高级筛选选项
const advancedFilters = ref({
  language: 'en',
  country: '',
  sortBy: 'publishedAt',
  fromDate: '',
  toDate: '',
  useAdvanced: false
})

// 错误状态
const errorMessage = ref('')
const lastAction = ref<(() => void) | null>(null)

// 计算属性：是否有错误
const hasError = computed(() => {
  return !!errorMessage.value && !isLoadingTrending.value && !isLoadingCategory.value && !isLoadingCustomTopic.value
})

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

// 处理语言选择变化
const handleLanguageChange = (value: string) => {
  if (value) {
    // 选择语言时，清空国家选择
    advancedFilters.value.country = ''
  }
}

// 处理国家选择变化
const handleCountryChange = (value: string) => {
  if (value) {
    // 选择国家时，清空语言选择
    advancedFilters.value.language = ''
  }
}

// 计算是否有活跃的筛选条件
const hasActiveFilters = computed(() => {
  return !!(
    advancedFilters.value.language ||
    advancedFilters.value.country ||
    advancedFilters.value.sortBy !== 'publishedAt' ||
    advancedFilters.value.fromDate ||
    advancedFilters.value.toDate
  )
})

// 格式化日期预览
const formatDatePreview = (dateString: string) => {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch {
    return dateString
  }
}

// 获取当前发现类型
const getDiscoveryType = () => {
  if (resultTitle.value.includes('热点')) return 'trending'
  if (resultTitle.value.includes('主题')) return 'category'
  if (resultTitle.value.includes('自定义')) return 'custom'
  return 'trending' // 默认
}

// 初始化时加载用户配额和文章数据
onMounted(async () => {
  if (!userStore.isLoggedIn) return

  // 尝试手动刷新用户订阅信息，确保状态正确
  await userStore.fetchSubscription()

  await loadUserQuota()
  loadSavedArticles()

  // 确保非专业会员无法使用高级筛选
  if (!isProOrEnterpriseUser.value) {
    advancedFilters.value.useAdvanced = false
  }

  // 确保初始状态没有错误
  clearError()
})

// 监听用户等级变化，当等级变化时自动刷新配额
watch(
  () => userStore.userTier,
  async (newTier, oldTier) => {
    if (newTier !== oldTier) {
      console.log('用户等级发生变化，从', oldTier, '变为', newTier, '，刷新配额')
      await loadUserQuota()

      // 如果降级为非专业会员，关闭高级筛选
      if (!isProOrEnterpriseUser.value) {
        advancedFilters.value.useAdvanced = false
      }
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

// 清除错误
const clearError = () => {
  errorMessage.value = ''
  lastAction.value = null
}

// 重试最后的操作
const retryLastAction = () => {
  if (lastAction.value) {
    clearError()
    lastAction.value()
  }
}


// 获取热点文章
const fetchTrendingArticles = async () => {
  if (!canFetchTrending.value || isLoadingTrending.value) return

  clearError()
  lastAction.value = fetchTrendingArticles
  isLoadingTrending.value = true
  articles.value = [] // 清空文章列表，显示加载状态
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

    // 清除错误状态
    clearError()

    // 保存文章数据到localStorage
    saveArticlesToStorage()

    // 免费用户减少热点文章配额并保存
    if (!isPremiumUser.value) {
      remainingTrendingQuota.value--
      saveQuotaToStorage()
      ElMessage.success(`获取成功，热点文章剩余${remainingTrendingQuota.value}次`)
    }
  } catch (error: any) {
    console.error('获取热点文章失败:', error)
    errorMessage.value = error.response?.data?.message || '获取热点文章失败，请检查网络连接或稍后重试'
    articles.value = [] // 清空文章数组
  } finally {
    isLoadingTrending.value = false
  }
}

// 获取主题文章
const fetchCategoryArticles = async () => {
  if (!selectedCategory.value || !canFetchCategory.value || isLoadingCategory.value) return

  clearError()
  lastAction.value = fetchCategoryArticles
  isLoadingCategory.value = true
  articles.value = [] // 清空文章列表，显示加载状态
  try {
    let response

    // 根据是否使用高级筛选选择API（仅专业会员及以上可用）
    if (advancedFilters.value.useAdvanced && isProOrEnterpriseUser.value) {
      // 构建参数，根据GNews API限制，不能同时使用language和country
      const params: any = {
        category: selectedCategory.value,
        limit: 9,
        fromDate: advancedFilters.value.fromDate,
        toDate: advancedFilters.value.toDate,
        sortBy: advancedFilters.value.sortBy
      }

      // 优先使用语言参数，如果同时设置了国家和语言，只使用语言
      if (advancedFilters.value.language) {
        params.language = advancedFilters.value.language
      } else if (advancedFilters.value.country) {
        params.country = advancedFilters.value.country
      }

      // 使用增强版API
      response = await articleApi.getArticlesByCategoryAdvanced(params)
    } else {
      // 使用基础版API
      response = await articleApi.getArticlesByCategory(selectedCategory.value, 9)
    }

    // 将difficultyLevel映射到difficulty属性，并确保wordCount字段存在
    articles.value = (response.data || []).map((article: any) => ({
      ...article,
      difficulty: article.difficultyLevel || '',
      wordCount: article.wordCount || article.word_count || 0
    }))

    // 设置结果标题（根据选择的主题显示对应的中文名称）
    const categoryLabel = CATEGORY_MAP[selectedCategory.value] || selectedCategory.value
    const languageLabel = getLanguageLabel(advancedFilters.value.language)
    const countryLabel = getCountryLabel(advancedFilters.value.country)

    if (advancedFilters.value.useAdvanced) {
      resultTitle.value = `${categoryLabel}主题文章 (${languageLabel} - ${countryLabel})`
    } else {
      resultTitle.value = `${categoryLabel}主题文章`
    }

    // 清除错误状态
    clearError()

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
      console.error('请求参数:', { category: selectedCategory.value, count: 9 })
    }
    errorMessage.value = error.response?.data?.message || '获取主题文章失败，请尝试调整筛选条件'
    articles.value = [] // 清空文章数组
  } finally {
    isLoadingCategory.value = false
  }
}

// 获取自定义主题文章（仅对PRO和ENTERPRISE会员开放）
const fetchCustomTopicArticles = async () => {
  if (!customTopic.value || !canFetchCustomTopic.value || isLoadingCustomTopic.value) {
    return
  }

  clearError()
  lastAction.value = fetchCustomTopicArticles
  isLoadingCustomTopic.value = true
  articles.value = [] // 清空文章列表，显示加载状态
  try {
    let response

    // 根据是否使用高级筛选选择API（仅专业会员及以上可用）
    if (advancedFilters.value.useAdvanced && isProOrEnterpriseUser.value) {
      // 构建参数，根据GNews API限制，不能同时使用language和country
      const params: any = {
        keyword: customTopic.value,
        limit: 9,
        fromDate: advancedFilters.value.fromDate,
        toDate: advancedFilters.value.toDate,
        sortBy: advancedFilters.value.sortBy
      }

      // 优先使用语言参数，如果同时设置了国家和语言，只使用语言
      if (advancedFilters.value.language) {
        params.language = advancedFilters.value.language
      } else if (advancedFilters.value.country) {
        params.country = advancedFilters.value.country
      }

      // 使用增强版API
      response = await articleApi.searchArticlesAdvanced(params)
    } else {
      // 使用基础版API
      response = await articleApi.searchArticles(customTopic.value, 9)
    }

    // 将difficultyLevel映射到difficulty属性，并确保wordCount字段存在
    articles.value = (response.data || []).map((article: any) => ({
      ...article,
      difficulty: article.difficultyLevel || '',
      wordCount: article.wordCount || article.word_count || 0
    }))

    // 设置结果标题
    if (advancedFilters.value.useAdvanced) {
      const languageLabel = getLanguageLabel(advancedFilters.value.language)
      const countryLabel = getCountryLabel(advancedFilters.value.country)
      resultTitle.value = `🔍 "${customTopic.value}" 自定义主题文章 (${languageLabel} - ${countryLabel})`
    } else {
      resultTitle.value = `🔍 "${customTopic.value}" 自定义主题文章`
    }

    // 清除错误状态
    clearError()

    // 保存文章数据到localStorage
    saveArticlesToStorage()

    ElMessage.success(`"${customTopic.value}" 主题文章搜索成功`)
  } catch (error: any) {
    console.error('获取自定义主题文章失败:', error)
    errorMessage.value = error.response?.data?.message || '获取自定义主题文章失败，请尝试其他关键词'
    articles.value = [] // 清空文章数组
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
  flex-direction: row;
  align-items: flex-start;
  gap: 20px;
  flex-wrap: nowrap;
  margin-bottom: 24px;
  justify-content: center;
  overflow-x: auto;
  padding: 10px 0;
}

/* 高级筛选面板样式 */
.advanced-filters {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  margin: 16px auto;
  max-width: 800px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.filters-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.filters-title {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.filters-title h4 {
  margin: 0 0 6px 0;
  color: #303133;
  font-size: 15px;
  font-weight: 600;
  text-align: center;
}

.filter-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 12px;
  justify-content: center;
}

.help-icon {
  color: #409eff;
  cursor: help;
  font-size: 14px;
}

.filters-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.filter-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
  width: 100%;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 180px;
}

.filter-item label {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.option-flag {
  font-size: 16px;
  min-width: 20px;
}

.option-label {
  font-weight: 500;
  color: #303133;
}

.option-desc {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.advanced-toggle {
  text-align: center;
  margin: 16px 0;
  padding: 8px 0;
}

.advanced-toggle .el-button {
  color: #409eff;
  font-size: 14px;
}

.advanced-toggle .el-button:hover {
  color: #66b1ff;
}

/* 筛选条件预览样式 */
.filter-preview {
  margin-top: 16px;
  padding: 16px;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.preview-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preview-tags .el-tag {
  font-size: 12px;
  border-radius: 4px;
}

/* 统一按钮样式 */
.discovery-button {
  /* 移除可能影响高度的样式，让 TactileButton 的样式生效 */
  min-width: 160px;
}

/* 确保所有按钮容器高度一致 */
.action-buttons .button-with-tag {
  height: 52px;
  display: inline-flex;
  align-items: center;
}

.discovery-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 按钮和标签容器 */
.button-with-tag {
  position: relative;
  display: inline-flex;
  align-items: center;
  height: 52px; /* 与 TactileButton lg 尺寸的 min-height 保持一致 */
}

/* 会员等级标签样式 */
.membership-tag {
  position: absolute;
  top: -8px;
  right: -8px;
  display: inline-flex;
  align-items: center;
  padding: 3px 8px;
  font-size: 10px;
  font-weight: 500;
  border-radius: 12px;
  white-space: nowrap;
  transition: all 0.2s ease;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.membership-tag.basic {
  background: #409eff;
  color: #fff;
  border: 1px solid #409eff;
}

.membership-tag.pro {
  background: #ff6700;
  color: #fff;
  border: 1px solid #ff6700;
}

.membership-tag:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
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

/* 热点文章选择器布局 */
.trending-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: nowrap;
  background: #f8f9fa;
  padding: 10px;
  border-radius: 8px;
  min-width: 200px;
  flex-shrink: 0;
}

/* 优化主题选择器布局 */
.category-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: nowrap;
  background: #f8f9fa;
  padding: 10px;
  border-radius: 8px;
  min-width: 300px;
  flex-shrink: 0;
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
  flex-wrap: nowrap;
  background: #f8f9fa;
  padding: 10px;
  border-radius: 8px;
  min-width: 320px;
  flex-shrink: 0;
}

/* 文章结果区域样式 */
/* 内容展示区 */
.content-area {
  margin-top: 30px;
}

.articles-result {
  margin-top: 0;
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

/* 错误状态样式 */
.error-state {
  text-align: center;
  padding: 60px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.error-icon-wrapper {
  margin-bottom: 20px;
}

.error-icon {
  font-size: 64px;
  color: #f56c6c;
}

.error-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.error-message {
  font-size: 14px;
  color: #606266;
  margin: 0 0 24px 0;
}

.error-suggestions {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin: 0 auto 24px;
  max-width: 500px;
  text-align: left;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}

.suggestion-item:last-child {
  margin-bottom: 0;
}

.suggestion-item .el-icon {
  color: #409eff;
  margin-top: 2px;
  flex-shrink: 0;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 40px 20px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-content {
  text-align: center;
  max-width: 500px;
  width: 100%;
}

.empty-icon-wrapper {
  margin-bottom: 24px;
}

.empty-icon {
  font-size: 80px;
  color: #e1e6eb;
  opacity: 0.8;
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.empty-description {
  font-size: 16px;
  color: #606266;
  margin: 0 0 32px 0;
  line-height: 1.5;
}

.empty-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.empty-actions .el-button {
  min-width: 160px;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
}

.upgrade-prompt {
  margin-top: 24px;
}

.divider-text {
  color: #909399;
  font-size: 14px;
  padding: 0 16px;
}

.upgrade-prompt .el-button {
  margin-top: 16px;
  height: 40px;
  border-radius: 6px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .empty-actions {
    flex-direction: column;
    align-items: center;
  }

  .empty-actions .el-button {
    width: 100%;
    max-width: 280px;
  }

  .empty-title {
    font-size: 20px;
  }

  .empty-description {
    font-size: 14px;
  }
}

/* 加载状态样式 */
.loading-state {
  padding: 40px 0;
}

.loading-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* 加载指示器 */
.loading-indicator {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 20px;
  background: linear-gradient(135deg,
    rgba(255, 255, 255, 0.95) 0%,
    rgba(248, 250, 252, 0.9) 100%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.loading-spinner {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-radius: 50%;
  animation: spin 2s linear infinite;
}

.spinner-ring:nth-child(1) {
  border-top-color: #409eff;
  animation-duration: 1.5s;
}

.spinner-ring:nth-child(2) {
  border-right-color: #67c23a;
  animation-duration: 2s;
  animation-delay: -0.5s;
}

.spinner-ring:nth-child(3) {
  border-bottom-color: #e6a23c;
  animation-duration: 2.5s;
  animation-delay: -1s;
}

.loading-text {
  margin-bottom: 24px;
}

.loading-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #409eff, #67c23a, #e6a23c);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradientShift 3s ease-in-out infinite;
}

.loading-subtitle {
  font-size: 16px;
  color: #606266;
  margin: 0;
  opacity: 0.8;
}

.loading-progress {
  max-width: 300px;
  margin: 0 auto;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 16px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #67c23a, #e6a23c);
  background-size: 200% 100%;
  border-radius: 2px;
  animation: progressMove 2s ease-in-out infinite;
}

.progress-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

.dot:nth-child(2) {
  background: #67c23a;
}

.dot:nth-child(3) {
  background: #e6a23c;
}

.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  padding: 0 20px;
}

.loading-card {
  background: #ffffff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid rgba(0, 0, 0, 0.08);
  opacity: 0;
  transform: translateY(20px) scale(0.95);
  animation: cardAppear 0.6s ease-out forwards;
}

.loading-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: shimmer 2s ease-in-out infinite;
}

.loading-card-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  z-index: 1;
  height: 100%;
  flex: 1;
}

/* 图片区域骨架 */
.loading-image {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 2s ease-in-out infinite;
}

/* 顶部信息栏骨架 */
.loading-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.loading-source-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.loading-source-name {
  width: 80px;
  height: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 2s ease-in-out infinite;
}

.loading-publish-time {
  width: 60px;
  height: 11px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 2s ease-in-out infinite;
}

.loading-category-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.loading-category-tag {
  width: 50px;
  height: 20px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 6px;
  animation: shimmer 2s ease-in-out infinite;
}

.loading-difficulty-tag {
  width: 40px;
  height: 20px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 6px;
  animation: shimmer 2s ease-in-out infinite;
}

.loading-badge-icon {
  width: 12px;
  height: 12px;
  background: linear-gradient(135deg, #409eff, #67c23a);
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

.loading-badge-text {
  width: 40px;
  height: 12px;
  background: linear-gradient(135deg,
    rgba(64, 158, 255, 0.6) 0%,
    rgba(103, 194, 58, 0.4) 100%);
  border-radius: 6px;
  animation: shimmer 2s ease-in-out infinite;
}

.loading-new-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 24px;
  height: 16px;
  background: linear-gradient(135deg, #f56c6c, #e6a23c);
  border-radius: 8px;
  animation: pulse 1.8s ease-in-out infinite;
  z-index: 2;
}

.loading-image {
  width: 100%;
  height: 180px;
  background: linear-gradient(135deg,
    rgba(240, 244, 248, 0.8) 0%,
    rgba(226, 232, 240, 0.6) 50%,
    rgba(240, 244, 248, 0.8) 100%);
  border-radius: 12px;
  margin-bottom: 16px;
  animation: shimmer 2s ease-in-out infinite;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.loading-text {
  padding: 0 4px;
}

.loading-card-title {
  height: 18px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  margin-bottom: 8px;
  animation: shimmer 2s ease-in-out infinite;
}

.loading-card-title.short {
  width: 60%;
}

.loading-card-desc {
  height: 14px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 3px;
  margin-bottom: 6px;
  animation: shimmer 2s ease-in-out infinite;
}

.loading-card-desc.short {
  width: 40%;
}

/* 底部信息骨架 */
.loading-footer {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #F2F2F7;
}

.loading-reading-info {
  display: flex;
  gap: 16px;
  align-items: center;
}

.loading-read-time {
  width: 60px;
  height: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 2s ease-in-out infinite;
}

.loading-word-count {
  width: 40px;
  height: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 2s ease-in-out infinite;
}

.loading-discovery-type {
  width: 50px;
  height: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 2s ease-in-out infinite;
}


@keyframes shimmer {
  0% {
    background-position: -200% 0;
    opacity: 0.8;
  }
  50% {
    opacity: 1;
  }
  100% {
    background-position: 200% 0;
    opacity: 0.8;
  }
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

@keyframes progressMove {
  0% {
    background-position: 0% 50%;
    width: 0%;
  }
  50% {
    background-position: 100% 50%;
    width: 70%;
  }
  100% {
    background-position: 0% 50%;
    width: 100%;
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0.7;
  }
}

@keyframes cardAppear {
  0% {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  50% {
    opacity: 0.8;
    transform: translateY(-5px) scale(1.02);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .loading-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 0 16px;
  }

  .loading-card-content {
    padding: 16px;
  }

  .loading-image {
    height: 160px;
  }

  .loading-title {
    width: 150px;
    height: 24px;
  }
}

/* 响应式设计优化 */
@media (max-width: 1200px) {
  .action-buttons {
    flex-wrap: wrap;
    justify-content: center;
  }

  .trending-selector, .category-selector, .custom-search {
    min-width: 280px;
  }
}

@media (max-width: 768px) {
  .article-discovery {
    padding: 16px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 16px;
    flex-wrap: wrap;
  }

  .trending-selector, .category-selector, .custom-search {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    min-width: auto;
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

  .membership-tag {
    font-size: 9px;
    padding: 2px 6px;
    top: -6px;
    right: -6px;
  }

  /* 加载卡片移动端样式 */
  .loading-card {
    border-radius: 16px;
  }

  .loading-card-content {
    padding: 16px;
    gap: 12px;
  }

  .loading-image {
    height: 160px;
  }

  .loading-header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .loading-category-tags {
    gap: 6px;
  }

  .loading-category-tag,
  .loading-difficulty-tag {
    width: 40px;
    height: 16px;
  }

  .loading-reading-info {
    gap: 12px;
  }

  .loading-read-time,
  .loading-word-count,
  .loading-discovery-type {
    height: 10px;
  }
}
</style>

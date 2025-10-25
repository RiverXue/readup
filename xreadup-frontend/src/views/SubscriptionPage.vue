<template>
  <div class="subscription-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>
        <span style="font-size: 28px; margin-right: 8px;">💎</span>
        会员订阅
      </h1>
      <p>升级会员，享受更好的学习体验。</p>
    </div>

    <!-- 当前订阅状态 -->
    <div v-if="currentSubscription && currentSubscription.planType !== 'FREE'" class="current-subscription">
      <div class="subscription-card modern-card">
        <div class="card-header">
          <span>当前订阅</span>
          <span class="capsule-tag" :class="`capsule-tag--${getStatusType(currentSubscription.status)}`">
            {{ getStatusText(currentSubscription.status) }}
          </span>
        </div>

        <div class="subscription-info">
          <div class="plan-info">
            <h3>{{ getPlanName(currentSubscription.planType) }}</h3>
            <p class="price" v-if="currentSubscription.planType === 'TRIAL'">
              <span class="trial-price">免费试用</span>
              <span class="trial-period">(7天)</span>
            </p>
            <p class="price" v-else>¥{{ getPlanPrice(currentSubscription.planType) }}/月</p>
          </div>

          <div class="usage-info">
            <div class="usage-item">
              <span>已阅读文章：</span>
              <span>{{ completedArticles || 0 }}/{{ currentSubscription.maxArticlesPerMonth }}</span>
            </div>
            <div class="usage-item">
              <span>单篇字数：</span>
              <span>{{ currentSubscription.maxWordsPerArticle }}字</span>
            </div>
            <div class="usage-item">
              <span>完整AI功能：</span>
              <el-tag :type="currentSubscription.aiFeaturesEnabled ? 'success' : 'info'">
                {{ currentSubscription.aiFeaturesEnabled ? '已开启' : '未开启' }}
              </el-tag>
            </div>
          </div>

          <div class="subscription-actions">
            <!-- 试用用户的操作按钮 -->
            <template v-if="currentSubscription.planType === 'TRIAL'">
              <div class="trial-status-card">
                <div class="trial-status-header">
                  <el-icon class="trial-icon"><Clock /></el-icon>
                  <span class="trial-status-text">试用期剩余 {{ getTrialRemainingDays() }} 天</span>
                </div>
                <TactileButton
                  variant="primary"
                  @click="showUpgradeDialog = true"
                  class="unified-button"
                >
                  <el-icon><Star /></el-icon>
                  立即升级为正式会员
                </TactileButton>
              </div>
            </template>
            <!-- 正式用户的操作按钮 -->
            <template v-else>
            <TactileButton
              v-if="currentSubscription.planType !== 'ENTERPRISE'"
              variant="primary"
              @click="showUpgradeDialog = true"
              class="unified-button"
            >
              升级套餐
            </TactileButton>
            <TactileButton variant="danger" @click="cancelSubscription" class="unified-button">
              取消订阅
            </TactileButton>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- 免费用户状态 -->
    <div v-else-if="currentSubscription && currentSubscription.planType === 'FREE'" class="current-subscription">
      <div class="subscription-card modern-card">
        <div class="card-header">
          <span>当前状态</span>
          <span class="capsule-tag capsule-tag--info">免费用户</span>
        </div>

        <div class="subscription-info">
          <div class="plan-info">
            <h3>免费用户</h3>
            <p class="price">¥{{ getPlanPrice('FREE') }}/永久</p>
          </div>

          <div class="usage-info">
            <div class="usage-item">
              <span>已阅读文章：</span>
              <span>{{ completedArticles || 0 }}/{{ mergedSubscriptionPlans.find(p => p.type === 'FREE')?.maxArticles || 30 }}篇/月</span>
            </div>
            <div class="usage-item">
              <span>单篇字数：</span>
              <span>{{ mergedSubscriptionPlans.find(p => p.type === 'FREE')?.maxWords || 1500 }}字</span>
            </div>
            <div class="usage-item">
              <span>完整AI功能：</span>
              <el-tag type="info">未开启</el-tag>
            </div>
          </div>

          <div class="subscription-actions">
            <TactileButton variant="primary" @click="showUpgradeDialog = true" class="unified-button">
              升级为付费会员
            </TactileButton>
          </div>
        </div>
      </div>
    </div>

    <!-- 智能试用横幅 -->
    <div v-if="showTrialBanner" class="trial-banner-smart">
      <div class="trial-content">
        <div class="trial-info">
          <el-icon size="20" color="#007AFF">
            <Star />
          </el-icon>
          <span>免费试用专业版7天，体验完整AI功能</span>
        </div>
        <TactileButton size="sm" variant="promotion" @click="startTrial">
          立即试用
        </TactileButton>
      </div>
    </div>

    <!-- 套餐选择 - 移到顶部，突出显示 -->
    <div class="plans-section" id="subscription-plans">
      <h2 class="section-title">选择适合你的套餐</h2>
      <div class="plans-grid">
        <el-card
          v-for="plan in mergedSubscriptionPlans"
          :key="plan.type"
          :class="['plan-card', {
            'recommended': plan.recommended,
            'current-plan': isCurrentPlan(plan.type)
          }]"
        >
          <div v-if="plan.recommended" class="recommended-badge">推荐</div>
          <div v-if="isCurrentPlan(plan.type)" class="current-plan-badge">当前套餐</div>
          
          <!-- 智能推荐理由 -->
          <div v-if="plan.recommended" class="recommendation-reason">
            <el-icon size="16" color="#34C759">
              <TrendCharts />
            </el-icon>
            <span>{{ getRecommendationReason(plan.type) }}</span>
          </div>

          <div class="plan-header">
            <h3>{{ plan.name }}</h3>
            <!-- 促销标签 -->
            <div v-if="hasPromotion(plan.type)" class="promotion-badge">
              <span class="promotion-text">{{ getPromotionText(plan.type) }}</span>
            </div>
            <div class="plan-price">
                <span class="price">¥{{ getPlanPrice(plan.type) }}</span>
                <span class="period">/{{ plan.duration }}</span>
              </div>
          </div>

          <div class="plan-features">
            <!-- 动态显示所有特性 -->
            <div v-for="feature in plan.features" :key="feature" class="feature-item">
              <el-icon>
                <!-- 根据特性内容显示不同图标 -->
                <Document v-if="feature.includes('文章')" />
                <Edit v-else-if="feature.includes('字数')" />
                <MagicStick v-else-if="feature.includes('AI')" />
                <TrendCharts v-else-if="feature.includes('热点')" />
                <Search v-else-if="feature.includes('搜索')" />
                <Document v-else />
              </el-icon>
              <span>{{ feature }}</span>
            </div>
          </div>

          <!-- 套餐优势对比 -->
          <div v-if="plan.type !== 'FREE'" class="plan-advantages">
            <div class="advantage-item">
              <span class="advantage-icon">✨</span>
              <span class="advantage-text">{{ getPlanAdvantage(plan.type) }}</span>
            </div>
            <div class="advantage-item">
              <span class="advantage-icon">🚀</span>
              <span class="advantage-text">{{ getUpgradeBenefit(plan.type) }}</span>
            </div>
          </div>

          <div class="plan-action">
            <TactileButton
              variant="primary"
              :disabled="isCurrentPlan(plan.type)"
              @click="selectPlan(plan)"
              class="unified-button"
              block
            >
              {{ isCurrentPlan(plan.type) ? '当前套餐' : '选择套餐' }}
            </TactileButton>
          </div>
        </el-card>
      </div>
    </div>

    
    <!-- 加载状态 -->
    <div v-if="mergedSubscriptionPlans.length === 0" class="loading-state">
      <el-skeleton :rows="3" animated />
      <p style="text-align: center; margin-top: 16px; color: var(--text-secondary);">
        正在加载套餐数据...
      </p>
    </div>


    <!-- 移动端套餐卡片 -->
    <div v-if="isMobile && mergedSubscriptionPlans.length > 0" class="mobile-plans">
      <MobilePlanCard
        v-for="plan in mergedSubscriptionPlans"
        :key="`mobile-${plan.type}`"
        :plan="plan"
        :is-selected="isCurrentPlan(plan.type)"
        @select-plan="selectPlan"
      />
    </div>

    <!-- 使用情况统计 -->
    <div v-if="usageQuota" class="usage-section">
      <el-card>
        <template #header>
          <span>本月使用情况</span>
        </template>

        <div class="usage-stats">
          <div class="stat-item">
            <div class="stat-header">
              <span>文章阅读</span>
              <span>{{ completedArticles || 0 }}/{{ currentSubscription?.maxArticlesPerMonth || 0 }}</span>
            </div>
            <el-progress
                :percentage="currentSubscription?.maxArticlesPerMonth ? Math.min((completedArticles / currentSubscription.maxArticlesPerMonth) * 100, 100) : 0"
                :status="getProgressStatus(completedArticles / (currentSubscription?.maxArticlesPerMonth || 1))"
              />
          </div>

          <div class="stat-item" v-if="usageQuota.aiQuota?.enabled">
            <div class="stat-header">
              <span>AI功能</span>
              <span>{{ usageQuota.aiQuota?.unlimited ? '无限制' : `${usageQuota.aiQuota?.used || 0}/${usageQuota.aiQuota?.dailyLimit || 0}` }}</span>
            </div>
            <el-progress
              v-if="!usageQuota.aiQuota?.unlimited"
              :percentage="usageQuota.aiQuota?.dailyLimit ? Math.min(((usageQuota.aiQuota.used || 0) / usageQuota.aiQuota.dailyLimit) * 100, 100) : 0"
              :status="getProgressStatus((usageQuota.aiQuota?.used || 0) / (usageQuota.aiQuota?.dailyLimit || 1))"
            />
            <el-tag v-else type="success">无限制使用</el-tag>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 订阅历史 -->
    <div class="history-section">
      <el-card>
        <template #header>
          <span>订阅历史</span>
        </template>

        <el-table :data="subscriptionHistory" stripe>
          <el-table-column prop="planType" label="套餐类型">
            <template #default="{ row }">
              {{ getPlanName(row.planType) }}
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格">
            <template #default="{ row }">
              ¥{{ getPlanPrice(row.planType) }}
            </template>
          </el-table-column>
          <el-table-column prop="startDate" label="开始日期" />
          <el-table-column prop="endDate" label="结束日期" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="paymentMethod" label="支付方式">
            <template #default="{ row }">
              {{ getPaymentMethodName(row.paymentMethod) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 支付对话框 -->
    <el-dialog
      v-model="showPaymentDialog"
      title="选择支付方式"
      width="500px"
      @close="resetPaymentDialog"
    >
      <div class="payment-dialog">
          <div class="selected-plan">
            <h3>{{ selectedPlan?.name }}</h3>
            <div class="plan-price-section">
              <p class="plan-price">¥{{ selectedPlan ? getPlanPrice(selectedPlan.type) : 0 }}/{{ selectedPlan?.duration }}</p>
              <!-- 升级差价显示 -->
              <div v-if="selectedPlan?.type && shouldShowUpgradePrice(selectedPlan.type)" class="upgrade-price-info">
                <span class="upgrade-price-label">升级差价：</span>
                <span class="upgrade-price-amount">¥{{ getUpgradePrice(selectedPlan.type) }}</span>
              </div>
            </div>
          </div>

        <div class="payment-methods">
          <div
            v-for="method in paymentMethods"
            :key="method.type"
            :class="['payment-method', { 'selected': selectedPaymentMethod === method.type }]"
            @click="selectedPaymentMethod = method.type"
          >
            <div class="method-icon">
              <span v-if="method.type === 'CREDIT_CARD'" class="emoji-icon">💳</span>
              <span v-else-if="method.type === 'ALIPAY'" class="emoji-icon">💰</span>
              <span v-else-if="method.type === 'WECHAT'" class="emoji-icon">💚</span>
              <span v-else class="emoji-icon">💳</span>
            </div>
            <div class="method-info">
              <div class="method-name">{{ method.name }}</div>
              <div class="method-desc">{{ method.description }}</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <TactileButton variant="ghost" @click="showPaymentDialog = false">取消</TactileButton>
        <TactileButton
          variant="primary"
          :disabled="!selectedPaymentMethod"
          :loading="paymentLoading"
          @click="confirmPayment"
        >
          确认支付
        </TactileButton>
      </template>
    </el-dialog>

    <!-- 升级对话框 -->
    <el-dialog
      v-model="showUpgradeDialog"
      title="升级套餐"
      width="700px"
      @open="handleUpgradeDialogChange(true)"
    >
      <div class="upgrade-options">
        <div
          v-for="plan in availableUpgrades"
          :key="plan.type"
          class="upgrade-option"
          @click="selectUpgradePlan(plan)"
        >
          <div class="upgrade-info">
            <h4>{{ plan.name }}</h4>
            <div class="price-info">
              <p class="original-price">原价: ¥{{ getPlanPrice(plan.type) }}/{{ plan.duration }}</p>
              <p v-if="upgradePrices[plan.type]" class="upgrade-price">
                <span v-if="currentSubscription && currentSubscription.planType !== 'FREE'">
                  升级差价: ¥{{ upgradePrices[plan.type].upgradePrice }}/{{ plan.duration }}
                  <span v-if="upgradePrices[plan.type].remainingDays > 0" class="remaining-days">
                    (剩余{{ upgradePrices[plan.type].remainingDays }}天)
                  </span>
                </span>
                <span v-else>
                  升级价格: ¥{{ upgradePrices[plan.type].upgradePrice }}/{{ plan.duration }}
                </span>
              </p>
              <p v-else class="loading-price">计算价格中...</p>
            </div>
          </div>
          <div class="upgrade-benefits">
            <div v-for="feature in plan.features" :key="feature" class="benefit-item">
              • {{ feature }}
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Edit, MagicStick, Service, TrendCharts, Search, User, Star, Check, Close, QuestionFilled, ChatLineRound, Filter, Setting, CreditCard, Money, ChatDotRound, Clock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { subscriptionApi, reportApi } from '@/utils/api'
import type { Subscription, SubscriptionPlan, UsageQuota, PaymentMethod } from '@/types/subscription'
import type { ApiResponse } from '@/types/apiResponse'
import TactileButton from '@/components/common/TactileButton.vue'
import MobilePlanCard from '@/components/MobilePlanCard.vue'

const userStore = useUserStore()

// 响应式数据
const currentSubscription = ref<Subscription | null>(null)
const subscriptionHistory = ref<Subscription[]>([])
const usageQuota = ref<UsageQuota | null>(null)
const loading = ref(false)
// 新增：存储通过reportApi获取的阅读篇数
const completedArticles = ref(0)
// 新增：存储从后端获取的完整套餐配置信息
const backendPlanConfigs = ref<Record<string, Partial<SubscriptionPlan>>>({})
const backendConfigsLoaded = ref(false)

// 对话框状态
const showPaymentDialog = ref(false)
const showUpgradeDialog = ref(false)
const selectedPlan = ref<SubscriptionPlan | null>(null)
const selectedPaymentMethod = ref<string>('')
const paymentLoading = ref(false)

// 升级相关状态
const upgradePrices = ref<Record<string, {upgradePrice: number, remainingDays: number}>>({})
const upgradeLoading = ref(false)

// 初始化空的套餐配置数组，将完全依赖后端数据
const subscriptionPlans = ref<SubscriptionPlan[]>([])

// 支付方式
const paymentMethods = ref<PaymentMethod[]>([
  {
    type: 'ALIPAY',
    name: '支付宝',
    icon: 'Money',
    description: '使用支付宝快速支付'
  },
  {
    type: 'WECHAT',
    name: '微信支付',
    icon: 'ChatDotRound',
    description: '使用微信扫码支付'
  },
  {
    type: 'CREDIT_CARD',
    name: '信用卡',
    icon: 'CreditCard',
    description: '使用信用卡支付'
  }
])

// 计算属性
// 完全依赖后端数据
const mergedSubscriptionPlans = computed(() => {
  // 直接返回从后端加载的套餐配置
  return subscriptionPlans.value
})

// 根据套餐配置生成特性列表
const generateFeaturesForPlan = (plan: SubscriptionPlan): string[] => {
  const features: string[] = []
  features.push(`${plan.maxArticles}篇文章/月`)
  features.push(`${plan.maxWords}字/篇`)
  features.push(plan.aiFeatures ? 'AI智能翻译' : '基础翻译功能')

  if (plan.aiFeatures) {
    features.push('AI摘要分析')
    features.push('AI句子完整解析')
    features.push('AI助手对话')
  } else {
    features.push('生词本管理')
  }

  // 添加探索文章功能
  if (plan.type === 'FREE') {
    features.push('每日5次热点文章浏览')
    features.push('不可使用固定主题探索')
  } else if (plan.type === 'BASIC') {
    features.push('每日10次热点文章浏览')
    features.push('每日10次固定主题探索')
  } else {
    features.push('无限热点文章浏览')
    features.push('无限固定主题探索')
  }

  // 为PRO和ENTERPRISE会员添加自定义主题搜索功能
  if (plan.type === 'PRO' || plan.type === 'ENTERPRISE') {
    features.push('自定义主题文章探索')
    features.push('高级筛选功能')
    features.push('字体大小控制')
    features.push('行间翻译功能')
  }

  if (plan.prioritySupport) {
    features.push('优先使用AI')
  }

  // 为企业会员添加未来功能描述
  if (plan.type === 'ENTERPRISE') {
    features.push('未来功能免费更新')
    features.push('新功能优先体验')
  }

  return features
}

// 获取可升级的套餐列表
const availableUpgrades = computed(() => {
  if (!currentSubscription.value || currentSubscription.value.planType === 'FREE') {
    // 如果是免费用户或没有订阅，显示所有付费套餐
    return mergedSubscriptionPlans.value.filter(plan => plan.type !== 'FREE')
  }

  // 如果已经是最高级别（ENTERPRISE），不显示任何升级选项
  if (currentSubscription.value.planType === 'ENTERPRISE') {
    return []
  }

  const currentIndex = mergedSubscriptionPlans.value.findIndex(
    plan => plan.type === currentSubscription.value?.planType
  )
  // 确保使用mergedSubscriptionPlans而不是subscriptionPlans
  return mergedSubscriptionPlans.value.slice(currentIndex + 1)
})

// 工具方法 - 添加FREE套餐名称映射
const getPlanName = (planType: string) => {
  const planMap: Record<string, string> = {
    'FREE': '免费用户',
    'BASIC': '基础会员',
    'PRO': '专业会员',
    'ENTERPRISE': '企业会员',
    'TRIAL': '试用专业会员'
  }
  return planMap[planType] || planType
}

const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'ACTIVE': 'success',
    'CANCELLED': 'warning',
    'EXPIRED': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'ACTIVE': '激活中',
    'CANCELLED': '已取消',
    'EXPIRED': '已过期'
  }
  return statusMap[status] || status
}

const getPaymentMethodName = (method: string) => {
  const methodMap: Record<string, string> = {
    'ALIPAY': '支付宝',
    'WECHAT': '微信支付',
    'CREDIT_CARD': '信用卡',
    'TRIAL': '试用'
  }
  return methodMap[method] || method
}

// 完全使用后端数据获取价格
const getPlanPrice = (planType: string) => {
  // 试用专业会员免费
  if (planType === 'TRIAL') {
    return 0
  }
  // 从mergedSubscriptionPlans中查找对应的套餐价格
  const plan = mergedSubscriptionPlans.value.find(p => p.type === planType)
  return plan ? plan.price : 0
}

const getProgressStatus = (ratio: number) => {
  if (ratio >= 0.9) return 'exception'
  if (ratio >= 0.7) return 'warning'
  return 'success'
}

// 计算试用剩余天数
const getTrialRemainingDays = () => {
  if (!currentSubscription.value || currentSubscription.value.planType !== 'TRIAL') {
    return 0
  }
  
  const endDate = new Date(currentSubscription.value.endDate)
  const now = new Date()
  const diffTime = endDate.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  return Math.max(0, diffDays)
}

const isCurrentPlan = (planType: string) => {
  return currentSubscription.value?.planType === planType
}

// 判断是否应该显示升级差价
const shouldShowUpgradePrice = (planType: string) => {
  // 如果是当前套餐，不显示升级差价
  if (isCurrentPlan(planType)) {
    return false
  }
  
  // 如果是免费用户，不显示升级差价（显示原价）
  if (!currentSubscription.value || currentSubscription.value.planType === 'FREE') {
    return false
  }
  
  // 如果有升级差价数据，显示升级差价
  return upgradePrices.value[planType] !== undefined
}

// 获取升级差价
const getUpgradePrice = (planType: string) => {
  if (upgradePrices.value[planType]) {
    return upgradePrices.value[planType].upgradePrice
  }
  return 0
}

// 扩展：加载后端完整套餐配置信息
const loadBackendPlanPrices = async () => {
  try {
    const result = await subscriptionApi.getPlanPrices() as any

    if (result?.success && result?.data) {
      // 成功获取后端套餐配置信息
      // 转换后端返回的数据格式为完整的SubscriptionPlan对象
      const plans: SubscriptionPlan[] = []
      const configsMap: Record<string, Partial<SubscriptionPlan>> = {}

      // 处理result.data中的每个套餐
      Object.entries(result.data).forEach(([planType, planData]) => {
          if (typeof planData === 'object' && planData !== null) {
            const planKey = planType.toUpperCase()

            // 创建完整的套餐对象
            const plan: SubscriptionPlan = {
              type: planKey as 'FREE' | 'BASIC' | 'PRO' | 'ENTERPRISE',
              name: getPlanName(planKey),
              price: planData && ('priceCny' in planData || 'price' in planData)
                ? parseFloat(String('priceCny' in planData ? planData.priceCny : planData.price))
                : 0,
              currency: (planData as { currency?: string })?.currency || 'CNY',
              duration: planKey === 'FREE' ? '永久' : '月',
              maxArticles: planData && 'maxArticlesPerMonth' in planData
                ? parseInt(String(planData.maxArticlesPerMonth))
                : 0,
              maxWords: planData && 'maxWordsPerArticle' in planData
                ? parseInt(String(planData.maxWordsPerArticle))
                : 0,
              aiFeatures: planData && 'aiFeaturesEnabled' in planData
                ? Boolean(planData.aiFeaturesEnabled)
                : false,
              prioritySupport: planData && 'prioritySupport' in planData
                ? Boolean(planData.prioritySupport)
                : false,
              features: [] // 特性将通过generateFeaturesForPlan生成
            }

          // 生成特性列表
          plan.features = generateFeaturesForPlan(plan)

          // 标记PRO套餐为推荐
          if (planKey === 'PRO') {
            plan.recommended = true
          }

          plans.push(plan)
          configsMap[planKey] = plan
        }
      })

      // 按FREE、BASIC、PRO、ENTERPRISE顺序排序
      const planOrder = ['FREE', 'BASIC', 'PRO', 'ENTERPRISE']
      plans.sort((a, b) => {
        const indexA = planOrder.indexOf(a.type)
        const indexB = planOrder.indexOf(b.type)
        return (indexA === -1 ? 999 : indexA) - (indexB === -1 ? 999 : indexB)
      })

      subscriptionPlans.value = plans
      backendPlanConfigs.value = configsMap
      backendConfigsLoaded.value = true
    } else {
      console.warn('获取后端套餐配置信息失败或数据格式不正确:', result)
      backendConfigsLoaded.value = false
      // 如果后端数据不可用，创建默认的套餐配置
      subscriptionPlans.value = [
        {
          type: 'FREE',
          name: '免费用户',
          price: 0,
          currency: 'CNY',
          duration: '永久',
          maxArticles: 30,
          maxWords: 1500,
          aiFeatures: false,
          prioritySupport: false,
          features: generateFeaturesForPlan({
            type: 'FREE',
            name: '免费用户',
            price: 0,
            currency: 'CNY',
            duration: '永久',
            maxArticles: 30,
            maxWords: 1500,
            aiFeatures: false,
            prioritySupport: false,
            features: []
          })
        }
      ]
    }
  } catch (error) {
    console.error('调用getPlanPrices API失败:', error)
    // 失败时不设置backendConfigsLoaded，保留为false
    // 创建默认的套餐配置作为后备
    subscriptionPlans.value = [
      {
        type: 'FREE',
        name: '免费用户',
        price: 0,
        currency: 'CNY',
        duration: '永久',
        maxArticles: 30,
        maxWords: 1500,
        aiFeatures: false,
        prioritySupport: false,
        features: generateFeaturesForPlan({
          type: 'FREE',
          name: '免费用户',
          price: 0,
          currency: 'CNY',
          duration: '永久',
          maxArticles: 30,
          maxWords: 1500,
          aiFeatures: false,
          prioritySupport: false,
          features: []
        })
      }
    ]
  }
}

// 业务方法
const loadSubscriptionData = async () => {
  if (!userStore.userInfo?.id) {
    console.warn('加载订阅数据失败: 用户ID不存在')
    return
  }

  loading.value = true
  try {
    const userId = userStore.userInfo.id
    // 正在加载订阅数据

    // 确保userId是数字类型，以匹配后端Long类型参数
    const numericUserId = typeof userId === 'string' ? parseInt(userId, 10) : userId;
    if (isNaN(numericUserId)) {
      console.error('无效的用户ID:', userId)
      ElMessage.error('用户ID无效')
      loading.value = false
      return
    }

    // 并行加载数据 - 添加reportApi获取阅读篇数，与ReportPage.vue保持一致
    const [currentRes, historyRes, quotaRes, readingRes] = await Promise.allSettled([
      subscriptionApi.getCurrentSubscription(numericUserId) as Promise<any>,
      subscriptionApi.getSubscriptionHistory(numericUserId) as Promise<any>,
      subscriptionApi.getRemainingQuota(numericUserId) as Promise<any>,
      reportApi.getReadingTime(String(userId)) as Promise<any>
    ])

    // 处理当前订阅 - 提取response中的data字段
    if (currentRes.status === 'fulfilled') {
      if (currentRes.value) {
        // 成功获取当前订阅信息
        // 检查响应格式，如果包含success和data字段，则使用data
        // 如果没有订阅数据，创建一个免费用户订阅对象
        if (!currentRes.value || (typeof currentRes.value === 'object' &&
            'success' in currentRes.value &&
            'data' in currentRes.value &&
            (!currentRes.value.data || Object.keys(currentRes.value.data).length === 0))) {
          // 用户没有订阅，创建免费用户对象
          // 使用默认的免费用户配置，不依赖mergedSubscriptionPlans
          currentSubscription.value = {
            id: 0,
            userId: numericUserId,
            planType: 'FREE',
            price: 0,
            currency: 'CNY',
            status: 'ACTIVE',
            startDate: new Date().toISOString(),
            endDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString(), // 一年有效期
            paymentMethod: 'ALIPAY',
            maxArticlesPerMonth: 30, // 免费用户30篇/月
            maxWordsPerArticle: 1500, // 免费用户1500字/篇
            aiFeaturesEnabled: false,
            autoRenew: false
          } as Subscription
        } else if (typeof currentRes.value === 'object' && 'success' in currentRes.value && 'data' in currentRes.value) {
          currentSubscription.value = currentRes.value.data
        } else {
          currentSubscription.value = currentRes.value
        }
      } else {
        // 当前订阅数据为空
        currentSubscription.value = null
      }
    } else {
      console.error('获取当前订阅信息失败:', currentRes.reason)
      // 即使获取失败，也设置为null以避免页面错误
      currentSubscription.value = null
    }

    // 处理订阅历史 - 提取response中的data字段（必须是数组）
    if (historyRes.status === 'fulfilled') {
      if (historyRes.value) {
        // 成功获取订阅历史
        // 检查响应格式，如果包含success和data字段，则使用data
        if (typeof historyRes.value === 'object' && 'success' in historyRes.value && 'data' in historyRes.value) {
          subscriptionHistory.value = Array.isArray(historyRes.value.data) ? historyRes.value.data : []
        } else {
          subscriptionHistory.value = Array.isArray(historyRes.value) ? historyRes.value : []
        }
      } else {
        // 订阅历史数据为空
        subscriptionHistory.value = []
      }
    } else {
      console.error('获取订阅历史失败:', historyRes.reason)
      subscriptionHistory.value = []
    }

    // 处理使用额度 - 提取response中的data字段
    if (quotaRes.status === 'fulfilled') {
      if (quotaRes.value) {
        // 成功获取使用额度信息
        // 检查响应格式，如果包含success和data字段，则使用data
        if (typeof quotaRes.value === 'object' && 'success' in quotaRes.value && 'data' in quotaRes.value) {
          usageQuota.value = quotaRes.value.data
        } else {
          usageQuota.value = quotaRes.value
        }
      } else {
        // 使用额度数据为空
        usageQuota.value = null
      }
    } else {
      console.error('获取使用额度失败:', quotaRes.reason)
      usageQuota.value = null
    }

    // 处理阅读篇数数据 - 与ReportPage.vue保持一致的实现方式
    if (readingRes.status === 'fulfilled') {
      if (readingRes.value) {
        // 成功获取阅读篇数信息
        // 检查响应格式，如果包含success和data字段，则使用data
        const readingData = readingRes.value?.data || readingRes.value || {};
        // 从reading API获取完成文章数
        completedArticles.value = typeof readingData.totalArticles === 'number' ? readingData.totalArticles : 0;
      } else {
        // 阅读篇数数据为空
        completedArticles.value = 0
      }
    } else {
      console.error('获取阅读篇数失败:', readingRes.reason)
      completedArticles.value = 0
    }

  } catch (error: any) {
    console.error('加载订阅数据过程中发生错误:', error)
    if (error.response) {
      console.error('API响应错误状态:', error.response.status)
      console.error('API响应错误数据:', error.response.data)
      console.error('API请求URL:', error.response.config.url)
    }
    ElMessage.error('加载数据失败，请稍后重试。')
    // 设置默认值以避免页面错误
    currentSubscription.value = null
    subscriptionHistory.value = []
    usageQuota.value = null
    completedArticles.value = 0
  } finally {
    loading.value = false
  }
}

const selectPlan = (plan: SubscriptionPlan) => {
  selectedPlan.value = plan
  showPaymentDialog.value = true
  showUpgradeDialog.value = false
}

// 选择升级套餐
const selectUpgradePlan = (plan: SubscriptionPlan) => {
  selectedPlan.value = plan
  showPaymentDialog.value = true
  showUpgradeDialog.value = false
}

// 计算升级差价
const calculateUpgradePrices = async () => {
  if (!userStore.userInfo?.id) return
  
  upgradeLoading.value = true
  try {
    const userId = userStore.userInfo.id
    const numericUserId = typeof userId === 'string' ? parseInt(userId, 10) : userId
    
    // 为每个可升级的套餐计算差价
    const pricePromises = availableUpgrades.value.map(async (plan) => {
      try {
        const result = await subscriptionApi.calculateUpgradePrice(numericUserId, plan.type) as any
        if (result?.success && result?.data) {
          return {
            planType: plan.type,
            upgradePrice: parseFloat(result.data.upgradePrice),
            remainingDays: result.data.remainingDays
          }
        }
        return null
      } catch (error) {
        console.error(`计算${plan.type}升级差价失败:`, error)
        return null
      }
    })
    
    const results = await Promise.all(pricePromises)
    const priceMap: Record<string, {upgradePrice: number, remainingDays: number}> = {}
    
    results.forEach(result => {
      if (result) {
        priceMap[result.planType] = {
          upgradePrice: result.upgradePrice,
          remainingDays: result.remainingDays
        }
      }
    })
    
    upgradePrices.value = priceMap
  } catch (error) {
    console.error('计算升级差价失败:', error)
  } finally {
    upgradeLoading.value = false
  }
}

const confirmPayment = async () => {
  if (!selectedPlan.value || !selectedPaymentMethod.value || !userStore.userInfo?.id) return

  paymentLoading.value = true
  try {
    const userId = parseInt(userStore.userInfo.id)
    // 确保userId是有效数字
    if (isNaN(userId)) {
      console.warn('无效的用户ID:', userStore.userInfo.id)
      ElMessage.error('用户信息不完整，请重新登录')
      paymentLoading.value = false
      return
    }

    let result: any
    
    // 判断是升级还是新建订阅
    if (currentSubscription.value && currentSubscription.value.planType !== 'FREE') {
      // 升级订阅
      result = await subscriptionApi.upgradeSubscription(
      userId,
      selectedPlan.value.type,
      selectedPaymentMethod.value
    )
    } else {
      // 新建订阅（包括免费用户升级）
      result = await subscriptionApi.create(
        userId,
        selectedPlan.value.type,
        selectedPaymentMethod.value
      )
    }

    // 根据后端实际响应结构调整判断逻辑
    if (result?.success) {
      const message = currentSubscription.value && currentSubscription.value.planType !== 'FREE' 
        ? '升级成功！感谢您的支持。' 
        : '订阅成功！感谢您的支持。'
      ElMessage.success(message)
      await loadSubscriptionData()
      // 同时更新userStore中的订阅信息
      await userStore.fetchSubscription()
      resetPaymentDialog()
    } else {
      ElMessage.error(result?.message || '操作失败，请稍后重试。')
    }
  } catch (error: any) {
    console.error('支付失败:', error)
    const message = error.response?.data?.message || error.message || '支付失败，请稍后重试。'
    ElMessage.error(message)
  } finally {
    paymentLoading.value = false
  }
}

const cancelSubscription = async () => {
  if (!currentSubscription.value) return

  try {
    await ElMessageBox.confirm(
      '确定要取消当前订阅吗？取消后将在下个计费周期生效。',
      '确认取消',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '继续使用',
        type: 'warning',
      }
    )

    const result: any = await subscriptionApi.cancelSubscription(currentSubscription.value.id)
    // 根据后端实际响应结构调整判断逻辑
    if (result?.success) {
      ElMessage.success('订阅已取消，将在下个计费周期生效。')
      await loadSubscriptionData()
    } else {
      ElMessage.error(result?.message || '取消失败，请稍后重试。')
    }
  } catch (error: any) {
    // 用户取消操作或请求失败
    if (error.message !== 'cancel') {
      const message = error.response?.data?.message || error.message || '操作失败，请稍后重试。'
      ElMessage.error(message)
    }
  }
}

const resetPaymentDialog = () => {
  showPaymentDialog.value = false
  selectedPlan.value = null
  selectedPaymentMethod.value = ''
  paymentLoading.value = false
}

// 监听升级对话框显示状态
const handleUpgradeDialogChange = async (visible: boolean) => {
  if (visible) {
    // 对话框打开时计算升级差价
    await calculateUpgradePrices()
  }
}

// 新增：用户统计数据
const userStats = ref({
  monthlyArticles: 25,
  avgArticleLength: 2000,
  aiUsageRate: 30,
  activityLevel: '良好'
})

// 移除重复定义


// 新增：当前使用量
const currentUsage = computed(() => completedArticles.value || 0)
const maxUsage = computed(() => currentSubscription.value?.maxArticlesPerMonth || 30)

// 新增：用户等级
const userTier = computed(() => userStore.userTier)

// 移动端检测
const isMobile = ref(false)

// 试用状态
const isTrialActive = ref(false)
const hasUsedTrial = ref(false)

// 计算AI功能权限（包括试用权限）
const hasAIFeatures = computed(() => {
  // 如果有试用权限，也允许使用AI功能
  if (isTrialActive.value) {
    return true
  }
  
  // 原有逻辑
  return userStore.hasAIFeatures
})

// 试用横幅显示
const showTrialBanner = computed(() => {
  const isFreeUser = currentSubscription.value?.planType === 'FREE'
  const hasNotUsedTrial = !hasUsedTrial.value
  const notDismissed = !localStorage.getItem('trial_banner_dismissed')
  const shouldShow = isFreeUser && hasNotUsedTrial && notDismissed
  
  console.log('试用横幅显示计算:', {
    isFreeUser,
    hasNotUsedTrial,
    notDismissed,
    shouldShow,
    currentSubscription: currentSubscription.value?.planType,
    hasUsedTrial: hasUsedTrial.value,
    localStorageValue: localStorage.getItem('trial_banner_dismissed')
  })
  
  return shouldShow
})


// 套餐特点 - 根据实际配置的功能描述
const getPlanAdvantage = (planType: string) => {
  const advantageMap: Record<string, string> = {
    'BASIC': '更多文章阅读量，单篇字数翻倍，适合个人学习',
    'PRO': 'AI智能翻译+总结解析，专业学习工具，文章阅读量提升3倍',
    'ENTERPRISE': 'AI聊天问答+智能测验，企业级服务，文章阅读量提升3.3倍'
  }
  return advantageMap[planType] || '更多功能'
}

// 对比优势 - 根据实际数据的具体对比
const getUpgradeBenefit = (planType: string) => {
  const benefitMap: Record<string, string> = {
    'BASIC': '相比免费版：每月可读100篇文章，单篇字数从1500字提升到3000字',
    'PRO': '相比基础版：每月可读300篇文章，获得AI智能功能，学习效率大幅提升',
    'ENTERPRISE': '相比专业版：每月可读1000篇文章，单篇字数从5000字提升到20000字'
  }
  return benefitMap[planType] || '显著提升学习效率'
}

// 智能推荐理由
const getRecommendationReason = (planType: string) => {
  const reasonMap: Record<string, string> = {
    'PRO': '基于你的学习习惯推荐',
    'ENTERPRISE': '企业用户首选方案',
    'BASIC': '性价比最高的选择'
  }
  return reasonMap[planType] || '最受欢迎的选择'
}

// 促销策略
const hasPromotion = (planType: string) => {
  return planType === 'PRO' || planType === 'ENTERPRISE'
}

const getPromotionText = (planType: string) => {
  const promotionMap: Record<string, string> = {
    'PRO': '年付8折',
    'ENTERPRISE': '限时优惠'
  }
  return promotionMap[planType] || ''
}

// 试用功能
const startTrial = async () => {
  try {
    if (!userStore.userInfo?.id) {
      ElMessage.error('用户信息不存在')
      return
    }
    
    console.log('开始试用，用户ID:', userStore.userInfo.id)
    const result = await subscriptionApi.startTrial(userStore.userInfo.id) as any
    console.log('试用API响应:', result)
    
    if (result.success) {
      ElMessage.success(result.message || '试用已开始，享受7天专业版功能！')
      localStorage.setItem('trial_banner_dismissed', 'true')
      // 更新user store中的试用状态
      hasUsedTrial.value = true
      isTrialActive.value = true
      // 重新加载订阅数据
      await loadSubscriptionData()
    } else {
      ElMessage.error(result.message || '试用启动失败')
    }
  } catch (error) {
    console.error('试用启动失败:', error)
    ElMessage.error('试用启动失败，请稍后重试')
  }
}

// 新增：对比功能列表
const comparisonFeatures = ref([
  {
    key: 'maxArticles',
    name: '每月文章数',
    icon: 'Document',
    description: '每月可阅读的文章数量'
  },
  {
    key: 'maxWords',
    name: '单篇字数限制',
    icon: 'Edit',
    description: '每篇文章的最大字数限制'
  },
  {
    key: 'aiTranslation',
    name: 'AI智能翻译',
    icon: 'MagicStick',
    description: '使用AI进行智能翻译'
  },
  {
    key: 'aiSummary',
    name: 'AI摘要分析',
    icon: 'Document',
    description: 'AI自动生成文章摘要'
  },
  {
    key: 'aiParse',
    name: 'AI句子解析',
    icon: 'Edit',
    description: 'AI解析句子结构和语法'
  },
  {
    key: 'aiAssistant',
    name: 'AI助手对话',
    icon: 'ChatLineRound',
    description: '与AI助手进行学习对话'
  },
  {
    key: 'trendingArticles',
    name: '热点文章浏览',
    icon: 'TrendCharts',
    description: '浏览热门文章'
  },
  {
    key: 'categorySearch',
    name: '主题文章探索',
    icon: 'Search',
    description: '按主题搜索文章'
  },
  {
    key: 'customSearch',
    name: '自定义主题搜索',
    icon: 'Search',
    description: '自定义关键词搜索文章'
  },
  {
    key: 'advancedFilter',
    name: '高级筛选功能',
    icon: 'Filter',
    description: '按语言、国家、时间等筛选'
  },
  {
    key: 'fontControl',
    name: '字体大小控制',
    icon: 'Setting',
    description: '自定义文章字体大小'
  },
  {
    key: 'inlineTranslation',
    name: '行间翻译功能',
    icon: 'Document',
    description: '在原文下方显示翻译'
  },
  {
    key: 'prioritySupport',
    name: '优先使用AI',
    icon: 'Star',
    description: '优先获得AI服务响应'
  }
])

// 事件处理方法

// 新增：窗口大小变化处理
const handleResize = () => {
  isMobile.value = window.innerWidth <= 768
}

// 生命周期
onMounted(async () => {
  // 并行加载订阅数据和套餐价格配置
  await Promise.all([
    loadSubscriptionData(),
    loadBackendPlanPrices()
  ])

  // 检查试用状态
  try {
    if (userStore.userInfo?.id) {
      console.log('检查试用状态，用户ID:', userStore.userInfo.id)
      const trialStatus = await subscriptionApi.checkTrialStatus(userStore.userInfo.id) as any
      console.log('试用状态API响应:', trialStatus)
      if (trialStatus.success) {
        // 更新user store中的试用状态
        hasUsedTrial.value = trialStatus.hasUsedTrial
        isTrialActive.value = trialStatus.isTrialActive
        console.log('试用状态更新:', { hasUsedTrial: hasUsedTrial.value, isTrialActive: isTrialActive.value })
        
        // 如果用户没有使用过试用，清除dismissed标记，允许重新显示横幅
        if (!trialStatus.hasUsedTrial) {
          localStorage.removeItem('trial_banner_dismissed')
          console.log('已清除试用横幅dismissed标记，允许重新显示')
        }
      } else {
        console.log('试用状态检查失败:', trialStatus.message)
      }
    } else {
      console.log('用户ID不存在，无法检查试用状态')
    }
  } catch (error) {
    console.error('检查试用状态失败:', error)
  }

  // 检查URL参数，如果有showUpgrade=true则自动显示升级对话框
  const urlParams = new URLSearchParams(window.location.search)
  if (urlParams.get('showUpgrade') === 'true') {
    showUpgradeDialog.value = true
  }

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
  handleResize() // 初始化
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
@import '@/assets/design-system.css';

.subscription-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6);
  animation: fadeIn 0.3s ease-out;
  background: var(--bg-secondary);
  border-radius: var(--radius-2xl);
  position: relative;
  min-height: 100vh;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.page-header {
  text-align: center;
  margin-bottom: var(--space-16);
  position: relative;
  padding: var(--space-8) var(--space-6);
  background: var(--bg-primary);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-3xl);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all 0.2s ease;
  overflow: hidden;
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

.page-header::after {
  content: '';
  position: absolute;
  bottom: -var(--space-3);
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: var(--ios-blue);
  border-radius: var(--radius-sm);
}

.page-header:hover {
  transform: translateY(-2px);
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.15),
    0 4px 12px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
  border-color: rgba(0, 122, 255, 0.2);
}

.page-header h1 {
  font-size: var(--text-4xl);
  margin-bottom: var(--space-4);
  color: var(--text-primary);
  font-weight: var(--font-weight-bold);
  letter-spacing: -0.01em;
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-header p {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto;
  line-height: var(--line-height-relaxed);
  position: relative;
  z-index: 2;
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

.current-subscription {
  margin-bottom: var(--space-12);
}

.subscription-card {
  background: var(--bg-primary);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-3xl);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.08),
    0 1px 4px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.7),
    inset 0 -1px 0 rgba(0, 0, 0, 0.03);
  border: 2px solid rgba(255, 255, 255, 0.4);
  transition: all 0.2s ease;
  overflow: hidden;
  position: relative;
}

.subscription-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--ios-blue);
  opacity: 0.9;
}

.subscription-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.2s ease;
}

.subscription-card:hover::after {
  left: 100%;
}

.subscription-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow:
    0 16px 48px rgba(0, 0, 0, 0.18),
    0 4px 16px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    inset 0 -1px 0 rgba(0, 0, 0, 0.05);
  border-color: rgba(0, 122, 255, 0.3);
}

.subscription-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-6);
  background: linear-gradient(135deg,
    rgba(255, 255, 255, 0.9) 0%,
    rgba(248, 250, 252, 0.8) 100%);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.subscription-info {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: var(--space-8);
  align-items: center;
  padding: var(--space-8);
}

.plan-info h3 {
  margin: 0 0 var(--space-2) 0;
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.price {
  font-size: var(--text-xl);
  color: var(--primary-600);
  font-weight: var(--font-weight-bold);
}

.usage-info {
  background: linear-gradient(135deg,
    rgba(248, 250, 252, 0.8) 0%,
    rgba(241, 245, 249, 0.6) 100%);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  padding: var(--space-6);
  border-radius: var(--radius-2xl);
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.6),
    0 2px 8px rgba(0, 0, 0, 0.04);
}

.usage-info .usage-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-3);
  padding: var(--space-2) 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.usage-info .usage-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.subscription-actions {
  display: flex;
  gap: var(--space-4);
  justify-content: flex-end;
}

.plans-section {
  margin-bottom: var(--space-16);
}

/* 使用统一的section-title样式 */

.plans-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-6);
  margin-top: var(--space-12);
}

/* 大屏幕优化 */
@media (min-width: 1400px) {
  .subscription-container {
    max-width: 1400px;
  }
  
  .plans-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: var(--space-8);
  }
}

/* 桌面端 */
@media (max-width: 1200px) {
  .plans-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-6);
  }
}

/* 平板端 */
@media (max-width: 1024px) {
  .subscription-container {
    padding: var(--space-4);
  }
  
  .plans-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-4);
  }
  
  .recommended-plan .plan-card {
    max-width: 400px;
    transform: scale(1.05);
  }
}

/* 移动端 */
@media (max-width: 768px) {
  .plans-grid {
    grid-template-columns: 1fr;
    gap: var(--space-4);
  }
  
  .recommended-plan .plan-card {
    max-width: 100%;
    transform: scale(1.02);
  }
}

.plan-card {
  position: relative;
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.08),
    0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;
  overflow: hidden;
  cursor: pointer;
}

.plan-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--ios-blue);
  opacity: 0;
  transition: opacity 0.2s ease;
}

/* 移除过度的装饰效果 */

.plan-card:hover {
  transform: translateY(-4px);
  box-shadow:
    0 12px 32px rgba(0, 0, 0, 0.15),
    0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(0, 122, 255, 0.3);
}

.plan-card:hover::before {
  opacity: 1;
}

.plan-card.recommended {
  border: 3px solid var(--ios-blue);
  box-shadow:
    0 20px 60px rgba(0, 122, 255, 0.3),
    0 8px 24px rgba(0, 122, 255, 0.2),
    0 4px 12px rgba(0, 122, 255, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transform: scale(1.05);
  position: relative;
  z-index: 2;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%, 
    rgba(248, 250, 252, 0.9) 100%);
}

.plan-card.recommended::before {
  opacity: 1;
}

.plan-card.current-plan {
  border-color: var(--ios-blue);
  box-shadow:
    0 8px 32px rgba(0, 122, 255, 0.25),
    0 2px 8px rgba(0, 122, 255, 0.15),
    0 1px 4px rgba(0, 122, 255, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.7),
    inset 0 -1px 0 rgba(0, 122, 255, 0.2);
  position: relative;
}



.plan-card.current-plan::after {
  background: linear-gradient(90deg, transparent, rgba(0, 122, 255, 0.3), transparent);
}

.plan-card.current-plan:hover {
  border-color: var(--ios-blue);
  box-shadow:
    0 16px 48px rgba(0, 122, 255, 0.3),
    0 4px 16px rgba(0, 122, 255, 0.2),
    0 2px 8px rgba(0, 122, 255, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    inset 0 -1px 0 rgba(0, 122, 255, 0.25);
}

.recommended-badge {
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
  background: linear-gradient(135deg, var(--ios-blue) 0%, #5AC8FA 100%);
  color: var(--text-inverse);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-weight-semibold);
  box-shadow:
    0 4px 12px rgba(0, 122, 255, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  z-index: 2;
  transition: all 0.2s ease;
}

.recommended-badge:hover {
  transform: scale(1.05);
  box-shadow:
    0 6px 16px rgba(0, 122, 255, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.current-plan-badge {
  position: absolute;
  top: var(--space-4);
  left: var(--space-4);
  background: linear-gradient(135deg, var(--ios-green) 0%, #34C759 100%);
  color: var(--text-inverse);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-weight-semibold);
  box-shadow:
    0 4px 12px rgba(52, 199, 89, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  z-index: 2;
  transition: all 0.2s ease;
}

.current-plan-badge:hover {
  transform: scale(1.05);
  box-shadow:
    0 6px 16px rgba(52, 199, 89, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.plan-header {
  text-align: center;
  padding: var(--space-8) var(--space-6) var(--space-6);
  background: var(--bg-secondary);
  position: relative;
  overflow: hidden;
}

.plan-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.02) 0%, rgba(90, 200, 250, 0.01) 50%, rgba(0, 122, 255, 0.02) 100%);
  pointer-events: none;
  animation: liquidFlow 35s ease-in-out infinite;
}

.plan-header h3 {
  margin: 0 0 var(--space-3) 0;
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  position: relative;
  z-index: 2;
}

.plan-price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: var(--space-1);
  position: relative;
  z-index: 2;
}

.plan-price .price {
  font-size: var(--text-4xl);
  font-weight: var(--font-weight-bold);
  color: var(--ios-blue);
  text-shadow: 0 2px 4px rgba(0, 122, 255, 0.2);
}

.plan-price .duration {
  color: var(--text-secondary);
  font-size: var(--text-lg);
}

.plan-features {
  padding: var(--space-6);
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-4);
  color: var(--text-secondary);
  font-size: var(--text-sm);
  transition: color 0.2s ease;
}

.feature-item:hover {
  color: var(--text-primary);
}

.feature-item .el-icon {
  margin-right: var(--space-3);
  color: var(--primary-500);
  font-size: var(--text-lg);
}

.plan-action {
  padding: var(--space-6);
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-light);
}

.usage-section,
.history-section {
  margin-bottom: var(--space-8);
}

.usage-section .el-card {
  background: var(--bg-primary);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-2xl);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.08),
    0 1px 4px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.7),
    inset 0 -1px 0 rgba(0, 0, 0, 0.03);
  border: 2px solid rgba(255, 255, 255, 0.4);
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.usage-section .el-card::before {
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

.usage-section .el-card:hover {
  transform: translateY(-2px);
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.15),
    0 4px 12px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border-color: rgba(0, 122, 255, 0.2);
}

.usage-stats .stat-item {
  margin-bottom: var(--space-6);
  position: relative;
  z-index: 2;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--space-3);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.payment-dialog .selected-plan {
  text-align: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.payment-methods .payment-method {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.payment-method:hover {
  border-color: #409eff;
}

.payment-method.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.emoji-icon {
  font-size: 24px;
  line-height: 1;
}

.method-icon {
  font-size: 2em;
  margin-right: 15px;
}

.method-info .method-name {
  font-weight: bold;
  margin-bottom: 3px;
}

.method-info .method-desc {
  color: #666;
  font-size: 0.9em;
}

.upgrade-options .upgrade-option {
  display: flex;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--bg-primary);
}

.upgrade-option:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.upgrade-info {
  flex: 0 0 250px;
  margin-right: 20px;
}

.upgrade-info h4 {
  margin: 0 0 10px 0;
  font-size: 1.2em;
  color: var(--text-primary);
}

.price-info {
  margin-bottom: 10px;
}

.original-price {
  margin: 0 0 5px 0;
  color: var(--text-secondary);
  font-size: 0.9em;
  text-decoration: line-through;
}

.upgrade-price {
  margin: 0;
  color: var(--ios-blue);
  font-weight: var(--font-weight-semibold);
  font-size: 1.1em;
}

.remaining-days {
  color: var(--text-secondary);
  font-size: 0.8em;
  font-weight: normal;
}

/* 支付对话框中的升级差价样式 */
.plan-price-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.upgrade-price-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.upgrade-price-label {
  color: var(--text-secondary);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
}

.upgrade-price-amount {
  color: var(--ios-blue);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-semibold);
}

.loading-price {
  margin: 0;
  color: var(--text-secondary);
  font-style: italic;
}

.upgrade-benefits {
  flex: 1;
}

.benefit-item {
  color: var(--text-secondary);
  margin-bottom: 5px;
  font-size: 0.9em;
}

.unified-button {
  padding: var(--space-4) var(--space-8);
  border-radius: var(--radius-xl);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  min-width: 140px;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
  background: var(--primary-500);
  color: var(--text-inverse);
  border: none;
  box-shadow: var(--shadow-primary);
}

.unified-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.2s ease;
}

.unified-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg), var(--shadow-primary);
}

.unified-button:hover:not(:disabled)::before {
  left: 100%;
}

.unified-button:active:not(:disabled) {
  transform: translateY(0);
}

.unified-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 试用状态样式 */
.trial-price {
  color: var(--ios-green);
  font-weight: 600;
  font-size: 1.2em;
}

.trial-period {
  color: var(--text-secondary);
  font-size: 0.9em;
  margin-left: 8px;
}

.trial-status-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  width: 100%;
}

.trial-status-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.trial-icon {
  color: var(--primary-500);
  font-size: 16px;
}

.trial-status-text {
  color: var(--text-primary);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
}

/* 移动端优化 */
@media (max-width: 768px) {
  .subscription-info {
    grid-template-columns: 1fr;
    text-align: center;
    gap: var(--space-4);
  }

  .subscription-actions {
    justify-content: center;
    gap: var(--space-4);
  }

  .upgrade-option {
    flex-direction: column;
    padding: var(--space-4);
    min-height: 60px; /* 确保触摸目标足够大 */
  }

  .upgrade-info {
    flex: none;
    margin-right: 0;
    margin-bottom: var(--space-4);
    text-align: center;
  }

  .unified-button {
    min-width: 100%;
    min-height: 44px; /* iOS推荐的最小触摸目标 */
    padding: var(--space-3) var(--space-6);
    font-size: var(--text-base);
  }
  
  .plan-card {
    min-height: 200px; /* 确保卡片有足够的触摸区域 */
  }
  
  .plan-action .unified-button {
    min-height: 48px;
    font-size: var(--text-base);
  }
  
  .trial-status-card {
    gap: var(--space-3);
  }
  
  .trial-status-header {
    padding: var(--space-2) var(--space-3);
    font-size: var(--text-xs);
  }
  
  .trial-icon {
    font-size: 14px;
  }
}

/* 推荐套餐区域 */
.recommended-section {
  margin: var(--space-8) 0;
}

.section-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  text-align: center;
  margin-bottom: var(--space-6);
}

.recommended-plan {
  display: flex;
  justify-content: center;
  margin-bottom: var(--space-8);
}

.recommended-plan .plan-card {
  max-width: 450px;
  width: 100%;
  transform: scale(1.08);
  border: 4px solid var(--ios-blue);
  box-shadow: 
    0 25px 80px rgba(0, 122, 255, 0.4),
    0 8px 24px rgba(0, 122, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  position: relative;
  z-index: 2;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%, 
    rgba(248, 250, 252, 0.9) 100%);
}

.recommended-plan .plan-header {
  text-align: center;
  margin-bottom: var(--space-4);
  position: relative;
}

.recommended-plan .plan-header h3 {
  font-size: var(--text-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.recommended-plan .price-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: var(--space-1);
  margin-bottom: var(--space-4);
}

.recommended-plan .price {
  font-size: var(--text-4xl);
  font-weight: var(--font-weight-bold);
  color: var(--ios-blue);
}

.recommended-plan .period {
  font-size: var(--text-lg);
  color: var(--text-secondary);
}

.recommended-plan .recommended-badge {
  position: absolute;
  top: -var(--space-2);
  right: -var(--space-2);
  background: linear-gradient(135deg, var(--ios-green) 0%, #30D158 100%);
  color: white;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-semibold);
  box-shadow: 0 4px 12px rgba(52, 199, 89, 0.3);
}


/* 智能试用横幅 */
.trial-banner-smart {
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  border-radius: var(--radius-xl);
  padding: var(--space-4);
  margin: var(--space-6) 0;
  box-shadow: 0 4px 16px rgba(0, 122, 255, 0.2);
}

.trial-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

.trial-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: white;
  font-weight: var(--font-weight-medium);
}

/* 推荐理由 */
.recommendation-reason {
  position: absolute;
  top: var(--space-2);
  left: var(--space-2);
  background: rgba(52, 199, 89, 0.1);
  color: var(--ios-green);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  font-size: var(--text-xs);
  display: flex;
  align-items: center;
  gap: var(--space-1);
  z-index: 3;
}

/* 促销标签 */
.promotion-badge {
  display: inline-block;
  background: linear-gradient(135deg, #FF9500 0%, #FF6B6B 100%);
  color: white;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-weight-semibold);
  box-shadow: 0 2px 8px rgba(255, 149, 0, 0.3);
  margin-bottom: var(--space-2);
  align-self: center;
}

/* 套餐优势对比 */
.plan-advantages {
  margin: var(--space-3) 0;
  padding: var(--space-3);
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.08) 0%, rgba(90, 200, 250, 0.05) 100%);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(0, 122, 255, 0.15);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.advantage-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-primary);
  padding: var(--space-3);
  background: rgba(255, 255, 255, 0.8);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(0, 122, 255, 0.15);
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.05);
}

.advantage-item:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.1);
}

.advantage-icon {
  font-size: var(--text-lg);
  line-height: 1;
  flex-shrink: 0;
  margin-top: 2px;
}

.advantage-text {
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  line-height: 1.5;
  flex: 1;
  font-size: var(--text-sm);
}

/* 移动端优化 */
@media (max-width: 768px) {
  .trial-content {
    flex-direction: column;
    text-align: center;
    gap: var(--space-3);
  }
  
  .plan-advantages {
    margin: var(--space-2) 0;
    padding: var(--space-2);
    gap: var(--space-1);
  }
  
  .advantage-item {
    padding: var(--space-2);
    font-size: var(--text-xs);
    gap: var(--space-2);
  }
  
  .advantage-text {
    font-size: var(--text-xs);
    line-height: 1.4;
  }
  
  .recommendation-reason {
    position: static;
    margin-bottom: var(--space-2);
    justify-content: center;
  }
}


/* 移动端套餐卡片 */
.mobile-plans {
  display: none;
}

@media (max-width: 768px) {
  .mobile-plans {
    display: block;
  }
  
  .plans-grid {
    display: none;
  }
}
</style>

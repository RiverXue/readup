<template>
  <div class="subscription-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>💎 会员订阅</h1>
      <p>升级会员，解锁更多AI功能，享受更好的学习体验。</p>
    </div>

    <!-- 当前订阅状态 -->
    <div v-if="currentSubscription && currentSubscription.planType !== 'FREE'" class="current-subscription">
      <el-card class="subscription-card">
        <template #header>
          <div class="card-header">
            <span>当前订阅</span>
            <el-tag :type="getStatusType(currentSubscription.status)">
              {{ getStatusText(currentSubscription.status) }}
            </el-tag>
          </div>
        </template>

        <div class="subscription-info">
          <div class="plan-info">
            <h3>{{ getPlanName(currentSubscription.planType) }}</h3>
            <p class="price">¥{{ getPlanPrice(currentSubscription.planType) }}/月</p>
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
            <el-button 
              v-if="currentSubscription.planType !== 'ENTERPRISE'" 
              type="primary" 
              @click="showUpgradeDialog = true" 
              class="unified-button"
            >
              升级套餐
            </el-button>
            <el-button type="danger" plain @click="cancelSubscription" class="unified-button">
              取消订阅
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 免费用户状态 -->
    <div v-else-if="currentSubscription && currentSubscription.planType === 'FREE'" class="current-subscription">
      <el-card class="subscription-card">
        <template #header>
          <div class="card-header">
            <span>当前状态</span>
            <el-tag type="info">免费用户</el-tag>
          </div>
        </template>

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
              <span>{{ mergedSubscriptionPlans.find(p => p.type === 'FREE')?.maxWords || 500 }}字</span>
            </div>
            <div class="usage-item">
              <span>完整AI功能：</span>
              <el-tag type="info">未开启</el-tag>
            </div>
          </div>

          <div class="subscription-actions">
            <el-button type="primary" @click="showUpgradeDialog = true" class="unified-button">
              升级为付费会员
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 套餐选择 -->
    <div class="plans-section">
      <h2>选择适合你的套餐</h2>
      <div class="plans-grid">
        <el-card
          v-for="plan in mergedSubscriptionPlans"
          :key="plan.type"
          :class="['plan-card', { 'recommended': plan.recommended }]"
        >
          <div v-if="plan.recommended" class="recommended-badge">推荐</div>

          <div class="plan-header">
            <h3>{{ plan.name }}</h3>
            <div class="plan-price">
                <span class="price">¥{{ getPlanPrice(plan.type) }}</span>
                <span class="duration">/{{ plan.duration }}</span>
              </div>
          </div>

          <div class="plan-features">
            <!-- 动态显示所有特性 -->
            <div v-for="feature in plan.features" :key="feature" class="feature-item">
              <el-icon>
                <!-- 根据特性内容显示不同图标 -->
                <Document v-if="feature.includes('文章')" />
                <Edit v-else-if="feature.includes('字')" />
                <MagicStick v-else-if="feature.includes('AI')" />
                <Service v-else-if="feature.includes('客服')" />
                <TrendCharts v-else-if="feature.includes('热点')" />
                <Search v-else-if="feature.includes('搜索')" />
                <Document v-else />
              </el-icon>
              <span>{{ feature }}</span>
            </div>
          </div>

          <div class="plan-action">
            <el-button
              type="primary"
              :disabled="isCurrentPlan(plan.type)"
              @click="selectPlan(plan)"
              class="unified-button"
              block
            >
              {{ isCurrentPlan(plan.type) ? '当前套餐' : '选择套餐' }}
            </el-button>
          </div>
        </el-card>
      </div>
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
                :percentage="currentSubscription?.maxArticlesPerMonth ? (completedArticles / currentSubscription.maxArticlesPerMonth) * 100 : 0"
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
              :percentage="usageQuota.aiQuota?.dailyLimit ? ((usageQuota.aiQuota.used || 0) / usageQuota.aiQuota.dailyLimit) * 100 : 0"
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
          <p class="plan-price">¥{{ selectedPlan ? getPlanPrice(selectedPlan.type) : 0 }}/{{ selectedPlan?.duration }}</p>
        </div>

        <div class="payment-methods">
          <div
            v-for="method in paymentMethods"
            :key="method.type"
            :class="['payment-method', { 'selected': selectedPaymentMethod === method.type }]"
            @click="selectedPaymentMethod = method.type"
          >
            <div class="method-icon">{{ method.icon }}</div>
            <div class="method-info">
              <div class="method-name">{{ method.name }}</div>
              <div class="method-desc">{{ method.description }}</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showPaymentDialog = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="!selectedPaymentMethod"
          :loading="paymentLoading"
          @click="confirmPayment"
        >
          确认支付
        </el-button>
      </template>
    </el-dialog>

    <!-- 升级对话框 -->
    <el-dialog
      v-model="showUpgradeDialog"
      title="升级套餐"
      width="600px"
    >
      <div class="upgrade-options">
        <div
          v-for="plan in availableUpgrades"
          :key="plan.type"
          class="upgrade-option"
          @click="selectPlan(plan)"
        >
          <div class="upgrade-info">
            <h4>{{ plan.name }}</h4>
            <p>¥{{ getPlanPrice(plan.type) }}/{{ plan.duration }}</p>
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Edit, MagicStick, Service, TrendCharts, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { subscriptionApi, reportApi } from '@/utils/api'
import type { Subscription, SubscriptionPlan, UsageQuota, PaymentMethod } from '@/types/subscription'
import type { ApiResponse } from '@/types/apiResponse'

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

// 初始化空的套餐配置数组，将完全依赖后端数据
const subscriptionPlans = ref<SubscriptionPlan[]>([])

// 支付方式
const paymentMethods = ref<PaymentMethod[]>([
  {
    type: 'ALIPAY',
    name: '支付宝',
    icon: '💰',
    description: '使用支付宝快速支付'
  },
  {
    type: 'WECHAT',
    name: '微信支付',
    icon: '💚',
    description: '使用微信扫码支付'
  },
  {
    type: 'CREDIT_CARD',
    name: '信用卡',
    icon: '💳',
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
  }

  if (plan.prioritySupport) {
    features.push('优先使用AI')
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
    'ENTERPRISE': '企业会员'
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
    'CREDIT_CARD': '信用卡'
  }
  return methodMap[method] || method
}

// 完全使用后端数据获取价格
const getPlanPrice = (planType: string) => {
  // 从mergedSubscriptionPlans中查找对应的套餐价格
  const plan = mergedSubscriptionPlans.value.find(p => p.type === planType)
  return plan ? plan.price : 0
}

const getProgressStatus = (ratio: number) => {
  if (ratio >= 0.9) return 'exception'
  if (ratio >= 0.7) return 'warning'
  return 'success'
}

const isCurrentPlan = (planType: string) => {
  return currentSubscription.value?.planType === planType
}

// 扩展：加载后端完整套餐配置信息
const loadBackendPlanPrices = async () => {
  try {
    const result = await subscriptionApi.getPlanPrices() as any

    if (result?.success && result?.data) {
      console.log('成功获取后端套餐配置信息:', result.data)
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
          maxWords: 500,
          aiFeatures: false,
          prioritySupport: false,
          features: generateFeaturesForPlan({
            type: 'FREE',
            name: '免费用户',
            price: 0,
            currency: 'CNY',
            duration: '永久',
            maxArticles: 30,
            maxWords: 500,
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
        maxWords: 500,
        aiFeatures: false,
        prioritySupport: false,
        features: generateFeaturesForPlan({
          type: 'FREE',
          name: '免费用户',
          price: 0,
          currency: 'CNY',
          duration: '永久',
          maxArticles: 30,
          maxWords: 500,
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
    console.log('正在加载订阅数据，用户ID:', userId, '类型:', typeof userId)

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
        console.log('成功获取当前订阅信息:', currentRes.value)
        // 检查响应格式，如果包含success和data字段，则使用data
        // 如果没有订阅数据，创建一个免费用户订阅对象
        if (!currentRes.value || (typeof currentRes.value === 'object' &&
            'success' in currentRes.value &&
            'data' in currentRes.value &&
            (!currentRes.value.data || Object.keys(currentRes.value.data).length === 0))) {
          // 用户没有订阅，创建免费用户对象
          // 从mergedSubscriptionPlans中获取FREE套餐的配置，如果不存在则使用安全默认值
          const freePlan = mergedSubscriptionPlans.value.find(p => p.type === 'FREE');
          currentSubscription.value = {
            id: 0,
            userId: numericUserId,
            planType: 'FREE',
            price: freePlan?.price || 0,
            currency: freePlan?.currency || 'CNY',
            status: 'ACTIVE',
            startDate: new Date().toISOString(),
            endDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString(), // 一年有效期
            paymentMethod: 'ALIPAY',
            maxArticlesPerMonth: freePlan?.maxArticles || 30, // 默认使用30篇作为后备值
            maxWordsPerArticle: freePlan?.maxWords || 500,
            aiFeaturesEnabled: freePlan?.aiFeatures || false,
            autoRenew: false
          } as Subscription
        } else if (typeof currentRes.value === 'object' && 'success' in currentRes.value && 'data' in currentRes.value) {
          currentSubscription.value = currentRes.value.data
        } else {
          currentSubscription.value = currentRes.value
        }
      } else {
        console.log('当前订阅数据为空')
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
        console.log('成功获取订阅历史:', historyRes.value)
        // 检查响应格式，如果包含success和data字段，则使用data
        if (typeof historyRes.value === 'object' && 'success' in historyRes.value && 'data' in historyRes.value) {
          subscriptionHistory.value = Array.isArray(historyRes.value.data) ? historyRes.value.data : []
        } else {
          subscriptionHistory.value = Array.isArray(historyRes.value) ? historyRes.value : []
        }
      } else {
        console.log('订阅历史数据为空')
        subscriptionHistory.value = []
      }
    } else {
      console.error('获取订阅历史失败:', historyRes.reason)
      subscriptionHistory.value = []
    }

    // 处理使用额度 - 提取response中的data字段
    if (quotaRes.status === 'fulfilled') {
      if (quotaRes.value) {
        console.log('成功获取使用额度信息:', quotaRes.value)
        // 检查响应格式，如果包含success和data字段，则使用data
        if (typeof quotaRes.value === 'object' && 'success' in quotaRes.value && 'data' in quotaRes.value) {
          usageQuota.value = quotaRes.value.data
        } else {
          usageQuota.value = quotaRes.value
        }
      } else {
        console.log('使用额度数据为空')
        usageQuota.value = null
      }
    } else {
      console.error('获取使用额度失败:', quotaRes.reason)
      usageQuota.value = null
    }

    // 处理阅读篇数数据 - 与ReportPage.vue保持一致的实现方式
    if (readingRes.status === 'fulfilled') {
      if (readingRes.value) {
        console.log('成功获取阅读篇数信息:', readingRes.value)
        // 检查响应格式，如果包含success和data字段，则使用data
        const readingData = readingRes.value?.data || readingRes.value || {};
        // 从reading API获取完成文章数
        completedArticles.value = typeof readingData.totalArticles === 'number' ? readingData.totalArticles : 0;
      } else {
        console.log('阅读篇数数据为空')
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

    const result: any = await subscriptionApi.create(
      userId,
      selectedPlan.value.type,
      selectedPaymentMethod.value
    )

    // 根据后端实际响应结构调整判断逻辑
    if (result?.success) {
      ElMessage.success('订阅成功！感谢您的支持。')
      await loadSubscriptionData()
      // 同时更新userStore中的订阅信息
      await userStore.fetchSubscription()
      resetPaymentDialog()
    } else {
      ElMessage.error(result?.message || '订阅失败，请稍后重试。')
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

// 生命周期
onMounted(() => {
  // 并行加载订阅数据和套餐价格配置
  Promise.all([
    loadSubscriptionData(),
    loadBackendPlanPrices()
  ])

  // 检查URL参数，如果有showUpgrade=true则自动显示升级对话框
  const urlParams = new URLSearchParams(window.location.search)
  if (urlParams.get('showUpgrade') === 'true') {
    showUpgradeDialog.value = true
  }
})
</script>

<style scoped>
.subscription-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 2.5em;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.current-subscription {
  margin-bottom: 30px;
}

.subscription-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subscription-info {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
  align-items: center;
}

.plan-info h3 {
  margin: 0 0 5px 0;
  font-size: 1.5em;
}

.price {
  font-size: 1.2em;
  color: #409eff;
  font-weight: bold;
}

.usage-info .usage-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.subscription-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.plans-section {
  margin-bottom: 30px;
}

.plans-section h2 {
  text-align: center;
  margin-bottom: 20px;
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.plan-card {
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.plan-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.plan-card.recommended {
  border: 2px solid #409eff;
}

.recommended-badge {
  position: absolute;
  top: -10px;
  right: 20px;
  background: #409eff;
  color: white;
  padding: 5px 15px;
  border-radius: 15px;
  font-size: 0.8em;
  font-weight: bold;
}

.plan-header {
  text-align: center;
  margin-bottom: 20px;
}

.plan-header h3 {
  margin: 0 0 10px 0;
  font-size: 1.5em;
}

.plan-price .price {
  font-size: 2em;
  color: #409eff;
  font-weight: bold;
}

.plan-price .duration {
  color: #666;
}

.plan-features {
  margin-bottom: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  color: #666;
}

.feature-item .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.usage-section,
.history-section {
  margin-bottom: 30px;
}

.usage-stats .stat-item {
  margin-bottom: 20px;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: bold;
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
  transition: border-color 0.3s ease;
}

.upgrade-option:hover {
  border-color: #409eff;
}

.upgrade-info {
  flex: 0 0 200px;
  margin-right: 20px;
}

.upgrade-info h4 {
  margin: 0 0 5px 0;
  font-size: 1.2em;
}

.upgrade-benefits {
  flex: 1;
}

.benefit-item {
  color: #666;
  margin-bottom: 5px;
}

.unified-button {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  min-width: 120px;
  transition: all 0.3s ease;
}

.unified-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.unified-button:active:not(:disabled) {
  transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .subscription-info {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .plans-grid {
    grid-template-columns: 1fr;
  }

  .subscription-actions {
    justify-content: center;
    gap: 15px;
  }

  .upgrade-option {
    flex-direction: column;
  }

  .upgrade-info {
    flex: none;
    margin-right: 0;
    margin-bottom: 15px;
    text-align: center;
  }

  .unified-button {
    min-width: 100%;
    padding: 10px 20px;
    font-size: 13px;
  }
}
</style>

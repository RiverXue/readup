<template>
  <div class="comparison-table">
    <h3>功能对比</h3>
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th class="feature-column">功能</th>
            <th 
              v-for="plan in plans" 
              :key="plan.type"
              :class="['plan-column', { 'recommended': plan.recommended }]"
            >
              <div class="plan-header">
                <h4>{{ plan.name }}</h4>
                <div class="plan-price">
                  <span class="price">¥{{ plan.price }}</span>
                  <span class="duration">/{{ plan.duration }}</span>
                </div>
                <div v-if="plan.recommended" class="recommended-badge">推荐</div>
              </div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="feature in features" :key="feature.key">
            <td class="feature-name">
              <div class="feature-info">
                <el-icon><component :is="feature.icon" /></el-icon>
                <span>{{ feature.name }}</span>
                <el-tooltip v-if="feature.description" :content="feature.description" placement="top">
                  <el-icon class="info-icon"><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
            </td>
            <td 
              v-for="plan in plans" 
              :key="`${feature.key}-${plan.type}`"
              :class="['feature-value', { 'recommended': plan.recommended }]"
            >
              <div class="value-content">
                <span v-if="getFeatureValue(plan, feature.key) === true" class="check-mark">✅</span>
                <span v-else-if="getFeatureValue(plan, feature.key) === false" class="cross-mark">❌</span>
                <span v-else class="feature-text">{{ getFeatureValue(plan, feature.key) }}</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Plan {
  type: 'FREE' | 'BASIC' | 'PRO' | 'ENTERPRISE'
  name: string
  price: number
  duration: string
  recommended?: boolean
  maxArticles: number
  maxWords: number
  aiFeatures: boolean
  prioritySupport: boolean
  features: string[]
}

interface Feature {
  key: string
  name: string
  icon: string
  description?: string
}

interface Props {
  plans: Plan[]
  features: Feature[]
}

const props = defineProps<Props>()

const getFeatureValue = (plan: Plan, featureKey: string) => {
  // 根据featureKey映射到plan的实际属性
  switch (featureKey) {
    case 'maxArticles':
      return plan.maxArticles || 0
    case 'maxWords':
      return plan.maxWords || 0
    case 'aiTranslation':
      return plan.aiFeatures || false
    case 'aiSummary':
      return plan.aiFeatures || false
    case 'aiParse':
      return plan.aiFeatures || false
    case 'aiAssistant':
      return plan.aiFeatures || false
    case 'trendingArticles':
      return plan.type !== 'FREE' // 免费用户有配额限制，其他用户无限制
    case 'categorySearch':
      return plan.type !== 'FREE' // 免费用户不能使用
    case 'customSearch':
      return plan.type === 'PRO' || plan.type === 'ENTERPRISE'
    case 'advancedFilter':
      return plan.type === 'PRO' || plan.type === 'ENTERPRISE'
    case 'fontControl':
      return plan.type === 'PRO' || plan.type === 'ENTERPRISE'
    case 'inlineTranslation':
      return plan.type === 'PRO' || plan.type === 'ENTERPRISE'
    case 'prioritySupport':
      return plan.prioritySupport || false
    default:
      return false
  }
}

// 默认功能列表
const defaultFeatures = computed(() => [
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
</script>

<style scoped>
.comparison-table {
  margin: 40px 0;
}

.comparison-table h3 {
  text-align: center;
  margin-bottom: 24px;
  font-size: 1.5em;
  color: var(--text-primary);
}

.table-container {
  overflow-x: auto;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  min-width: 800px;
}

thead {
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  color: white;
}

.feature-column {
  width: 200px;
  text-align: left;
  padding: 16px;
  font-weight: 600;
}

.plan-column {
  min-width: 150px;
  text-align: center;
  padding: 16px;
  position: relative;
}

.plan-column.recommended {
  background: linear-gradient(135deg, #34C759 0%, #30D158 100%);
  color: white;
}

.plan-header h4 {
  margin: 0 0 8px 0;
  font-size: 1.1em;
}

.plan-price {
  margin-bottom: 8px;
}

.price {
  font-size: 1.3em;
  font-weight: bold;
}

.duration {
  font-size: 0.9em;
  opacity: 0.8;
}

.recommended-badge {
  position: absolute;
  top: -8px;
  right: 8px;
  background: #ff4757;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
}

.feature-name {
  background: #f8f9fa;
  font-weight: 500;
}

.feature-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-icon {
  color: #6c757d;
  cursor: help;
}

.feature-value {
  text-align: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e9ecef;
}

.feature-value.recommended {
  background: rgba(255, 107, 107, 0.05);
}

.value-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-mark, .cross-mark {
  font-size: 1.2em;
}

.feature-text {
  font-size: 0.9em;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .table-container {
    font-size: 0.9em;
  }
  
  .feature-column {
    width: 150px;
    padding: 12px 8px;
  }
  
  .plan-column {
    min-width: 120px;
    padding: 12px 8px;
  }
  
  .plan-header h4 {
    font-size: 1em;
  }
  
  .price {
    font-size: 1.1em;
  }
}
</style>

<template>
  <div class="recommendation-engine">
    <div class="recommendation-header">
      <h3>🤖 基于你的使用习惯，我们推荐：</h3>
      <p>通过分析你的学习数据，为你推荐最适合的套餐</p>
    </div>
    
    <div v-if="recommendedPlan" class="recommended-plan" :class="`plan-${recommendedPlan.type.toLowerCase()}`">
      <div class="plan-badge">
        <el-icon><Star /></el-icon>
        <span>最适合你</span>
      </div>
      
      <div class="plan-content">
        <h4>{{ recommendedPlan.name }}</h4>
        <div class="plan-price">
          <span class="price">¥{{ recommendedPlan.price }}</span>
          <span class="duration">/{{ recommendedPlan.duration }}</span>
        </div>
        
        <div class="recommendation-reasons">
          <h5>推荐理由：</h5>
          <ul>
            <li v-for="reason in recommendationReasons" :key="reason">
              <el-icon><Check /></el-icon>
              {{ reason }}
            </li>
          </ul>
        </div>
        
        <div class="plan-actions">
          <el-button type="primary" @click="selectPlan" size="large">
            选择此套餐
          </el-button>
          <el-button @click="viewAllPlans" text>
            查看所有套餐
          </el-button>
        </div>
      </div>
    </div>
    
    <div v-else class="no-recommendation">
      <p>暂无推荐套餐，请查看所有套餐选项</p>
      <el-button @click="viewAllPlans" type="primary">
        查看所有套餐
      </el-button>
    </div>
    
    <div class="usage-analysis">
      <h4>📊 你的使用分析</h4>
      <div class="analysis-grid">
        <div class="analysis-item">
          <div class="analysis-label">平均每月阅读</div>
          <div class="analysis-value">{{ userStats.monthlyArticles }}篇文章</div>
        </div>
        <div class="analysis-item">
          <div class="analysis-label">平均文章长度</div>
          <div class="analysis-value">{{ userStats.avgArticleLength }}字</div>
        </div>
        <div class="analysis-item">
          <div class="analysis-label">AI功能使用率</div>
          <div class="analysis-value">{{ userStats.aiUsageRate }}%</div>
        </div>
        <div class="analysis-item">
          <div class="analysis-label">学习活跃度</div>
          <div class="analysis-value">{{ userStats.activityLevel }}</div>
        </div>
      </div>
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
  maxArticles: number
  maxWords: number
  aiFeatures: boolean
  currency: string
  prioritySupport: boolean
  features: string[]
}

interface UserStats {
  monthlyArticles: number
  avgArticleLength: number
  aiUsageRate: number
  activityLevel: string
}

interface Props {
  plans: Plan[]
  userStats: UserStats
}

const props = defineProps<Props>()
const emit = defineEmits<{
  selectPlan: [plan: Plan]
  viewAllPlans: []
}>()

const recommendedPlan = computed(() => {
  // 如果plans数组为空，返回null
  if (!props.plans || props.plans.length === 0) {
    return null
  }
  
  // 智能推荐算法
  const { monthlyArticles, avgArticleLength, aiUsageRate } = props.userStats
  
  // 根据使用量推荐套餐
  if (monthlyArticles <= 20 && avgArticleLength <= 1000 && aiUsageRate <= 30) {
    return props.plans.find(p => p.type === 'BASIC') || props.plans[0]
  } else if (monthlyArticles <= 100 && avgArticleLength <= 3000 && aiUsageRate <= 70) {
    return props.plans.find(p => p.type === 'PRO') || props.plans[1]
  } else {
    return props.plans.find(p => p.type === 'ENTERPRISE') || props.plans[2]
  }
})

const recommendationReasons = computed(() => {
  if (!recommendedPlan.value) {
    return ['暂无推荐套餐']
  }
  
  const { monthlyArticles, avgArticleLength, aiUsageRate } = props.userStats
  const reasons = []
  
  if (monthlyArticles > recommendedPlan.value.maxArticles * 0.8) {
    reasons.push(`你每月阅读${monthlyArticles}篇文章，需要更多额度`)
  }
  
  if (avgArticleLength > recommendedPlan.value.maxWords * 0.8) {
    reasons.push(`你经常阅读长文章，需要更高的字数限制`)
  }
  
  if (aiUsageRate > 50 && !recommendedPlan.value.aiFeatures) {
    reasons.push(`你经常使用AI功能，需要AI权限`)
  }
  
  if (reasons.length === 0) {
    reasons.push('这个套餐完美匹配你的使用习惯')
  }
  
  return reasons
})

const selectPlan = () => {
  if (recommendedPlan.value) {
    emit('selectPlan', recommendedPlan.value)
  }
}

const viewAllPlans = () => {
  emit('viewAllPlans')
}
</script>

<style scoped>
.recommendation-engine {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 16px;
  padding: 24px;
  margin: 20px 0;
}

.recommendation-header {
  text-align: center;
  margin-bottom: 24px;
}

.recommendation-header h3 {
  margin: 0 0 8px 0;
  color: var(--text-primary);
  font-size: 1.3em;
}

.recommendation-header p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.9em;
}

.recommended-plan {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  position: relative;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.recommended-plan:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.plan-badge {
  position: absolute;
  top: -12px;
  left: 24px;
  background: linear-gradient(135deg, #ff6b6b, #ffa500);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9em;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.plan-content h4 {
  margin: 16px 0 8px 0;
  font-size: 1.4em;
  color: var(--text-primary);
}

.plan-price {
  margin-bottom: 16px;
}

.price {
  font-size: 1.8em;
  font-weight: bold;
  color: var(--primary-color);
}

.duration {
  font-size: 1em;
  color: var(--text-secondary);
  margin-left: 4px;
}

.recommendation-reasons {
  margin-bottom: 20px;
}

.recommendation-reasons h5 {
  margin: 0 0 12px 0;
  color: var(--text-primary);
  font-size: 1em;
}

.recommendation-reasons ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.recommendation-reasons li {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-size: 0.9em;
}

.plan-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.usage-analysis {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  padding: 20px;
}

.usage-analysis h4 {
  margin: 0 0 16px 0;
  color: var(--text-primary);
  font-size: 1.1em;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.analysis-item {
  text-align: center;
}

.analysis-label {
  font-size: 0.9em;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.analysis-value {
  font-size: 1.2em;
  font-weight: 600;
  color: var(--text-primary);
}

.no-recommendation {
  background: white;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  color: var(--text-secondary);
}

.no-recommendation p {
  margin: 0 0 16px 0;
  font-size: 1.1em;
}

@media (max-width: 768px) {
  .recommendation-engine {
    padding: 16px;
  }
  
  .plan-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .analysis-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

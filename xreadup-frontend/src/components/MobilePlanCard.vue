<template>
  <div class="mobile-plan-card" :class="{ 'selected': isSelected, 'recommended': plan.recommended }">
    <div class="plan-header">
      <div class="plan-badges">
        <div v-if="plan.recommended" class="badge recommended">推荐</div>
        <div v-if="isSelected" class="badge selected">已选择</div>
      </div>
      
      <h3>{{ plan.name }}</h3>
      <div class="plan-price">
        <span class="price">¥{{ plan.price }}</span>
        <span class="duration">/{{ plan.duration }}</span>
      </div>
    </div>
    
    <div class="plan-features">
      <div v-for="feature in plan.features.slice(0, 3)" :key="feature" class="feature-item">
        <el-icon><Check /></el-icon>
        <span>{{ feature }}</span>
      </div>
    </div>
    
    <div class="plan-action">
      <el-button 
        :type="isSelected ? 'success' : 'primary'"
        :disabled="isSelected"
        @click="selectPlan"
        block
        size="large"
      >
        {{ isSelected ? '当前套餐' : '选择套餐' }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Plan {
  type: 'FREE' | 'BASIC' | 'PRO' | 'ENTERPRISE'
  name: string
  price: number
  duration: string
  features: string[]
  recommended?: boolean
  currency: string
  maxArticles: number
  maxWords: number
  aiFeatures: boolean
  prioritySupport: boolean
}

interface Props {
  plan: Plan
  isSelected: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  selectPlan: [plan: Plan]
}>()

const selectPlan = () => {
  if (!props.isSelected) {
    emit('selectPlan', props.plan)
  }
}
</script>

<style scoped>
.mobile-plan-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 2px solid transparent;
  transition: all 0.3s ease;
  position: relative;
}

.mobile-plan-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.mobile-plan-card.selected {
  border-color: var(--primary-color);
  background: linear-gradient(135deg, #f8f9ff 0%, #e8f2ff 100%);
}

.mobile-plan-card.recommended {
  border-color: #ff6b6b;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
}

.plan-badges {
  position: absolute;
  top: -8px;
  right: 16px;
  display: flex;
  gap: 8px;
}

.badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: 600;
}

.badge.recommended {
  background: #ff6b6b;
  color: white;
}

.badge.selected {
  background: var(--primary-color);
  color: white;
}

.plan-header h3 {
  margin: 8px 0 12px 0;
  font-size: 1.3em;
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

.plan-features {
  margin-bottom: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-size: 0.9em;
}

.plan-action {
  margin-top: auto;
}

@media (max-width: 480px) {
  .mobile-plan-card {
    padding: 16px;
  }
  
  .plan-header h3 {
    font-size: 1.2em;
  }
  
  .price {
    font-size: 1.6em;
  }
  
  .feature-item {
    font-size: 0.85em;
  }
}
</style>

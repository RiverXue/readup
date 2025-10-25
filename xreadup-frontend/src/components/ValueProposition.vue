<template>
  <div class="value-proposition">
    <div class="value-header">
      <h3>💡 选择{{ planName }}，你将获得：</h3>
      <p class="value-subtitle">基于用户数据统计，平均学习效果提升</p>
    </div>
    
    <div class="value-metrics">
      <div class="metric-item">
        <div class="metric-icon">⏰</div>
        <div class="metric-content">
          <div class="metric-value">{{ timeSaved }}分钟</div>
          <div class="metric-label">每天节省学习时间</div>
        </div>
      </div>
      
      <div class="metric-item">
        <div class="metric-icon">📈</div>
        <div class="metric-content">
          <div class="metric-value">{{ efficiencyGain }}%</div>
          <div class="metric-label">阅读理解能力提升</div>
        </div>
      </div>
      
      <div class="metric-item">
        <div class="metric-icon">🎯</div>
        <div class="metric-content">
          <div class="metric-value">{{ personalizationLevel }}</div>
          <div class="metric-label">个性化学习功能</div>
        </div>
      </div>
      
      <div class="metric-item">
        <div class="metric-icon">💬</div>
        <div class="metric-content">
          <div class="metric-value">{{ aiFeatures }}</div>
          <div class="metric-label">AI智能功能</div>
        </div>
      </div>
    </div>
    
    <div class="value-proof">
      <div class="proof-item">
        <el-icon><User /></el-icon>
        <span>已有{{ userCount }}+用户选择</span>
      </div>
      <div class="proof-item">
        <el-icon><Star /></el-icon>
        <span>用户满意度{{ satisfaction }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  planName: string
  timeSaved: number
  efficiencyGain: number
  personalizationLevel: string
  aiFeatures: string
  userCount: number
  satisfaction: number
}

const props = defineProps<Props>()

// 根据套餐类型计算价值指标
const valueMetrics = computed(() => {
  const metrics = {
    '免费用户': {
      timeSaved: 0,
      efficiencyGain: 0,
      personalizationLevel: '无',
      aiFeatures: '无',
      userCount: 1000,
      satisfaction: 85
    },
    '基础版': {
      timeSaved: 15,
      efficiencyGain: 20,
      personalizationLevel: '基础',
      aiFeatures: '无',
      userCount: 500,
      satisfaction: 88
    },
    '专业版': {
      timeSaved: 30,
      efficiencyGain: 40,
      personalizationLevel: '完整',
      aiFeatures: '5项',
      userCount: 300,
      satisfaction: 95
    },
    '企业版': {
      timeSaved: 45,
      efficiencyGain: 60,
      personalizationLevel: '企业级',
      aiFeatures: '全部',
      userCount: 50,
      satisfaction: 98
    }
  }
  
  return metrics[props.planName as keyof typeof metrics] || metrics['免费用户']
})
</script>

<style scoped>
.value-proposition {
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  border-radius: 16px;
  padding: 24px;
  color: white;
  margin: 20px 0;
  box-shadow: 0 8px 32px rgba(0, 122, 255, 0.2);
}

.value-header h3 {
  margin: 0 0 8px 0;
  font-size: 1.5em;
  font-weight: 600;
  text-align: center;
}

.value-subtitle {
  margin: 0 0 20px 0;
  opacity: 0.9;
  font-size: 0.9em;
  text-align: center;
}

.value-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.1);
  padding: 16px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.metric-icon {
  font-size: 2em;
  flex-shrink: 0;
}

.metric-content {
  flex: 1;
}

.metric-value {
  font-size: 1.5em;
  font-weight: bold;
  margin-bottom: 4px;
  color: #fff;
}

.metric-label {
  font-size: 0.9em;
  opacity: 0.9;
  color: #fff;
}

.value-proof {
  display: flex;
  gap: 20px;
  justify-content: center;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 16px;
}

.proof-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9em;
  opacity: 0.9;
}

@media (max-width: 768px) {
  .value-metrics {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .value-proof {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
  
  .metric-item {
    flex-direction: column;
    text-align: center;
  }
}
</style>

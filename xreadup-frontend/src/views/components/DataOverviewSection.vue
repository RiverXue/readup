<template>
  <div class="data-overview-section">
    <div class="section-header">
      <h2>📊 学习概览</h2>
      <div class="header-actions">
        <el-select v-model="timeRange" @change="handleTimeRangeChange" size="small">
          <el-option label="最近7天" value="7"></el-option>
          <el-option label="最近30天" value="30"></el-option>
          <el-option label="最近90天" value="90"></el-option>
        </el-select>
      </div>
    </div>
    
    <div class="overview-cards" v-loading="loading">
      <div 
        v-for="card in overviewCards" 
        :key="card.type"
        class="overview-card"
        :class="[card.type, { 'trending-up': card.trend > 0, 'trending-down': card.trend < 0 }]"
        @click="handleCardClick(card.type)"
      >
        <div class="card-icon">
          <el-icon :size="32">
            <component :is="card.icon" />
          </el-icon>
        </div>
        <div class="card-content">
          <div class="card-title">{{ card.title }}</div>
          <div class="card-value">{{ card.value }}</div>
          <div class="card-trend" v-if="card.trend !== 0">
            <el-icon :size="14">
              <ArrowUp v-if="card.trend > 0" />
              <ArrowDown v-else />
            </el-icon>
            <span>{{ Math.abs(card.trend) }}%</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { 
  Reading, 
  Collection, 
  Clock, 
  Document, 
  ArrowUp, 
  ArrowDown, 
  ArrowRight 
} from '@element-plus/icons-vue'

interface Props {
  dashboardData: any
  loading: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  cardClick: [type: string]
  timeRangeChange: [range: number]
}>()

const timeRange = ref(7)

const overviewCards = computed(() => {
  if (!props.dashboardData) return []
  
  const data = props.dashboardData
  return [
    {
      type: 'vocabulary',
      title: '总词汇量',
      value: data.vocabularyData?.totalWords || 0,
      icon: 'Collection',
      trend: calculateTrend(data.vocabularyData?.weeklyNewWords, 20)
    },
    {
      type: 'reading',
      title: '阅读时长',
      value: `${data.readingData?.totalMinutes || 0}分钟`,
      icon: 'Reading',
      trend: calculateTrend(data.readingData?.weeklyAverageMinutes, 30)
    },
    {
      type: 'articles',
      title: '阅读文章',
      value: `${data.readingData?.totalArticles || 0}篇`,
      icon: 'Document',
      trend: calculateTrend(data.readingData?.totalArticles, 5)
    },
    {
      type: 'streak',
      title: '连续学习',
      value: `${data.currentStreak || 0}天`,
      icon: 'Clock',
      trend: calculateTrend(data.currentStreak, 3)
    }
  ]
})

const calculateTrend = (current: number, baseline: number) => {
  if (!current || !baseline) return 0
  return Math.round(((current - baseline) / baseline) * 100)
}

const handleCardClick = (type: string) => {
  emit('cardClick', type)
}

const handleTimeRangeChange = (range: number) => {
  emit('timeRangeChange', range)
}

watch(() => props.dashboardData, (newData) => {
  if (newData) {
    console.log('概览数据更新:', newData)
  }
})
</script>

<style scoped>
.data-overview-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #2d3748;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.overview-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
  position: relative;
  overflow: hidden;
  min-height: 140px;
}

.overview-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #409eff, #67c23a);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.overview-card:hover::before {
  transform: scaleX(1);
}

.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.card-icon {
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
}

.overview-card.vocabulary .card-icon {
  background: linear-gradient(135deg, #409eff, #5ac8fa);
  color: white;
}

.overview-card.reading .card-icon {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  color: white;
}

.overview-card.articles .card-icon {
  background: linear-gradient(135deg, #e6a23c, #f0c78a);
  color: white;
}

.overview-card.streak .card-icon {
  background: linear-gradient(135deg, #f56c6c, #f89898);
  color: white;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.card-title {
  font-size: 13px;
  color: #718096;
  margin: 0;
}

.card-value {
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
  margin: 0;
}

.card-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 500;
  margin-top: 4px;
}

.card-trend.trending-up {
  color: #67c23a;
}

.card-trend.trending-down {
  color: #f56c6c;
}


@media (max-width: 1200px) {
  .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .overview-cards {
    grid-template-columns: 1fr;
  }
  
  .overview-card {
    padding: 16px;
    min-height: 120px;
  }
  
  .card-value {
    font-size: 20px;
  }
  
  .card-title {
    font-size: 12px;
  }
}
</style>

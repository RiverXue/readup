<template>
  <div class="comparison-section">
    <div class="section-header">
      <h2>📊 数据对比分析</h2>
      <div class="comparison-controls">
        <el-select v-model="comparisonType" @change="updateComparison" size="small">
          <el-option label="本周 vs 上周" value="weekly"></el-option>
          <el-option label="本月 vs 上月" value="monthly"></el-option>
          <el-option label="今年 vs 去年" value="yearly"></el-option>
        </el-select>
      </div>
    </div>

    <div class="comparison-content" v-loading="loading">
      <!-- 对比图表 -->
      <div class="comparison-chart">
        <div class="chart-header">
          <h3>{{ getComparisonTitle() }}</h3>
          <el-button size="small" @click="refreshComparisonChart">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
        <div ref="comparisonChart" class="chart"></div>
      </div>

      <!-- 对比指标 -->
      <div class="comparison-metrics">
        <div class="metric-card">
          <div class="metric-title">学习时长对比</div>
          <div class="metric-value">
            <span class="current">{{ comparisonData.current.time }}</span>
            <span class="vs">vs</span>
            <span class="previous">{{ comparisonData.previous.time }}</span>
          </div>
          <div class="metric-change" :class="comparisonData.timeChange >= 0 ? 'positive' : 'negative'">
            {{ comparisonData.timeChange >= 0 ? '+' : '' }}{{ comparisonData.timeChange }}%
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-title">阅读文章对比</div>
          <div class="metric-value">
            <span class="current">{{ comparisonData.current.articles }}</span>
            <span class="vs">vs</span>
            <span class="previous">{{ comparisonData.previous.articles }}</span>
          </div>
          <div class="metric-change" :class="comparisonData.articlesChange >= 0 ? 'positive' : 'negative'">
            {{ comparisonData.articlesChange >= 0 ? '+' : '' }}{{ comparisonData.articlesChange }}%
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-title">词汇学习对比</div>
          <div class="metric-value">
            <span class="current">{{ comparisonData.current.words }}</span>
            <span class="vs">vs</span>
            <span class="previous">{{ comparisonData.previous.words }}</span>
          </div>
          <div class="metric-change" :class="comparisonData.wordsChange >= 0 ? 'positive' : 'negative'">
            {{ comparisonData.wordsChange >= 0 ? '+' : '' }}{{ comparisonData.wordsChange }}%
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'

interface Props {
  currentData: any
  historicalData: any
  loading: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  comparisonChange: [type: string]
}>()

// 监听props变化 - 移除重复的watch，使用下面的统一watch

const comparisonType = ref('weekly')

// 图表引用
const comparisonChart = ref<HTMLElement>()
let comparisonChartInstance: echarts.ECharts | null = null

// 对比数据
const comparisonData = computed(() => {
  const hasCurrentData = props.currentData && props.currentData.length > 0
  const hasHistoricalData = props.historicalData && props.historicalData.length > 0

  if (!hasCurrentData && !hasHistoricalData) {
    return {
      current: { time: '0分钟', articles: '0篇', words: '0个' },
      previous: { time: '0分钟', articles: '0篇', words: '0个' },
      timeChange: 0,
      articlesChange: 0,
      wordsChange: 0
    }
  }

  // 计算当前数据
  const currentTime = hasCurrentData ? props.currentData.reduce((sum: number, day: any) => sum + (day.minutes || 0), 0) : 0
  const currentArticles = hasCurrentData ? props.currentData.reduce((sum: number, day: any) => sum + (day.articles || 0), 0) : 0
  const currentWords = hasCurrentData ? props.currentData.reduce((sum: number, day: any) => sum + (day.newWords || 0), 0) : 0

  // 计算历史数据
  const previousTime = hasHistoricalData ? props.historicalData.reduce((sum: number, day: any) => sum + (day.minutes || 0), 0) : 0
  const previousArticles = hasHistoricalData ? props.historicalData.reduce((sum: number, day: any) => sum + (day.articles || 0), 0) : 0
  const previousWords = hasHistoricalData ? props.historicalData.reduce((sum: number, day: any) => sum + (day.newWords || 0), 0) : 0

  // 计算变化率
  const timeChange = previousTime > 0 ? Math.round(((currentTime - previousTime) / previousTime) * 100) : 0
  const articlesChange = previousArticles > 0 ? Math.round(((currentArticles - previousArticles) / previousArticles) * 100) : 0
  const wordsChange = previousWords > 0 ? Math.round(((currentWords - previousWords) / previousWords) * 100) : 0

  return {
    current: {
      time: `${currentTime}分钟`,
      articles: `${currentArticles}篇`,
      words: `${currentWords}个`
    },
    previous: {
      time: `${previousTime}分钟`,
      articles: `${previousArticles}篇`,
      words: `${previousWords}个`
    },
    timeChange,
    articlesChange,
    wordsChange
  }
})

const getComparisonTitle = () => {
  const titles: Record<string, string> = {
    'weekly': '学习时长对比 (本周 vs 上周)',
    'monthly': '学习时长对比 (本月 vs 上月)',
    'yearly': '学习时长对比 (今年 vs 去年)'
  }
  return titles[comparisonType.value] || titles.weekly
}

const initChart = () => {
  nextTick(() => {
    if (comparisonChart.value) {
      comparisonChartInstance = echarts.init(comparisonChart.value)
      updateChart()
    }
  })
}

const updateChart = () => {
  if (!comparisonChartInstance) {
    console.log('图表实例不存在，跳过更新')
    return
  }

  console.log('对比图数据:', {
    currentData: props.currentData,
    historicalData: props.historicalData,
    currentDataLength: props.currentData?.length,
    historicalDataLength: props.historicalData?.length
  })

  // 检查是否有有效数据
  const hasCurrentData = props.currentData && props.currentData.length > 0
  const hasHistoricalData = props.historicalData && props.historicalData.length > 0

  console.log('数据状态检查:', {
    hasCurrentData,
    hasHistoricalData,
    currentDataValid: props.currentData && Array.isArray(props.currentData),
    historicalDataValid: props.historicalData && Array.isArray(props.historicalData)
  })

  // 只有在完全没有数据时才显示空状态
  if (!hasCurrentData && !hasHistoricalData) {
    console.log('数据完全为空，显示空状态')
    // 显示空状态
    const option = {
      title: {
        text: getComparisonTitle(),
        left: 'center',
        textStyle: { fontSize: 16, fontWeight: 'bold' }
      },
      graphic: {
        type: 'text',
        left: 'center',
        top: 'middle',
        style: {
          text: '',
          fontSize: 14,
          fill: '#999'
        }
      }
    }
    comparisonChartInstance.setOption(option)
    return
  }

  // 处理数据，允许部分数据为空
  const currentData = hasCurrentData ? props.currentData.map((day: any) => day.minutes || 0) : []
  const previousData = hasHistoricalData ? props.historicalData.map((day: any) => day.minutes || 0) : []

  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

  // 动态生成图例数据
  const legendData = []
  if (hasCurrentData) legendData.push('当前')
  if (hasHistoricalData) legendData.push('对比')

  // 动态生成系列数据
  const series = []
  if (hasCurrentData) {
    series.push({
      name: '当前',
      type: 'bar',
      data: currentData,
      itemStyle: { color: '#409eff' }
    })
  }
  if (hasHistoricalData) {
    series.push({
      name: '对比',
      type: 'bar',
      data: previousData,
      itemStyle: { color: '#67c23a' }
    })
  }

  const option = {
    title: {
      text: getComparisonTitle(),
      left: 'center',
      top: '10px',
      textStyle: { fontSize: 16, fontWeight: 'bold' }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: legendData,
      top: '40px',
      left: 'center'
    },
    grid: {
      top: '80px',
      left: '50px',
      right: '50px',
      bottom: '50px',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: days.slice(0, Math.max(currentData.length, previousData.length, 7))
    },
    yAxis: {
      type: 'value',
      name: '学习时长(分钟)'
    },
    series: series
  }

  comparisonChartInstance.setOption(option)
}

const updateComparison = (type: string) => {
  emit('comparisonChange', type)
}

const refreshComparisonChart = () => {
  updateChart()
}

// 监听数据变化
watch(() => [props.currentData, props.historicalData, comparisonType.value], () => {
  console.log('ComparisonSection data changed, updating chart', {
    currentData: props.currentData,
    historicalData: props.historicalData,
    comparisonType: comparisonType.value
  })
  updateChart()
}, { deep: true, immediate: true })

onMounted(() => {
  initChart()
})
</script>

<style scoped>
.comparison-section {
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

.comparison-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comparison-chart {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.chart {
  width: 100%;
  height: 300px;
}

.comparison-metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.metric-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.metric-title {
  font-size: 14px;
  color: #718096;
  margin-bottom: 12px;
}

.metric-value {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}

.metric-value .current {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
}

.metric-value .vs {
  font-size: 14px;
  color: #a0aec0;
}

.metric-value .previous {
  font-size: 16px;
  font-weight: 600;
  color: #67c23a;
}

.metric-change {
  font-size: 16px;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 6px;
}

.metric-change.positive {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}

.metric-change.negative {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

@media (max-width: 768px) {
  .comparison-metrics {
    grid-template-columns: 1fr;
  }

  .metric-value {
    flex-direction: column;
    gap: 8px;
  }

  .chart {
    height: 250px;
  }
}
</style>

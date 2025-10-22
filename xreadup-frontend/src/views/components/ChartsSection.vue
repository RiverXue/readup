<template>
  <div class="charts-section">
    <div class="section-header">
      <h2>📈 数据可视化</h2>
      <div class="chart-controls">
        <el-select v-model="chartTimeRange" @change="handleTimeRangeChange" size="small">
          <el-option label="最近7天" value="7"></el-option>
          <el-option label="最近30天" value="30"></el-option>
          <el-option label="最近90天" value="90"></el-option>
        </el-select>
      </div>
    </div>
    
    <div class="charts-grid" v-loading="loading">
      <!-- 词汇增长曲线 -->
      <div class="chart-container">
        <div class="chart-header">
          <h3>词汇增长趋势</h3>
        </div>
        <div ref="vocabularyChart" class="chart"></div>
      </div>
      
      <!-- 阅读时长统计 -->
      <div class="chart-container">
        <div class="chart-header">
          <h3>阅读时长统计</h3>
        </div>
        <div ref="readingChart" class="chart"></div>
      </div>
      
      <!-- 难度分布饼图 -->
      <div class="chart-container">
        <div class="chart-header">
          <h3>阅读难度分布</h3>
        </div>
        <div ref="difficultyChart" class="chart"></div>
      </div>
      
      <!-- 学习效率雷达图 -->
      <div class="efficiency-chart-container">
        <div class="chart-header">
          <h3>学习效率分析</h3>
          <span class="chart-subtitle">多维度评估学习状态</span>
        </div>
        <div ref="efficiencyChart" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import type { ChartClickEvent } from '@/types/report'
import { Refresh } from '@element-plus/icons-vue'

interface Props {
  vocabularyData: any
  readingData: any
  loading: boolean
}

const props = defineProps<Props>()

// 定义事件
const emit = defineEmits<{
  chartClick: [event: ChartClickEvent]
  timeRangeChange: [range: number]
}>()

const chartTimeRange = ref(7)

// 图表引用
const efficiencyChart = ref<HTMLElement>()
const vocabularyChart = ref<HTMLElement>()
const readingChart = ref<HTMLElement>()
const difficultyChart = ref<HTMLElement>()

// 图表实例管理
const chartInstances = ref<Map<string, echarts.ECharts>>(new Map())

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    // 清理已存在的实例
    chartInstances.value.forEach(instance => instance.dispose())
    chartInstances.value.clear()
    
    // 初始化新实例
    if (vocabularyChart.value) {
      const instance = echarts.init(vocabularyChart.value)
      chartInstances.value.set('vocabulary', instance)
    }
    if (readingChart.value) {
      const instance = echarts.init(readingChart.value)
      chartInstances.value.set('reading', instance)
    }
    if (difficultyChart.value) {
      const instance = echarts.init(difficultyChart.value)
      chartInstances.value.set('difficulty', instance)
    }
    if (efficiencyChart.value) {
      const instance = echarts.init(efficiencyChart.value)
      chartInstances.value.set('efficiency', instance)
    }
    
    updateAllCharts()
  })
}

// 更新所有图表
const updateAllCharts = () => {
  updateVocabularyChart()
  updateReadingChart()
  updateDifficultyChart()
  updateEfficiencyChart()
}

// 更新词汇增长图表
const updateVocabularyChart = () => {
  const instance = chartInstances.value.get('vocabulary')
  if (!instance || !props.vocabularyData) return
  
  const option = {
    title: {
      text: '词汇增长趋势',
      left: 'center',
      textStyle: { fontSize: 16, fontWeight: 'bold' }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function (params: any) {
        const data = params[0]
        return `${data.axisValue}<br/>新增词汇: ${data.value}个`
      }
    },
    xAxis: {
      type: 'category',
      data: props.vocabularyData.dates || []
    },
    yAxis: {
      type: 'value',
      name: '词汇数量'
    },
    series: [{
      name: '新增词汇',
      type: 'line',
      data: props.vocabularyData.counts || [],
      smooth: true,
      lineStyle: { color: '#409eff' },
      itemStyle: { color: '#409eff' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ]
        }
      }
    }]
  }
  
  instance.setOption(option)
  
  // 添加点击事件
  instance.off('click')
  instance.on('click', (params: any) => {
    emit('chartClick', {
      type: 'vocabulary',
      data: params,
      date: params.name
    })
  })
}

// 更新阅读时长图表
const updateReadingChart = () => {
  const instance = chartInstances.value.get('reading')
  if (!instance || !props.readingData) return
  
  // 格式化日期为MM/DD格式，与词汇增长图表保持一致
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr)
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    return `${month}/${day}`
  }
  
  // 确保数据按时间顺序排列（从早到晚），与词汇增长图表保持一致
  const sortedReadings = [...(props.readingData.dailyReadings || [])].sort((a: any, b: any) => 
    new Date(a.date).getTime() - new Date(b.date).getTime()
  )
  
  // 获取词汇增长数据的日期范围，确保两个图表时间范围一致
  const vocabularyDates = props.vocabularyData?.dates || []
  const readingDates = sortedReadings.map((d: any) => formatDate(d.date))
  
  // 如果词汇数据有更早的日期，需要为阅读数据补充0值
  let finalDates = readingDates
  let finalData = sortedReadings.map((d: any) => d.minutes)
  
  if (vocabularyDates.length > 0 && readingDates.length > 0) {
    const vocabStartDate = vocabularyDates[0]
    const readingStartDate = readingDates[0]
    
    // 如果词汇数据开始日期更早，需要补充阅读数据
    if (vocabStartDate !== readingStartDate) {
      // 找到词汇数据中阅读数据开始日期之前的天数
      const vocabStartIndex = vocabularyDates.indexOf(readingStartDate)
      if (vocabStartIndex > 0) {
        // 在阅读数据前面补充0值
        const prefixDates = vocabularyDates.slice(0, vocabStartIndex)
        const prefixData = new Array(vocabStartIndex).fill(0)
        
        finalDates = [...prefixDates, ...readingDates]
        finalData = [...prefixData, ...finalData]
      }
    }
  }
  
  const option = {
    title: {
      text: '阅读时长统计',
      left: 'center',
      textStyle: { fontSize: 16, fontWeight: 'bold' }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function (params: any) {
        const data = params[0]
        return `${data.axisValue}<br/>阅读时长: ${data.value}分钟`
      }
    },
    xAxis: {
      type: 'category',
      data: finalDates
    },
    yAxis: {
      type: 'value',
      name: '分钟'
    },
    series: [{
      name: '阅读时长',
      type: 'bar',
      data: finalData,
      itemStyle: { color: '#67c23a' }
    }]
  }
  
  instance.setOption(option)
  
  // 添加点击事件
  instance.off('click')
  instance.on('click', (params: any) => {
    emit('chartClick', {
      type: 'reading',
      data: params,
      date: params.name
    })
  })
}

// 更新难度分布图表
const updateDifficultyChart = () => {
  const instance = chartInstances.value.get('difficulty')
  if (!instance || !props.readingData?.difficultyStats) return
  
  const difficultyStats = props.readingData.difficultyStats.filter((stat: any) => stat && stat.difficulty && stat.count)
  if (difficultyStats.length === 0) return
  
  const option = {
    title: {
      text: '阅读难度分布',
      left: 'center',
      textStyle: { fontSize: 16, fontWeight: 'bold' }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: difficultyStats.map((stat: any) => stat.difficulty)
    },
    series: [{
      name: '难度分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '60%'],
      data: difficultyStats.map((stat: any) => ({
        value: stat.count,
        name: stat.difficulty,
        itemStyle: {
          color: getDifficultyColor(stat.difficulty)
        }
      })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  instance.setOption(option)
  
  // 添加点击事件
  instance.off('click')
  instance.on('click', (params: any) => {
    emit('chartClick', {
      type: 'difficulty',
      data: params,
      date: undefined
    })
  })
}

// 获取难度颜色
const getDifficultyColor = (difficulty: string) => {
  const colors: Record<string, string> = {
    'A1': '#67c23a',
    'A2': '#85ce61',
    'B1': '#e6a23c',
    'B2': '#f0c78a',
    'C1': '#f56c6c',
    'C2': '#f89898'
  }
  return colors[difficulty] || '#909399'
}

// 统一的图表刷新方法
const refreshAllCharts = () => {
  updateAllCharts()
}

const handleTimeRangeChange = (range: number) => {
  emit('timeRangeChange', range)
}

// 更新学习效率雷达图
const updateEfficiencyChart = () => {
  const instance = chartInstances.value.get('efficiency')
  if (!instance || !props.readingData || !props.vocabularyData) return
  
  const dailyReadings = props.readingData.dailyReadings || []
  const difficultyStats = props.readingData.difficultyStats || []
  
  // 计算各维度分数（0-100）
  // 1. 阅读量得分
  const totalArticles = dailyReadings.reduce((sum: number, day: any) => sum + (day.articles || 0), 0)
  const articleScore = Math.min(100, (totalArticles / 70) * 100) // 假设70篇为满分
  
  // 2. 学习时长得分
  const totalMinutes = dailyReadings.reduce((sum: number, day: any) => sum + (day.minutes || 0), 0)
  const timeScore = Math.min(100, (totalMinutes / 420) * 100) // 假设420分钟（7小时）为满分
  
  // 3. 词汇积累得分
  const totalWords = dailyReadings.reduce((sum: number, day: any) => sum + (day.newWords || 0), 0)
  const wordScore = Math.min(100, (totalWords / 35) * 100) // 假设35个词为满分
  
  // 4. 学习频率得分（基于7天数据，但限制在合理范围内）
  const activeDays = dailyReadings.filter((day: any) => day.minutes > 0).length
  // 使用更合理的评分标准：5天及以上为满分，3-4天为良好，1-2天为一般
  let frequencyScore = 0
  if (activeDays >= 5) {
    frequencyScore = 100
  } else if (activeDays >= 3) {
    frequencyScore = 80
  } else if (activeDays >= 1) {
    frequencyScore = 60
  } else {
    frequencyScore = 0
  }
  
  // 5. 难度挑战得分（B2及以上难度的比例）
  const totalDifficultyArticles = difficultyStats.reduce((sum: number, stat: any) => sum + (stat.count || 0), 0)
  const highDifficultyArticles = difficultyStats
    .filter((stat: any) => ['B2', 'C1', 'C2'].includes(stat.difficulty))
    .reduce((sum: number, stat: any) => sum + (stat.count || 0), 0)
  const challengeScore = totalDifficultyArticles > 0 ? (highDifficultyArticles / totalDifficultyArticles) * 100 : 0
  
  // 确保所有分数都在0-100范围内
  const finalScores = [
    Math.min(100, Math.max(0, articleScore)),
    Math.min(100, Math.max(0, timeScore)),
    Math.min(100, Math.max(0, wordScore)),
    Math.min(100, Math.max(0, frequencyScore)),
    Math.min(100, Math.max(0, challengeScore))
  ]
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: function (params: any) {
        const dimensionNames = ['阅读量', '学习时长', '词汇积累', '学习频率', '难度挑战']
        const dimensionDescs = [
          '基于文章阅读数量，满分70篇',
          '基于每日学习时间，满分7小时', 
          '基于新增词汇数量，满分35个',
          '基于活跃学习天数，5天以上满分',
          '基于高难度文章比例，B2及以上'
        ]
        
        // 如果是雷达图数据
        if (params.seriesType === 'radar' && Array.isArray(params.value)) {
          // 显示所有维度的信息
          let tooltipContent = '<div style="padding: 8px;">'
          tooltipContent += '<div style="font-weight: bold; margin-bottom: 8px; color: #2d3748;">学习效率分析</div>'
          
          for (let i = 0; i < params.value.length && i < dimensionNames.length; i++) {
            const score = params.value[i] || 0
            const dimensionName = dimensionNames[i]
            const dimensionDesc = dimensionDescs[i]
            
            tooltipContent += `
              <div style="margin-bottom: 6px; padding: 4px; background: rgba(64, 158, 255, 0.05); border-radius: 4px;">
                <div style="font-weight: 500; color: #2d3748; font-size: 13px;">${dimensionName}: ${score.toFixed(1)}分</div>
                <div style="color: #666; font-size: 11px; margin-top: 2px;">${dimensionDesc}</div>
              </div>
            `
          }
          
          tooltipContent += '</div>'
          return tooltipContent
        }
        
        // 默认tooltip
        return `${params.name}: ${(params.value || 0).toFixed(1)}分`
      }
    },
    legend: {
      orient: 'horizontal',
      bottom: '10px',
      data: ['本周表现'],
      textStyle: {
        fontSize: 12,
        color: '#666'
      }
    },
    radar: {
      indicator: [
        { name: '阅读量', max: 100 },
        { name: '学习时长', max: 100 },
        { name: '词汇积累', max: 100 },
        { name: '学习频率', max: 100 },
        { name: '难度挑战', max: 100 }
      ],
      shape: 'polygon',
      splitNumber: 5,
      axisName: {
        color: '#666',
        fontSize: 12
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(64, 158, 255, 0.05)', 'rgba(64, 158, 255, 0.1)', 'rgba(64, 158, 255, 0.15)', 'rgba(64, 158, 255, 0.2)', 'rgba(64, 158, 255, 0.25)']
        }
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(64, 158, 255, 0.3)'
        }
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(64, 158, 255, 0.3)'
        }
      }
    },
    series: [{
      name: '学习效率',
      type: 'radar',
      data: [{
        value: finalScores,
        name: '本周表现',
        areaStyle: {
          color: 'rgba(64, 158, 255, 0.3)'
        },
        itemStyle: {
          color: '#409eff'
        },
        lineStyle: {
          width: 2,
          color: '#409eff'
        }
      }]
    }]
  }
  
  instance.setOption(option)
  
  // 添加点击事件
  instance.off('click')
  instance.on('click', (params: any) => {
    emit('chartClick', {
      type: 'efficiency',
      data: params,
      date: undefined
    })
  })
}

// 监听数据变化
watch(() => [props.vocabularyData, props.readingData], () => {
  updateAllCharts()
}, { deep: true })

// 生命周期
onMounted(() => {
  initCharts()
})

// 组件销毁时清理图表实例
onUnmounted(() => {
  chartInstances.value.forEach(instance => {
    instance.dispose()
  })
  chartInstances.value.clear()
})
</script>

<style scoped>
.charts-section {
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

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.chart-container {
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

/* 学习效率雷达图样式 */
.efficiency-chart-container {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.efficiency-chart-container .chart-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
}

.efficiency-chart-container .chart-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.chart-subtitle {
  font-size: 12px;
  color: #909399;
}


@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .chart-container {
    padding: 16px;
  }
  
  .chart {
    height: 250px;
    min-height: 200px;
  }
  
  .efficiency-chart-container {
    padding: 16px;
  }
  
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .chart-header h3 {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .charts-grid {
    gap: 12px;
  }
  
  .chart-container {
    padding: 12px;
  }
  
  .chart {
    height: 200px;
    min-height: 180px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .chart-controls {
    width: 100%;
  }
}
</style>

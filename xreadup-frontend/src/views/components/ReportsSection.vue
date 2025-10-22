<template>
  <div class="reports-section">
    <div class="section-header">
      <h2>📋 学习报告</h2>
      <div class="header-actions">
        <el-button size="small" @click="refreshReports">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>
    
    <div class="reports-content" v-loading="loading">
      <!-- 今日报告 -->
      <div class="today-report">
        <div class="report-header">
          <h3>📊 今日学习报告</h3>
          <span class="report-date">{{ formatDate(new Date()) }}</span>
        </div>
        
        <div class="report-content">
          <!-- 学习概览 -->
          <div class="overview-section">
            <div class="stat-card">
              <div class="stat-icon">📚</div>
              <div class="stat-content">
                <div class="stat-value">{{ todayData.vocabularyCount }}</div>
                <div class="stat-label">新增词汇</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">⏰</div>
              <div class="stat-content">
                <div class="stat-value">{{ todayData.readingMinutes }}</div>
                <div class="stat-label">学习时长</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📖</div>
              <div class="stat-content">
                <div class="stat-value">{{ todayData.articlesRead }}</div>
                <div class="stat-label">阅读文章</div>
              </div>
            </div>
          </div>
          
          <!-- 学习洞察 -->
          <div class="insights-section">
            <h4>💡 今日学习洞察</h4>
            <div class="insights-list">
              <div 
                v-for="insight in todayInsights" 
                :key="insight.id"
                class="insight-item"
                :class="insight.type"
              >
                <div class="insight-icon">{{ getInsightIcon(insight.type) }}</div>
                <div class="insight-content">
                  <div class="insight-title">{{ insight.title }}</div>
                  <div class="insight-text">{{ insight.content }}</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 明日建议 -->
          <div class="suggestions-section">
            <h4>🎯 明日学习建议</h4>
            <ul class="suggestions-list">
              <li v-for="suggestion in tomorrowSuggestions" :key="suggestion">
                {{ suggestion }}
              </li>
            </ul>
          </div>
        </div>
      </div>
      
      <!-- 周报 -->
      <div class="weekly-report">
        <div class="report-header">
          <h3>📈 本周学习周报</h3>
          <span class="report-period">{{ getWeekPeriod() }}</span>
        </div>
        
        <div class="report-content">
          <!-- 周度统计 -->
          <div class="weekly-stats">
            <div class="stat-grid">
              <div class="stat-item">
                <div class="stat-value">{{ weeklyData.totalMinutes }}</div>
                <div class="stat-label">总学习时长</div>
                <div class="stat-change" :class="weeklyData.minutesChange >= 0 ? 'positive' : 'negative'">
                  {{ weeklyData.minutesChange >= 0 ? '+' : '' }}{{ weeklyData.minutesChange }}分钟
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ weeklyData.totalArticles }}</div>
                <div class="stat-label">阅读文章数</div>
                <div class="stat-change" :class="weeklyData.articlesChange >= 0 ? 'positive' : 'negative'">
                  {{ weeklyData.articlesChange >= 0 ? '+' : '' }}{{ weeklyData.articlesChange }}篇
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ weeklyData.newWords }}</div>
                <div class="stat-label">新增词汇</div>
                <div class="stat-change" :class="weeklyData.wordsChange >= 0 ? 'positive' : 'negative'">
                  {{ weeklyData.wordsChange >= 0 ? '+' : '' }}{{ weeklyData.wordsChange }}个
                </div>
              </div>
            </div>
          </div>
          
          <!-- 周度洞察 -->
          <div class="weekly-insights">
            <h4>🔍 本周学习洞察</h4>
            <div class="insights-grid">
              <div 
                v-for="insight in weeklyInsights" 
                :key="insight.id"
                class="insight-card"
                :class="insight.type"
              >
                <div class="insight-header">
                  <div class="insight-icon">{{ getInsightIcon(insight.type) }}</div>
                  <div class="insight-title">{{ insight.title }}</div>
                </div>
                <div class="insight-content">{{ insight.content }}</div>
                <div class="insight-action" v-if="insight.action">
                  <el-button size="small" @click="executeAction(insight.action)">
                    {{ insight.actionText }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Refresh } from '@element-plus/icons-vue'

interface Props {
  todaySummary: any
  weeklyInsights: any
  loading: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  reportClick: [type: string]
}>()

// 今日数据
const todayData = computed(() => {
  if (!props.todaySummary) {
    return {
      vocabularyCount: 0,
      readingMinutes: 0,
      articlesRead: 0
    }
  }
  
  return {
    vocabularyCount: props.todaySummary.dailyNewWords || 0,
    readingMinutes: props.todaySummary.todayMinutes || 0,
    articlesRead: props.todaySummary.todayArticles || 0
  }
})

// 周度数据
const weeklyData = computed(() => {
  if (!props.weeklyInsights) {
    return {
      totalMinutes: 0,
      totalArticles: 0,
      newWords: 0,
      minutesChange: 0,
      articlesChange: 0,
      wordsChange: 0
    }
  }
  
  return {
    totalMinutes: props.weeklyInsights.totalMinutes || 0,
    totalArticles: props.weeklyInsights.totalArticles || 0,
    newWords: props.weeklyInsights.weeklyNewWords || 0,
    minutesChange: props.weeklyInsights.minutesChange || 0,
    articlesChange: props.weeklyInsights.articlesChange || 0,
    wordsChange: props.weeklyInsights.wordsChange || 0
  }
})

// 今日洞察
const todayInsights = computed(() => {
  const insights = []
  
  if (todayData.value.readingMinutes > 60) {
    insights.push({
      id: 'reading_excellent',
      type: 'positive',
      title: '学习时长优秀',
      content: `您今天学习了${todayData.value.readingMinutes}分钟，学习时长表现优秀！`
    })
  } else if (todayData.value.readingMinutes < 30) {
    insights.push({
      id: 'reading_suggestion',
      type: 'suggestion',
      title: '增加学习时长',
      content: `建议每天至少学习30分钟，当前学习${todayData.value.readingMinutes}分钟`
    })
  }
  
  if (todayData.value.vocabularyCount > 10) {
    insights.push({
      id: 'vocabulary_good',
      type: 'positive',
      title: '词汇学习积极',
      content: `今天新增${todayData.value.vocabularyCount}个词汇，学习进度良好！`
    })
  }
  
  return insights
})

// 明日建议
const tomorrowSuggestions = computed(() => {
  const suggestions = []
  
  if (todayData.value.readingMinutes < 30) {
    suggestions.push('建议明天增加学习时长，目标30分钟以上')
  }
  
  if (todayData.value.vocabularyCount < 5) {
    suggestions.push('建议明天多学习一些新词汇，目标5个以上')
  }
  
  if (todayData.value.articlesRead === 0) {
    suggestions.push('建议明天阅读一篇英语文章')
  }
  
  if (suggestions.length === 0) {
    suggestions.push('继续保持今天的学习状态！')
  }
  
  return suggestions
})

// 周度洞察
const weeklyInsights = computed(() => {
  const insights = []
  
  if (weeklyData.value.totalMinutes > 300) {
    insights.push({
      id: 'weekly_time_excellent',
      type: 'positive',
      title: '周学习时长优秀',
      content: `本周学习${weeklyData.value.totalMinutes}分钟，学习时长表现优秀！`
    })
  }
  
  if (weeklyData.value.newWords > 50) {
    insights.push({
      id: 'weekly_vocabulary_good',
      type: 'positive',
      title: '词汇学习积极',
      content: `本周新增${weeklyData.value.newWords}个词汇，学习进度良好！`
    })
  }
  
  if (weeklyData.value.minutesChange > 0) {
    insights.push({
      id: 'weekly_improvement',
      type: 'achievement',
      title: '学习时长提升',
      content: `相比上周，学习时长增加了${weeklyData.value.minutesChange}分钟`
    })
  }
  
  return insights
})

const formatDate = (date: Date) => {
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getWeekPeriod = () => {
  const now = new Date()
  const startOfWeek = new Date(now.setDate(now.getDate() - now.getDay()))
  const endOfWeek = new Date(startOfWeek)
  endOfWeek.setDate(startOfWeek.getDate() + 6)
  
  return `${startOfWeek.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })} - ${endOfWeek.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })}`
}

const getInsightIcon = (type: string) => {
  const icons = {
    'positive': '✅',
    'suggestion': '💡',
    'achievement': '🏆',
    'warning': '⚠️'
  }
  return icons[type] || '📊'
}

const executeAction = (action: string) => {
  console.log('执行动作:', action)
}

const refreshReports = () => {
  console.log('刷新报告数据')
}
</script>

<style scoped>
.reports-section {
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

.reports-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.today-report,
.weekly-report {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.report-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
}

.report-date,
.report-period {
  font-size: 14px;
  color: #718096;
}

.overview-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.stat-icon {
  font-size: 24px;
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409eff, #5ac8fa);
  color: white;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #718096;
}

.insights-section,
.suggestions-section {
  margin-bottom: 24px;
}

.insights-section h4,
.suggestions-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.insights-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.insight-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.insight-item.positive {
  background: #f0f9ff;
  border-left-color: #67c23a;
}

.insight-item.suggestion {
  background: #fff7ed;
  border-left-color: #e6a23c;
}

.insight-item.achievement {
  background: #f0f9ff;
  border-left-color: #409eff;
}

.insight-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.insight-content {
  flex: 1;
}

.insight-title {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
}

.insight-text {
  font-size: 13px;
  color: #718096;
}

.suggestions-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.suggestions-list li {
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #2d3748;
  position: relative;
  padding-left: 32px;
}

.suggestions-list li::before {
  content: '💡';
  position: absolute;
  left: 12px;
  top: 12px;
}

.weekly-stats {
  margin-bottom: 24px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.stat-item .stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
}

.stat-item .stat-label {
  font-size: 14px;
  color: #718096;
  margin-bottom: 8px;
}

.stat-change {
  font-size: 12px;
  font-weight: 600;
}

.stat-change.positive {
  color: #67c23a;
}

.stat-change.negative {
  color: #f56c6c;
}

.weekly-insights h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.insights-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.insight-card {
  padding: 20px;
  border-radius: 12px;
  border-left: 4px solid #409eff;
  background: #f8f9fa;
}

.insight-card.positive {
  border-left-color: #67c23a;
  background: #f0f9ff;
}

.insight-card.suggestion {
  border-left-color: #e6a23c;
  background: #fff7ed;
}

.insight-card.achievement {
  border-left-color: #409eff;
  background: #f0f9ff;
}

.insight-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.insight-header .insight-icon {
  font-size: 20px;
}

.insight-header .insight-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.insight-card .insight-content {
  font-size: 14px;
  color: #718096;
  margin-bottom: 12px;
}

.insight-action {
  text-align: right;
}

@media (max-width: 768px) {
  .overview-section {
    grid-template-columns: 1fr;
  }
  
  .stat-grid {
    grid-template-columns: 1fr;
  }
  
  .insights-grid {
    grid-template-columns: 1fr;
  }
  
  .report-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>

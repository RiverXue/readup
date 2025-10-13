<template>
  <div class="report-container">
    <h2>学习报告</h2>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card v-for="stat in statsCards" :key="stat.title" class="stat-card">
        <div class="stat-icon">
          <el-icon :size="40" :color="stat.color">
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-content">
          <h3>{{ stat.title }}</h3>
          <p class="stat-value">{{ stat.value }}</p>
          <p class="stat-desc">{{ stat.desc }}</p>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-area">
      <el-card class="chart-card">
        <template #header>
          <h3>词汇增长曲线</h3>
        </template>
        <div ref="growthChart" class="chart-container"></div>
      </el-card>

      <el-card class="chart-card">
        <template #header>
          <h3>每日阅读时长</h3>
        </template>
        <div ref="readingChart" class="chart-container"></div>
      </el-card>
    </div>

    <!-- 学习成就 -->
    <div class="achievements">
      <h3>学习成就</h3>
      <div class="achievement-list">
        <el-card v-for="achievement in achievements" :key="achievement.id"
                 class="achievement-item"
                 :class="{ 'achieved': achievement.achieved }">
          <div class="achievement-icon">
            <el-icon :size="30" :color="achievement.achieved ? '#67c23a' : '#909399'">
              <Trophy />
            </el-icon>
          </div>
          <div class="achievement-content">
            <h4>{{ achievement.name }}</h4>
            <p>{{ achievement.description }}</p>
            <el-progress
              v-if="achievement.progress < 100"
              :percentage="achievement.progress"
              :color="achievement.achieved ? '#67c23a' : '#409eff'"
            />
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { reportApi, vocabularyApi, learningApi } from '@/utils/api'
import { useUserStore } from '@/stores/user'
// 图标通过全局注册，无需单独导入

const growthChart = ref<HTMLElement>()
const readingChart = ref<HTMLElement>()
let growthChartInstance: echarts.ECharts | null = null
let readingChartInstance: echarts.ECharts | null = null

const userStore = useUserStore()

const statsCards = ref([
  {
    title: '生词量',
    value: '0',
    desc: '生词本中添加的单词总数',
    icon: 'Collection',
    color: '#409eff'
  },
  {
    title: '连续打卡',
    value: '0天',
    desc: '连续学习天数',
    icon: 'Clock',
    color: '#67c23a'
  },
  {
    title: '今日阅读',
    value: '0分钟',
    desc: '今日阅读时长',
    icon: 'Reading',
    color: '#e6a23c'
  },
  {
    title: '完成文章',
    value: '0篇',
    desc: '已完成阅读',
    icon: 'Document',
    color: '#909399'
  }
])

const achievements = ref([
  {
    id: 1,
    name: '初出茅庐',
    description: '完成第一篇英语文章阅读',
    progress: 0,
    achieved: false
  },
  {
    id: 2,
    name: '词汇达人',
    description: '累计学习100个新单词',
    progress: 0,
    achieved: false
  },
  {
    id: 3,
    name: '坚持不懈',
    description: '连续7天完成阅读任务',
    progress: 0,
    achieved: false
  },
  {
    id: 4,
    name: '阅读高手',
    description: '累计阅读10篇英语文章',
    progress: 0,
    achieved: false
  }
])

onMounted(async () => {
  // 先初始化图表，然后再加载数据
  initCharts()
  await loadReportData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (growthChartInstance) {
    growthChartInstance.dispose()
  }
  if (readingChartInstance) {
    readingChartInstance.dispose()
  }
  window.removeEventListener('resize', handleResize)
})

// 定义学习报告数据接口
interface ReportData {
  completedArticles: number
  totalWords: number
  streakDays: number
  totalReadingTime: number
  dates: string[]
  vocabularyCounts: number[]
  readingTimes: number[]
}

// 辅助函数：生成指定天数的日期数组（从今天往前推）
const generateDateArray = (days: number): string[] => {
  const today = new Date();
  const dates: string[] = [];
  
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(today.getDate() - i);
    dates.push(date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }));
  }
  
  return dates;
}

// 辅助函数：将数组填充或截断到指定长度
const padOrTruncateArray = (arr: number[], targetLength: number): number[] => {
  const result = [...arr];
  
  // 如果数组长度小于目标长度，用0填充
  while (result.length < targetLength) {
    result.push(0);
  }
  
  // 如果数组长度大于目标长度，截断
  if (result.length > targetLength) {
    result.length = targetLength;
  }
  
  return result;
}

// 辅助函数：将阅读数据映射到对应的日期位置
const mapReadingDataToDates = (dailyReadings: any[], dates: string[]): number[] => {
  // 创建日期到分钟数的映射
  const readingMap = new Map<string, number>();
  
  dailyReadings.forEach(item => {
    if (!item.date || typeof item.minutes !== 'number') return;
    
    // 将API返回的日期格式转换为与dates数组匹配的格式
    let dateKey = item.date;
    if (dateKey.includes('-')) {
      const parts = dateKey.split('-');
      dateKey = `${parts[1]}/${parts[2]}`;
    }
    
    readingMap.set(dateKey, item.minutes);
  });
  
  // 根据dates数组生成对应的分钟数数组
  return dates.map(date => readingMap.get(date) || 0);
}

// 在顶部添加接口定义
const reportData = ref<ReportData>({
  completedArticles: 0,
  totalWords: 0,
  streakDays: 0,
  totalReadingTime: 0,
  dates: [],
  vocabularyCounts: [],
  readingTimes: []
})

const loadReportData = async () => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      ElMessage.warning('请先登录以查看学习报告')
      // 重置为0数据
      reportData.value = {
        completedArticles: 0,
        totalWords: 0,
        streakDays: 0,
        totalReadingTime: 0,
        dates: [],
        vocabularyCounts: [],
        readingTimes: []
      }
      return
    }

    // 仪表盘综合数据
    const dashboardRes = await reportApi.getDashboardData(String(userId))
    // 更灵活地处理后端返回的数据格式
    const dash = dashboardRes?.data || {}
    console.log('Dashboard API response:', dashboardRes)

    // 词汇增长曲线
    const growthRes = await reportApi.getGrowthCurve(String(userId), 7)
    const growth = growthRes?.data || {}
    console.log('Growth API response:', growthRes)

    // 每日阅读时长
    const readingRes = await reportApi.getReadingTime(String(userId))
    const reading = readingRes?.data || {}
    console.log('Reading API response:', readingRes)

    // 尝试从词汇统计API获取数据
    try {
      const vocabularyStatsRes = await vocabularyApi.getVocabularyStats(String(userId))
      if (vocabularyStatsRes) {
        // 增强数据解析 - 适配report-service返回的today/summary数据结构
        const vocabularyStatsData = vocabularyStatsRes.data || {};

        // 更新词汇统计数据
        reportData.value.totalWords = vocabularyStatsData.totalWords || reportData.value.totalWords || 0;
      }
    } catch (error) {
      console.warn('词汇统计API调用失败，使用dashboard数据作为备选:', error)
      // 不影响主流程，继续使用dashboard数据
    }

    // 尝试从dailyCheckIn接口直接获取连续打卡天数
    let dailyCheckInData: number | undefined;
    try {
      const checkInResponse = await learningApi.dailyCheckIn(userId);
      if (typeof checkInResponse.data === 'number') {
        dailyCheckInData = checkInResponse.data;
        console.log('从dailyCheckIn接口获取连续打卡天数:', dailyCheckInData);
      }
    } catch (checkInError) {
      console.warn('获取连续打卡天数失败，使用dashboard数据作为备选:', checkInError);
      // 不影响主流程，继续使用dashboard数据
    }

    // 词汇增长数据解析
    const growthData = growth?.data || growth || {};
    const readingTimeData = reading?.data || reading || {};

    reportData.value = {
      // 完成文章数：优先从reading API获取，其次是dashboard
      completedArticles: typeof readingTimeData.totalArticles === 'number' ? readingTimeData.totalArticles :
                        (typeof dash.completedArticles === 'number' ? dash.completedArticles : 0),

      // 总词汇量：优先从词汇统计API获取，其次是dashboard或growth API
      totalWords: typeof reportData.value.totalWords === 'number' ? reportData.value.totalWords :
                 (typeof growthData.totalWords === 'number' ? growthData.totalWords :
                 (typeof dash.totalWords === 'number' ? dash.totalWords : 0)),

      // 连续打卡天数：优先从dailyCheckIn接口获取，其次是dashboard
    streakDays: typeof dailyCheckInData === 'number' ? dailyCheckInData :
               (typeof dash?.currentStreak === 'number' ? dash.currentStreak :
               (typeof dash?.streakDays === 'number' ? dash.streakDays : 0)),

      // 总阅读时长：优先从reading API获取，其次是dashboard
      totalReadingTime: typeof readingTimeData.todayMinutes === 'number' ? readingTimeData.todayMinutes :
                        (typeof dash.totalReadingTime === 'number' ? dash.totalReadingTime : 0),

      // 生成完整的7天日期数组（从今天往前推6天）
      dates: Array.isArray(growthData.dates) ? growthData.dates : generateDateArray(7),

      // 词汇增长曲线数据：从growth API的counts字段获取，不足则补0
      vocabularyCounts: Array.isArray(growthData.counts) ? 
                        padOrTruncateArray(growthData.counts, 7) : 
                        Array(7).fill(0),

      // 每日阅读时长数据：将dailyReadings数据匹配到正确的日期位置
      readingTimes: Array.isArray(readingTimeData.dailyReadings) ? 
                   mapReadingDataToDates(readingTimeData.dailyReadings, generateDateArray(7)) :
                   Array(7).fill(0)
    }

    console.log('Processed report data:', reportData.value);

    // 确保数据长度匹配（以防growthData.dates提供了不同长度的数据）
    const dateLength = reportData.value.dates.length;

    // 确保词汇量数据长度与日期长度匹配
    if (reportData.value.vocabularyCounts.length < dateLength) {
      const padding = Array(dateLength - reportData.value.vocabularyCounts.length).fill(0);
      reportData.value.vocabularyCounts = [...reportData.value.vocabularyCounts, ...padding];
    } else if (reportData.value.vocabularyCounts.length > dateLength) {
      reportData.value.vocabularyCounts = reportData.value.vocabularyCounts.slice(0, dateLength);
    }

    // 确保阅读时长数据长度与日期长度匹配
    if (reportData.value.readingTimes.length < dateLength) {
      const padding = Array(dateLength - reportData.value.readingTimes.length).fill(0);
      reportData.value.readingTimes = [...reportData.value.readingTimes, ...padding];
    } else if (reportData.value.readingTimes.length > dateLength) {
      reportData.value.readingTimes = reportData.value.readingTimes.slice(0, dateLength);
    }

    // 检查数据是否都是0，如果是，可能需要提供一些有意义的模拟数据（可选）
    // 这里已经有了基本的空数据处理，可以根据需要添加更复杂的模拟数据生成逻辑

    // 更新图表和统计卡片数据
    updateCharts()
    updateStatsCards()
  } catch (error) {
    console.error('获取报告数据失败:', error)
    ElMessage.error('获取报告数据失败，请稍后重试')
  }
}

// 更新图表数据
const updateCharts = () => {
  // 更新词汇增长图表
  if (growthChartInstance && reportData.value.dates.length > 0) {
    // 确保数据长度匹配
    const vocabularyData = reportData.value.vocabularyCounts.slice(0, reportData.value.dates.length)

    // 完全重新设置图表选项，而不仅仅是更新部分数据
    growthChartInstance.setOption({
      title: { text: '词汇增长趋势', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: reportData.value.dates
      },
      yAxis: { type: 'value' },
      series: [{
        data: vocabularyData,
        type: 'line',
        smooth: true,
        areaStyle: {},
        // 添加线条颜色和点标记以提高可视化效果
        lineStyle: { color: '#409eff' },
        itemStyle: { color: '#409eff' }
      }]
    })
  }

  // 更新阅读时长图表
  if (readingChartInstance && reportData.value.dates.length > 0) {
    // 确保数据长度匹配
    const readingData = reportData.value.readingTimes.slice(0, reportData.value.dates.length)

    // 完全重新设置图表选项，而不仅仅是更新部分数据
    readingChartInstance.setOption({
      title: { text: '每日阅读时长', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: reportData.value.dates
      },
      yAxis: { type: 'value', name: '分钟' },
      series: [{
        data: readingData,
        type: 'bar',
        itemStyle: { color: '#409eff' }
      }]
    })
  }

  console.log('图表数据已更新:', {
    dates: reportData.value.dates,
    vocabularyCounts: reportData.value.vocabularyCounts,
    readingTimes: reportData.value.readingTimes
  })
}

// 更新统计卡片数据
const updateStatsCards = () => {
  statsCards.value[0].value = String(reportData.value.totalWords || 0)
  statsCards.value[1].value = `${reportData.value.streakDays || 0}天`
  statsCards.value[2].value = `${reportData.value.totalReadingTime || 0}分钟`
  statsCards.value[3].value = `${reportData.value.completedArticles || 0}篇`

  // 更新成就进度
  updateAchievements()
}

// 更新成就进度
const updateAchievements = () => {
  // 初出茅庐：完成第一篇英语文章阅读
  achievements.value[0].progress = reportData.value.completedArticles >= 1 ? 100 : 0
  achievements.value[0].achieved = reportData.value.completedArticles >= 1

  // 词汇达人：累计学习100个新单词 - 使用Math.round修复浮点数精度问题
  achievements.value[1].progress = Math.round(Math.min((reportData.value.totalWords / 100) * 100, 100))
  achievements.value[1].achieved = reportData.value.totalWords >= 100

  // 坚持不懈：连续7天完成阅读任务 - 使用Math.round修复浮点数精度问题
  achievements.value[2].progress = Math.round(Math.min((reportData.value.streakDays / 7) * 100, 100))
  achievements.value[2].achieved = reportData.value.streakDays >= 7

  // 阅读高手：累计阅读10篇英语文章 - 使用Math.round修复浮点数精度问题
  achievements.value[3].progress = Math.round(Math.min((reportData.value.completedArticles / 10) * 100, 100))
  achievements.value[3].achieved = reportData.value.completedArticles >= 10
}
const initCharts = () => {
  // 先初始化空图表
  if (growthChart.value) {
    growthChartInstance = echarts.init(growthChart.value)
    growthChartInstance.setOption({
      title: { text: '词汇增长趋势', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: []
      },
      yAxis: { type: 'value' },
      series: [{
        data: [],
        type: 'line',
        smooth: true,
        areaStyle: {}
      }]
    })
  }

  if (readingChart.value) {
    readingChartInstance = echarts.init(readingChart.value)
    readingChartInstance.setOption({
      title: { text: '每日阅读时长', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: []
      },
      yAxis: { type: 'value', name: '分钟' },
      series: [{
        data: [],
        type: 'bar',
        itemStyle: { color: '#409eff' }
      }]
    })
  }
}



const handleResize = () => {
  if (growthChartInstance) {
    growthChartInstance.resize()
  }
  if (readingChartInstance) {
    readingChartInstance.resize()
  }
}
</script>

<style scoped>
@import '@/assets/design-system.css';

.report-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6);
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.report-container h2 {
  font-size: var(--text-4xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-8);
  text-align: center;
  position: relative;
}

.report-container h2::after {
  content: '';
  position: absolute;
  bottom: -var(--space-3);
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-500), var(--warm-orange));
  border-radius: var(--radius-sm);
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-6);
  margin-bottom: var(--space-12);
}

.stat-card {
  display: flex;
  align-items: center;
  padding: var(--space-6);
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
  overflow: hidden;
  position: relative;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--primary-200), transparent);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-icon {
  margin-right: var(--space-4);
}

.stat-content h3 {
  margin: 0 0 var(--space-1) 0;
  color: var(--text-secondary);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
}

.stat-value {
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  margin: var(--space-1) 0;
  color: var(--primary-600);
}

.stat-desc {
  margin: 0;
  color: var(--text-tertiary);
  font-size: var(--text-sm);
}

.charts-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-6);
  margin-bottom: var(--space-12);
}

.chart-card {
  padding: var(--space-6);
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
}

.chart-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.chart-container {
  width: 100%;
  height: 300px;
}

.achievements h3 {
  margin-bottom: var(--space-5);
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.achievement-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

.achievement-item {
  display: flex;
  align-items: center;
  padding: var(--space-4);
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
}

.achievement-item.achieved {
  border-color: var(--accent-success);
  background: rgba(16, 185, 129, 0.05);
}

.achievement-icon {
  margin-right: var(--space-4);
}

.achievement-content h4 {
  margin: 0 0 5px 0;
}

.achievement-content p {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr 1fr;
  }

  .charts-area {
    grid-template-columns: 1fr;
  }

  .achievement-list {
    grid-template-columns: 1fr;
  }
}
</style>

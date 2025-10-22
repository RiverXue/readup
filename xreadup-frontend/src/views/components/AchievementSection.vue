<template>
  <div class="achievement-section">
    <div class="section-header">
      <h2>🏆 学习成就</h2>
      <div class="header-actions">
        <el-button size="small" @click="refreshAchievements">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>
    
    <div class="achievement-content" v-loading="loading">
      <!-- 成就概览 -->
      <div class="achievement-overview">
        <div class="overview-card">
          <div class="card-icon">🏆</div>
          <div class="card-content">
            <div class="card-value">{{ achievementData?.totalAchievements || 0 }}</div>
            <div class="card-label">已获得成就</div>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-icon">🎯</div>
          <div class="card-content">
            <div class="card-value">{{ achievementData?.totalMilestones || 0 }}</div>
            <div class="card-label">里程碑</div>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-icon">⭐</div>
          <div class="card-content">
            <div class="card-value">{{ calculateAchievementScore() }}</div>
            <div class="card-label">成就积分</div>
          </div>
        </div>
      </div>
      
      <!-- 成就时间线 -->
      <div class="achievement-timeline">
        <h3>📅 成就时间线</h3>
        <div class="timeline-container">
          <div 
            v-for="achievement in achievementData?.achievements" 
            :key="achievement.id"
            class="timeline-item"
            :class="achievement.type"
            @click="handleAchievementClick(achievement)"
          >
            <div class="timeline-marker">
              <div class="marker-icon">{{ achievement.icon }}</div>
            </div>
            <div class="timeline-content">
              <div class="timeline-title">{{ achievement.title }}</div>
              <div class="timeline-description">{{ achievement.description }}</div>
              <div class="timeline-date">{{ formatDate(achievement.unlockedAt) }}</div>
            </div>
            <div class="timeline-action">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 里程碑展示 -->
      <div class="milestone-showcase">
        <h3>🎯 学习里程碑</h3>
        <div class="milestone-grid">
          <div 
            v-for="milestone in achievementData?.milestones" 
            :key="milestone.id"
            class="milestone-card"
            :class="{ 'achieved': milestone.achieved }"
            @click="handleMilestoneClick(milestone)"
          >
            <div class="milestone-icon">
              <el-icon :size="32" :color="milestone.achieved ? '#67c23a' : '#909399'">
                <Trophy />
              </el-icon>
            </div>
            <div class="milestone-content">
              <div class="milestone-title">{{ milestone.title }}</div>
              <div class="milestone-description">{{ milestone.description }}</div>
              <div class="milestone-date">{{ formatDate(milestone.date) }}</div>
            </div>
            <div class="milestone-status">
              <el-tag 
                :type="milestone.achieved ? 'success' : 'info'"
                size="small"
              >
                {{ milestone.achieved ? '已完成' : '进行中' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 成就统计 -->
      <div class="achievement-stats">
        <h3>📊 成就统计</h3>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-label">连续学习成就</div>
            <div class="stat-value">{{ getAchievementCountByType('streak') }}</div>
            <div class="stat-progress">
              <el-progress 
                :percentage="(getAchievementCountByType('streak') / 5) * 100" 
                :show-text="false"
                color="#67c23a"
              />
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-label">词汇学习成就</div>
            <div class="stat-value">{{ getAchievementCountByType('vocabulary') }}</div>
            <div class="stat-progress">
              <el-progress 
                :percentage="(getAchievementCountByType('vocabulary') / 5) * 100" 
                :show-text="false"
                color="#409eff"
              />
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-label">阅读学习成就</div>
            <div class="stat-value">{{ getAchievementCountByType('reading') }}</div>
            <div class="stat-progress">
              <el-progress 
                :percentage="(getAchievementCountByType('reading') / 5) * 100" 
                :show-text="false"
                color="#e6a23c"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Trophy, ArrowRight, Refresh } from '@element-plus/icons-vue'

interface Props {
  achievementData: any
  loading: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  achievementClick: [achievement: any]
}>()

const calculateAchievementScore = () => {
  if (!props.achievementData?.achievements) return 0
  
  return props.achievementData.achievements.reduce((score: number, achievement: any) => {
    const baseScore = 10
    const typeMultiplier = {
      'streak': 1.5,
      'vocabulary': 1.2,
      'reading': 1.0
    }
    return score + baseScore * (typeMultiplier[achievement.type] || 1)
  }, 0)
}

const getAchievementCountByType = (type: string) => {
  if (!props.achievementData?.achievements) return 0
  
  return props.achievementData.achievements.filter((achievement: any) => 
    achievement.type === type
  ).length
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const handleAchievementClick = (achievement: any) => {
  emit('achievementClick', achievement)
}

const handleMilestoneClick = (milestone: any) => {
  console.log('里程碑点击:', milestone)
}

const refreshAchievements = () => {
  console.log('刷新成就数据')
}
</script>

<style scoped>
.achievement-section {
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

.achievement-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.achievement-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.overview-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #718096;
}

.achievement-timeline {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.achievement-timeline h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.timeline-container {
  position: relative;
}

.timeline-container::before {
  content: '';
  position: absolute;
  left: 20px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, #409eff, #67c23a);
}

.timeline-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.timeline-item:hover {
  background: rgba(64, 158, 255, 0.05);
  border-radius: 8px;
  padding: 16px;
  margin: 0 -16px;
}

.timeline-marker {
  position: relative;
  z-index: 1;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  border: 3px solid #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.timeline-item.streak .timeline-marker {
  border-color: #f56c6c;
}

.timeline-item.vocabulary .timeline-marker {
  border-color: #67c23a;
}

.timeline-item.reading .timeline-marker {
  border-color: #e6a23c;
}

.marker-icon {
  font-size: 16px;
}

.timeline-content {
  flex: 1;
}

.timeline-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
}

.timeline-description {
  font-size: 14px;
  color: #718096;
  margin-bottom: 4px;
}

.timeline-date {
  font-size: 12px;
  color: #a0aec0;
}

.timeline-action {
  color: #a0aec0;
  transition: color 0.3s ease;
}

.timeline-item:hover .timeline-action {
  color: #409eff;
}

.milestone-showcase {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.milestone-showcase h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.milestone-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.milestone-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 16px;
}

.milestone-card.achieved {
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  border: 2px solid #67c23a;
}

.milestone-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.milestone-icon {
  flex-shrink: 0;
}

.milestone-content {
  flex: 1;
}

.milestone-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
}

.milestone-description {
  font-size: 14px;
  color: #718096;
  margin-bottom: 4px;
}

.milestone-date {
  font-size: 12px;
  color: #a0aec0;
}

.milestone-status {
  flex-shrink: 0;
}

.achievement-stats {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.achievement-stats h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #718096;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 12px;
}

.stat-progress {
  width: 100%;
}

@media (max-width: 768px) {
  .achievement-overview {
    grid-template-columns: 1fr;
  }
  
  .milestone-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .timeline-item {
    flex-direction: column;
    text-align: center;
  }
  
  .timeline-container::before {
    display: none;
  }
}
</style>

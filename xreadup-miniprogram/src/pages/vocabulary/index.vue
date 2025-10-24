<template>
  <view class="vocabulary-container">
    <!-- 学习统计卡片 -->
    <view class="stats-card">
      <view class="stats-header">
        <text class="stats-title">学习统计</text>
        <button class="refresh-btn" @click="refreshData">
          <image src="/static/refresh-icon.png" class="refresh-icon" />
        </button>
      </view>
      
      <view class="stats-content">
        <view class="stat-item">
          <text class="stat-number">{{ vocabularyStore.learningStats.totalWords }}</text>
          <text class="stat-label">总单词</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-number">{{ vocabularyStore.learningStats.masteredWords }}</text>
          <text class="stat-label">已掌握</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-number">{{ vocabularyStore.learningStats.streakDays }}</text>
          <text class="stat-label">连续天数</text>
        </view>
      </view>
      
      <!-- 学习进度 -->
      <view class="progress-section">
        <view class="progress-header">
          <text class="progress-label">学习进度</text>
          <text class="progress-percent">{{ vocabularyStore.learningProgress }}%</text>
        </view>
        <view class="progress-bar">
          <view 
            class="progress-fill" 
            :style="{ width: vocabularyStore.learningProgress + '%' }"
          ></view>
        </view>
      </view>
    </view>

    <!-- 复习计划 -->
    <view class="review-plan-card">
      <view class="plan-header">
        <text class="plan-title">复习计划</text>
        <text class="plan-subtitle">基于艾宾浩斯遗忘曲线</text>
      </view>
      
      <view class="plan-items">
        <view class="plan-item">
          <text class="plan-count">{{ vocabularyStore.reviewPlan.todayReview }}</text>
          <text class="plan-label">今日复习</text>
        </view>
        <view class="plan-item">
          <text class="plan-count">{{ vocabularyStore.reviewPlan.overdueReview }}</text>
          <text class="plan-label">逾期复习</text>
        </view>
        <view class="plan-item">
          <text class="plan-count">{{ vocabularyStore.reviewPlan.thisWeekReview }}</text>
          <text class="plan-label">本周复习</text>
        </view>
      </view>
    </view>

    <!-- 快速操作 -->
    <view class="quick-actions">
      <view class="action-item" @click="startFlashcardReview">
        <image src="/static/flashcard-icon.png" class="action-icon" />
        <text class="action-text">闪卡复习</text>
      </view>
      <view class="action-item" @click="startQuickReview">
        <image src="/static/quick-icon.png" class="action-icon" />
        <text class="action-text">快速刷词</text>
      </view>
      <view class="action-item" @click="startDictationReview">
        <image src="/static/dictation-icon.png" class="action-icon" />
        <text class="action-text">听写练习</text>
      </view>
      <view class="action-item" @click="goToAddWords">
        <image src="/static/add-icon.png" class="action-icon" />
        <text class="action-text">添加单词</text>
      </view>
    </view>

    <!-- 单词列表 -->
    <view class="words-section">
      <view class="section-header">
        <text class="section-title">我的生词本</text>
        <view class="filter-tabs">
          <text 
            class="filter-tab"
            :class="{ active: selectedFilter === 'all' }"
            @click="handleFilterChange('all')"
          >全部</text>
          <text 
            class="filter-tab"
            :class="{ active: selectedFilter === 'learning' }"
            @click="handleFilterChange('learning')"
          >学习中</text>
          <text 
            class="filter-tab"
            :class="{ active: selectedFilter === 'mastered' }"
            @click="handleFilterChange('mastered')"
          >已掌握</text>
        </view>
      </view>

      <view class="words-list" v-if="filteredWords.length > 0">
        <view 
          class="word-item"
          v-for="word in filteredWords"
          :key="word.id"
          @click="goToWordDetail(word.id)"
        >
          <view class="word-content">
            <view class="word-header">
              <text class="word-text">{{ word.word }}</text>
              <text class="word-phonetic">/{{ word.phonetic }}/</text>
            </view>
            <text class="word-meaning">{{ word.meaning }}</text>
            <view class="word-meta">
              <text class="word-difficulty">{{ getDifficultyText(word.difficulty) }}</text>
              <text class="word-level">掌握度: {{ word.masteryLevel }}%</text>
              <text class="word-review-count">复习{{ word.reviewCount }}次</text>
            </view>
          </view>
          
          <view class="word-actions">
            <button class="action-btn" @click.stop="playPronunciation(word.word)">
              <image src="/static/play-icon.png" class="btn-icon" />
            </button>
            <button class="action-btn" @click.stop="toggleMastery(word)">
              <image 
                :src="word.isMastered ? '/static/mastered-icon.png' : '/static/learning-icon.png'" 
                class="btn-icon"
              />
            </button>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading-container" v-if="vocabularyStore.loading">
        <view class="loading"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-else-if="!vocabularyStore.loading && filteredWords.length === 0">
        <image src="/static/empty-words.png" class="empty-icon" />
        <text class="empty-text">{{ getEmptyText() }}</text>
        <button class="add-btn" @click="goToAddWords">添加单词</button>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="vocabularyStore.hasMore && !vocabularyStore.loading">
        <button class="load-more-btn" @click="loadMoreWords">加载更多</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onShow } from 'vue'
import { useVocabularyStore } from '@/store/vocabulary'
import { useUserStore } from '@/store/user'

const vocabularyStore = useVocabularyStore()
const userStore = useUserStore()

// 响应式数据
const selectedFilter = ref('all')

// 计算属性
const filteredWords = computed(() => {
  switch (selectedFilter.value) {
    case 'learning':
      return vocabularyStore.learningWords
    case 'mastered':
      return vocabularyStore.masteredWords
    default:
      return vocabularyStore.vocabularyList
  }
})

// 刷新数据
const refreshData = async () => {
  try {
    await vocabularyStore.refreshData()
    uni.showToast({
      title: '刷新成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('刷新失败:', error)
    uni.showToast({
      title: '刷新失败',
      icon: 'error'
    })
  }
}

// 开始闪卡复习
const startFlashcardReview = () => {
  if (!userStore.checkLoginStatus()) return
  
  if (vocabularyStore.reviewWords.length === 0) {
    uni.showToast({
      title: '没有需要复习的单词',
      icon: 'none'
    })
    return
  }
  
  uni.navigateTo({
    url: '/pages/vocabulary/review?type=flashcard'
  })
}

// 开始快速刷词
const startQuickReview = () => {
  if (!userStore.checkLoginStatus()) return
  
  if (vocabularyStore.reviewWords.length === 0) {
    uni.showToast({
      title: '没有需要复习的单词',
      icon: 'none'
    })
    return
  }
  
  uni.navigateTo({
    url: '/pages/vocabulary/review?type=quick'
  })
}

// 开始听写练习
const startDictationReview = () => {
  if (!userStore.checkLoginStatus()) return
  
  if (vocabularyStore.reviewWords.length === 0) {
    uni.showToast({
      title: '没有需要复习的单词',
      icon: 'none'
    })
    return
  }
  
  uni.navigateTo({
    url: '/pages/vocabulary/dictation'
  })
}

// 跳转到添加单词页面
const goToAddWords = () => {
  if (!userStore.checkLoginStatus()) return
  
  uni.navigateTo({
    url: '/pages/vocabulary/add'
  })
}

// 跳转到单词详情
const goToWordDetail = (id: number) => {
  uni.navigateTo({
    url: `/pages/vocabulary/detail?id=${id}`
  })
}

// 播放发音
const playPronunciation = (word: string) => {
  // 这里应该调用TTS API播放单词发音
  uni.showToast({
    title: '播放发音',
    icon: 'success'
  })
}

// 切换掌握状态
const toggleMastery = async (word: any) => {
  try {
    if (word.isMastered) {
      await vocabularyStore.resetWordProgress(word.id)
      uni.showToast({
        title: '已重置学习进度',
        icon: 'success'
      })
    } else {
      await vocabularyStore.markAsMastered(word.id)
      uni.showToast({
        title: '已标记为已掌握',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('操作失败:', error)
    uni.showToast({
      title: '操作失败',
      icon: 'error'
    })
  }
}

// 筛选切换
const handleFilterChange = (filter: string) => {
  selectedFilter.value = filter
}

// 加载更多单词
const loadMoreWords = () => {
  vocabularyStore.loadMoreWords()
}

// 获取难度文本
const getDifficultyText = (difficulty: string) => {
  const difficultyMap = {
    'easy': '简单',
    'medium': '中等',
    'hard': '困难'
  }
  return difficultyMap[difficulty] || '未知'
}

// 获取空状态文本
const getEmptyText = () => {
  switch (selectedFilter.value) {
    case 'learning':
      return '没有正在学习的单词'
    case 'mastered':
      return '没有已掌握的单词'
    default:
      return '生词本为空，快去添加单词吧'
  }
}

// 页面加载
onMounted(async () => {
  if (userStore.isLoggedIn) {
    await vocabularyStore.refreshData()
  }
})

// 页面显示时刷新
onShow(async () => {
  if (userStore.isLoggedIn) {
    await vocabularyStore.loadLearningStats()
    await vocabularyStore.loadReviewPlan()
  }
})
</script>

<style lang="scss" scoped>
.vocabulary-container {
  min-height: 100vh;
  background-color: var(--background-color);
  padding: 20rpx;
}

.stats-card {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  
  .stats-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;
    
    .stats-title {
      font-size: 32rpx;
      font-weight: bold;
      color: var(--text-primary);
    }
    
    .refresh-btn {
      width: 60rpx;
      height: 60rpx;
      background: #F5F5F5;
      border-radius: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border: none;
      
      .refresh-icon {
        width: 32rpx;
        height: 32rpx;
      }
    }
  }
  
  .stats-content {
    display: flex;
    align-items: center;
    justify-content: space-around;
    margin-bottom: 30rpx;
    
    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .stat-number {
        font-size: 36rpx;
        font-weight: bold;
        color: var(--primary-color);
        margin-bottom: 8rpx;
      }
      
      .stat-label {
        font-size: 24rpx;
        color: var(--text-secondary);
      }
    }
    
    .stat-divider {
      width: 1rpx;
      height: 60rpx;
      background: var(--border-color);
    }
  }
  
  .progress-section {
    .progress-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12rpx;
      
      .progress-label {
        font-size: 26rpx;
        color: var(--text-primary);
      }
      
      .progress-percent {
        font-size: 26rpx;
        font-weight: bold;
        color: var(--primary-color);
      }
    }
    
    .progress-bar {
      height: 8rpx;
      background: #F5F5F5;
      border-radius: 4rpx;
      overflow: hidden;
      
      .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, var(--primary-color), #4CAF50);
        border-radius: 4rpx;
        transition: width 0.3s ease;
      }
    }
  }
}

.review-plan-card {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  
  .plan-header {
    margin-bottom: 20rpx;
    
    .plan-title {
      font-size: 28rpx;
      font-weight: bold;
      color: var(--text-primary);
      margin-bottom: 8rpx;
      display: block;
    }
    
    .plan-subtitle {
      font-size: 22rpx;
      color: var(--text-secondary);
    }
  }
  
  .plan-items {
    display: flex;
    justify-content: space-around;
    
    .plan-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .plan-count {
        font-size: 32rpx;
        font-weight: bold;
        color: var(--warning-color);
        margin-bottom: 8rpx;
      }
      
      .plan-label {
        font-size: 22rpx;
        color: var(--text-secondary);
      }
    }
  }
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 20rpx;
  
  .action-item {
    background: white;
    border-radius: 16rpx;
    padding: 40rpx 20rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    
    &:active {
      transform: scale(0.95);
    }
    
    .action-icon {
      width: 60rpx;
      height: 60rpx;
      margin-bottom: 16rpx;
    }
    
    .action-text {
      font-size: 26rpx;
      color: var(--text-primary);
      font-weight: 500;
    }
  }
}

.words-section {
  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 28rpx;
      font-weight: bold;
      color: var(--text-primary);
    }
    
    .filter-tabs {
      display: flex;
      gap: 16rpx;
      
      .filter-tab {
        padding: 8rpx 16rpx;
        font-size: 22rpx;
        color: var(--text-secondary);
        background: #F5F5F5;
        border-radius: 12rpx;
        transition: all 0.3s ease;
        
        &.active {
          background: var(--primary-color);
          color: white;
        }
      }
    }
  }
  
  .words-list {
    .word-item {
      background: white;
      border-radius: 16rpx;
      padding: 30rpx;
      margin-bottom: 16rpx;
      display: flex;
      align-items: center;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;
      
      &:active {
        transform: scale(0.98);
      }
      
      .word-content {
        flex: 1;
        margin-right: 20rpx;
        
        .word-header {
          display: flex;
          align-items: center;
          margin-bottom: 12rpx;
          
          .word-text {
            font-size: 32rpx;
            font-weight: bold;
            color: var(--text-primary);
            margin-right: 16rpx;
          }
          
          .word-phonetic {
            font-size: 24rpx;
            color: var(--primary-color);
          }
        }
        
        .word-meaning {
          font-size: 26rpx;
          color: var(--text-secondary);
          line-height: 1.4;
          margin-bottom: 12rpx;
          display: block;
        }
        
        .word-meta {
          display: flex;
          align-items: center;
          gap: 16rpx;
          
          .word-difficulty {
            font-size: 20rpx;
            color: var(--warning-color);
            background: #FFF3E0;
            padding: 4rpx 8rpx;
            border-radius: 8rpx;
          }
          
          .word-level {
            font-size: 20rpx;
            color: var(--text-secondary);
          }
          
          .word-review-count {
            font-size: 20rpx;
            color: var(--text-secondary);
          }
        }
      }
      
      .word-actions {
        display: flex;
        flex-direction: column;
        gap: 12rpx;
        
        .action-btn {
          width: 60rpx;
          height: 60rpx;
          background: #F5F5F5;
          border-radius: 30rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          border: none;
          
          .btn-icon {
            width: 24rpx;
            height: 24rpx;
          }
        }
      }
    }
  }
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  
  .loading {
    width: 40rpx;
    height: 40rpx;
    border: 4rpx solid #f3f3f3;
    border-top: 4rpx solid var(--primary-color);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 20rpx;
  }
  
  .loading-text {
    font-size: 26rpx;
    color: var(--text-secondary);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  
  .empty-icon {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 40rpx;
    opacity: 0.6;
  }
  
  .empty-text {
    font-size: 28rpx;
    color: var(--text-secondary);
    margin-bottom: 40rpx;
  }
  
  .add-btn {
    padding: 16rpx 32rpx;
    background: var(--primary-color);
    color: white;
    border-radius: 24rpx;
    font-size: 26rpx;
    border: none;
  }
}

.load-more {
  display: flex;
  justify-content: center;
  padding: 40rpx 0;
  
  .load-more-btn {
    padding: 16rpx 32rpx;
    background: white;
    color: var(--primary-color);
    border: 1rpx solid var(--primary-color);
    border-radius: 24rpx;
    font-size: 26rpx;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>

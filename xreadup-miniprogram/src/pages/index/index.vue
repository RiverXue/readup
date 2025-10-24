<template>
  <view class="home-container">
    <!-- 顶部状态栏 -->
    <view class="status-bar safe-area-inset-top">
      <view class="status-content">
        <view class="user-info" @click="goToProfile">
          <image 
            :src="userStore.user?.avatar || '/static/default-avatar.png'" 
            class="avatar"
            mode="aspectFill"
          />
          <view class="user-details">
            <text class="username">{{ userStore.user?.nickname || '未登录' }}</text>
            <text class="user-level">{{ getUserLevelText() }}</text>
          </view>
        </view>
        
        <view class="checkin-btn" @click="handleCheckIn" v-if="userStore.isLoggedIn">
          <image src="/static/checkin-icon.png" class="checkin-icon" />
          <text class="checkin-text">打卡</text>
        </view>
      </view>
    </view>
    
    <!-- 今日学习统计 -->
    <view class="today-stats" v-if="userStore.isLoggedIn">
      <view class="stats-card">
        <view class="stat-item">
          <text class="stat-number">{{ todaySummary?.studyTime || 0 }}</text>
          <text class="stat-label">学习时长(分钟)</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-number">{{ todaySummary?.wordsLearned || 0 }}</text>
          <text class="stat-label">新学单词</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-number">{{ todaySummary?.articlesRead || 0 }}</text>
          <text class="stat-label">阅读文章</text>
        </view>
      </view>
    </view>
    
    <!-- 快速操作 -->
    <view class="quick-actions">
      <view class="section-title">
        <text class="title-text">快速开始</text>
      </view>
      
      <view class="actions-grid">
        <view class="action-item" @click="goToArticleList">
          <image src="/static/action-read.png" class="action-icon" />
          <text class="action-text">阅读文章</text>
        </view>
        
        <view class="action-item" @click="goToVocabulary">
          <image src="/static/action-vocab.png" class="action-icon" />
          <text class="action-text">生词本</text>
        </view>
        
        <view class="action-item" @click="goToAIAssistant">
          <image src="/static/action-ai.png" class="action-icon" />
          <text class="action-text">AI助手</text>
        </view>
        
        <view class="action-item" @click="goToReport">
          <image src="/static/action-report.png" class="action-icon" />
          <text class="action-text">学习报告</text>
        </view>
      </view>
    </view>
    
    <!-- 推荐文章 -->
    <view class="recommended-articles">
      <view class="section-title">
        <text class="title-text">推荐文章</text>
        <text class="more-text" @click="goToArticleList">更多</text>
      </view>
      
      <view class="articles-list" v-if="recommendedArticles.length > 0">
        <view 
          class="article-item" 
          v-for="article in recommendedArticles" 
          :key="article.id"
          @click="goToArticleDetail(article.id)"
        >
          <view class="article-content">
            <text class="article-title">{{ article.title }}</text>
            <text class="article-summary">{{ article.summary }}</text>
            <view class="article-meta">
              <text class="article-source">{{ article.source }}</text>
              <text class="article-time">{{ formatTime(article.publishTime) }}</text>
            </view>
          </view>
          <image 
            :src="article.imageUrl || '/static/default-article.png'" 
            class="article-image"
            mode="aspectFill"
          />
        </view>
      </view>
      
      <view class="empty-state" v-else-if="!loading">
        <image src="/static/empty-articles.png" class="empty-icon" />
        <text class="empty-text">暂无推荐文章</text>
      </view>
    </view>
    
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <view class="loading"></view>
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { articleApi, type Article } from '@/api/article'

const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const recommendedArticles = ref<Article[]>([])
const todaySummary = ref<any>(null)

// 获取用户等级文本
const getUserLevelText = () => {
  if (!userStore.isLoggedIn) return '游客'
  
  const levelMap = {
    free: '免费用户',
    basic: '基础会员',
    pro: '专业会员',
    enterprise: '企业会员',
    guest: '游客'
  }
  
  return levelMap[userStore.userLevel] || '免费用户'
}

// 处理打卡
const handleCheckIn = async () => {
  if (!userStore.checkLoginStatus()) return
  
  try {
    const result = await userStore.dailyCheckIn()
    
    uni.showToast({
      title: `打卡成功！连续${result.streakDays}天`,
      icon: 'success',
      duration: 2000
    })
    
    // 刷新今日统计
    await loadTodaySummary()
  } catch (error) {
    console.error('打卡失败:', error)
    uni.showToast({
      title: '打卡失败，请重试',
      icon: 'error'
    })
  }
}

// 加载今日学习统计
const loadTodaySummary = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const summary = await userStore.getTodaySummary()
    todaySummary.value = summary
  } catch (error) {
    console.error('加载今日统计失败:', error)
  }
}

// 加载推荐文章
const loadRecommendedArticles = async () => {
  loading.value = true
  
  try {
    const response = await articleApi.getRecommendedArticles(5)
    recommendedArticles.value = response.data
  } catch (error) {
    console.error('加载推荐文章失败:', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'error'
    })
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (timeStr: string) => {
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - time.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return time.toLocaleDateString()
  }
}

// 页面跳转方法
const goToProfile = () => {
  if (!userStore.checkLoginStatus()) return
  uni.navigateTo({
    url: '/pages/profile/index'
  })
}

const goToArticleList = () => {
  uni.switchTab({
    url: '/pages/article/list'
  })
}

const goToVocabulary = () => {
  if (!userStore.checkLoginStatus()) return
  uni.switchTab({
    url: '/pages/vocabulary/index'
  })
}

const goToAIAssistant = () => {
  if (!userStore.checkLoginStatus()) return
  uni.navigateTo({
    url: '/pages/ai-assistant/chat'
  })
}

const goToReport = () => {
  if (!userStore.checkLoginStatus()) return
  uni.switchTab({
    url: '/pages/report/index'
  })
}

const goToArticleDetail = (id: number) => {
  uni.navigateTo({
    url: `/pages/article/detail?id=${id}`
  })
}

// 页面加载
onMounted(async () => {
  // 自动登录
  await userStore.autoLogin()
  
  // 加载数据
  await Promise.all([
    loadTodaySummary(),
    loadRecommendedArticles()
  ])
})

// 页面显示时刷新数据
onShow(() => {
  if (userStore.isLoggedIn) {
    loadTodaySummary()
  }
})
</script>

<style lang="scss" scoped>
.home-container {
  min-height: 100vh;
  background-color: var(--background-color);
}

.status-bar {
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
  padding: 20rpx 30rpx;
  
  .status-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .user-info {
      display: flex;
      align-items: center;
      
      .avatar {
        width: 80rpx;
        height: 80rpx;
        border-radius: 40rpx;
        margin-right: 20rpx;
        border: 2rpx solid rgba(255, 255, 255, 0.3);
      }
      
      .user-details {
        display: flex;
        flex-direction: column;
        
        .username {
          font-size: 32rpx;
          font-weight: bold;
          color: white;
          margin-bottom: 4rpx;
        }
        
        .user-level {
          font-size: 24rpx;
          color: rgba(255, 255, 255, 0.8);
        }
      }
    }
    
    .checkin-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16rpx;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 16rpx;
      
      .checkin-icon {
        width: 32rpx;
        height: 32rpx;
        margin-bottom: 8rpx;
      }
      
      .checkin-text {
        font-size: 22rpx;
        color: white;
      }
    }
  }
}

.today-stats {
  padding: 30rpx;
  
  .stats-card {
    background: white;
    border-radius: 16rpx;
    padding: 30rpx;
    display: flex;
    align-items: center;
    justify-content: space-around;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    
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
}

.quick-actions {
  padding: 0 30rpx 30rpx;
  
  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;
    
    .title-text {
      font-size: 32rpx;
      font-weight: bold;
      color: var(--text-primary);
    }
    
    .more-text {
      font-size: 26rpx;
      color: var(--primary-color);
    }
  }
  
  .actions-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;
    
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
}

.recommended-articles {
  padding: 0 30rpx 30rpx;
  
  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;
    
    .title-text {
      font-size: 32rpx;
      font-weight: bold;
      color: var(--text-primary);
    }
    
    .more-text {
      font-size: 26rpx;
      color: var(--primary-color);
    }
  }
  
  .articles-list {
    .article-item {
      background: white;
      border-radius: 16rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;
      display: flex;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
      
      .article-content {
        flex: 1;
        margin-right: 20rpx;
        
        .article-title {
          font-size: 28rpx;
          font-weight: bold;
          color: var(--text-primary);
          line-height: 1.4;
          margin-bottom: 12rpx;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
        .article-summary {
          font-size: 24rpx;
          color: var(--text-secondary);
          line-height: 1.4;
          margin-bottom: 16rpx;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
        .article-meta {
          display: flex;
          align-items: center;
          justify-content: space-between;
          
          .article-source {
            font-size: 22rpx;
            color: var(--primary-color);
          }
          
          .article-time {
            font-size: 22rpx;
            color: var(--text-secondary);
          }
        }
      }
      
      .article-image {
        width: 120rpx;
        height: 120rpx;
        border-radius: 12rpx;
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
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>

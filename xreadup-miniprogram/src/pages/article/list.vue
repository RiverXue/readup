<template>
  <view class="article-list-container">
    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-bar">
        <image src="/static/search-icon.png" class="search-icon" />
        <input 
          class="search-input"
          placeholder="搜索文章..."
          v-model="searchKeyword"
          @confirm="handleSearch"
          @input="handleSearchInput"
        />
        <view class="search-btn" @click="handleSearch" v-if="searchKeyword">
          <text class="search-text">搜索</text>
        </view>
      </view>
    </view>

    <!-- 分类筛选 -->
    <view class="filter-section">
      <scroll-view class="filter-scroll" scroll-x="true">
        <view class="filter-list">
          <view 
            class="filter-item"
            :class="{ active: selectedCategory === item.value }"
            v-for="item in categories"
            :key="item.value"
            @click="handleCategoryChange(item.value)"
          >
            <text class="filter-text">{{ item.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 文章列表 -->
    <view class="articles-section">
      <view class="articles-list" v-if="articles.length > 0">
        <view 
          class="article-item"
          v-for="article in articles"
          :key="article.id"
          @click="goToArticleDetail(article.id)"
        >
          <view class="article-content">
            <text class="article-title">{{ article.title }}</text>
            <text class="article-summary">{{ article.summary }}</text>
            
            <view class="article-meta">
              <view class="meta-left">
                <text class="article-source">{{ article.source }}</text>
                <text class="article-time">{{ formatTime(article.publishTime) }}</text>
              </view>
              <view class="meta-right">
                <text class="article-difficulty">{{ getDifficultyText(article.difficulty) }}</text>
                <text class="article-read-time">{{ article.readTime }}分钟</text>
              </view>
            </view>
            
            <view class="article-tags">
              <text class="tag" v-for="tag in article.tags" :key="tag">{{ tag }}</text>
            </view>
          </view>
          
          <image 
            :src="article.imageUrl || '/static/default-article.png'" 
            class="article-image"
            mode="aspectFill"
          />
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading-container" v-if="loading">
        <view class="loading"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-else-if="!loading && articles.length === 0">
        <image src="/static/empty-articles.png" class="empty-icon" />
        <text class="empty-text">{{ searchKeyword ? '没有找到相关文章' : '暂无文章' }}</text>
        <button class="refresh-btn" @click="loadArticles">刷新</button>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMore && !loading">
        <button class="load-more-btn" @click="loadMore">加载更多</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onShow } from 'vue'
import { articleApi, type Article, type ArticleListParams } from '@/api/article'

// 响应式数据
const loading = ref(false)
const articles = ref<Article[]>([])
const searchKeyword = ref('')
const selectedCategory = ref('all')
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)

// 分类选项
const categories = ref([
  { label: '全部', value: 'all' },
  { label: '科技', value: 'technology' },
  { label: '财经', value: 'finance' },
  { label: '体育', value: 'sports' },
  { label: '娱乐', value: 'entertainment' },
  { label: '健康', value: 'health' },
  { label: '教育', value: 'education' },
  { label: '国际', value: 'international' }
])

// 加载文章列表
const loadArticles = async (reset = true) => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    const params: ArticleListParams = {
      page: reset ? 1 : currentPage.value,
      pageSize: pageSize.value,
      category: selectedCategory.value === 'all' ? undefined : selectedCategory.value,
      keyword: searchKeyword.value || undefined
    }
    
    const response = await articleApi.getArticleList(params)
    
    if (reset) {
      articles.value = response.data.list
      currentPage.value = 1
    } else {
      articles.value.push(...response.data.list)
    }
    
    hasMore.value = response.data.hasMore
    currentPage.value++
    
  } catch (error) {
    console.error('加载文章失败:', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'error'
    })
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (hasMore.value && !loading.value) {
    loadArticles(false)
  }
}

// 搜索处理
const handleSearch = () => {
  loadArticles(true)
}

// 搜索输入处理
const handleSearchInput = (e: any) => {
  searchKeyword.value = e.detail.value
}

// 分类切换
const handleCategoryChange = (category: string) => {
  selectedCategory.value = category
  loadArticles(true)
}

// 跳转到文章详情
const goToArticleDetail = (id: number) => {
  uni.navigateTo({
    url: `/pages/article/detail?id=${id}`
  })
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

// 获取难度文本
const getDifficultyText = (difficulty: string) => {
  const difficultyMap = {
    'easy': '简单',
    'medium': '中等',
    'hard': '困难'
  }
  return difficultyMap[difficulty] || '未知'
}

// 页面加载
onMounted(() => {
  loadArticles()
})

// 页面显示时刷新
onShow(() => {
  // 可以在这里添加刷新逻辑
})
</script>

<style lang="scss" scoped>
.article-list-container {
  min-height: 100vh;
  background-color: var(--background-color);
}

.search-section {
  padding: 20rpx 30rpx;
  background: white;
  border-bottom: 1rpx solid var(--border-color);
  
  .search-bar {
    display: flex;
    align-items: center;
    background: #F5F5F5;
    border-radius: 24rpx;
    padding: 0 24rpx;
    height: 72rpx;
    
    .search-icon {
      width: 32rpx;
      height: 32rpx;
      margin-right: 16rpx;
      opacity: 0.6;
    }
    
    .search-input {
      flex: 1;
      font-size: 28rpx;
      color: var(--text-primary);
      
      &::placeholder {
        color: var(--text-secondary);
      }
    }
    
    .search-btn {
      padding: 8rpx 16rpx;
      background: var(--primary-color);
      border-radius: 16rpx;
      
      .search-text {
        font-size: 24rpx;
        color: white;
      }
    }
  }
}

.filter-section {
  background: white;
  border-bottom: 1rpx solid var(--border-color);
  
  .filter-scroll {
    white-space: nowrap;
    
    .filter-list {
      display: flex;
      padding: 20rpx 30rpx;
      
      .filter-item {
        flex-shrink: 0;
        padding: 12rpx 24rpx;
        margin-right: 16rpx;
        background: #F5F5F5;
        border-radius: 20rpx;
        transition: all 0.3s ease;
        
        &.active {
          background: var(--primary-color);
          
          .filter-text {
            color: white;
          }
        }
        
        .filter-text {
          font-size: 26rpx;
          color: var(--text-primary);
        }
      }
    }
  }
}

.articles-section {
  padding: 20rpx 30rpx;
  
  .articles-list {
    .article-item {
      background: white;
      border-radius: 16rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;
      display: flex;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;
      
      &:active {
        transform: scale(0.98);
      }
      
      .article-content {
        flex: 1;
        margin-right: 20rpx;
        
        .article-title {
          font-size: 30rpx;
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
          font-size: 26rpx;
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
          margin-bottom: 16rpx;
          
          .meta-left {
            display: flex;
            align-items: center;
            
            .article-source {
              font-size: 22rpx;
              color: var(--primary-color);
              margin-right: 16rpx;
            }
            
            .article-time {
              font-size: 22rpx;
              color: var(--text-secondary);
            }
          }
          
          .meta-right {
            display: flex;
            align-items: center;
            
            .article-difficulty {
              font-size: 22rpx;
              color: var(--warning-color);
              margin-right: 16rpx;
            }
            
            .article-read-time {
              font-size: 22rpx;
              color: var(--text-secondary);
            }
          }
        }
        
        .article-tags {
          display: flex;
          flex-wrap: wrap;
          
          .tag {
            font-size: 20rpx;
            color: var(--text-secondary);
            background: #F5F5F5;
            padding: 4rpx 12rpx;
            border-radius: 12rpx;
            margin-right: 12rpx;
            margin-bottom: 8rpx;
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
    margin-bottom: 40rpx;
  }
  
  .refresh-btn {
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

<template>
  <view class="article-detail-container">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <view class="loading"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 文章内容 -->
    <view class="article-content" v-else-if="article">
      <!-- 文章头部 -->
      <view class="article-header">
        <text class="article-title">{{ article.title }}</text>
        
        <view class="article-meta">
          <view class="meta-row">
            <text class="article-source">{{ article.source }}</text>
            <text class="article-time">{{ formatTime(article.publishTime) }}</text>
          </view>
          <view class="meta-row">
            <text class="article-difficulty">{{ getDifficultyText(article.difficulty) }}</text>
            <text class="article-read-time">{{ article.readTime }}分钟阅读</text>
          </view>
        </view>
      </view>

      <!-- 文章图片 -->
      <image 
        v-if="article.imageUrl"
        :src="article.imageUrl" 
        class="article-image"
        mode="aspectFill"
      />

      <!-- 文章正文 -->
      <view class="article-body">
        <!-- 英文内容 -->
        <view class="content-section">
          <view class="section-header">
            <text class="section-title">English</text>
            <view class="section-actions">
              <button class="action-btn" @click="toggleTranslation">
                <image src="/static/translate-icon.png" class="action-icon" />
                <text class="action-text">{{ showTranslation ? '隐藏翻译' : '显示翻译' }}</text>
              </button>
              <button class="action-btn" @click="toggleTTS">
                <image src="/static/tts-icon.png" class="action-icon" />
                <text class="action-text">{{ isPlaying ? '停止朗读' : '开始朗读' }}</text>
              </button>
            </view>
          </view>
          
          <view class="content-text" @click="handleTextClick">
            <text class="text-content">{{ article.contentEn }}</text>
          </view>
        </view>

        <!-- 中文翻译 -->
        <view class="content-section" v-if="showTranslation">
          <view class="section-header">
            <text class="section-title">中文翻译</text>
          </view>
          
          <view class="content-text">
            <text class="text-content">{{ article.contentCn }}</text>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="article-actions">
        <button class="action-button" @click="toggleBookmark">
          <image 
            :src="article.isBookmarked ? '/static/bookmark-filled.png' : '/static/bookmark.png'" 
            class="action-icon"
          />
          <text class="action-text">{{ article.isBookmarked ? '已收藏' : '收藏' }}</text>
        </button>
        
        <button class="action-button" @click="shareArticle">
          <image src="/static/share-icon.png" class="action-icon" />
          <text class="action-text">分享</text>
        </button>
        
        <button class="action-button" @click="goToAIAssistant">
          <image src="/static/ai-icon.png" class="action-icon" />
          <text class="action-text">AI助手</text>
        </button>
      </view>
    </view>

    <!-- 错误状态 -->
    <view class="error-state" v-else>
      <image src="/static/error-icon.png" class="error-icon" />
      <text class="error-text">文章加载失败</text>
      <button class="retry-btn" @click="loadArticle">重试</button>
    </view>

    <!-- 单词查询弹窗 -->
    <view class="word-modal" v-if="showWordModal" @click="closeWordModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ selectedWord }}</text>
          <button class="close-btn" @click="closeWordModal">×</button>
        </view>
        
        <view class="modal-body">
          <view class="word-info">
            <text class="word-phonetic">/{{ wordInfo.phonetic }}/</text>
            <text class="word-meaning">{{ wordInfo.meaning }}</text>
            <text class="word-example">{{ wordInfo.example }}</text>
          </view>
          
          <view class="word-actions">
            <button class="word-btn" @click="addToVocabulary">加入生词本</button>
            <button class="word-btn" @click="playPronunciation">发音</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { articleApi, type Article } from '@/api/article'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const article = ref<Article | null>(null)
const showTranslation = ref(true)
const showWordModal = ref(false)
const selectedWord = ref('')
const wordInfo = ref({
  phonetic: '',
  meaning: '',
  example: ''
})
const isPlaying = ref(false)

// 获取文章ID
const articleId = ref(0)

// 加载文章详情
const loadArticle = async () => {
  if (!articleId.value) return
  
  loading.value = true
  
  try {
    const response = await articleApi.getArticleDetail(articleId.value)
    article.value = response.data
    
    // 开始阅读
    await articleApi.startReading(articleId.value)
    
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

// 切换翻译显示
const toggleTranslation = () => {
  showTranslation.value = !showTranslation.value
}

// 切换TTS播放
const toggleTTS = () => {
  if (isPlaying.value) {
    // 停止播放
    uni.stopVoice()
    isPlaying.value = false
  } else {
    // 开始播放
    uni.playVoice({
      filePath: '', // 这里需要调用TTS API生成音频
      success: () => {
        isPlaying.value = true
      },
      fail: () => {
        uni.showToast({
          title: '播放失败',
          icon: 'error'
        })
      }
    })
  }
}

// 处理文本点击
const handleTextClick = (e: any) => {
  // 获取点击的单词
  const word = e.detail.text
  if (word && /^[a-zA-Z]+$/.test(word)) {
    selectedWord.value = word
    showWordModal.value = true
    
    // 查询单词信息
    queryWordInfo(word)
  }
}

// 查询单词信息
const queryWordInfo = async (word: string) => {
  try {
    // 这里应该调用API查询单词信息
    wordInfo.value = {
      phonetic: 'ˈwɜːrd',
      meaning: '单词，词语',
      example: 'This is a new word for me.'
    }
  } catch (error) {
    console.error('查询单词失败:', error)
  }
}

// 关闭单词弹窗
const closeWordModal = () => {
  showWordModal.value = false
  selectedWord.value = ''
}

// 加入生词本
const addToVocabulary = () => {
  if (!userStore.checkLoginStatus()) return
  
  // 这里应该调用API加入生词本
  uni.showToast({
    title: '已加入生词本',
    icon: 'success'
  })
  
  closeWordModal()
}

// 播放发音
const playPronunciation = () => {
  // 这里应该调用TTS API播放单词发音
  uni.showToast({
    title: '播放发音',
    icon: 'success'
  })
}

// 切换收藏
const toggleBookmark = async () => {
  if (!userStore.checkLoginStatus()) return
  
  try {
    await articleApi.toggleBookmark(articleId.value)
    if (article.value) {
      article.value.isBookmarked = !article.value.isBookmarked
    }
    
    uni.showToast({
      title: article.value?.isBookmarked ? '已收藏' : '已取消收藏',
      icon: 'success'
    })
  } catch (error) {
    console.error('收藏操作失败:', error)
    uni.showToast({
      title: '操作失败',
      icon: 'error'
    })
  }
}

// 分享文章
const shareArticle = () => {
  uni.share({
    provider: 'weixin',
    scene: 'WXSceneSession',
    type: 0,
    href: `https://readup.com/article/${articleId.value}`,
    title: article.value?.title,
    summary: article.value?.summary,
    imageUrl: article.value?.imageUrl,
    success: () => {
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    },
    fail: () => {
      uni.showToast({
        title: '分享失败',
        icon: 'error'
      })
    }
  })
}

// 跳转到AI助手
const goToAIAssistant = () => {
  if (!userStore.checkLoginStatus()) return
  
  uni.navigateTo({
    url: `/pages/ai-assistant/chat?articleId=${articleId.value}`
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
  // 获取文章ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  articleId.value = parseInt(options.id || '0')
  
  if (articleId.value) {
    loadArticle()
  } else {
    uni.showToast({
      title: '文章ID无效',
      icon: 'error'
    })
    uni.navigateBack()
  }
})

// 页面卸载时结束阅读
onUnmounted(() => {
  if (articleId.value && article.value) {
    // 计算阅读时间
    const readingTime = Math.floor((Date.now() - Date.now()) / 1000 / 60) // 这里需要记录开始时间
    articleApi.endReading(articleId.value, readingTime)
  }
})
</script>

<style lang="scss" scoped>
.article-detail-container {
  min-height: 100vh;
  background-color: var(--background-color);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  
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

.article-content {
  padding: 30rpx;
  
  .article-header {
    margin-bottom: 30rpx;
    
    .article-title {
      font-size: 36rpx;
      font-weight: bold;
      color: var(--text-primary);
      line-height: 1.4;
      margin-bottom: 20rpx;
      display: block;
    }
    
    .article-meta {
      .meta-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 12rpx;
        
        .article-source {
          font-size: 24rpx;
          color: var(--primary-color);
        }
        
        .article-time {
          font-size: 24rpx;
          color: var(--text-secondary);
        }
        
        .article-difficulty {
          font-size: 24rpx;
          color: var(--warning-color);
        }
        
        .article-read-time {
          font-size: 24rpx;
          color: var(--text-secondary);
        }
      }
    }
  }
  
  .article-image {
    width: 100%;
    height: 400rpx;
    border-radius: 16rpx;
    margin-bottom: 30rpx;
  }
  
  .article-body {
    .content-section {
      background: white;
      border-radius: 16rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;
      
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
        
        .section-actions {
          display: flex;
          gap: 16rpx;
          
          .action-btn {
            display: flex;
            align-items: center;
            padding: 8rpx 16rpx;
            background: #F5F5F5;
            border-radius: 16rpx;
            border: none;
            
            .action-icon {
              width: 24rpx;
              height: 24rpx;
              margin-right: 8rpx;
            }
            
            .action-text {
              font-size: 22rpx;
              color: var(--text-primary);
            }
          }
        }
      }
      
      .content-text {
        .text-content {
          font-size: 28rpx;
          line-height: 1.6;
          color: var(--text-primary);
        }
      }
    }
  }
  
  .article-actions {
    display: flex;
    justify-content: space-around;
    background: white;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-top: 20rpx;
    
    .action-button {
      display: flex;
      flex-direction: column;
      align-items: center;
      background: none;
      border: none;
      
      .action-icon {
        width: 40rpx;
        height: 40rpx;
        margin-bottom: 8rpx;
      }
      
      .action-text {
        font-size: 22rpx;
        color: var(--text-primary);
      }
    }
  }
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  
  .error-icon {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 40rpx;
    opacity: 0.6;
  }
  
  .error-text {
    font-size: 28rpx;
    color: var(--text-secondary);
    margin-bottom: 40rpx;
  }
  
  .retry-btn {
    padding: 16rpx 32rpx;
    background: var(--primary-color);
    color: white;
    border-radius: 24rpx;
    font-size: 26rpx;
    border: none;
  }
}

.word-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  
  .modal-content {
    background: white;
    border-radius: 16rpx;
    margin: 40rpx;
    max-width: 600rpx;
    width: 100%;
    
    .modal-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 30rpx;
      border-bottom: 1rpx solid var(--border-color);
      
      .modal-title {
        font-size: 32rpx;
        font-weight: bold;
        color: var(--text-primary);
      }
      
      .close-btn {
        width: 60rpx;
        height: 60rpx;
        background: #F5F5F5;
        border-radius: 30rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 32rpx;
        color: var(--text-secondary);
        border: none;
      }
    }
    
    .modal-body {
      padding: 30rpx;
      
      .word-info {
        margin-bottom: 30rpx;
        
        .word-phonetic {
          font-size: 24rpx;
          color: var(--primary-color);
          margin-bottom: 12rpx;
          display: block;
        }
        
        .word-meaning {
          font-size: 28rpx;
          color: var(--text-primary);
          margin-bottom: 12rpx;
          display: block;
        }
        
        .word-example {
          font-size: 24rpx;
          color: var(--text-secondary);
          line-height: 1.4;
          display: block;
        }
      }
      
      .word-actions {
        display: flex;
        gap: 16rpx;
        
        .word-btn {
          flex: 1;
          padding: 16rpx;
          background: var(--primary-color);
          color: white;
          border-radius: 12rpx;
          font-size: 26rpx;
          border: none;
        }
      }
    }
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>

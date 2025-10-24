# XReadUp 微信小程序 Uni-app 具体实施方案

<div align="center">

![Uni-app](https://img.shields.io/badge/Uni--app-Vue3_Cross_Platform-blue.svg)
![WeChat](https://img.shields.io/badge/WeChat_MiniProgram-Implementation-green.svg)
![Timeline](https://img.shields.io/badge/Timeline-2--3_Months-orange.svg)

**基于Vue3技术栈的微信小程序开发详细方案**

</div>

## 📋 项目概述

基于XReadUp现有Vue3项目，使用Uni-app框架开发微信小程序版本，实现一套代码多端运行，最大化代码复用率。

## 🛠 技术架构设计

### 技术栈选择

```yaml
# 核心技术栈
Framework: Uni-app 3.x
Language: Vue 3 + TypeScript
UI Library: uni-ui + uView Plus
State Management: Pinia (兼容)
Build Tool: Vite (Uni-app内置)
Package Manager: npm/yarn

# 开发工具
IDE: HBuilderX / VS Code
Debug: 微信开发者工具
Version Control: Git
```

### 项目结构设计

```
xreadup-miniprogram/
├── src/
│   ├── pages/                    # 页面文件
│   │   ├── index/               # 首页
│   │   ├── article/             # 文章相关页面
│   │   ├── vocabulary/          # 生词本页面
│   │   ├── report/              # 学习报告页面
│   │   ├── subscription/        # 订阅管理页面
│   │   ├── ai-assistant/        # AI助手页面
│   │   └── profile/             # 个人中心页面
│   ├── components/              # 组件文件
│   │   ├── common/              # 通用组件
│   │   ├── business/            # 业务组件
│   │   └── ui/                  # UI组件
│   ├── static/                  # 静态资源
│   ├── store/                   # 状态管理
│   ├── utils/                   # 工具函数
│   ├── api/                     # API接口
│   ├── types/                   # TypeScript类型定义
│   └── uni_modules/             # uni-app插件
├── manifest.json                # 应用配置
├── pages.json                   # 页面配置
├── uni.scss                     # 全局样式
└── package.json                 # 依赖配置
```

## 🎯 核心功能实现方案

### 1. 用户认证系统

#### 1.1 微信登录实现

```typescript
// store/user.ts
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null as UserInfo | null,
    token: '',
    isLoggedIn: false
  }),
  
  actions: {
    // 微信登录
    async loginWithWeChat() {
      try {
        // 1. 获取微信登录凭证
        const loginRes = await uni.login({
          provider: 'weixin'
        })
        
        // 2. 获取用户信息
        const userInfoRes = await uni.getUserProfile({
          desc: '用于完善用户资料'
        })
        
        // 3. 调用后端API
        const response = await this.$api.post('/api/user/wechat-login', {
          code: loginRes.code,
          encryptedData: userInfoRes.encryptedData,
          iv: userInfoRes.iv,
          signature: userInfoRes.signature,
          rawData: userInfoRes.rawData
        })
        
        // 4. 保存用户信息
        this.user = response.data.user
        this.token = response.data.token
        this.isLoggedIn = true
        
        // 5. 持久化存储
        uni.setStorageSync('token', this.token)
        uni.setStorageSync('user', this.user)
        
        return response.data
      } catch (error) {
        console.error('微信登录失败:', error)
        throw error
      }
    },
    
    // 自动登录
    async autoLogin() {
      const token = uni.getStorageSync('token')
      const user = uni.getStorageSync('user')
      
      if (token && user) {
        this.token = token
        this.user = user
        this.isLoggedIn = true
        
        // 验证token有效性
        try {
          await this.$api.get('/api/user/profile')
        } catch (error) {
          this.logout()
        }
      }
    },
    
    // 退出登录
    logout() {
      this.user = null
      this.token = ''
      this.isLoggedIn = false
      uni.removeStorageSync('token')
      uni.removeStorageSync('user')
    }
  }
})
```

#### 1.2 登录页面实现

```vue
<!-- pages/login/login.vue -->
<template>
  <view class="login-container">
    <view class="logo-section">
      <image src="/static/logo.png" class="logo" />
      <text class="app-name">XReadUp</text>
      <text class="app-desc">真实新闻驱动的AI英语学习平台</text>
    </view>
    
    <view class="login-section">
      <button 
        class="wechat-login-btn"
        @click="handleWeChatLogin"
        :loading="loginLoading"
      >
        <image src="/static/wechat-icon.png" class="wechat-icon" />
        微信一键登录
      </button>
      
      <view class="agreement">
        <text class="agreement-text">
          登录即表示同意
          <text class="link" @click="showAgreement">《用户协议》</text>
          和
          <text class="link" @click="showPrivacy">《隐私政策》</text>
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loginLoading = ref(false)

const handleWeChatLogin = async () => {
  loginLoading.value = true
  
  try {
    await userStore.loginWithWeChat()
    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })
    
    // 跳转到首页
    uni.switchTab({
      url: '/pages/index/index'
    })
  } catch (error) {
    uni.showToast({
      title: '登录失败，请重试',
      icon: 'error'
    })
  } finally {
    loginLoading.value = false
  }
}

const showAgreement = () => {
  uni.navigateTo({
    url: '/pages/agreement/agreement?type=user'
  })
}

const showPrivacy = () => {
  uni.navigateTo({
    url: '/pages/agreement/agreement?type=privacy'
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.logo-section {
  text-align: center;
  margin-bottom: 120rpx;
  
  .logo {
    width: 120rpx;
    height: 120rpx;
    margin-bottom: 20rpx;
  }
  
  .app-name {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    color: white;
    margin-bottom: 10rpx;
  }
  
  .app-desc {
    display: block;
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.8);
  }
}

.login-section {
  width: 100%;
  
  .wechat-login-btn {
    width: 100%;
    height: 88rpx;
    background: #07c160;
    color: white;
    border-radius: 44rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    font-weight: bold;
    border: none;
    
    .wechat-icon {
      width: 40rpx;
      height: 40rpx;
      margin-right: 16rpx;
    }
  }
  
  .agreement {
    margin-top: 40rpx;
    text-align: center;
    
    .agreement-text {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.7);
      
      .link {
        color: #ffd700;
        text-decoration: underline;
      }
    }
  }
}
</style>
```

### 2. 文章阅读系统

#### 2.1 文章列表页面

```vue
<!-- pages/article/list.vue -->
<template>
  <view class="article-list-container">
    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-bar">
        <input 
          v-model="searchKeyword"
          placeholder="搜索文章..."
          class="search-input"
          @confirm="handleSearch"
        />
        <image src="/static/search-icon.png" class="search-icon" />
      </view>
      
      <!-- 分类筛选 -->
      <scroll-view class="category-scroll" scroll-x>
        <view class="category-list">
          <view 
            v-for="category in categories"
            :key="category.value"
            class="category-item"
            :class="{ active: selectedCategory === category.value }"
            @click="selectCategory(category.value)"
          >
            {{ category.label }}
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 文章列表 -->
    <scroll-view 
      class="article-scroll"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="article-list">
        <view 
          v-for="article in articleList"
          :key="article.id"
          class="article-item"
          @click="goToArticle(article.id)"
        >
          <view class="article-content">
            <text class="article-title">{{ article.title }}</text>
            <text class="article-summary">{{ article.summary }}</text>
            
            <view class="article-meta">
              <view class="meta-item">
                <image src="/static/time-icon.png" class="meta-icon" />
                <text class="meta-text">{{ formatTime(article.publishTime) }}</text>
              </view>
              <view class="meta-item">
                <image src="/static/read-icon.png" class="meta-icon" />
                <text class="meta-text">{{ article.readTime }}分钟</text>
              </view>
              <view class="meta-item">
                <image src="/static/difficulty-icon.png" class="meta-icon" />
                <text class="meta-text">{{ article.difficulty }}</text>
              </view>
            </view>
          </view>
          
          <view class="article-image" v-if="article.imageUrl">
            <image :src="article.imageUrl" class="cover-image" />
          </view>
        </view>
      </view>
      
      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMore">
        <text class="load-text">{{ loading ? '加载中...' : '上拉加载更多' }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useArticleStore } from '@/store/article'

const articleStore = useArticleStore()

// 响应式数据
const searchKeyword = ref('')
const selectedCategory = ref('all')
const articleList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)

// 分类选项
const categories = ref([
  { label: '全部', value: 'all' },
  { label: '科技', value: 'technology' },
  { label: '商业', value: 'business' },
  { label: '文化', value: 'culture' },
  { label: '健康', value: 'health' },
  { label: '体育', value: 'sports' }
])

// 页面加载
onMounted(() => {
  loadArticles()
})

// 加载文章列表
const loadArticles = async (page = 1, reset = false) => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    const params = {
      page,
      pageSize: 10,
      category: selectedCategory.value === 'all' ? '' : selectedCategory.value,
      keyword: searchKeyword.value
    }
    
    const response = await articleStore.getArticleList(params)
    
    if (reset) {
      articleList.value = response.data.list
    } else {
      articleList.value.push(...response.data.list)
    }
    
    hasMore.value = response.data.hasMore
    currentPage.value = page
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'error'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  loadArticles(1, true)
}

// 上拉加载更多
const loadMore = () => {
  if (hasMore.value && !loading.value) {
    loadArticles(currentPage.value + 1)
  }
}

// 搜索文章
const handleSearch = () => {
  loadArticles(1, true)
}

// 选择分类
const selectCategory = (category: string) => {
  selectedCategory.value = category
  loadArticles(1, true)
}

// 跳转到文章详情
const goToArticle = (articleId: number) => {
  uni.navigateTo({
    url: `/pages/article/detail?id=${articleId}`
  })
}

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}
</script>

<style lang="scss" scoped>
.article-list-container {
  height: 100vh;
  background: #f5f5f5;
}

.search-section {
  background: white;
  padding: 20rpx;
  
  .search-bar {
    position: relative;
    margin-bottom: 20rpx;
    
    .search-input {
      width: 100%;
      height: 72rpx;
      background: #f8f8f8;
      border-radius: 36rpx;
      padding: 0 60rpx 0 30rpx;
      font-size: 28rpx;
    }
    
    .search-icon {
      position: absolute;
      right: 20rpx;
      top: 50%;
      transform: translateY(-50%);
      width: 32rpx;
      height: 32rpx;
    }
  }
  
  .category-scroll {
    white-space: nowrap;
    
    .category-list {
      display: flex;
      
      .category-item {
        padding: 12rpx 24rpx;
        margin-right: 20rpx;
        background: #f8f8f8;
        border-radius: 20rpx;
        font-size: 26rpx;
        color: #666;
        
        &.active {
          background: #007aff;
          color: white;
        }
      }
    }
  }
}

.article-scroll {
  height: calc(100vh - 200rpx);
}

.article-list {
  padding: 20rpx;
}

.article-item {
  background: white;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  display: flex;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  
  .article-content {
    flex: 1;
    
    .article-title {
      display: block;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 12rpx;
      line-height: 1.4;
    }
    
    .article-summary {
      display: block;
      font-size: 26rpx;
      color: #666;
      margin-bottom: 16rpx;
      line-height: 1.5;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
    
    .article-meta {
      display: flex;
      
      .meta-item {
        display: flex;
        align-items: center;
        margin-right: 24rpx;
        
        .meta-icon {
          width: 24rpx;
          height: 24rpx;
          margin-right: 8rpx;
        }
        
        .meta-text {
          font-size: 22rpx;
          color: #999;
        }
      }
    }
  }
  
  .article-image {
    width: 160rpx;
    height: 120rpx;
    margin-left: 20rpx;
    
    .cover-image {
      width: 100%;
      height: 100%;
      border-radius: 8rpx;
    }
  }
}

.load-more {
  text-align: center;
  padding: 40rpx;
  
  .load-text {
    font-size: 26rpx;
    color: #999;
  }
}
</style>
```

#### 2.2 文章阅读页面

```vue
<!-- pages/article/detail.vue -->
<template>
  <view class="article-detail-container">
    <!-- 顶部导航 -->
    <view class="header">
      <view class="nav-bar">
        <image src="/static/back-icon.png" class="back-btn" @click="goBack" />
        <text class="nav-title">文章阅读</text>
        <image src="/static/share-icon.png" class="share-btn" @click="shareArticle" />
      </view>
    </view>
    
    <!-- 文章内容 -->
    <scroll-view class="content-scroll" scroll-y>
      <!-- 文章头部 -->
      <view class="article-header">
        <text class="article-title">{{ article.title }}</text>
        
        <view class="article-meta">
          <view class="meta-row">
            <text class="meta-text">{{ article.source }}</text>
            <text class="meta-text">{{ formatTime(article.publishTime) }}</text>
          </view>
          <view class="meta-row">
            <text class="meta-text">{{ article.readTime }}分钟阅读</text>
            <text class="meta-text">{{ article.wordCount }}字</text>
            <text class="difficulty-tag">{{ article.difficulty }}</text>
          </view>
        </view>
      </view>
      
      <!-- 文章正文 -->
      <view class="article-content">
        <view 
          v-for="(paragraph, index) in englishParagraphs"
          :key="`en-${index}`"
          class="paragraph-section"
        >
          <!-- 英文段落 -->
          <view 
            class="english-paragraph"
            @click="onWordClick"
          >
            <text class="paragraph-text">{{ paragraph }}</text>
          </view>
          
          <!-- 中文翻译 -->
          <view 
            v-if="showTranslation && chineseParagraphs[index]"
            class="chinese-paragraph"
          >
            <text class="paragraph-text">{{ chineseParagraphs[index] }}</text>
          </view>
        </view>
      </view>
      
      <!-- 文章底部 -->
      <view class="article-footer">
        <view class="action-buttons">
          <button 
            class="action-btn"
            :class="{ active: showTranslation }"
            @click="toggleTranslation"
          >
            <image src="/static/translate-icon.png" class="btn-icon" />
            <text class="btn-text">{{ showTranslation ? '隐藏翻译' : '显示翻译' }}</text>
          </button>
          
          <button class="action-btn" @click="addToVocabulary">
            <image src="/static/vocab-icon.png" class="btn-icon" />
            <text class="btn-text">加入生词本</text>
          </button>
          
          <button class="action-btn" @click="openAIAssistant">
            <image src="/static/ai-icon.png" class="btn-icon" />
            <text class="btn-text">AI助手</text>
          </button>
        </view>
      </view>
    </scroll-view>
    
    <!-- 底部工具栏 -->
    <view class="bottom-toolbar">
      <view class="toolbar-item" @click="toggleBookmark">
        <image 
          :src="isBookmarked ? '/static/bookmark-filled.png' : '/static/bookmark.png'" 
          class="toolbar-icon"
        />
        <text class="toolbar-text">收藏</text>
      </view>
      
      <view class="toolbar-item" @click="shareArticle">
        <image src="/static/share-icon.png" class="toolbar-icon" />
        <text class="toolbar-text">分享</text>
      </view>
      
      <view class="toolbar-item" @click="openComments">
        <image src="/static/comment-icon.png" class="toolbar-icon" />
        <text class="toolbar-text">评论</text>
      </view>
    </view>
    
    <!-- 单词查询弹窗 -->
    <view v-if="showWordModal" class="word-modal" @click="closeWordModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ selectedWord.word }}</text>
          <image src="/static/close-icon.png" class="close-btn" @click="closeWordModal" />
        </view>
        
        <view class="modal-body">
          <view class="word-info">
            <text class="phonetic">{{ selectedWord.phonetic }}</text>
            <text class="meaning">{{ selectedWord.meaning }}</text>
            <text class="example">{{ selectedWord.example }}</text>
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
import { ref, onMounted, computed } from 'vue'
import { useArticleStore } from '@/store/article'
import { useVocabularyStore } from '@/store/vocabulary'

const articleStore = useArticleStore()
const vocabularyStore = useVocabularyStore()

// 响应式数据
const article = ref(null)
const englishParagraphs = ref([])
const chineseParagraphs = ref([])
const showTranslation = ref(true)
const isBookmarked = ref(false)
const showWordModal = ref(false)
const selectedWord = ref({})

// 页面参数
const articleId = ref(0)

// 页面加载
onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  articleId.value = currentPage.options.id
  
  await loadArticleDetail()
})

// 加载文章详情
const loadArticleDetail = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    
    const response = await articleStore.getArticleDetail(articleId.value)
    article.value = response.data
    
    // 处理文章内容分段
    englishParagraphs.value = article.value.contentEn.split('\n\n')
    chineseParagraphs.value = article.value.contentCn.split('\n\n')
    
    // 记录阅读开始时间
    articleStore.startReading(articleId.value)
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'error'
    })
  } finally {
    uni.hideLoading()
  }
}

// 单词点击事件
const onWordClick = async (event) => {
  const word = event.detail.text.trim()
  
  if (word && /^[a-zA-Z]+$/.test(word)) {
    try {
      const response = await vocabularyStore.lookupWord(word)
      selectedWord.value = response.data
      showWordModal.value = true
    } catch (error) {
      uni.showToast({
        title: '查询失败',
        icon: 'error'
      })
    }
  }
}

// 关闭单词弹窗
const closeWordModal = () => {
  showWordModal.value = false
  selectedWord.value = {}
}

// 切换翻译显示
const toggleTranslation = () => {
  showTranslation.value = !showTranslation.value
}

// 添加到生词本
const addToVocabulary = async () => {
  try {
    await vocabularyStore.addWord(selectedWord.value)
    uni.showToast({
      title: '已加入生词本',
      icon: 'success'
    })
    closeWordModal()
  } catch (error) {
    uni.showToast({
      title: '添加失败',
      icon: 'error'
    })
  }
}

// 播放发音
const playPronunciation = () => {
  uni.createInnerAudioContext().src = selectedWord.value.audioUrl
}

// 打开AI助手
const openAIAssistant = () => {
  uni.navigateTo({
    url: `/pages/ai-assistant/chat?articleId=${articleId.value}`
  })
}

// 切换收藏状态
const toggleBookmark = async () => {
  try {
    await articleStore.toggleBookmark(articleId.value)
    isBookmarked.value = !isBookmarked.value
    uni.showToast({
      title: isBookmarked.value ? '已收藏' : '已取消收藏',
      icon: 'success'
    })
  } catch (error) {
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
    href: `https://xreadup.com/article/${articleId.value}`,
    title: article.value.title,
    summary: article.value.summary,
    imageUrl: article.value.imageUrl
  })
}

// 打开评论
const openComments = () => {
  uni.navigateTo({
    url: `/pages/article/comments?id=${articleId.value}`
  })
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 页面卸载时记录阅读时长
onUnmounted(() => {
  if (articleId.value) {
    articleStore.endReading(articleId.value)
  }
})
</script>

<style lang="scss" scoped>
.article-detail-container {
  height: 100vh;
  background: white;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: white;
  border-bottom: 1rpx solid #eee;
  
  .nav-bar {
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 30rpx;
    
    .back-btn, .share-btn {
      width: 40rpx;
      height: 40rpx;
    }
    
    .nav-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
  }
}

.content-scroll {
  height: 100vh;
  padding-top: 88rpx;
  padding-bottom: 120rpx;
}

.article-header {
  padding: 40rpx 30rpx 20rpx;
  
  .article-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    line-height: 1.5;
    margin-bottom: 20rpx;
  }
  
  .article-meta {
    .meta-row {
      display: flex;
      align-items: center;
      margin-bottom: 10rpx;
      
      .meta-text {
        font-size: 24rpx;
        color: #999;
        margin-right: 20rpx;
      }
      
      .difficulty-tag {
        background: #ff9500;
        color: white;
        padding: 4rpx 12rpx;
        border-radius: 12rpx;
        font-size: 20rpx;
      }
    }
  }
}

.article-content {
  padding: 0 30rpx;
  
  .paragraph-section {
    margin-bottom: 40rpx;
    
    .english-paragraph {
      margin-bottom: 20rpx;
      
      .paragraph-text {
        font-size: 32rpx;
        line-height: 1.8;
        color: #333;
      }
    }
    
    .chinese-paragraph {
      background: #f8f9fa;
      padding: 20rpx;
      border-radius: 12rpx;
      
      .paragraph-text {
        font-size: 28rpx;
        line-height: 1.6;
        color: #666;
      }
    }
  }
}

.article-footer {
  padding: 40rpx 30rpx;
  
  .action-buttons {
    display: flex;
    justify-content: space-around;
    
    .action-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      background: #f8f9fa;
      border: none;
      border-radius: 16rpx;
      padding: 20rpx;
      min-width: 120rpx;
      
      &.active {
        background: #007aff;
        color: white;
      }
      
      .btn-icon {
        width: 40rpx;
        height: 40rpx;
        margin-bottom: 8rpx;
      }
      
      .btn-text {
        font-size: 22rpx;
      }
    }
  }
}

.bottom-toolbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1rpx solid #eee;
  display: flex;
  justify-content: space-around;
  padding: 20rpx 0;
  
  .toolbar-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .toolbar-icon {
      width: 40rpx;
      height: 40rpx;
      margin-bottom: 8rpx;
    }
    
    .toolbar-text {
      font-size: 22rpx;
      color: #666;
    }
  }
}

.word-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .modal-content {
    background: white;
    border-radius: 20rpx;
    margin: 40rpx;
    max-width: 600rpx;
    
    .modal-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 30rpx;
      border-bottom: 1rpx solid #eee;
      
      .modal-title {
        font-size: 36rpx;
        font-weight: bold;
        color: #333;
      }
      
      .close-btn {
        width: 32rpx;
        height: 32rpx;
      }
    }
    
    .modal-body {
      padding: 30rpx;
      
      .word-info {
        margin-bottom: 30rpx;
        
        .phonetic {
          display: block;
          font-size: 24rpx;
          color: #007aff;
          margin-bottom: 10rpx;
        }
        
        .meaning {
          display: block;
          font-size: 28rpx;
          color: #333;
          margin-bottom: 15rpx;
        }
        
        .example {
          display: block;
          font-size: 24rpx;
          color: #666;
          font-style: italic;
        }
      }
      
      .word-actions {
        display: flex;
        justify-content: space-around;
        
        .word-btn {
          background: #007aff;
          color: white;
          border: none;
          border-radius: 20rpx;
          padding: 16rpx 32rpx;
          font-size: 26rpx;
        }
      }
    }
  }
}
</style>
```

### 3. 生词本系统

#### 3.1 生词本页面

```vue
<!-- pages/vocabulary/index.vue -->
<template>
  <view class="vocabulary-container">
    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stats-card">
        <text class="stats-number">{{ vocabularyStats.total }}</text>
        <text class="stats-label">总词汇</text>
      </view>
      <view class="stats-card">
        <text class="stats-number">{{ vocabularyStats.new }}</text>
        <text class="stats-label">新词</text>
      </view>
      <view class="stats-card">
        <text class="stats-number">{{ vocabularyStats.learning }}</text>
        <text class="stats-label">学习中</text>
      </view>
      <view class="stats-card">
        <text class="stats-number">{{ vocabularyStats.mastered }}</text>
        <text class="stats-label">已掌握</text>
      </view>
    </view>
    
    <!-- 学习模式选择 -->
    <view class="mode-section">
      <view class="mode-tabs">
        <view 
          v-for="mode in studyModes"
          :key="mode.value"
          class="mode-tab"
          :class="{ active: selectedMode === mode.value }"
          @click="selectMode(mode.value)"
        >
          <image :src="mode.icon" class="mode-icon" />
          <text class="mode-text">{{ mode.label }}</text>
        </view>
      </view>
    </view>
    
    <!-- 词汇列表 -->
    <scroll-view class="vocabulary-scroll" scroll-y>
      <view class="vocabulary-list">
        <view 
          v-for="word in vocabularyList"
          :key="word.id"
          class="vocabulary-item"
          @click="openWordDetail(word)"
        >
          <view class="word-content">
            <view class="word-header">
              <text class="word-text">{{ word.word }}</text>
              <text class="phonetic">{{ word.phonetic }}</text>
            </view>
            
            <text class="meaning">{{ word.meaning }}</text>
            <text class="example">{{ word.example }}</text>
            
            <view class="word-meta">
              <text class="difficulty">{{ word.difficulty }}</text>
              <text class="status">{{ getStatusText(word.status) }}</text>
              <text class="added-time">{{ formatTime(word.addedAt) }}</text>
            </view>
          </view>
          
          <view class="word-actions">
            <button 
              class="action-btn"
              :class="getStatusClass(word.status)"
              @click.stop="updateWordStatus(word)"
            >
              {{ getActionText(word.status) }}
            </button>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="vocabularyList.length === 0" class="empty-state">
        <image src="/static/empty-vocab.png" class="empty-image" />
        <text class="empty-text">还没有生词，快去阅读文章吧！</text>
        <button class="empty-btn" @click="goToArticles">开始阅读</button>
      </view>
    </scroll-view>
    
    <!-- 底部工具栏 -->
    <view class="bottom-toolbar">
      <button class="toolbar-btn" @click="startReview">
        <image src="/static/review-icon.png" class="btn-icon" />
        <text class="btn-text">开始复习</text>
      </button>
      
      <button class="toolbar-btn" @click="startDictation">
        <image src="/static/dictation-icon.png" class="btn-icon" />
        <text class="btn-text">听写练习</text>
      </button>
      
      <button class="toolbar-btn" @click="exportVocabulary">
        <image src="/static/export-icon.png" class="btn-icon" />
        <text class="btn-text">导出词汇</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useVocabularyStore } from '@/store/vocabulary'

const vocabularyStore = useVocabularyStore()

// 响应式数据
const selectedMode = ref('all')
const vocabularyList = ref([])
const vocabularyStats = ref({
  total: 0,
  new: 0,
  learning: 0,
  mastered: 0
})

// 学习模式
const studyModes = ref([
  { label: '全部', value: 'all', icon: '/static/all-icon.png' },
  { label: '新词', value: 'new', icon: '/static/new-icon.png' },
  { label: '学习中', value: 'learning', icon: '/static/learning-icon.png' },
  { label: '已掌握', value: 'mastered', icon: '/static/mastered-icon.png' }
])

// 页面加载
onMounted(async () => {
  await loadVocabularyData()
})

// 加载词汇数据
const loadVocabularyData = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    
    // 加载词汇列表
    const listResponse = await vocabularyStore.getVocabularyList({
      status: selectedMode.value === 'all' ? '' : selectedMode.value
    })
    vocabularyList.value = listResponse.data
    
    // 加载统计数据
    const statsResponse = await vocabularyStore.getVocabularyStats()
    vocabularyStats.value = statsResponse.data
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'error'
    })
  } finally {
    uni.hideLoading()
  }
}

// 选择学习模式
const selectMode = (mode: string) => {
  selectedMode.value = mode
  loadVocabularyData()
}

// 打开词汇详情
const openWordDetail = (word: any) => {
  uni.navigateTo({
    url: `/pages/vocabulary/detail?id=${word.id}`
  })
}

// 更新词汇状态
const updateWordStatus = async (word: any) => {
  try {
    const newStatus = getNextStatus(word.status)
    await vocabularyStore.updateWordStatus(word.id, newStatus)
    
    // 更新本地数据
    word.status = newStatus
    
    uni.showToast({
      title: '状态已更新',
      icon: 'success'
    })
  } catch (error) {
    uni.showToast({
      title: '更新失败',
      icon: 'error'
    })
  }
}

// 获取下一个状态
const getNextStatus = (currentStatus: string) => {
  const statusFlow = ['new', 'learning', 'mastered']
  const currentIndex = statusFlow.indexOf(currentStatus)
  return statusFlow[currentIndex + 1] || 'mastered'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap = {
    'new': '新词',
    'learning': '学习中',
    'mastered': '已掌握'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status: string) => {
  return `status-${status}`
}

// 获取操作文本
const getActionText = (status: string) => {
  const actionMap = {
    'new': '开始学习',
    'learning': '标记掌握',
    'mastered': '重新学习'
  }
  return actionMap[status] || '操作'
}

// 开始复习
const startReview = () => {
  uni.navigateTo({
    url: '/pages/vocabulary/review'
  })
}

// 开始听写
const startDictation = () => {
  uni.navigateTo({
    url: '/pages/vocabulary/dictation'
  })
}

// 导出词汇
const exportVocabulary = () => {
  uni.showActionSheet({
    itemList: ['导出为Excel', '导出为PDF', '分享给朋友'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          exportToExcel()
          break
        case 1:
          exportToPDF()
          break
        case 2:
          shareVocabulary()
          break
      }
    }
  })
}

// 导出为Excel
const exportToExcel = async () => {
  try {
    await vocabularyStore.exportVocabulary('excel')
    uni.showToast({
      title: '导出成功',
      icon: 'success'
    })
  } catch (error) {
    uni.showToast({
      title: '导出失败',
      icon: 'error'
    })
  }
}

// 导出为PDF
const exportToPDF = async () => {
  try {
    await vocabularyStore.exportVocabulary('pdf')
    uni.showToast({
      title: '导出成功',
      icon: 'success'
    })
  } catch (error) {
    uni.showToast({
      title: '导出失败',
      icon: 'error'
    })
  }
}

// 分享词汇
const shareVocabulary = () => {
  uni.share({
    provider: 'weixin',
    scene: 'WXSceneSession',
    type: 0,
    title: '我的英语生词本',
    summary: `我已经学习了${vocabularyStats.value.total}个单词`,
    imageUrl: '/static/vocab-share.png'
  })
}

// 跳转到文章列表
const goToArticles = () => {
  uni.switchTab({
    url: '/pages/index/index'
  })
}

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 86400000) return '今天'
  if (diff < 172800000) return '昨天'
  return `${date.getMonth() + 1}月${date.getDate()}日`
}
</script>

<style lang="scss" scoped>
.vocabulary-container {
  height: 100vh;
  background: #f5f5f5;
}

.stats-section {
  background: white;
  padding: 30rpx;
  display: flex;
  justify-content: space-around;
  
  .stats-card {
    text-align: center;
    
    .stats-number {
      display: block;
      font-size: 48rpx;
      font-weight: bold;
      color: #007aff;
      margin-bottom: 8rpx;
    }
    
    .stats-label {
      font-size: 24rpx;
      color: #666;
    }
  }
}

.mode-section {
  background: white;
  margin-top: 20rpx;
  padding: 30rpx;
  
  .mode-tabs {
    display: flex;
    justify-content: space-around;
    
    .mode-tab {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20rpx;
      border-radius: 16rpx;
      min-width: 120rpx;
      
      &.active {
        background: #007aff;
        color: white;
      }
      
      .mode-icon {
        width: 40rpx;
        height: 40rpx;
        margin-bottom: 8rpx;
      }
      
      .mode-text {
        font-size: 24rpx;
      }
    }
  }
}

.vocabulary-scroll {
  height: calc(100vh - 400rpx);
  padding: 20rpx;
}

.vocabulary-list {
  .vocabulary-item {
    background: white;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    
    .word-content {
      flex: 1;
      
      .word-header {
        display: flex;
        align-items: center;
        margin-bottom: 12rpx;
        
        .word-text {
          font-size: 32rpx;
          font-weight: bold;
          color: #333;
          margin-right: 16rpx;
        }
        
        .phonetic {
          font-size: 24rpx;
          color: #007aff;
        }
      }
      
      .meaning {
        display: block;
        font-size: 28rpx;
        color: #666;
        margin-bottom: 12rpx;
      }
      
      .example {
        display: block;
        font-size: 24rpx;
        color: #999;
        font-style: italic;
        margin-bottom: 16rpx;
      }
      
      .word-meta {
        display: flex;
        
        .difficulty, .status, .added-time {
          font-size: 22rpx;
          color: #999;
          margin-right: 20rpx;
        }
        
        .difficulty {
          background: #ff9500;
          color: white;
          padding: 4rpx 8rpx;
          border-radius: 8rpx;
        }
      }
    }
    
    .word-actions {
      .action-btn {
        background: #f8f9fa;
        border: none;
        border-radius: 20rpx;
        padding: 12rpx 24rpx;
        font-size: 24rpx;
        color: #666;
        
        &.status-new {
          background: #e3f2fd;
          color: #1976d2;
        }
        
        &.status-learning {
          background: #fff3e0;
          color: #f57c00;
        }
        
        &.status-mastered {
          background: #e8f5e8;
          color: #388e3c;
        }
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 100rpx 40rpx;
  
  .empty-image {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 40rpx;
  }
  
  .empty-text {
    display: block;
    font-size: 28rpx;
    color: #999;
    margin-bottom: 40rpx;
  }
  
  .empty-btn {
    background: #007aff;
    color: white;
    border: none;
    border-radius: 24rpx;
    padding: 20rpx 40rpx;
    font-size: 28rpx;
  }
}

.bottom-toolbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1rpx solid #eee;
  display: flex;
  justify-content: space-around;
  padding: 20rpx 0;
  
  .toolbar-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    background: none;
    border: none;
    
    .btn-icon {
      width: 40rpx;
      height: 40rpx;
      margin-bottom: 8rpx;
    }
    
    .btn-text {
      font-size: 22rpx;
      color: #666;
    }
  }
}
</style>
```

### 4. API接口适配

#### 4.1 HTTP请求封装

```typescript
// utils/request.ts
interface RequestConfig {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  timeout?: number
}

interface ApiResponse<T = any> {
  code: number
  message: string
  success: boolean
  data: T
}

class Request {
  private baseURL: string
  private timeout: number
  
  constructor() {
    this.baseURL = 'https://api.xreadup.com'
    this.timeout = 10000
  }
  
  // 请求拦截器
  private interceptRequest(config: RequestConfig): RequestConfig {
    // 添加认证头
    const token = uni.getStorageSync('token')
    if (token) {
      config.header = {
        ...config.header,
        'Authorization': `Bearer ${token}`
      }
    }
    
    // 添加通用头
    config.header = {
      'Content-Type': 'application/json',
      ...config.header
    }
    
    return config
  }
  
  // 响应拦截器
  private interceptResponse<T>(response: any): ApiResponse<T> {
    const { statusCode, data } = response
    
    if (statusCode === 200) {
      return data
    } else if (statusCode === 401) {
      // Token过期，清除登录状态
      uni.removeStorageSync('token')
      uni.removeStorageSync('user')
      uni.navigateTo({
        url: '/pages/login/login'
      })
      throw new Error('登录已过期')
    } else {
      throw new Error(data.message || '请求失败')
    }
  }
  
  // 通用请求方法
  async request<T = any>(config: RequestConfig): Promise<ApiResponse<T>> {
    const finalConfig = this.interceptRequest(config)
    
    return new Promise((resolve, reject) => {
      uni.request({
        url: `${this.baseURL}${finalConfig.url}`,
        method: finalConfig.method || 'GET',
        data: finalConfig.data,
        header: finalConfig.header,
        timeout: finalConfig.timeout || this.timeout,
        success: (response) => {
          try {
            const result = this.interceptResponse<T>(response)
            resolve(result)
          } catch (error) {
            reject(error)
          }
        },
        fail: (error) => {
          reject(new Error('网络请求失败'))
        }
      })
    })
  }
  
  // GET请求
  async get<T = any>(url: string, params?: any): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'GET',
      data: params
    })
  }
  
  // POST请求
  async post<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'POST',
      data
    })
  }
  
  // PUT请求
  async put<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'PUT',
      data
    })
  }
  
  // DELETE请求
  async delete<T = any>(url: string): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'DELETE'
    })
  }
}

export default new Request()
```

#### 4.2 API接口定义

```typescript
// api/user.ts
import request from '@/utils/request'

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar?: string
  email?: string
  phone?: string
  createdAt: string
}

export interface LoginRequest {
  code: string
  encryptedData: string
  iv: string
  signature: string
  rawData: string
}

export interface LoginResponse {
  user: UserInfo
  token: string
  expiresIn: number
}

export const userApi = {
  // 微信登录
  wechatLogin: (data: LoginRequest) => 
    request.post<LoginResponse>('/api/user/wechat-login', data),
  
  // 获取用户信息
  getUserInfo: () => 
    request.get<UserInfo>('/api/user/profile'),
  
  // 更新用户信息
  updateUserInfo: (data: Partial<UserInfo>) => 
    request.put<UserInfo>('/api/user/profile', data),
  
  // 获取用户订阅信息
  getUserSubscription: () => 
    request.get('/api/user/subscription'),
  
  // 获取用户使用统计
  getUserUsage: () => 
    request.get('/api/user/usage')
}

// api/article.ts
export interface Article {
  id: number
  title: string
  summary: string
  contentEn: string
  contentCn: string
  source: string
  publishTime: string
  readTime: number
  wordCount: number
  difficulty: string
  category: string
  imageUrl?: string
  isBookmarked: boolean
}

export interface ArticleListParams {
  page: number
  pageSize: number
  category?: string
  keyword?: string
  difficulty?: string
}

export interface ArticleListResponse {
  list: Article[]
  total: number
  hasMore: boolean
}

export const articleApi = {
  // 获取文章列表
  getArticleList: (params: ArticleListParams) => 
    request.get<ArticleListResponse>('/api/article/list', params),
  
  // 获取文章详情
  getArticleDetail: (id: number) => 
    request.get<Article>(`/api/article/${id}`),
  
  // 开始阅读
  startReading: (id: number) => 
    request.post(`/api/article/${id}/start-reading`),
  
  // 结束阅读
  endReading: (id: number, readingTime: number) => 
    request.post(`/api/article/${id}/end-reading`, { readingTime }),
  
  // 收藏/取消收藏
  toggleBookmark: (id: number) => 
    request.post(`/api/article/${id}/bookmark`),
  
  // 搜索文章
  searchArticles: (keyword: string, params?: Partial<ArticleListParams>) => 
    request.get<ArticleListResponse>('/api/article/search', { keyword, ...params })
}

// api/vocabulary.ts
export interface Vocabulary {
  id: number
  word: string
  meaning: string
  example: string
  phonetic: string
  difficulty: string
  status: 'new' | 'learning' | 'mastered'
  addedAt: string
  context?: string
}

export interface VocabularyListParams {
  status?: string
  page?: number
  pageSize?: number
  keyword?: string
}

export interface VocabularyStats {
  total: number
  new: number
  learning: number
  mastered: number
}

export const vocabularyApi = {
  // 获取词汇列表
  getVocabularyList: (params: VocabularyListParams) => 
    request.get<Vocabulary[]>('/api/vocabulary/user', params),
  
  // 获取词汇统计
  getVocabularyStats: () => 
    request.get<VocabularyStats>('/api/vocabulary/stats'),
  
  // 查询单词
  lookupWord: (word: string, context?: string) => 
    request.post('/api/vocabulary/lookup', { word, context }),
  
  // 添加单词
  addWord: (word: Partial<Vocabulary>) => 
    request.post('/api/vocabulary/add', word),
  
  // 更新单词状态
  updateWordStatus: (id: number, status: string) => 
    request.put(`/api/vocabulary/${id}/status`, { status }),
  
  // 删除单词
  deleteWord: (id: number) => 
    request.delete(`/api/vocabulary/${id}`),
  
  // 导出词汇
  exportVocabulary: (format: 'excel' | 'pdf') => 
    request.get(`/api/vocabulary/export?format=${format}`)
}

// api/ai.ts
export interface AIChatRequest {
  question: string
  context?: string
  articleId?: number
}

export interface AIChatResponse {
  answer: string
  followUpQuestion?: string
  difficulty?: string
}

export const aiApi = {
  // AI对话
  chat: (data: AIChatRequest) => 
    request.post<AIChatResponse>('/api/ai/assistant/chat', data),
  
  // 文章摘要
  generateSummary: (articleId: number) => 
    request.post<string>(`/api/ai/summary`, { articleId }),
  
  // 句子解析
  parseSentence: (sentence: string) => 
    request.post('/api/ai/parse', { sentence }),
  
  // 生成测验
  generateQuiz: (articleId: number) => 
    request.post(`/api/ai/quiz`, { articleId }),
  
  // 翻译文本
  translateText: (text: string, targetLang: string = 'zh') => 
    request.post('/api/ai/translate', { text, targetLang })
}
```

## 📱 小程序配置

### manifest.json配置

```json
{
  "name": "XReadUp",
  "appid": "__UNI__XXXXXXX",
  "description": "真实新闻驱动的AI英语学习平台",
  "versionName": "1.0.0",
  "versionCode": "100",
  "transformPx": false,
  "app-plus": {
    "usingComponents": true,
    "nvueStyleCompiler": "uni-app",
    "compilerVersion": 3,
    "splashscreen": {
      "alwaysShowBeforeRender": true,
      "waiting": true,
      "autoclose": true,
      "delay": 0
    },
    "modules": {},
    "distribute": {
      "android": {
        "permissions": [
          "<uses-permission android:name=\"android.permission.CHANGE_NETWORK_STATE\" />",
          "<uses-permission android:name=\"android.permission.MOUNT_UNMOUNT_FILESYSTEMS\" />",
          "<uses-permission android:name=\"android.permission.VIBRATE\" />",
          "<uses-permission android:name=\"android.permission.READ_LOGS\" />",
          "<uses-permission android:name=\"android.permission.ACCESS_WIFI_STATE\" />",
          "<uses-feature android:name=\"android.hardware.camera.autofocus\" />",
          "<uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />",
          "<uses-permission android:name=\"android.permission.CAMERA\" />",
          "<uses-permission android:name=\"android.permission.GET_ACCOUNTS\" />",
          "<uses-permission android:name=\"android.permission.READ_PHONE_STATE\" />",
          "<uses-permission android:name=\"android.permission.CHANGE_WIFI_STATE\" />",
          "<uses-permission android:name=\"android.permission.WAKE_LOCK\" />",
          "<uses-permission android:name=\"android.permission.FLASHLIGHT\" />",
          "<uses-feature android:name=\"android.hardware.camera\" />",
          "<uses-permission android:name=\"android.permission.WRITE_SETTINGS\" />"
        ]
      },
      "ios": {},
      "sdkConfigs": {}
    }
  },
  "quickapp": {},
  "mp-weixin": {
    "appid": "wx1234567890abcdef",
    "setting": {
      "urlCheck": false,
      "es6": true,
      "enhance": true,
      "postcss": true,
      "preloadBackgroundData": false,
      "minified": true,
      "newFeature": false,
      "coverView": true,
      "nodeModules": false,
      "autoAudits": false,
      "showShadowRootInWxmlPanel": true,
      "scopeDataCheck": false,
      "uglifyFileName": false,
      "checkInvalidKey": true,
      "checkSiteMap": true,
      "uploadWithSourceMap": true,
      "compileHotReLoad": false,
      "lazyloadPlaceholderEnable": false,
      "useMultiFrameRuntime": true,
      "useApiHook": true,
      "useApiHostProcess": true,
      "babelSetting": {
        "ignore": [],
        "disablePlugins": [],
        "outputPath": ""
      },
      "enableEngineNative": false,
      "useIsolateContext": true,
      "userConfirmedBundleSwitch": false,
      "packNpmManually": false,
      "packNpmRelationList": [],
      "minifyWXSS": true,
      "disableUseStrict": false,
      "minifyWXML": true,
      "showES6CompileOption": false,
      "useCompilerPlugins": false
    },
    "usingComponents": true,
    "permission": {
      "scope.userLocation": {
        "desc": "你的位置信息将用于小程序位置接口的效果展示"
      }
    },
    "requiredPrivateInfos": [
      "getLocation"
    ],
    "lazyCodeLoading": "requiredComponents"
  },
  "mp-alipay": {
    "usingComponents": true
  },
  "mp-baidu": {
    "usingComponents": true
  },
  "mp-toutiao": {
    "usingComponents": true
  },
  "uniStatistics": {
    "enable": false
  },
  "vueVersion": "3"
}
```

### pages.json配置

```json
{
  "pages": [
    {
      "path": "pages/index/index",
      "style": {
        "navigationBarTitleText": "XReadUp",
        "navigationBarBackgroundColor": "#007AFF",
        "navigationBarTextStyle": "white",
        "backgroundColor": "#F5F5F5"
      }
    },
    {
      "path": "pages/article/list",
      "style": {
        "navigationBarTitleText": "文章列表",
        "navigationBarBackgroundColor": "#007AFF",
        "navigationBarTextStyle": "white"
      }
    },
    {
      "path": "pages/article/detail",
      "style": {
        "navigationBarTitleText": "文章阅读",
        "navigationBarBackgroundColor": "#007AFF",
        "navigationBarTextStyle": "white"
      }
    },
    {
      "path": "pages/vocabulary/index",
      "style": {
        "navigationBarTitleText": "生词本",
        "navigationBarBackgroundColor": "#007AFF",
        "navigationBarTextStyle": "white"
      }
    },
    {
      "path": "pages/vocabulary/review",
      "style": {
        "navigationBarTitleText": "单词复习",
        "navigationBarBackgroundColor": "#007AFF",
        "navigationBarTextStyle": "white"
      }
    },
    {
      "path": "pages/vocabulary/dictation",
      "style": {
        "navigationBarTitleText": "听写练习",
        "navigationBarBackgroundColor": "#007AFF",
        "navigationBarTextStyle": "white"
      }
    },
    {
      "path": "pages/report/index",
      "style": {
        "navigationBarTitleText": "学习报告",
        "navigationBarBackgroundColor": "#007AFF",
        "navigationBarTextStyle": "white"
      }
    },
    {
      "path": "pages/subscription/index",
      "style": {
        "navigationBarTitleText": "订阅管理",
        "navigationBarBackgroundColor": "#007AFF",
        "navigationBarTextStyle": "white"
      }
    },
    {
      "path": "pages/ai-assistant/chat",
      "style": {
        "navigationBarTitleText": "AI助手",
        "navigationBarBackgroundColor": "#007AFF",
        "navigationBarTextStyle": "white"
      }
    },
    {
      "path": "pages/profile/index",
      "style": {
        "navigationBarTitleText": "个人中心",
        "navigationBarBackgroundColor": "#007AFF",
        "navigationBarTextStyle": "white"
      }
    },
    {
      "path": "pages/login/login",
      "style": {
        "navigationBarTitleText": "登录",
        "navigationStyle": "custom"
      }
    }
  ],
  "globalStyle": {
    "navigationBarTextStyle": "white",
    "navigationBarTitleText": "XReadUp",
    "navigationBarBackgroundColor": "#007AFF",
    "backgroundColor": "#F5F5F5"
  },
  "tabBar": {
    "color": "#7A7E83",
    "selectedColor": "#007AFF",
    "borderStyle": "black",
    "backgroundColor": "#ffffff",
    "list": [
      {
        "pagePath": "pages/index/index",
        "iconPath": "static/tabbar/home.png",
        "selectedIconPath": "static/tabbar/home-active.png",
        "text": "首页"
      },
      {
        "pagePath": "pages/article/list",
        "iconPath": "static/tabbar/article.png",
        "selectedIconPath": "static/tabbar/article-active.png",
        "text": "文章"
      },
      {
        "pagePath": "pages/vocabulary/index",
        "iconPath": "static/tabbar/vocabulary.png",
        "selectedIconPath": "static/tabbar/vocabulary-active.png",
        "text": "生词本"
      },
      {
        "pagePath": "pages/report/index",
        "iconPath": "static/tabbar/report.png",
        "selectedIconPath": "static/tabbar/report-active.png",
        "text": "报告"
      },
      {
        "pagePath": "pages/profile/index",
        "iconPath": "static/tabbar/profile.png",
        "selectedIconPath": "static/tabbar/profile-active.png",
        "text": "我的"
      }
    ]
  },
  "condition": {
    "current": 0,
    "list": [
      {
        "name": "文章详情",
        "path": "pages/article/detail",
        "query": "id=1"
      }
    ]
  }
}
```

## 🚀 开发实施计划

### 第一阶段：项目搭建和基础功能（4-6周）

#### Week 1-2: 项目初始化
- [ ] Uni-app项目创建和配置
- [ ] 开发环境搭建
- [ ] 基础项目结构设计
- [ ] API接口封装
- [ ] 状态管理配置

#### Week 3-4: 用户认证系统
- [ ] 微信登录功能实现
- [ ] JWT Token管理
- [ ] 用户信息管理
- [ ] 登录页面UI设计

#### Week 5-6: 文章阅读功能
- [ ] 文章列表页面
- [ ] 文章详情页面
- [ ] 双语对照阅读
- [ ] 点击查词功能
- [ ] 文章收藏功能

### 第二阶段：核心功能开发（4-6周）

#### Week 7-8: 生词本系统
- [ ] 生词本页面
- [ ] 词汇状态管理
- [ ] 单词复习功能
- [ ] 听写练习功能

#### Week 9-10: AI助手功能
- [ ] AI对话界面
- [ ] 智能问答功能
- [ ] 文章摘要生成
- [ ] 句子解析功能

#### Week 11-12: 学习报告和订阅
- [ ] 学习数据统计
- [ ] 学习报告页面
- [ ] 订阅管理功能
- [ ] 微信支付集成

### 第三阶段：优化和发布（2-3周）

#### Week 13-14: 性能优化
- [ ] 页面加载优化
- [ ] 图片懒加载
- [ ] 数据缓存优化
- [ ] 错误处理完善

#### Week 15: 测试和发布
- [ ] 功能测试
- [ ] 性能测试
- [ ] 小程序审核
- [ ] 正式发布

## 📊 预期效果

### 技术指标
- **包体积**: < 2MB
- **首屏加载**: < 2秒
- **页面切换**: < 500ms
- **API响应**: < 1秒

### 业务指标
- **用户增长**: 月活跃用户增长30%
- **转化率**: 小程序用户订阅转化率>15%
- **留存率**: 7日留存率>40%
- **使用时长**: 平均使用时长>15分钟

## 🎯 总结

基于Uni-app的微信小程序开发方案具有以下优势：

1. **技术栈匹配**: 基于Vue3语法，团队学习成本低
2. **开发效率高**: 可复用70-80%的业务逻辑
3. **成本可控**: 2-3个月开发周期，1-2人团队
4. **扩展性好**: 支持多端发布，未来可扩展其他平台

通过分阶段实施，可以快速实现核心功能，并根据用户反馈持续优化，确保项目的成功实施。

<template>
  <div class="modern-news-card" @click="handleClick">

    <!-- 封面图片区域 -->
    <div class="card-image-container" v-if="article.image && !imageLoadFailed">
      <img 
        :src="article.image" 
        :alt="article.title"
        class="card-image"
        @error="handleImageError"
      />
      <div class="image-overlay">
        <div class="source-badge" v-if="article.source">
          {{ formatSourceName(article.source) }}
        </div>
        <div class="publish-time" v-if="article.publishedAt">
          {{ formatPublishTime(article.publishedAt) }}
        </div>
      </div>
    </div>
    
    <!-- 卡片内容 -->
    <div class="card-content" :class="{ 'no-image': !article.image || imageLoadFailed }">
      <!-- 顶部信息栏 -->
      <div class="card-header">
        <div class="source-info" v-if="!article.image || imageLoadFailed">
          <span class="source-name" v-if="article.source">{{ formatSourceName(article.source) }}</span>
        </div>
        <div class="category-tags">
          <span class="category-tag">{{ article.category || '未分类' }}</span>
          <span class="difficulty-tag">{{ getDifficultyText(article.difficulty || '') }}</span>
        </div>
      </div>

      <!-- 标题 -->
      <h3 class="card-title">{{ article.title }}</h3>

      <!-- 摘要 -->
      <p class="card-description">
        {{ article.description || (article.enContent ? truncateText(article.enContent, 120) + '...' : '暂无描述') }}
      </p>

      <!-- 底部元信息 -->
      <div class="card-footer">
        <div class="reading-info">
          <span class="read-time">{{ getEstimatedReadTime }}分钟阅读</span>
          <span class="word-count" v-if="article.wordCount">{{ formatWordCount(article.wordCount) }}词</span>
          <span class="discovery-type" v-if="showDiscoveryBadge">{{ getDiscoveryLabel.text }}</span>
          <span class="publish-time" v-if="(!article.image || imageLoadFailed) && article.publishedAt">{{ formatPublishTime(article.publishedAt) }}</span>
        </div>
      </div>
    </div>

    <!-- 智能加载状态 -->
    <div v-if="loading" class="smart-loading-overlay">
      <div class="loading-spinner"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

// Props定义
interface Article {
  id: string | number
  title: string
  excerpt?: string
  description?: string
  coverImage?: string
  image?: string  // GNews API封面图片
  url?: string    // 原文链接
  publishedAt?: string | Date  // 发布时间
  source?: string  // 来源信息
  category?: string
  readTime?: number
  wordCount?: number
  difficulty?: number | string
  enContent?: string
  [key: string]: any
}

const props = withDefaults(defineProps<{
  article: Article
  showDiscoveryBadge?: boolean
  discoveryType?: 'trending' | 'category' | 'custom'
}>(), {
  showDiscoveryBadge: false,
  discoveryType: 'trending'
})

// 调试信息已移除

const router = useRouter()
const loading = ref(false)

// 截断文本函数
const truncateText = (text: string, maxLength: number): string => {
  if (!text || text.length <= maxLength) return text
  return text.slice(0, maxLength) + '...'
}

// 优化来源名称显示
const formatSourceName = (source: string): string => {
  if (!source) return ''
  
  // 移除常见的后缀
  let cleanSource = source
    .replace(/\s*-\s*Breaking News.*$/i, '')
    .replace(/\s*-\s*Latest News.*$/i, '')
    .replace(/\s*-\s*News.*$/i, '')
    .replace(/\s*-\s*Videos.*$/i, '')
    .replace(/\s*News\s*$/i, '')
    .replace(/\s*\.com.*$/i, '')
    .replace(/\s*\.org.*$/i, '')
    .replace(/\s*\.net.*$/i, '')
    .trim()
  
  // 如果还是太长，截断到合适长度
  if (cleanSource.length > 20) {
    cleanSource = cleanSource.slice(0, 20) + '...'
  }
  
  return cleanSource || source.slice(0, 15) + '...'
}

// 格式化单词数量
const formatWordCount = (count: number): string => {
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count.toString()
}

// 处理卡片点击事件
const handleClick = () => {
  loading.value = true
  router.push(`/article/${props.article.id || 1}`)
  // 模拟加载完成
  setTimeout(() => {
    loading.value = false
  }, 1000)
}

// 将难度等级转换为文本描述（与HomeView.vue保持一致）
const getDifficultyText = (difficulty: number | string): string => {
  // 如果难度等级不存在或为空字符串，直接返回'未知'
  if (!difficulty && difficulty !== 0) {
    return '未知'
  }

  // 处理数字类型的难度等级
  if (typeof difficulty === 'number' && !isNaN(difficulty)) {
    switch (difficulty) {
      case 1:
        return '简单'
      case 2:
        return '较易'
      case 3:
        return '中等'
      case 4:
        return '较难'
      case 5:
        return '极难'
      default:
        return '未知'
    }
  }

  // 处理字符串类型的难度等级
  const difficultyStr = String(difficulty).trim()
  if (!difficultyStr) {
    return '未知'
  }

  // 尝试将字符串转换为数字
  const numDifficulty = parseInt(difficultyStr)
  if (!isNaN(numDifficulty)) {
    switch (numDifficulty) {
      case 1:
        return '简单'
      case 2:
        return '较易'
      case 3:
        return '中等'
      case 4:
        return '较难'
      case 5:
        return '极难'
    }
  }

  // 如果是字符串且不是数字，直接返回原字符串（例如'A1', 'B2'等格式）
  return difficultyStr
}

// 获取预计阅读时长（与ArticleReader.vue中的getReadingTime保持一致的实现逻辑）
const getEstimatedReadTime = computed(() => {
  // 根据字数计算阅读时长，假设每分钟阅读200词
  if (props.article.wordCount) {
    return Math.ceil(props.article.wordCount / 200)
  }
  // 没有字数时默认返回5分钟
  return 5
})

// 格式化发布时间
const formatPublishTime = (publishedAt: string | Date): string => {
  const date = new Date(publishedAt)
  const now = new Date()
  const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60))
  
  if (diffInHours < 1) {
    return '刚刚'
  } else if (diffInHours < 24) {
    return `${diffInHours}小时前`
  } else if (diffInHours < 48) {
    return '昨天'
  } else if (diffInHours < 168) { // 7天
    return `${Math.floor(diffInHours / 24)}天前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }
}

// 处理图片加载错误
const imageLoadFailed = ref(false)

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
  imageLoadFailed.value = true
  // 隐藏图片后，卡片回到无图片的现有状态
}


// 获取发现类型标签
const getDiscoveryLabel = computed(() => {
  switch (props.discoveryType) {
    case 'trending':
      return { icon: '🔥', text: '热点发现' }
    case 'category':
      return { icon: '🎯', text: '主题发现' }
    case 'custom':
      return { icon: '🔍', text: '自定义发现' }
    default:
      return { icon: '🔍', text: '探索发现' }
  }
})

// 计算卡片背景渐变（根据标题生成简单的主题色）
const cardGradient = computed(() => {
  const title = props.article.title || ''
  // 简单的哈希函数生成主题色
  let hash = 0
  for (let i = 0; i < title.length; i++) {
    hash = title.charCodeAt(i) + ((hash << 5) - hash)
  }
  // 生成柔和的蓝色系色调
  const hue = (hash % 30) + 200 // 200-230之间的蓝色调
  const saturation = 60 + (hash % 20) // 60-80%
  const lightness = 90 - (Math.abs(hash) % 10) // 80-90%
  return `hsl(${hue}, ${saturation}%, ${lightness}%)`
})
</script>

<style scoped>
@import '@/assets/design-system.css';

.modern-news-card {
  background: #ffffff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.modern-news-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border-color: rgba(0, 122, 255, 0.2);
}


.card-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  z-index: 1;
  height: 100%;
  flex: 1;
}

/* 无图片情况下的特殊样式 */
.card-content.no-image {
  padding: 24px;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

/* 无图片情况下的顶部信息栏样式 */
.card-content.no-image .card-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F2F2F7;
}

.source-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.source-name {
  font-size: 12px;
  font-weight: 600;
  color: #007AFF;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.publish-time {
  font-size: 11px;
  color: #8E8E93;
  font-weight: 500;
}


.category-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.category-tag {
  background: #F2F2F7;
  color: #007AFF;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.difficulty-tag {
  background: #FFF3E0;
  color: #FF9500;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #1D1D1F;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
  letter-spacing: -0.2px;
}

/* 无图片情况下的标题样式 */
.card-content.no-image .card-title {
  font-size: 20px;
  line-height: 1.4;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  margin-bottom: 8px;
}

.card-description {
  font-size: 14px;
  color: #6E6E73;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
  flex: 1;
}

/* 无图片情况下的描述样式 */
.card-content.no-image .card-description {
  font-size: 15px;
  line-height: 1.6;
  -webkit-line-clamp: 4;
  line-clamp: 4;
  margin-bottom: 12px;
}

.card-footer {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #F2F2F7;
}

.reading-info {
  display: flex;
  gap: 16px;
  align-items: center;
  font-size: 12px;
  color: #8E8E93;
  justify-content: space-between;
}

.read-time {
  font-weight: 500;
}

.word-count {
  font-weight: 500;
}

.discovery-type {
  font-weight: 500;
  color: #007AFF;
  background: rgba(0, 122, 255, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

/* 底部时间信息右对齐 */
.card-footer .publish-time {
  margin-left: auto;
  font-weight: 500;
}

/* 封面图片样式 */
.card-image-container {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 20px 20px 0 0;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.card-image:hover {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.4) 0%,
    transparent 30%,
    transparent 70%,
    rgba(0, 0, 0, 0.6) 100%
  );
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px;
}

.source-badge {
  background: rgba(255, 255, 255, 0.95);
  color: #007AFF;
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-overlay .publish-time {
  color: white;
  font-size: 11px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  background: rgba(0, 0, 0, 0.3);
  padding: 4px 8px;
  border-radius: 6px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}


/* 智能加载状态和响应式保留 */
.smart-loading-overlay { position: absolute; inset: 0; background: rgba(255,255,255,0.9); display:flex; align-items:center; justify-content:center; backdrop-filter: blur(10px); }
.loading-spinner { width: 32px; height: 32px; border: 3px solid var(--border-light); border-top: 3px solid var(--ios-blue); border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg);} 100% { transform: rotate(360deg);} }

@media (max-width: 768px) {
  .modern-news-card { 
    border-radius: 16px; 
  }
  
  .card-content { 
    padding: 16px; 
    gap: 12px;
  }
  
  .card-title { 
    font-size: 16px; 
    line-height: 1.4;
  }
  
  .card-description { 
    font-size: 13px; 
    line-height: 1.4;
  }
  
  .card-header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  .category-tags {
    gap: 6px;
  }
  
  .category-tag,
  .difficulty-tag {
    padding: 3px 6px;
    font-size: 10px;
  }
  
  .reading-info {
    gap: 12px;
    font-size: 11px;
  }
  
  .card-image-container {
    height: 160px;
  }
  
  .discovery-type {
    font-size: 10px;
    padding: 1px 4px;
  }
  
  /* 无图片情况下的移动端样式 */
  .card-content.no-image {
    padding: 20px;
    gap: 16px;
  }
  
  .card-content.no-image .card-title {
    font-size: 18px;
    -webkit-line-clamp: 2;
    line-clamp: 2;
  }
  
  .card-content.no-image .card-description {
    font-size: 14px;
    -webkit-line-clamp: 3;
    line-clamp: 3;
  }
  
}
</style>

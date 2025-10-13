<template>
  <div class="airbnb-modern-card tactile-button" @click="handleClick">
    <!-- 卡片内容 -->
    <div class="card-content compact">
      <!-- 分类与难度 -->
      <div class="compact-meta">
        <span class="tag category">{{ article.category || '未分类' }}</span>
        <span class="tag difficulty">{{ getDifficultyText(article.difficulty || '') }}</span>
      </div>

      <!-- 标题 -->
      <h3 class="card-title">{{ article.title }}</h3>

      <!-- 摘要 -->
      <p class="card-description">
        {{ article.description || (article.enContent ? truncateText(article.enContent, 120) + '...' : '暂无描述') }}
      </p>

      <!-- 元信息 -->
      <div class="card-meta">
        <div class="meta-item">
          <span class="meta-icon">⏱️</span>
          <span>{{ getEstimatedReadTime }}分钟</span>
        </div>
        <div class="meta-item" v-if="article.wordCount">
          <span class="meta-icon">📝</span>
          <span>{{ formatWordCount(article.wordCount) }}词</span>
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
  category?: string
  readTime?: number
  wordCount?: number
  difficulty?: number | string
  enContent?: string
  [key: string]: any
}

const props = defineProps<{
  article: Article
}>()

const router = useRouter()
const loading = ref(false)

// 截断文本函数
const truncateText = (text: string, maxLength: number): string => {
  if (!text || text.length <= maxLength) return text
  return text.slice(0, maxLength) + '...'
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

.airbnb-modern-card {
  background: linear-gradient(135deg, var(--glass-white) 0%, rgba(255, 255, 255, 0.8) 100%);
  border-radius: 20px;
  overflow: hidden;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 2px solid transparent;
  background-clip: padding-box;
}

.airbnb-modern-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(0, 122, 255, 0.1) 0%, 
    rgba(255, 119, 198, 0.1) 50%, 
    rgba(120, 219, 255, 0.1) 100%);
  border-radius: 20px;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.airbnb-modern-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.15),
    0 8px 16px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.airbnb-modern-card:hover::before {
  opacity: 1;
}

.card-content.compact {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  z-index: 1;
  height: 100%;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.1) 0%, 
    rgba(255, 255, 255, 0.05) 100%);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 
    inset 0 1px 0 rgba(255, 255, 255, 0.2),
    0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.compact-meta {
  display: flex;
  gap: 8px;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tag.category {
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.2) 0%, rgba(90, 200, 250, 0.2) 100%);
  color: #007AFF;
}

.tag.category::before {
  content: '📂';
  font-size: 10px;
}

.tag.difficulty {
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.2) 0%, rgba(255, 235, 59, 0.2) 100%);
  color: #FF9500;
}

.tag.difficulty::before {
  content: '⭐';
  font-size: 10px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, var(--text-primary) 0%, var(--text-secondary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-description {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.05);
  background: linear-gradient(135deg, var(--text-secondary) 0%, rgba(0, 0, 0, 0.6) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-meta {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-top: auto;
  justify-content: flex-end;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.meta-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.25);
}

.meta-item:first-child {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.meta-item:last-child {
  background: linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%);
  box-shadow: 0 4px 12px rgba(78, 205, 196, 0.3);
}

/* 智能加载状态和响应式保留 */
.smart-loading-overlay { position: absolute; inset: 0; background: rgba(255,255,255,0.9); display:flex; align-items:center; justify-content:center; backdrop-filter: blur(10px); }
.loading-spinner { width: 32px; height: 32px; border: 3px solid var(--border-light); border-top: 3px solid var(--ios-blue); border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg);} 100% { transform: rotate(360deg);} }

@media (max-width: 768px) {
  .airbnb-modern-card { 
    border-radius: 16px; 
  }
  .card-content.compact { 
    padding: 20px; 
    border-radius: 12px;
  }
  .card-title { 
    font-size: 16px; 
  }
  .card-description { 
    font-size: 13px; 
  }
  .tag {
    padding: 4px 8px;
    font-size: 11px;
  }
  .meta-item {
    padding: 4px 8px;
    font-size: 11px;
    border-radius: 16px;
  }
}
</style>

<template>
  <div class="discovery-card" @click="handleClick">
    <!-- 探索标识 -->
    <div class="discovery-badge">
      <span class="badge-icon">{{ getDiscoveryLabel.icon }}</span>
      <span class="badge-text">{{ getDiscoveryLabel.text }}</span>
    </div>

    <!-- 新获取标识 -->
    <div class="new-badge">
      <span class="new-text">NEW</span>
    </div>

    <!-- 卡片内容 -->
    <div class="card-content">
      <!-- 标题 -->
      <h3 class="card-title">{{ article.title }}</h3>

      <!-- 摘要 -->
      <p class="card-description">
        {{ article.description || (article.enContent ? truncateText(article.enContent, 120) + '...' : '暂无描述') }}
      </p>

      <!-- 分类与难度标签 -->
      <div class="compact-meta">
        <span class="tag category">{{ article.category || '未分类' }}</span>
        <span class="tag difficulty">{{ getDifficultyText(article.difficulty || '') }}</span>
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
  discoveryType?: 'trending' | 'category' | 'custom'
}>()

const router = useRouter()
const loading = ref(false)

// 截断文本函数
const truncateText = (text: string, maxLength: number): string => {
  if (!text || text.length <= maxLength) return text
  return text.slice(0, maxLength) + '...'
}

// 将难度等级转换为文本描述
const getDifficultyText = (difficulty: number | string): string => {
  if (!difficulty && difficulty !== 0) {
    return '未知'
  }

  if (typeof difficulty === 'number' && !isNaN(difficulty)) {
    switch (difficulty) {
      case 1: return '简单'
      case 2: return '较易'
      case 3: return '中等'
      case 4: return '较难'
      case 5: return '极难'
      default: return '未知'
    }
  }

  const difficultyStr = String(difficulty).trim()
  if (!difficultyStr) {
    return '未知'
  }

  const numDifficulty = parseInt(difficultyStr)
  if (!isNaN(numDifficulty)) {
    switch (numDifficulty) {
      case 1: return '简单'
      case 2: return '较易'
      case 3: return '中等'
      case 4: return '较难'
      case 5: return '极难'
    }
  }

  return difficultyStr
}

// 处理卡片点击事件
const handleClick = () => {
  loading.value = true
  router.push(`/article/${props.article.id || 1}`)
  setTimeout(() => {
    loading.value = false
  }, 1000)
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
</script>

<style scoped>
@import '@/assets/design-system.css';

.discovery-card {
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

.discovery-card::before {
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

.discovery-card:hover::before {
  opacity: 1;
}

.discovery-card:hover {
  transform: translateY(-12px) scale(1.03);
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.15),
    0 8px 16px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  border-color: rgba(0, 122, 255, 0.3);
}

/* 探索标识 */
.discovery-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3);
  z-index: 2;
}

.badge-icon {
  font-size: 14px;
}

.badge-text {
  letter-spacing: 0.5px;
}

/* 新获取标识 */
.new-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background: linear-gradient(135deg, #FF3B30 0%, #FF9500 100%);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1px;
  box-shadow: 0 2px 8px rgba(255, 59, 48, 0.4);
  z-index: 2;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.card-content {
  padding: 24px;
  padding-top: 60px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  z-index: 1;
  height: 100%; /* 恢复完整高度 */
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.1) 0%, 
    rgba(255, 255, 255, 0.05) 100%);
  border-radius: 16px; /* 恢复完整圆角 */
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 
    inset 0 1px 0 rgba(255, 255, 255, 0.2),
    0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  /* 移除margin-top，让content填满整个卡片 */
}

.compact-meta {
  display: flex;
  gap: 8px;
  margin-top: auto;
  justify-content: flex-end;
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
  background: linear-gradient(135deg, rgba(255, 149, 0, 0.2) 0%, rgba(255, 204, 0, 0.2) 100%);
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

/* 智能加载状态 */
.smart-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  z-index: 10;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--border-light);
  border-top: 3px solid var(--ios-blue);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .discovery-card {
    border-radius: 16px;
  }
  
  .card-content {
    padding: 20px;
  }
  
  .card-title {
    font-size: 16px;
  }
  
  .card-description {
    font-size: 13px;
  }
  
  .compact-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
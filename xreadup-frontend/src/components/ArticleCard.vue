<template>
  <div class="article-card" @click="handleClick">
    <!-- 文章内容 -->
    <div class="card-content">
      <!-- 文章分类标签和难度等级 -->
      <div class="article-category-container" v-if="article.category || true">
        <div class="article-category" v-if="article.category">
          {{ article.category }}
        </div>
        <div class="article-difficulty">
          {{ getDifficultyText(article.difficulty) }}
        </div>
      </div>

      <!-- 文章标题 -->
      <h3 class="article-title">{{ article.title }}</h3>

      <!-- 文章摘要 -->
      <p class="article-excerpt">
        {{ article.description || (article.enContent ? truncateText(article.enContent, 100) + '...' : '暂无描述') }}
      </p>

      <!-- 文章元信息 -->
      <div class="article-meta">
        <span class="meta-item word-count" v-if="article.wordCount">
          {{ formatWordCount(article.wordCount) }}字 · {{ getEstimatedReadTime }}分钟
        </span>
        <span class="meta-item read-time" v-else>
          {{ getEstimatedReadTime }}分钟
        </span>
      </div>
    </div>

    <!-- 悬浮效果层 -->
    <div class="card-overlay">
      <span class="read-more">阅读全文</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
// 移除Reading图标的导入

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
  router.push(`/article/${props.article.id}`)
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
.article-card {
  position: relative;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-content {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

/* 分类和难度等级容器 */
.article-category-container {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  align-self: flex-start;
}

.article-category {
  display: inline-block;
  padding: 4px 10px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
  border-radius: 12px;
  font-weight: 500;
}

/* 难度等级样式 */
.article-difficulty {
  display: inline-block;
  padding: 4px 10px;
  background: #fff7e6;
  color: #e6a23c;
  font-size: 12px;
  border-radius: 12px;
  font-weight: 500;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-excerpt {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  position: absolute;
  bottom: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #909399;
  background: rgba(255, 255, 255, 0.8);
  padding: 4px 8px;
  border-radius: 12px;
  backdrop-filter: blur(2px);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-icon {
  font-size: 12px;
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0) 0%, rgba(0,0,0,0.6) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 24px;
  color: white;
}

.article-card:hover .card-overlay {
  opacity: 1;
}

.read-more {
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  backdrop-filter: blur(4px);
  transition: all 0.3s ease;
}

.article-card:hover .read-more {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-card {
    border-radius: 8px;
  }

  .card-content {
    padding: 16px;
  }

  .article-title {
    font-size: 16px;
  }

  .article-excerpt {
    font-size: 13px;
  }
}

/* 骨架屏样式适配 */
:deep(.skeleton-cover) {
  display: none;
}

:deep(.skeleton-title) {
  width: 80%;
  height: 20px;
  background: #f5f5f5;
  border-radius: 4px;
  margin-bottom: 8px;
}

:deep(.skeleton-desc) {
  width: 100%;
  height: 16px;
  background: #f5f5f5;
  border-radius: 4px;
  margin-bottom: 8px;
}

:deep(.skeleton-meta) {
  width: 50%;
  height: 14px;
  background: #f5f5f5;
  border-radius: 4px;
}
</style>

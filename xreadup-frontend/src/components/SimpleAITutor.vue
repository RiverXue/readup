<template>
  <el-dialog
    v-model="visible"
    title=""
    width="800px"
    :before-close="handleClose"
    class="ai-tutor-dialog"
    :show-close="false"
  >
    <!-- 自定义头部 -->
    <template #header>
      <div class="dialog-header">
        <div class="header-left">
          <div class="tutor-avatar">
            <el-icon><Star /></el-icon>
          </div>
          <div class="header-info">
            <h2>🎓 Rayda老师</h2>
            <p>文章学习指导</p>
          </div>
        </div>
        <div class="header-actions">
          <el-button @click="handleClose" type="text" class="close-btn">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>
    </template>

    <div class="ai-tutor-content">
      <!-- 用户提示 -->
      <div class="user-notice">
        <div class="notice-card">
          <div class="notice-icon">📚</div>
          <div class="notice-content">
            <h4>专注文章学习</h4>
            <p>Rayda老师专注于当前文章的学习指导，帮助您深入理解文章内容。</p>
            <p class="notice-tip">如需更多学习规划、个性化建议等功能，请前往 <strong>AI学导</strong> 体验完整功能。</p>
          </div>
        </div>
      </div>

      <!-- 文章信息卡片 -->
      <div class="article-info-card">
        <div class="card-header">
          <h3>📖 当前文章</h3>
        </div>
        <div class="article-content">
          <div class="article-title">{{ article.title }}</div>
          <div class="article-meta">
            <el-tag :type="getDifficultyType(article.difficulty)" size="small">
              {{ article.difficulty }}
            </el-tag>
            <span class="category">{{ article.category }}</span>
          </div>
        </div>
      </div>

      <!-- 快速问题建议 -->
      <div class="quick-questions" v-if="!isLoading && !currentAnswer">
        <div class="section-header">
          <h3>💡 快速提问</h3>
          <p>选择一个问题开始学习</p>
        </div>
        <div class="question-grid">
          <div
            v-for="question in quickQuestions"
            :key="question.id"
            @click="askQuestion(question.text)"
            class="question-card"
          >
            <div class="question-icon">{{ question.icon }}</div>
            <div class="question-text">{{ question.text }}</div>
            <div class="question-type">{{ getQuestionType(question.text) }}</div>
          </div>
        </div>
      </div>

      <!-- 对话区域 -->
      <div class="chat-area" v-if="currentAnswer || isLoading">
        <div class="ai-response" v-if="currentAnswer">
          <div class="message ai">
            <div class="message-avatar">
              <el-icon><Star /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-text">{{ currentAnswer }}</div>
              <div class="message-time">{{ formatTime(new Date()) }}</div>
            </div>
          </div>
          <div class="follow-up" v-if="followUpQuestion">
            <el-button 
              @click="askQuestion(followUpQuestion)" 
              type="primary" 
              size="small"
              class="follow-up-btn"
            >
              {{ followUpQuestion }}
            </el-button>
          </div>
        </div>
        
        <div class="loading" v-if="isLoading">
          <div class="loading-content">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>Rayda老师正在分析文章...</span>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-container">
          <el-input
            v-model="userQuestion"
            placeholder="向Rayda老师提问关于这篇文章的任何问题..."
            @keyup.enter="submitQuestion"
            :disabled="isLoading"
            clearable
            class="question-input"
          >
            <template #append>
              <el-button 
                @click="submitQuestion" 
                :loading="isLoading"
                type="primary"
                class="send-btn"
              >
                提问
              </el-button>
            </template>
          </el-input>
          <div class="input-tips">
            <span>按 Enter 发送，Shift + Enter 换行</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="clearChat" type="info" plain>
          <el-icon><Refresh /></el-icon>
          重新开始
        </el-button>
        <el-button @click="handleClose" type="primary">
          <el-icon><Check /></el-icon>
          完成学习
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Star, Close, Refresh, Check } from '@element-plus/icons-vue'
import { aiApi } from '@/utils/api'
import { useUserStore } from '@/stores/user'

interface Article {
  id: number
  title: string
  category: string
  difficulty: string
  enContent: string
  description?: string
}

interface QuickQuestion {
  id: number
  text: string
  icon: string
  type: string
}

const props = defineProps<{
  modelValue: boolean
  article: Article
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const userStore = useUserStore()

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const userQuestion = ref('')
const currentAnswer = ref('')
const followUpQuestion = ref('')
const isLoading = ref(false)

// 快速问题建议
const quickQuestions = ref<QuickQuestion[]>([
  {
    id: 1,
    text: '这篇文章讲了什么？',
    icon: '📖',
    type: 'primary'
  },
  {
    id: 2,
    text: '有哪些重点词汇？',
    icon: '📝',
    type: 'success'
  },
  {
    id: 3,
    text: '语法点解析',
    icon: '🔍',
    type: 'warning'
  },
  {
    id: 4,
    text: '如何提高理解？',
    icon: '💡',
    type: 'info'
  },
  {
    id: 5,
    text: '写作技巧分析',
    icon: '✍️',
    type: 'danger'
  },
  {
    id: 6,
    text: '文化背景介绍',
    icon: '🌍',
    type: 'primary'
  }
])

// 获取难度类型
const getDifficultyType = (difficulty: string) => {
  const difficultyMap: Record<string, string> = {
    'A1': 'success',
    'A2': 'info', 
    'B1': 'warning',
    'B2': 'danger',
    'C1': 'danger',
    'C2': 'danger'
  }
  return difficultyMap[difficulty] || 'info'
}

// 获取问题类型
const getQuestionType = (question: string) => {
  if (question.includes('讲了什么') || question.includes('内容')) return '内容理解'
  if (question.includes('词汇') || question.includes('单词')) return '词汇学习'
  if (question.includes('语法')) return '语法解析'
  if (question.includes('提高') || question.includes('理解')) return '学习建议'
  if (question.includes('技巧') || question.includes('写作')) return '写作技巧'
  if (question.includes('文化') || question.includes('背景')) return '文化背景'
  return '学习指导'
}

// 格式化时间
const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// 提问函数
const askQuestion = async (question: string) => {
  if (!question.trim()) return
  
  userQuestion.value = question
  await submitQuestion()
}

// 提交问题
const submitQuestion = async () => {
  if (!userQuestion.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }

  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    return
  }

  if (!userStore.checkAiQuota()) {
    return
  }

  isLoading.value = true
  currentAnswer.value = ''
  followUpQuestion.value = ''

  try {
    console.log('🤖 简配AI学导提问:', {
      question: userQuestion.value,
      article: props.article.title,
      difficulty: props.article.difficulty
    })

    // 使用专门的简配版API，节省token
    const response = await aiApi.simpleTutorChat(
      userQuestion.value,
      Number(userStore.userInfo.id),
      {
        title: props.article.title,
        category: props.article.category,
        difficulty: props.article.difficulty,
        description: props.article.description || 
                    (props.article.enContent ? props.article.enContent.substring(0, 200) : '文章内容不可用')
      }
    )

    if (response && response.data) {
      // 处理简配版API的响应格式
      currentAnswer.value = response.data.answer || ''
      followUpQuestion.value = response.data.followUpQuestion || ''
      
      console.log('✅ 简配AI学导回答:', currentAnswer.value)
    } else {
      throw new Error('AI响应格式错误')
    }

  } catch (error: any) {
    console.error('AI学导提问失败:', error)
    
    let errorMessage = '提问失败，请稍后重试'
    if (error.response?.status === 401) {
      errorMessage = '请先登录'
    } else if (error.response?.status === 403) {
      errorMessage = 'AI功能权限不足'
    } else if (error.response?.status >= 500) {
      errorMessage = 'AI服务暂时不可用'
    }
    
    ElMessage.error(errorMessage)
    currentAnswer.value = 'Rayda老师暂时无法回答这个问题。请稍后再试或换个方式提问。'
  } finally {
    isLoading.value = false
  }
}

// 清空对话
const clearChat = () => {
  currentAnswer.value = ''
  followUpQuestion.value = ''
  userQuestion.value = ''
}

// 关闭弹窗
const handleClose = () => {
  visible.value = false
  clearChat()
}

// 监听弹窗打开，清空之前的内容
watch(visible, (newVal) => {
  if (newVal) {
    clearChat()
  }
})
</script>

<style scoped>
/* 弹窗整体样式 */
.ai-tutor-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.ai-tutor-dialog :deep(.el-dialog__header) {
  padding: 0;
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  color: white;
}

.ai-tutor-dialog :deep(.el-dialog__body) {
  padding: 0;
  background: #f8f9fa;
}

.ai-tutor-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  background: #f8f9fa;
  border-top: 1px solid #e2e8f0;
}

/* 自定义头部 */
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tutor-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.header-info h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.header-info p {
  margin: 4px 0 0 0;
  font-size: 14px;
  opacity: 0.9;
}

.close-btn {
  color: white !important;
  font-size: 18px;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1) !important;
}

/* 内容区域 */
.ai-tutor-content {
  max-height: 600px;
  overflow-y: auto;
  padding: 24px;
}

/* 用户提示 */
.user-notice {
  margin-bottom: 24px;
}

.notice-card {
  background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
  border: 1px solid #bbdefb;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.notice-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.notice-content h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1976d2;
}

.notice-content p {
  margin: 0 0 8px 0;
  font-size: 14px;
  line-height: 1.5;
  color: #424242;
}

.notice-tip {
  font-size: 13px;
  color: #666;
}

/* 文章信息卡片 */
.article-info-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  margin-bottom: 24px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  color: white;
  padding: 12px 16px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.article-content {
  padding: 16px;
}

.article-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  line-height: 1.4;
  color: #2d3748;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category {
  font-size: 14px;
  color: #718096;
}

/* 快速问题区域 */
.quick-questions {
  margin-bottom: 24px;
}

.section-header {
  text-align: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 700;
  color: #2d3748;
}

.section-header p {
  margin: 0;
  font-size: 14px;
  color: #718096;
}

.question-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.question-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.question-card:hover {
  border-color: #667eea;
  background: #f8f9ff;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
}

.question-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.question-text {
  font-size: 14px;
  font-weight: 500;
  color: #2d3748;
  margin-bottom: 6px;
  line-height: 1.4;
}

.question-type {
  font-size: 12px;
  color: #667eea;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 对话区域 */
.chat-area {
  margin-bottom: 24px;
  min-height: 120px;
}

.message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  font-size: 16px;
}

.message-content {
  flex: 1;
  max-width: 80%;
}

.message-text {
  background: white;
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  color: #2d3748;
  word-wrap: break-word;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.message-time {
  font-size: 12px;
  color: #a0aec0;
  margin-top: 4px;
}

.follow-up {
  margin-top: 16px;
  text-align: center;
}

.follow-up-btn {
  border-radius: 20px;
  padding: 8px 20px;
}

/* 加载状态 */
.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.loading-content {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
}

/* 输入区域 */
.input-area {
  background: white;
  border-top: 1px solid #e2e8f0;
  padding: 20px 24px;
}

.input-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.question-input {
  border-radius: 12px;
}

.input-tips {
  font-size: 12px;
  color: #a0aec0;
  text-align: center;
}

.send-btn {
  border-radius: 0 12px 12px 0;
  font-weight: 600;
}

/* 底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer .el-button {
  border-radius: 8px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-tutor-dialog :deep(.el-dialog) {
    width: 95%;
    margin: 0 auto;
  }
  
  .question-grid {
    grid-template-columns: 1fr;
  }
  
  .dialog-header {
    padding: 16px 20px;
  }
  
  .ai-tutor-content {
    padding: 20px;
  }
  
  .input-area {
    padding: 16px 20px;
  }
}
</style>

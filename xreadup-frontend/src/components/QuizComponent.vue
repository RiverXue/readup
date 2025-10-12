<template>
  <div class="quiz-container">
    <!-- 测验题头部 -->
    <div class="quiz-header">
      <div class="quiz-title">
        <el-icon class="quiz-icon"><Reading /></el-icon>
        <h3>读后测验</h3>
      </div>
      <div class="quiz-progress">
        <span class="current-question">{{ currentQuestionIndex + 1 }}</span>
        <span class="separator">/</span>
        <span class="total-questions">{{ questions.length }}</span>
      </div>
    </div>

    <!-- 测验题内容 -->
    <div class="quiz-content" v-if="!quizCompleted">
      <!-- 问题卡片 -->
      <el-card class="question-card" shadow="hover">
        <div class="question-header">
          <div class="question-number">问题 {{ currentQuestionIndex + 1 }}</div>
          <div class="question-difficulty" :class="currentQuestion.difficulty">
            {{ getDifficultyText(currentQuestion.difficulty) }}
          </div>
        </div>

        <div class="question-text">
          {{ currentQuestion.question }}
        </div>

        <!-- 选项列表 -->
        <div class="options-container">
          <div
            v-for="(option, index) in currentQuestion.options"
            :key="index"
            class="option-item"
            :class="{
              'selected': selectedAnswers[currentQuestionIndex] === index,
              'correct': showAnswers && isCorrectAnswer(index),
              'incorrect': showAnswers && selectedAnswers[currentQuestionIndex] === index && !isCorrectAnswer(index)
            }"
            @click="selectAnswer(index)"
          >
            <div class="option-letter">{{ String.fromCharCode(65 + index) }}</div>
            <div class="option-text">{{ option }}</div>
            <div class="option-status" v-if="showAnswers">
              <el-icon v-if="isCorrectAnswer(index)" class="correct-icon"><Check /></el-icon>
              <el-icon v-else-if="selectedAnswers[currentQuestionIndex] === index && !isCorrectAnswer(index)" class="incorrect-icon"><Close /></el-icon>
            </div>
          </div>
        </div>

        <!-- 解析区域 -->
        <div v-if="showAnswers" class="explanation-section">
          <div class="explanation-header">
            <el-icon><InfoFilled /></el-icon>
            <span>解析</span>
          </div>
          <div class="explanation-content">
            {{ currentQuestion.explanation }}
          </div>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="quiz-actions">
        <el-button
          v-if="currentQuestionIndex > 0"
          @click="previousQuestion"
          :icon="ArrowLeft"
        >
          上一题
        </el-button>

        <el-button
          v-if="currentQuestionIndex < questions.length - 1"
          type="primary"
          @click="nextQuestion"
          :disabled="selectedAnswers[currentQuestionIndex] === undefined"
          :icon="ArrowRight"
        >
          下一题
        </el-button>

        <el-button
          v-if="currentQuestionIndex === questions.length - 1 && !showAnswers"
          type="success"
          @click="submitQuiz"
          :disabled="selectedAnswers[currentQuestionIndex] === undefined"
          :icon="Check"
        >
          提交答案
        </el-button>

        <el-button
          v-if="showAnswers && currentQuestionIndex < questions.length - 1"
          type="primary"
          @click="nextQuestion"
          :icon="ArrowRight"
        >
          下一题
        </el-button>

        <el-button
          v-if="showAnswers && currentQuestionIndex === questions.length - 1"
          type="success"
          @click="finishQuiz"
          :icon="Trophy"
        >
          查看成绩
        </el-button>
      </div>
    </div>

    <!-- 测验完成页面 -->
    <div v-if="quizCompleted" class="quiz-completed">
      <el-card class="result-card" shadow="hover">
        <div class="result-header">
          <el-icon class="result-icon" :class="getScoreClass()">
            <Trophy v-if="score >= 80" />
            <Medal v-else-if="score >= 60" />
            <Star v-else />
          </el-icon>
          <h2>测验完成</h2>
        </div>

        <div class="score-display">
          <div class="score-circle" :class="getScoreClass()">
            <span class="score-number">{{ score }}</span>
            <span class="score-total">/100</span>
          </div>
          <div class="score-text">{{ getScoreText() }}</div>
        </div>

        <div class="score-details">
          <div class="detail-item">
            <span class="detail-label">正确题数：</span>
            <span class="detail-value">{{ correctCount }}/{{ questions.length }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">用时：</span>
            <span class="detail-value">{{ formatTime(quizTime) }}</span>
          </div>
        </div>

        <div class="result-actions">
          <el-button @click="restartQuiz" :icon="Refresh">重新测验</el-button>
          <el-button type="primary" @click="reviewAnswers" :icon="View">查看解析</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import {
  Reading,
  Check,
  Close,
  InfoFilled,
  ArrowLeft,
  ArrowRight,
  Trophy,
  Medal,
  Star,
  Refresh,
  View
} from '@element-plus/icons-vue'

interface QuizQuestion {
  id: string
  question: string
  options: string[]
  answer: string
  correctAnswer?: string
  correctAnswerText?: string
  explanation: string
  questionType: string
  difficulty: string
}

interface Props {
  questions: QuizQuestion[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  complete: [score: number, time: number]
}>()

// 响应式数据
const currentQuestionIndex = ref(0)
const selectedAnswers = ref<number[]>([])
const showAnswers = ref(false)
const quizCompleted = ref(false)
const quizStartTime = ref<number>(0)
const quizTime = ref<number>(0)
const timer = ref<number | null>(null)

// 计算属性
const currentQuestion = computed(() => props.questions[currentQuestionIndex.value])

const correctCount = computed(() => {
  return selectedAnswers.value.filter((answer, index) => {
    const question = props.questions[index]
    const correctAnswer = question.answer || question.correctAnswer || question.correctAnswerText
    return answer === getCorrectAnswerIndex(question, correctAnswer)
  }).length
})

const score = computed(() => {
  if (props.questions.length === 0) return 0
  return Math.round((correctCount.value / props.questions.length) * 100)
})

// 方法
const selectAnswer = (index: number) => {
  if (showAnswers.value) return
  selectedAnswers.value[currentQuestionIndex.value] = index
}

const isCorrectAnswer = (index: number) => {
  const question = currentQuestion.value
  const correctAnswer = question.answer || question.correctAnswer || question.correctAnswerText
  return index === getCorrectAnswerIndex(question, correctAnswer || undefined)
}

const getCorrectAnswerIndex = (question: QuizQuestion, correctAnswer: string | undefined) => {
  if (!correctAnswer) return -1

  // 提取答案字母 (A, B, C, D)
  const answerLetter = correctAnswer.trim().charAt(0).toUpperCase()
  return answerLetter.charCodeAt(0) - 65
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < props.questions.length - 1) {
    currentQuestionIndex.value++
  }
}

const previousQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const submitQuiz = () => {
  showAnswers.value = true
  stopTimer()
}

const finishQuiz = () => {
  quizCompleted.value = true
  emit('complete', score.value, quizTime.value)
}

const restartQuiz = () => {
  currentQuestionIndex.value = 0
  selectedAnswers.value = []
  showAnswers.value = false
  quizCompleted.value = false
  quizTime.value = 0
  startTimer()
}

const reviewAnswers = () => {
  currentQuestionIndex.value = 0
  showAnswers.value = true
  quizCompleted.value = false
}

const startTimer = () => {
  quizStartTime.value = Date.now()
  timer.value = setInterval(() => {
    quizTime.value = Date.now() - quizStartTime.value
  }, 1000)
}

const stopTimer = () => {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
}

const formatTime = (ms: number) => {
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const getDifficultyText = (difficulty: string) => {
  const difficultyMap: Record<string, string> = {
    'easy': '简单',
    'medium': '中等',
    'hard': '困难'
  }
  return difficultyMap[difficulty] || '中等'
}

const getScoreClass = () => {
  if (score.value >= 80) return 'excellent'
  if (score.value >= 60) return 'good'
  return 'poor'
}

const getScoreText = () => {
  if (score.value >= 90) return '优秀！'
  if (score.value >= 80) return '良好！'
  if (score.value >= 60) return '及格'
  return '需要加强'
}

// 生命周期
onMounted(() => {
  startTimer()
})

onUnmounted(() => {
  stopTimer()
})
</script>

<style scoped>
.quiz-container {
  width: 100%;
  margin: 0;
  padding: 0;
}

.quiz-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.quiz-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quiz-title h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.quiz-icon {
  font-size: 24px;
}

.quiz-progress {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 16px;
  font-weight: 500;
}

.current-question {
  font-size: 20px;
  font-weight: bold;
}

.separator {
  opacity: 0.7;
}

.total-questions {
  opacity: 0.7;
}

.question-card {
  margin-bottom: 24px;
  border-radius: 16px;
  overflow: hidden;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.question-number {
  font-size: 16px;
  font-weight: 600;
  color: #667eea;
}

.question-difficulty {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.question-difficulty.easy {
  background: #e8f5e8;
  color: #52c41a;
}

.question-difficulty.medium {
  background: #fff7e6;
  color: #fa8c16;
}

.question-difficulty.hard {
  background: #fff1f0;
  color: #ff4d4f;
}

.question-text {
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 24px;
  color: #333;
  font-weight: 500;
}

.options-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.option-item:hover {
  border-color: #667eea;
  background: #f8f9ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.option-item.selected {
  border-color: #667eea;
  background: #f0f2ff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.option-item.correct {
  border-color: #52c41a;
  background: #f6ffed;
}

.option-item.incorrect {
  border-color: #ff4d4f;
  background: #fff2f0;
}

.option-letter {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #667eea;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-right: 16px;
  flex-shrink: 0;
}

.option-item.correct .option-letter {
  background: #52c41a;
}

.option-item.incorrect .option-letter {
  background: #ff4d4f;
}

.option-text {
  flex: 1;
  font-size: 16px;
  line-height: 1.5;
}

.option-status {
  margin-left: 12px;
}

.correct-icon {
  color: #52c41a;
  font-size: 20px;
}

.incorrect-icon {
  color: #ff4d4f;
  font-size: 20px;
}

.explanation-section {
  margin-top: 24px;
  padding: 20px;
  background: #f8f9ff;
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.explanation-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #667eea;
}

.explanation-content {
  font-size: 15px;
  line-height: 1.6;
  color: #555;
}

.quiz-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

/* 测验完成页面优化 */
.quiz-completed {
  text-align: center;
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-card {
  max-width: 100%;
  margin: 0;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.result-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.result-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
  padding: 20px 0;
  background: #ffffff;
}

.result-icon {
  font-size: 80px;
  margin-bottom: 16px;
  animation: bounceIn 0.6s ease-in-out;
}

@keyframes bounceIn {
  0%, 20%, 40%, 60%, 80%, 100% {
    transition-timing-function: cubic-bezier(0.215, 0.610, 0.355, 1.000);
  }
  0% {
    opacity: 0;
    transform: scale3d(0.3, 0.3, 0.3);
  }
  20% {
    transform: scale3d(1.1, 1.1, 1.1);
  }
  40% {
    transform: scale3d(0.9, 0.9, 0.9);
  }
  60% {
    opacity: 1;
    transform: scale3d(1.03, 1.03, 1.03);
  }
  80% {
    transform: scale3d(0.97, 0.97, 0.97);
  }
  100% {
    opacity: 1;
    transform: scale3d(1, 1, 1);
  }
}

.result-icon.excellent {
  color: #ffd700;
  filter: drop-shadow(0 0 15px rgba(255, 215, 0, 0.3));
}

.result-icon.good {
  color: #2196f3;
  filter: drop-shadow(0 0 15px rgba(33, 150, 243, 0.3));
}

.result-icon.poor {
  color: #ff9800;
  filter: drop-shadow(0 0 15px rgba(255, 152, 0, 0.3));
}

.result-header h2 {
  margin: 0;
  font-size: 32px;
  color: #333;
  font-weight: 700;
}

.score-display {
  margin-bottom: 32px;
  position: relative;
}

.score-circle {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-weight: bold;
  color: white;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
  position: relative;
  overflow: hidden;
}

.score-circle::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: inherit;
  filter: blur(8px);
  transform: scale(1.2);
  opacity: 0.3;
}

.score-circle:hover {
  transform: scale(1.05);
}

.score-circle.excellent {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  color: #333;
}

.score-circle.good {
  background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%);
  color: white;
}

.score-circle.poor {
  background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
  color: white;
}

.score-number {
  font-size: 48px;
  line-height: 1;
  position: relative;
  z-index: 1;
}

.score-total {
  font-size: 20px;
  opacity: 0.9;
  position: relative;
  z-index: 1;
}

.score-text {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-top: 8px;
}

.score-details {
  display: flex;
  justify-content: space-around;
  margin-bottom: 32px;
  padding: 24px;
  background: white;
  border-radius: 16px;
  border: 2px solid #f0f5ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.detail-item {
  text-align: center;
  position: relative;
  padding: 0 10px;
}

.detail-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 15%;
  height: 70%;
  width: 1px;
  background: #e8e8e8;
}

.detail-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 500;
}

.detail-value {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  line-height: 1;
}

.result-actions {
  display: flex;
  justify-content: center;
  padding: 0 20px 20px;
}

.result-actions .el-button {
  font-size: 13px;
  font-weight: 600;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.result-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.result-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.result-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .quiz-container {
    padding: 16px;
  }

  .quiz-header {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }

  .question-text {
    font-size: 16px;
  }

  .option-item {
    padding: 12px 16px;
  }

  .option-text {
    font-size: 15px;
  }

  .quiz-actions {
    flex-direction: column;
    align-items: center;
  }

  .result-actions {
    flex-direction: column;
    align-items: center;
  }

  .score-circle {
    width: 120px;
    height: 120px;
  }

  .score-number {
    font-size: 36px;
  }

  .detail-value {
    font-size: 18px;
  }
}
</style>

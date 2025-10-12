<template>
  <div class="quiz-test-page">
    <div class="test-header">
      <h1>测验题功能测试</h1>
      <p>测试交互式测验题组件的功能</p>
    </div>

    <div class="test-actions">
      <el-button type="primary" @click="loadSampleQuiz">加载示例测验题</el-button>
      <el-button @click="clearQuiz">清空测验题</el-button>
    </div>

    <div v-if="quizQuestions.length > 0" class="quiz-container">
      <QuizComponent 
        :questions="quizQuestions" 
        @complete="onQuizComplete"
      />
    </div>

    <div v-else class="empty-state">
      <el-empty description="暂无测验题">
        <el-button type="primary" @click="loadSampleQuiz">加载示例测验题</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import QuizComponent from '@/components/QuizComponent.vue'

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

const quizQuestions = ref<QuizQuestion[]>([])

const loadSampleQuiz = () => {
  quizQuestions.value = [
    {
      id: '1',
      question: 'What is the main idea of this passage?',
      options: [
        'Artificial intelligence is dangerous',
        'AI technology is becoming more integrated into daily life',
        'Machine learning is a subset of AI',
        'AI development raises ethical questions'
      ],
      answer: 'B',
      correctAnswer: 'B',
      correctAnswerText: 'B',
      explanation: 'The passage discusses how AI technology is becoming increasingly integrated into our daily lives, from smartphones to self-driving cars.',
      questionType: 'comprehension',
      difficulty: 'medium'
    },
    {
      id: '2',
      question: 'According to the passage, what enables computers to learn without explicit programming?',
      options: [
        'Artificial intelligence',
        'Machine learning',
        'Deep learning',
        'Neural networks'
      ],
      answer: 'B',
      correctAnswer: 'B',
      correctAnswerText: 'B',
      explanation: 'The passage specifically states that "Machine learning, a subset of AI, enables computers to learn and improve from experience without being explicitly programmed."',
      questionType: 'detail',
      difficulty: 'easy'
    },
    {
      id: '3',
      question: 'What does the passage suggest about the future of AI?',
      options: [
        'AI will completely replace human workers',
        'AI development should be stopped',
        'Both benefits and risks need to be considered',
        'AI will solve all ethical problems'
      ],
      answer: 'C',
      correctAnswer: 'C',
      correctAnswerText: 'C',
      explanation: 'The passage emphasizes that "it\'s crucial to consider both the benefits and potential risks of artificial intelligence" as we continue to advance in this field.',
      questionType: 'inference',
      difficulty: 'hard'
    }
  ]
  
  ElMessage.success('示例测验题加载成功！')
}

const clearQuiz = () => {
  quizQuestions.value = []
  ElMessage.info('测验题已清空')
}

const onQuizComplete = (score: number, time: number) => {
  console.log('测验完成:', { score, time })
  ElMessage.success(`测验完成！得分：${score}分，用时：${Math.floor(time / 1000)}秒`)
}
</script>

<style scoped>
.quiz-test-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.test-header {
  text-align: center;
  margin-bottom: 40px;
}

.test-header h1 {
  color: #333;
  margin-bottom: 10px;
}

.test-header p {
  color: #666;
  font-size: 16px;
}

.test-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 40px;
}

.quiz-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.empty-state {
  margin-top: 60px;
}

@media (max-width: 768px) {
  .quiz-test-page {
    padding: 16px;
  }
  
  .test-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>

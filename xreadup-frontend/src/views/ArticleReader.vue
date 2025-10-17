<template>
  <div class="reader-container">

    <!-- 左侧边栏：状态、提示、AI工具 -->
    <div class="sidebar" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
      <!-- 用户状态卡片（顶部） -->
      <div class="user-status-card">
        <div class="user-info">
          <div class="user-name">{{ userStore.userInfo?.username || '游客' }}</div>
        </div>
        <div class="user-stats">
          <div class="user-level" :class="getUserLevelClass()">
            <el-icon><Trophy /></el-icon>
            {{ getUserLevelText() }}
          </div>
        </div>
      </div>

      <!-- AI工具栏（中部核心） -->
      <div class="ai-tools-section">
        <div class="section-header">
          <h3>AI 引擎</h3>
          <div class="ai-status" :class="getAIStatusClass()">
            <el-icon><Star /></el-icon>
            {{ getAIStatusText() }}
          </div>
        </div>
        <!-- 常驻AI状态提示区 -->
        <div class="ai-live-status" :class="['phase-' + aiState.phase]">
          <div class="status-dot" :class="['phase-' + aiState.phase]"></div>
          <div class="status-text">{{ aiState.message }}</div>
        </div>

        <!-- 核心功能区域 -->
        <div class="core-functions">
          <el-button
            type="primary"
            :loading="loading.translate"
            @click="translate"
            class="function-button primary-button"
            size="large"
          >
            <el-icon><Document /></el-icon>
            <span>{{ userStore.hasAIFeatures ? '智能翻译' : '基础翻译' }}</span>
          </el-button>

          <el-button
            type="success"
            :loading="loading.summary"
            @click="aiSummary"
            class="function-button success-button"
            size="large"
          >
            <el-icon><Document /></el-icon>
            <span>AI摘要</span>
          </el-button>
        </div>

        <!-- 高级功能区域 -->
        <div class="advanced-functions">
          <el-button
            type="warning"
            :loading="loading.parse"
            @click="aiParseSelection"
            :disabled="!hasTextSelection"
            class="function-button warning-button"
            size="large"
          >
            <el-icon><MagicStick /></el-icon>
            <span>句子解析</span>
          </el-button>

          <el-button
            type="info"
            :loading="loading.quiz"
            @click="generateQuiz"
            class="function-button info-button"
            size="large"
          >
            <el-icon><Reading /></el-icon>
            <span>读后测验</span>
          </el-button>

          <el-button
            type="danger"
            @click="askAI"
            class="function-button danger-button"
            size="large"
          >
            <el-icon><ChatLineRound /></el-icon>
            <span>AI助手</span>
          </el-button>
        </div>

        <!-- AI结果展示区 - 融入侧边栏 -->
        <div v-if="aiResult && !isQuizMode" class="sidebar-ai-result">
          <div class="sidebar-result-header">
            <h4>{{ aiTitle }}</h4>
          </div>
          <div class="formatted-ai-result">
            <p v-for="(paragraph, index) in formattedAIResult" :key="index" class="result-paragraph">{{ paragraph }}</p>
          </div>
        </div>

        <!-- 交互式测验题组件 -->
        <div v-if="isQuizMode" class="sidebar-quiz-result">
          <QuizComponent
            :questions="quizQuestions"
            @complete="onQuizComplete"
          />
        </div>
      </div>

      <!-- 会员升级提示（底部） -->
      <div class="upgrade-section" v-if="!userStore.hasAIFeatures && userStore.isLoggedIn">
        <el-card class="upgrade-card" shadow="hover">
          <div class="upgrade-content">
            <div class="upgrade-icon">
              <el-icon size="24"><Star /></el-icon>
            </div>
            <div class="upgrade-text">
              <div class="upgrade-title">解锁全部AI功能</div>
              <div class="upgrade-desc">体验智能翻译、AI摘要等高级功能</div>
            </div>
            <el-button
              type="primary"
              size="small"
              @click="$router.push('/subscription')"
              class="upgrade-button"
            >
              立即升级
            </el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 侧边栏切换按钮 -->
    <div class="sidebar-toggle" @click="toggleSidebar">
      <el-button
        type="default"
        circle
        size="small"
        :title="isSidebarCollapsed ? '展开侧边栏' : '收起侧边栏'"
      >
        <el-icon>
          <ArrowLeft v-if="!isSidebarCollapsed" />
          <ArrowRight v-else />
        </el-icon>
      </el-button>
    </div>

    <!-- 主内容区：文章与翻译 -->
    <div class="main-content" :class="{ 'main-content-expanded': isSidebarCollapsed }">
      <!-- 文章标题与元数据 -->
      <div class="article-header">
        <div class="article-title-section">
          <h1 class="article-title">{{ article.title }}</h1>
        </div>

        <div class="article-meta">
          <div class="meta-tags">
            <el-tag
              size="large"
              :type="getDifficultyType(article.difficulty)"
              class="difficulty-tag"
            >
              {{ article.difficulty || '未知难度' }}
            </el-tag>
            <el-tag size="large" type="info" class="category-tag">
              {{ article.category || '未分类' }}
            </el-tag>
          </div>
          <div class="meta-stats">
            <div class="stat-item">
              <el-icon><View /></el-icon>
              <span>{{ article.readCount || 0 }} 次阅读</span>
            </div>
            <div class="stat-item">
              <el-icon><Clock /></el-icon>
              <span>{{ getReadingTime() }} 分钟阅读</span>
            </div>
            <div class="stat-item">
              <el-icon><Document /></el-icon>
              <span>{{ getWordCount() }} 词</span>
            </div>
          </div>
        </div>
      </div>



      <!-- 双语阅读区 -->
      <div class="bilingual-content">
        <!-- 付费用户：行间翻译 -->
        <div v-if="isPremiumUser" class="free-bilingual-layout">
          <div class="section-header">
            <h3>📖 英文原文</h3>
            <div class="article-actions">
              <el-button
                v-if="!tts.isBusy()"
                @click="handleSpeakArticle"
                size="small"
                type="primary"
                title="朗读全文"
              >
                朗读
              </el-button>
              <el-button
                v-else
                @click="handleStopSpeaking"
                size="small"
                type="danger"
                title="停止朗读"
              >
                停止
              </el-button>
            </div>
          </div>
          <div class="english-content" @click="onWordClick" @dblclick="onWordDoubleClick">
            <template v-for="(item, index) in contentItems" :key="index">
              <!-- 段落和对应的行间翻译 -->
              <div v-if="item.type === 'paragraph'" class="sentence-group">
                <p class="paragraph english-sentence" :class="{ 'highlighted': highlightedParagraphIndex === index }" :data-paragraph-index="index">
                  {{ item.content }}
                </p>
                <!-- 付费用户的行间翻译 -->
                <p v-if="showTranslation && chineseParagraphs && chineseParagraphs[index]" class="paragraph chinese-sentence">
                  {{ chineseParagraphs[index] }}
                </p>
              </div>

              <!-- AI句子解析结果（嵌入在段落下方） -->
              <div v-else-if="item.type === 'ai-parse'" class="inline-parse-card" :class="isPremiumUser ? 'premium-card' : 'free-card'">
                <div class="parse-card-header">
                  <h4>AI句子解析</h4>
                  <el-button
                    type="text"
                    size="small"
                    @click="removeParseResult(index)"
                    class="close-button"
                  >
                    <el-icon><CircleClose /></el-icon>
                  </el-button>
                </div>

                <!-- 原句和核心含义 (所有用户可见) -->
                <div class="parse-basic-info">
                  <p class="original-sentence">🎯 原句：{{ item.content?.originalSentence }}</p>
                  <p class="core-meaning">💡 核心含义：{{ item.content?.meaning }}</p>

                  <!-- 重点词汇 (所有用户可见) -->
                  <div v-if="item.content?.keyVocabulary && item.content.keyVocabulary.length > 0" class="key-vocab">
                    <p>📚 重点词汇：</p>
                    <div class="vocab-list">
                      <div v-for="(vocab, idx) in item.content.keyVocabulary" :key="idx" class="vocab-item">
                        <span class="vocab-word">{{ vocab.word ? vocab.word : vocab }}</span>
                        <span v-if="vocab.meaning" class="vocab-meaning">({{ vocab.meaning }})</span>
                        <span v-if="vocab.wordType" class="vocab-type">{{ vocab.wordType }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 完整解析内容 (仅付费用户可见) -->
                <div v-if="isPremiumUser && item.content" class="premium-content">
                  <!-- 句子结构 -->
                  <div v-if="item.content.sentenceStructure" class="sentence-structure">
                    <p>🏗 句子结构：</p>
                    <div class="structure-detail">
                      <template v-if="typeof item.content.sentenceStructure === 'object'">
                        <div v-for="(value, key) in item.content.sentenceStructure" :key="key" class="structure-item">
                          <strong>{{ getStructureKeyName(String(key)) }}：</strong>
                          <span>{{ Array.isArray(value) ? value.join(', ') : value }}</span>
                        </div>
                      </template>
                      <template v-else>
                        <span>{{ item.content.sentenceStructure }}</span>
                      </template>
                    </div>
                  </div>

                  <!-- 语法分析 -->
                  <div v-if="item.content.grammar" class="grammar-analysis">
                    <p>📝 语法分析：</p>
                    <div class="grammar-detail">
                      <p>时态: {{ item.content.grammar.tense || '未知' }}</p>
                      <p>语态: {{ item.content.grammar.voice || '未知' }}</p>
                    </div>
                  </div>

                  <!-- 语法要点 -->
                  <div v-if="item.content.grammarPoints && item.content.grammarPoints.length > 0" class="grammar-points">
                    <p>⚡ 语法要点：</p>
                    <ul>
                      <li v-for="(point, idx) in item.content.grammarPoints" :key="idx">{{ point }}</li>
                    </ul>
                  </div>

                  <!-- 学习建议 -->
                  <div v-if="item.content.learningTip" class="learning-tip">
                    <p>💡 学习建议：</p>
                    <p>{{ item.content.learningTip }}</p>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>

        <!-- 免费用户：双栏并排显示 -->
        <div v-else class="premium-bilingual-layout">
          <!-- 英文原文 -->
    <div class="english-section">
      <div class="section-header">
        <h3>📖 英文原文</h3>
        <div class="article-actions">
          <el-button
            v-if="!isTTSSpeaking()"
            @click="handleSpeakArticle"
            size="small"
            type="primary"
            title="朗读全文"
          >
            朗读
          </el-button>
          <el-button
            v-else
            @click="handleStopSpeaking"
            size="small"
            type="danger"
            title="停止朗读"
          >
            停止
          </el-button>
        </div>
      </div>
      <div class="english-content" @click="onWordClick" @dblclick="onWordDoubleClick">
        <template v-for="(item, index) in contentItems" :key="index">
          <!-- 段落 -->
          <p v-if="item.type === 'paragraph'" class="paragraph" :class="{ 'highlighted': highlightedParagraphIndex === index }" :data-paragraph-index="index">
            {{ item.content }}
          </p>

                <!-- AI句子解析结果（嵌入在段落下方） -->
                <div v-else-if="item.type === 'ai-parse'" class="inline-parse-card" :class="isPremiumUser ? 'premium-card' : 'free-card'">
                  <div class="parse-card-header">
                    <h4>AI句子解析</h4>
                    <el-button
                      type="text"
                      size="small"
                      @click="removeParseResult(index)"
                      class="close-button"
                    >
                      <el-icon><CircleClose /></el-icon>
                    </el-button>
                  </div>

                  <!-- 原句和核心含义 (所有用户可见) -->
                  <div class="parse-basic-info">
                    <p class="original-sentence">🎯 原句：{{ item.content?.originalSentence }}</p>
                    <p class="core-meaning">💡 核心含义：{{ item.content?.meaning }}</p>

                    <!-- 重点词汇 (所有用户可见) -->
                    <div v-if="item.content?.keyVocabulary && item.content.keyVocabulary.length > 0" class="key-vocab">
                      <p>📚 重点词汇：</p>
                      <div class="vocab-list">
                        <div v-for="(vocab, idx) in getLimitedVocabulary(item.content.keyVocabulary)" :key="idx" class="vocab-item">
                          <span class="vocab-word">{{ vocab.word ? vocab.word : vocab }}</span>
                          <span v-if="vocab.meaning" class="vocab-meaning">({{ vocab.meaning }})</span>
                          <span v-if="vocab.wordType" class="vocab-type">{{ vocab.wordType }}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 升级提示 (仅免费用户可见) -->
                  <div class="upgrade-prompt">
                    <p>完整解析（语法结构、学习建议等）仅对专业会员开放</p>
                    <el-button
                      type="primary"
                      size="small"
                      @click="$router.push('/subscription')"
                      class="upgrade-button"
                    >
                      升级会员
                    </el-button>
                  </div>
                </div>
              </template>
            </div>
          </div>

          <!-- 翻译分界线 -->
          <div v-if="showTranslation" class="translation-divider"></div>

          <!-- 免费用户的中文翻译 (显示在原文下方) -->
          <div v-if="showTranslation" class="chinese-section">
            <h3>📚 中文翻译</h3>
            <div class="chinese-content">
              <p v-for="(paragraph, index) in chineseParagraphs" :key="index" class="paragraph">
                {{ paragraph }}
              </p>
            </div>
          </div>
        </div>

        <!-- 翻译切换按钮 -->
        <div class="translation-toggle">
          <el-button type="text" @click="toggleTranslation">
            {{ showTranslation ? '收起翻译' : '展开中文翻译' }}
            <el-icon>
              <ArrowDown v-if="!showTranslation" />
              <ArrowUp v-else />
            </el-icon>
          </el-button>
        </div>

        <!-- AI摘要结果（文章末尾） -->
        <div v-if="articleSummary" class="ai-summary-result">
          <h3>📋 AI摘要</h3>
          <div v-html="formatAIAnswer(articleSummary)"></div>
        </div>
      </div>


    </div>

    <!-- AI助手对话框 -->
    <el-dialog v-model="aiDialogVisible" title="💬 AI助手" width="500px">
      <div class="ai-chat">
        <el-input
          v-model="aiQuestion"
          type="textarea"
          :rows="3"
          placeholder="请输入你的问题..."
          maxlength="200"
          show-word-limit
        />
        <div class="chat-actions">
          <el-button type="primary" @click="submitAIQuestion" :loading="aiLoading">
            发送问题
          </el-button>
        </div>
        <div v-if="aiAnswer" class="ai-answer">
          <h4>AI回答：</h4>
          <div v-html="formatAIAnswer(aiAnswer)"></div>
        </div>
      </div>
    </el-dialog>

    <!-- 单词查询弹窗 -->
    <el-dialog v-model="wordDialogVisible" title="📚 单词详情" width="400px">
      <div v-if="wordDetail" class="word-detail">
            <div style="text-align: center;">
              <h3 class="word">{{ wordDetail.word }}</h3>
            </div>
            <div v-if="wordDetail.phonetic" style="text-align: center;">
              <div class="phonetic" style="margin: 0 auto;">[{{ wordDetail.phonetic }}]</div>
              <el-button
            v-if="ttsControlRef"
            type="text"
            size="small"
            @click="ttsControlRef.speakWord(wordDetail.word)"
            title="发音"
            class="pronunciation-button"
            style="display: block; margin: 10px auto 0;"
          >
            🔊
          </el-button>
            </div>
            <div class="meaning" v-html="formatMeaning(wordDetail.meaning)"></div>
            <div class="example" v-if="wordDetail.example">
              <strong>例句：</strong><span v-html="formatExample(wordDetail.example)"></span>
            </div>
            <div class="context" v-if="wordDetail.context">
              <strong>语境：</strong>{{ wordDetail.context }}
            </div>
            <div class="actions">
              <el-button type="primary" size="small" @click="addWordToVocabulary">
                📚 已同步添加到生词本
              </el-button>
            </div>
          </div>
    </el-dialog>

    <!-- 首次使用引导弹窗 -->
    <el-dialog
      v-model="showFirstUseGuide"
      title="功能使用提示"
      width="500px"
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="first-use-guide">
        <div class="guide-content">
          <div class="guide-icon">
            <svg width="60" height="60" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="#409EFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="#409EFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="#409EFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="guide-text">
            <h4>如何使用阅读功能？</h4>
            <p>📚 <strong>双击单词</strong> 可快速查询单词释义</p>
            <p>✏️ <strong>选中句子</strong> 后点击"AI句子解析"可深入学习语法</p>
            <p>🎭 <strong>点击侧边栏切换按钮</strong> 可收起侧边栏，享受沉浸式阅读体验</p>
            <p>🎯 试试这些功能，提升你的阅读体验！</p>
          </div>
        </div>
        <div class="guide-actions">
          <el-button @click="remindLater">下次提醒我</el-button>
          <el-button type="primary" @click="neverRemind">下次不再提示</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 生词本浮动按钮 -->
    <div class="floating-vocab" @click="goToVocabulary">
      <el-badge :value="vocabCount" :hidden="vocabCount === 0">
        <el-button type="primary" circle>
          <el-icon><Collection /></el-icon>
        </el-button>
      </el-badge>
      <span class="vocab-text">生词本</span>
    </div>

    <!-- TTS控制组件 -->
    <TTSControl
      ref="ttsControlRef"
      :paragraphs="englishParagraphs"
      :highlighted-paragraph-index="highlightedParagraphIndex"
      @highlight-paragraph="handleHighlightParagraph"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { aiApi, articleApi, vocabularyApi, learningApi, request as api } from '@/utils/api'
import { useUserStore } from '@/stores/user'
import { Document, MagicStick, ChatLineRound, ArrowDown, ArrowUp, Collection, Search, ArrowLeft, ArrowRight, CircleClose, Trophy, Star, StarFilled, Reading, View, Clock } from '@element-plus/icons-vue'
import { subscriptionApi } from '@/utils/api'
import type { UsageQuota } from '@/types/subscription'
import QuizComponent from '@/components/QuizComponent.vue'
import TTSControl from '@/components/common/TTSControl.vue'
import { tts } from '@/utils/tts'

type AiPhase = 'idle' | 'loading' | 'success' | 'error'
interface AiState {
  phase: AiPhase
  message: string
}

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

// TTSControl组件暴露的方法类型定义
interface TTSControlExposed {
  handleSpeakArticle: () => void
  handleStopSpeaking: () => void
  handlePauseResumeSpeaking: () => void
  handleSpeedChange: (value: number) => void
  speakWord: (word: string) => void
  handleWordClick: (event: MouseEvent) => string | null
  loadVoices: () => void
}

// 组件引用
const ttsControlRef = ref<TTSControlExposed | undefined>()
const highlightedParagraphIndex = ref<number | null>(null)

// 格式化AI助手回复，过滤特殊标签
const formatAIAnswer = (answer: string): string => {
  if (!answer) return ''
  // 过滤掉<lookupWord>和<translate>标签
  let formatted = answer
    .replace(/<lookupWord>.*?<\/lookupWord>/gs, '')
    .replace(/<translate>.*?<\/translate>/gs, '')
    .replace(/调用结果：\n?/g, '')
  // 替换换行符为<br>
  formatted = formatted.replace(/\n/g, '<br>')
  return formatted
}

// 格式化单词释义，处理换行和词性
const formatMeaning = (meaning: string): string => {
  if (!meaning) return ''

  // 在每个新的词性前添加换行符
  // 匹配中文词性标签：名词、动词、形容词、副词、介词、代词、连词、数词、感叹词、量词、拟声词、专有名词
  let formatted = meaning.replace(/([,，]\s*)(名词|动词|形容词|副词|介词|代词|连词|数词|感叹词|量词|拟声词|专有名词)/g, '<br>$2')

  // 突出显示词性标记
  formatted = formatted.replace(/(名词|动词|形容词|副词|介词|代词|连词|数词|感叹词|量词|拟声词|专有名词):/g, '<strong>$1：</strong>')

  return formatted
}

// 格式化例句，处理换行和中英文对照
const formatExample = (example: string): string => {
  if (!example) return ''

  // 在例句的中文翻译前添加换行符
  let formatted = example.replace(/【/g, '<br>【')

  return formatted
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

interface Article {
  id: number
  title: string
  enContent: string
  cnContent: string
  category: string
  difficulty: string
  tags: string[]
  readCount?: number
}

interface WordDetail {
  word: string
  meaning: string
  example?: string
  context?: string
  phonetic?: string
}

// 文章数据
const article = ref<Article>({
  id: 0,
  title: '',
  enContent: '',
  cnContent: '',
  category: '',
  difficulty: '',
  tags: [],
  readCount: 0
})

// 状态管理
const showTranslation = ref(false)
const loading = ref({
  translate: false,
  summary: false,
  parse: false,
  quiz: false
})
const aiResult = ref('')
const aiTitle = ref('')


// 格式化AI结果为段落数组
const formattedAIResult = computed(() => {
  if (!aiResult.value) return []

  // 将文本按换行符分割为段落
  let paragraphs = aiResult.value.split(/\n\n+/)

  // 清理每个段落
  paragraphs = paragraphs
    .map(p => p.trim())
    .filter(p => p.length > 0) // 过滤掉空段落

  // 如果没有段落，就按单换行分割
  if (paragraphs.length === 0) {
    paragraphs = aiResult.value.split(/\n/)
      .map(p => p.trim())
      .filter(p => p.length > 0)
  }

  return paragraphs
})

const isQuizMode = ref(false)
const quizQuestions = ref<QuizQuestion[]>([])
const selectedWord = ref('')
const hasTextSelection = ref(false)
const isFavorited = ref(false)
const subscriptionInfo = ref({
  currentPlan: 'FREE',
  usedArticles: 0,
  totalArticles: 3
})
const showUpgradeTip = ref(true)
// 首次使用引导状态
const showFirstUseGuide = ref(false)
// 侧边栏折叠状态
const isSidebarCollapsed = ref(false)

// 上下文内容项（文章段落和AI解析结果）
interface ContentItem {
  type: 'paragraph' | 'ai-parse'
  content: string | any
}
const contentItems = ref<ContentItem[]>([])

// 文章摘要
const articleSummary = ref<string>('')

// 常驻AI状态提示区
const aiState = ref<AiState>({ phase: 'idle', message: '准备就绪' })

const setAiState = (phase: AiPhase, message: string) => {
  aiState.value = { phase, message }
}

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

// 限制免费用户看到的词汇数量
const getLimitedVocabulary = (vocabList: any[]) => {
  // 免费用户只显示1个重点词汇，付费用户显示全部
  return isPremiumUser.value ? vocabList : vocabList.slice(0, 1)
}

// 句子结构关键字名称映射
const getStructureKeyName = (key: string) => {
  const keyNameMap: Record<string, string> = {
    'subject': '主语',
    'predicate': '谓语',
    'object': '宾语',
    'adverbial': '状语',
    'prepositionalPhrase': '介词短语',
    'phraseBreakdown': '句法分析',
    'mainClause': '主句',
    'subordinateClause': '从句',
    'sentenceType': '句子类型'
  }
  return keyNameMap[key] || key
}

// 关闭内联提示卡片
const closeInlineParseResult = () => {
  // 移除所有AI解析结果
  contentItems.value = contentItems.value.filter(item => item.type !== 'ai-parse')
}

// 移除特定位置的AI解析结果
const removeParseResult = (index: number) => {
  contentItems.value.splice(index, 1)
}

// 单词查询
const wordDialogVisible = ref(false)
const wordDetail = ref<WordDetail | null>(null)

// AI助手
const aiDialogVisible = ref(false)
const aiQuestion = ref('')
const aiAnswer = ref('')
const aiLoading = ref(false)

// 生词本计数
const vocabCount = ref(0)

// 阅读计时相关变量
const startTime = ref<number | null>(null)
const timerInterval = ref<number | null>(null)
const readTimeSec = ref(0)
// 页面可见性状态
const isPageVisible = ref(true)

// 会员等级检测计算属性
const isBasicMember = computed(() => {
  return userStore.userTier === 'basic'
})

const isPremiumUser = computed(() => {
  return userStore.userTier === 'pro' || userStore.userTier === 'enterprise'
})

const isProOrEnterpriseUser = computed(() => {
  return userStore.userTier === 'pro' || userStore.userTier === 'enterprise'
})

// 加载用户生词本数量
const loadVocabCount = async () => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      vocabCount.value = 0
      return
    }

    // 使用实际API获取词汇数量
    const statsResponse = await vocabularyApi.getVocabularyStats(String(userId))
    if (statsResponse?.data) {
      vocabCount.value = statsResponse.data.totalWords || 0
    }
  } catch (error) {
    console.error('获取生词本数量失败:', error)
    // 保持当前值不变
  }
}

// 加载订阅使用情况
const loadSubscriptionInfo = async () => {
  try {
    if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
      return
    }

    const userId = userStore.userInfo.id
    const numericUserId = typeof userId === 'string' ? parseInt(userId, 10) : userId

    const quotaRes = await subscriptionApi.getRemainingQuota(numericUserId)
    if (quotaRes?.data) {
      subscriptionInfo.value = {
        currentPlan: userStore.userTier,
        usedArticles: quotaRes.data.articlesQuota?.used || 0,
        totalArticles: quotaRes.data.articlesQuota?.total || 3
      }
    }
  } catch (error) {
    console.error('获取订阅使用情况失败:', error)
    // 保持默认值
  }
}

// 获取进度条状态
const getProgressStatus = (used: number, total: number) => {
  const percentage = (used / total) * 100
  if (percentage >= 90) return 'exception'
  if (percentage >= 70) return 'warning'
  return 'success'
}

// 关闭升级提示
const dismissUpgradeTip = () => {
  // 设置临时关闭状态，页面刷新后会重新显示
  localStorage.setItem('dismissUpgradeTip', 'true')
  showUpgradeTip.value = false
}

// 计算属性 - 智能段落分割，支持多种换行符格式和自然段落边界
const englishParagraphs = computed(() => {
  if (!article.value.enContent) return []

  // 首先尝试使用段落标记分割（两个以上的换行符）
  const paragraphsByDoubleNewline = article.value.enContent.split(/\n{2,}/).filter(p => p.trim())
  if (paragraphsByDoubleNewline.length > 0) {
    // 对每个段落内部再处理，移除多余的换行符
    return paragraphsByDoubleNewline.map(p => p.replace(/\n+/g, ' ').trim())
  }

  // 其次尝试使用单个换行符分割
  const paragraphsByNewline = article.value.enContent.split(/\n+/).filter(p => p.trim())
  if (paragraphsByNewline.length > 3) {
    return paragraphsByNewline
  }

  // 如果内容没有明显的换行符段落结构，则尝试智能分割自然段落
  // 1. 首先处理原始内容，去除多余空格
  let content = article.value.enContent.trim().replace(/\s+/g, ' ')

  // 2. 改进的智能段落分割逻辑：识别句号、问号、感叹号后接多个空格+大写字母的模式
  // 这种模式更可能表示一个段落结束，一个新段落开始
  const naturalParagraphs: string[] = []
  let currentParagraph = ''
  let i = 0

  while (i < content.length) {
    currentParagraph += content[i]

    // 检查是否是段落结束的标志
    if ((content[i] === '.' || content[i] === '?' || content[i] === '!') &&
        i + 3 < content.length &&
        /\s{2,}/.test(content.substring(i+1, i+3)) &&
        /[A-Z]/.test(content[i + 2])) {

      // 收集当前段落
      naturalParagraphs.push(currentParagraph.trim())
      currentParagraph = ''
      i += 2 // 跳过空格和下一个大写字母
    } else {
      i++
    }
  }

  // 添加最后一个段落（如果有）
  if (currentParagraph.trim()) {
    naturalParagraphs.push(currentParagraph.trim())
  }

  // 如果智能分割得到了合理数量的段落，则使用智能分割结果
  if (naturalParagraphs.length > 1) {
    // 过滤掉过短的段落（少于10个单词），将它们合并到前一个段落
    const filteredParagraphs: string[] = []
    for (let i = 0; i < naturalParagraphs.length; i++) {
      const paragraph = naturalParagraphs[i]
      const wordCount = paragraph.split(/\s+/).length

      if (wordCount < 10 && i > 0) {
        // 将短段落合并到前一个段落
        filteredParagraphs[filteredParagraphs.length - 1] += ' ' + paragraph
      } else {
        filteredParagraphs.push(paragraph)
      }
    }
    return filteredParagraphs
  }

  // 如果所有分割方法都不奏效，返回原始内容作为一个段落
  return [article.value.enContent.trim()]
})

// 更新上下文内容项
const updateContentItems = () => {
  // 重置内容项
  contentItems.value = []

  // 将英文段落转换为内容项
  englishParagraphs.value.forEach(paragraph => {
    contentItems.value.push({
      type: 'paragraph',
      content: paragraph
    })
  })
}

// 在指定位置插入AI解析结果
const insertParseResult = (parseData: any, sentence: string) => {
  // 查找包含选中句子的段落
  const targetIndex = contentItems.value.findIndex(item =>
    item.type === 'paragraph' &&
    (item.content as string).includes(sentence)
  )

  if (targetIndex !== -1) {
    // 在目标段落下方插入解析结果
    contentItems.value.splice(targetIndex + 1, 0, {
      type: 'ai-parse',
      content: parseData
    })
  } else {
    // 如果找不到目标段落，将解析结果添加到内容末尾
    contentItems.value.push({
      type: 'ai-parse',
      content: parseData
    })
  }
}

const chineseParagraphs = computed(() => {
  if (!article.value.cnContent) return []

  // 首先尝试使用段落标记分割（两个以上的换行符）
  const paragraphsByDoubleNewline = article.value.cnContent.split(/\n{2,}/).filter(p => p.trim())
  if (paragraphsByDoubleNewline.length > 0) {
    // 对每个段落内部再处理，移除多余的换行符
    return paragraphsByDoubleNewline.map(p => p.replace(/\n+/g, ' ').trim())
  }

  // 其次尝试使用单个换行符分割
  const paragraphsByNewline = article.value.cnContent.split(/\n+/).filter(p => p.trim())
  if (paragraphsByNewline.length > 3) {
    return paragraphsByNewline
  }

  // 如果英文段落已分段，尝试让中文段落与英文段落数量保持一致
  if (englishParagraphs.value.length > 1 && paragraphsByNewline.length === 1) {
    // 改进的段落划分策略：考虑中文语义段落结构
    const singleParagraph = paragraphsByNewline[0]
    const result: string[] = []
    let currentPos = 0

    // 首先尝试寻找中文自然段落分割点
    const chinesePunctuationMarks = ['。', '？', '！', '…', '；']
    const naturalBreaks: number[] = []

    // 找出所有可能的段落分割点
    for (let i = 0; i < singleParagraph.length; i++) {
      if (chinesePunctuationMarks.includes(singleParagraph[i]) && i > 0) {
        // 查看上下文，判断是否可能是段落结束
        const prevChar = singleParagraph[i-1]
        const nextChar = i + 1 < singleParagraph.length ? singleParagraph[i+1] : ''

        // 更严格的段落分割规则：
        // 1. 句号后接换行或两个以上空格
        // 2. 句号后接段落起始特征（如数字、小标题等）
        if (nextChar === '\n' || /\s{2,}/.test(nextChar) || /[\d一二三四五六七八九十]/.test(nextChar)) {
          naturalBreaks.push(i + 1)
        }
      }
    }

    // 如果找到了足够的自然分割点，使用这些分割点
    if (naturalBreaks.length >= englishParagraphs.value.length - 1) {
      // 选择最接近平均分布的分割点
      const targetBreaks: number[] = []
      const avgCharsPerParagraph = Math.ceil(singleParagraph.length / englishParagraphs.value.length)

      for (let i = 0; i < englishParagraphs.value.length - 1; i++) {
        const targetPos = (i + 1) * avgCharsPerParagraph
        // 找到最接近目标位置的自然分割点
        const closestBreak = naturalBreaks.reduce((prev, curr) => {
          return Math.abs(curr - targetPos) < Math.abs(prev - targetPos) ? curr : prev
        }, naturalBreaks[0])
        targetBreaks.push(closestBreak)
      }

      // 确保分割点按顺序排列且不重复
      const uniqueSortedBreaks = [...new Set(targetBreaks)].sort((a, b) => a - b)

      // 根据分割点创建段落
      for (let i = 0; i <= uniqueSortedBreaks.length; i++) {
        const start = i === 0 ? 0 : uniqueSortedBreaks[i - 1]
        const end = i === uniqueSortedBreaks.length ? singleParagraph.length : uniqueSortedBreaks[i]
        const paragraph = singleParagraph.substring(start, end).trim()
        if (paragraph) {
          result.push(paragraph)
        }
      }
    } else {
      // 如果没有找到足够的自然分割点，使用平均分配策略
      const avgLength = Math.ceil(singleParagraph.length / englishParagraphs.value.length)

      // 为每个英文段落创建对应的中文段落
      for (let i = 0; i < englishParagraphs.value.length; i++) {
        // 尝试在标点符号处分割，保持语义完整
        if (i < englishParagraphs.value.length - 1) {
          // 查找最佳分割点（优先选择句号、问号、感叹号、省略号等）
          let splitPoint = currentPos + avgLength

          // 扩大搜索范围，确保找到合适的分割点
          for (let j = splitPoint; j < Math.min(splitPoint + 40, singleParagraph.length); j++) {
            if (chinesePunctuationMarks.includes(singleParagraph[j])) {
              splitPoint = j + 1
              break
            }
          }

          result.push(singleParagraph.substring(currentPos, splitPoint).trim())
          currentPos = splitPoint
        } else {
          // 最后一段包含剩余所有内容
          result.push(singleParagraph.substring(currentPos).trim())
        }
      }
    }

    // 过滤掉过短的段落（少于20个字符），将它们合并到前一个段落
    const filteredParagraphs: string[] = []
    for (let i = 0; i < result.length; i++) {
      const paragraph = result[i]

      if (paragraph.length < 20 && i > 0) {
        // 将短段落合并到前一个段落
        filteredParagraphs[filteredParagraphs.length - 1] += paragraph
      } else {
        filteredParagraphs.push(paragraph)
      }
    }

    return filteredParagraphs.filter(p => p.trim())
  }

  // 默认返回通过换行符分割的段落
  return paragraphsByNewline
})

// 获取文章
const loadArticle = async () => {
  try {
    const res = await articleApi.readArticle(route.params.id as string)
    const data = res.data
    article.value = {
      id: data.article?.id || 0,
      title: data.article?.title || '',
      enContent: data.article?.contentEn || '',
      cnContent: data.article?.contentCn || '',
      category: data.article?.category || '',
      difficulty: data.article?.difficultyLevel || '',
      tags: data.article?.tags || [],
      readCount: data.article?.readCount || 0
    }

    // 初始化内容项
    updateContentItems()

    // 加载用户生词本数量
    if (userStore.isLoggedIn) {
      loadVocabCount()
      loadSubscriptionInfo()
    }
  } catch {
    ElMessage.error('获取文章失败')
  }
}

// 腾讯云翻译
const translate = async () => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    ElMessage.warning('请先登录以使用翻译功能')
    return
  }

  // 检查数据库是否已有翻译结果
  if (article.value.cnContent && article.value.cnContent.trim()) {
    console.log('📚 发现数据库已有翻译结果，直接显示')
    showTranslation.value = true
    aiResult.value = article.value.cnContent
    aiTitle.value = userStore.hasAIFeatures ? '智能翻译结果' : '基础翻译结果'
    return
  }

  // 检查AI调用配额
  if (!userStore.checkAiQuota()) return

  // 重置测验模式
  isQuizMode.value = false
  quizQuestions.value = []

  loading.value.translate = true
  try {
    // 使用类型断言解决TypeScript类型推断问题
    console.log('🔄 开始翻译请求:', {
      timestamp: new Date().toISOString(),
      userId: userStore.userInfo?.id,
      contentLength: article.value.enContent.length,
      articleId: route.params.id,
      route: window.location.pathname,
      userTier: userStore.userTier,
      hasAIFeatures: userStore.hasAIFeatures
    })

    setAiState('loading', '正在翻译，请稍候…')
    console.time('翻译请求耗时')
    // 使用分层翻译策略：根据用户等级自动选择翻译服务
    const userId = userStore.userInfo?.id ? parseInt(userStore.userInfo.id) : undefined
    const res = (await aiApi.translate(article.value.enContent, userId)) as any
    console.timeEnd('翻译请求耗时')

    console.log('✅ 翻译请求成功，结果:', res)

    // 处理不同格式的翻译结果
    // 从ApiResponse<TencentTranslateResponseDTO>中提取数据
    const translatedText = res?.data?.translatedText || res?.data || '翻译失败'
    article.value.cnContent = translatedText
    showTranslation.value = true
    aiResult.value = translatedText
    aiTitle.value = userStore.hasAIFeatures ? '智能翻译结果' : '基础翻译结果'
    setAiState('success', '翻译完成')
  } catch (error) {
    // 使用类型断言解决TypeScript unknown类型问题
    const err = error as any
    console.error('❌ 翻译失败:',
      err.response?.data || err.message || error,
      {
        requestDetails: {
          articleId: route.params.id,
          contentPreview: article.value.enContent.substring(0, 50),
          errorCode: err.code,
          errorName: err.name,
          url: err.config?.url,
          userTier: userStore.userTier
        }
      }
    )
    ElMessage.error('翻译失败，请稍后重试')
    setAiState('error', '翻译失败，请稍后重试')
  } finally {
    loading.value.translate = false
    if (aiState.value.phase === 'loading') setAiState('idle', '准备就绪')
  }
}

// AI摘要
const aiSummary = async () => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    ElMessage.warning('请先登录以使用AI摘要功能')
    return
  }

  // 检查AI调用配额
  if (!userStore.checkAiQuota()) return

  // 重置测验模式
  isQuizMode.value = false
  quizQuestions.value = []

  loading.value.summary = true
  try {
    // 使用类型断言解决TypeScript类型推断问题
    console.log('🔄 开始AI摘要请求:', {
      timestamp: new Date().toISOString(),
      userId: userStore.userInfo?.id,
      contentLength: article.value.enContent.length,
      articleId: article.value.id
    })

    setAiState('loading', '正在生成AI摘要，请稍候…')
    console.time('AI摘要请求耗时')
    const res = (await aiApi.generateSummary(article.value.enContent, article.value.id)) as any
    console.timeEnd('AI摘要请求耗时')

    console.log('✅ AI摘要请求成功，结果:', {
      summaryLength: res.data?.length,
      fullResponse: res
    })

    // 同时更新侧边栏和文章末尾的摘要显示
    aiResult.value = res.data || res || '未获取到摘要'
    aiTitle.value = 'AI摘要'
    articleSummary.value = res.data || res || '未获取到摘要'
    setAiState('success', '摘要已生成')
  } catch (error) {
    // 使用类型断言解决TypeScript unknown类型问题
    const err = error as any
    console.error('❌ 生成摘要失败:',
      err.response?.data || err.message || error,
      {
        requestDetails: {
          contentPreview: article.value.enContent.substring(0, 50),
          errorCode: err.code,
          errorName: err.name,
          url: err.config?.url
        }
      }
    )
    ElMessage.error('生成摘要失败，请稍后重试')
    setAiState('error', '摘要生成失败，请稍后重试')
  } finally {
    loading.value.summary = false
    if (aiState.value.phase === 'loading') setAiState('idle', '准备就绪')
  }
}

// 监听文本选择变化
const handleTextSelection = () => {
  const selection = window.getSelection()?.toString().trim()
  hasTextSelection.value = !!(selection && selection.length > 0 && selection.length <= 300)
}

// AI句子解析 - 解析用户选中的句子
const aiParseSelection = async () => {
  // 检查是否有选中文本
  const selectedText = window.getSelection()?.toString().trim()
  if (!selectedText) {
    ElMessage.warning('请先选中要解析的句子')
    return
  }

  // 检查选中文本长度是否合理（应该是句子，不是整段文章）
  if (selectedText.length > 300) {
    ElMessage.warning('选中的文本过长，请选择单个句子进行解析')
    return
  }

  // 检查用户是否登录
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    ElMessage.warning('请先登录以使用AI句子解析功能')
    return
  }

  // 检查AI调用配额
  if (!userStore.checkAiQuota()) return

  loading.value.parse = true
  setAiState('loading', '正在解析句子，请稍候…')
  try {
    console.log('🔄 开始AI句子解析请求:', {
      timestamp: new Date().toISOString(),
      userId: userStore.userInfo?.id,
      sentence: selectedText,
      articleId: article.value.id
    })

    console.time('AI句子解析请求耗时')
    // 使用正确的句子解析接口
    const res = (await aiApi.parseSentence(selectedText, article.value.id)) as any
    console.timeEnd('AI句子解析请求耗时')

    console.log('✅ AI句子解析请求成功，结果:', {
      hasData: !!res.data,
      fullResponse: res
    })

    // 处理句子解析结果
    const parseData = res.data
    if (parseData) {
      // 格式化解析数据
      const formattedParseData = {
        ...parseData,
        originalSentence: parseData.originalSentence || selectedText
      }

      // 将解析结果插入到文章内容中
      insertParseResult(formattedParseData, selectedText)
      setAiState('success', '句子解析完成')

      // 滚动到插入的解析结果位置
      setTimeout(() => {
        const parseElements = document.querySelectorAll('.inline-parse-card')
        if (parseElements.length > 0) {
          const lastParseElement = parseElements[parseElements.length - 1]
          lastParseElement.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
        }
      }, 100)
    } else {
      ElMessage.warning('未获取到解析结果')
      setAiState('error', '未获取到解析结果')
    }
  } catch (error) {
    const err = error as any
    console.error('❌ AI句子解析失败:', err.response?.data || err.message || error)
    ElMessage.error('AI句子解析失败，请稍后重试')
    setAiState('error', '句子解析失败，请稍后重试')
  } finally {
    loading.value.parse = false
    if (aiState.value.phase === 'loading') setAiState('idle', '准备就绪')
  }
}

// 生成测验题 - 优先从数据库加载，没有则生成新的
const generateQuiz = async () => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    ElMessage.warning('请先登录以使用生成测验题功能')
    return
  }

  // 检查文章ID是否有效
  const articleId = Number(article.value.id)
  if (isNaN(articleId) || !articleId) {
    ElMessage.error('文章ID无效，无法生成测验题')
    return
  }

  loading.value.quiz = true
  setAiState('loading', '正在加载测验题，请稍候…')

  try {
    console.log('🔄 开始加载测验题:', {
      timestamp: new Date().toISOString(),
      userId: userStore.userInfo?.id,
      articleId: articleId
    })

    // 首先尝试从数据库加载已保存的测验题
    try {
      console.log('📚 尝试从数据库加载已保存的测验题...')
      const savedRes = await aiApi.getSavedQuiz(articleId)
      
      if (savedRes?.data && Array.isArray(savedRes.data) && savedRes.data.length > 0) {
        console.log('✅ 成功从数据库加载测验题:', savedRes.data.length, '道题')
        
        // 转换为交互式测验题格式
        quizQuestions.value = savedRes.data.map((q: any, index: number) => ({
          id: q.id || String(index + 1),
          question: q.question || '问题内容为空',
          options: q.options && Array.isArray(q.options) && q.options.length > 0
            ? q.options.map((opt: string) => opt.replace(/^[A-D]\.\s*/, '')) // 移除选项前缀
            : ['选项A', '选项B', '选项C', '选项D'],
          answer: q.answer || q.correctAnswerText || q.correctAnswer || 'A',
          correctAnswer: q.correctAnswerText || q.correctAnswer,
          correctAnswerText: q.correctAnswerText || q.correctAnswer,
          explanation: q.explanation || '暂无解析',
          questionType: q.questionType || 'comprehension',
          difficulty: q.difficulty || 'medium'
        }))

        // 切换到测验模式
        isQuizMode.value = true
        aiResult.value = ''
        aiTitle.value = ''
        setAiState('success', '已加载保存的测验题')
        ElMessage.success('已加载保存的测验题')
        return
      }
    } catch (savedError) {
      console.log('📚 数据库中没有保存的测验题，将生成新的:', savedError)
    }

    // 如果数据库中没有，则生成新的测验题
    console.log('🔄 开始生成新的测验题...')
    setAiState('loading', '正在生成新测验题，请稍候…')

    // 检查AI调用配额
    if (!userStore.checkAiQuota()) return

    console.time('生成测验题请求耗时')
    // 使用Function Calling接口生成新的测验题
    const res = (await aiApi.assistantGenerateQuiz({
      articleContent: article.value.enContent,
      articleId: articleId
    })) as any
    console.timeEnd('生成测验题请求耗时')

    console.log('✅ 生成测验题请求成功，结果:', {
      questionCount: res.data?.length || 0,
      fullResponse: res
    })

    // 处理测验题结果
    const quizData = res.data
    if (quizData && Array.isArray(quizData) && quizData.length > 0) {
      // 过滤掉所有字段都为null的无效问题
      const validQuestions = quizData.filter((q: any) =>
        q.question || (q.options && q.options.length > 0) || q.answer || q.correctAnswer || q.explanation
      )

      if (validQuestions.length > 0) {
        // 转换为交互式测验题格式
        quizQuestions.value = validQuestions.map((q: any, index: number) => ({
          id: q.id || String(index + 1),
          question: q.question || '问题内容为空',
          options: q.options && Array.isArray(q.options) && q.options.length > 0
            ? q.options.map((opt: string) => opt.replace(/^[A-D]\.\s*/, '')) // 移除选项前缀
            : ['选项A', '选项B', '选项C', '选项D'],
          answer: q.answer || q.correctAnswerText || q.correctAnswer || 'A',
          correctAnswer: q.correctAnswerText || q.correctAnswer,
          correctAnswerText: q.correctAnswerText || q.correctAnswer,
          explanation: q.explanation || '暂无解析',
          questionType: q.questionType || 'comprehension',
          difficulty: q.difficulty || 'medium'
        }))

        // 切换到测验模式
        isQuizMode.value = true
        aiResult.value = ''
        aiTitle.value = ''
        setAiState('success', '测验题已生成')
        ElMessage.success('测验题已生成')
      } else {
        // 所有问题都无效，尝试回退到DeepSeek接口
        console.log('Function Calling接口返回的测验题全部无效，尝试DeepSeek接口')
        await tryFallbackQuiz()
      }
    } else {
      // 没有返回测验题，尝试回退到DeepSeek接口
      console.log('Function Calling接口无结果，尝试DeepSeek接口')
      await tryFallbackQuiz()
    }
  } catch (error) {
    const err = error as any
    console.error('❌ 生成测验题失败:', err.response?.data || err.message || error)
    ElMessage.error('生成测验题失败，请稍后重试')
    setAiState('error', '测验题生成失败，请稍后重试')
  } finally {
    loading.value.quiz = false
    if (aiState.value.phase === 'loading') setAiState('idle', '准备就绪')
  }
}

// 尝试使用DeepSeek接口生成测验题
const tryFallbackQuiz = async () => {
  try {
    const articleId = Number(article.value.id)
    const fallbackRes = (await api.post('/api/ai/quiz', {
      articleId: articleId,
      text: article.value.enContent,
      questionCount: 5,
      questionType: 'comprehension',
      difficulty: 'medium'
    })) as any

    const fallbackData = fallbackRes.data
    if (fallbackData && Array.isArray(fallbackData) && fallbackData.length > 0) {
      // 转换为交互式测验题格式
      quizQuestions.value = fallbackData.map((q: any, index: number) => ({
        id: q.id || String(index + 1),
        question: q.question || '问题内容为空',
        options: q.options && Array.isArray(q.options) && q.options.length > 0
          ? q.options.map((opt: string) => opt.replace(/^[A-D]\.\s*/, '')) // 移除选项前缀
          : ['选项A', '选项B', '选项C', '选项D'],
        answer: q.answer || q.correctAnswerText || q.correctAnswer || 'A',
        correctAnswer: q.correctAnswerText || q.correctAnswer,
        correctAnswerText: q.correctAnswerText || q.correctAnswer,
        explanation: q.explanation || '暂无解析',
        questionType: q.questionType || 'comprehension',
        difficulty: q.difficulty || 'medium'
      }))

      // 切换到测验模式
      isQuizMode.value = true
      aiResult.value = ''
      aiTitle.value = ''
      setAiState('success', '测验题已生成')
      ElMessage.success('测验题已生成')
    } else {
      aiResult.value = '测验题生成暂时不可用，请稍后再试'
      ElMessage.warning('测验题生成服务暂时不可用，请稍后再试')
      setAiState('error', '测验题生成暂时不可用')
    }
  } catch (fallbackError) {
    console.error('❌ 回退测验题生成也失败:', fallbackError)
    aiResult.value = '测验题生成暂时不可用，请稍后再试'
    ElMessage.warning('测验题生成服务暂时不可用，请稍后再试')
    setAiState('error', '测验题生成暂时不可用')
  }
}

// 测验完成回调
const onQuizComplete = (score: number, time: number) => {
  console.log('测验完成:', { score, time })
  ElMessage.success(`测验完成！得分：${score}分，用时：${Math.floor(time / 1000)}秒`)

  // 可以在这里添加更多逻辑，比如保存成绩到后端
  // 或者显示更详细的成绩分析
}

// 用户等级相关方法
const getUserLevelClass = () => {
  if (isPremiumUser.value) return 'premium'
  if (isBasicMember.value) return 'basic'
  return 'free'
}

const getUserLevelText = () => {
  if (isPremiumUser.value) {
    return userStore.userTier === 'enterprise' ? '企业会员' : '专业会员'
  }
  if (isBasicMember.value) return '基础会员'
  return '免费用户'
}

const getAIStatusClass = () => {
  if (isPremiumUser.value) return 'premium'
  if (isBasicMember.value) return 'basic'
  return 'free'
}

const getAIStatusText = () => {
  if (isPremiumUser.value) return '全部功能已解锁'
  if (isBasicMember.value) return '部分功能可用'
  return '基础功能可用'
}

// 文章操作相关方法

const getReadingTime = () => {
  const wordCount = getWordCount()
  return Math.ceil(wordCount / 200) // 假设每分钟阅读200词
}

const getWordCount = () => {
  return article.value.enContent?.split(/\s+/).length || 0
}

// 问AI助手
const askAI = () => {
  aiDialogVisible.value = true
  aiQuestion.value = ''
  aiAnswer.value = ''
}

const submitAIQuestion = async () => {
  if (!aiQuestion.value.trim()) return

  // 检查用户是否登录
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    ElMessage.warning('请先登录以使用AI助手功能')
    return
  }

  // 检查AI调用配额
  if (!userStore.checkAiQuota()) return

  aiLoading.value = true
  setAiState('loading', 'AI助手正在思考，请稍候…')
  try {
    // 使用类型断言解决TypeScript类型推断问题
    console.log('🔄 开始AI助手对话请求:', {
      timestamp: new Date().toISOString(),
      userId: userStore.userInfo?.id,
      question: aiQuestion.value,
      contentLength: article.value.enContent.length,
      articleId: article.value.id
    })

    console.time('AI助手对话请求耗时')
    // 传递文章上下文给AI助手
    const res = (await aiApi.chat(aiQuestion.value, Number(userStore.userInfo.id), article.value.enContent)) as any
    console.timeEnd('AI助手对话请求耗时')

    console.log('✅ AI助手对话请求成功，结果:', {
      success: res.success,
      code: res.code,
      message: res.message,
      hasData: !!res.data,
      answerLength: res.data?.answer?.length || 0,
      fullResponse: res
    })

    // 检查响应是否成功
    if (!res.success || !res.data) {
      console.error('AI助手响应失败:', res.message || '未知错误')
      aiAnswer.value = res.message || 'AI助手暂时无法回答，请稍后再试'
      setAiState('error', 'AI助手响应失败')
      return
    }

    // 处理AI助手的响应
    const aiResponse = res.data.answer
    if (!aiResponse || aiResponse.trim() === '') {
      console.warn('AI返回空响应')
      aiAnswer.value = '抱歉，我暂时无法回答这个问题。请尝试换个方式提问，或者稍后再试。'
      setAiState('error', 'AI返回空响应')
      return
    }

    // 设置AI回答
    aiAnswer.value = aiResponse
    
    // 如果有后续问题建议，可以在这里处理
    if (res.data.followUpQuestion) {
      console.log('后续问题建议:', res.data.followUpQuestion)
    }
    setAiState('success', 'AI回答已生成')
  } catch (error) {
    // 使用类型断言解决TypeScript unknown类型问题
    const err = error as any
    console.error('❌ AI助手对话失败:',
      err.response?.data || err.message || error,
      {
        requestDetails: {
          question: aiQuestion.value,
          contentPreview: article.value.enContent.substring(0, 50),
          errorCode: err.code,
          errorName: err.name,
          url: err.config?.url,
          status: err.response?.status
        }
      }
    )
    
    // 根据错误类型提供不同的错误信息
    let errorMessage = 'AI助手暂时无法回答，请稍后重试'
    if (err.response?.status === 401) {
      errorMessage = '请先登录以使用AI助手功能'
    } else if (err.response?.status === 403) {
      errorMessage = '您的AI功能权限不足，请升级订阅'
    } else if (err.response?.status >= 500) {
      errorMessage = 'AI服务暂时不可用，请稍后再试'
    } else if (err.code === 'NETWORK_ERROR' || err.message?.includes('Network Error')) {
      errorMessage = '网络连接失败，请检查网络后重试'
    }
    
    aiAnswer.value = errorMessage
    ElMessage.error(errorMessage)
    setAiState('error', errorMessage)
  } finally {
    aiLoading.value = false
    if (aiState.value.phase === 'loading') setAiState('idle', '准备就绪')
  }
}

// 处理高亮段落
const handleHighlightParagraph = (index: number | null) => {
  highlightedParagraphIndex.value = index
}

// 处理文章朗读
const handleSpeakArticle = () => {
  if (ttsControlRef.value) {
    ttsControlRef.value.handleSpeakArticle()
  }
}

// 处理停止朗读
const handleStopSpeaking = () => {
  if (ttsControlRef.value) {
    ttsControlRef.value.handleStopSpeaking()
  }
}

// 检查是否正在朗读
const isTTSSpeaking = () => {
  return tts.isBusy() || false
}

// 单词点击事件
// 处理单词单击事件（查词功能）
const onWordClick = (event: MouseEvent) => {
  const selection = window.getSelection()?.toString().trim()
  if (selection && selection.length <= 20 && /^[a-zA-Z]+$/.test(selection)) {
    selectedWord.value = selection
    lookupWord(selection)
  }
}

// 处理单词双击事件（朗读功能）
const onWordDoubleClick = (event: MouseEvent) => {
  event.preventDefault()
  if (ttsControlRef.value) {
    ttsControlRef.value.handleWordClick(event)
  }
}

// 查词
const lookupWord = async (word: string) => {
  try {
    // 检查用户是否登录
    if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
      ElMessage.warning('请先登录以使用查词功能')
      return
    }

    // 检查是否有选中的单词
    if (!word || word.trim() === '') {
      ElMessage.warning('请先选择要查询的单词')
      return
    }

    // 查词操作不计入AI调用次数限制

    // 使用用户实际ID和适当的上下文，确保userId是number类型
    const userId = Number(userStore.userInfo.id)
    const context = article.value.enContent.slice(0, 200)

    setAiState('loading', '正在查词，请稍候…')
    const res = await vocabularyApi.lookupWord({
      word,
      context: context || '',
      userId,
      articleId: article.value.id
    })
    wordDetail.value = {
      word,
      meaning: res.data?.meaning || '未找到释义',
      example: res.data?.example || '无例句',
      context: res.data?.context || context,
      phonetic: res.data?.phonetic || ''
    }
    wordDialogVisible.value = true
    setAiState('success', '查词完成')
  } catch (error) {
    console.error('查词失败:', error)
    ElMessage.error('查词失败，请稍后重试')
    setAiState('error', '查词失败，请稍后重试')
  }
}

// 添加到生词本（单词详情弹窗）
const addWordToVocabulary = async () => {
  if (!wordDetail.value || !userStore.isLoggedIn || !userStore.userInfo?.id) return

  try {
    // 使用用户实际ID
    const userId = Number(userStore.userInfo.id)

    await vocabularyApi.addWord({
      word: wordDetail.value.word,
      translation: wordDetail.value.meaning || '',
      context: wordDetail.value.context || '',
      articleId: article.value.id,
      userId: userId
    })
    ElMessage.success('已添加到生词本')
    wordDialogVisible.value = false
    vocabCount.value++

    // 重新加载词汇统计以确保计数准确
    loadVocabCount()
  } catch (error) {
    console.error('添加生词失败:', error)
    ElMessage.error('添加失败，请稍后重试')
  }
}

// 添加到生词本（AI结果面板）
const addToVocabulary = async () => {
  if (!selectedWord.value || !userStore.isLoggedIn || !userStore.userInfo?.id) return

  try {
    // 使用用户实际ID并确保是number类型
    const userId = Number(userStore.userInfo.id)

    // 先查找单词释义
    const res = await vocabularyApi.lookupWord({
      word: selectedWord.value,
      context: article.value.enContent.slice(0, 200) || '',
      userId,
      articleId: article.value.id
    })

    await vocabularyApi.addWord({
      word: selectedWord.value,
      translation: res.data?.meaning || '无释义',
      context: article.value.enContent.slice(0, 200) || '',
      articleId: article.value.id,
      userId: userId
    })
    ElMessage.success('已添加到生词本')
    vocabCount.value++

    // 重新加载词汇统计以确保计数准确
    loadVocabCount()
  } catch (error) {
    console.error('添加生词失败:', error)
    ElMessage.error('添加失败，请稍后重试')
  }
}

// 跳转到生词本
const goToVocabulary = () => {
  router.push('/vocabulary')
}

// 获取难度类型
const getDifficultyType = (difficulty: string) => {
  const map: Record<string, string> = {
    '初级': 'success',
    '中级': 'warning',
    '高级': 'danger'
  }
  return map[difficulty] || 'info'
}

// 切换翻译显示
const toggleTranslation = () => {
  showTranslation.value = !showTranslation.value
}

// 首次使用引导相关方法
const remindLater = () => {
  // 下次打开页面时再次显示引导
  showFirstUseGuide.value = false
}

const neverRemind = () => {
  // 记住用户选择，不再显示引导
  localStorage.setItem('hasSeenFirstUseGuide', 'true')
  showFirstUseGuide.value = false
}

// 重置引导显示状态（用于调试或用户需要再次查看引导）
const resetFirstUseGuide = () => {
  localStorage.removeItem('hasSeenFirstUseGuide')
  showFirstUseGuide.value = true
}

// 添加选择监听器
// 修改onMounted钩子，确保正确等待用户状态加载
onMounted(async () => {
  // 检查是否应该显示升级提示
  const dismissed = localStorage.getItem('dismissUpgradeTip')
  showUpgradeTip.value = dismissed !== 'true'

  // 检查是否应该显示首次使用引导
  // 添加URL参数支持，允许强制显示引导 (例如: ?showGuide=true)
  const urlParams = new URLSearchParams(window.location.search)
  const forceShowGuide = urlParams.get('showGuide') === 'true'
  const hasSeenGuide = localStorage.getItem('hasSeenFirstUseGuide')

  // 开发环境下或URL参数指定时，强制显示引导
  if (forceShowGuide || import.meta.env.DEV || !hasSeenGuide) {
    // 延迟显示引导，让页面加载完成
    setTimeout(() => {
      showFirstUseGuide.value = true
    }, 1000)
  }

  // 先确保用户状态已加载
  if (userStore.isLoggedIn) {
    try {
      await userStore.fetchUserProfile()
    } catch (error) {
      console.log('Failed to fetch user profile')
    }
  }

  // 等待文章加载完成，确保订阅信息也加载完毕
  await loadArticle()

  document.addEventListener('selectionchange', handleTextSelection)

  // 添加页面可见性变化监听
  document.addEventListener('visibilitychange', () => {
    isPageVisible.value = !document.hidden
  })

  // 加载用户生词本数量
  if (userStore.isLoggedIn) {
    await loadVocabCount()
  }

  // 启动阅读计时器
  startTime.value = Date.now()
  timerInterval.value = window.setInterval(() => {
    // 只有当页面可见时才更新计时器
    if (startTime.value && isPageVisible.value) {
      readTimeSec.value = Math.floor((Date.now() - startTime.value) / 1000)
    }
  }, 1000) as unknown as number

  // 确保页面加载时直接显示顶部
  window.scrollTo(0, 0);
  // 同时确保主内容区也滚动到顶部
  const mainContent = document.querySelector('.main-content');
  if (mainContent) {
    (mainContent as HTMLElement).scrollTop = 0;
  }

  // 初始化TTS控制组件
  if (ttsControlRef.value) {
    setTimeout(() => {
      ttsControlRef.value?.loadVoices()
    }, 100);
  }
})

watch(
  () => [userStore.hasAIFeatures, userStore.isLoggedIn],
  () => {
    // 当用户状态变化时，重新加载订阅信息以确保显示准确
    if (userStore.isLoggedIn) {
      loadSubscriptionInfo()
    }
  },
  { immediate: true }
)
onUnmounted(async () => {
  document.removeEventListener('selectionchange', handleTextSelection)
  document.removeEventListener('visibilitychange', () => {})

  // 停止计时器并记录阅读时长
  if (timerInterval.value) {
    clearInterval(timerInterval.value)
    timerInterval.value = null
  }

  // 只有当阅读时间超过2秒且用户已登录时才记录
  if (readTimeSec.value >= 2 &&
      userStore.isLoggedIn &&
      userStore.userInfo?.id &&
      article.value &&
      article.value.id &&
      Number.isInteger(Number(article.value.id)) &&
      Number(article.value.id) > 0) {
    try {
      const userId = Number(userStore.userInfo.id)
      const articleId = Number(article.value.id)

      // 确保userId是有效的数字
      if (Number.isInteger(userId) && userId > 0) {
        await learningApi.recordReadingTime(userId, articleId, readTimeSec.value)
        console.log(`✅ 阅读时长记录成功: 用户ID=${userId}, 文章ID=${articleId}, 时长=${readTimeSec.value}秒`)
      } else {
        console.warn('⚠️ 无效的用户ID，无法记录阅读时长')
      }
    } catch (error) {
      console.error('❌ 记录阅读时长失败:', error)
      // 失败时不显示错误消息，避免影响用户体验
    }
  }
})
</script>

<style scoped>
.reader-container {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
  position: relative;
}

/* 侧边栏样式 */
.sidebar {
  width: 280px;
  background-color: #fff;
  border-right: 1px solid #ebeef5;
  padding: 20px;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  justify-content: flex-start; /* 顶部对齐，避免中间大空白 */
  transition: width 0.3s ease, padding 0.3s ease;
  position: relative;
  z-index: 20;
  height: 100%;
}

/* 侧边栏分区样式 */
.user-tip-section,
.ai-tools-section,
.upgrade-section {
  width: 100%;
}

.upgrade-section {
  margin-top: auto;
}

/* 侧边栏折叠状态 */
.sidebar.sidebar-collapsed {
  width: 0;
  padding: 0;
  overflow: hidden;
}

.sidebar.sidebar-collapsed > * {
  display: none;
}

/* 侧边栏切换按钮 */
.sidebar-toggle {
  position: absolute;
  left: 280px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 100;
  transition: left 0.3s ease;
}

.sidebar-toggle .el-button {
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-left: none;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  width: 30px;
  height: 60px;
  border-radius: 0 4px 4px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: none !important; /* 修复悬停时向下跑 */
}

.sidebar-toggle .el-button:hover {
  transform: none !important; /* 确保悬停时不会位移 */
  background-color: #f0f0f0;
}

/* 侧边栏折叠时切换按钮位置 */
.sidebar.sidebar-collapsed ~ .sidebar-toggle {
  left: 0; /* 完全贴边 */
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  height: 100vh;
  overflow-y: auto;
  padding: 20px;
  background-color: #fff;
  transition: all 0.3s ease;
}

/* 主内容区展开状态 */
.main-content.main-content-expanded {
  margin-left: 0;
}

/* 侧边栏收起时的英文内容样式 - 沉浸式阅读体验 */
/* 为免费用户布局（上下排列）的英文内容设置样式 */
.sidebar.sidebar-collapsed ~ .main-content .english-content {
  max-width: 1500px; /* 扩大内容宽度 */
}

.sidebar.sidebar-collapsed ~ .main-content .english-content .paragraph {
  font-size: 18px;  /* 增大字体大小 */
  line-height: 1.8; /* 调整行高，提升阅读舒适度 */
}

/* 为付费用户布局（行间翻译）的英文内容设置样式 */
.sidebar.sidebar-collapsed ~ .main-content .free-bilingual-layout {
  max-width: 1500px; /* 扩大整个布局宽度 */
  margin: 0 auto;
  transition: max-width 0.3s ease;
}

.sidebar.sidebar-collapsed ~ .main-content .free-bilingual-layout .english-sentence {
  font-size: 18px;  /* 增大字体大小 */
  line-height: 1.8; /* 调整行高，提升阅读舒适度 */
  transition: font-size 0.3s ease, line-height 0.3s ease;
}

/* 用户成长提示样式 */
.user-tip {
  padding: 15px 20px;
  border-radius: 8px;
  font-weight: 500;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.newbie-tip {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1565c0;
  border-left: 4px solid #1976d2;
}

.intermediate-tip {
  background: linear-gradient(135deg, #fff8e1 0%, #ffecb3 100%);
  color: #f57c00;
  border-left: 4px solid #ff9800;
}

.advanced-tip {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #2e7d32;
  border-left: 4px solid #4caf50;
}

/* 会员等级递进尊贵颜色 */
.free-tip {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1565c0;
  border-left: 4px solid #1976d2;
}

.basic-tip {
  background: linear-gradient(135deg, #fff3e0 0%, #ffcc80 100%);
  color: #e65100;
  border-left: 4px solid #f57c00;
}

.pro-tip {
  background: linear-gradient(135deg, #f3e5f5 0%, #ce93d8 100%);
  color: #6a1b9a;
  border-left: 4px solid #8e24aa;
  box-shadow: 0 4px 12px rgba(142, 36, 170, 0.15);
}

.enterprise-tip {
  background: linear-gradient(135deg, #fff8e1 0%, #ffd700 100%);
  color: #ff6f00;
  border-left: 4px solid #ff9800;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
}

/* 文章标题样式 */
.article-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.article-header h1 {
  font-size: 2.5em;
  margin: 0 0 15px 0;
  line-height: 1.2;
}

.article-meta {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.read-count {
  font-size: 14px;
  opacity: 0.8;
}

/* AI工具栏样式 */
.ai-toolbar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

/* 全宽按钮样式 */
.full-width-button {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 会员升级提示样式 */
.upgrade-tip {
  width: 100%;
  display: flex;
  justify-content: center;
}

.upgrade-tip .el-button {
  background-color: #f0f7ff;
  border: 1px solid #d4e6ff;
  color: #409eff;
  font-size: 12px;
  padding: 4px 16px;
  transition: all 0.3s ease;
  width: 100%;
}

/* 用户状态卡片样式 */
.user-status-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.user-level {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  opacity: 0.9;
}

.user-level.premium {
  color: #ffd700;
}

.user-level.basic {
  color: #c0c0c0;
}

.user-level.free {
  color: #e8f5e8;
}

.user-stats {
  text-align: right;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 18px;
  font-weight: bold;
  line-height: 1;
}

.stat-label {
  font-size: 11px;
  opacity: 0.8;
  margin-top: 2px;
}

/* AI工具栏样式 */
.ai-tools-section {
  margin-bottom: 20px;
}

/* 常驻AI状态提示区 */
.ai-live-status {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #ebeef5;
  background: #fafafa;
  padding: 8px 10px;
  border-radius: 8px;
  margin-bottom: 12px; /* 填补用户状态和工具栏之间的空白 */
}
.ai-live-status .status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #d9d9d9;
}
.ai-live-status.phase-loading .status-dot { background: #409eff; animation: pulse 1.2s infinite ease-in-out; }
.ai-live-status.phase-success .status-dot { background: #67c23a; }
.ai-live-status.phase-error .status-dot { background: #f56c6c; }
.ai-live-status.phase-idle .status-dot { background: #d9d9d9; }

@keyframes pulse {
  0% { transform: scale(1); opacity: .9; }
  50% { transform: scale(1.25); opacity: .6; }
  100% { transform: scale(1); opacity: .9; }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.ai-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.ai-status.premium {
  background: #e6f7ff;
  color: #1890ff;
}

.ai-status.basic {
  background: #fff7e6;
  color: #fa8c16;
}

.ai-status.free {
  background: #f6ffed;
  color: #52c41a;
}

/* 功能按钮区域 */
.core-functions,
.advanced-functions {
  display: flex;
  flex-direction: column; /* 单列展示 */
  gap: 12px;
  margin-bottom: 16px;
}

/* 统一所有功能区域的按钮样式 - 无左margin */
.core-functions .function-button,
.advanced-functions .function-button {
  margin-left: 0;
}

.function-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
  width: 100%; /* 统一按钮宽度 */
}

.function-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.function-button.primary-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.function-button.success-button {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border: none;
  color: white;
}

.function-button.warning-button {
  background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
  border: none;
  color: white;
}

.function-button.info-button {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border: none;
  color: white;
}

.function-button.danger-button {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  border: none;
  color: white;
}

/* 升级卡片样式 */
.upgrade-card {
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  margin-top: 20px;
}

.upgrade-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upgrade-icon {
  color: #ff6b6b;
}

.upgrade-text {
  flex: 1;
}

.upgrade-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.upgrade-desc {
  font-size: 12px;
  color: #666;
}

.upgrade-button {
  background: #ff6b6b;
  border: none;
  border-radius: 20px;
  font-weight: 500;
}

/* 文章头部样式 */
.article-header {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.article-title-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.article-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.3;
  margin: 0;
  flex: 1;
  margin-right: 20px;
}

.article-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.meta-tags {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.difficulty-tag,
.category-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  border-radius: 20px;
  padding: 8px 16px;
}

.meta-stats {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 14px;
}

/* 侧边栏AI结果展示区样式 */
.sidebar-ai-result {
  width: 100%;
  margin-top: 15px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.sidebar-result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 12px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.sidebar-result-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.add-vocab-button {
  padding: 4px 10px !important;
  font-size: 12px !important;
  min-width: auto !important;
}

.formatted-ai-result {
  padding: 20px;
  max-height: 400px;
  overflow-y: auto;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
}

.result-paragraph {
  margin: 0 0 16px 0;
  padding: 0;
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  text-align: left;
  word-wrap: break-word;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.result-paragraph:last-child {
  margin-bottom: 0;
}

/* 响应式设计：在小屏幕上调整字体大小和行高 */
@media (max-width: 768px) {
  .result-paragraph {
    font-size: 13px;
    line-height: 1.6;
  }
}

.upgrade-tip .el-button:hover {
  background-color: #e6f0ff;
  border-color: #409eff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

/* 双语阅读区样式 */
.bilingual-content {
  background: white;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  min-height: calc(100vh - 200px);
}

.english-section, .chinese-section {
  margin-bottom: 20px;
}

/* 免费用户双栏并排布局 */
.premium-bilingual-layout {
  /* 免费用户：英文原文在上，中文翻译在下 */
  display: block;
}

.premium-bilingual-layout .english-content {
  width: 100%;
  margin-bottom: 20px;
}

/* 翻译分界线 */
.translation-divider {
  height: 1px;
  background: #e0e0e0;
  margin: 20px 0 30px 0;
  width: 100%;
}

.premium-bilingual-layout .chinese-section {
  width: 100%;
  margin-bottom: 20px;
}

/* 付费用户行间翻译布局 */
.free-bilingual-layout {
  max-width: 768px;
  margin: 0 auto;
}

.free-bilingual-layout .sentence-group {
  padding: 5px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.free-bilingual-layout .sentence-group:hover {
  background-color: #f8f9fa;
}

.free-bilingual-layout .english-sentence {
  font-weight: 500;
  margin-bottom: 8px;
}

.free-bilingual-layout .chinese-sentence {
  color: #606266;
  margin-bottom: 0;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .premium-bilingual-layout {
    flex-direction: column;
  }

  .premium-bilingual-layout .english-content,
  .premium-bilingual-layout .chinese-section {
    max-width: 100%;
  }
}

.english-section h3, .chinese-section h3 {
  color: #303133;
  margin-bottom: 15px;
  font-size: 1.2em;
}

/* 英文板块整体 */
.english-section {
  margin: 24px 0; /* 与上下模块保持间距 */
}

/* 英文内容容器：控制行宽、居中、基础字体 */
.english-content {
  max-width: 768px; /* 限制行宽（建议每行 50-70 个单词，减少阅读疲劳） */
  margin: 0 auto;    /* 容器水平居中 */
  line-height: 1.7;  /* 行高设为 1.6-1.8 倍，提升纵向呼吸感 */
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; /* 选用系统无衬线字体，跨设备一致性好 */
  color: #333;       /* 深灰色文字，比纯黑更柔和 */
  padding: 0 16px;   /* 小屏幕下保留左右内边距，避免文字贴边 */
  transition: max-width 0.3s ease; /* 添加过渡动画 */
}

/* 英文段落：增加段落间距，优化换行，添加缩进 */
.english-content .paragraph {
  margin-bottom: 1.2em; /* 增加段落底部间距，更清晰地区分不同段落 */
  text-align: left;     /* 英文默认左对齐，符合阅读习惯 */
  word-wrap: break-word;/* 长单词自动换行，防止溢出 */
  text-indent: 2em;     /* 添加首行缩进，提升段落可读性 */
  font-size: 16px;      /* 设置合适的字体大小 */
  line-height: 1.7;     /* 合适的行高，提高阅读舒适度 */
  transition: font-size 0.3s ease, line-height 0.3s ease; /* 添加过渡动画 */
}

/* 中文内容样式 */
.chinese-content {
  line-height: 1.8;
  font-size: 16px;
}

.chinese-content .paragraph {
  margin-bottom: 1.2em; /* 增加段落底部间距，更清晰地区分不同段落 */
  text-align: left;     /* 中文默认左对齐，符合阅读习惯 */
  text-indent: 2em;     /* 添加首行缩进，提升段落可读性 */
  line-height: 1.8;     /* 合适的行高，提高阅读舒适度 */
  font-size: 16px;      /* 设置合适的字体大小 */
  color: #606266;
  font-family: 'Microsoft YaHei', sans-serif;
}
/* 段落通用样式 */
.paragraph {
  cursor: pointer;
  transition: background-color 0.3s;
  padding: 5px;
  border-radius: 4px;
}

.paragraph:hover {
  background-color: #f5f7fa;
}

/* 高亮当前朗读的段落 */
.paragraph.highlighted {
  background-color: #e6f7ff;
  border-left: 3px solid #409eff;
  padding: 8px 12px;
  margin: 8px 0;
  border-radius: 4px;
  transition: background-color 0.2s;
}

/* 翻译切换按钮 */
.translation-toggle {
  text-align: center;
  margin-top: 20px;
}

/* AI结果面板样式 */
.ai-result-panel {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.result-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Arial', sans-serif;
  line-height: 1.6;
}

/* 浮动生词本按钮 */
.floating-vocab {
  position: fixed;
  right: 30px;
  bottom: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  z-index: 1000;
}

.vocab-text {
  font-size: 12px;
  color: #409eff;
  margin-top: 5px;
}

/* 单词详情弹窗样式 */
.word-detail {
  text-align: center;
}

.word-detail .word {
  font-size: 1.5em;
  font-weight: bold;
  margin-bottom: 10px;
}

.word-detail .phonetic {
  color: #909399;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.word-detail .pronunciation-button {
  font-size: 16px;
  width: 32px;
  height: 32px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.word-detail .pronunciation-button:hover {
  background-color: #e6f7ff;
  color: #409eff;
  transform: scale(1.1);
}

.word-detail .meaning {
  font-size: 1.1em;
  margin-bottom: 10px;
  color: #303133;
  line-height: 1.8;
  white-space: pre-line;
}

.word-detail .meaning br {
  margin-bottom: 5px;
}

.word-detail .example,
.word-detail .context {
  margin-bottom: 10px;
  color: #606266;
  line-height: 1.6;
}

/* AI聊天样式 */
.ai-chat {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.chat-actions {
  text-align: right;
}

.ai-answer {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 15px;
}

/* 首次使用引导样式 */
.first-use-guide {
  text-align: center;
}

.guide-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.guide-icon {
  margin-bottom: 20px;
}

.guide-text h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 18px;
}

.guide-text p {
  margin: 8px 0;
  color: #606266;
  font-size: 15px;
  text-align: left;
}

.guide-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .reader-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid #ebeef5;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }

  .sidebar-toggle {
    display: none;
  }

  .main-content {
    height: calc(100vh - 200px); /* 减去头部和侧边栏高度 */
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 10px;
  }

  .article-header h1 {
    font-size: 1.8em;
  }

  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .floating-vocab {
    right: 20px;
    bottom: 20px;
  }

  .bilingual-content {
    padding: 20px 15px;
  }
}

/* 微交互和动画 */
.reader-container {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.main-content {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-toggle {
  transition: all 0.3s ease;
}

.sidebar-toggle:hover {
  /* 移除缩放效果，避免错位 */
  background-color: rgba(64, 169, 255, 0.1);
}

/* 卡片悬停效果 */
.user-status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.upgrade-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 107, 0.3);
}

.article-header:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

/* 按钮加载状态 */
.function-button.is-loading {
  opacity: 0.7;
  cursor: not-allowed;
}

.function-button.is-loading:hover {
  transform: none;
  box-shadow: none;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .sidebar {
    width: 320px;
  }

  .main-content {
    margin-left: 320px;
  }
}

@media (max-width: 768px) {
  .reader-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    height: auto;
    position: relative;
    transform: none;
    transition: none;
    order: 2;
  }

  .sidebar-collapsed {
    transform: none;
  }

  .main-content {
    width: 100%;
    margin-left: 0;
    order: 1;
  }

  .main-content-expanded {
    margin-left: 0;
  }

  .sidebar-toggle {
    display: none;
  }

  .core-functions {
    flex-direction: row;
    gap: 8px;
  }

  .advanced-functions {
    grid-template-columns: 1fr 1fr 1fr;
    gap: 6px;
  }

  .function-button {
    padding: 8px 4px;
    font-size: 12px;
  }

  .function-button span {
    font-size: 11px;
  }

  .article-title-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .article-title {
    font-size: 24px;
    margin-right: 0;
  }

  .article-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .meta-stats {
    gap: 16px;
  }

  .user-status-card {
    padding: 12px;
  }

  .user-name {
    font-size: 14px;
  }

  .stat-number {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .article-header {
    padding: 16px;
    margin-bottom: 16px;
  }

  .article-title {
    font-size: 20px;
  }

  .core-functions {
    flex-direction: column;
  }

  .advanced-functions {
    grid-template-columns: 1fr 1fr;
  }

  .function-button {
    padding: 10px 8px;
  }

  .meta-stats {
    flex-direction: column;
    gap: 8px;
  }

  .meta-stats .stat-item {
    font-size: 12px;
  }
}

/* 文章标题和操作按钮样式 */
.article-title-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.article-actions {
  display: flex;
  gap: 8px;
}

.article-actions .el-button {
  border-radius: 6px;
  transition: all 0.3s ease;
  font-size: 14px;
  padding: 4px 12px;
}

.article-actions .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* AI句子解析弹窗样式 */
.inline-parse-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  margin: 16px 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
}

.inline-parse-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.parse-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-bottom: 1px solid #e4e7ed;
}

.parse-card-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.close-button {
  color: #909399;
  transition: color 0.3s ease;
}

.close-button:hover {
  color: #f56c6c;
}

.parse-basic-info {
  padding: 20px;
}

.original-sentence {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.6;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.core-meaning {
  font-size: 14px;
  color: #606266;
  margin-bottom: 16px;
  line-height: 1.6;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #67c23a;
}

.key-vocab {
  margin-top: 16px;
}

.key-vocab p {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.vocab-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.vocab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 20px;
  font-size: 13px;
  transition: all 0.3s ease;
}

.vocab-item:hover {
  background: #f0f9ff;
  border-color: #409eff;
  transform: translateY(-1px);
}

.vocab-word {
  font-weight: 600;
  color: #303133;
}

.vocab-meaning {
  color: #606266;
  font-style: italic;
}

.vocab-type {
  color: #909399;
  font-size: 11px;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 10px;
}

.premium-content {
  padding: 20px;
  background: #fafbfc;
  border-top: 1px solid #e4e7ed;
}

.sentence-structure,
.grammar-analysis,
.grammar-points,
.learning-tip {
  margin-bottom: 20px;
}

.sentence-structure p,
.grammar-analysis p,
.grammar-points p,
.learning-tip p {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.structure-detail,
.grammar-detail {
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.structure-item {
  margin-bottom: 8px;
  font-size: 13px;
  line-height: 1.5;
}

.structure-item strong {
  color: #409eff;
  margin-right: 8px;
}

.grammar-detail p {
  margin-bottom: 6px;
  font-size: 13px;
  color: #606266;
}

.grammar-points ul {
  margin: 0;
  padding-left: 20px;
}

.grammar-points li {
  margin-bottom: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.learning-tip p:last-child {
  font-weight: normal;
  color: #606266;
  font-style: italic;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  border-left: 4px solid #e6a23c;
}

.upgrade-prompt {
  padding: 20px;
  text-align: center;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border-top: 1px solid #e4e7ed;
}

.upgrade-prompt p {
  margin-bottom: 12px;
  font-size: 14px;
  color: #e65100;
}

.upgrade-button {
  background: #ff9800;
  border: none;
  border-radius: 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.upgrade-button:hover {
  background: #f57c00;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
}

/* 响应式设计 - 文章标题区域 */
@media (max-width: 768px) {
  .article-title-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .article-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .inline-parse-card {
    margin: 12px 0;
  }

  .parse-card-header {
    padding: 12px 16px;
  }

  .parse-basic-info,
  .premium-content,
  .upgrade-prompt {
    padding: 16px;
  }

  .vocab-list {
    gap: 6px;
  }

  .vocab-item {
    padding: 4px 8px;
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .article-actions .el-button {
    font-size: 12px;
    padding: 3px 10px;
  }
}

/* 英文原文区域标题和按钮布局 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

/* 朗读控制侧边栏样式 */
.read-control-sidebar {
  position: fixed;
  top: 50%;
  right: 10px; /* 更靠近右侧边缘 */
  transform: translateY(-50%);
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  padding: 16px 12px;
  width: 120px; /* 大幅减小宽度 */
  z-index: 1000;
  transition: all 0.3s ease;
  border: 1px solid #e8e8e8;
}

.read-control-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.read-control-header h3 {
  margin: 0;
  font-size: 14px;
  color: #333;
  white-space: nowrap;
}

.close-control .el-icon {
  font-size: 16px;
  color: #999;
}

.close-control:hover .el-icon {
  color: #666;
}

.read-control-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.control-buttons {
    display: flex;
    flex-direction: column;
    gap: 8px;
    align-items: center !important;
    width: 100%;
    padding: 0 !important;
    margin: 0 !important;
  }

  .control-buttons .el-button {
    min-width: 90px !important;
    width: 90px !important;
    border-radius: 8px !important;
    transition: all 0.3s ease !important;
    font-size: 12px !important;
    padding: 6px 8px !important;
    margin: 0 auto !important;
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
    flex-shrink: 0 !important;
  }

  /* 段落高亮样式 */
  .paragraph.highlighted {
    background-color: #e6f7ff;
    transition: background-color 0.3s ease;
  }
</style>

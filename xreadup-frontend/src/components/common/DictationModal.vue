<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isBatchMode ? '批量听写' : '单词听写'"
    width="600px"
    :before-close="handleClose"
    custom-class="dictation-modal"
  >
    <div class="dictation-content">
      <!-- 进度条 -->
      <div v-if="isBatchMode" class="dictation-progress">
        <div class="progress-info">
          <span>进度: {{ currentIndex + 1 }}/{{ words.length }}</span>
        </div>
        <el-progress :percentage="((currentIndex + 1) / words.length) * 100" />
      </div>

      <!-- 单词发音区 -->
      <div class="word-pronunciation">
        <div class="current-word" v-if="currentWord">
          <h3 v-if="showWord" class="word-text">{{ currentWord.word }}</h3>
          <div v-else class="word-placeholder">点击发音按钮开始听写</div>
          <p v-if="showWord" class="word-translation">{{ currentWord.translation }}</p>
        </div>
        <div class="pronunciation-controls">
          <el-button
            type="primary"
            icon="el-icon-video-play"
            @click="speakCurrentWord"
            :loading="isSpeaking"
          >
            发音
          </el-button>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <el-input
          v-model="userInput"
          placeholder="请输入听到的单词"
          @keyup.enter="checkAnswer"
          :disabled="showAnswer"
          clearable
          class="dictation-input"
        />
        <el-button
          type="success"
          @click="checkAnswer"
          :disabled="!userInput.trim() || showAnswer"
          class="check-button"
        >
          提交
        </el-button>
      </div>

      <!-- 反馈区 -->
      <div v-if="showAnswer" class="feedback-area">
        <div v-if="isCorrect" class="feedback-correct">
          <i class="el-icon-success"></i>
          <span>回答正确！</span>
        </div>
        <div v-else class="feedback-wrong">
          <i class="el-icon-error"></i>
          <span>回答错误！正确答案：{{ currentWord?.word }}</span>
        </div>
      </div>

      <!-- 控制按钮 -->
      <div v-if="showAnswer" class="control-buttons">
        <el-button v-if="isBatchMode" @click="nextWord">下一个</el-button>
        <el-button v-else @click="handleClose">完成</el-button>
      </div>

      <!-- 提示信息 -->
      <div v-if="!currentWord" class="empty-state">
        <p>没有可用的听写单词</p>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { tts } from '@/utils/tts';

interface WordItem {
  id: string;
  word: string;
  translation: string;
  reviewStatus: 'new' | 'learning' | 'mastered' | 'overdue';
  noLongerReview: boolean;
  nextReviewTime?: string;
}

interface DictationModalProps {
  words?: WordItem[];
  word?: WordItem;
  isBatchMode?: boolean;
  visible?: boolean;
}

const props = withDefaults(defineProps<DictationModalProps>(), {
  isBatchMode: false,
  words: () => [],
  visible: false
});

const emit = defineEmits<{
  close: [];
  finish: [results: { word: WordItem; isCorrect: boolean }[]];
}>();

const dialogVisible = ref(props.visible);

// 监听visible prop变化
watch(() => props.visible, (newValue) => {
  dialogVisible.value = newValue;
});

// 监听dialogVisible变化，仅当模态框关闭时向父组件发送close事件
watch(dialogVisible, (newValue, oldValue) => {
  if (oldValue && !newValue) {
    emit('close');
  }
});
const currentIndex = ref(0);
const userInput = ref('');
const showWord = ref(false);
const showAnswer = ref(false);
const isCorrect = ref(false);
const isSpeaking = ref(false);
const results = ref<{ word: WordItem; isCorrect: boolean }[]>([]);

// 计算当前单词
const currentWord = computed(() => {
  // 如果提供了单个word，则直接使用
  if (props.word) {
    return props.word;
  }
  // 否则从words数组中获取
  if (props.words && props.words.length > 0) {
    return props.words[currentIndex.value % props.words.length];
  }
  return null;
});

// 重置状态
const resetState = () => {
  userInput.value = '';
  showWord.value = false;
  showAnswer.value = false;
  isCorrect.value = false;
};

// 播放当前单词发音
const speakCurrentWord = async () => {
  if (!currentWord.value) return;

  try {
    isSpeaking.value = true;
    await tts.speakWord(currentWord.value.word);
  } catch (error) {
    console.error('发音失败:', error);
    ElMessage.error('发音失败，请稍后再试');
  } finally {
    isSpeaking.value = false;
  }
};

// 显示答案
const displayAnswer = () => {
  showWord.value = true;
  showAnswer.value = true;

  // 检查答案是否正确（不区分大小写）
  isCorrect.value = userInput.value.trim().toLowerCase() === currentWord.value?.word.toLowerCase();

  // 记录结果
  if (currentWord.value) {
    results.value.push({
      word: currentWord.value,
      isCorrect: isCorrect.value
    });
  }
};

// 清空输入
const onInputClear = () => {
  userInput.value = '';
};

// 检查答案
const checkAnswer = () => {
  if (!userInput.value.trim()) {
    ElMessage.warning('请输入答案');
    return;
  }

  displayAnswer();
};

// 下一个单词
const nextWord = () => {
  // 如果是最后一个单词，结束听写
  if (currentIndex.value >= props.words.length - 1) {
    ElMessage.success('听写完成！');
    emit('finish', results.value);
    dialogVisible.value = false;
    return;
  }

  // 进入下一个单词
  currentIndex.value++;
  // 不再在这里调用resetState和speakCurrentWord
  // 由watch监听器统一处理状态重置和自动发音
};

// 关闭模态框
const handleClose = () => {
  // 在非批量模式下，如果有听写结果，也发送完成事件
  if (!props.isBatchMode && results.value.length > 0) {
    emit('finish', results.value);
  }
  dialogVisible.value = false;
  emit('close');
};

// 监听单词变化，自动发音并重置状态
watch(currentWord, (newWord, oldWord) => {
  // 当单词变化时（不是首次加载），完全重置状态
  if (newWord && oldWord && newWord.id !== oldWord.id) {
    resetState();
  }

  if (newWord && !showWord.value) {
    setTimeout(() => {
      speakCurrentWord();
    }, 300);
  }
});

// 专门监听props.word的变化，确保父组件切换单词时能正确重置状态
watch(() => props.word, (newWord) => {
  // 当父组件传递的word存在时，重置状态
  if (newWord) {
    resetState();
  }
});

// 组件挂载时自动发音第一个单词
onMounted(() => {
  if (currentWord.value) {
    setTimeout(() => {
      speakCurrentWord();
    }, 300);
  }
});

// 组件卸载时清理状态
onUnmounted(() => {
  // 清理tts相关资源
});
</script>

<style scoped>
.dictation-modal {
  .dictation-content {
    padding: 20px 0;
  }

  .dictation-progress {
    margin-bottom: 20px;

    .progress-info {
      margin-bottom: 10px;
      text-align: right;
      font-size: 14px;
      color: #606266;
    }
  }

  .word-pronunciation {
    text-align: center;
    margin-bottom: 25px;

    .current-word {
      margin-bottom: 15px;

      .word-text {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 10px;
      }

      .word-translation {
        font-size: 16px;
        color: #606266;
        margin: 0;
      }

      .word-placeholder {
        font-size: 16px;
        color: #909399;
        padding: 20px 0;
      }
    }

    .pronunciation-controls {
      .el-button {
        min-width: 120px;
      }
    }
  }

  .input-area {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;

    .dictation-input {
      flex: 1;
    }

    .check-button {
      white-space: nowrap;
    }
  }

  .feedback-area {
    text-align: center;
    padding: 15px;
    border-radius: 4px;
    margin-bottom: 20px;

    &.feedback-correct {
      background-color: #f0f9eb;
      color: #67c23a;
      border: 1px solid #e1f3d8;
    }

    &.feedback-wrong {
      background-color: #fef0f0;
      color: #f56c6c;
      border: 1px solid #fbc4c4;
    }

    i {
      margin-right: 5px;
    }
  }

  .control-buttons {
    text-align: center;

    .el-button {
      min-width: 100px;
    }
  }

  .empty-state {
    text-align: center;
    color: #909399;
    padding: 40px 0;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dictation-modal {
    width: 90% !important;

    .word-pronunciation {
      .current-word {
        .word-text {
          font-size: 24px;
        }
      }
    }

    .input-area {
      flex-direction: column;

      .dictation-input,
      .check-button {
        width: 100%;
      }
    }
  }
}
</style>

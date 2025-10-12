<template>
  <!-- 朗读控制侧边栏 -->
  <div class="read-control-sidebar" v-if="showReadControlSidebar">
    <div class="read-control-header">
      <h3>朗读控制</h3>
      <el-button
        type="text"
        @click="handleStopSpeaking"
        class="close-control"
        title="关闭朗读控制"
      >
        <el-icon><CircleClose /></el-icon>
      </el-button>
    </div>

    <div class="read-control-content">
      <!-- 控制按钮组 -->
      <div class="control-buttons">
        <el-button
          @click="handlePauseResumeSpeaking"
          type="primary"
          size="large"
        >
          {{ isPaused ? '继续' : '暂停' }}
        </el-button>
        <el-button
          @click="handleStopSpeaking"
          type="danger"
          size="large"
        >
          停止
        </el-button>
      </div>

      <!-- 语速调节 -->
      <div class="speed-control">
        <label for="speed-slider">语速: {{ readingSpeed.toFixed(1) }}</label>
        <el-slider
          id="speed-slider"
          v-model="readingSpeed"
          :min="0.5"
          :max="2.0"
          :step="0.1"
          @change="handleSpeedChange"
        />
        <div class="speed-marks">
          <span>慢</span>
          <span>标准</span>
          <span>快</span>
        </div>
      </div>

      <!-- 音色选择 -->
      <div class="voice-control" v-if="voices.length > 0">
        <label class="voice-label">音色</label>
        <el-select v-model="selectedVoiceName" @change="handleChangeVoice" size="small" class="voice-select">
          <el-option
            v-for="voice in voices"
            :key="voice.name"
            :label="voice.name"
            :value="voice.name"
            class="voice-option"
          />
        </el-select>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { CircleClose } from '@element-plus/icons-vue';
import { tts } from '@/utils/tts';

// Props
const props = defineProps({
  paragraphs: {
    type: Array,
    required: true
  },
  highlightedParagraphIndex: {
    type: [Number, null],
    default: null
  }
});

// Emits
const emit = defineEmits<{
  highlightParagraph: [index: number | null];
  toggleReadControl: [show: boolean];
}>();

// State
const showReadControlSidebar = ref(false);
const isPaused = ref(false);
const readingSpeed = ref(1.0);
const voices = ref<any[]>([]);
const selectedVoiceName = ref('');

// Check TTS support
const checkTTSSupport = (): boolean => {
  if (!tts.isSupported()) {
    ElMessage.warning('当前浏览器不支持发音功能，建议使用 Chrome 或 Edge 浏览器');
    return false;
  }
  return true;
};

// Load voices
const loadVoices = (): void => {
  voices.value = tts.getVoices();
  if (voices.value.length > 0) {
    // Default to first English female voice or the first available
    const defaultVoice = voices.value.find(v => v.name.includes('Female') || v.name.includes('Google UK English Female'))
      || voices.value[0];
    selectedVoiceName.value = defaultVoice.name;
    tts.setVoice(defaultVoice);
  }
};

// Change voice
const handleChangeVoice = (name: string): void => {
  const voice = voices.value.find(v => v.name === name);
  if (voice) {
    tts.setVoice(voice);
    // If currently speaking, restart
    if (tts.isBusy()) {
      handleStopSpeaking();
      setTimeout(() => handleSpeakArticle(), 100);
    }
  }
};

// Speak article
const handleSpeakArticle = (): void => {
  if (!checkTTSSupport()) return;

  const paragraphs = props.paragraphs;
  if (!paragraphs.length) {
    ElMessage.warning('没有可朗读的内容');
    return;
  }

  // Start speaking with highlight callback
  tts.speakParagraphs(paragraphs as string[], {
    rate: readingSpeed.value,
    onHighlight: (index) => {
      emit('highlightParagraph', index);
    },
    onFinish: () => {
      emit('highlightParagraph', null);
      showReadControlSidebar.value = false;
      emit('toggleReadControl', false);
    }
  });

  showReadControlSidebar.value = true;
  emit('toggleReadControl', true);
  isPaused.value = false;
};

// Stop speaking
const handleStopSpeaking = (): void => {
  tts.stop();
  emit('highlightParagraph', null);
  isPaused.value = false;
  showReadControlSidebar.value = false;
  emit('toggleReadControl', false);
};

// Pause/resume speaking
const handlePauseResumeSpeaking = (): void => {
  if (isPaused.value) {
    tts.resume();
  } else {
    tts.pause();
  }
  isPaused.value = !isPaused.value;
};

// Change speed
const handleSpeedChange = (value: number): void => {
  readingSpeed.value = value;

  // If currently speaking, restart with new speed
  if (tts.isBusy()) {
    handleStopSpeaking();
    setTimeout(() => {
      handleSpeakArticle();
    }, 100);
  }
};

// Speak single word
const speakWord = (word: string): void => {
  if (!checkTTSSupport()) return;

  if (!word || word.trim() === '') {
    ElMessage.warning('没有可发音的单词');
    return;
  }

  // Call TTS to speak the word
  tts.speakWord(word);
};

// Handle word click event
const handleWordClick = (event: MouseEvent): string | null => {
  // Check TTS support
  if (!checkTTSSupport()) return null;

  const selection = window.getSelection()?.toString().trim();
  if (selection && selection.length <= 20 && /^[a-zA-Z]+$/.test(selection)) {
    // Call TTS to speak the word
    speakWord(selection);
    // Highlight the selected word (visual feedback)
    const range = window.getSelection()?.getRangeAt(0);
    if (range) {
      const wrapper = document.createElement('span');
      wrapper.style.backgroundColor = '#e6f7ff';
      wrapper.style.borderRadius = '2px';
      wrapper.style.padding = '1px 2px';
      wrapper.style.transition = 'background-color 0.3s';

      // Replace selected content with highlighted span
      range.surroundContents(wrapper);

      // Remove highlight after 1 second
      setTimeout(() => {
        if (wrapper.parentNode) {
          const parent = wrapper.parentNode;
          while (wrapper.firstChild) {
            parent.insertBefore(wrapper.firstChild, wrapper);
          }
          parent.removeChild(wrapper);
        }
      }, 1000);
    }
    return selection;
  }
  return null;
};

// Public methods to be exposed
const publicMethods = {
  handleSpeakArticle,
  handleStopSpeaking,
  handlePauseResumeSpeaking,
  handleSpeedChange,
  speakWord,
  handleWordClick,
  loadVoices
};

defineExpose(publicMethods);

// Lifecycle
onMounted(() => {
  // Load voices when component mounts
  loadVoices();
});

onUnmounted(() => {
  // Stop speaking when component unmounts
  tts.stop();
});
</script>

<style scoped>
.read-control-sidebar {
  position: fixed;
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 180px;
  padding: 16px;
  z-index: 1000;
  animation: slideInRight 0.3s ease;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateY(-50%) translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateY(-50%) translateX(0);
  }
}

.read-control-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.read-control-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.close-control {
  padding: 0 !important;
  width: 24px !important;
  height: 24px !important;
  min-width: auto !important;
}

.read-control-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
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

/* Handle Element UI button icons */
.control-buttons .el-button .el-icon {
  margin-right: 0 !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
}

/* Ensure large buttons are also centered */
.control-buttons .el-button--large {
  min-width: 90px !important;
  width: 90px !important;
  margin: 0 auto !important;
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
  padding: 12px 20px !important;
  flex-shrink: 0 !important;
}

/* Override Element UI button default styles */
.read-control-sidebar .control-buttons .el-button {
  min-width: 90px !important;
  width: 90px !important;
}

.read-control-sidebar .control-buttons .el-button--large {
  min-width: 90px !important;
  width: 90px !important;
}

/* Fix alignment of icons and text inside Element UI buttons */
.control-buttons .el-button .el-button__content {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 4px !important;
}

.speed-control {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.speed-control label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
  text-align: center;
}

.speed-control .el-slider {
  margin: 4px 0;
  width: 90px;
  align-self: center;
}

.speed-marks {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: #999;
  padding: 0 2px;
  width: 90px;
  align-self: center;
}

/* Voice selection UI styles */
.voice-control {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.voice-label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
  text-align: center;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.voice-select {
  width: 100%;
  max-width: 100px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.voice-select .el-input__inner {
  font-size: 12px;
  padding: 4px 8px;
  height: 28px;
  font-family: inherit;
}

.voice-option {
  font-size: 12px;
  padding: 6px 10px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

/* Responsive design - read control sidebar */
@media (max-width: 768px) {
  .read-control-sidebar {
    width: calc(100% - 20px);
    right: 10px;
    left: 10px;
    top: auto;
    bottom: 10px;
    transform: none;
  }

  .voice-select {
    max-width: 100%;
  }

  .control-buttons {
    flex-direction: column;
  }

  .control-buttons .el-button {
    width: 100%;
    min-width: unset;
  }

  .speed-control .el-slider,
  .speed-marks {
    width: 100%;
  }
}
</style>
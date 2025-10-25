<template>
  <div class="upgrade-prompt" v-if="shouldShowPrompt">
    <div class="prompt-content">
      <div class="prompt-icon">🚀</div>
      <div class="prompt-text">
        <h4>升级享受更多功能</h4>
        <p>{{ promptMessage }}</p>
      </div>
      <div class="prompt-actions">
        <el-button type="primary" @click="showUpgradeDialog" size="small">
          立即升级
        </el-button>
        <el-button @click="dismissPrompt" text size="small">
          稍后再说
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

interface Props {
  currentUsage: number
  maxUsage: number
  featureName: string
  userTier: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  showUpgradeDialog: []
  dismissPrompt: []
}>()

const dismissed = ref(false)

const shouldShowPrompt = computed(() => {
  if (dismissed.value) return false
  if (props.userTier === 'enterprise') return false
  
  const usageRatio = props.currentUsage / props.maxUsage
  return usageRatio >= 0.8 // 使用率达到80%时显示提示
})

const promptMessage = computed(() => {
  const usageRatio = Math.round((props.currentUsage / props.maxUsage) * 100)
  return `你已使用 ${usageRatio}% 的${props.featureName}额度，升级享受无限可能`
})

const showUpgradeDialog = () => {
  emit('showUpgradeDialog')
}

const dismissPrompt = () => {
  dismissed.value = true
  emit('dismissPrompt')
}
</script>

<style scoped>
.upgrade-prompt {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 16px;
  margin: 16px 0;
  color: white;
  animation: slideInUp 0.5s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.prompt-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.prompt-icon {
  font-size: 2em;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-5px);
  }
  60% {
    transform: translateY(-3px);
  }
}

.prompt-text {
  flex: 1;
}

.prompt-text h4 {
  margin: 0 0 4px 0;
  font-size: 1.1em;
  font-weight: 600;
}

.prompt-text p {
  margin: 0;
  font-size: 0.9em;
  opacity: 0.9;
}

.prompt-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .prompt-content {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .prompt-actions {
    justify-content: center;
  }
  
  .prompt-icon {
    font-size: 1.5em;
  }
}
</style>

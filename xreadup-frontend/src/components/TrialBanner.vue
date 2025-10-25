<template>
  <div class="trial-banner" v-if="showTrial">
    <div class="banner-content">
      <div class="banner-left">
        <div class="trial-icon">🎉</div>
        <div class="trial-text">
          <h3>新用户专享</h3>
          <p>{{ planName }} {{ trialDays }}天免费试用，体验完整功能</p>
        </div>
      </div>
      <div class="banner-right">
        <el-button type="primary" @click="startTrial" :loading="loading">
          立即试用
        </el-button>
        <el-button @click="dismissBanner" text>
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

interface Props {
  planName: string
  trialDays: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  startTrial: [planType: string]
}>()

const showTrial = ref(true)
const loading = ref(false)

// 检查是否已经关闭过试用横幅
const isDismissed = computed(() => {
  return localStorage.getItem('trial_banner_dismissed') === 'true'
})

// 检查是否是新用户
const isNewUser = computed(() => {
  const lastVisit = localStorage.getItem('last_visit')
  const now = Date.now()
  const oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000)
  
  if (!lastVisit) {
    localStorage.setItem('last_visit', now.toString())
    return true
  }
  
  return parseInt(lastVisit) > oneWeekAgo
})

// 显示试用横幅的条件
const shouldShowTrial = computed(() => {
  return showTrial.value && !isDismissed.value && isNewUser.value
})

const startTrial = async () => {
  loading.value = true
  try {
    // 模拟试用开始API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('试用已开始，享受完整功能！')
    emit('startTrial', props.planName)
    
    // 隐藏横幅
    showTrial.value = false
  } catch (error) {
    ElMessage.error('开始试用失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const dismissBanner = () => {
  showTrial.value = false
  localStorage.setItem('trial_banner_dismissed', 'true')
}
</script>

<style scoped>
.trial-banner {
  background: linear-gradient(135deg, #ff6b6b, #ffa500);
  border-radius: 12px;
  margin: 20px 0;
  overflow: hidden;
  position: relative;
  animation: slideInDown 0.5s ease-out;
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.trial-banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent 30%, rgba(255,255,255,0.1) 50%, transparent 70%);
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.banner-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.trial-icon {
  font-size: 2.5em;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

.trial-text h3 {
  margin: 0 0 4px 0;
  color: white;
  font-size: 1.2em;
  font-weight: 600;
}

.trial-text p {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9em;
}

.banner-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

@media (max-width: 768px) {
  .banner-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .banner-right {
    justify-content: center;
  }
  
  .trial-icon {
    font-size: 2em;
  }
}
</style>

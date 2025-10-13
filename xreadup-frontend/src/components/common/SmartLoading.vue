<template>
  <div class="smart-loading-container" :class="containerClass">
    <!-- 主要加载状态 -->
    <div v-if="type === 'spinner'" class="smart-spinner">
      <div class="spinner-ring"></div>
      <div class="spinner-ring"></div>
      <div class="spinner-ring"></div>
    </div>
    
    <!-- 脉冲加载状态 -->
    <div v-else-if="type === 'pulse'" class="smart-pulse">
      <div class="pulse-dot"></div>
      <div class="pulse-dot"></div>
      <div class="pulse-dot"></div>
    </div>
    
    <!-- 波浪加载状态 -->
    <div v-else-if="type === 'wave'" class="smart-wave">
      <div class="wave-bar"></div>
      <div class="wave-bar"></div>
      <div class="wave-bar"></div>
      <div class="wave-bar"></div>
      <div class="wave-bar"></div>
    </div>
    
    <!-- 骨架屏加载状态 -->
    <div v-else-if="type === 'skeleton'" class="smart-skeleton">
      <div class="skeleton-line" v-for="i in lines" :key="i" :style="{ width: getSkeletonWidth(i) }"></div>
    </div>
    
    <!-- 加载文本 -->
    <div v-if="text" class="loading-text">{{ text }}</div>
    
    <!-- 进度条 -->
    <div v-if="showProgress && progress !== undefined" class="loading-progress">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progress + '%' }"></div>
      </div>
      <div class="progress-text">{{ progress }}%</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  type?: 'spinner' | 'pulse' | 'wave' | 'skeleton'
  size?: 'sm' | 'md' | 'lg'
  text?: string
  showProgress?: boolean
  progress?: number
  lines?: number
  color?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'spinner',
  size: 'md',
  text: '',
  showProgress: false,
  progress: undefined,
  lines: 3,
  color: 'primary'
})

const containerClass = computed(() => ({
  [`smart-loading--${props.size}`]: true,
  [`smart-loading--${props.color}`]: true
}))

const getSkeletonWidth = (index: number) => {
  const widths = ['100%', '80%', '60%', '90%', '70%']
  return widths[(index - 1) % widths.length]
}
</script>

<style scoped>
@import '@/assets/design-system.css';

.smart-loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-4);
}

/* 尺寸变体 */
.smart-loading--sm {
  padding: var(--space-2);
  gap: var(--space-2);
}

.smart-loading--md {
  padding: var(--space-4);
  gap: var(--space-3);
}

.smart-loading--lg {
  padding: var(--space-6);
  gap: var(--space-4);
}

/* 智能旋转加载器 */
.smart-spinner {
  position: relative;
  width: 40px;
  height: 40px;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-radius: 50%;
  animation: smartSpin 1.5s linear infinite;
}

.spinner-ring:nth-child(1) {
  border-top-color: var(--ios-blue);
  animation-delay: 0s;
}

.spinner-ring:nth-child(2) {
  border-right-color: var(--ios-purple);
  animation-delay: 0.5s;
}

.spinner-ring:nth-child(3) {
  border-bottom-color: var(--ios-pink);
  animation-delay: 1s;
}

@keyframes smartSpin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 脉冲加载器 */
.smart-pulse {
  display: flex;
  gap: var(--space-2);
  align-items: center;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  background: var(--ios-blue);
  border-radius: 50%;
  animation: smartPulse 1.4s ease-in-out infinite;
}

.pulse-dot:nth-child(1) {
  animation-delay: 0s;
}

.pulse-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.pulse-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes smartPulse {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1.2);
    opacity: 1;
  }
}

/* 波浪加载器 */
.smart-wave {
  display: flex;
  gap: 2px;
  align-items: flex-end;
  height: 20px;
}

.wave-bar {
  width: 4px;
  background: var(--ios-blue);
  border-radius: 2px;
  animation: smartWave 1.2s ease-in-out infinite;
}

.wave-bar:nth-child(1) {
  height: 8px;
  animation-delay: 0s;
}

.wave-bar:nth-child(2) {
  height: 16px;
  animation-delay: 0.1s;
}

.wave-bar:nth-child(3) {
  height: 20px;
  animation-delay: 0.2s;
}

.wave-bar:nth-child(4) {
  height: 16px;
  animation-delay: 0.3s;
}

.wave-bar:nth-child(5) {
  height: 8px;
  animation-delay: 0.4s;
}

@keyframes smartWave {
  0%, 100% {
    transform: scaleY(0.4);
    opacity: 0.5;
  }
  50% {
    transform: scaleY(1);
    opacity: 1;
  }
}

/* 骨架屏加载器 */
.smart-skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  width: 100%;
  max-width: 300px;
}

.skeleton-line {
  height: 12px;
  background: var(--gradient-liquid-glass);
  border-radius: 6px;
  animation: skeletonShimmer 2s ease-in-out infinite;
}

@keyframes skeletonShimmer {
  0% {
    opacity: 0.6;
    transform: translateX(-100%);
  }
  50% {
    opacity: 1;
    transform: translateX(0);
  }
  100% {
    opacity: 0.6;
    transform: translateX(100%);
  }
}

/* 加载文本 */
.loading-text {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-weight-medium);
  text-align: center;
}

/* 进度条 */
.loading-progress {
  width: 100%;
  max-width: 200px;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: var(--bg-tertiary);
  border-radius: 3px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: var(--gradient-primary);
  border-radius: 3px;
  transition: width 0.3s ease;
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: progressShimmer 2s infinite;
}

@keyframes progressShimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.progress-text {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  text-align: center;
  font-weight: var(--font-weight-medium);
}

/* 颜色变体 */
.smart-loading--primary .spinner-ring,
.smart-loading--primary .pulse-dot,
.smart-loading--primary .wave-bar {
  background: var(--ios-blue);
}

.smart-loading--success .spinner-ring,
.smart-loading--success .pulse-dot,
.smart-loading--success .wave-bar {
  background: var(--ios-green);
}

.smart-loading--warning .spinner-ring,
.smart-loading--warning .pulse-dot,
.smart-loading--warning .wave-bar {
  background: var(--ios-orange);
}

.smart-loading--error .spinner-ring,
.smart-loading--error .pulse-dot,
.smart-loading--error .wave-bar {
  background: var(--ios-red);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .smart-loading--lg {
    padding: var(--space-4);
    gap: var(--space-3);
  }
  
  .smart-spinner {
    width: 32px;
    height: 32px;
  }
  
  .smart-wave {
    height: 16px;
  }
  
  .wave-bar {
    width: 3px;
  }
}
</style>

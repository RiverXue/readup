<template>
  <button 
    :class="buttonClass" 
    :disabled="disabled || loading"
    @click="handleClick"
    @mousedown="handleMouseDown"
    @mouseup="handleMouseUp"
    @mouseleave="handleMouseLeave"
    type="button"
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="button-loading">
      <SmartLoading type="spinner" size="sm" />
    </div>
    
    <!-- 图标 -->
    <div v-if="$slots.icon && !loading" class="button-icon">
      <slot name="icon"></slot>
    </div>
    
    <!-- 按钮文本 -->
    <span class="button-text">
      <slot></slot>
    </span>
    
    <!-- 后缀图标 -->
    <div v-if="$slots.suffix && !loading" class="button-suffix">
      <slot name="suffix"></slot>
    </div>
    
    <!-- 触觉反馈波纹 -->
    <div v-if="showRipple" class="ripple-effect" :style="rippleStyle"></div>
  </button>
</template>

<script setup lang="ts">
import { computed, ref, reactive } from 'vue'
import SmartLoading from './SmartLoading.vue'

interface Props {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger' | 'success' | 'warning' | 'liquid-glass'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
  loading?: boolean
  block?: boolean
  rounded?: 'sm' | 'md' | 'lg' | 'xl' | 'full'
  tactile?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  disabled: false,
  loading: false,
  block: false,
  rounded: 'lg',
  tactile: true
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const showRipple = ref(false)
const rippleStyle = reactive({
  left: '0px',
  top: '0px',
  width: '0px',
  height: '0px'
})

const buttonClass = computed(() => ({
  'tactile-button': true,
  [`tactile-button--${props.variant}`]: true,
  [`tactile-button--${props.size}`]: true,
  [`tactile-button--rounded-${props.rounded}`]: true,
  'tactile-button--block': props.block,
  'tactile-button--disabled': props.disabled,
  'tactile-button--loading': props.loading
}))

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}

const handleMouseDown = (event: MouseEvent) => {
  if (!props.disabled && !props.loading && props.tactile) {
    createRipple(event)
  }
}

const handleMouseUp = () => {
  if (showRipple.value) {
    setTimeout(() => {
      showRipple.value = false
    }, 300)
  }
}

const handleMouseLeave = () => {
  if (showRipple.value) {
    setTimeout(() => {
      showRipple.value = false
    }, 150)
  }
}

const createRipple = (event: MouseEvent) => {
  const button = event.currentTarget as HTMLElement
  const rect = button.getBoundingClientRect()
  const size = Math.max(rect.width, rect.height)
  const x = event.clientX - rect.left - size / 2
  const y = event.clientY - rect.top - size / 2
  
  rippleStyle.left = x + 'px'
  rippleStyle.top = y + 'px'
  rippleStyle.width = size + 'px'
  rippleStyle.height = size + 'px'
  
  showRipple.value = true
}
</script>

<style scoped>
@import '@/assets/design-system.css';

.tactile-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border-radius: var(--radius-lg);
  font-weight: var(--font-weight-medium);
  line-height: var(--line-height-tight);
  transition: all var(--transition-normal);
  cursor: pointer;
  border: none;
  position: relative;
  overflow: hidden;
  font-family: var(--font-family-primary);
  text-decoration: none;
  letter-spacing: -0.01em;
}

/* 尺寸变体 */
.tactile-button--sm {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  min-height: 36px;
}

.tactile-button--md {
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-sm);
  min-height: 44px;
}

.tactile-button--lg {
  padding: var(--space-4) var(--space-8);
  font-size: var(--text-base);
  min-height: 52px;
}

/* 圆角变体 */
.tactile-button--rounded-sm {
  border-radius: var(--radius-sm);
}

.tactile-button--rounded-md {
  border-radius: var(--radius-md);
}

.tactile-button--rounded-lg {
  border-radius: var(--radius-lg);
}

.tactile-button--rounded-xl {
  border-radius: var(--radius-xl);
}

.tactile-button--rounded-full {
  border-radius: var(--radius-full);
}

/* 变体样式 */
.tactile-button--primary {
  background: linear-gradient(135deg, var(--ios-green) 0%, #2dd4bf 100%);
  color: var(--text-inverse);
  box-shadow: 
    0 8px 25px rgba(52, 199, 89, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.3);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.tactile-button--primary:hover:not(.tactile-button--disabled) {
  transform: translateY(-2px);
  box-shadow: 
    0 12px 35px rgba(52, 199, 89, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  border-color: rgba(255, 255, 255, 0.5);
}

.tactile-button--secondary {
  background: var(--bg-primary);
  color: var(--ios-blue);
  border: 2px solid var(--ios-blue);
}

.tactile-button--secondary:hover:not(.tactile-button--disabled) {
  background: rgba(0, 122, 255, 0.1);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.tactile-button--ghost {
  background: transparent;
  color: var(--text-secondary);
  border: 2px solid var(--border-light);
}

.tactile-button--ghost:hover:not(.tactile-button--disabled) {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--ios-blue);
  transform: translateY(-2px);
}

.tactile-button--success {
  background: var(--gradient-success);
  color: var(--text-inverse);
  box-shadow: var(--shadow-success);
}

.tactile-button--success:hover:not(.tactile-button--disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg), var(--shadow-success);
}

.tactile-button--warning {
  background: linear-gradient(135deg, #efc331 0%, #e9c243 100%);
  color: #ffffff;
  box-shadow: 
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.3);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.tactile-button--warning:hover:not(.tactile-button--disabled) {
  transform: translateY(-2px);
  box-shadow: 
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  border-color: rgba(255, 255, 255, 0.6);
}

.tactile-button--danger {
  background: linear-gradient(135deg, var(--ios-red), #dc2626);
  color: var(--text-inverse);
  box-shadow: var(--shadow-error);
}

.tactile-button--danger:hover:not(.tactile-button--disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg), var(--shadow-error);
}

.tactile-button--liquid-glass {
  background: var(--glass-white);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--glass-border);
  color: var(--text-inverse);
  box-shadow: 
    0 8px 32px var(--glass-shadow),
    inset 0 1px 0 rgba(255, 255, 255, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.tactile-button--liquid-glass:hover:not(.tactile-button--disabled) {
  background: var(--glass-white-medium);
  transform: translateY(-2px) scale(1.02);
  box-shadow: 
    0 12px 40px var(--glass-shadow-medium),
    inset 0 1px 0 rgba(255, 255, 255, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

/* 特殊样式 */
.tactile-button--block {
  width: 100%;
}

/* 状态样式 */
.tactile-button--disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.tactile-button--loading {
  cursor: wait;
}

.tactile-button--loading .button-text {
  opacity: 0.7;
}

/* 触觉反馈效果 */
.tactile-button:active:not(.tactile-button--disabled) {
  transform: scale(0.98);
  transition: transform 0.1s ease;
}

/* 内部元素 */
.button-loading {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.button-icon {
  display: flex;
  align-items: center;
}

.button-text {
  transition: opacity var(--transition-normal);
}

.button-suffix {
  display: flex;
  align-items: center;
}

/* 波纹效果 */
.ripple-effect {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: scale(0);
  animation: ripple 0.6s ease-out;
  pointer-events: none;
}

@keyframes ripple {
  to {
    transform: scale(2);
    opacity: 0;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tactile-button--lg {
    padding: var(--space-3) var(--space-6);
    font-size: var(--text-sm);
    min-height: 48px;
  }
  
  .tactile-button--md {
    padding: var(--space-2) var(--space-5);
    min-height: 40px;
  }
}

/* 无障碍支持 */
@media (prefers-reduced-motion: reduce) {
  .tactile-button {
    transition: none;
  }
  
  .tactile-button:hover {
    transform: none;
  }
  
  .ripple-effect {
    animation: none;
  }
}
</style>

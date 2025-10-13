<template>
  <button 
    :class="buttonClass" 
    :disabled="disabled || loading"
    @click="handleClick"
    type="button"
  >
    <div v-if="loading" class="modern-button__loading">
      <div class="modern-spinner"></div>
    </div>
    
    <div v-if="$slots.icon && !loading" class="modern-button__icon">
      <slot name="icon"></slot>
    </div>
    
    <span class="modern-button__text">
      <slot></slot>
    </span>
    
    <div v-if="$slots.suffix && !loading" class="modern-button__suffix">
      <slot name="suffix"></slot>
    </div>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger' | 'success' | 'warning'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
  loading?: boolean
  block?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  disabled: false,
  loading: false,
  block: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const buttonClass = computed(() => ({
  'modern-button': true,
  [`modern-button--${props.variant}`]: true,
  [`modern-button--${props.size}`]: true,
  'modern-button--block': props.block,
  'modern-button--loading': props.loading,
  'modern-button--disabled': props.disabled
}))

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style scoped>
.modern-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border-radius: var(--radius-xl);
  font-weight: var(--font-weight-medium);
  line-height: var(--line-height-tight);
  transition: all var(--transition-normal);
  cursor: pointer;
  border: none;
  position: relative;
  overflow: hidden;
  text-decoration: none;
  font-family: var(--font-family-primary);
}

.modern-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left var(--transition-slow);
}

.modern-button:hover::before {
  left: 100%;
}

/* 尺寸样式 */
.modern-button--sm {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  min-height: 32px;
}

.modern-button--md {
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-sm);
  min-height: 40px;
}

.modern-button--lg {
  padding: var(--space-4) var(--space-8);
  font-size: var(--text-base);
  min-height: 48px;
}

/* 变体样式 */
.modern-button--primary {
  background: linear-gradient(135deg, var(--primary-500), var(--primary-600));
  color: var(--text-inverse);
  box-shadow: var(--shadow-primary);
}

.modern-button--primary:hover:not(.modern-button--disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg), var(--shadow-primary);
}

.modern-button--secondary {
  background: var(--bg-primary);
  color: var(--primary-600);
  border: 1px solid var(--primary-200);
}

.modern-button--secondary:hover:not(.modern-button--disabled) {
  background: var(--primary-50);
  border-color: var(--primary-300);
}

.modern-button--ghost {
  background: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--border-light);
}

.modern-button--ghost:hover:not(.modern-button--disabled) {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.modern-button--danger {
  background: linear-gradient(135deg, var(--accent-error), #dc2626);
  color: var(--text-inverse);
  box-shadow: var(--shadow-error);
}

.modern-button--danger:hover:not(.modern-button--disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg), var(--shadow-error);
}

.modern-button--success {
  background: linear-gradient(135deg, var(--accent-success), #059669);
  color: var(--text-inverse);
  box-shadow: var(--shadow-success);
}

.modern-button--success:hover:not(.modern-button--disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg), var(--shadow-success);
}

.modern-button--warning {
  background: linear-gradient(135deg, var(--accent-warning), #d97706);
  color: var(--text-inverse);
  box-shadow: var(--shadow-warning);
}

.modern-button--warning:hover:not(.modern-button--disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg), var(--shadow-warning);
}

/* 状态样式 */
.modern-button--block {
  width: 100%;
}

.modern-button--disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.modern-button--loading {
  cursor: wait;
}

.modern-button--loading .modern-button__text {
  opacity: 0.7;
}

/* 内部元素 */
.modern-button__loading {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.modern-button__icon {
  display: flex;
  align-items: center;
}

.modern-button__text {
  transition: opacity var(--transition-normal);
}

.modern-button__suffix {
  display: flex;
  align-items: center;
}

/* 加载动画 */
.modern-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: var(--radius-full);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式 */
@media (max-width: 768px) {
  .modern-button--lg {
    padding: var(--space-3) var(--space-6);
    font-size: var(--text-sm);
    min-height: 44px;
  }
}
</style>

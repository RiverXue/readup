<template>
  <button 
    :class="buttonClass" 
    :disabled="disabled || loading"
    @click="handleClick"
    type="button"
  >
    <div v-if="loading" class="airbnb-button__loading">
      <div class="airbnb-spinner"></div>
    </div>
    
    <div v-if="$slots.icon && !loading" class="airbnb-button__icon">
      <slot name="icon"></slot>
    </div>
    
    <span class="airbnb-button__text">
      <slot></slot>
    </span>
    
    <div v-if="$slots.suffix && !loading" class="airbnb-button__suffix">
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
  floating?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  disabled: false,
  loading: false,
  block: false,
  floating: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const buttonClass = computed(() => ({
  'airbnb-button': true,
  [`airbnb-button--${props.variant}`]: true,
  [`airbnb-button--${props.size}`]: true,
  'airbnb-button--block': props.block,
  'airbnb-button--floating': props.floating,
  'airbnb-button--loading': props.loading,
  'airbnb-button--disabled': props.disabled
}))

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style scoped>
.airbnb-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border-radius: var(--radius-ios-large);
  font-weight: var(--font-weight-semibold);
  line-height: var(--line-height-tight);
  transition: all var(--transition-normal);
  cursor: pointer;
  border: none;
  position: relative;
  overflow: hidden;
  text-decoration: none;
  font-family: var(--font-family-primary);
  letter-spacing: -0.01em;
}

/* 尺寸样式 */
.airbnb-button--sm {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  min-height: 36px;
}

.airbnb-button--md {
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-sm);
  min-height: 44px;
}

.airbnb-button--lg {
  padding: var(--space-4) var(--space-8);
  font-size: var(--text-base);
  min-height: 52px;
}

/* 变体样式 */
.airbnb-button--primary {
  background: var(--gradient-primary);
  color: var(--text-inverse);
  box-shadow: var(--shadow-primary);
}

.airbnb-button--primary:hover:not(.airbnb-button--disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-ios-heavy), var(--shadow-primary);
}

.airbnb-button--secondary {
  background: var(--bg-primary);
  color: var(--ios-blue);
  border: 2px solid var(--ios-blue);
}

.airbnb-button--secondary:hover:not(.airbnb-button--disabled) {
  background: rgba(0, 122, 255, 0.1);
  transform: translateY(-2px);
  box-shadow: var(--shadow-ios-medium);
}

.airbnb-button--ghost {
  background: transparent;
  color: var(--text-secondary);
  border: 2px solid var(--border-light);
}

.airbnb-button--ghost:hover:not(.airbnb-button--disabled) {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--ios-blue);
  transform: translateY(-2px);
}

.airbnb-button--success {
  background: var(--gradient-success);
  color: var(--text-inverse);
  box-shadow: var(--shadow-success);
}

.airbnb-button--success:hover:not(.airbnb-button--disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-ios-heavy), var(--shadow-success);
}

.airbnb-button--warning {
  background: var(--gradient-warm);
  color: var(--text-inverse);
  box-shadow: var(--shadow-warning);
}

.airbnb-button--warning:hover:not(.airbnb-button--disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-ios-heavy), var(--shadow-warning);
}

.airbnb-button--danger {
  background: linear-gradient(135deg, var(--ios-red), #dc2626);
  color: var(--text-inverse);
  box-shadow: var(--shadow-error);
}

.airbnb-button--danger:hover:not(.airbnb-button--disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-ios-heavy), var(--shadow-error);
}

/* 特殊样式 */
.airbnb-button--floating {
  box-shadow: var(--shadow-ios-heavy);
  border-radius: var(--radius-full);
}

.airbnb-button--floating:hover:not(.airbnb-button--disabled) {
  transform: translateY(-4px) scale(1.05);
  box-shadow: var(--shadow-2xl);
}

.airbnb-button--block {
  width: 100%;
}

/* 状态样式 */
.airbnb-button--disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.airbnb-button--loading {
  cursor: wait;
}

.airbnb-button--loading .airbnb-button__text {
  opacity: 0.7;
}

/* 内部元素 */
.airbnb-button__loading {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.airbnb-button__icon {
  display: flex;
  align-items: center;
}

.airbnb-button__text {
  transition: opacity var(--transition-normal);
}

.airbnb-button__suffix {
  display: flex;
  align-items: center;
}

/* 加载动画 */
.airbnb-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: var(--radius-full);
  animation: spin-airbnb 1s linear infinite;
}

@keyframes spin-airbnb {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式 */
@media (max-width: 768px) {
  .airbnb-button--lg {
    padding: var(--space-3) var(--space-6);
    font-size: var(--text-sm);
    min-height: 48px;
  }
  
  .airbnb-button--md {
    padding: var(--space-2) var(--space-5);
    min-height: 40px;
  }
}
</style>

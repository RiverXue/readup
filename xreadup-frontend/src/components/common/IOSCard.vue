<template>
  <div class="ios-card" :class="cardClass">
    <div v-if="$slots.header" class="ios-card__header">
      <slot name="header"></slot>
    </div>
    
    <div class="ios-card__content">
      <slot></slot>
    </div>
    
    <div v-if="$slots.footer" class="ios-card__footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'default' | 'elevated' | 'glass' | 'outlined'
  size?: 'sm' | 'md' | 'lg'
  interactive?: boolean
  rounded?: 'sm' | 'md' | 'lg' | 'xl'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
  size: 'md',
  interactive: false,
  rounded: 'lg'
})

const cardClass = computed(() => ({
  [`ios-card--${props.variant}`]: true,
  [`ios-card--${props.size}`]: true,
  [`ios-card--rounded-${props.rounded}`]: true,
  'ios-card--interactive': props.interactive
}))
</script>

<style scoped>
.ios-card {
  background: var(--bg-primary);
  border-radius: var(--radius-ios-large);
  box-shadow: var(--shadow-ios-light);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all var(--transition-normal);
  overflow: hidden;
  position: relative;
  font-family: var(--font-family-primary);
}

/* 变体样式 */
.ios-card--elevated {
  box-shadow: var(--shadow-ios-medium);
}

.ios-card--glass {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: var(--shadow-glass);
}

.ios-card--outlined {
  box-shadow: none;
  border-width: 2px;
  border-color: var(--ios-blue);
}

/* 尺寸样式 */
.ios-card--sm {
  padding: var(--space-4);
}

.ios-card--md {
  padding: var(--space-6);
}

.ios-card--lg {
  padding: var(--space-8);
}

/* 圆角样式 */
.ios-card--rounded-sm {
  border-radius: var(--radius-ios-small);
}

.ios-card--rounded-md {
  border-radius: var(--radius-ios-medium);
}

.ios-card--rounded-lg {
  border-radius: var(--radius-ios-large);
}

.ios-card--rounded-xl {
  border-radius: var(--radius-ios-xl);
}

/* 交互样式 */
.ios-card--interactive {
  cursor: pointer;
}

.ios-card--interactive:hover {
  transform: translateY(-2px) scale(1.01);
  box-shadow: var(--shadow-ios-heavy);
  border-color: var(--ios-blue);
}

.ios-card--interactive:active {
  transform: translateY(0) scale(0.99);
  transition: all var(--transition-fast);
}

/* 内部结构 */
.ios-card__header {
  padding: var(--space-6) var(--space-6) 0;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: var(--space-6);
}

.ios-card__content {
  flex: 1;
}

.ios-card__footer {
  padding: var(--space-6) var(--space-6) 0;
  border-top: 1px solid var(--border-light);
  margin-top: var(--space-6);
}

/* 响应式 */
@media (max-width: 768px) {
  .ios-card--lg {
    padding: var(--space-5);
  }
  
  .ios-card--md {
    padding: var(--space-4);
  }
}
</style>

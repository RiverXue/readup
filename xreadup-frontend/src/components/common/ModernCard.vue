<template>
  <div class="modern-card" :class="cardClass">
    <div v-if="$slots.header" class="modern-card__header">
      <slot name="header"></slot>
    </div>
    
    <div class="modern-card__content">
      <slot></slot>
    </div>
    
    <div v-if="$slots.footer" class="modern-card__footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'default' | 'elevated' | 'outlined' | 'filled'
  size?: 'sm' | 'md' | 'lg'
  interactive?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
  size: 'md',
  interactive: false
})

const cardClass = computed(() => ({
  [`modern-card--${props.variant}`]: true,
  [`modern-card--${props.size}`]: true,
  'modern-card--interactive': props.interactive
}))
</script>

<style scoped>
.modern-card {
  background: var(--bg-primary);
  border-radius: var(--radius-ios-large);
  box-shadow: var(--shadow-ios-light);
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
  overflow: hidden;
  position: relative;
}

.modern-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--gradient-primary);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

/* 变体样式 */
.modern-card--elevated {
  box-shadow: var(--shadow-ios-medium);
}

.modern-card--outlined {
  box-shadow: none;
  border-width: 2px;
  border-color: var(--ios-blue);
}

.modern-card--filled {
  background: var(--bg-secondary);
  box-shadow: none;
}

/* 尺寸样式 */
.modern-card--sm {
  padding: var(--space-4);
}

.modern-card--md {
  padding: var(--space-6);
}

.modern-card--lg {
  padding: var(--space-8);
}

/* 交互样式 */
.modern-card--interactive {
  cursor: pointer;
}

.modern-card--interactive:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: var(--shadow-ios-heavy);
  border-color: var(--ios-blue);
}

.modern-card--interactive:hover::before {
  opacity: 1;
}

.modern-card__header {
  padding: var(--space-6) var(--space-6) 0;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: var(--space-6);
}

.modern-card__content {
  flex: 1;
}

.modern-card__footer {
  padding: var(--space-6) var(--space-6) 0;
  border-top: 1px solid var(--border-light);
  margin-top: var(--space-6);
}
</style>

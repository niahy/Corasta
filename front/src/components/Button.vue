<template>
  <component
    :is="tag"
    :class="['btn', `btn-${variant}`, `btn-${size}`, { 'btn-block': block, 'btn-loading': loading }]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="loading" class="btn-spinner"></span>
    <slot></slot>
  </component>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'outline', 'text', 'danger'].includes(value),
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value),
  },
  block: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  tag: {
    type: String,
    default: 'button',
    validator: (value) => ['button', 'a', 'router-link'].includes(value),
  },
});

const emit = defineEmits(['click']);

const handleClick = (event) => {
  if (!props.disabled && !props.loading) {
    emit('click', event);
  }
};
</script>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 500;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  white-space: nowrap;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 尺寸 */
.btn-small {
  padding: 8px 16px;
  font-size: 13px;
}

.btn-medium {
  padding: 12px 24px;
  font-size: 15px;
}

.btn-large {
  padding: 16px 32px;
  font-size: 16px;
}

/* 主要按钮 */
.btn-primary {
  background: linear-gradient(135deg, var(--coral-pink), var(--stardust-purple));
  color: var(--text-light);
  box-shadow: 0 12px 24px rgba(255, 126, 138, 0.35);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 126, 138, 0.4);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

/* 次要按钮 */
.btn-secondary {
  background: var(--glass-panel-bg);
  color: var(--midnight-purple);
  border: 1px solid var(--glass-border);
}

.btn-secondary:hover:not(:disabled) {
  background: var(--stardust-purple);
  color: var(--text-light);
}

/* 轮廓按钮 */
.btn-outline {
  background: rgba(255, 255, 255, 0.05);
  color: var(--midnight-purple);
  border: 1px solid var(--glass-border);
}

.btn-outline:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.18);
  color: var(--coral-pink);
}

/* 文本按钮 */
.btn-text {
  background: transparent;
  color: var(--midnight-purple);
  padding: 8px 16px;
}

.btn-text:hover:not(:disabled) {
  color: var(--coral-pink);
  background: rgba(255, 126, 138, 0.1);
}

/* 危险按钮 */
.btn-danger {
  background: #ff4757;
  color: var(--text-light);
}

.btn-danger:hover:not(:disabled) {
  background: #ff3838;
}

/* 块级按钮 */
.btn-block {
  width: 100%;
}

/* 加载状态 */
.btn-loading {
  pointer-events: none;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: currentColor;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>


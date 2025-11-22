<template>
  <span :class="['badge', `badge-${variant}`, `badge-${size}`]">
    <slot></slot>
  </span>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'default',
    validator: (value) =>
      ['default', 'primary', 'secondary', 'success', 'warning', 'danger', 'info'].includes(value),
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value),
  },
});
</script>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  font-weight: 500;
  border-radius: 12px;
  white-space: nowrap;
}

.badge-small {
  padding: 2px 8px;
  font-size: 11px;
}

.badge-medium {
  padding: 4px 12px;
  font-size: 12px;
}

.badge-large {
  padding: 6px 16px;
  font-size: 14px;
}

.badge-default {
  background: var(--light-purple);
  color: var(--midnight-purple);
}

.badge-primary {
  background: linear-gradient(135deg, var(--coral-pink), var(--stardust-purple));
  color: var(--text-light);
}

.badge-success {
  background: #2ed573;
  color: var(--text-light);
}

.badge-warning {
  background: #ffa502;
  color: var(--text-light);
}

.badge-danger {
  background: #ff4757;
  color: var(--text-light);
}

.badge-info {
  background: var(--starry-blue);
  color: var(--midnight-purple);
}

.badge-secondary {
  background: var(--light-purple);
  color: var(--midnight-purple);
  opacity: 0.8;
}
</style>


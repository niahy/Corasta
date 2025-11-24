<template>
  <article :class="['card', `card-${size}`, { 'card-hover': hover, 'card-gradient': gradient }]">
    <div v-if="$slots.header" class="card-header">
      <slot name="header"></slot>
    </div>
    <div class="card-body">
      <slot></slot>
    </div>
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer"></slot>
    </div>
  </article>
</template>

<script setup>
defineProps({
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large', 'xl'].includes(value),
  },
  hover: {
    type: Boolean,
    default: false,
  },
  gradient: {
    type: Boolean,
    default: false,
  },
});
</script>

<style scoped>
.card {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  box-shadow: var(--glass-shadow);
  overflow: hidden;
  transition: all 0.3s;
  border: 1px solid var(--glass-border-soft);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  position: relative;
}

.card::after {
  content: '';
  position: absolute;
  inset: 1px;
  border-radius: inherit;
  border-top: 1px solid var(--glass-highlight);
  opacity: 0.6;
  pointer-events: none;
}

.card-hover:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 36px rgba(93, 79, 146, 0.35);
}

.card-gradient {
  background: linear-gradient(120deg, rgba(255, 126, 138, 0.12), rgba(166, 140, 224, 0.12));
}

.card-small {
  padding: 16px;
}

.card-medium {
  padding: 24px;
}

.card-large {
  padding: 32px;
}

.card-xl {
  padding: 36px;
  line-height: 1.9;
}

.card-header {
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 16px;
}

.card-body {
  flex: 1;
}

.card-footer {
  padding-top: 16px;
  border-top: 1px solid var(--border);
  margin-top: 16px;
}
</style>


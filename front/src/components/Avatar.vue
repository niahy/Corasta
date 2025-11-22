<template>
  <div
    :class="['avatar', `avatar-${size}`, { 'avatar-clickable': clickable }]"
    :style="avatarStyle"
    @click="handleClick"
  >
    <img v-if="src" :src="src" :alt="alt" />
    <span v-else class="avatar-placeholder">{{ placeholderText }}</span>
    <span v-if="online" class="avatar-online"></span>
  </div>
</template>

<script setup>
import { computed, defineProps, defineEmits } from 'vue';

const props = defineProps({
  src: {
    type: String,
    default: '',
  },
  alt: {
    type: String,
    default: '',
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large', 'xl'].includes(value),
  },
  name: {
    type: String,
    default: '',
  },
  online: {
    type: Boolean,
    default: false,
  },
  clickable: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['click']);

const placeholderText = computed(() => {
  if (props.name) {
    return props.name.charAt(0).toUpperCase();
  }
  return '?';
});

const avatarStyle = computed(() => {
  if (!props.src) {
    return {
      background: 'linear-gradient(135deg, var(--coral-pink), var(--stardust-purple))',
    };
  }
  return {};
});

const handleClick = () => {
  if (props.clickable) {
    emit('click');
  }
};
</script>

<style scoped>
.avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-clickable {
  cursor: pointer;
  transition: transform 0.2s;
}

.avatar-clickable:hover {
  transform: scale(1.05);
}

.avatar-small {
  width: 32px;
  height: 32px;
  font-size: 12px;
}

.avatar-medium {
  width: 40px;
  height: 40px;
  font-size: 16px;
}

.avatar-large {
  width: 64px;
  height: 64px;
  font-size: 24px;
}

.avatar-xl {
  width: 96px;
  height: 96px;
  font-size: 36px;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  color: var(--text-light);
  font-weight: 600;
}

.avatar-online {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background: #2ed573;
  border: 2px solid var(--card-bg);
  border-radius: 50%;
}

.avatar-small .avatar-online {
  width: 8px;
  height: 8px;
  border-width: 1.5px;
}

.avatar-large .avatar-online,
.avatar-xl .avatar-online {
  width: 16px;
  height: 16px;
  border-width: 3px;
}
</style>


<template>
  <div class="input-wrapper">
    <label v-if="label" :for="inputId" class="input-label">
      {{ label }}
      <span v-if="required" class="required">*</span>
    </label>
    <div class="input-container" :class="{ 'input-error': error, 'input-disabled': disabled }">
      <span v-if="$slots.prefix" class="input-prefix">
        <slot name="prefix"></slot>
      </span>
      <input
        :id="inputId"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :maxlength="maxlength"
        class="input-field"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
      />
      <span v-if="$slots.suffix" class="input-suffix">
        <slot name="suffix"></slot>
      </span>
    </div>
    <div v-if="error" class="input-error-message">{{ error }}</div>
    <div v-if="hint && !error" class="input-hint">{{ hint }}</div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue';

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: '',
  },
  type: {
    type: String,
    default: 'text',
  },
  label: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: '',
  },
  error: {
    type: String,
    default: '',
  },
  hint: {
    type: String,
    default: '',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  readonly: {
    type: Boolean,
    default: false,
  },
  required: {
    type: Boolean,
    default: false,
  },
  maxlength: {
    type: Number,
    default: undefined,
  },
});

const emit = defineEmits(['update:modelValue', 'blur', 'focus']);

const inputId = computed(() => `input-${Math.random().toString(36).substr(2, 9)}`);

const handleInput = (event) => {
  emit('update:modelValue', event.target.value);
};

const handleBlur = (event) => {
  emit('blur', event);
};

const handleFocus = (event) => {
  emit('focus', event);
};
</script>

<style scoped>
.input-wrapper {
  width: 100%;
}

.input-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-dark);
}

.required {
  color: var(--coral-pink);
  margin-left: 4px;
}

.input-container {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid var(--border);
  border-radius: var(--radius-sm);
  transition: all 0.2s;
}

.input-container:focus-within {
  border-color: var(--coral-pink);
  box-shadow: 0 0 0 3px rgba(255, 126, 138, 0.1);
}

.input-error {
  border-color: #ff4757;
}

.input-error:focus-within {
  border-color: #ff4757;
  box-shadow: 0 0 0 3px rgba(255, 71, 87, 0.1);
}

.input-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.input-field {
  flex: 1;
  padding: 12px 16px;
  font-size: 15px;
  color: var(--text-dark);
  background: transparent;
  border: none;
  outline: none;
  width: 100%;
}

.input-field::placeholder {
  color: #999;
}

.input-field:disabled {
  cursor: not-allowed;
}

.input-prefix,
.input-suffix {
  display: flex;
  align-items: center;
  padding: 0 12px;
  color: var(--midnight-purple);
}

.input-error-message {
  margin-top: 6px;
  font-size: 13px;
  color: #ff4757;
}

.input-hint {
  margin-top: 6px;
  font-size: 13px;
  color: #666;
}
</style>


<template>
  <Teleport to="body">
    <Transition name="toast">
      <div v-if="visible" class="message-toast" :class="type">
        <span class="toast-icon">{{ icon }}</span>
        <span class="toast-message">{{ message }}</span>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  visible: Boolean,
  message: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'success', // success, error, warning, info
    validator: (value) => ['success', 'error', 'warning', 'info'].includes(value)
  },
  icon: {
    type: String,
    default: '✓'
  },
  duration: {
    type: Number,
    default: 2000
  }
})

const emit = defineEmits(['update:visible'])

// 自动关闭
let timer = null

const startTimer = () => {
  if (props.duration > 0) {
    timer = setTimeout(() => {
      emit('update:visible', false)
    }, props.duration)
  }
}

const clearTimer = () => {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

// 监听 visible 变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    startTimer()
  } else {
    clearTimer()
  }
})

onUnmounted(() => {
  clearTimer()
})
</script>

<script>
import { watch, onUnmounted } from 'vue'
</script>

<style scoped>
.message-toast {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  padding: 14px 24px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 500;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 10001;
  min-width: 200px;
  justify-content: center;
}

.message-toast.success {
  background: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #a5d6a7;
}

.message-toast.error {
  background: #ffebee;
  color: #c62828;
  border: 1px solid #ef9a9a;
}

.message-toast.warning {
  background: #fff8e1;
  color: #f57c00;
  border: 1px solid #ffe082;
}

.message-toast.info {
  background: #e3f2fd;
  color: #1565c0;
  border: 1px solid #90caf9;
}

.toast-icon {
  font-size: 20px;
}

.toast-message {
  white-space: nowrap;
}

/* 动画 */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-10px);
}
</style>

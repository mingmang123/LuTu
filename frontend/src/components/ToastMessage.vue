<template>
  <TransitionGroup name="toast" tag="div" class="toast-container">
    <div
      v-for="toast in toasts"
      :key="toast.id"
      class="toast"
      :class="toast.type"
    >
      <span class="toast-icon">{{ getIcon(toast.type) }}</span>
      <span class="toast-message">{{ toast.message }}</span>
      <button class="toast-close" @click="removeToast(toast.id)">×</button>
    </div>
  </TransitionGroup>
</template>

<script setup>
import { ref } from 'vue'

const toasts = ref([])
let toastId = 0

const getIcon = (type) => {
  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  }
  return icons[type] || icons.info
}

const addToast = (message, type = 'info', duration = 3000) => {
  const id = ++toastId
  toasts.value.push({ id, message, type })
  
  if (duration > 0) {
    setTimeout(() => removeToast(id), duration)
  }
}

const removeToast = (id) => {
  const index = toasts.value.findIndex(t => t.id === id)
  if (index > -1) {
    toasts.value.splice(index, 1)
  }
}

// 暴露方法给外部使用
defineExpose({
  success: (message, duration) => addToast(message, 'success', duration),
  error: (message, duration) => addToast(message, 'error', duration),
  warning: (message, duration) => addToast(message, 'warning', duration),
  info: (message, duration) => addToast(message, 'info', duration),
  remove: removeToast
})
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 80px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.toast {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  min-width: 280px;
  max-width: 400px;
  animation: slideIn 0.3s ease;
}

.toast.success {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
}

.toast.error {
  background: linear-gradient(135deg, #ef5350 0%, #e53935 100%);
  color: white;
}

.toast.warning {
  background: linear-gradient(135deg, #ffa726 0%, #ff9800 100%);
  color: white;
}

.toast.info {
  background: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
  color: white;
}

.toast-icon {
  font-size: 18px;
  font-weight: bold;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

.toast-message {
  flex: 1;
  font-size: 14px;
  line-height: 1.5;
}

.toast-close {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.8;
  transition: opacity 0.3s;
}

.toast-close:hover {
  opacity: 1;
}

/* 动画 */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@media (max-width: 768px) {
  .toast-container {
    left: 20px;
    right: 20px;
  }
  
  .toast {
    min-width: auto;
    max-width: none;
  }
}
</style>

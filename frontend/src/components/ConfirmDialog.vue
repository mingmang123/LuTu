<template>
  <Teleport to="body">
    <div v-if="visible" class="confirm-dialog-overlay" @click="handleCancel">
      <div class="confirm-dialog-content" @click.stop>
        <div class="confirm-dialog-icon">{{ icon }}</div>
        <h3 class="confirm-dialog-title">{{ title }}</h3>
        <p class="confirm-dialog-message" v-html="message"></p>
        <div class="confirm-dialog-actions">
          <button class="btn-cancel" @click="handleCancel">{{ cancelText }}</button>
          <button :class="['btn-confirm', type]" @click="handleConfirm">{{ confirmText }}</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  visible: Boolean,
  title: {
    type: String,
    default: '确认删除'
  },
  message: {
    type: String,
    default: '确定要删除吗？'
  },
  icon: {
    type: String,
    default: '🗑️'
  },
  type: {
    type: String,
    default: 'danger', // danger, warning, info
    validator: (value) => ['danger', 'warning', 'info'].includes(value)
  },
  confirmText: {
    type: String,
    default: '删除'
  },
  cancelText: {
    type: String,
    default: '取消'
  }
})

const emit = defineEmits(['confirm', 'cancel', 'update:visible'])

const handleConfirm = () => {
  emit('confirm')
  emit('update:visible', false)
}

const handleCancel = () => {
  emit('cancel')
  emit('update:visible', false)
}
</script>

<style scoped>
.confirm-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  animation: fadeIn 0.2s ease;
}

.confirm-dialog-content {
  background: white;
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  max-width: 400px;
  width: 90%;
  animation: slideUp 0.3s ease;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.confirm-dialog-icon {
  font-size: 64px;
  margin-bottom: 20px;
  animation: bounce 0.5s ease;
}

.confirm-dialog-title {
  color: #333;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 16px 0;
}

.confirm-dialog-message {
  color: #666;
  font-size: 16px;
  line-height: 1.6;
  margin: 0 0 32px 0;
}

.confirm-dialog-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.btn-cancel {
  padding: 12px 32px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  background: #e0e0e0;
  transform: translateY(-2px);
}

.btn-confirm {
  padding: 12px 32px;
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-confirm.danger {
  background: linear-gradient(135deg, #ef5350 0%, #e53935 100%);
}

.btn-confirm.danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(229, 57, 53, 0.3);
}

.btn-confirm.warning {
  background: linear-gradient(135deg, #ffa726 0%, #ff9800 100%);
}

.btn-confirm.warning:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
}

.btn-confirm.info {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
}

.btn-confirm.info:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}
</style>

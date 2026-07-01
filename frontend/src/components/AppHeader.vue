<template>
  <header class="header">
    <div class="header-left">
      <button v-if="showBack"
              class="back-btn"
              @click="goBack"
              title="返回上一页">
        <svg class="back-icon"
             viewBox="0 0 24 24"
             fill="none"
             xmlns="http://www.w3.org/2000/svg">
          <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"
                fill="currentColor" />
        </svg>
      </button>
      <div class="brand"
           @click="goHome">
        <span class="brand-icon">🌿</span>
        <span class="brand-text">途记</span>
      </div>
    </div>

    <div class="user-info">
      <span @click="goToProfile"
            class="username">{{ userStore.userInfo.username || '用户' }}</span>
      <button @click="handleLogout"
              class="logout-btn">退出</button>
    </div>
  </header>
</template>

<script setup>
import {useRoute, useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {computed} from 'vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 在首页和 dashboard 页面不显示返回按钮
const showBack = computed(() => {
  return route.path !== '/' && route.path !== '/dashboard' && route.path !== '/platform'
})

const goBack = () => {
  router.back()
}

const goHome = () => {
  router.push('/')
}

const goToProfile = () => {
  router.push('/user-profile')
}

const handleLogout = () => {
  userStore.clearUser()
  router.push('/login')
}
</script>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
  padding: 12px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  z-index: 100;
  box-sizing: border-box;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 4px;
}

.back-btn:hover {
  background: #e8e8e8;
  transform: translateX(-2px);
}

.back-icon {
  width: 20px;
  height: 20px;
  color: #666;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s ease;
}

.brand:hover {
  background: rgba(46, 125, 50, 0.08);
}

.brand-icon {
  font-size: 24px;
}

.brand-text {
  color: #2e7d32;
  font-size: 20px;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  color: #558b2f;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.username:hover {
  background: rgba(102, 187, 106, 0.1);
}

.logout-btn {
  padding: 8px 18px;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.4);
}

/* 响应式设计 */
@media (max-width: 640px) {
  .header {
    padding: 10px 16px;
  }

  .back-btn {
    width: 32px;
    height: 32px;
  }

  .back-icon {
    width: 18px;
    height: 18px;
  }

  .brand-icon {
    font-size: 20px;
  }

  .brand-text {
    font-size: 18px;
  }

  .username {
    font-size: 13px;
    padding: 6px 12px;
  }

  .logout-btn {
    padding: 6px 14px;
    font-size: 12px;
  }
}
</style>

<template>
  <div class="share-page">
    <!-- 顶部导航 -->
    <header class="share-header">
      <div class="header-content">
        <div class="brand" @click="goHome">
          <span class="brand-icon">🌿</span>
          <span class="brand-text">途记</span>
        </div>
        <div class="header-actions">
          <button v-if="!isLoggedIn" class="login-btn" @click="goToLogin">
            登录 / 注册
          </button>
          <button v-else class="home-btn" @click="goHome">
            进入首页
          </button>
        </div>
      </div>
    </header>

    <div class="container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载行程...</p>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="error-state">
        <div class="error-icon">😕</div>
        <h3>行程分享不存在</h3>
        <p>{{ error }}</p>
        <button class="btn-primary" @click="goHome">返回首页</button>
      </div>

      <!-- 分享内容 -->
      <template v-else>
        <!-- 行程标题卡片 -->
        <div class="plan-header-card">
          <div class="share-badge">
            <svg class="share-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92s2.92-1.31 2.92-2.92-1.31-2.92-2.92-2.92z" fill="currentColor"/>
            </svg>
            行程分享
          </div>
          <div class="plan-header-content">
            <div class="plan-title-section">
              <h2>{{ planTitle }}</h2>
              <div class="plan-dates">
                <span class="date-badge">
                  <span class="date-icon">📅</span>
                  <span>{{ dateRange }}</span>
                </span>
                <span class="duration-badge" v-if="duration">
                  {{ duration }}
                </span>
              </div>
            </div>
            <div class="plan-stats">
              <div class="stat-item">
                <span class="stat-value">{{ planItems.length }}</span>
                <span class="stat-label">环节</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">¥{{ totalAmount }}</span>
                <span class="stat-label">总花费</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 时间线视图 -->
        <div class="timeline-section">
          <h3 class="section-title">
            <span class="title-icon">🗓️</span>
            行程安排
          </h3>

          <div v-if="planItems.length > 0" class="timeline">
            <div v-for="(item, index) in planItems"
                 :key="item.id"
                 class="timeline-item"
                 :class="{ 'last': index === planItems.length - 1 }">
              <div class="timeline-left">
                <div class="timeline-dot" :class="getTypeClass(item.itemType)">
                  <span class="dot-icon">{{ getTypeIcon(item.itemType) }}</span>
                </div>
                <div class="timeline-line" v-if="index !== planItems.length - 1"></div>
              </div>

              <div class="timeline-content">
                <div class="content-card">
                  <div class="card-header">
                    <div class="header-main">
                      <h4 class="item-title">
                        <template v-if="item.itemType === 1">
                          {{ item.fromLocation }} <span class="arrow">→</span> {{ item.toLocation }}
                        </template>
                        <template v-else>
                          {{ item.toLocation || item.fromLocation || '未设置地点' }}
                        </template>
                      </h4>
                      <span class="item-type-tag" :class="getTypeClass(item.itemType)">
                        {{ getTypeLabel(item.itemType) }}
                      </span>
                    </div>
                    <div class="header-actions">
                      <span class="item-amount">¥{{ item.amount || 0 }}</span>
                    </div>
                  </div>

                  <div class="card-summary">
                    <div class="time-info">
                      <span class="time-icon">⏰</span>
                      <span>{{ formatItemTime(item.startTime, item.endTime) }}</span>
                    </div>
                    <div v-if="item.itemType === 1 && item.transportType && item.transportType !== 'null'"
                         class="transport-info">
                      <span class="transport-icon">🚗</span>
                      <span>{{ item.transportType }}</span>
                    </div>
                  </div>

                  <div class="card-details">
                    <div class="details-grid">
                      <div class="detail-item">
                        <span class="detail-label">开始时间</span>
                        <span class="detail-value">{{ formatFullDateTime(item.startTime) }}</span>
                      </div>
                      <div class="detail-item">
                        <span class="detail-label">结束时间</span>
                        <span class="detail-value">{{ formatFullDateTime(item.endTime) }}</span>
                      </div>
                      <div class="detail-item" v-if="item.itemType === 1 && item.transportType && item.transportType !== 'null'">
                        <span class="detail-label">交通工具</span>
                        <span class="detail-value">{{ item.transportType }}</span>
                      </div>
                      <div class="detail-item">
                        <span class="detail-label">花费金额</span>
                        <span class="detail-value amount">¥{{ item.amount || 0 }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="empty-state">
            <div class="empty-icon">🗺️</div>
            <h4>暂无行程环节</h4>
            <p>该行程暂时没有安排任何环节</p>
          </div>
        </div>

        <!-- 底部提示 -->
        <div class="share-footer">
          <div class="footer-content">
            <p class="footer-text">💡 想创建自己的行程计划？</p>
            <button v-if="!isLoggedIn" class="btn-primary" @click="goToLogin">
              立即注册 / 登录
            </button>
            <button v-else class="btn-primary" @click="goHome">
              去创建行程
            </button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {queryByShareCode} from '@/api/travelPlanShare'
import {useUserStore} from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const error = ref('')
const planItems = ref([])
const planTitle = ref('分享的行程')

const isLoggedIn = computed(() => userStore.isLoggedIn)

// 计算日期范围
const dateRange = computed(() => {
  if (planItems.value.length === 0) return ''
  const dates = planItems.value.map(item => new Date(item.startTime)).filter(d => !isNaN(d))
  if (dates.length === 0) return ''
  const minDate = new Date(Math.min(...dates))
  const maxDate = new Date(Math.max(...dates))
  return `${formatDate(minDate)} - ${formatDate(maxDate)}`
})

// 计算行程天数
const duration = computed(() => {
  if (planItems.value.length === 0) return ''
  const dates = planItems.value.map(item => new Date(item.startTime)).filter(d => !isNaN(d))
  if (dates.length === 0) return ''
  const minDate = new Date(Math.min(...dates))
  const maxDate = new Date(Math.max(...dates))
  const days = Math.ceil((maxDate - minDate) / (1000 * 60 * 60 * 24)) + 1
  return `${days} 天`
})

// 计算总花费
const totalAmount = computed(() => {
  return planItems.value.reduce((sum, item) => sum + (parseFloat(item.amount) || 0), 0).toFixed(2)
})

onMounted(() => {
  loadShareData()
})

const loadShareData = async () => {
  const shareCode = route.params.code || route.query.shareCode
  if (!shareCode) {
    error.value = '分享码无效'
    loading.value = false
    return
  }

  try {
    const res = await queryByShareCode(shareCode)
    if (res.code === 200) {
      planItems.value = res.data || []
    } else {
      error.value = res.msg || '分享内容不存在或已过期'
    }
  } catch (err) {
    console.error('加载分享内容失败:', err)
    error.value = '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const goHome = () => {
  router.push('/dashboard')
}

const goToLogin = () => {
  router.push('/login')
}

const formatDate = (date) => {
  if (!date) return ''
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

const formatFullDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatItemTime = (start, end) => {
  if (!start || !end) return ''
  const startDate = new Date(start)
  const endDate = new Date(end)
  const format = (d) => `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  return `${format(startDate)} - ${format(endDate)}`
}

const getTypeIcon = (type) => {
  const iconMap = { 1: '🚗', 2: '🎡', 3: '🏨' }
  return iconMap[type] || '📍'
}

const getTypeLabel = (type) => {
  const labelMap = { 1: '交通', 2: '游玩', 3: '住宿' }
  return labelMap[type] || '其他'
}

const getTypeClass = (type) => {
  const classMap = { 1: 'transport', 2: 'play', 3: 'stay' }
  return classMap[type] || ''
}
</script>

<style scoped>
.share-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 50%, #a5d6a7 100%);
}

/* 顶部导航 */
.share-header {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
  padding: 12px 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.login-btn,
.home-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.login-btn {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.4);
}

.home-btn {
  background: #f5f5f5;
  color: #666;
}

.home-btn:hover {
  background: #e8e8e8;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 80px 20px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid #e8f5e9;
  border-top-color: #4caf50;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 20px;
}

.loading-state p {
  color: #666;
}

/* 错误状态 */
.error-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(46, 125, 50, 0.1);
}

.error-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.error-state h3 {
  color: #2e7d32;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 8px;
}

.error-state p {
  color: #78909c;
  margin-bottom: 24px;
}

/* 行程标题卡片 */
.plan-header-card {
  background: white;
  border-radius: 24px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(46, 125, 50, 0.1);
  position: relative;
}

.share-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
  color: white;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.share-icon {
  width: 16px;
  height: 16px;
}

.plan-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.plan-title-section h2 {
  color: #1b5e20;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
}

.plan-dates {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.date-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #e8f5e9;
  border-radius: 12px;
  color: #2e7d32;
  font-size: 14px;
  font-weight: 500;
}

.date-icon {
  font-size: 16px;
}

.duration-badge {
  padding: 10px 16px;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
}

.plan-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  text-align: center;
  padding: 16px 24px;
  background: #f8f9fa;
  border-radius: 16px;
  min-width: 100px;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #4caf50;
  line-height: 1;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #78909c;
  margin-top: 4px;
}

/* 时间线区域 */
.timeline-section {
  background: white;
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(46, 125, 50, 0.1);
}

.section-title {
  color: #2e7d32;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 28px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 24px;
}

/* 时间线 */
.timeline {
  display: flex;
  flex-direction: column;
}

.timeline-item {
  display: flex;
  gap: 20px;
  position: relative;
}

.timeline-item.last {
  padding-bottom: 0;
}

.timeline-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.timeline-dot {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.timeline-dot.transport {
  background: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
}

.timeline-dot.play {
  background: linear-gradient(135deg, #ffa726 0%, #ff9800 100%);
}

.timeline-dot.stay {
  background: linear-gradient(135deg, #ab47bc 0%, #9c27b0 100%);
}

.dot-icon {
  filter: grayscale(1) brightness(2);
}

.timeline-line {
  width: 3px;
  flex: 1;
  background: linear-gradient(to bottom, #e8f5e9, #c8e6c9);
  margin: 8px 0;
  border-radius: 2px;
}

.timeline-content {
  flex: 1;
  padding-bottom: 28px;
}

.timeline-item.last .timeline-content {
  padding-bottom: 0;
}

.content-card {
  background: #f8f9fa;
  border-radius: 16px;
  padding: 20px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.content-card:hover {
  border-color: #c8e6c9;
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.item-title {
  color: #1b5e20;
  font-size: 17px;
  font-weight: 600;
  margin: 0;
}

.arrow {
  color: #4caf50;
  margin: 0 4px;
}

.item-type-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.item-type-tag.transport {
  background: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
}

.item-type-tag.play {
  background: linear-gradient(135deg, #ffa726 0%, #ff9800 100%);
}

.item-type-tag.stay {
  background: linear-gradient(135deg, #ab47bc 0%, #9c27b0 100%);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-amount {
  color: #4caf50;
  font-size: 18px;
  font-weight: 700;
}

.card-summary {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.time-info,
.transport-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #78909c;
  font-size: 14px;
}

.time-icon,
.transport-icon {
  font-size: 14px;
}

.card-details {
  padding-top: 16px;
  border-top: 1px dashed #e0e0e0;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  color: #78909c;
  font-size: 12px;
}

.detail-value {
  color: #37474f;
  font-size: 14px;
  font-weight: 500;
}

.detail-value.amount {
  color: #4caf50;
  font-weight: 600;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state h4 {
  color: #2e7d32;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.empty-state p {
  color: #78909c;
  font-size: 14px;
}

/* 底部提示 */
.share-footer {
  margin-top: 32px;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.1);
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.footer-text {
  color: #558b2f;
  font-size: 15px;
  font-weight: 500;
  margin: 0;
}

.btn-primary {
  padding: 12px 28px;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(76, 175, 80, 0.4);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .share-header {
    padding: 10px 16px;
  }

  .brand-icon {
    font-size: 20px;
  }

  .brand-text {
    font-size: 18px;
  }

  .container {
    padding: 20px 16px;
  }

  .plan-header-card {
    padding: 24px 20px;
  }

  .share-badge {
    position: static;
    display: inline-flex;
    margin-bottom: 12px;
  }

  .plan-header-content {
    flex-direction: column;
    gap: 20px;
  }

  .plan-title-section h2 {
    font-size: 22px;
  }

  .plan-stats {
    width: 100%;
    justify-content: space-around;
  }

  .timeline-section {
    padding: 24px 20px;
  }

  .timeline-content {
    padding-bottom: 16px;
  }

  .content-card {
    padding: 16px;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .details-grid {
    grid-template-columns: 1fr;
  }

  .footer-content {
    flex-direction: column;
    text-align: center;
  }

  .btn-primary {
    width: 100%;
  }
}
</style>

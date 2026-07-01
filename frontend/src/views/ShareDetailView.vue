<template>
  <div class="share-detail-page">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="nav-brand"
           @click="goHome">
        <span class="logo">🌿</span>
        <span class="brand-text">途记</span>
      </div>
      <div class="nav-actions">
        <template v-if="userStore.isLoggedIn">
          <div class="user-info"
               @click="goToProfile">
            <div class="user-avatar-wrapper">
              <img v-if="userStore.userInfo?.avatar"
                   :src="userStore.userInfo.avatar"
                   alt="avatar"
                   class="user-avatar">
              <div v-else
                   class="user-avatar-default">👤</div>
            </div>
            <span class="user-name">{{ userStore.userInfo?.username || '用户' }}</span>
          </div>
        </template>
        <template v-else>
          <button class="btn-login"
                  @click="goToLogin">登录</button>
          <button class="btn-register"
                  @click="goToRegister">免费注册</button>
        </template>
      </div>
    </nav>

    <!-- 加载状态 -->
    <div v-if="loading"
         class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error"
         class="error-container">
      <div class="error-icon">😕</div>
      <h2>分享不存在或已失效</h2>
      <p>该分享可能已被删除或分享码已过期</p>
      <button class="btn-primary"
              @click="goHome">返回首页</button>
    </div>

    <!-- 分享内容 -->
    <template v-else-if="share">
      <!-- 封面区域 -->
      <div class="share-hero"
           :style="{ backgroundImage: `url(${share.coverImage || defaultCover})` }">
        <div class="hero-overlay"></div>
        <div class="hero-content">
          <div class="share-tags"
               v-if="share.tags && share.tags.length > 0">
            <span v-for="tag in share.tags"
                  :key="tag"
                  class="tag">{{ tag }}</span>
          </div>
          <h1 class="share-title">{{ share.title }}</h1>
          <p class="share-description">{{ share.description }}</p>
          <div class="share-meta">
            <div class="author-info">
              <img :src="share.author?.avatar || defaultAvatar"
                   :alt="share.author?.username"
                   class="author-avatar">
              <div class="author-detail">
                <span class="author-name">{{ share.author?.username || '匿名用户' }}</span>
                <span class="publish-time">{{ formatDate(share.createTime) }} 发布</span>
              </div>
            </div>
            <div class="share-stats">
              <span class="stat view-stat">
                <i class="icon-eye">👁</i>
                <span class="view-count">{{ formatNumber(share.viewCount || 0) }}</span>
                <span class="view-text">浏览</span>
              </span>
              <button class="like-btn"
                      :class="{ liked: hasLiked }"
                      @click="handleLike">
                <span class="like-icon">{{ hasLiked ? '❤️' : '🤍' }}</span>
                <span class="like-count">{{ formatNumber(share.likeCount || 0) }}</span>
                <span class="like-text">{{ hasLiked ? '已点赞' : '点赞' }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 行程详情 -->
      <div class="container">
        <div class="content-layout">
          <!-- 左侧：行程环节 -->
          <div class="main-content">
            <div class="section-header">
              <h2>行程详情</h2>
              <div class="trip-info"
                   v-if="share.travelPlan">
                <span class="info-item">
                  <i class="icon-location">📍</i>
                  {{ share.travelPlan.destination || '暂无目的地' }}
                </span>
                <span class="info-item"
                      v-if="share.travelPlan.startDate && share.travelPlan.endDate">
                  <i class="icon-calendar">📅</i>
                  {{ formatDateRange(share.travelPlan.startDate, share.travelPlan.endDate) }}
                </span>
                <span class="info-item"
                      v-if="share.travelPlan.totalDays">
                  <i class="icon-days">⏱</i>
                  {{ share.travelPlan.totalDays }} 天
                </span>
              </div>
            </div>

            <!-- 行程时间轴 -->
            <div class="timeline">
              <div v-for="(item, index) in sortedItems"
                   :key="item.id"
                   class="timeline-item"
                   :class="getItemTypeClass(item.itemType)">
                <div class="timeline-marker">
                  <div class="marker-icon">{{ getItemTypeIcon(item.itemType) }}</div>
                  <div class="marker-line"
                       v-if="index < sortedItems.length - 1"></div>
                </div>
                <div class="timeline-content">
                  <div class="item-card">
                    <div class="item-card-header">
                      <div class="item-main-info">
                        <h3 class="item-title">{{ item.toLocation || item.fromLocation || '未知地点' }}</h3>
                        <span class="item-type-tag">{{ getItemTypeName(item.itemType) }}</span>
                      </div>
                      <span class="item-amount"
                            v-if="item.amount">¥{{ item.amount }}</span>
                    </div>
                    <div class="item-card-body">
                      <div class="item-time-info">
                        <span class="time-icon">⏰</span>
                        <span class="time-range">{{ formatDateTime(item.startTime) }} - {{ formatDateTime(item.endTime) }}</span>
                      </div>
                      <p class="item-route"
                         v-if="item.fromLocation && item.toLocation && item.fromLocation !== item.toLocation">
                        <span class="route-icon">📍</span>
                        从 {{ item.fromLocation }} 前往 {{ item.toLocation }}
                      </p>

                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 无行程环节提示 -->
            <div v-if="!sortedItems || sortedItems.length === 0"
                 class="empty-timeline">
              <div class="empty-icon">📝</div>
              <p>暂无行程环节详情</p>
            </div>
          </div>

          <!-- 右侧：作者信息 -->
          <div class="sidebar">
            <div class="author-card">
              <img :src="share.author?.avatar || defaultAvatar"
                   :alt="share.author?.username"
                   class="author-avatar-large">
              <h3 class="author-name-large">{{ share.author?.username || '匿名用户' }}</h3>
              <p class="author-bio">{{ share.author?.bio || '热爱旅行，分享美好' }}</p>
            </div>

            <div class="action-card">
              <template v-if="!userStore.isLoggedIn">
                <h4>喜欢这个行程？</h4>
                <p>登录后可以收藏、克隆和创建自己的行程</p>
                <button class="btn-primary"
                        @click="goToLogin">立即登录</button>
              </template>
              <template v-else>
                <h4>喜欢这个行程？</h4>
                <p>点击下方按钮收藏或克隆这个行程</p>
                <button class="btn-primary"
                        @click="handleCollect">
                  <span>{{ hasCollected ? '⭐ 已收藏' : '☆ 收藏行程' }}</span>
                </button>
                <button class="btn-secondary"
                        @click="handleClone">
                  <span>📋 克隆到我的行程</span>
                </button>
              </template>
            </div>

            <!-- 相关推荐 -->
            <div class="related-shares"
                 v-if="relatedShares.length > 0">
              <h4>相关推荐</h4>
              <div class="related-list">
                <div v-for="related in relatedShares"
                     :key="related.id"
                     class="related-item"
                     @click="viewRelated(related)">
                  <img :src="related.coverImage || getDefaultCover(related)"
                       :alt="related.title">
                  <div class="related-info">
                    <h5>{{ related.title }}</h5>
                    <span>{{ formatNumber(related.viewCount || 0) }} 浏览</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 图片预览 -->
    <div v-if="previewImageUrl"
         class="image-preview"
         @click="closePreview">
      <img :src="previewImageUrl"
           alt="预览">
      <span class="close-preview">×</span>
    </div>

    <!-- Toast 消息提示 -->
    <MessageToast v-model:visible="showToast"
                  :message="toastMessage"
                  :type="toastType"
                  :icon="toastIcon" />
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {checkLikeStatus, getHotShares, getShareDetail, likeShare} from '@/api/share'
import {useUserStore} from '@/stores/user'
import {checkFavorite, clonePlan, toggleFavorite} from '@/api/favorite'
import MessageToast from '@/components/MessageToast.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const error = ref(false)
const share = ref(null)
const relatedShares = ref([])
const hasLiked = ref(false)
const hasCollected = ref(false)
const previewImageUrl = ref(null)

// Toast 提示相关
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')
const toastIcon = ref('✓')

const showMessage = (message, type = 'success', icon = '✓') => {
  toastMessage.value = message
  toastType.value = type
  toastIcon.value = icon
  showToast.value = true
}

const defaultCover = 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=1200'
const defaultAvatar = 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100'

// 排序后的行程环节
const sortedItems = computed(() => {
  if (!share.value?.items || !Array.isArray(share.value.items)) return []
  return [...share.value.items].sort((a, b) => {
    return new Date(a.startTime) - new Date(b.startTime)
  })
})

onMounted(() => {
  loadShareDetail()
  loadRelatedShares()
  checkFavoriteStatus()
  // 只有登录用户才检查点赞状态
  if (userStore.isLoggedIn) {
    checkLikeStatusOnLoad()
  }
})

const loadShareDetail = async () => {
  const shareCode = route.params.code
  if (!shareCode) {
    error.value = true
    loading.value = false
    return
  }

  try {
    const res = await getShareDetail(shareCode)
    if (res.code === 200 && res.data) {
      share.value = res.data
      // 解析标签 - 确保是数组
      if (typeof share.value.tags === 'string') {
        share.value.tags = share.value.tags.split(',').filter(t => t.trim())
      } else if (!Array.isArray(share.value.tags)) {
        share.value.tags = []
      }
      // 解析图片
      if (share.value.items && Array.isArray(share.value.items)) {
        share.value.items.forEach(item => {
          if (typeof item.images === 'string') {
            try {
              item.images = JSON.parse(item.images)
            } catch {
              item.images = []
            }
          } else if (!Array.isArray(item.images)) {
            item.images = []
          }
        })
      } else {
        share.value.items = []
      }
    } else {
      error.value = true
    }
  } catch (err) {
    console.error('加载分享详情失败:', err)
    error.value = true
  } finally {
    loading.value = false
  }
}

const loadRelatedShares = async () => {
  try {
    const res = await getHotShares(1, 4)
    if (res.code === 200 && res.data) {
      // 处理分页数据
      const shares = res.data.records || res.data || []
      // 过滤掉当前分享
      relatedShares.value = shares.filter(s => s.shareCode !== route.params.code).slice(0, 3)
    }
  } catch (err) {
    console.error('加载相关分享失败:', err)
  }
}

const checkLikeStatusOnLoad = async () => {
  const shareCode = route.params.code
  if (!shareCode) return
  try {
    const res = await checkLikeStatus(shareCode)
    if (res.code === 200) {
      hasLiked.value = res.data
    }
  } catch (err) {
    console.log('检查点赞状态失败:', err)
  }
}

const handleLike = async () => {
  if (!share.value) return
  // 未登录时提示登录
  if (!userStore.isLoggedIn) {
    showMessage('请先登录后再点赞', 'warning', '⚠️')
    return
  }
  try {
    const res = await likeShare(share.value.shareCode)
    if (res.code === 200) {
      share.value.likeCount = res.data.likeCount
      hasLiked.value = res.data.hasLiked
    }
  } catch (err) {
    console.error('点赞失败:', err)
  }
}

const previewImage = (url) => {
  previewImageUrl.value = url
}

const closePreview = () => {
  previewImageUrl.value = null
}

const viewRelated = (related) => {
  router.push(`/share/${related.shareCode}`)
}

const getItemTypeClass = (type) => {
  const classes = {
    1: 'type-transport',
    2: 'type-play',
    3: 'type-food',
    4: 'type-shopping',
    5: 'type-hotel',
    6: 'type-other'
  }
  return classes[type] || 'type-other'
}

const getItemTypeIcon = (type) => {
  const icons = {
    1: '🚗',
    2: '🎡',
    3: '🍽️',
    4: '🛍️',
    5: '🏨',
    6: '📍'
  }
  return icons[type] || '📍'
}

const getItemTypeName = (type) => {
  const names = {
    1: '交通',
    2: '游玩',
    3: '餐饮',
    4: '购物',
    5: '住宿',
    6: '其他'
  }
  return names[type] || '其他'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

const formatDateRange = (start, end) => {
  if (!start || !end) return ''
  const s = new Date(start)
  const e = new Date(end)
  const days = Math.ceil((e - s) / (1000 * 60 * 60 * 24)) + 1
  return `${s.getMonth() + 1}月${s.getDate()}日 - ${e.getMonth() + 1}月${e.getDate()}日 · ${days}天`
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const formatDateTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}/${day} ${hours}:${minutes}`
}

const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

const getDefaultCover = (share) => {
  const covers = [
    'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=600',
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600',
    'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=600'
  ]
  return covers[share.id % covers.length]
}

const goHome = () => router.push('/')
const goToLogin = () => router.push('/login')
const goToRegister = () => router.push('/login')
const goToProfile = () => router.push('/user-profile')
const goToCreatePlan = () => router.push('/platform/ai-chat')

// 检查收藏状态
const checkFavoriteStatus = async () => {
  if (!share.value?.id) return
  try {
    const res = await checkFavorite(share.value.id)
    if (res.code === 200) {
      hasCollected.value = res.data
    }
  } catch (err) {
    console.error('检查收藏状态失败:', err)
  }
}

// 收藏/取消收藏
const handleCollect = async () => {
  if (!share.value) return
  try {
    const res = await toggleFavorite(share.value.id)
    if (res.code === 200) {
      hasCollected.value = res.data
      alert(hasCollected.value ? '收藏成功' : '已取消收藏')
    } else {
      alert(res.msg || '操作失败')
    }
  } catch (err) {
    alert('操作失败：' + err.message)
  }
}

// 克隆行程
const handleClone = async () => {
  if (!share.value) return
  try {
    const res = await clonePlan(share.value.id)
    if (res.code === 200) {
      alert('行程已克隆到您的行程列表')
      router.push('/platform/plans')
    } else {
      alert(res.msg || '克隆失败')
    }
  } catch (err) {
    alert('克隆失败：' + err.message)
  }
}
</script>

<style scoped>
.share-detail-page {
  min-height: 100vh;
  background: #f8faf8;
}

/* 导航栏 */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 48px;
  height: 72px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo {
  font-size: 28px;
}

.brand-text {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 16px;
  border-radius: 24px;
  background: rgba(76, 175, 80, 0.1);
  transition: all 0.3s ease;
}

.user-info:hover {
  background: rgba(76, 175, 80, 0.2);
}

.user-avatar-wrapper {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #4caf50;
}

.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar-default {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  font-size: 16px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.btn-login {
  padding: 10px 24px;
  border: 1px solid #4caf50;
  background: transparent;
  color: #4caf50;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-register {
  padding: 10px 24px;
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

/* 加载和错误状态 */
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding-top: 72px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid #e0e0e0;
  border-top-color: #4caf50;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.error-icon {
  font-size: 64px;
  margin-bottom: 24px;
}

.error-container h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.error-container p {
  color: #666;
  margin-bottom: 24px;
}

/* 封面区域 */
.share-hero {
  position: relative;
  height: 500px;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: flex-end;
  padding-top: 72px;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.8) 0%,
    rgba(0, 0, 0, 0.3) 50%,
    transparent 100%
  );
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px;
  width: 100%;
  color: white;
}

.share-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.share-tags .tag {
  padding: 6px 14px;
  background: rgba(76, 175, 80, 0.9);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.share-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.3;
}

.share-description {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 24px;
  max-width: 700px;
  line-height: 1.6;
}

.share-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid white;
}

.author-detail {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
}

.publish-time {
  font-size: 13px;
  opacity: 0.8;
}

.share-stats {
  display: flex;
  gap: 24px;
  align-items: center;
}

.share-stats .stat {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
}

/* 浏览量样式 */
.view-stat {
  background: #f8f9fa;
  padding: 8px 16px;
  border-radius: 20px;
  border: 1px solid #e9ecef;
}

.view-count {
  font-weight: 700;
  font-size: 16px;
  color: #333;
}

.view-text {
  color: #666;
  font-size: 13px;
}

/* 点赞按钮样式 */
.like-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 2px solid #e0e0e0;
  border-radius: 25px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  color: #666;
}

.like-btn:hover {
  border-color: #ff6b6b;
  background: #fff5f5;
  transform: scale(1.05);
}

.like-btn.liked {
  border-color: #ff6b6b;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
  color: white;
}

.like-btn.liked:hover {
  background: linear-gradient(135deg, #ff5252 0%, #ff7979 100%);
}

.like-icon {
  font-size: 18px;
}

.like-count {
  font-weight: 600;
  font-size: 16px;
}

.like-text {
  font-size: 13px;
}

/* 内容区域 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 48px;
}

.main-content {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.section-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 16px;
}

.trip-info {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
}

/* 时间轴 */
.timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.timeline-item {
  display: flex;
  gap: 20px;
  padding: 24px 0;
  border-bottom: 1px solid #f5f5f5;
}

.timeline-item:last-child {
  border-bottom: none;
}

.timeline-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.marker-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: #e8f5e9;
}

.type-transport .marker-icon {
  background: #e3f2fd;
}
.type-play .marker-icon {
  background: #f3e5f5;
}
.type-food .marker-icon {
  background: #fff3e0;
}
.type-shopping .marker-icon {
  background: #fce4ec;
}
.type-hotel .marker-icon {
  background: #e0f2f1;
}

.marker-line {
  width: 2px;
  flex: 1;
  background: #e0e0e0;
  margin-top: 12px;
}

.timeline-content {
  flex: 1;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.item-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 15px;
  font-weight: 600;
  color: #4caf50;
}

.time-separator {
  color: #999;
}

.item-type {
  padding: 4px 10px;
  background: #f5f5f5;
  border-radius: 12px;
  font-size: 12px;
  color: #666;
}

/* 新的卡片样式 */
.item-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.item-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.item-main-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.item-type-tag {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  width: fit-content;
}

.item-amount {
  font-size: 24px;
  font-weight: 700;
  color: #ff6b6b;
}

.item-card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-time-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.time-icon {
  font-size: 16px;
}

.time-range {
  font-weight: 500;
}

.item-route {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #888;
  margin: 0;
}

.route-icon {
  font-size: 16px;
}

.item-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.item-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.item-images img {
  width: 120px;
  height: 90px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.item-images img:hover {
  transform: scale(1.05);
}

.item-meta {
  display: flex;
  gap: 16px;
}

.item-cost {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #ff6b6b;
  font-weight: 500;
}

.empty-timeline {
  text-align: center;
  padding: 64px 0;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-timeline p {
  color: #999;
}

/* 侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.author-card {
  background: white;
  border-radius: 20px;
  padding: 32px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.author-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 16px;
}

.author-name-large {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.author-bio {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
}

.action-card {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border-radius: 20px;
  padding: 24px;
  text-align: center;
}

.action-card h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.action-card p {
  font-size: 13px;
  color: #666;
  margin-bottom: 16px;
}

.btn-primary {
  padding: 12px 24px;
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(76, 175, 80, 0.3);
}

.btn-secondary {
  display: block;
  width: 100%;
  margin-top: 12px;
  padding: 12px 24px;
  background: white;
  color: #4caf50;
  border: 2px solid #4caf50;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary:hover {
  background: #4caf50;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(76, 175, 80, 0.3);
}

.related-shares {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.related-shares h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 16px;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.related-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.related-item:hover {
  opacity: 0.8;
}

.related-item img {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.related-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.related-info h5 {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.related-info span {
  font-size: 12px;
  color: #999;
}

/* 图片预览 */
.image-preview {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(0, 0, 0, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: zoom-out;
}

.image-preview img {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
}

.close-preview {
  position: absolute;
  top: 24px;
  right: 24px;
  font-size: 36px;
  color: white;
  cursor: pointer;
}

/* 响应式 */
@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    order: -1;
  }

  .author-card,
  .action-card {
    display: none;
  }
}

@media (max-width: 768px) {
  .navbar {
    padding: 0 24px;
  }

  .share-hero {
    height: 400px;
  }

  .share-title {
    font-size: 28px;
  }

  .share-description {
    font-size: 15px;
  }

  .share-meta {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .main-content {
    padding: 24px;
  }

  .timeline-item {
    gap: 12px;
  }

  .item-images img {
    width: 100px;
    height: 75px;
  }
}
</style>

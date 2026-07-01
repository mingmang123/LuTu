<template>
  <div class="platform-page">
    <AppHeader />

    <div class="platform-container">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <div class="user-card">
          <div class="user-avatar">
            <img v-if="userStore.userInfo?.avatar"
                 :src="userStore.userInfo.avatar"
                 alt="avatar">
            <div v-else
                 class="user-avatar-default">👤</div>
          </div>
          <h3 class="user-name">{{ userStore.userInfo?.username || '旅行者' }}</h3>
          <p class="user-level">{{ userLevel }}</p>
        </div>

        <nav class="nav-menu">
          <router-link to="/platform"
                       class="nav-item"
                       :class="{ active: $route.path === '/platform' }">
            <span class="nav-icon">📊</span>
            <span class="nav-text">概览</span>
          </router-link>
          <router-link to="/platform/plans"
                       class="nav-item"
                       :class="{ active: $route.path.includes('/plans') }">
            <span class="nav-icon">🗺️</span>
            <span class="nav-text">我的行程</span>
            <span class="nav-badge"
                  v-if="stats.plans > 0">{{ stats.plans }}</span>
          </router-link>
          <router-link to="/platform/favorites"
                       class="nav-item"
                       :class="{ active: $route.path.includes('/favorites') }">
            <span class="nav-icon">⭐</span>
            <span class="nav-text">我的收藏</span>
            <span class="nav-badge"
                  v-if="stats.favorites > 0">{{ stats.favorites }}</span>
          </router-link>
          <router-link to="/platform/ai-chat"
                       class="nav-item"
                       :class="{ active: $route.path.includes('/ai-chat') }">
            <span class="nav-icon">🤖</span>
            <span class="nav-text">AI助手</span>
          </router-link>
          <router-link to="/platform/profile"
                       class="nav-item"
                       :class="{ active: $route.path.includes('/profile') }">
            <span class="nav-icon">⚙️</span>
            <span class="nav-text">个人设置</span>
          </router-link>
        </nav>
      </aside>

      <!-- 主内容区 -->
      <main class="main-content">
        <!-- 概览页面 -->
        <div v-if="$route.path === '/platform'"
             class="overview">
          <!-- 欢迎区域 -->
          <div class="welcome-banner">
            <div class="welcome-text">
              <h1>{{ greeting }}，{{ userStore.userInfo?.username || '旅行者' }}</h1>
              <p>今天想去哪里探索？让AI帮你规划完美旅程</p>
            </div>
            <div class="welcome-action">
              <button class="btn-primary"
                      @click="goToAiChat">
                <span>✨</span>
                开始规划
              </button>
            </div>
          </div>

          <!-- 统计卡片 -->
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon plans">🗺️</div>
              <div class="stat-info">
                <span class="stat-value">{{ stats.plans }}</span>
                <span class="stat-label">行程计划</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon days">📅</div>
              <div class="stat-info">
                <span class="stat-value">{{ stats.totalDays }}</span>
                <span class="stat-label">旅行天数</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon cost">💰</div>
              <div class="stat-info">
                <span class="stat-value">¥{{ stats.totalCost }}</span>
                <span class="stat-label">总花费</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon places">📍</div>
              <div class="stat-info">
                <span class="stat-value">{{ stats.places }}</span>
                <span class="stat-label">打卡地点</span>
              </div>
            </div>
          </div>

          <!-- 快捷操作 -->
          <div class="quick-actions">
            <h2 class="section-title">快速开始</h2>
            <div class="action-grid">
              <div class="action-item"
                   @click="goToAiChat">
                <div class="action-image ai-bg"></div>
                <div class="action-content">
                  <h3>AI 智能规划</h3>
                  <p>告诉AI你的需求，一键生成专属行程</p>
                </div>
              </div>
              <div class="action-item"
                   @click="goToPlans">
                <div class="action-image plan-bg"></div>
                <div class="action-content">
                  <h3>管理行程</h3>
                  <p>查看和编辑你的所有旅行计划</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 最近行程 -->
          <div class="recent-plans"
               v-if="recentPlans.length > 0">
            <div class="section-header">
              <h2 class="section-title">最近行程</h2>
              <router-link to="/platform/plans"
                           class="view-all">查看全部 →</router-link>
            </div>
            <div class="plans-list">
              <div v-for="plan in recentPlans"
                   :key="plan.id"
                   class="plan-card"
                   @click="goToPlanDetail(plan.id)">
                <div class="plan-image"
                     :style="{ backgroundImage: `url(${plan.coverImage || getDefaultImage(plan)})` }"></div>
                <div class="plan-info">
                  <h4>{{ plan.title }}</h4>
                  <p>{{ formatDate(plan.startDate) }} - {{ formatDate(plan.endDate) }}</p>
                  <div class="plan-meta">
                    <span class="plan-status"
                          :class="getPlanStatus(plan)">{{ getPlanStatusText(plan) }}</span>
                    <span class="plan-cost">¥{{ plan.totalAmount ? Math.round(Number(plan.totalAmount)) : 0 }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 推荐目的地 -->
          <div class="recommendations"
               v-if="recommendedDestinations.length > 0">
            <h2 class="section-title">为你推荐</h2>
            <div class="dest-grid">
              <div v-for="dest in recommendedDestinations"
                   :key="dest.id"
                   class="dest-item"
                   @click="viewDestination(dest)">
                <div class="dest-img"
                     :style="{ backgroundImage: `url(${dest.imageUrl || getDefaultDestImage(dest)})` }"></div>
                <div class="dest-info">
                  <h4>{{ dest.name }}</h4>
                  <p>{{ dest.location }}</p>
                  <span class="dest-price">¥{{ Math.round(dest.avgCost) }}起</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 子路由 -->
        <router-view v-else />
      </main>
    </div>

    <LoadingSpinner :visible="loading" />
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import AppHeader from '@/components/AppHeader.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import {getTravelItems, getTravelPlans} from '@/api/travel'
import {getHomeData, getRecommendedDestinations} from '@/api/home'
import {encodeId} from '@/utils/idEncoder'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const recentPlans = ref([])
const hotDestinations = ref([])
const recommendedDestinations = ref([])
const stats = ref({
  plans: 0,
  totalDays: 0,
  totalCost: 0,
  places: 0
})

const userLevel = computed(() => {
  const plans = stats.value.plans
  if (plans >= 20) return '🏆 旅行大师'
  if (plans >= 10) return '🥇 资深行者'
  if (plans >= 5) return '🥈 旅行达人'
  return '🥉 旅行新手'
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    // 加载首页数据
    const homeRes = await getHomeData()
    if (homeRes.code === 200) {
      hotDestinations.value = homeRes.data.hotDestinations || []
    }

    // 加载推荐目的地（为你推荐）
    const recommendRes = await getRecommendedDestinations(3)
    if (recommendRes.code === 200) {
      recommendedDestinations.value = recommendRes.data || []
    }

    // 加载行程数据
    const plansRes = await getTravelPlans()
    if (plansRes.code === 200) {
      const plans = plansRes.data || []
      recentPlans.value = plans.slice(0, 3)
      stats.value.plans = plans.length

      let totalDays = 0
      let totalCost = 0
      let totalPlaces = 0

      // 获取每个行程的环节来计算总花费和打卡地点
      for (const plan of plans) {
        if (plan.startDate && plan.endDate) {
          const start = new Date(plan.startDate)
          const end = new Date(plan.endDate)
          totalDays += Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
        }

        // 获取行程环节
        try {
          const itemsRes = await getTravelItems(plan.id)
          if (itemsRes.code === 200) {
            const items = itemsRes.data?.records || itemsRes.data || []
            // 计算花费
            const planCost = items.reduce((sum, item) => sum + (parseFloat(item.amount) || 0), 0)
            totalCost += planCost
            // 计算打卡地点（游玩类型的环节）
            const playItems = items.filter(item => item.itemType === 2 || item.itemType?.code === 2)
            totalPlaces += playItems.length
          }
        } catch (e) {
          console.error(`获取行程 ${plan.id} 环节失败:`, e)
        }
      }
      stats.value.totalDays = totalDays
      stats.value.totalCost = totalCost.toFixed(0)
      stats.value.places = totalPlaces
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const goToAiChat = () => router.push('/platform/ai-chat')
const goToPlans = () => router.push('/platform/plans')
const goToPlanDetail = (id) => router.push(`/travel-plan/${encodeId(id)}`)
const viewDestination = (dest) => {
  console.log('查看目的地:', dest.name)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const getPlanStatus = (plan) => {
  const now = new Date()
  const start = new Date(plan.startDate)
  const end = new Date(plan.endDate)

  if (end < now) return 'completed'
  if (start <= now && end >= now) return 'ongoing'
  return 'upcoming'
}

const getPlanStatusText = (plan) => {
  const status = getPlanStatus(plan)
  const map = { completed: '已完成', ongoing: '进行中', upcoming: '即将出发' }
  return map[status]
}

const getDefaultImage = (plan) => {
  const images = [
    'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=400',
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400',
    'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=400'
  ]
  return images[plan.id % images.length]
}

const getDefaultDestImage = (dest) => {
  return 'https://images.unsplash.com/photo-1527684651001-731c474bbb5a?w=400'
}
</script>

<style scoped>
.platform-page {
  min-height: calc(100vh - 64px);
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 50%, #a5d6a7 100%);
}

.platform-container {
  display: flex;
  padding-top: 64px;
  min-height: calc(100vh - 64px);
}

/* 侧边栏 */
.sidebar {
  width: 260px;
  background: white;
  border-right: 1px solid #e8e8e8;
  padding: 24px;
  position: fixed;
  top: 64px;
  bottom: 0;
  left: 0;
  overflow-y: auto;
}

.user-card {
  text-align: center;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 24px;
}

.user-avatar {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #e8f5e9;
}

.user-avatar img {
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
  font-size: 40px;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.user-level {
  font-size: 13px;
  color: #4caf50;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  color: #666;
  text-decoration: none;
  transition: all 0.3s;
}

.nav-item:hover {
  background: #f5f5f5;
  color: #1a1a1a;
}

.nav-item.active {
  background: #e8f5e9;
  color: #2e7d32;
}

.nav-icon {
  font-size: 20px;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  flex: 1;
}

.nav-badge {
  padding: 2px 8px;
  background: #4caf50;
  color: white;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

/* 主内容区 */
.main-content {
  flex: 1;
  margin-left: 260px;
  padding: 32px;
  height: calc(100vh - 64px);
  overflow-y: auto;
}

.overview {
  max-width: 1200px;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  border-radius: 20px;
  padding: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  color: white;
}

.welcome-text h1 {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.welcome-text p {
  opacity: 0.9;
  font-size: 15px;
}

.btn-primary {
  padding: 14px 28px;
  background: white;
  color: #4caf50;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-icon.plans {
  background: #e3f2fd;
}

.stat-icon.days {
  background: #f3e5f5;
}

.stat-icon.cost {
  background: #e8f5e9;
}

.stat-icon.places {
  background: #fff3e0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

/* 快捷操作 */
.quick-actions {
  margin-bottom: 32px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.action-item {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.action-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
}

.action-image {
  height: 160px;
  background-size: cover;
  background-position: center;
}

.ai-bg {
  background-image: url("https://images.unsplash.com/photo-1677442136019-21780ecad995?w=600");
}

.plan-bg {
  background-image: url("https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=600");
}

.action-content {
  padding: 24px;
}

.action-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.action-content p {
  font-size: 14px;
  color: #666;
}

/* 最近行程 */
.recent-plans {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.view-all {
  color: #4caf50;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.plans-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.plan-card {
  display: flex;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.plan-card:hover {
  transform: translateX(8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.plan-image {
  width: 200px;
  background-size: cover;
  background-position: center;
}

.plan-info {
  flex: 1;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.plan-info h4 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.plan-info p {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.plan-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.plan-status {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.plan-status.completed {
  background: #e8f5e9;
  color: #4caf50;
}

.plan-status.ongoing {
  background: #fff3e0;
  color: #ff9800;
}

.plan-status.upcoming {
  background: #e3f2fd;
  color: #2196f3;
}

.plan-cost {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

/* 推荐目的地 */
.recommendations {
  margin-bottom: 32px;
}

.dest-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.dest-item {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.dest-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
}

.dest-img {
  height: 160px;
  background-size: cover;
  background-position: center;
}

.dest-info {
  padding: 20px;
}

.dest-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.dest-info p {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
}

.dest-price {
  font-size: 15px;
  font-weight: 600;
  color: #4caf50;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 968px) {
  .sidebar {
    display: none;
  }

  .main-content {
    margin-left: 0;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .action-grid {
    grid-template-columns: 1fr;
  }

  .plan-card {
    flex-direction: column;
  }

  .plan-image {
    width: 100%;
    height: 160px;
  }

  .dest-grid {
    grid-template-columns: 1fr;
  }

  .welcome-banner {
    flex-direction: column;
    text-align: center;
    gap: 24px;
  }
}
</style>

<template>
  <div class="dashboard-page">
    <AppHeader />

    <div class="container">
      <!-- 欢迎区域 -->
      <div class="welcome-section">
        <div class="welcome-content">
          <h2 class="fade-in">
            <span class="greeting">{{ greeting }}</span>，
            <span class="username">{{ userStore.userInfo?.username || '旅行者' }}</span>
          </h2>
          <p class="subtitle fade-in-delay">探索世界，从这里开始您的智能旅行体验</p>
        </div>
        <div class="stats-bar">
          <div class="stat-item"
               v-if="stats.plans > 0">
            <span class="stat-number">{{ stats.plans }}</span>
            <span class="stat-label">行程计划</span>
          </div>
          <div class="stat-item"
               v-if="stats.totalDays > 0">
            <span class="stat-number">{{ stats.totalDays }}</span>
            <span class="stat-label">旅行天数</span>
          </div>
          <div class="stat-item"
               v-if="stats.totalCost > 0">
            <span class="stat-number">¥{{ stats.totalCost }}</span>
            <span class="stat-label">总花费</span>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <h3 class="section-title">快速开始</h3>
        <div class="actions-grid">
          <div class="action-card ai-card"
               @click="goToAiChat">
            <div class="card-content">
              <div class="card-icon">🤖</div>
              <div class="card-text">
                <h4>AI 助手</h4>
                <p>智能规划您的完美旅程</p>
              </div>
              <div class="card-arrow">→</div>
            </div>
          </div>

          <div class="action-card plan-card"
               @click="goToTravelPlans">
            <div class="card-content">
              <div class="card-icon">🗺️</div>
              <div class="card-text">
                <h4>我的行程</h4>
                <p>管理您的所有旅行计划</p>
              </div>
              <div class="card-arrow">→</div>
            </div>
          </div>

          <div class="action-card user-card"
               @click="goToUserProfile">
            <div class="card-content">
              <div class="card-icon">👤</div>
              <div class="card-text">
                <h4>个人中心</h4>
                <p>管理您的账户信息</p>
              </div>
              <div class="card-arrow">→</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 热门目的地 -->
      <div class="section"
           v-if="hotDestinations.length > 0">
        <div class="section-header">
          <h3 class="section-title">
            <span class="title-icon">🔥</span>
            热门目的地
          </h3>
        </div>
        <div class="destinations-grid">
          <div v-for="dest in hotDestinations"
               :key="dest.id"
               class="destination-card"
               @click="viewDestination(dest)">
            <div class="destination-image">
              <div class="destination-placeholder">
                <span class="destination-emoji">{{ getDestinationEmoji(dest.name) }}</span>
              </div>
            </div>
            <div class="destination-info">
              <h4 class="destination-name">{{ dest.name }}</h4>
              <p class="destination-location">{{ dest.location }}</p>
              <div class="destination-tags">
                <span v-for="tag in parseTags(dest.tags)"
                      :key="tag"
                      class="tag">{{ tag }}</span>
              </div>
              <div class="destination-cost">
                <span class="cost-label">平均花费</span>
                <span class="cost-value">¥{{ dest.avgCost }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 季节推荐 -->
      <div class="section"
           v-if="seasonDestinations.length > 0">
        <div class="section-header">
          <h3 class="section-title">
            <span class="title-icon">🌸</span>
            {{ currentSeason }}推荐
          </h3>
        </div>
        <div class="season-banner">
          <div class="season-content">
            <h4 class="season-title">{{ currentSeason }}去哪儿玩？</h4>
            <p class="season-desc">精选{{ currentSeason }}最佳旅行目的地</p>
          </div>
          <div class="season-destinations">
            <div v-for="dest in seasonDestinations.slice(0, 3)"
                 :key="dest.id"
                 class="season-dest-item"
                 @click="viewDestination(dest)">
              <span class="season-dest-emoji">{{ getDestinationEmoji(dest.name) }}</span>
              <span class="season-dest-name">{{ dest.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 推荐攻略 -->
      <div class="section"
           v-if="recommendedGuides.length > 0">
        <div class="section-header">
          <h3 class="section-title">
            <span class="title-icon">📖</span>
            精选攻略
          </h3>
        </div>
        <div class="guides-list">
          <div v-for="guide in recommendedGuides"
               :key="guide.id"
               class="guide-card"
               @click="viewGuide(guide)">
            <div class="guide-cover">
              <div class="guide-placeholder">
                <span class="guide-emoji">📍</span>
              </div>
            </div>
            <div class="guide-content">
              <h4 class="guide-title">{{ guide.title }}</h4>
              <p class="guide-summary">{{ guide.summary }}</p>
              <div class="guide-meta">
                <span class="guide-tag"
                      v-for="tag in parseTags(guide.tags).slice(0, 2)"
                      :key="tag">{{ tag }}</span>
                <span class="guide-readtime">{{ guide.readTime }}分钟阅读</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 最近行程 -->
      <div class="recent-section"
           v-if="recentPlans.length > 0">
        <div class="section-header">
          <h3 class="section-title">
            <span class="title-icon">🎯</span>
            最近行程
          </h3>
          <button class="view-all"
                  @click="goToTravelPlans">
            查看全部 →
          </button>
        </div>
        <!-- 调试信息 -->
        <div style="background: #ff0; padding: 10px; margin: 10px 0;">
          Debug: {{ JSON.stringify(recentPlans) }}
        </div>
        <div class="recent-plans-list">
          <div v-for="plan in recentPlans"
               :key="plan.id"
               class="recent-plan-card"
               @click="goToPlanDetail(plan.id)">
            <div class="recent-plan-icon">
              <span class="plan-emoji">{{ getPlanEmoji(plan) }}</span>
            </div>
            <div class="recent-plan-info">
              <h4 class="recent-plan-title">{{ plan.title }}</h4>
              <p class="recent-plan-date">{{ formatDate(plan.startDate) }} - {{ formatDate(plan.endDate) }}</p>
            </div>
            <div class="recent-plan-right">
              <div class="recent-plan-status"
                   :class="getPlanStatus(plan)">
                {{ getPlanStatusText(plan) }}
              </div>
              <div class="recent-plan-cost">
                金额:¥{{ plan.totalAmount ? Math.round(Number(plan.totalAmount)) : 0 }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- AI 快捷问题 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">
            <span class="title-icon">💡</span>
            问问 AI
          </h3>
        </div>
        <div class="ai-questions">
          <div v-for="question in aiQuestions"
               :key="question"
               class="ai-question-chip"
               @click="askAI(question)">
            <span class="question-icon">🤖</span>
            <span class="question-text">{{ question }}</span>
          </div>
        </div>
      </div>
    </div>

    <LoadingSpinner :visible="loading" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AppHeader from '@/components/AppHeader.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import { getTravelPlans, getTravelItems } from '@/api/travel'
import { getHomeData } from '@/api/home'
import { encodeId } from '@/utils/idEncoder'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const hotDestinations = ref([])
const seasonDestinations = ref([])
const recommendedGuides = ref([])
const recentPlans = ref([])
const currentSeason = ref('')
const stats = ref({ plans: 0, totalDays: 0, totalCost: 0 })

const aiQuestions = [
  '帮我规划一个北京3日游',
  '推荐适合亲子游的目的地',
  '预算3000元去哪里玩比较好',
  '五一假期去哪里人少',
  '帮我制定一份美食之旅'
]

// 根据时间显示问候语
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
      seasonDestinations.value = homeRes.data.seasonDestinations || []
      recommendedGuides.value = homeRes.data.recommendedGuides || []
      currentSeason.value = homeRes.data.currentSeason || ''
    }

    // 加载行程数据
    const plansRes = await getTravelPlans()
    console.log('行程数据响应:', plansRes)
    if (plansRes.code === 200) {
      const plans = plansRes.data || []
      console.log('行程列表:', plans)
      stats.value.plans = plans.length

      // 计算总天数和总花费
      let totalDays = 0
      let totalCost = 0

      // 处理每个行程的数据
      for (const plan of plans) {
        if (plan.startDate && plan.endDate) {
          const start = new Date(plan.startDate)
          const end = new Date(plan.endDate)
          totalDays += Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
        }

        // 使用后端返回的 totalAmount 作为行程花费
        const planCost = parseFloat(plan.totalAmount) || 0
        totalCost += planCost
        console.log('行程:', plan.title, 'totalAmount:', plan.totalAmount, 'planCost:', planCost)
      }
      stats.value.totalDays = totalDays
      stats.value.totalCost = totalCost.toFixed(0)
      recentPlans.value = plans.slice(0, 3)
      console.log('recentPlans:', recentPlans.value)
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const goToAiChat = () => router.push('/ai-chat')
const goToTravelPlans = () => router.push('/travel-plans')
const goToUserProfile = () => router.push('/profile')
const goToPlanDetail = (id) => router.push(`/travel-plan/${encodeId(id)}`)

const viewDestination = (dest) => {
  console.log('查看目的地:', dest.name)
}

const viewGuide = (guide) => {
  console.log('查看攻略:', guide.title)
}

const askAI = (question) => {
  router.push('/ai-chat')
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

const getPlanEmoji = (plan) => {
  const emojis = ['✈️', '🚗', '🚄', '🚢', '🏔️', '🏖️']
  return emojis[plan.id % emojis.length] || '✈️'
}

const getDestinationEmoji = (name) => {
  const map = {
    '北京': '🏛️', '上海': '🏙️', '杭州': '🌸', '成都': '🐼',
    '西安': '🏺', '厦门': '🏝️', '三亚': '🏖️', '丽江': '🏔️'
  }
  for (const key in map) {
    if (name.includes(key)) return map[key]
  }
  return '📍'
}

const parseTags = (tags) => {
  if (!tags) return []
  return tags.split(',').map(t => t.trim()).filter(t => t)
}
</script>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 50%, #a5d6a7 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

.welcome-section {
  margin-bottom: 40px;
}

.welcome-content h2 {
  color: #2e7d32;
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 8px;
}

.greeting {
  color: #558b2f;
}

.username {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  color: #78909c;
  font-size: 16px;
}

.fade-in {
  animation: fadeInUp 0.6s ease;
}

.fade-in-delay {
  animation: fadeInUp 0.6s ease 0.2s both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stats-bar {
  display: flex;
  gap: 24px;
  margin-top: 20px;
}

.stat-item {
  background: white;
  padding: 16px 24px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 100px;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #4caf50;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #78909c;
  margin-top: 4px;
}

.quick-actions {
  margin-bottom: 40px;
}

.section-title {
  color: #2e7d32;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 20px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.action-card {
  position: relative;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(46, 125, 50, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(46, 125, 50, 0.2);
}

.card-content {
  position: relative;
  padding: 28px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.ai-card .card-icon {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
}

.plan-card .card-icon {
  background: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
}

.user-card .card-icon {
  background: linear-gradient(135deg, #ab47bc 0%, #9c27b0 100%);
}

.card-text {
  flex: 1;
}

.card-text h4 {
  color: #2e7d32;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.card-text p {
  color: #78909c;
  font-size: 13px;
}

.card-arrow {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 16px;
  color: #78909c;
}

.action-card:hover .card-arrow {
  background: #4caf50;
  color: white;
}

.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.view-all {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: #4caf50;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.view-all:hover {
  background: rgba(76, 175, 80, 0.1);
}

.destinations-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.destination-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
}

.destination-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.destination-image {
  height: 160px;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.destination-placeholder {
  font-size: 64px;
}

.destination-info {
  padding: 20px;
}

.destination-name {
  color: #1b4332;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.destination-location {
  color: #74c69d;
  font-size: 13px;
  margin-bottom: 12px;
}

.destination-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  background: #f0f9f4;
  color: #40916c;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.destination-cost {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cost-label {
  color: #95d5b2;
  font-size: 12px;
}

.cost-value {
  color: #2d6a4f;
  font-size: 16px;
  font-weight: 600;
}

.season-banner {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe0e0 100%);
  border-radius: 20px;
  padding: 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.season-title {
  color: #c0392b;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.season-desc {
  color: #e74c3c;
  font-size: 14px;
}

.season-destinations {
  display: flex;
  gap: 16px;
}

.season-dest-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: white;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.season-dest-item:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.season-dest-emoji {
  font-size: 32px;
}

.season-dest-name {
  color: #2d3436;
  font-size: 14px;
  font-weight: 500;
}

.guides-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.guide-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
}

.guide-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.guide-cover {
  width: 140px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.guide-placeholder {
  font-size: 48px;
}

.guide-content {
  padding: 20px;
  flex: 1;
}

.guide-title {
  color: #1b4332;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  line-height: 1.4;
}

.guide-summary {
  color: #74c69d;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.guide-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.guide-tag {
  background: #f0f9f4;
  color: #40916c;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
}

.guide-readtime {
  color: #95d5b2;
  font-size: 12px;
}

.recent-section {
  margin-bottom: 40px;
}

.recent-plans-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-plan-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.recent-plan-card:hover {
  transform: translateX(8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.recent-plan-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f0f9f4 0%, #d8f3dc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.recent-plan-info {
  flex: 1;
}

.recent-plan-title {
  color: #1b4332;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.recent-plan-date {
  color: #74c69d;
  font-size: 13px;
}

.recent-plan-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.recent-plan-status {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.recent-plan-status.completed {
  background: #d5f5e3;
  color: #27ae60;
}

.recent-plan-status.ongoing {
  background: #fef9e7;
  color: #f39c12;
}

.recent-plan-status.upcoming {
  background: #ebf5fb;
  color: #3498db;
}

.recent-plan-cost {
  font-size: 14px;
  font-weight: 600;
  color: #e74c3c;
}

.ai-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.ai-question-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  padding: 12px 20px;
  border-radius: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
}

.ai-question-chip:hover {
  background: #2d6a4f;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(45, 106, 79, 0.3);
}

.question-icon {
  font-size: 18px;
}

.question-text {
  font-size: 14px;
  color: inherit;
}

@media (max-width: 968px) {
  .actions-grid,
  .destinations-grid {
    grid-template-columns: 1fr;
  }

  .guides-list {
    grid-template-columns: 1fr;
  }

  .welcome-content h2 {
    font-size: 24px;
  }

  .stats-bar {
    flex-wrap: wrap;
  }

  .season-banner {
    flex-direction: column;
    gap: 20px;
  }

  .season-destinations {
    width: 100%;
    justify-content: center;
  }
}
</style>

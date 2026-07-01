<template>
  <div class="home-page">
    <!-- 导航栏 -->
    <nav class="navbar"
         :class="{ 'navbar-scrolled': isNavbarScrolled }">
      <div class="nav-brand">
        <span class="logo">🌿</span>
        <span class="brand-text">途记</span>
      </div>
      <div class="nav-links">
        <a href="#features"
           class="nav-link">功能特色</a>
        <a href="#shares"
           class="nav-link">旅行分享</a>
        <a href="#destinations"
           class="nav-link">热门目的地</a>
      </div>
      <div class="nav-actions">
        <div v-if="isLoggedIn"
             class="user-info"
             @click="goToProfile">
          <div class="user-avatar-wrapper">
            <img v-if="userStore.userInfo?.avatar"
                 :src="userStore.userInfo.avatar"
                 alt="avatar"
                 class="user-avatar" />
            <div v-else
                 class="user-avatar-default">👤</div>
          </div>
          <span class="user-name">{{ userStore.userInfo?.username || '用户' }}</span>
        </div>
        <div v-else
             class="nav-buttons">
          <button class="btn-login"
                  @click="goToLogin">登录</button>
          <button class="btn-register"
                  @click="goToRegister">免费注册</button>
        </div>
      </div>
    </nav>

    <!-- Hero 区域 -->
    <section class="hero"
             :class="{ 'loaded': isPageLoaded }">
      <div class="hero-bg">
        <div class="hero-image"></div>
        <div class="hero-overlay"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title"
            :class="{ 'animate-in': isPageLoaded }">
          <span class="title-line">发现世界的美好</span>
          <span class="title-highlight">从这里开始</span>
        </h1>
        <p class="hero-subtitle"
           :class="{ 'animate-in': isPageLoaded }">AI智能规划，让每一次旅行都成为难忘的回忆</p>

        <div id="stats-section"
             class="hero-stats animate-section"
             :class="{ 'visible': visibleSections.has('stats-section') }">
          <div class="stat"
               v-for="(item, index) in [
            { key: 'plans', label: '旅行计划' },
            { key: 'users', label: '活跃用户' },
            { key: 'shares', label: '精彩分享' }
          ]"
               :key="item.key"
               :style="{ animationDelay: `${index * 0.15}s` }">
            <span class="stat-value">{{ formatStatNumber(stats[item.key]) }}</span>
            <span class="stat-label">{{ item.label }}</span>
          </div>
        </div>
        <div class="hero-cta"
             :class="{ 'animate-in': isPageLoaded }">
          <button class="btn-primary"
                  @click="goToLogin">
            开始规划旅程
            <span class="btn-arrow">→</span>
          </button>
          <button class="btn-secondary"
                  @click="scrollToSection('shares')">探索分享</button>
        </div>
      </div>
    </section>

    <!-- 功能特色 -->
    <section id="features"
             class="features animate-section"
             :class="{ 'visible': visibleSections.has('features') }">
      <div class="container">
        <div class="section-header">
          <span class="section-tag">FEATURES</span>
          <h2 class="section-title">为什么选择途记</h2>
          <p class="section-desc">让每一次出发，都成为值得珍藏的记忆</p>
        </div>
        <div class="features-showcase">
          <div class="feature-row"
               :style="{ animationDelay: '0.1s' }">
            <div class="feature-image-wrap">
              <img src="https://images.unsplash.com/photo-1677442136019-21780ecad995?w=600&h=400&fit=crop&q=60"
                   alt="AI智慧规划"
                   loading="lazy" />
            </div>
            <div class="feature-text">
              <div class="feature-tags">
                <span class="tag">智能推荐</span>
                <span class="tag">个性定制</span>
              </div>
              <h3>智慧随行</h3>
              <p>无需繁琐攻略，只需说出你的心愿。AI 懂你的偏好，知你的节奏，为你量身定制专属旅程。</p>
              <ul class="feature-points">
                <li>千人千面的个性化推荐</li>
                <li>实时优化的动态调整</li>
                <li>从景点到美食的全方位规划</li>
              </ul>
            </div>
          </div>
          <div class="feature-row reverse"
               :style="{ animationDelay: '0.2s' }">
            <div class="feature-image-wrap">
              <img src="https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=600&h=400&fit=crop&q=60"
                   alt="风景路线"
                   loading="lazy" />
            </div>
            <div class="feature-text">
              <div class="feature-tags">
                <span class="tag">最优路线</span>
                <span class="tag">省时省力</span>
              </div>
              <h3>一路风景</h3>
              <p>告别走马观花，拒绝疲于奔命。智能算法为你规划最合理的路线，把更多时间留给风景与体验。</p>
              <ul class="feature-points">
                <li>科学规划减少路途奔波</li>
                <li>深度体验每个目的地</li>
                <li>让旅途中的每一刻都值得</li>
              </ul>
            </div>
          </div>
          <div class="feature-row"
               :style="{ animationDelay: '0.3s' }">
            <div class="feature-image-wrap">
              <img src="https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=600&h=400&fit=crop&q=60"
                   alt="预算管理"
                   loading="lazy" />
            </div>
            <div class="feature-text">
              <div class="feature-tags">
                <span class="tag">透明预算</span>
                <span class="tag">超值体验</span>
              </div>
              <h3>从容出行</h3>
              <p>旅行不该被预算束缚，但也不能超支焦虑。智能预算管理帮你合理分配每一笔花费。</p>
              <ul class="feature-points">
                <li>明细清晰的消费记录</li>
                <li>灵活调整的预算方案</li>
                <li>尽兴又无后顾之忧</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门目的地 -->
    <section id="destinations"
             class="destinations-section animate-section"
             :class="{ 'visible': visibleSections.has('destinations') }">
      <div class="container">
        <div class="section-header">
          <span class="section-tag">DESTINATIONS</span>
          <h2 class="section-title">探索热门目的地</h2>
          <p class="section-desc">精选全球最受欢迎的旅游胜地</p>
        </div>
        <div v-if="homeLoading"
             class="loading-state">
          <span>加载中...</span>
        </div>
        <div v-else-if="destinations.length === 0"
             class="empty-state">
          <span>暂无目的地数据</span>
        </div>
        <div v-else
             <div
             class="destinations-grid">
          <div v-for="(dest, index) in destinations"
               :key="dest.id"
               class="destination-card"
               :style="{ animationDelay: `${index * 0.1}s` }">
            <div class="destination-cover">
              <img :src="dest.imageUrl || getDefaultDestinationImage(dest.name)"
                   :alt="dest.name"
                   loading="lazy" />
              <div class="destination-overlay"></div>
              <span class="destination-tag">{{ getDestinationTag(dest.tags) }}</span>
            </div>
            <div class="destination-content">
              <h3 class="destination-name">{{ dest.name }}</h3>
              <p class="destination-desc">{{ dest.description }}</p>
              <div class="destination-info">
                <span class="destination-price">¥{{ dest.avgCost || 0 }}起</span>
                <span class="destination-season">{{ dest.bestSeason || '四季皆宜' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 季节推荐 -->
    <section v-if="seasonDestinations.length > 0"
             class="season-section animate-section"
             :class="{ 'visible': visibleSections.has('season-section') }">
      <div class="container">
        <div class="section-header">
          <span class="section-tag">SEASON</span>
          <h2 class="section-title">{{ currentSeason }}去哪儿</h2>
          <p class="section-desc">精选{{ currentSeason }}最佳旅行目的地</p>
        </div>
        <div class="season-grid">
          <div v-for="(dest, index) in seasonDestinations"
               :key="dest.id"
               class="season-card"
               :style="{ animationDelay: `${index * 0.1}s` }">
            <div class="season-image">
              <img :src="dest.imageUrl || getDefaultDestinationImage(dest.name)"
                   :alt="dest.name"
                   loading="lazy" />
              <div class="season-overlay"></div>
            </div>
            <div class="season-content">
              <h3>{{ dest.name }}</h3>
              <p>{{ dest.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 旅行分享 -->
    <section id="shares"
             class="shares-section animate-section"
             :class="{ 'visible': visibleSections.has('shares') }">
      <div class="container">
        <div class="section-header">
          <span class="section-tag">SHARES</span>
          <h2 class="section-title">旅行分享</h2>
          <p class="section-desc">发现精彩旅程，分享美好回忆</p>
        </div>
        <div class="share-tabs">
          <button :class="['tab-btn', { active: currentTab === 'hot' }]"
                  @click="switchTab('hot')">热门推荐</button>
          <button :class="['tab-btn', { active: currentTab === 'latest' }]"
                  @click="switchTab('latest')">最新发布</button>
        </div>
        <div class="shares-grid">
          <div v-for="(share, index) in shareList"
               :key="share.id"
               class="share-card"
               :style="{ animationDelay: `${index * 0.08}s` }"
               @click="viewShare(share)">
            <div class="share-cover">
              <img :src="share.coverImage || getDefaultCover(share)"
                   :alt="share.title"
                   loading="lazy" />
            </div>
            <div class="share-content">
              <h3 class="share-title">{{ share.title }}</h3>
              <p class="share-desc">{{ share.summary }}</p>
              <div class="share-meta">
                <span>👁️ {{ formatNumber(share.viewCount) }}</span>
                <span>❤️ {{ formatNumber(share.likeCount) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="load-more">
          <button v-if="hasMore"
                  class="btn-load"
                  @click="loadMore"
                  :disabled="loading">
            {{ loading ? '加载中...' : '加载更多' }}
          </button>
          <button class="btn-view-all"
                  @click="goToShareList">
            查看更多 →
          </button>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-brand">
            <span class="logo">🌿</span>
            <span class="brand-text">途记</span>
            <p>让每一次旅行都成为难忘的回忆</p>
          </div>
        </div>
        <div class="footer-bottom">
          <p>© 2026 途记. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {getHotShares, getLatestShares} from '@/api/share'
import {getHomeData} from '@/api/home'

const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => !!userStore.token)
const stats = ref({ plans: 0, users: 0, shares: 0 })
const currentTab = ref('hot')
const shareList = ref([])
const page = ref(1)
const size = 6
const hasMore = ref(true)
const loading = ref(false)
const homeLoading = ref(false)

// 热门目的地数据（从后端获取）
const destinations = ref([])
// 季节推荐数据
const seasonDestinations = ref([])
// 当前季节
const currentSeason = ref('')

// 动画相关状态
const isPageLoaded = ref(false)
const animatedStats = ref({ plans: 0, users: 0, shares: 0 })
const visibleSections = ref(new Set())
const isNavbarScrolled = ref(false)

onMounted(() => {
  // 页面加载动画
  setTimeout(() => {
    isPageLoaded.value = true
  }, 100)

  loadHomeData()  // 合并了统计数据，一次请求获取全部内容
  loadShares()

  // 监听滚动
  window.addEventListener('scroll', handleScroll, { passive: true })

  // 初始化 Intersection Observer
  initIntersectionObserver()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 滚动处理
const handleScroll = () => {
  isNavbarScrolled.value = window.scrollY > 50

  // Hero 背景视差效果
  const heroImage = document.querySelector('.hero-image')
  if (heroImage && window.scrollY < window.innerHeight) {
    const scrollPercent = window.scrollY / window.innerHeight
    heroImage.style.transform = `scale(${1 + scrollPercent * 0.1}) translateY(${scrollPercent * 30}px)`
  }
}

// 手动触发的区块集合
const manuallyTriggeredSections = new Set()

// Intersection Observer 初始化
const initIntersectionObserver = () => {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          // 如果是手动触发的，跳过自动处理
          if (manuallyTriggeredSections.has(entry.target.id)) {
            return
          }

          visibleSections.value.add(entry.target.id)

          // 如果是统计区域，触发数字动画
          if (entry.target.id === 'stats-section') {
            animateNumbers()
          }
        }
      })
    },
    { threshold: 0.2, rootMargin: '0px 0px -50px 0px' }
  )

  // 观察所有需要动画的区域
  setTimeout(() => {
    document.querySelectorAll('.animate-section').forEach((el) => {
      observer.observe(el)
    })
  }, 500)
}

// 数字滚动动画
const animateNumbers = () => {
  const duration = 2000
  const steps = 60
  const interval = duration / steps

  const targetStats = { ...stats.value }
  let currentStep = 0

  const timer = setInterval(() => {
    currentStep++
    const progress = currentStep / steps
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)

    animatedStats.value = {
      plans: Math.floor(targetStats.plans * easeOutQuart),
      users: Math.floor(targetStats.users * easeOutQuart),
      shares: Math.floor(targetStats.shares * easeOutQuart)
    }

    if (currentStep >= steps) {
      clearInterval(timer)
      animatedStats.value = targetStats
    }
  }, interval)
}

// 加载首页数据（含统计数据，一次请求替代原来的两次）
const loadHomeData = async () => {
  homeLoading.value = true
  try {
    const res = await getHomeData()
    if (res.code === 200) {
      destinations.value = res.data.hotDestinations || []
      seasonDestinations.value = res.data.seasonDestinations || []
      currentSeason.value = res.data.currentSeason || ''
      // 统计数据已合入此接口
      if (res.data.stats) {
        stats.value = res.data.stats
      }
    }
  } catch (error) {
    console.error('加载首页数据失败:', error)
  } finally {
    homeLoading.value = false
  }
}

const loadShares = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const api = currentTab.value === 'hot' ? getHotShares : getLatestShares
    const res = await api(page.value, size)
    if (res.code === 200) {
      const list = res.data?.records || []
      if (page.value === 1) {
        shareList.value = list
      } else {
        shareList.value.push(...list)
      }
      hasMore.value = list.length === size
    }
  } finally {
    loading.value = false
  }
}

const switchTab = (tab) => {
  currentTab.value = tab
  page.value = 1
  shareList.value = []
  loadShares()
}

const loadMore = () => {
  page.value++
  loadShares()
}

const viewShare = (share) => {
  router.push(`/share-detail/${share.shareCode}`)
}

const getDefaultCover = (share) => {
  const covers = [
    'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=400&q=60',
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&q=60',
    'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=400&q=60'
  ]
  return covers[share.id % covers.length]
}

const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

const goToLogin = () => router.push('/login')
const goToRegister = () => router.push('/login')
const goToProfile = () => router.push('/platform/profile')

// 存储动画定时器
let animationTimers = {}

const scrollToSection = (id) => {
  const element = document.getElementById(id)
  if (element) {
    // 标记为手动触发
    manuallyTriggeredSections.add(id)

    // 清除之前的定时器
    if (animationTimers[id]) {
      clearTimeout(animationTimers[id])
      delete animationTimers[id]
    }

    // 强制移除 visible 类并重置样式
    element.classList.remove('visible')
    element.style.opacity = '0'
    element.style.transform = 'translateY(40px)'

    // 获取所有子动画元素并重置
    const animatedChildren = element.querySelectorAll('.destination-card, .season-card, .share-card, .feature-row')
    animatedChildren.forEach(child => {
      child.style.animation = 'none'
      child.style.opacity = '0'
      child.style.transform = 'translateY(30px) scale(0.95)'
    })

    // 强制重绘
    void element.offsetHeight

    element.scrollIntoView({ behavior: 'smooth', block: 'start' })

    // 滚动完成后重新触发动画
    animationTimers[id] = setTimeout(() => {
      element.classList.add('visible')
      element.style.opacity = ''
      element.style.transform = ''

      // 重新触发动画 - 使用 cardEnter 动画
      animatedChildren.forEach((child, index) => {
        child.style.animation = `cardEnter 0.6s ease ${index * 0.1}s forwards`
      })

      // 3秒后移除手动触发标记，允许滚动再次触发动画
      setTimeout(() => {
        manuallyTriggeredSections.delete(id)
      }, 3000)

      delete animationTimers[id]
    }, 500)
  }
}

const goToDestination = (dest) => {
  // 可以跳转到目的地详情页或搜索页
  router.push(`/platform/plan?destination=${encodeURIComponent(dest.name)}`)
}

// 格式化统计数字
const formatStatNumber = (num) => {
  if (!num || num === 0) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万+'
  }
  return num.toString()
}

// 获取目的地默认图片
const getDefaultDestinationImage = (name) => {
  const images = {
    '丽江': 'https://images.unsplash.com/photo-1527684651001-731c474bbb5a?w=400&q=60',
    '京都': 'https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=400&q=60',
    '普吉': 'https://images.unsplash.com/photo-1589394815804-964ed0be2eb5?w=400&q=60',
    '九寨沟': 'https://images.unsplash.com/photo-1564602990062-348691861a10?w=400&q=60',
    '北京': 'https://images.unsplash.com/photo-1508804185872-d7badad00f7d?w=400&q=60',
    '上海': 'https://images.unsplash.com/photo-1538428494232-9c0d8a3ab403?w=400&q=60',
    '杭州': 'https://images.unsplash.com/photo-1599571234909-29ed5d1321d6?w=400&q=60',
    '成都': 'https://images.unsplash.com/photo-1565104425087-ecaa11baf556?w=400&q=60',
    '西安': 'https://images.unsplash.com/photo-1598319964969-4336e4f6d1f9?w=400&q=60',
    '厦门': 'https://images.unsplash.com/photo-1569949381669-ecf31ae8e613?w=400&q=60',
    '三亚': 'https://images.unsplash.com/photo-1540202404-a6711df91000?w=400&q=60'
  }
  for (const key in images) {
    if (name && name.includes(key)) return images[key]
  }
  return 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=400&q=60'
}

// 获取目的地标签
const getDestinationTag = (tags) => {
  if (!tags) return '热门推荐'
  const tagList = tags.split(',').map(t => t.trim()).filter(t => t)
  return tagList[0] || '热门推荐'
}

// 处理搜索
const handleSearch = () => {
  if (!searchQuery.value.trim()) return
  // 跳转到搜索结果页或AI聊天页
  router.push(`/platform/ai-chat?query=${encodeURIComponent(searchQuery.value)}`)
}

// 快速搜索
const quickSearch = (tag) => {
  searchQuery.value = tag
  handleSearch()
}

// 跳转到分享列表页
const goToShareList = () => {
  router.push('/shares')
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f8fafc;
}

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
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-color);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  font-size: 28px;
}

.brand-text {
  font-size: 22px;
  font-weight: 700;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-links {
  display: flex;
  gap: 40px;
}

.nav-link {
  font-size: 15px;
  color: var(--text-secondary);
  text-decoration: none;
  font-weight: 500;
  transition: color var(--transition-fast);
}

.nav-link:hover {
  color: var(
      --accent-primary);
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
  background: var(--bg-tertiary);
}

.user-avatar-wrapper {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
}

.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.btn-login,
.btn-register {
  padding: 10px 24px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-login {
  border: 1px solid var(--accent-primary);
  background: transparent;
  color: var(--accent-primary);
}

.btn-register {
  background: var(--accent-gradient);
  color: white;
  border: none;
}

.hero {
  position: relative;
  height: 100vh;
  min-height: 700px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.hero-image {
  position: absolute;
  inset: 0;
  background: url("https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=1200&q=70")
    center/cover;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.3) 0%,
    rgba(255, 255, 255, 0.1) 50%,
    rgba(255, 248, 245, 0.4) 100%
  );
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 800px;
  padding: 0 24px;
  color: var(--text-primary);
}

.hero-title {
  font-size: 56px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 24px;
}

.title-highlight {
  display: block;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 20px;
  color: var(--text-secondary);
  margin-bottom: 32px;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 64px;
  margin-bottom: 48px;
}

.stat-value {
  display: block;
  font-size: 36px;
  font-weight: 700;
  color: var(--accent-primary);
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.hero-cta {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.btn-primary,
.btn-secondary {
  padding: 16px 32px;
  border-radius: var(--radius-sm);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-primary {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 50%, #059669 100%);
  color: white;
  border: none;
  box-shadow: 0 4px 16px rgba(34, 197, 94, 0.3);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.9);
  color: #16a34a;
  border: 1px solid rgba(22, 163, 74, 0.3);
  backdrop-filter: blur(10px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.features,
.destinations-section,
.shares-section {
  padding: 120px 0;
  content-visibility: auto;
  contain-intrinsic-size: 600px;
}

.features {
  background: var(--bg-secondary);
}

.destinations-section {
  background: var(--bg-primary);
}

.destinations-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 32px;
}

.destination-card {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.destination-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  background: rgba(255, 255, 255, 1);
}

.destination-cover {
  position: relative;
  height: 280px;
  overflow: hidden;
}

.destination-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.destination-card:hover .destination-cover img {
  transform: scale(1.08);
}

.destination-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.7) 0%,
    rgba(0, 0, 0, 0.2) 50%,
    transparent 100%
  );
  transition: opacity 0.3s ease;
}

.destination-card:hover .destination-overlay {
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.6) 0%,
    rgba(0, 0, 0, 0.1) 60%,
    transparent 100%
  );
}

.destination-tag {
  position: absolute;
  top: 16px;
  left: 16px;
  padding: 6px 14px;
  background: var(--accent-gradient);
  color: white;
  font-size: 12px;
  font-weight: 600;
  border-radius: 20px;
  z-index: 1;
}

.destination-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24px;
  color: white;
  z-index: 1;
}

.destination-name {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.destination-desc {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.destination-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.destination-price {
  font-size: 18px;
  font-weight: 700;
  color: #ff6b6b;
}

.destination-season {
  font-size: 12px;
  opacity: 0.8;
}

.section-header {
  text-align: center;
  margin-bottom: 64px;
}

.section-tag {
  display: inline-block;
  padding: 8px 16px;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);
}

.section-title {
  font-size: 40px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.section-desc {
  font-size: 18px;
  color: var(--text-secondary);
}

.features-showcase {
  display: flex;
  flex-direction: column;
  gap: 80px;
}

.feature-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.feature-row.reverse {
  direction: rtl;
}

.feature-row.reverse > * {
  direction: ltr;
}

.feature-image-wrap {
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: all 0.4s ease;
}

.feature-image-wrap:hover {
  transform: scale(1.02);
  box-shadow: 0 16px 50px rgba(14, 165, 233, 0.2);
  border-color: rgba(14, 165, 233, 0.3);
}

.feature-image-wrap img {
  width: 100%;
  height: 320px;
  object-fit: cover;
  display: block;
}

.feature-text {
  padding: 20px 0;
}

.feature-tags {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.tag {
  padding: 8px 16px;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
  font-size: 13px;
  font-weight: 500;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);
}

.feature-text h3 {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.feature-text p {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 24px;
}

.feature-points {
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-points li {
  font-size: 15px;
  color: var(--text-secondary);
  padding: 8px 0;
  padding-left: 24px;
  position: relative;
}

.feature-points li::before {
  content: "✦";
  position: absolute;
  left: 0;
  color: var(--accent-primary);
}

.share-tabs {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 48px;
}

.tab-btn {
  padding: 12px 32px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(22, 163, 74, 0.2);
  border-radius: 30px;
  font-size: 15px;
  font-weight: 500;
  color: #16a34a;
  cursor: pointer;
  transition: all var(--transition-fast);
  backdrop-filter: blur(10px);
}

.tab-btn.active,
.tab-btn:hover {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 16px rgba(34, 197, 94, 0.3);
}

.shares-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.share-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.share-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  background: rgba(255, 255, 255, 1);
}

.share-cover {
  height: 200px;
  overflow: hidden;
}

.share-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.share-card:hover .share-cover img {
  transform: scale(1.08);
}

.share-content {
  padding: 24px;
}

.share-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.share-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.share-meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: var(--text-muted);
}

.load-more {
  text-align: center;
  margin-top: 48px;
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.btn-load {
  padding: 14px 32px;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  border: none;
  border-radius: var(--radius-sm);
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 4px 16px rgba(34, 197, 94, 0.3);
}

.btn-load:hover:not(:disabled) {
  border-color: #16a34a;
  color: #16a34a;
}

.btn-view-all {
  padding: 14px 32px;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  border: none;
  border-radius: var(--radius-sm);
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.3);
}

.btn-view-all:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(76, 175, 80, 0.4);
}

.footer {
  padding: 60px 0 30px;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
  content-visibility: auto;
  contain-intrinsic-size: 200px;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
  font-size: 16px;
}

/* 季节推荐 */
.season-section {
  padding: 120px 0;
  background: linear-gradient(
    135deg,
    var(--bg-secondary) 0%,
    var(--bg-primary) 100%
  );
  content-visibility: auto;
  contain-intrinsic-size: 400px;
}

.season-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.season-card {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.season-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  background: rgba(255, 255, 255, 1);
}

.season-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.season-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.season-card:hover .season-image img {
  transform: scale(1.08);
}

.season-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.7) 0%,
    rgba(0, 0, 0, 0.2) 50%,
    transparent 100%
  );
}

.season-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  color: white;
  z-index: 1;
}

.season-content h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 6px;
}

.season-content p {
  font-size: 13px;
  opacity: 0.9;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.footer-content {
  text-align: center;
  margin-bottom: 40px;
}

.footer-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.footer-brand .logo {
  font-size: 40px;
}

.footer-brand .brand-text {
  font-size: 28px;
}

.footer-brand p {
  color: var(--text-secondary);
  font-size: 16px;
}

.footer-bottom {
  text-align: center;
  padding-top: 30px;
  border-top: 1px solid var(--border-color);
}

.footer-bottom p {
  color: var(--text-muted);
  font-size: 14px;
}

/* ==================== 动画效果 ==================== */

/* 页面加载动画 */
.hero-title.animate-in .title-line {
  opacity: 0;
  transform: translateY(30px);
  animation: fadeInUp 0.8s ease forwards;
}

.hero-title.animate-in .title-highlight {
  opacity: 0;
  transform: translateY(30px);
  animation: fadeInUp 0.8s ease 0.2s forwards;
}

.hero-subtitle.animate-in {
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.8s ease 0.4s forwards;
}

.hero-cta.animate-in {
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.8s ease 0.6s forwards;
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 统计数据动画 */
.hero-stats .stat {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

.hero-stats.visible .stat {
  animation: statPop 0.6s ease forwards;
}

@keyframes statPop {
  0% {
    opacity: 0;
    transform: translateY(20px) scale(0.9);
  }
  70% {
    transform: translateY(-5px) scale(1.05);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 滚动触发动画 */
.animate-section {
  opacity: 0;
  transform: translateY(40px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.animate-section.visible {
  opacity: 1;
  transform: translateY(0);
}

/* 功能特色行动画 */
.feature-row {
  opacity: 0;
  transform: translateX(-50px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.feature-row.reverse {
  transform: translateX(50px);
}

.animate-section.visible .feature-row {
  opacity: 1;
  transform: translateX(0);
}

/* 卡片入场动画 */
.destination-card,
.season-card,
.share-card {
  opacity: 0;
  transform: translateY(30px) scale(0.95);
}

.animate-section.visible .destination-card,
.animate-section.visible .season-card,
.animate-section.visible .share-card {
  animation: cardEnter 0.6s ease forwards;
}

@keyframes cardEnter {
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 导航栏滚动效果 */
.navbar {
  transition: all 0.3s ease;
}

.navbar-scrolled {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
}

.navbar-scrolled .nav-link {
  color: var(--text-primary);
}

.navbar-scrolled .brand-text {
  color: var(--text-primary);
}

/* 按钮悬停效果增强 */
.btn-primary {
  position: relative;
  overflow: hidden;
}

.btn-primary::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.6s ease, height 0.6s ease;
}

.btn-primary:hover::before {
  width: 300px;
  height: 300px;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 107, 107, 0.4);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

/* 卡片悬停效果增强 */
.destination-card,
.season-card,
.share-card {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.destination-card:hover,
.season-card:hover,
.share-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.destination-card:hover .destination-cover img,
.season-card:hover .season-image img,
.share-card:hover .share-cover img {
  transform: scale(1.1);
}

/* Tab 按钮动画 */
.tab-btn {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.tab-btn::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--accent-primary);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.tab-btn.active::after {
  width: 80%;
}

.tab-btn:hover {
  transform: translateY(-2px);
}

/* 加载更多按钮动画 */
.btn-load,
.btn-view-all {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.btn-load:hover,
.btn-view-all:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(76, 175, 80, 0.3);
}

/* 图片加载动画 */
.destination-cover img,
.season-image img,
.share-cover img {
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Hero 背景视差效果 */
.hero-image {
  transition: transform 0.1s ease-out;
}

@media (max-width: 768px) {
  .navbar {
    padding: 0 20px;
  }

  .nav-links {
    display: none;
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-stats {
    flex-direction: column;
    gap: 24px;
  }

  .hero-cta {
    flex-direction: column;
  }

  .features-showcase {
    gap: 48px;
  }

  .feature-row {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .feature-row.reverse {
    direction: ltr;
  }

  .feature-image-wrap img {
    height: 240px;
  }

  .feature-text h3 {
    font-size: 24px;
  }

  .shares-grid {
    grid-template-columns: 1fr;
  }

  .destinations-grid {
    grid-template-columns: 1fr;
  }

  .season-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* ==================== 区块背景图片 ==================== */

/* 功能特色 - 山脉背景 */
.features {
  background: linear-gradient(rgba(15, 23, 42, 0.75), rgba(15, 23, 42, 0.85)),
    url("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&q=60")
      center/cover no-repeat;
}

.features .section-title,
.features .section-desc,
.features .feature-text h3 {
  color: white;
}

.features .feature-text p,
.features .feature-points li {
  color: rgba(255, 255, 255, 0.9);
}

.features .feature-points li::before {
  color: #38bdf8;
}

/* 热门目的地 - 海滩背景 */
.destinations-section {
  background: linear-gradient(
      rgba(255, 255, 255, 0.92),
      rgba(255, 255, 255, 0.96)
    ),
    url("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&q=60")
      center/cover no-repeat;
}

/* 季节推荐 - 森林背景 */
.season-section {
  background: linear-gradient(rgba(20, 83, 45, 0.8), rgba(20, 83, 45, 0.88)),
    url("https://images.unsplash.com/photo-1448375240586-882707db888b?w=800&q=60")
      center/cover no-repeat;
}

.season-section .section-title,
.season-section .section-desc,
.season-section .season-content h3 {
  color: white;
}

.season-section .season-content p {
  color: rgba(255, 255, 255, 0.9);
}

/* 旅行分享 - 城市夜景背景 */
.shares-section {
  background: linear-gradient(rgba(15, 23, 42, 0.88), rgba(15, 23, 42, 0.92)),
    url("https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=800&q=60")
      center/cover no-repeat;
}

.shares-section .section-title,
.shares-section .section-desc {
  color: white;
}

/* 页脚 - 星空背景 */
.footer {
  background: linear-gradient(rgba(15, 23, 42, 0.92), rgba(2, 6, 23, 0.96)),
    url("https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800&q=60")
      center/cover no-repeat;
  color: white;
}

.footer-brand p {
  color: rgba(255, 255, 255, 0.8);
}

.footer-bottom p {
  color: rgba(255, 255, 255, 0.6);
}

/* 导航栏背景 - 星空图片 */
.navbar {
  background: linear-gradient(rgba(15, 23, 42, 0.7), rgba(15, 23, 42, 0.8)),
    url("https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800&q=60")
      center/cover no-repeat;
}

.navbar-scrolled {
  background: linear-gradient(rgba(15, 23, 42, 0.85), rgba(15, 23, 42, 0.9)),
    url("https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800&q=60")
      center/cover no-repeat;
  backdrop-filter: blur(10px);
}

.navbar .nav-link,
.navbar .brand-text {
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.navbar .nav-link:hover {
  color: rgba(255, 255, 255, 0.9);
}
</style>

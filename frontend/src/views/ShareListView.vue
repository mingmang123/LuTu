<template>
  <div class="share-list-page">
    <AppHeader />

    <div class="page-header">
      <div class="container">
        <h1 class="page-title">旅行分享</h1>
        <p class="page-desc">发现真实的旅行故事，获取灵感，规划您的下一次旅程</p>

        <!-- 搜索框 -->
        <div class="search-box">
          <input v-model="searchKeyword"
                 type="text"
                 placeholder="搜索分享标题、目的地、标签..."
                 @keyup.enter="handleSearch" />
          <button class="search-btn"
                  @click="handleSearch">
            <span>🔍</span>
            搜索
          </button>
        </div>

        <!-- 筛选标签 -->
        <div class="filter-tags">
          <span class="filter-label">热门标签：</span>
          <div class="tag-list">
            <button v-for="tag in hotTags"
                    :key="tag"
                    class="tag-btn"
                    :class="{ active: selectedTag === tag }"
                    @click="selectTag(tag)">
              {{ tag }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="container main-content">
      <!-- 标签页 -->
      <div class="tabs">
        <button v-for="tab in tabs"
                :key="tab.key"
                class="tab-btn"
                :class="{ active: currentTab === tab.key }"
                @click="switchTab(tab.key)">
          {{ tab.label }}
        </button>
      </div>

      <!-- 分享列表 -->
      <div v-if="loading && shareList.length === 0"
           class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载分享...</p>
      </div>

      <div v-else-if="shareList.length === 0"
           class="empty-state">
        <div class="empty-icon">📭</div>
        <h3>暂无分享</h3>
        <p>还没有相关的旅行分享，成为第一个分享的人吧！</p>
      </div>

      <div v-else
           class="shares-grid">
        <div v-for="share in shareList"
             :key="share.id"
             class="share-card"
             @click="viewShare(share)">
          <div class="share-cover">
            <img :src="share.coverImage || getDefaultCover(share)"
                 :alt="share.title">
            <div class="share-overlay">
              <div class="share-stats">
                <span class="stat">
                  <i class="icon-eye">👁</i>
                  {{ formatNumber(share.viewCount) }}
                </span>
                <span class="stat">
                  <i class="icon-heart">❤</i>
                  {{ formatNumber(share.likeCount) }}
                </span>
              </div>
            </div>
          </div>
          <div class="share-content">
            <div class="share-tags">
              <span v-for="tag in share.tags"
                    :key="tag"
                    class="tag">{{ tag }}</span>
            </div>
            <h3 class="share-title">{{ share.title }}</h3>
            <p class="share-desc">{{ share.description || '暂无描述' }}</p>
            <div class="share-meta">
              <div class="share-author">
                <img :src="share.author?.avatar || defaultAvatar"
                     :alt="share.author?.username">
                <span>{{ share.author?.username || '匿名用户' }}</span>
              </div>
              <span class="share-date">{{ formatDate(share.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="shareList.length > 0"
           class="pagination">
        <button class="page-btn"
                :disabled="page === 1"
                @click="changePage(page - 1)">
          上一页
        </button>
        <span class="page-info">第 {{ page }} 页</span>
        <button class="page-btn"
                :disabled="!hasMore"
                @click="changePage(page + 1)">
          下一页
        </button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { getHotShares, getLatestShares, searchSharesByTag } from '@/api/share'

const router = useRouter()
const route = useRoute()

// 标签页
const tabs = [
  { key: 'hot', label: '🔥 热门分享' },
  { key: 'latest', label: '🆕 最新分享' }
]
const currentTab = ref('hot')

// 分享列表
const shareList = ref([])
const loading = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 12

// 搜索
const searchKeyword = ref('')
const selectedTag = ref('')

// 热门标签
const hotTags = ['自由行', '亲子游', '美食', '摄影', '徒步', '海岛', '古镇', '雪山']

const defaultAvatar = 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100'

// 默认封面图
const defaultCovers = [
  'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=600',
  'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600',
  'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=600',
  'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=600',
  'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600'
]

const getDefaultCover = (share) => {
  const index = share.id % defaultCovers.length
  return defaultCovers[index]
}

const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const loadShares = async () => {
  if (loading.value) return
  loading.value = true

  try {
    let res
    if (selectedTag.value) {
      res = await searchSharesByTag(selectedTag.value, page.value, pageSize)
    } else {
      const api = currentTab.value === 'hot' ? getHotShares : getLatestShares
      res = await api(page.value, pageSize)
    }

    if (res.code === 200) {
      // 处理分页数据
      const records = res.data?.records || res.data || []
      const newShares = Array.isArray(records) ? records : []
      shareList.value = newShares
      hasMore.value = newShares.length === pageSize
    }
  } catch (error) {
    console.error('加载分享失败:', error)
  } finally {
    loading.value = false
  }
}

const switchTab = (tab) => {
  currentTab.value = tab
  selectedTag.value = ''
  page.value = 1
  loadShares()
}

const selectTag = (tag) => {
  if (selectedTag.value === tag) {
    selectedTag.value = ''
  } else {
    selectedTag.value = tag
  }
  page.value = 1
  loadShares()
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    // 如果有搜索关键词，可以跳转到搜索结果页或在这里处理
    // 暂时使用标签搜索
    selectedTag.value = searchKeyword.value.trim()
    page.value = 1
    loadShares()
  }
}

const changePage = (newPage) => {
  page.value = newPage
  loadShares()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const viewShare = (share) => {
  router.push(`/share-detail/${share.shareCode}`)
}

onMounted(() => {
  // 从 URL 参数中获取标签
  const tagFromQuery = route.query.tag
  if (tagFromQuery) {
    selectedTag.value = tagFromQuery
  }
  loadShares()
})

// 监听路由变化
watch(() => route.query.tag, (newTag) => {
  if (newTag) {
    selectedTag.value = newTag
    loadShares()
  }
})
</script>

<style scoped>
.share-list-page {
  min-height: 100vh;
  background: var(--bg-primary);
}

.page-header {
  padding: 100px 0 60px;
  background: linear-gradient(
    135deg,
    rgba(26, 26, 46, 0.95) 0%,
    rgba(22, 33, 62, 0.9) 50%,
    rgba(15, 52, 96, 0.85) 100%
  );
  color: var(--text-primary);
  text-align: center;
  border-bottom: 1px solid var(--border-color);
}

.page-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 16px;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-desc {
  font-size: 18px;
  color: var(--text-secondary);
  margin-bottom: 40px;
}

/* 搜索框 */
.search-box {
  max-width: 600px;
  margin: 0 auto 30px;
  display: flex;
  gap: 12px;
  padding: 0 20px;
}

.search-box input {
  flex: 1;
  padding: 16px 24px;
  border: 1px solid var(--border-color);
  border-radius: 50px;
  font-size: 16px;
  background: var(--bg-card);
  color: var(--text-primary);
  box-shadow: var(--shadow-sm);
}

.search-box input:focus {
  outline: none;
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 3px var(--accent-glow);
}

.search-box input::placeholder {
  color: var(--text-muted);
}

.search-btn {
  padding: 16px 32px;
  background: var(--accent-gradient);
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all var(--transition-fast);
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.6);
}

/* 筛选标签 */
.filter-tags {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  padding: 0 20px;
}

.filter-label {
  font-size: 14px;
  color: var(--text-muted);
}

.tag-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.tag-btn {
  padding: 8px 16px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tag-btn:hover {
  background: var(--bg-hover);
  border-color: var(--accent-primary);
  color: var(--accent-primary);
}

.tag-btn.active {
  background: var(--accent-gradient);
  border-color: var(--accent-primary);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
}

/* 主内容区 */
.main-content {
  padding: 60px 20px;
}

/* 标签页 */
.tabs {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 40px;
}

.tab-btn {
  padding: 12px 32px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 30px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tab-btn:hover {
  border-color: var(--accent-primary);
  color: var(--accent-primary);
}

.tab-btn.active {
  background: var(--accent-gradient);
  border-color: var(--accent-primary);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 80px 20px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid var(--bg-tertiary);
  border-top-color: var(--accent-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 24px;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.empty-state p {
  color: var(--text-secondary);
}

/* 分享网格 */
.shares-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 30px;
  margin-bottom: 50px;
}

.share-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.share-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg), var(--shadow-glow);
  border-color: var(--accent-primary);
}

.share-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.share-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.share-card:hover .share-cover img {
  transform: scale(1.1);
}

.share-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(26, 26, 46, 0.9) 100%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  align-items: flex-end;
  padding: 20px;
}

.share-card:hover .share-overlay {
  opacity: 1;
}

.share-stats {
  display: flex;
  gap: 20px;
  color: white;
  font-size: 14px;
}

.share-stats .stat {
  display: flex;
  align-items: center;
  gap: 6px;
}

.share-content {
  padding: 20px;
}

.share-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.share-tags .tag {
  padding: 4px 10px;
  background: var(--bg-tertiary);
  color: var(--accent-primary);
  border-radius: 12px;
  font-size: 12px;
}

.share-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.share-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.share-author img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.share-author span {
  font-size: 14px;
  color: var(--text-secondary);
}

.share-date {
  font-size: 12px;
  color: var(--text-muted);
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 40px 0;
}

.page-btn {
  padding: 12px 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.page-btn:hover:not(:disabled) {
  border-color: var(--accent-primary);
  color: var(--accent-primary);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 响应式 */
@media (max-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .search-box {
    flex-direction: column;
  }

  .search-btn {
    width: 100%;
    justify-content: center;
  }

  .filter-tags {
    flex-direction: column;
  }

  .shares-grid {
    grid-template-columns: 1fr;
  }

  .tabs {
    flex-wrap: wrap;
  }

  .tab-btn {
    padding: 10px 20px;
    font-size: 14px;
  }
}
</style>

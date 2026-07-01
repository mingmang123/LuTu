<template>
  <div class="favorites-page">
    <div class="page-header">
      <h2>我的收藏</h2>
      <p class="subtitle">查看您收藏的旅行行程</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="favorites.length === 0" class="empty-state">
      <div class="empty-icon">⭐</div>
      <h3>还没有收藏任何行程</h3>
      <p>浏览分享页面，收藏您感兴趣的行程</p>
      <button class="btn-primary" @click="goToShares">去发现</button>
    </div>

    <!-- 收藏列表 -->
    <div v-else class="favorites-list">
      <div v-for="item in favorites" :key="item.id" class="favorite-card">
        <div class="card-cover" @click="viewShare(item)">
          <img :src="item.coverImage || getDefaultCover(item)" :alt="item.title">
          <div class="cover-overlay">
            <span class="view-btn">查看详情</span>
          </div>
        </div>
        <div class="card-content">
          <h3 class="card-title" @click="viewShare(item)">{{ item.title }}</h3>
          <p class="card-desc">{{ item.description || '暂无描述' }}</p>
          <div class="card-meta">
            <span class="meta-item">
              <i class="icon-eye">👁</i>
              {{ formatNumber(item.viewCount || 0) }} 浏览
            </span>
            <span class="meta-item">
              <i class="icon-like">❤️</i>
              {{ formatNumber(item.likeCount || 0) }} 点赞
            </span>
          </div>
          <div class="card-actions">
            <button class="btn-clone" @click="clonePlan(item)">
              <span>📋</span> 克隆
            </button>
            <button class="btn-remove" @click="removeFavorite(item)">
              <span>🗑️</span> 取消收藏
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination">
      <button
        class="page-btn"
        :disabled="currentPage === 1"
        @click="changePage(currentPage - 1)"
      >
        上一页
      </button>
      <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
      <button
        class="page-btn"
        :disabled="currentPage === totalPages"
        @click="changePage(currentPage + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {clonePlan as clonePlanApi, getMyFavorites, toggleFavorite} from '@/api/favorite'

const router = useRouter()

const favorites = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(0)
const total = ref(0)

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getMyFavorites(currentPage.value, pageSize.value)
    console.log('收藏列表返回:', res)
    if (res.code === 200 && res.data) {
      // 处理分页数据
      if (res.data.records) {
        favorites.value = res.data.records
        total.value = res.data.total || 0
        totalPages.value = res.data.pages || 0
      } else if (Array.isArray(res.data)) {
        // 如果是数组直接赋值
        favorites.value = res.data
        total.value = res.data.length
        totalPages.value = 1
      } else {
        favorites.value = []
        total.value = 0
        totalPages.value = 0
      }
    } else {
      favorites.value = []
    }
  } catch (err) {
    console.error('加载收藏失败:', err)
    alert('加载失败，请重试')
    favorites.value = []
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  currentPage.value = page
  loadFavorites()
}

const viewShare = (item) => {
  router.push(`/share-detail/${item.shareCode}`)
}

const goToShares = () => {
  router.push('/shares')
}

const removeFavorite = async (item) => {
  try {
    // 传递 shareId 而不是收藏记录的 id
    const res = await toggleFavorite(item.shareId)
    if (res.code === 200) {
      alert('已取消收藏')
      loadFavorites()
    } else {
      alert(res.msg || '操作失败')
    }
  } catch (err) {
    alert('操作失败：' + err.message)
  }
}

const clonePlan = async (item) => {
  try {
    // 传递 shareId 而不是收藏记录的 id
    const res = await clonePlanApi(item.shareId)
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

const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

const getDefaultCover = (item) => {
  const covers = [
    'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=600',
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600',
    'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=600'
  ]
  return covers[item.id % covers.length]
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-page {
  padding: 0 32px 32px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.subtitle {
  color: #666;
  font-size: 14px;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e8f5e9;
  border-top-color: #4caf50;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.empty-state p {
  color: #666;
  margin-bottom: 24px;
}

.btn-primary {
  padding: 12px 32px;
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(76, 175, 80, 0.3);
}

.favorites-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.favorite-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.favorite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
}

.card-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
  cursor: pointer;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.favorite-card:hover .card-cover img {
  transform: scale(1.05);
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.favorite-card:hover .cover-overlay {
  opacity: 1;
}

.view-btn {
  padding: 10px 24px;
  background: white;
  color: #333;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.card-content {
  padding: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
  cursor: pointer;
  transition: color 0.2s;
}

.card-title:hover {
  color: #4caf50;
}

.card-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #888;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-actions {
  display: flex;
  gap: 12px;
}

.card-actions button {
  flex: 1;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.btn-clone {
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
  color: white;
  border: none;
}

.btn-clone:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.btn-remove {
  background: #f5f5f5;
  color: #666;
  border: none;
}

.btn-remove:hover {
  background: #ffebee;
  color: #e53935;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 40px;
}

.page-btn {
  padding: 10px 20px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #f5f5f5;
  border-color: #4caf50;
  color: #4caf50;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

@media (max-width: 768px) {
  .favorites-page {
    padding: 20px;
  }

  .favorites-list {
    grid-template-columns: 1fr;
  }
}
</style>

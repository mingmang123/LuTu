<template>
  <div class="travel-plan-detail-page">
    <AppHeader />

    <div class="container">
      <!-- 行程标题卡片 -->
      <div class="plan-header-card">
        <div class="plan-header-content">
          <div class="plan-title-section">
            <h2>{{ plan?.title }}</h2>
            <div class="plan-dates">
              <span class="date-badge">
                <span class="date-icon">📅</span>
                <span>{{ formatDate(plan?.startDate) }} - {{ formatDate(plan?.endDate) }}</span>
              </span>
              <span class="duration-badge"
                    v-if="plan?.startDate && plan?.endDate">
                {{ calculateDuration(plan.startDate, plan.endDate) }}
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
        <button class="add-item-btn"
                @click="openAddModal">
          <span class="btn-icon">+</span>
          <span>添加环节</span>
        </button>
      </div>

      <!-- 时间线视图 -->
      <div class="timeline-section">
        <h3 class="section-title">
          <span class="title-icon">🗓️</span>
          行程安排
        </h3>

        <div v-if="planItems.length > 0"
             class="timeline">
          <div v-for="(item, index) in planItems"
               :key="item.id"
               class="timeline-item"
               :class="{ 'last': index === planItems.length - 1 }">
            <div class="timeline-left">
              <div class="timeline-dot"
                   :class="getTypeClass(item.itemType)">
                <span class="dot-icon">{{ getTypeIcon(item.itemType) }}</span>
              </div>
              <div class="timeline-line"
                   v-if="index !== planItems.length - 1"></div>
            </div>

            <div class="timeline-content">
              <div class="content-card"
                   @click="toggleExpand(item)">
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
                    <span class="item-type-tag"
                          :class="getTypeClass(item.itemType)">
                      {{ getTypeLabel(item.itemType) }}
                    </span>
                  </div>
                  <div class="header-actions">
                    <span class="item-amount">¥{{ item.amount || 0 }}</span>
                    <button class="expand-btn"
                            :class="{ expanded: item.expanded }">
                      <svg viewBox="0 0 24 24"
                           fill="none"
                           stroke="currentColor"
                           stroke-width="2">
                        <polyline points="6 9 12 15 18 9"></polyline>
                      </svg>
                    </button>
                  </div>
                </div>

                <div class="card-summary">
                  <div class="time-info">
                    <span class="time-icon">⏰</span>
                    <span>{{ formatItemTime(item.startTime, item.endTime) }}</span>
                  </div>
                  <div v-if="item.itemType === 1 && item.transportType"
                       class="transport-info">
                    <span class="transport-icon">🚗</span>
                    <span>{{ item.transportType }}</span>
                  </div>
                </div>

                <div class="card-details"
                     v-show="item.expanded">
                  <div class="details-grid">
                    <div class="detail-item">
                      <span class="detail-label">开始时间</span>
                      <span class="detail-value">{{ formatFullDateTime(item.startTime) }}</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">结束时间</span>
                      <span class="detail-value">{{ formatFullDateTime(item.endTime) }}</span>
                    </div>
                    <div class="detail-item"
                         v-if="item.itemType === 1">
                      <span class="detail-label">交通工具</span>
                      <span class="detail-value">{{ item.transportType || '未设置' }}</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">花费金额</span>
                      <span class="detail-value amount">¥{{ item.amount || 0 }}</span>
                    </div>
                  </div>
                  <div class="detail-actions">
                    <button class="action-btn edit"
                            @click.stop="editItem(item)">
                      <svg viewBox="0 0 24 24"
                           fill="none"
                           stroke="currentColor"
                           stroke-width="2">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                      </svg>
                      编辑
                    </button>
                    <button class="action-btn delete"
                            @click.stop="deleteItemById(item)">
                      <svg viewBox="0 0 24 24"
                           fill="none"
                           stroke="currentColor"
                           stroke-width="2">
                        <polyline points="3 6 5 6 21 6"></polyline>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                      </svg>
                      删除
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else
             class="empty-state">
          <div class="empty-icon">🗺️</div>
          <h4>还没有行程环节</h4>
          <p>点击上方"添加环节"按钮开始规划您的旅程</p>
        </div>
      </div>
    </div>

    <!-- 添加/编辑环节弹窗 -->
    <div v-if="showAddModal"
         class="modal-overlay"
         @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">
            <span class="title-icon">{{ editingItem ? '✏️' : '➕' }}</span>
            <h3>{{ editingItem ? '编辑行程环节' : '添加行程环节' }}</h3>
          </div>
          <button class="close-btn"
                  @click="closeModal">
            <svg viewBox="0 0 24 24"
                 fill="none"
                 stroke="currentColor"
                 stroke-width="2">
              <line x1="18"
                    y1="6"
                    x2="6"
                    y2="18"></line>
              <line x1="6"
                    y1="6"
                    x2="18"
                    y2="18"></line>
            </svg>
          </button>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>环节类型</label>
            <div class="type-switcher">
              <div v-for="type in itemTypes"
                   :key="type.value"
                   :class="['type-option', { active: form.itemType === type.value }]"
                   @click="selectType(type.value)">
                <span class="type-icon">{{ type.icon }}</span>
                <span class="type-label">{{ type.label }}</span>
              </div>
            </div>
          </div>

          <!-- 交通类型字段 -->
          <template v-if="form.itemType === 1">
            <div class="form-row">
              <div class="form-group">
                <label>出发地</label>
                <div class="input-wrapper">
                  <span class="input-icon">📍</span>
                  <input v-model="form.fromLocation"
                         type="text"
                         placeholder="请输入出发地"
                         required />
                </div>
              </div>
              <div class="form-group">
                <label>目的地</label>
                <div class="input-wrapper">
                  <span class="input-icon">🎯</span>
                  <input v-model="form.toLocation"
                         type="text"
                         placeholder="请输入目的地"
                         required />
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>交通工具</label>
                <div class="input-wrapper">
                  <span class="input-icon">🚗</span>
                  <input v-model="form.transportType"
                         type="text"
                         placeholder="如：飞机、高铁、汽车等" />
                </div>
              </div>
              <div class="form-group">
                <label>花费（元）</label>
                <div class="input-wrapper">
                  <span class="input-icon">💰</span>
                  <input v-model="form.amount"
                         type="number"
                         step="0.01"
                         placeholder="0.00"
                         min="0" />
                </div>
              </div>
            </div>
          </template>

          <!-- 游玩和住宿类型字段 -->
          <template v-else>
            <div class="form-group">
              <label>地点</label>
              <div class="input-wrapper">
                <span class="input-icon">📍</span>
                <input v-model="form.toLocation"
                       type="text"
                       placeholder="请输入地点"
                       required />
              </div>
            </div>
            <div class="form-group">
              <label>花费（元）</label>
              <div class="input-wrapper">
                <span class="input-icon">💰</span>
                <input v-model="form.amount"
                       type="number"
                       step="0.01"
                       placeholder="0.00"
                       min="0" />
              </div>
            </div>
          </template>

          <div class="form-row">
            <div class="form-group">
              <label>开始时间</label>
              <div class="input-wrapper">
                <span class="input-icon">🕐</span>
                <input v-model="form.startTime"
                       type="datetime-local"
                       required />
              </div>
              <!-- 快速时间选择 -->
              <div class="quick-time-buttons">
                <button type="button"
                        class="quick-time-btn"
                        @click="quickSetTime(8)">08:00</button>
                <button type="button"
                        class="quick-time-btn"
                        @click="quickSetTime(9)">09:00</button>
                <button type="button"
                        class="quick-time-btn"
                        @click="quickSetTime(12)">12:00</button>
                <button type="button"
                        class="quick-time-btn"
                        @click="quickSetTime(14)">14:00</button>
                <button type="button"
                        class="quick-time-btn"
                        @click="quickSetTime(18)">18:00</button>
              </div>
              <small class="hint">
                时间范围: {{ formatDate(plan?.startDate) }} 00:00 - {{ formatDate(plan?.endDate) }} 23:59
              </small>
            </div>
            <div class="form-group">
              <label>结束时间</label>
              <div class="input-wrapper">
                <span class="input-icon">🕐</span>
                <input v-model="form.endTime"
                       type="datetime-local"
                       required />
              </div>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button"
                    class="btn-secondary"
                    @click="closeModal">
              取消
            </button>
            <button type="submit"
                    class="btn-primary"
                    :disabled="submitting">
              <span v-if="submitting"
                    class="loading-spinner-small"></span>
              <span>{{ submitting ? '保存中...' : (editingItem ? '保存' : '添加') }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- 确认删除弹窗 -->
  <ConfirmDialog v-model:visible="showDeleteConfirm"
                 title="确认删除"
                 :message="`确定要删除这个${itemToDelete ? getTypeLabel(itemToDelete.itemType) : ''}环节吗？<br>删除后将无法恢复`"
                 icon="🗑️"
                 type="danger"
                 confirm-text="删除"
                 @confirm="confirmDeleteItem" />
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import {addTravelItem, deleteTravelItem, getTravelItems, getTravelPlans, updateTravelItem} from '@/api/travel'
import {decodeId} from '@/utils/idEncoder'

const route = useRoute()
const router = useRouter()
const planId = decodeId(route.params.id)

const plan = ref(null)
const planItems = ref([])
const showAddModal = ref(false)
const submitting = ref(false)
const editingItem = ref(null)

const itemTypes = [
  { value: 1, label: '交通', icon: '🚗' },
  { value: 2, label: '游玩', icon: '🎡' },
  { value: 3, label: '住宿', icon: '🏨' }
]

const form = ref({
  itemType: 1,
  fromLocation: '',
  toLocation: '',
  transportType: '',
  amount: '',
  startTime: '',
  endTime: ''
})

// 计算总花费
const totalAmount = computed(() => {
  return planItems.value.reduce((sum, item) => sum + (parseFloat(item.amount) || 0), 0).toFixed(2)
})

onMounted(() => {
  loadPlanDetail()
})

const loadPlanDetail = async () => {
  try {
    const res = await getTravelPlans()
    if (res.code === 200 && res.data) {
      const plans = Array.isArray(res.data) ? res.data : [res.data]
      plan.value = plans.find((p) => String(p.id) === String(planId))
      if (plan.value) {
        loadPlanItems()
      }
    }
  } catch (error) {
    console.error('加载行程详情失败:', error)
  }
}

const loadPlanItems = async () => {
  try {
    const res = await getTravelItems(planId)
    if (res.code === 200) {
      const items = res.data?.records || res.data || []
      planItems.value = items.map(item => ({ ...item, expanded: false }))
      planItems.value.sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
    }
  } catch (error) {
    console.error('加载行程环节失败:', error)
  }
}

const toggleExpand = (item) => {
  item.expanded = !item.expanded
}

const selectType = (type) => {
  form.value.itemType = type
  form.value.fromLocation = ''
  form.value.toLocation = ''
  form.value.transportType = ''
}

const editItem = (item) => {
  editingItem.value = item
  form.value = {
    itemType: item.itemType,
    fromLocation: item.fromLocation || '',
    toLocation: item.toLocation || '',
    transportType: item.transportType || '',
    amount: item.amount || '',
    startTime: formatDateTimeLocal(item.startTime),
    endTime: formatDateTimeLocal(item.endTime)
  }
  showAddModal.value = true
}

// 删除确认弹窗
const showDeleteConfirm = ref(false)
const itemToDelete = ref(null)

const deleteItemById = (item) => {
  itemToDelete.value = item
  showDeleteConfirm.value = true
}

const confirmDeleteItem = async () => {
  if (!itemToDelete.value) return

  try {
    const res = await deleteTravelItem([{ id: itemToDelete.value.id, travelPlanId: planId }])
    if (res.code === 200) {
      planItems.value = planItems.value.filter((i) => i.id !== itemToDelete.value.id)
    }
  } catch (error) {
    console.error('删除环节失败:', error)
  } finally {
    itemToDelete.value = null
  }
}

const handleSubmit = async () => {
  submitting.value = true

  const data = {
    ...form.value,
    travelPlanId: planId,
    amount: form.value.amount ? parseFloat(form.value.amount) : 0
  }

  try {
    if (editingItem.value) {
      data.id = editingItem.value.id
      const res = await updateTravelItem([data])
      if (res.code === 200) {
        await loadPlanItems()
        closeModal()
      }
    } else {
      const res = await addTravelItem([data])
      if (res.code === 200) {
        await loadPlanItems()
        closeModal()
      }
    }
  } catch (error) {
    console.error('保存环节失败:', error)
  } finally {
    submitting.value = false
  }
}

// 智能初始化时间
const initSmartTime = () => {
  if (!plan.value) return

  const planStartDate = new Date(plan.value.startDate)
  const planEndDate = new Date(plan.value.endDate)
  const now = new Date()

  let startTime

  if (planItems.value.length > 0) {
    // 如果有已有环节，默认开始时间为最后一个环节的结束时间
    const lastItem = planItems.value[planItems.value.length - 1]
    startTime = new Date(lastItem.endTime)
  } else {
    // 如果没有环节，默认开始时间为行程开始日期的早上 9:00
    startTime = new Date(planStartDate)
    startTime.setHours(9, 0, 0, 0)

    // 如果行程开始日期是今天或之前，且当前时间已过早上9点，则使用当前时间（整点或半点）
    if (startTime < now) {
      startTime = new Date(now)
      const minutes = startTime.getMinutes()
      // 向上取整到最近的30分钟
      if (minutes > 30) {
        startTime.setHours(startTime.getHours() + 1, 0, 0, 0)
      } else {
        startTime.setMinutes(30, 0, 0)
      }
    }
  }

  // 确保不超出行程结束日期
  if (startTime > planEndDate) {
    startTime = new Date(planEndDate)
    startTime.setHours(9, 0, 0, 0)
  }

  // 结束时间默认在开始时间后 2 小时
  const endTime = new Date(startTime.getTime() + 2 * 60 * 60 * 1000)

  form.value.startTime = formatDateTimeLocal(startTime)
  form.value.endTime = formatDateTimeLocal(endTime)
}

// 快速设置时间
const quickSetTime = (hours, minutes = 0) => {
  if (!plan.value || !form.value.startTime) return

  const planStartDate = new Date(plan.value.startDate)
  const currentStart = new Date(form.value.startTime)

  // 保持当前日期，只修改时间
  const newStart = new Date(currentStart)
  newStart.setHours(hours, minutes, 0, 0)

  // 如果新时间早于行程开始时间，使用行程开始日期
  if (newStart < planStartDate) {
    newStart.setTime(planStartDate.getTime())
    newStart.setHours(hours, minutes, 0, 0)
  }

  form.value.startTime = formatDateTimeLocal(newStart)

  // 结束时间默认在开始时间后 2 小时
  const endTime = new Date(newStart.getTime() + 2 * 60 * 60 * 1000)
  form.value.endTime = formatDateTimeLocal(endTime)
}

const openAddModal = () => {
  editingItem.value = null
  form.value = {
    itemType: 2, // 默认为游玩类型，最常用
    fromLocation: '',
    toLocation: '',
    transportType: '',
    amount: '',
    startTime: '',
    endTime: ''
  }
  initSmartTime()
  showAddModal.value = true
}

const closeModal = () => {
  showAddModal.value = false
  editingItem.value = null
  form.value = {
    itemType: 1,
    fromLocation: '',
    toLocation: '',
    transportType: '',
    amount: '',
    startTime: '',
    endTime: ''
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
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

const formatDateTimeLocal = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
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

const calculateDuration = (start, end) => {
  if (!start || !end) return ''
  const startDate = new Date(start)
  const endDate = new Date(end)
  const days = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1
  return `${days} 天`
}
</script>

<style scoped>
.travel-plan-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 50%, #a5d6a7 100%);
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px;
}

/* 行程标题卡片 */
.plan-header-card {
  background: white;
  border-radius: 24px;
  padding: 32px;
  margin-bottom: 32px;
  box-shadow: 0 8px 32px rgba(46, 125, 50, 0.1);
}

.plan-header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.plan-title-section h2 {
  color: #2e7d32;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 16px;
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
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border-radius: 12px;
  color: #558b2f;
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

.add-item-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(76, 175, 80, 0.3);
}

.add-item-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.4);
}

.btn-icon {
  font-size: 20px;
  font-weight: 400;
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
  margin-bottom: 0;
}

.timeline-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.timeline-dot {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 2;
}

.timeline-dot.transport {
  background: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
}

.timeline-dot.play {
  background: linear-gradient(135deg, #ab47bc 0%, #9c27b0 100%);
}

.timeline-dot.stay {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
}

.dot-icon {
  filter: grayscale(1) brightness(2);
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: linear-gradient(to bottom, #e0e0e0, #e0e0e0);
  margin: 8px 0;
}

.timeline-content {
  flex: 1;
  padding-bottom: 24px;
}

.content-card {
  background: #f8f9fa;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.content-card:hover {
  background: white;
  border-color: #e8f5e9;
  box-shadow: 0 4px 20px rgba(46, 125, 50, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.header-main {
  flex: 1;
}

.item-title {
  color: #2e7d32;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.arrow {
  color: #81c784;
  font-weight: 400;
}

.item-type-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.item-type-tag.transport {
  background: #e3f2fd;
  color: #1976d2;
}

.item-type-tag.play {
  background: #f3e5f5;
  color: #7b1fa2;
}

.item-type-tag.stay {
  background: #e8f5e9;
  color: #388e3c;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-amount {
  font-size: 18px;
  font-weight: 700;
  color: #e53935;
}

.expand-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: white;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.expand-btn svg {
  width: 16px;
  height: 16px;
  color: #78909c;
  transition: transform 0.3s ease;
}

.expand-btn.expanded svg {
  transform: rotate(180deg);
}

.card-summary {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.time-info,
.transport-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #78909c;
  font-size: 13px;
}

.time-icon,
.transport-icon {
  font-size: 14px;
}

.card-details {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: #78909c;
}

.detail-value {
  font-size: 14px;
  color: #37474f;
  font-weight: 500;
}

.detail-value.amount {
  color: #e53935;
  font-weight: 600;
}

.detail-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn svg {
  width: 14px;
  height: 14px;
}

.action-btn.edit {
  background: #e8f5e9;
  color: #4caf50;
}

.action-btn.edit:hover {
  background: #4caf50;
  color: white;
}

.action-btn.delete {
  background: #ffebee;
  color: #e53935;
}

.action-btn.delete:hover {
  background: #e53935;
  color: white;
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

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(8px);
  padding: 20px;
}

.modal {
  background: white;
  border-radius: 24px;
  padding: 32px;
  width: 100%;
  max-width: 560px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.modal-title .title-icon {
  font-size: 24px;
}

.modal-title h3 {
  color: #2e7d32;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.close-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: #e0e0e0;
}

.close-btn svg {
  width: 18px;
  height: 18px;
  color: #78909c;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #558b2f;
  font-size: 14px;
  font-weight: 500;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  font-size: 16px;
  z-index: 1;
}

.input-wrapper input {
  width: 100%;
  padding: 12px 14px 12px 42px;
  border: 2px solid #e8f5e9;
  border-radius: 12px;
  font-size: 14px;
  background: white;
  transition: all 0.3s ease;
}

.input-wrapper input:focus {
  outline: none;
  border-color: #66bb6a;
  box-shadow: 0 0 0 4px rgba(102, 187, 106, 0.1);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.hint {
  display: block;
  margin-top: 6px;
  color: #81c784;
  font-size: 12px;
}

.quick-time-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
  margin-bottom: 6px;
}

.quick-time-btn {
  padding: 6px 12px;
  border: 1px solid #c8e6c9;
  border-radius: 20px;
  background: white;
  color: #558b2f;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-time-btn:hover {
  background: #e8f5e9;
  border-color: #66bb6a;
}

.quick-time-btn:active {
  background: #66bb6a;
  color: white;
}

.type-switcher {
  display: flex;
  gap: 12px;
}

.type-option {
  flex: 1;
  padding: 16px;
  border: 2px solid #e8f5e9;
  border-radius: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.type-option:hover {
  border-color: #a5d6a7;
  background: #f1f8e9;
}

.type-option.active {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border-color: #4caf50;
}

.type-icon {
  font-size: 28px;
}

.type-label {
  font-size: 14px;
  font-weight: 500;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #e8f5e9;
}

.btn-secondary {
  padding: 12px 24px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background: #e0e0e0;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(76, 175, 80, 0.4);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.share-link-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e8f5e9;
  border-radius: 10px;
  font-size: 14px;
  background: white;
  color: #666;
}

@media (max-width: 768px) {
  .container {
    padding: 20px 16px;
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

  .form-row {
    grid-template-columns: 1fr;
  }

  .type-switcher {
    flex-direction: column;
  }

  .modal {
    padding: 24px 20px;
  }
}
</style>

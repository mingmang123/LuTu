<template>
  <div class="travel-plans-page">
    <div class="container">
      <div class="page-header">
        <h2>我的行程规划</h2>
        <button class="create-btn"
                @click="showCreateModal = true">+ 新建行程</button>
      </div>

      <div class="plans-grid">
        <div v-for="plan in travelPlans"
             :key="plan.id"
             class="plan-card">
          <h3 class="plan-title">{{ plan.title }}</h3>
          <div class="plan-info">
            <div class="info-row">
              <span class="icon">📅</span>
              <span class="label">开始日期:</span>
              <span class="value">{{ formatDate(plan.startDate) }}</span>
            </div>
            <div class="info-row">
              <span class="icon">📅</span>
              <span class="label">结束日期:</span>
              <span class="value">{{ formatDate(plan.endDate) }}</span>
            </div>
          </div>
          <div class="budget-row">
            <span class="icon">💰</span>
            <span class="budget-label">预算花费:</span>
            <span class="budget-value">¥{{ Math.round(Number(plan.totalAmount || 0)) }}</span>
          </div>
          <div class="plan-actions">
            <button class="view-btn"
                    @click="viewPlanDetail(plan.id)">查看详情</button>
            <button class="share-btn"
                    @click="openShareModal(plan)">分享</button>
            <button class="delete-btn"
                    @click="deletePlanById(plan)">删除</button>
          </div>
        </div>
      </div>

      <div v-if="travelPlans.length === 0"
           class="empty-state">
        <div class="empty-icon">📋</div>
        <p>暂无行程规划</p>
        <button class="create-btn"
                @click="showCreateModal = true">创建第一个行程</button>
      </div>
    </div>

    <!-- 创建行程弹窗 -->
    <div v-if="showCreateModal"
         class="modal-overlay"
         @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>新建行程</h3>
          <button class="close-btn"
                  @click="closeModal">&times;</button>
        </div>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="title">行程名称</label>
            <input v-model="form.title"
                   type="text"
                   id="title"
                   placeholder="请输入行程名称"
                   required />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="startDate">开始日期</label>
              <input v-model="form.startDate"
                     type="date"
                     id="startDate"
                     required />
            </div>
            <div class="form-group">
              <label for="endDate">结束日期</label>
              <input v-model="form.endDate"
                     type="date"
                     id="endDate"
                     required />
            </div>
          </div>

          <div class="modal-actions">
            <button type="button"
                    class="btn-secondary"
                    @click="closeModal">取消</button>
            <button type="submit"
                    class="btn-primary"
                    :disabled="submitting">
              {{ submitting ? '创建中...' : '创建' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 分享弹窗 -->
    <div v-if="showShareModal"
         class="modal-overlay"
         @click.self="closeShareModal">
      <div class="modal share-modal">
        <div class="modal-header">
          <div class="modal-title">
            <span class="title-icon">🔗</span>
            <h3>分享行程</h3>
          </div>
          <button class="close-btn"
                  @click="closeShareModal">
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

        <div class="share-content">
          <div v-if="shareLoading"
               class="share-loading">
            <div class="loading-spinner"></div>
            <p>正在生成分享链接...</p>
          </div>

          <div v-else-if="showLinkResult"
               class="share-result">
            <div class="share-success-icon">✨</div>
            <h4>分享链接已生成</h4>
            <p class="share-desc">复制下方链接分享给好友，他们可以通过链接查看您的行程</p>

            <div class="share-link-box">
              <input type="text"
                     :value="shareUrl"
                     readonly
                     class="share-link-input"
                     ref="shareLinkInput" />
              <button class="copy-btn"
                      @click="copyShareLink"
                      :class="{ copied: copied }">
                <span v-if="copied">✓ 已复制</span>
                <span v-else>📋 复制</span>
              </button>
            </div>

            <div class="share-code-display">
              <span class="code-label">分享码：</span>
              <span class="code-value">{{ shareCode }}</span>
            </div>

            <!-- 分享状态管理 -->
            <div class="share-status-section">
              <div class="status-header">
                <span class="status-label">分享状态</span>
                <span :class="['status-badge', shareStatus === 1 ? 'active' : 'inactive']">
                  {{ shareStatus === 1 ? '✓ 分享中' : '✗ 已关闭' }}
                </span>
              </div>
              <div class="status-actions">
                <button v-if="shareStatus === 1"
                        class="btn-disable"
                        @click="toggleShareStatus(0)"
                        :disabled="statusLoading">
                  <span v-if="statusLoading">处理中...</span>
                  <span v-else>🔒 关闭分享</span>
                </button>
                <button v-else
                        class="btn-enable"
                        @click="toggleShareStatus(1)"
                        :disabled="statusLoading">
                  <span v-if="statusLoading">处理中...</span>
                  <span v-else>🔓 开启分享</span>
                </button>
              </div>
            </div>

            <div class="share-tips">
              <p v-if="shareStatus === 1">💡 提示：关闭分享后，他人将无法通过分享链接查看您的行程</p>
              <p v-else>⚠️ 分享已关闭，他人无法查看此行程</p>
            </div>

            <div class="form-actions">
              <button class="btn-secondary"
                      @click="showLinkResult = false">
                ← 返回
              </button>
            </div>
          </div>

          <div v-else-if="showNewShareForm"
               class="share-form">
            <div class="share-form-header">
              <div class="share-icon-large">✨</div>
              <h4>{{ isEditingShare ? '编辑分享' : '创建精彩分享' }}</h4>
              <p class="share-desc">填写分享信息，让更多人看到您的旅程</p>
            </div>

            <div class="form-content">
              <div class="form-group">
                <label>分享标题 <span class="required">*</span></label>
                <input v-model="shareForm.title"
                       type="text"
                       placeholder="给你的分享起个吸引人的标题"
                       maxlength="50" />
                <span class="char-count">{{ shareForm.title.length }}/50</span>
              </div>

              <div class="form-group">
                <label>分享描述</label>
                <textarea v-model="shareForm.description"
                          placeholder="介绍一下这次旅行的亮点、感受..."
                          maxlength="200"
                          rows="3"></textarea>
                <span class="char-count">{{ shareForm.description.length }}/200</span>
              </div>

              <div class="form-group">
                <label>封面图片</label>
                <div class="cover-upload">
                  <div v-if="shareForm.coverImage"
                       class="cover-preview">
                    <img :src="shareForm.coverImage"
                         alt="封面">
                    <button class="remove-cover"
                            @click="shareForm.coverImage = ''">×</button>
                  </div>
                  <div v-else
                       class="upload-area"
                       @click="$refs.coverInput.click()">
                    <div class="upload-icon">📷</div>
                    <span>点击上传封面图</span>
                    <small>支持 JPG、PNG，最大 10MB</small>
                  </div>
                  <input ref="coverInput"
                         type="file"
                         accept="image/*"
                         style="display: none"
                         @change="handleCoverUpload" />
                </div>
              </div>

              <div class="form-group">
                <label>标签</label>
                <input v-model="shareForm.tags"
                       type="text"
                       placeholder="添加标签，用逗号分隔，如：自由行,亲子游,美食" />
                <div class="tag-tips">
                  <span class="tag-suggestion"
                        @click="addTag('自由行')">自由行</span>
                  <span class="tag-suggestion"
                        @click="addTag('亲子游')">亲子游</span>
                  <span class="tag-suggestion"
                        @click="addTag('美食')">美食</span>
                  <span class="tag-suggestion"
                        @click="addTag('摄影')">摄影</span>
                  <span class="tag-suggestion"
                        @click="addTag('徒步')">徒步</span>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button class="btn-secondary"
                      @click="showNewShareForm = false"
                      :disabled="shareLoading">
                取消
              </button>
              <button class="btn-primary"
                      @click="submitNewShare"
                      :disabled="!shareForm.title || shareLoading">
                <span v-if="shareLoading">保存中...</span>
                <span v-else>{{ isEditingShare ? '保存修改' : '创建分享' }}</span>
              </button>
            </div>
          </div>

          <div v-else
               class="share-init">
            <div class="share-icon-large">🎁</div>
            <h4>分享行程</h4>
            <p class="share-desc">将「{{ sharingPlan?.title }}」分享给好友，让他们也能看到您的精彩旅程</p>

            <!-- 分享状态管理（提前显示） -->
            <div v-if="hasExistingShare || shareCode"
                 class="share-status-section init-status">
              <div class="status-header">
                <span class="status-label">分享状态</span>
                <span :class="['status-badge', shareStatus === 1 ? 'active' : 'inactive']">
                  {{ shareStatus === 1 ? '✓ 分享中' : '✗ 已关闭' }}
                </span>
              </div>
              <div class="status-actions">
                <button v-if="shareStatus === 1"
                        class="btn-disable"
                        @click="toggleShareStatus(0)"
                        :disabled="statusLoading">
                  <span v-if="statusLoading">处理中...</span>
                  <span v-else>🔒 关闭分享</span>
                </button>
                <button v-else
                        class="btn-enable"
                        @click="toggleShareStatus(1)"
                        :disabled="statusLoading">
                  <span v-if="statusLoading">处理中...</span>
                  <span v-else>🔓 开启分享</span>
                </button>
              </div>
            </div>

            <div class="share-options">
              <button v-if="!hasExistingShare && !shareCode"
                      class="btn-primary generate-btn"
                      @click="showNewShareForm = true">
                ✨ 创建精美分享
              </button>
              <button v-else-if="hasExistingShare"
                      class="btn-primary generate-btn"
                      @click="editExistingShare">
                ✏️ 再次编辑
              </button>
              <button class="btn-secondary generate-btn"
                      @click="generateShare"
                      :disabled="shareLoading">
                🔗 快速生成链接
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 确认删除弹窗 -->
  <ConfirmDialog v-model:visible="showDeleteConfirm"
                 title="确认删除"
                 :message="`确定要删除「${planToDelete?.title}」吗？<br>删除后将无法恢复`"
                 icon="🗑️"
                 type="danger"
                 confirm-text="删除"
                 @confirm="confirmDeletePlan" />
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import { getTravelPlans, createTravelPlan, deleteTravelPlan } from '@/api/travel'
import { generateShareCode, updateShareStatus, queryShareStatus, createShare, updateShare } from '@/api/travelPlanShare'
import { uploadImage } from '@/api/file'
import { encodeId } from '@/utils/idEncoder'

const router = useRouter()
const travelPlans = ref([])
const showCreateModal = ref(false)
const submitting = ref(false)

const form = ref({
  title: '',
  startDate: '',
  endDate: ''
})

// 分享相关
const showShareModal = ref(false)
const shareLoading = ref(false)
const shareCode = ref('')
const copied = ref(false)
const sharingPlan = ref(null)
const shareLinkInput = ref(null)
const shareStatus = ref(1)
const shareId = ref(null)
const statusLoading = ref(false)
const showNewShareForm = ref(false)
const hasExistingShare = ref(false)
const isEditingShare = ref(false)
const existingShareData = ref(null)
const showLinkResult = ref(false)

// 新分享表单
const shareForm = ref({
  title: '',
  description: '',
  coverImage: '',
  tags: ''
})
const uploading = ref(false)

const shareUrl = computed(() => {
  if (!shareCode.value) return ''
  return `${window.location.origin}/share-detail/${shareCode.value}`
})

onMounted(() => {
  loadTravelPlans()
})

const loadTravelPlans = async () => {
  try {
    const res = await getTravelPlans()
    if (res.code === 200) {
      travelPlans.value = res.data || []
    }
  } catch (error) {
    console.error('加载行程列表失败:', error)
  }
}

const viewPlanDetail = (id) => {
  router.push(`/travel-plan/${encodeId(id)}`)
}

// 删除确认弹窗
const showDeleteConfirm = ref(false)
const planToDelete = ref(null)

const deletePlanById = (plan) => {
  planToDelete.value = plan
  showDeleteConfirm.value = true
}

const confirmDeletePlan = async () => {
  if (!planToDelete.value) return

  try {
    const res = await deleteTravelPlan(planToDelete.value.id)
    if (res.code === 200) {
      travelPlans.value = travelPlans.value.filter((p) => p.id !== planToDelete.value.id)
    }
  } catch (error) {
    console.error('删除行程失败:', error)
  } finally {
    planToDelete.value = null
  }
}

const handleSubmit = async () => {
  submitting.value = true

  try {
    const res = await createTravelPlan(form.value)
    if (res.code === 200) {
      await loadTravelPlans()
      closeModal()
    }
  } catch (error) {
    console.error('创建行程失败:', error)
  } finally {
    submitting.value = false
  }
}

const closeModal = () => {
  showCreateModal.value = false
  form.value = {
    title: '',
    startDate: '',
    endDate: ''
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 分享相关方法
const openShareModal = async (plan) => {
  sharingPlan.value = plan
  showShareModal.value = true
  shareCode.value = ''
  copied.value = false
  shareStatus.value = 1
  hasExistingShare.value = false
  isEditingShare.value = false
  existingShareData.value = null
  showLinkResult.value = false
  showNewShareForm.value = false

  // 查询该行程的分享状态
  try {
    const res = await queryShareStatus(plan.id)
    if (res.code === 200 && res.data) {
      // 注意：status 可能为 0（关闭），不能用 || 判断
      shareStatus.value = res.data.status !== undefined ? res.data.status : 1
      if (res.data.shareCode) {
        shareCode.value = res.data.shareCode
      }
      // 检查是否已有精美分享
      if (res.data.title) {
        hasExistingShare.value = true
        existingShareData.value = res.data
      }
    }
  } catch (error) {
    // 没有分享记录，保持初始状态
    console.log('查询分享状态:', error)
  }
}

// 编辑已有分享
const editExistingShare = () => {
  if (!existingShareData.value) return

  // 如果 existingShareData 有 title，说明已有精美分享，是编辑模式
  // 如果只有 shareCode，说明是快速生成的链接，是创建模式
  isEditingShare.value = !!existingShareData.value.title

  shareForm.value = {
    title: existingShareData.value.title || '',
    description: existingShareData.value.description || '',
    coverImage: existingShareData.value.coverImage || '',
    tags: existingShareData.value.tags || ''
  }
  showNewShareForm.value = true
}

const closeShareModal = () => {
  showShareModal.value = false
  sharingPlan.value = null
  shareCode.value = ''
  copied.value = false
  shareStatus.value = 1
  shareId.value = null
  showNewShareForm.value = false
  hasExistingShare.value = false
  isEditingShare.value = false
  existingShareData.value = null
  showLinkResult.value = false
  shareForm.value = {
    title: '',
    description: '',
    coverImage: '',
    tags: ''
  }
}

// 添加标签
const addTag = (tag) => {
  const currentTags = shareForm.value.tags.split(',').map(t => t.trim()).filter(t => t)
  if (!currentTags.includes(tag)) {
    currentTags.push(tag)
    shareForm.value.tags = currentTags.join(',')
  }
}

// 上传封面图
const handleCoverUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (file.size > 10 * 1024 * 1024) {
    alert('图片大小不能超过 10MB')
    return
  }

  uploading.value = true
  try {
    const res = await uploadImage(file)
    if (res.code === 200) {
      shareForm.value.coverImage = res.data
    } else {
      alert(res.msg || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    alert('上传失败，请重试')
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

// 提交新分享或更新分享
const submitNewShare = async () => {
  if (!sharingPlan.value || !shareForm.value.title) return

  shareLoading.value = true
  try {
    const data = {
      travelPlanId: sharingPlan.value.id,
      title: shareForm.value.title,
      description: shareForm.value.description,
      coverImage: shareForm.value.coverImage,
      tags: shareForm.value.tags
    }

    // 根据是否是编辑模式选择API
    const isEditing = isEditingShare.value
    const api = isEditing ? updateShare : createShare
    const res = await api(data)

    if (res.code === 200) {
      if (!isEditing) {
        shareCode.value = res.data
      }
      shareStatus.value = 1
      hasExistingShare.value = true
      existingShareData.value = { ...data, shareCode: shareCode.value }
      showNewShareForm.value = false
      isEditingShare.value = false
      alert(isEditing ? '修改成功！' : '创建分享成功！')
    } else {
      alert(res.msg || (isEditing ? '修改分享失败' : '创建分享失败'))
    }
  } catch (error) {
    console.error(isEditingShare.value ? '修改分享失败:' : '创建分享失败:', error)
    alert(isEditingShare.value ? '修改分享失败，请重试' : '创建分享失败，请重试')
  } finally {
    shareLoading.value = false
  }
}

const generateShare = async () => {
  if (!sharingPlan.value) return
  shareLoading.value = true
  try {
    const res = await generateShareCode(sharingPlan.value.id)
    if (res.code === 200) {
      shareCode.value = res.data
      shareStatus.value = 1
      // 快速生成链接也会创建分享记录，所以设置 hasExistingShare
      hasExistingShare.value = true
      existingShareData.value = { shareCode: res.data }
      // 显示链接结果页面
      showLinkResult.value = true
    } else {
      alert(res.msg || '生成分享链接失败')
    }
  } catch (error) {
    console.error('生成分享链接失败:', error)
    alert('生成分享链接失败，请重试')
  } finally {
    shareLoading.value = false
  }
}

const toggleShareStatus = async (status) => {
  if (!sharingPlan.value) return

  statusLoading.value = true
  try {
    const res = await updateShareStatus(sharingPlan.value.id, status)
    if (res.code === 200) {
      shareStatus.value = status
    } else {
      alert(res.msg || '更新分享状态失败')
    }
  } catch (error) {
    console.error('更新分享状态失败:', error)
    alert('更新分享状态失败，请重试')
  } finally {
    statusLoading.value = false
  }
}

const copyShareLink = async () => {
  try {
    await navigator.clipboard.writeText(shareUrl.value)
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch (err) {
    // 降级方案
    const input = shareLinkInput.value
    input.select()
    document.execCommand('copy')
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  }
}
</script>

<style scoped>
.travel-plans-page {
  min-height: 100%;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 40px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.page-header h2 {
  color: #2e7d32;
  font-size: 28px;
  font-weight: 600;
}

.create-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(76, 175, 80, 0.4);
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.plan-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(46, 125, 50, 0.1);
  transition: all 0.3s ease;
}

.plan-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(46, 125, 50, 0.15);
}

.plan-title {
  color: #2e7d32;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}

.plan-info {
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-row .icon {
  font-size: 14px;
}

.info-row .label {
  color: #78909c;
}

.info-row .value {
  color: #37474f;
}

.budget-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  border-radius: 12px;
  margin-bottom: 16px;
}

.budget-label {
  color: #e53935;
  font-size: 14px;
  font-weight: 500;
}

.budget-value {
  color: #e53935;
  font-size: 16px;
  font-weight: 600;
  margin-left: auto;
}

.plan-actions {
  display: flex;
  gap: 8px;
}

.view-btn,
.share-btn,
.delete-btn {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.view-btn {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
}

.view-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.share-btn {
  background: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
  color: white;
}

.share-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
}

.delete-btn {
  background: linear-gradient(135deg, #ef5350 0%, #e53935 100%);
  color: white;
}

.delete-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(229, 57, 53, 0.3);
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #81c784;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
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
  backdrop-filter: blur(5px);
}

.modal {
  background: white;
  border-radius: 20px;
  padding: 28px;
  width: 90%;
  max-width: 480px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.modal-header h3 {
  color: #2e7d32;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #81c784;
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: #4caf50;
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

.form-group input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e8f5e9;
  border-radius: 12px;
  font-size: 15px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #66bb6a;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 187, 106, 0.1);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
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
  padding: 12px 24px;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
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

/* 分享弹窗样式 */
.share-modal {
  max-width: 480px;
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-title .title-icon {
  font-size: 22px;
}

.modal-title h3 {
  color: #2e7d32;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.close-btn svg {
  width: 24px;
  height: 24px;
}

.share-content {
  padding: 10px 0;
}

.share-loading {
  text-align: center;
  padding: 40px 20px;
}

.share-loading p {
  color: #666;
  margin-top: 16px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid #e8f5e9;
  border-top-color: #4caf50;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.share-init {
  text-align: center;
  padding: 20px;
}

.share-icon-large {
  font-size: 64px;
  margin-bottom: 16px;
}

.share-init h4 {
  color: #2e7d32;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.share-desc {
  color: #78909c;
  font-size: 14px;
  margin-bottom: 24px;
}

.generate-btn {
  width: 100%;
  justify-content: center;
  padding: 16px;
  font-size: 16px;
}

.share-result {
  text-align: center;
  padding: 10px 0;
}

.share-success-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.share-result h4 {
  color: #2e7d32;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.share-link-box {
  display: flex;
  gap: 12px;
  margin: 24px 0;
  background: #f8f9fa;
  padding: 12px;
  border-radius: 12px;
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

.copy-btn {
  padding: 12px 20px;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.copy-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.copy-btn.copied {
  background: linear-gradient(135deg, #81c784 0%, #66bb6a 100%);
}

/* 分享状态管理 */
.share-status-section {
  margin: 24px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.status-label {
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.status-badge {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.status-badge.active {
  background: #e8f5e9;
  color: #4caf50;
}

.status-badge.inactive {
  background: #ffebee;
  color: #e53935;
}

.status-actions {
  display: flex;
  justify-content: center;
}

.btn-disable,
.btn-enable {
  padding: 12px 28px;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-disable {
  background: linear-gradient(135deg, #ef5350 0%, #e53935 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(229, 57, 53, 0.3);
}

.btn-disable:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(229, 57, 53, 0.4);
}

.btn-enable {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.btn-enable:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(76, 175, 80, 0.4);
}

.btn-disable:disabled,
.btn-enable:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.share-code-display {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #e8f5e9;
  border-radius: 10px;
  margin-bottom: 16px;
}

.code-label {
  color: #558b2f;
  font-size: 14px;
}

.code-value {
  color: #2e7d32;
  font-size: 18px;
  font-weight: 700;
  font-family: monospace;
  letter-spacing: 2px;
}

.share-tips {
  padding: 16px;
  background: #fff8e1;
  border-radius: 10px;
  border-left: 4px solid #ffc107;
}

.share-tips p {
  color: #f57c00;
  font-size: 13px;
  margin: 0;
}

/* 新分享表单样式 */
.share-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 初始界面的分享状态 */
.share-status-section.init-status {
  margin: 16px 0;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.share-status-section.init-status .status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.share-status-section.init-status .status-actions {
  display: flex;
  justify-content: center;
}

.share-status-section.init-status .btn-disable,
.share-status-section.init-status .btn-enable {
  padding: 10px 24px;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.share-status-section.init-status .btn-disable {
  background: linear-gradient(135deg, #ef5350 0%, #e53935 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(229, 57, 53, 0.3);
}

.share-status-section.init-status .btn-enable {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.share-form {
  padding: 0;
}

.share-form-header {
  text-align: center;
  margin-bottom: 20px;
}

.share-form-header h4 {
  color: #2e7d32;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.form-content {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 8px;
}

.form-content .form-group {
  margin-bottom: 16px;
}

.form-content .form-group label {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
  color: #558b2f;
  font-size: 14px;
  font-weight: 500;
}

.form-content .form-group .required {
  color: #e53935;
}

.form-content .form-group input,
.form-content .form-group textarea {
  width: 100%;
  padding: 12px 14px;
  border: 2px solid #e8f5e9;
  border-radius: 10px;
  font-size: 14px;
  background: #fafafa;
  transition: all 0.3s ease;
  resize: none;
}

.form-content .form-group input:focus,
.form-content .form-group textarea:focus {
  outline: none;
  border-color: #66bb6a;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 187, 106, 0.1);
}

.char-count {
  display: block;
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.cover-upload {
  border: 2px dashed #e8f5e9;
  border-radius: 12px;
  overflow: hidden;
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-area:hover {
  background: #f8f9fa;
  border-color: #66bb6a;
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.upload-area span {
  color: #558b2f;
  font-size: 14px;
  font-weight: 500;
}

.upload-area small {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.cover-preview {
  position: relative;
  width: 100%;
  height: 160px;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-cover {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-cover:hover {
  background: rgba(0, 0, 0, 0.7);
}

.tag-tips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.tag-suggestion {
  padding: 6px 12px;
  background: #e8f5e9;
  color: #558b2f;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tag-suggestion:hover {
  background: #66bb6a;
  color: white;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e8f5e9;
}

.form-actions .btn-secondary,
.form-actions .btn-primary {
  flex: 1;
  padding: 14px;
  font-size: 15px;
}

@media (max-width: 768px) {
  .container {
    padding: 24px 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .plans-grid {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>

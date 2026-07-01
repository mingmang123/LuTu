<template>
  <div class="user-profile-page">
    <div class="container">
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar-wrapper">
            <div class="avatar"
                 @click="triggerAvatarUpload">
              <img v-if="userStore.userInfo.avatar"
                   :src="userStore.userInfo.avatar"
                   alt="头像" />
              <span v-else>👤</span>
              <div class="avatar-overlay">
                <span class="edit-icon">📷</span>
              </div>
            </div>
            <input ref="avatarInput"
                   type="file"
                   accept="image/*"
                   style="display: none"
                   @change="handleAvatarChange" />
          </div>
          <h2>{{ userStore.userInfo.username || '用户' }}</h2>
          <p class="email">{{ userStore.userInfo.email || '' }}</p>
        </div>

        <div class="profile-sections">
          <!-- 基本信息 -->
          <div class="section">
            <h3>基本信息</h3>

            <!-- 查看模式 -->
            <div class="info-list">
              <div class="info-item">
                <span class="label">用户名</span>
                <div class="value-wrapper">
                  <span class="value">{{ userStore.userInfo.username || '未设置' }}</span>
                  <button type="button"
                          class="btn-edit-inline"
                          @click="toggleEditUsername">修改</button>
                </div>
              </div>
              <div class="info-item">
                <span class="label">邮箱</span>
                <div class="value-wrapper">
                  <span class="value">{{ userStore.userInfo.email || '' }}</span>
                  <span class="btn-placeholder"></span>
                </div>
              </div>
            </div>

            <!-- 编辑模式 -->
            <div v-if="isEditingUsername"
                 class="username-edit">
              <div class="form-group">
                <label>新用户名</label>
                <input v-model="usernameForm.username"
                       type="text"
                       placeholder="请输入新用户名"
                       maxlength="20"
                       @blur="validateUsername" />
                <span class="char-count">{{ (usernameForm.username || '').length }}/20</span>
              </div>
              <div v-if="usernameError"
                   class="error-message">{{ usernameError }}</div>
              <div class="form-tips">
                <p>• 用户名长度2-20个字符</p>
                <p>• 支持中文、字母、数字和下划线</p>
              </div>
              <div class="form-actions">
                <button type="button"
                        class="btn-cancel"
                        @click="toggleEditUsername">取消</button>
                <button type="button"
                        class="btn-save"
                        @click="saveUsername"
                        :disabled="updatingUsername || !usernameForm.username">
                  {{ updatingUsername ? '保存中...' : '保存' }}
                </button>
              </div>
            </div>
          </div>

          <!-- 旅行偏好设置 -->
          <div class="section">
            <div class="section-header">
              <h3>旅行偏好设置</h3>
              <button class="btn-edit"
                      @click="toggleEditPortrait">
                {{ isEditingPortrait ? '取消' : '编辑偏好' }}
              </button>
            </div>

            <!-- 查看模式 -->
            <div v-if="!isEditingPortrait"
                 class="portrait-display">
              <div class="portrait-item">
                <span class="portrait-label">同行人群</span>
                <span :class="['portrait-value', { 'not-set': !portrait.crowdType }]">
                  {{ getCrowdTypeLabel(portrait.crowdType) }}
                  <span v-if="!portrait.crowdType"
                        class="required-tag">必填</span>
                </span>
              </div>
              <div class="portrait-item">
                <span class="portrait-label">旅行风格</span>
                <span :class="['portrait-value', { 'not-set': !portrait.travelStyle }]">
                  {{ getTravelStyleLabel(portrait.travelStyle) }}
                </span>
              </div>
              <div class="portrait-item">
                <span class="portrait-label">预算档位</span>
                <span :class="['portrait-value', { 'not-set': !portrait.budgetStyle }]">
                  {{ getBudgetStyleLabel(portrait.budgetStyle) }}
                </span>
              </div>
              <div class="portrait-item">
                <span class="portrait-label">旅行节奏</span>
                <span :class="['portrait-value', { 'not-set': !portrait.travelRhythm }]">
                  {{ getTravelRhythmLabel(portrait.travelRhythm) }}
                </span>
              </div>
            </div>

            <!-- 编辑模式 -->
            <div v-else
                 class="portrait-edit">
              <!-- 同行人群 - 必填 -->
              <div class="form-group required">
                <label>
                  同行人群
                  <span class="required-mark">*</span>
                </label>
                <div class="select-options">
                  <div v-for="option in crowdOptions"
                       :key="option.value"
                       :class="['select-option', { selected: portraitForm.crowdType === option.value }]"
                       @click="portraitForm.crowdType = option.value">
                    <span class="option-icon">{{ option.icon }}</span>
                    <span class="option-text">{{ option.label }}</span>
                  </div>
                </div>
              </div>

              <!-- 旅行风格 -->
              <div class="form-group">
                <label>旅行风格</label>
                <div class="select-options">
                  <div v-for="option in travelStyleOptions"
                       :key="option.value"
                       :class="['select-option', { selected: portraitForm.travelStyle === option.value }]"
                       @click="portraitForm.travelStyle = option.value">
                    <span class="option-icon">{{ option.icon }}</span>
                    <span class="option-text">{{ option.label }}</span>
                  </div>
                </div>
              </div>

              <!-- 预算档位 -->
              <div class="form-group">
                <label>预算档位</label>
                <div class="select-options">
                  <div v-for="option in budgetOptions"
                       :key="option.value"
                       :class="['select-option', { selected: portraitForm.budgetStyle === option.value }]"
                       @click="portraitForm.budgetStyle = option.value">
                    <span class="option-icon">{{ option.icon }}</span>
                    <span class="option-text">{{ option.label }}</span>
                  </div>
                </div>
              </div>

              <!-- 旅行节奏 -->
              <div class="form-group">
                <label>旅行节奏</label>
                <div class="select-options">
                  <div v-for="option in rhythmOptions"
                       :key="option.value"
                       :class="['select-option', { selected: portraitForm.travelRhythm === option.value }]"
                       @click="portraitForm.travelRhythm = option.value">
                    <span class="option-icon">{{ option.icon }}</span>
                    <span class="option-text">{{ option.label }}</span>
                  </div>
                </div>
              </div>

              <div v-if="portraitError"
                   class="error-message">{{ portraitError }}</div>
              <div v-if="portraitSuccess"
                   class="success-message">{{ portraitSuccess }}</div>

              <button class="btn-primary"
                      :disabled="updatingPortrait"
                      @click="savePortrait">
                {{ updatingPortrait ? '保存中...' : '保存偏好' }}
              </button>
            </div>
          </div>

          <!-- 修改密码 -->
          <div class="section password-section">
            <div class="password-header">
              <h3>修改密码</h3>
              <button class="btn-toggle-password"
                      @click="showPasswordForm = !showPasswordForm">
                {{ showPasswordForm ? '取消' : '修改密码' }}
              </button>
            </div>
            <form v-if="showPasswordForm"
                  @submit.prevent="handleUpdatePassword"
                  class="password-form">
              <div class="form-group">
                <label>原密码</label>
                <input v-model="passwordForm.password"
                       type="password"
                       placeholder="请输入原密码"
                       required />
              </div>
              <div class="form-group">
                <label>新密码</label>
                <input v-model="passwordForm.newPassword"
                       type="password"
                       placeholder="请输入新密码"
                       required />
              </div>
              <div class="form-group">
                <label>确认新密码</label>
                <input v-model="passwordForm.confirmPassword"
                       type="password"
                       placeholder="请再次输入新密码"
                       required />
              </div>
              <div v-if="passwordError"
                   class="error-message">{{ passwordError }}</div>
              <div v-if="passwordSuccess"
                   class="success-message">{{ passwordSuccess }}</div>
              <div class="password-actions">
                <button type="submit"
                        class="btn-primary"
                        :disabled="updatingPassword">
                  {{ updatingPassword ? '修改中...' : '确认修改' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Toast 消息提示 -->
  <MessageToast v-model:visible="showToast"
                :message="toastMessage"
                :type="toastType"
                :icon="toastIcon" />
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { updatePassword, uploadAvatar, updateUsername } from '@/api/user'
import { getUserPortrait, updateUserPortrait, addUserPortrait } from '@/api/userPortrait'
import MessageToast from '@/components/MessageToast.vue'

const userStore = useUserStore()

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

// 用户名编辑相关
const isEditingUsername = ref(false)
const updatingUsername = ref(false)
const usernameError = ref('')
const usernameForm = reactive({
  username: ''
})

// 切换用户名编辑模式
const toggleEditUsername = () => {
  isEditingUsername.value = !isEditingUsername.value
  if (isEditingUsername.value) {
    // 进入编辑模式时，填充当前用户名
    usernameForm.username = userStore.userInfo.username || ''
    usernameError.value = ''
  }
}

// 验证用户名
const validateUsername = () => {
  const username = usernameForm.username.trim()

  if (!username) {
    usernameError.value = '用户名不能为空'
    return false
  }

  if (username.length < 2 || username.length > 20) {
    usernameError.value = '用户名长度必须在2-20个字符之间'
    return false
  }

  // 只能包含中文、字母、数字和下划线
  if (!/[\u4e00-\u9fa5a-zA-Z0-9_]+$/.test(username)) {
    usernameError.value = '用户名只能包含中文、字母、数字和下划线'
    return false
  }

  usernameError.value = ''
  return true
}

// 保存用户名
const saveUsername = async () => {
  if (!validateUsername()) return

  updatingUsername.value = true
  usernameError.value = ''

  try {
    const res = await updateUsername(usernameForm.username.trim())
    if (res.code === 200) {
      // 更新本地用户信息
      const updatedUserInfo = { ...userStore.userInfo, username: usernameForm.username.trim() }
      userStore.setUserInfo(updatedUserInfo)
      isEditingUsername.value = false
      showMessage('用户名修改成功', 'success', '✓')
    } else {
      usernameError.value = res.msg || '修改失败'
    }
  } catch (error) {
    usernameError.value = error.message || '修改失败'
  } finally {
    updatingUsername.value = false
  }
}

// 用户画像数据
const portrait = ref({
  crowdType: null,
  travelStyle: null,
  budgetStyle: null,
  travelRhythm: null
})

const portraitForm = reactive({
  crowdType: null,
  travelStyle: null,
  budgetStyle: null,
  travelRhythm: null
})

const isEditingPortrait = ref(false)
const updatingPortrait = ref(false)
const portraitError = ref('')
const portraitSuccess = ref('')
const hasPortrait = ref(false)

// 选项数据
const crowdOptions = [
  { value: 1, label: '独自旅行', icon: '🚶' },
  { value: 2, label: '情侣出游', icon: '💑' },
  { value: 3, label: '家庭亲子', icon: '👨‍👩‍👧‍👦' },
  { value: 4, label: '朋友结伴', icon: '👫' },
  { value: 5, label: '商务出差', icon: '💼' }
]

const travelStyleOptions = [
  { value: 1, label: '山水风光', icon: '⛰️' },
  { value: 2, label: '海滨度假', icon: '🏖️' },
  { value: 3, label: '古镇人文', icon: '🏮' },
  { value: 4, label: '城市探索', icon: '🏙️' },
  { value: 5, label: '小众秘境', icon: '🌿' }
]

const budgetOptions = [
  {
    value: 1, label: '性价比优先', icon: '💰'
  },
  {
    value: 2, label: '舒适平衡', icon: '💰💰'
  },
  { value: 3, label: '高端奢享', icon: '💰💰💰' }
]

const rhythmOptions = [
  { value: 1, label: '慢享休闲', icon: '😌' },
  {
    value: 2, label: '短途打卡', icon: '📸'
  },
  { value: 3, label: '正常游玩', icon: '🎯' }
]

// 获取标签文本
const getCrowdTypeLabel = (value) => {
  if (!value) return '未设置'
  const option = crowdOptions.find(o => o.value === value)
  return option ? `${option.icon} ${option.label}` : '未设置'
}

const getTravelStyleLabel = (value) => {
  if (!value) return '未设置'
  const option = travelStyleOptions.find(o => o.value === value)
  return option ? `${option.icon} ${option.label}` : '未设置'
}

const getBudgetStyleLabel = (value) => {
  if (!value) return '未设置'
  const option = budgetOptions.find(o => o.value === value)
  return option ? `${option.icon} ${option.label}` : '未设置'
}

const getTravelRhythmLabel = (value) => {
  if (!value) return '未设置'
  const option = rhythmOptions.find(o => o.value === value)
  return option ? `${option.icon} ${option.label}` : '未设置'
}

// 获取用户画像
const fetchUserPortrait = async () => {
  try {
    const res = await getUserPortrait()
    if (res.code === 200 && res.data) {
      portrait.value = { ...res.data }
      hasPortrait.value = true
    }
  } catch (err) {
    console.error('获取用户画像失败:', err)
  }
}

// 切换编辑模式
const toggleEditPortrait = () => {
  if (isEditingPortrait.value) {
    // 取消编辑，重置表单
    isEditingPortrait.value = false
    portraitError.value = ''
    portraitSuccess.value = ''
  } else {
    // 开始编辑，复制当前值到表单
    isEditingPortrait.value = true
    portraitForm.crowdType = portrait.value.crowdType
    portraitForm.travelStyle = portrait.value.travelStyle
    portraitForm.budgetStyle = portrait.value.budgetStyle
    portraitForm.travelRhythm = portrait.value.travelRhythm
  }
}

// 保存用户画像
const savePortrait = async () => {
  portraitError.value = ''
  portraitSuccess.value = ''

  // 验证同行人群必填
  if (!portraitForm.crowdType) {
    portraitError.value = '请选择同行人群'
    return
  }

  updatingPortrait.value = true
  try {
    let res
    if (hasPortrait.value) {
      res = await updateUserPortrait(portraitForm)
    } else {
      res = await addUserPortrait(portraitForm)
    }

    if (res.code === 200) {
      portrait.value = { ...portraitForm }
      hasPortrait.value = true
      portraitSuccess.value = '保存成功'
      setTimeout(() => {
        isEditingPortrait.value = false
        portraitSuccess.value = ''
      }, 1500)
    } else {
      portraitError.value = res.msg || '保存失败'
    }
  } catch (err) {
    portraitError.value = err.message || '网络错误，请重试'
  } finally {
    updatingPortrait.value = false
  }
}

// 修改密码表单
const passwordForm = reactive({
  password: '',
  newPassword: '',
  confirmPassword: ''
})

const updatingPassword = ref(false)
const passwordError = ref('')
const passwordSuccess = ref('')
const showPasswordForm = ref(false)

// 修改密码
const handleUpdatePassword = async () => {
  passwordError.value = ''
  passwordSuccess.value = ''

  if (!passwordForm.password) {
    passwordError.value = '请输入原密码'
    return
  }
  if (!passwordForm.newPassword) {
    passwordError.value = '请输入新密码'
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }
  if (passwordForm.newPassword.length < 6) {
    passwordError.value = '新密码长度不能少于6位'
    return
  }

  updatingPassword.value = true
  try {
    const res = await updatePassword({
      password: passwordForm.password,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      passwordSuccess.value = '密码修改成功'
      passwordForm.password = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      passwordError.value = res.msg || '密码修改失败'
    }
  } catch (error) {
    passwordError.value = error.message || '密码修改失败'
  } finally {
    updatingPassword.value = false
  }
}

// 头像上传
const avatarInput = ref(null)
const uploadingAvatar = ref(false)

const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    showMessage('请选择图片文件', 'warning', '⚠️')
    return
  }

  // 检查文件大小（最大 5MB）
  if (file.size > 5 * 1024 * 1024) {
    showMessage('图片大小不能超过 5MB', 'warning', '⚠️')
    return
  }

  uploadingAvatar.value = true
  try {
    // 调用上传 API
    const res = await uploadAvatar(file)
    if (res.code === 200) {
      // 更新用户信息并持久化到 localStorage
      const updatedUserInfo = { ...userStore.userInfo, avatar: res.data }
      userStore.setUserInfo(updatedUserInfo)
      showMessage('头像上传成功', 'success', '✓')
    } else {
      showMessage(res.msg || '上传失败', 'error', '✗')
    }
  } catch (error) {
    showMessage('上传失败：' + error.message, 'error', '✗')
  } finally {
    uploadingAvatar.value = false
    // 清空 input，允许重复选择同一文件
    event.target.value = ''
  }
}

onMounted(() => {
  fetchUserPortrait()
})
</script>

<style scoped>
.user-profile-page {
  min-height: 100%;
  background: var(--bg-secondary);
  padding: 40px 0;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

.profile-card {
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
}

.profile-header {
  background: var(--accent-gradient);
  padding: 48px 40px;
  text-align: center;
  color: white;
  position: relative;
}

.profile-header::before {
  content: "";
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 16px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 50px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border: 3px solid rgba(255, 255, 255, 0.5);
  transition: all var(--transition-fast);
}

.avatar:hover {
  border-color: white;
  transform: scale(1.05);
  box-shadow: 0 0 30px rgba(255, 255, 255, 0.5);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(12, 74, 110, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
  border-radius: 50%;
}

.avatar:hover .avatar-overlay {
  opacity: 1;
}

.edit-icon {
  font-size: 28px;
  color: white;
}

.profile-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  position: relative;
}

.email {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
  position: relative;
}

.profile-sections {
  padding: 30px;
}

.section {
  margin-bottom: 30px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 24px;
  border: 1px solid var(--border-color);
}

.section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
  min-height: 40px;
}

.section h3 {
  color: var(--accent-primary);
  font-size: 18px;
  margin: 0;
}

.btn-edit {
  padding: 6px 16px;
  background: var(--accent-gradient);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: inline-block;
  visibility: visible;
  opacity: 1;
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);
}

.btn-edit:hover {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.4);
  transform: translateY(-1px);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
}

.info-item .label {
  color: var(--text-secondary);
  font-size: 14px;
}

.info-item .value {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.value-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 140px;
  justify-content: flex-end;
}

.btn-placeholder {
  width: 60px;
  height: 32px;
  visibility: hidden;
}

.btn-edit-inline {
  padding: 6px 14px;
  background: var(--accent-gradient);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);
}

.btn-edit-inline:hover {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.4);
  transform: translateY(-1px);
}

.btn-cancel {
  padding: 12px 24px;
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.btn-cancel:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

/* 用户画像展示 */
.portrait-display {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.portrait-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
}

.portrait-label {
  color: var(--text-muted);
  font-size: 12px;
}

.portrait-value {
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 500;
}

.portrait-value.not-set {
  color: var(--text-muted);
}

.required-tag {
  display: inline-block;
  margin-left: 8px;
  padding: 2px 8px;
  background: var(--accent-gradient);
  color: white;
  font-size: 11px;
  border-radius: 4px;
}

/* 用户画像编辑 */
.portrait-edit {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-group.required label {
  color: var(--accent-primary);
}

.form-group label {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.required-mark {
  color: var(--accent-primary);
}

.select-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 10px;
}

.select-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 10px;
  background: var(--bg-tertiary);
  border: 2px solid transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.select-option:hover {
  background: var(--bg-hover);
  transform: translateY(-2px);
}

.select-option.selected {
  background: var(--bg-card);
  border-color: var(--accent-primary);
  box-shadow: var(--shadow-glow);
}

.option-icon {
  font-size: 24px;
}

.option-text {
  font-size: 13px;
  color: var(--text-secondary);
  text-align: center;
}

.password-section {
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 20px;
  border: 1px solid var(--border-color);
}

.password-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.password-header h3 {
  margin: 0;
  font-size: 16px;
  color: var(--text-primary);
}

.btn-toggle-password {
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.btn-toggle-password:hover {
  border-color: var(--accent-primary);
  color: var(--accent-primary);
}

.password-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
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

.password-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.form-group input {
  padding: 12px 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 14px;
  transition: all var(--transition-fast);
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.form-group input:focus {
  outline: none;
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.15);
}

.form-group input::placeholder {
  color: var(--text-muted);
}

.error-message {
  color: #ff6b6b;
  font-size: 14px;
  padding: 10px 14px;
  background: rgba(255, 107, 107, 0.1);
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 107, 107, 0.3);
}

.success-message {
  color: #4ade80;
  font-size: 14px;
  padding: 10px 14px;
  background: rgba(74, 222, 128, 0.1);
  border-radius: var(--radius-sm);
  border: 1px solid rgba(74, 222, 128, 0.3);
}

.btn-primary {
  padding: 14px 28px;
  background: var(--accent-gradient);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-glow);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg), var(--shadow-glow);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 用户名编辑样式 */
.username-edit {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  margin-top: 16px;
  border: 1px solid var(--border-color);
}

.username-edit .form-group {
  position: relative;
}

.username-edit .form-group input {
  width: 100%;
  padding: 12px 16px;
  padding-right: 60px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 15px;
  transition: all var(--transition-fast);
  box-sizing: border-box;
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.username-edit .form-group input:focus {
  outline: none;
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.15);
}

.username-edit .form-group input::placeholder {
  color: var(--text-muted);
}

.username-edit .char-count {
  position: absolute;
  right: 16px;
  bottom: 12px;
  font-size: 12px;
  color: var(--text-muted);
}

.username-edit .form-tips {
  background: var(--bg-tertiary);
  padding: 12px 16px;
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--accent-primary);
}

.username-edit .form-tips p {
  margin: 4px 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.username-edit .form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.username-edit .btn-save {
  padding: 12px 24px;
  background: var(--accent-gradient);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-glow);
}

.username-edit .btn-save:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg), var(--shadow-glow);
}

.username-edit .btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .container {
    padding: 20px 12px;
  }

  .profile-header {
    padding: 30px 20px;
  }

  .profile-sections {
    padding: 20px;
  }

  .portrait-display {
    grid-template-columns: 1fr;
  }

  .select-options {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

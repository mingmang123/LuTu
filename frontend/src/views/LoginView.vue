<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <div class="logo">
          <svg viewBox="0 0 24 24"
               fill="none"
               xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"
                  fill="currentColor" />
          </svg>
        </div>
        <h1>途记</h1>
        <p>开启您的智能旅行规划之旅</p>
      </div>

      <!-- 登录方式切换 -->
      <div class="login-tabs">
        <button :class="['login-tab', { active: loginType === 'password' }]"
                @click="switchTab('password')">
          <svg class="tab-icon"
               viewBox="0 0 24 24"
               fill="none"
               xmlns="http://www.w3.org/2000/svg">
            <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"
                  fill="currentColor" />
          </svg>
          密码登录
        </button>
        <button :class="['login-tab', { active: loginType === 'captcha' }]"
                @click="switchTab('captcha')">
          <svg class="tab-icon"
               viewBox="0 0 24 24"
               fill="none"
               xmlns="http://www.w3.org/2000/svg">
            <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"
                  fill="currentColor" />
          </svg>
          邮箱注册
        </button>
      </div>

      <form class="login-form"
            @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="email">
            <svg class="label-icon"
                 viewBox="0 0 24 24"
                 fill="none"
                 xmlns="http://www.w3.org/2000/svg">
              <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"
                    fill="currentColor" />
            </svg>
            邮箱地址
          </label>
          <div class="input-wrapper">
            <input v-model="form.email"
                   type="email"
                   id="email"
                   placeholder="请输入邮箱地址"
                   required />
          </div>
        </div>

        <!-- 密码登录表单 -->
        <div v-if="loginType === 'password'"
             class="form-section">
          <div class="form-group">
            <label for="password">
              <svg class="label-icon"
                   viewBox="0 0 24 24"
                   fill="none"
                   xmlns="http://www.w3.org/2000/svg">
                <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"
                      fill="currentColor" />
              </svg>
              密码
            </label>
            <div class="input-wrapper">
              <input v-model="form.password"
                     :type="showPassword ? 'text' : 'password'"
                     id="password"
                     placeholder="请输入密码"
                     required />
              <button type="button"
                      class="toggle-password"
                      @click="showPassword = !showPassword">
                <svg v-if="showPassword"
                     viewBox="0 0 24 24"
                     fill="none"
                     xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"
                        fill="currentColor" />
                </svg>
                <svg v-else
                     viewBox="0 0 24 24"
                     fill="none"
                     xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z"
                        fill="currentColor" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- 验证码注册表单 -->
        <div v-else
             class="form-section">
          <div class="form-group verify-code">
            <label>
              <svg class="label-icon"
                   viewBox="0 0 24 24"
                   fill="none"
                   xmlns="http://www.w3.org/2000/svg">
                <path d="M21 3H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 13H3V5h18v11z"
                      fill="currentColor" />
              </svg>
              验证码
            </label>
            <div class="verify-input-wrapper">
              <input v-model="form.code"
                     type="text"
                     placeholder="请输入验证码"
                     maxlength="6"
                     required />
              <button type="button"
                      :disabled="countdown > 0"
                      @click="getVerifyCode"
                      class="code-btn">
                <span v-if="countdown === 0">
                  <svg class="btn-icon"
                       viewBox="0 0 24 24"
                       fill="none"
                       xmlns="http://www.w3.org/2000/svg">
                    <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"
                          fill="currentColor" />
                  </svg>
                  获取验证码
                </span>
                <span v-else>{{ countdown }}s</span>
              </button>
            </div>
          </div>
        </div>

        <div v-if="error"
             class="error-message">
          <svg class="message-icon"
               viewBox="0 0 24 24"
               fill="none"
               xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"
                  fill="currentColor" />
          </svg>
          {{ error }}
        </div>
        <div v-if="success"
             class="success-message">
          <svg class="message-icon"
               viewBox="0 0 24 24"
               fill="none"
               xmlns="http://www.w3.org/2000/svg">
            <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"
                  fill="currentColor" />
          </svg>
          {{ success }}
        </div>

        <button type="submit"
                class="login-btn"
                :disabled="loading">
          <span v-if="loading"
                class="loading-spinner"></span>
          <span v-else>
            <svg class="btn-icon"
                 viewBox="0 0 24 24"
                 fill="none"
                 xmlns="http://www.w3.org/2000/svg">
              <path d="M10.09 15.59L11.5 17l5-5-5-5-1.41 1.41L12.67 11H3v2h9.67l-2.58 2.59zM19 3H5c-1.11 0-2 .9-2 2v4h2V5h14v14H5v-4H3v4c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z"
                    fill="currentColor" />
            </svg>
          </span>
          {{ loading ? '登录中...' : submitButtonText }}
        </button>
      </form>

      <div class="login-footer">
        <p>登录即表示您同意我们的服务条款</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {captchaLogin, getUserInfo, login, sendVerifyCode} from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const loginType = ref('password')
const form = ref({
  email: '',
  password: '',
  code: ''
})
const loading = ref(false)
const error = ref('')
const success = ref('')
const countdown = ref(0)
const showPassword = ref(false)
let timer = null

const submitButtonText = computed(() => {
  return loginType.value === 'password' ? '登 录' : '注册并登录'
})

const switchTab = (type) => {
  loginType.value = type
  error.value = ''
  success.value = ''
  form.value.password = ''
  form.value.code = ''
}

const getVerifyCode = async () => {
  const email = form.value.email

  if (!email) {
    error.value = '请输入邮箱地址'
    return
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email)) {
    error.value = '请输入正确的邮箱地址'
    return
  }

  try {
    const res = await sendVerifyCode(email)
    if (res.code === 200) {
      success.value = '验证码已发送至邮箱'
      startCountdown()
    } else {
      error.value = res.msg || '获取验证码失败'
    }
  } catch (err) {
    error.value = err.message || '网络错误，请重试'
  }
}

const startCountdown = () => {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleLogin = async () => {
  loading.value = true
  error.value = ''
  success.value = ''

  // 登录前清除旧 token，防止残留的无效 token 干扰
  userStore.clearUser()
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')

  const email = form.value.email

  if (!email) {
    error.value = '请输入邮箱地址'
    loading.value = false
    return
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email)) {
    error.value = '请输入正确的邮箱地址'
    loading.value = false
    return
  }

  try {
    let res
    if (loginType.value === 'password') {
      if (!form.value.password) {
        error.value = '请输入密码'
        loading.value = false
        return
      }
      res = await login({
        email: form.value.email,
        password: form.value.password
      })
    } else {
      if (!form.value.code) {
        error.value = '请输入验证码'
        loading.value = false
        return
      }
      res = await captchaLogin({
        email: form.value.email,
        code: form.value.code
      })
    }

    if (res.code === 200) {
      // 存储 Access Token（JWT）到 store 和 localStorage
      const tokenData = res.data
      userStore.setToken(tokenData.accessToken || tokenData)
      localStorage.setItem('token', tokenData.accessToken || tokenData)
      // 存储 Refresh Token 到 localStorage（供刷新时使用）
      if (tokenData.refreshToken) {
        localStorage.setItem('refreshToken', tokenData.refreshToken)
      }
      success.value = '登录成功，正在跳转...'
      // 获取用户信息
      await fetchUserInfo()
      // 延迟跳转，确保状态更新完成
      setTimeout(() => {
        // 验证码登录（注册）跳转到用户画像引导页
        if (loginType.value === 'captcha') {
          router.push('/portrait-guide')
        } else {
          router.push('/dashboard')
        }
      }, 500)
    } else {
      error.value = res.msg || '登录失败'
    }
  } catch (err) {
    error.value = err.message || '网络错误，请重试'
  } finally {
    loading.value = false
  }
}

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      userStore.setUserInfo(res.data)
    }
  } catch (err) {
    console.error('获取用户信息失败:', err)
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(
    135deg,
    #f4a261 0%,
    #e76f51 30%,
    #2a9d8f 70%,
    #264653 100%
  );
}

.login-page::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(
      circle at 20% 80%,
      rgba(255, 255, 255, 0.15) 0%,
      transparent 50%
    ),
    radial-gradient(
      circle at 80% 20%,
      rgba(255, 255, 255, 0.1) 0%,
      transparent 50%
    ),
    radial-gradient(
      circle at 40% 40%,
      rgba(255, 255, 255, 0.08) 0%,
      transparent 40%
    );
  animation: float 20s ease-in-out infinite;
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(120deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(240deg);
  }
}

.login-container {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.15), 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 48px 40px;
  width: 100%;
  max-width: 440px;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #e9c46a 0%, #f4a261 50%, #e76f51 100%);
  border-radius: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 20px;
  box-shadow: 0 8px 32px rgba(231, 111, 81, 0.4),
    0 4px 16px rgba(244, 162, 97, 0.3);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.logo:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 12px 40px rgba(231, 111, 81, 0.5),
    0 6px 20px rgba(244, 162, 97, 0.4);
}

.logo svg {
  width: 40px;
  height: 40px;
  color: white;
}

.login-header h1 {
  color: #1a1a2e;
  font-size: 32px;
  margin-bottom: 8px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.login-header p {
  color: #6b7280;
  font-size: 15px;
  font-weight: 400;
}

/* 登录方式切换 Tab */
.login-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 28px;
  background: #f3f4f6;
  padding: 4px;
  border-radius: 14px;
}

.login-tab {
  flex: 1;
  padding: 12px 16px;
  background: transparent;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.tab-icon {
  width: 18px;
  height: 18px;
}

.login-tab:hover {
  color: #4b5563;
}

.login-tab.active {
  background: white;
  color: #e76f51;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08), 0 1px 3px rgba(0, 0, 0, 0.04);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
}

.label-icon {
  width: 16px;
  height: 16px;
  color: #6b7280;
}

.input-wrapper {
  position: relative;
}

.form-group input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fafafa;
  color: #1f2937;
}

.form-group input:hover {
  border-color: #d1d5db;
  background: #f9fafb;
}

.form-group input:focus {
  outline: none;
  border-color: #2a9d8f;
  background: white;
  box-shadow: 0 0 0 4px rgba(42, 157, 143, 0.15);
}

.form-group input::placeholder {
  color: #9ca3af;
}

.toggle-password {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: #9ca3af;
  transition: color 0.2s ease;
}

.toggle-password:hover {
  color: #6b7280;
}

.toggle-password svg {
  width: 20px;
  height: 20px;
}

/* 验证码输入框 */
.verify-code .verify-input-wrapper {
  display: flex;
  gap: 12px;
}

.verify-code input {
  flex: 1;
  text-align: center;
  letter-spacing: 4px;
  font-size: 18px;
  font-weight: 600;
}

.code-btn {
  padding: 14px 20px;
  background: linear-gradient(135deg, #e9c46a 0%, #f4a261 50%, #e76f51 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(231, 111, 81, 0.35);
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 120px;
  justify-content: center;
}

.code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(231, 111, 81, 0.45);
}

.code-btn:disabled {
  background: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-icon {
  width: 16px;
  height: 16px;
}

.error-message,
.success-message {
  margin-bottom: 20px;
  padding: 14px 16px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.error-message {
  background: rgba(239, 68, 68, 0.08);
  color: #dc2626;
  border: 1px solid rgba(239, 68, 68, 0.15);
}

.success-message {
  background: rgba(34, 197, 94, 0.08);
  color: #16a34a;
  border: 1px solid rgba(34, 197, 94, 0.15);
}

.login-btn {
  width: 100%;
  padding: 16px 24px;
  background: linear-gradient(135deg, #2a9d8f 0%, #264653 100%);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 16px rgba(42, 157, 143, 0.4),
    0 2px 8px rgba(38, 70, 83, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(42, 157, 143, 0.5),
    0 4px 12px rgba(38, 70, 83, 0.4);
}

.login-btn:disabled {
  background: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.loading-spinner {
  width: 20px;
  height: 20px;
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

.login-footer {
  margin-top: 24px;
  text-align: center;
}

.login-footer p {
  color: #9ca3af;
  font-size: 12px;
  font-weight: 400;
}

/* 响应式设计 */
@media (max-width: 640px) {
  .login-page {
    padding: 20px;
    background: linear-gradient(135deg, #f4a261 0%, #e76f51 50%, #2a9d8f 100%);
  }

  .login-page::before {
    display: none;
  }

  .login-container {
    width: 100%;
    max-width: 100%;
    padding: 32px 24px;
    border-radius: 20px;
  }

  .logo {
    width: 64px;
    height: 64px;
  }

  .logo svg {
    width: 32px;
    height: 32px;
  }

  .login-header h1 {
    font-size: 26px;
  }

  .login-header p {
    font-size: 14px;
  }

  .login-tab {
    padding: 10px 12px;
    font-size: 13px;
  }

  .tab-icon {
    width: 16px;
    height: 16px;
  }

  .form-group input {
    padding: 12px 14px;
    font-size: 16px;
  }

  .code-btn {
    padding: 12px 16px;
    font-size: 12px;
    min-width: 100px;
  }

  .login-btn {
    padding: 14px 20px;
    font-size: 15px;
  }
}

@media (max-width: 380px) {
  .login-container {
    padding: 28px 20px;
  }

  .verify-code .verify-input-wrapper {
    flex-direction: column;
    gap: 10px;
  }

  .code-btn {
    width: 100%;
  }
}
</style>

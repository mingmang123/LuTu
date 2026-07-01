<template>
  <div class="ai-chat-page">
    <div class="chat-container">
      <!-- 左侧会话列表 -->
      <div class="sessions-sidebar">
        <div class="sidebar-header">
          <h3>AI对话</h3>
          <button class="new-session-btn"
                  @click="openCreateModal">+ 新建会话</button>
        </div>
        <div class="sessions-list">
          <div v-for="session in sessions"
               :key="session.id"
               :class="['session-item', { active: currentSession?.id === session.id }]"
               @click="selectSession(session)">
            <span class="session-icon">💬</span>
            <span class="session-title">{{ session.title || '新会话' }}</span>
            <button class="delete-btn"
                    @click.stop="deleteSessionHandler(session.id)"
                    title="删除会话">
              ×
            </button>
          </div>
          <div v-if="sessions.length === 0"
               class="empty-sessions">
            暂无会话，点击上方按钮创建
          </div>
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-main">
        <div class="chat-messages"
             ref="messagesContainer">
          <div v-if="messages.length === 0"
               class="welcome-message">
            <div class="ai-avatar-large">🤖</div>
            <h3>AI 旅行助手</h3>
            <p>我是您的智能旅行规划助手，可以帮您：</p>
            <ul>
              <li>🗺️ 规划旅行路线</li>
              <li>🏨 推荐住宿和景点</li>
              <li>🍜 推荐当地美食</li>
              <li>💡 回答旅行相关问题</li>
            </ul>
            <p class="hint">在下方输入框开始对话吧！</p>
          </div>
          <div v-for="(message, index) in messages"
               :key="index"
               :class="['message', message.role]">
            <div class="message-content">
              <div v-if="message.role === 'assistant'"
                   class="ai-avatar">🤖</div>
              <div class="message-bubble"
                   v-html="formatMessage(message.content)"></div>
            </div>
          </div>
          <div v-if="loading"
               class="message assistant">
            <div class="message-content">
              <div class="ai-avatar">🤖</div>
              <div class="message-bubble typing">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-input-area">
          <div class="input-wrapper">
            <input v-model="inputMessage"
                   type="text"
                   placeholder="输入您的问题..."
                   @keyup.enter="sendMessageHandler"
                   :disabled="loading || isStreaming" />
            <button v-if="isStreaming"
                    class="send-btn stop-btn"
                    @click="stopChatHandler">
              <span>⏹ 停止</span>
            </button>
            <button v-else
                    class="send-btn"
                    @click="sendMessageHandler"
                    :disabled="!inputMessage.trim() || loading"
                    :class="{ loading: loading }">
              <span v-if="loading">思考中...</span>
              <span v-else>发送</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 选择旅行计划弹窗 -->
    <div v-if="showPlanModal"
         class="modal-overlay"
         @click="showPlanModal = false">
      <div class="modal-content"
           @click.stop>
        <div class="modal-header">
          <h3>选择旅行计划</h3>
          <button class="close-btn"
                  @click="showPlanModal = false">×</button>
        </div>
        <div class="modal-body">
          <div v-if="travelPlans.length === 0"
               class="empty-plans">
            <p>您还没有创建旅行计划</p>
            <button class="btn-primary"
                    @click="goToCreatePlan">去创建行程</button>
          </div>
          <div v-else
               class="plans-list">
            <div v-for="plan in travelPlans"
                 :key="plan.id"
                 :class="['plan-option', { selected: selectedPlanId === plan.id }]"
                 @click="selectedPlanId = plan.id">
              <h4>{{ plan.title }}</h4>
              <p>{{ formatDate(plan.startDate) }} - {{ formatDate(plan.endDate) }}</p>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary"
                  @click="showPlanModal = false">取消</button>
          <button class="btn-primary"
                  @click="confirmCreateSession"
                  :disabled="!selectedPlanId">创建会话</button>
        </div>
      </div>
    </div>

    <!-- 确认删除弹窗 -->
    <div v-if="showDeleteModal"
         class="modal-overlay"
         @click="showDeleteModal = false">
      <div class="modal-content confirm-modal"
           @click.stop>
        <div class="confirm-icon">🗑️</div>
        <h3 class="confirm-title">确认删除</h3>
        <p class="confirm-message">确定要删除这个会话吗？<br>删除后将无法恢复</p>
        <div class="confirm-actions">
          <button class="btn-secondary"
                  @click="showDeleteModal = false">取消</button>
          <button class="btn-danger"
                  @click="confirmDelete">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {nextTick, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'

import {createSession, deleteSession, getMessages, getSessions, stopChat} from '@/api/aiChat'
import {getTravelPlans} from '@/api/travel'

const router = useRouter()
const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)

// 弹窗相关
const showPlanModal = ref(false)
const travelPlans = ref([])
const selectedPlanId = ref(null)

// 删除确认弹窗
const showDeleteModal = ref(false)
const sessionToDelete = ref(null)

onMounted(() => {
  loadSessions()
})

// 加载会话列表
const loadSessions = async () => {
  try {
    const res = await getSessions()
    if (res.code === 200) {
      sessions.value = res.data || []
      if (sessions.value.length > 0 && !currentSession.value) {
        selectSession(sessions.value[0])
      }
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 选择会话并加载消息
const selectSession = async (session) => {
  currentSession.value = session
  messages.value = []

  try {
    const res = await getMessages(session.id)
    // 后端直接返回 List<MessageVO>，不是 Result 包装
    const messageList = Array.isArray(res) ? res : (res.data || [])
    if (messageList.length > 0) {
      messages.value = messageList.map(msg => ({
        role: msg.type === 'USER' ? 'user' : 'assistant',
        content: msg.content
      }))
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  }

  scrollToBottom()
}

// 打开创建会话弹窗
const openCreateModal = async () => {
  selectedPlanId.value = null
  showPlanModal.value = true

  // 加载旅行计划列表
  try {
    const res = await getTravelPlans()
    if (res.code === 200) {
      travelPlans.value = res.data || []
    }
  } catch (error) {
    console.error('加载旅行计划失败:', error)
  }
}

// 确认创建会话
const confirmCreateSession = async () => {
  if (!selectedPlanId.value) return

  try {
    const res = await createSession({ travelPlanId: selectedPlanId.value })
    if (res.code === 200) {
      const newSession = res.data
      sessions.value.unshift(newSession)
      selectSession(newSession)
      showPlanModal.value = false
    }
  } catch (error) {
    console.error('创建会话失败:', error)
    alert('创建会话失败：' + (error.message || '请检查是否已创建旅行计划'))
  }
}

// 去创建行程
const goToCreatePlan = () => {
  router.push('/travel-plans')
}

// 删除会话
const deleteSessionHandler = (sessionId) => {
  sessionToDelete.value = sessionId
  showDeleteModal.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!sessionToDelete.value) return

  try {
    const res = await deleteSession(sessionToDelete.value)
    if (res.code === 200) {
      sessions.value = sessions.value.filter(s => s.id !== sessionToDelete.value)
      if (currentSession.value?.id === sessionToDelete.value) {
        currentSession.value = null
        messages.value = []
        if (sessions.value.length > 0) {
          selectSession(sessions.value[0])
        }
      }
    }
  } catch (error) {
    console.error('删除会话失败:', error)
  } finally {
    showDeleteModal.value = false
    sessionToDelete.value = null
  }
}

// 流式消息处理
const streamingMessage = ref('')
const isStreaming = ref(false)
let abortController = null

// 发送消息 - 流式打字机效果
const sendMessageHandler = async () => {
  const content = inputMessage.value.trim()
  if (!content || loading.value || isStreaming.value) return

  if (!currentSession.value) {
    alert('请先选择一个会话或创建新会话')
    return
  }

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: content
  })

  inputMessage.value = ''
  loading.value = true
  scrollToBottom()

  try {
    // 创建 AbortController 用于取消请求
    abortController = new AbortController()

    // 创建 EventSource 连接
    const token = localStorage.getItem('token') || ''
    const sessionId = currentSession.value.id

    const response = await fetch('/api/aichat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({ content, sessionId }),
      signal: abortController.signal
    })

    if (!response.ok) {
      throw new Error('网络请求失败')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    // 添加空的AI消息占位
    messages.value.push({
      role: 'assistant',
      content: ''
    })

    loading.value = false
    isStreaming.value = true
    streamingMessage.value = ''

    // 添加空的AI消息占位
    const messageIndex = messages.value.length - 1
    let buffer = ''

    // 读取流数据
    let sseBuffer = ''
    while (true) {
      const { done, value } = await reader.read()

      if (value) {
        const chunk = decoder.decode(value, { stream: true })
        sseBuffer += chunk
      }

      // 只有有新数据或流结束时才处理
      if (value || done) {
        // 解析 SSE 格式数据 - 按双换行符分割消息
        const messages_raw = sseBuffer.split('\n\n')
        // 保留最后一个可能不完整的部分（如果不是done）
        sseBuffer = done ? '' : (messages_raw.pop() || '')

        for (const message_raw of messages_raw) {
          const lines = message_raw.split('\n')
          for (const line of lines) {
            // SSE 格式: data: {...}
            if (line.startsWith('data:')) {
              const data = line.slice(5).trim()
              if (data) {
                try {
                  const parsed = JSON.parse(data)
                  // 后端返回 ChatEventVO 格式: { eventData: "文本内容", eventType: 1001 }
                  if (typeof parsed.eventData === 'string') {
                    // 解码 Unicode 转义字符
                    const decodedText = parsed.eventData.replace(/\\u([0-9a-fA-F]{4})/g, (_, hex) => String.fromCharCode(parseInt(hex, 16)))
                    buffer += decodedText
                  }
                } catch (e) {
                  console.warn('JSON解析失败:', data, e)
                }
              }
            }
          }
        }

        // 更新显示 - 使用 Vue 响应式方式
        if (messages.value[messageIndex]) {
          messages.value[messageIndex].content = buffer
          // 强制触发响应式更新
          messages.value = [...messages.value]
          scrollToBottom()
        }
      }

      if (done) {
        isStreaming.value = false
        break
      }
    }

  } catch (error) {
    // 如果是用户主动取消的请求，不显示错误
    if (error.name === 'AbortError') {
      console.log('用户取消了请求')
      loading.value = false
      isStreaming.value = false
      abortController = null
      return
    }

    console.error('发送消息失败:', error)
    loading.value = false
    isStreaming.value = false
    abortController = null
    messages.value.push({
      role: 'assistant',
      content: '抱歉，发送消息失败，请稍后重试。'
    })
    scrollToBottom()
  }
}

// 停止聊天
const stopChatHandler = async () => {
  if (!currentSession.value || !isStreaming.value) return

  // 1. 重置状态
  isStreaming.value = false

  // 2. 取消前端 fetch 请求
  if (abortController) {
    abortController.abort()
    abortController = null
  }

  // 3. 调用后端停止接口（异步，不等待）
  try {
    await stopChat(currentSession.value.id)
  } catch (error) {
    console.error('停止聊天失败:', error)
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatMessage = (content) => {
  if (!content) return ''
  // 先转义 HTML 特殊字符，防止内容被解析为 HTML 标签
  const escaped = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
  // 再处理格式（white-space: pre-wrap 会保留换行符，不需要替换为 <br>）
  return escaped
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.ai-chat-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* AI页面自己处理滚动，不需要外层滚动 */
:deep(.main-content) {
  overflow: hidden !important;
}

.chat-container {
  flex: 1;
  display: flex;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 0 20px 20px;
  gap: 20px;
  min-height: 0;
  overflow: hidden;
}

/* 左侧会话列表 */
.sessions-sidebar {
  width: 260px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(46, 125, 50, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e8f5e9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  color: #2e7d32;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.new-session-btn {
  padding: 6px 12px;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.new-session-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.sessions-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 4px;
  position: relative;
}

.session-item:hover {
  background: #f1f8e9;
}

.session-item:hover .delete-btn {
  opacity: 1;
}

.session-item.active {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
}

.session-icon {
  font-size: 16px;
}

.session-title {
  color: #37474f;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.session-item.active .session-title {
  color: #2e7d32;
  font-weight: 500;
}

.delete-btn {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: none;
  background: #e57373;
  color: white;
  font-size: 14px;
  cursor: pointer;
  opacity: 0;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-btn:hover {
  background: #d32f2f;
  transform: scale(1.1);
}

.empty-sessions {
  text-align: center;
  padding: 40px 20px;
  color: #9e9e9e;
  font-size: 14px;
}

/* 右侧聊天区域 */
.chat-main {
  flex: 1;
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(46, 125, 50, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

.welcome-message {
  text-align: center;
  padding: 40px;
  color: #555;
  flex-shrink: 0;
}

.welcome-message .ai-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  margin: 0 auto 20px;
}

.welcome-message h3 {
  color: #2e7d32;
  margin-bottom: 16px;
}

.welcome-message ul {
  list-style: none;
  padding: 0;
  margin: 20px 0;
}

.welcome-message li {
  padding: 8px 0;
  font-size: 14px;
}

.welcome-message .hint {
  color: #81c784;
  margin-top: 20px;
}

.message {
  display: flex;
  flex-shrink: 0;
}

.message.user {
  justify-content: flex-end;
}

.message-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 80%;
}

.message.user .message-content {
  flex-direction: row-reverse;
}

.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

.message.user .message-bubble {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-bubble {
  background: #f5f5f5;
  color: #37474f;
  border-bottom-left-radius: 4px;
}

.message-bubble.typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 18px 24px;
}

.dot {
  width: 8px;
  height: 8px;
  background: #81c784;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out;
}

.dot:nth-child(1) {
  animation-delay: 0s;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 输入区域 */
.chat-input-area {
  padding: 20px 24px;
  border-top: 1px solid #e8f5e9;
  flex-shrink: 0;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.input-wrapper input {
  flex: 1;
  padding: 14px 20px;
  border: 2px solid #e8f5e9;
  border-radius: 28px;
  font-size: 14px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.input-wrapper input:focus {
  outline: none;
  border-color: #66bb6a;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 187, 106, 0.1);
}

.input-wrapper input:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.send-btn {
  padding: 14px 28px;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border: none;
  border-radius: 28px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(76, 175, 80, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn.loading {
  background: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
  animation: pulse 1.5s ease-in-out infinite;
}

.send-btn.stop-btn {
  background: linear-gradient(135deg, #ef5350 0%, #e53935 100%);
  box-shadow: 0 4px 12px rgba(229, 57, 53, 0.3);
}

.send-btn.stop-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(229, 57, 53, 0.4);
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

/* 流式消息光标效果 */
.message.assistant .message-bubble {
  position: relative;
}

.message.assistant:last-child .message-bubble::after {
  content: "|";
  display: inline-block;
  margin-left: 2px;
  color: #4caf50;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
    opacity: 0;
  }
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #e8f5e9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #2e7d32;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.empty-plans {
  text-align: center;
  padding: 40px 20px;
}

.empty-plans p {
  color: #666;
  margin-bottom: 20px;
}

.plans-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan-option {
  padding: 16px;
  border: 2px solid #e8f5e9;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.plan-option:hover {
  border-color: #66bb6a;
  background: #f1f8e9;
}

.plan-option.selected {
  border-color: #4caf50;
  background: #e8f5e9;
}

.plan-option h4 {
  margin: 0 0 8px 0;
  color: #2e7d32;
}

.plan-option p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #e8f5e9;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-secondary {
  padding: 10px 24px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background: #e0e0e0;
}

.btn-primary {
  padding: 10px 24px;
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 确认删除弹窗样式 */
.confirm-modal {
  padding: 40px;
  text-align: center;
  max-width: 400px;
}

.confirm-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.confirm-title {
  color: #333;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 16px 0;
}

.confirm-message {
  color: #666;
  font-size: 16px;
  line-height: 1.6;
  margin: 0 0 32px 0;
}

.confirm-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.btn-danger {
  padding: 12px 32px;
  background: linear-gradient(135deg, #ef5350 0%, #e53935 100%);
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(229, 57, 53, 0.3);
}

@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
    padding: 12px;
    height: calc(100vh - 70px);
  }

  .sessions-sidebar {
    width: 100%;
    height: 200px;
  }

  .sessions-list {
    display: flex;
    flex-direction: row;
    overflow-x: auto;
    padding: 8px;
  }

  .session-item {
    flex-shrink: 0;
    min-width: 140px;
    margin-bottom: 0;
    margin-right: 8px;
  }

  .message-content {
    max-width: 90%;
  }
}
</style>

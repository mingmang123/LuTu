import request from './request'

// 会话管理
export const getSessions = () => {
  return request({
    url: '/aichatsession/get',
    method: 'post'
  })
}

export const createSession = (data = {}) => {
  return request({
    url: '/aichatsession/create',
    method: 'post',
    data
  })
}

export const deleteSession = (sessionId) => {
  return request({
    url: '/aichatsession/delete',
    method: 'post',
    params: { id: sessionId }
  })
}

// 消息管理 - 查询流式对话详情（新接口）
// 注意：此接口直接返回数组，不经过响应拦截器的 Result 包装处理
export const getMessages = (sessionId) => {
  const token = localStorage.getItem('token') || ''
  return fetch(`/api/aichat/${sessionId}`, {
    method: 'GET',
    headers: {
      'Authorization': token ? `Bearer ${token}` : ''
    }
  }).then(res => res.json())
}

// AI 对话 - 流式响应
export const chatStream = (content, sessionId) => {
  const token = localStorage.getItem('token') || ''
  return fetch('/api/aichat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify({ content, sessionId })
  })
}

// 停止流式聊天
export const stopChat = (sessionId) => {
  return request({
    url: '/aichat/stop',
    method: 'post',
    params: { sessionId }
  })
}

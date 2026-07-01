import axios from 'axios'
import {useUserStore} from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求队列，用于取消重复请求
const pendingRequests = new Map()

// 生成请求唯一标识
const generateRequestKey = (config) => {
  return `${config.method}_${config.url}_${JSON.stringify(config.params)}_${JSON.stringify(config.data)}`
}

// 添加请求到队列
const addPendingRequest = (config) => {
  const requestKey = generateRequestKey(config)
  config.cancelToken = config.cancelToken || new axios.CancelToken((cancel) => {
    if (!pendingRequests.has(requestKey)) {
      pendingRequests.set(requestKey, cancel)
    }
  })
}

// 移除请求从队列
const removePendingRequest = (config) => {
  const requestKey = generateRequestKey(config)
  if (pendingRequests.has(requestKey)) {
    const cancel = pendingRequests.get(requestKey)
    cancel('取消重复请求')
    pendingRequests.delete(requestKey)
  }
}

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 移除重复请求
    removePendingRequest(config)
    addPendingRequest(config)
    
    // 添加认证头
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = 'Bearer ' + userStore.token
    }
    
    // 添加时间戳防止缓存
    if (config.method === 'get') {
      config.params = { ...config.params, _t: Date.now() }
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 移除已完成的请求
    removePendingRequest(response.config)
    
    const res = response.data
    
    // 处理业务错误
    if (res.code !== 200) {
      const error = new Error(res.msg || res.message || '请求失败')
      error.code = res.code
      error.data = res.data
      return Promise.reject(error)
    }
    
    return res
  },
  (error) => {
    // 移除失败的请求
    if (error.config) {
      removePendingRequest(error.config)
    }
    
    // 处理不同类型的错误
    let message = '请求失败'
    
    if (error.response) {
      // 服务器返回错误状态码
      const status = error.response.status
      const data = error.response.data
      
      switch (status) {
        case 400:
          message = data?.msg || data?.message || '请求参数错误'
          break
        case 401:
          message = '登录已过期，请重新登录'
          // 清除用户信息并跳转登录页
          const userStore = useUserStore()
          userStore.clearUser()
          setTimeout(() => {
            window.location.href = '/login'
          }, 1500)
          break
        case 403:
          message = '没有权限执行此操作'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 405:
          message = '请求方法不允许'
          break
        case 408:
          message = '请求超时，请稍后重试'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务暂时不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = data?.msg || data?.message || `服务器错误 (${status})`
      }
    } else if (error.request) {
      // 请求发送但没有收到响应
      if (error.code === 'ECONNABORTED') {
        message = '请求超时，请检查网络连接'
      } else {
        message = '网络连接失败，请检查网络设置'
      }
    } else if (error.message === '取消重复请求') {
      // 取消重复请求，不显示错误
      return Promise.reject(error)
    } else {
      // 请求配置出错
      message = error.message || '请求配置错误'
    }
    
    // 创建友好的错误对象
    const friendlyError = new Error(message)
    friendlyError.originalError = error
    friendlyError.code = error.response?.status || error.code
    friendlyError.isNetworkError = !error.response
    
    return Promise.reject(friendlyError)
  }
)

export default request

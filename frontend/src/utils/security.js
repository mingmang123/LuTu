// XSS 防护 - 转义 HTML 特殊字符
export const escapeHtml = (text) => {
  if (!text || typeof text !== 'string') return ''
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

// 反转义 HTML
export const unescapeHtml = (html) => {
  if (!html || typeof html !== 'string') return ''
  const div = document.createElement('div')
  div.innerHTML = html
  return div.textContent
}

// 输入验证
export const validators = {
  // 手机号验证
  phone: (value) => {
    const phoneRegex = /^1[3-9]\d{9}$/
    return phoneRegex.test(value)
  },
  
  // 密码验证（至少6位）
  password: (value) => {
    return value && value.length >= 6
  },
  
  // 邮箱验证
  email: (value) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(value)
  },
  
  // 非空验证
  required: (value) => {
    if (typeof value === 'string') {
      return value.trim().length > 0
    }
    return value !== null && value !== undefined
  },
  
  // 长度验证
  minLength: (value, min) => {
    if (!value) return false
    return String(value).length >= min
  },
  
  maxLength: (value, max) => {
    if (!value) return true
    return String(value).length <= max
  },
  
  // 数字验证
  numeric: (value) => {
    return !isNaN(parseFloat(value)) && isFinite(value)
  },
  
  // 日期验证
  date: (value) => {
    if (!value) return false
    const date = new Date(value)
    return !isNaN(date.getTime())
  }
}

// 表单验证
export const validateForm = (form, rules) => {
  const errors = {}
  let isValid = true
  
  for (const field in rules) {
    const value = form[field]
    const fieldRules = rules[field]
    
    for (const rule of fieldRules) {
      const { validator, message, params } = rule
      
      let valid = true
      if (typeof validator === 'function') {
        valid = validator(value, params)
      } else if (typeof validators[validator] === 'function') {
        valid = validators[validator](value, params)
      }
      
      if (!valid) {
        errors[field] = message
        isValid = false
        break
      }
    }
  }
  
  return { isValid, errors }
}

// 敏感信息脱敏
export const maskSensitive = {
  // 手机号脱敏
  phone: (phone) => {
    if (!phone || phone.length !== 11) return phone
    return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
  },
  
  // 身份证号脱敏
  idCard: (idCard) => {
    if (!idCard || idCard.length !== 18) return idCard
    return idCard.replace(/(\d{4})\d{10}(\d{4})/, '$1**********$2')
  },
  
  // 邮箱脱敏
  email: (email) => {
    if (!email || !email.includes('@')) return email
    const [name, domain] = email.split('@')
    const maskedName = name.length > 2 
      ? name.slice(0, 2) + '***' 
      : '*'.repeat(name.length)
    return `${maskedName}@${domain}`
  }
}

// 防抖动函数
export const debounce = (func, wait) => {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

// 节流函数
export const throttle = (func, limit) => {
  let inThrottle
  return function executedFunction(...args) {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

// 本地存储安全封装
export const safeStorage = {
  set: (key, value) => {
    try {
      const data = JSON.stringify(value)
      localStorage.setItem(key, data)
    } catch (e) {
      console.error('Storage set error:', e)
    }
  },
  
  get: (key) => {
    try {
      const data = localStorage.getItem(key)
      return data ? JSON.parse(data) : null
    } catch (e) {
      console.error('Storage get error:', e)
      return null
    }
  },
  
  remove: (key) => {
    try {
      localStorage.removeItem(key)
    } catch (e) {
      console.error('Storage remove error:', e)
    }
  },
  
  clear: () => {
    try {
      localStorage.clear()
    } catch (e) {
      console.error('Storage clear error:', e)
    }
  }
}

// ID 加密/解密工具
// 使用简单的混淆算法，让 ID 在 URL 中不那么直白

const ENCODING_CHARS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_'
const XOR_KEY = 0x5A // 简单的异或密钥

/**
 * 将数字 ID 编码为字符串
 * @param {number} id - 数字 ID
 * @returns {string} - 编码后的字符串
 */
export const encodeId = (id) => {
  if (!id || isNaN(id)) return ''
  
  // 先对 ID 进行异或运算
  let num = parseInt(id) ^ XOR_KEY
  
  // 转换为 base64-like 字符串
  let result = ''
  while (num > 0) {
    result = ENCODING_CHARS[num % 64] + result
    num = Math.floor(num / 64)
  }
  
  // 添加校验位（前4位随机字符）
  const prefix = Array(4).fill(0).map(() => 
    ENCODING_CHARS[Math.floor(Math.random() * 64)]
  ).join('')
  
  return prefix + result
}

/**
 * 将编码的字符串解码为数字 ID
 * @param {string} encoded - 编码后的字符串
 * @returns {number|null} - 解码后的数字 ID
 */
export const decodeId = (encoded) => {
  if (!encoded || typeof encoded !== 'string') return null
  
  // 移除前4位随机字符
  const str = encoded.slice(4)
  if (!str) return null
  
  // 从 base64-like 字符串还原数字
  let num = 0
  for (let i = 0; i < str.length; i++) {
    const index = ENCODING_CHARS.indexOf(str[i])
    if (index === -1) return null
    num = num * 64 + index
  }
  
  // 异或解密
  return num ^ XOR_KEY
}

/**
 * 验证编码 ID 是否有效
 * @param {string} encoded - 编码后的字符串
 * @returns {boolean}
 */
export const isValidEncodedId = (encoded) => {
  if (!encoded || typeof encoded !== 'string') return false
  if (encoded.length < 5) return false
  
  const decoded = decodeId(encoded)
  return decoded !== null && !isNaN(decoded) && decoded > 0
}

/**
 * 生成随机的 URL 友好字符串（用于替代简单 ID）
 * @param {number} length - 生成长度
 * @returns {string}
 */
export const generateRandomId = (length = 8) => {
  return Array(length).fill(0).map(() => 
    ENCODING_CHARS[Math.floor(Math.random() * 64)]
  ).join('')
}

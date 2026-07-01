import request from './request'

// 收藏/取消收藏行程
export const toggleFavorite = (shareId) => {
  return request({
    url: `/favorite/toggle/${shareId}`,
    method: 'post'
  })
}

// 检查是否已收藏
export const checkFavorite = (shareId) => {
  return request({
    url: `/favorite/check/${shareId}`,
    method: 'get'
  })
}

// 获取我的收藏列表
export const getMyFavorites = (page = 1, size = 10) => {
  return request({
    url: '/favorite/my',
    method: 'get',
    params: { page, size }
  })
}

// 克隆行程
export const clonePlan = (shareId) => {
  return request({
    url: `/favorite/clone/${shareId}`,
    method: 'post'
  })
}

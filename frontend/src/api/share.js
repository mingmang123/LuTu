import request from './request'

/**
 * 获取热门分享列表
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 */
export const getHotShares = (page = 1, size = 10) => {
  return request({
    url: '/share/hot',
    method: 'GET',
    params: { page, size }
  })
}

/**
 * 获取最新分享列表
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 */
export const getLatestShares = (page = 1, size = 10) => {
  return request({
    url: '/share/latest',
    method: 'GET',
    params: { page, size }
  })
}

/**
 * 根据标签搜索分享
 * @param {string} tag - 标签
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 */
export const searchSharesByTag = (tag, page = 1, size = 10) => {
  return request({
    url: '/share/search',
    method: 'GET',
    params: { tag, page, size }
  })
}

/**
 * 获取分享详情
 * @param {string} shareCode - 分享码
 */
export const getShareDetail = (shareCode) => {
  return request({
    url: `/share/detail/${shareCode}`,
    method: 'GET'
  })
}

/**
 * 点赞分享
 * @param {string} shareCode - 分享码
 */
export const likeShare = (shareCode) => {
  return request({
    url: `/share/like/${shareCode}`,
    method: 'POST'
  })
}

/**
 * 取消点赞
 * @param {string} shareCode - 分享码
 */
export const unlikeShare = (shareCode) => {
  return request({
    url: `/share/unlike/${shareCode}`,
    method: 'POST'
  })
}

/**
 * 检查是否已点赞
 * @param {string} shareCode - 分享码
 */
export const checkLikeStatus = (shareCode) => {
  return request({
    url: `/share/liked/${shareCode}`,
    method: 'GET'
  })
}

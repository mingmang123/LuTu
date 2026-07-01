import request from './request'

/**
 * 生成分享码
 * @param {number} travelPlanId - 旅行计划ID
 * @returns {Promise} 分享码
 */
export const generateShareCode = (travelPlanId) => {
  return request({
    url: '/share/generate',
    method: 'POST',
    params: { travelPlanId }
  })
}

/**
 * 根据分享码查询分享内容
 * @param {string} shareCode - 分享码
 * @returns {Promise} 分享的旅行计划详情
 */
export const queryByShareCode = (shareCode) => {
  return request({
    url: `/share/detail/${shareCode}`,
    method: 'GET'
  })
}

/**
 * 查询分享码状态
 * @param {number} travelPlanId - 旅行计划ID
 * @returns {Promise} 分享状态信息
 */
export const queryShareStatus = (travelPlanId) => {
  return request({
    url: '/share/status',
    method: 'GET',
    params: { travelPlanId }
  })
}

/**
 * 更新分享码状态
 * @param {number} travelPlanId - 行程ID
 * @param {number} status - 状态 1开启，0关闭
 * @returns {Promise}
 */
export const updateShareStatus = (travelPlanId, status) => {
  return request({
    url: `/share/status/${travelPlanId}`,
    method: 'POST',
    params: { status }
  })
}

/**
 * 创建带详细信息的分享
 * @param {Object} data - 分享数据
 * @param {number} data.travelPlanId - 行程ID
 * @param {string} data.title - 分享标题
 * @param {string} data.description - 分享描述
 * @param {string} data.coverImage - 封面图片
 * @param {string} data.tags - 标签（逗号分隔）
 * @returns {Promise}
 */
export const createShare = (data) => {
  return request({
    url: '/share/create',
    method: 'POST',
    params: data
  })
}

/**
 * 获取我的分享列表
 * @returns {Promise}
 */
export const getMyShares = () => {
  return request({
    url: '/share/my',
    method: 'GET'
  })
}

/**
 * 更新分享信息
 * @param {Object} data - 分享数据
 * @param {number} data.travelPlanId - 行程ID
 * @param {string} data.title - 分享标题
 * @param {string} data.description - 分享描述
 * @param {string} data.coverImage - 封面图片
 * @param {string} data.tags - 标签（逗号分隔）
 * @returns {Promise}
 */
export const updateShare = (data) => {
  return request({
    url: '/share/update',
    method: 'POST',
    params: data
  })
}

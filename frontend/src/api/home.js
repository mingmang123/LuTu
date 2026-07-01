import request from './request'

/**
 * 获取首页数据
 */
export const getHomeData = () => {
  return request({
    url: '/home/data',
    method: 'GET'
  })
}

/**
 * 获取统计数据
 */
export const getStatistics = () => {
  return request({
    url: '/home/statistics',
    method: 'GET'
  })
}

/**
 * 获取热门目的地
 */
export const getHotDestinations = () => {
  return request({
    url: '/home/destinations',
    method: 'GET'
  })
}

/**
 * 获取推荐目的地（为你推荐）
 * @param {number} limit 数量限制，默认6
 */
export const getRecommendedDestinations = (limit = 6) => {
  return request({
    url: '/home/destinations/recommended',
    method: 'GET',
    params: { limit }
  })
}

import request from './request'

export const getTravelPlans = () => {
  return request({
    url: '/travelplan/get',
    method: 'get'
  })
}

export const createTravelPlan = (data) => {
  return request({
    url: '/travelplan/add',
    method: 'post',
    data
  })
}

export const deleteTravelPlan = (id) => {
  return request({
    url: '/travelplan/delete',
    method: 'delete',
    params: { id }
  })
}

// 行程环节接口 - 使用 travelPlanItem
export const getTravelItems = (tripId) => {
  return request({
    url: '/travelPlanItem/get',
    method: 'get',
    params: { tripId }
  })
}

export const addTravelItem = (data) => {
  return request({
    url: '/travelPlanItem/add',
    method: 'post',
    data: Array.isArray(data) ? data : [data]
  })
}

export const updateTravelItem = (data) => {
  return request({
    url: '/travelPlanItem/update',
    method: 'put',
    data: Array.isArray(data) ? data : [data]
  })
}

export const deleteTravelItem = (data) => {
  return request({
    url: '/travelPlanItem/delete',
    method: 'delete',
    data: Array.isArray(data) ? data : [data]
  })
}

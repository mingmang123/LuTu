import request from './request'

// 获取用户画像
export const getUserPortrait = () => {
  return request({
    url: '/traveluserportrait/get',
    method: 'get'
  })
}

// 更新用户画像
export const updateUserPortrait = (data) => {
  return request({
    url: '/traveluserportrait/update',
    method: 'post',
    data
  })
}

// 添加用户画像（首次创建）
export const addUserPortrait = (data) => {
  return request({
    url: '/traveluserportrait',
    method: 'put',
    data
  })
}

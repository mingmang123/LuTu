import request from './request'

export const login = (data) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export const captchaLogin = (data) => {
  return request({
    url: '/user/captchalogin',
    method: 'post',
    data
  })
}

export const sendVerifyCode = (email) => {
  return request({
    url: '/user/sendlogin',
    method: 'get',
    params: { email }
  })
}

export const getUserInfo = () => {
  return request({
    url: '/user/get',
    method: 'get'
  })
}

export const logout = () => {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

export const updatePassword = (data) => {
  return request({
    url: '/user/updatePassword',
    method: 'put',
    data
  })
}

export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/user/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const updateAvatar = (avatarUrl) => {
  return request({
    url: '/user/avatar',
    method: 'put',
    params: { avatarUrl }
  })
}

/**
 * 修改用户名
 * @param {string} username 新用户名
 */
export const updateUsername = (username) => {
  return request({
    url: '/user/username',
    method: 'put',
    params: { username }
  })
}

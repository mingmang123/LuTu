import request from './request'

/**
 * 上传图片
 * @param {File} file - 图片文件
 * @param {string} dir - 目录（默认images）
 */
export const uploadImage = (file, dir = 'images') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('dir', dir)

  return request({
    url: '/file/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传图片
 * @param {File[]} files - 图片文件数组
 * @param {string} dir - 目录（默认images）
 */
export const uploadImages = (files, dir = 'images') => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  formData.append('dir', dir)

  return request({
    url: '/file/upload/batch',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取上传凭证（用于前端直传OSS）
 * @param {string} dir - 目录
 */
export const getUploadToken = (dir = 'images') => {
  return request({
    url: '/file/upload/token',
    method: 'GET',
    params: { dir }
  })
}

/**
 * 删除文件
 * @param {string} url - 文件URL
 */
export const deleteFile = (url) => {
  return request({
    url: '/file/delete',
    method: 'DELETE',
    params: { url }
  })
}

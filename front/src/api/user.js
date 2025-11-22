/**
 * 用户相关 API
 * 包括用户信息、个人资料管理等
 */

import request from '@/utils/request'

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getCurrentUser() {
  return request.get('/users/me')
}

/**
 * 更新个人资料
 * @param {object} data - 用户资料数据
 * @returns {Promise}
 */
export function updateProfile(data) {
  return request.put('/users/me', data)
}

/**
 * 上传头像
 * @param {FormData} formData - 文件表单数据
 * @returns {Promise}
 */
export function uploadAvatar(formData) {
  return request.post('/users/me/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取用户主页信息
 * @param {number} userId - 用户ID
 * @param {string} tab - 内容类型筛选
 * @returns {Promise}
 */
export function getUserProfile(userId, tab = 'all') {
  return request.get(`/users/${userId}`, {
    params: { tab }
  })
}

/**
 * 更新隐私设置
 * @param {object} data - 隐私设置数据
 * @returns {Promise}
 */
export function updatePrivacy(data) {
  return request.put('/users/me/privacy', data)
}


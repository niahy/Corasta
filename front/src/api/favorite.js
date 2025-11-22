/**
 * 收藏相关 API
 */

import request from '@/utils/request'

/**
 * 创建收藏夹
 * @param {object} data - 收藏夹数据
 * @returns {Promise}
 */
export function createFolder(data) {
  return request.post('/favorites/folders', data)
}

/**
 * 获取收藏夹列表
 * @returns {Promise}
 */
export function getFolderList() {
  return request.get('/favorites/folders')
}

/**
 * 收藏内容
 * @param {object} data - 收藏数据 {targetType, targetId, folderId}
 * @returns {Promise}
 */
export function favoriteContent(data) {
  return request.post('/favorites', data)
}

/**
 * 取消收藏
 * @param {string} targetType - 目标类型
 * @param {number} targetId - 目标ID
 * @returns {Promise}
 */
export function unfavoriteContent(targetType, targetId) {
  return request.delete('/favorites', {
    params: { targetType, targetId }
  })
}

/**
 * 获取收藏列表
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getFavoriteList(params) {
  return request.get('/favorites', { params })
}


/**
 * 点赞相关 API
 */

import request from '@/utils/request'

/**
 * 点赞内容
 * @param {object} data - 点赞数据 {targetType, targetId}
 * @returns {Promise}
 */
export function likeContent(data) {
  return request.post('/likes', data)
}

/**
 * 取消点赞
 * @param {string} targetType - 目标类型
 * @param {number} targetId - 目标ID
 * @returns {Promise}
 */
export function unlikeContent(targetType, targetId) {
  return request.delete('/likes', {
    params: { targetType, targetId }
  })
}


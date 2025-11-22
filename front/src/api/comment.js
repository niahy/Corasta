/**
 * 评论相关 API
 */

import request from '@/utils/request'

/**
 * 创建评论
 * @param {object} data - 评论数据
 * @returns {Promise}
 */
export function createComment(data) {
  return request.post('/comments', data)
}

/**
 * 获取评论列表
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getCommentList(params) {
  return request.get('/comments', { params })
}

/**
 * 更新评论
 * @param {number} id - 评论ID
 * @param {object} data - 评论数据
 * @returns {Promise}
 */
export function updateComment(id, data) {
  return request.put(`/comments/${id}`, data)
}

/**
 * 删除评论
 * @param {number} id - 评论ID
 * @returns {Promise}
 */
export function deleteComment(id) {
  return request.delete(`/comments/${id}`)
}

/**
 * 点赞评论
 * @param {number} id - 评论ID
 * @returns {Promise}
 */
export function likeComment(id) {
  return request.post(`/comments/${id}/like`)
}

/**
 * 取消点赞评论
 * @param {number} id - 评论ID
 * @returns {Promise}
 */
export function unlikeComment(id) {
  return request.delete(`/comments/${id}/like`)
}

/**
 * 置顶评论
 * @param {number} id - 评论ID
 * @returns {Promise}
 */
export function pinComment(id) {
  return request.put(`/comments/${id}/pin`)
}


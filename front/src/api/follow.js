/**
 * 关注相关 API
 */

import request from '@/utils/request'

/**
 * 关注用户
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function followUser(userId) {
  return request.post(`/users/${userId}/follow`)
}

/**
 * 取消关注用户
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function unfollowUser(userId) {
  return request.delete(`/users/${userId}/follow`)
}

/**
 * 获取关注列表
 * @param {number} userId - 用户ID
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getFollowingList(userId, params) {
  return request.get(`/users/${userId}/following`, { params })
}

/**
 * 获取粉丝列表
 * @param {number} userId - 用户ID
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getFollowerList(userId, params) {
  return request.get(`/users/${userId}/followers`, { params })
}


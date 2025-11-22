/**
 * 通知相关 API
 */

import request from '@/utils/request'

/**
 * 获取通知列表
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getNotificationList(params) {
  return request.get('/notifications', { params })
}

/**
 * 标记通知已读
 * @param {number} id - 通知ID
 * @returns {Promise}
 */
export function markNotificationRead(id) {
  return request.put(`/notifications/${id}/read`)
}

/**
 * 标记全部通知已读
 * @returns {Promise}
 */
export function markAllNotificationsRead() {
  return request.put('/notifications/read-all')
}

/**
 * 获取未读通知数
 * @returns {Promise}
 */
export function getUnreadCount() {
  return request.get('/notifications/unread-count')
}


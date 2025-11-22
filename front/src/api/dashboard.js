/**
 * 内容管理后台相关 API
 */

import request from '@/utils/request'

/**
 * 获取内容管理仪表盘
 * @returns {Promise}
 */
export function getDashboardContent() {
  return request.get('/dashboard/content')
}

/**
 * 获取我的内容列表
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getMyContents(params) {
  return request.get('/dashboard/contents', { params })
}

/**
 * 获取我的互动记录
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getMyInteractions(params) {
  return request.get('/dashboard/interactions', { params })
}

/**
 * 获取数据分析仪表盘
 * @param {object} params - 查询参数 {startDate, endDate}
 * @returns {Promise}
 */
export function getDashboardAnalytics(params) {
  return request.get('/dashboard/analytics', { params })
}


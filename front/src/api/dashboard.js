/**
 * 内容管理后台相关 API
 */

import request from '@/utils/request'

/**
 * 获取仪表盘概览
 * @returns {Promise}
 */
export function getDashboardOverview() {
  return request.get('/dashboard/overview')
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
 * 获取数据趋势
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getTrends(params) {
  return request.get('/dashboard/trends', { params })
}


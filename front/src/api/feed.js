/**
 * Feed流相关 API
 * 包括获取动态Feed流
 */

import request from '@/utils/request'

/**
 * 获取动态Feed流
 * @param {object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.type - 内容类型筛选：all/articles/questions/videos
 * @returns {Promise}
 */
export function getFeed(params) {
  return request.get('/feed', { params })
}


/**
 * 搜索相关 API
 */

import request from '@/utils/request'

/**
 * 全站搜索
 * @param {object} params - 搜索参数
 * @returns {Promise}
 */
export function search(params) {
  return request.get('/search', { params })
}

/**
 * 获取搜索建议
 * @param {string} keyword - 关键词
 * @returns {Promise}
 */
export function getSearchSuggestions(keyword) {
  return request.get('/search/suggestions', {
    params: { keyword }
  })
}

/**
 * 获取热门搜索词
 * @returns {Promise}
 */
export function getHotKeywords() {
  return request.get('/search/hot')
}

/**
 * 获取搜索历史
 * @returns {Promise}
 */
export function getSearchHistory() {
  return request.get('/search/history')
}

/**
 * 清除搜索历史
 * @returns {Promise}
 */
export function clearSearchHistory() {
  return request.delete('/search/history')
}


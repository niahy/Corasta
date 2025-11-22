/**
 * 文章相关 API
 */

import request from '@/utils/request'

/**
 * 创建文章
 * @param {object} data - 文章数据
 * @returns {Promise}
 */
export function createArticle(data) {
  return request.post('/articles', data)
}

/**
 * 更新文章
 * @param {number} id - 文章ID
 * @param {object} data - 文章数据
 * @returns {Promise}
 */
export function updateArticle(id, data) {
  return request.put(`/articles/${id}`, data)
}

/**
 * 删除文章
 * @param {number} id - 文章ID
 * @returns {Promise}
 */
export function deleteArticle(id) {
  return request.delete(`/articles/${id}`)
}

/**
 * 批量删除文章
 * @param {number[]} ids - 文章ID数组
 * @returns {Promise}
 */
export function batchDeleteArticles(ids) {
  return request.delete('/articles/batch', { data: { ids } })
}

/**
 * 获取文章详情
 * @param {number|string} id - 文章ID或slug
 * @returns {Promise}
 */
export function getArticleDetail(id) {
  return request.get(`/articles/${id}`)
}

/**
 * 获取文章列表
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getArticleList(params) {
  return request.get('/articles', { params })
}

/**
 * 获取我的文章列表
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getMyArticles(params) {
  return request.get('/articles/me', { params })
}

/**
 * 上传文章图片
 * @param {FormData} formData - 文件表单数据
 * @returns {Promise}
 */
export function uploadArticleImage(formData) {
  return request.post('/articles/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}


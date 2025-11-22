/**
 * 问答相关 API
 */

import request from '@/utils/request'

/**
 * 创建问题
 * @param {object} data - 问题数据
 * @returns {Promise}
 */
export function createQuestion(data) {
  return request.post('/questions', data)
}

/**
 * 更新问题
 * @param {number} id - 问题ID
 * @param {object} data - 问题数据
 * @returns {Promise}
 */
export function updateQuestion(id, data) {
  return request.put(`/questions/${id}`, data)
}

/**
 * 删除问题
 * @param {number} id - 问题ID
 * @returns {Promise}
 */
export function deleteQuestion(id) {
  return request.delete(`/questions/${id}`)
}

/**
 * 获取问题详情
 * @param {number} id - 问题ID
 * @returns {Promise}
 */
export function getQuestionDetail(id) {
  return request.get(`/questions/${id}`)
}

/**
 * 获取问题列表
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getQuestionList(params) {
  return request.get('/questions', { params })
}

/**
 * 创建回答
 * @param {number} questionId - 问题ID
 * @param {object} data - 回答数据
 * @returns {Promise}
 */
export function createAnswer(questionId, data) {
  return request.post(`/questions/${questionId}/answers`, data)
}

/**
 * 更新回答
 * @param {number} id - 回答ID
 * @param {object} data - 回答数据
 * @returns {Promise}
 */
export function updateAnswer(id, data) {
  return request.put(`/answers/${id}`, data)
}

/**
 * 删除回答
 * @param {number} id - 回答ID
 * @returns {Promise}
 */
export function deleteAnswer(id) {
  return request.delete(`/answers/${id}`)
}

/**
 * 获取回答列表
 * @param {number} questionId - 问题ID
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getAnswerList(questionId, params) {
  return request.get(`/questions/${questionId}/answers`, { params })
}

/**
 * 投票回答
 * @param {number} id - 回答ID
 * @param {string} type - 投票类型：'upvote' 或 'downvote'
 * @returns {Promise}
 */
export function voteAnswer(id, type) {
  return request.post(`/answers/${id}/vote`, { type })
}

/**
 * 取消投票
 * @param {number} id - 回答ID
 * @returns {Promise}
 */
export function cancelVote(id) {
  return request.delete(`/answers/${id}/vote`)
}

/**
 * 标记最佳回答
 * @param {number} questionId - 问题ID
 * @param {number} answerId - 回答ID
 * @returns {Promise}
 */
export function markBestAnswer(questionId, answerId) {
  return request.put(`/questions/${questionId}/best-answer/${answerId}`)
}

/**
 * 关注问题
 * @param {number} id - 问题ID
 * @returns {Promise}
 */
export function followQuestion(id) {
  return request.post(`/questions/${id}/follow`)
}

/**
 * 取消关注问题
 * @param {number} id - 问题ID
 * @returns {Promise}
 */
export function unfollowQuestion(id) {
  return request.delete(`/questions/${id}/follow`)
}


/**
 * 认证相关 API
 * 包括登录、注册、密码管理等
 */

import request from '@/utils/request'

/**
 * 用户注册
 * @param {object} data - 注册数据
 * @returns {Promise}
 */
export function register(data) {
  return request.post('/auth/register', data)
}

/**
 * 获取验证码
 * @returns {Promise}
 */
export function getCaptcha() {
  return request.get('/auth/captcha')
}

/**
 * 用户登录
 * @param {object} data - 登录数据
 * @returns {Promise}
 */
export function login(data) {
  return request.post('/auth/login', data)
}

/**
 * 用户登出
 * @returns {Promise}
 */
export function logout() {
  return request.post('/auth/logout')
}

/**
 * 修改密码
 * @param {object} data - 密码数据
 * @returns {Promise}
 */
export function changePassword(data) {
  return request.put('/auth/password', data)
}

/**
 * 找回密码
 * @param {object} data - 找回密码数据
 * @returns {Promise}
 */
export function resetPassword(data) {
  return request.post('/auth/password/reset', data)
}

/**
 * 获取密保问题
 * @param {string} username - 用户名
 * @returns {Promise}
 */
export function getSecurityQuestions(username) {
  return request.get(`/auth/security-questions/${username}`)
}


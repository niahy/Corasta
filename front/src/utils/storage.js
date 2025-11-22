/**
 * 本地存储工具类
 * 封装 localStorage 操作，提供类型安全的方法
 */

const TOKEN_KEY = 'token'
const USER_KEY = 'user'

export const storage = {
  /**
   * 设置 token
   * @param {string} token - JWT token
   */
  setToken(token) {
    localStorage.setItem(TOKEN_KEY, token)
  },

  /**
   * 获取 token
   * @returns {string|null} token
   */
  getToken() {
    return localStorage.getItem(TOKEN_KEY)
  },

  /**
   * 移除 token
   */
  removeToken() {
    localStorage.removeItem(TOKEN_KEY)
  },

  /**
   * 设置用户信息
   * @param {object} user - 用户信息对象
   */
  setUser(user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  },

  /**
   * 获取用户信息
   * @returns {object|null} 用户信息对象
   */
  getUser() {
    const userStr = localStorage.getItem(USER_KEY)
    return userStr ? JSON.parse(userStr) : null
  },

  /**
   * 移除用户信息
   */
  removeUser() {
    localStorage.removeItem(USER_KEY)
  },

  /**
   * 清除所有存储
   */
  clear() {
    this.removeToken()
    this.removeUser()
  }
}


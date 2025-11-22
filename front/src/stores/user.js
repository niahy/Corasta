/**
 * 用户状态管理
 * 管理用户登录状态、用户信息等
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCurrentUser } from '@/api/user'
import { storage } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(storage.getToken())

  // 计算属性：是否已登录
  const isLoggedIn = computed(() => !!token.value && !!user.value)

  // 计算属性：用户信息（兼容性）
  const userInfo = computed(() => user.value)

  /**
   * 设置 token
   * @param {string} newToken - JWT token
   */
  function setToken(newToken) {
    token.value = newToken
    storage.setToken(newToken)
  }

  /**
   * 设置用户信息
   * @param {object} userData - 用户信息对象
   */
  function setUser(userData) {
    user.value = userData
    storage.setUser(userData)
  }

  /**
   * 获取用户信息
   */
  async function fetchUserInfo() {
    try {
      const response = await getCurrentUser()
      if (response.data) {
        setUser(response.data)
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果获取失败，清除 token
      logout()
    }
  }

  /**
   * 初始化用户信息
   * 如果已有 token，尝试获取用户信息
   */
  async function initUser() {
    if (token.value) {
      // 尝试从 localStorage 恢复用户信息
      const cachedUser = storage.getUser()
      if (cachedUser) {
        user.value = cachedUser
      }
      // 尝试从服务器获取最新用户信息
      await fetchUserInfo()
    }
  }

  /**
   * 登出
   */
  function logout() {
    user.value = null
    token.value = null
    storage.clear()
  }

  return {
    user,
    token,
    isLoggedIn,
    setToken,
    setUser,
    fetchUserInfo,
    initUser,
    logout
  }
})


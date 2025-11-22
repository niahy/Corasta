/**
 * HTTP 请求封装
 * 基于 Axios，提供统一的请求拦截、响应处理和错误处理
 */

import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { data } = response
    // 统一处理响应格式
    if (data.code === 200 || data.code === 201) {
      return data
    } else {
      // 业务错误
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  (error) => {
    // HTTP 错误处理
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          // 未授权，清除 token 并跳转登录
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          window.location.href = '/auth/login'
          break
        case 403:
          return Promise.reject(new Error('无权限访问'))
        case 404:
          return Promise.reject(new Error('资源不存在'))
        case 422:
          return Promise.reject(new Error(data.message || '参数验证失败'))
        case 500:
          return Promise.reject(new Error('服务器内部错误'))
        default:
          return Promise.reject(new Error(data?.message || '请求失败'))
      }
    } else if (error.request) {
      return Promise.reject(new Error('网络错误，请检查网络连接'))
    } else {
      return Promise.reject(error)
    }
  }
)

export default request


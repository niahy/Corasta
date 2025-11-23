/**
 * HTTP 请求封装
 * 基于 Axios，提供统一的请求拦截、响应处理和错误处理
 */

import axios from 'axios'

const request = axios.create({
  // 使用相对路径，通过 Vite 代理转发到后端
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 记录请求开始时间（用于计算响应时间）
    config.metadata = { startTime: Date.now() }
    
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 开发环境：打印请求日志
    if (import.meta.env.DEV) {
      const timestamp = new Date().toLocaleTimeString()
      const method = config.method?.toUpperCase() || 'GET'
      const url = config.url || ''
      const fullUrl = config.baseURL ? `${config.baseURL}${url}` : url
      
      console.group(`🚀 [${timestamp}] ${method} ${fullUrl}`)
      if (config.params) {
        console.log('📋 Query Params:', config.params)
      }
      if (config.data) {
        console.log('📦 Request Body:', config.data)
      }
      if (token) {
        console.log('🔑 Token:', token.substring(0, 20) + '...')
      }
      console.groupEnd()
    }
    
    return config
  },
  (error) => {
    // 开发环境：打印请求错误日志
    if (import.meta.env.DEV) {
      console.error('❌ Request Error:', error)
    }
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { data, config, status } = response
    
    // 开发环境：打印响应日志
    if (import.meta.env.DEV) {
      const timestamp = new Date().toLocaleTimeString()
      const method = config.method?.toUpperCase() || 'GET'
      const url = config.url || ''
      const fullUrl = config.baseURL ? `${config.baseURL}${url}` : url
      const statusColor = status >= 200 && status < 300 ? '✅' : '⚠️'
      
      console.group(`${statusColor} [${timestamp}] ${method} ${fullUrl} - ${status}`)
      console.log('📥 Response Data:', data)
      if (data.code !== undefined) {
        const codeColor = data.code === 200 || data.code === 201 ? '✅' : '❌'
        console.log(`${codeColor} Business Code: ${data.code} - ${data.message || 'N/A'}`)
      }
      console.log(`⏱️  Response Time: ${Date.now() - (config.metadata?.startTime || Date.now())}ms`)
      console.groupEnd()
    }
    
    // 统一处理响应格式
    if (data.code === 200 || data.code === 201) {
      return data
    } else {
      // 业务错误
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  (error) => {
    // 开发环境：打印错误日志
    if (import.meta.env.DEV) {
      const timestamp = new Date().toLocaleTimeString()
      
      if (error.response) {
        const { status, data, config } = error.response
        const method = config?.method?.toUpperCase() || 'GET'
        const url = config?.url || ''
        const fullUrl = config?.baseURL ? `${config.baseURL}${url}` : url
        
        console.group(`❌ [${timestamp}] ${method} ${fullUrl} - ${status}`)
        console.error('📥 Error Response:', data)
        console.error(`💬 Error Message: ${data?.message || error.message || '请求失败'}`)
        console.groupEnd()
      } else if (error.request) {
        console.error(`❌ [${timestamp}] Network Error:`, error.message)
      } else {
        console.error(`❌ [${timestamp}] Request Error:`, error.message)
      }
    }
    
    // HTTP 错误处理
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          // 未授权，清除 token 并跳转登录
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          window.location.href = '/login'
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


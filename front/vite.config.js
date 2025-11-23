import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// ANSI 颜色代码
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  cyan: '\x1b[36m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  red: '\x1b[31m',
  blue: '\x1b[34m',
  gray: '\x1b[90m'
}

const colorize = (text, color) => `${colors[color]}${text}${colors.reset}`

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000,
    open: true, // 自动打开浏览器
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // 记录请求开始时间
            req._startTime = Date.now()
          })
          
          proxy.on('proxyRes', (proxyRes, req, res) => {
            const duration = Date.now() - (req._startTime || Date.now())
            const statusCode = proxyRes.statusCode || 200
            const method = req.method || 'GET'
            const url = req.originalUrl || req.url || ''
            const timestamp = new Date().toLocaleTimeString()
            
            // 根据状态码选择颜色
            let statusColor = colors.green
            if (statusCode >= 400) {
              statusColor = colors.red
            } else if (statusCode >= 300) {
              statusColor = colors.yellow
            }
            
            // 输出完整的日志（时间、方法、路径、状态码、耗时）
            console.log(
              `${colorize(`[${timestamp}]`, 'cyan')} ` +
              `${colorize(method.padEnd(6), 'bright')} ` +
              `${colorize(url, 'blue')} ` +
              `${statusColor}${statusCode}${colors.reset} ` +
              `${colors.gray}(${duration}ms)${colors.reset}`
            )
          })
          
          proxy.on('error', (err, req, res) => {
            const timestamp = new Date().toLocaleTimeString()
            const method = req.method || 'GET'
            const url = req.originalUrl || req.url || ''
            console.log(
              `${colorize(`[${timestamp}]`, 'cyan')} ` +
              `${colorize(method.padEnd(6), 'bright')} ` +
              `${colorize(url, 'blue')} ` +
              `${colors.red}ERROR${colors.reset} ${err.message}`
            )
          })
        }
      }
    }
  }
})

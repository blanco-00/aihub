/**
 * API 客户端封装
 */
import axios, { AxiosInstance, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResult } from '../types/api'

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL || '/api',
  timeout: 20000, // 20 秒超时，确保有足够时间等待数据库连接测试
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    // 可以在这里添加 token 等认证信息
    // const token = getToken()
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => {
    const result: ApiResult = response.data
    // 如果后端返回的 code 不是 200，视为错误
    if (result.code !== 200) {
      // 对于配置相关的 API，不在这里显示错误，由调用方处理
      const url = response.config.url || ''
      if (!url.includes('/setup/') && !url.includes('/init/')) {
        ElMessage.error(result.message || '请求失败')
      }
      return Promise.reject(new Error(result.message || '请求失败'))
    }
    // 如果 data 是 undefined 或 null，返回 undefined（用于 void 类型的接口）
    return result.data !== undefined ? result.data : undefined
  },
  (error: AxiosError) => {
    // 处理 HTTP 错误
    if (error.response) {
      const status = error.response.status
      const data = error.response.data as any
      const url = error.config?.url || ''
      
      if (status === 503) {
        // 系统未初始化，不显示错误提示，由路由拦截处理
        return Promise.reject(error)
      }
      
      // 对于配置相关的 API，不在这里显示消息，由调用方处理
      if (url.includes('/setup/') || url.includes('/init/')) {
        return Promise.reject(error)
      }
      
      // 其他 HTTP 错误，不在这里显示消息，由路由拦截处理
      // 这样可以在错误页面显示更详细的错误信息
      return Promise.reject(error)
    } else if (error.request) {
      // 网络错误（如连接被拒绝），不显示消息，由调用方处理
      return Promise.reject(error)
    } else {
      // 请求配置错误
      ElMessage.error('请求配置错误')
      return Promise.reject(error)
    }
  }
)

export default apiClient

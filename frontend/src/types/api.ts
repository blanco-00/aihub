/**
 * API 响应类型定义
 */

export interface ApiResult<T = any> {
  code: number
  message: string
  data?: T
}

/**
 * 初始化相关类型
 */
export interface InitStatusResponse {
  initialized: boolean
}

export interface DatabaseStatusResponse {
  connected: boolean
  databaseExists: boolean
  tablesInitialized: boolean
  errorMessage?: string
}

export interface InitSuperAdminRequest {
  username: string
  email: string
  password: string
}

export interface DatabaseConfig {
  host: string
  port: number
  database: string
  username: string
  password: string
}

export interface ConnectionTestResult {
  success: boolean
  errorMessage?: string
  databaseExists: boolean
  duration?: number
}

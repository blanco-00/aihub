/**
 * 初始化服务
 */
import apiClient from './apiClient'
import type {
  InitStatusResponse,
  DatabaseStatusResponse,
  InitSuperAdminRequest,
} from '../types/api'

export const initService = {
  /**
   * 检查系统是否已初始化
   */
  checkStatus: async (): Promise<boolean> => {
    const result = await apiClient.get<boolean>('/init/status')
    return result as unknown as boolean
  },

  /**
   * 检查数据库状态
   */
  checkDatabaseStatus: async (): Promise<DatabaseStatusResponse> => {
    return await apiClient.get<DatabaseStatusResponse>('/init/database/status')
  },

  /**
   * 初始化数据库表结构
   */
  initializeDatabase: async (): Promise<void> => {
    await apiClient.post('/init/database/init')
  },

  /**
   * 创建超级管理员
   */
  createSuperAdmin: async (data: InitSuperAdminRequest): Promise<void> => {
    await apiClient.post('/init/super-admin', data)
  },
}

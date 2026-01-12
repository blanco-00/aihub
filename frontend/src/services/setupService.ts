/**
 * 配置服务
 */
import apiClient from './apiClient'
import type { DatabaseConfig, ConnectionTestResult } from '../types/api'

export const setupService = {
  /**
   * 检查是否已配置数据库
   */
  checkSetupStatus: async (): Promise<boolean> => {
    return await apiClient.get<boolean>('/setup/status')
  },

  /**
   * 测试数据库连接
   */
  testConnection: async (config: DatabaseConfig): Promise<ConnectionTestResult> => {
    return await apiClient.post<ConnectionTestResult>('/setup/test-connection', config)
  },

  /**
   * 保存数据库配置
   */
  saveConfig: async (config: DatabaseConfig): Promise<void> => {
    await apiClient.post('/setup/save-config', config)
  },
}

import { http } from "@/utils/http";

/**
 * 统一响应格式
 */
type Result<T = any> = {
  code: number;
  message: string;
  data: T;
};

/**
 * 操作日志信息
 */
export type OperationLogInfo = {
  id: number;
  userId?: number;
  username?: string;
  module?: string;
  operation?: string;
  method?: string;
  url?: string;
  params?: string;
  result?: string;
  status: number;
  ip?: string;
  duration?: number;
  operationTime: string;
};

/**
 * 操作日志列表查询请求参数
 */
export type OperationLogListRequest = {
  current?: number;
  size?: number;
  username?: string;
  module?: string;
  operation?: string;
  status?: number;
  startTime?: string;
  endTime?: string;
};

/**
 * 获取操作日志列表
 */
export const getOperationLogList = (params: OperationLogListRequest) => {
  return http.request<Result<{
    records: OperationLogInfo[];
    total: number;
    current: number;
    size: number;
    pages: number;
  }>>("get", "/api/operation-logs", { params });
};

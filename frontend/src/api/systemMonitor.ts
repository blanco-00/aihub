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
 * 主机信息
 */
export type HostInfo = {
  osName: string;
  osVersion: string;
  osArch: string;
  hostName: string;
  ip: string;
  cpuCores: number;
  cpuUsage: number;
  totalMemory: number;
  usedMemory: number;
  freeMemory: number;
  memoryUsage: number;
  totalDisk: number;
  usedDisk: number;
  freeDisk: number;
  diskUsage: number;
  uptime: number;
};

/**
 * JVM信息
 */
export type JvmInfo = {
  jvmName: string;
  jvmVersion: string;
  javaVersion: string;
  heapTotal: number;
  heapUsed: number;
  heapFree: number;
  heapUsage: number;
  nonHeapTotal: number;
  nonHeapUsed: number;
  nonHeapFree: number;
  nonHeapUsage: number;
  threadCount: number;
  gcCount: number;
  gcTime: number;
};

/**
 * 数据库信息
 */
export type DatabaseInfo = {
  status: string;
  version: string;
  activeConnections: number;
  maxConnections: number;
  idleConnections: number;
  connectionUsage: number;
};

/**
 * 缓存信息
 */
export type CacheInfo = {
  status: string;
  version: string;
  usedMemory: number;
  maxMemory: number;
  memoryUsage: number;
  connectedClients: number;
  totalKeys: number;
  hitCount: number;
  missCount: number;
  hitRate: number;
};

/**
 * 系统监控信息
 */
export type SystemMonitorInfo = {
  host: HostInfo;
  jvm: JvmInfo;
  mysql: DatabaseInfo;
  redis: CacheInfo;
};

/**
 * 获取系统监控信息
 */
export const getSystemMonitorInfo = () => {
  return http.request<Result<SystemMonitorInfo>>("get", "/api/monitor/system");
};

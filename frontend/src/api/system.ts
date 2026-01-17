import { http } from "@/utils/http";
import {
  getUserList as getUserListNew,
  createUser as createUserNew,
  updateUser as updateUserNew,
  deleteUser as deleteUserNew,
  toggleUserStatus as toggleUserStatusNew,
  type UserListParams,
  type CreateUserRequest,
  type UpdateUserRequest
} from "./user";
import { getDeptList as getDeptListNew } from "./department";

type Result<T = any> = {
  code: number;
  message: string;
  data?: T;
};

type PageResult<T = any> = {
  records: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
};

type ResultTable = {
  code: number;
  message: string;
  data?: {
    /** 列表数据 */
    list: Array<any>;
    /** 总条目数 */
    total?: number;
    /** 每页显示条目个数 */
    pageSize?: number;
    /** 当前页数 */
    currentPage?: number;
  };
};

/** 获取系统管理-用户管理列表 */
export const getUserList = (data?: any) => {
  const transformStartTime = performance.now();
  
  // 转换参数格式：从 { username, phone, status, currentPage, pageSize, departmentId } 转换为 { keyword, role, status, departmentId, current, size }
  const params: UserListParams = {};
  if (data) {
    params.current = data.currentPage || 1;
    params.size = data.pageSize || 10;
    if (data.username) {
      params.keyword = data.username;
    }
    if (data.phone) {
      params.phone = data.phone;
    }
    if (data.role) {
      params.role = data.role;
    }
    if (data.status !== undefined && data.status !== "") {
      params.status = Number(data.status);
    }
    if (data.departmentId !== undefined && data.departmentId !== null) {
      params.departmentId = Number(data.departmentId);
    }
  }
  
  const transformTime = performance.now() - transformStartTime;
  if (transformTime > 10) {
    console.warn(`[getUserList] 参数转换耗时: ${transformTime.toFixed(2)}ms`);
  }
  
  const requestStartTime = performance.now();
  return getUserListNew(params).then((response: any) => {
    const requestTime = performance.now() - requestStartTime;
    const responseProcessStart = performance.now();
    
    console.log("[getUserList] API响应接收", {
      code: response.code,
      requestTime: `${requestTime.toFixed(2)}ms`,
      timestamp: new Date().toISOString()
    });
    
    // 转换响应格式：从 { records, total, current, size, pages } 转换为 { list, total, pageSize, currentPage }
    // 后端返回 code: 200 表示成功
    if ((response.code === 200 || response.code === 0) && response.data) {
      // 适配前端需要的数据格式
      const list = (response.data.records || []).map((item: any) => ({
        id: item.id,
        username: item.username,
        nickname: item.nickname || item.username, // 使用后端返回的nickname，如果没有则使用username
        phone: item.phone || "",
        email: item.email,
        sex: 0, // 后端没有sex字段，默认0（男）
        avatar: "", // 后端没有avatar字段
        status: item.status,
        role: item.role,
        roleDescription: item.roleDescription,
        roleIds: item.roleIds || [], // 用户的所有角色ID列表
        roleNames: item.roleNames || [], // 用户的所有角色名称列表
        departmentId: item.departmentId || 0,
        departmentName: item.departmentName || "未分配",
        createTime: item.createdAt,
        updatedAt: item.updatedAt
      }));
      
      const responseProcessTime = performance.now() - responseProcessStart;
      const totalTime = performance.now() - transformStartTime;
      
      if (responseProcessTime > 10) {
        console.warn(`[getUserList] 响应处理耗时: ${responseProcessTime.toFixed(2)}ms`);
      }
      
      if (totalTime > 1000) {
        console.warn(`[getUserList] 总耗时过长: ${totalTime.toFixed(2)}ms (请求: ${requestTime.toFixed(2)}ms, 处理: ${responseProcessTime.toFixed(2)}ms)`);
      }
      
      return {
        code: 0,
        message: response.message || "success",
        data: {
          list,
          total: response.data.total || 0,
          pageSize: response.data.size || 10,
          currentPage: response.data.current || 1
        }
      };
    }
    // 如果响应格式不符合预期，返回错误格式
    return {
      code: response.code || -1,
      message: response.message || "获取用户列表失败",
      data: {
        list: [],
        total: 0,
        pageSize: 10,
        currentPage: 1
      }
    };
  });
};

/** 创建用户 */
export const createUser = (data: any) => {
  const request: CreateUserRequest = {
    username: data.username,
    nickname: data.nickname,
    email: data.email,
    phone: data.phone,
    password: data.password,
    role: data.role || "USER", // 默认角色
    departmentId: data.departmentId !== undefined && data.departmentId !== null ? data.departmentId : 0, // 部门ID
    status: data.status !== undefined ? data.status : 1,
    remark: data.remark || "" // 备注
  };
  return createUserNew(request);
};

/** 更新用户 */
export const updateUser = (id: number, data: any) => {
  const request: UpdateUserRequest = {
    username: data.username,
    nickname: data.nickname,
    email: data.email,
    phone: data.phone,
    role: data.role || "USER",
    departmentId: data.departmentId !== undefined ? data.departmentId : undefined, // 包含部门ID
    status: data.status,
    remark: data.remark, // 包含备注
    password: data.password || undefined // 只有提供密码时才更新
  };
  return updateUserNew(id, request);
};

/** 删除用户 */
export const deleteUser = (id: number) => {
  return deleteUserNew(id);
};

/** 切换用户状态 */
export const toggleUserStatus = (id: number, status: number) => {
  return toggleUserStatusNew(id, status);
};

/** 系统管理-用户管理-获取所有角色列表 */
export const getAllRoleList = () => {
  return http.request<Result>("get", "/api/roles/options");
};

/** 系统管理-角色管理-获取所有角色列表（从数据库） */
export const getAllRolesFromDB = () => {
  return http.request<Result>("get", "/api/roles");
};

/** 系统管理-用户管理-根据userId，获取对应角色id列表 */
export const getRoleIds = (userId: number) => {
  return http.request<Result<number[]>>("get", `/api/users/${userId}/roles`);
};

/** 系统管理-用户管理-分配用户角色（支持多角色） */
export const assignUserRoles = (userId: number, roleIds: number[]) => {
  return http.request<Result<void>>("post", `/api/users/${userId}/roles`, { data: roleIds });
};

/** 获取系统管理-角色管理列表 */
export const getRoleList = (data?: object) => {
  return http.request<ResultTable>("post", "/role", { data });
};

/** 获取系统管理-菜单管理列表 */
export const getMenuList = (data?: object) => {
  return http.request<Result>("post", "/menu", { data });
};

/** 获取系统管理-部门管理列表 */
export const getDeptList = (data?: object) => {
  return getDeptListNew();
};

/** 获取系统监控-在线用户列表 */
export const getOnlineLogsList = (data?: object) => {
  return http.request<ResultTable>("get", "/api/online-users", { params: data });
};

/** 强制用户下线 */
export const forceOfflineUser = (userId: number) => {
  return http.request<Result<void>>("delete", `/api/online-users/${userId}`);
};

/** 获取系统监控-登录日志列表 */
export const getLoginLogsList = (params?: object) => {
  return http.request<Result<PageResult<any>>>("get", "/api/login-logs", { params });
};

/** 获取系统监控-操作日志列表 */
export const getOperationLogsList = (params?: object) => {
  return http.request<Result<PageResult<any>>>("get", "/api/operation-logs", { params });
};

/** 获取系统监控-系统日志列表 */
export const getSystemLogsList = (params?: object) => {
  return http.request<Result<PageResult<any>>>("get", "/api/system-logs", { params });
};

/** 获取系统监控-系统日志-根据 id 查日志详情 */
export const getSystemLogsDetail = (id: number) => {
  return http.request<Result<any>>("get", `/api/system-logs/${id}`);
};

/** 获取角色管理-权限-菜单权限 */
export const getRoleMenu = (data?: object) => {
  return http.request<Result>("post", "/role-menu", { data });
};

/** 获取角色管理-权限-菜单权限-根据角色 id 查对应菜单 */
export const getRoleMenuIds = (data?: object) => {
  return http.request<Result>("post", "/role-menu-ids", { data });
};

/** 欢迎页面统计数据类型 */
export type WelcomeStatisticsResponse = {
  cards: Array<{
    name: string;
    value: number;
    percent: number;
    data: number[];
  }>;
  chartData: {
    lastWeek: {
      requireData: number[];
      questionData: number[];
    };
    thisWeek: {
      requireData: number[];
      questionData: number[];
    };
  };
  tableData: Array<{
    date: string;
    requiredNumber: number;
    questionNumber: number;
    resolveNumber: number;
    satisfaction: number;
  }>;
  latestNews: Array<{
    date: string;
    requiredNumber: number;
    resolveNumber: number;
  }>;
};

/** 获取欢迎页面统计数据 */
export const getWelcomeStatistics = () => {
  return http.request<Result<WelcomeStatisticsResponse>>("get", "/api/welcome/statistics");
};

/** 文件管理相关类型 */
export type FileInfo = {
  url: string;
  filename: string;
  size: number;
  contentType: string;
  category: string;
  uploadTime: string;
  path: string;
};

export type FileListParams = {
  category?: string;
  keyword?: string;
  current?: number;
  size?: number;
};

/** 获取文件列表 */
export const getFileList = (params?: FileListParams) => {
  return http.request<Result<PageResult<FileInfo>>>("get", "/api/files/list", { params });
};

/** 删除文件 */
export const deleteFile = (url: string) => {
  return http.request<Result<void>>("delete", "/api/files", { params: { url } });
};

/** 下载文件 */
export const downloadFile = (url: string) => {
  return http.request<Blob>("get", "/api/files/download", { 
    params: { url },
    responseType: "blob"
  });
};

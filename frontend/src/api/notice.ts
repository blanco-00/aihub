import { http } from "@/utils/http";

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

// ==================== 通知分类 API ====================

/** 获取通知分类列表 */
export const getNoticeCategoryList = (params?: any) => {
  return http.request<Result<PageResult>>("get", "/api/notice-categories", {
    params,
  });
};

/** 获取通知分类详情 */
export const getNoticeCategoryDetail = (id: number) => {
  return http.request<Result>("get", `/api/notice-categories/${id}`);
};

/** 创建通知分类 */
export const createNoticeCategory = (data: any) => {
  return http.request<Result>("post", "/api/notice-categories", { data });
};

/** 更新通知分类 */
export const updateNoticeCategory = (id: number, data: any) => {
  return http.request<Result>("put", `/api/notice-categories/${id}`, { data });
};

/** 删除通知分类 */
export const deleteNoticeCategory = (id: number) => {
  return http.request<Result>("delete", `/api/notice-categories/${id}`);
};

// ==================== 通知公告 API（管理员）====================

/** 获取通知列表 */
export const getNoticeList = (params?: any) => {
  return http.request<Result<PageResult>>("get", "/api/notices", {
    params,
  });
};

/** 获取通知详情 */
export const getNoticeDetail = (id: number) => {
  return http.request<Result>("get", `/api/notices/${id}`);
};

/** 创建通知 */
export const createNotice = (data: any) => {
  return http.request<Result>("post", "/api/notices", { data });
};

/** 更新通知 */
export const updateNotice = (id: number, data: any) => {
  return http.request<Result>("put", `/api/notices/${id}`, { data });
};

/** 发布通知 */
export const publishNotice = (id: number, data?: any) => {
  return http.request<Result>("post", `/api/notices/${id}/publish`, { data });
};

/** 撤回通知 */
export const withdrawNotice = (id: number) => {
  return http.request<Result>("post", `/api/notices/${id}/withdraw`);
};

/** 删除通知 */
export const deleteNotice = (id: number) => {
  return http.request<Result>("delete", `/api/notices/${id}`);
};

// ==================== 通知公告 API（用户端）====================

/** 获取我的通知列表 */
export const getMyNotices = (params?: any) => {
  return http.request<Result<PageResult>>("get", "/api/notices/my", {
    params,
  });
};

/** 获取未读通知数量 */
export const getUnreadNoticeCount = () => {
  return http.request<Result<number>>("get", "/api/notices/my/unread-count");
};

/** 查看通知详情（自动标记已读） */
export const getNoticeDetailForUser = (id: number) => {
  return http.request<Result>("get", `/api/notices/${id}/detail`);
};

/** 标记通知为已读 */
export const markNoticeAsRead = (id: number) => {
  return http.request<Result>("put", `/api/notices/${id}/read`);
};

/** 全部标记为已读 */
export const markAllNoticesAsRead = () => {
  return http.request<Result>("put", "/api/notices/read-all");
};

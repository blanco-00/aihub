/**
 * 文件管理相关类型定义
 */

export type FileInfo = {
  id?: number;
  url: string;
  filename: string;
  size: number;
  contentType: string;
  category: string;
  uploadTime: string;
  path: string;
  uploadUsername?: string;
  referenceCount?: number;
};

export type FileListParams = {
  category?: string;
  keyword?: string;
  current?: number;
  size?: number;
};

import { http } from "@/utils/http";

type Result = {
  code: number;
  message: string;
  data: Array<any>;
};

/** 地图数据 */
export const mapJson = (params?: object) => {
  return http.request<Result>("get", "/get-map-info", { params });
};

/** 文件上传响应类型 */
export type FileUploadResponse = {
  url: string;
  filename: string;
  size: number;
  contentType: string;
};

/** 文件上传 */
export const formUpload = (data: FormData, category?: string) => {
  const url = category
    ? `/api/files/upload?category=${category}`
    : "/api/files/upload";
  return http.request<Result<FileUploadResponse>>(
    "post",
    url,
    { data },
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    },
  );
};

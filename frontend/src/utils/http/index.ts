import Axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type CustomParamsSerializer,
} from "axios";
import type {
  PureHttpError,
  RequestMethods,
  PureHttpResponse,
  PureHttpRequestConfig,
} from "./types.d";
import { stringify } from "qs";
import { message } from "@/utils/message";
import { $t, transformI18n } from "@/plugins/i18n";
import { getToken, formatToken } from "@/utils/auth";
import { useUserStoreHook } from "@/store/modules/user";

// 相关配置请参考：www.axios-js.com/zh-cn/docs/#axios-request-config-1
const defaultConfig: AxiosRequestConfig = {
  // 请求超时时间（30秒，健康检查等特殊接口需要更长超时）
  timeout: 30000,
  headers: {
    Accept: "application/json, text/plain, */*",
    "Content-Type": "application/json",
    "X-Requested-With": "XMLHttpRequest",
  },
  // 数组格式参数序列化（https://github.com/axios/axios/issues/5142）
  paramsSerializer: {
    serialize: stringify as unknown as CustomParamsSerializer,
  },
  // 性能优化：启用 HTTP/1.1 连接复用
  // 注意：这些是浏览器默认行为，但明确设置可以确保连接复用正常工作
  // maxRedirects: 5,  // 默认值，保持重定向限制
  // validateStatus: (status) => status >= 200 && status < 300,  // 默认值
  // 性能优化：对于 localhost，使用 keep-alive 连接
  // 注意：浏览器会自动处理连接复用，但我们可以通过设置合适的 headers 来优化
  httpAgent: undefined, // 浏览器环境不需要设置
  httpsAgent: undefined, // 浏览器环境不需要设置
};

class PureHttp {
  constructor() {
    this.httpInterceptorsRequest();
    this.httpInterceptorsResponse();
  }

  /** `token`过期后，暂存待执行的请求 */
  private static requests = [];

  /** 防止重复刷新`token` */
  private static isRefreshing = false;

  /** 初始化配置对象 */
  private static initConfig: PureHttpRequestConfig = {};

  /** 保存当前`Axios`实例对象 */
  private static axiosInstance: AxiosInstance = Axios.create(defaultConfig);

  /** 重连原始请求 */
  private static retryOriginalRequest(config: PureHttpRequestConfig) {
    return new Promise((resolve) => {
      PureHttp.requests.push((token: string) => {
        config.headers["Authorization"] = formatToken(token);
        resolve(config);
      });
    });
  }

  /** 请求拦截 */
  private httpInterceptorsRequest(): void {
    PureHttp.axiosInstance.interceptors.request.use(
      async (config: PureHttpRequestConfig): Promise<any> => {
        const requestStartTime = performance.now();
        const requestId = `${config.method?.toUpperCase()}_${config.url}_${Date.now()}`;

        // 在 config 中保存开始时间，用于响应拦截器计算总耗时
        (config as any).__requestStartTime = requestStartTime;
        (config as any).__requestId = requestId;

        // 记录浏览器网络栈信息
        const networkInfo = {
          connection:
            (navigator as any).connection ||
            (navigator as any).mozConnection ||
            (navigator as any).webkitConnection,
          timing: performance.getEntriesByType(
            "navigation",
          )[0] as PerformanceNavigationTiming,
        };

        // 不记录每个请求的详细信息，只在性能问题时记录

        // 优先判断post/get等方法是否传入回调，否则执行初始化设置等回调
        if (typeof config.beforeRequestCallback === "function") {
          config.beforeRequestCallback(config);
          return config;
        }
        if (PureHttp.initConfig.beforeRequestCallback) {
          PureHttp.initConfig.beforeRequestCallback(config);
          return config;
        }

        /** 请求白名单，放置一些不需要`token`的接口（通过设置请求白名单，防止`token`过期后再请求造成的死循环问题） */
        const whiteList = [
          "/refresh-token",
          "/login",
          "/api/auth/login",
          "/api/auth/register",
          "/api/auth/refresh",
          "/api/auth/forgot-password",
          "/api/auth/reset-password",
          "/api/init",
        ];

        const isWhiteList = whiteList.some((url) => config.url?.endsWith(url));

        if (isWhiteList) {
          return config;
        }

        // 优化：使用 async/await 而不是 Promise，确保立即执行
        const data = getToken();

        if (data && data.accessToken) {
          const now = new Date().getTime();
          const expired = parseInt(String(data.expires)) - now <= 0;
          if (expired) {
            // Token 过期，需要刷新
            if (!PureHttp.isRefreshing) {
              PureHttp.isRefreshing = true;
              // token过期刷新
              useUserStoreHook()
                .handRefreshToken({ refreshToken: data.refreshToken })
                .then((res) => {
                  const token = res.data.accessToken;
                  config.headers["Authorization"] = formatToken(token);
                  PureHttp.requests.forEach((cb) => cb(token));
                  PureHttp.requests = [];
                })
                .catch((_err) => {
                  console.error(`[HTTP错误] ${requestId} Token刷新失败`, _err);
                  PureHttp.requests = [];
                  useUserStoreHook().logOut();
                  message(transformI18n($t("login.pureLoginExpired")), {
                    type: "warning",
                  });
                })
                .finally(() => {
                  PureHttp.isRefreshing = false;
                });
            }
            // 返回重试请求的 Promise
            return PureHttp.retryOriginalRequest(config);
          } else {
            // Token 有效，立即设置并返回
            config.headers["Authorization"] = formatToken(data.accessToken);
            return config;
          }
        } else {
          // 没有 token，但不在白名单中，可能是未登录状态
          // 让请求继续，由响应拦截器处理认证错误
          return config;
        }
      },
      (error) => {
        return Promise.reject(error);
      },
    );
  }

  /** 响应拦截 */
  private httpInterceptorsResponse(): void {
    const instance = PureHttp.axiosInstance;
    instance.interceptors.response.use(
      (response: PureHttpResponse) => {
        const responseStartTime = performance.now();
        const $config = response.config;
        const requestId = ($config as any).__requestId || "unknown";
        const requestStartTime = ($config as any).__requestStartTime;

        // 只记录性能警告和错误
        if (requestStartTime) {
          const totalTime = performance.now() - requestStartTime;

          // 获取浏览器 Performance API 的详细信息
          const performanceEntries = performance.getEntriesByName(
            $config.url || "",
            "resource",
          ) as PerformanceResourceTiming[];
          const resourceTiming = performanceEntries[0];

          if (resourceTiming) {
            // 如果排队时间很长，特别警告
            const queuingTime =
              resourceTiming.startTime - resourceTiming.fetchStart;
            if (queuingTime > 1000) {
              console.error(
                `[HTTP严重警告] ${requestId} 请求排队时间过长: ${queuingTime.toFixed(2)}ms, URL: ${$config.url}`,
              );
            }

            // 如果连接建立时间很长，特别警告
            const connectTime =
              resourceTiming.connectEnd - resourceTiming.connectStart;
            if (connectTime > 1000) {
              console.error(
                `[HTTP严重警告] ${requestId} 连接建立时间过长: ${connectTime.toFixed(2)}ms, URL: ${$config.url}`,
              );
            }
          }

          // 只记录超过1秒的请求
          if (totalTime > 1000) {
            console.warn(
              `[HTTP性能警告] ${requestId} 请求总耗时过长: ${totalTime.toFixed(2)}ms, URL: ${$config.url}`,
            );
          }
        }

        // 优先判断post/get等方法是否传入回调，否则执行初始化设置等回调
        if (typeof $config.beforeResponseCallback === "function") {
          $config.beforeResponseCallback(response);
          return response.data;
        }
        if (PureHttp.initConfig.beforeResponseCallback) {
          PureHttp.initConfig.beforeResponseCallback(response);
          return response.data;
        }

        return response.data;
      },
      async (error: PureHttpError) => {
        const errorStartTime = performance.now();
        const $error = error;
        $error.isCancelRequest = Axios.isCancel($error);

        const $config = $error.config;
        const requestId = ($config as any)?.__requestId || "unknown";
        const requestStartTime = ($config as any)?.__requestStartTime;

        if (requestStartTime) {
          const totalTime = performance.now() - requestStartTime;
          console.error(`[HTTP响应错误] ${requestId}`, {
            url: $config?.url,
            method: $config?.method,
            status: $error.response?.status,
            message: $error.message,
            totalTime: `${totalTime.toFixed(2)}ms`,
          });
        }

        // 处理认证错误（401/500 且包含 Token 相关错误信息）
        if (!$error.isCancelRequest && $error.response) {
          const status = $error.response.status;
          const responseData = $error.response.data;
          const errorMessage = (responseData as any)?.message || "";

          // 检查是否为认证错误（401 未授权 或 500 且包含 Token 相关错误）
          const isAuthError =
            status === 401 ||
            (status === 500 &&
              (errorMessage.includes("Token") ||
                errorMessage.includes("token") ||
                errorMessage.includes("认证") ||
                errorMessage.includes("授权")));

          if (isAuthError) {
            const tokenData = getToken();

            // 如果有 refreshToken，尝试刷新
            if (tokenData?.refreshToken && !PureHttp.isRefreshing) {
              try {
                PureHttp.isRefreshing = true;
                const res = await useUserStoreHook().handRefreshToken({
                  refreshToken: tokenData.refreshToken,
                });

                // 刷新成功，重试原请求
                const originalRequest = $error.config;
                if (originalRequest) {
                  originalRequest.headers["Authorization"] = formatToken(
                    res.data.accessToken,
                  );
                  return PureHttp.axiosInstance.request(originalRequest);
                }
              } catch (refreshError) {
                // 刷新失败，清除 token 并跳转登录
                useUserStoreHook().logOut();
                message(transformI18n($t("login.pureLoginExpired")), {
                  type: "warning",
                });
                return Promise.reject(refreshError);
              } finally {
                PureHttp.isRefreshing = false;
              }
            } else {
              // 没有 refreshToken 或正在刷新，直接登出
              useUserStoreHook().logOut();
              message(
                errorMessage || transformI18n($t("login.pureLoginExpired")),
                {
                  type: "warning",
                },
              );
            }
          } else {
            // 其他错误，显示错误消息
            const errorMsg = errorMessage || $error.message || "请求失败";
            if (errorMsg && !errorMsg.includes("Network Error")) {
              message(errorMsg, {
                type: "error",
              });
            }
          }
        }

        // 所有的响应异常 区分来源为取消请求/非取消请求
        return Promise.reject($error);
      },
    );
  }

  /** 通用请求工具函数 */
  public request<T>(
    method: RequestMethods,
    url: string,
    param?: AxiosRequestConfig,
    axiosConfig?: PureHttpRequestConfig,
  ): Promise<T> {
    const config = {
      method,
      url,
      ...param,
      ...axiosConfig,
    } as PureHttpRequestConfig;

    // 单独处理自定义请求/响应回调
    return new Promise((resolve, reject) => {
      PureHttp.axiosInstance
        .request(config)
        .then((response: undefined) => {
          resolve(response);
        })
        .catch((error) => {
          reject(error);
        });
    });
  }

  /** 单独抽离的`post`工具函数 */
  public post<T, P>(
    url: string,
    params?: AxiosRequestConfig<P>,
    config?: PureHttpRequestConfig,
  ): Promise<T> {
    return this.request<T>("post", url, params, config);
  }

  /** 单独抽离的`get`工具函数 */
  public get<T, P>(
    url: string,
    params?: AxiosRequestConfig<P>,
    config?: PureHttpRequestConfig,
  ): Promise<T> {
    return this.request<T>("get", url, params, config);
  }
}

export const http = new PureHttp();

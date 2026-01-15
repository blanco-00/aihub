import { defineStore } from "pinia";
import {
  type userType,
  store,
  router,
  resetRouter,
  routerArrays,
  storageLocal
} from "../utils";
import {
  type UserResult,
  type RefreshTokenResult,
  getLogin,
  refreshTokenApi
} from "@/api/user";
import {
  type LoginRequest,
  type RefreshTokenRequest,
  login as authLogin,
  refreshToken as authRefreshToken
} from "@/api/auth";
import { useMultiTagsStoreHook } from "./multiTags";
import { type DataInfo, setToken, removeToken, userKey, saveLastUsername } from "@/utils/auth";

export const useUserStore = defineStore("pure-user", {
  state: (): userType => ({
    // 头像
    avatar: storageLocal().getItem<DataInfo<number>>(userKey)?.avatar ?? "",
    // 用户名
    username: storageLocal().getItem<DataInfo<number>>(userKey)?.username ?? "",
    // 昵称
    nickname: storageLocal().getItem<DataInfo<number>>(userKey)?.nickname ?? "",
    // 页面级别权限
    roles: storageLocal().getItem<DataInfo<number>>(userKey)?.roles ?? [],
    // 按钮级别权限
    permissions:
      storageLocal().getItem<DataInfo<number>>(userKey)?.permissions ?? [],
    // 前端生成的验证码（按实际需求替换）
    verifyCode: "",
    // 判断登录页面显示哪个组件（0：登录（默认）、1：手机登录、2：二维码登录、3：注册、4：忘记密码）
    currentPage: 0,
    // 是否勾选了登录页的免登录（后端固定30天）
    isRemembered: false
  }),
  actions: {
    /** 存储头像 */
    SET_AVATAR(avatar: string) {
      this.avatar = avatar;
    },
    /** 存储用户名 */
    SET_USERNAME(username: string) {
      this.username = username;
    },
    /** 存储昵称 */
    SET_NICKNAME(nickname: string) {
      this.nickname = nickname;
    },
    /** 存储角色 */
    SET_ROLES(roles: Array<string>) {
      this.roles = roles;
    },
    /** 存储按钮级别权限 */
    SET_PERMS(permissions: Array<string>) {
      this.permissions = permissions;
    },
    /** 存储前端生成的验证码 */
    SET_VERIFYCODE(verifyCode: string) {
      this.verifyCode = verifyCode;
    },
    /** 存储登录页面显示哪个组件 */
    SET_CURRENTPAGE(value: number) {
      this.currentPage = value;
    },
    /** 存储是否勾选了登录页的免登录 */
    SET_ISREMEMBERED(bool: boolean) {
      this.isRemembered = bool;
    },
    /** 登入 */
    async loginByUsername(data: { username?: string; password?: string }) {
      return new Promise<UserResult>((resolve, reject) => {
        // 使用新的 auth API
        const loginData: LoginRequest = {
          usernameOrEmail: data.username || "",
          password: data.password || "",
          rememberMe: this.isRemembered
        };
        authLogin(loginData)
          .then(response => {
            if (response.code === 200) {
              // 转换后端数据格式为前端需要的格式
              const loginResponse = response.data;
              // 计算过期时间：当前时间 + expiresIn（秒）转毫秒
              const expires = new Date(Date.now() + loginResponse.expiresIn * 1000);
              const tokenData = {
                accessToken: loginResponse.token, // 后端返回的是 token
                refreshToken: loginResponse.refreshToken,
                expires: expires, // 根据 expiresIn 计算过期时间
                username: loginResponse.user.username,
                avatar: "", // 后端暂未返回头像
                nickname: loginResponse.user.nickname || loginResponse.user.username, // 使用后端返回的nickname，如果没有则使用username
                roles: [loginResponse.user.role],
                permissions: [] // 后端暂未返回权限，后续可扩展
              };
              setToken(tokenData);
              // 保存上次登录的用户名
              if (data.username) {
                saveLastUsername(data.username);
              }
              // 返回兼容旧格式的数据
              const userResult: UserResult = {
                code: 0,
                message: "登录成功",
                data: tokenData
              };
              resolve(userResult);
            } else {
              reject(response.message || "登录失败");
            }
          })
          .catch(error => {
            // 兼容旧 API 调用（降级处理）
            getLogin(data)
              .then(data => {
                if (data.code === 0) {
                  setToken(data.data);
                  // 保存上次登录的用户名
                  if (data.data?.username) {
                    saveLastUsername(data.data.username);
                  }
                  resolve(data);
                } else {
                  reject(data.message);
                }
              })
              .catch(err => {
                reject(error?.message || err?.message || "登录失败");
              });
          });
      });
    },
    /** 前端登出（不调用接口） */
    logOut() {
      this.username = "";
      this.roles = [];
      this.permissions = [];
      removeToken();
      useMultiTagsStoreHook().handleTags("equal", [...routerArrays]);
      resetRouter();
      router.push("/login");
    },
    /** 刷新`token` */
    async handRefreshToken(data: { refreshToken?: string }) {
      return new Promise<RefreshTokenResult>((resolve, reject) => {
        // 使用新的 auth API
        const refreshData: RefreshTokenRequest = {
          refreshToken: data.refreshToken || ""
        };
        authRefreshToken(refreshData)
          .then(response => {
            if (response.code === 200) {
              // 转换后端数据格式为前端需要的格式
              const loginResponse = response.data;
              // 计算过期时间：当前时间 + expiresIn（秒）转毫秒
              const expires = new Date(Date.now() + loginResponse.expiresIn * 1000);
              const tokenData = {
                accessToken: loginResponse.token, // 后端返回的是 token
                refreshToken: loginResponse.refreshToken,
                expires: expires, // 根据 expiresIn 计算过期时间
                username: loginResponse.user.username,
                avatar: "", // 后端暂未返回头像
                nickname: loginResponse.user.nickname || loginResponse.user.username, // 使用后端返回的nickname，如果没有则使用username
                roles: [loginResponse.user.role],
                permissions: [] // 后端暂未返回权限，后续可扩展
              };
              setToken(tokenData);
              // 返回兼容旧格式的数据
              const refreshResult: RefreshTokenResult = {
                code: 0,
                message: "刷新成功",
                data: {
                  accessToken: tokenData.accessToken,
                  refreshToken: tokenData.refreshToken,
                  expires: tokenData.expires
                }
              };
              resolve(refreshResult);
            } else {
              reject(response.message || "刷新失败");
            }
          })
          .catch(error => {
            // 兼容旧 API 调用（降级处理）
            refreshTokenApi(data)
              .then(data => {
                if (data.code === 0) {
                  setToken(data.data);
                  resolve(data);
                } else {
                  reject(data.message);
                }
              })
              .catch(err => {
                reject(error?.message || err?.message || "刷新失败");
              });
          });
      });
    }
  }
});

export function useUserStoreHook() {
  return useUserStore(store);
}

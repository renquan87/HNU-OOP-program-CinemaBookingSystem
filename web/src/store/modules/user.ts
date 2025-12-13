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
import { useMultiTagsStoreHook } from "./multiTags";
import { type DataInfo, setToken, removeToken, userKey } from "@/utils/auth";

// 定义扩展类型
type UserState = userType & {
  userId: string;
};

// 定义一个单独的 Key 用来存储 userId，防止被框架逻辑覆盖
const USER_ID_KEY = "cinema-user-id";

export const useUserStore = defineStore("pure-user", {
  state: (): UserState => ({
    // 头像
    avatar: storageLocal().getItem<DataInfo<number>>(userKey)?.avatar ?? "",
    // 用户名
    username: storageLocal().getItem<DataInfo<number>>(userKey)?.username ?? "",

    // 🔴 核心修复：优先从我们自定义的 Key 读取 userId
    userId: storageLocal().getItem<string>(USER_ID_KEY) ?? "",

    // 昵称
    nickname: storageLocal().getItem<DataInfo<number>>(userKey)?.nickname ?? "",
    // 页面级别权限
    roles: storageLocal().getItem<DataInfo<number>>(userKey)?.roles ?? [],
    // 按钮级别权限
    permissions:
      storageLocal().getItem<DataInfo<number>>(userKey)?.permissions ?? [],
    // 前端生成的验证码
    verifyCode: "",
    // 判断登录页面显示哪个组件
    currentPage: 0,
    // 是否勾选了登录页的免登录
    isRemembered: false,
    // 登录页的免登录存储几天
    loginDay: 7
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
    /** 存储用户ID */
    SET_USERID(userId: string) {
      this.userId = userId;
      // 手动同步到缓存
      storageLocal().setItem(USER_ID_KEY, userId);
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
    /** 设置登录页的免登录存储几天 */
    SET_LOGINDAY(value: number) {
      this.loginDay = Number(value);
    },
    /** 登入 */
    async loginByUsername(data) {
      return new Promise<UserResult>((resolve, reject) => {
        getLogin(data)
          .then(data => {
            if (data?.success) {
              const responseData = data.data;

              // 1. 更新内存状态
              this.userId = responseData["userId"] || "";
              this.username = responseData.username;
              this.roles = responseData.roles;

              // 🔴 2. 强制手动持久化 userId (双保险)
              if (this.userId) {
                storageLocal().setItem(USER_ID_KEY, this.userId);
              }

              // 3. 调用框架默认的存储逻辑 (存 Token 等)
              setToken(responseData as any);
            }
            resolve(data);
          })
          .catch(error => {
            reject(error);
          });
      });
    },
    /** 前端登出 */
    logOut() {
      this.username = "";
      this.userId = "";
      this.roles = [];
      this.permissions = [];

      // 🔴 登出时清理我们的自定义 Key
      storageLocal().removeItem(USER_ID_KEY);

      removeToken();
      useMultiTagsStoreHook().handleTags("equal", [...routerArrays]);
      resetRouter();
      router.push("/login");
    },
    /** 刷新`token` */
    async handRefreshToken(data) {
      return new Promise<RefreshTokenResult>((resolve, reject) => {
        refreshTokenApi(data)
          .then(data => {
            if (data) {
              setToken(data.data as any);

              // 刷新 Token 时，确保 userId 不丢失（虽然一般不会变，但为了保险可以重写一次）
              const storedId = storageLocal().getItem<string>(USER_ID_KEY);
              if (storedId) {
                this.userId = storedId;
              }

              resolve(data);
            }
          })
          .catch(error => {
            reject(error);
          });
      });
    }
  }
});

export function useUserStoreHook() {
  return useUserStore(store);
}

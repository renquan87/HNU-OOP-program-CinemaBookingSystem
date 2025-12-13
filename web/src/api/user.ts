import { http } from "@/utils/http";

// 1. 定义登录返回的类型
export type UserResult = {
  success: boolean;
  data: {
    username: string;
    roles: Array<string>;
    accessToken: string;
    refreshToken?: string;
    expires?: Date;
  };
};

// 2. 定义注册返回的类型
export type RegisterResult = {
  success: boolean;
  message: string;
  data: any;
};

// ==========================================
// 👇👇👇【新增】这里是你缺失的代码 👇👇👇
// ==========================================
export type RefreshTokenResult = {
  success: boolean;
  data: {
    accessToken: string;
    refreshToken: string;
    expires: Date;
  };
};
// ==========================================

/** 登录接口 */
export const getLogin = (data?: object) => {
  return http.request<UserResult>("post", "/api/login", { data });
};

/** 注册接口 (新增) */
export const getRegister = (data?: object) => {
  return http.request<RegisterResult>("post", "/api/register", { data });
};

/** 刷新 Token 接口 */
// 现在有了上面的定义，这里的 <RefreshTokenResult> 就不会报错了
export const refreshTokenApi = (data?: object) => {
  return http.request<RefreshTokenResult>("post", "/refreshToken", { data });
};

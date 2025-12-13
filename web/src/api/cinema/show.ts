import { http } from "@/utils/http";
import type { ShowResult } from "./model/showModel";

/** 获取场次列表 (支持按 movieId 筛选) */
export const getShowList = (params?: object) => {
  return http.request<ShowResult>("get", "/api/shows", { params });
};

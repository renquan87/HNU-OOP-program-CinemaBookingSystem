import { http } from "@/utils/http";
import type { MovieResult, MovieForm } from "@/api/cinema/model/movieModel";

/** 获取电影列表 */
export const getMovieList = () => {
  return http.request<MovieResult>("get", "/api/movies");
};

/** 🔴 新增：获取单个电影详情 */
export const getMovieDetail = (id: string) => {
  // 返回类型通常是包含 Movie 及其评论的响应结构，这里使用 any 简化
  return http.request<any>("get", `/api/movies/${id}`);
};

/** 🔴 新增：发表评论 */
export const addComment = (movieId: string, data: { userId: string; content: string; rating: number }) => {
  // 返回类型通常是包含新评论的响应结构，这里使用 any 简化
  return http.request<any>("post", `/api/movies/${movieId}/comments`, { data });
};

/** 添加电影 */
export const addMovie = (data: MovieForm) => {
  return http.request<MovieResult>("post", "/api/movies", { data });
};

/** 删除电影 */
export const deleteMovie = (id: string) => {
  return http.request<MovieResult>("delete", `/api/movies/${id}`);
};

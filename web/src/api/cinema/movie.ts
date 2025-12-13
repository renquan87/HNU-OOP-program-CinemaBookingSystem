import { http } from "@/utils/http";
import type { MovieResult, MovieForm } from "@/api/cinema/model/movieModel";

/** 获取电影列表 */
export const getMovieList = () => {
  return http.request<MovieResult>("get", "/api/movies");
};

/** 添加电影 */
export const addMovie = (data: MovieForm) => {
  return http.request<MovieResult>("post", "/api/movies", { data });
};

/** 删除电影 */
export const deleteMovie = (id: string) => {
  return http.request<MovieResult>("delete", `/api/movies/${id}`);
};

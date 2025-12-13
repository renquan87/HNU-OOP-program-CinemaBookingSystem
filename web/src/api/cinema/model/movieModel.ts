/** 电影实体接口 */
export interface MovieItem {
  id: string;
  title: string;
  director: string;
  actors: string[]; // 对应 Java 的 List<String>
  duration: number;
  rating: number;
  description: string;
  genre: string;
  releaseTime: string; // "2023-01-01"
}

/** 提交表单时的参数类型 */
export interface MovieForm {
  title: string;
  director: string;
  actors: string; // 提交时还是字符串，方便输入
  duration: number;
  rating: number;
  description: string;
  genre: string;
  releaseTime: string;
}

/** 通用返回结构 */
export type MovieResult = {
  success: boolean;
  data: Array<MovieItem>;
  message?: string;
};

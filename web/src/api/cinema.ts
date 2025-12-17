import { http } from "@/utils/http";
import type { ShowResult } from "./cinema/model/showModel";

/** 获取所有场次 (可传movieId筛选) */
export const getShowList = (movieId?: string) => {
  return http.request<ShowResult>("get", "/api/shows", { params: { movieId } });
};

/** 获取所有放映厅 */
export const getRoomList = () => {
  return http.request<any>("get", "/api/rooms");
};

/** 添加场次 (排片) */
export const addShow = (data: {
  movieId: string;
  roomId: string;
  startTime: string;
  price: number;
}) => {
  return http.request<any>("post", "/api/shows", { data });
};

/** 删除场次 */
export const deleteShow = (id: string) => {
  return http.request<any>("delete", `/api/shows/${id}`);
};

/** 获取场次座位图 */
export const getShowSeats = (showId: string) => {
  return http.request<any>("get", `/api/shows/${showId}/seats`);
};

/** 创建订单 (锁座) */
export const createOrder = (data: {
  userId: string;
  showId: string;
  seatIds: string[];
}) => {
  return http.request<any>("post", "/api/booking/create", { data });
};

/** 支付订单 */
export const payOrder = (data: { orderId: string }) => {
  return http.request<any>("post", "/api/booking/pay", { data });
};

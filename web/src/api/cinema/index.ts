import { http } from "@/utils/http";
import type { ShowResult } from "./model/showModel";

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

/** 获取用户订单 */
export const getUserOrders = (userId: string) => {
  return http.request<any>("get", "/api/booking/my-orders", {
    params: { userId }
  });
};

/** 退票 */
export const refundOrder = (data: { orderId: string }) => {
  return http.request<any>("post", "/api/booking/refund", { data });
};

// ================== 新增：管理员接口 ==================

/** 🔴 新增：管理员获取所有订单 */
export const getAllOrders = () => {
  return http.request<any>("get", "/api/booking/all");
};

/** 🔴 新增：获取消息通知 */
export const getNoticeList = (userId: string) => {
  return http.request<any>("get", "/api/notice/list", { params: { userId } });
};

// 🔴 新增：调用 AI 助手
export const askAi = (message: string) => {
  return http.request<any>("post", "/api/chat/ask", { data: { message } });
};

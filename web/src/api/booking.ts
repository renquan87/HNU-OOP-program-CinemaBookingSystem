import { http } from "@/utils/http";

// 获取座位图结果类型
export type SeatResult = {
  success: boolean;
  data: Array<{
    id: string;      // "1-1"
    row: number;
    col: number;
    type: string;    // "regular", "vip", "discount"
    status: string;  // "available", "locked", "sold"
    price: number;
  }>;
};

// 下单请求参数
export type BookingRequest = {
  userId: string;
  showId: string;
  seatIds: string[];
};

// 下单结果
export type BookingResult = {
  success: boolean;
  message: string;
  data: {
    orderId: string;
    totalAmount: number;
  };
};

/** 获取某场次的座位图 */
export const getShowSeats = (showId: string) => {
  return http.request<SeatResult>("get", `/api/shows/${showId}/seats`);
};

/** 创建订单 */
export const createOrder = (data: BookingRequest) => {
  return http.request<BookingResult>("post", "/api/booking/create", { data });
};

package com.cinema.controller;

import com.cinema.model.Order;
import com.cinema.model.Show;
import com.cinema.model.User;
import com.cinema.service.BookingService;
import com.cinema.service.CinemaManager;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 请求参数类
class BookingRequest {
    public String userId;
    public String showId;
    public List<String> seatIds; // 例如 ["1-1", "1-2"]
}

class PayRequest {
    public String orderId;
}

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    // 1. 创建订单 (锁座)
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody BookingRequest req) {
        Map<String, Object> response = new HashMap<>();
        try {
            CinemaManager cinemaManager = CinemaManager.getInstance();
            BookingService bookingService = BookingService.getInstance();

            User user = cinemaManager.getUser(req.userId);
            Show show = cinemaManager.getShow(req.showId);

            // 调用 Service 层的核心逻辑
            // 使用 reserveOrder 进行预订（带15分钟锁定逻辑）
            Order order = bookingService.reserveOrder(user, show, req.seatIds);

            response.put("success", true);
            response.put("code", 200);
            response.put("message", "下单成功，请在15分钟内支付");
            response.put("data", Map.of(
                    "orderId", order.getOrderId(),
                    "totalAmount", order.getTotalAmount(),
                    "createTime", order.getCreateTime()
            ));

        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 400);
            response.put("message", "下单失败: " + e.getMessage());
        }
        return response;
    }

    // 2. 支付订单
    @PostMapping("/pay")
    public Map<String, Object> payOrder(@RequestBody PayRequest req) {
        Map<String, Object> response = new HashMap<>();
        try {
            BookingService bookingService = BookingService.getInstance();
            Order order = bookingService.getOrder(req.orderId);

            if (order == null) {
                throw new RuntimeException("订单不存在");
            }

            // 执行支付逻辑
            bookingService.processReservedOrderPayment(order);

            response.put("success", true);
            response.put("code", 200);
            response.put("message", "支付成功");

        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 400);
            response.put("message", "支付失败: " + e.getMessage());
        }
        return response;
    }
}
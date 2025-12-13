package com.cinema.controller;

import com.cinema.model.Order;
import com.cinema.model.Show;
import com.cinema.model.User;
import com.cinema.model.Seat;
import com.cinema.service.BookingService;
import com.cinema.service.CinemaManager;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

// 请求参数类
class BookingRequest {
    public String userId;
    public String showId;
    public List<String> seatIds; // 例如 ["1-1", "1-2"]
}

class PayRequest {
    public String orderId;
}

class RefundRequest {
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
            // 注意：BookingService 必须在 CinemaManager 或其他地方初始化时通过 PricingStrategy 传入
            BookingService bookingService = BookingService.getInstance();

            User user = cinemaManager.getUser(req.userId);
            Show show = cinemaManager.getShow(req.showId);

            if (user == null) {
                throw new RuntimeException("用户ID无效");
            }
            if (show == null) {
                throw new RuntimeException("场次ID无效");
            }

            // 调用 Service 层的核心逻辑，使用 reserveOrder 进行预订（带15分钟锁定逻辑）
            Order order = bookingService.reserveOrder(user, show, req.seatIds);

            response.put("success", true);
            response.put("code", 200);
            response.put("message", "下单成功，座位已锁定，请在15分钟内支付");
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

            // 执行支付逻辑，处理 RESERVED -> PAID 状态转换
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

    // 3. 用户查询自己的订单
    @GetMapping("/my-orders")
    public Map<String, Object> getMyOrders(@RequestParam String userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            CinemaManager cinemaManager = CinemaManager.getInstance();
            BookingService bookingService = BookingService.getInstance();

            User user = cinemaManager.getUser(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 刷新订单状态（处理过期，确保用户看到最新状态）
            bookingService.checkExpiredOrders();

            List<Order> orders = bookingService.getOrdersByUser(user);

            // 转换为简化的前端结构
            List<Map<String, Object>> orderList = new ArrayList<>();
            for (Order order : orders) {
                Map<String, Object> item = new HashMap<>();
                item.put("orderId", order.getOrderId());
                item.put("movieTitle", order.getShow().getMovieTitle());
                item.put("startTime", order.getShow().getStartTime().toString());
                item.put("roomName", order.getShow().getScreeningRoomName());
                item.put("seats", order.getSeatIds());
                item.put("totalAmount", order.getTotalAmount());
                item.put("status", order.getStatus().toString()); // PENDING, PAID, REFUNDED...
                item.put("createTime", order.getCreateTime().toString());
                orderList.add(item);
            }

            response.put("success", true);
            response.put("code", 200);
            response.put("data", orderList);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取订单失败: " + e.getMessage());
        }
        return response;
    }

    // 4. 退票接口
    @PostMapping("/refund")
    public Map<String, Object> refundOrder(@RequestBody RefundRequest req) {
        Map<String, Object> response = new HashMap<>();
        try {
            BookingService bookingService = BookingService.getInstance();
            Order order = bookingService.getOrder(req.orderId);

            if (order == null) {
                throw new RuntimeException("订单不存在");
            }

            bookingService.cancelOrder(order); // 调用 Service 层的取消/退票逻辑

            response.put("success", true);
            response.put("code", 200);
            response.put("message", "退票成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "退票失败: " + e.getMessage());
        }
        return response;
    }

    // 5. 管理员获取所有订单 (新增功能)
    @GetMapping("/all")
    public Map<String, Object> getAllOrders() {
        // 在实际应用中，这里需要进行权限校验，确保只有管理员能访问
        // 假设当前逻辑仅为数据展示

        Map<String, Object> response = new HashMap<>();
        try {
            BookingService bookingService = BookingService.getInstance();
            List<Order> orders = bookingService.getAllOrders();

            List<Map<String, Object>> list = new ArrayList<>();
            for (Order order : orders) {
                Map<String, Object> item = new HashMap<>();
                item.put("orderId", order.getOrderId());
                // 确保 order.getUser() 不为空，以防数据不完整
                item.put("userId", order.getUser() != null ? order.getUser().getId() : "未知");
                item.put("movieTitle", order.getShow().getMovieTitle());
                item.put("seats", order.getSeatIds());
                item.put("amount", order.getTotalAmount());
                item.put("status", order.getStatus());
                item.put("time", order.getCreateTime().toString());
                list.add(item);
            }

            response.put("success", true);
            response.put("code", 200);
            response.put("data", list);
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500); // 内部错误
            response.put("message", "获取所有订单失败: " + e.getMessage());
        }
        return response;
    }
}
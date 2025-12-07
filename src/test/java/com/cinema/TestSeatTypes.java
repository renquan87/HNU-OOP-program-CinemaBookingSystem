package com.cinema;

import com.cinema.service.CinemaManager;
import com.cinema.service.BookingService;
import com.cinema.strategy.StandardPricing;
import com.cinema.model.User;
import com.cinema.model.Show;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Order;
import java.util.List;

public class TestSeatTypes {
    public static void main(String[] args) {
        System.out.println("===== 测试座位类型和预订功能 =====\n");
        
        // 初始化服务
        BookingService bookingService = BookingService.getInstance(new StandardPricing());
        CinemaManager cinemaManager = CinemaManager.getInstance();
        
        // 获取管理员用户
        User admin = cinemaManager.getUser("ADMIN-001");
        if (admin == null) {
            System.out.println("管理员用户不存在");
            return;
        }
        
        // 测试1: 检查座位类型分布
        System.out.println("1. 测试座位类型分布");
        List<ScreeningRoom> rooms = cinemaManager.getAllScreeningRooms();
        for (ScreeningRoom room : rooms) {
            System.out.println("\n放映厅: " + room.getName());
            System.out.println("  总座位数: " + room.getTotalSeats());
            System.out.println("  VIP座位数: " + room.getVipSeatsCount());
            System.out.println("  普通座位数: " + room.getRegularSeatsCount());
            
            // 计算优惠座位数（第一排）
            int discountSeats = room.getColumns();
            System.out.println("  优惠座位数: " + discountSeats + " (第一排)");
        }
        
        // 测试2: 测试预订功能
        System.out.println("\n2. 测试预订功能");
        List<Show> shows = cinemaManager.getAllShows();
        if (!shows.isEmpty()) {
            Show show = shows.get(0);
            System.out.println("选择场次: " + show.getMovie().getTitle());
            
            // 尝试预订座位
            try {
                Order order = bookingService.reserveOrder(admin, show, List.of("1-1", "1-2"));
                System.out.println("预订成功!");
                System.out.println("订单ID: " + order.getOrderId());
                System.out.println("订单状态: " + order.getStatus());
                System.out.println("剩余锁定时间: " + order.getRemainingLockMinutes() + " 分钟");
                
                // 测试支付预订订单
                bookingService.processReservedOrderPayment(order);
                System.out.println("支付成功！订单状态: " + order.getStatus());
                
            } catch (Exception e) {
                System.out.println("预订失败: " + e.getMessage());
            }
        }
        
        // 测试3: 检查过期订单
        System.out.println("\n3. 测试过期订单处理");
        bookingService.checkExpiredOrders();
        System.out.println("过期订单检查完成");
        
        System.out.println("\n测试完成！");
    }
}
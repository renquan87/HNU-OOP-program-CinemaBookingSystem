package com.cinema;

import com.cinema.service.CinemaManager;
import com.cinema.service.BookingService;
import com.cinema.strategy.StandardPricing;
import com.cinema.model.User;
import com.cinema.model.Order;
import com.cinema.model.Show;
import java.util.List;

public class TestNewFeatures {
    public static void main(String[] args) {
        System.out.println("===== 测试新功能 =====\n");
        
        // 初始化服务
        BookingService bookingService = BookingService.getInstance(new StandardPricing());
        CinemaManager cinemaManager = CinemaManager.getInstance();
        
        // 获取管理员用户
        User admin = cinemaManager.getUser("ADMIN-001");
        if (admin == null) {
            System.out.println("管理员用户不存在");
            return;
        }
        
        // 测试1: 检查场次列表
        System.out.println("1. 测试场次列表显示");
        List<Show> shows = cinemaManager.getAllShows();
        System.out.println("可用场次数量: " + shows.size());
        for (int i = 0; i < Math.min(3, shows.size()); i++) {
            Show show = shows.get(i);
            System.out.println("  " + (i + 1) + ". " + show.getMovie().getTitle() + 
                " - " + show.getScreeningRoom().getName() + 
                " (" + show.getStartTime() + ") | 可用座位: " + show.getAvailableSeatsCount());
        }
        
        // 测试2: 检查订单状态
        System.out.println("\n2. 测试订单状态");
        List<Order> orders = admin.getOrders();
        System.out.println("订单数量: " + orders.size());
        for (Order order : orders) {
            System.out.println("  订单 " + order.getOrderId() + " - 状态: " + order.getStatus());
            if (order.getStatus() == Order.OrderStatus.RESERVED) {
                System.out.println("    剩余锁定时间: " + order.getRemainingLockMinutes() + " 分钟");
                System.out.println("    是否过期: " + order.isExpired());
            }
        }
        
        // 测试3: 检查过期订单处理
        System.out.println("\n3. 测试过期订单处理");
        bookingService.checkExpiredOrders();
        System.out.println("过期订单检查完成");
        
        System.out.println("\n测试完成！");
    }
}
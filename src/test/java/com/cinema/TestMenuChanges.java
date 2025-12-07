package com.cinema;

import com.cinema.service.CinemaManager;
import com.cinema.service.BookingService;
import com.cinema.strategy.StandardPricing;
import com.cinema.strategy.PremiumPricing;
import com.cinema.model.User;

public class TestMenuChanges {
    public static void main(String[] args) {
        System.out.println("===== 测试菜单更改 =====");
        
        // 初始化服务
        BookingService bookingService = BookingService.getInstance(new StandardPricing());
        CinemaManager cinemaManager = CinemaManager.getInstance();
        
        // 测试定价策略切换
        System.out.println("\n1. 测试定价策略切换");
        System.out.println("当前策略: " + bookingService.getPricingStrategy().getClass().getSimpleName());
        
        bookingService.setPricingStrategy(new PremiumPricing());
        System.out.println("切换后策略: " + bookingService.getPricingStrategy().getClass().getSimpleName());
        
        bookingService.setPricingStrategy(new StandardPricing());
        System.out.println("再次切换后: " + bookingService.getPricingStrategy().getClass().getSimpleName());
        
        // 测试用户角色验证
        System.out.println("\n2. 测试用户角色验证");
        User admin = cinemaManager.getUser("ADMIN-001");
        if (admin != null) {
            System.out.println("管理员用户: " + admin.getName() + " (角色: " + admin.getRole() + ")");
            System.out.println("是否为管理员: " + admin.isAdmin());
        }
        
        System.out.println("\n测试完成！");
    }
}
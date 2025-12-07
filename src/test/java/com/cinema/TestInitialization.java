package com.cinema;

import com.cinema.service.CinemaManager;
import com.cinema.service.BookingService;
import com.cinema.strategy.StandardPricing;
import com.cinema.model.User;

public class TestInitialization {
    public static void main(String[] args) {
        System.out.println("===== 测试系统初始化 =====");
        
        // 先初始化BookingService
        BookingService bookingService = BookingService.getInstance(new StandardPricing());
        
        // 创建新的CinemaManager实例会触发初始化
        CinemaManager cinemaManager = CinemaManager.getInstance();
        
        System.out.println("初始化完成");
        
        // 检查管理员用户
        User admin = cinemaManager.getUser("ADMIN-001");
        System.out.println("管理员用户存在: " + (admin != null));
        
        // 强制保存所有数据
        System.out.println("保存所有数据...");
        cinemaManager.saveAllData();
        System.out.println("数据保存完成");
    }
}
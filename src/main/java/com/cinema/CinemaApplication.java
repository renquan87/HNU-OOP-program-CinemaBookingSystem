package com.cinema;

import com.cinema.service.BookingService;
import com.cinema.service.CinemaManager;
import com.cinema.storage.SimpleDatabaseConnection;
import com.cinema.strategy.StandardPricing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CinemaApplication {
    public static void main(String[] args) {
        try {
            // =========================================================
            // 🔴 核心修复：在 Spring 启动前，手动初始化遗留的单例服务
            // =========================================================

            // 1. 设置数据库连接参数 (避免 BookingService 加载数据时连不上库)
            SimpleDatabaseConnection.setCommandLineArgs(args);

            // 2. 初始化 BookingService (注入标准定价策略)
            // 这一步会触发数据库连接、加载订单数据等操作
            BookingService.getInstance(new StandardPricing());
            System.out.println("✅ BookingService 初始化完成");

            // 3. 初始化 CinemaManager
            // 这一步会加载电影、影厅、场次、用户数据
            CinemaManager.getInstance();
            System.out.println("✅ CinemaManager 初始化完成");

            // =========================================================
            // 4. 启动 Spring Boot
            SpringApplication.run(CinemaApplication.class, args);

        } catch (Throwable e) {
            System.err.println("❌❌❌ 系统启动失败 ❌❌❌");
            e.printStackTrace();
        }
    }
}
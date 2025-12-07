package com.cinema;

import com.cinema.ui.ConsoleUI;
import com.cinema.service.CinemaManager;
import com.cinema.service.BookingService;
import com.cinema.strategy.StandardPricing;

public class TestUIAlignment {
    public static void main(String[] args) {
        System.out.println("===== 测试UI对齐问题 =====\n");
        
        // 初始化服务
        BookingService bookingService = BookingService.getInstance(new StandardPricing());
        CinemaManager cinemaManager = CinemaManager.getInstance();
        ConsoleUI ui = new ConsoleUI();
        
        // 测试不同宽度的分隔线
        System.out.println("1. 测试不同宽度的分隔线:");
        int[] lengths = {30, 50, 60, 70};
        for (int length : lengths) {
            System.out.println("长度 " + length + ":");
            try {
                java.lang.reflect.Method method = ConsoleUI.class.getDeclaredMethod("printSeparator", char.class, int.class);
                method.setAccessible(true);
                method.invoke(ui, ' ', length);
                method.setAccessible(false);
            } catch (Exception e) {
                System.err.println("无法调用printSeparator方法: " + e.getMessage());
            }
            System.out.println();
        }
        
        // 测试不同长度的标题
        System.out.println("\n2. 测试不同长度的标题:");
        String[] titles = {
            "登录",
            "用户登录系统",
            "这是一个非常长的标题用来测试对齐效果是否正确"
        };
        for (String title : titles) {
            System.out.println("标题: \"" + title + "\"");
            try {
                java.lang.reflect.Method method = ConsoleUI.class.getDeclaredMethod("printTitle", String.class);
                method.setAccessible(true);
                method.invoke(ui, title);
                method.setAccessible(false);
            } catch (Exception e) {
                System.err.println("无法调用printTitle方法: " + e.getMessage());
            }
            System.out.println();
        }
        
        // 测试菜单项
        System.out.println("\n3. 测试菜单项:");
        try {
            java.lang.reflect.Method method = ConsoleUI.class.getDeclaredMethod("printMenuItem", int.class, String.class);
            method.setAccessible(true);
            method.invoke(ui, 1, "浏览电影");
            method.invoke(ui, 2, "查询场次");
            method.invoke(ui, 3, "这是一个非常长的菜单项用来测试对齐");
            method.setAccessible(false);
        } catch (Exception e) {
            System.err.println("无法调用printMenuItem方法: " + e.getMessage());
        }
        
        System.out.println("\n测试完成！");
    }
}
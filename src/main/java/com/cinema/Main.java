package com.cinema;

import com.cinema.ui.ConsoleUI;
import com.cinema.service.BookingService;
import com.cinema.service.CinemaManager;
import com.cinema.strategy.StandardPricing;
import java.io.Console;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            // 设置系统编码为UTF-8
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("sun.jnu.encoding", "UTF-8");
            
            // 设置默认字符集
            if (Charset.defaultCharset().name().equals("GBK")) {
                // 在Windows系统上尝试设置控制台代码页为UTF-8
                try {
                    new ProcessBuilder("cmd", "/c", "chcp 65001").inheritIO().start().waitFor();
                } catch (Exception e) {
                    // 忽略设置失败
                }
            }
            
            // Initialize BookingService first
            BookingService bookingService = BookingService.getInstance(new StandardPricing());
            
            // Then initialize CinemaManager
            CinemaManager cinemaManager = CinemaManager.getInstance();
            
            // Start the console UI
            ConsoleUI ui = new ConsoleUI();
            ui.start();
        } catch (Exception e) {
            System.err.println("系统启动失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 确保程序退出时关闭数据库连接
            try {
                CinemaManager cinemaManager = CinemaManager.getInstance();
                cinemaManager.shutdown();
            } catch (Exception e) {
                System.err.println("关闭数据库连接时出错: " + e.getMessage());
            }
        }
    }
}
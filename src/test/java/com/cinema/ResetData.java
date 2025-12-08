package com.cinema;

import com.cinema.model.*;
import com.cinema.service.CinemaManager;
import com.cinema.service.BookingService;
import com.cinema.strategy.StandardPricing;
import com.cinema.storage.SimpleDataStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 数据重置程序 - 清空所有订单和场次，保留用户账号
 * 更新电影放映日期为未来日期，重新创建放映厅和场次
 */
public class ResetData {
    public static void main(String[] args) {
        System.out.println("开始重置数据...");
        
        // 获取管理器实例
        CinemaManager cinemaManager = CinemaManager.getInstance();
        BookingService bookingService = BookingService.getInstance(new StandardPricing());
        
        // 1. 保存所有用户
        System.out.println("保存用户数据...");
        List<User> users = cinemaManager.getAllUsers();
        System.out.println("保存了 " + users.size() + " 个用户账号");
        
        // 2. 保存所有电影
        System.out.println("保存电影数据...");
        List<Movie> movies = cinemaManager.getAllMovies();
        System.out.println("保存了 " + movies.size() + " 部电影");
        
        // 3. 清空所有订单
        System.out.println("清空所有订单...");
        bookingService.getAllOrders().clear();
        bookingService.saveOrders();
        
        // 清空用户的订单列表
        for (User user : users) {
            user.getOrders().clear();
        }
        
        // 4. 清空所有场次
        System.out.println("清空所有场次...");
        cinemaManager.getAllShows().clear();
        
        // 5. 清空所有放映厅
        System.out.println("清空所有放映厅...");
        cinemaManager.getAllScreeningRooms().clear();
        
        // 6. 重新创建放映厅（使用新的座位布局）
        System.out.println("创建新的放映厅...");
        createScreeningRooms(cinemaManager);
        
        // 7. 更新电影放映日期并创建新场次
        System.out.println("创建新的场次...");
        createShows(cinemaManager);
        
        // 8. 保存所有数据
        System.out.println("保存数据...");
        cinemaManager.saveAllData();
        bookingService.saveOrders();
        
        System.out.println("数据重置完成！");
        System.out.println("用户账号: " + users.size() + " 个（已保留）");
        System.out.println("电影: " + movies.size() + " 部（已保留）");
        System.out.println("放映厅: " + cinemaManager.getAllScreeningRooms().size() + " 个（已重新创建）");
        System.out.println("场次: " + cinemaManager.getAllShows().size() + " 个（已重新创建）");
        System.out.println("订单: 0 个（已清空）");
    }
    
    private static void createScreeningRooms(CinemaManager cinemaManager) {
        // 创建多个不同大小的放映厅
        ScreeningRoom room1 = new ScreeningRoom("ROOM-1", "1号厅（小厅）", 8, 12);
        ScreeningRoom room2 = new ScreeningRoom("ROOM-2", "2号厅（中厅）", 10, 15);
        ScreeningRoom room3 = new ScreeningRoom("ROOM-3", "3号厅（大厅）", 12, 18);
        ScreeningRoom room4 = new ScreeningRoom("ROOM-4", "4号厅（VIP厅）", 8, 10);
        
        cinemaManager.addScreeningRoom(room1);
        cinemaManager.addScreeningRoom(room2);
        cinemaManager.addScreeningRoom(room3);
        cinemaManager.addScreeningRoom(room4);
        
        System.out.println("  - 1号厅: 8排×12列 (96座位)");
        System.out.println("  - 2号厅: 10排×15列 (150座位)");
        System.out.println("  - 3号厅: 12排×18列 (216座位)");
        System.out.println("  - 4号厅: 8排×10列 (80座位，VIP专享)");
    }
    
    private static void createShows(CinemaManager cinemaManager) {
        List<Movie> movies = cinemaManager.getAllMovies();
        List<ScreeningRoom> rooms = cinemaManager.getAllScreeningRooms();
        
        if (movies.isEmpty() || rooms.isEmpty()) {
            System.out.println("警告: 没有电影或放映厅，无法创建场次");
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseTime = LocalDateTime.of(now.getYear(), now.getMonth(), 
                                                  now.getDayOfMonth(), 9, 0);
        
        int showIndex = 1;
        
        // 为每部电影创建多个场次，分布在不同的放映厅和时间段
        for (int movieIndex = 0; movieIndex < movies.size(); movieIndex++) {
            Movie movie = movies.get(movieIndex);
            
            // 每部电影创建6-8个场次
            int showsPerMovie = 6 + (movieIndex % 3);
            
            for (int i = 0; i < showsPerMovie; i++) {
                // 选择放映厅（轮换使用）
                ScreeningRoom room = rooms.get((movieIndex + i) % rooms.size());
                
                // 计算放映时间（从今天开始的未来几天）
                int daysFromNow = (movieIndex * 2 + i / 3) % 7 + 1; // 1-7天后
                int timeSlot = i % 5; // 0-4个时间段
                
                LocalDateTime showTime = baseTime
                    .plusDays(daysFromNow)
                    .plusHours(timeSlot * 3 + 9); // 9:00, 12:00, 15:00, 18:00, 21:00
                
                // 创建场次
                Show show = new Show(
                    "SHOW-" + String.format("%03d", showIndex++),
                    movie,
                    room,
                    showTime,
                    50.0 // 基础价格50元
                );
                
                cinemaManager.addShow(show);
                
                System.out.println(String.format("  - 场次 %s: %s @ %s (%s)", 
                    show.getId(), 
                    movie.getTitle(), 
                    showTime.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                    room.getName()));
            }
        }
    }
}
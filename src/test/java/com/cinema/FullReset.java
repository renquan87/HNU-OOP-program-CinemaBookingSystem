package com.cinema;

import com.cinema.model.*;
import com.cinema.service.CinemaManager;
import com.cinema.service.BookingService;
import com.cinema.strategy.StandardPricing;
import com.cinema.storage.SimpleDataStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 完全重置程序 - 清空所有订单、场次和放映厅，保留用户和电影
 */
public class FullReset {
    public static void main(String[] args) {
        System.out.println("开始完全重置数据...");
        
        // 获取管理器实例
        CinemaManager cinemaManager = CinemaManager.getInstance();
        BookingService bookingService = BookingService.getInstance(new StandardPricing());
        SimpleDataStorage dataStorage = new SimpleDataStorage();
        
        // 1. 保存所有用户
        System.out.println("保存用户数据...");
        List<User> users = cinemaManager.getAllUsers();
        
        // 清空所有用户的订单
        for (User user : users) {
            user.getOrders().clear();
        }
        System.out.println("保存了 " + users.size() + " 个用户账号，已清空其订单");
        
        // 2. 保存所有电影
        System.out.println("保存电影数据...");
        List<Movie> movies = cinemaManager.getAllMovies();
        System.out.println("保存了 " + movies.size() + " 部电影");
        
        // 3. 清空所有数据
        System.out.println("清空所有数据...");
        bookingService.getAllOrders().clear();
        cinemaManager.getAllShows().clear();
        cinemaManager.getAllScreeningRooms().clear();
        
        // 4. 重新创建放映厅（统一使用标准布局）
        System.out.println("创建新的放映厅...");
        createStandardScreeningRooms(cinemaManager);
        
        // 5. 更新电影放映日期并创建新场次
        System.out.println("创建新的场次...");
        createNewShows(cinemaManager);
        
        // 6. 保存所有数据
        System.out.println("保存数据...");
        cinemaManager.saveAllData();
        bookingService.saveOrders();
        
        System.out.println("\n数据重置完成！");
        System.out.println("用户账号: " + users.size() + " 个（已保留，订单已清空）");
        System.out.println("电影: " + movies.size() + " 部（已保留）");
        System.out.println("放映厅: " + cinemaManager.getAllScreeningRooms().size() + " 个（已重新创建）");
        System.out.println("场次: " + cinemaManager.getAllShows().size() + " 个（已重新创建）");
        System.out.println("订单: " + bookingService.getAllOrders().size() + " 个（已清空）");
    }
    
    private static void createStandardScreeningRooms(CinemaManager cinemaManager) {
        // 创建标准放映厅，所有厅都使用相同的座位布局规则
        ScreeningRoom room1 = new ScreeningRoom("ROOM-1", "1号厅", 8, 12);
        ScreeningRoom room2 = new ScreeningRoom("ROOM-2", "2号厅", 10, 15);
        ScreeningRoom room3 = new ScreeningRoom("ROOM-3", "3号厅", 12, 18);
        ScreeningRoom room4 = new ScreeningRoom("ROOM-4", "4号厅", 8, 10);
        
        cinemaManager.addScreeningRoom(room1);
        cinemaManager.addScreeningRoom(room2);
        cinemaManager.addScreeningRoom(room3);
        cinemaManager.addScreeningRoom(room4);
        
        System.out.println("  - 1号厅: 8排×12列");
        System.out.println("  - 2号厅: 10排×15列");
        System.out.println("  - 3号厅: 12排×18列");
        System.out.println("  - 4号厅: 8排×10列");
    }
    
    private static void createNewShows(CinemaManager cinemaManager) {
        List<Movie> movies = cinemaManager.getAllMovies();
        List<ScreeningRoom> rooms = cinemaManager.getAllScreeningRooms();
        
        if (movies.isEmpty() || rooms.isEmpty()) {
            System.out.println("警告: 没有电影或放映厅，无法创建场次");
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        int showIndex = 1;
        
        // 为每部电影创建多个场次
        for (int movieIndex = 0; movieIndex < movies.size(); movieIndex++) {
            Movie movie = movies.get(movieIndex);
            
            // 每部电影创建6-8个场次
            int showsPerMovie = 6 + (movieIndex % 3);
            
            for (int i = 0; i < showsPerMovie; i++) {
                // 选择放映厅（轮换使用）
                ScreeningRoom room = rooms.get((movieIndex + i) % rooms.size());
                
                // 计算放映时间（从今天开始的未来几天）
                int daysFromNow = (movieIndex * 2 + i / 3) % 14 + 1; // 1-14天后
                int hourOfDay = 9 + (i % 4) * 4; // 9:00, 13:00, 17:00, 21:00
                
                LocalDateTime showTime = LocalDateTime.of(
                    now.plusDays(daysFromNow).toLocalDate(),
                    java.time.LocalTime.of(hourOfDay, 0)
                );
                
                // 创建场次
                Show show = new Show(
                    "SHOW-" + String.format("%03d", showIndex++),
                    movie,
                    room,
                    showTime,
                    50.0 // 基础价格50元
                );
                
                cinemaManager.addShow(show);
            }
        }
    }
}
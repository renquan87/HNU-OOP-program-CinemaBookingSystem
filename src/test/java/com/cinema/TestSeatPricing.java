package com.cinema;

import com.cinema.service.CinemaManager;
import com.cinema.service.BookingService;
import com.cinema.strategy.StandardPricing;
import com.cinema.model.User;
import com.cinema.model.Show;
import com.cinema.model.ScreeningRoom;
import java.util.List;

public class TestSeatPricing {
    public static void main(String[] args) {
        System.out.println("===== 测试座位定价 =====\n");
        
        // 初始化服务
        BookingService bookingService = BookingService.getInstance(new StandardPricing());
        CinemaManager cinemaManager = CinemaManager.getInstance();
        
        // 获取管理员用户
        User admin = cinemaManager.getUser("ADMIN-001");
        if (admin == null) {
            System.out.println("管理员用户不存在");
            return;
        }
        
        // 测试1: 检查放映厅座位价格
        System.out.println("1. 检查放映厅座位价格");
        List<ScreeningRoom> rooms = cinemaManager.getAllScreeningRooms();
        for (ScreeningRoom room : rooms) {
            System.out.println("\n放映厅: " + room.getName());
            System.out.println("  第1排座位类型: " + room.getSeatLayout()[0][0].getClass().getSimpleName());
            System.out.println("  第1排座位价格: ￥" + room.getSeatLayout()[0][0].getBasePrice());
            
            int midRow = room.getRows() / 2;
            System.out.println("  中间排座位类型: " + room.getSeatLayout()[midRow][0].getClass().getSimpleName());
            System.out.println("  中间排座位价格: ￥" + room.getSeatLayout()[midRow][0].getBasePrice());
            
            int lastRow = room.getRows() - 1;
            System.out.println("   最后一排座位类型: " + room.getSeatLayout()[lastRow][0].getClass().getSimpleName());
            System.out.println("   最后一排座位价格: ￥" + room.getSeatLayout()[lastRow][0].getBasePrice());
        }
        
        // 测试2: 测试场次定价计算
        System.out.println("\n2. 测试场次定价计算");
        List<Show> shows = cinemaManager.getAllShows();
        if (!shows.isEmpty()) {
            Show show = shows.get(0);
            System.out.println("\n场次: " + show.getMovie().getTitle());
            System.out.println("  放映厅: " + show.getScreeningRoom().getName());
            
            // 测试不同座位类型的价格
            for (int row = 0; row < Math.min(3, show.getScreeningRoom().getRows()); row++) {
                for (int col = 0; col < Math.min(3, show.getScreeningRoom().getColumns()); col++) {
                    var seat = show.getSeat(row + 1, col + 1);
                    System.out.printf("  座位 %s: %s - ￥%.2f\n", 
                        seat.getSeatId(), 
                        seat.getClass().getSimpleName(), 
                        bookingService.calculateSeatPrice(show, seat));
                }
            }
        }
        
        System.out.println("\n测试完成！");
    }
}
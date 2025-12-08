package com.cinema;

import com.cinema.model.*;
import com.cinema.service.CinemaManager;
import com.cinema.service.BookingService;
import com.cinema.strategy.StandardPricing;

public class VerifyReset {
    public static void main(String[] args) {
        System.out.println("=== 验证数据重置结果 ===\n");
        
        CinemaManager cinemaManager = CinemaManager.getInstance();
        BookingService bookingService = BookingService.getInstance(new StandardPricing());
        
        // 1. 验证用户
        System.out.println("1. 用户账号:");
        for (User user : cinemaManager.getAllUsers()) {
            System.out.println("   - " + user.getId() + " (" + user.getName() + ") " + 
                             (user.isAdmin() ? "[管理员]" : "[普通用户]"));
            System.out.println("     订单数: " + user.getOrders().size());
        }
        
        // 2. 验证放映厅和座位布局
        System.out.println("\n2. 放映厅座位布局:");
        for (ScreeningRoom room : cinemaManager.getAllScreeningRooms()) {
            System.out.println("\n" + room.getName() + " (" + room.getLayout() + "):");
            Seat[][] seats = room.getSeatLayout();
            
            // 统计各类座位
            int discountCount = 0, vipCount = 0, regularCount = 0;
            
            // 打印座位图（只打印前5排和后5排的缩略）
            for (int row = 0; row < seats.length; row++) {
                if (row == 5 && seats.length > 10) {
                    System.out.println("   ...");
                    row = seats.length - 5;
                }
                
                System.out.printf("   第%2d排: ", row + 1);
                for (int col = 0; col < Math.min(seats[row].length, 10); col++) {
                    Seat seat = seats[row][col];
                    if (seat instanceof VIPSeat) {
                        System.out.print("[V]");
                        vipCount++;
                    } else if (seat instanceof DiscountSeat) {
                        System.out.print("[D]");
                        discountCount++;
                    } else {
                        System.out.print("[O]");
                        regularCount++;
                    }
                }
                if (seats[row].length > 10) {
                    System.out.print("...");
                }
                System.out.println();
            }
            
            System.out.println("   座位统计: 优惠=" + discountCount + ", VIP=" + vipCount + 
                             ", 普通=" + regularCount);
        }
        
        // 3. 验证场次
        System.out.println("\n3. 场次信息:");
        for (Show show : cinemaManager.getAllShows()) {
            System.out.println("   - " + show.getId() + ": " + 
                             show.getMovie().getTitle() + " @ " + 
                             show.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm")) +
                             " (" + show.getScreeningRoom().getName() + ")");
            System.out.println("     座位: " + show.getAvailableSeatsCount() + "/" + 
                             show.getTotalSeats() + " 可用");
        }
        
        // 4. 验证价格计算
        System.out.println("\n4. 价格验证:");
        if (!cinemaManager.getAllShows().isEmpty()) {
            Show testShow = cinemaManager.getAllShows().get(0);
            Seat[][] seats = testShow.getScreeningRoom().getSeatLayout();
            
            // 找到不同类型的座位
            Seat discountSeat = null, vipSeat = null, regularSeat = null;
            
            for (int row = 0; row < seats.length; row++) {
                for (int col = 0; col < seats[row].length; col++) {
                    Seat seat = seats[row][col];
                    if (seat instanceof DiscountSeat && discountSeat == null) {
                        discountSeat = seat;
                    } else if (seat instanceof VIPSeat && vipSeat == null) {
                        vipSeat = seat;
                    } else if (!(seat instanceof VIPSeat) && !(seat instanceof DiscountSeat) && regularSeat == null) {
                        regularSeat = seat;
                    }
                }
                if (discountSeat != null && vipSeat != null && regularSeat != null) {
                    break;
                }
            }
            
            if (discountSeat != null && vipSeat != null && regularSeat != null) {
                double discountPrice = bookingService.calculateSeatPrice(testShow, discountSeat);
                double vipPrice = bookingService.calculateSeatPrice(testShow, vipSeat);
                double regularPrice = bookingService.calculateSeatPrice(testShow, regularSeat);
                
                System.out.println("   优惠座位: ￥" + discountPrice);
                System.out.println("   普通座位: ￥" + regularPrice);
                System.out.println("   VIP座位: ￥" + vipPrice);
                System.out.println("   价格比例 - 优惠/普通: " + String.format("%.1f", discountPrice/regularPrice) + 
                                 " (期望: 0.8)");
                System.out.println("   价格比例 - VIP/普通: " + String.format("%.1f", vipPrice/regularPrice) + 
                                 " (期望: 2.0)");
            }
        }
        
        // 5. 订单验证
        System.out.println("\n5. 订单验证:");
        System.out.println("   总订单数: " + bookingService.getAllOrders().size());
    }
}
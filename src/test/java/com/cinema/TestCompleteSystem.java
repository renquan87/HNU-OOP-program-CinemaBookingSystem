package com.cinema;

import com.cinema.model.*;
import com.cinema.service.*;
import com.cinema.strategy.StandardPricing;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TestCompleteSystem {
    public static void main(String[] args) {
        // 创建影院管理器
        CinemaManager cinemaManager = CinemaManager.getInstance();

        // 创建放映厅
        ScreeningRoom room = new ScreeningRoom("ROOM-1", "测试放映厅", 10, 15);
        cinemaManager.addScreeningRoom(room);

        // --- 🔴 修复点开始 ---
        // 创建电影 (注意参数顺序和类型调整)
        Movie movie = new Movie(
                "MOV-001",           // id
                "测试电影",           // title
                LocalDate.now(),     // releaseTime (这里需要 LocalDate)
                List.of("演员1", "演员2"), // actors (这里需要 List<String>)
                "测试导演",           // director
                120,                 // duration
                8.5,                 // rating
                "测试描述",           // description
                MovieGenre.ACTION    // genre
        );
        // --- 🔴 修复点结束 ---

        cinemaManager.addMovie(movie);

        // 创建场次
        Show show = new Show("SHOW-001", movie, room,
                LocalDateTime.now().plusDays(1), 50.0);
        cinemaManager.addShow(show);

        // 创建预订服务
        BookingService bookingService = BookingService.getInstance(new StandardPricing());

        // 测试座位布局
        System.out.println("=== 座位布局测试 ===");
        Seat[][] seats = show.getScreeningRoom().getSeatLayout();

        // 打印座位布局
        System.out.println("\n座位图:");
        for (int row = 0; row < seats.length; row++) {
            for (int col = 0; col < seats[row].length; col++) {
                Seat seat = seats[row][col];
                if (seat instanceof VIPSeat) {
                    System.out.print("[V] ");
                } else if (seat instanceof DiscountSeat) {
                    System.out.print("[D] ");
                } else {
                    System.out.print("[O] ");
                }
            }
            System.out.println();
        }

        // 测试价格计算
        System.out.println("\n=== 价格测试 ===");
        Seat discountSeat = seats[0][0]; // 第一排
        Seat regularSeat = seats[1][0];  // 第二排
        Seat vipSeat = seats[4][0];      // 第五排（中间排）

        double discountPrice = bookingService.calculateSeatPrice(show, discountSeat);
        double regularPrice = bookingService.calculateSeatPrice(show, regularSeat);
        double vipPrice = bookingService.calculateSeatPrice(show, vipSeat);

        System.out.println("优惠座位(第一排): ￥" + discountPrice);
        System.out.println("普通座位(第二排): ￥" + regularPrice);
        System.out.println("VIP座位(第五排): ￥" + vipPrice);

        // 验证价格比例
        System.out.println("\n=== 价格比例验证 ===");
        System.out.println("优惠座位/普通座位: " + (discountPrice / regularPrice) + " (期望: 0.8)");
        System.out.println("VIP座位/普通座位: " + (vipPrice / regularPrice) + " (期望: 2.0)");

        // 统计各类座位数量
        int discountCount = 0, vipCount = 0, regularCount = 0;
        for (int row = 0; row < seats.length; row++) {
            for (int col = 0; col < seats[row].length; col++) {
                Seat seat = seats[row][col];
                if (seat instanceof VIPSeat) {
                    vipCount++;
                } else if (seat instanceof DiscountSeat) {
                    discountCount++;
                } else {
                    regularCount++;
                }
            }
        }

        System.out.println("\n=== 座位统计 ===");
        System.out.println("优惠座位数量: " + discountCount + " (期望: " + room.getTotalCols() + ")");
        System.out.println("VIP座位数量: " + vipCount + " (期望: " + (room.getTotalCols() * 3) + ")");
        System.out.println("普通座位数量: " + regularCount);
    }
}
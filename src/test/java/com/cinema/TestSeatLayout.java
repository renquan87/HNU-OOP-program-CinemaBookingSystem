package com.cinema;

import com.cinema.model.*;
import com.cinema.service.CinemaManager;

public class TestSeatLayout {
    public static void main(String[] args) {
        // 创建一个10排15列的放映厅
        ScreeningRoom room = new ScreeningRoom("ROOM-1", "测试放映厅", 10, 15);
        
        System.out.println("座位布局测试:");
        System.out.println("总排数: " + room.getTotalRows());
        System.out.println("总列数: " + room.getTotalCols());
        System.out.println();
        
        Seat[][] seats = room.getSeatLayout();
        
        // 打印座位布局
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
        
        System.out.println("\n座位价格:");
        // 检查第一排（优惠座位）
        Seat firstRowSeat = seats[0][0];
        System.out.println("第一排座位类型: " + firstRowSeat.getClass().getSimpleName());
        System.out.println("第一排座位价格: ￥" + firstRowSeat.getBasePrice());
        
        // 检查中间排（VIP座位）
        int middleRow = room.getTotalRows() / 2;
        Seat vipSeat = seats[middleRow][0];
        System.out.println("\n中间排(第" + (middleRow + 1) + "排)座位类型: " + vipSeat.getClass().getSimpleName());
        System.out.println("中间排座位价格: ￥" + vipSeat.getBasePrice());
        
        // 检查其他排（普通座位）
        Seat regularSeat = seats[room.getTotalRows() - 1][0];
        System.out.println("\n最后一排座位类型: " + regularSeat.getClass().getSimpleName());
        System.out.println("最后一排座位价格: ￥" + regularSeat.getBasePrice());
    }
}
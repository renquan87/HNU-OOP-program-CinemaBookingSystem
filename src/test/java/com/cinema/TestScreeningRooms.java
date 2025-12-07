package com.cinema;

import com.cinema.model.ScreeningRoom;
import com.cinema.service.CinemaManager;

public class TestScreeningRooms {
    public static void main(String[] args) {
        CinemaManager cinemaManager = CinemaManager.getInstance();
        
        System.out.println("===== 测试放映厅查看功能 =====");
        
        // 获取所有放映厅
        var rooms = cinemaManager.getAllScreeningRooms();
        
        if (rooms.isEmpty()) {
            System.out.println("暂无放映厅信息");
        } else {
            for (int i = 0; i < rooms.size(); i++) {
                ScreeningRoom room = rooms.get(i);
                System.out.println("============================================================");
                System.out.printf("%d. %s%n", i + 1, room.getName());
                System.out.println("   放映厅ID: " + room.getId());
                System.out.println("   座位布局: " + room.getRows() + " 行 × " + room.getColumns() + " 列");
                System.out.println("   总座位数: " + room.getTotalSeats());
                System.out.println("   VIP座位: " + room.getVipSeatsCount());
                System.out.println("   普通座位: " + room.getRegularSeatsCount());
                System.out.println();
            }
            System.out.println("============================================================");
        }
        
        System.out.println("测试完成！");
    }
}
package com.cinema;

import com.cinema.model.ScreeningRoom;
import com.cinema.service.CinemaManager;
import com.cinema.ui.ConsoleUI;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class TestAdminViewRooms {
    public static void main(String[] args) {
        // 模拟管理员登录
        String adminInput = "ADMIN-001\n";  // 输入管理员ID
        InputStream in = new ByteArrayInputStream(adminInput.getBytes());
        System.setIn(in);
        
        CinemaManager cinemaManager = CinemaManager.getInstance();
        ConsoleUI ui = new ConsoleUI();
        
        System.out.println("===== 测试管理员查看放映厅功能 =====");
        
        // 获取所有放映厅
        List<ScreeningRoom> rooms = cinemaManager.getAllScreeningRooms();
        
        if (rooms.isEmpty()) {
            System.out.println("暂无放映厅信息");
        } else {
            // 使用ConsoleUI的美化方法显示
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                        放映厅列表                        ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            
            for (int i = 0; i < rooms.size(); i++) {
                ScreeningRoom room = rooms.get(i);
                
                System.out.println("════════════════════════════════════════════════════════════");
                System.out.printf("\u001B[32m\u001B[1m%d. %s\u001B[0m\n", i + 1, room.getName());
                
                System.out.println("   \u001B[36m放映厅ID: \u001B[0m" + room.getId());
                System.out.println("   \u001B[36m座位布局: \u001B[0m" + room.getRows() + " 行 × " + room.getColumns() + " 列");
                System.out.println("   \u001B[36m总座位数: \u001B[0m" + room.getTotalSeats());
                System.out.println("   \u001B[36mVIP座位: \u001B[0m\u001B[35m" + room.getVipSeatsCount() + "\u001B[0m");
                System.out.println("   \u001B[36m普通座位: \u001B[0m" + room.getRegularSeatsCount());
                System.out.println();
            }
            
            System.out.println("════════════════════════════════════════════════════════════");
        }
        
        System.out.println("测试完成！管理员现在可以正常查看放映厅信息了。");
    }
}
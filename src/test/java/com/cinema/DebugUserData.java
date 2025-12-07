package com.cinema;

import com.cinema.service.CinemaManager;
import com.cinema.model.User;
import java.util.Map;

public class DebugUserData {
    public static void main(String[] args) {
        System.out.println("===== 调试用户数据加载 =====");
        
        CinemaManager cinemaManager = CinemaManager.getInstance();
        
        // 检查用户Map是否为空
        Map<String, User> users = cinemaManager.getAllUsers()
            .stream()
            .collect(java.util.stream.Collectors.toMap(User::getId, user -> user));
        
        System.out.println("用户总数: " + users.size());
        
        // 查找管理员用户
        User admin = cinemaManager.getUser("ADMIN-001");
        System.out.println("管理员用户存在: " + (admin != null));
        
        if (admin != null) {
            System.out.println("管理员详情:");
            System.out.println("  ID: " + admin.getId());
            System.out.println("  姓名: " + admin.getName());
            System.out.println("  电话: " + admin.getPhone());
            System.out.println("  邮箱: " + admin.getEmail());
            System.out.println("  角色: " + admin.getRole());
        }
        
        // 列出所有用户
        System.out.println("\n所有用户:");
        for (User user : cinemaManager.getAllUsers()) {
            System.out.println("  - " + user.getId() + " (" + user.getName() + ")");
        }
        
        // 如果没有管理员用户，重新创建一个
        if (admin == null) {
            System.out.println("\n重新创建管理员用户...");
            User newAdmin = new User(
                "ADMIN-001",
                "管理员",
                "13800138000",
                "admin@cinema.com",
                User.UserRole.ADMIN
            );
            cinemaManager.addUser(newAdmin);
            System.out.println("管理员用户已创建");
            
            // 验证创建是否成功
            admin = cinemaManager.getUser("ADMIN-001");
            System.out.println("验证管理员用户存在: " + (admin != null));
        }
    }
}
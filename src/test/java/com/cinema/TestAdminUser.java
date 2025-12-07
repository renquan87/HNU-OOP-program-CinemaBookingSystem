package com.cinema;

import com.cinema.service.CinemaManager;
import com.cinema.model.User;

public class TestAdminUser {
    public static void main(String[] args) {
        CinemaManager cinemaManager = CinemaManager.getInstance();
        
        System.out.println("===== 测试管理员用户 =====");
        
        // 检查管理员用户是否存在
        User admin = cinemaManager.getUser("ADMIN-001");
        
        if (admin != null) {
            System.out.println("管理员用户存在:");
            System.out.println("ID: " + admin.getId());
            System.out.println("姓名: " + admin.getName());
            System.out.println("电话: " + admin.getPhone());
            System.out.println("邮箱: " + admin.getEmail());
            System.out.println("角色: " + (admin.isAdmin() ? "管理员" : "普通用户"));
        } else {
            System.out.println("管理员用户不存在!");
            
            // 如果管理员用户不存在，手动创建一个
            User newAdmin = new User(
                "ADMIN-001",
                "管理员",
                "13800138000",
                "admin@cinema.com",
                User.UserRole.ADMIN
            );
            cinemaManager.addUser(newAdmin);
            System.out.println("已创建新的管理员用户");
        }
        
        // 显示所有用户
        System.out.println("\n所有用户列表:");
        for (User user : cinemaManager.getAllUsers()) {
            System.out.println("- " + user.getId() + " (" + user.getName() + ") - " + 
                (user.isAdmin() ? "管理员" : "普通用户"));
        }
    }
}
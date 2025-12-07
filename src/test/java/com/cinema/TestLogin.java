package com.cinema;

import com.cinema.service.CinemaManager;
import com.cinema.model.User;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class TestLogin {
    public static void main(String[] args) {
        // 模拟输入管理员ID
        String input = "ADMIN-001\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        
        CinemaManager cinemaManager = CinemaManager.getInstance();
        
        System.out.println("===== 测试管理员登录 =====");
        
        // 模拟登录逻辑
        String userId = "ADMIN-001";
        User user = cinemaManager.getUser(userId);
        
        if (user != null) {
            System.out.println("✓ 登录成功！欢迎，" + user.getName());
            if (user.isAdmin()) {
                System.out.println("当前角色: 管理员");
            } else {
                System.out.println("当前角色: 普通用户");
            }
        } else {
            System.out.println("✗ 用户不存在，请先注册");
        }
    }
}
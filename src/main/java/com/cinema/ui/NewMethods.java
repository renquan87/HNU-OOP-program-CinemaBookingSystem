package com.cinema.ui;

import com.cinema.model.*;
import com.cinema.service.CinemaManager;
import com.cinema.service.BookingService;

import java.util.List;
import java.util.Scanner;

public class NewMethods {
    private final CinemaManager cinemaManager;
    private final BookingService bookingService;
    private final Scanner scanner;
    private User currentUser;

    public NewMethods(CinemaManager cinemaManager, BookingService bookingService, Scanner scanner, User currentUser) {
        this.cinemaManager = cinemaManager;
        this.bookingService = bookingService;
        this.scanner = scanner;
        this.currentUser = currentUser;
    }

    public void editProfile() {
        System.out.println("\n----- 修改个人信息 -----");
        System.out.println("当前用户信息:");
        System.out.println("ID: " + currentUser.getId());
        System.out.println("姓名: " + currentUser.getName());
        System.out.println("电话: " + currentUser.getPhone());
        System.out.println("邮箱: " + currentUser.getEmail());
        System.out.println("角色: " + (currentUser.isAdmin() ? "管理员" : "普通用户"));
        
        System.out.println("\n请选择要修改的信息:");
        System.out.println("1. 姓名");
        System.out.println("2. 电话");
        System.out.println("3. 邮箱");
        System.out.println("0. 返回");
        System.out.print("请选择: ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                System.out.print("请输入新姓名: ");
                String newName = scanner.nextLine().trim();
                if (!newName.isEmpty()) {
                    currentUser.setName(newName);
                    cinemaManager.saveAllData();
                    System.out.println("姓名修改成功");
                }
                break;
            case "2":
                System.out.print("请输入新电话: ");
                String newPhone = scanner.nextLine().trim();
                if (!newPhone.isEmpty() && newPhone.matches("^1[3-9]\\d{9}$")) {
                    currentUser.setPhone(newPhone);
                    cinemaManager.saveAllData();
                    System.out.println("电话修改成功");
                } else {
                    System.out.println("电话格式错误");
                }
                break;
            case "3":
                System.out.print("请输入新邮箱: ");
                String newEmail = scanner.nextLine().trim();
                if (!newEmail.isEmpty() && newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    currentUser.setEmail(newEmail);
                    cinemaManager.saveAllData();
                    System.out.println("邮箱修改成功");
                } else {
                    System.out.println("邮箱格式错误");
                }
                break;
            case "0":
                break;
            default:
                System.out.println("无效选择");
        }
    }

    public void manageUsers() {
        while (true) {
            System.out.println("\n----- 用户管理 -----");
            System.out.println("1. 查看所有用户");
            System.out.println("2. 删除用户");
            System.out.println("3. 修改用户角色");
            System.out.println("0. 返回");
            System.out.print("请选择操作: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    viewAllUsers();
                    break;
                case "2":
                    deleteUser();
                    break;
                case "3":
                    changeUserRole();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("无效选择");
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n----- 用户列表 -----");
        List<User> users = cinemaManager.getAllUsers();
        
        if (users.isEmpty()) {
            System.out.println("暂无用户");
            return;
        }
        
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            System.out.println((i + 1) + ". " + user.toString());
            System.out.println();
        }
    }

    private void deleteUser() {
        viewAllUsers();
        
        System.out.print("请输入要删除的用户ID: ");
        String userId = scanner.nextLine().trim();
        
        if (userId.equals(currentUser.getId())) {
            System.out.println("不能删除当前登录用户");
            return;
        }
        
        User user = cinemaManager.getUser(userId);
        if (user == null) {
            System.out.println("用户不存在");
            return;
        }
        
        System.out.println("用户信息: " + user.toString());
        System.out.print("确认删除用户？(Y/N): ");
        String confirm = scanner.nextLine().trim();
        
        if (confirm.equalsIgnoreCase("Y")) {
            // 检查用户是否有未完成的订单
            List<Order> userOrders = bookingService.getAllOrders().stream()
                .filter(order -> order.getUser() != null && order.getUser().getId().equals(userId))
                .filter(order -> order.getStatus() == Order.OrderStatus.PAID)
                .collect(java.util.stream.Collectors.toList());
                
            if (!userOrders.isEmpty()) {
                System.out.println("警告：该用户有 " + userOrders.size() + " 个已支付订单");
                System.out.print("仍要删除？(Y/N): ");
                String finalConfirm = scanner.nextLine().trim();
                if (!finalConfirm.equalsIgnoreCase("Y")) {
                    System.out.println("取消删除");
                    return;
                }
            }
            
            cinemaManager.removeUser(userId);
            System.out.println("用户删除成功");
        } else {
            System.out.println("取消删除");
        }
    }

    private void changeUserRole() {
        viewAllUsers();
        
        System.out.print("请输入要修改角色的用户ID: ");
        String userId = scanner.nextLine().trim();
        
        User user = cinemaManager.getUser(userId);
        if (user == null) {
            System.out.println("用户不存在");
            return;
        }
        
        if (userId.equals(currentUser.getId())) {
            System.out.println("不能修改当前登录用户的角色");
            return;
        }
        
        System.out.println("用户当前角色: " + (user.isAdmin() ? "管理员" : "普通用户"));
        System.out.print("修改为管理员？(Y/N): ");
        String choice = scanner.nextLine().trim();
        
        boolean newIsAdmin = choice.equalsIgnoreCase("Y");
        User.UserRole newRole = newIsAdmin ? User.UserRole.ADMIN : User.UserRole.CUSTOMER;
        
        user.setRole(newRole);
        cinemaManager.saveAllData();
        
        System.out.println("角色修改成功，新角色: " + (newIsAdmin ? "管理员" : "普通用户"));
    }

    public void backupData() {
        System.out.println("\n----- 数据备份 -----");
        System.out.print("确认备份数据？(Y/N): ");
        String confirm = scanner.nextLine().trim();
        
        if (confirm.equalsIgnoreCase("Y")) {
            cinemaManager.backupData();
        } else {
            System.out.println("取消备份");
        }
    }

    public void logout() {
        System.out.println("\n----- 退出登录 -----");
        try {
            // 只保存关键数据，避免卡死
            cinemaManager.saveMovies();
            cinemaManager.saveUsers();
            System.out.println("数据已保存");
        } catch (Exception e) {
            System.err.println("保存数据时出错: " + e.getMessage());
        }
        System.out.println("再见，" + currentUser.getName() + "！");
        currentUser = null;
    }
}
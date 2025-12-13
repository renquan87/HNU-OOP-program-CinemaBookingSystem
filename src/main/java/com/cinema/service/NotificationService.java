package com.cinema.service;

import com.cinema.model.AppNotification; // 引入新模型
import com.cinema.model.Order;
import com.cinema.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationService {
    private static NotificationService instance;

    // 🔴 新增：用于存储用户通知 (UserId -> List<Notification>)
    private final Map<String, List<AppNotification>> userNotifications = new ConcurrentHashMap<>();

    private NotificationService() {}

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    /**
     * 发送订单状态更新通知
     */
    public void sendOrderUpdate(User user, Order order, String message) {
        if (user == null) return;

        // 1. 控制台打印 (保留旧逻辑)
        System.out.println("\n========== [通知服务] ==========");
        System.out.println("致用户: " + user.getName());
        System.out.println("内容: " + message);
        System.out.println("==============================\n");

        // 🔴 2. 存储到内存中
        String title = "订单状态更新";
        // 根据消息内容判断类型 (仅仅为了前端图标好看)
        String type = "通知";

        AppNotification notification = new AppNotification(title, message, type);
        addNotification(user.getId(), notification);
    }

    /**
     * 发送系统广播 (给所有用户发)
     */
    public void sendBroadcast(String message) {
        System.out.println("[系统广播] " + message);
        // 简单实现：这里暂时只存给管理员，实际应该遍历所有用户
        addNotification("ADMIN-001", new AppNotification("系统广播", message, "消息"));
    }

    // 🔴 辅助方法：添加通知
    private void addNotification(String userId, AppNotification notification) {
        userNotifications.computeIfAbsent(userId, k -> new ArrayList<>()).add(0, notification); // 插到最前面
    }

    // 🔴 新增：获取某用户的通知
    public List<AppNotification> getUserNotifications(String userId) {
        return userNotifications.getOrDefault(userId, new ArrayList<>());
    }
}
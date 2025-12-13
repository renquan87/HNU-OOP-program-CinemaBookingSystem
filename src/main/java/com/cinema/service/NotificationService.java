package com.cinema.service;

import com.cinema.model.Order;
import com.cinema.model.User;

/**
 * 通知服务
 * 负责系统中的所有消息推送
 */
public class NotificationService {
    private static NotificationService instance;

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

        // 模拟发送通知
        System.out.println("\n========== [通知服务] ==========");
        System.out.println("致用户: " + user.getName() + " (" + user.getPhone() + ")");
        System.out.println("订单号: " + order.getOrderId());
        System.out.println("内容: " + message);
        System.out.println("==============================\n");
    }

    /**
     * 发送系统广播
     */
    public void sendBroadcast(String message) {
        System.out.println("[系统广播] " + message);
    }
}
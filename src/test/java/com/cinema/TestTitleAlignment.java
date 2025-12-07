package com.cinema;

import com.cinema.ui.ConsoleUI;

public class TestTitleAlignment {
    public static void main(String[] args) {
        System.out.println("===== 测试标题框对齐 =====\n");
        
        ConsoleUI ui = new ConsoleUI();
        
        // 测试不同长度的标题
        String[] titles = {
            "短",
            "中等长度标题",
            "这是一个比较长的标题用来测试对齐效果",
            "管理员菜单",
            "用户登录系统"
        };
        
        for (String title : titles) {
            System.out.println("标题: \"" + title + "\" (长度: " + title.length() + ")");
            // 使用反射调用私有方法
            try {
                java.lang.reflect.Method method = ConsoleUI.class.getDeclaredMethod("printTitle", String.class);
                method.setAccessible(true);
                method.invoke(ui, title);
                method.setAccessible(false);
            } catch (Exception e) {
                System.err.println("无法调用printTitle方法: " + e.getMessage());
            }
            System.out.println();
        }
        
        System.out.println("测试完成！");
    }
}
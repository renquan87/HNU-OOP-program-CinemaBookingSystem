package com.cinema;

public class TestUIFixes {
    public static void main(String[] args) {
        // ANSI颜色代码
        String CYAN = "\u001B[36m";
        String YELLOW = "\u001B[33m";
        String BOLD = "\u001B[1m";
        String RESET = "\u001B[0m";
        
        // 界面装饰符号
        String LINE = "═";
        String CORNER_TL = "╔";
        String CORNER_TR = "╗";
        String CORNER_BL = "╚";
        String CORNER_BR = "╝";
        String VERTICAL = "║";
        
        // 测试标题框对齐
        System.out.println("=== 测试标题框对齐 ===");
        String title = "电影院购票系统";
        int width = Math.max(title.length() + 10, 60);
        String border = LINE.repeat(width);
        
        int padding = (width - title.length()) / 2;
        int leftPadding = padding;
        int rightPadding = width - title.length() - leftPadding;
        
        String titleLine = VERTICAL + " ".repeat(leftPadding) + title + " ".repeat(rightPadding) + VERTICAL;
        
        System.out.print(CYAN + CORNER_TL + border + CORNER_TR);
        System.out.println();
        System.out.print(CYAN + YELLOW + BOLD + titleLine);
        System.out.println();
        System.out.print(CYAN + CORNER_BL + border + CORNER_BR);
        System.out.println();
        System.out.println();
        
        // 测试价格显示
        System.out.println("=== 测试价格显示（无括号说明） ===");
        
        // 模拟价格显示
        String CYAN = "\u001B[36m";
        String YELLOW = "\u001B[33m";
        String BOLD = "\u001B[1m";
        String BLUE = "\u001B[34m";
        String PURPLE = "\u001B[35m";
        String GREEN = "\u001B[32m";
        String RESET = "\u001B[0m";
        
        System.out.print(BLUE + "  [D] 优惠座位: " + RESET);
        System.out.println(YELLOW + BOLD + "￥46.00" + RESET);
        
        System.out.print(PURPLE + BOLD + "  [V] VIP座位: " + RESET);
        System.out.println(YELLOW + BOLD + "￥69.00" + RESET);
        
        System.out.print(GREEN + "  [O] 普通座位: " + RESET);
        System.out.println(YELLOW + BOLD + "￥57.50" + RESET);
    }
}
package com.cinema.ui;

import com.cinema.model.*;
import com.cinema.service.*;
import com.cinema.strategy.StandardPricing;
import com.cinema.strategy.PremiumPricing;
import com.cinema.exception.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private final CinemaManager cinemaManager;
    private final BookingService bookingService;
    private final Scanner scanner;
    private User currentUser;
    private NewMethods newMethods;
    
    // ANSI颜色代码（用于美化控制台输出）
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String ORANGE = "\u001B[38;5;208m"; // 橙色
    private static final String BOLD = "\u001B[1m";
    
    // 界面装饰符号
    private static final String LINE = "═";
    private static final String CORNER_TL = "╔";
    private static final String CORNER_TR = "╗";
    private static final String CORNER_BL = "╚";
    private static final String CORNER_BR = "╝";
    private static final String VERTICAL = "║";
    private static final String HORIZONTAL = "─";
    private static final String CROSS = "╬";

    public ConsoleUI() {
        this.cinemaManager = CinemaManager.getInstance();
        this.bookingService = BookingService.getInstance(new StandardPricing());
        this.scanner = new Scanner(System.in);
        this.currentUser = null;
        this.newMethods = null;
        
        // 添加关闭钩子，确保程序退出时保存数据
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // 只保存关键数据，避免卡死
                cinemaManager.saveMovies();
                cinemaManager.saveUsers();
                System.out.println("\n数据已自动保存");
            } catch (Exception e) {
                System.err.println("自动保存数据时出错: " + e.getMessage());
            }
        }));
    }
    
    // 辅助方法：安全地读取用户输入
    private String readLine() {
        try {
            if (System.console() != null) {
                String input = System.console().readLine();
                return input != null ? input.trim() : "";
            } else {
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    return input != null ? input.trim() : "";
                } else {
                    return "";
                }
            }
        } catch (Exception e) {
            return "";
        }
    }
    
    // ========== 界面美化工具方法 ==========
    
    /**
     * 清屏
     */
    private void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // 打印空行来模拟清屏
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * 打印带颜色的文本
     */
    private void printColored(String color, String text) {
        System.out.print(color + text + RESET);
    }
    
    /**
     * 打印带颜色的文本并换行
     */
    private void printlnColored(String color, String text) {
        System.out.println(color + text + RESET);
    }
    
    /**
     * 计算文本在终端的显示宽度（中文等全角字符按2）
     */
    private int getDisplayWidth(String text) {
        int width = 0;
        for (char ch : text.toCharArray()) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
            boolean isWide = Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)
                    || Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)
                    || Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B.equals(block)
                    || Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block)
                    || Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION.equals(block)
                    || Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS.equals(block)
                    || Character.UnicodeBlock.HANGUL_SYLLABLES.equals(block)
                    || Character.UnicodeBlock.KATAKANA.equals(block)
                    || Character.UnicodeBlock.HIRAGANA.equals(block);
            width += isWide ? 2 : 1;
        }
        return width;
    }

    /**
     * 打印标题框
     */
    private void printTitle(String title) {
        int titleWidth = getDisplayWidth(title);
        int width = Math.max(titleWidth + 10, 60);
        String border = LINE.repeat(width);
        
        // 使用显示宽度计算填充，避免全角字符导致歪斜
        int spaceTotal = width - titleWidth;
        int leftPadding = spaceTotal / 2;
        int rightPadding = spaceTotal - leftPadding;
        
        // 构建标题行，确保垂直符号对齐
        String titleLine = VERTICAL + " ".repeat(leftPadding) + title + " ".repeat(rightPadding) + VERTICAL;
        
        printColored(CYAN, CORNER_TL + border + CORNER_TR);
        System.out.println();
        printColored(CYAN + YELLOW + BOLD, titleLine);
        System.out.println();
        printColored(CYAN, CORNER_BL + border + CORNER_BR);
        System.out.println();
    }
    
    /**
     * 打印分隔线
     */
    private void printSeparator(char character, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(character);
        }
        printlnColored(CYAN, sb.toString());
    }
    
    /**
     * 打印菜单项
     */
    private void printMenuItem(int number, String description) {
        printColored(GREEN, "  " + number + ". ");
        printlnColored(WHITE, description);
    }
    
    /**
     * 打印成功消息
     */
    private void printSuccess(String message) {
        printColored(GREEN, "✓ ");
        printlnColored(GREEN + BOLD, message);
    }
    
    /**
     * 打印错误消息
     */
    private void printError(String message) {
        printColored(RED, "✗ ");
        printlnColored(RED + BOLD, message);
    }
    
    /**
     * 打印警告消息
     */
    private void printWarning(String message) {
        printColored(YELLOW, "⚠ ");
        printlnColored(YELLOW + BOLD, message);
    }
    
    /**
     * 打印信息消息
     */
    private void printInfo(String message) {
        printColored(BLUE, "ℹ ");
        printlnColored(BLUE, message);
    }
    
    /**
     * 等待用户按回车继续
     */
    private void pressEnterToContinue() {
        printColored(YELLOW, "\n按回车键继续...");
        readLine();
    }

    public void start() {
        try {
            // 系统启动时检查并处理过期订单
            bookingService.checkExpiredOrders();
            
            while (true) {
                clearScreen();
                printTitle("欢迎使用电影院购票系统");
                
                login();
                
                // 如果用户选择了退出系统，login()会返回null
                if (currentUser == null) {
                    // 检查是否是主动退出系统
                    break;
                }
                
                newMethods = new NewMethods(cinemaManager, bookingService, scanner, currentUser);
                if (currentUser.isAdmin()) {
                    showAdminMenu();
                } else {
                    showCustomerMenu();
                }
                
                // 退出菜单后，重置用户，准备重新登录
                currentUser = null;
            }
        } finally {
            // 确保在程序退出前保存关键数据
            try {
                cinemaManager.saveMovies();
                cinemaManager.saveUsers();
                clearScreen();
                printTitle("感谢使用电影院购票系统");
                printSuccess("关键数据已保存");
                printColored(YELLOW, "再见！\n");
            } catch (Exception e) {
                System.err.println("保存数据时出错: " + e.getMessage());
            }
        }
    }

    private void login() {
        while (true) {
            clearScreen();
            printTitle("电影院购票系统");
            printlnColored(CYAN, "\n请选择登录方式：\n");
            
            printMenuItem(1, "普通用户登录");
            printMenuItem(2, "管理员登录");
            printMenuItem(3, "用户注册");
            printMenuItem(0, "退出系统");
            
            printColored(YELLOW, "\n请选择操作 (0-3): ");
            String choice = readLine();
            
            switch (choice) {
                case "1":
                    if (performLogin(false)) {
                        printSuccess("登录成功！");
                        return;
                    }
                    break;
                case "2":
                    if (performLogin(true)) {
                        printSuccess("管理员登录成功！");
                        return;
                    }
                    break;
                case "3":
                    performRegister();
                    break;
                case "0":
                    System.out.println("感谢使用电影院购票系统！");
                    System.exit(0); // 直接退出程序
                default:
                    printError("无效选择，请输入0-3之间的数字");
                    pressEnterToContinue();
            }
        }
    }
    
    private boolean performLogin(boolean isAdminLogin) {
        clearScreen();
        if (isAdminLogin) {
            printTitle("管理员登录");
        } else {
            printTitle("用户登录");
        }
        
        printColored(CYAN, "请输入用户ID: ");
        String userId = readLine();
        
        if (userId.isEmpty()) {
            printError("用户ID不能为空");
            pressEnterToContinue();
            return false;
        }
        
        User user = cinemaManager.getUser(userId);
        if (user != null) {
            // 验证角色是否匹配
            if (isAdminLogin && !user.isAdmin()) {
                printError("该用户不是管理员，无法使用管理员登录");
                pressEnterToContinue();
                return false;
            }
            
            if (!isAdminLogin && user.isAdmin()) {
                printError("管理员用户请使用管理员登录入口");
                pressEnterToContinue();
                return false;
            }
            
            currentUser = user;
            printSuccess("登录成功！欢迎，" + currentUser.getName());
            if (currentUser.isAdmin()) {
                printInfo("当前角色: 管理员");
            } else {
                printInfo("当前角色: 普通用户");
            }
            pressEnterToContinue();
            return true;
        } else {
            printError("用户不存在，请先注册");
            pressEnterToContinue();
            return false;
        }
    }

    private void performRegister() {
        System.out.println("\n----- 用户注册 -----");

        while (true) {
            System.out.print("请输入用户ID (字母数字组合，3-20位): ");
            String userId = readLine();

            if (userId.isEmpty()) {
                System.out.println("用户ID不能为空");
                continue;
            }

            if (userId.length() < 3 || userId.length() > 20) {
                System.out.println("用户ID长度必须在3-20位之间");
                continue;
            }

            if (!userId.matches("^[a-zA-Z0-9_]+$")) {
                System.out.println("用户ID只能包含字母、数字和下划线");
                continue;
            }

            if (cinemaManager.getUser(userId) != null) {
                System.out.println("用户ID已存在，请选择其他ID");
                continue;
            }

            System.out.print("请输入姓名: ");
            String name = readLine();
            if (name.isEmpty()) {
                System.out.println("姓名不能为空");
                continue;
            }

            // ================= [新增开始] =================
            System.out.print("请输入密码 (至少6位): ");
            String password = readLine();
            if (password.length() < 6) {
                System.out.println("密码长度不能少于6位");
                continue;
            }
            // ================= [新增结束] =================

            System.out.print("请输入电话: ");
            String phone = readLine();
            if (phone.isEmpty()) {
                System.out.println("电话不能为空");
                continue;
            }

            if (!phone.matches("^1[3-9]\\d{9}$")) {
                System.out.println("请输入有效的手机号码");
                continue;
            }

            System.out.print("请输入邮箱: ");
            String email = readLine();
            if (email.isEmpty()) {
                System.out.println("邮箱不能为空");
                continue;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                System.out.println("请输入有效的邮箱地址");
                continue;
            }

            System.out.print("注册为管理员用户？(Y/N): ");
            String adminChoice = readLine();
            boolean isAdmin = adminChoice.equalsIgnoreCase("Y");

            User.UserRole role = isAdmin ? User.UserRole.ADMIN : User.UserRole.CUSTOMER;

            // ================= [修改这里] =================
            // 原来的代码：User newUser = new User(userId, name, phone, email, role);
            // 修改后的代码（加入了 password）：
            User newUser = new User(userId, name, password, phone, email, role);
            // ============================================

            cinemaManager.addUser(newUser);

            System.out.println("用户注册成功！");
            System.out.println("用户ID: " + userId);
            System.out.println("姓名: " + name);
            System.out.println("角色: " + (isAdmin ? "管理员" : "普通用户"));
            System.out.println("现在可以使用该用户ID登录系统");

            return;
        }
    }

    private void showCustomerMenu() {
        while (true) {
            clearScreen();
            printTitle("用户菜单");
            printlnColored(CYAN, "\n当前用户: " + currentUser.getName() + " (" + 
                (currentUser.isAdmin() ? "管理员" : "普通用户") + ")");
            printlnColored(CYAN, "\n请选择操作：\n");
            
            printMenuItem(1, "浏览电影");
            printMenuItem(2, "查询场次");
            printMenuItem(3, "购买电影票");
            printMenuItem(4, "查看我的订单");
            printMenuItem(5, "退票");
            printMenuItem(6, "修改个人信息");
            
            if (currentUser.isAdmin()) {
                printMenuItem(7, "进入管理员菜单");
            }
            
            printMenuItem(0, "退出登录");
            
            printColored(YELLOW, "\n请选择操作: ");
            String choice = readLine();
            
            switch (choice) {
                case "1":
                    browseMovies();
                    break;
                case "2":
                    searchShows();
                    break;
                case "3":
                    purchaseTicket();
                    break;
                case "4":
                    viewMyOrders();
                    break;
                case "5":
                    refundTicket();
                    break;
                case "6":
                    newMethods.editProfile();
                    break;
                case "7":
                    if (currentUser.isAdmin()) {
                        showAdminMenu();
                    } else {
                        printError("权限不足");
                        pressEnterToContinue();
                    }
                    break;
                case "0":
                    newMethods.logout();
                    return;
                default:
                    printError("无效选择，请重试");
                    pressEnterToContinue();
            }
        }
    }

    private void showAdminMenu() {
        while (true) {
            clearScreen();
            printTitle("管理员菜单");
            printlnColored(CYAN, "\n请选择操作：\n");
            
            printMenuItem(1, "管理电影信息");
            printMenuItem(2, "管理放映厅");
            printMenuItem(3, "管理场次");
            printMenuItem(4, "查看统计信息");
            printMenuItem(5, "管理用户");
            printMenuItem(6, "切换定价策略");
            printMenuItem(7, "浏览电影");
            printMenuItem(8, "查询场次");
            printMenuItem(9, "数据备份");
            printMenuItem(0, "退出登录");
            
            printColored(YELLOW, "\n请选择操作 (0-9): ");
            String choice = readLine();
            
            switch (choice) {
                case "1":
                    manageMovies();
                    break;
                case "2":
                    manageScreeningRooms();
                    break;
                case "3":
                    manageShows();
                    break;
                case "4":
                    viewStatistics();
                    break;
                case "5":
                    newMethods.manageUsers();
                    break;
                case "6":
                    switchPricingStrategy();
                    break;
                case "7":
                    browseMovies();
                    break;
                case "8":
                    searchShows();
                    break;
                case "9":
                    newMethods.backupData();
                    break;
                case "0":
                    return;
                default:
                    printError("无效选择，请输入0-9之间的数字");
                    pressEnterToContinue();
            }
        }
    }

    private void browseMovies() {
        clearScreen();
        printTitle("电影列表");
        
        List<Movie> movies = cinemaManager.getAllMovies();
        
        if (movies.isEmpty()) {
            printWarning("暂无电影信息");
            pressEnterToContinue();
            return;
        }
        
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            
            printSeparator('═', 60);
            printColored(GREEN + BOLD, String.format("%d. %s\n", i + 1, movie.getTitle()));
            
            printColored(CYAN, "   电影ID: ");
            printlnColored(WHITE, movie.getId());
            
            printColored(CYAN, "   导演: ");
            printlnColored(WHITE, movie.getDirector());
            
            printColored(CYAN, "   主演: ");
            printlnColored(WHITE, String.join(", ", movie.getActors()));
            
            printColored(CYAN, "   时长: ");
            printlnColored(WHITE, movie.getDuration() + "分钟");
            
            printColored(CYAN, "   评分: ");
            if (movie.getRating() >= 8.0) {
                printColored(GREEN + BOLD, String.format("%.1f", movie.getRating()));
            } else if (movie.getRating() >= 6.0) {
                printColored(YELLOW, String.format("%.1f", movie.getRating()));
            } else {
                printColored(RED, String.format("%.1f", movie.getRating()));
            }
            System.out.println();
            
            printColored(CYAN, "   类型: ");
            printlnColored(WHITE, movie.getGenre().getDescription());
            
            printColored(CYAN, "   上映日期: ");
            printlnColored(WHITE, movie.getReleaseTime().toString());
            
            printColored(CYAN, "   简介: ");
            printlnColored(WHITE, movie.getDescription());
            System.out.println();
        }
        
        printSeparator('═', 60);
        pressEnterToContinue();
    }

    private void searchShows() {
        clearScreen();
        printTitle("查询场次");
        
        printColored(CYAN, "请输入电影名称 (直接回车显示所有): ");
        String movieTitle = readLine();
        
        printColored(CYAN, "请输入日期 (YYYY-MM-DD，直接回车显示所有): ");
        String dateStr = readLine();
        
        LocalDate date = null;
        if (!dateStr.isEmpty()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                printWarning("日期格式错误，显示所有场次");
            }
        }
        
        List<Show> shows = cinemaManager.searchShows(movieTitle, date);
        
        if (shows.isEmpty()) {
            printWarning("未找到符合条件的场次");
            pressEnterToContinue();
            return;
        }
        
        printSeparator('═', 70);
        printColored(GREEN + BOLD, "找到 " + shows.size() + " 个场次:\n");
        
        for (int i = 0; i < shows.size(); i++) {
            Show show = shows.get(i);
            
            printSeparator('-', 50);
            printColored(GREEN + BOLD, String.format("场次 %d\n", i + 1));
            
            printColored(CYAN, "电影名称: ");
            printlnColored(WHITE, show.getMovieTitle());
            
            printColored(CYAN, "场次ID: ");
            printlnColored(WHITE, show.getId());
            
            printColored(CYAN, "放映厅: ");
            printlnColored(WHITE, show.getScreeningRoomName());
            
            printColored(CYAN, "开始时间: ");
            printlnColored(WHITE, show.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            
            printColored(CYAN, "基础票价: ");
            printColored(YELLOW + BOLD, "￥" + show.getBasePrice());
            System.out.println();
            
            printColored(CYAN, "座位情况: ");
            int available = show.getAvailableSeatsCount();
            int total = show.getTotalSeats();
            if (available == 0) {
                printColored(RED, "已满座");
            } else if (available < total * 0.2) {
                printColored(YELLOW, available + "/" + total + " (座位紧张)");
            } else {
                printColored(GREEN, available + "/" + total + " (余票充足)");
            }
            System.out.println("\n");
        }
        
        printSeparator('═', 70);
        pressEnterToContinue();
    }

    private void purchaseTicket() {
        clearScreen();
        printTitle("购买电影票");
        
        // 获取所有场次并显示
        List<Show> shows = cinemaManager.getAllShows();
        
        if (shows.isEmpty()) {
            printWarning("暂无场次信息");
            pressEnterToContinue();
            return;
        }
        
        // 显示场次列表
        printSeparator('═', 80);
        printColored(GREEN + BOLD, "可用场次列表：\n");
        
        java.util.Map<String, Show> showMap = new java.util.HashMap<>();
        int index = 1;
        
        for (Show show : shows) {
            // 只显示未来的场次
            if (show.getStartTime().isAfter(java.time.LocalDateTime.now())) {
                printColored(CYAN, String.format("%d. ", index++));
                printColored(WHITE, show.getMovieTitle());
                printColored(CYAN, " - ");
                printColored(YELLOW, show.getScreeningRoomName());
                printColored(CYAN, " (");
                printColored(WHITE, show.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                printColored(CYAN, ") | ");
                
                int available = show.getAvailableSeatsCount();
                int total = show.getTotalSeats();
                if (available == 0) {
                    printColored(RED, "已满座");
                } else if (available < total * 0.2) {
                    printColored(YELLOW, "可用座位: " + available + " (座位紧张)");
                } else {
                    printColored(GREEN, "可用座位: " + available);
                }
                System.out.println();
                
                showMap.put(String.valueOf(index - 1), show);
            }
        }
        
        if (showMap.isEmpty()) {
            printWarning("暂无可用场次");
            pressEnterToContinue();
            return;
        }
        
        printSeparator('═', 80);
        
        printColored(YELLOW, "\n请选择场次 (1-" + (index - 1) + "): ");
        String choice = readLine();
        
        Show show = showMap.get(choice);
        if (show == null) {
            printError("无效选择");
            pressEnterToContinue();
            return;
        }
        
        // 显示座位图
        displaySeatMap(show);
        
        System.out.print("请输入要购买的座位ID (多个座位用逗号分隔，格式: 行-列，如: 3-5,3-6): ");
        String seatIdsStr = readLine();
        
        if (seatIdsStr.isEmpty()) {
            System.out.println("未选择座位");
            return;
        }
        
        String[] seatIds = seatIdsStr.split(",");
        List<String> seatIdList = new java.util.ArrayList<>();
        for (String seatId : seatIds) {
            seatIdList.add(seatId.trim());
        }
        
        // 先检查过期订单
        bookingService.checkExpiredOrders();
        
        try {
            // 显示选项
            clearScreen();
            printTitle("确认订单");
            printlnColored(CYAN, "\n请选择操作：\n");
            
            printMenuItem(1, "立即支付");
            printMenuItem(2, "预定座位（15分钟内支付）");
            printMenuItem(0, "取消");
            
            printColored(YELLOW, "\n请选择操作: ");
            String actionChoice = readLine();
            
            Order order;
            
            switch (actionChoice) {
                case "1": // 立即支付
                    order = bookingService.createOrder(currentUser, show, seatIdList);
                    
                    System.out.println("\n----- 订单信息 -----");
                    printlnColored(CYAN, "订单ID: ");
                    printlnColored(WHITE, order.getOrderId());
                    printlnColored(CYAN, "电影: ");
                    printlnColored(WHITE, show.getMovieTitle());
                    printlnColored(CYAN, "场次时间: ");
                    printlnColored(WHITE, show.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    printlnColored(CYAN, "座位: ");
                    printlnColored(WHITE, order.getSeatIds());
                    printlnColored(CYAN, "总金额: ");
                    printColored(YELLOW + BOLD, "￥" + order.getTotalAmount());
                    System.out.println();
                    
                    printColored(YELLOW, "确认支付？(Y/N): ");
                    String confirm = readLine();
                    
                    if (confirm.equalsIgnoreCase("Y")) {
                        try {
                            bookingService.processPayment(order);
                            printSuccess("支付成功！订单已完成。");
                        } catch (PaymentFailedException e) {
                            printError("支付失败: " + e.getMessage());
                            printWarning("订单已取消，座位已释放");
                        }
                    } else {
                        try {
                            bookingService.cancelOrder(order);
                            printInfo("订单已取消");
                        } catch (InvalidBookingException e) {
                            printError("取消订单失败: " + e.getMessage());
                        }
                    }
                    break;
                    
                case "2": // 预定座位
                    order = bookingService.reserveOrder(currentUser, show, seatIdList);
                    
                    System.out.println("\n----- 预订成功 -----");
                    printlnColored(CYAN, "订单ID: ");
                    printlnColored(WHITE, order.getOrderId());
                    printlnColored(CYAN, "电影: ");
                    printlnColored(WHITE, show.getMovieTitle());
                    printlnColored(CYAN, "场次时间: ");
                    printlnColored(WHITE, show.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    printlnColored(CYAN, "座位: ");
                    printlnColored(WHITE, order.getSeatIds());
                    printlnColored(CYAN, "总金额: ");
                    printColored(YELLOW + BOLD, "￥" + order.getTotalAmount());
                    System.out.println();
                    
                    printWarning("座位已锁定，请在15分钟内完成支付！");
                    printInfo("锁定时间剩余: " + order.getRemainingLockMinutes() + " 分钟");
                    
                    printColored(YELLOW, "\n是否立即支付？(Y/N): ");
                    String payConfirm = readLine();
                    
                    if (payConfirm.equalsIgnoreCase("Y")) {
                        try {
                            bookingService.processReservedOrderPayment(order);
                            printSuccess("支付成功！订单已完成。");
                        } catch (PaymentFailedException e) {
                            printError("支付失败: " + e.getMessage());
                            printWarning("预订仍然有效，您可以在15分钟内再次支付");
                        } catch (InvalidBookingException e) {
                            printError("支付失败: " + e.getMessage());
                        }
                    } else {
                        printInfo("预订已创建，您可以在15分钟内通过'查看我的订单'完成支付");
                    }
                    break;
                    
                case "0": // 取消
                    printInfo("已取消购买");
                    return;
                    
                default:
                    printError("无效选择");
                    return;
            }

            
        } catch (SeatNotAvailableException e) {
            printError("座位预订失败: " + e.getMessage());
            printWarning("原因: " + e.getReason());
            printInfo("请选择其他座位");
        } catch (InvalidBookingException e) {
            printError("预订失败: " + e.getMessage());
            if (!e.getBookingDetails().isEmpty()) {
                printInfo("详情: " + e.getBookingDetails());
            }
        } catch (Exception e) {
            printError("购票失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        pressEnterToContinue();
    }

    private void displaySeatMap(Show show) {
        printTitle("座位选择 - " + show.getMovieTitle());
        
        // 显示图例
        printlnColored(CYAN, "\n图例：");
        printColored(BLUE, "  [D] 优惠座位(第一排，80%价格)  ");
        printColored(PURPLE, "[V] VIP座位(中间3排，比普通座位贵10元)  ");
        printColored(GREEN, "[O] 普通座位  ");
        printColored(YELLOW, "[L] 已锁定  ");
        printlnColored(RED, "[X] 已售出");
        
        Seat[][] seats = show.getScreeningRoom().getSeatLayout();
        
        // 显示屏幕（放在顶部）
        int screenWidth = seats[0].length * 4 - 1; // 调整宽度以匹配座位显示
        printSeparator('═', screenWidth);
        String screenText = "银幕";
        int screenPadding = (screenWidth - screenText.length()) / 2;
        printlnColored(CYAN + BOLD, " ".repeat(screenPadding) + screenText);
        printSeparator('═', screenWidth);
        
        // 打印列号
        printColored(CYAN, "\n     ");
        for (int col = 1; col <= seats[0].length; col++) {
            printColored(CYAN, String.format("%3d", col)); // 使用3位数字对齐
        }
        System.out.println();
        
        // 打印座位
        for (int row = 0; row < seats.length; row++) {
            printColored(CYAN, String.format("  %2d ", row + 1));
            
            for (int col = 0; col < seats[row].length; col++) {
                Seat showSeat = show.getSeat(row + 1, col + 1);
                if (showSeat.isAvailable()) {
                    if (showSeat instanceof VIPSeat) {
                        printColored(PURPLE + BOLD, "[V] ");
                    } else if (showSeat instanceof DiscountSeat) {
                        printColored(BLUE, "[D] ");
                    } else {
                        printColored(GREEN, "[O] ");
                    }
                } else if (showSeat.isLocked()) {
                    printColored(YELLOW, "[L] ");
                } else {
                    printColored(RED, "[X] ");
                }
            }
            System.out.println();
        }
        
        // 显示价格说明
        printSeparator('-', 60);
        printlnColored(CYAN, "\n座位定价说明：");
        
        // 计算不同类型座位的价格
        double regularPrice = 0;
        double vipPrice = 0;
        double discountPrice = 0;
        
        for (Seat seat : show.getAvailableSeats()) {
            double price = bookingService.calculateSeatPrice(show, seat);
            if (seat instanceof VIPSeat && vipPrice == 0) {
                vipPrice = price;
            } else if (seat instanceof DiscountSeat && discountPrice == 0) {
                discountPrice = price;
            } else if (!(seat instanceof VIPSeat) && !(seat instanceof DiscountSeat) && regularPrice == 0) {
                regularPrice = price;
            }
            
            if (regularPrice > 0 && vipPrice > 0 && discountPrice > 0) {
                break;
            }
        }
        
        printColored(BLUE, "  [D] 优惠座位: ");
        printlnColored(YELLOW + BOLD, String.format("￥%.2f", discountPrice));
        
        printColored(PURPLE + BOLD, "  [V] VIP座位: ");
        printlnColored(YELLOW + BOLD, String.format("￥%.2f", vipPrice));
        
        printColored(GREEN, "  [O] 普通座位: ");
        printlnColored(YELLOW + BOLD, String.format("￥%.2f", regularPrice));
        
        printSeparator('-', 60);
        printInfo("请输入座位位置（格式：行-列，例如：1-1），多个座位用逗号分隔");
    }

    private void viewMyOrders() {
        clearScreen();
        printTitle("我的订单");

        // 先检查过期订单
        bookingService.checkExpiredOrders();
        
        // 获取用户的最新订单
        List<Order> orders = bookingService.getOrdersByUser(currentUser);
        
        if (orders.isEmpty()) {
            printWarning("暂无订单");
            pressEnterToContinue();
            return;
        }
        
        // 使用Set去重，避免重复显示
        java.util.Set<String> displayedOrderIds = new java.util.HashSet<>();
        int displayIndex = 1;
        java.util.Map<String, Order> reservableOrders = new java.util.HashMap<>();
        
        for (Order order : orders) {
            // 避免重复显示相同订单
            if (!displayedOrderIds.contains(order.getOrderId())) {
                displayedOrderIds.add(order.getOrderId());
                
                printSeparator('═', 80);
                printColored(GREEN + BOLD, String.format("订单 #%d\n", displayIndex++));
                
                printColored(CYAN, "订单编号: ");
                printlnColored(WHITE, order.getOrderId());
                
                printColored(CYAN, "电影名称: ");
                printlnColored(WHITE, order.getShow().getMovie().getTitle());
                
                printColored(CYAN, "放映时间: ");
                printlnColored(WHITE, order.getShow().getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                
                printColored(CYAN, "座位信息: ");
                StringBuilder seatInfo = new StringBuilder();
                for (Seat seat : order.getSeats()) {
                    if (seatInfo.length() > 0) seatInfo.append(", ");
                    seatInfo.append(seat.getSeatId());
                    if (seat instanceof VIPSeat) {
                        seatInfo.append("(VIP)");
                    }
                }
                printlnColored(WHITE, seatInfo.toString());
                
                printColored(CYAN, "创建时间: ");
                printlnColored(WHITE, order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                printColored(CYAN, "订单状态: ");
                switch (order.getStatus()) {
                    case PENDING:
                        printColored(YELLOW + BOLD, "待支付");
                        break;
                    case RESERVED:
                        long remainingMinutes = order.getRemainingLockMinutes();
                        printColored(ORANGE + BOLD, "已预订");
                        if (remainingMinutes > 0) {
                            printColored(ORANGE, " (剩余" + remainingMinutes + "分钟)");
                        } else {
                            printColored(RED, " (已过期)");
                        }
                        reservableOrders.put(String.valueOf(displayIndex - 1), order);
                        break;
                    case PAID:
                        printColored(GREEN + BOLD, "已支付");
                        break;
                    case CANCELLED:
                        printColored(RED, "已取消");
                        break;
                    case REFUNDED:
                        printColored(PURPLE, "已退款");
                        break;
                    case EXPIRED:
                        printColored(RED + BOLD, "已过期");
                        break;
                }
                System.out.println();
                
                printColored(CYAN, "订单金额: ");
                printColored(YELLOW + BOLD, String.format("￥%.2f", order.getTotalAmount()));
                System.out.println("\n");
            }
        }
        
        printSeparator('═', 80);
        
        // 如果有可支付的预订订单，提供支付选项
        if (!reservableOrders.isEmpty()) {
            printlnColored(CYAN, "\n可支付的预订订单：");
            for (String index : reservableOrders.keySet()) {
                Order order = reservableOrders.get(index);
                printColored(GREEN, index + ". ");
                printlnColored(WHITE, order.getOrderId() + " - " + order.getShow().getMovie().getTitle());
            }
            
            printColored(YELLOW, "\n输入订单编号或序号进行支付（如：1 或 ORD-001），或按回车键返回: ");
            String choice = readLine();
            
            Order selectedOrder = null;
            if (!choice.isEmpty()) {
                // 先尝试按序号查找
                if (reservableOrders.containsKey(choice)) {
                    selectedOrder = reservableOrders.get(choice);
                } else {
                    // 再尝试按订单号查找
                    for (Order order : reservableOrders.values()) {
                        if (order.getOrderId().equalsIgnoreCase(choice)) {
                            selectedOrder = order;
                            break;
                        }
                    }
                }
                
                if (selectedOrder != null) {
                    printSeparator('-', 60);
                    printlnColored(CYAN, "订单详情：");
                    printlnColored(CYAN, "订单号: " + selectedOrder.getOrderId());
                    printlnColored(CYAN, "电影: " + selectedOrder.getShow().getMovie().getTitle());
                    printlnColored(CYAN, "座位: " + selectedOrder.getSeatIds());
                    printlnColored(CYAN, "金额: ");
                    printColored(YELLOW + BOLD, "￥" + selectedOrder.getTotalAmount());
                    printlnColored(ORANGE, "剩余锁定时间: " + selectedOrder.getRemainingLockMinutes() + " 分钟");
                    
                    printColored(YELLOW, "\n确认支付？(Y/N): ");
                    String confirm = readLine();
                    
                    if (confirm.equalsIgnoreCase("Y")) {
                        try {
                            bookingService.processReservedOrderPayment(selectedOrder);
                            printSuccess("支付成功！订单已完成。");
                        } catch (PaymentFailedException e) {
                            printError("支付失败: " + e.getMessage());
                        } catch (InvalidBookingException e) {
                            printError("支付失败: " + e.getMessage());
                        }
                    } else {
                        printInfo("支付已取消");
                    }
                }
            }
        }
        
        pressEnterToContinue();
    }

    private void refundTicket() {
        clearScreen();
        printTitle("退票");
        
        List<Order> userOrders = currentUser.getOrders();
        if (userOrders.isEmpty()) {
            printWarning("您没有可退票的订单");
            pressEnterToContinue();
            return;
        }
        
        // 显示可退票的订单
        printlnColored(CYAN, "\n您的订单列表：");
        printSeparator('═', 70);
        
        java.util.Set<String> displayedOrderIds = new java.util.HashSet<>();
        int displayIndex = 1;
        java.util.Map<String, Order> orderMap = new java.util.HashMap<>();
        
        for (Order order : userOrders) {
            if (!displayedOrderIds.contains(order.getOrderId()) && 
                (order.getStatus() == Order.OrderStatus.PAID || order.getStatus() == Order.OrderStatus.PENDING)) {
                displayedOrderIds.add(order.getOrderId());
                
                printColored(GREEN, String.format("%d. ", displayIndex));
                printColored(WHITE, order.getOrderId());
                printColored(CYAN, " - ");
                printlnColored(WHITE, order.getShow().getMovie().getTitle());
                
                printColored(CYAN, "   座位: ");
                StringBuilder seatInfo = new StringBuilder();
                for (Seat seat : order.getSeats()) {
                    if (seatInfo.length() > 0) seatInfo.append(", ");
                    seatInfo.append(seat.getSeatId());
                }
                printlnColored(WHITE, seatInfo.toString());
                
                printColored(CYAN, "   状态: ");
                switch (order.getStatus()) {
                    case PENDING:
                        printColored(YELLOW + BOLD, "待支付");
                        break;
                    case PAID:
                        printColored(GREEN + BOLD, "已支付");
                        break;
                }
                System.out.println();
                
                printColored(CYAN, "   金额: ");
                printColored(YELLOW + BOLD, String.format("￥%.2f", order.getTotalAmount()));
                System.out.println("\n");
                
                orderMap.put(String.valueOf(displayIndex), order);
                displayIndex++;
            }
        }
        
        printSeparator('═', 70);
        printColored(YELLOW, "请输入要退票的订单编号 (1-" + (displayIndex-1) + ") 或输入0返回: ");
        String choice = readLine();
        
        if ("0".equals(choice)) {
            return;
        }
        
        Order selectedOrder = orderMap.get(choice);
        if (selectedOrder == null) {
            printError("无效的订单编号");
            pressEnterToContinue();
            return;
        }
        
        // 确认退票
        printSeparator('-', 50);
        printColored(CYAN, "订单详情：\n");
        printColored(CYAN, "订单号: ");
        printlnColored(WHITE, selectedOrder.getOrderId());
        printColored(CYAN, "电影: ");
        printlnColored(WHITE, selectedOrder.getShow().getMovie().getTitle());
        printColored(CYAN, "座位: ");
        StringBuilder seatInfoDetail = new StringBuilder();
        for (Seat seat : selectedOrder.getSeats()) {
            if (seatInfoDetail.length() > 0) seatInfoDetail.append(", ");
            seatInfoDetail.append(seat.getSeatId());
        }
        printlnColored(WHITE, seatInfoDetail.toString());
        printColored(CYAN, "金额: ");
        printlnColored(YELLOW + BOLD, String.format("￥%.2f", selectedOrder.getTotalAmount()));
        
        printColored(YELLOW, "\n确认退票？(Y/N): ");
        String confirm = readLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            try {
                bookingService.cancelOrder(selectedOrder);
                printSuccess("退票成功！座位已释放，退款将在3-5个工作日内到账");
                pressEnterToContinue();
            } catch (InvalidBookingException e) {
                printError("退票失败: " + e.getMessage());
                pressEnterToContinue();
            }
        } else {
            printInfo("取消退票");
            pressEnterToContinue();
        }
    }

    private void switchPricingStrategy() {
        clearScreen();
        printTitle("定价策略管理");
        printlnColored(CYAN, "\n当前定价策略: " + 
            (bookingService.getPricingStrategy().getClass().getSimpleName().equals("StandardPricing") ? "标准定价" : "高级定价"));
        printlnColored(CYAN, "\n请选择新的定价策略：\n");
        
        printMenuItem(1, "标准定价 - 基础票价，无额外费用");
        printMenuItem(2, "高级定价 - VIP座位加价，时段差异化定价");
        printMenuItem(0, "返回");
        
        printColored(YELLOW, "\n请选择操作: ");
        String choice = readLine();
        
        switch (choice) {
            case "1":
                bookingService.setPricingStrategy(new com.cinema.strategy.StandardPricing());
                printSuccess("已切换到标准定价策略");
                printlnColored(CYAN, "所有座位将使用基础票价");
                break;
            case "2":
                bookingService.setPricingStrategy(new com.cinema.strategy.PremiumPricing());
                printSuccess("已切换到高级定价策略");
                printlnColored(CYAN, "VIP座位将加价20%，高峰时段票价上浮10%");
                break;
            case "0":
                return;
            default:
                printError("无效选择");
        }
        
        pressEnterToContinue();
    }

    private void manageMovies() {
        System.out.println("\n----- 管理电影信息 -----");
        System.out.println("1. 添加电影");
        System.out.println("2. 删除电影");
        System.out.println("3. 查看所有电影");
        System.out.print("请选择操作: ");
        
        String choice = readLine();
        
        switch (choice) {
            case "1":
                addMovie();
                break;
            case "2":
                removeMovie();
                break;
            case "3":
                browseMovies();
                break;
            default:
                System.out.println("无效选择");
        }
    }

    private void addMovie() {
        System.out.println("\n----- 添加电影 -----");
        System.out.print("请输入电影ID: ");
        String id = readLine();
        
        if (cinemaManager.getMovie(id) != null) {
            System.out.println("电影ID已存在");
            return;
        }
        
        System.out.print("请输入电影名称: ");
        String title = readLine();
        
        System.out.print("请输入导演: ");
        String director = readLine();
        
        System.out.print("请输入主演 (用逗号分隔): ");
        String actorsStr = readLine();
        List<String> actors = List.of(actorsStr.split(","));
        
        // 输入时长（带验证）
        int duration = 0;
        while (true) {
            try {
                System.out.print("请输入时长 (分钟): ");
                duration = Integer.parseInt(readLine());
                if (duration <= 0) {
                    System.out.println("时长必须大于0");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("请输入有效的数字");
            }
        }
        
        // 输入评分（带验证）
        double rating = 0;
        while (true) {
            try {
                System.out.print("请输入评分 (0-10): ");
                rating = Double.parseDouble(readLine());
                if (rating < 0 || rating > 10) {
                    System.out.println("评分必须在0-10之间");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("请输入有效的数字");
            }
        }
        
        System.out.print("请输入类型: ");
        String genre = readLine();
        
        System.out.print("请输入简介: ");
        String description = readLine();
        
        // 输入上映日期（带验证）
        LocalDate releaseTime = null;
        while (true) {
            try {
                System.out.print("请输入上映日期 (YYYY-MM-DD): ");
                String dateStr = readLine();
                releaseTime = LocalDate.parse(dateStr);
                // 检查日期是否是未来的日期
                if (releaseTime.isAfter(LocalDate.now().plusYears(1))) {
                    System.out.println("上映日期不能超过一年后");
                    continue;
                }
                break;
            } catch (Exception e) {
                System.out.println("日期格式错误，请使用YYYY-MM-DD格式，例如：2023-12-07");
            }
        }
        
        Movie movie = new Movie(id, title, releaseTime, actors, director, duration, rating, description, genre);
        cinemaManager.addMovie(movie);
        
        System.out.println("电影添加成功");
    }

    private void removeMovie() {
        System.out.println("\n----- 删除电影 -----");
        browseMovies();
        
        System.out.print("请输入要删除的电影ID: ");
        String movieId = readLine();
        
        if (cinemaManager.getMovie(movieId) == null) {
            System.out.println("电影不存在");
            return;
        }
        
        System.out.print("确认删除电影？(Y/N): ");
        String confirm = readLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            cinemaManager.removeMovie(movieId);
            System.out.println("电影删除成功");
        } else {
            System.out.println("取消删除");
        }
    }

    private void manageScreeningRooms() {
        System.out.println("\n----- 管理放映厅 -----");
        System.out.println("1. 添加放映厅");
        System.out.println("2. 删除放映厅");
        System.out.println("3. 查看所有放映厅");
        System.out.print("请选择操作: ");
        
        String choice = readLine();
        
        switch (choice) {
            case "1":
                addScreeningRoom();
                break;
            case "2":
                removeScreeningRoom();
                break;
            case "3":
                viewScreeningRooms();
                break;
            default:
                System.out.println("无效选择");
        }
    }

    private void addScreeningRoom() {
        System.out.println("\n----- 添加放映厅 -----");
        System.out.print("请输入放映厅ID: ");
        String id = readLine();
        
        if (cinemaManager.getScreeningRoom(id) != null) {
            System.out.println("放映厅ID已存在");
            return;
        }
        
        System.out.print("请输入放映厅名称: ");
        String name = readLine();
        
        System.out.print("请输入行数: ");
        int rows = Integer.parseInt(readLine());
        
        System.out.print("请输入列数: ");
        int cols = Integer.parseInt(readLine());
        
        ScreeningRoom room = new ScreeningRoom(id, name, rows, cols);
        cinemaManager.addScreeningRoom(room);
        
        System.out.println("放映厅添加成功");
    }

    private void removeScreeningRoom() {
        System.out.println("\n----- 删除放映厅 -----");
        viewScreeningRooms();
        
        System.out.print("请输入要删除的放映厅ID: ");
        String roomId = readLine();
        
        if (cinemaManager.getScreeningRoom(roomId) == null) {
            System.out.println("放映厅不存在");
            return;
        }
        
        System.out.print("确认删除放映厅？(Y/N): ");
        String confirm = readLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            cinemaManager.removeScreeningRoom(roomId);
            System.out.println("放映厅删除成功");
        } else {
            System.out.println("取消删除");
        }
    }

    private void viewScreeningRooms() {
        clearScreen();
        printTitle("放映厅列表");
        
        List<ScreeningRoom> rooms = cinemaManager.getAllScreeningRooms();
        
        if (rooms.isEmpty()) {
            printWarning("暂无放映厅信息");
            pressEnterToContinue();
            return;
        }
        
        for (int i = 0; i < rooms.size(); i++) {
            ScreeningRoom room = rooms.get(i);
            
            printSeparator('═', 60);
            printColored(GREEN + BOLD, String.format("%d. %s\n", i + 1, room.getName()));
            
            printColored(CYAN, "   放映厅ID: ");
            printlnColored(WHITE, room.getId());
            
            printColored(CYAN, "   座位布局: ");
            printlnColored(WHITE, room.getRows() + " 行 × " + room.getColumns() + " 列");
            
            printColored(CYAN, "   总座位数: ");
            printlnColored(WHITE, String.valueOf(room.getTotalSeats()));
            
            printColored(CYAN, "   VIP座位: ");
            printlnColored(PURPLE, String.valueOf(room.getVipSeatsCount()));
            
            printColored(CYAN, "   普通座位: ");
            printlnColored(WHITE, String.valueOf(room.getRegularSeatsCount()));
            System.out.println();
        }
        
        printSeparator('═', 60);
        pressEnterToContinue();
    }

    private void manageShows() {
        System.out.println("\n----- 管理场次 -----");
        System.out.println("1. 添加场次");
        System.out.println("2. 删除场次");
        System.out.println("3. 查看所有场次");
        System.out.print("请选择操作: ");
        
        String choice = readLine();
        
        switch (choice) {
            case "1":
                addShow();
                break;
            case "2":
                removeShow();
                break;
            case "3":
                searchShows();
                break;
            default:
                System.out.println("无效选择");
        }
    }

    private void addShow() {
        System.out.println("\n----- 添加场次 -----");
        browseMovies();
        
        System.out.print("请输入电影ID: ");
        String movieId = readLine();
        
        Movie movie = cinemaManager.getMovie(movieId);
        if (movie == null) {
            System.out.println("电影不存在");
            return;
        }
        
        viewScreeningRooms();
        
        System.out.print("请输入放映厅ID: ");
        String roomId = readLine();
        
        ScreeningRoom room = cinemaManager.getScreeningRoom(roomId);
        if (room == null) {
            System.out.println("放映厅不存在");
            return;
        }
        
        System.out.print("请输入开始时间 (YYYY-MM-DD HH:MM): ");
        String timeStr = readLine();
        LocalDateTime startTime;
        try {
            startTime = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            System.out.println("时间格式错误，请使用格式：YYYY-MM-DD HH:MM（如：2025-12-11 19:00）");
            return;
        }
        
        System.out.print("请输入基础票价: ");
        double basePrice = Double.parseDouble(readLine());
        
        Show show = new Show("SHOW-" + System.currentTimeMillis(), movie, room, startTime, basePrice);
        cinemaManager.addShow(show);
        
        System.out.println("场次添加成功");
    }

    private void removeShow() {
        System.out.println("\n----- 删除场次 -----");
        searchShows();
        
        System.out.print("请输入要删除的场次ID: ");
        String showId = readLine();
        
        if (cinemaManager.getShow(showId) == null) {
            System.out.println("场次不存在");
            return;
        }
        
        System.out.print("确认删除场次？(Y/N): ");
        String confirm = readLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            cinemaManager.removeShow(showId);
            System.out.println("场次删除成功");
        } else {
            System.out.println("取消删除");
        }
    }

    private void viewStatistics() {
        clearScreen();
        printTitle("统计信息");
        
        List<Movie> movies = cinemaManager.getAllMovies();
        List<ScreeningRoom> rooms = cinemaManager.getAllScreeningRooms();
        List<Show> shows = cinemaManager.getAllShows();
        List<User> users = cinemaManager.getAllUsers();
        List<Order> orders = bookingService.getAllOrders();
        
        printSeparator('═', 60);
        printColored(GREEN + BOLD, "系统资源统计\n");
        
        printColored(CYAN, "电影总数: ");
        printlnColored(WHITE, String.valueOf(movies.size()));
        
        printColored(CYAN, "放映厅总数: ");
        printlnColored(WHITE, String.valueOf(rooms.size()));
        
        printColored(CYAN, "场次总数: ");
        printlnColored(WHITE, String.valueOf(shows.size()));
        
        printColored(CYAN, "注册用户: ");
        printlnColored(WHITE, String.valueOf(users.size()));
        
        printColored(CYAN, "订单总数: ");
        printlnColored(WHITE, String.valueOf(orders.size()));
        
        printSeparator('-', 60);
        printColored(GREEN + BOLD, "业务统计\n");
        
        int paidOrders = 0;
        int pendingOrders = 0;
        int cancelledOrders = 0;
        double totalRevenue = 0.0;
        
        for (Order order : orders) {
            switch (order.getStatus()) {
                case PAID:
                    paidOrders++;
                    totalRevenue += order.getTotalAmount();
                    break;
                case PENDING:
                    pendingOrders++;
                    break;
                case RESERVED:
                    pendingOrders++; // 预订订单也算作待支付
                    break;
                case CANCELLED:
                    cancelledOrders++;
                    break;
                case REFUNDED:
                    cancelledOrders++;
                    break;
            }
        }
        
        printColored(CYAN, "已支付订单: ");
        printlnColored(GREEN, String.valueOf(paidOrders));
        
        printColored(CYAN, "待支付订单: ");
        printlnColored(YELLOW, String.valueOf(pendingOrders));
        
        printColored(CYAN, "已取消订单: ");
        printlnColored(RED, String.valueOf(cancelledOrders));
        
        printSeparator('-', 60);
        printColored(GREEN + BOLD, "收入统计\n");
        
        printColored(CYAN, "总收入: ");
        printColored(YELLOW + BOLD, "￥" + String.format("%.2f", totalRevenue));
        System.out.println();
        
        if (paidOrders > 0) {
            double avgOrderValue = totalRevenue / paidOrders;
            printColored(CYAN, "平均订单金额: ");
            printlnColored(WHITE, "￥" + String.format("%.2f", avgOrderValue));
        }
        
        double orderRate = orders.size() > 0 ? (double) paidOrders / orders.size() * 100 : 0;
        printColored(CYAN, "订单完成率: ");
        if (orderRate >= 70) {
            printColored(GREEN + BOLD, String.format("%.1f%%", orderRate));
        } else if (orderRate >= 50) {
            printColored(YELLOW, String.format("%.1f%%", orderRate));
        } else {
            printColored(RED, String.format("%.1f%%", orderRate));
        }
        System.out.println();
        
        printSeparator('═', 60);
        pressEnterToContinue();
    }
}
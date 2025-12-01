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
     * 打印标题框
     */
    private void printTitle(String title) {
        int width = Math.max(title.length() + 10, 60);
        String border = LINE.repeat(width);
        
        printColored(CYAN, CORNER_TL + border + CORNER_TR + "\n");
        printColored(CYAN, VERTICAL);
        printColored(YELLOW + BOLD, " ".repeat((width - title.length()) / 2) + title);
        printColored(CYAN, VERTICAL + "\n");
        printColored(CYAN, CORNER_BL + border + CORNER_BR + "\n");
    }
    
    /**
     * 打印分隔线
     */
    private void printSeparator(char character, int length) {
        printlnColored(CYAN, String.valueOf(character).repeat(length));
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
        scanner.nextLine();
    }

    public void start() {
        clearScreen();
        printTitle("欢迎使用电影院购票系统");
        
        login();
        
        if (currentUser != null) {
            newMethods = new NewMethods(cinemaManager, bookingService, scanner, currentUser);
            if (currentUser.isAdmin()) {
                showAdminMenu();
            } else {
                showCustomerMenu();
            }
        }
        
        clearScreen();
        printTitle("感谢使用电影院购票系统");
        printColored(YELLOW, "再见！\n");
    }

    private void login() {
        while (true) {
            clearScreen();
            printTitle("用户登录系统");
            printlnColored(CYAN, "\n请选择操作：\n");
            
            printMenuItem(1, "用户登录");
            printMenuItem(2, "用户注册");
            printMenuItem(0, "退出系统");
            
            printColored(YELLOW, "\n请选择操作 (0-2): ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    if (performLogin()) {
                        printSuccess("登录成功！");
                        return;
                    }
                    break;
                case "2":
                    performRegister();
                    break;
                case "0":
                    return;
                default:
                    printError("无效选择，请输入0-2之间的数字");
                    pressEnterToContinue();
            }
        }
    }
    
    private boolean performLogin() {
        clearScreen();
        printTitle("用户登录");
        
        printColored(CYAN, "请输入用户ID: ");
        String userId = scanner.nextLine().trim();
        
        if (userId.isEmpty()) {
            printError("用户ID不能为空");
            pressEnterToContinue();
            return false;
        }
        
        User user = cinemaManager.getUser(userId);
        if (user != null) {
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
            String userId = scanner.nextLine().trim();
            
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
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("姓名不能为空");
                continue;
            }
            
            System.out.print("请输入电话: ");
            String phone = scanner.nextLine().trim();
            if (phone.isEmpty()) {
                System.out.println("电话不能为空");
                continue;
            }
            
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                System.out.println("请输入有效的手机号码");
                continue;
            }
            
            System.out.print("请输入邮箱: ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) {
                System.out.println("邮箱不能为空");
                continue;
            }
            
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                System.out.println("请输入有效的邮箱地址");
                continue;
            }
            
            System.out.print("注册为管理员用户？(Y/N): ");
            String adminChoice = scanner.nextLine().trim();
            boolean isAdmin = adminChoice.equalsIgnoreCase("Y");
            
            User.UserRole role = isAdmin ? User.UserRole.ADMIN : User.UserRole.CUSTOMER;
            User newUser = new User(userId, name, phone, email, role);
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
            System.out.println("\n===== 用户菜单 =====");
            System.out.println("当前用户: " + currentUser.getName() + " (" + 
                (currentUser.isAdmin() ? "管理员" : "普通用户") + ")");
            System.out.println("1. 浏览电影");
            System.out.println("2. 查询场次");
            System.out.println("3. 购买电影票");
            System.out.println("4. 查看我的订单");
            System.out.println("5. 退票");
            System.out.println("6. 修改个人信息");
            System.out.println("7. 切换定价策略");
            
            if (currentUser.isAdmin()) {
                System.out.println("8. 管理员功能");
            }
            
            System.out.println("0. 退出登录");
            System.out.print("请选择操作: ");
            
            String choice = scanner.nextLine().trim();
            
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
                    switchPricingStrategy();
                    break;
                case "8":
                    if (currentUser.isAdmin()) {
                        showAdminMenu();
                    } else {
                        System.out.println("权限不足");
                    }
                    break;
                case "0":
                    newMethods.logout();
                    login();
                    if (currentUser != null) {
                        newMethods = new NewMethods(cinemaManager, bookingService, scanner, currentUser);
                    }
                    return;
                default:
                    System.out.println("无效选择，请重试");
            }
        }
    }

    private void showAdminMenu() {
        while (true) {
            System.out.println("\n===== 管理员菜单 =====");
            System.out.println("1. 管理电影信息");
            System.out.println("2. 管理放映厅");
            System.out.println("3. 管理场次");
            System.out.println("4. 查看统计信息");
            System.out.println("5. 浏览电影");
            System.out.println("6. 查询场次");
            System.out.println("0. 退出");
            System.out.print("请选择操作: ");
            
            String choice = scanner.nextLine().trim();
            
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
                    newMethods.manageUsers();
                    break;
                case "5":
                    viewStatistics();
                    break;
                case "6":
                    newMethods.backupData();
                    break;
                case "7":
                    browseMovies();
                    break;
                case "8":
                    searchShows();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("无效选择，请重试");
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
        System.out.println("\n----- 查询场次 -----");
        System.out.print("请输入电影名称 (直接回车显示所有): ");
        String movieTitle = scanner.nextLine().trim();
        
        System.out.print("请输入日期 (YYYY-MM-DD，直接回车显示所有): ");
        String dateStr = scanner.nextLine().trim();
        
        LocalDate date = null;
        if (!dateStr.isEmpty()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                System.out.println("日期格式错误，显示所有场次");
            }
        }
        
        List<Show> shows = cinemaManager.searchShows(movieTitle, date);
        
        if (shows.isEmpty()) {
            System.out.println("未找到符合条件的场次");
            return;
        }
        
        System.out.println("\n找到 " + shows.size() + " 个场次:");
        for (int i = 0; i < shows.size(); i++) {
            Show show = shows.get(i);
            System.out.println((i + 1) + ". " + show.getMovie().getTitle());
            System.out.println("   场次ID: " + show.getId());
            System.out.println("   放映厅: " + show.getScreeningRoom().getName());
            System.out.println("   开始时间: " + show.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            System.out.println("   基础票价: ￥" + show.getBasePrice());
            System.out.println("   可用座位: " + show.getAvailableSeatsCount() + "/" + show.getTotalSeats());
            System.out.println();
        }
    }

    private void purchaseTicket() {
        System.out.println("\n----- 购买电影票 -----");
        
        // 先查询场次
        searchShows();
        
        System.out.print("请输入场次ID: ");
        String showId = scanner.nextLine().trim();
        
        Show show = cinemaManager.getShow(showId);
        if (show == null) {
            System.out.println("场次不存在");
            return;
        }
        
        // 显示座位图
        displaySeatMap(show);
        
        System.out.print("请输入要购买的座位ID (多个座位用逗号分隔，格式: 行-列，如: 3-5,3-6): ");
        String seatIdsStr = scanner.nextLine().trim();
        
        if (seatIdsStr.isEmpty()) {
            System.out.println("未选择座位");
            return;
        }
        
        String[] seatIds = seatIdsStr.split(",");
        List<String> seatIdList = new java.util.ArrayList<>();
        for (String seatId : seatIds) {
            seatIdList.add(seatId.trim());
        }
        
        try {
            Order order = bookingService.createOrder(currentUser, show, seatIdList);
            
            System.out.println("\n----- 订单信息 -----");
            System.out.println("订单ID: " + order.getOrderId());
            System.out.println("电影: " + show.getMovie().getTitle());
            System.out.println("场次时间: " + show.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            System.out.println("座位: " + order.getSeatIds());
            System.out.println("总金额: ￥" + order.getTotalAmount());
            
            System.out.print("确认支付？(Y/N): ");
            String confirm = scanner.nextLine().trim();
            
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
        }
        
        pressEnterToContinue();
    }

    private void displaySeatMap(Show show) {
        printTitle("座位选择 - " + show.getMovie().getTitle());
        
        // 显示图例
        printlnColored(CYAN, "\n图例：");
        printColored(GREEN, "  [O] 可用座位  ");
        printColored(YELLOW, "[L] 已锁定  ");
        printlnColored(RED, "[X] 已售出");
        
        Seat[][] seats = show.getScreeningRoom().getSeatLayout();
        
        // 打印列号
        printColored(CYAN, "\n     ");
        for (int col = 1; col <= seats[0].length; col++) {
            printColored(CYAN, String.format("%2d ", col));
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
        
        // 显示屏幕
        printSeparator('═', seats[0].length * 3 + 5);
        printlnColored(CYAN + BOLD, "           银幕");
        
        // 显示可用座位及价格
        printSeparator('-', 50);
        printlnColored(CYAN, "\n可用座位及价格：");
        
        List<Seat> availableSeats = show.getAvailableSeats();
        if (availableSeats.isEmpty()) {
            printError("暂无可选座位");
        } else {
            for (Seat seat : availableSeats) {
                double price = bookingService.calculateSeatPrice(show, seat);
                printColored(GREEN, "  座位 " + seat.getSeatId() + ": ");
                printColored(YELLOW + BOLD, String.format("￥%.2f", price));
                if (seat instanceof VIPSeat) {
                    printColored(PURPLE + BOLD, " (VIP座位)");
                }
                System.out.println();
            }
        }
        
        printSeparator('-', 50);
        printInfo("请输入座位位置（格式：行-列，例如：1-1），输入'完成'结束选择");
    }

    private void viewMyOrders() {
        clearScreen();
        printTitle("我的订单");
        
        List<Order> orders = currentUser.getOrders();
        
        if (orders.isEmpty()) {
            printWarning("暂无订单");
            pressEnterToContinue();
            return;
        }
        
        // 使用Set去重，避免重复显示
        java.util.Set<String> displayedOrderIds = new java.util.HashSet<>();
        int displayIndex = 1;
        
        for (Order order : orders) {
            // 避免重复显示相同订单
            if (!displayedOrderIds.contains(order.getOrderId())) {
                displayedOrderIds.add(order.getOrderId());
                
                printSeparator('═', 70);
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
                    case PAID:
                        printColored(GREEN + BOLD, "已支付");
                        break;
                    case CANCELLED:
                        printColored(RED, "已取消");
                        break;
                    case REFUNDED:
                        printColored(PURPLE, "已退款");
                        break;
                }
                System.out.println();
                
                printColored(CYAN, "订单金额: ");
                printColored(YELLOW + BOLD, String.format("￥%.2f", order.getTotalAmount()));
                System.out.println("\n");
            }
        }
        
        printSeparator('═', 70);
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
        String choice = scanner.nextLine().trim();
        
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
        String confirm = scanner.nextLine().trim();
        
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
        System.out.println("\n----- 切换定价策略 -----");
        System.out.println("1. 标准定价");
        System.out.println("2. 高级定价");
        System.out.print("请选择定价策略: ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                System.out.println("已切换到标准定价策略");
                // Note: In a real implementation, you'd need to restart the service
                break;
            case "2":
                System.out.println("已切换到高级定价策略");
                // Note: In a real implementation, you'd need to restart the service
                break;
            default:
                System.out.println("无效选择");
        }
    }

    private void manageMovies() {
        System.out.println("\n----- 管理电影信息 -----");
        System.out.println("1. 添加电影");
        System.out.println("2. 删除电影");
        System.out.println("3. 查看所有电影");
        System.out.print("请选择操作: ");
        
        String choice = scanner.nextLine().trim();
        
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
        String id = scanner.nextLine().trim();
        
        if (cinemaManager.getMovie(id) != null) {
            System.out.println("电影ID已存在");
            return;
        }
        
        System.out.print("请输入电影名称: ");
        String title = scanner.nextLine().trim();
        
        System.out.print("请输入导演: ");
        String director = scanner.nextLine().trim();
        
        System.out.print("请输入主演 (用逗号分隔): ");
        String actorsStr = scanner.nextLine().trim();
        List<String> actors = List.of(actorsStr.split(","));
        
        System.out.print("请输入时长 (分钟): ");
        int duration = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("请输入评分: ");
        double rating = Double.parseDouble(scanner.nextLine().trim());
        
        System.out.print("请输入类型: ");
        String genre = scanner.nextLine().trim();
        
        System.out.print("请输入简介: ");
        String description = scanner.nextLine().trim();
        
        System.out.print("请输入上映日期 (YYYY-MM-DD): ");
        LocalDate releaseTime = LocalDate.parse(scanner.nextLine().trim());
        
        Movie movie = new Movie(id, title, releaseTime, actors, director, duration, rating, description, genre);
        cinemaManager.addMovie(movie);
        
        System.out.println("电影添加成功");
    }

    private void removeMovie() {
        System.out.println("\n----- 删除电影 -----");
        browseMovies();
        
        System.out.print("请输入要删除的电影ID: ");
        String movieId = scanner.nextLine().trim();
        
        if (cinemaManager.getMovie(movieId) == null) {
            System.out.println("电影不存在");
            return;
        }
        
        System.out.print("确认删除电影？(Y/N): ");
        String confirm = scanner.nextLine().trim();
        
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
        
        String choice = scanner.nextLine().trim();
        
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
        String id = scanner.nextLine().trim();
        
        if (cinemaManager.getScreeningRoom(id) != null) {
            System.out.println("放映厅ID已存在");
            return;
        }
        
        System.out.print("请输入放映厅名称: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("请输入行数: ");
        int rows = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("请输入列数: ");
        int cols = Integer.parseInt(scanner.nextLine().trim());
        
        ScreeningRoom room = new ScreeningRoom(id, name, rows, cols);
        cinemaManager.addScreeningRoom(room);
        
        System.out.println("放映厅添加成功");
    }

    private void removeScreeningRoom() {
        System.out.println("\n----- 删除放映厅 -----");
        viewScreeningRooms();
        
        System.out.print("请输入要删除的放映厅ID: ");
        String roomId = scanner.nextLine().trim();
        
        if (cinemaManager.getScreeningRoom(roomId) == null) {
            System.out.println("放映厅不存在");
            return;
        }
        
        System.out.print("确认删除放映厅？(Y/N): ");
        String confirm = scanner.nextLine().trim();
        
        if (confirm.equalsIgnoreCase("Y")) {
            cinemaManager.removeScreeningRoom(roomId);
            System.out.println("放映厅删除成功");
        } else {
            System.out.println("取消删除");
        }
    }

    private void viewScreeningRooms() {
        System.out.println("\n----- 放映厅列表 -----");
        List<ScreeningRoom> rooms = cinemaManager.getAllScreeningRooms();
        
        if (rooms.isEmpty()) {
            System.out.println("暂无放映厅");
            return;
        }
        
        for (int i = 0; i < rooms.size(); i++) {
            ScreeningRoom room = rooms.get(i);
            System.out.println((i + 1) + ". " + room.toString());
            System.out.println();
        }
    }

    private void manageShows() {
        System.out.println("\n----- 管理场次 -----");
        System.out.println("1. 添加场次");
        System.out.println("2. 删除场次");
        System.out.println("3. 查看所有场次");
        System.out.print("请选择操作: ");
        
        String choice = scanner.nextLine().trim();
        
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
        String movieId = scanner.nextLine().trim();
        
        Movie movie = cinemaManager.getMovie(movieId);
        if (movie == null) {
            System.out.println("电影不存在");
            return;
        }
        
        viewScreeningRooms();
        
        System.out.print("请输入放映厅ID: ");
        String roomId = scanner.nextLine().trim();
        
        ScreeningRoom room = cinemaManager.getScreeningRoom(roomId);
        if (room == null) {
            System.out.println("放映厅不存在");
            return;
        }
        
        System.out.print("请输入开始时间 (YYYY-MM-DD HH:MM): ");
        LocalDateTime startTime = LocalDateTime.parse(scanner.nextLine().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        System.out.print("请输入基础票价: ");
        double basePrice = Double.parseDouble(scanner.nextLine().trim());
        
        Show show = new Show("SHOW-" + System.currentTimeMillis(), movie, room, startTime, basePrice);
        cinemaManager.addShow(show);
        
        System.out.println("场次添加成功");
    }

    private void removeShow() {
        System.out.println("\n----- 删除场次 -----");
        searchShows();
        
        System.out.print("请输入要删除的场次ID: ");
        String showId = scanner.nextLine().trim();
        
        if (cinemaManager.getShow(showId) == null) {
            System.out.println("场次不存在");
            return;
        }
        
        System.out.print("确认删除场次？(Y/N): ");
        String confirm = scanner.nextLine().trim();
        
        if (confirm.equalsIgnoreCase("Y")) {
            cinemaManager.removeShow(showId);
            System.out.println("场次删除成功");
        } else {
            System.out.println("取消删除");
        }
    }

    private void viewStatistics() {
        System.out.println("\n----- 统计信息 -----");
        
        List<Movie> movies = cinemaManager.getAllMovies();
        List<ScreeningRoom> rooms = cinemaManager.getAllScreeningRooms();
        List<Show> shows = cinemaManager.getAllShows();
        List<User> users = cinemaManager.getAllUsers();
        List<Order> orders = bookingService.getAllOrders();
        
        System.out.println("电影总数: " + movies.size());
        System.out.println("放映厅总数: " + rooms.size());
        System.out.println("场次总数: " + shows.size());
        System.out.println("用户总数: " + users.size());
        System.out.println("订单总数: " + orders.size());
        
        int paidOrders = 0;
        double totalRevenue = 0.0;
        
        for (Order order : orders) {
            if (order.getStatus() == Order.OrderStatus.PAID) {
                paidOrders++;
                totalRevenue += order.getTotalAmount();
            }
        }
        
        System.out.println("已支付订单: " + paidOrders);
        System.out.println("总收入: ￥" + String.format("%.2f", totalRevenue));
    }
}
package com.cinema.ui;

import com.cinema.model.*;
import com.cinema.service.*;
import com.cinema.strategy.StandardPricing;
import com.cinema.strategy.PremiumPricing;

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

    public ConsoleUI() {
        this.cinemaManager = CinemaManager.getInstance();
        this.bookingService = BookingService.getInstance(new StandardPricing());
        this.scanner = new Scanner(System.in);
        this.currentUser = null;
        this.newMethods = null;
    }

    public void start() {
        System.out.println("=====================================");
        System.out.println("     欢迎使用电影院购票系统");
        System.out.println("=====================================");
        
        login();
        
        if (currentUser != null) {
            newMethods = new NewMethods(cinemaManager, bookingService, scanner, currentUser);
            if (currentUser.isAdmin()) {
                showAdminMenu();
            } else {
                showCustomerMenu();
            }
        }
        
        System.out.println("感谢使用电影院购票系统，再见！");
    }

    private void login() {
        while (true) {
            System.out.println("\n===== 用户登录系统 =====");
            System.out.println("1. 用户登录");
            System.out.println("2. 用户注册");
            System.out.println("0. 退出系统");
            System.out.print("请选择操作: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    if (performLogin()) {
                        return;
                    }
                    break;
                case "2":
                    performRegister();
                    break;
                case "0":
                    System.out.println("感谢使用电影院购票系统，再见！");
                    System.exit(0);
                    break;
                default:
                    System.out.println("无效选择，请重试");
            }
        }
    }
    
    private boolean performLogin() {
        System.out.println("\n----- 用户登录 -----");
        System.out.print("请输入用户ID: ");
        String userId = scanner.nextLine().trim();
        
        if (userId.isEmpty()) {
            System.out.println("用户ID不能为空");
            return false;
        }
        
        User user = cinemaManager.getUser(userId);
        if (user != null) {
            currentUser = user;
            System.out.println("登录成功！欢迎，" + currentUser.getName());
            if (currentUser.isAdmin()) {
                System.out.println("当前角色: 管理员");
            } else {
                System.out.println("当前角色: 普通用户");
            }
            return true;
        } else {
            System.out.println("用户不存在，请先注册");
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
        System.out.println("\n----- 电影列表 -----");
        List<Movie> movies = cinemaManager.getAllMovies();
        
        if (movies.isEmpty()) {
            System.out.println("暂无电影信息");
            return;
        }
        
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            System.out.println((i + 1) + ". " + movie.getTitle());
            System.out.println("   ID: " + movie.getId());
            System.out.println("   导演: " + movie.getDirector());
            System.out.println("   主演: " + String.join(", ", movie.getActors()));
            System.out.println("   时长: " + movie.getDuration() + "分钟");
            System.out.println("   评分: " + movie.getRating());
            System.out.println("   类型: " + movie.getGenre());
            System.out.println("   上映日期: " + movie.getReleaseTime());
            System.out.println("   简介: " + movie.getDescription());
            System.out.println();
        }
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
                if (bookingService.processPayment(order)) {
                    System.out.println("支付成功！订单已完成。");
                } else {
                    System.out.println("支付失败，订单已取消。");
                }
            } else {
                bookingService.cancelOrder(order);
                System.out.println("订单已取消。");
            }
            
        } catch (Exception e) {
            System.out.println("购票失败: " + e.getMessage());
        }
    }

    private void displaySeatMap(Show show) {
        System.out.println("\n----- 座位图 -----");
        System.out.println("图例: [O] 可用  [X] 已售  [L] 锁定");
        
        Seat[][] seats = show.getScreeningRoom().getSeatLayout();
        
        System.out.print("   ");
        for (int col = 1; col <= seats[0].length; col++) {
            System.out.printf("%2d ", col);
        }
        System.out.println();
        
        for (int row = 0; row < seats.length; row++) {
            System.out.printf("%2d ", row + 1);
            for (int col = 0; col < seats[row].length; col++) {
                Seat showSeat = show.getSeat(row + 1, col + 1);
                if (showSeat.isAvailable()) {
                    System.out.print("[O] ");
                } else if (showSeat.isLocked()) {
                    System.out.print("[L] ");
                } else {
                    System.out.print("[X] ");
                }
            }
            System.out.println();
        }
        
        System.out.println("\n可用座位:");
        List<Seat> availableSeats = show.getAvailableSeats();
        for (Seat seat : availableSeats) {
            double price = bookingService.calculateSeatPrice(show, seat);
            System.out.printf("座位 %s: ￥%.2f", seat.getSeatId(), price);
            if (seat instanceof VIPSeat) {
                System.out.print(" (VIP)");
            }
            System.out.println();
        }
    }

    private void viewMyOrders() {
        System.out.println("\n----- 我的订单 -----");
        List<Order> orders = currentUser.getOrders();
        
        if (orders.isEmpty()) {
            System.out.println("暂无订单");
            return;
        }
        
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            System.out.println((i + 1) + ". " + order.toString());
            System.out.println();
        }
    }

    private void refundTicket() {
        System.out.println("\n----- 退票 -----");
        viewMyOrders();
        
        System.out.print("请输入要退票的订单ID: ");
        String orderId = scanner.nextLine().trim();
        
        Order order = bookingService.getOrder(orderId);
        if (order == null) {
            System.out.println("订单不存在");
            return;
        }
        
        if (!order.getOrderId().equals(orderId)) {
            System.out.println("订单不属于当前用户");
            return;
        }
        
        System.out.println("订单信息: " + order.toString());
        System.out.print("确认退票？(Y/N): ");
        String confirm = scanner.nextLine().trim();
        
        if (confirm.equalsIgnoreCase("Y")) {
            if (bookingService.cancelOrder(order)) {
                System.out.println("退票成功");
            } else {
                System.out.println("退票失败");
            }
        } else {
            System.out.println("取消退票");
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
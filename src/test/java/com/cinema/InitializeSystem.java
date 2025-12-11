package com.cinema;

import com.cinema.model.*;
import com.cinema.service.CinemaManager;
import com.cinema.service.BookingService;
import com.cinema.strategy.StandardPricing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 初始化系统数据 - 创建用户、电影、放映厅和场次
 */
public class InitializeSystem {
    public static void main(String[] args) {
        System.out.println("初始化电影院系统...\n");
        
        CinemaManager cinemaManager = CinemaManager.getInstance();
        BookingService bookingService = BookingService.getInstance(new StandardPricing());
        
        // 1. 创建用户（使用你修改过的带密码版本）
        System.out.println("1. 创建用户账号");
        createUsers(cinemaManager);
        
        // 2. 创建电影（使用同学的完整版本）
        System.out.println("\n2. 创建电影");
        createMovies(cinemaManager);
        
        // 3. 创建放映厅（使用同学的完整版本）
        System.out.println("\n3. 创建放映厅");
        createScreeningRooms(cinemaManager);
        
        // 4. 创建场次（使用同学的完整逻辑）
        System.out.println("\n4. 创建场次");
        createShows(cinemaManager);
        
        // 5. 保存数据
        System.out.println("\n5. 保存数据");
        cinemaManager.saveAllData();
        bookingService.saveOrders();
        
        // 6. 验证
        System.out.println("\n系统初始化完成！");
        System.out.println("用户: " + cinemaManager.getAllUsers().size() + " 个");
        System.out.println("电影: " + cinemaManager.getAllMovies().size() + " 部");
        System.out.println("放映厅: " + cinemaManager.getAllScreeningRooms().size() + " 个");
        System.out.println("场次: " + cinemaManager.getAllShows().size() + " 个");
        System.out.println("订单: " + bookingService.getAllOrders().size() + " 个");
    }
    
    // 【你的修改】使用了带复杂密码的构造函数
    private static void createUsers(CinemaManager cinemaManager) {
        // 管理员 (Admin@123)
        User admin = new User(
                "ADMIN-001", 
                "系统管理员", 
                "Admin@123",  // 你的修改：加入了密码
                "13800138000", 
                "admin@cinema.com", 
                User.UserRole.ADMIN
        );
        cinemaManager.addUser(admin);
        System.out.println("  - 创建管理员: " + admin.getId());
        
        // 普通用户 (User@123)
        User user = new User(
                "renquan", 
                "renquan", 
                "User@123",   // 你的修改：加入了密码
                "13900139000", 
                "renquan@example.com", 
                User.UserRole.CUSTOMER
        );
        cinemaManager.addUser(user);
        System.out.println("  - 创建用户: " + user.getId());
        
        // 测试用户 (User@123)
        User testUser = new User(
                "test", 
                "测试用户", 
                "User@123",   // 你的修改：加入了密码
                "13700137000", 
                "test@example.com", 
                User.UserRole.CUSTOMER
        );
        cinemaManager.addUser(testUser);
        System.out.println("  - 创建用户: " + testUser.getId());
    }
    
    // 【同学的代码】保留他写的电影数据
    private static void createMovies(CinemaManager cinemaManager) {
        Movie movie1 = new Movie("MOV-001", "阿凡达：水之道", LocalDate.of(2022, 12, 16),
                               Arrays.asList("萨姆·沃辛顿", "佐伊·索尔达娜", "西格妮·韦弗"),
                               "詹姆斯·卡梅隆", 192, 9.0, 
                               "杰克·萨利与妻子奈蒂莉组建了家庭，他们的孩子也逐渐成长", 
                               MovieGenre.ACTION);
        
        Movie movie2 = new Movie("MOV-002", "流浪地球2", LocalDate.of(2023, 1, 22),
                               Arrays.asList("吴京", "刘德华", "李雪健", "沙溢", "宁理"),
                               "郭帆", 173, 8.3,
                               "太阳即将毁灭，人类在地球表面建造出巨大的推进器",
                               MovieGenre.DRAMA);
        
        Movie movie3 = new Movie("MOV-003", "满江红", LocalDate.of(2023, 1, 22),
                               Arrays.asList("沈腾", "易烊千玺", "张译", "雷佳音", "岳云鹏", "王佳怡"),
                               "张艺谋", 159, 7.9,
                               "南宋绍兴年间，岳飞死后四年，秦桧率兵与金国会谈",
                               MovieGenre.ACTION);
        
        Movie movie4 = new Movie("MOV-004", "深海", LocalDate.of(2023, 1, 22),
                               Arrays.asList("苏鑫", "王亭文", "滕奎兴"),
                               "田晓鹏", 112, 7.3,
                               "在大海的最深处，藏着所有秘密。一位现代少女'参宿'",
                               MovieGenre.ANIMATION);
        
        cinemaManager.addMovie(movie1);
        cinemaManager.addMovie(movie2);
        cinemaManager.addMovie(movie3);
        cinemaManager.addMovie(movie4);
        
        System.out.println("  - " + movie1.getTitle());
        System.out.println("  - " + movie2.getTitle());
        System.out.println("  - " + movie3.getTitle());
        System.out.println("  - " + movie4.getTitle());
    }
    
    // 【同学的代码】保留他写的放映厅数据
    private static void createScreeningRooms(CinemaManager cinemaManager) {
        ScreeningRoom room1 = new ScreeningRoom("ROOM-1", "1号厅", 8, 12);
        ScreeningRoom room2 = new ScreeningRoom("ROOM-2", "2号厅", 10, 15);
        ScreeningRoom room3 = new ScreeningRoom("ROOM-3", "3号厅", 12, 18);
        ScreeningRoom room4 = new ScreeningRoom("ROOM-4", "4号厅", 8, 10);
        
        cinemaManager.addScreeningRoom(room1);
        cinemaManager.addScreeningRoom(room2);
        cinemaManager.addScreeningRoom(room3);
        cinemaManager.addScreeningRoom(room4);
        
        System.out.println("  - 1号厅: 8排×12列");
        System.out.println("  - 2号厅: 10排×15列");
        System.out.println("  - 3号厅: 12排×18列");
        System.out.println("  - 4号厅: 8排×10列");
    }
    
    // 【同学的代码】保留他写的复杂的场次计算逻辑
    private static void createShows(CinemaManager cinemaManager) {
        List<Movie> movies = cinemaManager.getAllMovies();
        List<ScreeningRoom> rooms = cinemaManager.getAllScreeningRooms();
        
        // LocalDateTime now = LocalDateTime.now(); // 同学代码里有但这行似乎没用上，可以注释掉
        int showIndex = 1;
        
        // 为每部电影创建多个场次
        for (int movieIndex = 0; movieIndex < movies.size(); movieIndex++) {
            Movie movie = movies.get(movieIndex);
            
            // 每部电影创建6-8个场次
            int showsPerMovie = 6 + (movieIndex % 3);
            
            for (int i = 0; i < showsPerMovie; i++) {
                // 选择放映厅（轮换使用）
                ScreeningRoom room = rooms.get((movieIndex + i) % rooms.size());
                
                // 计算放映时间（从12月21日开始的未来日期）
                int daysFrom21 = (movieIndex * 2 + i / 3) % 10 + 1; // 12月21日后的1-10天
                int hourOfDay = 9 + (i % 4) * 4; // 9:00, 13:00, 17:00, 21:00
                
                LocalDateTime showTime = LocalDateTime.of(
                    LocalDate.of(2025, 12, 21).plusDays(daysFrom21 - 1),
                    java.time.LocalTime.of(hourOfDay, 0)
                );
                
                // 创建场次，分别设置三种价格
                Show show = new Show(
                    "SHOW-" + String.format("%03d", showIndex++),
                    movie,
                    room,
                    showTime,
                    50.0, // 普通座位价格50元
                    40.0, // 优惠座位价格40元（80%）
                    60.0  // VIP座位价格60元（比普通贵10元）
                );
                
                cinemaManager.addShow(show);
            }
        }
    }
}
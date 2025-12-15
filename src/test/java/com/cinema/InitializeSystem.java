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
 * 初始化系统数据 - 创建用户、电影、放映厅和场次 (本地资源版)
 */
public class InitializeSystem {
    public static void main(String[] args) {
        System.out.println("初始化电影院系统 (本地资源版)...\n");

        CinemaManager cinemaManager = CinemaManager.getInstance();
        BookingService bookingService = BookingService.getInstance(new StandardPricing());

        // 1. 创建用户
        System.out.println("1. 创建用户账号");
        createUsers(cinemaManager);

        // 2. 创建电影 (使用本地相对路径)
        System.out.println("\n2. 创建电影");
        createMovies(cinemaManager);

        // 3. 创建放映厅 (取消注释，必须执行)
        System.out.println("\n3. 创建放映厅");
        createScreeningRooms(cinemaManager);

        // 4. 创建场次
        System.out.println("\n4. 创建场次");
        createShows(cinemaManager);

        // 5. 保存数据
        System.out.println("\n5. 保存数据");
        cinemaManager.saveAllData();
        bookingService.saveOrders();

        // 6. 验证
        System.out.println("\n系统初始化完成！资源已指向本地 public/media 目录。");
        System.out.println("用户: " + cinemaManager.getAllUsers().size() + " 个");
        System.out.println("电影: " + cinemaManager.getAllMovies().size() + " 部");
        System.out.println("放映厅: " + cinemaManager.getAllScreeningRooms().size() + " 个");
        System.out.println("场次: " + cinemaManager.getAllShows().size() + " 个");
        System.out.println("订单: " + bookingService.getAllOrders().size() + " 个");
    }

    private static void createUsers(CinemaManager cinemaManager) {
        // 管理员
        User admin = new User("ADMIN-001", "系统管理员", "Admin@123", "13800138000", "admin@cinema.com", User.UserRole.ADMIN);
        cinemaManager.addUser(admin);

        // 普通用户
        User user = new User("renquan", "renquan", "User@123", "13900139000", "renquan@example.com", User.UserRole.CUSTOMER);
        cinemaManager.addUser(user);

        User testUser = new User("test", "测试用户", "User@123", "13700137000", "test@example.com", User.UserRole.CUSTOMER);
        cinemaManager.addUser(testUser);

        System.out.println("  - 创建管理员: " + admin.getId());
        System.out.println("  - 创建用户: " + user.getId());
        System.out.println("  - 创建用户: " + testUser.getId());
    }

    private static void createMovies(CinemaManager cinemaManager) {
        // 🔴 核心修改：使用本地相对路径，对应 web/public/media/ 目录

        Movie movie1 = new Movie("MOV-001", "阿凡达：水之道", LocalDate.of(2022, 12, 16),
                Arrays.asList("萨姆·沃辛顿", "佐伊·索尔达娜"),
                "詹姆斯·卡梅隆", 192, 9.0,
                "杰克·萨利与妻子奈蒂莉组建了家庭，他们的孩子也逐渐成长。",
                MovieGenre.ACTION,
                "/media/trailers/1.mp4", // 🔴 本地视频路径
                "/media/covers/1.jpg"    // 🔴 本地图片路径
        );

        Movie movie2 = new Movie("MOV-002", "流浪地球2", LocalDate.of(2023, 1, 22),
                Arrays.asList("吴京", "刘德华"),
                "郭帆", 173, 8.3,
                "太阳即将毁灭，人类在地球表面建造出巨大的推进器。",
                MovieGenre.DRAMA,
                "/media/trailers/2.mp4",  // 🔴 本地视频路径
                "/media/covers/2.jpg"     // 🔴 本地图片路径
        );

        Movie movie3 = new Movie("MOV-003", "满江红", LocalDate.of(2023, 1, 22),
                Arrays.asList("沈腾", "易烊千玺"),
                "张艺谋", 159, 7.9,
                "南宋绍兴年间，岳飞死后四年，秦桧率兵与金国会谈。",
                MovieGenre.ACTION,
                "/media/trailers/3.mp4", // 无预告片
                "/media/covers/3.jpg" // 🔴 本地图片路径
        );



        cinemaManager.addMovie(movie1);
        cinemaManager.addMovie(movie2);
        cinemaManager.addMovie(movie3);

        System.out.println("  - " + movie1.getTitle());
        System.out.println("  - " + movie2.getTitle());
        System.out.println("  - " + movie3.getTitle());
    }

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

    private static void createShows(CinemaManager cinemaManager) {
        List<Movie> movies = cinemaManager.getAllMovies();
        List<ScreeningRoom> rooms = cinemaManager.getAllScreeningRooms();

        int showIndex = 1;
        for (int movieIndex = 0; movieIndex < movies.size(); movieIndex++) {
            Movie movie = movies.get(movieIndex);
            // 每部电影创建6-8个场次
            int showsPerMovie = 6 + (movieIndex % 3);
            for (int i = 0; i < showsPerMovie; i++) {
                ScreeningRoom room = rooms.get((movieIndex + i) % rooms.size());
                // 计算放映时间 (基于当前时间未来 1-10 天)
                int daysFromNow = (movieIndex * 2 + i / 3) % 10 + 1;
                int hourOfDay = 9 + (i % 4) * 4; // 9:00, 13:00, 17:00, 21:00

                LocalDateTime showTime = LocalDateTime.now().plusDays(daysFromNow).withHour(hourOfDay).withMinute(0);

                Show show = new Show(
                        "SHOW-" + String.format("%03d", showIndex++),
                        movie,
                        room,
                        showTime,
                        50.0,
                        40.0,
                        60.0
                );
                cinemaManager.addShow(show);
            }
        }
    }
}
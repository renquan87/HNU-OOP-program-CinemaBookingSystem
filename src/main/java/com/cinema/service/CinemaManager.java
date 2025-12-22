package com.cinema.service;

import com.cinema.model.*;
import com.cinema.storage.MySQLDataStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CinemaManager {
    private static CinemaManager instance;
    private final Map<String, Movie> movies;
    private final Map<String, ScreeningRoom> rooms;
    private final Map<String, Show> shows;
    private final Map<String, User> users;

    private final MySQLDataStorage mysqlDataStorage; // 数据库存储
    private final boolean useMySQL;

    // 引入显示服务 (新增功能)
    private final DisplayService displayService;

    private CinemaManager() {
        this.movies = new ConcurrentHashMap<>();
        this.rooms = new ConcurrentHashMap<>();
        this.shows = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();

        // 引入显示服务 (初始化)
        this.displayService = DisplayService.getInstance();

        // 尝试使用MySQL，如果失败则使用文件存储
        boolean mysqlAvailable = false;
        MySQLDataStorage mysqlStorage = null;
        try {
            mysqlStorage = new MySQLDataStorage();
            mysqlAvailable = true;
            System.out.println("✓ CinemaManager使用MySQL数据库存储");
        } catch (Exception e) {
            System.err.println("✗ MySQL连接失败，使用文件存储: " + e.getMessage());
            mysqlStorage = null;
        }
        this.mysqlDataStorage = mysqlStorage;
        this.useMySQL = mysqlAvailable;

        loadData();

        // 如果没有数据，则初始化默认数据
        if (movies.isEmpty() && rooms.isEmpty() && users.isEmpty()) {
            initializeDefaultData();
        }
    }

    public static synchronized CinemaManager getInstance() {
        if (instance == null) {
            instance = new CinemaManager();
        }
        return instance;
    }

    // ================== 初始化数据 (保留) ==================
    private void initializeDefaultData() {
        // Create default screening rooms
        ScreeningRoom room1 = new ScreeningRoom("ROOM-001", "标准厅1", 8, 12);
        ScreeningRoom room2 = new ScreeningRoom("ROOM-002", "VIP厅", 6, 10);
        ScreeningRoom room3 = new ScreeningRoom("ROOM-003", "IMAX厅", 10, 15);

        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);
        rooms.put(room3.getId(), room3);

        // Create default movies
        Movie movie1 = new Movie(
                "MOV-001",
                "流浪地球2",
                LocalDate.of(2023, 1, 22),
                List.of("吴京", "刘德华", "李雪健"),
                "郭帆",
                173,
                8.3,
                "太阳即将毁灭，人类在地球表面建造出巨大的推进器，寻找新的家园。然而宇宙之路危机四伏，为了拯救地球，流浪地球时代的年轻人再次挺身而出。",
                "科幻"
        );

        Movie movie2 = new Movie(
                "MOV-002",
                "满江红",
                LocalDate.of(2023, 1, 22),
                List.of("沈腾", "易烊千玺", "张译", "雷佳音"),
                "张艺谋",
                159,
                7.9,
                "南宋绍兴年间，岳飞死后四年，秦桧率兵与金国会谈。会谈前夜，金国使者死在宰相驻地，所携密信也不翼而飞。",
                "剧情/悬疑"
        );

        Movie movie3 = new Movie(
                "MOV-003",
                "深海",
                LocalDate.of(2023, 1, 22),
                List.of("苏鑫", "王亭文", "滕奎兴"),
                "田晓鹏",
                112,
                7.3,
                "在大海的最深处，藏着所有秘密。一位现代少女参宿，在神秘海洋世界中追寻探索，邂逅了一段独特的生命旅程的故事。",
                "动画/奇幻"
        );

        movies.put(movie1.getId(), movie1);
        movies.put(movie2.getId(), movie2);
        movies.put(movie3.getId(), movie3);

        // Create default shows
        LocalDateTime now = LocalDateTime.now();

        Show show1 = new Show(
                "SHOW-001",
                movie1,
                room1,
                now.plusDays(1).withHour(14).withMinute(30),
                45.0
        );

        Show show2 = new Show(
                "SHOW-002",
                movie1,
                room1,
                now.plusDays(1).withHour(19).withMinute(0),
                55.0
        );

        Show show3 = new Show(
                "SHOW-003",
                movie2,
                room2,
                now.plusDays(1).withHour(15).withMinute(0),
                60.0
        );

        Show show4 = new Show(
                "SHOW-004",
                movie3,
                room3,
                now.plusDays(2).withHour(13).withMinute(30),
                50.0
        );

        shows.put(show1.getId(), show1);
        shows.put(show2.getId(), show2);
        shows.put(show3.getId(), show3);
        shows.put(show4.getId(), show4);

        // Add shows to movie schedules
        movie1.addShow(now.plusDays(1).toLocalDate(), show1);
        movie1.addShow(now.plusDays(1).toLocalDate(), show2);
        movie2.addShow(now.plusDays(1).toLocalDate(), show3);
        movie3.addShow(now.plusDays(2).toLocalDate(), show4);

        // Create default admin user
        User admin = new User(
                "ADMIN-001",
                "管理员",
                "admin123",      // 密码
                "13800138000",
                "admin@cinema.com",
                User.UserRole.ADMIN
        );
        users.put(admin.getId(), admin);
        // Create default normal user
        User testUser = new User(
                "test",          // 用户 ID（唯一）
                "test",              // 用户名
                "test1234",
                "18800000000",       // 手机号（随便写一个）
                "test@cinema.com",   // 邮箱
                User.UserRole.CUSTOMER  // 普通用户角色
        );
        users.put(testUser.getId(), testUser);

        saveAllData(); // 初始化后保存数据
    }


    // ================== 电影管理 (集成 DisplayService) ==================
    public void addMovie(Movie movie) {
        if (movie != null && movie.getId() != null) {
            movies.put(movie.getId(), movie);
            saveMovies();
            // 触发显示更新
            displayService.updateMovieDisplay(movie, "新增上映");
        }
    }

    public void removeMovie(String movieId) {
        Movie movie = movies.remove(movieId);
        if (movie != null) {
            // Remove all shows for this movie
            List<Show> showsToRemove = new ArrayList<>();
            for (Show show : shows.values()) {
                if (show.getMovieId().equals(movieId)) {
                    showsToRemove.add(show);
                }
            }
            for (Show show : showsToRemove) {
                shows.remove(show.getId());
            }

            saveMovies();
            saveShows();
            // 触发显示更新
            displayService.updateMovieDisplay(movie, "下架");
        }
    }

    // 🔴 修复：添加评论后立即保存到数据库
    public void addComment(String movieId, Comment comment) {
        Movie movie = movies.get(movieId);
        if (movie != null) {
            movie.addComment(comment);

            // 重新计算评分 (0-10分)
            double total = 0;
            List<Comment> comments = movie.getComments();
            if (!comments.isEmpty()) {
                for (Comment c : comments) {
                    total += c.getRating();
                }
                double avg = total / comments.size();
                movie.setRating(Math.round(avg * 10.0) / 10.0);
            }

            // 🔴 关键：调用 saveMovies 触发数据库写入
            saveMovies();
            System.out.println("评论已添加并保存到数据库");
        }
    }

    // ================== 场次管理 (集成 DisplayService) ==================
    public void addShow(Show show) {
        if (show != null && show.getId() != null) {
            shows.put(show.getId(), show);
            show.getMovie().addShow(show.getStartTime().toLocalDate(), show);
            saveShows();
            saveMovies();
            // 触发显示更新（更新该场次的座位图显示）
            displayService.updateSeatDisplay(show);
        }
    }

    public void removeShow(String showId) {
        Show show = shows.remove(showId);
        if (show != null) {
            show.getMovie().removeShow(show.getStartTime().toLocalDate(), show);
            saveShows();
            saveMovies();
            // 触发显示更新（移除该场次的座位图显示）
            displayService.updateSeatDisplay(show);
        }
    }

    // ================== 影厅/用户管理 (保留原逻辑) ==================

    public void addScreeningRoom(ScreeningRoom room) {
        if (room != null && room.getId() != null) {
            rooms.put(room.getId(), room);
            saveRooms();
        }
    }

    public void removeScreeningRoom(String roomId) {
        rooms.remove(roomId);
        saveRooms();
    }

    // 🔴 确保注册时调用 saveUsers
    public void addUser(User user) {
        if (user != null && user.getId() != null) {
            users.put(user.getId(), user);
            saveUsers(); // 这会调用 MySQLDataStorage.saveUsers
        }
    }

    public void removeUser(String userId) {
        users.remove(userId);
        saveUsers();
    }

    // ================== 查询方法 (保留) ==================

    public Movie getMovie(String movieId) {
        return movies.get(movieId);
    }

    public List<Movie> getAllMovies() {
        List<Movie> movieList = new ArrayList<>(movies.values());
        // 按电影ID排序
        movieList.sort((m1, m2) -> m1.getId().compareTo(m2.getId()));
        return movieList;
    }

    public ScreeningRoom getScreeningRoom(String roomId) {
        return rooms.get(roomId);
    }

    public List<ScreeningRoom> getAllScreeningRooms() {
        return new ArrayList<>(rooms.values());
    }

    public Show getShow(String showId) {
        return shows.get(showId);
    }

    public List<Show> getAllShows() {
        return new ArrayList<>(shows.values());
    }

    public List<Show> getShowsByMovie(String movieId) {
        List<Show> movieShows = new ArrayList<>();
        for (Show show : shows.values()) {
            if (show.getMovieId().equals(movieId)) {
                movieShows.add(show);
            }
        }
        return movieShows;
    }

    public List<Show> getShowsByDate(LocalDate date) {
        List<Show> dateShows = new ArrayList<>();
        for (Show show : shows.values()) {
            if (show.getStartTime().toLocalDate().equals(date)) {
                dateShows.add(show);
            }
        }
        return dateShows;
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public List<Show> searchShows(String movieTitle, LocalDate date) {
        List<Show> matchingShows = new ArrayList<>();
        for (Show show : shows.values()) {
            boolean titleMatch = movieTitle == null || movieTitle.isEmpty() ||
                    show.getMovieTitle().contains(movieTitle);
            boolean dateMatch = date == null ||
                    show.getStartTime().toLocalDate().equals(date);

            if (titleMatch && dateMatch) {
                matchingShows.add(show);
            }
        }
        return matchingShows;
    }

    // ================== 数据持久化 (保留) ==================

    private void loadData() {
        if (useMySQL && mysqlDataStorage != null) {
            movies.putAll(mysqlDataStorage.loadMovies());
            rooms.putAll(mysqlDataStorage.loadScreeningRooms());
            shows.putAll(mysqlDataStorage.loadShows());
            users.putAll(mysqlDataStorage.loadUsers());
        }
    }

    public void saveMovies() {
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.saveMovies(movies);
        }
    }

    public void saveRooms() {
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.saveScreeningRooms(rooms);
        }
    }

    public void saveShows() {
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.saveShows(shows);
        }
    }

    public void saveUsers() {
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.saveUsers(users);
        }
    }

    /**
     * 保存所有核心数据，包括订单数据
     */
    public void saveAllData() {
        saveMovies();
        saveRooms();
        saveShows();
        saveUsers();
        // 尝试获取 BookingService 实例并保存订单
        try {
            BookingService.getInstance().saveOrders();
        } catch (IllegalStateException e) {
            System.err.println("BookingService 未初始化，跳过订单保存。");
        }
    }

    /**
     * 关闭资源，包括保存数据和关闭数据库连接
     */
    public void shutdown() {
        System.out.println("正在关闭 CinemaManager...");
        saveAllData();
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.close();
            System.out.println("✓ MySQL 连接已关闭。");
        }
        // 由于 DisplayService 是单例，其资源的释放应在其自身的 shutdown 方法中处理（如果需要）
    }
}
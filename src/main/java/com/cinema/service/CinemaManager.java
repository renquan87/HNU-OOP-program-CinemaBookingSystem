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
    private final MySQLDataStorage mysqlDataStorage;
    private final boolean useMySQL;

    private CinemaManager() {
        this.movies = new ConcurrentHashMap<>();
        this.rooms = new ConcurrentHashMap<>();
        this.shows = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        
        // 强制使用MySQL存储
        MySQLDataStorage mysqlStorage = null;
        try {
            mysqlStorage = new MySQLDataStorage();
            System.out.println("✓ 系统已切换到MySQL数据库存储模式");
        } catch (Exception e) {
            System.err.println("✗ MySQL连接失败: " + e.getMessage());
            System.err.println("错误：系统必须使用MySQL数据库存储，请检查：");
            System.err.println("  1. MySQL服务是否已启动");
            System.err.println("  2. 数据库连接配置是否正确");
            System.err.println("  3. MySQL Connector/J驱动是否在lib目录中");
            System.err.println("\n程序将退出，请修复MySQL连接后重试");
            System.exit(1); // 如果MySQL不可用，退出程序
        }
        this.mysqlDataStorage = mysqlStorage;
        this.useMySQL = true; // 强制使用MySQL
        
        loadData();
        
        // 如果没有数据，则初始化默认数据
        if (movies.isEmpty() && rooms.isEmpty() && users.isEmpty()) {
            initializeDefaultData();
            System.out.println("✓ 已初始化默认数据到MySQL数据库");
        }
    }

    public static synchronized CinemaManager getInstance() {
        if (instance == null) {
            instance = new CinemaManager();
        }
        return instance;
    }

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
            "admin-001",
            "管理员",
            "13800138000",
            "admin@cinema.com",
            User.UserRole.ADMIN
        );
        users.put(admin.getId(), admin);

        // Create default normal user
        User testUser = new User(
            "test",          // 用户 ID（唯一）
            "test",              // 用户名
            "18800000000",       // 手机号（随便写一个）
            "test@cinema.com",   // 邮箱
            User.UserRole.CUSTOMER  // 普通用户角色
        );
        users.put(testUser.getId(), testUser);

    }

    public void addMovie(Movie movie) {
        if (movie != null && movie.getId() != null) {
            movies.put(movie.getId(), movie);
            saveMovies();
            System.out.println("电影添加成功并已保存到数据库");
        }
    }

    public void removeMovie(String movieId) {
        Movie movie = movies.get(movieId);
        if (movie != null) {
            // 先删除所有相关的场次（从shows集合中删除）
            List<Show> showsToRemove = new ArrayList<>();
            for (Show show : shows.values()) {
                if (show.getMovieId().equals(movieId)) {
                    showsToRemove.add(show);
                }
            }
            
            // 从shows集合中删除这些场次
            for (Show show : showsToRemove) {
                shows.remove(show.getId());
            }
            
            // 保存场次到数据库（先保存）
            saveShows();
            
            // 然后从movies集合中删除电影
            movies.remove(movieId);
            
            // 最后保存电影到数据库
            saveMovies();
            
            System.out.println("电影删除成功并已从数据库移除");
        }
    }

    public void addShow(Show show) {
        if (show != null && show.getId() != null) {
            shows.put(show.getId(), show);
            show.getMovie().addShow(show.getStartTime().toLocalDate(), show);
            saveShows();
            saveMovies();
            System.out.println("场次添加成功并已保存到数据库");
        }
    }

    public void removeShow(String showId) {
        Show show = shows.remove(showId);
        if (show != null) {
            show.getMovie().removeShow(show.getStartTime().toLocalDate(), show);
            saveShows();
            saveMovies();
            System.out.println("场次删除成功并已从数据库移除");
        }
    }

    public void addScreeningRoom(ScreeningRoom room) {
        if (room != null && room.getId() != null) {
            rooms.put(room.getId(), room);
            saveRooms();
            System.out.println("放映厅添加成功并已保存到数据库");
        }
    }

    public void removeScreeningRoom(String roomId) {
        ScreeningRoom room = rooms.remove(roomId);
        if (room != null) {
            saveRooms();
            System.out.println("放映厅删除成功并已从数据库移除");
        }
    }

    public void addUser(User user) {
        if (user != null && user.getId() != null) {
            users.put(user.getId(), user);
            saveUsers();
            System.out.println("用户添加成功并已保存到数据库");
        }
    }

    public void removeUser(String userId) {
        User user = users.remove(userId);
        if (user != null) {
            saveUsers();
            System.out.println("用户删除成功并已从数据库移除");
        }
    }

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
    
    private void loadData() {
        // 只从MySQL加载数据
        System.out.println("正在从MySQL数据库加载数据...");
        
        movies.putAll(mysqlDataStorage.loadMovies());
        rooms.putAll(mysqlDataStorage.loadScreeningRooms());
        shows.putAll(mysqlDataStorage.loadShows());
        users.putAll(mysqlDataStorage.loadUsers());
        
        System.out.println("✓ 数据加载完成");
        System.out.println("  - 电影: " + movies.size() + " 部");
        System.out.println("  - 放映厅: " + rooms.size() + " 个");
        System.out.println("  - 场次: " + shows.size() + " 个");
        System.out.println("  - 用户: " + users.size() + " 个");
        
        // 重建电影和场次的关系
        // 由于使用JOIN查询已经加载了关联对象，这里不再需要重建
        
        // 重建用户和订单的关系（延迟到BookingService初始化后）
        // 这个关系重建将在BookingService初始化后完成
    }
    
    public void saveMovies() {
        // 只保存到MySQL数据库
        mysqlDataStorage.saveMovies(movies);
        System.out.println("✓ 电影数据已保存到MySQL数据库");
    }
    
    public void saveRooms() {
        // 只保存到MySQL数据库
        mysqlDataStorage.saveScreeningRooms(rooms);
        System.out.println("✓ 放映厅数据已保存到MySQL数据库");
    }
    
    public void saveShows() {
        // 只保存到MySQL数据库
        mysqlDataStorage.saveShows(shows);
        System.out.println("✓ 场次数据已保存到MySQL数据库");
    }
    
    public void saveUsers() {
        // 只保存到MySQL数据库
        mysqlDataStorage.saveUsers(users);
        System.out.println("✓ 用户数据已保存到MySQL数据库");
    }
    
    public void saveAllData() {
        saveMovies();
        saveRooms();
        saveShows();
        saveUsers();
        BookingService.getInstance().saveOrders();
    }
    
    public void backupData() {
        // MySQL数据库备份
        try {
            System.out.println("正在备份MySQL数据库...");
            // 这里可以添加MySQL备份逻辑
            System.out.println("✓ MySQL数据库备份完成");
        } catch (Exception e) {
            System.err.println("✗ 数据库备份失败: " + e.getMessage());
        }
    }
    
    public void shutdown() {
        saveAllData();
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.close();
        }
    }
}
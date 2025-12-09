package com.cinema.service;

import com.cinema.model.*;
import com.cinema.storage.SimpleDataStorage;
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
    private final SimpleDataStorage dataStorage;
    private final MySQLDataStorage mysqlDataStorage;
    private final boolean useMySQL;

    private CinemaManager() {
        this.dataStorage = new SimpleDataStorage();
        this.movies = new ConcurrentHashMap<>();
        this.rooms = new ConcurrentHashMap<>();
        this.shows = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        
        // 尝试使用MySQL，如果失败则使用文件存储
        boolean mysqlAvailable = false;
        MySQLDataStorage mysqlStorage = null;
        try {
            mysqlStorage = new MySQLDataStorage();
            mysqlAvailable = true;
            System.out.println("使用MySQL数据库存储");
        } catch (Exception e) {
            System.err.println("MySQL连接失败，使用文件存储: " + e.getMessage());
            System.err.println("提示：如需使用MySQL，请下载MySQL Connector/J并添加到classpath");
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
            "13800138000",
            "admin@cinema.com",
            User.UserRole.ADMIN
        );
        users.put(admin.getId(), admin);
    }

    public void addMovie(Movie movie) {
        if (movie != null && movie.getId() != null) {
            movies.put(movie.getId(), movie);
            saveMovies();
        }
    }

    public void removeMovie(String movieId) {
        Movie movie = movies.remove(movieId);
        if (movie != null) {
            // Remove all shows for this movie
            Map<LocalDate, List<Show>> allShows = movie.getAllShows();
            for (List<Show> showList : allShows.values()) {
                for (Show show : showList) {
                    shows.remove(show.getId());
                }
            }
            saveMovies();
            saveShows();
        }
    }

    public void addShow(Show show) {
        if (show != null && show.getId() != null) {
            shows.put(show.getId(), show);
            show.getMovie().addShow(show.getStartTime().toLocalDate(), show);
            saveShows();
            saveMovies();
        }
    }

    public void removeShow(String showId) {
        Show show = shows.remove(showId);
        if (show != null) {
            show.getMovie().removeShow(show.getStartTime().toLocalDate(), show);
            saveShows();
            saveMovies();
        }
    }

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

    public void addUser(User user) {
        if (user != null && user.getId() != null) {
            users.put(user.getId(), user);
            saveUsers();
        }
    }

    public void removeUser(String userId) {
        users.remove(userId);
        saveUsers();
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
                               show.getMovie().getTitle().contains(movieTitle);
            boolean dateMatch = date == null || 
                              show.getStartTime().toLocalDate().equals(date);
            
            if (titleMatch && dateMatch) {
                matchingShows.add(show);
            }
        }
        return matchingShows;
    }
    
    private void loadData() {
        if (useMySQL && mysqlDataStorage != null) {
            movies.putAll(mysqlDataStorage.loadMovies());
            rooms.putAll(mysqlDataStorage.loadScreeningRooms());
            shows.putAll(mysqlDataStorage.loadShows());
            users.putAll(mysqlDataStorage.loadUsers());
        } else {
            movies.putAll(dataStorage.loadMovies());
            rooms.putAll(dataStorage.loadScreeningRooms());
            shows.putAll(dataStorage.loadShows());
            users.putAll(dataStorage.loadUsers());
        }
        
        // 重建电影和场次的关系
        // 注意：从MySQL加载的Show对象暂时没有关联的Movie和ScreeningRoom
        // 这里暂时跳过重建关系，避免空指针异常
        // 实际应用中需要从数据库加载关联关系或使用JOIN查询
        
        // 重建用户和订单的关系（延迟到BookingService初始化后）
        // 这个关系重建将在BookingService初始化后完成
    }
    
    private void saveMovies() {
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.saveMovies(movies);
        } else {
            dataStorage.saveMovies(movies);
        }
    }
    
    private void saveRooms() {
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.saveScreeningRooms(rooms);
        } else {
            dataStorage.saveScreeningRooms(rooms);
        }
    }
    
    private void saveShows() {
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.saveShows(shows);
        } else {
            dataStorage.saveShows(shows);
        }
    }
    
    private void saveUsers() {
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.saveUsers(users);
        } else {
            dataStorage.saveUsers(users);
        }
    }
    
    public void saveAllData() {
        saveMovies();
        saveRooms();
        saveShows();
        saveUsers();
        BookingService.getInstance().saveOrders();
    }
    
    public void backupData() {
        dataStorage.backupData();
    }
    
    public void shutdown() {
        saveAllData();
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.close();
        }
    }
}
package com.cinema.storage;

import com.cinema.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStorage {
    private static final String DATA_DIR = "data";
    private static final String MOVIES_FILE = DATA_DIR + "/movies.json";
    private static final String ROOMS_FILE = DATA_DIR + "/rooms.json";
    private static final String SHOWS_FILE = DATA_DIR + "/shows.json";
    private static final String USERS_FILE = DATA_DIR + "/users.json";
    private static final String ORDERS_FILE = DATA_DIR + "/orders.json";
    
    private final ObjectMapper mapper;
    
    public DataStorage() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        createDataDirectory();
    }
    
    private void createDataDirectory() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        } catch (IOException e) {
            System.err.println("创建数据目录失败: " + e.getMessage());
        }
    }
    
    public void saveMovies(Map<String, Movie> movies) {
        try {
            mapper.writeValue(new File(MOVIES_FILE), new ArrayList<>(movies.values()));
        } catch (IOException e) {
            System.err.println("保存电影数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, Movie> loadMovies() {
        try {
            File file = new File(MOVIES_FILE);
            if (!file.exists()) {
                return new ConcurrentHashMap<>();
            }
            
            List<Movie> movieList = mapper.readValue(file, 
                mapper.getTypeFactory().constructCollectionType(List.class, Movie.class));
            
            Map<String, Movie> movies = new ConcurrentHashMap<>();
            for (Movie movie : movieList) {
                movies.put(movie.getId(), movie);
            }
            return movies;
        } catch (IOException e) {
            System.err.println("加载电影数据失败: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }
    
    public void saveScreeningRooms(Map<String, ScreeningRoom> rooms) {
        try {
            mapper.writeValue(new File(ROOMS_FILE), new ArrayList<>(rooms.values()));
        } catch (IOException e) {
            System.err.println("保存放映厅数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, ScreeningRoom> loadScreeningRooms() {
        try {
            File file = new File(ROOMS_FILE);
            if (!file.exists()) {
                return new ConcurrentHashMap<>();
            }
            
            List<ScreeningRoom> roomList = mapper.readValue(file, 
                mapper.getTypeFactory().constructCollectionType(List.class, ScreeningRoom.class));
            
            Map<String, ScreeningRoom> rooms = new ConcurrentHashMap<>();
            for (ScreeningRoom room : roomList) {
                rooms.put(room.getId(), room);
            }
            return rooms;
        } catch (IOException e) {
            System.err.println("加载放映厅数据失败: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }
    
    public void saveShows(Map<String, Show> shows) {
        try {
            mapper.writeValue(new File(SHOWS_FILE), new ArrayList<>(shows.values()));
        } catch (IOException e) {
            System.err.println("保存场次数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, Show> loadShows() {
        try {
            File file = new File(SHOWS_FILE);
            if (!file.exists()) {
                return new ConcurrentHashMap<>();
            }
            
            List<Show> showList = mapper.readValue(file, 
                mapper.getTypeFactory().constructCollectionType(List.class, Show.class));
            
            Map<String, Show> shows = new ConcurrentHashMap<>();
            for (Show show : showList) {
                shows.put(show.getId(), show);
            }
            return shows;
        } catch (IOException e) {
            System.err.println("加载场次数据失败: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }
    
    public void saveUsers(Map<String, User> users) {
        try {
            mapper.writeValue(new File(USERS_FILE), new ArrayList<>(users.values()));
        } catch (IOException e) {
            System.err.println("保存用户数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, User> loadUsers() {
        try {
            File file = new File(USERS_FILE);
            if (!file.exists()) {
                return new ConcurrentHashMap<>();
            }
            
            List<User> userList = mapper.readValue(file, 
                mapper.getTypeFactory().constructCollectionType(List.class, User.class));
            
            Map<String, User> users = new ConcurrentHashMap<>();
            for (User user : userList) {
                users.put(user.getId(), user);
            }
            return users;
        } catch (IOException e) {
            System.err.println("加载用户数据失败: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }
    
    public void saveOrders(Map<String, Order> orders) {
        try {
            mapper.writeValue(new File(ORDERS_FILE), new ArrayList<>(orders.values()));
        } catch (IOException e) {
            System.err.println("保存订单数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, Order> loadOrders() {
        try {
            File file = new File(ORDERS_FILE);
            if (!file.exists()) {
                return new ConcurrentHashMap<>();
            }
            
            List<Order> orderList = mapper.readValue(file, 
                mapper.getTypeFactory().constructCollectionType(List.class, Order.class));
            
            Map<String, Order> orders = new ConcurrentHashMap<>();
            for (Order order : orderList) {
                orders.put(order.getOrderId(), order);
            }
            return orders;
        } catch (IOException e) {
            System.err.println("加载订单数据失败: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }
    
    public void backupData() {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupDir = DATA_DIR + "/backup_" + timestamp;
        
        try {
            Path backupPath = Paths.get(backupDir);
            Files.createDirectories(backupPath);
            
            // 复制所有数据文件到备份目录
            Files.copy(Paths.get(MOVIES_FILE), backupPath.resolve("movies.json"));
            Files.copy(Paths.get(ROOMS_FILE), backupPath.resolve("rooms.json"));
            Files.copy(Paths.get(SHOWS_FILE), backupPath.resolve("shows.json"));
            Files.copy(Paths.get(USERS_FILE), backupPath.resolve("users.json"));
            Files.copy(Paths.get(ORDERS_FILE), backupPath.resolve("orders.json"));
            
            System.out.println("数据备份完成: " + backupDir);
        } catch (IOException e) {
            System.err.println("数据备份失败: " + e.getMessage());
        }
    }
}
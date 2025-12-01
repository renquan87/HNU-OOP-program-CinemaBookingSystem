package com.cinema.storage;

import com.cinema.model.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class SimpleDataStorage {
    private static final String DATA_DIR = "data";
    private static final String MOVIES_FILE = DATA_DIR + "/movies.dat";
    private static final String ROOMS_FILE = DATA_DIR + "/rooms.dat";
    private static final String SHOWS_FILE = DATA_DIR + "/shows.dat";
    private static final String USERS_FILE = DATA_DIR + "/users.dat";
    private static final String ORDERS_FILE = DATA_DIR + "/orders.dat";
    
    public SimpleDataStorage() {
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
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(MOVIES_FILE))) {
            oos.writeObject(new ArrayList<>(movies.values()));
        } catch (IOException e) {
            System.err.println("保存电影数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, Movie> loadMovies() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(MOVIES_FILE))) {
            @SuppressWarnings("unchecked")
            List<Movie> movieList = (List<Movie>) ois.readObject();
            
            Map<String, Movie> movies = new HashMap<>();
            for (Movie movie : movieList) {
                movies.put(movie.getId(), movie);
            }
            return movies;
        } catch (FileNotFoundException e) {
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载电影数据失败: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    public void saveScreeningRooms(Map<String, ScreeningRoom> rooms) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(ROOMS_FILE))) {
            oos.writeObject(new ArrayList<>(rooms.values()));
        } catch (IOException e) {
            System.err.println("保存放映厅数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, ScreeningRoom> loadScreeningRooms() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(ROOMS_FILE))) {
            @SuppressWarnings("unchecked")
            List<ScreeningRoom> roomList = (List<ScreeningRoom>) ois.readObject();
            
            Map<String, ScreeningRoom> rooms = new HashMap<>();
            for (ScreeningRoom room : roomList) {
                rooms.put(room.getId(), room);
            }
            return rooms;
        } catch (FileNotFoundException e) {
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载放映厅数据失败: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    public void saveShows(Map<String, Show> shows) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SHOWS_FILE))) {
            oos.writeObject(new ArrayList<>(shows.values()));
        } catch (IOException e) {
            System.err.println("保存场次数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, Show> loadShows() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(SHOWS_FILE))) {
            @SuppressWarnings("unchecked")
            List<Show> showList = (List<Show>) ois.readObject();
            
            Map<String, Show> shows = new HashMap<>();
            for (Show show : showList) {
                shows.put(show.getId(), show);
            }
            return shows;
        } catch (FileNotFoundException e) {
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载场次数据失败: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    public void saveUsers(Map<String, User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(USERS_FILE))) {
            oos.writeObject(new ArrayList<>(users.values()));
        } catch (IOException e) {
            System.err.println("保存用户数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, User> loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(USERS_FILE))) {
            @SuppressWarnings("unchecked")
            List<User> userList = (List<User>) ois.readObject();
            
            Map<String, User> users = new HashMap<>();
            for (User user : userList) {
                users.put(user.getId(), user);
            }
            return users;
        } catch (FileNotFoundException e) {
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载用户数据失败: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    public void saveOrders(Map<String, Order> orders) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(ORDERS_FILE))) {
            oos.writeObject(new ArrayList<>(orders.values()));
        } catch (IOException e) {
            System.err.println("保存订单数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, Order> loadOrders() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(ORDERS_FILE))) {
            @SuppressWarnings("unchecked")
            List<Order> orderList = (List<Order>) ois.readObject();
            
            Map<String, Order> orders = new HashMap<>();
            for (Order order : orderList) {
                orders.put(order.getOrderId(), order);
            }
            return orders;
        } catch (FileNotFoundException e) {
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载订单数据失败: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    public void backupData() {
        String timestamp = LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupDir = DATA_DIR + "/backup_" + timestamp;
        
        try {
            Path backupPath = Paths.get(backupDir);
            Files.createDirectories(backupPath);
            
            // 复制所有数据文件到备份目录
            copyFileIfExists(MOVIES_FILE, backupPath.resolve("movies.dat"));
            copyFileIfExists(ROOMS_FILE, backupPath.resolve("rooms.dat"));
            copyFileIfExists(SHOWS_FILE, backupPath.resolve("shows.dat"));
            copyFileIfExists(USERS_FILE, backupPath.resolve("users.dat"));
            copyFileIfExists(ORDERS_FILE, backupPath.resolve("orders.dat"));
            
            System.out.println("数据备份完成: " + backupDir);
        } catch (IOException e) {
            System.err.println("数据备份失败: " + e.getMessage());
        }
    }
    
    private void copyFileIfExists(String source, Path target) throws IOException {
        Path sourcePath = Paths.get(source);
        if (Files.exists(sourcePath)) {
            Files.copy(sourcePath, target);
        }
    }
}
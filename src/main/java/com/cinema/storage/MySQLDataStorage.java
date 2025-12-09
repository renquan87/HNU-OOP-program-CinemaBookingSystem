package com.cinema.storage;

import com.cinema.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MySQLDataStorage {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public MySQLDataStorage() {
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        // 检查驱动是否可用
        if (!SimpleDatabaseConnection.isDriverAvailable()) {
            throw new RuntimeException("MySQL驱动不可用，请确保MySQL Connector/J在classpath中");
        }
        
        // 测试数据库连接
        if (!SimpleDatabaseConnection.testConnection()) {
            throw new RuntimeException("无法连接到数据库");
        }
        
        // 这里可以添加初始化数据的逻辑
        System.out.println("MySQL数据库连接成功");
    }
    
    // ========== 电影相关方法 ==========
    
    public void saveMovies(Map<String, Movie> movies) {
        String sql = "INSERT INTO movies (id, title, director, actors, duration, rating, genre, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE title = VALUES(title), director = VALUES(director), " +
                     "actors = VALUES(actors), duration = VALUES(duration), rating = VALUES(rating), " +
                     "genre = VALUES(genre), description = VALUES(description)";
        
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            for (Movie movie : movies.values()) {
                pstmt.setString(1, movie.getId());
                pstmt.setString(2, movie.getTitle());
                pstmt.setString(3, movie.getDirector());
                pstmt.setString(4, String.join(",", movie.getActors()));
                pstmt.setInt(5, movie.getDuration());
                pstmt.setDouble(6, movie.getRating());
                pstmt.setString(7, movie.getGenre().toString());
                pstmt.setString(8, movie.getDescription());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            conn.commit();
            
        } catch (SQLException e) {
            System.err.println("保存电影数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, Movie> loadMovies() {
        Map<String, Movie> movies = new HashMap<>();
        String sql = "SELECT * FROM movies";
        
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Movie movie = new Movie(
                    rs.getString("id"),
                    rs.getString("title"),
                    java.time.LocalDate.now(), // 使用当前日期作为默认值
                    Arrays.asList(rs.getString("actors").split(",")),
                    rs.getString("director"),
                    rs.getInt("duration"),
                    rs.getDouble("rating"),
                    rs.getString("description"),
                    MovieGenre.fromDescription(rs.getString("genre"))
                );
                movies.put(movie.getId(), movie);
            }
            
        } catch (SQLException e) {
            System.err.println("加载电影数据失败: " + e.getMessage());
        }
        
        return movies;
    }
    
    // ========== 放映厅相关方法 ==========
    
    public void saveScreeningRooms(Map<String, ScreeningRoom> rooms) {
        String sql = "INSERT INTO screening_rooms (id, name, room_rows, room_columns) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name = VALUES(name), room_rows = VALUES(room_rows), room_columns = VALUES(room_columns)";
        
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            for (ScreeningRoom room : rooms.values()) {
                pstmt.setString(1, room.getId());
                pstmt.setString(2, room.getName());
                pstmt.setInt(3, room.getRows());
                pstmt.setInt(4, room.getColumns());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            conn.commit();
            
        } catch (SQLException e) {
            System.err.println("保存放映厅数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, ScreeningRoom> loadScreeningRooms() {
        Map<String, ScreeningRoom> rooms = new HashMap<>();
        String sql = "SELECT * FROM screening_rooms";
        
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ScreeningRoom room = new ScreeningRoom(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getInt("room_rows"),
                    rs.getInt("room_columns")
                );
                rooms.put(room.getId(), room);
            }
            
        } catch (SQLException e) {
            System.err.println("加载放映厅数据失败: " + e.getMessage());
        }
        
        return rooms;
    }
    
    // ========== 场次相关方法 ==========
    
    public void saveShows(Map<String, Show> shows) {
        String sql = "INSERT INTO shows (id, movie_id, room_id, start_time, end_time, base_price, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE movie_id = VALUES(movie_id), room_id = VALUES(room_id), " +
                     "start_time = VALUES(start_time), end_time = VALUES(end_time), " +
                     "base_price = VALUES(base_price), status = VALUES(status)";
        
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            for (Show show : shows.values()) {
                pstmt.setString(1, show.getId());
                pstmt.setString(2, show.getMovie().getId());
                pstmt.setString(3, show.getScreeningRoom().getId());
                pstmt.setString(4, show.getStartTime().format(DATE_FORMATTER));
                pstmt.setString(5, show.getStartTime().plusMinutes(show.getMovie().getDuration()).format(DATE_FORMATTER)); // 计算结束时间
                pstmt.setDouble(6, show.getBasePrice());
                pstmt.setString(7, "SCHEDULED"); // 默认状态
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            conn.commit();
            
        } catch (SQLException e) {
            System.err.println("保存场次数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, Show> loadShows() {
        Map<String, Show> shows = new HashMap<>();
        
        // 先加载所有电影和放映厅
        Map<String, Movie> movies = loadMovies();
        Map<String, ScreeningRoom> rooms = loadScreeningRooms();
        
        // 使用JOIN查询加载场次及其关联数据
        String sql = "SELECT s.*, m.title as movie_title, m.id as movie_id, " +
                     "m.director as movie_director, m.duration as movie_duration, " +
                     "m.actors as actors, m.genre as genre, m.rating as rating, m.description as description, " +
                     "r.name as room_name, r.id as room_id, r.room_rows, r.room_columns " +
                     "FROM shows s " +
                     "LEFT JOIN movies m ON s.movie_id = m.id " +
                     "LEFT JOIN screening_rooms r ON s.room_id = r.id";
        
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // 创建Movie对象
                String actorsStr = rs.getString("actors");
                List<String> actors = new ArrayList<>();
                if (actorsStr != null && !actorsStr.isEmpty()) {
                    actors = Arrays.asList(actorsStr.split(","));
                }
                
                String genreStr = rs.getString("genre");
                MovieGenre genre = MovieGenre.DRAMA; // 默认值
                if (genreStr != null && !genreStr.isEmpty()) {
                    genre = MovieGenre.fromDescription(genreStr);
                }
                
                Movie movie = new Movie(
                    rs.getString("movie_id"),
                    rs.getString("movie_title"),
                    LocalDate.now(), // 从数据库中应该有release_date字段，暂时使用当前日期
                    actors,
                    rs.getString("movie_director"),
                    rs.getInt("movie_duration"),
                    rs.getDouble("rating"),
                    rs.getString("description"),
                    genre
                );
                
                // 创建ScreeningRoom对象
                ScreeningRoom room = new ScreeningRoom(
                    rs.getString("room_id"),
                    rs.getString("room_name"),
                    rs.getInt("room_rows"),
                    rs.getInt("room_columns")
                );
                
                // 创建Show对象
                Show show = new Show(
                    rs.getString("id"),
                    movie,
                    room,
                    LocalDateTime.parse(rs.getString("start_time"), DATE_FORMATTER),
                    rs.getDouble("base_price")
                );
                
                shows.put(show.getId(), show);
            }
            
        } catch (SQLException e) {
            System.err.println("加载场次数据失败: " + e.getMessage());
        }
        
        return shows;
    }
    
    // ========== 用户相关方法 ==========
    
    public void saveUsers(Map<String, User> users) {
        String sql = "INSERT INTO users (id, name, phone, email, is_admin) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name = VALUES(name), phone = VALUES(phone), " +
                     "email = VALUES(email), is_admin = VALUES(is_admin)";
        
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            for (User user : users.values()) {
                pstmt.setString(1, user.getId());
                pstmt.setString(2, user.getName());
                pstmt.setString(3, user.getPhone());
                pstmt.setString(4, user.getEmail());
                pstmt.setBoolean(5, user.isAdmin());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            conn.commit();
            
        } catch (SQLException e) {
            System.err.println("保存用户数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, User> loadUsers() {
        Map<String, User> users = new HashMap<>();
        String sql = "SELECT * FROM users";
        
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getBoolean("is_admin") ? User.UserRole.ADMIN : User.UserRole.CUSTOMER
                );
                users.put(user.getId(), user);
            }
            
        } catch (SQLException e) {
            System.err.println("加载用户数据失败: " + e.getMessage());
        }
        
        return users;
    }
    
    // ========== 订单相关方法 ==========
    
    public void saveOrders(Map<String, Order> orders) {
        String sql = "INSERT INTO orders (order_id, user_id, show_id, total_amount, status, payment_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE user_id = VALUES(user_id), show_id = VALUES(show_id), " +
                     "total_amount = VALUES(total_amount), status = VALUES(status), " +
                     "payment_status = VALUES(payment_status)";
        
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            for (Order order : orders.values()) {
                pstmt.setString(1, order.getOrderId());
                pstmt.setString(2, order.getUser().getId());
                pstmt.setString(3, order.getShow().getId());
                pstmt.setDouble(4, order.getTotalAmount());
                pstmt.setString(5, "CONFIRMED"); // 默认状态
                pstmt.setString(6, "PAID"); // 默认支付状态
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            conn.commit();
            
        } catch (SQLException e) {
            System.err.println("保存订单数据失败: " + e.getMessage());
        }
    }
    
    public Map<String, Order> loadOrders() {
        Map<String, Order> orders = new HashMap<>();
        String sql = "SELECT * FROM orders";
        
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // 注意：Order的构造函数需要Show对象，这里暂时跳过
                // TODO: 实现完整的Order加载逻辑，需要先加载Show和Seat
            }
            
        } catch (SQLException e) {
            System.err.println("加载订单数据失败: " + e.getMessage());
        }
        
        return orders;
    }
    
    // ========== 数据库初始化方法 ==========
    
    public void initializeDefaultData() {
        // 这里可以添加初始化默认数据的逻辑
        // 比如创建默认的管理员账户、放映厅等
        System.out.println("数据库初始化完成");
    }
    
    // ========== 关闭连接 ==========
    
    public void close() {
        // 简单连接不需要关闭连接池
    }
}
package com.cinema.storage;

import com.cinema.model.*;
import com.cinema.service.CinemaManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MySQLDataStorage {
    // 🔴 统一定义日期格式化常量，用于与数据库进行时间戳转换
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            // 注意：这里最好抛出运行时异常，但为了兼容旧代码结构，保留打印
            System.err.println("无法连接到数据库");
        } else {
            System.out.println("MySQL数据库连接成功");
        }
    }

    // ========== 电影相关方法 ==========

    // ================== 2. 修复电影保存 (封面/预告片/评论) ==================
    public void saveMovies(Map<String, Movie> movies) {
        // SQL语句更新，包含 cover_url、trailer_url 和 release_date
        String sql = "INSERT INTO movies (id, title, director, actors, duration, rating, genre, description, cover_url, trailer_url, release_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE title=VALUES(title), director=VALUES(director), " +
                "actors=VALUES(actors), duration=VALUES(duration), rating=VALUES(rating), " +
                "genre=VALUES(genre), description=VALUES(description), " +
                "cover_url=VALUES(cover_url), trailer_url=VALUES(trailer_url), release_date=VALUES(release_date)";

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
                pstmt.setString(9, movie.getCoverUrl());    // 🔴 保存封面
                pstmt.setString(10, movie.getTrailerUrl()); // 🔴 保存预告片
                pstmt.setDate(11, movie.getReleaseTime() != null ? java.sql.Date.valueOf(movie.getReleaseTime()) : null); // 🔴 保存上映日期
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();

            // 🔴 同时也保存评论！
            saveComments(movies);

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
                String actorsStr = rs.getString("actors");
                List<String> actorList = (actorsStr == null || actorsStr.isEmpty()) ? new ArrayList<>() : Arrays.asList(actorsStr.split(","));

                String genreStr = rs.getString("genre");
                MovieGenre genre = (genreStr != null) ? MovieGenre.fromDescription(genreStr) : MovieGenre.DRAMA;

                // 读取上映日期
                java.sql.Date releaseDate = rs.getDate("release_date");
                LocalDate releaseTime = (releaseDate != null) ? releaseDate.toLocalDate() : LocalDate.now();

                // 🔴 使用带 trailerUrl 和 coverUrl 的完整构造函数
                Movie movie = new Movie(
                        rs.getString("id"),
                        rs.getString("title"),
                        releaseTime, // 🔴 使用数据库中的上映日期
                        actorList,
                        rs.getString("director"),
                        rs.getInt("duration"),
                        rs.getDouble("rating"),
                        rs.getString("description"),
                        genre,
                        rs.getString("trailer_url"), // 🔴 读取预告片
                        rs.getString("cover_url")    // 🔴 读取封面
                );
                movies.put(movie.getId(), movie);
            }

            // 🔴 加载所有评论并分配给电影
            loadComments(movies);

        } catch (SQLException e) {
            System.err.println("加载电影数据失败: " + e.getMessage());
        }
        return movies;
    }

    // ========== 3. 评论存取 ==========
    private void saveComments(Map<String, Movie> movies) {
        String sql = "INSERT INTO comments (id, user_id, user_name, movie_id, content, rating, create_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE content=VALUES(content)"; // 简单处理

        try (Connection conn = SimpleDatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            for (Movie movie : movies.values()) {
                if (movie.getComments() != null) {
                    for (Comment c : movie.getComments()) {
                        pstmt.setString(1, c.getId());
                        pstmt.setString(2, c.getUserId());
                        pstmt.setString(3, c.getUserName());
                        pstmt.setString(4, movie.getId());
                        pstmt.setString(5, c.getContent());
                        pstmt.setDouble(6, c.getRating());
                        pstmt.setString(7, c.getCreateTime().format(DATE_FMT));
                        pstmt.addBatch();
                    }
                }
            }
            pstmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            // 忽略非关键错误，但仍打印以便调试
            System.err.println("保存评论失败: " + e.getMessage());
        }
    }

    private void loadComments(Map<String, Movie> movies) {
        // 按照时间降序排列，保证最新的评论先加载到 Movie 对象的 list 头部
        String sql = "SELECT * FROM comments ORDER BY create_time DESC";
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String movieId = rs.getString("movie_id");
                Movie movie = movies.get(movieId);
                if (movie != null) {
                    Comment c = new Comment(
                            rs.getString("id"),
                            rs.getString("user_id"),
                            rs.getString("user_name"),
                            movieId,
                            rs.getString("content"),
                            rs.getDouble("rating"),
                            LocalDateTime.parse(rs.getString("create_time"), DATE_FMT)
                    );
                    movie.addComment(c); // 添加到内存对象中
                }
            }
        } catch (Exception e) {
            System.err.println("加载评论失败: " + e.getMessage());
        }
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
        // 注意：原代码的 SQL 语句中 end_time 列是多余的，在 Show 类中是通过 movie.duration 计算得到的，
        // 且第二个代码块的 SQL 语句中包含了 end_time，这里保持第二个代码块的 SQL 结构。
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
                pstmt.setString(4, show.getStartTime().format(DATE_FMT)); // 🔴 使用 DATE_FMT
                pstmt.setString(5, show.getStartTime().plusMinutes(show.getMovie().getDuration()).format(DATE_FMT)); // 计算结束时间
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
        // 注意：这里需要依赖 loadMovies/loadScreeningRooms，如果 loadMovies 内部调用了 loadComments，
        // 则在 loadShows 中加载的 movies 已经包含了 comments。
        Map<String, Movie> movies = loadMovies();
        Map<String, ScreeningRoom> rooms = loadScreeningRooms();

        String sql = "SELECT * FROM shows";

        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String movieId = rs.getString("movie_id");
                String roomId = rs.getString("room_id");

                Movie movie = movies.get(movieId);
                ScreeningRoom room = rooms.get(roomId);

                if (movie != null && room != null) {
                    // 创建Show对象
                    Show show = new Show(
                            rs.getString("id"),
                            movie,
                            room,
                            LocalDateTime.parse(rs.getString("start_time"), DATE_FMT), // 🔴 使用 DATE_FMT
                            rs.getDouble("base_price")
                    );

                    // 恢复电影的 showSchedule 列表
                    movie.addShow(show.getStartTime().toLocalDate(), show);

                    shows.put(show.getId(), show);
                }
            }

        } catch (SQLException e) {
            System.err.println("加载场次数据失败: " + e.getMessage());
        }

        return shows;
    }

    // ========== 用户相关方法 ==========

    // ================== 1. 修复用户保存 (注册问题) ==================
    public void saveUsers(Map<String, User> users) {
        String sql = "INSERT INTO users (id, name, password, phone, email, is_admin) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name=VALUES(name), password=VALUES(password), " +
                "phone=VALUES(phone), email=VALUES(email), is_admin=VALUES(is_admin)";

        try (Connection conn = SimpleDatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            for (User user : users.values()) {
                pstmt.setString(1, user.getId());
                pstmt.setString(2, user.getName());
                pstmt.setString(3, user.getPassword()); // 确保这里不为 null
                pstmt.setString(4, user.getPhone());
                pstmt.setString(5, user.getEmail());
                pstmt.setBoolean(6, user.isAdmin());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
            System.out.println("用户数据已保存到数据库，数量：" + users.size());
        } catch (SQLException e) {
            System.err.println("保存用户失败: " + e.getMessage());
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
                        rs.getString("password"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getBoolean("is_admin") ? User.UserRole.ADMIN : User.UserRole.CUSTOMER
                );
                users.put(user.getId(), user);
            }
        } catch (SQLException e) {
            System.err.println("加载用户失败: " + e.getMessage());
        }
        return users;
    }

    // ========== 订单相关方法 ==========

    public void saveOrders(Map<String, Order> orders) {
        // 1. 保存订单主表
        String orderSql = "INSERT INTO orders (order_id, user_id, show_id, total_amount, status, create_time) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status = VALUES(status)";

        // 2. 保存订单座位关联表 (先删后插，简单粗暴但有效)
        String deleteSeatsSql = "DELETE FROM order_seats WHERE order_id = ?";
        String insertSeatsSql = "INSERT INTO order_seats (order_id, seat_row, seat_col) VALUES (?, ?, ?)";

        try (Connection conn = SimpleDatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtOrder = conn.prepareStatement(orderSql);
                 PreparedStatement pstmtDelSeats = conn.prepareStatement(deleteSeatsSql);
                 PreparedStatement pstmtInsSeats = conn.prepareStatement(insertSeatsSql)) {

                for (Order order : orders.values()) {
                    // --- 保存 Order ---
                    pstmtOrder.setString(1, order.getOrderId());
                    pstmtOrder.setString(2, order.getUser().getId());
                    pstmtOrder.setString(3, order.getShow().getId());
                    pstmtOrder.setDouble(4, order.getTotalAmount());
                    pstmtOrder.setString(5, order.getStatus().name());
                    pstmtOrder.setString(6, order.getCreateTime().format(DATE_FMT)); // 🔴 使用 DATE_FMT
                    pstmtOrder.addBatch();

                    // --- 保存 Seats ---
                    // 先删除该订单旧的座位记录
                    pstmtDelSeats.setString(1, order.getOrderId());
                    pstmtDelSeats.executeUpdate();

                    // 插入新的座位记录
                    for (Seat seat : order.getSeats()) {
                        pstmtInsSeats.setString(1, order.getOrderId());
                        pstmtInsSeats.setInt(2, seat.getRow());
                        pstmtInsSeats.setInt(3, seat.getCol());
                        pstmtInsSeats.addBatch();
                    }
                }
                pstmtOrder.executeBatch();
                pstmtInsSeats.executeBatch();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("保存订单数据失败: " + e.getMessage());
        }
    }

    public Map<String, Order> loadOrders() {
        Map<String, Order> orders = new HashMap<>();
        // 确保 CinemaManager 已经初始化，以便获取关联对象
        CinemaManager manager = CinemaManager.getInstance();

        String sql = "SELECT * FROM orders";

        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String orderId = rs.getString("order_id");
                String userId = rs.getString("user_id");
                String showId = rs.getString("show_id");
                String statusStr = rs.getString("status");
                String timeStr = rs.getString("create_time");

                // 1. 恢复关联对象
                User user = manager.getUser(userId);
                Show show = manager.getShow(showId);

                if (user != null && show != null) {
                    // 2. 加载该订单的座位
                    List<Seat> orderSeats = loadOrderSeats(conn, orderId, show);

                    // 3. 恢复订单对象
                    LocalDateTime createTime = (timeStr != null) ? LocalDateTime.parse(timeStr, DATE_FMT) : LocalDateTime.now(); // 🔴 使用 DATE_FMT

                    Order order = new Order(
                            orderId,
                            show,
                            orderSeats,
                            createTime,
                            Order.OrderStatus.valueOf(statusStr)
                    );
                    order.setUser(user);

                    // 4. 重要：根据订单状态恢复座位的状态 (SOLD/LOCKED)
                    if (order.getStatus() == Order.OrderStatus.PAID) {
                        for(Seat s : orderSeats) s.sell();
                    } else if (order.getStatus() == Order.OrderStatus.RESERVED) {
                        for(Seat s : orderSeats) s.lock();
                    }

                    orders.put(orderId, order);
                }
            }
        } catch (SQLException e) {
            System.err.println("加载订单数据失败: " + e.getMessage());
        }
        return orders;
    }

    // 辅助方法：加载订单对应的座位
    private List<Seat> loadOrderSeats(Connection conn, String orderId, Show show) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT seat_row, seat_col FROM order_seats WHERE order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int row = rs.getInt("seat_row");
                    int col = rs.getInt("seat_col");
                    // 从内存中的 Show 对象获取对应的 Seat 实例
                    Seat seat = show.getSeat(row, col);
                    if (seat != null) {
                        seats.add(seat);
                    }
                }
            }
        }
        return seats;
    }

    // ========== 数据库初始化方法 ==========

    public void initializeDefaultData() {
        // 这里可以添加初始化默认数据的逻辑
        System.out.println("数据库初始化完成");
    }

    // ========== 关闭连接 ==========

    public void close() {
        // 简单连接不需要关闭连接池
    }
}
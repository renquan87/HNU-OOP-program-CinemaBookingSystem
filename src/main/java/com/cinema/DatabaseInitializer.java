package com.cinema;

import com.cinema.config.DbPasswordResolver;
import com.cinema.storage.SimpleDatabaseConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseInitializer {
    private static String dbPassword;
    
    public static void main(String[] args) {
        try {
            System.out.println("开始初始化数据库...");
            
            // 加载数据库密码：首先尝试从命令行参数获取，然后从配置文件获取
            dbPassword = loadPassword(args);
            
            // 将命令行参数传递给SimpleDatabaseConnection（用于密码加载）
            SimpleDatabaseConnection.setCommandLineArgs(args);
            
            // 先尝试连接到MySQL服务器（不指定数据库）
            String url = "jdbc:mysql://localhost:3306?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, "root", dbPassword)) {
                System.out.println("MySQL服务器连接成功");
                
                // 创建数据库
                try (java.sql.Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE DATABASE IF NOT EXISTS cinema_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                    System.out.println("数据库 cinema_db 创建成功");
                }
            } catch (SQLException e) {
                System.err.println("连接MySQL服务器失败: " + e.getMessage());
                System.err.println("请确保MySQL服务已启动，用户名密码正确");
                return;
            }
            
            // 测试数据库连接
            if (!SimpleDatabaseConnection.testConnection()) {
                System.err.println("无法连接到cinema_db数据库");
                return;
            }
            
            // 读取并执行SQL脚本
            executeSqlScript("schema.sql");
            
            // 🔴 新增：初始化用户数据（管理员和测试用户）
            initializeUserData();
            
            // 🔴 新增：初始化电影数据（带媒体URL）
            initializeMovieData();
            
            // 🔴 新增：初始化放映厅数据
            initializeScreeningRooms();
            
            // 🔴 新增：初始化电影场次数据（每个电影3场）
            initializeShows();
            
            System.out.println("数据库初始化完成！");



            
        } catch (Exception e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 简单连接不需要关闭连接池
        }
    }
    
    private static void executeSqlScript(String scriptFile) {
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 先删除所有表（按依赖关系倒序）
            dropTablesIfExists(stmt);
            
            // 读取SQL脚本文件
            InputStream inputStream = DatabaseInitializer.class.getClassLoader().getResourceAsStream(scriptFile);
            if (inputStream == null) {
                throw new RuntimeException("无法找到SQL脚本文件: " + scriptFile);
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue; // 跳过空行和注释
                }
                sqlBuilder.append(line).append("\n");
                
                // 如果遇到分号，执行SQL语句
                if (line.endsWith(";")) {
                    String sql = sqlBuilder.toString();
                    try {
                        stmt.execute(sql);
                        System.out.println("执行SQL: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                    } catch (Exception e) {
                        System.err.println("SQL执行失败: " + e.getMessage());
                    }
                    sqlBuilder.setLength(0); // 清空StringBuilder
                }
            }
            
            reader.close();
            
        } catch (Exception e) {
            throw new RuntimeException("执行SQL脚本失败", e);
        }
    }
    
    private static void dropTablesIfExists(Statement stmt) throws SQLException {
        // 🔴 修改：删除订单相关表，但保留 users 用户表
        String[] tables = {
            "order_seats",  // 删除订单座位
            "orders",       // 删除订单
            // "users",      // ✅ 不删除用户表，保留用户数据
            "shows",        // 删除放映场次
            "seats",        // 删除座位
            "screening_rooms", // 删除放映厅
            "movies"        // 删除电影（会重新初始化）
        };
        
        for (String table : tables) {
            try {
                stmt.execute("DROP TABLE IF EXISTS " + table);
                System.out.println("删除表: " + table);
            } catch (Exception e) {
                // 忽略删除失败的情况
            }
        }
    }
    
    /**
     * 加载数据库密码
     * 1. 首先尝试从命令行参数获取
     * 2. 如果失败，尝试从config.properties中加载
     */
    private static String loadPassword(String[] args) {
        String password = DbPasswordResolver.fromEnvironment();
        if (password != null) {
            System.out.println("从 DB_PASSWORD 环境变量加载数据库密码");
            return password;
        }

        password = DbPasswordResolver.fromCommandLine(args);
        if (password != null) {
            System.out.println("从命令行参数加载数据库密码");
            return password;
        }

        // 2. 如果失败，尝试从config.properties中加载
        try {
            Properties props = new Properties();
            InputStream input = DatabaseInitializer.class.getClassLoader().getResourceAsStream("config.properties");
            if (input != null) {
                props.load(input);
                password = DbPasswordResolver.fromProperties(props);
                if (password != null) {
                    System.out.println("从config.properties加载数据库密码");
                    return password;
                }
            }
        } catch (Exception e) {
            System.err.println("读取config.properties失败: " + e.getMessage());
        }

        // 如果都失败，使用默认值
        System.err.println("警告: 未找到数据库密码配置，使用默认密码");
        return "123421";
    }

    /**
     * 🔴 新增：初始化电影数据（带媒体URL）
     * 插入包含封面和预告片URL的电影数据
     */
    private static void initializeMovieData() {
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("\n开始初始化电影数据...");
            
            // 插入电影数据
            String[] insertSqls = {
                "INSERT INTO movies (id, title, director, actors, duration, rating, genre, description, cover_url, trailer_url, release_date) " +
                "VALUES ('MOV-001', '阿凡达：水之道', '詹姆斯·卡梅隆', '萨姆·沃辛顿,佐伊·索尔达娜', 192, 9.0, 'ACTION', " +
                "'杰克·萨利与妻子奈蒂莉组建了家庭，他们的孩子也逐渐成长。', '/media/covers/1.jpg', '/media/trailers/1.mp4', '2022-12-16')",
                
                "INSERT INTO movies (id, title, director, actors, duration, rating, genre, description, cover_url, trailer_url, release_date) " +
                "VALUES ('MOV-002', '流浪地球2', '郭帆', '吴京,刘德华,李雪健', 173, 8.3, 'DRAMA', " +
                "'太阳即将毁灭，人类在地球表面建造出巨大的推进器，寻找新的家园。然而宇宙之路危机四伏。', '/media/covers/2.jpg', '/media/trailers/2.mp4', '2023-01-22')",
                
                "INSERT INTO movies (id, title, director, actors, duration, rating, genre, description, cover_url, trailer_url, release_date) " +
                "VALUES ('MOV-003', '满江红', '张艺谋', '沈腾,易烊千玺,张译,雷佳音', 159, 7.9, 'ACTION', " +
                "'南宋绍兴年间，岳飞死后四年，秦桧率兵与金国会谈。会谈前夜，金国使者死在宰相驻地。', '/media/covers/3.jpg', '/media/trailers/3.mp4', '2023-01-22')"
            };
            
            for (String sql : insertSqls) {
                try {
                    stmt.execute(sql);
                    System.out.println("✅ 插入电影数据成功");
                } catch (SQLException e) {
                    System.err.println("⚠️  插入电影数据失败: " + e.getMessage());
                }
            }
            
            System.out.println("电影数据初始化完成！\n");
            
        } catch (Exception e) {
            System.err.println("初始化电影数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔴 新增：初始化用户数据
     * 插入管理员账户和测试用户
     */
    private static void initializeUserData() {
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("\n开始初始化用户数据...");
            
            // 插入用户数据
            String[] insertSqls = {
                "INSERT IGNORE INTO users (id, name, password, is_admin) " +
                "VALUES ('ADMIN-001', 'ADMIN-001', 'admin123', 1)",
                
                "INSERT IGNORE INTO users (id, name, password, is_admin) " +
                "VALUES ('test', 'test', 'test1234', 0)"
            };
            
            for (String sql : insertSqls) {
                try {
                    stmt.execute(sql);
                    System.out.println("✅ 插入用户数据成功");
                } catch (SQLException e) {
                    System.err.println("⚠️  插入用户数据失败: " + e.getMessage());
                }
            }
            
            System.out.println("用户数据初始化完成！\n");
            
        } catch (Exception e) {
            System.err.println("初始化用户数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔴 新增：初始化放映厅数据
     */
    private static void initializeScreeningRooms() {
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("\n开始初始化放映厅数据...");
            
            // 插入放映厅数据
            String[] insertSqls = {
                "INSERT IGNORE INTO screening_rooms (id, name, room_rows, room_columns) " +
                "VALUES ('ROOM-001', '放映厅 A', 10, 12)",
                
                "INSERT IGNORE INTO screening_rooms (id, name, room_rows, room_columns) " +
                "VALUES ('ROOM-002', '放映厅 B', 10, 12)",
                
                "INSERT IGNORE INTO screening_rooms (id, name, room_rows, room_columns) " +
                "VALUES ('ROOM-003', '放映厅 C', 8, 10)"
            };
            
            for (String sql : insertSqls) {
                try {
                    stmt.execute(sql);
                    System.out.println("✅ 插入放映厅数据成功");
                } catch (SQLException e) {
                    System.err.println("⚠️  插入放映厅数据失败: " + e.getMessage());
                }
            }
            
            System.out.println("放映厅数据初始化完成！\n");
            
        } catch (Exception e) {
            System.err.println("初始化放映厅数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔴 新增：初始化电影场次数据
     * 每个电影有3场放映，共9场
     */
    private static void initializeShows() {
        try (Connection conn = SimpleDatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("\n开始初始化电影场次数据...");
            
            // 插入电影场次数据（每个电影3场）
            String[] insertSqls = {
                // MOV-001: 阿凡达 - 4场
                //本场用来测试
                "INSERT IGNORE INTO shows (id, movie_id, room_id, start_time, base_price, status) " +
                "VALUES ('SHOW-001', 'MOV-001', 'ROOM-001', '2025-12-25 10:00:00', 45.0, 'SCHEDULED')",

                "INSERT IGNORE INTO shows (id, movie_id, room_id, start_time, base_price, status) " +
                "VALUES ('SHOW-010', 'MOV-001', 'ROOM-001', '2025-12-26 10:00:00', 45.0, 'SCHEDULED')",
                
                "INSERT IGNORE INTO shows (id, movie_id, room_id, start_time, base_price, status) " +
                "VALUES ('SHOW-002', 'MOV-001', 'ROOM-002', '2025-12-26 14:00:00', 50.0, 'SCHEDULED')",
                
                "INSERT IGNORE INTO shows (id, movie_id, room_id, start_time, base_price, status) " +
                "VALUES ('SHOW-003', 'MOV-001', 'ROOM-003', '2025-12-26 19:00:00', 55.0, 'SCHEDULED')",
                
                // MOV-002: 流浪地球2 - 3场
                "INSERT IGNORE INTO shows (id, movie_id, room_id, start_time, base_price, status) " +
                "VALUES ('SHOW-004', 'MOV-002', 'ROOM-001', '2025-12-27 10:00:00', 48.0, 'SCHEDULED')",
                
                "INSERT IGNORE INTO shows (id, movie_id, room_id, start_time, base_price, status) " +
                "VALUES ('SHOW-005', 'MOV-002', 'ROOM-002', '2025-12-27 14:00:00', 52.0, 'SCHEDULED')",
                
                "INSERT IGNORE INTO shows (id, movie_id, room_id, start_time, base_price, status) " +
                "VALUES ('SHOW-006', 'MOV-002', 'ROOM-003', '2025-12-27 19:00:00', 58.0, 'SCHEDULED')",
                
                // MOV-003: 满江红 - 3场
                "INSERT IGNORE INTO shows (id, movie_id, room_id, start_time, base_price, status) " +
                "VALUES ('SHOW-007', 'MOV-003', 'ROOM-001', '2025-12-28 10:00:00', 46.0, 'SCHEDULED')",
                
                "INSERT IGNORE INTO shows (id, movie_id, room_id, start_time, base_price, status) " +
                "VALUES ('SHOW-008', 'MOV-003', 'ROOM-002', '2025-12-28 14:00:00', 51.0, 'SCHEDULED')",
                
                "INSERT IGNORE INTO shows (id, movie_id, room_id, start_time, base_price, status) " +
                "VALUES ('SHOW-009', 'MOV-003', 'ROOM-003', '2025-12-28 19:00:00', 56.0, 'SCHEDULED')"
            };
            
            for (String sql : insertSqls) {
                try {
                    stmt.execute(sql);
                    System.out.println("✅ 插入电影场次数据成功");
                } catch (SQLException e) {
                    System.err.println("⚠️  插入电影场次数据失败: " + e.getMessage());
                }
            }
            
            System.out.println("电影场次数据初始化完成（9场电影）！\n");
            
        } catch (Exception e) {
            System.err.println("初始化电影场次数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
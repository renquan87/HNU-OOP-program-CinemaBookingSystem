package com.cinema.storage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SimpleDatabaseConnection {
    private static String url;
    private static String username;
    private static String password;
    private static String driver;
    private static boolean driverAvailable = false;
    private static boolean initialized = false;
    
    private static void initialize() {
        if (initialized) return;
        
        try {
            // 加载配置文件
            Properties props = new Properties();
            InputStream input = SimpleDatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties");
            if (input != null) {
                props.load(input);
                url = props.getProperty("db.url");
                username = props.getProperty("db.username");
                password = props.getProperty("db.password");
                driver = props.getProperty("db.driver");
            }
            
            // 如果配置文件不存在或配置不完整，使用默认值
            if (url == null || username == null || password == null || driver == null) {
                System.err.println("数据库配置不完整，将使用默认配置");
                url = "jdbc:mysql://localhost:3306/cinema_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
                username = "root";
                password = "123421";
                driver = "com.mysql.cj.jdbc.Driver";
            }
            
            // 尝试加载驱动
            Class.forName(driver);
            driverAvailable = true;
            System.out.println("MySQL驱动加载成功");
            
        } catch (IOException e) {
            throw new RuntimeException("初始化数据库连接失败", e);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL驱动未找到，将无法使用MySQL数据库");
            driverAvailable = false;
        }
        
        initialized = true;
    }
    
    public static Connection getConnection() throws SQLException {
        initialize();
        if (!driverAvailable) {
            throw new SQLException("MySQL驱动不可用");
        }
        return DriverManager.getConnection(url, username, password);
    }
    
    // 测试连接
    public static boolean testConnection() {
        initialize();
        if (!driverAvailable) {
            return false;
        }
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            return false;
        }
    }
    
    // 检查驱动是否可用
    public static boolean isDriverAvailable() {
        initialize();
        return driverAvailable;
    }
}
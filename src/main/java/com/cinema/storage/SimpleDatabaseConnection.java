package com.cinema.storage;

import com.cinema.config.DbPasswordResolver;
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
    private static String[] commandLineArgs = null;
    
    /**
     * 设置命令行参数（用于密码加载）
     */
    public static void setCommandLineArgs(String[] args) {
        commandLineArgs = args;
    }
    
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
                driver = props.getProperty("db.driver");
                
                // 加载密码：首先尝试从命令行参数获取，然后从配置文件获取
                password = loadPassword(commandLineArgs, props);
            } else {
                // 如果配置文件不存在，使用默认值
                url = "jdbc:mysql://localhost:3306/cinema_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
                username = "root";
                driver = "com.mysql.cj.jdbc.Driver";
                password = loadPassword(commandLineArgs, null);
            }
            
            // 如果配置不完整，使用默认值
            if (url == null || username == null || password == null || driver == null) {
                System.err.println("数据库配置不完整，将使用默认配置");
                if (url == null) {
                    url = "jdbc:mysql://localhost:3306/cinema_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
                }
                if (username == null) {
                    username = "root";
                }
                if (driver == null) {
                    driver = "com.mysql.cj.jdbc.Driver";
                }
                if (password == null) {
                    password = loadPassword(commandLineArgs, null);
                }
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
    
    /**
     * 加载数据库密码
     * 1. 首先尝试从命令行参数获取
     * 2. 如果失败，尝试从config.properties中加载
     */
    private static String loadPassword(String[] args, Properties props) {
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
        if (props != null) {
            password = DbPasswordResolver.fromProperties(props);
            if (password != null) {
                System.out.println("从config.properties加载数据库密码");
                return password;
            }
        }

        // 如果都失败，使用默认值
        System.err.println("警告: 未找到数据库密码配置，使用默认密码");
        return "123421";
    }
}
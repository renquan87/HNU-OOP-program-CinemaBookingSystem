package com.cinema;

import com.cinema.storage.SimpleDatabaseConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void main(String[] args) {
        try {
            System.out.println("开始初始化数据库...");
            
            // 先尝试连接到MySQL服务器（不指定数据库）
            String url = "jdbc:mysql://localhost:3306?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, "root", "123421")) {
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
}
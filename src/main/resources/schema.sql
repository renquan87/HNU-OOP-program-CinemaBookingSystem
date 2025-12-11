-- 创建数据库
CREATE DATABASE IF NOT EXISTS cinema_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cinema_db;

-- 1. 临时关闭外键检查，防止删除表时报错
SET FOREIGN_KEY_CHECKS = 0;

-- 2. 删除旧表（确保完全重置）
DROP TABLE IF EXISTS order_seats;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS shows;
DROP TABLE IF EXISTS seats; -- 这个表在目前的Java逻辑中用不到，可以删掉或保留空表
DROP TABLE IF EXISTS screening_rooms;
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS users;

-- 3. 电影表
CREATE TABLE movies (
                        id VARCHAR(50) PRIMARY KEY,
                        title VARCHAR(100) NOT NULL,
                        director VARCHAR(100) DEFAULT NULL,
                        actors TEXT,
                        duration INT NOT NULL,
                        rating DOUBLE NOT NULL,
                        genre VARCHAR(50) DEFAULT NULL,
                        description TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 放映厅表
CREATE TABLE screening_rooms (
                                 id VARCHAR(50) PRIMARY KEY,
                                 name VARCHAR(100) NOT NULL,
                                 room_rows INT NOT NULL,
                                 room_columns INT NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 场次表 (时间字段改为 VARCHAR 以匹配 Java 的 String 存储)
CREATE TABLE shows (
                       id VARCHAR(50) PRIMARY KEY,
                       movie_id VARCHAR(50) NOT NULL,
                       room_id VARCHAR(50) NOT NULL,
                       start_time VARCHAR(30) NOT NULL,
                       end_time VARCHAR(30) DEFAULT NULL,
                       base_price DOUBLE NOT NULL,
                       status VARCHAR(20) DEFAULT 'SCHEDULED',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
                       FOREIGN KEY (room_id) REFERENCES screening_rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 用户表 (【关键修改】增加了 password 列)
CREATE TABLE users (
                       id VARCHAR(50) PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       password VARCHAR(100) NOT NULL DEFAULT 'User@123', -- 新增：默认密码
                       phone VARCHAR(20) DEFAULT NULL,
                       email VARCHAR(100) DEFAULT NULL,
                       is_admin TINYINT(1) DEFAULT 0,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 订单表 (【关键修改】增加了 create_time 匹配 Java)
CREATE TABLE orders (
                        order_id VARCHAR(50) PRIMARY KEY,
                        user_id VARCHAR(50) NOT NULL,
                        show_id VARCHAR(50) NOT NULL,
                        total_amount DOUBLE NOT NULL,
                        status VARCHAR(20) DEFAULT 'PENDING',
                        payment_status VARCHAR(20) DEFAULT 'UNPAID',
                        create_time VARCHAR(30) DEFAULT NULL, -- 新增：匹配 Java 的 createTime 字段
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 订单座位关联表 (【关键修改】改为使用行和列定位)
CREATE TABLE order_seats (
                             order_id VARCHAR(50) NOT NULL,
                             seat_row INT NOT NULL, -- 改为行号
                             seat_col INT NOT NULL, -- 改为列号
                             KEY order_id (order_id),
                             FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;
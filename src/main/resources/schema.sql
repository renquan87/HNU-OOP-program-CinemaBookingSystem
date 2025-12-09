-- 创建数据库
CREATE DATABASE IF NOT EXISTS cinema_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cinema_db;

-- 电影表
CREATE TABLE IF NOT EXISTS movies (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    director VARCHAR(100) NOT NULL,
    actors TEXT,
    duration INT NOT NULL,
    rating DOUBLE NOT NULL,
    genre VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 放映厅表
CREATE TABLE IF NOT EXISTS screening_rooms (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    room_rows INT NOT NULL,
    room_columns INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 座位表
CREATE TABLE IF NOT EXISTS seats (
    id VARCHAR(50) PRIMARY KEY,
    room_id VARCHAR(50) NOT NULL,
    row_number INT NOT NULL,
    column_number INT NOT NULL,
    seat_type VARCHAR(20) NOT NULL, -- 'REGULAR', 'VIP', 'DISCOUNT'
    price_multiplier DOUBLE NOT NULL DEFAULT 1.0,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES screening_rooms(id) ON DELETE CASCADE
);

-- 场次表
CREATE TABLE IF NOT EXISTS shows (
    id VARCHAR(50) PRIMARY KEY,
    movie_id VARCHAR(50) NOT NULL,
    room_id VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    base_price DOUBLE NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED', -- 'SCHEDULED', 'CANCELLED', 'COMPLETED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES screening_rooms(id) ON DELETE CASCADE
);

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    show_id VARCHAR(50) NOT NULL,
    total_amount DOUBLE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'
    payment_status VARCHAR(20) DEFAULT 'UNPAID', -- 'UNPAID', 'PAID', 'REFUNDED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE
);

-- 订单座位关联表
CREATE TABLE IF NOT EXISTS order_seats (
    id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    seat_id VARCHAR(50) NOT NULL,
    price DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_movies_title ON movies(title);
CREATE INDEX idx_shows_movie_id ON shows(movie_id);
CREATE INDEX idx_shows_room_id ON shows(room_id);
CREATE INDEX idx_shows_start_time ON shows(start_time);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_show_id ON orders(show_id);
CREATE INDEX idx_seats_room_id ON seats(room_id);
CREATE INDEX idx_order_seats_order_id ON order_seats(order_id);
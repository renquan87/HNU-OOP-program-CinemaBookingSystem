# MySQL数据库详解

## 数据库配置

配置文件位置：`src/main/resources/config.properties`
```properties
# 数据库连接配置
db.url=jdbc:mysql://localhost:3306/cinema_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
# db.password 保持空白，避免将凭据提交到仓库
# 启动脚本通过 DB_PASSWORD 环境变量提供密码
db.driver=com.mysql.cj.jdbc.Driver

# 连接池配置
db.pool.maximumPoolSize=10
db.pool.minimumIdle=5
db.pool.connectionTimeout=30000
db.pool.idleTimeout=600000
db.pool.maxLifetime=1800000
```

**配置说明**：
- `useUnicode=true&characterEncoding=utf8`: 支持中文存储
- `useSSL=false`: 禁用SSL（开发环境）
- `serverTimezone=UTC`: 设置时区
- `allowPublicKeyRetrieval=true`: MySQL 8.x必需参数
- `db.password` 保持为空，启动脚本通过 `DB_PASSWORD` 环境变量提供密码，避免将凭据提交到仓库

## 数据库表结构

### movies表 - 电影信息
```sql
CREATE TABLE movies (
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
```

### screening_rooms表 - 放映厅
```sql
CREATE TABLE screening_rooms (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    room_rows INT NOT NULL,
    room_columns INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**注意**：`rows`是MySQL保留字，因此使用`room_rows`

### seats表 - 座位信息
```sql
CREATE TABLE seats (
    id VARCHAR(50) PRIMARY KEY,
    room_id VARCHAR(50) NOT NULL,
    seat_row INT NOT NULL,
    seat_column INT NOT NULL,
    seat_type VARCHAR(20) NOT NULL,
    price_multiplier DOUBLE NOT NULL DEFAULT 1.0,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES screening_rooms(id) ON DELETE CASCADE
);
```

### shows表 - 场次信息
```sql
CREATE TABLE shows (
    id VARCHAR(50) PRIMARY KEY,
    movie_id VARCHAR(50) NOT NULL,
    room_id VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    base_price DOUBLE NOT NULL,
    discount_price DOUBLE NOT NULL,
    vip_price DOUBLE NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (room_id) REFERENCES screening_rooms(id)
);
```

### users表 - 用户信息
```sql
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER'
);
```

### order_seats表 - 订单座位关联表
```sql
CREATE TABLE order_seats (
    id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    seat_id VARCHAR(50) NOT NULL,
    price DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);
```

### orders表 - 订单信息
```sql
CREATE TABLE orders (
    order_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    show_id VARCHAR(50) NOT NULL,
    total_amount DOUBLE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    create_time VARCHAR(30) DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (show_id) REFERENCES shows(id)
);
```

## MySQL常用操作指南

### 数据库初始化
```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 创建数据库
CREATE DATABASE cinema_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. 使用数据库
USE cinema_db;

# 4. 执行建表脚本
SOURCE src/main/resources/schema.sql;
```

### 常用查询命令
```sql
-- 查看所有表
SHOW TABLES;

-- 查看表结构
DESC movies;

-- 查看所有电影
SELECT * FROM movies;

-- 查询指定电影的场次
SELECT s.*, m.title FROM shows s 
JOIN movies m ON s.movie_id = m.id 
WHERE m.title = '流浪地球2';

-- 查询某日期的场次
SELECT * FROM shows 
WHERE DATE(start_time) = '2025-12-11';

-- 统计每个电影的场次数量
SELECT m.title, COUNT(s.id) as show_count 
FROM movies m 
LEFT JOIN shows s ON m.id = s.movie_id 
GROUP BY m.id;
```

### 数据导入导出
```bash
# 导出整个数据库
mysqldump -u root -p cinema_db > cinema_db_backup.sql

# 导入数据库
mysql -u root -p cinema_db < cinema_db_backup.sql

# 导出特定表
mysqldump -u root -p cinema_db movies > movies_backup.sql
```

### 用户权限管理
```sql
-- 创建新用户
CREATE USER 'cinema_user'@'localhost' IDENTIFIED BY 'password';

-- 授予权限
GRANT ALL PRIVILEGES ON cinema_db.* TO 'cinema_user'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 查看用户权限
SHOW GRANTS FOR 'cinema_user'@'localhost';
```

## MySQL性能优化建议

### 索引优化
```sql
-- 为常用查询字段添加索引
CREATE INDEX idx_movie_title ON movies(title);
CREATE INDEX idx_show_start_time ON shows(start_time);
CREATE INDEX idx_show_movie_id ON shows(movie_id);
CREATE INDEX idx_order_user_id ON orders(user_id);
```

### 查询优化
- 使用JOIN而不是子查询
- 避免SELECT *，只查询需要的字段
- 使用LIMIT限制返回结果数量
- 合理使用索引
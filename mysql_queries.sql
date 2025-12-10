-- ========================================
-- 电影院购票系统 - MySQL常用查询
-- ========================================

-- 1. 查看所有表
SHOW TABLES;

-- 2. 查看电影信息
SELECT * FROM movies;

-- 3. 查看放映厅信息
SELECT * FROM screening_rooms;

-- 4. 查看场次信息（包含关联的电影和放映厅）
SELECT 
    s.id as 场次ID,
    m.title as 电影名称,
    r.name as 放映厅,
    s.start_time as 开始时间,
    s.end_time as 结束时间,
    s.base_price as 基础价格,
    s.status as 状态
FROM shows s
LEFT JOIN movies m ON s.movie_id = m.id
LEFT JOIN screening_rooms r ON s.room_id = r.id
ORDER BY s.start_time;

-- 5. 查看用户信息
SELECT 
    id as 用户ID,
    name as 姓名,
    phone as 电话,
    email as 邮箱,
    CASE 
        WHEN is_admin = 1 THEN '管理员'
        ELSE '普通用户'
    END as 角色
FROM users;

-- 6. 查看订单信息
SELECT 
    o.order_id as 订单ID,
    u.name as 用户名,
    m.title as 电影名称,
    r.name as 放映厅,
    s.start_time as 场次时间,
    o.total_amount as 总金额,
    o.status as 订单状态,
    o.payment_status as 支付状态,
    o.created_at as 创建时间
FROM orders o
LEFT JOIN users u ON o.user_id = u.id
LEFT JOIN shows s ON o.show_id = s.id
LEFT JOIN movies m ON s.movie_id = m.id
LEFT JOIN screening_rooms r ON s.room_id = r.id
ORDER BY o.created_at DESC;

-- 7. 统计信息
-- 电影总数
SELECT COUNT(*) as 电影总数 FROM movies;

-- 放映厅总数
SELECT COUNT(*) as 放映厅总数 FROM screening_rooms;

-- 场次总数
SELECT COUNT(*) as 场次总数 FROM shows;

-- 用户总数
SELECT 
    COUNT(*) as 总用户数,
    SUM(CASE WHEN is_admin = 1 THEN 1 ELSE 0 END) as 管理员数,
    SUM(CASE WHEN is_admin = 0 THEN 1 ELSE 0 END) as 普通用户数
FROM users;

-- 订单统计
SELECT 
    COUNT(*) as 总订单数,
    SUM(CASE WHEN payment_status = 'PAID' THEN 1 ELSE 0 END) as 已支付订单,
    SUM(total_amount) as 总收入
FROM orders;

-- 8. 今日场次
SELECT 
    m.title as 电影名称,
    r.name as 放映厅,
    s.start_time as 开始时间,
    s.base_price as 价格,
    s.status as 状态
FROM shows s
LEFT JOIN movies m ON s.movie_id = m.id
LEFT JOIN screening_rooms r ON s.room_id = r.id
WHERE DATE(s.start_time) = CURDATE()
ORDER BY s.start_time;

-- 9. 查看座位布局
SELECT 
    sr.name as 放映厅,
    se.seat_row as 行,
    se.seat_column as 列,
    se.seat_type as 座位类型,
    CASE 
        WHEN se.is_available = 1 THEN '可用'
        ELSE '不可用'
    END as 状态
FROM seats se
LEFT JOIN screening_rooms sr ON se.room_id = sr.id
ORDER BY sr.name, se.seat_row, se.seat_column;

-- 10. 热门电影（按场次数量排序）
SELECT 
    m.title as 电影名称,
    m.director as 导演,
    COUNT(s.id) as 场次数
FROM movies m
LEFT JOIN shows s ON m.id = s.movie_id
GROUP BY m.id, m.title, m.director
ORDER BY COUNT(s.id) DESC;

-- 11. 票房统计（按电影）
SELECT 
    m.title as 电影名称,
    COUNT(DISTINCT o.order_id) as 订单数,
    SUM(o.total_amount) as 票房收入
FROM movies m
LEFT JOIN shows s ON m.id = s.movie_id
LEFT JOIN orders o ON s.id = o.show_id AND o.payment_status = 'PAID'
GROUP BY m.id, m.title
ORDER BY SUM(o.total_amount) DESC;

-- 12. 查看特定用户的订单
-- 将 'USER-ID' 替换为实际的用户ID
SELECT 
    o.order_id as 订单ID,
    m.title as 电影名称,
    s.start_time as 场次时间,
    r.name as 放映厅,
    o.total_amount as 金额,
    o.status as 订单状态,
    o.created_at as 创建时间
FROM orders o
LEFT JOIN shows s ON o.show_id = s.id
LEFT JOIN movies m ON s.movie_id = m.id
LEFT JOIN screening_rooms r ON s.room_id = r.id
WHERE o.user_id = 'USER-ID'
ORDER BY o.created_at DESC;
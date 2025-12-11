# 电影院购票系统 - 详细开发文档

## 项目概述

本项目是一个基于Java 21开发的电影院购票管理系统，采用面向对象设计模式，实现了完整的电影院业务流程。系统支持MySQL数据库和文件存储双模式（暂未测试双模式），具备用户购票、管理员管理、座位选择、订单处理等核心功能。

### 系统特色

1. **双模式存储架构**：自动检测MySQL可用性，优先使用数据库，不可用时回退到文件存储
2. **完整的业务流程**：从电影管理到座位选择、订单支付的全流程支持
3. **灵活的定价策略**：支持标准定价和高级定价策略，可根据时段、座位类型动态调价
4. **安全的座位管理**：支持座位锁定、并发访问控制
5. **完善的异常处理**：自定义异常体系，确保系统稳定性

## 系统架构详解

### 1. 核心实体层 (model包)

#### Movie类 - 电影实体
```java
// 核心属性
- id: 电影ID (唯一标识)
- title: 电影标题
- director: 导演
- actors: 主演列表
- duration: 时长(分钟)
- rating: 评分
- genre: 电影类型(枚举)
- description: 简介
- releaseTime: 上映日期
- showSchedule: 场次安排(Map<日期, List<场次>>)
```

**关键方法**：
- `addShow(LocalDate, Show)`: 添加场次到电影日程
- `getShowsByDate(LocalDate)`: 获取指定日期的所有场次
- `removeShow(LocalDate, Show)`: 从日程中移除场次

#### ScreeningRoom类 - 放映厅实体
```java
// 核心属性
- id: 放映厅ID
- name: 放映厅名称
- rows: 行数
- columns: 列数
- seatLayout: 座位布局(二维数组)
```

**座位布局管理**：
- 使用`Seat[][] seatLayout`维护座位矩阵
- 支持三种座位类型：普通座、VIP座、折扣座
- 座位ID格式：`行-列`（如：3-5）

#### Show类 - 场次实体
```java
// 核心属性
- id: 场次ID
- movie: 关联电影
- screeningRoom: 关联放映厅
- startTime: 开始时间
- basePrice: 基础价格
- discountPrice: 折扣座价格
- vipPrice: VIP座价格
- seats: 座位列表
```

**安全访问方法**（防止空指针异常）：
- `getMovieTitle()`: 安全获取电影标题
- `getScreeningRoomName()`: 安全获取放映厅名称
- `getMovieId()`: 安全获取电影ID
- `getScreeningRoomId()`: 安全获取放映厅ID

#### Seat类及其子类 - 座位体系
```
Seat (抽象类)
├── RegularSeat (普通座位)
├── VIPSeat (VIP座位)
└── DiscountSeat (折扣座位)
```

**座位状态枚举**：
- `AVAILABLE`: 可用
- `SOLD`: 已售
- `LOCKED`: 锁定（预订中）
- `MAINTENANCE`: 维护中

#### User类 - 用户实体
```java
// 核心属性
- id: 用户ID
- name: 姓名
- phone: 电话
- email: 邮箱
- role: 用户角色(枚举)
```

**用户角色**：
- `ADMIN`: 管理员
- `CUSTOMER`: 普通用户

#### Order类 - 订单实体
```java
// 核心属性
- orderId: 订单ID
- user: 下单用户
- show: 关联场次
- seatIds: 座位ID列表
- totalAmount: 总金额
- status: 订单状态
- paymentStatus: 支付状态
- orderTime: 下单时间
- lockTime: 锁定时间
```

### 2. 策略模式层 (strategy包)

#### PricingStrategy接口
```java
public interface PricingStrategy {
    double calculatePrice(Seat seat, Show show);
}
```

#### StandardPricing - 标准定价策略
- 周末票价上浮20%
- 晚间时段（18:00后）票价上浮15%
- VIP座位在基础价格上加10元
- 折扣座位为基础价格的80%

#### PremiumPricing - 高级定价策略
- VIP座位为基础价格的2倍
- 周末票价上浮30%
- 晚间时段票价上浮25%
- 黄金时段（19:00-21:00）额外上浮10%
- 折扣座位为基础价格的70%

### 3. 服务层 (service包)

#### CinemaManager - 影院管理服务（单例模式）
```java
// 核心功能
- 电影管理：增删改查
- 放映厅管理：增删改查
- 场次管理：增删改查
- 用户管理：增删改查
- 数据持久化：自动保存/加载
```

**关键方法**：
- `searchShows(String movieTitle, LocalDate date)`: 场次查询
- `getShowsByMovie(String movieId)`: 获取电影的所有场次
- `getShowsByDate(LocalDate date)`: 获取日期的所有场次
- `saveAllData()`: 保存所有数据
- `shutdown()`: 系统关闭时的清理工作

#### BookingService - 预订服务（单例模式）
```java
// 核心功能
- 订单创建：createOrder()
- 座位预订：reserveOrder()
- 支付处理：processPayment()
- 订单取消：cancelOrder()
- 退票处理：refundOrder()
- 过期订单检查：checkExpiredOrders()
```

**座位锁定机制**：
- 预订时锁定座位15分钟
- 超时自动释放锁定的座位
- 支持并发访问控制

### 4. 数据存储层 (storage包)

#### SimpleDataStorage - 文件存储实现
- 使用Java原生序列化
- 数据文件存储在`data/`目录
- 支持系统重启后数据恢复

#### MySQLDataStorage - MySQL数据库实现
- 使用JDBC连接MySQL
- 支持事务处理
- 实现关系数据的JOIN查询

**关键SQL查询示例**：
```sql
-- 场次查询(包含关联的电影和放映厅信息)
SELECT s.*, m.title as movie_title, m.id as movie_id, 
       m.director as movie_director, m.duration as movie_duration, 
       m.actors as actors, m.genre as genre, m.rating as rating, m.description as description,
       r.name as room_name, r.id as room_id, r.room_rows, r.room_columns 
FROM shows s 
LEFT JOIN movies m ON s.movie_id = m.id 
LEFT JOIN screening_rooms r ON s.room_id = r.id
```

#### SimpleDatabaseConnection - 数据库连接管理
- 自动检测MySQL驱动可用性
- 管理数据库连接池
- 处理连接异常

### 5. 异常处理层 (exception包)

- **InvalidBookingException**: 无效预订异常
  - 包含预订详情和错误原因
  - 用于座位不可用、时间冲突等场景
  
- **PaymentFailedException**: 支付失败异常
  - 包含支付失败原因
  - 用于支付流程异常处理
  
- **SeatNotAvailableException**: 座位不可用异常
  - 包含座位ID和不可用原因
  - 用于座位状态检查

### 6. 用户界面层 (ui包)

#### ConsoleUI - 控制台界面
- 彩色输出支持（ANSI颜色代码）
- 中文字符宽度计算
- 美化的菜单和表格显示
- 安全的输入处理（解决PowerShell兼容性问题）

**输入处理方法**：
```java
private String readLine() {
    try {
        if (System.console() != null) {
            String input = System.console().readLine();
            return input != null ? input.trim() : "";
        } else {
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                return input != null ? input.trim() : "";
            } else {
                return "";
            }
        }
    } catch (Exception e) {
        return "";
    }
}
```

## MySQL数据库详解

### 1. 数据库配置

配置文件位置：`src/main/resources/config.properties`
```properties
# 数据库连接配置
db.url=jdbc:mysql://localhost:3306/cinema_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=123421
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

### 2. 数据库表结构

#### movies表 - 电影信息
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

#### screening_rooms表 - 放映厅
```sql
CREATE TABLE screening_rooms (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    room_rows INT NOT NULL,
    room_columns INT NOT NULL
);
```

**注意**：`rows`是MySQL保留字，因此使用`room_rows`

#### shows表 - 场次信息
```sql
CREATE TABLE shows (
    id VARCHAR(50) PRIMARY KEY,
    movie_id VARCHAR(50) NOT NULL,
    room_id VARCHAR(50) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    base_price DOUBLE NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (room_id) REFERENCES screening_rooms(id)
);
```

#### users表 - 用户信息
```sql
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE
);
```

#### orders表 - 订单信息
```sql
CREATE TABLE orders (
    order_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    show_id VARCHAR(50) NOT NULL,
    total_amount DOUBLE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    payment_status VARCHAR(20) DEFAULT 'UNPAID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (show_id) REFERENCES shows(id)
);
```

### 3. MySQL常用操作指南

#### 数据库初始化
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

#### 常用查询命令
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

#### 数据导入导出
```bash
# 导出整个数据库
mysqldump -u root -p cinema_db > cinema_db_backup.sql

# 导入数据库
mysql -u root -p cinema_db < cinema_db_backup.sql

# 导出特定表
mysqldump -u root -p cinema_db movies > movies_backup.sql
```

#### 用户权限管理
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

### 4. MySQL性能优化建议

#### 索引优化
```sql
-- 为常用查询字段添加索引
CREATE INDEX idx_movie_title ON movies(title);
CREATE INDEX idx_show_start_time ON shows(start_time);
CREATE INDEX idx_show_movie_id ON shows(movie_id);
CREATE INDEX idx_order_user_id ON orders(user_id);
```

#### 查询优化
- 使用JOIN而不是子查询
- 避免SELECT *，只查询需要的字段
- 使用LIMIT限制返回结果数量
- 合理使用索引

## 项目结构与依赖

### Maven依赖管理 (pom.xml)
```xml
<dependencies>
    <!-- MySQL连接器 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- JUnit测试框架 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.9.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 目录结构详解
```
CinemaBookingSystem/
├── src/
│   ├── main/
│   │   ├── java/com/cinema/
│   │   │   ├── Main.java                 # 程序入口
│   │   │   ├── DatabaseInitializer.java  # 数据库初始化工具
│   │   │   ├── exception/                # 异常类定义
│   │   │   ├── model/                    # 实体类
│   │   │   ├── service/                  # 业务服务层
│   │   │   ├── storage/                  # 数据存储层
│   │   │   ├── strategy/                 # 策略模式实现
│   │   │   └── ui/                       # 用户界面层
│   │   └── resources/                    # 资源文件
│   │       ├── config.properties         # 数据库配置
│   │       └── schema.sql                # 数据库建表脚本
│   └── test/java/com/cinema/             # 测试代码
├── data/                                 # 文件存储目录
│   ├── movies.dat                        # 电影数据文件
│   ├── orders.dat                        # 订单数据文件
│   ├── rooms.dat                         # 放映厅数据文件
│   ├── shows.dat                         # 场次数据文件
│   └── users.dat                         # 用户数据文件
├── lib/                                  # 依赖库目录
├── target/                               # Maven构建输出
├── pom.xml                               # Maven配置文件
├── run.bat                               # Windows运行脚本
└── README.md                             # 项目文档
```

## 开发指南
### *一步到位*
```shell
mvn dependency:copy-dependencies -DoutputDirectory=lib
mvn clean compile
java -cp "lib/*;target/classes" com.cinema.DatabaseInitializer
java -cp "lib/*;target/classes" com.cinema.Main
```
我用的是vscode，集成终端。
IDEA可能有终端中文乱码问题，可搜教程解决。
用MySQL Workbench或者Command Line可查看数据库存储情况。
### 1. 环境搭建

#### 安装Java 21
```bash
# Windows: 下载并安装Oracle JDK 21
# 设置环境变量JAVA_HOME
# 验证安装
java -version
javac -version
```

#### 安装MySQL 8.0
```bash
# Windows: 下载MySQL Installer
# 安装MySQL Server 8.0
# 配置root密码（后续需在项目相关位置将密码修改成你自己的）
# 启动MySQL服务
```

#### 安装Maven
```bash
# Windows: 下载Apache Maven
# 解压到指定目录
# 设置环境变量MAVEN_HOME和PATH
# 验证安装
mvn -version
```

### 2. 项目初始化


#### 克隆项目
```bash
git clone https://github.com/renquan87/HNU-OOP-program2-CinemaBookingSystem.git
cd CinemaBookingSystem
```

#### 下载依赖
```bash
# 下载所有依赖到lib目录
mvn dependency:copy-dependencies -DoutputDirectory=lib
```

#### 编译项目
```bash
mvn clean compile
```

#### 初始化数据库
```bash
# 创建数据库表
java -cp "lib/*;target/classes" com.cinema.DatabaseInitializer
```

### 3. 运行项目

#### 方式1：使用批处理文件（推荐）
```bash
# Windows
.\run.bat
```

#### 方式2：命令行运行
```bash
# 使用MySQL数据库
java -cp "lib/*;target/classes" com.cinema.Main

# 仅使用文件存储
java -cp "target/classes" com.cinema.Main
```

#### 方式3：Maven运行
```bash
mvn exec:java -Dexec.mainClass="com.cinema.Main"
```

### 4. 测试指南(未测试测试指南)

#### 运行单元测试
```bash
mvn test
```

#### 运行特定测试类
```bash
mvn test -Dtest=CinemaManagerTest
```

#### 测试覆盖率
```bash
mvn jacoco:report
```

### 5. 代码规范

#### 命名规范
- 类名：PascalCase（如：CinemaManager）
- 方法名：camelCase（如：searchShows）
- 常量：UPPER_SNAKE_CASE（如：DEFAULT_PRICE）
- 包名：lowercase（如：com.cinema.service）

#### 注释规范
- 类级别：说明类的职责和用法
- 方法级别：说明方法的功能、参数和返回值
- 复杂逻辑：解释实现思路

#### 异常处理
- 使用自定义异常
- 异常信息要明确
- 避免捕获Exception而不处理

### 6. 调试技巧

#### 日志输出
```java
// 使用System.err输出错误信息
System.err.println("错误信息");

// 使用System.out输出调试信息
System.out.println("调试信息");
```

#### 数据库调试
```sql
-- 开启MySQL日志
SET GLOBAL general_log = 'ON';
SET GLOBAL general_log_file = '/path/to/mysql.log';

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
```

## 业务流程详解

### 1. 用户购票流程

```
1. 用户登录
   ├── 输入用户ID
   ├── 验证用户存在
   └── 验证角色权限

2. 浏览电影
   ├── 显示所有电影列表
   ├── 查看电影详细信息
   └── 选择感兴趣的电影

3. 查询场次
   ├── 输入筛选条件（电影名称、日期）
   └── 显示符合条件的场次列表

4. 选择场次
   ├── 查看场次详情
   ├── 查看座位图
   └── 选择座位

5. 确认订单
   ├── 显示订单信息
   ├── 选择支付方式（立即支付/预订）
   └── 确认支付

6. 完成购票
   ├── 支付成功
   ├── 生成订单
   └── 显示订单详情
```

### 2. 管理员管理流程

```
1. 管理员登录
   ├── 使用管理员账号登录
   └── 验证管理员权限

2. 电影管理
   ├── 添加新电影
   ├── 编辑电影信息
   └── 删除电影

3. 放映厅管理
   ├── 添加新放映厅
   ├── 设置座位布局
   └── 删除放映厅

4. 场次管理
   ├── 创建新场次
   ├── 设置票价
   └── 删除场次

5. 统计查看
   ├── 查看票房统计
   ├── 查看用户统计
   └── 查看座位使用率
```

### 3. 数据存储流程

```
1. 数据加载（系统启动）
   ├── 检测MySQL可用性
   ├── MySQL可用：从数据库加载
   └── MySQL不可用：从文件加载

2. 数据保存（实时）
   ├── 数据变更时立即保存
   ├── MySQL模式：保存到数据库
   └── 文件模式：保存到文件

3. 数据备份（管理员功能）
   ├── 导出数据库数据
   ├── 保存备份文件
   └── 恢复备份数据
```

## 扩展开发指南

### 1. 添加新的定价策略

#### 步骤1：实现PricingStrategy接口
```java
public class StudentPricing implements PricingStrategy {
    @Override
    public double calculatePrice(Seat seat, Show show) {
        double price = seat.getBasePrice();
        // 学生票5折优惠
        return price * 0.5;
    }
}
```

#### 步骤2：在BookingService中注册
```java
// 添加策略选择菜单
private void selectPricingStrategy() {
    System.out.println("选择定价策略：");
    System.out.println("1. 标准定价");
    System.out.println("2. 高级定价");
    System.out.println("3. 学生定价");
    // 处理用户选择
}
```

### 2. 添加新的座位类型

#### 步骤1：继承Seat类
```java
public class CoupleSeat extends Seat {
    public CoupleSeat(int row, int col, double basePrice) {
        super(row, col, basePrice);
        // 情侣座特有属性
    }
    
    @Override
    public String getType() {
        return "情侣座";
    }
}
```

#### 步骤2：在放映厅初始化时添加
```java
// 在ScreeningRoom的initializeSeats方法中添加
if (isCoupleSeatArea(row, col)) {
    seatLayout[row][col] = new CoupleSeat(row, col, basePrice * 1.5);
}
```

### 3. 集成第三方支付

#### 步骤1：创建支付接口
```java
public interface PaymentGateway {
    PaymentResult processPayment(Order order, PaymentMethod method);
    RefundResult processRefund(Order order);
}
```

#### 步骤2：实现具体支付方式
```java
public class AlipayGateway implements PaymentGateway {
    @Override
    public PaymentResult processPayment(Order order, PaymentMethod method) {
        // 调用支付宝API
        return new PaymentResult(true, "支付成功");
    }
}
```

### 4. 开发Web界面

#### 技术选型建议
- 前端：React/Vue.js + Bootstrap
- 后端：Spring Boot
- 数据库：保持MySQL

#### API设计示例
```java
@RestController
@RequestMapping("/api")
public class CinemaController {
    
    @GetMapping("/movies")
    public List<Movie> getAllMovies() {
        return cinemaManager.getAllMovies();
    }
    
    @PostMapping("/orders")
    public Order createOrder(@RequestBody OrderRequest request) {
        return bookingService.createOrder(request.getUser(), 
                                         request.getShow(), 
                                         request.getSeatIds());
    }
}
```

## 故障排除

### 1. 常见问题及解决方案

#### MySQL连接失败
**问题**：`No suitable driver found for jdbc:mysql://`
**解决方案**：
```bash
# 确保MySQL驱动在lib目录中
ls lib/mysql-connector*.jar

# 重新下载依赖
mvn dependency:copy-dependencies -DoutputDirectory=lib

# 检查数据库服务是否启动
mysql -u root -p -e "SELECT 1"
```

#### 中文乱码问题
**问题**：数据库中的中文显示为问号
**解决方案**：
```sql
-- 检查数据库字符集
SHOW VARIABLES LIKE 'character_set%';

-- 修改数据库字符集
ALTER DATABASE cinema_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修改表字符集
ALTER TABLE movies CONVERT TO CHARACTER SET utf8mb4;
```

#### PowerShell输入问题
**问题**：使用Scanner.nextLine()时抛出NoSuchElementException
**解决方案**：
- 使用项目中的`readLine()`方法代替`scanner.nextLine()`
- 或使用cmd代替PowerShell

#### 座位锁定超时
**问题**：座位被锁定后无法自动释放
**解决方案**：
```java
// 在BookingService中添加定时检查
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(this::checkExpiredOrders, 0, 1, TimeUnit.MINUTES);
```

### 2. 性能优化建议

#### 数据库优化
- 为常用查询字段添加索引
- 使用连接池管理数据库连接
- 定期清理过期订单数据

#### 内存优化
- 使用弱引用缓存不常用数据
- 及时释放大对象
- 避免内存泄漏

#### 并发优化
- 使用线程安全的数据结构
- 合理使用同步机制
- 避免死锁

### 3. 日志和监控

#### 添加日志框架
```xml
<!-- pom.xml中添加logback依赖 -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.4.7</version>
</dependency>
```

#### 监控关键指标
- 数据库连接数
- 内存使用情况
- 订单处理速度
- 座位使用率

## 团队协作指南

### 1. 代码管理

#### Git工作流
```bash
# 创建功能分支
git checkout -b feature/new-pricing-strategy

# 提交代码
git add .
git commit -m "feat: 添加学生定价策略"

# 推送分支
git push origin feature/new-pricing-strategy

# 合并到主分支
git checkout main
git merge feature/new-pricing-strategy
```

#### 代码审查清单
- [ ] 代码符合项目规范
- [ ] 异常处理完善
- [ ] 添加必要的注释
- [ ] 通过所有测试
- [ ] 更新相关文档

### 2. 测试策略

#### 单元测试
- 每个服务类都要有对应的测试类
- 测试覆盖率不低于80%
- 使用Mock对象测试外部依赖

#### 集成测试
- 测试数据库操作
- 测试完整的业务流程
- 测试并发场景

#### 测试数据管理
```java
// 使用测试数据库
@Test
public void testCreateOrder() {
    // 准备测试数据
    Movie testMovie = new Movie("TEST-001", "测试电影", ...);
    // 执行测试
    // 验证结果
    // 清理测试数据
}
```

### 3. 文档维护

#### API文档
- 使用JavaDoc生成API文档
- 保持文档与代码同步
- 提供使用示例

#### 更新日志
```markdown
## 更新日志

### v1.1.0 (2025-12-10)
- 新增：MySQL数据库支持
- 修复：场次查询空指针异常
- 优化：座位锁定机制

### v1.0.0 (2025-12-01)
- 初始版本发布
```

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 联系方式

- 项目维护者：renquan87
- 项目地址：https://github.com/renquan87/HNU-OOP-program2-CinemaBookingSystem
- 问题反馈：请提交Issue

---

*本文档最后更新时间：2025年12月10日*(AI生成)
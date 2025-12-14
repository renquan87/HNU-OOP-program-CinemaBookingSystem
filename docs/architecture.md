# 系统架构详解

## 核心实体层 (model包)

### Movie类 - 电影实体
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

### ScreeningRoom类 - 放映厅实体
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

### Show类 - 场次实体
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

### Seat类及其子类 - 座位体系
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

### User类 - 用户实体
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

### Order类 - 订单实体
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

## 策略模式层 (strategy包)

### PricingStrategy接口
```java
public interface PricingStrategy {
    double calculatePrice(Seat seat, Show show);
}
```

### StandardPricing - 标准定价策略
- 周末票价上浮20%
- 晚间时段（18:00后）票价上浮15%
- VIP座位在基础价格上加10元
- 折扣座位为基础价格的80%

### PremiumPricing - 高级定价策略
- VIP座位为基础价格的2倍
- 周末票价上浮30%
- 晚间时段票价上浮25%
- 黄金时段（19:00-21:00）额外上浮10%
- 折扣座位为基础价格的70%

## 服务层 (service包)

### CinemaManager - 影院管理服务（单例模式）
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

### BookingService - 预订服务（单例模式）
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

## 数据存储层 (storage包)

### MySQLDataStorage - MySQL数据库实现
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

### SimpleDatabaseConnection - 数据库连接管理
- 自动检测MySQL驱动可用性
- 支持从命令行参数加载密码
- 处理连接异常

## 异常处理层 (exception包)

- **InvalidBookingException**: 无效预订异常
  - 包含预订详情和错误原因
  - 用于座位不可用、时间冲突等场景

- **PaymentFailedException**: 支付失败异常
  - 包含支付失败原因
  - 用于支付流程异常处理
  
- **SeatNotAvailableException**: 座位不可用异常
  - 包含座位ID和不可用原因
  - 用于座位状态检查

## 用户界面层 (ui包)

### ConsoleUI - 控制台界面
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
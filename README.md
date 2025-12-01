# 电影院购票系统

基于Java实现的电影院购票管理系统，采用面向对象设计模式，支持用户购票、管理员管理等核心功能。

## 系统架构

### 核心实体类
- **Movie**: 电影信息管理
- **ScreeningRoom**: 放映厅管理
- **Show**: 场次信息管理
- **Seat**: 座位抽象类，包含RegularSeat和VIPSeat子类
- **User**: 用户管理，支持普通用户和管理员角色
- **Order**: 订单管理

### 设计模式
- **策略模式**: PricingStrategy接口及其实现类StandardPricing和PremiumPricing
- **单例模式**: CinemaManager和BookingService服务类
- **继承多态**: Seat类及其子类RegularSeat和VIPSeat

### 服务层
- **CinemaManager**: 影院管理服务，单例模式
- **BookingService**: 预订服务，支持订单创建、支付和取消

### 用户界面
- **ConsoleUI**: 控制台用户界面，支持完整的用户交互流程

## 功能特性

### 用户功能
- 浏览电影列表和详细信息
- 查询场次信息
- 选择座位并购买电影票
- 支付订单
- 申请退票
- 查看个人订单历史
- 切换定价策略

### 管理员功能
- 添加、删除电影信息
- 管理放映厅信息
- 管理场次安排
- 查看票房统计和用户统计

### 定价策略
- **标准定价**: 周末+20%，晚间+15%
- **高级定价**: VIP座位2倍价格，周末+30%，晚间+25%，黄金时段+10%

## 技术栈
- Java 17
- Maven 3.x
- JUnit 5 (测试)

## 项目结构
```
CinemaBookingSystem/
├── src/main/java/com/cinema/
│   ├── Main.java                    # 主程序入口
│   ├── model/                       # 实体类
│   │   ├── Movie.java
│   │   ├── ScreeningRoom.java
│   │   ├── Show.java
│   │   ├── Seat.java
│   │   ├── RegularSeat.java
│   │   ├── VIPSeat.java
│   │   ├── User.java
│   │   └── Order.java
│   ├── strategy/                    # 策略模式实现
│   │   ├── PricingStrategy.java
│   │   ├── StandardPricing.java
│   │   └── PremiumPricing.java
│   ├── service/                     # 服务层
│   │   ├── CinemaManager.java
│   │   └── BookingService.java
│   └── ui/                          # 用户界面
│       └── ConsoleUI.java
├── src/test/java/                   # 测试代码
├── pom.xml                          # Maven配置
└── README.md                        # 项目说明
```

## 编译和运行

### 编译项目
```bash
mvn clean compile
```

### 运行程序
```bash
mvn exec:java -Dexec.mainClass="com.cinema.Main"
```

### 或者编译并运行
```bash
mvn clean compile exec:java -Dexec.mainClass="com.cinema.Main"
```

### 运行测试
```bash
mvn test
```

### 创建可执行JAR
```bash
mvn clean package
java -jar target/cinema-booking-system-1.0-SNAPSHOT.jar
```

## 使用说明

### 启动系统
1. 运行主程序后，系统会显示欢迎界面
2. 输入用户ID登录，或直接回车使用默认用户

### 用户操作流程
1. **浏览电影**: 查看所有电影的详细信息
2. **查询场次**: 根据电影名称和日期筛选场次
3. **购买电影票**: 选择场次，查看座位图，选择座位并支付
4. **查看订单**: 查看个人所有订单信息
5. **退票**: 选择订单进行退票操作

### 管理员操作流程
1. **管理电影**: 添加或删除电影信息
2. **管理放映厅**: 添加或删除放映厅
3. **管理场次**: 添加或删除放映场次
4. **查看统计**: 查看系统运营统计数据

## 设计特点

1. **面向对象设计**: 严格遵循面向对象设计原则，封装、继承、多态
2. **设计模式应用**: 策略模式、单例模式、继承多态的合理运用
3. **线程安全**: 使用ConcurrentHashMap保证多线程环境下的数据安全
4. **模块化设计**: 清晰的分层架构，便于维护和扩展
5. **用户体验**: 直观的控制台界面，完整的操作流程

## 扩展性

系统设计具有良好的扩展性：
- 可轻松添加新的定价策略
- 支持添加新的座位类型
- 可扩展新的用户角色和权限
- 支持接入真实的支付系统
- 可扩展为Web应用或移动应用

## 注意事项

1. 系统使用内存存储数据，重启后数据会丢失
2. 支付功能为模拟实现，实际应用需要接入真实支付系统
3. 建议在生产环境中使用数据库存储数据
4. 可根据需要添加日志记录功能
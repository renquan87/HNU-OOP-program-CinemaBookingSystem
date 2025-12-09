# 电影院购票系统

基于Java实现的电影院购票管理系统，采用面向对象设计模式，支持MySQL数据库和文件存储，支持用户购票、管理员管理等核心功能。

## 系统架构

### 核心实体类
- **Movie**: 电影信息管理
- **ScreeningRoom**: 放映厅管理
- **Show**: 场次信息管理
- **Seat**: 座位抽象类，包含RegularSeat、VIPSeat和DiscountSeat子类
- **User**: 用户管理，支持普通用户和管理员角色
- **Order**: 订单管理

### 设计模式
- **策略模式**: PricingStrategy接口及其实现类StandardPricing和PremiumPricing
- **单例模式**: CinemaManager和BookingService服务类
- **继承多态**: Seat类及其子类RegularSeat、VIPSeat和DiscountSeat

### 服务层
- **CinemaManager**: 影院管理服务，单例模式
- **BookingService**: 预订服务，支持订单创建、支付和取消

### 数据存储
- **SimpleDataStorage**: 文件存储实现，使用Java原生序列化
- **MySQLDataStorage**: MySQL数据库存储实现
- **SimpleDatabaseConnection**: 数据库连接管理
- **DatabaseInitializer**: 数据库初始化工具

系统支持双模式存储：自动检测MySQL可用性，优先使用数据库，不可用时回退到文件存储

### 异常处理
- **InvalidBookingException**: 无效预订异常
- **PaymentFailedException**: 支付失败异常
- **SeatNotAvailableException**: 座位不可用异常

### 用户界面
- **ConsoleUI**: 控制台用户界面，支持完整的用户交互流程
- **NewMethods**: 新增功能方法

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

### 定价策略（正在完善）
- **标准定价**: 周末+20%，晚间+15%
- **高级定价**: VIP座位2倍价格，周末+30%，晚间+25%，黄金时段+10%

### 座位类型
- **RegularSeat**: 普通座位
- **VIPSeat**: VIP座位
- **DiscountSeat**: 折扣座位

## 技术栈
- Java 21
- Maven 3.x
- MySQL 8.0+
- JUnit 5 (测试)
- HikariCP (连接池)

## 项目结构
```
CinemaBookingSystem/
├── src/main/java/com/cinema/
│   ├── Main.java                    # 主程序入口
│   ├── exception/                   # 异常类
│   │   ├── InvalidBookingException.java
│   │   ├── PaymentFailedException.java
│   │   └── SeatNotAvailableException.java
│   ├── model/                       # 实体类
│   │   ├── Movie.java
│   │   ├── ScreeningRoom.java
│   │   ├── Show.java
│   │   ├── Seat.java
│   │   ├── RegularSeat.java
│   │   ├── VIPSeat.java
│   │   ├── DiscountSeat.java
│   │   ├── User.java
│   │   └── Order.java
│   ├── strategy/                    # 策略模式实现
│   │   ├── PricingStrategy.java
│   │   ├── StandardPricing.java
│   │   └── PremiumPricing.java
│   ├── service/                     # 服务层
│   │   ├── CinemaManager.java
│   │   └── BookingService.java
│   ├── storage/                     # 数据存储
│   │   ├── SimpleDataStorage.java
│   │   ├── MySQLDataStorage.java
│   │   └── SimpleDatabaseConnection.java
│   └── ui/                          # 用户界面
│       ├── ConsoleUI.java
│       └── NewMethods.java
├── src/test/java/                   # 测试代码
├── src/main/resources/              # 资源文件
│   ├── config.properties            # 数据库配置
│   └── schema.sql                   # 数据库表结构
├── data/                            # 文件存储数据
│   ├── movies.dat
│   ├── orders.dat
│   ├── rooms.dat
│   ├── shows.dat
│   └── users.dat
├── lib/                             # 依赖库目录
├── pom.xml                          # Maven配置
└── README.md                        # 项目说明
```

## 编译和运行

### 环境要求
- Java 21 或更高版本
- MySQL 8.0+（可选，不使用时可回退到文件存储）
- Maven 3.x

### 1. 下载依赖
```bash
# 自动下载所有依赖到lib目录
mvn dependency:copy-dependencies -DoutputDirectory=lib
```

### 2. 编译项目
```bash
mvn clean compile
```

### 3. 初始化MySQL数据库（首次使用）
```bash
java -cp "lib/*;target/classes" com.cinema.DatabaseInitializer
```

### 4. 运行程序

#### 使用MySQL数据库
```bash
java -cp "lib/*;target/classes" com.cinema.Main
```

#### 使用文件存储（无需MySQL）
```bash
java -cp target/classes com.cinema.Main
```

### 5. 运行测试
```bash
mvn test
```

### 6. 创建可执行JAR
```bash
mvn clean package
java -jar target/cinema-booking-system-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Windows批处理文件
- `run.bat`: 编译并运行主程序
- `run_system.bat`: 检查环境并运行系统
- `start_system.bat`: 简化启动脚本

### 数据库配置
编辑 `src/main/resources/config.properties`：
```properties
db.url=jdbc:mysql://localhost:3306/cinema_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC
db.username=root
db.password=你的密码
db.driver=com.mysql.cj.jdbc.Driver
```


## 使用说明

### 启动系统
1. 运行主程序后，系统会显示欢迎界面
2. 输入用户ID登录(当前系统暂时拥有普通用户renquan,管理员ADMIN-001)

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
3. **数据持久化**: 使用文件存储数据，支持系统重启后数据恢复
4. **异常处理**: 完善的异常处理机制，提高系统健壮性
5. **模块化设计**: 清晰的分层架构，便于维护和扩展
6. **用户体验**: 直观的控制台界面，完整的操作流程

## 扩展性

系统设计具有良好的扩展性：
- 可轻松添加新的定价策略
- 支持添加新的座位类型
- 可扩展新的用户角色和权限
- 支持接入真实的支付系统
- 可扩展为Web应用或移动应用
- 支持数据库存储替换

## 注意事项

1. 系统支持双模式存储：优先使用MySQL数据库，不可用时自动回退到文件存储
2. 确保data目录存在且有读写权限（文件存储模式）
3. 支付功能为模拟实现，实际应用需要接入真实支付系统
4. 系统支持多用户并发操作，使用线程安全的数据结构
5. MySQL连接失败时会显示提示信息并自动使用文件存储

## 数据库表结构

系统创建以下MySQL表：
- `movies` - 电影信息
- `screening_rooms` - 放映厅
- `seats` - 座位信息
- `shows` - 场次信息
- `users` - 用户信息
- `orders` - 订单信息
- `order_seats` - 订单座位关联

## 故障排除

1. **MySQL连接失败**
   - 检查MySQL服务是否启动
   - 验证配置文件中的用户名密码
   - 确认MySQL驱动在lib目录中

2. **编码问题**
   - Windows系统建议使用cmd而不是PowerShell
   - 或使用start_system.bat启动脚本

3. **依赖问题**
   - 运行 `mvn dependency:copy-dependencies` 重新下载依赖
   - 确保lib目录包含所有必要的jar文件

## 许可证

本项目采用开源许可证，详见LICENSE文件。
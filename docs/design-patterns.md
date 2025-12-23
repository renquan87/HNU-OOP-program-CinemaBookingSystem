# 设计模式在 CinemaBookingSystem 中的应用

本文档详细分析了 CinemaBookingSystem 项目中实际使用的各种设计模式，包括它们的具体实现位置、应用场景和优势。

## 目录
1. [策略模式 (Strategy Pattern)](#策略模式-strategy-pattern)
2. [单例模式 (Singleton Pattern)](#单例模式-singleton-pattern)
3. [建造者模式 (Builder Pattern)](#建造者模式-builder-pattern)
4. [简单工厂模式 (Simple Factory Pattern)](#简单工厂模式-simple-factory-pattern)
5. [继承模式 (Inheritance Pattern)](#继承模式-inheritance-pattern)
6. [其他设计特性](#其他设计特性)

---

## 策略模式 (Strategy Pattern)

### 模式定义
策略模式定义了一系列算法，把它们一个个封装起来，并且使它们可相互替换。

### 应用位置

#### 1. 策略接口 - `src/main/java/com/cinema/strategy/PricingStrategy.java`
```java
public interface PricingStrategy {
    double calculatePrice(Show show, Seat seat);
}
```

#### 2. 具体策略实现

**标准定价策略** - `src/main/java/com/cinema/strategy/StandardPricing.java`
```java
public class StandardPricing implements PricingStrategy {
    @Override
    public double calculatePrice(Show show, Seat seat) {
        double basePrice;
        
        // 根据座位类型使用对应的价格
        if (seat instanceof VIPSeat) {
            basePrice = show.getVipPrice();
        } else if (seat instanceof DiscountSeat) {
            basePrice = show.getDiscountPrice();
        } else {
            basePrice = show.getBasePrice();
        }

        // 周末定价 (20% increase)
        if (isWeekend(show.getStartTime().getDayOfWeek().getValue())) {
            basePrice *= 1.2;
        }

        // 晚间定价 (after 6 PM, 15% increase)
        if (show.getStartTime().getHour() >= 18) {
            basePrice *= 1.15;
        }

        // 四舍五入到小数点后两位
        long roundedPrice = Math.round(basePrice * 100);
        return roundedPrice / 100.0;
    }
}
```

**高级定价策略** - `src/main/java/com/cinema/strategy/PremiumPricing.java`
```java
public class PremiumPricing implements PricingStrategy {
    @Override
    public double calculatePrice(Show show, Seat seat) {
        double basePrice = show.getBasePrice();
        
        // VIP座位获得高级定价
        if (seat instanceof VIPSeat) {
            basePrice *= 2.0;
        }
        
        // 周末定价 (30% increase)
        if (isWeekend(show.getStartTime().getDayOfWeek().getValue())) {
            basePrice *= 1.3;
        }
        
        // 晚间定价 (after 6 PM, 25% increase)
        if (show.getStartTime().getHour() >= 18) {
            basePrice *= 1.25;
        }
        
        // 黄金时段定价 (7-9 PM, additional 10%)
        if (show.getStartTime().getHour() >= 19 && show.getStartTime().getHour() <= 21) {
            basePrice *= 1.1;
        }
        
        return basePrice;
    }
}
```

#### 3. 策略使用者 - `src/main/java/com/cinema/service/BookingService.java`
```java
public class BookingService {
    // 策略对象
    private PricingStrategy pricingStrategy;
    
    // 构造函数中注入策略
    private BookingService(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }
    
    // 使用策略计算价格
    public double calculateSeatPrice(Show show, Seat seat) {
        return pricingStrategy.calculatePrice(show, seat);
    }
    
    // 运行时切换策略
    public void setPricingStrategy(PricingStrategy newPricingStrategy) {
        this.pricingStrategy = newPricingStrategy;
        notificationService.sendBroadcast("系统定价策略已更新为: " + newPricingStrategy.getClass().getSimpleName());
    }
}
```

#### 4. 策略管理界面 - `src/main/java/com/cinema/ui/ConsoleUI.java`
```java
private void switchPricingStrategy() {
    // 显示当前策略
    printlnColored(CYAN, "\n当前定价策略: " + 
        (bookingService.getPricingStrategy().getClass().getSimpleName().equals("StandardPricing") ? "标准定价" : "高级定价"));
    
    switch (choice) {
        case "1":
            bookingService.setPricingStrategy(new com.cinema.strategy.StandardPricing());
            printSuccess("已切换到标准定价策略");
            break;
        case "2":
            bookingService.setPricingStrategy(new com.cinema.strategy.PremiumPricing());
            printSuccess("已切换到高级定价策略");
            break;
    }
}
```

### 应用场景
- **动态定价调整**：管理员可以根据节假日、促销活动等实时切换定价策略
- **A/B测试**：可以测试不同定价策略对用户购票行为的影响
- **业务扩展**：新增定价策略只需实现接口，无需修改现有代码

### 模式优势
1. **算法可替换性**：运行时动态切换定价算法
2. **开闭原则**：新增策略不修改现有代码
3. **代码复用**：定价逻辑与业务逻辑分离
4. **易于测试**：每种策略可独立测试

---

## 单例模式 (Singleton Pattern)

### 模式定义
单例模式确保一个类只有一个实例，并提供全局访问点。

### 应用位置

#### 1. CinemaManager - `src/main/java/com/cinema/service/CinemaManager.java`
```java
public class CinemaManager {
    private static CinemaManager instance;
    private final Map<String, Movie> movies;
    private final Map<String, ScreeningRoom> screeningRooms;
    private final Map<String, Show> shows;
    private final Map<String, User> users;
    
    // 私有构造函数
    private CinemaManager() {
        this.movies = new HashMap<>();
        this.screeningRooms = new HashMap<>();
        this.shows = new HashMap<>();
        this.users = new HashMap<>();
        
        // 初始化服务
        this.displayService = DisplayService.getInstance();
    }
    
    // 线程安全的单例获取方法
    public static synchronized CinemaManager getInstance() {
        if (instance == null) {
            instance = new CinemaManager();
        }
        return instance;
    }
    
    // 核心业务方法
    public void addMovie(Movie movie) {
        movies.put(movie.getId(), movie);
        saveMovies();
    }
    
    public Movie getMovie(String id) {
        return movies.get(id);
    }
    
    // ... 其他管理方法
}
```

#### 2. BookingService - `src/main/java/com/cinema/service/BookingService.java`
```java
public class BookingService {
    private static BookingService instance;
    private final ConcurrentMap<String, Order> orders;
    private PricingStrategy pricingStrategy;
    
    // 私有构造函数，需要定价策略参数
    private BookingService(PricingStrategy pricingStrategy) {
        this.orders = new ConcurrentHashMap<>();
        this.pricingStrategy = pricingStrategy;
        
        // 初始化其他服务
        this.notificationService = NotificationService.getInstance();
        this.displayService = DisplayService.getInstance();
    }
    
    // 带参数的单例获取方法
    public static synchronized BookingService getInstance(PricingStrategy pricingStrategy) {
        if (instance == null) {
            instance = new BookingService(pricingStrategy);
        }
        return instance;
    }
    
    // 获取已初始化的实例
    public static BookingService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BookingService not initialized. Call getInstance(PricingStrategy) first.");
        }
        return instance;
    }
    
    // 核心业务方法
    public Order createOrder(User user, Show show, List<String> seatIds) 
            throws InvalidBookingException, SeatNotAvailableException {
        // 订单创建逻辑...
    }
}
```

#### 3. NotificationService - `src/main/java/com/cinema/service/NotificationService.java`
```java
public class NotificationService {
    private static NotificationService instance;
    private final List<AppNotification> notifications;
    
    private NotificationService() {
        this.notifications = new ArrayList<>();
    }
    
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    public void sendOrderUpdate(User user, Order order, String message) {
        // 发送订单更新通知
        AppNotification notification = new AppNotification(
            user.getId(),
            "订单更新",
            message,
            LocalDateTime.now()
        );
        notifications.add(notification);
    }
    
    public void sendBroadcast(String message) {
        // 广播消息给所有用户
        for (User user : CinemaManager.getInstance().getAllUsers()) {
            AppNotification notification = new AppNotification(
                user.getId(),
                "系统通知",
                message,
                LocalDateTime.now()
            );
            notifications.add(notification);
        }
    }
}
```

#### 4. DisplayService - `src/main/java/com/cinema/service/DisplayService.java`
```java
public class DisplayService {
    private static DisplayService instance;
    
    private DisplayService() {
        // 私有构造函数
    }
    
    public static synchronized DisplayService getInstance() {
        if (instance == null) {
            instance = new DisplayService();
        }
        return instance;
    }
    
    public void updateSeatDisplay(Show show) {
        // 更新座位显示状态
        System.out.println("座位图已更新: " + show.getId());
    }
}
```

#### 5. AiService - `src/main/java/com/cinema/service/AiService.java`
```java
public class AiService {
    private static AiService instance;
    private RestTemplate restTemplate;
    private CinemaManager cinemaManager;
    
    private AiService() {
        this.cinemaManager = CinemaManager.getInstance();
        this.restTemplate = createRestTemplate();
    }
    
    public static synchronized AiService getInstance() {
        if (instance == null) {
            instance = new AiService();
        }
        return instance;
    }
    
    public String getAnswer(String message) {
        // AI服务实现
        return "AI回答: " + message;
    }
}
```

### 应用场景
- **全局状态管理**：确保整个系统中只有一个核心服务实例
- **资源共享**：避免重复创建昂贵的资源（如数据库连接、网络连接）
- **状态一致性**：保证系统状态的一致性和完整性
- **服务协调**：各个服务之间的协调和通信

### 模式优势
1. **内存效率**：避免重复创建对象，节省内存
2. **全局访问**：提供统一的访问点
3. **状态共享**：多个组件可以共享同一个实例的状态
4. **线程安全**：通过 synchronized 保证多线程环境下的安全

### 使用示例
```java
// 在应用启动时初始化
public class CinemaApplication {
    public static void main(String[] args) {
        // 初始化核心服务
        BookingService.getInstance(new StandardPricing());
        CinemaManager.getInstance();
        
        // 在任何地方获取实例
        CinemaManager manager = CinemaManager.getInstance();
        BookingService booking = BookingService.getInstance();
        NotificationService notification = NotificationService.getInstance();
    }
}
```

---

## 建造者模式 (Builder Pattern)

### 模式定义
建造者模式将复杂对象的构建与其表示分离，使得同样的构建过程可以创建不同的表示。

### 应用位置

#### 1. StringBuilder 在字符串构建中的应用

**Movie 信息构建** - `src/main/java/com/cinema/model/Movie.java`
```java
public class Movie {
    // 使用 StringBuilder 构建复杂的电影信息字符串
    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append("电影标题: ").append(title).append("\n");
        info.append("导演: ").append(director).append("\n");
        info.append("主演: ").append(String.join(", ", actors)).append("\n");
        info.append("时长: ").append(duration).append(" 分钟\n");
        info.append("评分: ").append(rating).append("\n");
        info.append("类型: ").append(genre.getDescription()).append("\n");
        info.append("简介: ").append(description).append("\n");
        info.append("上映日期: ").append(releaseTime).append("\n");
        
        if (!showSchedule.isEmpty()) {
            info.append("场次安排:\n");
            for (Map.Entry<LocalDate, List<Show>> entry : showSchedule.entrySet()) {
                info.append("  ").append(entry.getKey()).append(": ");
                info.append(entry.getValue().size()).append(" 场\n");
            }
        }
        
        if (!comments.isEmpty()) {
            info.append("评论数量: ").append(comments.size()).append("\n");
        }
        
        return info.toString();
    }
}
```

**Order 信息构建** - `src/main/java/com/cinema/model/Order.java`
```java
public class Order {
    // 使用 StringBuilder 构建订单详情
    public String getOrderDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("订单号: ").append(orderId).append("\n");
        sb.append("用户: ").append(user.getName()).append("\n");
        sb.append("电影: ").append(show.getMovie().getTitle()).append("\n");
        sb.append("放映厅: ").append(show.getScreeningRoom().getName()).append("\n");
        sb.append("开场时间: ").append(show.getStartTime()).append("\n");
        sb.append("座位: ");
        
        for (int i = 0; i < seats.size(); i++) {
            if (i > 0) sb.append(", ");
            Seat seat = seats.get(i);
            sb.append(seat.getRow()).append("排").append(seat.getCol()).append("座");
        }
        sb.append("\n");
        sb.append("总金额: ¥").append(totalAmount).append("\n");
        sb.append("订单状态: ").append(getStatusDescription()).append("\n");
        sb.append("下单时间: ").append(createTime).append("\n");
        
        return sb.toString();
    }
}
```

#### 2. ConsoleUI 中的 StringBuilder 使用

**座位信息构建** - `src/main/java/com/cinema/ui/ConsoleUI.java`
```java
public class ConsoleUI {
    // 构建座位图显示
    private void displaySeatMap(Show show) {
        StringBuilder seatInfo = new StringBuilder();
        seatInfo.append("\n=== ").append(show.getMovie().getTitle()).append(" ===\n");
        seatInfo.append("放映厅: ").append(show.getScreeningRoom().getName()).append("\n");
        seatInfo.append("时间: ").append(show.getStartTime()).append("\n\n");
        
        ScreeningRoom room = show.getScreeningRoom();
        for (int row = 1; row <= room.getRows(); row++) {
            for (int col = 1; col <= room.getColumns(); col++) {
                String seatId = row + "-" + col;
                Seat seat = show.getSeat(seatId);
                
                if (seat.isAvailable()) {
                    seatInfo.append("□ ");
                } else if (seat.isLocked()) {
                    seatInfo.append("△ ");
                } else {
                    seatInfo.append("■ ");
                }
            }
            seatInfo.append("\n");
        }
        
        System.out.println(seatInfo.toString());
    }
    
    // 构建订单列表显示
    private void displayOrderList(List<Order> orders) {
        StringBuilder seatInfo = new StringBuilder();
        seatInfo.append("\n=== 订单列表 ===\n");
        
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            seatInfo.append(i + 1).append(". ")
                   .append(order.getOrderId()).append(" - ")
                   .append(order.getShow().getMovie().getTitle()).append(" - ")
                   .append(order.getSeats().size()).append(" 张票 - ")
                   .append("¥").append(order.getTotalAmount()).append(" - ")
                   .append(order.getStatus()).append("\n");
        }
        
        System.out.println(seatInfo.toString());
    }
}
```

#### 3. AiService 中的 StringBuilder 使用

**AI 响应构建** - `src/main/java/com/cinema/service/AiService.java`
```java
public class AiService {
    public String getAnswer(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("AI助手回复:\n");
        sb.append("问题: ").append(message).append("\n");
        
        // 根据问题类型构建不同的回答
        if (message.contains("电影")) {
            sb.append("关于电影的问题，我建议您查看最新的电影排片信息。\n");
        } else if (message.contains("座位")) {
            sb.append("关于座位预订，您可以选择合适的场次和座位。\n");
        } else {
            sb.append("感谢您的咨询，我会尽力帮助您。\n");
        }
        
        sb.append("\n如有其他问题，请随时提问。");
        
        return sb.toString();
    }
}
```

#### 4. DatabaseInitializer 中的 SQL 构建

**SQL 语句构建** - `src/main/java/com/cinema/DatabaseInitializer.java`
```java
public class DatabaseInitializer {
    // 使用 StringBuilder 构建 SQL 脚本
    private void executeSqlScript(String scriptPath) {
        StringBuilder sqlBuilder = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(scriptPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sqlBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("读取SQL脚本失败: " + e.getMessage());
            return;
        }
        
        String sql = sqlBuilder.toString();
        // 执行 SQL 语句...
    }
    
    // 构建批量插入语句
    private void buildInsertStatements(List<Movie> movies) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO movies (id, title, director, actors, duration, rating, genre, description) VALUES ");
        
        for (int i = 0; i < movies.size(); i++) {
            if (i > 0) sqlBuilder.append(", ");
            
            Movie movie = movies.get(i);
            sqlBuilder.append("(")
                     .append("'").append(movie.getId()).append("', ")
                     .append("'").append(movie.getTitle()).append("', ")
                     .append("'").append(movie.getDirector()).append("', ")
                     .append("'").append(String.join(",", movie.getActors())).append("', ")
                     .append(movie.getDuration()).append(", ")
                     .append(movie.getRating()).append(", ")
                     .append("'").append(movie.getGenre()).append("', ")
                     .append("'").append(movie.getDescription()).append("')");
        }
        
        String sql = sqlBuilder.toString();
        // 执行批量插入...
    }
}
```

### 应用场景
- **字符串拼接**：构建复杂的文本信息（订单详情、电影信息等）
- **SQL 语句构建**：动态生成数据库查询语句
- **UI 显示构建**：构建控制台界面的格式化输出
- **响应消息构建**：构建 AI 助手的回复内容

### 模式优势
1. **性能优化**：避免字符串拼接时的性能问题
2. **代码清晰**：使复杂的字符串构建逻辑更清晰
3. **可维护性**：易于修改和扩展字符串构建逻辑
4. **内存效率**：减少临时字符串对象的创建

### 实际效果
```java
// 传统字符串拼接（低效）
String info = "电影标题: " + title + "\n" + 
              "导演: " + director + "\n" + 
              "主演: " + String.join(", ", actors) + "\n";

// 使用 StringBuilder（高效）
StringBuilder info = new StringBuilder();
info.append("电影标题: ").append(title).append("\n");
info.append("导演: ").append(director).append("\n");
info.append("主演: ").append(String.join(", ", actors)).append("\n");
```

---

## 简单工厂模式 (Simple Factory Pattern)

### 模式定义
简单工厂模式根据提供的参数创建并返回不同类型的实例，而不需要客户端知道具体的创建逻辑。

### 应用位置

#### 1. ScreeningRoom 中的座位创建 - `src/main/java/com/cinema/model/ScreeningRoom.java`
```java
public class ScreeningRoom {
    private void initializeSeats() {
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < totalCols; col++) {
                // 根据位置创建不同类型的座位
                if (row == 0) {
                    // 第一排为优惠座位
                    seatLayout[row][col] = new DiscountSeat(row + 1, col + 1, 50.0);
                } else if (row >= totalRows / 2 - 1 && row <= totalRows / 2 + 1) {
                    // 中间几排为VIP座位
                    seatLayout[row][col] = new VIPSeat(row + 1, col + 1);
                } else {
                    // 其他为普通座位
                    seatLayout[row][col] = new RegularSeat(row + 1, col + 1);
                }
            }
        }
    }
}
```

#### 2. Show 中的座位创建 - `src/main/java/com/cinema/model/Show.java`
```java
public class Show {
    private void initializeSeats() {
        Seat[][] roomSeats = screeningRoom.getSeatLayout();
        for (int row = 0; row < roomSeats.length; row++) {
            for (int col = 0; col < roomSeats[row].length; col++) {
                Seat roomSeat = roomSeats[row][col];
                Seat showSeat;
                
                // 根据房间座位类型创建场次座位
                if (roomSeat instanceof VIPSeat) {
                    showSeat = new VIPSeat(roomSeat.getRow(), roomSeat.getCol(), vipPrice);
                } else if (roomSeat instanceof DiscountSeat) {
                    showSeat = new DiscountSeat(roomSeat.getRow(), roomSeat.getCol(), discountPrice);
                } else {
                    showSeat = new RegularSeat(roomSeat.getRow(), roomSeat.getCol(), basePrice);
                }
                
                seats.add(showSeat);
            }
        }
    }
}
```

### 应用场景
- **座位布局初始化**：根据放映厅布局自动创建不同类型的座位
- **价格差异化**：为不同类型的座位设置不同的基础价格
- **类型安全创建**：确保创建的座位对象类型正确

### 模式优势
1. **封装创建逻辑**：隐藏对象创建的复杂性
2. **统一接口**：通过统一的方法创建不同类型的对象
3. **易于维护**：创建逻辑集中管理，便于修改和扩展
4. **类型安全**：通过 instanceof 确保类型匹配

---

## 继承模式 (Inheritance Pattern)

### 模式定义
继承模式通过类的继承机制实现代码复用和功能扩展，子类继承父类的属性和方法。

### 应用位置

#### 1. 座位继承体系 - `src/main/java/com/cinema/model/`
```java
// 抽象基类
public abstract class Seat implements java.io.Serializable {
    protected int row;
    protected int col;
    protected SeatStatus status;
    protected double basePrice;
    
    // 通用方法
    public boolean isAvailable() { return status == SeatStatus.AVAILABLE; }
    public void lock() { this.status = SeatStatus.LOCKED; }
    public void sell() { this.status = SeatStatus.SOLD; }
}

// VIP座位子类
public class VIPSeat extends Seat implements java.io.Serializable {
    private static final double PRICE_PREMIUM = 10.0;
    
    public VIPSeat(int row, int col, double basePrice) {
        super(row, col, basePrice + PRICE_PREMIUM); // VIP座位加价
    }
}

// 折扣座位子类
public class DiscountSeat extends Seat implements java.io.Serializable {
    public DiscountSeat(int row, int col, double basePrice) {
        super(row, col, basePrice); // 使用折扣价格
    }
}

// 普通座位子类
public class RegularSeat extends Seat implements java.io.Serializable {
    private static final double DEFAULT_BASE_PRICE = 50.0;
    
    public RegularSeat(int row, int col) {
        super(row, col, DEFAULT_BASE_PRICE);
    }
}
```

### 应用场景
- **座位类型扩展**：通过继承实现不同类型的座位
- **价格差异化**：子类可以重写价格计算逻辑
- **功能扩展**：子类可以添加特定的功能

### 模式优势
1. **代码复用**：子类继承父类的通用功能
2. **类型安全**：通过类型系统确保对象类型正确
3. **多态支持**：支持向上转型和动态绑定
4. **易于扩展**：新增座位类型只需继承基类


---
## 其他设计特性

### 1. 模板方法模式 (Template Method Pattern)

#### 应用位置

**抽象座位类** - `src/main/java/com/cinema/model/Seat.java`
```java
public abstract class Seat {
    protected int row;
    protected int col;
    protected double basePrice;
    protected SeatStatus status;
    
    // 模板方法：定义座位状态变更的流程
    public final boolean changeStatus(SeatStatus newStatus) {
        if (!canChangeTo(newStatus)) {
            return false;
        }
        
        beforeStatusChange(newStatus);
        this.status = newStatus;
        afterStatusChange(newStatus);
        
        return true;
    }
    
    // 钩子方法：子类可以重写
    protected boolean canChangeTo(SeatStatus newStatus) {
        return true; // 默认允许所有状态变更
    }
    
    protected void beforeStatusChange(SeatStatus newStatus) {
        // 状态变更前的处理
    }
    
    protected void afterStatusChange(SeatStatus newStatus) {
        // 状态变更后的处理
    }
}
```

### 2. 适配器模式 (Adapter Pattern)

#### 应用位置

**数据库连接适配** - `src/main/java/com/cinema/storage/SimpleDatabaseConnection.java`
```java
public class SimpleDatabaseConnection {
    // 适配不同的数据库配置来源
    private static String loadPassword(String[] args, Properties props) {
        // 1. 环境变量适配
        String password = DbPasswordResolver.fromEnvironment();
        if (password != null) {
            return password;
        }

        // 2. 命令行参数适配
        password = DbPasswordResolver.fromCommandLine(args);
        if (password != null) {
            return password;
        }

        // 3. 配置文件适配
        if (props != null) {
            password = DbPasswordResolver.fromProperties(props);
            if (password != null) {
                return password;
            }
        }

        // 4. 默认值适配
        return "123421";
    }
}
```

### 3. 委托模式 (Delegation Pattern)

#### 应用位置

**服务间委托** - `src/main/java/com/cinema/service/BookingService.java`
```java
public class BookingService {
    // 委托给通知服务
    private final NotificationService notificationService;
    private final DisplayService displayService;
    
    public void processPayment(Order order) throws PaymentFailedException {
        // 支付处理逻辑...
        
        // 委托给通知服务发送通知
        notificationService.sendOrderUpdate(order.getUser(), order, "支付成功！");
        
        // 委托给显示服务更新界面
        displayService.updateSeatDisplay(order.getShow());
    }
}
```

### 4. 数据访问对象模式 (DAO Pattern)

#### 应用位置

**MySQL 数据访问** - `src/main/java/com/cinema/storage/MySQLDataStorage.java`
```java
public class MySQLDataStorage {
    // 电影数据访问
    public void saveMovies(Map<String, Movie> movies) {
        String sql = "INSERT INTO movies (id, title, director, actors, duration, rating, genre, description) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE title=VALUES(title), director=VALUES(director)";
        // 数据库操作...
    }
    
    public Map<String, Movie> loadMovies() {
        Map<String, Movie> movies = new HashMap<>();
        String sql = "SELECT * FROM movies";
        // 数据库操作...
        return movies;
    }
    
    // 订单数据访问
    public void saveOrders(Map<String, Order> orders) {
        // 订单保存逻辑...
    }
    
    public Map<String, Order> loadOrders() {
        // 订单加载逻辑...
    }
}
```

### 5. 异常处理策略

#### 应用位置

**自定义异常体系** - `src/main/java/com/cinema/exception/`
```java
// 无效预订异常
public class InvalidBookingException extends Exception {
    private final String details;
    
    public InvalidBookingException(String message) {
        super(message);
        this.details = "";
    }
    
    public InvalidBookingException(String message, String details) {
        super(message);
        this.details = details;
    }
}

// 支付失败异常
public class PaymentFailedException extends Exception {
    private final String orderId;
    private final double amount;
    
    public PaymentFailedException(String orderId, double amount, String method, String reason) {
        super("支付失败: " + reason);
        this.orderId = orderId;
        this.amount = amount;
    }
}

// 座位不可用异常
public class SeatNotAvailableException extends Exception {
    private final String seatId;
    
    public SeatNotAvailableException(String seatId, String reason) {
        super("座位 " + seatId + " 不可用: " + reason);
        this.seatId = seatId;
    }
}
```

### 6. 枚举策略模式

#### 应用位置

**电影类型枚举** - `src/main/java/com/cinema/model/MovieGenre.java`
```java
public enum MovieGenre {
    ACTION("动作片"),
    COMEDY("喜剧片"),
    DRAMA("剧情片"),
    HORROR("恐怖片"),
    ROMANCE("爱情片"),
    SCIFI("科幻片"),
    THRILLER("惊悚片");
    
    private final String description;
    
    MovieGenre(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    // 从描述获取枚举值
    public static MovieGenre fromDescription(String description) {
        for (MovieGenre genre : values()) {
            if (genre.description.equals(description)) {
                return genre;
            }
        }
        return DRAMA; // 默认值
    }
}
```

---

## 设计模式协同应用

### 策略模式 + 单例模式
```java
// 在单例服务中使用策略模式
public class BookingService {
    private static BookingService instance;
    private PricingStrategy pricingStrategy;
    
    // 单例创建时注入策略
    public static synchronized BookingService getInstance(PricingStrategy pricingStrategy) {
        if (instance == null) {
            instance = new BookingService(pricingStrategy);
        }
        return instance;
    }
    
    // 运行时切换策略
    public void setPricingStrategy(PricingStrategy newPricingStrategy) {
        this.pricingStrategy = newPricingStrategy;
        // 通知其他单例服务
        NotificationService.getInstance().sendBroadcast("定价策略已更新");
    }
}
```

### 建造者模式 + 单例模式
```java
// 在单例服务中使用建造者模式构建复杂信息
public class NotificationService {
    private static NotificationService instance;
    
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    // 使用 StringBuilder 构建通知消息
    public void sendOrderUpdate(User user, Order order, String message) {
        StringBuilder notification = new StringBuilder();
        notification.append("尊敬的 ").append(user.getName()).append("：\n");
        notification.append("您的订单 ").append(order.getOrderId()).append(" ");
        notification.append(message).append("\n");
        notification.append("订单详情：\n");
        notification.append(order.getOrderDetails());
        
        // 发送构建好的通知
        deliverNotification(user.getId(), notification.toString());
    }
}
```

### DAO 模式 + 单例模式
```java
// 在单例服务中使用 DAO 模式
public class CinemaManager {
    private static CinemaManager instance;
    private MySQLDataStorage dataStorage;
    
    private CinemaManager() {
        this.dataStorage = new MySQLDataStorage();
    }
    
    public static synchronized CinemaManager getInstance() {
        if (instance == null) {
            instance = new CinemaManager();
        }
        return instance;
    }
    
    // 委托给 DAO 对象进行数据操作
    public void saveMovies() {
        dataStorage.saveMovies(movies);
    }
    
    public void loadMovies() {
        Map<String, Movie> loadedMovies = dataStorage.loadMovies();
        this.movies.putAll(loadedMovies);
    }
}
```

---

## 总结

CinemaBookingSystem 项目实际运用了以下设计模式和设计特性：

### 核心设计模式
1. **策略模式**：实现了灵活的定价策略切换，支持业务动态调整
2. **单例模式**：确保了核心服务的全局唯一性和资源共享
3. **建造者模式**：优化了复杂字符串的构建，提升了性能
4. **简单工厂模式**：封装了座位对象的创建逻辑，隐藏创建复杂性

### 辅助设计特性
5. **继承模式**：通过继承实现了座位类型的扩展和差异化
6. **模板方法模式**：定义了座位状态变更的标准流程
7. **适配器模式**：适配了不同的数据库配置来源
8. **委托模式**：实现了服务间的职责分离
9. **DAO 模式**：封装了数据访问逻辑
10. **异常处理策略**：建立了完整的异常处理体系
11. **枚举策略模式**：提供了类型安全的枚举实现

### 设计模式的价值

#### 1. **可维护性**
- 单例模式确保了核心服务的统一管理
- 策略模式使定价逻辑易于扩展和修改
- DAO 模式分离了业务逻辑和数据访问

#### 2. **可扩展性**
- 策略模式支持新增定价策略而不影响现有代码
- 模板方法模式为子类提供了扩展点
- 异常处理策略支持新增异常类型

#### 3. **性能优化**
- 单例模式避免了重复创建昂贵的对象
- 建造者模式优化了字符串操作性能
- 委托模式实现了合理的职责分工

#### 4. **代码质量**
- 适配器模式提高了配置的灵活性
- 枚举策略模式提供了类型安全
- 统一的异常处理提高了系统的健壮性

这些设计模式和特性的协同应用，使得 CinemaBookingSystem 具有良好的架构设计，能够满足电影票务系统的复杂业务需求，同时保证了代码的可读性、可维护性和可扩展性。
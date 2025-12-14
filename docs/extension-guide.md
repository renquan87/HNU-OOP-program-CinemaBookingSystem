# 扩展开发指南

## 添加新的定价策略

### 步骤1：实现PricingStrategy接口
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

### 步骤2：在BookingService中注册
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

## 添加新的座位类型

### 步骤1：继承Seat类
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

### 步骤2：在放映厅初始化时添加
```java
// 在ScreeningRoom的initializeSeats方法中添加
if (isCoupleSeatArea(row, col)) {
    seatLayout[row][col] = new CoupleSeat(row, col, basePrice * 1.5);
}
```

## 集成第三方支付

### 步骤1：创建支付接口
```java
public interface PaymentGateway {
    PaymentResult processPayment(Order order, PaymentMethod method);
    RefundResult processRefund(Order order);
}
```

### 步骤2：实现具体支付方式
```java
public class AlipayGateway implements PaymentGateway {
    @Override
    public PaymentResult processPayment(Order order, PaymentMethod method) {
        // 调用支付宝API
        return new PaymentResult(true, "支付成功");
    }
}
```

## 开发Web界面

### 技术选型建议
- 前端：React/Vue.js + Bootstrap
- 后端：Spring Boot
- 数据库：保持MySQL

### API设计示例
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
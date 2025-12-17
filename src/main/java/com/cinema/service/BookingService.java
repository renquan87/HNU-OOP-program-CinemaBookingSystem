package com.cinema.service;

import com.cinema.model.*;
import com.cinema.storage.MySQLDataStorage; // 引入 MySQL 存储
import com.cinema.strategy.PricingStrategy;
import com.cinema.exception.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.cinema.ws.SeatWebSocketServer;

public class BookingService {
    private static BookingService instance;
    private final ConcurrentMap<String, Order> orders;

    // 1. 引入策略模式 (定价)
    private PricingStrategy pricingStrategy; // 不再是 final，以便运行时修改

    // 2. 引入其他服务 (通知、显示)
    private final NotificationService notificationService;
    private final DisplayService displayService;

    // 3. 切换数据存储
    private final MySQLDataStorage mysqlDataStorage;
    private final boolean useMySQL;

    private BookingService(PricingStrategy pricingStrategy) {
        this.orders = new ConcurrentHashMap<>();
        this.pricingStrategy = pricingStrategy;

        // 初始化服务
        this.notificationService = NotificationService.getInstance();
        this.displayService = DisplayService.getInstance();

        // 初始化数据库
        MySQLDataStorage mysqlStorage = null;
        boolean connected = false;
        try {
            mysqlStorage = new MySQLDataStorage();
            System.out.println("✓ BookingService使用MySQL数据库存储");
            connected = true;
        } catch (Exception e) {
            System.err.println("✗ MySQL连接失败: " + e.getMessage());
            // 根据实际需求，这里可能需要抛出 RuntimeException 或切换到内存存储
            // 保持原代码逻辑：连接失败则抛出异常
            throw new RuntimeException("MySQL连接失败", e);
        }
        this.mysqlDataStorage = mysqlStorage;
        this.useMySQL = connected;

        loadOrders();
        rebuildUserOrderRelations();
    }

    public static synchronized BookingService getInstance(PricingStrategy pricingStrategy) {
        if (instance == null) {
            instance = new BookingService(pricingStrategy);
        }
        return instance;
    }

    public static BookingService getInstance() {
        if (instance == null) {
            // 异常信息调整，更清晰地提示未初始化
            throw new IllegalStateException("BookingService not initialized. Call getInstance(PricingStrategy) first.");
        }
        return instance;
    }

    // ================== 定价策略计算/修改 ==================
    /**
     * 使用策略模式计算票价
     */
    public double calculateSeatPrice(Show show, Seat seat) {
        // 委托给具体的策略类
        return pricingStrategy.calculatePrice(show, seat);
    }

    /**
     * 动态设置新的定价策略
     */
    public void setPricingStrategy(PricingStrategy newPricingStrategy) {
        this.pricingStrategy = newPricingStrategy;
        // 触发通知服务
        notificationService.sendBroadcast("系统定价策略已更为: " + newPricingStrategy.getClass().getSimpleName());
    }

    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

    // ================== 订单管理：创建 ==================
    public Order createOrder(User user, Show show, List<String> seatIds) throws InvalidBookingException, SeatNotAvailableException {
        // 简化校验，采用上段代码的校验逻辑
        if (show == null || user == null || seatIds == null || seatIds.isEmpty()) {
            throw new InvalidBookingException("参数无效");
        }

        List<Seat> selectedSeats = new ArrayList<>();
        // 锁座逻辑
        for (String seatId : seatIds) {
            Seat seat = show.getSeat(seatId);
            if (seat == null || !seat.isAvailable()) {
                throw new SeatNotAvailableException(seatId, "座位不可用");
            }
            // 使用定价策略计算实际价格并更新座位价格
            double actualPrice = calculateSeatPrice(show, seat);
            seat.setBasePrice(actualPrice);
            // 暂时锁定座位
            seat.lock();
            selectedSeats.add(seat);
        }

        Order order = new Order(
                "ORD-" + System.currentTimeMillis(),
                show,
                selectedSeats,
                LocalDateTime.now(),
                Order.OrderStatus.PENDING
        );

        order.setUser(user);
        orders.put(order.getOrderId(), order);
        user.addOrder(order);

        // 持久化订单和场次状态
        saveOrder(order);
        CinemaManager.getInstance().saveShows();

        // 🔔 [新增] 广播通知：告诉该场次的所有人，座位变了
        SeatWebSocketServer.fireUpdate(show.getId(), "UPDATE"); // 发送简单的更新信号

        notificationService.sendOrderUpdate(user, order, "订单已创建...");
        return order;
    }

    // ================== 订单管理：处理/支付 ==================
    public void processPayment(Order order) throws PaymentFailedException {
        if (order == null || order.getStatus() != Order.OrderStatus.PENDING) {
            throw new PaymentFailedException(order == null ? "" : order.getOrderId(), 0, "Unknown", "订单状态无效或已处理");
        }

        // 模拟支付逻辑
        boolean success = true;

        if (success) {
            order.setStatus(Order.OrderStatus.PAID);
            for (Seat seat : order.getSeats()) {
                seat.sell(); // 标记为已售出
            }
            saveOrder(order);
            CinemaManager.getInstance().saveShows();

            SeatWebSocketServer.fireUpdate(order.getShow().getId(), "UPDATE");

            // 3. 触发通知服务
            notificationService.sendOrderUpdate(order.getUser(), order, "支付成功！您的座位已锁定。");

            // 4. 触发显示服务 (更新座位图)
            displayService.updateSeatDisplay(order.getShow());


        } else {
            // 支付失败时应释放座位
            for (Seat seat : order.getSeats()) {
                seat.unlock();
            }
            throw new PaymentFailedException(order.getOrderId(), order.getTotalAmount(), "Online", "支付被拒绝");
        }
    }

    // ================== 订单管理：取消/退款 ==================
    public void cancelOrder(Order order) throws InvalidBookingException {
        if (order == null) throw new InvalidBookingException("订单为空");

        if (!orders.containsKey(order.getOrderId())) {
            throw new InvalidBookingException("订单不存在", "订单号: " + order.getOrderId());
        }

        // 统一处理 PENDING, PAID 状态的取消
        if (order.getStatus() == Order.OrderStatus.CANCELLED || order.getStatus() == Order.OrderStatus.REFUNDED) {
            throw new InvalidBookingException("订单已经取消或已退款", "订单号: " + order.getOrderId());
        }

        boolean isRefund = (order.getStatus() == Order.OrderStatus.PAID);

        if (isRefund) {
            order.setStatus(Order.OrderStatus.REFUNDED);
        } else if (order.getStatus() == Order.OrderStatus.PENDING || order.getStatus() == Order.OrderStatus.RESERVED) {
            order.setStatus(Order.OrderStatus.CANCELLED);
        } else {
            throw new InvalidBookingException("无法取消此状态的订单", "订单号: " + order.getOrderId() + ", 状态: " + order.getStatus());
        }

        // 释放座位
        for (Seat seat : order.getSeats()) {
            seat.unlock();
        }

        saveOrder(order);
        CinemaManager.getInstance().saveShows(); // 保存场次状态

        SeatWebSocketServer.fireUpdate(order.getShow().getId(), "UPDATE");

        // 3. 触发通知服务
        String msg = isRefund ? "退票成功，款项将原路返回。" : "订单已取消。";
        notificationService.sendOrderUpdate(order.getUser(), order, msg);

        // 4. 触发显示服务 (座位变回空闲)
        displayService.updateSeatDisplay(order.getShow());
    }

    // ================== 订单管理：查询 ==================
    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    /**
     * 获取指定用户的所有订单
     */
    public List<Order> getOrdersByUser(User user) {
        if (user == null) {
            return new ArrayList<>();
        }
        List<Order> userOrders = new ArrayList<>();
        // 遍历内存中的所有订单，找到属于该用户的
        for (Order order : orders.values()) {
            if (order.getUser() != null && order.getUser().getId().equals(user.getId())) {
                userOrders.add(order);
            }
        }
        return userOrders;
    }

    // ================== 预订相关 (保留原有逻辑，但取消时会触发新服务) ==================
    // 预订座位（锁定15分钟） - 逻辑与 createOrder 类似，但状态为 RESERVED
    public Order reserveOrder(User user, Show show, List<String> seatIds)
            throws InvalidBookingException, SeatNotAvailableException {
        // ... (预订逻辑，略) ...
        // 注意：原第一段代码没有这个方法，但第二段有，故保留，并确保 seat.lock() 被调用。

        if (show == null || user == null || seatIds == null || seatIds.isEmpty()) {
            throw new InvalidBookingException("参数无效");
        }

        List<Seat> selectedSeats = new ArrayList<>();
        for (String seatId : seatIds) {
            Seat seat = show.getSeat(seatId);
            if (seat == null || !seat.isAvailable()) {
                throw new SeatNotAvailableException(seatId, "座位不可用");
            }
            // 使用定价策略计算实际价格并更新座位价格
            double actualPrice = calculateSeatPrice(show, seat);
            seat.setBasePrice(actualPrice);
            seat.lock(); // 锁定座位
            selectedSeats.add(seat);
        }

        String orderId = "RESERVE-" + System.currentTimeMillis();
        Order order = new Order(orderId, show, selectedSeats, LocalDateTime.now(), Order.OrderStatus.RESERVED);
        order.setLockTime(LocalDateTime.now());
        order.setUser(user);

        orders.put(orderId, order);
        user.addOrder(order);
        saveOrder(order);
        CinemaManager.getInstance().saveShows();

        notificationService.sendOrderUpdate(user, order, "座位已预留，请在15分钟内支付。");
        return order;
    }

    // 支付预订订单 - 逻辑与 processPayment 类似
    public void processReservedOrderPayment(Order order) throws PaymentFailedException, InvalidBookingException {
        if (order == null) {
            throw new InvalidBookingException("订单不存在");
        }

        if (order.getStatus() != Order.OrderStatus.RESERVED) {
            throw new InvalidBookingException("订单状态不是预订状态");
        }

        if (order.isExpired()) {
            // 如果过期，视为支付失败，并触发取消逻辑 (释放座位等)
            cancelOrder(order);
            throw new PaymentFailedException(order.getOrderId(), order.getTotalAmount(), "Online", "预订已过期，请重新下单");
        }

        // 模拟支付成功
        order.setStatus(Order.OrderStatus.PAID);
        for (Seat seat : order.getSeats()) {
            seat.sell(); // 确认座位（将锁定状态改为已售出）
        }

        saveOrder(order);
        CinemaManager.getInstance().saveShows();

        notificationService.sendOrderUpdate(order.getUser(), order, "支付成功！您的座位已锁定。");
        displayService.updateSeatDisplay(order.getShow());
    }

    // 检查并处理过期的预订
    public void checkExpiredOrders() {
        // ... (保持原逻辑，并在过期时调用 notificationService 和 displayService) ...
        List<Order> expiredOrders = new ArrayList<>();

        for (Order order : orders.values()) {
            // 检查PENDING 或 RESERVED 订单是否过期
            if (order.isExpired() && (order.getStatus() == Order.OrderStatus.PENDING || order.getStatus() == Order.OrderStatus.RESERVED)) {
                expiredOrders.add(order);
            }
        }

        if (!expiredOrders.isEmpty()) {
            for (Order order : expiredOrders) {
                // 释放座位
                for (Seat seat : order.getSeats()) {
                    seat.unlock();
                }

                // 更新订单状态
                order.setStatus(Order.OrderStatus.EXPIRED);

                // 从用户订单列表中移除 (注意：第一段代码没有移除逻辑，此处沿用第二段代码的保留逻辑)
                if (order.getUser() != null) {
                    // 移除订单关联，但保留订单记录
                    order.getUser().removeOrder(order);
                }

                // 持久化更新
                saveOrder(order);
                CinemaManager.getInstance().saveShows();

                // 触发通知和显示服务
                notificationService.sendOrderUpdate(order.getUser(), order, "订单因超时已自动取消。");
                displayService.updateSeatDisplay(order.getShow());
            }
            // 批量保存订单和场次状态 (已在循环内 saveOrder 和 saveShows)
        }
    }

    // ================== 数据持久化 (使用 MySQLDataStorage) ==================
    private void loadOrders() {
        if (useMySQL) {
            orders.putAll(mysqlDataStorage.loadOrders());
        }
    }

    public void saveOrder(Order order) {
        if (useMySQL) {
            mysqlDataStorage.saveOrders(orders); // 简化处理，保存所有
        }
    }

    public void saveOrders() {
        if (useMySQL) {
            mysqlDataStorage.saveOrders(orders);
        }
    }

    private void rebuildUserOrderRelations() {
        CinemaManager cinemaManager = CinemaManager.getInstance();
        for (Order order : orders.values()) {
            if (order.getUser() != null) {
                User user = cinemaManager.getUser(order.getUser().getId());
                if (user != null) {
                    order.setUser(user);
                    if (!user.getOrders().contains(order)) {
                        user.addOrder(order);
                    }
                }
            }
        }
    }

    // ================== 关闭资源 ==================
    /**
     * 关闭数据库连接
     */
    public void shutdown() {
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.close();
            System.out.println("✓ BookingService已关闭MySQL连接");
        }
    }
}
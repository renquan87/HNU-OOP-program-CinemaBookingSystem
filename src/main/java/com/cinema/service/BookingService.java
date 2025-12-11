package com.cinema.service;

import com.cinema.model.*;
import com.cinema.strategy.PricingStrategy;
import com.cinema.storage.MySQLDataStorage;
import com.cinema.exception.*;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BookingService {
    private static BookingService instance;
    private final ConcurrentMap<String, Order> orders;
    private final PricingStrategy pricingStrategy;
    private final MySQLDataStorage mysqlDataStorage;
    private final boolean useMySQL;

    private BookingService(PricingStrategy pricingStrategy) {
        this.orders = new ConcurrentHashMap<>();
        this.pricingStrategy = pricingStrategy;
        
        // 强制使用MySQL存储
        MySQLDataStorage mysqlStorage = null;
        try {
            mysqlStorage = new MySQLDataStorage();
            System.out.println("✓ BookingService使用MySQL数据库存储");
        } catch (Exception e) {
            System.err.println("✗ BookingService MySQL连接失败: " + e.getMessage());
            System.err.println("错误：系统必须使用MySQL数据库存储");
            throw new RuntimeException("MySQL连接失败，无法初始化订单服务", e);
        }
        this.mysqlDataStorage = mysqlStorage;
        this.useMySQL = true; // 强制使用MySQL
        
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
            throw new IllegalStateException("BookingService not initialized. Call getInstance(PricingStrategy) first.");
        }
        return instance;
    }

    public Order createOrder(User user, Show show, List<String> seatIds) throws InvalidBookingException, SeatNotAvailableException {
        if (show == null) {
            throw new InvalidBookingException("场次不能为空");
        }

        if (user == null) {
            throw new InvalidBookingException("用户不能为空");
        }

        if (seatIds == null || seatIds.isEmpty()) {
            throw new InvalidBookingException("座位列表不能为空");
        }

        List<Seat> selectedSeats = new java.util.ArrayList<>();
        StringBuilder bookingDetails = new StringBuilder();
        bookingDetails.append("场次: ").append(show.getMovie().getTitle());
        bookingDetails.append(", 座位: ");

        for (String seatId : seatIds) {
            String[] parts = seatId.split("-");
            if (parts.length != 2) {
                throw new InvalidBookingException("座位ID格式无效: " + seatId, 
                    "期望格式: 行-列 (例如: 1-1)");
            }

            try {
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                Seat seat = show.getSeat(row, col);
                
                if (seat == null) {
                    throw new SeatNotAvailableException(seatId, "座位不存在");
                }
                
                if (!seat.isAvailable()) {
                    throw new SeatNotAvailableException(seatId, "座位已被预订");
                }
                
                selectedSeats.add(seat);
                bookingDetails.append(seatId).append(" ");
            } catch (NumberFormatException e) {
                throw new InvalidBookingException("座位ID格式无效: " + seatId, 
                    "行和列必须是数字", e);
            }
        }

        if (selectedSeats.isEmpty()) {
            throw new InvalidBookingException("没有选择有效的座位", bookingDetails.toString());
        }

        // Lock seats temporarily
        for (Seat seat : selectedSeats) {
            seat.lock();
        }

        Order order = new Order(
            "ORD-" + System.currentTimeMillis(),
            show,
            selectedSeats,
            java.time.LocalDateTime.now(),
            Order.OrderStatus.PENDING
        );

        orders.put(order.getOrderId(), order);
        order.setUser(user);
        user.addOrder(order);
        saveOrder(order);
        
        // 保存Show对象以同步座位状态
        CinemaManager cinemaManager = CinemaManager.getInstance();
        cinemaManager.saveShows();

        return order;
    }

    public void processPayment(Order order) throws PaymentFailedException {
        if (order == null) {
            throw new PaymentFailedException("", 0.0, "未知", "订单不能为空");
        }

        if (!orders.containsKey(order.getOrderId())) {
            throw new PaymentFailedException(order.getOrderId(), order.getTotalAmount(), "未知", "订单不存在");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new PaymentFailedException(order.getOrderId(), order.getTotalAmount(), "未知", 
                "订单状态不是待支付状态，无法支付");
        }

        // Process payment logic would go here
        boolean paymentSuccessful = true; // Simulate successful payment
        String paymentMethod = "支付宝"; // Simulate payment method

        if (paymentSuccessful) {
            order.processPayment();
            
            // Mark seats as sold
            for (Seat seat : order.getSeats()) {
                seat.sell();
            }
            
            saveOrder(order);
            // 保存Show对象以同步座位状态
            CinemaManager cinemaManager = CinemaManager.getInstance();
            cinemaManager.saveShows();
            System.out.println("订单支付成功并已保存到数据库");
        } else {
            // Unlock seats if payment fails
            for (Seat seat : order.getSeats()) {
                seat.unlock();
            }
            throw new PaymentFailedException(order.getOrderId(), order.getTotalAmount(), paymentMethod, 
                "支付处理失败，请检查支付信息");
        }
    }

    public void cancelOrder(Order order) throws InvalidBookingException {
        if (order == null) {
            throw new InvalidBookingException("订单不能为空");
        }

        if (!orders.containsKey(order.getOrderId())) {
            throw new InvalidBookingException("订单不存在", "订单号: " + order.getOrderId());
        }

        if (order.getStatus() == Order.OrderStatus.PAID) {
            // Refund logic would go here
            boolean refundSuccessful = true; // Simulate successful refund
            
            if (refundSuccessful) {
                order.refund();
                // Unlock seats
                for (Seat seat : order.getSeats()) {
                    seat.unlock();
                }
                saveOrder(order);
                // 保存Show对象以同步座位状态
                CinemaManager cinemaManager = CinemaManager.getInstance();
                cinemaManager.saveShows();
                System.out.println("订单退款成功并已保存到数据库");
            } else {
                throw new InvalidBookingException("退款失败", "订单号: " + order.getOrderId());
            }
        } else if (order.getStatus() == Order.OrderStatus.PENDING) {
            order.cancel();
            // Unlock seats
            for (Seat seat : order.getSeats()) {
                seat.unlock();
            }
            saveOrder(order);
            // 保存Show对象以同步座位状态
            CinemaManager cinemaManager = CinemaManager.getInstance();
            cinemaManager.saveShows();
            System.out.println("订单取消成功并已保存到数据库");
        } else if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new InvalidBookingException("订单已经取消", "订单号: " + order.getOrderId());
        } else {
            throw new InvalidBookingException("无法取消此状态的订单", 
                "订单号: " + order.getOrderId() + ", 状态: " + order.getStatus());
        }
    }

    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    public List<Order> getAllOrders() {
        return new java.util.ArrayList<>(orders.values());
    }
    
    public List<Order> getOrdersByUser(User user) {
        if (user == null) {
            return new java.util.ArrayList<>();
        }
        List<Order> userOrders = new java.util.ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getUser() != null && order.getUser().getId().equals(user.getId())) {
                userOrders.add(order);
            }
        }
        return userOrders;
    }
    
    // 刷新订单数据，从数据库重新加载
    public void refreshOrders() {
        System.out.println("正在刷新订单数据...");
        orders.clear();
        orders.putAll(mysqlDataStorage.loadOrders());
        rebuildUserOrderRelations();
        System.out.println("✓ 订单数据刷新完成，共 " + orders.size() + " 个订单");
    }

    public void updateOrder(Order order) {
        if (order != null && orders.containsKey(order.getOrderId())) {
            orders.put(order.getOrderId(), order);
            saveOrder(order);
            System.out.println("订单更新成功并已保存到数据库");
        }
    }

    public double calculateSeatPrice(Show show, Seat seat) {
        return pricingStrategy.calculatePrice(show, seat);
    }

    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }
    
    public void setPricingStrategy(PricingStrategy newPricingStrategy) {
        // 由于是单例模式，我们需要通过反射来修改pricingStrategy字段
        try {
            java.lang.reflect.Field field = BookingService.class.getDeclaredField("pricingStrategy");
            field.setAccessible(true);
            field.set(instance, newPricingStrategy);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException("无法更改定价策略: " + e.getMessage());
        }
    }
    
    private void loadOrders() {
        // 只从MySQL加载订单
        System.out.println("正在从MySQL数据库加载订单数据...");
        orders.putAll(mysqlDataStorage.loadOrders());
        System.out.println("✓ 订单数据加载完成，共 " + orders.size() + " 个订单");
    }
    
    private void saveOrder(Order order) {
        // 只保存到MySQL数据库
        mysqlDataStorage.saveOrders(orders);
    }
    
    public void saveOrders() {
        // 只保存到MySQL数据库
        mysqlDataStorage.saveOrders(orders);
        System.out.println("✓ 订单数据已保存到MySQL数据库");
    }
    
    private void rebuildUserOrderRelations() {
        CinemaManager cinemaManager = CinemaManager.getInstance();
        for (Order order : orders.values()) {
            if (order.getUser() != null) {
                User user = cinemaManager.getUser(order.getUser().getId());
                if (user != null) {
                    order.setUser(user);
                    // 检查用户是否已经有这个订单，避免重复添加
                    if (!user.getOrders().contains(order)) {
                        user.addOrder(order);
                    }
                }
            }
        }
    }
    
    // 预订座位（锁定15分钟）
    public Order reserveOrder(User user, Show show, List<String> seatIds) 
            throws InvalidBookingException, SeatNotAvailableException {
        if (show == null) {
            throw new InvalidBookingException("场次不能为空");
        }
        
        if (user == null) {
            throw new InvalidBookingException("用户不能为空");
        }
        
        if (seatIds == null || seatIds.isEmpty()) {
            throw new InvalidBookingException("座位列表不能为空");
        }
        
        // 检查座位可用性并锁定
        List<Seat> selectedSeats = new ArrayList<>();
        for (String seatId : seatIds) {
            Seat seat = show.getSeat(seatId);
            if (seat == null) {
                throw new InvalidBookingException("座位不存在: " + seatId);
            }
            
            if (!seat.isAvailable()) {
                throw new SeatNotAvailableException("座位不可用: " + seatId, "座位已被预订或售出");
            }
            
            // 锁定座位
            seat.lock();
            selectedSeats.add(seat);
        }
        
        // 创建预订订单
        String orderId = "ORDER-" + System.currentTimeMillis();
        Order order = new Order(orderId, show, selectedSeats, LocalDateTime.now(), Order.OrderStatus.RESERVED);
        order.setLockTime(LocalDateTime.now());
        order.setUser(user);
        
        // 添加到订单列表
        orders.put(orderId, order);
        user.addOrder(order);
        
        // 锁定座位
        for (Seat seat : selectedSeats) {
            seat.lock();
        }
        
        saveOrders();
        // 保存Show对象以同步座位状态
        CinemaManager cinemaManager = CinemaManager.getInstance();
        cinemaManager.saveShows();
        System.out.println("订单预订成功并已保存到数据库");
        
        return order;
    }
    
    // 检查并处理过期的预订
    public void checkExpiredOrders() {
        List<Order> expiredOrders = new ArrayList<>();
        
        for (Order order : orders.values()) {
            if (order.isExpired()) {
                expiredOrders.add(order);
            }
        }
        
        for (Order order : expiredOrders) {
            // 释放座位
            for (Seat seat : order.getSeats()) {
                seat.unlock();
            }
            
            // 更新订单状态
            order.setStatus(Order.OrderStatus.EXPIRED);
            
            // 从用户订单列表中移除
            if (order.getUser() != null) {
                order.getUser().removeOrder(order);
            }
            
            // 从订单列表中移除
            orders.remove(order.getOrderId());
        }
        
        if (!expiredOrders.isEmpty()) {
            saveOrders();
            // 保存Show对象以同步座位状态
            CinemaManager cinemaManager = CinemaManager.getInstance();
            cinemaManager.saveShows();
            System.out.println("过期订单处理完成，已保存到数据库");
        }
    }
    
    // 支付预订订单
    public void processReservedOrderPayment(Order order) throws PaymentFailedException, InvalidBookingException {
        if (order == null) {
            throw new InvalidBookingException("订单不存在");
        }
        
        if (order.getStatus() != Order.OrderStatus.RESERVED) {
            throw new InvalidBookingException("订单状态不是预订状态");
        }
        
        if (order.isExpired()) {
            throw new InvalidBookingException("预订已过期");
        }
        
        // 更新订单状态为已支付
        order.setStatus(Order.OrderStatus.PAID);
        
        // 确认座位（将锁定状态改为已售出）
        for (Seat seat : order.getSeats()) {
            seat.book();
        }
        
        saveOrders();
        // 保存Show对象以同步座位状态
        CinemaManager cinemaManager = CinemaManager.getInstance();
        cinemaManager.saveShows();
        System.out.println("预订订单支付成功并已保存到数据库");
    }
    
    public void shutdown() {
        saveOrders();
        if (useMySQL && mysqlDataStorage != null) {
            mysqlDataStorage.close();
        }
    }
}
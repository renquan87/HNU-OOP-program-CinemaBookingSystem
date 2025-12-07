package com.cinema.service;

import com.cinema.model.*;
import com.cinema.strategy.PricingStrategy;
import com.cinema.storage.SimpleDataStorage;
import com.cinema.exception.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BookingService {
    private static BookingService instance;
    private final ConcurrentMap<String, Order> orders;
    private final PricingStrategy pricingStrategy;
    private final SimpleDataStorage dataStorage;

    private BookingService(PricingStrategy pricingStrategy) {
        this.dataStorage = new SimpleDataStorage();
        this.orders = new ConcurrentHashMap<>();
        this.pricingStrategy = pricingStrategy;
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

    public void updateOrder(Order order) {
        if (order != null && orders.containsKey(order.getOrderId())) {
            orders.put(order.getOrderId(), order);
            saveOrder(order);
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
        orders.putAll(dataStorage.loadOrders());
    }
    
    private void saveOrder(Order order) {
        dataStorage.saveOrders(orders);
    }
    
    public void saveOrders() {
        dataStorage.saveOrders(orders);
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
}
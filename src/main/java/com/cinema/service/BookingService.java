package com.cinema.service;

import com.cinema.model.*;
import com.cinema.strategy.PricingStrategy;
import com.cinema.storage.SimpleDataStorage;

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

    public Order createOrder(User user, Show show, List<String> seatIds) {
        if (show == null) {
            throw new IllegalArgumentException("Show cannot be null");
        }

        List<Seat> selectedSeats = new java.util.ArrayList<>();
        for (String seatId : seatIds) {
            String[] parts = seatId.split("-");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid seat ID format: " + seatId);
            }

            try {
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                Seat seat = show.getSeat(row, col);
                
                if (seat == null) {
                    throw new IllegalArgumentException("Seat not found: " + seatId);
                }
                
                if (!seat.isAvailable()) {
                    throw new IllegalStateException("Seat not available: " + seatId);
                }
                
                selectedSeats.add(seat);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid seat ID format: " + seatId);
            }
        }

        if (selectedSeats.isEmpty()) {
            throw new IllegalArgumentException("No valid seats selected");
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

    public boolean processPayment(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        if (!orders.containsKey(order.getOrderId())) {
            throw new IllegalArgumentException("Order not found: " + order.getOrderId());
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            return false;
        }

        // Process payment logic would go here
        boolean paymentSuccessful = true; // Simulate successful payment

        if (paymentSuccessful) {
            order.processPayment();
            
            // Mark seats as sold
            for (Seat seat : order.getSeats()) {
                seat.sell();
            }
            
            saveOrder(order);
            return true;
        } else {
            // Unlock seats if payment fails
            for (Seat seat : order.getSeats()) {
                seat.unlock();
            }
            return false;
        }
    }

    public boolean cancelOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        if (!orders.containsKey(order.getOrderId())) {
            throw new IllegalArgumentException("Order not found: " + order.getOrderId());
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
                return true;
            }
            return false;
        } else if (order.getStatus() == Order.OrderStatus.PENDING) {
            order.cancel();
            // Unlock seats
            for (Seat seat : order.getSeats()) {
                seat.unlock();
            }
            saveOrder(order);
            return true;
        }

        return false;
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

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        // Note: This would require re-initializing the singleton
        throw new UnsupportedOperationException("Changing pricing strategy requires service restart");
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
                    user.addOrder(order);
                }
            }
        }
    }
}
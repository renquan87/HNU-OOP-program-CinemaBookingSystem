package com.cinema.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order implements java.io.Serializable {
    private String orderId;
    private Show show;
    private List<Seat> seats;
    private LocalDateTime createTime;
    private OrderStatus status;
    private double totalAmount;
    private User user;

    public enum OrderStatus {
        PENDING,
        PAID,
        CANCELLED,
        REFUNDED
    }

    public Order(String orderId, Show show, List<Seat> seats, LocalDateTime createTime, OrderStatus status) {
        this.orderId = orderId;
        this.show = show;
        this.seats = seats;
        this.createTime = createTime;
        this.status = status;
        this.totalAmount = calculateTotal();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
        this.totalAmount = calculateTotal();
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double calculateTotal() {
        double total = 0.0;
        for (Seat seat : seats) {
            total += seat.getBasePrice();
        }
        this.totalAmount = total;
        return total;
    }

    public boolean processPayment() {
        if (status == OrderStatus.PENDING) {
            status = OrderStatus.PAID;
            return true;
        }
        return false;
    }

    public boolean cancel() {
        if (status == OrderStatus.PENDING || status == OrderStatus.PAID) {
            status = OrderStatus.CANCELLED;
            return true;
        }
        return false;
    }

    public boolean refund() {
        if (status == OrderStatus.PAID) {
            status = OrderStatus.REFUNDED;
            return true;
        }
        return false;
    }

    public int getSeatCount() {
        return seats.size();
    }

    public String getSeatIds() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < seats.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(seats.get(i).getSeatId());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", movie='" + show.getMovie().getTitle() + '\'' +
                ", showTime=" + show.getStartTime() +
                ", seats=" + getSeatIds() +
                ", createTime=" + createTime +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
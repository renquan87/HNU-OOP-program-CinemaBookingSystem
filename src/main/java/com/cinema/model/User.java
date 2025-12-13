package com.cinema.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String password;
    private String phone;
    private String email;
    private List<Order> orders;
    private UserRole role;

    public enum UserRole {
        CUSTOMER,
        ADMIN
    }

    // [修改] 全参构造函数，增加 password
    public User(String id, String name, String password, String phone, String email, UserRole role) {
        this.id = id;
        this.name = name;
        this.password = password; // [新增]
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.orders = new ArrayList<>();
    }

    // [修改] 简化构造函数
    public User(String id, String name, String password, String phone, String email) {
        this(id, name, password, phone, email, UserRole.CUSTOMER);
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public void addOrder(Order order) {
        this.orders.add(order);
    }

    public void removeOrder(Order order) {
        this.orders.remove(order);
    }

    public List<Show> searchShows(String movieTitle, LocalDateTime date) {
        List<Show> matchingShows = new ArrayList<>();
        for (Order order : orders) {
            Show show = order.getShow();
            if (show.getMovie().getTitle().contains(movieTitle) &&
                show.getStartTime().toLocalDate().equals(date.toLocalDate())) {
                matchingShows.add(show);
            }
        }
        return matchingShows;
    }

    public Order createOrder(Show show, List<Seat> seats) {
        Order order = new Order(
            "ORD-" + System.currentTimeMillis(),
            show,
            seats,
            LocalDateTime.now(),
            Order.OrderStatus.PENDING
        );
        addOrder(order);
        return order;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", orderCount=" + orders.size() +
                '}';
    }
}

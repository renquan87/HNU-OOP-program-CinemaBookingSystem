package com.cinema.service;

import com.cinema.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SimpleTest {
    public static void main(String[] args) {
        testCinemaManager();
        testBookingService();
        System.out.println("All tests completed successfully!");
    }

    public static void testCinemaManager() {
        System.out.println("Testing CinemaManager...");
        
        CinemaManager cinemaManager = CinemaManager.getInstance();
        
        // Test singleton
        CinemaManager instance2 = CinemaManager.getInstance();
        if (cinemaManager != instance2) {
            throw new RuntimeException("CinemaManager singleton test failed");
        }
        
        // Test adding movie
        Movie movie = new Movie(
            "TEST-001",
            "测试电影",
            LocalDate.of(2023, 1, 1),
            List.of("演员1", "演员2"),
            "测试导演",
            120,
            8.0,
            "测试描述",
            "测试类型"
        );

        cinemaManager.addMovie(movie);
        Movie retrieved = cinemaManager.getMovie("TEST-001");

        if (retrieved == null || !retrieved.getTitle().equals("测试电影")) {
            throw new RuntimeException("Movie retrieval test failed");
        }

        // Test adding screening room
        ScreeningRoom room = new ScreeningRoom("TEST-ROOM", "测试厅", 5, 8);
        cinemaManager.addScreeningRoom(room);

        ScreeningRoom retrievedRoom = cinemaManager.getScreeningRoom("TEST-ROOM");
        if (retrievedRoom == null || !retrievedRoom.getName().equals("测试厅")) {
            throw new RuntimeException("ScreeningRoom retrieval test failed");
        }

        System.out.println("CinemaManager tests passed!");
    }

    public static void testBookingService() {
        System.out.println("Testing BookingService...");
        
        CinemaManager cinemaManager = CinemaManager.getInstance();
        BookingService bookingService = BookingService.getInstance(new com.cinema.strategy.StandardPricing());
        
        // Create test user
        User testUser = new User("TEST-USER", "测试用户", "13800138000", "test@example.com");
        cinemaManager.addUser(testUser);

        // Create test show
        Movie movie = new Movie(
            "TEST-MOVIE",
            "测试电影",
            LocalDate.of(2023, 1, 1),
            List.of("演员1"),
            "导演",
            120,
            8.0,
            "描述",
            "类型"
        );

        ScreeningRoom room = new ScreeningRoom("TEST-ROOM-BOOKING", "测试厅2", 3, 5);
        cinemaManager.addScreeningRoom(room);

        Show testShow = new Show(
            "TEST-SHOW",
            movie,
            room,
            LocalDateTime.now().plusDays(1),
            50.0
        );

        cinemaManager.addMovie(movie);
        cinemaManager.addShow(testShow);

        // Test creating order
        List<String> seatIds = List.of("1-1", "1-2");
        Order order = bookingService.createOrder(testUser, testShow, seatIds);

        if (order == null || order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Order creation test failed");
        }

        if (order.getSeatCount() != 2) {
            throw new RuntimeException("Order seat count test failed");
        }

        // Test payment
        boolean paymentResult = bookingService.processPayment(order);
        if (!paymentResult || order.getStatus() != Order.OrderStatus.PAID) {
            throw new RuntimeException("Payment processing test failed");
        }

        // Check seat is sold
        Seat seat = testShow.getSeat(1, 1);
        if (seat.getStatus() != Seat.SeatStatus.SOLD) {
            throw new RuntimeException("Seat status test failed");
        }

        System.out.println("BookingService tests passed!");
    }
}
package com.cinema.service;

import com.cinema.model.*;
import com.cinema.strategy.StandardPricing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {
    private BookingService bookingService;
    private CinemaManager cinemaManager;
    private User testUser;
    private Show testShow;

    @BeforeEach
    void setUp() {
        bookingService = BookingService.getInstance(new StandardPricing());
        cinemaManager = CinemaManager.getInstance();
        
        testUser = new User("TEST-USER", "测试用户", "13800138000", "test@example.com");
        cinemaManager.addUser(testUser);

        Movie movie = new Movie(
            "TEST-MOVIE",
            "测试电影",
            java.time.LocalDate.of(2023, 1, 1),
            List.of("演员1"),
            "导演",
            120,
            8.0,
            "描述",
            "类型"
        );

        ScreeningRoom room = new ScreeningRoom("TEST-ROOM", "测试厅", 3, 5);
        cinemaManager.addScreeningRoom(room);

        testShow = new Show(
            "TEST-SHOW",
            movie,
            room,
            LocalDateTime.now().plusDays(1),
            50.0
        );

        cinemaManager.addMovie(movie);
        cinemaManager.addShow(testShow);
    }

    @Test
    void testCreateOrder() {
        List<String> seatIds = List.of("1-1", "1-2");
        Order order = bookingService.createOrder(testUser, testShow, seatIds);

        assertNotNull(order);
        assertEquals(Order.OrderStatus.PENDING, order.getStatus());
        assertEquals(2, order.getSeatCount());
        assertEquals(testShow, order.getShow());
        assertTrue(testUser.getOrders().contains(order));
    }

    @Test
    void testCreateOrderWithInvalidSeat() {
        List<String> seatIds = List.of("10-10"); // Invalid seat
        assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createOrder(testUser, testShow, seatIds);
        });
    }

    @Test
    void testProcessPayment() {
        List<String> seatIds = List.of("1-1");
        Order order = bookingService.createOrder(testUser, testShow, seatIds);

        boolean result = bookingService.processPayment(order);
        assertTrue(result);
        assertEquals(Order.OrderStatus.PAID, order.getStatus());

        // Check seat is sold
        Seat seat = testShow.getSeat(1, 1);
        assertEquals(Seat.SeatStatus.SOLD, seat.getStatus());
    }

    @Test
    void testCancelPendingOrder() {
        List<String> seatIds = List.of("1-1");
        Order order = bookingService.createOrder(testUser, testShow, seatIds);

        boolean result = bookingService.cancelOrder(order);
        assertTrue(result);
        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());

        // Check seat is unlocked
        Seat seat = testShow.getSeat(1, 1);
        assertEquals(Seat.SeatStatus.AVAILABLE, seat.getStatus());
    }

    @Test
    void testRefundPaidOrder() {
        List<String> seatIds = List.of("1-1");
        Order order = bookingService.createOrder(testUser, testShow, seatIds);
        bookingService.processPayment(order);

        boolean result = bookingService.cancelOrder(order);
        assertTrue(result);
        assertEquals(Order.OrderStatus.REFUNDED, order.getStatus());

        // Check seat is unlocked
        Seat seat = testShow.getSeat(1, 1);
        assertEquals(Seat.SeatStatus.AVAILABLE, seat.getStatus());
    }

    @Test
    void testCalculateSeatPrice() {
        Seat seat = testShow.getSeat(1, 1);
        double price = bookingService.calculateSeatPrice(testShow, seat);
        
        assertTrue(price > 0);
        assertEquals(testShow.getBasePrice(), price, 0.01); // Standard pricing
    }

    @Test
    void testGetOrder() {
        List<String> seatIds = List.of("1-1");
        Order order = bookingService.createOrder(testUser, testShow, seatIds);

        Order retrieved = bookingService.getOrder(order.getOrderId());
        assertNotNull(retrieved);
        assertEquals(order.getOrderId(), retrieved.getOrderId());
    }

    @Test
    void testGetAllOrders() {
        List<String> seatIds = List.of("1-1");
        bookingService.createOrder(testUser, testShow, seatIds);

        List<Order> orders = bookingService.getAllOrders();
        assertNotNull(orders);
        assertFalse(orders.isEmpty());
    }
}
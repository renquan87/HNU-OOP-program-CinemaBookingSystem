package com.cinema.exception;

/**
 * 无效预订异常
 * 当预订请求包含无效信息时抛出
 */
public class InvalidBookingException extends Exception {
    private final String bookingDetails;

    public InvalidBookingException(String message) {
        super(message);
        this.bookingDetails = "";
    }

    public InvalidBookingException(String message, String bookingDetails) {
        super(message + " - 预订详情: " + bookingDetails);
        this.bookingDetails = bookingDetails;
    }

    public InvalidBookingException(String message, Throwable cause) {
        super(message, cause);
        this.bookingDetails = "";
    }

    public InvalidBookingException(String message, String bookingDetails, Throwable cause) {
        super(message + " - 预订详情: " + bookingDetails, cause);
        this.bookingDetails = bookingDetails;
    }

    public String getBookingDetails() {
        return bookingDetails;
    }
}
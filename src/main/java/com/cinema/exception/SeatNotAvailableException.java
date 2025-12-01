package com.cinema.exception;

/**
 * 座位不可用异常
 * 当试图预订已被占用或不存在的座位时抛出
 */
public class SeatNotAvailableException extends Exception {
    private final String seatId;
    private final String reason;

    public SeatNotAvailableException(String seatId) {
        super("座位不可用: " + seatId);
        this.seatId = seatId;
        this.reason = "座位已被预订";
    }

    public SeatNotAvailableException(String seatId, String reason) {
        super("座位不可用: " + seatId + " - " + reason);
        this.seatId = seatId;
        this.reason = reason;
    }

    public SeatNotAvailableException(String seatId, String reason, Throwable cause) {
        super("座位不可用: " + seatId + " - " + reason, cause);
        this.seatId = seatId;
        this.reason = reason;
    }

    public String getSeatId() {
        return seatId;
    }

    public String getReason() {
        return reason;
    }
}
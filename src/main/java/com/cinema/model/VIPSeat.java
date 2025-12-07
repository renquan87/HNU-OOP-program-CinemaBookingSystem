package com.cinema.model;

public class VIPSeat extends Seat implements java.io.Serializable {
    private static final double PRICE_MULTIPLIER = 2.0; // VIP座位价格为基准价格的2倍

    public VIPSeat(int row, int col) {
        super(row, col, 50.0 * PRICE_MULTIPLIER); // 默认基准价格50元
    }

    public VIPSeat(int row, int col, double basePrice) {
        super(row, col, basePrice * PRICE_MULTIPLIER);
    }

    @Override
    public String toString() {
        return "VIPSeat{" +
                "row=" + row +
                ", col=" + col +
                ", status=" + status +
                ", basePrice=" + basePrice +
                '}';
    }
}
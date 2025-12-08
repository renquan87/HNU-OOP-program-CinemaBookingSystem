package com.cinema.model;

public class VIPSeat extends Seat implements java.io.Serializable {
    private static final double PRICE_PREMIUM = 10.0; // VIP座位比普通座位贵10元

    public VIPSeat(int row, int col) {
        super(row, col, 50.0 + PRICE_PREMIUM); // 默认基准价格50元 + 10元
    }

    public VIPSeat(int row, int col, double basePrice) {
        super(row, col, basePrice + PRICE_PREMIUM);
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
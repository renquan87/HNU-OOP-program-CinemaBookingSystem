package com.cinema.model;

public class RegularSeat extends Seat implements java.io.Serializable {
    private static final double DEFAULT_BASE_PRICE = 50.0; // 统一基准价格

    public RegularSeat(int row, int col) {
        super(row, col, DEFAULT_BASE_PRICE);
    }

    public RegularSeat(int row, int col, double basePrice) {
        super(row, col, basePrice);
    }

    @Override
    public String toString() {
        return "RegularSeat{" +
                "row=" + row +
                ", col=" + col +
                ", status=" + status +
                ", basePrice=" + basePrice +
                '}';
    }
}
package com.cinema.model;

public class VIPSeat extends Seat implements java.io.Serializable {
    private static final double DEFAULT_BASE_PRICE = 60.0;

    public VIPSeat(int row, int col) {
        super(row, col, DEFAULT_BASE_PRICE);
    }

    public VIPSeat(int row, int col, double basePrice) {
        super(row, col, basePrice);
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
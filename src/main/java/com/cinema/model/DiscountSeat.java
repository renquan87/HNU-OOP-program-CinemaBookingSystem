package com.cinema.model;

public class DiscountSeat extends Seat implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    
    public DiscountSeat(int row, int col, double basePrice) {
        super(row, col, basePrice * 0.8); // 优惠座位价格为基准价格的80%
    }
    
    @Override
    public String toString() {
        return "DiscountSeat{" +
                "row=" + getRow() +
                ", col=" + getCol() +
                ", status=" + getStatus() +
                ", basePrice=" + getBasePrice() +
                '}';
    }
}
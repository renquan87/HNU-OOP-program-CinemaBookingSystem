package com.cinema.model;

public class DiscountSeat extends Seat implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    
    public DiscountSeat(int row, int col, double basePrice) {
        super(row, col, basePrice); // 使用传入的价格（已经是折扣价）
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
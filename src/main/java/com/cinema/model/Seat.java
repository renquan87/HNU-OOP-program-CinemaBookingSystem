package com.cinema.model;

public abstract class Seat implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    protected int row;
    protected int col;
    protected SeatStatus status;
    protected double basePrice;

    public enum SeatStatus {
        AVAILABLE,
        LOCKED,
        SOLD
    }

    public Seat(int row, int col, double basePrice) {
        this.row = row;
        this.col = col;
        this.basePrice = basePrice;
        this.status = SeatStatus.AVAILABLE;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    public boolean isLocked() {
        return status == SeatStatus.LOCKED;
    }

    public void lock() {
        this.status = SeatStatus.LOCKED;
    }

    public void unlock() {
        this.status = SeatStatus.AVAILABLE;
    }

    public void sell() {
        this.status = SeatStatus.SOLD;
    }

    public String getSeatId() {
        return row + "-" + col;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "row=" + row +
                ", col=" + col +
                ", status=" + status +
                ", basePrice=" + basePrice +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Seat seat = (Seat) obj;
        return row == seat.row && col == seat.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }
}
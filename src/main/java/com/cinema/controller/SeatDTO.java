package com.cinema.controller;

public class SeatDTO {
    public String id;        // 座位ID (如 "1-1")
    public int row;
    public int col;
    public String type;      // "regular", "vip", "discount"
    public String status;    // "available", "locked", "sold"
    public double price;     // 该座位在该场次的实际价格

    public SeatDTO(String id, int row, int col, String type, String status, double price) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.type = type;
        this.status = status;
        this.price = price;
    }
}
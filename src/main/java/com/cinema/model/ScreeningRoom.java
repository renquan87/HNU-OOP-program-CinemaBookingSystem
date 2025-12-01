package com.cinema.model;

import java.util.ArrayList;
import java.util.List;

public class ScreeningRoom implements java.io.Serializable {
    private String id;
    private String name;
    private String layout;
    private Seat[][] seatLayout;
    private int totalRows;
    private int totalCols;

    public ScreeningRoom(String id, String name, int totalRows, int totalCols) {
        this.id = id;
        this.name = name;
        this.totalRows = totalRows;
        this.totalCols = totalCols;
        this.seatLayout = new Seat[totalRows][totalCols];
        this.layout = totalRows + "x" + totalCols;
        initializeSeats();
    }

    private void initializeSeats() {
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < totalCols; col++) {
                // First 3 rows are VIP seats, rest are regular seats
                if (row < 3) {
                    seatLayout[row][col] = new VIPSeat(row + 1, col + 1);
                } else {
                    seatLayout[row][col] = new RegularSeat(row + 1, col + 1);
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public Seat[][] getSeatLayout() {
        return seatLayout;
    }

    public void setSeatLayout(Seat[][] seatLayout) {
        this.seatLayout = seatLayout;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getTotalCols() {
        return totalCols;
    }

    public List<Seat> getAvailableSeats() {
        List<Seat> availableSeats = new ArrayList<>();
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < totalCols; col++) {
                Seat seat = seatLayout[row][col];
                if (seat.isAvailable()) {
                    availableSeats.add(seat);
                }
            }
        }
        return availableSeats;
    }

    public Seat getSeat(int row, int col) {
        if (row >= 1 && row <= totalRows && col >= 1 && col <= totalCols) {
            return seatLayout[row - 1][col - 1];
        }
        return null;
    }

    public int getTotalSeats() {
        return totalRows * totalCols;
    }

    public int getAvailableSeatsCount() {
        return getAvailableSeats().size();
    }

    @Override
    public String toString() {
        return "ScreeningRoom{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", layout='" + layout + '\'' +
                ", totalSeats=" + getTotalSeats() +
                ", availableSeats=" + getAvailableSeatsCount() +
                '}';
    }
}
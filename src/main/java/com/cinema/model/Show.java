package com.cinema.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Show implements java.io.Serializable {
    private String id;
    private Movie movie;
    private ScreeningRoom screeningRoom;
    private LocalDateTime startTime;
    private double basePrice;
    private List<Seat> seats;

    public Show(String id, Movie movie, ScreeningRoom screeningRoom, LocalDateTime startTime, double basePrice) {
        this.id = id;
        this.movie = movie;
        this.screeningRoom = screeningRoom;
        this.startTime = startTime;
        this.basePrice = basePrice;
        this.seats = new ArrayList<>();
        initializeSeats();
    }

    private void initializeSeats() {
        Seat[][] roomSeats = screeningRoom.getSeatLayout();
        for (int row = 0; row < roomSeats.length; row++) {
            for (int col = 0; col < roomSeats[row].length; col++) {
                Seat originalSeat = roomSeats[row][col];
                Seat showSeat;
                if (originalSeat instanceof VIPSeat) {
                    showSeat = new VIPSeat(originalSeat.getRow(), originalSeat.getCol(), basePrice * 2);
                } else {
                    showSeat = new RegularSeat(originalSeat.getRow(), originalSeat.getCol(), basePrice);
                }
                seats.add(showSeat);
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public ScreeningRoom getScreeningRoom() {
        return screeningRoom;
    }

    public void setScreeningRoom(ScreeningRoom screeningRoom) {
        this.screeningRoom = screeningRoom;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
        updateSeatPrices();
    }

    private void updateSeatPrices() {
        for (Seat seat : seats) {
            if (seat instanceof VIPSeat) {
                seat.setBasePrice(basePrice * 2);
            } else {
                seat.setBasePrice(basePrice);
            }
        }
    }

    public List<Seat> getSeats() {
        return new ArrayList<>(seats);
    }

    public List<Seat> getAvailableSeats() {
        List<Seat> availableSeats = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.isAvailable()) {
                availableSeats.add(seat);
            }
        }
        return availableSeats;
    }

    public Seat getSeat(int row, int col) {
        for (Seat seat : seats) {
            if (seat.getRow() == row && seat.getCol() == col) {
                return seat;
            }
        }
        return null;
    }

    public boolean hostSell(List<Seat> selectedSeats) {
        for (Seat seat : selectedSeats) {
            if (!seat.isAvailable()) {
                return false;
            }
        }
        
        for (Seat seat : selectedSeats) {
            seat.sell();
        }
        
        return true;
    }

    public int getTotalSeats() {
        return seats.size();
    }

    public int getAvailableSeatsCount() {
        return getAvailableSeats().size();
    }

    public int getSoldSeatsCount() {
        int count = 0;
        for (Seat seat : seats) {
            if (seat.getStatus() == Seat.SeatStatus.SOLD) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return "Show{" +
                "id='" + id + '\'' +
                ", movie=" + movie.getTitle() +
                ", screeningRoom=" + screeningRoom.getName() +
                ", startTime=" + startTime +
                ", basePrice=" + basePrice +
                ", availableSeats=" + getAvailableSeatsCount() + "/" + getTotalSeats() +
                '}';
    }
}
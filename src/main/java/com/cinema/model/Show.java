package com.cinema.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Show implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private Movie movie;
    private ScreeningRoom screeningRoom;
    private LocalDateTime startTime;
    private double basePrice;
    private double discountPrice; // 优惠座位价格
    private double vipPrice; // VIP座位价格
    private List<Seat> seats;

    public Show(String id, Movie movie, ScreeningRoom screeningRoom, LocalDateTime startTime, double basePrice) {
        this.id = id;
        this.movie = movie;
        this.screeningRoom = screeningRoom;
        this.startTime = startTime;
        this.basePrice = basePrice;
        this.discountPrice = basePrice * 0.8; // 默认优惠价格为基准价格的80%
        this.vipPrice = basePrice + 10.0; // 默认VIP价格为基准价格+10元
        this.seats = new ArrayList<>();
        initializeSeats();
    }
    
    // 新的构造函数，允许设置三种价格
    public Show(String id, Movie movie, ScreeningRoom screeningRoom, LocalDateTime startTime, 
                double basePrice, double discountPrice, double vipPrice) {
        this.id = id;
        this.movie = movie;
        this.screeningRoom = screeningRoom;
        this.startTime = startTime;
        this.basePrice = basePrice;
        this.discountPrice = discountPrice;
        this.vipPrice = vipPrice;
        this.seats = new ArrayList<>();
        initializeSeats();
    }
    
    // 用于数据库加载的构造函数，不初始化座位
    public Show(String id, LocalDateTime startTime, double basePrice) {
        this.id = id;
        this.movie = null;
        this.screeningRoom = null;
        this.startTime = startTime;
        this.basePrice = basePrice;
        this.discountPrice = basePrice * 0.8;
        this.vipPrice = basePrice + 10.0;
        this.seats = new ArrayList<>();
        // 不调用initializeSeats()
    }

    private void initializeSeats() {
        Seat[][] roomSeats = screeningRoom.getSeatLayout();
        for (int row = 0; row < roomSeats.length; row++) {
            for (int col = 0; col < roomSeats[row].length; col++) {
                // 创建新的座位实例，确保价格正确
                Seat roomSeat = roomSeats[row][col];
                Seat showSeat;
                
                if (roomSeat instanceof VIPSeat) {
                    showSeat = new VIPSeat(roomSeat.getRow(), roomSeat.getCol(), vipPrice);
                } else if (roomSeat instanceof DiscountSeat) {
                    showSeat = new DiscountSeat(roomSeat.getRow(), roomSeat.getCol(), discountPrice);
                } else {
                    showSeat = new RegularSeat(roomSeat.getRow(), roomSeat.getCol(), basePrice);
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
    
    // 安全获取电影标题，避免空指针
    public String getMovieTitle() {
        return movie != null ? movie.getTitle() : "未知电影";
    }
    
    // 安全获取电影ID，避免空指针
    public String getMovieId() {
        return movie != null ? movie.getId() : "UNKNOWN";
    }
    
    // 安全获取放映厅名称，避免空指针
    public String getScreeningRoomName() {
        return screeningRoom != null ? screeningRoom.getName() : "未知放映厅";
    }
    
    // 安全获取放映厅ID，避免空指针
    public String getScreeningRoomId() {
        return screeningRoom != null ? screeningRoom.getId() : "UNKNOWN";
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
    
    public double getDiscountPrice() {
        return discountPrice;
    }
    
    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
        updateSeatPrices();
    }
    
    public double getVipPrice() {
        return vipPrice;
    }
    
    public void setVipPrice(double vipPrice) {
        this.vipPrice = vipPrice;
        updateSeatPrices();
    }

    private void updateSeatPrices() {
        for (Seat seat : seats) {
            if (seat instanceof VIPSeat) {
                seat.setBasePrice(vipPrice);
            } else if (seat instanceof DiscountSeat) {
                seat.setBasePrice(discountPrice);
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
    
    public Seat getSeat(String seatId) {
        String[] parts = seatId.split("-");
        if (parts.length != 2) {
            return null;
        }
        try {
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            return getSeat(row, col);
        } catch (NumberFormatException e) {
            return null;
        }
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
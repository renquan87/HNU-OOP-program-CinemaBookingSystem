package com.cinema.controller;

import com.cinema.model.Show;
import com.cinema.service.CinemaManager;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cinema.model.Seat;
import com.cinema.service.BookingService;
import com.cinema.model.VIPSeat;
import com.cinema.model.DiscountSeat;

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    @GetMapping
    public Map<String, Object> getShows(@RequestParam(required = false) String movieId) {
        CinemaManager manager = CinemaManager.getInstance();
        List<Show> shows;

        // 如果前端传了 movieId，就只查这部电影的场次
        if (movieId != null && !movieId.isEmpty()) {
            shows = manager.getShowsByMovie(movieId);
        } else {
            // 否则查所有
            shows = manager.getAllShows();
        }

        // 构造简单的 DTO 返回给前端，避免嵌套过深
        List<Map<String, Object>> showList = new ArrayList<>();
        for (Show show : shows) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", show.getId());
            item.put("movieTitle", show.getMovieTitle());
            item.put("roomName", show.getScreeningRoomName());
            item.put("startTime", show.getStartTime().toString());
            item.put("basePrice", show.getBasePrice());
            item.put("availableSeats", show.getAvailableSeatsCount());
            item.put("totalSeats", show.getTotalSeats());
            showList.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", 200);
        response.put("data", showList);
        return response;
    }

    @GetMapping("/{id}/seats")
    public Map<String, Object> getShowSeats(@PathVariable String id) {
        CinemaManager manager = CinemaManager.getInstance();
        Show show = manager.getShow(id);

        if (show == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "场次不存在");
            return response;
        }

        BookingService bookingService = BookingService.getInstance();
        List<SeatDTO> seatList = new ArrayList<>();

        // 遍历场次中的所有座位
        for (Seat seat : show.getSeats()) {
            // 计算具体价格（策略模式）
            double price = bookingService.calculateSeatPrice(show, seat);

            // 判断类型
            String type = "regular";
            if (seat instanceof VIPSeat) type = "vip";
            else if (seat instanceof DiscountSeat) type = "discount";

            // 转换状态
            String status = "available";
            if (!seat.isAvailable()) {
                status = seat.isLocked() ? "locked" : "sold";
            }

            seatList.add(new SeatDTO(
                    seat.getSeatId(),
                    seat.getRow(),
                    seat.getCol(),
                    type,
                    status,
                    price
            ));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", 200);
        response.put("data", seatList);
        return response;
    }
}


package com.cinema.controller;

import com.cinema.model.Movie;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Show;
import com.cinema.service.CinemaManager;
import com.cinema.model.Seat;
import com.cinema.service.BookingService;
import com.cinema.model.VIPSeat;
import com.cinema.model.DiscountSeat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 新增：接收前端添加场次的参数
class ShowRequest {
    public String movieId;
    public String roomId;
    public String startTime; // 格式 "yyyy-MM-dd HH:mm"
    public double price;
}

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    // ... 保留原有的 getShows 和 getShowSeats 方法 ...
    // ... (此处省略原有的 @GetMapping 代码，请保持不变) ...

    @GetMapping
    public Map<String, Object> getShows(@RequestParam(required = false) String movieId) {
        CinemaManager manager = CinemaManager.getInstance();
        List<Show> shows;
        if (movieId != null && !movieId.isEmpty()) {
            shows = manager.getShowsByMovie(movieId);
        } else {
            shows = manager.getAllShows();
        }

        LocalDateTime now = LocalDateTime.now();
        shows.sort(Comparator.comparing(Show::getStartTime));

        List<Map<String, Object>> upcomingShows = new ArrayList<>();
        List<Map<String, Object>> historyShows = new ArrayList<>();

        for (Show show : shows) {
            Map<String, Object> item = toShowDto(show, now);
            if (show.getStartTime().isAfter(now)) {
                upcomingShows.add(item);
            } else {
                historyShows.add(item);
            }
        }

        historyShows.sort((a, b) -> {
            LocalDateTime t1 = LocalDateTime.parse(a.get("startTime").toString());
            LocalDateTime t2 = LocalDateTime.parse(b.get("startTime").toString());
            return t2.compareTo(t1);
        });

        Map<String, Object> payload = new HashMap<>();
        payload.put("serverTime", now.toString());
        payload.put("upcomingShows", upcomingShows);
        payload.put("historyShows", historyShows);
        return buildResponse(200, "获取成功", payload);
    }

    // ... 原有的 getShowSeats ...
    @GetMapping("/{id}/seats")
    public Map<String, Object> getShowSeats(@PathVariable String id) {
        // ... (保持原代码不变) ...
        // 为了完整性，这里简略写，请保留你原始文件中的 getShowSeats 逻辑
        CinemaManager manager = CinemaManager.getInstance();
        Show show = manager.getShow(id);
        if (show == null) return buildResponse(404, "场次不存在", null);

        BookingService bookingService = BookingService.getInstance();
        List<SeatDTO> seatList = new ArrayList<>();
        for (Seat seat : show.getSeats()) {
            double price = bookingService.calculateSeatPrice(show, seat);
            String type = "regular";
            if (seat instanceof VIPSeat) type = "vip";
            else if (seat instanceof DiscountSeat) type = "discount";
            String status = "available";
            if (!seat.isAvailable()) status = seat.isLocked() ? "locked" : "sold";
            seatList.add(new SeatDTO(seat.getSeatId(), seat.getRow(), seat.getCol(), type, status, price));
        }
        return buildResponse(200, "获取成功", seatList);
    }

    // 🔴 新增：添加场次接口 (供管理员使用)
    @PostMapping
    public Map<String, Object> addShow(@RequestBody ShowRequest req) {
        try {
            CinemaManager manager = CinemaManager.getInstance();

            Movie movie = manager.getMovie(req.movieId);
            if (movie == null) return buildResponse(400, "电影不存在", null);

            ScreeningRoom room = manager.getScreeningRoom(req.roomId);
            if (room == null) return buildResponse(400, "放映厅不存在", null);

            // 解析时间 "2023-12-12 14:00" -> LocalDateTime
            // 前端传来的时间可能带有 'T' 或者没有，建议前端统一格式，或者后端做兼容
            // 这里假设前端传的是标准 ISO 格式或者 yyyy-MM-dd HH:mm
            LocalDateTime start = LocalDateTime.parse(req.startTime.replace(" ", "T"));

            Show show = new Show(
                    "SHOW-" + System.currentTimeMillis(),
                    movie,
                    room,
                    start,
                    req.price
            );

            manager.addShow(show);
            return buildResponse(200, "排片成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return buildResponse(500, "排片失败: " + e.getMessage(), null);
        }
    }

    // 🔴 新增：删除场次接口 (供管理员使用)
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteShow(@PathVariable String id) {
        CinemaManager manager = CinemaManager.getInstance();
        if (manager.getShow(id) == null) {
            return buildResponse(404, "场次不存在", null);
        }
        manager.removeShow(id);
        return buildResponse(200, "删除成功", null);
    }

    private Map<String, Object> toShowDto(Show show, LocalDateTime now) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", show.getId());
        item.put("movieTitle", show.getMovieTitle());
        item.put("movieId", show.getMovieId());
        item.put("roomName", show.getScreeningRoomName());
        item.put("roomId", show.getScreeningRoomId());
        item.put("startTime", show.getStartTime().toString());
        item.put("basePrice", show.getBasePrice());
        item.put("availableSeats", show.getAvailableSeatsCount());
        item.put("totalSeats", show.getTotalSeats());
        item.put("status", show.getStartTime().isAfter(now) ? "UPCOMING" : "HISTORY");
        return item;
    }

    private Map<String, Object> buildResponse(int code, String msg, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", code == 200);
        response.put("code", code);
        response.put("message", msg);
        response.put("data", data);
        return response;
    }
}
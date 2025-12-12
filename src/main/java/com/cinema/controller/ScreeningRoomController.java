package com.cinema.controller;

import com.cinema.model.ScreeningRoom;
import com.cinema.service.CinemaManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class ScreeningRoomController {

    @GetMapping
    public Map<String, Object> getAllRooms() {
        CinemaManager manager = CinemaManager.getInstance();
        List<ScreeningRoom> rooms = manager.getAllScreeningRooms();

        // 简化返回数据
        List<Map<String, Object>> roomList = new ArrayList<>();
        for (ScreeningRoom room : rooms) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", room.getId());
            item.put("name", room.getName());
            item.put("capacity", room.getTotalSeats());
            roomList.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("data", roomList);
        return response;
    }
}
package com.cinema.controller;

import com.cinema.model.AppNotification;
import com.cinema.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notice")
public class NotificationController {

    @GetMapping("/list")
    public Map<String, Object> getNotices(@RequestParam String userId) {
        Map<String, Object> response = new HashMap<>();

        List<AppNotification> list = NotificationService.getInstance().getUserNotifications(userId);

        response.put("success", true);
        response.put("code", 200);
        response.put("data", list);
        return response;
    }
}
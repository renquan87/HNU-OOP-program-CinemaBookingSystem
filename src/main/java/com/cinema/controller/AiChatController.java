package com.cinema.controller;

import com.cinema.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

class ChatRequest {
    public String message;
}

@RestController
@RequestMapping("/api/chat")
public class AiChatController {

    @PostMapping("/ask")
    public Map<String, Object> askAi(@RequestBody ChatRequest req) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 调用 AI 服务
            String aiResponse = AiService.getInstance().getAnswer(req.message);

            response.put("success", true);
            response.put("code", 200);
            response.put("data", aiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "AI 大脑过载了: " + e.getMessage());
        }
        return response;
    }
}
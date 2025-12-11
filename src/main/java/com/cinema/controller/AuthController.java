package com.cinema.controller;

import com.cinema.model.User;
import com.cinema.service.CinemaManager;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// 登录请求参数
class LoginRequest {
    public String username;
    public String password;
}

// 注册请求参数
class RegisterRequest {
    public String username; // 作为 User ID
    public String password;
    public String nickname; // 姓名
    public String phone;
    public String email;
}

@RestController
@RequestMapping("/api")
public class AuthController {

    // 1. 登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        CinemaManager manager = CinemaManager.getInstance();
        User user = manager.getUser(request.username);

        // 核心修改：校验密码
        if (user != null && user.getPassword() != null && user.getPassword().equals(request.password)) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getName());
            data.put("accessToken", "fake-token-" + user.getId());
            data.put("roles", user.isAdmin() ? new String[]{"admin"} : new String[]{"common"});
            data.put("expires", "2030/01/01 00:00:00");

            response.put("success", true);
            response.put("code", 200);
            response.put("data", data);
            response.put("message", "登录成功");
        } else {
            response.put("success", false);
            response.put("code", 401);
            response.put("message", "账号或密码错误");
        }
        return response;
    }

    // 2. 注册接口
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();
        CinemaManager manager = CinemaManager.getInstance();

        // 检查用户是否已存在
        if (manager.getUser(request.username) != null) {
            response.put("success", false);
            response.put("code", 400);
            response.put("message", "用户ID已存在");
            return response;
        }

        // 创建新用户
        User newUser = new User(
                request.username,
                request.nickname,
                request.password,
                request.phone,
                request.email
        );

        manager.addUser(newUser);

        // 注册成功后直接返回部分信息
        Map<String, Object> data = new HashMap<>();
        data.put("username", request.username);
        data.put("name", request.nickname);

        response.put("success", true);
        response.put("code", 200);
        response.put("data", data);
        response.put("message", "注册成功，请登录");

        return response;
    }
}
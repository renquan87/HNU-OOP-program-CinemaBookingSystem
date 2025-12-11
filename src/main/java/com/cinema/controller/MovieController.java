package com.cinema.controller;

import com.cinema.model.Movie;
import com.cinema.model.MovieGenre; // 确保导入了你的枚举
import com.cinema.service.CinemaManager;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

// 接收前端添加电影参数的 DTO (Data Transfer Object)
class MovieRequest {
    public String title;
    public String director;
    public String actors; // 前端传逗号分隔的字符串，例如 "吴京,刘德华"
    public int duration;
    public double rating;
    public String description;
    public String genre;
    public String releaseTime; // 格式: "2023-01-01"
}

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    // 1. 获取所有电影
    @GetMapping
    public Map<String, Object> getAllMovies() {
        CinemaManager manager = CinemaManager.getInstance();
        List<Movie> movies = manager.getAllMovies();

        return buildResponse(200, "获取成功", movies);
    }

    // 2. 添加电影
    @PostMapping
    public Map<String, Object> addMovie(@RequestBody MovieRequest req) {
        try {
            CinemaManager manager = CinemaManager.getInstance();

            // 生成唯一ID
            String id = "MOV-" + System.currentTimeMillis();

            // 处理演员列表 (逗号分隔)
            List<String> actorList = Arrays.asList(req.actors.split("[,，]")); // 支持中英文逗号

            // 处理日期
            LocalDate date = LocalDate.parse(req.releaseTime, DateTimeFormatter.ISO_LOCAL_DATE);

            Movie movie = new Movie(
                    id,
                    req.title,
                    date,
                    actorList,
                    req.director,
                    req.duration,
                    req.rating,
                    req.description,
                    req.genre
            );

            manager.addMovie(movie);
            return buildResponse(200, "添加成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return buildResponse(500, "添加失败: " + e.getMessage(), null);
        }
    }

    // 3. 删除电影
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteMovie(@PathVariable String id) {
        CinemaManager manager = CinemaManager.getInstance();

        if (manager.getMovie(id) == null) {
            return buildResponse(404, "电影不存在", null);
        }

        manager.removeMovie(id);
        return buildResponse(200, "删除成功", null);
    }

    // 辅助方法：构建统一响应格式
    private Map<String, Object> buildResponse(int code, String msg, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", code == 200);
        response.put("code", code);
        response.put("message", msg);
        response.put("data", data);
        return response;
    }
}
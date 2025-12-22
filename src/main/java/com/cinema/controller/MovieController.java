package com.cinema.controller;

import com.cinema.model.Comment;
import com.cinema.model.Movie;
import com.cinema.model.MovieGenre; // 确保导入了你的枚举
import com.cinema.model.User;
import com.cinema.service.CinemaManager;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

// 🔴 新增：评论请求体 DTO
class CommentRequest {
    public String userId;
    public String content;
    public double rating;
}

// 电影DTO，避免循环引用
class MovieDTO {
    public String id;
    public String title;
    public String releaseTime;
    public List<String> actors;
    public String director;
    public int duration;
    public double rating;
    public String description;
    public String genre;
    public String trailerUrl;
    public String coverUrl;
    public List<Comment> comments;
    public String detailedInfo;

    public MovieDTO(Movie movie) {
        this.id = movie.getId();
        this.title = movie.getTitle();
        this.releaseTime = movie.getReleaseTime().toString();
        this.actors = movie.getActors();
        this.director = movie.getDirector();
        this.duration = movie.getDuration();
        this.rating = movie.getRating();
        this.description = movie.getDescription();
        this.genre = movie.getGenre().getDescription();
        this.trailerUrl = movie.getTrailerUrl();
        this.coverUrl = movie.getCoverUrl();
        this.comments = movie.getComments();
        this.detailedInfo = movie.getDetailedInfo();
    }
}

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    // 1. 获取所有电影
    @GetMapping
    public Map<String, Object> getAllMovies() {
        CinemaManager manager = CinemaManager.getInstance();
        List<Movie> movies = manager.getAllMovies();
        
        // 转换为DTO避免循环引用
        List<MovieDTO> movieDTOs = new ArrayList<>();
        for (Movie movie : movies) {
            movieDTOs.add(new MovieDTO(movie));
        }

        return buildResponse(200, "获取成功", movieDTOs);
    }

    // 🔴 新增：获取单个电影详情（包含评论）
    @GetMapping("/{id}")
    public Map<String, Object> getMovieDetail(@PathVariable String id) {
        CinemaManager manager = CinemaManager.getInstance();
        Movie movie = manager.getMovie(id);
        if (movie == null) {
            return buildResponse(404, "电影不存在", null);
        }
        return buildResponse(200, "获取成功", movie);
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

            // 注意：Movie 构造函数应支持 String genre，或者在调用前转换
            // 假设你的 Movie 类中已经有支持 String genre 的构造函数
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

    // 🔴 新增：发表评论
    @PostMapping("/{id}/comments")
    public Map<String, Object> addComment(@PathVariable String id, @RequestBody CommentRequest req) {
        CinemaManager manager = CinemaManager.getInstance();
        Movie movie = manager.getMovie(id);
        if (movie == null) return buildResponse(404, "电影不存在", null);

        // 验证用户
        User user = manager.getUser(req.userId);
        // 注意：在实际应用中，用户验证（如token）比简单查ID更安全
        if (user == null) return buildResponse(401, "用户未登录或ID无效", null);

        // 创建评论对象
        Comment comment = new Comment(
                "COM-" + System.currentTimeMillis(), // 简单的ID生成
                user.getId(),
                user.getName(),
                id,
                req.content,
                req.rating,
                LocalDateTime.now()
        );

        // 调用 Service 层方法处理评论和更新评分
        manager.addComment(id, comment);

        // 返回创建的评论对象
        return buildResponse(200, "评论成功", comment);
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
package com.cinema.model;

import java.time.LocalDateTime;

public class Comment implements java.io.Serializable {
    private String id;
    private String userId;
    private String userName; // 冗余存一下用户名，方便显示
    private String movieId;
    private String content;
    private double rating; // 用户评分 0-10
    private LocalDateTime createTime;

    public Comment(String id, String userId, String userName, String movieId, String content, double rating, LocalDateTime createTime) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.movieId = movieId;
        this.content = content;
        this.rating = rating;
        this.createTime = createTime;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getMovieId() { return movieId; }
    public String getContent() { return content; }
    public double getRating() { return rating; }
    public LocalDateTime getCreateTime() { return createTime; }
}
package com.cinema.model;

import java.time.LocalDateTime;

public class AppNotification {
    private String title;   // 标题
    private String content; // 内容
    private String type;    // 类型：通知、消息、待办
    private String datetime;// 时间
    private boolean read;   // 是否已读

    public AppNotification(String title, String content, String type) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.datetime = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        this.read = false;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public String getDatetime() { return datetime; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
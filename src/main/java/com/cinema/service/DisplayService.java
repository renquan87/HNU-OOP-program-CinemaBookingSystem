package com.cinema.service;

import com.cinema.model.Movie;
import com.cinema.model.Show;

/**
 * 显示服务
 * 负责更新前端或大屏幕的显示信息
 */
public class DisplayService {
    private static DisplayService instance;

    private DisplayService() {}

    public static synchronized DisplayService getInstance() {
        if (instance == null) {
            instance = new DisplayService();
        }
        return instance;
    }

    /**
     * 更新特定场次的座位显示
     */
    public void updateSeatDisplay(Show show) {
        // 在实际系统中，这里可能会推送到WebSocket前端
        // 这里我们模拟更新日志
        int available = show.getAvailableSeatsCount();
        int total = show.getTotalSeats();
        System.out.println("[显示服务] 场次 " + show.getId() + " 座位图已刷新。余票: " + available + "/" + total);
    }

    /**
     * 更新电影信息显示
     */
    public void updateMovieDisplay(Movie movie, String action) {
        System.out.println("[显示服务] 电影看板已更新: " + movie.getTitle() + " (" + action + ")");
    }
}
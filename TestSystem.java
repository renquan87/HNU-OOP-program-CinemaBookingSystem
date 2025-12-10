package com.cinema;

import com.cinema.service.CinemaManager;
import com.cinema.model.*;

public class TestSystem {
    public static void main(String[] args) {
        try {
            CinemaManager manager = CinemaManager.getInstance();
            
            System.out.println("=== 系统测试 ===");
            
            // 测试电影数据
            System.out.println("\n1. 测试电影数据:");
            var movies = manager.getAllMovies();
            System.out.println("电影总数: " + movies.size());
            for (Movie movie : movies) {
                System.out.println("- " + movie.getTitle() + " (ID: " + movie.getId() + ")");
            }
            
            // 测试放映厅数据
            System.out.println("\n2. 测试放映厅数据:");
            var rooms = manager.getAllScreeningRooms();
            System.out.println("放映厅总数: " + rooms.size());
            for (ScreeningRoom room : rooms) {
                System.out.println("- " + room.getName() + " (ID: " + room.getId() + 
                                 ", 座位: " + room.getRows() + "x" + room.getColumns() + ")");
            }
            
            // 测试场次数据
            System.out.println("\n3. 测试场次数据:");
            var shows = manager.getAllShows();
            System.out.println("场次总数: " + shows.size());
            for (Show show : shows) {
                System.out.println("- " + show.getMovieTitle() + " @ " + 
                                 show.getScreeningRoomName() + 
                                 " (ID: " + show.getId() + ")");
            }
            
            // 测试用户数据
            System.out.println("\n4. 测试用户数据:");
            var users = manager.getAllUsers();
            System.out.println("用户总数: " + users.size());
            for (User user : users) {
                System.out.println("- " + user.getName() + " (ID: " + user.getId() + 
                                 ", 角色: " + (user.isAdmin() ? "管理员" : "普通用户") + ")");
            }
            
            // 测试搜索功能
            System.out.println("\n5. 测试搜索功能:");
            var searchResults = manager.searchShows("阿凡达", null);
            System.out.println("搜索'阿凡达'找到 " + searchResults.size() + " 个场次");
            
            System.out.println("\n=== 测试完成 ===");
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
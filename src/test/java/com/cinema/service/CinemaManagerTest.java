package com.cinema.service;

import com.cinema.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CinemaManagerTest {
    private CinemaManager cinemaManager;

    @BeforeEach
    void setUp() {
        cinemaManager = CinemaManager.getInstance();
    }

    @Test
    void testGetInstance() {
        CinemaManager instance1 = CinemaManager.getInstance();
        CinemaManager instance2 = CinemaManager.getInstance();
        assertSame(instance1, instance2, "CinemaManager should be singleton");
    }

    @Test
    void testAddAndGetMovie() {
        Movie movie = new Movie(
            "TEST-001",
            "测试电影",
            LocalDate.of(2023, 1, 1),
            List.of("演员1", "演员2"),
            "测试导演",
            120,
            8.0,
            "测试描述",
            "测试类型"
        );

        cinemaManager.addMovie(movie);
        Movie retrieved = cinemaManager.getMovie("TEST-001");

        assertNotNull(retrieved);
        assertEquals("测试电影", retrieved.getTitle());
        assertEquals("TEST-001", retrieved.getId());
    }

    @Test
    void testAddAndGetScreeningRoom() {
        ScreeningRoom room = new ScreeningRoom("TEST-ROOM", "测试厅", 5, 8);
        cinemaManager.addScreeningRoom(room);

        ScreeningRoom retrieved = cinemaManager.getScreeningRoom("TEST-ROOM");
        assertNotNull(retrieved);
        assertEquals("测试厅", retrieved.getName());
        assertEquals(5, retrieved.getTotalRows());
        assertEquals(8, retrieved.getTotalCols());
    }

    @Test
    void testAddAndGetShow() {
        Movie movie = new Movie(
            "TEST-002",
            "测试电影2",
            LocalDate.of(2023, 1, 1),
            List.of("演员1"),
            "导演",
            100,
            7.5,
            "描述",
            "类型"
        );

        ScreeningRoom room = new ScreeningRoom("TEST-ROOM-2", "测试厅2", 3, 5);
        cinemaManager.addScreeningRoom(room);

        Show show = new Show(
            "TEST-SHOW",
            movie,
            room,
            LocalDateTime.now().plusDays(1),
            50.0
        );

        cinemaManager.addMovie(movie);
        cinemaManager.addShow(show);

        Show retrieved = cinemaManager.getShow("TEST-SHOW");
        assertNotNull(retrieved);
        assertEquals(movie, retrieved.getMovie());
        assertEquals(room, retrieved.getScreeningRoom());
    }

    @Test
    void testSearchShows() {
        Movie movie = new Movie(
            "TEST-003",
            "搜索测试电影",
            LocalDate.of(2023, 1, 1),
            List.of("演员1"),
            "导演",
            100,
            7.5,
            "描述",
            "类型"
        );

        ScreeningRoom room = new ScreeningRoom("TEST-ROOM-3", "测试厅3", 3, 5);
        cinemaManager.addScreeningRoom(room);

        Show show = new Show(
            "TEST-SHOW-SEARCH",
            movie,
            room,
            LocalDateTime.now().plusDays(1),
            50.0
        );

        cinemaManager.addMovie(movie);
        cinemaManager.addShow(show);

        List<Show> results = cinemaManager.searchShows("搜索测试", null);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(s -> s.getId().equals("TEST-SHOW-SEARCH")));
    }

    @Test
    void testRemoveMovie() {
        Movie movie = new Movie(
            "TEST-REMOVE",
            "待删除电影",
            LocalDate.of(2023, 1, 1),
            List.of("演员1"),
            "导演",
            100,
            7.5,
            "描述",
            "类型"
        );

        cinemaManager.addMovie(movie);
        assertNotNull(cinemaManager.getMovie("TEST-REMOVE"));

        cinemaManager.removeMovie("TEST-REMOVE");
        assertNull(cinemaManager.getMovie("TEST-REMOVE"));
    }

    @Test
    void testGetAllMovies() {
        List<Movie> movies = cinemaManager.getAllMovies();
        assertNotNull(movies);
        assertFalse(movies.isEmpty());
    }

    @Test
    void testGetAllScreeningRooms() {
        List<ScreeningRoom> rooms = cinemaManager.getAllScreeningRooms();
        assertNotNull(rooms);
        assertFalse(rooms.isEmpty());
    }

    @Test
    void testGetAllShows() {
        List<Show> shows = cinemaManager.getAllShows();
        assertNotNull(shows);
        assertFalse(shows.isEmpty());
    }
}
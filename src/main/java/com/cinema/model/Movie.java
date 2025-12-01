package com.cinema.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Movie implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private LocalDate releaseTime;
    private List<String> actors;
    private String director;
    private int duration; // in minutes
    private double rating;
    private String description;
    private MovieGenre genre;
    private Map<LocalDate, List<Show>> showSchedule;

    public Movie(String id, String title, LocalDate releaseTime, List<String> actors, 
                 String director, int duration, double rating, String description, MovieGenre genre) {
        this.id = id;
        this.title = title;
        this.releaseTime = releaseTime;
        this.actors = new ArrayList<>(actors);
        this.director = director;
        this.duration = duration;
        this.rating = rating;
        this.description = description;
        this.genre = genre;
        this.showSchedule = new ConcurrentHashMap<>();
    }

    /**
     * 兼容性构造函数，支持String类型的genre参数
     */
    public Movie(String id, String title, LocalDate releaseTime, List<String> actors, 
                 String director, int duration, double rating, String description, String genre) {
        this(id, title, releaseTime, actors, director, duration, rating, description, 
             MovieGenre.fromDescription(genre));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(LocalDate releaseTime) {
        this.releaseTime = releaseTime;
    }

    public List<String> getActors() {
        return new ArrayList<>(actors);
    }

    public void setActors(List<String> actors) {
        this.actors = new ArrayList<>(actors);
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        if (rating < 0 || rating > 10) {
            throw new IllegalArgumentException("评分必须在0-10之间");
        }
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }

    /**
     * 兼容性方法，支持String类型的genre参数
     */
    public void setGenre(String genre) {
        this.genre = MovieGenre.fromDescription(genre);
    }

    /**
     * 演员管理方法
     */
    public void addActor(String actor) {
        if (actor != null && !actor.trim().isEmpty() && !actors.contains(actor)) {
            actors.add(actor);
        }
    }

    public void removeActor(String actor) {
        actors.remove(actor);
    }

    

    public List<Show> getShowsByDate(LocalDate date) {
        return showSchedule.getOrDefault(date, new ArrayList<>());
    }

    public void addShow(LocalDate date, Show show) {
        showSchedule.computeIfAbsent(date, k -> new ArrayList<>()).add(show);
    }

    public void removeShow(LocalDate date, Show show) {
        List<Show> shows = showSchedule.get(date);
        if (shows != null) {
            shows.remove(show);
            if (shows.isEmpty()) {
                showSchedule.remove(date);
            }
        }
    }

    public Map<LocalDate, List<Show>> getAllShows() {
        return new ConcurrentHashMap<>(showSchedule);
    }

    @Override
    public String toString() {
        return String.format("Movie{id='%s', title='%s', director='%s', duration=%d分钟, rating=%.1f, genre=%s}",
                id, title, director, duration, rating, genre.getDescription());
    }

    /**
     * 获取电影的详细信息字符串
     */
    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append("电影信息:\n");
        info.append(String.format("  片名: %s\n", title));
        info.append(String.format("  导演: %s\n", director));
        info.append(String.format("  类型: %s\n", genre.getDescription()));
        info.append(String.format("  时长: %d分钟\n", duration));
        info.append(String.format("  评分: %.1f\n", rating));
        info.append(String.format("  上映日期: %s\n", releaseTime));
        info.append(String.format("  演员: %s\n", String.join(", ", actors)));
        info.append(String.format("  简介: %s\n", description));
        return info.toString();
    }
}
package com.cinema.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Movie implements java.io.Serializable {
    private String id;
    private String title;
    private LocalDate releaseTime;
    private List<String> actors;
    private String director;
    private int duration; // in minutes
    private double rating;
    private String description;
    private String genre;
    private Map<LocalDate, List<Show>> showSchedule;

    public Movie(String id, String title, LocalDate releaseTime, List<String> actors, 
                 String director, int duration, double rating, String description, String genre) {
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
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
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
        return "Movie{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", director='" + director + '\'' +
                ", duration=" + duration +
                ", rating=" + rating +
                ", genre='" + genre + '\'' +
                '}';
    }
}
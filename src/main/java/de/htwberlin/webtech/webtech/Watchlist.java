package de.htwberlin.webtech.webtech;

import jakarta.persistence.*;

@Entity
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String type;
    private String genre;
    private boolean watched;
    private int rating;

    // Leerer Konstruktor für Hibernate
    public Watchlist() {}

    public Watchlist(String title, String type, String genre, boolean watched, int rating) {
        this.title = title;
        this.type = type;
        this.genre = genre;
        this.watched = watched;
        this.rating = rating;
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
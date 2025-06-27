package de.htwberlin.webtech.webtech;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

    // Neu: Poster URL für Cover-Bilder
    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    // Many-to-One Beziehung zu User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Leerer Konstruktor für Hibernate
    public Watchlist() {}

    public Watchlist(String title, String type, String genre, boolean watched, int rating, User user) {
        this.title = title;
        this.type = type;
        this.genre = genre;
        this.watched = watched;
        this.rating = rating;
        this.user = user;
    }

    public Watchlist(String title, String type, String genre, boolean watched, int rating, String posterUrl, User user) {
        this.title = title;
        this.type = type;
        this.genre = genre;
        this.watched = watched;
        this.rating = rating;
        this.posterUrl = posterUrl;
        this.user = user;
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

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
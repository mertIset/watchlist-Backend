package de.htwberlin.webtech.webtech;

import de.htwberlin.webtech.webtech.Watchlist;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://watchlist-frontend-bzxi.onrender.com"})
public class controllerWatchlist {

    // Temporärer In-Memory-Speicher (sollte später durch eine Datenbank ersetzt werden)
    private final List<de.htwberlin.webtech.webtech.Watchlist> watchlistItems = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    // Initialisiere mit Test-Daten
    public controllerWatchlist() {
        watchlistItems.add(new de.htwberlin.webtech.webtech.Watchlist(idCounter.getAndIncrement(), "This is a test Watchlist item 1.", "Movie", "test1", false, 3));
        watchlistItems.add(new de.htwberlin.webtech.webtech.Watchlist(idCounter.getAndIncrement(), "This is a test Watchlist item 2.", "Show", "test2", true, 4));
        watchlistItems.add(new de.htwberlin.webtech.webtech.Watchlist(idCounter.getAndIncrement(), "This is a test Watchlist item 3.", "Movie", "test3", false, 2));
    }

    @GetMapping("/Watchlist")
    public List<de.htwberlin.webtech.webtech.Watchlist> getAllWatchlistItems() {
        return new ArrayList<>(watchlistItems);
    }

    @PostMapping("/Watchlist")
    public de.htwberlin.webtech.webtech.Watchlist addWatchlistItem(@RequestBody WatchlistRequest request) {
        de.htwberlin.webtech.webtech.Watchlist newItem = new de.htwberlin.webtech.webtech.Watchlist(
                idCounter.getAndIncrement(),
                request.getTitle(),
                request.getType(),
                request.getGenre(),
                request.isWatched(),
                request.getRating()
        );
        watchlistItems.add(newItem);
        return newItem;
    }

    @DeleteMapping("/Watchlist/{id}")
    public boolean deleteWatchlistItem(@PathVariable long id) {
        return watchlistItems.removeIf(item -> item.getId() == id);
    }

    @PutMapping("/Watchlist/{id}")
    public de.htwberlin.webtech.webtech.Watchlist updateWatchlistItem(@PathVariable long id, @RequestBody WatchlistRequest request) {
        for (int i = 0; i < watchlistItems.size(); i++) {
            de.htwberlin.webtech.webtech.Watchlist item = watchlistItems.get(i);
            if (item.getId() == id) {
                de.htwberlin.webtech.webtech.Watchlist updatedItem = new Watchlist(
                        id,
                        request.getTitle(),
                        request.getType(),
                        request.getGenre(),
                        request.isWatched(),
                        request.getRating()
                );
                watchlistItems.set(i, updatedItem);
                return updatedItem;
            }
        }
        throw new RuntimeException("Watchlist item with id " + id + " not found");
    }

    // Request DTO für POST/PUT Requests
    public static class WatchlistRequest {
        private String title;
        private String type;
        private String genre;
        private boolean watched;
        private int rating;

        // Getters und Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }

        public boolean isWatched() { return watched; }
        public void setWatched(boolean watched) { this.watched = watched; }

        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }
    }
}
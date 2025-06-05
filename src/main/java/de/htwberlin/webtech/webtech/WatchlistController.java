package de.htwberlin.webtech.webtech;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://watchlist-frontend-bzxi.onrender.com"})
public class WatchlistController {

    @Autowired
    private WatchlistService watchlistService;

    @Autowired
    private UserService userService;

    @GetMapping("/Watchlist")
    public List<Watchlist> getAllWatchlistItems(@RequestParam Long userId) {
        return watchlistService.getAllWatchlistItemsByUser(userId);
    }

    @PostMapping("/Watchlist")
    public Watchlist addWatchlistItem(@RequestBody WatchlistRequest request) {
        User user = userService.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

        Watchlist newItem = new Watchlist(
                request.getTitle(),
                request.getType(),
                request.getGenre(),
                request.isWatched(),
                request.getRating(),
                user
        );
        return watchlistService.saveWatchlistItem(newItem);
    }

    @DeleteMapping("/Watchlist/{id}")
    public boolean deleteWatchlistItem(@PathVariable Long id, @RequestParam Long userId) {
        return watchlistService.deleteWatchlistItem(id, userId);
    }

    @PutMapping("/Watchlist/{id}")
    public Watchlist updateWatchlistItem(@PathVariable Long id, @RequestBody WatchlistRequest request) {
        User user = userService.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

        Watchlist updatedItem = new Watchlist(
                request.getTitle(),
                request.getType(),
                request.getGenre(),
                request.isWatched(),
                request.getRating(),
                user
        );
        return watchlistService.updateWatchlistItem(id, updatedItem, request.getUserId());
    }

    @GetMapping("/Watchlist/{id}")
    public Watchlist getWatchlistItem(@PathVariable Long id, @RequestParam Long userId) {
        return watchlistService.getWatchlistItem(id, userId)
                .orElseThrow(() -> new RuntimeException("Watchlist item with id " + id + " not found"));
    }

    // Request DTO für POST/PUT Requests
    public static class WatchlistRequest {
        private String title;
        private String type;
        private String genre;
        private boolean watched;
        private int rating;
        private Long userId;

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

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}
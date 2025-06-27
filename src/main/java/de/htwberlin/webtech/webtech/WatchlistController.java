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
    public List<Watchlist> getAllWatchlistItems(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return watchlistService.getAllWatchlistItemsByUser(userId);
        } else {
            return watchlistService.getAllWatchlistItems();
        }
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
                request.getPosterUrl(), // Kann null sein - wird automatisch gesucht
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
                request.getPosterUrl(),
                user
        );
        return watchlistService.updateWatchlistItem(id, updatedItem, request.getUserId());
    }

    @GetMapping("/Watchlist/{id}")
    public Watchlist getWatchlistItem(@PathVariable Long id, @RequestParam Long userId) {
        return watchlistService.getWatchlistItem(id, userId)
                .orElseThrow(() -> new RuntimeException("Watchlist item with id " + id + " not found"));
    }

    /**
     * Endpunkt zum manuellen Aktualisieren des Covers eines Eintrags
     */
    @PostMapping("/Watchlist/{id}/refresh-poster")
    public Watchlist refreshPoster(@PathVariable Long id, @RequestParam Long userId) {
        return watchlistService.refreshPoster(id, userId);
    }

    /**
     * Endpunkt zum Batch-Update aller fehlenden Cover für einen User
     */
    @PostMapping("/Watchlist/refresh-all-posters")
    public String refreshAllPosters(@RequestParam Long userId) {
        try {
            watchlistService.refreshAllMissingPosters(userId);
            return "Cover-Update für alle Einträge gestartet!";
        } catch (Exception e) {
            return "Fehler beim Aktualisieren der Cover: " + e.getMessage();
        }
    }

    // Erweiterte Request DTO für POST/PUT Requests
    public static class WatchlistRequest {
        private String title;
        private String type;
        private String genre;
        private boolean watched;
        private int rating;
        private String posterUrl; // Neu: Optional für manuell gesetzte Cover
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

        public String getPosterUrl() { return posterUrl; }
        public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}
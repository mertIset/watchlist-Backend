package de.htwberlin.webtech.webtech;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class WatchlistService {

    @Autowired
    private WatchlistRepository repository;

    @Autowired
    private OMDbService omdbService;

    public List<Watchlist> getAllWatchlistItemsByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    public List<Watchlist> getAllWatchlistItems() {
        return (List<Watchlist>) repository.findAll();
    }

    public Watchlist saveWatchlistItem(Watchlist watchlist) {
        // Automatisch Cover suchen, falls noch keines vorhanden
        if (watchlist.getPosterUrl() == null || watchlist.getPosterUrl().isEmpty()) {
            String posterUrl = omdbService.fetchPosterUrl(watchlist.getTitle(), watchlist.getType());
            watchlist.setPosterUrl(posterUrl);

            System.out.println("🎬 Watchlist Item erstellt: " + watchlist.getTitle() +
                    " | Cover: " + (posterUrl != null ? "✅ Gefunden" : "❌ Nicht gefunden"));
        }

        return repository.save(watchlist);
    }

    public Optional<Watchlist> getWatchlistItem(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId);
    }

    public boolean deleteWatchlistItem(Long id, Long userId) {
        Optional<Watchlist> item = repository.findByIdAndUserId(id, userId);
        if (item.isPresent()) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public Watchlist updateWatchlistItem(Long id, Watchlist updatedWatchlist, Long userId) {
        return repository.findByIdAndUserId(id, userId)
                .map(existingItem -> {
                    String oldTitle = existingItem.getTitle();
                    String oldType = existingItem.getType();

                    existingItem.setTitle(updatedWatchlist.getTitle());
                    existingItem.setType(updatedWatchlist.getType());
                    existingItem.setGenre(updatedWatchlist.getGenre());
                    existingItem.setWatched(updatedWatchlist.isWatched());
                    existingItem.setRating(updatedWatchlist.getRating());

                    // Cover neu suchen, falls Titel oder Type geändert wurde
                    boolean titleChanged = !oldTitle.equals(updatedWatchlist.getTitle());
                    boolean typeChanged = !oldType.equals(updatedWatchlist.getType());

                    if (titleChanged || typeChanged) {
                        String newPosterUrl = omdbService.fetchPosterUrl(
                                updatedWatchlist.getTitle(),
                                updatedWatchlist.getType()
                        );
                        existingItem.setPosterUrl(newPosterUrl);

                        System.out.println("🔄 Watchlist Item aktualisiert: " + updatedWatchlist.getTitle() +
                                " | Cover neu gesucht: " + (newPosterUrl != null ? "✅ Gefunden" : "❌ Nicht gefunden"));
                    } else if (updatedWatchlist.getPosterUrl() != null) {
                        // Behalte vorhandene Poster URL, falls keine Änderung
                        existingItem.setPosterUrl(updatedWatchlist.getPosterUrl());
                    }

                    return repository.save(existingItem);
                })
                .orElseThrow(() -> new RuntimeException("Watchlist item with id " + id + " not found or access denied"));
    }

    /**
     * Manuelles Neu-Laden von Covern für bestehende Einträge
     */
    public Watchlist refreshPoster(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId)
                .map(item -> {
                    String newPosterUrl = omdbService.fetchPosterUrl(item.getTitle(), item.getType());
                    item.setPosterUrl(newPosterUrl);

                    System.out.println("🔄 Cover manuell aktualisiert für: " + item.getTitle() +
                            " | Neues Cover: " + (newPosterUrl != null ? "✅ Gefunden" : "❌ Nicht gefunden"));

                    return repository.save(item);
                })
                .orElseThrow(() -> new RuntimeException("Watchlist item with id " + id + " not found"));
    }

    /**
     * Batch-Update für alle Einträge eines Users ohne Cover
     */
    public void refreshAllMissingPosters(Long userId) {
        List<Watchlist> userItems = repository.findByUserId(userId);

        for (Watchlist item : userItems) {
            if (item.getPosterUrl() == null || item.getPosterUrl().isEmpty()) {
                String posterUrl = omdbService.fetchPosterUrl(item.getTitle(), item.getType());
                if (posterUrl != null) {
                    item.setPosterUrl(posterUrl);
                    repository.save(item);

                    System.out.println("📦 Batch-Update - Cover hinzugefügt für: " + item.getTitle());

                    // Kleine Verzögerung um API-Limits zu respektieren
                    try {
                        Thread.sleep(200); // 200ms Pause zwischen Requests
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
}
package de.htwberlin.webtech.webtech;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WatchlistService {

    @Autowired
    private WatchlistRepository repository;

    public List<Watchlist> getAllWatchlistItems() {
        return (List<Watchlist>) repository.findAll();
    }

    public Watchlist saveWatchlistItem(Watchlist watchlist) {
        return repository.save(watchlist);
    }

    public Optional<Watchlist> getWatchlistItem(Long id) {
        return repository.findById(id);
    }

    public boolean deleteWatchlistItem(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public Watchlist updateWatchlistItem(Long id, Watchlist updatedWatchlist) {
        return repository.findById(id)
                .map(existingItem -> {
                    existingItem.setTitle(updatedWatchlist.getTitle());
                    existingItem.setType(updatedWatchlist.getType());
                    existingItem.setGenre(updatedWatchlist.getGenre());
                    existingItem.setWatched(updatedWatchlist.isWatched());
                    existingItem.setRating(updatedWatchlist.getRating());
                    return repository.save(existingItem);
                })
                .orElseThrow(() -> new RuntimeException("Watchlist item with id " + id + " not found"));
    }
}
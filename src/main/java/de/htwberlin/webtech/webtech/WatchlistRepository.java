package de.htwberlin.webtech.webtech;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchlistRepository extends CrudRepository<Watchlist, Long> {
    // CrudRepository stellt bereits Standard-Methoden bereit:
    // save(), findById(), findAll(), deleteById(), etc.
}
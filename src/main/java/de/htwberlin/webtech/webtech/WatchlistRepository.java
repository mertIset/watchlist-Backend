package de.htwberlin.webtech.webtech;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends CrudRepository<Watchlist, Long> {

    List<Watchlist> findByUserId(Long userId);

    Optional<Watchlist> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
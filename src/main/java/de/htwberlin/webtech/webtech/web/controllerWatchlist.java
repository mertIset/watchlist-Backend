package de.htwberlin.webtech.webtech.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class controllerWatchlist {

    @GetMapping("/")
    public List<Watchlist> testRoute() {
        return List.of(
                new Watchlist(001, "This is a test Watchlist item 1.", "Movie", "test1", false, 3),
                new Watchlist(002, "This is a test Watchlist item 2.", "Show", "test2", true, 4),
                new Watchlist(003, "This is a test Watchlist item 3.", "Movie", "test3", false, 2)
        );
    }
}


package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WatchlistEntityTest {

    private Watchlist watchlist;
    private User testUser;

    @BeforeEach
    void setUp() {
        watchlist = new Watchlist();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testDefaultConstructor() {
        // When
        Watchlist newWatchlist = new Watchlist();

        // Then
        assertNotNull(newWatchlist);
        assertNull(newWatchlist.getId());
        assertNull(newWatchlist.getTitle());
        assertNull(newWatchlist.getType());
        assertNull(newWatchlist.getGenre());
        assertFalse(newWatchlist.isWatched()); // boolean default is false
        assertEquals(0, newWatchlist.getRating()); // int default is 0
        assertNull(newWatchlist.getPosterUrl());
        assertNull(newWatchlist.getUser());
    }

    @Test
    void testParameterizedConstructorWithoutPoster() {
        // When
        Watchlist newWatchlist = new Watchlist("Test Movie", "Film", "Action",
                true, 8, testUser);

        // Then
        assertEquals("Test Movie", newWatchlist.getTitle());
        assertEquals("Film", newWatchlist.getType());
        assertEquals("Action", newWatchlist.getGenre());
        assertTrue(newWatchlist.isWatched());
        assertEquals(8, newWatchlist.getRating());
        assertEquals(testUser, newWatchlist.getUser());
        assertNull(newWatchlist.getPosterUrl());
    }

    @Test
    void testParameterizedConstructorWithPoster() {
        // When
        Watchlist newWatchlist = new Watchlist("Test Movie", "Film", "Action",
                true, 8, "https://example.com/poster.jpg", testUser);

        // Then
        assertEquals("Test Movie", newWatchlist.getTitle());
        assertEquals("Film", newWatchlist.getType());
        assertEquals("Action", newWatchlist.getGenre());
        assertTrue(newWatchlist.isWatched());
        assertEquals(8, newWatchlist.getRating());
        assertEquals("https://example.com/poster.jpg", newWatchlist.getPosterUrl());
        assertEquals(testUser, newWatchlist.getUser());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        Long id = 1L;
        String title = "Test Movie";
        String type = "Film";
        String genre = "Action";
        boolean watched = true;
        int rating = 9;
        String posterUrl = "https://example.com/poster.jpg";

        // When
        watchlist.setId(id);
        watchlist.setTitle(title);
        watchlist.setType(type);
        watchlist.setGenre(genre);
        watchlist.setWatched(watched);
        watchlist.setRating(rating);
        watchlist.setPosterUrl(posterUrl);
        watchlist.setUser(testUser);

        // Then
        assertEquals(id, watchlist.getId());
        assertEquals(title, watchlist.getTitle());
        assertEquals(type, watchlist.getType());
        assertEquals(genre, watchlist.getGenre());
        assertEquals(watched, watchlist.isWatched());
        assertEquals(rating, watchlist.getRating());
        assertEquals(posterUrl, watchlist.getPosterUrl());
        assertEquals(testUser, watchlist.getUser());
    }

    @Test
    void testWatchedProperty() {
        // When setting watched to true
        watchlist.setWatched(true);

        // Then
        assertTrue(watchlist.isWatched());

        // When setting watched to false
        watchlist.setWatched(false);

        // Then
        assertFalse(watchlist.isWatched());
    }

    @Test
    void testRatingBoundaries() {
        // Test minimum rating
        watchlist.setRating(0);
        assertEquals(0, watchlist.getRating());

        // Test maximum rating (assuming 1-10 scale)
        watchlist.setRating(10);
        assertEquals(10, watchlist.getRating());

        // Test negative rating (should be allowed by entity, validation elsewhere)
        watchlist.setRating(-1);
        assertEquals(-1, watchlist.getRating());

        // Test rating above maximum (should be allowed by entity, validation elsewhere)
        watchlist.setRating(15);
        assertEquals(15, watchlist.getRating());
    }

    @Test
    void testNullValues() {
        // When setting null values
        watchlist.setId(null);
        watchlist.setTitle(null);
        watchlist.setType(null);
        watchlist.setGenre(null);
        watchlist.setPosterUrl(null);
        watchlist.setUser(null);

        // Then
        assertNull(watchlist.getId());
        assertNull(watchlist.getTitle());
        assertNull(watchlist.getType());
        assertNull(watchlist.getGenre());
        assertNull(watchlist.getPosterUrl());
        assertNull(watchlist.getUser());
        // Primitive types can't be null
        assertFalse(watchlist.isWatched()); // default boolean value
        assertEquals(0, watchlist.getRating()); // default int value
    }

    @Test
    void testEmptyStringValues() {
        // When setting empty strings
        watchlist.setTitle("");
        watchlist.setType("");
        watchlist.setGenre("");
        watchlist.setPosterUrl("");

        // Then
        assertEquals("", watchlist.getTitle());
        assertEquals("", watchlist.getType());
        assertEquals("", watchlist.getGenre());
        assertEquals("", watchlist.getPosterUrl());
    }

    @Test
    void testUserRelationship() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("anotheruser");

        // When setting user
        watchlist.setUser(testUser);
        assertEquals(testUser, watchlist.getUser());

        // When changing user
        watchlist.setUser(anotherUser);
        assertEquals(anotherUser, watchlist.getUser());
        assertNotEquals(testUser, watchlist.getUser());
    }

    @Test
    void testMovieTypes() {
        // Test different movie types
        String[] movieTypes = {"Film", "Serie", "Dokumentation", "Anime"};

        for (String type : movieTypes) {
            watchlist.setType(type);
            assertEquals(type, watchlist.getType());
        }
    }

    @Test
    void testGenres() {
        // Test different genres
        String[] genres = {"Action", "Drama", "Comedy", "Horror", "Sci-Fi", "Romance"};

        for (String genre : genres) {
            watchlist.setGenre(genre);
            assertEquals(genre, watchlist.getGenre());
        }
    }

    @Test
    void testLongTitles() {
        // Test very long title
        String longTitle = "A".repeat(500);
        watchlist.setTitle(longTitle);
        assertEquals(longTitle, watchlist.getTitle());
    }

    @Test
    void testSpecialCharactersInFields() {
        // Given
        String titleWithSpecialChars = "Movie: The Return (2024) - Director's Cut";
        String typeWithSpecialChars = "TV-Serie";
        String genreWithSpecialChars = "Action/Adventure";

        // When
        watchlist.setTitle(titleWithSpecialChars);
        watchlist.setType(typeWithSpecialChars);
        watchlist.setGenre(genreWithSpecialChars);

        // Then
        assertEquals(titleWithSpecialChars, watchlist.getTitle());
        assertEquals(typeWithSpecialChars, watchlist.getType());
        assertEquals(genreWithSpecialChars, watchlist.getGenre());
    }

    @Test
    void testUnicodeCharacters() {
        // Test with Unicode characters (German, Japanese, etc.)
        watchlist.setTitle("Der Herr der Ringe: Die Gefährten");
        watchlist.setGenre("ファンタジー"); // Fantasy in Japanese

        assertEquals("Der Herr der Ringe: Die Gefährten", watchlist.getTitle());
        assertEquals("ファンタジー", watchlist.getGenre());
    }

    @Test
    void testPosterUrlValidation() {
        // Test various poster URL formats
        String[] validUrls = {
                "https://example.com/poster.jpg",
                "http://movie-db.com/images/poster123.png",
                "https://cdn.movies.com/covers/movie.jpeg",
                "https://images.amazon.com/images/I/81234567890._SL500_.jpg"
        };

        for (String url : validUrls) {
            watchlist.setPosterUrl(url);
            assertEquals(url, watchlist.getPosterUrl());
        }
    }

    @Test
    void testEqualsAndHashCode() {
        // Note: Die Watchlist-Entity hat möglicherweise keine equals/hashCode Implementation
        // Dieser Test prüft das Standard-Verhalten

        // Given
        Watchlist watchlist1 = new Watchlist("Test Movie", "Film", "Action",
                true, 8, testUser);
        Watchlist watchlist2 = new Watchlist("Test Movie", "Film", "Action",
                true, 8, testUser);

        // When & Then
        // Ohne equals-Implementation sollten sie unterschiedlich sein
        assertNotEquals(watchlist1, watchlist2);
        assertNotEquals(watchlist1.hashCode(), watchlist2.hashCode());

        // Aber mit sich selbst sollte es gleich sein
        assertEquals(watchlist1, watchlist1);
        assertEquals(watchlist1.hashCode(), watchlist1.hashCode());
    }

    @Test
    void testToString() {
        // Given
        watchlist.setId(1L);
        watchlist.setTitle("Test Movie");
        watchlist.setType("Film");

        // When
        String toString = watchlist.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("Watchlist")); // Should contain class name
        // Note: Ohne custom toString-Implementation ist der Inhalt nicht vorhersagbar
    }

    @Test
    void testWatchedAndRatingConsistency() {
        // Test that watched and rating can be independently set

        // Case 1: Watched but no rating
        watchlist.setWatched(true);
        watchlist.setRating(0);
        assertTrue(watchlist.isWatched());
        assertEquals(0, watchlist.getRating());

        // Case 2: Not watched but has rating (edge case)
        watchlist.setWatched(false);
        watchlist.setRating(8);
        assertFalse(watchlist.isWatched());
        assertEquals(8, watchlist.getRating());

        // Case 3: Watched with rating
        watchlist.setWatched(true);
        watchlist.setRating(9);
        assertTrue(watchlist.isWatched());
        assertEquals(9, watchlist.getRating());
    }

    @Test
    void testBidirectionalUserRelationship() {
        // Given
        testUser.setWatchlistItems(new java.util.ArrayList<>());

        // When
        watchlist.setUser(testUser);
        testUser.getWatchlistItems().add(watchlist);

        // Then
        assertEquals(testUser, watchlist.getUser());
        assertTrue(testUser.getWatchlistItems().contains(watchlist));
        assertEquals(1, testUser.getWatchlistItems().size());
    }

    @Test
    void testNoPosterScenario() {
        // Test the "N/A" scenario commonly returned by APIs
        watchlist.setPosterUrl("N/A");
        assertEquals("N/A", watchlist.getPosterUrl());

        // Test null poster
        watchlist.setPosterUrl(null);
        assertNull(watchlist.getPosterUrl());

        // Test empty poster
        watchlist.setPosterUrl("");
        assertEquals("", watchlist.getPosterUrl());
    }
}
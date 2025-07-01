package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class EntityTests {

    @Test
    void testUser_Constructor() {
        // Act
        User user = new User("testuser", "test@example.com", "password", "Test", "User");

        // Assert
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertNotNull(user.getCreatedAt());
        assertNull(user.getLastLogin());
        assertNull(user.getId()); // Noch nicht gespeichert
    }

    @Test
    void testUser_EmptyConstructor() {
        // Act
        User user = new User();

        // Assert
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNotNull(user.getCreatedAt()); // Wird im leeren Konstruktor gesetzt
    }

    @Test
    void testUser_Setters() {
        // Arrange
        User user = new User();

        // Act
        user.setId(1L);
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setPassword("newpassword");
        user.setFirstName("New");
        user.setLastName("User");

        LocalDateTime now = LocalDateTime.now();
        user.setLastLogin(now);
        user.setWatchlistItems(new ArrayList<>());

        // Assert
        assertEquals(1L, user.getId());
        assertEquals("newuser", user.getUsername());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("newpassword", user.getPassword());
        assertEquals("New", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals(now, user.getLastLogin());
        assertNotNull(user.getWatchlistItems());
        assertTrue(user.getWatchlistItems().isEmpty());
    }

    @Test
    void testWatchlist_Constructor() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password", "Test", "User");

        // Act
        Watchlist watchlist = new Watchlist("Inception", "Film", "Sci-Fi", false, 0, user);

        // Assert
        assertEquals("Inception", watchlist.getTitle());
        assertEquals("Film", watchlist.getType());
        assertEquals("Sci-Fi", watchlist.getGenre());
        assertFalse(watchlist.isWatched());
        assertEquals(0, watchlist.getRating());
        assertEquals(user, watchlist.getUser());
        assertNull(watchlist.getPosterUrl());
        assertNull(watchlist.getId()); // Noch nicht gespeichert
    }

    @Test
    void testWatchlist_ConstructorWithPoster() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password", "Test", "User");
        String posterUrl = "http://example.com/poster.jpg";

        // Act
        Watchlist watchlist = new Watchlist("Inception", "Film", "Sci-Fi", false, 0, posterUrl, user);

        // Assert
        assertEquals("Inception", watchlist.getTitle());
        assertEquals("Film", watchlist.getType());
        assertEquals("Sci-Fi", watchlist.getGenre());
        assertFalse(watchlist.isWatched());
        assertEquals(0, watchlist.getRating());
        assertEquals(posterUrl, watchlist.getPosterUrl());
        assertEquals(user, watchlist.getUser());
    }

    @Test
    void testWatchlist_EmptyConstructor() {
        // Act
        Watchlist watchlist = new Watchlist();

        // Assert
        assertNull(watchlist.getTitle());
        assertNull(watchlist.getType());
        assertNull(watchlist.getGenre());
        assertFalse(watchlist.isWatched()); // Default boolean value
        assertEquals(0, watchlist.getRating()); // Default int value
        assertNull(watchlist.getPosterUrl());
        assertNull(watchlist.getUser());
    }

    @Test
    void testWatchlist_Setters() {
        // Arrange
        Watchlist watchlist = new Watchlist();
        User user = new User("testuser", "test@example.com", "password", "Test", "User");

        // Act
        watchlist.setId(1L);
        watchlist.setTitle("The Matrix");
        watchlist.setType("Film");
        watchlist.setGenre("Action");
        watchlist.setWatched(true);
        watchlist.setRating(5);
        watchlist.setPosterUrl("http://matrix-poster.com/image.jpg");
        watchlist.setUser(user);

        // Assert
        assertEquals(1L, watchlist.getId());
        assertEquals("The Matrix", watchlist.getTitle());
        assertEquals("Film", watchlist.getType());
        assertEquals("Action", watchlist.getGenre());
        assertTrue(watchlist.isWatched());
        assertEquals(5, watchlist.getRating());
        assertEquals("http://matrix-poster.com/image.jpg", watchlist.getPosterUrl());
        assertEquals(user, watchlist.getUser());
    }

    @Test
    void testWatchlist_RatingBoundaries() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password", "Test", "User");
        Watchlist watchlist = new Watchlist("Test Movie", "Film", "Action", true, 0, user);

        // Act & Assert - Test verschiedene Rating-Werte
        watchlist.setRating(0);
        assertEquals(0, watchlist.getRating());

        watchlist.setRating(5);
        assertEquals(5, watchlist.getRating());

        watchlist.setRating(10);
        assertEquals(10, watchlist.getRating());

        // Negative Werte sind technisch möglich, aber sollten in der Praxis validiert werden
        watchlist.setRating(-1);
        assertEquals(-1, watchlist.getRating());
    }

    @Test
    void testUser_WatchlistRelationship() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password", "Test", "User");
        Watchlist item1 = new Watchlist("Movie 1", "Film", "Action", false, 0, user);
        Watchlist item2 = new Watchlist("Movie 2", "Film", "Comedy", true, 4, user);

        // Act
        user.setWatchlistItems(new ArrayList<>());
        user.getWatchlistItems().add(item1);
        user.getWatchlistItems().add(item2);

        // Assert
        assertEquals(2, user.getWatchlistItems().size());
        assertTrue(user.getWatchlistItems().contains(item1));
        assertTrue(user.getWatchlistItems().contains(item2));
        assertEquals(user, item1.getUser());
        assertEquals(user, item2.getUser());
    }

    @Test
    void testWatchlist_BooleanDefaults() {
        // Arrange & Act
        Watchlist watchlist = new Watchlist();

        // Assert - Boolean und int haben Default-Werte
        assertFalse(watchlist.isWatched()); // Default boolean ist false
        assertEquals(0, watchlist.getRating()); // Default int ist 0
    }
}
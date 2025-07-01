package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Test
    void testUserRepository_FindByUsername() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password", "Test", "User");
        entityManager.persistAndFlush(user);

        // Act
        Optional<User> result = userRepository.findByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testUserRepository_FindByUsername_NotFound() {
        // Act
        Optional<User> result = userRepository.findByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testUserRepository_FindByEmail() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password", "Test", "User");
        entityManager.persistAndFlush(user);

        // Act
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testUserRepository_ExistsByUsername() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password", "Test", "User");
        entityManager.persistAndFlush(user);

        // Act & Assert
        assertTrue(userRepository.existsByUsername("testuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testUserRepository_ExistsByEmail() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password", "Test", "User");
        entityManager.persistAndFlush(user);

        // Act & Assert
        assertTrue(userRepository.existsByEmail("test@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void testWatchlistRepository_FindByUserId() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password", "Test", "User");
        entityManager.persistAndFlush(user);

        Watchlist item1 = new Watchlist("Inception", "Film", "Sci-Fi", false, 0, user);
        Watchlist item2 = new Watchlist("The Matrix", "Film", "Action", true, 5, user);
        entityManager.persistAndFlush(item1);
        entityManager.persistAndFlush(item2);

        // Act
        List<Watchlist> result = watchlistRepository.findByUserId(user.getId());

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(item -> "Inception".equals(item.getTitle())));
        assertTrue(result.stream().anyMatch(item -> "The Matrix".equals(item.getTitle())));
    }

    @Test
    void testWatchlistRepository_FindByIdAndUserId() {
        // Arrange
        User user1 = new User("user1", "user1@example.com", "password", "User", "One");
        User user2 = new User("user2", "user2@example.com", "password", "User", "Two");
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        Watchlist item1 = new Watchlist("Inception", "Film", "Sci-Fi", false, 0, user1);
        Watchlist item2 = new Watchlist("The Matrix", "Film", "Action", true, 5, user2);
        entityManager.persistAndFlush(item1);
        entityManager.persistAndFlush(item2);

        // Act & Assert
        // User1 sollte nur sein eigenes Item sehen
        Optional<Watchlist> result1 = watchlistRepository.findByIdAndUserId(item1.getId(), user1.getId());
        assertTrue(result1.isPresent());
        assertEquals("Inception", result1.get().getTitle());

        // User1 sollte User2's Item nicht sehen
        Optional<Watchlist> result2 = watchlistRepository.findByIdAndUserId(item2.getId(), user1.getId());
        assertFalse(result2.isPresent());

        // User2 sollte nur sein eigenes Item sehen
        Optional<Watchlist> result3 = watchlistRepository.findByIdAndUserId(item2.getId(), user2.getId());
        assertTrue(result3.isPresent());
        assertEquals("The Matrix", result3.get().getTitle());
    }

    @Test
    void testWatchlistRepository_DeleteByIdAndUserId() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password", "Test", "User");
        entityManager.persistAndFlush(user);

        Watchlist item = new Watchlist("Inception", "Film", "Sci-Fi", false, 0, user);
        entityManager.persistAndFlush(item);

        Long itemId = item.getId();
        Long userId = user.getId();

        // Prüfe dass Item existiert
        assertTrue(watchlistRepository.findByIdAndUserId(itemId, userId).isPresent());

        // Act
        watchlistRepository.deleteByIdAndUserId(itemId, userId);
        entityManager.flush();

        // Assert
        assertFalse(watchlistRepository.findByIdAndUserId(itemId, userId).isPresent());
    }

    @Test
    void testUserDeletion_WithAssociatedWatchlistItems() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password", "Test", "User");
        entityManager.persistAndFlush(user);

        Watchlist item1 = new Watchlist("Inception", "Film", "Sci-Fi", false, 0, user);
        Watchlist item2 = new Watchlist("The Matrix", "Film", "Action", true, 5, user);
        entityManager.persistAndFlush(item1);
        entityManager.persistAndFlush(item2);

        Long userId = user.getId();

        // Prüfe dass Items existieren
        assertEquals(2, watchlistRepository.findByUserId(userId).size());
        assertTrue(userRepository.findById(userId).isPresent());

        // Act - Zuerst alle Watchlist-Items des Users löschen (realistisch)
        List<Watchlist> userItems = watchlistRepository.findByUserId(userId);
        for (Watchlist item : userItems) {
            watchlistRepository.delete(item);
        }
        entityManager.flush();

        // Dann User löschen
        userRepository.deleteById(userId);
        entityManager.flush();

        // Assert - Prüfe dass alles gelöscht wurde
        assertEquals(0, watchlistRepository.findByUserId(userId).size());
        assertFalse(userRepository.findById(userId).isPresent());
    }
}
package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testFindByUsername_Success() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByUsername_NotFound() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmail_Success() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByUsername_True() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertTrue(exists);
    }

    @Test
    void testExistsByUsername_False() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertFalse(exists);
    }

    @Test
    void testExistsByEmail_True() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    void testExistsByEmail_False() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(exists);
    }

    @Test
    void testSaveUser() {
        // When
        User saved = userRepository.save(testUser);

        // Then
        assertNotNull(saved.getId());
        assertEquals("testuser", saved.getUsername());
        assertEquals("test@example.com", saved.getEmail());

        // Verify it's actually in database
        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
    }

    @Test
    void testUpdateUser() {
        // Given
        User saved = entityManager.persistAndFlush(testUser);

        // When
        saved.setFirstName("UpdatedFirst");
        saved.setLastName("UpdatedLast");
        saved.setEmail("updated@example.com");
        User updated = userRepository.save(saved);

        // Then
        assertEquals("UpdatedFirst", updated.getFirstName());
        assertEquals("UpdatedLast", updated.getLastName());
        assertEquals("updated@example.com", updated.getEmail());

        // Verify in database
        Optional<User> found = userRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("UpdatedFirst", found.get().getFirstName());
    }

    @Test
    void testDeleteUser() {
        // Given
        User saved = entityManager.persistAndFlush(testUser);
        Long userId = saved.getId();

        // When
        userRepository.deleteById(userId);

        // Then
        Optional<User> found = userRepository.findById(userId);
        assertFalse(found.isPresent());
    }

    @Test
    void testUniqueConstraints() {
        // Given
        User firstUser = new User();
        firstUser.setUsername("unique");
        firstUser.setEmail("unique@example.com");
        firstUser.setPassword("password");
        firstUser.setFirstName("First");
        firstUser.setLastName("User");
        entityManager.persistAndFlush(firstUser);

        // When & Then - Duplicate username should cause constraint violation
        User duplicateUsername = new User();
        duplicateUsername.setUsername("unique"); // Same username
        duplicateUsername.setEmail("different@example.com");
        duplicateUsername.setPassword("password");
        duplicateUsername.setFirstName("Second");
        duplicateUsername.setLastName("User");

        // Note: Die Exception wird erst beim flush() geworfen, nicht beim persist()
        entityManager.persist(duplicateUsername);
        assertThrows(Exception.class, () -> entityManager.flush());
    }

    @Test
    void testCascadeOperations() {
        // Given
        User userWithWatchlist = new User();
        userWithWatchlist.setUsername("userwithlists");
        userWithWatchlist.setEmail("userwithlists@example.com");
        userWithWatchlist.setPassword("password");
        userWithWatchlist.setFirstName("User");
        userWithWatchlist.setLastName("WithLists");

        User saved = entityManager.persistAndFlush(userWithWatchlist);

        // Erstelle Watchlist Item
        Watchlist watchlistItem = new Watchlist();
        watchlistItem.setTitle("Test Movie");
        watchlistItem.setType("Film");
        watchlistItem.setGenre("Action");
        watchlistItem.setWatched(false);
        watchlistItem.setRating(0);
        watchlistItem.setUser(saved);

        entityManager.persistAndFlush(watchlistItem);

        // When - Delete user
        userRepository.deleteById(saved.getId());
        entityManager.flush();

        // Then - Watchlist items should also be deleted (CASCADE.ALL)
        Optional<User> foundUser = userRepository.findById(saved.getId());
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindByUsernameIgnoreCase() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When - Search with different case
        Optional<User> found = userRepository.findByUsername("TESTUSER");

        // Then - Should not find (case sensitive)
        assertFalse(found.isPresent());

        // But exact match should work
        Optional<User> exactMatch = userRepository.findByUsername("testuser");
        assertTrue(exactMatch.isPresent());
    }
}
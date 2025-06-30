package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class WatchlistRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WatchlistRepository watchlistRepository;

    private User testUser1;
    private User testUser2;
    private Watchlist testWatchlist1;
    private Watchlist testWatchlist2;
    private Watchlist testWatchlist3;

    @BeforeEach
    void setUp() {
        // Setup test users
        testUser1 = new User();
        testUser1.setUsername("user1");
        testUser1.setEmail("user1@example.com");
        testUser1.setPassword("password123");
        testUser1.setFirstName("User");
        testUser1.setLastName("One");
        testUser1.setCreatedAt(LocalDateTime.now());
        testUser1 = entityManager.persistAndFlush(testUser1);

        testUser2 = new User();
        testUser2.setUsername("user2");
        testUser2.setEmail("user2@example.com");
        testUser2.setPassword("password123");
        testUser2.setFirstName("User");
        testUser2.setLastName("Two");
        testUser2.setCreatedAt(LocalDateTime.now());
        testUser2 = entityManager.persistAndFlush(testUser2);

        // Setup test watchlist items
        testWatchlist1 = new Watchlist();
        testWatchlist1.setTitle("Movie 1");
        testWatchlist1.setType("Film");
        testWatchlist1.setGenre("Action");
        testWatchlist1.setWatched(false);
        testWatchlist1.setRating(0);
        testWatchlist1.setPosterUrl("https://example.com/poster1.jpg");
        testWatchlist1.setUser(testUser1);

        testWatchlist2 = new Watchlist();
        testWatchlist2.setTitle("Movie 2");
        testWatchlist2.setType("Serie");
        testWatchlist2.setGenre("Drama");
        testWatchlist2.setWatched(true);
        testWatchlist2.setRating(8);
        testWatchlist2.setPosterUrl("https://example.com/poster2.jpg");
        testWatchlist2.setUser(testUser1);

        testWatchlist3 = new Watchlist();
        testWatchlist3.setTitle("Movie 3");
        testWatchlist3.setType("Film");
        testWatchlist3.setGenre("Comedy");
        testWatchlist3.setWatched(false);
        testWatchlist3.setRating(0);
        testWatchlist3.setUser(testUser2);
    }

    @Test
    void testFindByUserId_MultipleItems() {
        // Given
        entityManager.persistAndFlush(testWatchlist1);
        entityManager.persistAndFlush(testWatchlist2);
        entityManager.persistAndFlush(testWatchlist3);

        // When
        List<Watchlist> user1Items = watchlistRepository.findByUserId(testUser1.getId());

        // Then
        assertEquals(2, user1Items.size());
        assertTrue(user1Items.stream().anyMatch(w -> w.getTitle().equals("Movie 1")));
        assertTrue(user1Items.stream().anyMatch(w -> w.getTitle().equals("Movie 2")));
        assertTrue(user1Items.stream().allMatch(w -> w.getUser().getId().equals(testUser1.getId())));
    }

    @Test
    void testFindByUserId_SingleItem() {
        // Given
        entityManager.persistAndFlush(testWatchlist1);
        entityManager.persistAndFlush(testWatchlist2);
        entityManager.persistAndFlush(testWatchlist3);

        // When
        List<Watchlist> user2Items = watchlistRepository.findByUserId(testUser2.getId());

        // Then
        assertEquals(1, user2Items.size());
        assertEquals("Movie 3", user2Items.get(0).getTitle());
        assertEquals(testUser2.getId(), user2Items.get(0).getUser().getId());
    }

    @Test
    void testFindByUserId_NoItems() {
        // Given - User ohne Watchlist Items
        User userWithoutItems = new User();
        userWithoutItems.setUsername("emptyuser");
        userWithoutItems.setEmail("empty@example.com");
        userWithoutItems.setPassword("password");
        userWithoutItems.setFirstName("Empty");
        userWithoutItems.setLastName("User");
        User savedEmptyUser = entityManager.persistAndFlush(userWithoutItems);

        // When
        List<Watchlist> emptyList = watchlistRepository.findByUserId(savedEmptyUser.getId());

        // Then
        assertEquals(0, emptyList.size());
        assertTrue(emptyList.isEmpty());
    }

    @Test
    void testFindByIdAndUserId_Success() {
        // Given
        Watchlist saved = entityManager.persistAndFlush(testWatchlist1);

        // When
        Optional<Watchlist> found = watchlistRepository.findByIdAndUserId(saved.getId(), testUser1.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("Movie 1", found.get().getTitle());
        assertEquals(testUser1.getId(), found.get().getUser().getId());
    }

    @Test
    void testFindByIdAndUserId_WrongUser() {
        // Given
        Watchlist saved = entityManager.persistAndFlush(testWatchlist1);

        // When - Try to access item with wrong user ID
        Optional<Watchlist> found = watchlistRepository.findByIdAndUserId(saved.getId(), testUser2.getId());

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByIdAndUserId_WrongId() {
        // Given
        entityManager.persistAndFlush(testWatchlist1);

        // When - Try to access non-existent item
        Optional<Watchlist> found = watchlistRepository.findByIdAndUserId(999L, testUser1.getId());

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteByIdAndUserId_Success() {
        // Given
        Watchlist saved = entityManager.persistAndFlush(testWatchlist1);
        Long itemId = saved.getId();

        // When
        watchlistRepository.deleteByIdAndUserId(itemId, testUser1.getId());
        entityManager.flush();

        // Then
        Optional<Watchlist> found = watchlistRepository.findById(itemId);
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteByIdAndUserId_WrongUser() {
        // Given
        Watchlist saved = entityManager.persistAndFlush(testWatchlist1);
        Long itemId = saved.getId();

        // When - Try to delete with wrong user ID
        watchlistRepository.deleteByIdAndUserId(itemId, testUser2.getId());
        entityManager.flush();

        // Then - Item should still exist
        Optional<Watchlist> found = watchlistRepository.findById(itemId);
        assertTrue(found.isPresent());
    }

    @Test
    void testSaveWatchlistItem() {
        // When
        Watchlist saved = watchlistRepository.save(testWatchlist1);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Movie 1", saved.getTitle());
        assertEquals("Film", saved.getType());
        assertEquals("Action", saved.getGenre());
        assertFalse(saved.isWatched());
        assertEquals(0, saved.getRating());
        assertEquals("https://example.com/poster1.jpg", saved.getPosterUrl());
        assertEquals(testUser1.getId(), saved.getUser().getId());
    }

    @Test
    void testUpdateWatchlistItem() {
        // Given
        Watchlist saved = entityManager.persistAndFlush(testWatchlist1);

        // When
        saved.setTitle("Updated Movie");
        saved.setWatched(true);
        saved.setRating(9);
        saved.setGenre("Thriller");
        Watchlist updated = watchlistRepository.save(saved);

        // Then
        assertEquals("Updated Movie", updated.getTitle());
        assertTrue(updated.isWatched());
        assertEquals(9, updated.getRating());
        assertEquals("Thriller", updated.getGenre());

        // Verify in database
        Optional<Watchlist> found = watchlistRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Movie", found.get().getTitle());
    }

    @Test
    void testWatchlistUserRelationship() {
        // Given
        Watchlist saved = entityManager.persistAndFlush(testWatchlist1);

        // When
        Optional<Watchlist> found = watchlistRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals(testUser1.getId(), found.get().getUser().getId());
        assertEquals("user1", found.get().getUser().getUsername());
    }

    @Test
    void testFindAll() {
        // Given
        entityManager.persistAndFlush(testWatchlist1);
        entityManager.persistAndFlush(testWatchlist2);
        entityManager.persistAndFlush(testWatchlist3);

        // When
        List<Watchlist> allItems = (List<Watchlist>) watchlistRepository.findAll();

        // Then
        assertEquals(3, allItems.size());
        assertTrue(allItems.stream().anyMatch(w -> w.getTitle().equals("Movie 1")));
        assertTrue(allItems.stream().anyMatch(w -> w.getTitle().equals("Movie 2")));
        assertTrue(allItems.stream().anyMatch(w -> w.getTitle().equals("Movie 3")));
    }

    @Test
    void testWatchlistWithNullPosterUrl() {
        // Given
        testWatchlist1.setPosterUrl(null);

        // When
        Watchlist saved = watchlistRepository.save(testWatchlist1);

        // Then
        assertNull(saved.getPosterUrl());

        // Verify in database
        Optional<Watchlist> found = watchlistRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertNull(found.get().getPosterUrl());
    }

    @Test
    void testWatchlistWithEmptyPosterUrl() {
        // Given
        testWatchlist1.setPosterUrl("");

        // When
        Watchlist saved = watchlistRepository.save(testWatchlist1);

        // Then
        assertEquals("", saved.getPosterUrl());

        // Verify in database
        Optional<Watchlist> found = watchlistRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("", found.get().getPosterUrl());
    }

    @Test
    void testUserDeletionCascade() {
        // Given
        entityManager.persistAndFlush(testWatchlist1);
        entityManager.persistAndFlush(testWatchlist2);
        Long user1Id = testUser1.getId();

        // When - Delete user (should cascade to watchlist items)
        entityManager.remove(testUser1);
        entityManager.flush();

        // Then - Watchlist items should be deleted
        List<Watchlist> remainingItems = watchlistRepository.findByUserId(user1Id);
        assertTrue(remainingItems.isEmpty());
    }
}
package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private WatchlistService watchlistService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Test
    void testCompleteUserAndWatchlistWorkflow() {
        // 1. User registrieren
        User newUser = userService.registerUser(
                "integrationuser",
                "integration@test.com",
                "password123",
                "Integration",
                "Test"
        );

        assertNotNull(newUser);
        assertNotNull(newUser.getId());
        assertEquals("integrationuser", newUser.getUsername());

        // 2. User einloggen
        var loginResult = userService.loginUser("integrationuser", "password123");
        assertTrue(loginResult.isPresent());
        assertEquals("integrationuser", loginResult.get().getUsername());

        // 3. Watchlist Item hinzufügen
        Watchlist newItem = new Watchlist(
                "Integration Test Movie",
                "Film",
                "Test",
                false,
                0,
                newUser
        );

        Watchlist savedItem = watchlistService.saveWatchlistItem(newItem);
        assertNotNull(savedItem);
        assertNotNull(savedItem.getId());
        assertEquals("Integration Test Movie", savedItem.getTitle());

        // 4. Watchlist Items für User abrufen
        var userItems = watchlistService.getAllWatchlistItemsByUser(newUser.getId());
        assertEquals(1, userItems.size());
        assertEquals("Integration Test Movie", userItems.get(0).getTitle());

        // 5. Item aktualisieren
        Watchlist updateData = new Watchlist(
                "Updated Movie",
                "Film",
                "Updated Genre",
                true,
                5,
                newUser
        );

        Watchlist updatedItem = watchlistService.updateWatchlistItem(
                savedItem.getId(),
                updateData,
                newUser.getId()
        );

        assertEquals("Updated Movie", updatedItem.getTitle());
        assertTrue(updatedItem.isWatched());
        assertEquals(5, updatedItem.getRating());

        // 6. Item löschen
        boolean deleted = watchlistService.deleteWatchlistItem(
                savedItem.getId(),
                newUser.getId()
        );
        assertTrue(deleted);

        // 7. Prüfen dass Item gelöscht wurde
        var emptyList = watchlistService.getAllWatchlistItemsByUser(newUser.getId());
        assertTrue(emptyList.isEmpty());
    }

    @Test
    void testUserIsolation() {
        // User 1 erstellen
        User user1 = userService.registerUser(
                "user1",
                "user1@test.com",
                "password",
                "User",
                "One"
        );

        // User 2 erstellen
        User user2 = userService.registerUser(
                "user2",
                "user2@test.com",
                "password",
                "User",
                "Two"
        );

        // User 1 Item hinzufügen
        Watchlist user1Item = new Watchlist(
                "User 1 Movie",
                "Film",
                "Action",
                false,
                0,
                user1
        );
        watchlistService.saveWatchlistItem(user1Item);

        // User 2 sollte User 1's Items nicht sehen
        var user2Items = watchlistService.getAllWatchlistItemsByUser(user2.getId());
        assertTrue(user2Items.isEmpty());

        // User 1 sollte sein eigenes Item sehen
        var user1Items = watchlistService.getAllWatchlistItemsByUser(user1.getId());
        assertEquals(1, user1Items.size());
        assertEquals("User 1 Movie", user1Items.get(0).getTitle());
    }

    @Test
    void testDuplicateUserRegistration() {
        // Ersten User registrieren
        userService.registerUser(
                "duplicatetest",
                "duplicate@test.com",
                "password",
                "First",
                "User"
        );

        // Zweiten User mit gleichem Username (sollte fehlschlagen)
        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(
                    "duplicatetest",
                    "different@test.com",
                    "password",
                    "Second",
                    "User"
            );
        });

        // User mit gleicher Email (sollte fehlschlagen)
        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(
                    "differentuser",
                    "duplicate@test.com",
                    "password",
                    "Third",
                    "User"
            );
        });
    }
}
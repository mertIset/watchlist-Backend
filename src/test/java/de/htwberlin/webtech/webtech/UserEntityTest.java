package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testDefaultConstructor() {
        // When
        User newUser = new User();

        // Then
        assertNotNull(newUser);
        assertNotNull(newUser.getCreatedAt()); // Should be set automatically
        assertTrue(newUser.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(newUser.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void testParameterizedConstructor() {
        // When
        User newUser = new User("testuser", "test@example.com",
                "password123", "Test", "User");

        // Then
        assertEquals("testuser", newUser.getUsername());
        assertEquals("test@example.com", newUser.getEmail());
        assertEquals("password123", newUser.getPassword());
        assertEquals("Test", newUser.getFirstName());
        assertEquals("User", newUser.getLastName());
        assertNotNull(newUser.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String firstName = "Test";
        String lastName = "User";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime lastLogin = LocalDateTime.now();

        // When
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCreatedAt(createdAt);
        user.setLastLogin(lastLogin);

        // Then
        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(lastLogin, user.getLastLogin());
    }

    @Test
    void testWatchlistItemsRelationship() {
        // Given
        user.setWatchlistItems(new ArrayList<>());

        Watchlist item1 = new Watchlist();
        item1.setTitle("Movie 1");
        item1.setUser(user);

        Watchlist item2 = new Watchlist();
        item2.setTitle("Movie 2");
        item2.setUser(user);

        // When
        user.getWatchlistItems().add(item1);
        user.getWatchlistItems().add(item2);

        // Then
        assertEquals(2, user.getWatchlistItems().size());
        assertTrue(user.getWatchlistItems().contains(item1));
        assertTrue(user.getWatchlistItems().contains(item2));
        assertEquals(user, item1.getUser());
        assertEquals(user, item2.getUser());
    }

    @Test
    void testNullValues() {
        // When setting null values
        user.setId(null);
        user.setUsername(null);
        user.setEmail(null);
        user.setPassword(null);
        user.setFirstName(null);
        user.setLastName(null);
        user.setLastLogin(null);
        user.setWatchlistItems(null);

        // Then
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getLastLogin());
        assertNull(user.getWatchlistItems());
        // createdAt should still be set from constructor
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void testEmptyStringValues() {
        // When setting empty strings
        user.setUsername("");
        user.setEmail("");
        user.setPassword("");
        user.setFirstName("");
        user.setLastName("");

        // Then
        assertEquals("", user.getUsername());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPassword());
        assertEquals("", user.getFirstName());
        assertEquals("", user.getLastName());
    }

    @Test
    void testCreatedAtImmutability() {
        // Given
        LocalDateTime originalCreatedAt = user.getCreatedAt();

        // When - Try to modify the returned LocalDateTime
        LocalDateTime modifiedTime = user.getCreatedAt().plusDays(1);

        // Then - Original should be unchanged
        assertEquals(originalCreatedAt, user.getCreatedAt());
        assertNotEquals(modifiedTime, user.getCreatedAt());
    }

    @Test
    void testLongUsernameAndEmail() {
        // Given
        String longUsername = "a".repeat(255); // Very long username
        String longEmail = "a".repeat(240) + "@example.com"; // Very long email

        // When
        user.setUsername(longUsername);
        user.setEmail(longEmail);

        // Then
        assertEquals(longUsername, user.getUsername());
        assertEquals(longEmail, user.getEmail());
    }

    @Test
    void testSpecialCharactersInFields() {
        // Given
        String usernameWithSpecialChars = "test_user-123";
        String emailWithSpecialChars = "test+tag@sub.example.com";
        String nameWithSpecialChars = "O'Connor";
        String passwordWithSpecialChars = "P@ssw0rd!123";

        // When
        user.setUsername(usernameWithSpecialChars);
        user.setEmail(emailWithSpecialChars);
        user.setFirstName(nameWithSpecialChars);
        user.setLastName(nameWithSpecialChars);
        user.setPassword(passwordWithSpecialChars);

        // Then
        assertEquals(usernameWithSpecialChars, user.getUsername());
        assertEquals(emailWithSpecialChars, user.getEmail());
        assertEquals(nameWithSpecialChars, user.getFirstName());
        assertEquals(nameWithSpecialChars, user.getLastName());
        assertEquals(passwordWithSpecialChars, user.getPassword());
    }

    @Test
    void testEqualsAndHashCode() {
        // Note: Die User-Entity hat möglicherweise keine equals/hashCode Implementation
        // Dieser Test prüft das Standard-Verhalten

        // Given
        User user1 = new User("test", "test@example.com", "pass", "First", "Last");
        User user2 = new User("test", "test@example.com", "pass", "First", "Last");

        // When & Then
        // Ohne equals-Implementation sollten sie unterschiedlich sein
        assertNotEquals(user1, user2);
        assertNotEquals(user1.hashCode(), user2.hashCode());

        // Aber mit sich selbst sollte es gleich sein
        assertEquals(user1, user1);
        assertEquals(user1.hashCode(), user1.hashCode());
    }

    @Test
    void testToString() {
        // Given
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        // When
        String toString = user.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("User")); // Should contain class name
        // Note: Ohne custom toString-Implementation ist der Inhalt nicht vorhersagbar
    }

    @Test
    void testWatchlistItemsCascade() {
        // Given
        user.setWatchlistItems(new ArrayList<>());

        Watchlist item = new Watchlist();
        item.setTitle("Test Movie");
        item.setType("Film");
        item.setGenre("Action");
        item.setWatched(false);
        item.setRating(0);
        item.setUser(user);

        // When
        user.getWatchlistItems().add(item);

        // Then
        assertEquals(1, user.getWatchlistItems().size());
        assertEquals(item, user.getWatchlistItems().get(0));
        assertEquals(user, item.getUser());
    }

    @Test
    void testDateTimeFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastTime = now.minusDays(30);
        LocalDateTime futureTime = now.plusDays(30);

        // When
        user.setCreatedAt(pastTime);
        user.setLastLogin(futureTime);

        // Then
        assertEquals(pastTime, user.getCreatedAt());
        assertEquals(futureTime, user.getLastLogin());
        assertTrue(user.getCreatedAt().isBefore(user.getLastLogin()));
    }
}
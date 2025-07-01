package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class WatchlistServiceTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private OMDbService omdbService;

    @InjectMocks
    private WatchlistService watchlistService;

    private User testUser;
    private Watchlist testWatchlistItem;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password", "Test", "User");
        testUser.setId(1L);

        testWatchlistItem = new Watchlist("Inception", "Film", "Sci-Fi", false, 0, testUser);
        testWatchlistItem.setId(1L);
    }

    @Test
    void testGetAllWatchlistItemsByUser() {
        // Arrange
        Long userId = 1L;
        List<Watchlist> expectedItems = Arrays.asList(testWatchlistItem);
        when(watchlistRepository.findByUserId(userId)).thenReturn(expectedItems);

        // Act
        List<Watchlist> result = watchlistService.getAllWatchlistItemsByUser(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Inception", result.get(0).getTitle());
        verify(watchlistRepository).findByUserId(userId);
    }

    @Test
    void testSaveWatchlistItem_WithoutPoster() {
        // Arrange
        Watchlist newItem = new Watchlist("The Matrix", "Film", "Sci-Fi", false, 0, testUser);
        String expectedPosterUrl = "http://example.com/poster.jpg";

        when(omdbService.fetchPosterUrl("The Matrix", "Film")).thenReturn(expectedPosterUrl);
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> {
            Watchlist item = invocation.getArgument(0);
            item.setId(2L);
            return item;
        });

        // Act
        Watchlist result = watchlistService.saveWatchlistItem(newItem);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPosterUrl, result.getPosterUrl());
        verify(omdbService).fetchPosterUrl("The Matrix", "Film");
        verify(watchlistRepository).save(newItem);
    }

    @Test
    void testSaveWatchlistItem_WithExistingPoster() {
        // Arrange
        String existingPosterUrl = "http://existing.com/poster.jpg";
        Watchlist newItem = new Watchlist("The Matrix", "Film", "Sci-Fi", false, 0, existingPosterUrl, testUser);

        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Watchlist result = watchlistService.saveWatchlistItem(newItem);

        // Assert
        assertEquals(existingPosterUrl, result.getPosterUrl());
        verify(omdbService, never()).fetchPosterUrl(anyString(), anyString());
        verify(watchlistRepository).save(newItem);
    }

    @Test
    void testDeleteWatchlistItem_Success() {
        // Arrange
        Long itemId = 1L;
        Long userId = 1L;
        when(watchlistRepository.findByIdAndUserId(itemId, userId)).thenReturn(Optional.of(testWatchlistItem));

        // Act
        boolean result = watchlistService.deleteWatchlistItem(itemId, userId);

        // Assert
        assertTrue(result);
        verify(watchlistRepository).findByIdAndUserId(itemId, userId);
        verify(watchlistRepository).deleteById(itemId);
    }

    @Test
    void testUpdateWatchlistItem_TitleChanged() {
        // Arrange
        Long itemId = 1L;
        Long userId = 1L;

        Watchlist updatedItem = new Watchlist("The Matrix", "Film", "Action", true, 5, testUser);
        String newPosterUrl = "http://matrix-poster.com/image.jpg";

        when(watchlistRepository.findByIdAndUserId(itemId, userId)).thenReturn(Optional.of(testWatchlistItem));
        when(omdbService.fetchPosterUrl("The Matrix", "Film")).thenReturn(newPosterUrl);
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Watchlist result = watchlistService.updateWatchlistItem(itemId, updatedItem, userId);

        // Assert
        assertEquals("The Matrix", result.getTitle());
        assertEquals("Action", result.getGenre());
        assertTrue(result.isWatched());
        assertEquals(5, result.getRating());
        assertEquals(newPosterUrl, result.getPosterUrl());

        verify(omdbService).fetchPosterUrl("The Matrix", "Film");
        verify(watchlistRepository).save(testWatchlistItem);
    }
}
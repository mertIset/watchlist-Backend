package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testWatchlistItem = new Watchlist();
        testWatchlistItem.setId(1L);
        testWatchlistItem.setTitle("Inception");
        testWatchlistItem.setType("Film");
        testWatchlistItem.setGenre("Sci-Fi");
        testWatchlistItem.setWatched(true);
        testWatchlistItem.setRating(9);
        testWatchlistItem.setUser(testUser);
    }

    @Test
    void saveWatchlistItem_ShouldSaveWithPoster_WhenPosterUrlNotProvided() {
        // Arrange
        Watchlist itemWithoutPoster = new Watchlist("Test Movie", "Film", "Action", false, 0, testUser);
        String expectedPosterUrl = "https://example.com/poster.jpg";

        when(omdbService.fetchPosterUrl("Test Movie", "Film")).thenReturn(expectedPosterUrl);
        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(testWatchlistItem);

        // Act
        Watchlist result = watchlistService.saveWatchlistItem(itemWithoutPoster);

        // Assert
        assertNotNull(result);
        verify(omdbService).fetchPosterUrl("Test Movie", "Film");
        verify(watchlistRepository).save(any(Watchlist.class));
    }

    @Test
    void saveWatchlistItem_ShouldNotFetchPoster_WhenPosterUrlProvided() {
        // Arrange
        String existingPosterUrl = "https://existing.com/poster.jpg";
        Watchlist itemWithPoster = new Watchlist("Test Movie", "Film", "Action", false, 0, existingPosterUrl, testUser);

        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(testWatchlistItem);

        // Act
        Watchlist result = watchlistService.saveWatchlistItem(itemWithPoster);

        // Assert
        assertNotNull(result);
        verify(omdbService, never()).fetchPosterUrl(anyString(), anyString());
        verify(watchlistRepository).save(any(Watchlist.class));
    }

    @Test
    void getAllWatchlistItemsByUser_ShouldReturnUserItems() {
        // Arrange
        List<Watchlist> expectedItems = Arrays.asList(testWatchlistItem);
        when(watchlistRepository.findByUserId(1L)).thenReturn(expectedItems);

        // Act
        List<Watchlist> result = watchlistService.getAllWatchlistItemsByUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Inception", result.get(0).getTitle());
        verify(watchlistRepository).findByUserId(1L);
    }

    @Test
    void getWatchlistItem_ShouldReturnItem_WhenItemExists() {
        // Arrange
        when(watchlistRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testWatchlistItem));

        // Act
        Optional<Watchlist> result = watchlistService.getWatchlistItem(1L, 1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Inception", result.get().getTitle());
        verify(watchlistRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    void getWatchlistItem_ShouldReturnEmpty_WhenItemNotExists() {
        // Arrange
        when(watchlistRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        // Act
        Optional<Watchlist> result = watchlistService.getWatchlistItem(999L, 1L);

        // Assert
        assertFalse(result.isPresent());
        verify(watchlistRepository).findByIdAndUserId(999L, 1L);
    }

    @Test
    void deleteWatchlistItem_ShouldReturnTrue_WhenItemExists() {
        // Arrange
        when(watchlistRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testWatchlistItem));

        // Act
        boolean result = watchlistService.deleteWatchlistItem(1L, 1L);

        // Assert
        assertTrue(result);
        verify(watchlistRepository).findByIdAndUserId(1L, 1L);
        verify(watchlistRepository).deleteById(1L);
    }

    @Test
    void deleteWatchlistItem_ShouldReturnFalse_WhenItemNotExists() {
        // Arrange
        when(watchlistRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        // Act
        boolean result = watchlistService.deleteWatchlistItem(999L, 1L);

        // Assert
        assertFalse(result);
        verify(watchlistRepository).findByIdAndUserId(999L, 1L);
        verify(watchlistRepository, never()).deleteById(any());
    }

    @Test
    void updateWatchlistItem_ShouldUpdateAndFetchNewPoster_WhenTitleChanged() {
        // Arrange
        Watchlist updatedItem = new Watchlist();
        updatedItem.setTitle("Updated Movie"); // Changed title
        updatedItem.setType("Film");
        updatedItem.setGenre("Drama");
        updatedItem.setWatched(false);
        updatedItem.setRating(8);

        when(watchlistRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testWatchlistItem));
        when(omdbService.fetchPosterUrl("Updated Movie", "Film")).thenReturn("new-poster-url");
        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(testWatchlistItem);

        // Act
        Watchlist result = watchlistService.updateWatchlistItem(1L, updatedItem, 1L);

        // Assert
        assertNotNull(result);
        verify(watchlistRepository).findByIdAndUserId(1L, 1L);
        verify(omdbService).fetchPosterUrl("Updated Movie", "Film");
        verify(watchlistRepository).save(any(Watchlist.class));
    }

    @Test
    void updateWatchlistItem_ShouldThrowException_WhenItemNotFound() {
        // Arrange
        Watchlist updatedItem = new Watchlist();
        when(watchlistRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            watchlistService.updateWatchlistItem(999L, updatedItem, 1L);
        });

        assertTrue(exception.getMessage().contains("not found"));
        verify(watchlistRepository).findByIdAndUserId(999L, 1L);
        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    void refreshPoster_ShouldFetchNewPoster() {
        // Arrange
        String newPosterUrl = "https://new-poster.com/image.jpg";
        when(watchlistRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testWatchlistItem));
        when(omdbService.fetchPosterUrl("Inception", "Film")).thenReturn(newPosterUrl);
        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(testWatchlistItem);

        // Act
        Watchlist result = watchlistService.refreshPoster(1L, 1L);

        // Assert
        assertNotNull(result);
        verify(watchlistRepository).findByIdAndUserId(1L, 1L);
        verify(omdbService).fetchPosterUrl("Inception", "Film");
        verify(watchlistRepository).save(any(Watchlist.class));
    }
}
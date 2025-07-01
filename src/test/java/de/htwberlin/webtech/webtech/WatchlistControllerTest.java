package de.htwberlin.webtech.webtech;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WatchlistController.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class WatchlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // Hier können wir noch @MockBean verwenden für @WebMvcTest
    private WatchlistService watchlistService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Watchlist testWatchlistItem;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password", "Test", "User");
        testUser.setId(1L);

        testWatchlistItem = new Watchlist("Inception", "Film", "Sci-Fi", false, 0, testUser);
        testWatchlistItem.setId(1L);
        testWatchlistItem.setPosterUrl("http://example.com/poster.jpg");
    }

    @Test
    void testGetAllWatchlistItems_WithUserId() throws Exception {
        // Arrange
        Long userId = 1L;
        List<Watchlist> mockItems = Arrays.asList(testWatchlistItem);

        when(watchlistService.getAllWatchlistItemsByUser(userId)).thenReturn(mockItems);

        // Act & Assert
        mockMvc.perform(get("/Watchlist")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Inception"))
                .andExpect(jsonPath("$[0].type").value("Film"));

        verify(watchlistService).getAllWatchlistItemsByUser(userId);
        verify(watchlistService, never()).getAllWatchlistItems();
    }

    @Test
    void testAddWatchlistItem_Success() throws Exception {
        // Arrange
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(watchlistService.saveWatchlistItem(any(Watchlist.class))).thenReturn(testWatchlistItem);

        WatchlistController.WatchlistRequest request = new WatchlistController.WatchlistRequest();
        request.setTitle("Inception");
        request.setType("Film");
        request.setGenre("Sci-Fi");
        request.setWatched(false);
        request.setRating(0);
        request.setUserId(1L);

        // Act & Assert
        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception"))
                .andExpect(jsonPath("$.type").value("Film"))
                .andExpect(jsonPath("$.posterUrl").value("http://example.com/poster.jpg"));

        verify(userService).findById(1L);
        verify(watchlistService).saveWatchlistItem(any(Watchlist.class));
    }

    @Test
    void testDeleteWatchlistItem_Success() throws Exception {
        // Arrange
        Long itemId = 1L;
        Long userId = 1L;
        when(watchlistService.deleteWatchlistItem(itemId, userId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/Watchlist/{id}", itemId)
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(watchlistService).deleteWatchlistItem(itemId, userId);
    }

    @Test
    void testGetWatchlistItem_Found() throws Exception {
        // Arrange
        Long itemId = 1L;
        Long userId = 1L;

        when(watchlistService.getWatchlistItem(itemId, userId)).thenReturn(Optional.of(testWatchlistItem));

        // Act & Assert
        mockMvc.perform(get("/Watchlist/{id}", itemId)
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception"))
                .andExpect(jsonPath("$.type").value("Film"));

        verify(watchlistService).getWatchlistItem(itemId, userId);
    }
}
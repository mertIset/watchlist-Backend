package de.htwberlin.webtech.webtech;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WatchlistController.class)
@ActiveProfiles("test")
@Tag(TestConstants.TestCategories.CONTROLLER_TEST)
class WatchlistControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WatchlistService watchlistService;

    @MockBean
    private UserService userService;

    private User testUser;
    private Watchlist testWatchlist;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.aUser()
                .withId(1L)
                .withUsername("testuser")
                .withEmail("test@example.com")
                .build();

        testWatchlist = TestDataBuilder.aWatchlistItem()
                .withId(1L)
                .withTitle("Test Movie")
                .withType("Film")
                .withGenre("Action")
                .withWatched(false)
                .withRating(0)
                .withPosterUrl("https://example.com/poster.jpg")
                .withUser(testUser)
                .build();
    }

    @Test
    void testGetAllWatchlistItems_WithUserId() throws Exception {
        // Given
        List<Watchlist> watchlistItems = Arrays.asList(testWatchlist);
        when(watchlistService.getAllWatchlistItemsByUser(1L)).thenReturn(watchlistItems);

        // When & Then
        mockMvc.perform(get("/Watchlist")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Movie"))
                .andExpect(jsonPath("$[0].type").value("Film"))
                .andExpect(jsonPath("$[0].genre").value("Action"))
                .andExpect(jsonPath("$[0].watched").value(false))
                .andExpect(jsonPath("$[0].rating").value(0))
                .andExpect(jsonPath("$[0].posterUrl").value("https://example.com/poster.jpg"));
    }

    @Test
    void testGetAllWatchlistItems_WithoutUserId() throws Exception {
        // Given
        List<Watchlist> watchlistItems = Arrays.asList(testWatchlist);
        when(watchlistService.getAllWatchlistItems()).thenReturn(watchlistItems);

        // When & Then
        mockMvc.perform(get("/Watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Movie"));
    }

    @Test
    void testAddWatchlistItem_Success() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(watchlistService.saveWatchlistItem(any(Watchlist.class))).thenReturn(testWatchlist);

        WatchlistController.WatchlistRequest request = TestDataBuilder.aWatchlistRequest()
                .withTitle("New Movie")
                .withType("Film")
                .withGenre("Drama")
                .withWatched(true)
                .withRating(8)
                .withUserId(1L)
                .build();

        // When & Then
        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Movie"))
                .andExpect(jsonPath("$.type").value("Film"));
    }

    @Test
    void testAddWatchlistItem_UserNotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(Optional.empty());

        WatchlistController.WatchlistRequest request = TestDataBuilder.aWatchlistRequest()
                .withTitle("New Movie")
                .withType("Film")
                .withGenre("Drama")
                .withWatched(true)
                .withRating(8)
                .withUserId(999L)
                .build();

        // When & Then
        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testDeleteWatchlistItem_Success() throws Exception {
        // Given
        when(watchlistService.deleteWatchlistItem(1L, 1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/Watchlist/1")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testDeleteWatchlistItem_NotFound() throws Exception {
        // Given
        when(watchlistService.deleteWatchlistItem(999L, 1L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/Watchlist/999")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testUpdateWatchlistItem_Success() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(watchlistService.updateWatchlistItem(anyLong(), any(Watchlist.class), anyLong()))
                .thenReturn(testWatchlist);

        WatchlistController.WatchlistRequest request = TestDataBuilder.aWatchlistRequest()
                .withTitle("Updated Movie")
                .withType("Serie")
                .withGenre("Drama")
                .withWatched(true)
                .withRating(9)
                .withUserId(1L)
                .build();

        // When & Then
        mockMvc.perform(put("/Watchlist/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.title").value("Test Movie"));
    }

    @Test
    void testUpdateWatchlistItem_UserNotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(Optional.empty());

        WatchlistController.WatchlistRequest request = TestDataBuilder.aWatchlistRequest()
                .withTitle("Updated Movie")
                .withType("Serie")
                .withGenre("Drama")
                .withWatched(true)
                .withRating(9)
                .withUserId(999L)
                .build();

        // When & Then
        mockMvc.perform(put("/Watchlist/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetWatchlistItem_Success() throws Exception {
        // Given
        when(watchlistService.getWatchlistItem(1L, 1L)).thenReturn(Optional.of(testWatchlist));

        // When & Then
        mockMvc.perform(get("/Watchlist/1")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Movie"))
                .andExpect(jsonPath("$.type").value("Film"))
                .andExpect(jsonPath("$.genre").value("Action"));
    }

    @Test
    void testGetWatchlistItem_NotFound() throws Exception {
        // Given
        when(watchlistService.getWatchlistItem(999L, 1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/Watchlist/999")
                        .param("userId", "1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testRefreshPoster_Success() throws Exception {
        // Given
        Watchlist updatedWatchlist = TestDataBuilder.aWatchlistItem()
                .withId(1L)
                .withTitle("Test Movie")
                .withPosterUrl("https://new-poster.com/image.jpg")
                .build();

        when(watchlistService.refreshPoster(1L, 1L)).thenReturn(updatedWatchlist);

        // When & Then
        mockMvc.perform(post("/Watchlist/1/refresh-poster")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posterUrl").value("https://new-poster.com/image.jpg"));
    }

    @Test
    void testRefreshAllPosters_Success() throws Exception {
        // Given
        // watchlistService.refreshAllMissingPosters hat void return type

        // When & Then
        mockMvc.perform(post("/Watchlist/refresh-all-posters")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cover-Update für alle Einträge gestartet!"));
    }

    @Test
    void testRefreshAllPosters_Exception() throws Exception {
        // Given
        doThrow(new RuntimeException("Database error"))
                .when(watchlistService).refreshAllMissingPosters(1L);

        // When & Then
        mockMvc.perform(post("/Watchlist/refresh-all-posters")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fehler beim Aktualisieren der Cover")));
    }

    @Test
    void testAddWatchlistItem_InvalidRequest() throws Exception {
        // Given - Request ohne erforderliche Felder
        WatchlistController.WatchlistRequest request = new WatchlistController.WatchlistRequest();
        // Nur userId gesetzt, andere Felder null
        request.setUserId(1L);

        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Controller validiert nicht streng
    }

    @Test
    void testCORSHeaders() throws Exception {
        // Test, dass CORS-Header korrekt gesetzt werden
        mockMvc.perform(options("/Watchlist")
                        .header("Origin", TestConstants.Http.CORS_ORIGIN_LOCALHOST)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void testGetWatchlistWithValidUserId() throws Exception {
        // Given
        List<Watchlist> emptyList = Arrays.asList();
        when(watchlistService.getAllWatchlistItemsByUser(1L)).thenReturn(emptyList);

        // When & Then
        mockMvc.perform(get("/Watchlist")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testRequestValidation() throws Exception {
        // Test mit komplett leerem JSON
        String emptyJson = "{}";

        // When & Then
        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isInternalServerError()); // Wegen null userId
    }
}
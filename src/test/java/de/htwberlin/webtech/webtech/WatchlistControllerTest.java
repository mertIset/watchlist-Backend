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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testGetAllWatchlistItems_EmptyList() throws Exception {
        // Given
        when(watchlistService.getAllWatchlistItemsByUser(1L)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/Watchlist")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testAddWatchlistItem_WithPosterUrl() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(watchlistService.saveWatchlistItem(any(Watchlist.class))).thenReturn(testWatchlist);

        WatchlistController.WatchlistRequest request = TestDataBuilder.aWatchlistRequest()
                .withTitle("Movie with Poster")
                .withType("Film")
                .withGenre("Action")
                .withWatched(false)
                .withRating(0)
                .withPosterUrl("https://custom-poster.com/image.jpg")
                .withUserId(1L)
                .build();

        // When & Then
        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Movie"));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testDeleteWatchlistItem_MissingUserId() throws Exception {
        // When & Then - Missing required userId parameter
        mockMvc.perform(delete("/Watchlist/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
                .andExpect(jsonPath("$.title").value("Test Movie"));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testUpdateWatchlistItem_WatchlistNotFound() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(watchlistService.updateWatchlistItem(anyLong(), any(Watchlist.class), anyLong()))
                .thenThrow(new RuntimeException("Watchlist item not found"));

        WatchlistController.WatchlistRequest request = TestDataBuilder.aWatchlistRequest()
                .withTitle("Updated Movie")
                .withType("Serie")
                .withGenre("Drama")
                .withWatched(true)
                .withRating(9)
                .withUserId(1L)
                .build();

        // When & Then
        mockMvc.perform(put("/Watchlist/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testGetWatchlistItem_NotFound() throws Exception {
        // Given
        when(watchlistService.getWatchlistItem(999L, 1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/Watchlist/999")
                        .param("userId", "1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testGetWatchlistItem_MissingUserId() throws Exception {
        // When & Then - Missing required userId parameter
        mockMvc.perform(get("/Watchlist/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testRefreshPoster_ItemNotFound() throws Exception {
        // Given
        when(watchlistService.refreshPoster(999L, 1L))
                .thenThrow(new RuntimeException("Watchlist item not found"));

        // When & Then
        mockMvc.perform(post("/Watchlist/999/refresh-poster")
                        .param("userId", "1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testRefreshAllPosters_Success() throws Exception {
        // Given
        doNothing().when(watchlistService).refreshAllMissingPosters(1L);

        // When & Then
        mockMvc.perform(post("/Watchlist/refresh-all-posters")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cover-Update für alle Einträge gestartet!"));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
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
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testAddWatchlistItem_InvalidRequest() throws Exception {
        // Given - Request ohne erforderliche Felder
        WatchlistController.WatchlistRequest request = new WatchlistController.WatchlistRequest();
        // Nur userId gesetzt, andere Felder null
        request.setUserId(1L);

        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(watchlistService.saveWatchlistItem(any(Watchlist.class))).thenReturn(testWatchlist);

        // When & Then
        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Controller validiert nicht streng
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testAddWatchlistItem_NullUserId() throws Exception {
        // Given - Request ohne userId
        WatchlistController.WatchlistRequest request = TestDataBuilder.aWatchlistRequest()
                .withTitle("Test Movie")
                .withType("Film")
                .withGenre("Action")
                .withUserId(null) // null userId
                .build();

        // When & Then
        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // NullPointerException
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testCORSHeaders() throws Exception {
        // Test, dass CORS-Header korrekt gesetzt werden
        mockMvc.perform(options("/Watchlist")
                        .header("Origin", TestConstants.Http.CORS_ORIGIN_LOCALHOST)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testAddWatchlistItem_SpecialCharacters() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(watchlistService.saveWatchlistItem(any(Watchlist.class))).thenReturn(testWatchlist);

        WatchlistController.WatchlistRequest request = TestDataBuilder.aWatchlistRequest()
                .withTitle("Der Herr der Ringe: Die Gefährten")
                .withType("Film")
                .withGenre("Fantasy/Abenteuer")
                .withWatched(false)
                .withRating(0)
                .withUserId(1L)
                .build();

        // When & Then
        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Movie"));
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testAddWatchlistItem_MaxRating() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(watchlistService.saveWatchlistItem(any(Watchlist.class))).thenReturn(testWatchlist);

        WatchlistController.WatchlistRequest request = TestDataBuilder.aWatchlistRequest()
                .withTitle("Perfect Movie")
                .withType("Film")
                .withGenre("Drama")
                .withWatched(true)
                .withRating(10) // Maximum rating
                .withUserId(1L)
                .build();

        // When & Then
        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testAddWatchlistItem_DifferentTypes() throws Exception {
        // Test verschiedene Medientypen
        String[] types = {"Film", "Serie", "Dokumentation", "Anime"};

        for (String type : types) {
            when(userService.findById(1L)).thenReturn(Optional.of(testUser));
            when(watchlistService.saveWatchlistItem(any(Watchlist.class))).thenReturn(testWatchlist);

            WatchlistController.WatchlistRequest request = TestDataBuilder.aWatchlistRequest()
                    .withTitle("Test " + type)
                    .withType(type)
                    .withGenre("Test")
                    .withWatched(false)
                    .withRating(0)
                    .withUserId(1L)
                    .build();

            mockMvc.perform(post("/Watchlist")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testRequestValidation() throws Exception {
        // Test mit komplett leerem JSON
        String emptyJson = "{}";

        // When & Then
        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isInternalServerError()); // Wegen null userId
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testGetWatchlistWithValidUserId() throws Exception {
        // Given
        List<Watchlist> emptyList = Collections.emptyList();
        when(watchlistService.getAllWatchlistItemsByUser(1L)).thenReturn(emptyList);

        // When & Then
        mockMvc.perform(get("/Watchlist")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Tag(TestConstants.TestCategories.FAST_TEST)
    void testInvalidPathVariables() throws Exception {
        // Test mit ungültigen ID-Formaten
        mockMvc.perform(get("/Watchlist/abc")
                        .param("userId", "1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/Watchlist/xyz")
                        .param("userId", "1"))
                .andExpect(status().isBadRequest());
    }
}
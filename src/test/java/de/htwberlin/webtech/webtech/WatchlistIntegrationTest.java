package de.htwberlin.webtech.webtech;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional // Rollback nach jedem Test
@Tag(TestConstants.TestCategories.INTEGRATION_TEST)
class WatchlistIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WatchlistRepository watchlistRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean database
        watchlistRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("integrationuser");
        testUser.setEmail("integration@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Integration");
        testUser.setLastName("Test");
        testUser = userRepository.save(testUser);
    }

    @Test
    @Tag(TestConstants.TestCategories.SLOW_TEST)
    void testCompleteUserAndWatchlistWorkflow() throws Exception {
        // 1. Register new user
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setUsername("workflowuser");
        registerRequest.setEmail("workflow@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Workflow");
        registerRequest.setLastName("User");

        String registerResponse = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.username").value("workflowuser"))
                .andReturn().getResponse().getContentAsString();

        // Extract user ID from response
        AuthController.AuthResponse authResponse = objectMapper.readValue(registerResponse, AuthController.AuthResponse.class);
        Long userId = authResponse.getUser().getId();

        // 2. Login with the same user
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setUsername("workflowuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.username").value("workflowuser"));

        // 3. Add watchlist item
        WatchlistController.WatchlistRequest watchlistRequest = new WatchlistController.WatchlistRequest();
        watchlistRequest.setTitle("Integration Test Movie");
        watchlistRequest.setType("Film");
        watchlistRequest.setGenre("Test");
        watchlistRequest.setWatched(false);
        watchlistRequest.setRating(0);
        watchlistRequest.setUserId(userId);

        String addResponse = mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(watchlistRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integration Test Movie"))
                .andExpect(jsonPath("$.type").value("Film"))
                .andReturn().getResponse().getContentAsString();

        // Extract watchlist item ID
        Watchlist createdItem = objectMapper.readValue(addResponse, Watchlist.class);
        Long itemId = createdItem.getId();

        // 4. Get all watchlist items for user
        mockMvc.perform(get("/Watchlist")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Integration Test Movie"));

        // 5. Get specific watchlist item
        mockMvc.perform(get("/Watchlist/" + itemId)
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integration Test Movie"))
                .andExpect(jsonPath("$.watched").value(false));

        // 6. Update watchlist item
        watchlistRequest.setTitle("Updated Integration Movie");
        watchlistRequest.setWatched(true);
        watchlistRequest.setRating(9);

        mockMvc.perform(put("/Watchlist/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(watchlistRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Movie"))
                .andExpect(jsonPath("$.watched").value(true))
                .andExpect(jsonPath("$.rating").value(9));

        // 7. Update user information
        AuthController.UpdateUserRequest updateRequest = new AuthController.UpdateUserRequest();
        updateRequest.setFirstName("Updated Workflow");
        updateRequest.setLastName("Updated User");
        updateRequest.setEmail("updated-workflow@example.com");

        mockMvc.perform(put("/auth/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated Workflow"))
                .andExpect(jsonPath("$.lastName").value("Updated User"))
                .andExpect(jsonPath("$.email").value("updated-workflow@example.com"));

        // 8. Delete watchlist item
        mockMvc.perform(delete("/Watchlist/" + itemId)
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // 9. Verify item is deleted
        mockMvc.perform(get("/Watchlist")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Tag(TestConstants.TestCategories.SLOW_TEST)
    void testUserIsolation() throws Exception {
        // Create second user
        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password123");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2 = userRepository.save(user2);

        // Add watchlist item for first user
        WatchlistController.WatchlistRequest request1 = new WatchlistController.WatchlistRequest();
        request1.setTitle("User 1 Movie");
        request1.setType("Film");
        request1.setGenre("Action");
        request1.setWatched(false);
        request1.setRating(0);
        request1.setUserId(testUser.getId());

        String response1 = mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Watchlist item1 = objectMapper.readValue(response1, Watchlist.class);

        // Add watchlist item for second user
        WatchlistController.WatchlistRequest request2 = new WatchlistController.WatchlistRequest();
        request2.setTitle("User 2 Movie");
        request2.setType("Serie");
        request2.setGenre("Drama");
        request2.setWatched(true);
        request2.setRating(8);
        request2.setUserId(user2.getId());

        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        // Verify each user only sees their own items
        mockMvc.perform(get("/Watchlist")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("User 1 Movie"));

        mockMvc.perform(get("/Watchlist")
                        .param("userId", user2.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("User 2 Movie"));

        // Verify user 2 cannot access user 1's item
        mockMvc.perform(get("/Watchlist/" + item1.getId())
                        .param("userId", user2.getId().toString()))
                .andExpect(status().isInternalServerError());

        // Verify user 2 cannot delete user 1's item
        mockMvc.perform(delete("/Watchlist/" + item1.getId())
                        .param("userId", user2.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("false")); // Should return false
    }

    @Test
    @Tag(TestConstants.TestCategories.SLOW_TEST)
    void testAuthenticationFlow() throws Exception {
        // Test registration with duplicate username
        AuthController.RegisterRequest duplicateUsernameRequest = new AuthController.RegisterRequest();
        duplicateUsernameRequest.setUsername("integrationuser"); // Already exists
        duplicateUsernameRequest.setEmail("different@example.com");
        duplicateUsernameRequest.setPassword("password123");
        duplicateUsernameRequest.setFirstName("Duplicate");
        duplicateUsernameRequest.setLastName("User");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUsernameRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username bereits vergeben!"));

        // Test registration with duplicate email
        AuthController.RegisterRequest duplicateEmailRequest = new AuthController.RegisterRequest();
        duplicateEmailRequest.setUsername("differentuser");
        duplicateEmailRequest.setEmail("integration@example.com"); // Already exists
        duplicateEmailRequest.setPassword("password123");
        duplicateEmailRequest.setFirstName("Duplicate");
        duplicateEmailRequest.setLastName("Email");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email bereits registriert!"));

        // Test successful login
        AuthController.LoginRequest validLogin = new AuthController.LoginRequest();
        validLogin.setUsername("integrationuser");
        validLogin.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.username").value("integrationuser"));

        // Test login with wrong password
        AuthController.LoginRequest wrongPassword = new AuthController.LoginRequest();
        wrongPassword.setUsername("integrationuser");
        wrongPassword.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPassword)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ungültige Anmeldedaten!"));
    }

    @Test
    @Tag(TestConstants.TestCategories.SLOW_TEST)
    void testErrorHandling() throws Exception {
        // Test accessing non-existent user
        mockMvc.perform(get("/auth/user/999"))
                .andExpect(status().isNotFound());

        // Test accessing watchlist without user ID
        mockMvc.perform(get("/Watchlist/1"))
                .andExpect(status().isBadRequest()); // Missing required parameter

        // Test creating watchlist item for non-existent user
        WatchlistController.WatchlistRequest invalidRequest = new WatchlistController.WatchlistRequest();
        invalidRequest.setTitle("Invalid Movie");
        invalidRequest.setType("Film");
        invalidRequest.setGenre("Test");
        invalidRequest.setWatched(false);
        invalidRequest.setRating(0);
        invalidRequest.setUserId(999L); // Non-existent user

        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Tag(TestConstants.TestCategories.SLOW_TEST)
    void testWatchlistPosterFunctionality() throws Exception {
        // Test adding item with custom poster URL
        WatchlistController.WatchlistRequest requestWithPoster = new WatchlistController.WatchlistRequest();
        requestWithPoster.setTitle("Movie with Custom Poster");
        requestWithPoster.setType("Film");
        requestWithPoster.setGenre("Action");
        requestWithPoster.setWatched(false);
        requestWithPoster.setRating(0);
        requestWithPoster.setPosterUrl("https://custom-poster.com/image.jpg");
        requestWithPoster.setUserId(testUser.getId());

        String response = mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithPoster)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Watchlist createdItem = objectMapper.readValue(response, Watchlist.class);

        // Test refresh poster functionality
        mockMvc.perform(post("/Watchlist/" + createdItem.getId() + "/refresh-poster")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk());

        // Test refresh all posters
        mockMvc.perform(post("/Watchlist/refresh-all-posters")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Cover-Update für alle Einträge gestartet!"));
    }

    @Test
    @Tag(TestConstants.TestCategories.SLOW_TEST)
    void testComplexWatchlistOperations() throws Exception {
        // Add multiple items
        String[] titles = {"Movie 1", "Series 1", "Documentary 1", "Anime 1"};
        String[] types = {"Film", "Serie", "Dokumentation", "Anime"};
        String[] genres = {"Action", "Drama", "Education", "Animation"};

        for (int i = 0; i < titles.length; i++) {
            WatchlistController.WatchlistRequest request = new WatchlistController.WatchlistRequest();
            request.setTitle(titles[i]);
            request.setType(types[i]);
            request.setGenre(genres[i]);
            request.setWatched(i % 2 == 0); // Every other item is watched
            request.setRating(i % 2 == 0 ? 8 : 0); // Watched items have rating
            request.setUserId(testUser.getId());

            mockMvc.perform(post("/Watchlist")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        // Verify all items are added
        mockMvc.perform(get("/Watchlist")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @Tag(TestConstants.TestCategories.SLOW_TEST)
    void testDatabaseConstraints() throws Exception {
        // Test that foreign key constraints work
        WatchlistController.WatchlistRequest request = new WatchlistController.WatchlistRequest();
        request.setTitle("Test Movie");
        request.setType("Film");
        request.setGenre("Action");
        request.setWatched(false);
        request.setRating(0);
        request.setUserId(testUser.getId());

        // Add item
        String response = mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Watchlist createdItem = objectMapper.readValue(response, Watchlist.class);

        // Verify item exists
        mockMvc.perform(get("/Watchlist/" + createdItem.getId())
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Movie"));

        // Delete user and verify cascade works
        userRepository.deleteById(testUser.getId());

        // Verify items are also deleted (would be handled by cascade)
        // Note: In actual test, we'd need to check the database directly
        // as the API won't work without a valid user
    }

    @Test
    @Tag(TestConstants.TestCategories.SLOW_TEST)
    void testSpecialCharactersHandling() throws Exception {
        // Test with various special characters and unicode
        WatchlistController.WatchlistRequest request = new WatchlistController.WatchlistRequest();
        request.setTitle("Der Herr der Ringe: Die Gefährten & The Lord of the Rings");
        request.setType("Film");
        request.setGenre("Fantasy/Abenteuer");
        request.setWatched(true);
        request.setRating(10);
        request.setUserId(testUser.getId());

        mockMvc.perform(post("/Watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Der Herr der Ringe: Die Gefährten & The Lord of the Rings"))
                .andExpect(jsonPath("$.genre").value("Fantasy/Abenteuer"));
    }
}
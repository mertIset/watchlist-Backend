package de.htwberlin.webtech.webtech;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Basis-Testklasse mit gemeinsamen Utilities und Setup-Methoden
 */
@ActiveProfiles("test")
public abstract class BaseTest {

    @Autowired(required = false)
    protected ObjectMapper objectMapper;

    protected User testUser;
    protected User secondUser;
    protected Watchlist testWatchlist;

    @BeforeEach
    void baseSetUp() {
        setupTestUsers();
        setupTestWatchlistItems();
    }

    /**
     * Erstellt Standard-Test-Benutzer
     */
    protected void setupTestUsers() {
        testUser = TestDataBuilder.aUser()
                .withId(1L)
                .withUsername(TestConstants.DEFAULT_USERNAME)
                .withEmail(TestConstants.DEFAULT_EMAIL)
                .withPassword(TestConstants.DEFAULT_PASSWORD)
                .withFirstName(TestConstants.DEFAULT_FIRST_NAME)
                .withLastName(TestConstants.DEFAULT_LAST_NAME)
                .build();

        secondUser = TestDataBuilder.aUser()
                .withId(2L)
                .withUsername(TestConstants.SECOND_USER_USERNAME)
                .withEmail(TestConstants.SECOND_USER_EMAIL)
                .withPassword(TestConstants.DEFAULT_PASSWORD)
                .withFirstName("Second")
                .withLastName("User")
                .build();
    }

    /**
     * Erstellt Standard-Test-Watchlist-Items
     */
    protected void setupTestWatchlistItems() {
        testWatchlist = TestDataBuilder.aWatchlistItem()
                .withId(1L)
                .withTitle(TestConstants.DEFAULT_MOVIE_TITLE)
                .withType(TestConstants.MOVIE_TYPE)
                .withGenre(TestConstants.ACTION_GENRE)
                .withWatched(false)
                .withRating(TestConstants.DEFAULT_RATING)
                .withPosterUrl(TestConstants.DEFAULT_POSTER_URL)
                .withUser(testUser)
                .build();
    }

    /**
     * Utility-Methoden für JSON-Serialisierung
     */
    protected String toJson(Object object) throws Exception {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper.writeValueAsString(object);
    }

    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Erstellt einen neuen User für Tests
     */
    protected User createTestUser(String username, String email) {
        return TestDataBuilder.aUser()
                .withUsername(username)
                .withEmail(email)
                .build();
    }

    /**
     * Erstellt mehrere Test-Users
     */
    protected List<User> createMultipleTestUsers(int count) {
        return java.util.stream.IntStream.range(1, count + 1)
                .mapToObj(i -> TestDataBuilder.aUser()
                        .withUsername("user" + i)
                        .withEmail("user" + i + "@example.com")
                        .withFirstName("User")
                        .withLastName(String.valueOf(i))
                        .build())
                .toList();
    }

    /**
     * Erstellt ein Watchlist-Item für einen bestimmten User
     */
    protected Watchlist createTestWatchlistItem(User user, String title, String type) {
        return TestDataBuilder.aWatchlistItem()
                .withTitle(title)
                .withType(type)
                .withUser(user)
                .build();
    }

    /**
     * Erstellt mehrere Watchlist-Items für einen User
     */
    protected List<Watchlist> createMultipleWatchlistItems(User user, int count) {
        return TestDataBuilder.createMultipleWatchlistItems(user, count);
    }

    /**
     * Utility-Methoden für Zeitvergleiche
     */
    protected boolean isRecentTime(LocalDateTime time) {
        return time.isAfter(LocalDateTime.now().minusMinutes(1)) &&
                time.isBefore(LocalDateTime.now().plusMinutes(1));
    }

    protected LocalDateTime getPastTime(int daysAgo) {
        return LocalDateTime.now().minusDays(daysAgo);
    }

    protected LocalDateTime getFutureTime(int daysFromNow) {
        return LocalDateTime.now().plusDays(daysFromNow);
    }

    /**
     * Erstellt Auth-Request-DTOs
     */
    protected AuthController.RegisterRequest createRegisterRequest() {
        return TestDataBuilder.aRegisterRequest().build();
    }

    protected AuthController.RegisterRequest createRegisterRequest(String username, String email) {
        return TestDataBuilder.aRegisterRequest()
                .withUsername(username)
                .withEmail(email)
                .build();
    }

    protected AuthController.LoginRequest createLoginRequest() {
        return TestDataBuilder.aLoginRequest().build();
    }

    protected AuthController.LoginRequest createLoginRequest(String username, String password) {
        return TestDataBuilder.aLoginRequest()
                .withUsername(username)
                .withPassword(password)
                .build();
    }

    /**
     * Erstellt Watchlist-Request-DTOs
     */
    protected WatchlistController.WatchlistRequest createWatchlistRequest() {
        return TestDataBuilder.aWatchlistRequest()
                .withUserId(testUser.getId())
                .build();
    }

    protected WatchlistController.WatchlistRequest createWatchlistRequest(String title, String type, Long userId) {
        return TestDataBuilder.aWatchlistRequest()
                .withTitle(title)
                .withType(type)
                .withUserId(userId)
                .build();
    }

    /**
     * Erstellt OMDb-Response-Mocks
     */
    protected OMDbService.OMDbResponse createSuccessfulOMDbResponse(String title, String posterUrl) {
        OMDbService.OMDbResponse response = TestDataBuilder.createSuccessfulOMDbResponse();
        response.setTitle(title);
        response.setPoster(posterUrl);
        return response;
    }

    protected OMDbService.OMDbResponse createFailedOMDbResponse(String error) {
        OMDbService.OMDbResponse response = TestDataBuilder.createFailedOMDbResponse();
        response.setError(error);
        return response;
    }

    /**
     * Validation-Utilities
     */
    protected void assertUserEquals(User expected, User actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected.getUsername(), actual.getUsername());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getEmail(), actual.getEmail());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getFirstName(), actual.getFirstName());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getLastName(), actual.getLastName());
    }

    protected void assertWatchlistEquals(Watchlist expected, Watchlist actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected.getTitle(), actual.getTitle());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getType(), actual.getType());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getGenre(), actual.getGenre());
        org.junit.jupiter.api.Assertions.assertEquals(expected.isWatched(), actual.isWatched());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getRating(), actual.getRating());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getPosterUrl(), actual.getPosterUrl());
    }

    /**
     * String-Utilities für Tests
     */
    protected String createLongString(int length) {
        return "a".repeat(length);
    }

    protected String createRandomUsername() {
        return "user_" + System.currentTimeMillis();
    }

    protected String createRandomEmail() {
        return "test_" + System.currentTimeMillis() + "@example.com";
    }

    /**
     * Mock-Response-Utilities
     */
    protected String createJsonResponse(boolean success, String message) {
        return String.format("{\"success\":%b,\"message\":\"%s\"}", success, message);
    }

    protected String createUserJsonResponse(User user) throws Exception {
        return toJson(new AuthController.AuthResponse(true, "Success",
                new AuthController.UserDTO(user)));
    }

    /**
     * Test-Kategorien für @Tag Annotations
     */
    protected static class Tags {
        public static final String UNIT = TestConstants.TestCategories.UNIT_TEST;
        public static final String INTEGRATION = TestConstants.TestCategories.INTEGRATION_TEST;
        public static final String CONTROLLER = TestConstants.TestCategories.CONTROLLER_TEST;
        public static final String SERVICE = TestConstants.TestCategories.SERVICE_TEST;
        public static final String REPOSITORY = TestConstants.TestCategories.REPOSITORY_TEST;
        public static final String ENTITY = TestConstants.TestCategories.ENTITY_TEST;
        public static final String PERFORMANCE = TestConstants.TestCategories.PERFORMANCE_TEST;
        public static final String SLOW = TestConstants.TestCategories.SLOW_TEST;
        public static final String FAST = TestConstants.TestCategories.FAST_TEST;
    }

    /**
     * Database Cleanup Utilities (für Integrationstests)
     */
    protected static class DatabaseCleanup {
        public static final String DELETE_ALL_WATCHLIST = TestConstants.Cleanup.DELETE_ALL_WATCHLIST;
        public static final String DELETE_ALL_USERS = TestConstants.Cleanup.DELETE_ALL_USERS;
        public static final String RESET_USER_SEQUENCE = TestConstants.Cleanup.RESET_USER_SEQUENCE;
        public static final String RESET_WATCHLIST_SEQUENCE = TestConstants.Cleanup.RESET_WATCHLIST_SEQUENCE;
    }

    /**
     * Performance-Test-Utilities
     */
    protected void measureExecutionTime(Runnable task, String taskName) {
        long startTime = System.currentTimeMillis();
        task.run();
        long endTime = System.currentTimeMillis();
        System.out.println(String.format("Task '%s' took %d ms", taskName, endTime - startTime));
    }

    protected void repeatTask(Runnable task, int times) {
        for (int i = 0; i < times; i++) {
            task.run();
        }
    }

    /**
     * Logging-Utilities für Tests
     */
    protected void logTestStart(String testName) {
        System.out.println("=== Starting test: " + testName + " ===");
    }

    protected void logTestEnd(String testName) {
        System.out.println("=== Finished test: " + testName + " ===");
    }

    protected void logDebug(String message) {
        System.out.println("DEBUG: " + message);
    }
}
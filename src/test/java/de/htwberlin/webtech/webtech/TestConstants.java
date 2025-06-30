package de.htwberlin.webtech.webtech;

/**
 * Zentrale Konstanten für alle Tests
 */
public final class TestConstants {

    // User Test Data
    public static final String DEFAULT_USERNAME = "testuser";
    public static final String DEFAULT_EMAIL = "test@example.com";
    public static final String DEFAULT_PASSWORD = "password123";
    public static final String DEFAULT_FIRST_NAME = "Test";
    public static final String DEFAULT_LAST_NAME = "User";

    public static final String SECOND_USER_USERNAME = "seconduser";
    public static final String SECOND_USER_EMAIL = "second@example.com";

    // Watchlist Test Data
    public static final String DEFAULT_MOVIE_TITLE = "Test Movie";
    public static final String MOVIE_TYPE = "Film";
    public static final String SERIES_TYPE = "Serie";
    public static final String DOCUMENTARY_TYPE = "Dokumentation";
    public static final String ANIME_TYPE = "Anime";

    public static final String ACTION_GENRE = "Action";
    public static final String DRAMA_GENRE = "Drama";
    public static final String COMEDY_GENRE = "Comedy";

    public static final int DEFAULT_RATING = 7;
    public static final String DEFAULT_POSTER_URL = "https://example.com/poster.jpg";

    // API Test Data
    public static final String OMDB_SUCCESS_RESPONSE = "True";
    public static final String OMDB_FAILURE_RESPONSE = "False";
    public static final String OMDB_NOT_FOUND_ERROR = "Movie not found!";

    // Test Categories for @Tag
    public static final class TestCategories {
        public static final String UNIT_TEST = "unit";
        public static final String INTEGRATION_TEST = "integration";
        public static final String CONTROLLER_TEST = "controller";
        public static final String SERVICE_TEST = "service";
        public static final String REPOSITORY_TEST = "repository";
        public static final String ENTITY_TEST = "entity";
        public static final String PERFORMANCE_TEST = "performance";
        public static final String SLOW_TEST = "slow";
        public static final String FAST_TEST = "fast";
    }

    // Database Cleanup
    public static final class Cleanup {
        public static final String DELETE_ALL_WATCHLIST = "DELETE FROM watchlist";
        public static final String DELETE_ALL_USERS = "DELETE FROM app_user";
        public static final String RESET_USER_SEQUENCE = "ALTER SEQUENCE app_user_id_seq RESTART WITH 1";
        public static final String RESET_WATCHLIST_SEQUENCE = "ALTER SEQUENCE watchlist_id_seq RESTART WITH 1";
    }

    // HTTP Test Data
    public static final class Http {
        public static final String CORS_ORIGIN_LOCALHOST = "http://localhost:5173";
        public static final String CORS_ORIGIN_PRODUCTION = "https://watchlist-frontend-bzxi.onrender.com";
        public static final String CONTENT_TYPE_JSON = "application/json";
    }

    // Validation Test Data
    public static final class Validation {
        public static final String EMPTY_STRING = "";
        public static final String WHITESPACE_STRING = "   ";
        public static final String VERY_LONG_STRING = "a".repeat(1000);
        public static final String SPECIAL_CHARS_STRING = "!@#$%^&*()_+-={}[]|;':\",./<>?";
        public static final String UNICODE_STRING = "Ümlaut 日本語 🎬";
    }

    // Private constructor to prevent instantiation
    private TestConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
package de.htwberlin.webtech.webtech;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Builder-Pattern für Test-Daten
 */
public final class TestDataBuilder {

    // ======= USER BUILDER =======
    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String username = TestConstants.DEFAULT_USERNAME;
        private String email = TestConstants.DEFAULT_EMAIL;
        private String password = TestConstants.DEFAULT_PASSWORD;
        private String firstName = TestConstants.DEFAULT_FIRST_NAME;
        private String lastName = TestConstants.DEFAULT_LAST_NAME;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime lastLogin;

        public UserBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserBuilder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserBuilder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserBuilder withLastLogin(LocalDateTime lastLogin) {
            this.lastLogin = lastLogin;
            return this;
        }

        public User build() {
            User user = new User();
            user.setId(id);
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setCreatedAt(createdAt);
            user.setLastLogin(lastLogin);
            return user;
        }
    }

    // ======= WATCHLIST BUILDER =======
    public static WatchlistBuilder aWatchlistItem() {
        return new WatchlistBuilder();
    }

    public static class WatchlistBuilder {
        private Long id;
        private String title = TestConstants.DEFAULT_MOVIE_TITLE;
        private String type = TestConstants.MOVIE_TYPE;
        private String genre = TestConstants.ACTION_GENRE;
        private boolean watched = false;
        private int rating = 0;
        private String posterUrl = TestConstants.DEFAULT_POSTER_URL;
        private User user;

        public WatchlistBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public WatchlistBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public WatchlistBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public WatchlistBuilder withGenre(String genre) {
            this.genre = genre;
            return this;
        }

        public WatchlistBuilder withWatched(boolean watched) {
            this.watched = watched;
            return this;
        }

        public WatchlistBuilder withRating(int rating) {
            this.rating = rating;
            return this;
        }

        public WatchlistBuilder withPosterUrl(String posterUrl) {
            this.posterUrl = posterUrl;
            return this;
        }

        public WatchlistBuilder withUser(User user) {
            this.user = user;
            return this;
        }

        public Watchlist build() {
            Watchlist watchlist = new Watchlist();
            watchlist.setId(id);
            watchlist.setTitle(title);
            watchlist.setType(type);
            watchlist.setGenre(genre);
            watchlist.setWatched(watched);
            watchlist.setRating(rating);
            watchlist.setPosterUrl(posterUrl);
            watchlist.setUser(user);
            return watchlist;
        }
    }

    // ======= AUTH REQUEST BUILDERS =======
    public static RegisterRequestBuilder aRegisterRequest() {
        return new RegisterRequestBuilder();
    }

    public static class RegisterRequestBuilder {
        private String username = TestConstants.DEFAULT_USERNAME;
        private String email = TestConstants.DEFAULT_EMAIL;
        private String password = TestConstants.DEFAULT_PASSWORD;
        private String firstName = TestConstants.DEFAULT_FIRST_NAME;
        private String lastName = TestConstants.DEFAULT_LAST_NAME;

        public RegisterRequestBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public RegisterRequestBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public RegisterRequestBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public RegisterRequestBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public RegisterRequestBuilder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public AuthController.RegisterRequest build() {
            AuthController.RegisterRequest request = new AuthController.RegisterRequest();
            request.setUsername(username);
            request.setEmail(email);
            request.setPassword(password);
            request.setFirstName(firstName);
            request.setLastName(lastName);
            return request;
        }
    }

    public static LoginRequestBuilder aLoginRequest() {
        return new LoginRequestBuilder();
    }

    public static class LoginRequestBuilder {
        private String username = TestConstants.DEFAULT_USERNAME;
        private String password = TestConstants.DEFAULT_PASSWORD;

        public LoginRequestBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public LoginRequestBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public AuthController.LoginRequest build() {
            AuthController.LoginRequest request = new AuthController.LoginRequest();
            request.setUsername(username);
            request.setPassword(password);
            return request;
        }
    }

    // ======= WATCHLIST REQUEST BUILDER =======
    public static WatchlistRequestBuilder aWatchlistRequest() {
        return new WatchlistRequestBuilder();
    }

    public static class WatchlistRequestBuilder {
        private String title = TestConstants.DEFAULT_MOVIE_TITLE;
        private String type = TestConstants.MOVIE_TYPE;
        private String genre = TestConstants.ACTION_GENRE;
        private boolean watched = false;
        private int rating = 0;
        private String posterUrl;
        private Long userId;

        public WatchlistRequestBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public WatchlistRequestBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public WatchlistRequestBuilder withGenre(String genre) {
            this.genre = genre;
            return this;
        }

        public WatchlistRequestBuilder withWatched(boolean watched) {
            this.watched = watched;
            return this;
        }

        public WatchlistRequestBuilder withRating(int rating) {
            this.rating = rating;
            return this;
        }

        public WatchlistRequestBuilder withPosterUrl(String posterUrl) {
            this.posterUrl = posterUrl;
            return this;
        }

        public WatchlistRequestBuilder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public WatchlistController.WatchlistRequest build() {
            WatchlistController.WatchlistRequest request = new WatchlistController.WatchlistRequest();
            request.setTitle(title);
            request.setType(type);
            request.setGenre(genre);
            request.setWatched(watched);
            request.setRating(rating);
            request.setPosterUrl(posterUrl);
            request.setUserId(userId);
            return request;
        }
    }

    // ======= OMDB RESPONSE BUILDERS =======
    public static OMDbService.OMDbResponse createSuccessfulOMDbResponse() {
        OMDbService.OMDbResponse response = new OMDbService.OMDbResponse();
        response.setResponse(TestConstants.OMDB_SUCCESS_RESPONSE);
        response.setTitle(TestConstants.DEFAULT_MOVIE_TITLE);
        response.setType("movie");
        response.setYear("2020");
        response.setPoster(TestConstants.DEFAULT_POSTER_URL);
        return response;
    }

    public static OMDbService.OMDbResponse createFailedOMDbResponse() {
        OMDbService.OMDbResponse response = new OMDbService.OMDbResponse();
        response.setResponse(TestConstants.OMDB_FAILURE_RESPONSE);
        response.setError(TestConstants.OMDB_NOT_FOUND_ERROR);
        return response;
    }

    // ======= UTILITY METHODS =======
    public static List<Watchlist> createMultipleWatchlistItems(User user, int count) {
        return IntStream.range(1, count + 1)
                .mapToObj(i -> aWatchlistItem()
                        .withTitle("Movie " + i)
                        .withType(i % 2 == 0 ? TestConstants.SERIES_TYPE : TestConstants.MOVIE_TYPE)
                        .withGenre(i % 3 == 0 ? TestConstants.DRAMA_GENRE : TestConstants.ACTION_GENRE)
                        .withWatched(i % 2 == 0)
                        .withRating(i % 2 == 0 ? i + 5 : 0)
                        .withUser(user)
                        .build())
                .toList();
    }

    public static List<User> createMultipleUsers(int count) {
        return IntStream.range(1, count + 1)
                .mapToObj(i -> aUser()
                        .withUsername("user" + i)
                        .withEmail("user" + i + "@example.com")
                        .withFirstName("User")
                        .withLastName(String.valueOf(i))
                        .build())
                .toList();
    }

    // Private constructor to prevent instantiation
    private TestDataBuilder() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
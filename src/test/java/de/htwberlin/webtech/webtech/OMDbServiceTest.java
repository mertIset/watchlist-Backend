package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OMDbServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OMDbService omdbService;

    private static final String TEST_API_KEY = "test-api-key";
    private static final String OMDB_BASE_URL = "http://www.omdbapi.com/";

    @BeforeEach
    void setUp() {
        // Setze API Key über Reflection da @Value in Tests nicht funktioniert
        ReflectionTestUtils.setField(omdbService, "apiKey", TEST_API_KEY);
        // Setze RestTemplate über Reflection
        ReflectionTestUtils.setField(omdbService, "restTemplate", restTemplate);
    }

    @Test
    void testFetchPosterUrl_Success() {
        // Given
        String title = "Inception";
        String type = "Film";
        String expectedPosterUrl = "https://example.com/inception-poster.jpg";

        OMDbService.OMDbResponse mockResponse = new OMDbService.OMDbResponse();
        mockResponse.setResponse("True");
        mockResponse.setPoster(expectedPosterUrl);
        mockResponse.setTitle("Inception");
        mockResponse.setType("movie");

        String expectedUrl = String.format("%s?t=%s&type=%s&apikey=%s",
                OMDB_BASE_URL, "Inception", "movie", TEST_API_KEY);

        when(restTemplate.getForObject(expectedUrl, OMDbService.OMDbResponse.class))
                .thenReturn(mockResponse);

        // When
        String result = omdbService.fetchPosterUrl(title, type);

        // Then
        assertEquals(expectedPosterUrl, result);
        verify(restTemplate).getForObject(expectedUrl, OMDbService.OMDbResponse.class);
    }

    @Test
    void testFetchPosterUrl_MovieNotFound() {
        // Given
        String title = "NonExistentMovie";
        String type = "Film";

        OMDbService.OMDbResponse mockResponse = new OMDbService.OMDbResponse();
        mockResponse.setResponse("False");
        mockResponse.setError("Movie not found!");

        String expectedUrl = String.format("%s?t=%s&type=%s&apikey=%s",
                OMDB_BASE_URL, "NonExistentMovie", "movie", TEST_API_KEY);

        when(restTemplate.getForObject(expectedUrl, OMDbService.OMDbResponse.class))
                .thenReturn(mockResponse);

        // When
        String result = omdbService.fetchPosterUrl(title, type);

        // Then
        assertNull(result);
        verify(restTemplate).getForObject(expectedUrl, OMDbService.OMDbResponse.class);
    }

    @Test
    void testFetchPosterUrl_NoPosterAvailable() {
        // Given
        String title = "MovieWithoutPoster";
        String type = "Film";

        OMDbService.OMDbResponse mockResponse = new OMDbService.OMDbResponse();
        mockResponse.setResponse("True");
        mockResponse.setPoster("N/A"); // Kein Poster verfügbar
        mockResponse.setTitle("MovieWithoutPoster");

        String expectedUrl = String.format("%s?t=%s&type=%s&apikey=%s",
                OMDB_BASE_URL, "MovieWithoutPoster", "movie", TEST_API_KEY);

        when(restTemplate.getForObject(expectedUrl, OMDbService.OMDbResponse.class))
                .thenReturn(mockResponse);

        // When
        String result = omdbService.fetchPosterUrl(title, type);

        // Then
        assertNull(result);
        verify(restTemplate).getForObject(expectedUrl, OMDbService.OMDbResponse.class);
    }

    @Test
    void testFetchPosterUrl_EmptyPoster() {
        // Given
        String title = "MovieWithEmptyPoster";
        String type = "Film";

        OMDbService.OMDbResponse mockResponse = new OMDbService.OMDbResponse();
        mockResponse.setResponse("True");
        mockResponse.setPoster(""); // Leerer Poster String
        mockResponse.setTitle("MovieWithEmptyPoster");

        String expectedUrl = String.format("%s?t=%s&type=%s&apikey=%s",
                OMDB_BASE_URL, "MovieWithEmptyPoster", "movie", TEST_API_KEY);

        when(restTemplate.getForObject(expectedUrl, OMDbService.OMDbResponse.class))
                .thenReturn(mockResponse);

        // When
        String result = omdbService.fetchPosterUrl(title, type);

        // Then
        assertNull(result);
        verify(restTemplate).getForObject(expectedUrl, OMDbService.OMDbResponse.class);
    }

    @Test
    void testFetchPosterUrl_RestClientException() {
        // Given
        String title = "TestMovie";
        String type = "Film";

        String expectedUrl = String.format("%s?t=%s&type=%s&apikey=%s",
                OMDB_BASE_URL, "TestMovie", "movie", TEST_API_KEY);

        when(restTemplate.getForObject(expectedUrl, OMDbService.OMDbResponse.class))
                .thenThrow(new RestClientException("Network error"));

        // When
        String result = omdbService.fetchPosterUrl(title, type);

        // Then
        assertNull(result);
        verify(restTemplate).getForObject(expectedUrl, OMDbService.OMDbResponse.class);
    }

    @Test
    void testFetchPosterUrl_NullResponse() {
        // Given
        String title = "TestMovie";
        String type = "Film";

        String expectedUrl = String.format("%s?t=%s&type=%s&apikey=%s",
                OMDB_BASE_URL, "TestMovie", "movie", TEST_API_KEY);

        when(restTemplate.getForObject(expectedUrl, OMDbService.OMDbResponse.class))
                .thenReturn(null);

        // When
        String result = omdbService.fetchPosterUrl(title, type);

        // Then
        assertNull(result);
        verify(restTemplate).getForObject(expectedUrl, OMDbService.OMDbResponse.class);
    }

    @Test
    void testFetchPosterUrl_SerieType() {
        // Given
        String title = "Breaking Bad";
        String type = "Serie";
        String expectedPosterUrl = "https://example.com/breaking-bad-poster.jpg";

        OMDbService.OMDbResponse mockResponse = new OMDbService.OMDbResponse();
        mockResponse.setResponse("True");
        mockResponse.setPoster(expectedPosterUrl);
        mockResponse.setTitle("Breaking Bad");
        mockResponse.setType("series");

        String expectedUrl = String.format("%s?t=%s&type=%s&apikey=%s",
                OMDB_BASE_URL, "Breaking Bad", "series", TEST_API_KEY);

        when(restTemplate.getForObject(expectedUrl, OMDbService.OMDbResponse.class))
                .thenReturn(mockResponse);

        // When
        String result = omdbService.fetchPosterUrl(title, type);

        // Then
        assertEquals(expectedPosterUrl, result);
        verify(restTemplate).getForObject(expectedUrl, OMDbService.OMDbResponse.class);
    }

    @Test
    void testFetchPosterUrlWithYear_Success() {
        // Given
        String title = "Batman";
        String type = "Film";
        Integer year = 1989;
        String expectedPosterUrl = "https://example.com/batman-1989-poster.jpg";

        OMDbService.OMDbResponse mockResponse = new OMDbService.OMDbResponse();
        mockResponse.setResponse("True");
        mockResponse.setPoster(expectedPosterUrl);
        mockResponse.setTitle("Batman");
        mockResponse.setYear("1989");

        String expectedUrl = String.format("%s?t=%s&y=%d&type=%s&apikey=%s",
                OMDB_BASE_URL, "Batman", year, "movie", TEST_API_KEY);

        when(restTemplate.getForObject(expectedUrl, OMDbService.OMDbResponse.class))
                .thenReturn(mockResponse);

        // When
        String result = omdbService.fetchPosterUrlWithYear(title, type, year);

        // Then
        assertEquals(expectedPosterUrl, result);
        verify(restTemplate).getForObject(expectedUrl, OMDbService.OMDbResponse.class);
    }

    @Test
    void testFetchPosterUrlWithYear_InvalidYear_FallbackToNormalSearch() {
        // Given
        String title = "TestMovie";
        String type = "Film";
        Integer year = 1800; // Ungültiges Jahr
        String expectedPosterUrl = "https://example.com/test-movie-poster.jpg";

        OMDbService.OMDbResponse mockResponse = new OMDbService.OMDbResponse();
        mockResponse.setResponse("True");
        mockResponse.setPoster(expectedPosterUrl);
        mockResponse.setTitle("TestMovie");

        // Der fallback zur normalen Suche wird aufgerufen
        String fallbackUrl = String.format("%s?t=%s&type=%s&apikey=%s",
                OMDB_BASE_URL, "TestMovie", "movie", TEST_API_KEY);

        when(restTemplate.getForObject(fallbackUrl, OMDbService.OMDbResponse.class))
                .thenReturn(mockResponse);

        // When
        String result = omdbService.fetchPosterUrlWithYear(title, type, year);

        // Then
        assertEquals(expectedPosterUrl, result);
        verify(restTemplate).getForObject(fallbackUrl, OMDbService.OMDbResponse.class);
    }

    @Test
    void testFetchPosterUrlWithYear_NullYear_FallbackToNormalSearch() {
        // Given
        String title = "TestMovie";
        String type = "Film";
        Integer year = null;
        String expectedPosterUrl = "https://example.com/test-movie-poster.jpg";

        OMDbService.OMDbResponse mockResponse = new OMDbService.OMDbResponse();
        mockResponse.setResponse("True");
        mockResponse.setPoster(expectedPosterUrl);
        mockResponse.setTitle("TestMovie");

        String fallbackUrl = String.format("%s?t=%s&type=%s&apikey=%s",
                OMDB_BASE_URL, "TestMovie", "movie", TEST_API_KEY);

        when(restTemplate.getForObject(fallbackUrl, OMDbService.OMDbResponse.class))
                .thenReturn(mockResponse);

        // When
        String result = omdbService.fetchPosterUrlWithYear(title, type, year);

        // Then
        assertEquals(expectedPosterUrl, result);
        verify(restTemplate).getForObject(fallbackUrl, OMDbService.OMDbResponse.class);
    }

    @Test
    void testTitleCleaning() {
        // Given
        String dirtyTitle = "Test Movie!@#$%^&*()";
        String type = "Film";
        String expectedPosterUrl = "https://example.com/poster.jpg";

        OMDbService.OMDbResponse mockResponse = new OMDbService.OMDbResponse();
        mockResponse.setResponse("True");
        mockResponse.setPoster(expectedPosterUrl);

        // Der bereinigte Titel sollte "Test Movie" sein
        String expectedUrl = String.format("%s?t=%s&type=%s&apikey=%s",
                OMDB_BASE_URL, "Test Movie", "movie", TEST_API_KEY);

        when(restTemplate.getForObject(expectedUrl, OMDbService.OMDbResponse.class))
                .thenReturn(mockResponse);

        // When
        String result = omdbService.fetchPosterUrl(dirtyTitle, type);

        // Then
        assertEquals(expectedPosterUrl, result);
        verify(restTemplate).getForObject(expectedUrl, OMDbService.OMDbResponse.class);
    }
}
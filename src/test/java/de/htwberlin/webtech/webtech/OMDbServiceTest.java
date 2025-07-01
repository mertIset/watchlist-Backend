package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class OMDbServiceTest {

    private RestTemplate restTemplate;
    private OMDbService omdbService;

    @BeforeEach
    void setUp() {
        omdbService = new OMDbService();
        restTemplate = mock(RestTemplate.class);

        // ReflectionTestUtils für private Felder
        ReflectionTestUtils.setField(omdbService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(omdbService, "apiKey", "test-api-key");
    }

    @Test
    void testFetchPosterUrl_Success() {
        // Arrange
        String title = "Inception";
        String type = "Film";
        String expectedPosterUrl = "http://example.com/inception-poster.jpg";

        OMDbService.OMDbResponse mockResponse = new OMDbService.OMDbResponse();
        mockResponse.setResponse("True");
        mockResponse.setPoster(expectedPosterUrl);
        mockResponse.setTitle("Inception");

        when(restTemplate.getForObject(anyString(), eq(OMDbService.OMDbResponse.class)))
                .thenReturn(mockResponse);

        // Act
        String result = omdbService.fetchPosterUrl(title, type);

        // Assert
        assertEquals(expectedPosterUrl, result);
        verify(restTemplate).getForObject(anyString(), eq(OMDbService.OMDbResponse.class));
    }

    @Test
    void testFetchPosterUrl_NoPosterAvailable() {
        // Arrange
        String title = "Unknown Movie";
        String type = "Film";

        OMDbService.OMDbResponse mockResponse = new OMDbService.OMDbResponse();
        mockResponse.setResponse("True");
        mockResponse.setPoster("N/A");
        mockResponse.setTitle("Unknown Movie");

        when(restTemplate.getForObject(anyString(), eq(OMDbService.OMDbResponse.class)))
                .thenReturn(mockResponse);

        // Act
        String result = omdbService.fetchPosterUrl(title, type);

        // Assert
        assertNull(result);
        verify(restTemplate).getForObject(anyString(), eq(OMDbService.OMDbResponse.class));
    }

    @Test
    void testFetchPosterUrl_MovieNotFound() {
        // Arrange
        String title = "Nonexistent Movie";
        String type = "Film";

        OMDbService.OMDbResponse mockResponse = new OMDbService.OMDbResponse();
        mockResponse.setResponse("False");
        mockResponse.setError("Movie not found!");

        when(restTemplate.getForObject(anyString(), eq(OMDbService.OMDbResponse.class)))
                .thenReturn(mockResponse);

        // Act
        String result = omdbService.fetchPosterUrl(title, type);

        // Assert
        assertNull(result);
        verify(restTemplate).getForObject(anyString(), eq(OMDbService.OMDbResponse.class));
    }

    @Test
    void testFetchPosterUrl_ApiException() {
        // Arrange
        String title = "Inception";
        String type = "Film";

        when(restTemplate.getForObject(anyString(), eq(OMDbService.OMDbResponse.class)))
                .thenThrow(new RestClientException("API not available"));

        // Act
        String result = omdbService.fetchPosterUrl(title, type);

        // Assert
        assertNull(result);
        verify(restTemplate).getForObject(anyString(), eq(OMDbService.OMDbResponse.class));
    }
}
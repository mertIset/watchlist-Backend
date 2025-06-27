package de.htwberlin.webtech.webtech;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Service
public class OMDbService {

    // API Key aus application.properties oder Umgebungsvariable
    @Value("${omdb.api.key:YOUR_API_KEY_HERE}")
    private String apiKey;

    private static final String OMDB_BASE_URL = "http://www.omdbapi.com/";
    private final RestTemplate restTemplate;

    public OMDbService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Sucht nach einem Film/Serie und gibt die Poster-URL zurück
     */
    public String fetchPosterUrl(String title, String type) {
        try {
            // Bereinige den Titel für die API-Anfrage
            String cleanTitle = cleanTitle(title);

            // Bestimme den OMDb-Type basierend auf unserem Type
            String omdbType = mapToOMDbType(type);

            // Baue die API-URL
            String url = String.format("%s?t=%s&type=%s&apikey=%s",
                    OMDB_BASE_URL,
                    cleanTitle,
                    omdbType,
                    apiKey);

            System.out.println("🎬 OMDb API Request: " + url);

            // Mache die API-Anfrage
            OMDbResponse response = restTemplate.getForObject(url, OMDbResponse.class);

            if (response != null && "True".equals(response.getResponse())) {
                String posterUrl = response.getPoster();

                // Prüfe ob ein gültiges Poster vorhanden ist
                if (posterUrl != null && !posterUrl.equals("N/A") && !posterUrl.isEmpty()) {
                    System.out.println("✅ Poster gefunden: " + posterUrl);
                    return posterUrl;
                } else {
                    System.out.println("❌ Kein Poster verfügbar für: " + title);
                }
            } else {
                System.out.println("❌ Film/Serie nicht gefunden: " + title);
                if (response != null) {
                    System.out.println("OMDb Error: " + response.getError());
                }
            }

        } catch (RestClientException e) {
            System.err.println("❌ OMDb API Fehler für '" + title + "': " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unerwarteter Fehler beim Abrufen des Posters für '" + title + "': " + e.getMessage());
        }

        return null; // Kein Poster gefunden
    }

    /**
     * Alternative Suche mit Jahr, falls verfügbar
     */
    public String fetchPosterUrlWithYear(String title, String type, Integer year) {
        if (year != null && year > 1900) {
            try {
                String cleanTitle = cleanTitle(title);
                String omdbType = mapToOMDbType(type);

                String url = String.format("%s?t=%s&y=%d&type=%s&apikey=%s",
                        OMDB_BASE_URL,
                        cleanTitle,
                        year,
                        omdbType,
                        apiKey);

                OMDbResponse response = restTemplate.getForObject(url, OMDbResponse.class);

                if (response != null && "True".equals(response.getResponse())) {
                    String posterUrl = response.getPoster();
                    if (posterUrl != null && !posterUrl.equals("N/A") && !posterUrl.isEmpty()) {
                        return posterUrl;
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Fehler bei Jahr-spezifischer Suche: " + e.getMessage());
            }
        }

        // Fallback auf normale Suche ohne Jahr
        return fetchPosterUrl(title, type);
    }

    /**
     * Bereinigt den Titel für die API-Anfrage
     */
    private String cleanTitle(String title) {
        if (title == null) return "";

        return title
                .trim()
                .replaceAll("[^a-zA-Z0-9\\s]", "") // Entferne Sonderzeichen
                .replaceAll("\\s+", " ") // Mehrere Leerzeichen zu einem
                .trim();
    }

    /**
     * Mappt unsere Types zu OMDb Types
     */
    private String mapToOMDbType(String type) {
        if (type == null) return "";

        switch (type.toLowerCase()) {
            case "film":
            case "movie":
                return "movie";
            case "serie":
            case "series":
            case "tv":
                return "series";
            case "dokumentation":
            case "documentary":
                return "movie"; // OMDb hat keinen speziellen Documentary-Type
            case "anime":
                return "series"; // Anime meist als Serie
            default:
                return ""; // Leerer String = alle Types
        }
    }

    /**
     * DTO für OMDb API Response
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OMDbResponse {
        @JsonProperty("Title")
        private String title;

        @JsonProperty("Year")
        private String year;

        @JsonProperty("Type")
        private String type;

        @JsonProperty("Poster")
        private String poster;

        @JsonProperty("Response")
        private String response;

        @JsonProperty("Error")
        private String error;

        // Getters
        public String getTitle() { return title; }
        public String getYear() { return year; }
        public String getType() { return type; }
        public String getPoster() { return poster; }
        public String getResponse() { return response; }
        public String getError() { return error; }

        // Setters
        public void setTitle(String title) { this.title = title; }
        public void setYear(String year) { this.year = year; }
        public void setType(String type) { this.type = type; }
        public void setPoster(String poster) { this.poster = poster; }
        public void setResponse(String response) { this.response = response; }
        public void setError(String error) { this.error = error; }
    }
}
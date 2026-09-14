package project.server;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public final class GeoLocationService {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    private GeoLocationService() {
    }

    public static GeoPoint geocode(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        String query = address.trim();
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = NOMINATIM_URL + "?format=jsonv2&limit=1&q=" + encoded;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "TheKnife/1.0 (+https://example.com)")
                    .header("Accept-Language", "it-IT,it;q=0.9,en;q=0.8")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.err.println("[Geo] Geocoding failed: HTTP " + response.statusCode());
                return null;
            }

            String body = response.body();
            int latIndex = body.indexOf("\"lat\"");
            int lonIndex = body.indexOf("\"lon\"");
            if (latIndex < 0 || lonIndex < 0) {
                return null;
            }

            String latValue = extractJsonStringValue(body, latIndex);
            String lonValue = extractJsonStringValue(body, lonIndex);
            if (latValue == null || lonValue == null) {
                return null;
            }

            double lat = Double.parseDouble(latValue);
            double lon = Double.parseDouble(lonValue);
            if (Double.isNaN(lat) || Double.isNaN(lon)) {
                return null;
            }
            return new GeoPoint(lat, lon);
        } catch (IOException | InterruptedException | NumberFormatException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Geo] Errore geocoding per: " + query + " -> " + e.getMessage());
            return null;
        }
    }

    private static String extractJsonStringValue(String json, int startIndex) {
        int colonIndex = json.indexOf(':', startIndex);
        if (colonIndex < 0) {
            return null;
        }
        int valueStart = json.indexOf('"', colonIndex + 1);
        if (valueStart < 0) {
            return null;
        }
        int valueEnd = json.indexOf('"', valueStart + 1);
        if (valueEnd < 0) {
            return null;
        }
        return json.substring(valueStart + 1, valueEnd);
    }

    public static final class GeoPoint {
        private final double latitude;
        private final double longitude;

        public GeoPoint(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}

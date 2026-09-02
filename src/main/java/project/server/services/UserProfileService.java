package project.server.services;

import project.server.GeoLocationService;
import project.shared.validation.RequestValidator;

public final class UserProfileService {
    private UserProfileService() {
    }

    public static double[] normalizeCoordinates(String location, double latitude, double longitude) {
        double lat = latitude;
        double lon = longitude;

        if (!RequestValidator.isValidCoordinate(lat, lon)) {
            lat = 0.0;
            lon = 0.0;
        }

        if ((lat == 0.0 && lon == 0.0) && location != null && !location.isBlank()) {
            GeoLocationService.GeoPoint point = GeoLocationService.geocode(location);
            if (point != null) {
                lat = point.getLatitude();
                lon = point.getLongitude();
            }
        }

        return new double[] { lat, lon };
    }
}

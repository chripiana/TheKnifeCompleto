package project.shared.validation;

import java.util.regex.Pattern;

public final class RequestValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MAX_TEXT_LENGTH = 255;

    private RequestValidator() {
    }

    public static boolean isValidRequest(String request) {
        return request != null && !request.isBlank() && request.length() <= 4096
                && !request.contains("\n") && !request.contains("\r") && !request.contains("\0");
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 7 || password.length() > 128) {
            return false;
        }
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        return hasUpper && hasLower && hasDigit;
    }

    public static boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    public static String sanitizeText(String value) {
        if (value == null) {
            return "";
        }
        String sanitized = value.replace("\u0000", "").trim();
        if (sanitized.length() > MAX_TEXT_LENGTH) {
            return sanitized.substring(0, MAX_TEXT_LENGTH);
        }
        return sanitized;
    }
}

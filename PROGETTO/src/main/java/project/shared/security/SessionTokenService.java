package project.shared.security;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionTokenService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);
    private static final Map<String, SessionEntry> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    private SessionTokenService() {
    }

    public static String generateToken() {
        byte[] tokenBytes = new byte[32];
        RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public static void register(String token, int userId) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token non valido");
        }
        ACTIVE_SESSIONS.put(token, new SessionEntry(userId, Instant.now().plus(SESSION_TTL)));
    }

    public static boolean isValid(String token, int expectedUserId) {
        if (token == null || token.isBlank()) {
            return false;
        }
        SessionEntry entry = ACTIVE_SESSIONS.get(token);
        if (entry == null || entry.isExpired()) {
            invalidate(token);
            return false;
        }
        return entry.userId() == expectedUserId;
    }

    public static boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        SessionEntry entry = ACTIVE_SESSIONS.get(token);
        if (entry == null) {
            return false;
        }
        if (entry.isExpired()) {
            invalidate(token);
            return false;
        }
        return true;
    }

    public static void invalidate(String token) {
        if (token != null) {
            ACTIVE_SESSIONS.remove(token);
        }
    }

    public static void cleanupExpired() {
        Instant now = Instant.now();
        ACTIVE_SESSIONS.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private record SessionEntry(int userId, Instant expiresAt) {
        boolean isExpired() {
            return isExpired(Instant.now());
        }

        boolean isExpired(Instant now) {
            return now.isAfter(expiresAt) || now.equals(expiresAt);
        }
    }
}

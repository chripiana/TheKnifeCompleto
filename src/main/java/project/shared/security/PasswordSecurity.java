package project.shared.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordSecurity {
    private static final String PREFIX = "pbkdf2";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordSecurity() {
    }

    public static String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("La password non può essere vuota");
        }

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = deriveKey(rawPassword, salt, ITERATIONS, KEY_LENGTH_BITS);

        return PREFIX + "$" +
                Base64.getEncoder().encodeToString(salt) + "$" +
                Base64.getEncoder().encodeToString(hash) + "$" +
                ITERATIONS;
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || rawPassword.isBlank() || storedHash == null || storedHash.isBlank()) {
            return false;
        }

        if (!storedHash.startsWith(PREFIX + "$")) {
            return storedHash.equals(rawPassword);
        }

        try {
            String[] parts = storedHash.split("\\$", -1);
            if (parts.length != 4) {
                return false;
            }

            int iterations = Integer.parseInt(parts[3]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            byte[] actual = deriveKey(rawPassword, salt, iterations, expected.length * 8);

            return MessageDigest.isEqual(expected, actual);
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isLegacyPlaintext(String storedHash) {
        return storedHash != null && !storedHash.startsWith(PREFIX + "$");
    }

    private static byte[] deriveKey(String password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLengthBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("Impossibile derivare la chiave di hashing", ex);
        }
    }
}

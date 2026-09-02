package project.shared.protocol;

public final class ProtocolMessage {
    public static final int MAX_LENGTH = 4096;

    private ProtocolMessage() {
    }

    public static String build(String command, Object... parts) {
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("Comando obbligatorio");
        }

        StringBuilder sb = new StringBuilder(command);
        for (Object part : parts) {
            sb.append(':');
            sb.append(part == null ? "" : String.valueOf(part));
        }
        return sb.toString();
    }

    public static String attachSessionToken(String request, String token) {
        if (request == null || request.isBlank()) {
            return request;
        }
        if (token == null || token.isBlank()) {
            return request;
        }
        if (request.contains(":TOKEN:")) {
            return request;
        }
        return request + ":TOKEN:" + token;
    }

    public static String[] split(String request) {
        if (request == null) {
            return new String[0];
        }
        return request.split(":", -1);
    }

    public static boolean isSafe(String request) {
        if (request == null || request.isBlank() || request.length() > MAX_LENGTH) {
            return false;
        }
        return !request.contains("\n") && !request.contains("\r") && !request.contains("\0");
    }
}

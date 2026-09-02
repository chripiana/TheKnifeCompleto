package project.shared.logging;

public final class AppLogger {
    private AppLogger() {
    }

    public static void info(String component, String message) {
        System.out.println("[" + component + "] " + message);
    }

    public static void warn(String component, String message) {
        System.out.println("[WARN][" + component + "] " + message);
    }

    public static void error(String component, String message, Throwable throwable) {
        System.err.println("[ERROR][" + component + "] " + message);
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }
}

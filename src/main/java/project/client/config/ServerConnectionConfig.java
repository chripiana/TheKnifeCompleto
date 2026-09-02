package project.client.config;

import project.shared.models.ServerConnection;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;

/**
 * Gestisce la configurazione della connessione al server
 * Memorizza le impostazioni in un file locale
 */
public class ServerConnectionConfig {
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.theknife";
    private static final String CONFIG_FILE = CONFIG_DIR + "/server-config.properties";
    private static ServerConnection instance;

    public static void initialize() {
        instance = loadFromFile();
        if (instance == null) {
            instance = new ServerConnection();
            saveToFile(instance);
        }
    }

    public static ServerConnection getInstance() {
        if (instance == null) {
            initialize();
        }
        return instance;
    }

    public static void saveConfiguration(ServerConnection config) {
        instance = config;
        saveToFile(config);
    }

    private static void saveToFile(ServerConnection config) {
        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));
            Path configPath = Paths.get(CONFIG_FILE);
            try (PrintWriter writer = new PrintWriter(new FileWriter(configPath.toFile()))) {
                writer.println("# Configurazione server TheKnife");
                writer.println("# Il client salva solo host e porta del server: nessun dato del database");
                writer.println("server.host=" + config.getHost());
                writer.println("server.port=" + config.getPort());
            }

            try {
                Files.setPosixFilePermissions(configPath,
                        EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
            } catch (UnsupportedOperationException ignored) {
                // Alcuni sistemi non supportano PosixFilePermissions.
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio della configurazione: " + e.getMessage());
        }
    }

    private static ServerConnection loadFromFile() {
        try {
            if (!Files.exists(Paths.get(CONFIG_FILE))) {
                return null;
            }

            ServerConnection config = new ServerConnection();
            Files.lines(Paths.get(CONFIG_FILE)).forEach(line -> {
                if (line.isEmpty() || line.startsWith("#")) return;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    switch (key) {
                        case "server.host" -> config.setHost(value);
                        case "server.port" -> config.setPort(Integer.parseInt(value));
                        case "db.port", "db.name", "db.user", "db.password" -> {
                            // Il client non memorizza le credenziali del database.
                        }
                    }
                }
            });
            return config;
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della configurazione: " + e.getMessage());
            return null;
        }
    }
}

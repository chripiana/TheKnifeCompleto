package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String HOST     = "localhost";
    private static final int    PORT     = 5432;
    private static final String DB_NAME  = "theknife";
    private static final String USER     = "postgres";
    // ATTENZIONE: Assicurati che questa sia la password corretta del tuo PostgreSQL
    private static final String PASSWORD = "password";

    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME;

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void initialize() {
        ensureDatabaseExists();
        DatabaseInitializer.initializeIfNeeded();
    }

    private static void ensureDatabaseExists() {
        // Ci connettiamo al DB predefinito 'postgres' per poter creare il nuovo DB
        String adminUrl = "jdbc:postgresql://" + HOST + ":" + PORT + "/postgres";

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL non trovato!");
            return;
        }

        // 1. Connessione come admin
        try (Connection admin = DriverManager.getConnection(adminUrl, USER, PASSWORD)) {

            // 2. Controllo se il DB esiste
            boolean exists = false;
            String checkSql = "SELECT 1 FROM pg_database WHERE datname = '" + DB_NAME + "'";
            try (Statement stmt = admin.createStatement();
                 var rs = stmt.executeQuery(checkSql)) {
                if (rs.next()) {
                    exists = true;
                }
            }

            // 3. Se non esiste, lo creo
            if (!exists) {
                // Il comando CREATE DATABASE non può essere eseguito in un blocco preparedStatement con parametri in alcuni casi
                try (Statement stmt = admin.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE " + DB_NAME);
                    System.out.println("[DB] Database '" + DB_NAME + "' creato con successo.");
                }
            } else {
                System.out.println("[DB] Database '" + DB_NAME + "' già esistente.");
            }

        } catch (SQLException e) {
            // Se l'errore è "password authentication failed", il problema è la variabile PASSWORD
            System.err.println("ERRORE CRITICO: " + e.getMessage());
            throw new RuntimeException("Impossibile verificare/creare il database. Controlla password e permessi.", e);
        }
    }
}
/*
 * TheKnife - Progetto JavaFX/DB per il modulo client/server.
 * Autore: Nome Cognome, Matricola 000000, sede VA
 */
package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {

    private static String host = "localhost";
    private static int port = 5432;
    private static String dbName = "theknife";
    private static String user = "postgres";
    private static String password = "";

    private static String url;

    private static HikariDataSource dataSource = null;

    // PostGIS availability flag
    private static boolean postGisAvailable = false;

    public static void configure(String host, int port, String dbName, String user, String password) {
        DatabaseManager.host = host;
        DatabaseManager.port = port;
        DatabaseManager.dbName = dbName;
        DatabaseManager.user = user;
        DatabaseManager.password = password;
        DatabaseManager.url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
        // reset dataSource if reconfiguring
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            // lazy init with reasonable defaults
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setPoolName("TheKnifePool");
            // Ask the driver to switch to server-side prepared statements quickly
            config.addDataSourceProperty("prepareThreshold", "1");
            // keep some prepared statement caching hints (driver-dependent)
            config.addDataSourceProperty("preparedStatementCacheQueries", "256");

            dataSource = new HikariDataSource(config);
        }
        return dataSource.getConnection();
    }

    public static void initialize() {
        if (url == null) {
            configure(host, port, dbName, user, password);
        }
        ensureDatabaseExists();
        DatabaseInitializer.initializeIfNeeded();
        // After initialization, detect PostGIS availability
        try {
            detectPostGis();
            System.out.println("[DB] PostGIS available: " + postGisAvailable);
        } catch (Exception e) {
            System.err.println("[DB] Errore durante la detection PostGIS: " + e.getMessage());
            postGisAvailable = false;
        }
    }

    private static void ensureDatabaseExists() {
        String adminUrl = "jdbc:postgresql://" + host + ":" + port + "/postgres";

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL non trovato!");
            return;
        }

        // 1. Connessione come admin
        try (Connection admin = DriverManager.getConnection(adminUrl, user, password)) {

            // 2. Controllo se il DB esiste
            boolean exists = false;
            String checkSql = "SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'";
            try (Statement stmt = admin.createStatement();
                    var rs = stmt.executeQuery(checkSql)) {
                if (rs.next()) {
                    exists = true;
                }
            }

            // 3. Se non esiste, lo creo
            if (!exists) {
                // Il comando CREATE DATABASE non può essere eseguito in un blocco
                // preparedStatement con parametri in alcuni casi
                try (Statement stmt = admin.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE " + dbName);
                    System.out.println("[DB] Database '" + dbName + "' creato con successo.");
                }
            } else {
                System.out.println("[DB] Database '" + dbName + "' già esistente.");
            }

        } catch (SQLException e) {
            // Se l'errore è "password authentication failed", il problema è la variabile
            // PASSWORD
            System.err.println("ERRORE CRITICO: " + e.getMessage());
            throw new RuntimeException("Impossibile verificare/creare il database. Controlla password e permessi.", e);
        }
    }

    private static void detectPostGis() {
        try (Connection c = getConnection(); Statement stmt = c.createStatement()) {
            String sql = "SELECT EXISTS(SELECT 1 FROM pg_extension WHERE extname = 'postgis')";
            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    postGisAvailable = rs.getBoolean(1);
                } else {
                    postGisAvailable = false;
                }
            }
        } catch (SQLException e) {
            postGisAvailable = false;
            System.err.println("[DB] detectPostGis error: " + e.getMessage());
        }
    }

    public static boolean isPostGisAvailable() {
        return postGisAvailable;
    }
}
package db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    public static void initializeIfNeeded() {
        try (Connection conn = DatabaseManager.getConnection()) {

            if (tablesExist(conn)) {
                System.out.println("[DB] Schema già presente, avvio il controllo o l'importazione dei ristoranti.");
                return;
            }

            System.out.println("[DB] Schema non trovato, eseguo theknife_create_db.sql...");
            executeSqlScript(conn, "/theknife_create_db.sql");
            System.out.println("[DB] Schema creato con successo.");

            System.out.println("[DB] Avvio importazione ristoranti da ristoranti_clean.csv...");
            importaRistoranti(conn, "ristoranti_clean.csv");

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'inizializzazione del database.", e);
        }
    }

    private static boolean tablesExist(Connection conn) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM information_schema.tables
             WHERE table_schema = 'public'
               AND table_name   = 'utenti'
            """;
        try (var stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static void executeSqlScript(Connection conn, String resourcePath) throws Exception {
        try (var is = DatabaseInitializer.class.getResourceAsStream(resourcePath)) {
            if (is == null) throw new RuntimeException("Script non trovato: " + resourcePath);

            String sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            try (var stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        }
    }

    private static void importaRistoranti(Connection conn, String resourcePath) {
        String sql = """
                INSERT INTO RistorantiTheKnife 
                (id_ristorante, nome, nazione, citta, indirizzo, latitudine, longitudine, prezzo_medio, stellato, prenotazione_obbligatoria, tipologia_cucina)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id_ristorante) DO NOTHING;
                """;

        String cleanedPath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        try (var is = Thread.currentThread().getContextClassLoader().getResourceAsStream(cleanedPath)) {
            if (is == null) {
                System.err.println("[DB] ERRORE: File delle risorse " + resourcePath + " non trovato!");
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                String riga;
                int contatore = 0;
                int batchSize = 100;

                while ((riga = br.readLine()) != null) {
                    if (riga.trim().isEmpty()) continue;

                    // =========================================================================
                    // LOGICA DI SANIFICAZIONE AVANZATA PER I PIPE INTERNI AI NOMI DEI RISTORANTI
                    // =========================================================================
                    if (riga.contains("|a|o c|") || riga.contains("|a|o|c|")) {
                        riga = riga.replace("|a|o c|", "|a o c|").replace("|a|o|c|", "|a o c|");
                    }
                    if (riga.contains("|ZweiSinn Meiers")) {
                        riga = riga.replace("|ZweiSinn Meiers | Bistro | Fine Dining|", "|ZweiSinn Meiers - Bistro - Fine Dining|");
                    }
                    if (riga.contains("|Opera | di Danilo Bei|")) {
                        riga = riga.replace("|Opera | di Danilo Bei|", "|Opera - di Danilo Bei|");
                    }
                    if (riga.contains("|SO |") || riga.contains("|SO|")) {
                        riga = riga.replace("|SO |", "|SO - |");
                    }
                    if (riga.contains("|Materia |")) {
                        riga = riga.replace("|Materia |", "|Materia - |");
                    }
                    if (riga.contains("|Eckert |")) {
                        riga = riga.replace("|Eckert |", "|Eckert - |");
                    }
                    // =========================================================================

                    String[] dati = riga.split("\\|");

                    if (dati.length >= 11) {
                        try {
                            // 1. id_ristorante (UUID)
                            pstmt.setObject(1, java.util.UUID.fromString(dati[0].trim()));

                            // Campi testuali
                            pstmt.setString(2, dati[1].trim()); // nome
                            pstmt.setString(3, dati[2].trim()); // nazione
                            pstmt.setString(4, dati[3].trim()); // citta
                            pstmt.setString(5, dati[4].trim()); // indirizzo

                            // Coordinate Geografiche (Double)
                            pstmt.setDouble(6, Double.parseDouble(dati[5].trim().replace(",", "."))); // latitudine
                            pstmt.setDouble(7, Double.parseDouble(dati[6].trim().replace(",", "."))); // longitudine

                            // Prezzo Medio (Integer)
                            String prezzoStr = dati[7].trim();
                            if (prezzoStr.contains(",")) {
                                prezzoStr = prezzoStr.split(",")[0];
                            } else if (prezzoStr.contains(".")) {
                                prezzoStr = prezzoStr.split("\\.")[0];
                            }
                            pstmt.setInt(8, Integer.parseInt(prezzoStr)); // prezzo_medio

                            // 9. Gestione valore stellato come intero (da 1 a 5, o 0 se false)
                            String stellatoStr = dati[8].trim().toLowerCase();
                            int numeroStelle = 0;

                            if (stellatoStr.equals("true")) {
                                numeroStelle = 1;
                            } else if (stellatoStr.equals("false")) {
                                numeroStelle = 0;
                            } else {
                                try {
                                    int val = Integer.parseInt(stellatoStr);
                                    if (val >= 0 && val <= 5) {
                                        numeroStelle = val;
                                    }
                                } catch (NumberFormatException nfe) {
                                    numeroStelle = 0;
                                }
                            }
                            pstmt.setInt(9, numeroStelle);

                            // 10. Prenotazione Obbligatoria (Boolean)
                            pstmt.setBoolean(10, Boolean.parseBoolean(dati[9].trim()));

                            // 11. Tipologia cucina
                            pstmt.setString(11, dati[10].trim()); // tipologia_cucina

                            pstmt.addBatch();
                            contatore++;

                            if (contatore % batchSize == 0) {
                                pstmt.executeBatch();
                            }
                        } catch (Exception e) {
                            System.err.println("[DB] Salto riga malformata ");
                        }
                    }
                }
                pstmt.executeBatch();
                System.out.println("[DB] Caricamento completato con successo! Ristoranti inseriti nel database: " + contatore);
            }
        } catch (Exception e) {
            System.err.println("[DB] Errore critico durante l'importazione del CSV: " + e.getMessage());
        }
    }
}
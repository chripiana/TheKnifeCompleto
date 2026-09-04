package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import project.shared.logging.AppLogger;

/**
 * TheKnifeDAO.java
 * Data Access Object per il serverTK - TheKnife
 * Laboratorio Interdisciplinare B - a.a. 2024/2025
 * Università degli Studi dell'Insubria
 */
public class TheKnifeDAO implements AutoCloseable {

    private final Connection conn;

    public TheKnifeDAO(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    // Helper: convert ResultSet to List<Map<String,Object>> and close metadata
    private List<Map<String,Object>> resultSetToList(ResultSet rs) throws SQLException {
        List<Map<String,Object>> rows = new ArrayList<>();
        if (rs == null) return rows;
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        while (rs.next()) {
            Map<String,Object> row = new HashMap<>(columns);
            for (int i = 1; i <= columns; i++) {
                String colName = md.getColumnLabel(i);
                if (colName == null || colName.isEmpty()) colName = md.getColumnName(i);
                row.put(colName, rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    // =========================================================
    // AUTENTICAZIONE E REGISTRAZIONE
    // =========================================================

    public int registrazione(String nome, String cognome, String email,
            String passwordHash, java.sql.Date dataNascita,
            String luogoDomicilio, double latDomicilio,
            double lonDomicilio, String ruolo) throws SQLException {

        String sql = """
                INSERT INTO Utenti
                    (nome, cognome, email, password_hash, data_nascita,
                     luogo_domicilio, lat_domicilio, lon_domicilio, ruolo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setString(3, email);
            ps.setString(4, passwordHash);
            ps.setDate(5, dataNascita);
            ps.setString(6, luogoDomicilio);
            ps.setDouble(7, latDomicilio);
            ps.setDouble(8, lonDomicilio);
            ps.setString(9, ruolo);
            return ps.executeUpdate();
        }
    }

    public ResultSet login(String email) throws SQLException {
        String sql = """
                SELECT id_utente, password_hash, ruolo
                  FROM Utenti
                 WHERE email = ?
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        return ps.executeQuery();
    }

    // =========================================================
    // FUNZIONALITÀ SENZA LOGIN
    // =========================================================

    public List<Map<String,Object>> cercaRistorante(String citta, String tipoCucina,
            Integer prezzoMin, Integer prezzoMax,
            Boolean delivery, Boolean prenotazioneOnline,
            Double minStelle) throws SQLException {

        long t0 = System.nanoTime();

        StringBuilder sql = new StringBuilder("""
                SELECT r.*,
                       COALESCE(AVG(rec.stelle), 0) AS media_stelle,
                       COUNT(rec.id_recensione)      AS num_recensioni
                  FROM RistorantiTheKnife r
                  LEFT JOIN Recensioni rec ON rec.id_ristorante = r.id_ristorante
                 WHERE (?::text IS NULL OR ?::text = '' OR LOWER(r.citta) LIKE LOWER(?))
                """);

        List<Object> params = new ArrayList<>();
        params.add(citta);
        params.add(citta);
        params.add(citta == null ? null : "%" + citta + "%");

        if (tipoCucina != null && !tipoCucina.isEmpty()) {
            sql.append(" AND (LOWER(r.tipologia_cucina) LIKE ? OR LOWER(r.nome) LIKE ?)");
            params.add("%" + tipoCucina.toLowerCase() + "%");
            params.add("%" + tipoCucina.toLowerCase() + "%");
        }
        if (prezzoMin != null) {
            sql.append(" AND r.prezzo_medio >= ?");
            params.add(prezzoMin);
        }
        if (prezzoMax != null) {
            sql.append(" AND r.prezzo_medio <= ?");
            params.add(prezzoMax);
        }
        if (delivery != null) {
            sql.append(" AND r.delivery = ?");
            params.add(delivery);
        }
        if (prenotazioneOnline != null) {
            sql.append(" AND r.prenotazione_online = ?");
            params.add(prenotazioneOnline);
        }

        sql.append(" GROUP BY r.id_ristorante");

        if (minStelle != null) {
            sql.append(" HAVING COALESCE(AVG(rec.stelle), 0) >= ?");
            params.add(minStelle);
        }

        sql.append(" ORDER BY media_stelle DESC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String,Object>> rows = resultSetToList(rs);
                long durMs = (long)((System.nanoTime() - t0) / 1_000_000.0);
                AppLogger.info("DAO", "cercaRistorante took " + durMs + " ms, rows=" + rows.size());
                return rows;
            }
        }
    }

    // FEDELE ALLO SCHEMA: Parametro idRistorante modificato da int a String
    // (VARCHAR)
    public ResultSet getStatisticheRecensioni(String idRistorante) throws SQLException {
        String sql = """
                SELECT COALESCE(AVG(stelle), 0) AS media_stelle,
                       COUNT(*)                  AS num_recensioni
                  FROM Recensioni
                 WHERE id_ristorante = ?
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, idRistorante);
        return ps.executeQuery();
    }

    // FEDELE ALLO SCHEMA: Parametro idRistorante modificato da int a String
    // (VARCHAR)
    public ResultSet getDistribuzioneStelle(String idRistorante) throws SQLException {
        String sql = """
                SELECT stelle, COUNT(*) AS conteggio
                  FROM Recensioni
                 WHERE id_ristorante = ?
                 GROUP BY stelle
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, idRistorante);
        return ps.executeQuery();
    }

    // FEDELE ALLO SCHEMA: Parametro idRistorante modificato da int a String
    // (VARCHAR)
    public ResultSet visualizzaRecensioniConAutore(String idRistorante) throws SQLException {
        String sql = """
                SELECT u.nome, u.cognome, rec.stelle, rec.testo, rec.data_recensione,
                       risp.testo AS risposta_gestore, risp.data_risposta
                  FROM Recensioni rec
                  JOIN Utenti u ON u.id_utente = rec.id_utente
                  LEFT JOIN RisposteRecensioni risp ON risp.id_recensione = rec.id_recensione
                 WHERE rec.id_ristorante = ?
                 ORDER BY rec.data_recensione DESC
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, idRistorante);
        return ps.executeQuery();
    }

    // FEDELE ALLO SCHEMA: Parametro idRistorante modificato da int a String
    // (VARCHAR)
    public ResultSet visualizzaRecensioni(String idRistorante) throws SQLException {
        String sql = """
                SELECT rec.stelle,
                       rec.testo,
                       rec.data_recensione,
                       risp.testo         AS risposta_gestore,
                       risp.data_risposta
                  FROM Recensioni rec
                  LEFT JOIN RisposteRecensioni risp ON risp.id_recensione = rec.id_recensione
                 WHERE rec.id_ristorante = ?
                 ORDER BY rec.data_recensione DESC
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, idRistorante);
        return ps.executeQuery();
    }

    public ResultSet getCoordinateDomicilio(int idUtente) throws SQLException {
        String sql = """
                SELECT lat_domicilio, lon_domicilio
                  FROM Utenti
                 WHERE id_utente = ?
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idUtente);
        return ps.executeQuery();
    }

    public int creaPrenotazione(int idUtente, String idRistorante, java.sql.Date data, java.sql.Time ora,
            int numeroPersone, String note, String codicePrenotazione) throws SQLException {
        String sql = """
                INSERT INTO Prenotazioni
                   (id_utente, id_ristorante, data_prenotazione, ora_prenotazione,
                    numero_persone, codice_prenotazione, note, stato)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'attiva')
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            ps.setString(2, idRistorante);
            ps.setDate(3, data);
            ps.setTime(4, ora);
            ps.setInt(5, numeroPersone);
            ps.setString(6, codicePrenotazione);
            ps.setString(7, note == null ? "" : note);
            return ps.executeUpdate();
        }
    }

    public ResultSet getPrenotazioniUtente(int idUtente) throws SQLException {
        String sql = """
                SELECT p.id_prenotazione,
                      p.id_ristorante,
                      r.nome AS nome_ristorante,
                      p.data_prenotazione,
                      p.ora_prenotazione,
                      p.numero_persone,
                      p.codice_prenotazione,
                      p.note,
                      p.stato,
                      p.created_at
                 FROM Prenotazioni p
                 JOIN RistorantiTheKnife r ON r.id_ristorante = p.id_ristorante
                WHERE p.id_utente = ?
                ORDER BY p.data_prenotazione DESC, p.ora_prenotazione DESC
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idUtente);
        return ps.executeQuery();
    }

    public int aggiornaPrenotazione(int idPrenotazione, int idUtente, java.sql.Date data, java.sql.Time ora,
            int numeroPersone, String note) throws SQLException {
        String sql = """
                UPDATE Prenotazioni
                  SET data_prenotazione = ?,
                      ora_prenotazione = ?,
                      numero_persone = ?,
                      note = ?,
                      updated_at = CURRENT_TIMESTAMP,
                      stato = 'modificata'
                WHERE id_prenotazione = ?
                  AND id_utente = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, data);
            ps.setTime(2, ora);
            ps.setInt(3, numeroPersone);
            ps.setString(4, note == null ? "" : note);
            ps.setInt(5, idPrenotazione);
            ps.setInt(6, idUtente);
            return ps.executeUpdate();
        }
    }

    public int eliminaPrenotazione(int idPrenotazione, int idUtente) throws SQLException {
        String sql = "DELETE FROM Prenotazioni WHERE id_prenotazione = ? AND id_utente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPrenotazione);
            ps.setInt(2, idUtente);
            return ps.executeUpdate();
        }
    }

    public java.util.List<java.util.Map<String,Object>> ristorantiVicini(double lat, double lon) throws SQLException {
        String sql = """
                SELECT *,
                       SQRT(POWER(latitudine  - ?, 2) +
                            POWER(longitudine - ?, 2)) AS dist_approx
                  FROM RistorantiTheKnife
                 ORDER BY dist_approx
                 LIMIT 20
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, lat);
            ps.setDouble(2, lon);
            try (ResultSet rs = ps.executeQuery()) {
                return resultSetToList(rs);
            }
        }
    }

    public java.util.List<java.util.Map<String,Object>> cercaRistorantiViciniPostGis(double lat, double lon, double radiusKm,
            String citta, String tipoCucina, Integer prezzoMax, Double minStelle) throws SQLException {
        long t0 = System.nanoTime();
        AppLogger.info("DAO", "cercaRistorantiViciniPostGis: lat=" + lat + ", lon=" + lon + ", radiusKm=" + radiusKm + ", citta=" + citta + ", tipoCucina=" + tipoCucina);
        // Uses PostGIS ST_DWithin on geography type for fast spatial search
        // NOTE: Using WITH clause to avoid duplicate parameter placeholders for the search point
        StringBuilder sql = new StringBuilder("""
                WITH search_point AS (
                    SELECT ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography AS geom
                )
                SELECT r.*,
                       COALESCE(AVG(rec.stelle), 0) AS media_stelle,
                       COUNT(rec.id_recensione)     AS num_recensioni,
                       ST_Distance(r.geom, (SELECT geom FROM search_point)) AS distanza_m
                  FROM RistorantiTheKnife r
                  LEFT JOIN Recensioni rec ON rec.id_ristorante = r.id_ristorante
                 WHERE (?::text IS NULL OR ?::text = '' OR LOWER(r.citta) LIKE LOWER(?))
                   AND r.geom IS NOT NULL
                   AND ST_DWithin(r.geom, (SELECT geom FROM search_point), ?)
                """);

        java.util.List<Object> params = new java.util.ArrayList<>();
        // point for search (used in WITH clause and ST_DWithin)
        params.add(lon); // ST_MakePoint expects (lon, lat)
        params.add(lat);
        // city filter params
        params.add(citta);
        params.add(citta);
        params.add(citta == null ? null : "%" + citta + "%");
        // radius in meters
        params.add((int)(radiusKm * 1000));

        if (tipoCucina != null && !tipoCucina.isEmpty()) {
            sql.append(" AND (LOWER(r.tipologia_cucina) LIKE ? OR LOWER(r.nome) LIKE ?)");
            params.add("%" + tipoCucina.toLowerCase() + "%");
            params.add("%" + tipoCucina.toLowerCase() + "%");
        }
        if (prezzoMax != null) {
            sql.append(" AND r.prezzo_medio <= ?");
            params.add(prezzoMax);
        }

        sql.append(" GROUP BY r.id_ristorante");
        if (minStelle != null) {
            sql.append(" HAVING COALESCE(AVG(rec.stelle), 0) >= ?");
            params.add(minStelle);
        }
        sql.append(" ORDER BY distanza_m ASC LIMIT 50");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                java.util.List<java.util.Map<String,Object>> rows = resultSetToList(rs);
                long durMs = (long)((System.nanoTime() - t0) / 1_000_000.0);
                AppLogger.info("DAO", "cercaRistorantiViciniPostGis took " + durMs + " ms, rows=" + rows.size());
                return rows;
            }
        }
    }

    // Fallback: existing bounding-box + Haversine method kept for environments without PostGIS
    public java.util.List<java.util.Map<String,Object>> cercaRistorantiVicini(double lat, double lon, double radiusKm,
            String citta, String tipoCucina, Integer prezzoMax, Double minStelle) throws SQLException {
        long t0 = System.nanoTime();
        // Apply fast bounding-box filter before Haversine to drastically reduce scanned rows.
        // Compute lat/lon deltas for the given radius (in km).
        final double earthRadiusKm = 6371.0;
        double radiusRad = radiusKm / earthRadiusKm;
        double latRad = Math.toRadians(lat);
        double minLat = Math.toDegrees(latRad - radiusRad);
        double maxLat = Math.toDegrees(latRad + radiusRad);
        // Prevent division by zero near poles
        double deltaLonDeg;
        if (Math.abs(lat) >= 89.9) {
            deltaLonDeg = 180.0;
        } else {
            deltaLonDeg = Math.toDegrees(Math.asin(Math.min(1.0, Math.sin(radiusRad) / Math.cos(latRad))));
        }
        double minLon = lon - deltaLonDeg;
        double maxLon = lon + deltaLonDeg;

        StringBuilder sql = new StringBuilder("""
                SELECT r.*,
                      COALESCE(AVG(rec.stelle), 0) AS media_stelle,
                      COUNT(rec.id_recensione)     AS num_recensioni,
                      (6371 * ACOS(
                          COS(RADIANS(?)) * COS(RADIANS(r.latitudine)) *
                          COS(RADIANS(r.longitudine) - RADIANS(?)) +
                          SIN(RADIANS(?)) * SIN(RADIANS(r.latitudine))
                      )) AS distanza_km
                 FROM RistorantiTheKnife r
                 LEFT JOIN Recensioni rec ON rec.id_ristorante = r.id_ristorante
                WHERE r.latitudine BETWEEN ? AND ?
                  AND r.longitudine BETWEEN ? AND ?
                  AND (?::text IS NULL OR ?::text = '' OR LOWER(r.citta) LIKE LOWER(?))
                """);

        List<Object> params = new ArrayList<>();
        // Parameters for distance calculation (used in SELECT distancia_km)
        params.add(lat); // 1
        params.add(lon); // 2
        params.add(lat); // 3
        // Bounding box params
        params.add(minLat); // 4
        params.add(maxLat); // 5
        params.add(minLon); // 6
        params.add(maxLon); // 7
        // City filter params
        params.add(citta);
        params.add(citta);
        params.add(citta == null ? null : "%" + citta + "%");

        if (tipoCucina != null && !tipoCucina.isEmpty()) {
            sql.append(" AND (LOWER(r.tipologia_cucina) LIKE ? OR LOWER(r.nome) LIKE ?)");
            params.add("%" + tipoCucina.toLowerCase() + "%");
            params.add("%" + tipoCucina.toLowerCase() + "%");
        }
        if (prezzoMax != null) {
            sql.append(" AND r.prezzo_medio <= ?");
            params.add(prezzoMax);
        }

        // Final distance filter: keep it after bounding box to minimize trig calls.
        sql.append(" AND (6371 * ACOS(")
                .append("COS(RADIANS(?)) * COS(RADIANS(r.latitudine)) * ")
                .append("COS(RADIANS(r.longitudine) - RADIANS(?)) + ")
                .append("SIN(RADIANS(?)) * SIN(RADIANS(r.latitudine)))) <= ?");
        params.add(lat);     // for the distance comparison (again)
        params.add(lon);
        params.add(lat);
        params.add(radiusKm);

        sql.append(" GROUP BY r.id_ristorante");
        if (minStelle != null) {
            sql.append(" HAVING COALESCE(AVG(rec.stelle), 0) >= ?");
            params.add(minStelle);
        }
        sql.append(" ORDER BY distanza_km ASC LIMIT 50");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                java.util.List<java.util.Map<String,Object>> rows = resultSetToList(rs);
                long durMs = (long)((System.nanoTime() - t0) / 1_000_000.0);
                AppLogger.info("DAO", "cercaRistorantiVicini took " + durMs + " ms, rows=" + rows.size());
                return rows;
            }
        }
    }

    // =========================================================
    // FUNZIONALITÀ CLIENTI (login richiesto)
    // =========================================================

    // FEDELE ALLO SCHEMA: idRistorante modificato da int a String
    public boolean isPreferito(int idUtente, String idRistorante) throws SQLException {
        String sql = "SELECT 1 FROM Preferiti WHERE id_utente = ? AND id_ristorante = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            ps.setString(2, idRistorante);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // FEDELE ALLO SCHEMA: idRistorante modificato da int a String
    public int aggiungiPreferito(int idUtente, String idRistorante) throws SQLException {
        String sql = """
                INSERT INTO Preferiti (id_utente, id_ristorante)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            ps.setString(2, idRistorante);
            return ps.executeUpdate();
        }
    }

    // FEDELE ALLO SCHEMA: idRistorante modificato da int a String
    public int rimuoviPreferito(int idUtente, String idRistorante) throws SQLException {
        String sql = """
                DELETE FROM Preferiti
                 WHERE id_utente = ? AND id_ristorante = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            ps.setString(2, idRistorante);
            return ps.executeUpdate();
        }
    }

    public ResultSet visualizzaPreferiti(int idUtente) throws SQLException {
        String sql = """
                SELECT r.*,
                       COALESCE(AVG(rec.stelle), 0) AS media_stelle,
                       COUNT(rec.id_recensione)      AS num_recensioni
                  FROM Preferiti p
                  JOIN RistorantiTheKnife r ON r.id_ristorante = p.id_ristorante
                  LEFT JOIN Recensioni rec  ON rec.id_ristorante = r.id_ristorante
                 WHERE p.id_utente = ?
                 GROUP BY r.id_ristorante
                 ORDER BY r.nome
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idUtente);
        return ps.executeQuery();
    }

    // FEDELE ALLO SCHEMA: idRistorante modificato da int a String
    public int aggiungiRecensione(String idRistorante, int idUtente,
            int stelle, String testo) throws SQLException {
        String sql = """
                INSERT INTO Recensioni (id_ristorante, id_utente, stelle, testo)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idRistorante);
            ps.setInt(2, idUtente);
            ps.setInt(3, stelle);
            ps.setString(4, testo);
            return ps.executeUpdate();
        }
    }

    public int modificaRecensione(int idRecensione, int idUtente,
            int stelle, String testo) throws SQLException {
        String sql = """
                UPDATE Recensioni
                   SET stelle = ?,
                       testo  = ?
                 WHERE id_recensione = ?
                   AND id_utente     = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stelle);
            ps.setString(2, testo);
            ps.setInt(3, idRecensione);
            ps.setInt(4, idUtente);
            return ps.executeUpdate();
        }
    }

    public int eliminaRecensione(int idRecensione, int idUtente) throws SQLException {
        String sql = """
                DELETE FROM Recensioni
                 WHERE id_recensione = ?
                   AND id_utente     = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRecensione);
            ps.setInt(2, idUtente);
            return ps.executeUpdate();
        }
    }

    public ResultSet mieRecensioni(int idUtente) throws SQLException {
        String sql = """
                SELECT r.id_ristorante,
                       r.nome             AS nome_ristorante,
                       r.citta,
                       rec.id_recensione,
                       rec.stelle,
                       rec.testo          AS mia_recensione,
                       rec.data_recensione,
                       risp.testo         AS risposta_gestore,
                       risp.data_risposta
                  FROM Recensioni rec
                  JOIN RistorantiTheKnife r ON r.id_ristorante = rec.id_ristorante
                  LEFT JOIN RisposteRecensioni risp ON risp.id_recensione = rec.id_recensione
                 WHERE rec.id_utente = ?
                 ORDER BY rec.data_recensione DESC
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idUtente);
        return ps.executeQuery();
    }

    // =========================================================
    // FUNZIONALITÀ GESTORI (login richiesto)
    // =========================================================

    public int aggiungiRistorante(String nome, String nazione, String citta,
            String indirizzo, double latitudine, double longitudine,
            int fasciaPrezzo, boolean delivery,
            boolean prenotazioneOnline, String tipoCucina,
            int idGestore) throws SQLException {

        String idRistorante = UUID.randomUUID().toString();

        String sql = """
                INSERT INTO RistorantiTheKnife
                    (id_ristorante, nome, nazione, citta, indirizzo, latitudine, longitudine,
                     prezzo_medio, delivery, prenotazione_online, tipologia_cucina, id_gestore)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idRistorante);
            ps.setString(2, nome);
            ps.setString(3, nazione);
            ps.setString(4, citta);
            ps.setString(5, indirizzo);
            ps.setDouble(6, latitudine);
            ps.setDouble(7, longitudine);
            ps.setInt(8, fasciaPrezzo);
            ps.setBoolean(9, delivery);
            ps.setBoolean(10, prenotazioneOnline);
            ps.setString(11, tipoCucina);
            ps.setInt(12, idGestore);
            return ps.executeUpdate();
        }
    }

    // Recupera tutti i dati di un singolo ristorante (per popolare il form di
    // modifica)
    public ResultSet getRistorantePerId(String idRistorante, int idGestore) throws SQLException {
        String sql = """
                SELECT id_ristorante, nome, nazione, citta, indirizzo,
                       latitudine, longitudine, prezzo_medio, delivery,
                       prenotazione_online, tipologia_cucina, id_gestore
                  FROM RistorantiTheKnife
                 WHERE id_ristorante = ? AND id_gestore = ?
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, idRistorante);
        ps.setInt(2, idGestore);
        return ps.executeQuery();
    }

    /**
     * Recupera i dettagli di un ristorante come Map (chiude le risorse)
     */
    public java.util.Map<String,Object> getRistoranteDetailsMap(String idRistorante) throws SQLException {
        String sql = "SELECT id_ristorante, nome, nazione, citta, indirizzo, tipologia_cucina, prezzo_medio, delivery, prenotazione_online, latitudine, longitudine FROM RistorantiTheKnife WHERE id_ristorante = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idRistorante);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    Map<String,Object> row = new HashMap<>(cols);
                    for (int i = 1; i <= cols; i++) {
                        String colName = md.getColumnLabel(i);
                        if (colName == null || colName.isEmpty()) colName = md.getColumnName(i);
                        row.put(colName, rs.getObject(i));
                    }
                    return row;
                } else {
                    return new HashMap<>();
                }
            }
        }
    }

    // Modifica le specifiche di un ristorante esistente (solo dal proprio gestore)
    public boolean modificaRistorante(String idRistorante, int idGestore, String nome, String citta,
            String indirizzo, int prezzoMedio, boolean delivery,
            boolean prenotazioneOnline, String tipoCucina) throws SQLException {
        String sql = """
                UPDATE RistorantiTheKnife
                   SET nome = ?,
                       citta = ?,
                       indirizzo = ?,
                       prezzo_medio = ?,
                       delivery = ?,
                       prenotazione_online = ?,
                       tipologia_cucina = ?
                 WHERE id_ristorante = ? AND id_gestore = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, citta);
            ps.setString(3, indirizzo);
            ps.setInt(4, prezzoMedio);
            ps.setBoolean(5, delivery);
            ps.setBoolean(6, prenotazioneOnline);
            ps.setString(7, tipoCucina);
            ps.setString(8, idRistorante);
            ps.setInt(9, idGestore);
            return ps.executeUpdate() > 0;
        }
    }

    // Elimina un ristorante (solo se appartiene al gestore che ne fa richiesta)
    public boolean eliminaRistorante(String idRistorante, int idGestore) throws SQLException {
        String sql = """
                DELETE FROM RistorantiTheKnife
                 WHERE id_ristorante = ? AND id_gestore = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idRistorante);
            ps.setInt(2, idGestore);
            return ps.executeUpdate() > 0;
        }
    }

    public ResultSet visualizzaRiepilogo(int idGestore) throws SQLException {
        String sql = """
                SELECT r.id_ristorante,
                       r.nome,
                       r.citta,
                       COALESCE(AVG(rec.stelle), 0) AS media_stelle,
                       COUNT(rec.id_recensione)      AS num_recensioni
                  FROM RistorantiTheKnife r
                  LEFT JOIN Recensioni rec ON rec.id_ristorante = r.id_ristorante
                 WHERE r.id_gestore = ?
                 GROUP BY r.id_ristorante
                 ORDER BY r.nome
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idGestore);
        return ps.executeQuery();
    }

    public ResultSet visualizzaRecensioniGestore(int idGestore) throws SQLException {
        String sql = """
                SELECT r.id_ristorante,
                       r.nome AS nome,
                       rec.id_recensione,
                       rec.stelle,
                       rec.testo,
                       rec.data_recensione,
                       u.nome AS autore_nome,
                       u.cognome AS autore_cognome,
                       CASE WHEN risp.id_risposta IS NOT NULL
                            THEN TRUE ELSE FALSE END  AS gia_risposto,
                       risp.testo                     AS risposta,
                       risp.data_risposta
                  FROM RistorantiTheKnife r
                  JOIN Recensioni rec ON rec.id_ristorante = r.id_ristorante
                  JOIN Utenti u ON u.id_utente = rec.id_utente
                  LEFT JOIN RisposteRecensioni risp ON risp.id_recensione = rec.id_recensione
                 WHERE r.id_gestore = ?
                 ORDER BY r.nome, rec.data_recensione DESC
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idGestore);
        return ps.executeQuery();
    }

    public int rispostaRecensione(int idRecensione, int idGestore,
            String testo) throws SQLException {
        String sql = """
                INSERT INTO RisposteRecensioni (id_recensione, id_gestore, testo)
                VALUES (?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRecensione);
            ps.setInt(2, idGestore);
            ps.setString(3, testo);
            return ps.executeUpdate();
        }
    }

    // =========================================================
    // GESTIONE E MODIFICA PROFILO UTENTE
    // =========================================================

    public ResultSet getDatiUtente(int idUtente) throws SQLException {
        String sql = """
                SELECT nome, cognome, email, data_nascita,
                       luogo_domicilio, lat_domicilio, lon_domicilio, ruolo
                  FROM Utenti
                 WHERE id_utente = ?
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idUtente);
        return ps.executeQuery();
    }

    public boolean updateProfiloUtente(int idUtente, String nome, String cognome,
            java.sql.Date dataNascita, String luogoDomicilio,
            double latDomicilio, double lonDomicilio) throws SQLException {
        String sql = """
                UPDATE Utenti
                   SET nome = ?,
                       cognome = ?,
                       data_nascita = ?,
                       luogo_domicilio = ?,
                       lat_domicilio = ?,
                       lon_domicilio = ?
                 WHERE id_utente = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setDate(3, dataNascita);
            ps.setString(4, luogoDomicilio);
            ps.setDouble(5, latDomicilio);
            ps.setDouble(6, lonDomicilio);
            ps.setInt(7, idUtente);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateProfiloUtenteConPassword(int idUtente, String nome, String cognome,
            java.sql.Date dataNascita, String luogoDomicilio,
            double latDomicilio, double lonDomicilio,
            String nuovaPasswordHash) throws SQLException {
        String sql = """
                UPDATE Utenti
                   SET nome = ?,
                       cognome = ?,
                       data_nascita = ?,
                       luogo_domicilio = ?,
                       lat_domicilio = ?,
                       lon_domicilio = ?,
                       password_hash = ?
                 WHERE id_utente = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setDate(3, dataNascita);
            ps.setString(4, luogoDomicilio);
            ps.setDouble(5, latDomicilio);
            ps.setDouble(6, lonDomicilio);
            ps.setString(7, nuovaPasswordHash);
            ps.setInt(8, idUtente);
            return ps.executeUpdate() > 0;
        }
    }

    // FEDELE ALLO SCHEMA: colonne rinominate coerentemente con lo script SQL
    // (tipologia_cucina, prezzo_medio)
    public ResultSet getPreferitiUtente(int idUtente) throws SQLException {
        String sql = """
                SELECT r.id_ristorante, r.nome, r.citta, r.tipologia_cucina, r.prezzo_medio
                  FROM Preferiti p
                  JOIN RistorantiTheKnife r ON p.id_ristorante = r.id_ristorante
                 WHERE p.id_utente = ?
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idUtente);
        return ps.executeQuery();
    }

    public ResultSet getRecensioniUtente(int idUtente) throws SQLException {
        String sql = """
                SELECT rec.id_recensione, rec.testo, rec.stelle, rec.data_recensione, r.nome AS nome_ristorante
                  FROM Recensioni rec
                  JOIN RistorantiTheKnife r ON rec.id_ristorante = r.id_ristorante
                 WHERE rec.id_utente = ?
                 ORDER BY rec.data_recensione DESC
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idUtente);
        return ps.executeQuery();
    }

    // New convenience methods returning DTOs (List/Map) to avoid leaking ResultSet
    public java.util.Map<String,Object> getLoginData(String email) throws SQLException {
        String sql = """
                SELECT id_utente, password_hash, ruolo
                  FROM Utenti
                 WHERE email = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.util.Map<String,Object> m = new java.util.HashMap<>();
                    m.put("id_utente", rs.getObject("id_utente"));
                    m.put("password_hash", rs.getObject("password_hash"));
                    m.put("ruolo", rs.getObject("ruolo"));
                    return m;
                }
            }
        }
        return null;
    }

    public java.util.Map<String,Object> getStatisticheRecensioniMap(String idRistorante) throws SQLException {
        String sql = """
                SELECT COALESCE(AVG(stelle), 0) AS media_stelle,
                       COUNT(*)                  AS num_recensioni
                  FROM Recensioni
                 WHERE id_ristorante = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idRistorante);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.util.Map<String,Object> m = new java.util.HashMap<>();
                    m.put("media_stelle", rs.getObject("media_stelle"));
                    m.put("num_recensioni", rs.getObject("num_recensioni"));
                    return m;
                }
            }
        }
        return null;
    }

    public java.util.List<java.util.Map<String,Object>> getDistribuzioneStelleList(String idRistorante) throws SQLException {
        String sql = """
                SELECT stelle, COUNT(*) AS conteggio
                  FROM Recensioni
                 WHERE id_ristorante = ?
                 GROUP BY stelle
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idRistorante);
            try (ResultSet rs = ps.executeQuery()) {
                return resultSetToList(rs);
            }
        }
    }

    public java.util.List<java.util.Map<String,Object>> getPreferitiUtenteList(int idUtente) throws SQLException {
        String sql = """
                SELECT r.id_ristorante, r.nome, r.citta, r.tipologia_cucina, r.prezzo_medio
                  FROM Preferiti p
                  JOIN RistorantiTheKnife r ON p.id_ristorante = r.id_ristorante
                 WHERE p.id_utente = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                return resultSetToList(rs);
            }
        }
    }

    public java.util.List<java.util.Map<String,Object>> getRecensioniUtenteList(int idUtente) throws SQLException {
        String sql = """
                SELECT rec.id_recensione, rec.testo, rec.stelle, rec.data_recensione, r.nome AS nome_ristorante
                  FROM Recensioni rec
                  JOIN RistorantiTheKnife r ON rec.id_ristorante = r.id_ristorante
                 WHERE rec.id_utente = ?
                 ORDER BY rec.data_recensione DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                return resultSetToList(rs);
            }
        }
    }

    public java.util.List<java.util.Map<String,Object>> getPrenotazioniUtenteList(int idUtente) throws SQLException {
        // Legacy call kept for compatibility: cap to 200
        return getPrenotazioniUtenteList(idUtente, 200, 0);
    }

    public java.util.List<java.util.Map<String,Object>> getPrenotazioniUtenteList(int idUtente, int limit, int offset) throws SQLException {
        if (limit <= 0) limit = 50;
        if (limit > 500) limit = 500; // safety cap to prevent excessive memory use
        if (offset < 0) offset = 0;

        String sql = """
                SELECT p.id_prenotazione,
                      p.id_ristorante,
                      r.nome AS nome_ristorante,
                      p.data_prenotazione,
                      p.ora_prenotazione,
                      p.numero_persone,
                      p.codice_prenotazione,
                      p.note,
                      p.stato,
                      p.created_at
                 FROM Prenotazioni p
                 JOIN RistorantiTheKnife r ON r.id_ristorante = p.id_ristorante
                WHERE p.id_utente = ?
                ORDER BY p.data_prenotazione DESC, p.ora_prenotazione DESC
                LIMIT ? OFFSET ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                return resultSetToList(rs);
            }
        }
    }

    public java.util.List<java.util.Map<String,Object>> getPrenotazioniRicevuteGestore(int idGestore) throws SQLException {
        String sql = """
                SELECT p.id_prenotazione,
                      p.id_utente,
                      u.nome AS nome_cliente,
                      u.cognome AS cognome_cliente,
                      p.id_ristorante,
                      r.nome AS nome_ristorante,
                      p.data_prenotazione,
                      p.ora_prenotazione,
                      p.numero_persone,
                      p.codice_prenotazione,
                      p.note,
                      p.stato,
                      p.created_at
                 FROM Prenotazioni p
                 JOIN RistorantiTheKnife r ON r.id_ristorante = p.id_ristorante
                 JOIN Utenti u ON u.id_utente = p.id_utente
                 WHERE r.id_gestore = ?
                 ORDER BY p.data_prenotazione DESC, p.ora_prenotazione DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGestore);
            try (ResultSet rs = ps.executeQuery()) {
                return resultSetToList(rs);
            }
        }
    }

    public int aggiornaStatoPrenotazione(int idPrenotazione, int idGestore, String nuovoStato) throws SQLException {
        String stato = nuovoStato == null ? "attiva" : nuovoStato.trim().toLowerCase();
        if (!stato.equals("attiva") && !stato.equals("accettata") && !stato.equals("rifiutata")) {
            throw new SQLException("Stato prenotazione non valido: " + nuovoStato);
        }

        String sql = """
                UPDATE Prenotazioni p
                  SET stato = ?,
                      updated_at = CURRENT_TIMESTAMP
                 FROM RistorantiTheKnife r
                 WHERE p.id_ristorante = r.id_ristorante
                  AND p.id_prenotazione = ?
                  AND r.id_gestore = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stato);
            ps.setInt(2, idPrenotazione);
            ps.setInt(3, idGestore);
            return ps.executeUpdate();
        }
    }

    public java.util.Map<String,Object> getDatiUtenteMap(int idUtente) throws SQLException {
        String sql = """
                SELECT nome, cognome, email, data_nascita,
                       luogo_domicilio, lat_domicilio, lon_domicilio, ruolo
                  FROM Utenti
                 WHERE id_utente = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.util.Map<String,Object> m = new java.util.HashMap<>();
                    java.sql.ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    for (int i = 1; i <= cols; i++) {
                        String name = md.getColumnLabel(i);
                        if (name == null || name.isEmpty()) name = md.getColumnName(i);
                        m.put(name, rs.getObject(i));
                    }
                    return m;
                }
            }
        }
        return null;
    }

    public java.util.List<java.util.Map<String,Object>> visualizzaRiepilogoList(int idGestore) throws SQLException {
        String sql = """
                SELECT r.id_ristorante,
                       r.nome,
                       r.citta,
                       COALESCE(AVG(rec.stelle), 0) AS media_stelle,
                       COUNT(rec.id_recensione)      AS num_recensioni
                  FROM RistorantiTheKnife r
                  LEFT JOIN Recensioni rec ON rec.id_ristorante = r.id_ristorante
                 WHERE r.id_gestore = ?
                 GROUP BY r.id_ristorante
                 ORDER BY r.nome
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGestore);
            try (ResultSet rs = ps.executeQuery()) {
                return resultSetToList(rs);
            }
        }
    }

    public java.util.List<java.util.Map<String,Object>> visualizzaRecensioniGestoreList(int idGestore) throws SQLException {
        String sql = """
                SELECT r.id_ristorante,
                       r.nome AS nome,
                       rec.id_recensione,
                       rec.stelle,
                       rec.testo,
                       rec.data_recensione,
                       u.nome AS autore_nome,
                       u.cognome AS autore_cognome,
                       CASE WHEN risp.id_risposta IS NOT NULL
                            THEN TRUE ELSE FALSE END  AS gia_risposto,
                       risp.testo                     AS risposta,
                       risp.data_risposta
                  FROM RistorantiTheKnife r
                  JOIN Recensioni rec ON rec.id_ristorante = r.id_ristorante
                  JOIN Utenti u ON u.id_utente = rec.id_utente
                  LEFT JOIN RisposteRecensioni risp ON risp.id_recensione = rec.id_recensione
                 WHERE r.id_gestore = ?
                 ORDER BY r.nome, rec.data_recensione DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGestore);
            try (ResultSet rs = ps.executeQuery()) {
                return resultSetToList(rs);
            }
        }
    }

    public java.util.List<java.util.Map<String,Object>> mieRecensioniList(int idUtente) throws SQLException {
        String sql = """
                SELECT r.id_ristorante,
                       r.nome             AS nome_ristorante,
                       r.citta,
                       rec.id_recensione,
                       rec.stelle,
                       rec.testo          AS mia_recensione,
                       rec.data_recensione,
                       risp.testo         AS risposta_gestore,
                       risp.data_risposta
                  FROM Recensioni rec
                  JOIN RistorantiTheKnife r ON r.id_ristorante = rec.id_ristorante
                  LEFT JOIN RisposteRecensioni risp ON risp.id_recensione = rec.id_recensione
                 WHERE rec.id_utente = ?
                 ORDER BY rec.data_recensione DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                return resultSetToList(rs);
            }
        }
    }

    // end of new DTO helper methods
}
package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * TheKnifeDAO.java
 * Data Access Object per il serverTK - TheKnife
 * Laboratorio Interdisciplinare B - a.a. 2024/2025
 * Università degli Studi dell'Insubria
 */
public class TheKnifeDAO {

    private final Connection conn;

    public TheKnifeDAO(Connection conn) {
        this.conn = conn;
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

    public ResultSet cercaRistorante(String citta, String tipoCucina,
            Integer prezzoMin, Integer prezzoMax,
            Boolean delivery, Boolean prenotazioneOnline,
            Double minStelle) throws SQLException {

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

        PreparedStatement ps = conn.prepareStatement(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
        return ps.executeQuery();
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

    public ResultSet ristorantiVicini(double lat, double lon) throws SQLException {
        String sql = """
                SELECT *,
                       SQRT(POWER(latitudine  - ?, 2) +
                            POWER(longitudine - ?, 2)) AS dist_approx
                  FROM RistorantiTheKnife
                 ORDER BY dist_approx
                 LIMIT 20
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDouble(1, lat);
        ps.setDouble(2, lon);
        return ps.executeQuery();
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
}
package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TheKnifeDAO.java
 * Data Access Object per il serverTK - TheKnife
 * Laboratorio Interdisciplinare B - a.a. 2024/2025
 * Università degli Studi dell'Insubria
 *
 * Implementa tutte le PreparedStatement JDBC descritte in theknife_queries.sql.
 * La connessione viene passata nel costruttore (pattern dependency injection).
 * Usare try-with-resources per chiudere PreparedStatement e ResultSet.
 */
public class TheKnifeDAO {

    private final Connection conn;

    public TheKnifeDAO(Connection conn) {
        this.conn = conn;
    }


    // =========================================================
    //  AUTENTICAZIONE E REGISTRAZIONE
    // =========================================================

    /**
     * Inserisce un nuovo utente (cliente o gestore).
     * lat_domicilio e lon_domicilio devono essere già geocodificati
     * lato serverTK prima di chiamare questo metodo.
     *
     * @return il numero di righe inserite (1 se ok, 0 se fallito)
     */
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

    /**
     * Recupera id_utente, password_hash e ruolo dato l'indirizzo email.
     * Il confronto bcrypt va fatto lato serverTK dopo aver ottenuto l'hash.
     *
     * @return ResultSet con colonne: id_utente, password_hash, ruolo
     * (null se nessun utente trovato)
     */
    public ResultSet login(String email) throws SQLException {

        String sql = """
                SELECT id_utente, password_hash, ruolo
                  FROM Utenti
                 WHERE email = ?
                """;

        // NB: il chiamante è responsabile di chiudere il ResultSet e lo statement.
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        return ps.executeQuery();
    }


    // =========================================================
    //  FUNZIONALITÀ SENZA LOGIN
    // =========================================================

    /**
     * Cerca ristoranti applicando filtri opzionali.
     * Costruisce la WHERE clause dinamicamente: i parametri null vengono ignorati.
     *
     * @param citta              obbligatorio (o nazione - adattare se necessario)
     * @param tipoCucina         opzionale, null per ignorare
     * @param prezzoMin          opzionale, null per ignorare
     * @param prezzoMax          opzionale, null per ignorare
     * @param delivery           opzionale, null per ignorare
     * @param prenotazioneOnline opzionale, null per ignorare
     * @param minStelle          opzionale, null per ignorare (HAVING clause)
     */
    public ResultSet cercaRistorante(String citta, String tipoCucina,
                                     Integer prezzoMin, Integer prezzoMax,
                                     Boolean delivery, Boolean prenotazioneOnline,
                                     Double minStelle) throws SQLException {

        // RISOLUZIONE: Aggiunto il cast esplicito ?::text per permettere a PostgreSQL di determinare il tipo di dato
        StringBuilder sql = new StringBuilder("""
            SELECT r.*,
                   COALESCE(AVG(rec.stelle), 0) AS media_stelle,
                   COUNT(rec.id_recensione)      AS num_recensioni
              FROM RistorantiTheKnife r
              LEFT JOIN Recensioni rec ON rec.id_ristorante = r.id_ristorante
             WHERE (?::text IS NULL OR ?::text = '' OR r.citta = ?)
            """);

        List<Object> params = new ArrayList<>();
        params.add(citta);
        params.add(citta);
        params.add(citta);

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

    /**
     * Recensioni di un ristorante con eventuale risposta del gestore.
     * Visibili anche agli utenti non registrati (dati autore non esposti).
     */
    public ResultSet visualizzaRecensioni(int idRistorante) throws SQLException {

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
        ps.setInt(1, idRistorante);
        return ps.executeQuery();
    }

    /**
     * Recupera le coordinate del domicilio di un utente loggato.
     * Passo 1 per calcolare i ristoranti vicini (utente loggato).
     *
     * @return ResultSet con colonne: lat_domicilio, lon_domicilio
     */
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

    /**
     * Ristoranti vicini ordinati per distanza euclidea approssimata.
     * Passo 2, identico per utente guest e utente loggato.
     * Per distanze su scala globale usare PostGIS + ST_DWithin.
     *
     * @param lat latitudine del punto di riferimento
     * @param lon longitudine del punto di riferimento
     */
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
    //  FUNZIONALITÀ CLIENTI  (login richiesto)
    // =========================================================

    /**
     * Aggiunge un ristorante ai preferiti dell'utente.
     * ON CONFLICT DO NOTHING rende l'operazione idempotente.
     *
     * @return 1 se inserito, 0 se era già presente
     */
    public int aggiungiPreferito(int idUtente, int idRistorante) throws SQLException {

        String sql = """
                INSERT INTO Preferiti (id_utente, id_ristorante)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            ps.setInt(2, idRistorante);
            return ps.executeUpdate();
        }
    }

    /**
     * Rimuove un ristorante dai preferiti dell'utente.
     *
     * @return 1 se rimosso, 0 se non era presente
     */
    public int rimuoviPreferito(int idUtente, int idRistorante) throws SQLException {

        String sql = """
                DELETE FROM Preferiti
                 WHERE id_utente = ? AND id_ristorante = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            ps.setInt(2, idRistorante);
            return ps.executeUpdate();
        }
    }

    /**
     * Lista dei ristoranti preferiti del cliente con media stelle e conteggio recensioni.
     */
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

    /**
     * Inserisce una nuova recensione.
     *
     * @return 1 se inserita correttamente
     */
    public int aggiungiRecensione(int idRistorante, int idUtente,
                                  int stelle, String testo) throws SQLException {

        String sql = """
                INSERT INTO Recensioni (id_ristorante, id_utente, stelle, testo)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRistorante);
            ps.setInt(2, idUtente);
            ps.setInt(3, stelle);
            ps.setString(4, testo);
            return ps.executeUpdate();
        }
    }

    /**
     * Modifica una recensione esistente.
     * Il WHERE su id_utente impedisce che un cliente modifichi le recensioni altrui.
     *
     * @return 1 se aggiornata, 0 se la recensione non esiste o non appartiene all'utente
     */
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

    /**
     * Elimina una recensione.
     * Il WHERE su id_utente impedisce che un cliente elimini le recensioni altrui.
     *
     * @return 1 se eliminata, 0 se la recensione non esiste o non appartiene all'utente
     */
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

    /**
     * Le recensioni scritte dal cliente con ristorante e risposta del gestore.
     * Usata nella schermata "Le mie recensioni".
     */
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
    //  FUNZIONALITÀ GESTORI  (login richiesto)
    // =========================================================

    /**
     * Inserisce un nuovo ristorante.
     * Le coordinate (latitudine, longitudine) devono essere già geocodificate
     * lato serverTK prima di chiamare questo metodo.
     *
     * @return 1 se inserito correttamente
     */
    public int aggiungiRistorante(String nome, String nazione, String citta,
                                  String indirizzo, double latitudine, double longitudine,
                                  int fasciaPrezzo, boolean delivery,
                                  boolean prenotazioneOnline, String tipoCucina,
                                  int idGestore) throws SQLException {

        String sql = """
                INSERT INTO RistorantiTheKnife
                    (nome, nazione, citta, indirizzo, latitudine, longitudine,
                     fascia_prezzo, delivery, prenotazione_online, tipo_cucina, id_gestore)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, nazione);
            ps.setString(3, citta);
            ps.setString(4, indirizzo);
            ps.setDouble(5, latitudine);
            ps.setDouble(6, longitudine);
            ps.setInt(7, fasciaPrezzo);
            ps.setBoolean(8, delivery);
            ps.setBoolean(9, prenotazioneOnline);
            ps.setString(10, tipoCucina);
            ps.setInt(11, idGestore);
            return ps.executeUpdate();
        }
    }

    /**
     * Riepilogo dei ristoranti del gestore con media stelle e numero recensioni.
     * Usata nella schermata "Il mio riepilogo".
     */
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

    /**
     * Recensioni di tutti i ristoranti del gestore, con indicazione se ha già risposto.
     * Usata nella schermata "Gestisci recensioni" lato gestore.
     */
    public ResultSet visualizzaRecensioniGestore(int idGestore) throws SQLException {

        String sql = """
                SELECT r.nome           AS nome_ristorante,
                       rec.id_recensione,
                       rec.stelle,
                       rec.testo,
                       rec.data_recensione,
                       CASE WHEN risp.id_risposta IS NOT NULL
                            THEN TRUE ELSE FALSE END  AS gia_risposto,
                       risp.testo                     AS risposta,
                       risp.data_risposta
                  FROM RistorantiTheKnife r
                  JOIN Recensioni rec ON rec.id_ristorante = r.id_ristorante
                  LEFT JOIN RisposteRecensioni risp ON risp.id_recensione = rec.id_recensione
                 WHERE r.id_gestore = ?
                 ORDER BY r.nome, rec.data_recensione DESC
                """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idGestore);
        return ps.executeQuery();
    }

    /**
     * Inserisce una risposta a una recensione.
     * Il UNIQUE su id_recensione in DB impedisce una seconda risposta.
     * Il trigger tr_check_gestore_risposta verifica la proprietà del ristorante.
     *
     * @return 1 se inserita correttamente
     * @throws SQLException se si tenta di rispondere due volte (violazione UNIQUE)
     *                      o se il gestore non è il proprietario del ristorante
     */
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
    //  GESTIONE E MODIFICA PROFILO UTENTE
    // =========================================================

    /**
     * Recupera tutti i dati di un utente specifico tramite il suo ID
     * per poterli mostrare nella pagina del profilo.
     *
     * @param idUtente ID dell'utente loggato
     * @return ResultSet contenente i dati dell'utente
     * @throws SQLException in caso di errore della query
     */
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

    /**
     * Aggiorna i dati del profilo utente SENZA modificare la password.
     *
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    /**
     * Aggiorna i dati del profilo utente SENZA modificare la password.
     * Incorpora anche le coordinate geografiche aggiornate del domicilio.
     *
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
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

    /**
     * Aggiorna i dati del profilo utente INCLUSA la nuova password hashata.
     *
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
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

    /**
     * Recupera i ristoranti preferiti di un determinato utente cliente.
     */
    public ResultSet getPreferitiUtente(int idUtente) throws SQLException {
        String sql = """
            SELECT r.id_ristorante, r.nome, r.citta, r.cucina, r.prezzo
              FROM Preferiti p
              JOIN RistorantiTheKnife r ON p.id_ristorante = r.id_ristorante
             WHERE p.id_utente = ?
            """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idUtente);
        return ps.executeQuery();
    }

    /**
     * Recupera le recensioni scritte da un determinato utente cliente.
     */
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

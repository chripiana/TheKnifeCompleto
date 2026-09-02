package project.server;

import db.TheKnifeDAO;
import db.DatabaseManager;
import project.server.services.UserProfileService;
import project.server.services.SearchCache;
import project.shared.logging.AppLogger;
import project.shared.protocol.ProtocolMessage;
import project.shared.security.PasswordSecurity;
import project.shared.security.SessionTokenService;
import project.shared.validation.RequestValidator;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestisce una singola connessione client
 * Elabora le richieste e restituisce risposte dal database
 */
public class ClientHandler implements Runnable {
    private static final Map<String, Integer> LOGIN_FAILURES = new ConcurrentHashMap<>();
    private static final Map<String, Instant> LOCKED_UNTIL = new ConcurrentHashMap<>();
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_SECONDS = 600;

    private final Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String clientId = socket.getInetAddress() != null ? socket.getInetAddress().getHostAddress() : "unknown";
        ServerStatusRegistry.addClient(clientId);

        try {
            socket.setSoTimeout(30000);
            reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );
            writer = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                true
            );

            String request;
            while ((request = reader.readLine()) != null) {
                AppLogger.info("Handler", "Richiesta ricevuta da " + clientId + ": " + request);

                String response = processRequest(request);
                writer.println(response);
                writer.flush();
            }
        } catch (IOException e) {
            AppLogger.error("Handler", "Errore comunicazione con " + clientId, e);
        } finally {
            try {
                ServerStatusRegistry.removeClient(clientId);
                socket.close();
                AppLogger.info("Handler", "Client disconnesso: " + clientId);
            } catch (IOException e) {
                AppLogger.error("Handler", "Errore chiusura socket: " + clientId, e);
            }
        }
    }

    /**
     * Elabora una richiesta e ritorna una risposta
     * Le richieste sono nel formato: "COMANDO:parametri"
     */
    // Thread-local to expose raw request to handlers for token extraction when needed
    private static final ThreadLocal<String> RAW_REQUEST = new ThreadLocal<>();

    private String processRequest(String request) {
        RAW_REQUEST.set(request);
        try {
            if (!RequestValidator.isValidRequest(request)) {
                return "ERROR:Richiesta non valida";
            }

            String requestWithoutToken = stripSessionToken(request);
            String[] parts = requestWithoutToken.split(":", 2);
            String command = parts[0].trim().toUpperCase();
            String normalizedRequest = requestWithoutToken;

            if (!isPublicCommand(command) && !hasValidSession(request)) {
                return "ERROR:Sessione non valida o scaduta";
            }

            // Usa try-with-resources per chiudere automaticamente la connessione
            try (TheKnifeDAO dao = new TheKnifeDAO(DatabaseManager.getConnection())) {
                switch (command) {
                    case "PING":
                        return "PONG:Server OK";

                    case "LOGIN":
                        return handleLogin(parts, dao);

                    case "REGISTER":
                        return handleRegister(parts, dao, normalizedRequest);

                    case "SEARCH_RISTORANTI":
                        return handleSearchRistoranti(parts, dao, normalizedRequest);

                    case "GET_USER_DATA":
                        return handleGetUserData(parts, dao);

                    case "IS_PREFERITO":
                        return handleIsPreferito(parts, dao);

                    case "ADD_PREFERITO":
                        return handleAddPreferito(parts, dao);

                    case "REMOVE_PREFERITO":
                        return handleRemovePreferito(parts, dao);

                    case "ADD_REVIEW":
                        return handleAddReview(parts, dao, normalizedRequest);

                    case "GET_REVIEW_STATS":
                        return handleGetReviewStats(parts, dao);

                    case "GET_STAR_DISTRIBUTION":
                        return handleGetStarDistribution(parts, dao);

                    case "GET_PREFERITI_UTENTE":
                        return handleGetPreferitiUtente(parts, dao);

                    case "GET_RISTORANTE_DETAILS":
                        return handleGetRistoranteDetails(parts, dao);

                    case "GET_USER_REVIEWS":
                        return handleGetUserReviews(parts, dao);

                    case "DELETE_REVIEW":
                        return handleDeleteReview(parts, dao);
         
                    case "MODIFY_REVIEW":
                        return handleModifyReview(parts, dao, normalizedRequest);

                    case "CREATE_RESERVATION":
                        return handleCreateReservation(normalizedRequest, dao);

                    case "GET_USER_RESERVATIONS":
                        return handleGetUserReservations(normalizedRequest, dao);

                    case "UPDATE_RESERVATION":
                        return handleUpdateReservation(normalizedRequest, dao);

                    case "DELETE_RESERVATION":
                        return handleDeleteReservation(normalizedRequest, dao);
         
                    case "GET_USER_PROFILE":
                        return handleGetUserProfile(parts, dao);

                    case "UPDATE_USER_PROFILE":
                        return handleUpdateUserProfile(parts, dao, normalizedRequest);

                    case "CHANGE_PASSWORD":
                        return handleChangePassword(parts, dao, normalizedRequest);

                    case "GET_OWNER_RESTAURANT":
                        return handleGetOwnerRestaurant(parts, dao);

                    case "UPDATE_RESTAURANT":
                        return handleUpdateRestaurant(parts, dao, normalizedRequest);

                    case "DELETE_RESTAURANT":
                        return handleDeleteRestaurant(parts, dao);

                    case "GET_RECIPES":
                        return handleGetRecipes(parts, dao);

                    case "GET_RESTAURANT_REVIEWS":
                        return handleGetRestaurantReviews(parts, dao);

                    case "GET_FAVORITE_COUNT":
                        return handleGetFavoriteCount(parts, dao);

                    case "GET_RECENT_REVIEWS":
                        return handleGetRecentReviews(parts, dao, normalizedRequest);

                    case "GET_REVIEW_TRENDS":
                        return handleGetReviewTrends(parts, dao);

                    case "CREATE_RESTAURANT":
                        return handleCreateRestaurant(parts, dao, normalizedRequest);

                    default:
                        return "ERROR:Comando sconosciuto: " + command;
                }
            }
        } catch (Exception e) {
            System.err.println("[Handler] Errore elaborazione: " + e.getMessage());
            e.printStackTrace();
            return "ERROR:" + e.getMessage();
        }
    }

    private static boolean isPublicCommand(String command) {
       return switch (command) {
           case "PING", "LOGIN", "REGISTER", "SEARCH_RISTORANTI", "GET_RISTORANTE_DETAILS",
                "GET_RESTAURANT_REVIEWS", "GET_REVIEW_STATS", "GET_STAR_DISTRIBUTION", "GET_RECIPES", "GET_RECENT_REVIEWS", "GET_REVIEW_TRENDS" -> true;
           default -> false;
       };
    }

    private static String stripSessionToken(String request) {
       if (request == null || !request.contains(":TOKEN:")) {
           return request;
       }
       int index = request.lastIndexOf(":TOKEN:");
       if (index < 0) {
           return request;
       }
       return request.substring(0, index);
    }

    private static String extractSessionToken(String request) {
       if (request == null || !request.contains(":TOKEN:")) {
           // fallback to thread-local raw request if available
           String raw = RAW_REQUEST.get();
           if (raw == null || !raw.contains(":TOKEN:")) return null;
           request = raw;
       }
       int index = request.lastIndexOf(":TOKEN:");
       if (index < 0) {
           return null;
       }
       return request.substring(index + ":TOKEN:".length()).trim();
    }

    private static boolean hasValidSession(String request) {
       String token = extractSessionToken(request);
      AppLogger.info("Handler", "hasValidSession check: token=" + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "NULL"));
      boolean valid = token != null && SessionTokenService.isValid(token);
      AppLogger.info("Handler", "  Valid result: " + valid);
      return valid;
    }

    private static boolean userMatchesToken(int expectedUserId) {
        String token = extractSessionToken(null);
        if (token == null) return false;
        return SessionTokenService.isValid(token, expectedUserId);
    }

    private static boolean isLoginLocked(String email) {
       String key = email.trim().toLowerCase();
       Instant lockUntil = LOCKED_UNTIL.get(key);
       if (lockUntil != null && lockUntil.isAfter(Instant.now())) {
           return true;
       }
       if (lockUntil != null) {
           LOCKED_UNTIL.remove(key);
       }
       return false;
    }

    private static void registerFailedLogin(String email) {
       String key = email.trim().toLowerCase();
       int attempts = LOGIN_FAILURES.getOrDefault(key, 0) + 1;
       LOGIN_FAILURES.put(key, attempts);
       if (attempts >= MAX_LOGIN_ATTEMPTS) {
           LOCKED_UNTIL.put(key, Instant.now().plusSeconds(LOCKOUT_SECONDS));
           LOGIN_FAILURES.remove(key);
       }
    }

    private static void clearFailedLogin(String email) {
       String key = email.trim().toLowerCase();
       LOGIN_FAILURES.remove(key);
       LOCKED_UNTIL.remove(key);
    }

    /**
     * Gestisce la richiesta LOGIN
     * Formato: LOGIN:email:password
     */
    private String handleLogin(String[] parts, TheKnifeDAO dao) {
       try {
           // Support both: parts split into [LOGIN, "email:password"] or older [LOGIN, email, password]
           String email;
           String password;
           if (parts.length >= 3) {
               email = parts[1].trim();
               password = parts[2].trim();
           } else if (parts.length == 2) {
               String[] fields = parts[1].split(":", -1);
               if (fields.length < 2 || fields[0].isEmpty() || fields[1].isEmpty()) {
                   return "LOGIN_FAIL:Credenziali mancanti";
               }
               email = fields[0].trim();
               password = fields[1].trim();
           } else {
               return "LOGIN_FAIL:Credenziali mancanti";
           }

           if (!RequestValidator.isValidEmail(email)) {
               return "LOGIN_FAIL:Email non valida";
           }

           if (isLoginLocked(email)) {
               return "LOGIN_FAIL:Troppi tentativi. Riprova tra 10 minuti.";
           }

           System.out.println("[Handler] LOGIN: " + email);
           java.util.Map<String,Object> userMap = dao.getLoginData(email);

           if (userMap == null || userMap.isEmpty()) {
               registerFailedLogin(email);
               System.out.println("[Handler] LOGIN_FAIL: Email non trovata");
               return "LOGIN_FAIL:Email non trovata";
           }

           String storedHash = userMap.get("password_hash") == null ? "" : userMap.get("password_hash").toString();
           boolean passwordOk = PasswordSecurity.verifyPassword(password, storedHash);

           if (!passwordOk) {
               registerFailedLogin(email);
               System.out.println("[Handler] LOGIN_FAIL: Password non corretta");
               return "LOGIN_FAIL:Credenziali non valide";
           }

           clearFailedLogin(email);

           if (PasswordSecurity.isLegacyPlaintext(storedHash)) {
               String migratedHash = PasswordSecurity.hashPassword(password);
               String updateSql = "UPDATE Utenti SET password_hash = ? WHERE email = ?";
               try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(updateSql)) {
                   ps.setString(1, migratedHash);
                   ps.setString(2, email);
                   ps.executeUpdate();
               }
           }

           int userId = userMap.get("id_utente") instanceof Number ? ((Number)userMap.get("id_utente")).intValue() : Integer.parseInt(userMap.get("id_utente").toString());
           String ruolo = userMap.get("ruolo") == null ? "" : userMap.get("ruolo").toString();
           String sessionToken = SessionTokenService.generateToken();
           SessionTokenService.register(sessionToken, userId);
           String clientId = socket.getInetAddress() != null ? socket.getInetAddress().getHostAddress() : "unknown";
           ServerStatusRegistry.updateClient(clientId, userId, ruolo);
           AppLogger.info("Handler", "LOGIN_OK: userId=" + userId + " token=" + sessionToken.substring(0, Math.min(20, sessionToken.length())) + "...");
           return "LOGIN_OK:" + userId + ":" + ruolo + ":" + sessionToken;
       } catch (SQLException e) {
           AppLogger.error("Handler", "LOGIN - SQLException: " + e.getMessage(), e);
           return "LOGIN_FAIL:Errore database";
       }
    }

    /**
     * Gestisce la richiesta REGISTER
     * Formato: REGISTER:nome:cognome:email:password:dataNascita:luogo:lat:lon:ruolo
     */
    private String handleRegister(String[] parts, TheKnifeDAO dao, String request) {
        try {
            String params = request.substring("REGISTER:".length());
            String[] data = params.split(":", -1);

            if (data.length < 9) {
                return "REGISTER_FAIL:Parametri insufficienti";
            }

            String nome = data[0].trim();
            String cognome = data[1].trim();
            String email = data[2].trim();
            String password = data[3].trim();
            String dataNascita = data[4].trim();
            String luogo = data[5].trim();
            String lat = data[6].trim();
            String lon = data[7].trim();
            String ruolo = data[8].trim();
            // Normalize role to lowercase to satisfy DB check constraint (ck_ruolo expects 'cliente' or 'gestore')
            ruolo = ruolo == null ? "cliente" : ruolo.toLowerCase();
            if (!"cliente".equals(ruolo) && !"gestore".equals(ruolo)) {
                ruolo = "cliente"; // default to cliente for unknown values
            }

            // Validazione input
            if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() || password.isEmpty()) {
                return "REGISTER_FAIL:Campi obbligatori mancanti";
            }

            if (!RequestValidator.isValidEmail(email)) {
                return "REGISTER_FAIL:Email non valida";
            }

            if (!RequestValidator.isValidPassword(password)) {
                return "REGISTER_FAIL:Password deve contenere almeno 7 caratteri, una maiuscola, una minuscola e un numero";
            }

            double latitude, longitude;
            try {
                latitude = Double.parseDouble(lat);
                longitude = Double.parseDouble(lon);
            } catch (NumberFormatException e) {
                latitude = 0.0;
                longitude = 0.0;
            }

            double[] normalized = UserProfileService.normalizeCoordinates(luogo, latitude, longitude);
            latitude = normalized[0];
            longitude = normalized[1];

            // Converte dataNascita
            java.sql.Date sqlDate;
            try {
                java.time.LocalDate localDate = java.time.LocalDate.parse(dataNascita);
                if (!localDate.isBefore(java.time.LocalDate.now())) {
                    return "REGISTER_FAIL:Data di nascita non valida: deve essere precedente ad oggi";
                }
                sqlDate = java.sql.Date.valueOf(localDate);
            } catch (Exception e) {
                return "REGISTER_FAIL:Data di nascita non valida (formato: YYYY-MM-DD)";
            }

            String passwordHash = PasswordSecurity.hashPassword(password);
            System.out.println("[Handler] REGISTER: " + email);
            int result = dao.registrazione(nome, cognome, email, passwordHash, sqlDate, luogo, latitude, longitude, ruolo);

            if (result > 0) {
                System.out.println("[Handler] REGISTER_OK: userId=" + result);
                return "REGISTER_OK:Registrazione completata:" + result;
            } else {
                System.out.println("[Handler] REGISTER_FAIL");
                return "REGISTER_FAIL:Email già registrata o errore database";
            }
        } catch (SQLException e) {
            AppLogger.error("Handler", "REGISTER - SQLException: " + e.getMessage(), e);
            return "REGISTER_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta SEARCH_RISTORANTI
     * Formato: SEARCH_RISTORANTI:citta:tipoCucina:prezzoMin:prezzoMax:delivery:prenotazioneOnline:minStelle
     */
    private String handleSearchRistoranti(String[] parts, TheKnifeDAO dao, String request) {
        try {
            String params = request.substring("SEARCH_RISTORANTI:".length()).trim();

            if (params.isEmpty()) {
                return "SEARCH_RISTORANTI_FAIL:Parametri mancanti";
            }

            String[] searchParams = params.split(":", -1);

            AppLogger.info("Handler", "SEARCH_RISTORANTI request: " + params);
            AppLogger.info("Handler", "  Params split count: " + searchParams.length);

            String citta = searchParams.length > 0 ? searchParams[0].trim() : null;
            String tipoCucina = searchParams.length > 1 ? searchParams[1].trim() : null;
            Integer prezzoMin = null;
            Integer prezzoMax = null;
            Boolean delivery = null;
            Boolean prenotazioneOnline = null;
            Double minStelle = null;
            Double nearbyLat = null;
            Double nearbyLon = null;
            Double radiusKm = null;

            try {
                if (searchParams.length > 2 && !searchParams[2].isEmpty()) {
                    prezzoMin = Integer.parseInt(searchParams[2]);
                }
                if (searchParams.length > 3 && !searchParams[3].isEmpty()) {
                    prezzoMax = Integer.parseInt(searchParams[3]);
                }

                if (searchParams.length > 4 && !searchParams[4].isEmpty()) {
                    delivery = Boolean.parseBoolean(searchParams[4]);
                }
                if (searchParams.length > 5 && !searchParams[5].isEmpty()) {
                    prenotazioneOnline = Boolean.parseBoolean(searchParams[5]);
                }

                // Support the full search format and the legacy geo-search compact format.
                // Full format: city:type:prezzoMin:prezzoMax:delivery:prenotazione:minStelle:lat:lon:radius
                // Geo compact: city:type:prezzoMin:prezzoMax::::lat:lon:radius
                if (searchParams.length >= 10) {
                    if (searchParams[6] != null && !searchParams[6].isEmpty()) {
                        minStelle = Double.parseDouble(searchParams[6]);
                    }
                    if (searchParams[7] != null && !searchParams[7].isEmpty()) {
                        nearbyLat = Double.parseDouble(searchParams[7]);
                    }
                    if (searchParams[8] != null && !searchParams[8].isEmpty()) {
                        nearbyLon = Double.parseDouble(searchParams[8]);
                    }
                    if (searchParams[9] != null && !searchParams[9].isEmpty()) {
                        radiusKm = Double.parseDouble(searchParams[9]);
                    }
                } else if (searchParams.length >= 9) {
                    if (searchParams[6] != null && !searchParams[6].isEmpty()) {
                        nearbyLat = Double.parseDouble(searchParams[6]);
                    }
                    if (searchParams[7] != null && !searchParams[7].isEmpty()) {
                        nearbyLon = Double.parseDouble(searchParams[7]);
                    }
                    if (searchParams[8] != null && !searchParams[8].isEmpty()) {
                        radiusKm = Double.parseDouble(searchParams[8]);
                    }
                }
            } catch (NumberFormatException e) {
                AppLogger.error("Handler", "SEARCH_RISTORANTI parse error: " + e.getMessage(), e);
                return "SEARCH_RISTORANTI_FAIL:Parametri numerici non validi";
            }

            AppLogger.info("Handler", "Parsed SEARCH params: citta=" + citta + " tipoCucina=" + tipoCucina + 
                           " prezzoMin=" + prezzoMin + " prezzoMax=" + prezzoMax + 
                           " delivery=" + delivery + " prenotazione=" + prenotazioneOnline +
                           " minStelle=" + minStelle + " lat=" + nearbyLat + " lon=" + nearbyLon + " radius=" + radiusKm);

            java.util.List<java.util.Map<String,Object>> rows;

            // Build cache key from search parameters
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append("c=").append(citta).append("|");
            keyBuilder.append("tc=").append(tipoCucina).append("|");
            keyBuilder.append("pmin=").append(prezzoMin).append("|");
            keyBuilder.append("pmax=").append(prezzoMax).append("|");
            keyBuilder.append("d=").append(delivery).append("|");
            keyBuilder.append("po=").append(prenotazioneOnline).append("|");
            keyBuilder.append("ms=").append(minStelle).append("|");
            keyBuilder.append("lat=").append(nearbyLat).append("|");
            keyBuilder.append("lon=").append(nearbyLon).append("|");
            keyBuilder.append("r=").append(radiusKm);
            String cacheKey = keyBuilder.toString();

            // Try cache
            java.util.List<java.util.Map<String,Object>> cached = SearchCache.get(cacheKey);
            if (cached != null) {
                AppLogger.info("Handler", "Cache hit for search key=" + cacheKey + ", rows=" + cached.size());
                String resultCached = serializeRows(cached);
                return "SEARCH_RISTORANTI_OK:" + resultCached;
            }

            if (nearbyLat != null && nearbyLon != null && radiusKm != null && radiusKm > 0) {
                // Prefer PostGIS fast path when available
                if (DatabaseManager.isPostGisAvailable()) {
                    rows = dao.cercaRistorantiViciniPostGis(nearbyLat, nearbyLon, radiusKm, citta, tipoCucina, prezzoMax, minStelle);
                } else {
                    rows = dao.cercaRistorantiVicini(nearbyLat, nearbyLon, radiusKm, citta, tipoCucina, prezzoMax, minStelle);
                }
            } else {
                rows = dao.cercaRistorante(citta, tipoCucina, prezzoMin, prezzoMax, delivery, prenotazioneOnline, minStelle);
            }

            // Store in cache with default TTL
            try {
                SearchCache.put(cacheKey, rows);
            } catch (Exception e) {
                AppLogger.info("Handler", "Failed to put search result into cache: " + e.getMessage());
            }

            String result = serializeRows(rows);
            return "SEARCH_RISTORANTI_OK:" + result;
        } catch (SQLException e) {
            AppLogger.error("Handler", "SEARCH_RISTORANTI - SQLException: " + e.getMessage(), e);
            return "SEARCH_RISTORANTI_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_USER_DATA
     * Formato: GET_USER_DATA:userId
     */
    private String handleGetUserData(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2 || parts[1].isEmpty()) {
                return "GET_USER_DATA_FAIL:ID utente obbligatorio";
            }

            String userIdStr = parts[1].trim();
            int userId;

            try {
                userId = Integer.parseInt(userIdStr);
            } catch (NumberFormatException e) {
                return "GET_USER_DATA_FAIL:ID utente non valido";
            }

            if (userId <= 0) {
                return "GET_USER_DATA_FAIL:ID utente deve essere positivo";
            }

            System.out.println("[Handler] GET_USER_DATA: userId=" + userId);

            // Ottieni dati utente tramite DAO che restituisce Map per evitare ResultSet aperti
            java.util.Map<String,Object> user = dao.getDatiUtenteMap(userId);
            if (user != null && !user.isEmpty()) {
                String userData = serializeRows(java.util.List.of(user));
                System.out.println("[Handler] GET_USER_DATA_OK");
                return "GET_USER_DATA_OK:" + userData;
            } else {
                System.out.println("[Handler] GET_USER_DATA_FAIL: Utente non trovato");
                return "GET_USER_DATA_FAIL:Utente non trovato";
            }
        } catch (SQLException e) {
            System.err.println("[Handler] GET_USER_DATA - SQLException: " + e.getMessage());
            return "GET_USER_DATA_FAIL:Errore database";
        }
    }

    /**
     * Serializza un ResultSet in formato semplice
     * Formato: colonna1=valore1|colonna2=valore2
     */
    private String serializeResultSet(ResultSet rs) throws SQLException {
        if (rs == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int rowCount = 0;

        java.sql.Statement stmt = null;
        java.sql.Connection conn = null;

        try {
            while (rs.next()) {
                if (rowCount > 0) sb.append(";");
                
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) sb.append("|");
                    
                    String columnName = rs.getMetaData().getColumnName(i);
                    String value = rs.getObject(i) != null ? rs.getObject(i).toString() : "";
                    sb.append(columnName).append("=").append(value);
                }
                rowCount++;
            }
            return sb.toString();
        } finally {
            // Close resources and return connection to pool if any
            try {
                if (rs != null) {
                    stmt = rs.getStatement();
                    rs.close();
                }
            } catch (Exception ignored) {}
            try {
                if (stmt != null) {
                    conn = stmt.getConnection();
                    stmt.close();
                }
            } catch (Exception ignored) {}
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (Exception ignored) {}
        }
    }

    private String serializeRows(java.util.List<java.util.Map<String,Object>> rows) {
        if (rows == null || rows.isEmpty()) return "";
        StringBuilder sb2 = new StringBuilder();
        int r = 0;
        for (java.util.Map<String,Object> row : rows) {
            if (r > 0) sb2.append(";");
            int c = 0;
            for (java.util.Map.Entry<String,Object> e : row.entrySet()) {
                if (c > 0) sb2.append("|");
                sb2.append(e.getKey()).append("=").append(e.getValue() == null ? "" : e.getValue().toString());
                c++;
            }
            r++;
        }
        return sb2.toString();
    }

    /**
     * Gestisce la richiesta IS_PREFERITO
     * Formato: IS_PREFERITO:idUtente:idRistorante
     */
    private String handleIsPreferito(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2) {
                return "IS_PREFERITO_FAIL:Parametri insufficienti";
            }

            String params = parts[1].trim();
            String[] data = params.split(":");

            if (data.length < 2) {
                return "IS_PREFERITO_FAIL:Parametri insufficienti";
            }

            int idUtente;
            String idRistorante;

            try {
                idUtente = Integer.parseInt(data[0]);
            } catch (NumberFormatException e) {
                return "IS_PREFERITO_FAIL:ID utente non valido";
            }

            idRistorante = data[1].trim();

            if (idUtente <= 0 || idRistorante.isEmpty()) {
                return "IS_PREFERITO_FAIL:ID utente o ristorante non validi";
            }

            System.out.println("[Handler] IS_PREFERITO: userId=" + idUtente + ", ristoId=" + idRistorante);
            boolean isPreferito = dao.isPreferito(idUtente, idRistorante);
            return "IS_PREFERITO_OK:" + (isPreferito ? "true" : "false");

        } catch (SQLException e) {
            System.err.println("[Handler] IS_PREFERITO - SQLException: " + e.getMessage());
            return "IS_PREFERITO_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta ADD_PREFERITO
     * Formato: ADD_PREFERITO:idUtente:idRistorante
     */
    private String handleAddPreferito(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2) {
                return "ADD_PREFERITO_FAIL:Parametri insufficienti";
            }

            String params = parts[1].trim();
            String[] data = params.split(":");

            if (data.length < 2) {
                return "ADD_PREFERITO_FAIL:Parametri insufficienti";
            }

            int idUtente;
            String idRistorante;

            try {
                idUtente = Integer.parseInt(data[0]);
            } catch (NumberFormatException e) {
                return "ADD_PREFERITO_FAIL:ID utente non valido";
            }

            idRistorante = data[1].trim();

            if (idUtente <= 0 || idRistorante.isEmpty()) {
                return "ADD_PREFERITO_FAIL:ID utente o ristorante non validi";
            }

            // Verify that session token corresponds to the user performing the action
            if (!userMatchesToken(idUtente)) {
                System.err.println("[Handler] ADD_PREFERITO - token mismatch for userId=" + idUtente);
                return "ERROR:Sessione non valida o non autorizzata";
            }

            System.out.println("[Handler] ADD_PREFERITO: userId=" + idUtente + ", ristoId=" + idRistorante);
            dao.aggiungiPreferito(idUtente, idRistorante);
            return "ADD_PREFERITO_OK:Preferito aggiunto";

        } catch (SQLException e) {
            System.err.println("[Handler] ADD_PREFERITO - SQLException: " + e.getMessage());
            return "ADD_PREFERITO_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta REMOVE_PREFERITO
     * Formato: REMOVE_PREFERITO:idUtente:idRistorante
     */
    private String handleRemovePreferito(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2) {
                return "REMOVE_PREFERITO_FAIL:Parametri insufficienti";
            }

            String params = parts[1].trim();
            String[] data = params.split(":");

            if (data.length < 2) {
                return "REMOVE_PREFERITO_FAIL:Parametri insufficienti";
            }

            int idUtente;
            String idRistorante;

            try {
                idUtente = Integer.parseInt(data[0]);
            } catch (NumberFormatException e) {
                return "REMOVE_PREFERITO_FAIL:ID utente non valido";
            }

            idRistorante = data[1].trim();

            if (idUtente <= 0 || idRistorante.isEmpty()) {
                return "REMOVE_PREFERITO_FAIL:ID utente o ristorante non validi";
            }

            // Verify that session token corresponds to the user performing the action
            if (!userMatchesToken(idUtente)) {
                System.err.println("[Handler] REMOVE_PREFERITO - token mismatch for userId=" + idUtente);
                return "ERROR:Sessione non valida o non autorizzata";
            }

            System.out.println("[Handler] REMOVE_PREFERITO: userId=" + idUtente + ", ristoId=" + idRistorante);
            dao.rimuoviPreferito(idUtente, idRistorante);
            return "REMOVE_PREFERITO_OK:Preferito rimosso";

        } catch (SQLException e) {
            System.err.println("[Handler] REMOVE_PREFERITO - SQLException: " + e.getMessage());
            return "REMOVE_PREFERITO_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta ADD_REVIEW
     * Formato: ADD_REVIEW:ristoId:userId:voto:testo
     */
    private String handleAddReview(String[] parts, TheKnifeDAO dao, String request) {
        try {
            String params = request.substring("ADD_REVIEW:".length());
            String[] data = params.split(":", -1);

            if (data.length < 4) {
                return "ADD_REVIEW_FAIL:Parametri insufficienti";
            }

            String ristoId = data[0].trim();
            int userId;
            int voto;
            String testo = data[3].trim();

            try {
                userId = Integer.parseInt(data[1].trim());
                voto = Integer.parseInt(data[2].trim());
            } catch (NumberFormatException e) {
                return "ADD_REVIEW_FAIL:Parametri numerici non validi";
            }

            if (ristoId.isEmpty() || userId <= 0 || voto < 1 || voto > 5) {
                return "ADD_REVIEW_FAIL:Parametri non validi";
            }

            // Verify session token belongs to user
            if (!userMatchesToken(userId)) {
                System.err.println("[Handler] ADD_REVIEW - token mismatch for userId=" + userId);
                return "ERROR:Sessione non valida o non autorizzata";
            }

            System.out.println("[Handler] ADD_REVIEW: ristoId=" + ristoId + ", userId=" + userId + ", voto=" + voto);
            dao.aggiungiRecensione(ristoId, userId, voto, testo);
            return "ADD_REVIEW_OK:Recensione aggiunta";

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return "ADD_REVIEW_FAIL:Hai già recensito questo ristorante";
            }
            System.err.println("[Handler] ADD_REVIEW - SQLException: " + e.getMessage());
            return "ADD_REVIEW_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_REVIEW_STATS
     * Formato: GET_REVIEW_STATS:ristoId
     */
    private String handleGetReviewStats(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2 || parts[1].isEmpty()) {
                return "GET_REVIEW_STATS_FAIL:ID ristorante obbligatorio";
            }

            String ristoId = parts[1].trim();

            System.out.println("[Handler] GET_REVIEW_STATS: ristoId=" + ristoId);
            java.util.Map<String,Object> stats = dao.getStatisticheRecensioniMap(ristoId);
            String result = "";
            if (stats != null && !stats.isEmpty()) {
                result = serializeRows(java.util.List.of(stats));
            }
            return "GET_REVIEW_STATS_OK:" + result;

        } catch (SQLException e) {
            System.err.println("[Handler] GET_REVIEW_STATS - SQLException: " + e.getMessage());
            return "GET_REVIEW_STATS_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_STAR_DISTRIBUTION
     * Formato: GET_STAR_DISTRIBUTION:ristoId
     */
    private String handleGetStarDistribution(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2 || parts[1].isEmpty()) {
                return "GET_STAR_DISTRIBUTION_FAIL:ID ristorante obbligatorio";
            }

            String ristoId = parts[1].trim();

            System.out.println("[Handler] GET_STAR_DISTRIBUTION: ristoId=" + ristoId);
            java.util.List<java.util.Map<String,Object>> distro = dao.getDistribuzioneStelleList(ristoId);
            String result = serializeRows(distro);
            return "GET_STAR_DISTRIBUTION_OK:" + result;

        } catch (SQLException e) {
            System.err.println("[Handler] GET_STAR_DISTRIBUTION - SQLException: " + e.getMessage());
            return "GET_STAR_DISTRIBUTION_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_PREFERITI_UTENTE
     * Formato: GET_PREFERITI_UTENTE:userId
     */
    private String handleGetPreferitiUtente(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2 || parts[1].isEmpty()) {
                return "GET_PREFERITI_UTENTE_FAIL:ID utente obbligatorio";
            }

            String userIdStr = parts[1].trim();
            int userId;

            try {
                userId = Integer.parseInt(userIdStr);
            } catch (NumberFormatException e) {
                return "GET_PREFERITI_UTENTE_FAIL:ID utente non valido";
            }

            if (userId <= 0) {
                return "GET_PREFERITI_UTENTE_FAIL:ID utente deve essere positivo";
            }

            System.out.println("[Handler] GET_PREFERITI_UTENTE: userId=" + userId);
            java.util.List<java.util.Map<String,Object>> rows = dao.getPreferitiUtenteList(userId);
            String result = serializeRows(rows);
            return "GET_PREFERITI_UTENTE_OK:" + result;

        } catch (SQLException e) {
            System.err.println("[Handler] GET_PREFERITI_UTENTE - SQLException: " + e.getMessage());
            return "GET_PREFERITI_UTENTE_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_RISTORANTE_DETAILS
     * Formato: GET_RISTORANTE_DETAILS:ristoId
     */
    private String handleGetRistoranteDetails(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2 || parts[1].isEmpty()) {
                return "GET_RISTORANTE_DETAILS_FAIL:ID ristorante obbligatorio";
            }

            String ristoId = parts[1].trim();

            System.out.println("[Handler] GET_RISTORANTE_DETAILS: ristoId=" + ristoId);
            java.util.Map<String,Object> r = dao.getRistoranteDetailsMap(ristoId);
            if (r != null && !r.isEmpty()) {
                String result = serializeRows(java.util.List.of(r));
                System.out.println("[Handler] GET_RISTORANTE_DETAILS_OK");
                return "GET_RISTORANTE_DETAILS_OK:" + result;
            } else {
                return "GET_RISTORANTE_DETAILS_FAIL:Ristorante non trovato";
            }
        } catch (SQLException e) {
            System.err.println("[Handler] GET_RISTORANTE_DETAILS - SQLException: " + e.getMessage());
            return "GET_RISTORANTE_DETAILS_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_USER_REVIEWS
     * Formato: GET_USER_REVIEWS:userId
     */
    private String handleGetUserReviews(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2 || parts[1].isEmpty()) {
                return "GET_USER_REVIEWS_FAIL:ID utente obbligatorio";
            }

            String userIdStr = parts[1].trim();
            int userId;

            try {
                userId = Integer.parseInt(userIdStr);
            } catch (NumberFormatException e) {
                return "GET_USER_REVIEWS_FAIL:ID utente non valido";
            }

            System.out.println("[Handler] GET_USER_REVIEWS: userId=" + userId);
            java.util.List<java.util.Map<String,Object>> rows = dao.getRecensioniUtenteList(userId);
            String result = serializeRows(rows);
            return "GET_USER_REVIEWS_OK:" + result;

        } catch (SQLException e) {
            System.err.println("[Handler] GET_USER_REVIEWS - SQLException: " + e.getMessage());
            return "GET_USER_REVIEWS_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta DELETE_REVIEW
     * Formato: DELETE_REVIEW:reviewId:userId
     */
    private String handleDeleteReview(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2) {
                return "DELETE_REVIEW_FAIL:Parametri insufficienti";
            }

            String params = parts[1].trim();
            String[] data = params.split(":");

            if (data.length < 2) {
                return "DELETE_REVIEW_FAIL:Parametri insufficienti";
            }

            int reviewId, userId;

            try {
                reviewId = Integer.parseInt(data[0].trim());
                userId = Integer.parseInt(data[1].trim());
            } catch (NumberFormatException e) {
                return "DELETE_REVIEW_FAIL:ID non valido";
            }

            // Verify session token
            if (!userMatchesToken(userId)) {
                System.err.println("[Handler] DELETE_REVIEW - token mismatch for userId=" + userId);
                return "ERROR:Sessione non valida o non autorizzata";
            }

            System.out.println("[Handler] DELETE_REVIEW: reviewId=" + reviewId + ", userId=" + userId);
            int rowsDeleted = dao.eliminaRecensione(reviewId, userId);

            if (rowsDeleted > 0) {
                return "DELETE_REVIEW_OK:Recensione eliminata";
            } else {
                return "DELETE_REVIEW_FAIL:Impossibile eliminare la recensione";
            }

        } catch (SQLException e) {
            System.err.println("[Handler] DELETE_REVIEW - SQLException: " + e.getMessage());
            return "DELETE_REVIEW_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta MODIFY_REVIEW
     * Formato: MODIFY_REVIEW:reviewId:userId:stars:text
     */
    private String handleModifyReview(String[] parts, TheKnifeDAO dao, String request) {
        try {
            String params = request.substring("MODIFY_REVIEW:".length());
            String[] data = params.split(":", -1);

            if (data.length < 4) {
                return "MODIFY_REVIEW_FAIL:Parametri insufficienti";
            }

            int reviewId, userId, stars;
            String text = data[3].trim();

            try {
                reviewId = Integer.parseInt(data[0].trim());
                userId = Integer.parseInt(data[1].trim());
                stars = Integer.parseInt(data[2].trim());
            } catch (NumberFormatException e) {
                return "MODIFY_REVIEW_FAIL:Parametri numerici non validi";
            }

            if (stars < 1 || stars > 5) {
                return "MODIFY_REVIEW_FAIL:Stelle deve essere tra 1 e 5";
            }

            // Verify session token
            if (!userMatchesToken(userId)) {
                System.err.println("[Handler] MODIFY_REVIEW - token mismatch for userId=" + userId);
                return "ERROR:Sessione non valida o non autorizzata";
            }

            System.out.println("[Handler] MODIFY_REVIEW: reviewId=" + reviewId + ", userId=" + userId);
            int rowsModified = dao.modificaRecensione(reviewId, userId, stars, text);

            if (rowsModified > 0) {
                return "MODIFY_REVIEW_OK:Recensione modificata";
            } else {
                return "MODIFY_REVIEW_FAIL:Impossibile modificare la recensione";
            }

        } catch (SQLException e) {
            System.err.println("[Handler] MODIFY_REVIEW - SQLException: " + e.getMessage());
            return "MODIFY_REVIEW_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_USER_PROFILE
     * Formato: GET_USER_PROFILE:userId
     */
    private String handleCreateReservation(String request, TheKnifeDAO dao) {
        try {
            String params = request.substring("CREATE_RESERVATION:".length());
            String[] tokens = params.split(":", -1);
            if (tokens.length < 5) {
                return "CREATE_RESERVATION_FAIL:Parametri insufficienti";
            }

            int userId;
            try {
                userId = Integer.parseInt(tokens[0].trim());
            } catch (NumberFormatException e) {
                return "CREATE_RESERVATION_FAIL:ID utente non valido";
            }

            String restaurantId = tokens[1].trim();
            String dateValue = tokens.length > 2 ? tokens[2].trim() : "";

            // guests is expected as the second-last token; note is last; time may contain ':' so rebuild from tokens[3..(n-3)]
            int guests = 0;
            int guestsIdx = tokens.length - 2;
            String note = tokens[tokens.length - 1].trim();
            try {
                guests = Integer.parseInt(tokens[guestsIdx].trim());
            } catch (Exception e) {
                guests = 0;
            }

            StringBuilder tb = new StringBuilder();
            for (int i = 3; i < guestsIdx; i++) {
                if (tb.length() > 0) tb.append(":");
                tb.append(tokens[i]);
            }
            String timeValue = tb.toString();

            AppLogger.info("Handler", "CREATE_RESERVATION parsed for userId=" + userId + " risto=" + restaurantId);

            if (userId <= 0 || restaurantId.isEmpty() || dateValue.isEmpty() || timeValue.isEmpty() || guests < 1) {
                return "CREATE_RESERVATION_FAIL:Dati prenotazione non validi";
            }

            // Verify session token belongs to user
            if (!userMatchesToken(userId)) {
                System.err.println("[Handler] CREATE_RESERVATION - token mismatch for userId=" + userId);
                return "ERROR:Sessione non valida o non autorizzata";
            }

            // If client omitted date/time (e.g., platform date command differences), default to tomorrow 19:00
            if (dateValue.isEmpty()) {
                java.time.LocalDate ld = java.time.LocalDate.now().plusDays(1);
                dateValue = ld.toString();
            }
            if (timeValue.isEmpty()) {
                timeValue = "19:00";
            }
            String timeNormalized = timeValue.length() == 4 ? timeValue.substring(0, 2) + ":" + timeValue.substring(2) : timeValue;
            java.sql.Date reservationDate = java.sql.Date.valueOf(dateValue);
            java.sql.Time reservationTime = java.sql.Time.valueOf(java.time.LocalTime.parse(timeNormalized, java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            String reservationCode = "TK-" + (System.currentTimeMillis() % 900000 + 100000);

            int rows = dao.creaPrenotazione(userId, restaurantId, reservationDate, reservationTime, guests, note, reservationCode);
            if (rows > 0) {
                return "CREATE_RESERVATION_OK:" + reservationCode;
            }
            return "CREATE_RESERVATION_FAIL:Impossibile salvare la prenotazione";
        } catch (Exception e) {
            System.err.println("[Handler] CREATE_RESERVATION - Errore: " + e.getMessage());
            return "CREATE_RESERVATION_FAIL:Errore database";
        }
    }

    private String handleGetUserReservations(String request, TheKnifeDAO dao) {
        try {
            String params = request.substring("GET_USER_RESERVATIONS:".length()).trim();
            if (params.isEmpty()) {
                return "GET_USER_RESERVATIONS_FAIL:ID utente obbligatorio";
            }

            // Support optional pagination: GET_USER_RESERVATIONS:userId[:page[:size]]
            String[] parts = params.split(":");
            int userId = Integer.parseInt(parts[0].trim());
            int page = 0;
            int size = 50; // default page size
            try {
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    page = Integer.parseInt(parts[1].trim());
                }
                if (parts.length > 2 && !parts[2].isEmpty()) {
                    size = Integer.parseInt(parts[2].trim());
                }
            } catch (NumberFormatException ignored) {
                // fallback to defaults
                page = 0; size = 50;
            }
            if (page < 0) page = 0;
            if (size <= 0) size = 50;
            if (size > 500) size = 500; // safety cap

            int offset = page * size;

            java.util.List<java.util.Map<String,Object>> rows = dao.getPrenotazioniUtenteList(userId, size, offset);
            StringBuilder sb = new StringBuilder("GET_USER_RESERVATIONS_OK:");
            boolean first = true;
            for (java.util.Map<String,Object> r : rows) {
                if (!first) sb.append(";");
                sb.append(r.getOrDefault("id_prenotazione","0")).append("|")
                  .append(r.getOrDefault("id_ristorante","")).append("|")
                  .append(r.getOrDefault("nome_ristorante","")).append("|")
                  .append(r.getOrDefault("data_prenotazione","")).append("|");
                Object timeObj = r.get("ora_prenotazione");
                String timeStr = "";
                if (timeObj instanceof java.sql.Time) {
                    timeStr = ((java.sql.Time)timeObj).toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                } else if (timeObj != null) {
                    timeStr = timeObj.toString();
                }
                sb.append(timeStr).append("|")
                  .append(r.getOrDefault("numero_persone","0")).append("|")
                  .append(r.getOrDefault("codice_prenotazione","")).append("|")
                  .append(r.getOrDefault("note","")).append("|")
                  .append(r.getOrDefault("stato","").toString());
                first = false;
            }
            if (first) {
                sb.append("0");
            }

            // If we returned exactly size rows, indicate there might be more by appending a marker
            if (rows.size() == size) {
                sb.append("|MORE");
            }

            return sb.toString();
        } catch (Exception e) {
            System.err.println("[Handler] GET_USER_RESERVATIONS - Errore: " + e.getMessage());
            return "GET_USER_RESERVATIONS_FAIL:Errore database";
        }
    }

    private String handleUpdateReservation(String request, TheKnifeDAO dao) {
        try {
            String params = request.substring("UPDATE_RESERVATION:".length());
            String[] data = params.split(":", -1);
            if (data.length < 6) {
                return "UPDATE_RESERVATION_FAIL:Parametri insufficienti";
            }

            int reservationId = Integer.parseInt(data[0].trim());
            int userId = Integer.parseInt(data[1].trim());
            String dateValue = data[2].trim();
            String timeValue = data[3].trim();
            int guests = Integer.parseInt(data[4].trim());
            String note = data[5].trim();

            // Verify session token belongs to user
            if (!userMatchesToken(userId)) {
                System.err.println("[Handler] UPDATE_RESERVATION - token mismatch for userId=" + userId);
                return "ERROR:Sessione non valida o non autorizzata";
            }

            String timeNormalized = timeValue.length() == 4 ? timeValue.substring(0, 2) + ":" + timeValue.substring(2) : timeValue;
            java.sql.Date reservationDate = java.sql.Date.valueOf(dateValue);
            java.sql.Time reservationTime = java.sql.Time.valueOf(java.time.LocalTime.parse(timeNormalized, java.time.format.DateTimeFormatter.ofPattern("HH:mm")));

            int rows = dao.aggiornaPrenotazione(reservationId, userId, reservationDate, reservationTime, guests, note);
            if (rows > 0) {
                return "UPDATE_RESERVATION_OK:Prenotazione aggiornata";
            }
            return "UPDATE_RESERVATION_FAIL:Impossibile aggiornare la prenotazione";
        } catch (Exception e) {
            System.err.println("[Handler] UPDATE_RESERVATION - Errore: " + e.getMessage());
            return "UPDATE_RESERVATION_FAIL:Errore database";
        }
    }

    private String handleDeleteReservation(String request, TheKnifeDAO dao) {
        try {
            String params = request.substring("DELETE_RESERVATION:".length());
            String[] data = params.split(":", -1);
            if (data.length < 2) {
                return "DELETE_RESERVATION_FAIL:Parametri insufficienti";
            }

            int reservationId = Integer.parseInt(data[0].trim());
            int userId = Integer.parseInt(data[1].trim());

            // Verify session token belongs to user
            if (!userMatchesToken(userId)) {
                System.err.println("[Handler] UPDATE/DELETE_RESERVATION - token mismatch for userId=" + userId);
                return "ERROR:Sessione non valida o non autorizzata";
            }

            int rows = dao.eliminaPrenotazione(reservationId, userId);
            if (rows > 0) {
                return "DELETE_RESERVATION_OK:Prenotazione eliminata";
            }
            return "DELETE_RESERVATION_FAIL:Impossibile eliminare la prenotazione";
        } catch (Exception e) {
            System.err.println("[Handler] DELETE_RESERVATION - Errore: " + e.getMessage());
            return "DELETE_RESERVATION_FAIL:Errore database";
        }
    }

    private String handleGetUserProfile(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2 || parts[1].isEmpty()) {
                return "GET_USER_PROFILE_FAIL:ID utente obbligatorio";
            }

            String userIdStr = parts[1].trim();
            int userId;

            try {
                userId = Integer.parseInt(userIdStr);
            } catch (NumberFormatException e) {
                return "GET_USER_PROFILE_FAIL:ID utente non valido";
            }

            System.out.println("[Handler] GET_USER_PROFILE: userId=" + userId);
            java.util.Map<String,Object> user = dao.getDatiUtenteMap(userId);
            String result = "";
            if (user != null && !user.isEmpty()) {
                result = serializeRows(java.util.List.of(user));
            }
            return "GET_USER_PROFILE_OK:" + result;

        } catch (SQLException e) {
            System.err.println("[Handler] GET_USER_PROFILE - SQLException: " + e.getMessage());
            return "GET_USER_PROFILE_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta UPDATE_USER_PROFILE
     * Formato: UPDATE_USER_PROFILE:userId:nome:cognome:email:dataNascita:luogo:lat:lon
     */
    private String handleUpdateUserProfile(String[] parts, TheKnifeDAO dao, String request) {
        try {
            String params = request.substring("UPDATE_USER_PROFILE:".length());
            String[] data = params.split(":", -1);

            if (data.length < 8) {
                return "UPDATE_USER_PROFILE_FAIL:Parametri insufficienti";
            }

            int userId;
            String nome = data[1].trim();
            String cognome = data[2].trim();
            String email = data[3].trim();
            String dataNascitaStr = data[4].trim();
            String luogo = data[5].trim();

            try {
                userId = Integer.parseInt(data[0].trim());
            } catch (NumberFormatException e) {
                return "UPDATE_USER_PROFILE_FAIL:ID utente non valido";
            }

            if (nome.isEmpty() || cognome.isEmpty()) {
                return "UPDATE_USER_PROFILE_FAIL:Nome e cognome obbligatori";
            }

            java.sql.Date dataNascita = null;
            if (!dataNascitaStr.isEmpty()) {
                try {
                    java.time.LocalDate localDate = java.time.LocalDate.parse(dataNascitaStr);
                    dataNascita = java.sql.Date.valueOf(localDate);
                } catch (Exception e) {
                    return "UPDATE_USER_PROFILE_FAIL:Data non valida";
                }
            }

            double lat = 0.0, lon = 0.0;
            try {
               if (data.length > 6 && !data[6].isEmpty()) lat = Double.parseDouble(data[6].trim());
               if (data.length > 7 && !data[7].isEmpty()) lon = Double.parseDouble(data[7].trim());
            } catch (NumberFormatException e) {
               lat = 0.0;
               lon = 0.0;
            }

            double[] normalized = UserProfileService.normalizeCoordinates(luogo, lat, lon);
            lat = normalized[0];
            lon = normalized[1];

            System.out.println("[Handler] UPDATE_USER_PROFILE: userId=" + userId);
            boolean success = dao.updateProfiloUtente(userId, nome, cognome, dataNascita, luogo, lat, lon);

            if (success) {
                return "UPDATE_USER_PROFILE_OK:Profilo aggiornato";
            } else {
                return "UPDATE_USER_PROFILE_FAIL:Impossibile aggiornare il profilo";
            }

        } catch (SQLException e) {
            System.err.println("[Handler] UPDATE_USER_PROFILE - SQLException: " + e.getMessage());
            return "UPDATE_USER_PROFILE_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta CHANGE_PASSWORD
     * Formato: CHANGE_PASSWORD:userId:oldPassword:newPassword
     */
    private String handleChangePassword(String[] parts, TheKnifeDAO dao, String request) {
        try {
            String params = request.substring("CHANGE_PASSWORD:".length());
            String[] data = params.split(":", -1);

            if (data.length < 3) {
                return "CHANGE_PASSWORD_FAIL:Parametri insufficienti";
            }

            int userId;
            String oldPassword = data[1].trim();
            String newPassword = data[2].trim();

            try {
                userId = Integer.parseInt(data[0].trim());
            } catch (NumberFormatException e) {
                return "CHANGE_PASSWORD_FAIL:ID utente non valido";
            }

            if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                return "CHANGE_PASSWORD_FAIL:Passwords non possono essere vuote";
            }

            if (newPassword.length() < 6) {
                return "CHANGE_PASSWORD_FAIL:La nuova password deve avere almeno 6 caratteri";
            }

            System.out.println("[Handler] CHANGE_PASSWORD: userId=" + userId);

            String sql = "SELECT password_hash FROM Utenti WHERE id_utente = ?";
            try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    return "CHANGE_PASSWORD_FAIL:Utente non trovato";
                }

                String storedHash = rs.getString("password_hash");
                if (!PasswordSecurity.verifyPassword(oldPassword, storedHash)) {
                    return "CHANGE_PASSWORD_FAIL:Password attuale non corretta";
                }
            }

            String newHash = PasswordSecurity.hashPassword(newPassword);
            String updateSql = "UPDATE Utenti SET password_hash = ? WHERE id_utente = ?";
            try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(updateSql)) {
                ps.setString(1, newHash);
                ps.setInt(2, userId);
                int rowsUpdated = ps.executeUpdate();

                if (rowsUpdated > 0) {
                    return "CHANGE_PASSWORD_OK:Password cambiata";
                } else {
                    return "CHANGE_PASSWORD_FAIL:Impossibile cambiare la password";
                }
            }

        } catch (SQLException e) {
            System.err.println("[Handler] CHANGE_PASSWORD - SQLException: " + e.getMessage());
            return "CHANGE_PASSWORD_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_OWNER_RESTAURANT
     * Formato: GET_OWNER_RESTAURANT:userId
     */
    private String handleGetOwnerRestaurant(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2 || parts[1].isEmpty()) {
                return "GET_OWNER_RESTAURANT_FAIL:ID utente obbligatorio";
            }

            String userIdStr = parts[1].trim();
            int userId;

            try {
                userId = Integer.parseInt(userIdStr);
            } catch (NumberFormatException e) {
                return "GET_OWNER_RESTAURANT_FAIL:ID utente non valido";
            }

            System.out.println("[Handler] GET_OWNER_RESTAURANT: userId=" + userId);
            ResultSet rs = dao.visualizzaRiepilogo(userId);

            String result = serializeResultSet(rs);
            return "GET_OWNER_RESTAURANT_OK:" + result;

        } catch (SQLException e) {
            System.err.println("[Handler] GET_OWNER_RESTAURANT - SQLException: " + e.getMessage());
            return "GET_OWNER_RESTAURANT_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta UPDATE_RESTAURANT
     * Formato: UPDATE_RESTAURANT:restaurantId:userId:nome:citta:indirizzo:cucina:prezzo:delivery:online
     */
    private String handleUpdateRestaurant(String[] parts, TheKnifeDAO dao, String request) {
        try {
            String params = request.substring("UPDATE_RESTAURANT:".length());
            String[] data = params.split(":", -1);

            if (data.length < 9) {
                return "UPDATE_RESTAURANT_FAIL:Parametri insufficienti";
            }

            String restaurantId = data[0].trim();
            int userId;
            String nome = data[2].trim();
            String citta = data[3].trim();
            String indirizzo = data[4].trim();
            String cucina = data[5].trim();
            int prezzo;
            boolean delivery, online;

            try {
                userId = Integer.parseInt(data[1].trim());
                prezzo = Integer.parseInt(data[6].trim());
                delivery = Boolean.parseBoolean(data[7].trim());
                online = Boolean.parseBoolean(data[8].trim());
            } catch (NumberFormatException e) {
                return "UPDATE_RESTAURANT_FAIL:Parametri numerici non validi";
            }

            if (nome.isEmpty() || citta.isEmpty() || indirizzo.isEmpty()) {
                return "UPDATE_RESTAURANT_FAIL:Campi obbligatori mancanti";
            }

            System.out.println("[Handler] UPDATE_RESTAURANT: restaurantId=" + restaurantId + ", userId=" + userId);
            boolean success = dao.modificaRistorante(restaurantId, userId, nome, citta, indirizzo, prezzo, delivery, online, cucina);

            if (success) {
                return "UPDATE_RESTAURANT_OK:Ristorante aggiornato";
            } else {
                return "UPDATE_RESTAURANT_FAIL:Impossibile aggiornare il ristorante";
            }

        } catch (SQLException e) {
            System.err.println("[Handler] UPDATE_RESTAURANT - SQLException: " + e.getMessage());
            return "UPDATE_RESTAURANT_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta DELETE_RESTAURANT
     * Formato: DELETE_RESTAURANT:restaurantId:userId
     */
    private String handleDeleteRestaurant(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2) {
                return "DELETE_RESTAURANT_FAIL:Parametri insufficienti";
            }

            String params = parts[1].trim();
            String[] data = params.split(":");

            if (data.length < 2) {
                return "DELETE_RESTAURANT_FAIL:Parametri insufficienti";
            }

            String restaurantId = data[0].trim();
            int userId;

            try {
                userId = Integer.parseInt(data[1].trim());
            } catch (NumberFormatException e) {
                return "DELETE_RESTAURANT_FAIL:ID utente non valido";
            }

            System.out.println("[Handler] DELETE_RESTAURANT: restaurantId=" + restaurantId + ", userId=" + userId);
            boolean success = dao.eliminaRistorante(restaurantId, userId);

            if (success) {
                return "DELETE_RESTAURANT_OK:Ristorante eliminato";
            } else {
                return "DELETE_RESTAURANT_FAIL:Impossibile eliminare il ristorante";
            }

        } catch (SQLException e) {
            System.err.println("[Handler] DELETE_RESTAURANT - SQLException: " + e.getMessage());
            return "DELETE_RESTAURANT_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_RECIPES
     * Formato: GET_RECIPES:restaurantId
     */
    private String handleGetRecipes(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2 || parts[1].isEmpty()) {
                return "GET_RECIPES_FAIL:ID ristorante obbligatorio";
            }

            String restaurantId = parts[1].trim();

            System.out.println("[Handler] GET_RECIPES: restaurantId=" + restaurantId);
            
            String sql = "SELECT id_ricetta, nome, descrizione FROM Ricette WHERE id_ristorante = ?";
            try (java.sql.PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, restaurantId);
                ResultSet rs = ps.executeQuery();

                String result = serializeResultSet(rs);
                return "GET_RECIPES_OK:" + result;
            }

        } catch (SQLException e) {
            System.err.println("[Handler] GET_RECIPES - SQLException: " + e.getMessage());
            return "GET_RECIPES_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_RESTAURANT_REVIEWS
     * Formato: GET_RESTAURANT_REVIEWS:restaurantId
     */
    private String handleGetRestaurantReviews(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2 || parts[1].isEmpty()) {
                return "GET_RESTAURANT_REVIEWS_FAIL:ID ristorante obbligatorio";
            }

            String restaurantId = parts[1].trim();

            System.out.println("[Handler] GET_RESTAURANT_REVIEWS: restaurantId=" + restaurantId);
            
            // Use DAO method which handles joins/columns consistently with DB schema
            ResultSet rs = dao.visualizzaRecensioni(restaurantId);
            String result = serializeResultSet(rs);
            return "GET_RESTAURANT_REVIEWS_OK:" + result;

        } catch (SQLException e) {
            System.err.println("[Handler] GET_RESTAURANT_REVIEWS - SQLException: " + e.getMessage());
            return "GET_RESTAURANT_REVIEWS_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_FAVORITE_COUNT
     * Formato: GET_FAVORITE_COUNT:restaurantId
     */
    private String handleGetFavoriteCount(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2 || parts[1].isEmpty()) {
                return "GET_FAVORITE_COUNT_FAIL:ID ristorante obbligatorio";
            }

            String restaurantId = parts[1].trim();

            System.out.println("[Handler] GET_FAVORITE_COUNT: restaurantId=" + restaurantId);
            
            String sql = "SELECT COUNT(*) as count FROM Preferiti WHERE id_ristorante = ?";
            try (java.sql.PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, restaurantId);
                ResultSet rs = ps.executeQuery();

                String result = serializeResultSet(rs);
                return "GET_FAVORITE_COUNT_OK:" + result;
            }

        } catch (SQLException e) {
            System.err.println("[Handler] GET_FAVORITE_COUNT - SQLException: " + e.getMessage());
            return "GET_FAVORITE_COUNT_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_RECENT_REVIEWS
     * Formato: GET_RECENT_REVIEWS:restaurantId:days
     */
    private String handleGetRecentReviews(String[] parts, TheKnifeDAO dao, String request) {
        try {
            String params = request.substring("GET_RECENT_REVIEWS:".length());
            String[] data = params.split(":");

            if (data.length < 2) {
                return "GET_RECENT_REVIEWS_FAIL:Parametri insufficienti";
            }

            String restaurantId = data[0].trim();
            int days;

            try {
                days = Integer.parseInt(data[1].trim());
            } catch (NumberFormatException e) {
                return "GET_RECENT_REVIEWS_FAIL:Numero giorni non valido";
            }

            System.out.println("[Handler] GET_RECENT_REVIEWS: restaurantId=" + restaurantId + ", days=" + days);
            
            String sql = "SELECT id_recensione, id_utente, nome_utente, stelle, testo, data_recensione FROM Recensioni " +
                    "WHERE id_ristorante = ? AND data_recensione >= DATE_SUB(NOW(), INTERVAL ? DAY) ORDER BY data_recensione DESC";
            try (java.sql.PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, restaurantId);
                ps.setInt(2, days);
                ResultSet rs = ps.executeQuery();

                String result = serializeResultSet(rs);
                return "GET_RECENT_REVIEWS_OK:" + result;
            }

        } catch (SQLException e) {
            System.err.println("[Handler] GET_RECENT_REVIEWS - SQLException: " + e.getMessage());
            return "GET_RECENT_REVIEWS_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta GET_REVIEW_TRENDS
     * Formato: GET_REVIEW_TRENDS:restaurantId
     */
    private String handleGetReviewTrends(String[] parts, TheKnifeDAO dao) {
        try {
            if (parts.length < 2 || parts[1].isEmpty()) {
                return "GET_REVIEW_TRENDS_FAIL:ID ristorante obbligatorio";
            }

            String restaurantId = parts[1].trim();

            System.out.println("[Handler] GET_REVIEW_TRENDS: restaurantId=" + restaurantId);
            
            String sql = "SELECT DATE(data_recensione) as data, AVG(stelle) as media_stelle, COUNT(*) as num_recensioni " +
                    "FROM Recensioni WHERE id_ristorante = ? GROUP BY DATE(data_recensione) ORDER BY data DESC LIMIT 30";
            try (java.sql.PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, restaurantId);
                ResultSet rs = ps.executeQuery();

                String result = serializeResultSet(rs);
                return "GET_REVIEW_TRENDS_OK:" + result;
            }

        } catch (SQLException e) {
            System.err.println("[Handler] GET_REVIEW_TRENDS - SQLException: " + e.getMessage());
            return "GET_REVIEW_TRENDS_FAIL:Errore database";
        }
    }

    /**
     * Gestisce la richiesta CREATE_RESTAURANT
     * Formato: CREATE_RESTAURANT:userId:nome:nazione:citta:indirizzo:cucina:prezzo:delivery:online
     */
    private String handleCreateRestaurant(String[] parts, TheKnifeDAO dao, String request) {
        try {
            String params = request.substring("CREATE_RESTAURANT:".length());
            String[] data = params.split(":", -1);

            if (data.length < 9) {
                return "CREATE_RESTAURANT_FAIL:Parametri insufficienti";
            }

            int userId;
            String nome = data[1].trim();
            String nazione = data[2].trim();
            String citta = data[3].trim();
            String indirizzo = data[4].trim();
            String cucina = data[5].trim();
            int prezzo;
            boolean delivery, online;

            try {
                userId = Integer.parseInt(data[0].trim());
                prezzo = Integer.parseInt(data[6].trim());
                delivery = Boolean.parseBoolean(data[7].trim());
                online = Boolean.parseBoolean(data[8].trim());
            } catch (NumberFormatException e) {
                return "CREATE_RESTAURANT_FAIL:Parametri numerici non validi";
            }

            if (nome.isEmpty() || nazione.isEmpty() || citta.isEmpty() || indirizzo.isEmpty()) {
                return "CREATE_RESTAURANT_FAIL:Campi obbligatori mancanti";
            }

            System.out.println("[Handler] CREATE_RESTAURANT: userId=" + userId + ", nome=" + nome);
            int restaurantId = dao.aggiungiRistorante(nome, nazione, citta, indirizzo, 0.0, 0.0, prezzo, delivery, online, cucina, userId);

            if (restaurantId > 0) {
                return "CREATE_RESTAURANT_OK:" + restaurantId;
            } else {
                return "CREATE_RESTAURANT_FAIL:Impossibile creare il ristorante";
            }

        } catch (SQLException e) {
            System.err.println("[Handler] CREATE_RESTAURANT - SQLException: " + e.getMessage());
            return "CREATE_RESTAURANT_FAIL:Errore database";
        }
    }

    /**
     * Valida un formato email
     */
    private boolean isValidEmail(String email) {
        return RequestValidator.isValidEmail(email);
    }
}

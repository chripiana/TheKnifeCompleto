package project.client.services;

import project.shared.dto.ApiResponse;
import project.shared.models.ServerConnection;
import project.client.config.ServerConnectionConfig;
import project.shared.protocol.ProtocolMessage;
import project.shared.validation.RequestValidator;
import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * ServerApiClient
 *
 * Componente responsabile della comunicazione sincrona tra client desktop
 * e server via socket TCP. Incapsula la creazione del socket, la serializzazione
 * delle richieste testuali e la lettura delle risposte.
 *
 * Responsabilità principali:
 * - stabilire e monitorare la connessione socket
 * - inviare richieste testuali formattate secondo il protocollo dell'app
 * - ricevere risposte e gestire il token di sessione (quando presente)
 * - esporre metodi di utilità per verificare lo stato di connessione
 *
 * Motivazione della progettazione:
 * - Uso di socket semplici (PrintWriter/BufferedReader) per interoperabilità
 *   e semplicità: il protocollo è testuale e line-oriented
 * - Timeout di connessione breve per non bloccare l'interfaccia utente
 * - Memorizzazione statica del sessionToken semplifica il riuso tra istanze
 *   del client durante la sessione dell'applicazione.
 *
 */
/**
 * ServerApiClient
 *
 * Purpose: Brief description of the class responsibilities and role in the application.
 *
 * Responsibilities/Usage:
 * - Describe main responsibilities and how this class is used at a high level.
 *
 * Design notes / Dependencies:
 * - List key dependencies and rationale for design choices (separation of concerns, performance, simplicity).
 *
 * Implementation details:
 * - Mention important collaborators, expected inputs/outputs and lifecycle (initialization, cleanup, threading if relevant).
 */
public class ServerApiClient {
    /**
     * Configurazione di connessione (host/port) letta tramite ServerConnectionConfig.
     */
/**
 * Field: config
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private ServerConnection config;

    /**
     * Socket TCP verso il server. Viene creato al momento della connect().
     */
/**
 * Field: socket
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Socket socket;

    /**
     * Writer verso il socket: usato per inviare richieste al server in UTF-8.
     */
/**
 * Field: writer
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private PrintWriter writer;

    /**
     * Reader dal socket: usato per leggere linee di risposta dal server.*/
/**
 * Field: reader
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private BufferedReader reader;

    /**
     * Token di sessione condiviso (statico) tra eventuali istanze client.
     * Viene impostato dopo il LOGIN_OK e allegato alle richieste non pubbliche.*/
/**
 * Field: sessionToken
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private static String sessionToken;

    /**
     * Timeout per lettura/operazioni sul socket in ms. Mantiene l'app responsiva.*/
/**
 * Field: TIMEOUT_MS
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private static final int TIMEOUT_MS = 30000;

    /**
     * Timeout per la fase di connect (ms). Scelto breve per non bloccare UI.*/
    private static final int CONNECT_TIMEOUT_MS = 2000; // short connect timeout to avoid blocking UI

    /**
     * Costruttore: ottiene la configurazione di connessione dal ServerConnectionConfig.*/
    public ServerApiClient() {
        this.config = ServerConnectionConfig.getInstance();
    }

    /**
     * Stabilisce la connessione TCP verso il server usando i parametri
     * forniti da config. Se la connessione è già attiva ritorna true.
     * Restituisce false in caso di errore e provvede a pulire le risorse.*/
/**
 * Method: connect
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public boolean connect() {
        if (socket != null && !socket.isClosed()) {
            return true;
        }

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(config.getHost(), config.getPort()), CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(TIMEOUT_MS);

            writer = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                true
            );
            reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );

            System.out.println("[Client] Connesso al server: " + config.getHost() + ":" + config.getPort());
            return true;
        } catch (IOException e) {
            System.err.println("[Client] Errore connessione: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    /**
     * Verifica se esiste una connessione attiva e la restituisce; se non
     * presente tenta di ristabilirla tramite connect().*/
/**
 * Method: ensureConnected
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public boolean ensureConnected() {
        if (socket != null && !socket.isClosed()) {
            return true;
        }
        return connect();
    }

    /**
     * Chiude in modo sicuro writer, reader e socket liberando le risorse.
     * Richiamato sia in caso di errori che volontariamente per terminare la
     * connessione dal client.*/
/**
 * Method: disconnect
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void disconnect() {
        try {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("[Client] Errore disconnessione: " + e.getMessage());
        } finally {
            writer = null;
            reader = null;
            socket = null;
        }
    }

    /**
     * Invia una richiesta testuale al server e restituisce la linea di risposta.
     * Validazioni eseguite:
     * - verifica che la connessione sia attiva
     * - valida il formato della request tramite RequestValidator
     * - allega il sessionToken quando appropriato
     *
     * Note sulla concorrenza: questo metodo non è sincronizzato — l'uso di
     * istanze condivise da thread multipli potrebbe richiedere serializzazione
     * esterna delle chiamate.
     **/
    public String sendRequest(String request) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Non connesso al server");
        }
        if (!RequestValidator.isValidRequest(request)) {
            throw new IOException("Richiesta non valida");
        }

        String requestToSend = request;
        if (sessionToken != null && !sessionToken.isBlank()
                && !request.startsWith("LOGIN:")
                && !request.startsWith("REGISTER:")
                && !request.startsWith("PING:")
                && !request.startsWith("SEARCH_RISTORANTI:")
                && !request.startsWith("GET_RISTORANTE_DETAILS:")
                && !request.startsWith("GET_RESTAURANT_REVIEWS:")
                && !request.startsWith("GET_REVIEW_STATS:")
                && !request.startsWith("GET_STAR_DISTRIBUTION:")
                && !request.startsWith("GET_RECIPES:")
                && !request.startsWith("GET_RECENT_REVIEWS:")
                && !request.startsWith("GET_REVIEW_TRENDS:")) {
            System.out.println("[Client] ✓ TOKEN AVAILABLE (" + sessionToken.substring(0, Math.min(15, sessionToken.length())) + "...). Appending to: " + request.substring(0, Math.min(30, request.length())));
            requestToSend = ProtocolMessage.attachSessionToken(request, sessionToken);
        } else {
            if (sessionToken == null || sessionToken.isBlank()) {
                System.out.println("[Client] ✗ NO TOKEN available. Sending raw: " + request.substring(0, Math.min(40, request.length())));
            } else {
                System.out.println("[Client] ℹ Request is public/excluded: " + request.substring(0, Math.min(30, request.length())));
            }
        }

        writer.println(requestToSend);
        writer.flush();

        // Read response with a short retry in case of transient null (server closed stream briefly)
        String response = null;
        final int maxAttempts = 2;
        final long backoffMs = 300L;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            response = reader.readLine();
            if (response != null) break;
            if (attempt < maxAttempts - 1) {
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (response != null && response.startsWith("LOGIN_OK:")) {
            String[] parts = response.split(":", 4);
            if (parts.length >= 4) {
                ServerApiClient.sessionToken = parts[3];
                System.out.println("[Client] TOKEN SAVED after LOGIN: " + sessionToken.substring(0, Math.min(20, sessionToken.length())) + "...");
            }
        } else if (response != null && response.startsWith("LOGIN")) {
            System.out.println("[Client] Login response doesn't match LOGIN_OK: " + response.substring(0, Math.min(60, response.length())));
        }
        return response;
    }

    /**
     * Controlla se la connessione socket è attiva e risponde al heartbeat.
     * Implementa un ping/test leggero per rilevare socket half-open o server
     * non più reattivo.*/
/**
 * Method: isConnected
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public boolean isConnected() {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            return false;
        }
        // Heartbeat: invia un PING per verificare che il server sia ancora vivo
        try {
            return sendPing();
        } catch (Exception e) {
            System.err.println("[Client] Server non risponde al heartbeat: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    /**
     * Invia un messaggio PING e verifica che il server risponda con PONG.
     * Viene usato internamente come controllo di vivibilità della connessione.*/
    private boolean sendPing() throws IOException {
        try {
            String response = sendRequest("PING:");
            return response != null && response.startsWith("PONG:");
        } catch (IOException e) {
            throw new IOException("Heartbeat failed: " + e.getMessage(), e);
        }
    }

    /**
     * Restituisce l'oggetto di configurazione corrente usato dal client.*/
/**
 * Method: getConfig
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public ServerConnection getConfig() {
        return config;
    }

    /**
     * Imposta una nuova configurazione e la salva su file (persistenza locale).*/
/**
 * Method: setConfig
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void setConfig(ServerConnection config) {
        this.config = config;
        ServerConnectionConfig.saveConfiguration(config);
    }

    /**
     * Restituisce il token di sessione attualmente memorizzato (statico).*/
/**
 * Method: getSessionToken
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Pulisce il token di sessione (logout globale a livello client).*/
/**
 * Method: clearSessionToken
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public static void clearSessionToken() {
        sessionToken = null;
    }
}

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
 * Client per comunicare con il server via socket
 * Il client NON comunica direttamente con il database
 */
public class ServerApiClient {
    private ServerConnection config;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private static String sessionToken;
    private static final int TIMEOUT_MS = 30000;
    private static final int CONNECT_TIMEOUT_MS = 2000; // short connect timeout to avoid blocking UI

    public ServerApiClient() {
        this.config = ServerConnectionConfig.getInstance();
    }

    /**
     * Connette al server
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

    public boolean ensureConnected() {
        if (socket != null && !socket.isClosed()) {
            return true;
        }
        return connect();
    }

    /**
     * Scollega dal server
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
     * Invia una richiesta al server e riceve la risposta
     */
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
     * Verifica la connessione al server con heartbeat
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
     * Invia un PING al server e verifica la risposta
     */
    private boolean sendPing() throws IOException {
        try {
            String response = sendRequest("PING:");
            return response != null && response.startsWith("PONG:");
        } catch (IOException e) {
            throw new IOException("Heartbeat failed: " + e.getMessage(), e);
        }
    }

    public ServerConnection getConfig() {
        return config;
    }

    public void setConfig(ServerConnection config) {
        this.config = config;
        ServerConnectionConfig.saveConfiguration(config);
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public static void clearSessionToken() {
        sessionToken = null;
    }
}

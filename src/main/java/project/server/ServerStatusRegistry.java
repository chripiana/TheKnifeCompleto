package project.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerStatusRegistry {
    private static final Map<String, ConnectedClient> CONNECTED_CLIENTS = new ConcurrentHashMap<>();

    private ServerStatusRegistry() {
    }

    public static void addClient(String clientId) {
        CONNECTED_CLIENTS.put(clientId, new ConnectedClient(clientId, null, null));
    }

    public static void updateClient(String clientId, Integer userId, String role) {
        CONNECTED_CLIENTS.compute(clientId, (id, current) ->
                new ConnectedClient(id, userId, role));
    }

    public static void removeClient(String clientId) {
        CONNECTED_CLIENTS.remove(clientId);
    }

    public static List<ConnectedClient> snapshot() {
        return new ArrayList<>(CONNECTED_CLIENTS.values());
    }

    public static void clear() {
        CONNECTED_CLIENTS.clear();
    }

    public record ConnectedClient(String clientId, Integer userId, String role) {
        public String displayText() {
            if (userId == null) {
                return clientId + " - non autenticato";
            }
            String roleText = role == null ? "utente" : role;
            return clientId + " - utente " + userId + " (" + roleText + ")";
        }
    }
}

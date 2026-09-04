/*
 * TheKnife - Progetto JavaFX/DB per il modulo client/server.
 * Autore: Nome Cognome, Matricola 000000, sede VA
 */
package theknife;

import javafx.application.Application;
import javafx.stage.Stage;
import project.client.config.ServerConnectionConfig;
import project.client.services.ServerApiClient;
import project.controllers.Navigator;
import project.server.TheKnifeServer;
import project.shared.models.ServerConnection;

import java.io.IOException;
import java.net.Socket;

/**
 * Entry point del modulo client di TheKnife.
 *
 * @author Christian Pianarosa, Matricola 758419, sede CO
 */
public class ClientMain extends Application {
    private static final Object SERVER_LOCK = new Object();
    private static boolean serverStarted = false;

    @Override
    public void start(Stage stage) {
        Navigator.getInstance().setStage(stage);
        ServerConnectionConfig.initialize();

        // Enforce single automatic server connection using provided defaults
        ServerConnection config = new ServerConnection("localhost", 8080, 5432, "theknife", "postgres", "");
        ServerConnectionConfig.saveConfiguration(config);

        startEmbeddedServerIfNeeded(config);

        ServerApiClient apiClient = new ServerApiClient();
        apiClient.setConfig(config);

        // Ensure the client connects automatically and retains the socket open
        boolean connected = apiClient.ensureConnected();
        if (!connected) {
            System.err.println("[Client] Impossibile connettersi al server " + config.getHost() + ":" + config.getPort());
        } else {
            System.out.println("[Client] Connesso al server " + config.getHost() + ":" + config.getPort());
        }

        Navigator.getInstance().navigateTo("NotLoggedUser/home-view.fxml", "Trova il tuo ristorante");
    }

    private void startEmbeddedServerIfNeeded(ServerConnection config) {
        if (isPortReachable(config.getHost(), config.getPort())) {
            System.out.println("[Client] Server già attivo su " + config.getHost() + ":" + config.getPort());
            return;
        }

        synchronized (SERVER_LOCK) {
            if (serverStarted || isPortReachable(config.getHost(), config.getPort())) {
                return;
            }

            Thread serverThread = new Thread(() -> {
                try {
                    System.out.println("[Client] Avvio del server embedded su " + config.getHost() + ":" + config.getPort());
                    TheKnifeServer server = new TheKnifeServer(config.getPort(), config, "0.0.0.0");
                    server.start();
                } catch (Exception e) {
                    System.err.println("[Client] Impossibile avviare il server embedded: " + e.getMessage());
                }
            }, "theknife-embedded-server");
            serverThread.setDaemon(true);
            serverThread.start();
            serverStarted = true;
        }

        waitForServer(config.getHost(), config.getPort(), 5000);
    }

    private boolean isPortReachable(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private void waitForServer(String host, int port, int timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (isPortReachable(host, port)) {
                return;
            }
            try {
                Thread.sleep(150L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

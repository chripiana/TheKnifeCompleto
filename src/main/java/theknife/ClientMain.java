/*
 * TheKnife - Progetto JavaFX/DB per il modulo client/server.
 * Autore: Nome Cognome, Matricola 000000, sede VA
 */
package theknife;

import javafx.application.Application;
import javafx.application.Application;
import javafx.stage.Stage;
import project.client.config.ServerConnectionConfig;
import project.client.services.ServerApiClient;
import project.controllers.Navigator;
import project.shared.models.ServerConnection;

/**
 * Entry point del modulo client di TheKnife.
 *
 * @author Christian Pianarosa, Matricola 758419, sede CO
 */
public class ClientMain extends Application {
    @Override
    public void start(Stage stage) {
        Navigator.getInstance().setStage(stage);
        ServerConnectionConfig.initialize();

        // Enforce single automatic server connection using provided defaults
        ServerConnection config = new ServerConnection("localhost", 8080, 5432, "theknife", "postgres", "");
        ServerConnectionConfig.saveConfiguration(config);

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

    public static void main(String[] args) {
        launch(args);
    }
}

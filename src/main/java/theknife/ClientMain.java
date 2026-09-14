/*
 * TheKnife - Progetto JavaFX/DB per il modulo client/server.
 * Autore: Nome Cognome, Matricola 000000, sede VA
 */
package theknife;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
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

        ServerConnection config = new ServerConnection("localhost", 8080, 5432, "theknife", "postgres", "");
        ServerConnectionConfig.saveConfiguration(config);

        ServerApiClient apiClient = new ServerApiClient();
        apiClient.setConfig(config);

        boolean connected = apiClient.ensureConnected();
        if (connected) {
            System.out.println("[Client] Connesso al server " + config.getHost() + ":" + config.getPort());
        } else {
            System.err.println("[Client] Server non raggiungibile su " + config.getHost() + ":" + config.getPort() + ". Avviarlo prima di usare le funzionalità remote.");

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Server offline");
            alert.setHeaderText("Il server non è raggiungibile");
            alert.setContentText("Le funzionalità remote non saranno disponibili finché il backend non sarà avviato su " + config.getHost() + ":" + config.getPort() + ".");
            alert.setResizable(false);
            alert.show();
        }

        Navigator.getInstance().navigateTo("NotLoggedUser/home-view.fxml", "Trova il tuo ristorante");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

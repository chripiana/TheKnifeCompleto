package project.server;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import project.shared.models.ServerConnection;
/**
 * ServerDashboardController
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

public class ServerDashboardController {
/**
 * Field: dbHostField
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private TextField dbHostField;
/**
 * Field: dbPortField
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private TextField dbPortField;
/**
 * Field: dbNameField
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private TextField dbNameField;
/**
 * Field: dbUserField
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private TextField dbUserField;
/**
 * Field: dbPasswordField
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private PasswordField dbPasswordField;
/**
 * Field: serverPortField
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private TextField serverPortField;
/**
 * Field: statusLabel
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private Label statusLabel;
/**
 * Field: connectedClientsCountLabel
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private Label connectedClientsCountLabel;
/**
 * Field: serverStateLabel
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private Label serverStateLabel;
/**
 * Field: serverModeBadge
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private Label serverModeBadge;
/**
 * Field: connectedClientsList
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private ListView<String> connectedClientsList;

/**
 * Field: runningServer
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private TheKnifeServer runningServer;
/**
 * Field: refreshTimeline
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Timeline refreshTimeline;

    @FXML
/**
 * Method: initialize
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void initialize() {
        dbHostField.setText("localhost");
        dbPortField.setText("5432");
        dbNameField.setText("theknife");
        dbUserField.setText("postgres");
        dbPasswordField.setText("password");
        serverPortField.setText("8080");

        connectedClientsList.setItems(FXCollections.observableArrayList());
        connectedClientsCountLabel.setText("0");
        serverStateLabel.setText("OFFLINE");
        serverStateLabel.getStyleClass().removeAll("online", "offline");
        serverStateLabel.getStyleClass().add("offline");
        setModeBadge(false);
        startRefreshLoop();
    }

    @FXML
/**
 * Method: handleStartServer
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleStartServer() {
        try {
            String dbHost = dbHostField.getText().trim();
            int dbPort = Integer.parseInt(dbPortField.getText().trim());
            String dbName = dbNameField.getText().trim();
            String dbUser = dbUserField.getText().trim();
            String dbPassword = dbPasswordField.getText();
            int serverPort = Integer.parseInt(serverPortField.getText().trim());

            if (dbHost.isEmpty() || dbName.isEmpty() || dbUser.isEmpty()) {
                setStatus("Compila tutti i campi obbligatori.", "#c0392b");
                return;
            }

            if (serverPort < 1024 || serverPort > 65535) {
                setStatus("La porta server deve essere compresa tra 1024 e 65535.", "#c0392b");
                return;
            }

            if (dbPort < 1024 || dbPort > 65535) {
                setStatus("La porta database deve essere compresa tra 1024 e 65535.", "#c0392b");
                return;
            }

            if (!isPortAvailable(serverPort)) {
                setStatus("La porta " + serverPort + " è già in uso. Chiudi l'istanza attiva del server o seleziona un'altra porta.", "#c0392b");
                setServerOnlineState(false);
                setModeBadge(false);
                return;
            }

            ServerConnection config = new ServerConnection(
                    dbHost,
                    serverPort,
                    dbPort,
                    dbName,
                    dbUser,
                    dbPassword
            );

            if (runningServer != null && runningServer.isRunning()) {
                setStatus("Il server è già avviato.", "#2d6a4f");
                return;
            }

            runningServer = new TheKnifeServer(serverPort, config);
            Thread serverThread = new Thread(() -> {
                try {
                    Platform.runLater(() -> {
                        setStatus("Avvio del server in corso...", "#1d4ed8");
                        setServerOnlineState(true);
                    });
                    runningServer.start();
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        setStatus("Errore avvio server: " + ex.getMessage(), "#c0392b");
                        setServerOnlineState(false);
                    });
                }
            }, "theknife-server-thread");
            serverThread.setDaemon(true);
            serverThread.start();

            setStatus("Server avviato su porta " + serverPort + ". In attesa di client...", "#2d6a4f");
            setServerOnlineState(true);
            setModeBadge(true);
        } catch (NumberFormatException ex) {
            setStatus("Porta non valida. Inserisci solo numeri.", "#c0392b");
            setServerOnlineState(false);
            setModeBadge(false);
        }
    }

    @FXML
/**
 * Method: handleStopServer
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleStopServer() {
        if (runningServer != null) {
            runningServer.stop();
            runningServer = null;
            ServerStatusRegistry.clear();
            refreshClientList();
            setStatus("Server arrestato.", "#7f8c8d");
            setServerOnlineState(false);
            setModeBadge(false);
        } else {
            setStatus("Il server non è in esecuzione.", "#7f8c8d");
            setServerOnlineState(false);
            setModeBadge(false);
        }
    }

/**
 * Method: startRefreshLoop
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void startRefreshLoop() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> refreshClientList()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

/**
 * Method: refreshClientList
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void refreshClientList() {
        ObservableList<String> items = FXCollections.observableArrayList();
        ServerStatusRegistry.snapshot().forEach(client -> items.add(client.displayText()));
        connectedClientsList.setItems(items);
        connectedClientsCountLabel.setText(String.valueOf(items.size()));
    }

/**
 * Method: isPortAvailable
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private boolean isPortAvailable(int port) {
        try (java.net.ServerSocket probe = new java.net.ServerSocket()) {
            probe.setReuseAddress(true);
            probe.bind(new java.net.InetSocketAddress(java.net.InetAddress.getByName("0.0.0.0"), port), 1);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

/**
 * Method: setStatus
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void setStatus(String message, String color) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12;");
        });
    }

/**
 * Method: setServerOnlineState
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void setServerOnlineState(boolean online) {
        Platform.runLater(() -> {
            serverStateLabel.getStyleClass().removeAll("online", "offline");
            if (online) {
                serverStateLabel.setText("ONLINE");
                serverStateLabel.getStyleClass().add("online");
            } else {
                serverStateLabel.setText("OFFLINE");
                serverStateLabel.getStyleClass().add("offline");
            }
        });
    }

/**
 * Method: setModeBadge
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void setModeBadge(boolean online) {
        Platform.runLater(() -> {
            serverModeBadge.getStyleClass().removeAll("connected", "disconnected");
            if (online) {
                serverModeBadge.setText("LISTENING");
                serverModeBadge.getStyleClass().add("connected");
            } else {
                serverModeBadge.setText("DISCONNECTED");
                serverModeBadge.getStyleClass().add("disconnected");
            }
        });
    }
}

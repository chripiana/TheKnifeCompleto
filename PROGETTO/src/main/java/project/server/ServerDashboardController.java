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

public class ServerDashboardController {
    @FXML private TextField dbHostField;
    @FXML private TextField dbPortField;
    @FXML private TextField dbNameField;
    @FXML private TextField dbUserField;
    @FXML private PasswordField dbPasswordField;
    @FXML private TextField serverPortField;
    @FXML private Label statusLabel;
    @FXML private Label connectedClientsCountLabel;
    @FXML private Label serverStateLabel;
    @FXML private Label serverModeBadge;
    @FXML private ListView<String> connectedClientsList;

    private TheKnifeServer runningServer;
    private Timeline refreshTimeline;

    @FXML
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

    private void startRefreshLoop() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> refreshClientList()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void refreshClientList() {
        ObservableList<String> items = FXCollections.observableArrayList();
        ServerStatusRegistry.snapshot().forEach(client -> items.add(client.displayText()));
        connectedClientsList.setItems(items);
        connectedClientsCountLabel.setText(String.valueOf(items.size()));
    }

    private void setStatus(String message, String color) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12;");
        });
    }

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

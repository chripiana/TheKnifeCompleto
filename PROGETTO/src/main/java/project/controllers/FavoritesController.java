package project.controllers;

import project.client.services.ServerApiClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FavoritesController {

    @FXML private FlowPane containerPreferiti;
    @FXML private VBox emptyFavoritesBox;

    private Navigator navigator;
    private ServerApiClient apiClient;
    private int idUtenteLoggato;

    @FXML
    public void initialize() {
        this.navigator = Navigator.getInstance();
        this.apiClient = new ServerApiClient();
        this.idUtenteLoggato = navigator.getIdUtenteLoggato();

        if (idUtenteLoggato != -1) {
            caricaPreferiti();
        } else {
            navigator.navigateTo("login-view.fxml", "Accedi");
        }
    }

    private void caricaPreferiti() {
        containerPreferiti.getChildren().clear();
        boolean haPreferiti = false;

        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    showError("Errore di connessione", "Impossibile connettersi al server");
                    return;
                }
            }

            String response = apiClient.sendRequest("GET_PREFERITI_UTENTE:" + idUtenteLoggato);
            
            if (response != null && response.startsWith("GET_PREFERITI_UTENTE_OK:")) {
                String data = response.substring("GET_PREFERITI_UTENTE_OK:".length());
                if (!data.isEmpty()) {
                    String[] rows = data.split(";");
                    for (String row : rows) {
                        haPreferiti = true;
                        Map<String, String> rowData = parseRowData(row);
                        
                        String idRistorante = rowData.getOrDefault("id_ristorante", "");
                        String nome = rowData.getOrDefault("nome", "");
                        String citta = rowData.getOrDefault("citta", "");
                        String cucina = rowData.getOrDefault("tipologia_cucina", "");
                        String prezzoStr = rowData.getOrDefault("prezzo_medio", "0");
                        double prezzo = 0;
                        try {
                            prezzo = Double.parseDouble(prezzoStr);
                        } catch (NumberFormatException e) {
                        }

                        SearchController.RistoranteOggetto restaurant = new SearchController.RistoranteOggetto(rowData);
                        VBox card = new VBox(12);
                        card.setPrefWidth(260);
                        card.setMinHeight(180);
                        card.setMaxWidth(260);
                        card.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff 0%, #f3faf7 100%); -fx-background-radius: 20; -fx-padding: 18 18 14 18; -fx-border-color: #dfeee8; -fx-border-width: 1; -fx-border-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(27,67,50,0.08), 12, 0, 0, 5);");
                        card.setOnMouseClicked(e -> navigator.navigateToRestaurantDetails(restaurant));

                        HBox topRow = new HBox(10);
                        topRow.setAlignment(Pos.CENTER_LEFT);
                        Label emojiLabel = new Label(getEmojiCucina(cucina));
                        emojiLabel.setStyle("-fx-font-size: 26px;");
                        Label lblNome = new Label(nome);
                        lblNome.setWrapText(true);
                        lblNome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #163d2f;");
                        topRow.getChildren().addAll(emojiLabel, lblNome);

                        Label lblDettagli = new Label("📍 " + citta + " • " + cucina);
                        lblDettagli.setWrapText(true);
                        lblDettagli.setStyle("-fx-font-size: 12px; -fx-text-fill: #4A6B57;");

                        Label lblPrezzo = new Label("💰 Prezzo medio: " + prezzo + " €");
                        lblPrezzo.setStyle("-fx-font-size: 12px; -fx-text-fill: #2D6A4F; -fx-font-weight: bold;");

                        Button btnRimuovi = new Button("💔 Rimuovi");
                        btnRimuovi.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 8 12 8 12;");
                        btnRimuovi.setOnMouseClicked(e -> e.consume());
                        String finalIdRistorante = idRistorante;
                        btnRimuovi.setOnAction(e -> rimuoviPreferito(finalIdRistorante));

                        card.getChildren().addAll(topRow, lblDettagli, lblPrezzo, btnRimuovi);
                        containerPreferiti.getChildren().add(card);
                    }
                }
            } else if (response != null && response.startsWith("GET_PREFERITI_UTENTE_FAIL:")) {
                showError("Errore", response.substring("GET_PREFERITI_UTENTE_FAIL:".length()));
            } else if (response != null && response.startsWith("ERROR:Sessione")) {
                // Sessione scaduta o non valida -> forzare logout
                navigator.logout();
                navigator.navigateTo("login-view.fxml", "Accedi");
                showError("Sessione scaduta", "Effettua nuovamente il login per continuare.");
            }
        } catch (IOException e) {
            showError("Errore di connessione", e.getMessage());
        }

        if (haPreferiti) {
            containerPreferiti.setVisible(true);
            containerPreferiti.setManaged(true);
            emptyFavoritesBox.setVisible(false);
            emptyFavoritesBox.setManaged(false);
        } else {
            containerPreferiti.setVisible(false);
            containerPreferiti.setManaged(false);
            emptyFavoritesBox.setVisible(true);
            emptyFavoritesBox.setManaged(true);
        }
    }

    private void rimuoviPreferito(String idRistorante) {
        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    showError("Errore di connessione", "Impossibile connettersi al server");
                    return;
                }
            }

            String response = apiClient.sendRequest("REMOVE_PREFERITO:" + idUtenteLoggato + ":" + idRistorante);
            
            if (response != null && response.startsWith("REMOVE_PREFERITO_OK:")) {
                caricaPreferiti();
            } else if (response != null && response.startsWith("REMOVE_PREFERITO_FAIL:")) {
                showError("Errore", response.substring("REMOVE_PREFERITO_FAIL:".length()));
            } else if (response != null && response.startsWith("ERROR:Sessione")) {
                navigator.logout();
                navigator.navigateTo("login-view.fxml", "Accedi");
                showError("Sessione scaduta", "Effettua nuovamente il login per continuare.");
            }
        } catch (IOException e) {
            showError("Errore", e.getMessage());
        }
    }

    private Map<String, String> parseRowData(String row) {
        Map<String, String> data = new HashMap<>();
        String[] fields = row.split("\\|");
        for (String field : fields) {
            String[] kv = field.split("=", 2);
            if (kv.length == 2) {
                data.put(kv[0].trim(), kv[1].trim());
            }
        }
        return data;
    }

    private String getEmojiCucina(String cucina) {
        if (cucina == null) {
            return "🍽️";
        }
        String lower = cucina.toLowerCase();
        if (lower.contains("ital") || lower.contains("pasta") || lower.contains("pizza")) return "🍝";
        if (lower.contains("giap") || lower.contains("sushi") || lower.contains("japan")) return "🍣";
        if (lower.contains("mediterr") || lower.contains("veg") || lower.contains("salad")) return "🥗";
        if (lower.contains("meat") || lower.contains("grill") || lower.contains("steak") || lower.contains("bbq")) return "🥩";
        if (lower.contains("franc") || lower.contains("bistro")) return "🥐";
        if (lower.contains("india") || lower.contains("curry")) return "🍛";
        if (lower.contains("mess") || lower.contains("mex")) return "🌮";
        if (lower.contains("pesce") || lower.contains("fish") || lower.contains("sea")) return "🐟";
        return "🍽️";
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    @FXML void handleCerca(ActionEvent event) { navigator.navigateTo("search-view-logged.fxml", "Cerca Ristoranti"); }
    @FXML void handleRecensioni(ActionEvent event) { navigator.navigateTo("reviews-view.fxml", "Le Mie Recensioni"); }
    @FXML void handleProfilo(ActionEvent event) { navigator.navigateToProfile(); }
    @FXML void handleLogout(ActionEvent event) {
        navigator.logout();
        navigator.navigateTo("login-view.fxml", "Accedi");
    }
    @FXML private void handleGoToProfile(ActionEvent event) { navigator.navigateToProfile(); }
    @FXML void handleVaiAllaRicerca(ActionEvent event) { handleCerca(event); }
    @FXML private void handleGoToHome(javafx.scene.input.MouseEvent event) { navigator.navigateToHomeIntelligent(); }
}
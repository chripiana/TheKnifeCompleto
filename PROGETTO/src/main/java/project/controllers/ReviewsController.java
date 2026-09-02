package project.controllers;

import project.client.services.ServerApiClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ReviewsController {

    @FXML private VBox containerRecensioni;
    @FXML private VBox emptyReviewsBox;

    private Navigator navigator;
    private ServerApiClient apiClient;
    private int idUtenteLoggato;

    @FXML
    public void initialize() {
        this.navigator = Navigator.getInstance();
        this.apiClient = new ServerApiClient();
        this.idUtenteLoggato = navigator.getIdUtenteLoggato();

        if (idUtenteLoggato != -1) {
            caricaRecensioni();
        } else {
            navigator.navigateTo("login-view.fxml", "Accedi");
        }
    }

    private void caricaRecensioni() {
        containerRecensioni.getChildren().clear();
        boolean haRecensioni = false;

        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    showError("Errore di connessione", "Impossibile connettersi al server");
                    return;
                }
            }

            String response = apiClient.sendRequest("GET_USER_REVIEWS:" + idUtenteLoggato);
            
            if (response != null && response.startsWith("GET_USER_REVIEWS_OK:")) {
                String data = response.substring("GET_USER_REVIEWS_OK:".length());
                if (!data.isEmpty()) {
                    String[] rows = data.split(";");
                    for (String row : rows) {
                        haRecensioni = true;
                        Map<String, String> rowData = parseRowData(row);
                        
                        int idRecensione = 0;
                        try {
                            idRecensione = Integer.parseInt(rowData.getOrDefault("id_recensione", "0"));
                        } catch (NumberFormatException e) {
                        }
                        
                        String ristorante = rowData.getOrDefault("nome_ristorante", "");
                        String idRistorante = rowData.getOrDefault("id_ristorante", "");
                        String citta = rowData.getOrDefault("citta_ristorante", "");
                        String testo = rowData.getOrDefault("testo", "");
                        int stelle = 0;
                        try {
                            stelle = Integer.parseInt(rowData.getOrDefault("stelle", "0"));
                        } catch (NumberFormatException e) {
                        }
                        String dataStr = rowData.getOrDefault("data_recensione", "");

                        VBox card = new VBox(14);
                        card.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff 0%, #f4faf6 100%); -fx-background-radius: 18; -fx-padding: 18; -fx-border-color: #dfeee8; -fx-border-width: 1; -fx-border-radius: 18; -fx-effect: dropshadow(three-pass-box, rgba(27,67,50,0.08), 12, 0, 0, 5);");

                        HBox header = new HBox(12);
                        header.setAlignment(Pos.CENTER_LEFT);

                        VBox ristoranteBox = new VBox(4);
                        Label lblRisto = new Label(ristorante);
                        lblRisto.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #163d2f;");
                        Label lblLocation = new Label("📍 " + (citta.isEmpty() ? "Locale preferito" : citta));
                        lblLocation.setStyle("-fx-font-size: 12px; -fx-text-fill: #4A6B57;");
                        ristoranteBox.getChildren().addAll(lblRisto, lblLocation);

                        StringBuilder stelleStr = new StringBuilder();
                        for (int i = 0; i < 5; i++) {
                            stelleStr.append(i < stelle ? "★" : "☆");
                        }
                        Label lblStelle = new Label(stelleStr.toString());
                        lblStelle.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFB703;");

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        Label lblData = new Label(dataStr);
                        lblData.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

                        header.getChildren().addAll(ristoranteBox, spacer, lblStelle, lblData);

                        Label lblTesto = new Label(testo);
                        lblTesto.setWrapText(true);
                        lblTesto.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; -fx-padding: 4 0 0 0;");

                        HBox actionBox = new HBox(10);
                        actionBox.setAlignment(Pos.CENTER_LEFT);

                        Button btnRestaurant = new Button("Vai al ristorante");
                        btnRestaurant.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #1B4332; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 8 12 8 12;");
                        final String reviewRestaurantId = idRistorante;
                        final String reviewRestaurantName = ristorante;
                        final String reviewCity = citta;
                        final int reviewStars = stelle;
                        if (!reviewRestaurantId.isBlank()) {
                            Map<String, String> restaurantData = new HashMap<>();
                            restaurantData.put("id_ristorante", reviewRestaurantId);
                            restaurantData.put("nome", reviewRestaurantName);
                            restaurantData.put("citta", reviewCity);
                            restaurantData.put("nazione", "");
                            restaurantData.put("indirizzo", "");
                            restaurantData.put("tipologia_cucina", "");
                            restaurantData.put("prezzo_medio", "0");
                            restaurantData.put("latitudine", "0");
                            restaurantData.put("longitudine", "0");
                            restaurantData.put("delivery", "false");
                            restaurantData.put("prenotazione_online", "true");
                            restaurantData.put("distanza_km", "0");
                            restaurantData.put("media_stelle", String.valueOf(reviewStars));
                            restaurantData.put("num_recensioni", "0");
                            SearchController.RistoranteOggetto restaurant = new SearchController.RistoranteOggetto(restaurantData);
                            btnRestaurant.setOnAction(e -> navigator.navigateToRestaurantDetails(restaurant));
                        } else {
                            btnRestaurant.setDisable(true);
                        }

                        Button btnEdit = new Button("✏️ Modifica");
                        btnEdit.setStyle("-fx-background-color: #E0F2FE; -fx-text-fill: #0284C7; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 8 12 8 12;");
                        int finalIdRecensione = idRecensione;
                        int finalStelle = stelle;
                        String finalTesto = testo;
                        btnEdit.setOnAction(e -> mostraDialogModifica(finalIdRecensione, finalStelle, finalTesto));

                        Button btnDelete = new Button("🗑️ Elimina");
                        btnDelete.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 8 12 8 12;");
                        btnDelete.setOnAction(e -> eliminaRecensione(finalIdRecensione));

                        actionBox.getChildren().addAll(btnRestaurant, btnEdit, btnDelete);

                        card.getChildren().addAll(header, lblTesto, actionBox);
                        containerRecensioni.getChildren().add(card);
                    }
                }
            } else if (response != null && response.startsWith("GET_USER_REVIEWS_FAIL:")) {
                showError("Errore", response.substring("GET_USER_REVIEWS_FAIL:".length()));
            }
        } catch (IOException e) {
            showError("Errore di connessione", e.getMessage());
        }

        if (haRecensioni) {
            containerRecensioni.setVisible(true);
            containerRecensioni.setManaged(true);
            emptyReviewsBox.setVisible(false);
            emptyReviewsBox.setManaged(false);
        } else {
            containerRecensioni.setVisible(false);
            containerRecensioni.setManaged(false);
            emptyReviewsBox.setVisible(true);
            emptyReviewsBox.setManaged(true);
        }
    }

    private void mostraDialogModifica(int idRecensione, int vecchieStelle, String vecchioTesto) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifica Recensione");
        dialog.setHeaderText("Aggiorna la tua valutazione e il tuo commento");

        ComboBox<Integer> comboStelle = new ComboBox<>();
        comboStelle.getItems().addAll(1, 2, 3, 4, 5);
        comboStelle.setValue(vecchieStelle);

        TextArea txtTesto = new TextArea(vecchioTesto);
        txtTesto.setWrapText(true);
        txtTesto.setPrefRowCount(4);

        VBox content = new VBox(10, new Label("Valutazione (Stelle):"), comboStelle, new Label("Commento:"), txtTesto);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (!apiClient.isConnected()) {
                    if (!apiClient.ensureConnected()) {
                        showError("Errore di connessione", "Impossibile connettersi al server");
                        return;
                    }
                }

                String response = apiClient.sendRequest("MODIFY_REVIEW:" + idRecensione + ":" + idUtenteLoggato + ":" + 
                        comboStelle.getValue() + ":" + txtTesto.getText());
                
                if (response != null && response.startsWith("MODIFY_REVIEW_OK:")) {
                    caricaRecensioni();
                } else if (response != null && response.startsWith("MODIFY_REVIEW_FAIL:")) {
                    showError("Errore", response.substring("MODIFY_REVIEW_FAIL:".length()));
                } else if (response != null && response.startsWith("ERROR:Sessione")) {
                    navigator.logout();
                    navigator.navigateTo("login-view.fxml", "Accedi");
                    showError("Sessione scaduta", "Effettua nuovamente il login per continuare.");
                }
            } catch (IOException e) {
                showError("Errore", e.getMessage());
            }
        }
    }

    private void eliminaRecensione(int idRecensione) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Sei sicuro di voler eliminare questa recensione?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Conferma eliminazione");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                if (!apiClient.isConnected()) {
                    if (!apiClient.ensureConnected()) {
                        showError("Errore di connessione", "Impossibile connettersi al server");
                        return;
                    }
                }

                String response = apiClient.sendRequest("DELETE_REVIEW:" + idRecensione + ":" + idUtenteLoggato);
                
                if (response != null && response.startsWith("DELETE_REVIEW_OK:")) {
                    caricaRecensioni();
                } else if (response != null && response.startsWith("DELETE_REVIEW_FAIL:")) {
                    showError("Errore", response.substring("DELETE_REVIEW_FAIL:".length()));
                } else if (response != null && response.startsWith("ERROR:Sessione")) {
                    navigator.logout();
                    navigator.navigateTo("login-view.fxml", "Accedi");
                    showError("Sessione scaduta", "Effettua nuovamente il login per continuare.");
                }
            } catch (IOException e) {
                showError("Errore", e.getMessage());
            }
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

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    @FXML void handleCerca(ActionEvent event) { navigator.navigateTo("search-view-logged.fxml", "Cerca Ristoranti"); }
    @FXML void handlePreferiti(ActionEvent event) { navigator.navigateTo("favorites-view.fxml", "I Miei Preferiti"); }
    @FXML void handleProfilo(ActionEvent event) { navigator.navigateToProfile(); }
    @FXML void handleLogout(ActionEvent event) {
        navigator.logout();
        navigator.navigateTo("login-view.fxml", "Accedi");
    }
    @FXML private void handleGoToProfile(ActionEvent event) { navigator.navigateToProfile(); }
    @FXML void handleVaiAllaRicerca(ActionEvent event) { handleCerca(event); }
    @FXML private void handleGoToHome(javafx.scene.input.MouseEvent event) { navigator.navigateToHomeIntelligent(); }
}
package project.controllers;

import project.client.services.ServerApiClient;
import project.shared.dto.RestaurantSummary;
import project.shared.dto.ReviewItem;
import project.shared.util.ResponseParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.io.IOException;
import java.util.*;

public class OwnerDashboardController {

    @FXML
    private VBox containerRecensioni;
    @FXML
    private VBox containerRistoranti;

    @FXML
    private Label dashNomeLabel;
    @FXML
    private Label dashOwnerLabel;
    @FXML
    private Label dashAddressLabel;

    @FXML
    private Label kpiNumRecensioniLabel;
    @FXML
    private Label kpiMediaStelleLabel;
    @FXML
    private Label kpiSenzaRispostaLabel;

    // Form fields
    @FXML
    private TextField nomeField;
    @FXML
    private TextField cittaField;
    @FXML
    private TextField nazioneField;
    @FXML
    private TextField indirizzoField;
    @FXML
    private TextField latField;
    @FXML
    private TextField lonField;
    @FXML
    private ComboBox<String> fasciaCombo;
    @FXML
    private TextField tipoCucinaField;
    @FXML
    private CheckBox deliveryCheck;
    @FXML
    private CheckBox prenotazioneCheck;
    @FXML
    private Button btnSalvaRistorante;
    @FXML
    private Button btnEliminaRistorante;
    @FXML
    private Button btnVediScheda;

    private Navigator navigator;
    private ServerApiClient apiClient;
    private int idGestore;


    private List<RestaurantSummary> ristoranti = new ArrayList<>();
    private Map<String, List<ReviewItem>> reviewsByRistorante = new HashMap<>();
    private String selectedRestaurantId = null;
    private String selectedRestaurantName = null;

    @FXML
    public void initialize() {
        this.navigator = Navigator.getInstance();
        this.apiClient = new ServerApiClient();
        this.idGestore = navigator.getIdUtenteLoggato();


        if (fasciaCombo != null) {
            fasciaCombo.setItems(FXCollections.observableArrayList("15", "30", "45", "60", "80"));
        }

        if (idGestore == -1) {
            navigator.navigateTo("login-view.fxml", "Accedi");
            return;
        }

        Platform.runLater(this::caricaRistorantiERecensioni);

        if (btnSalvaRistorante != null) {
            btnSalvaRistorante.setOnAction(ev -> salvaRistorante());
        }
        if (btnEliminaRistorante != null) {
            btnEliminaRistorante.setOnAction(ev -> eliminaRistorante());
        }
        if (btnVediScheda != null) {
            btnVediScheda.setOnAction(ev -> vediSchedaRistorante());
        }
    }

    private void caricaRistorantiERecensioni() {
        containerRistoranti.getChildren().clear();
        containerRecensioni.getChildren().clear();
        ristoranti.clear();
        reviewsByRistorante.clear();

        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    Label err = new Label("Errore di connessione al server.");
                    err.setStyle("-fx-text-fill: #C0392B;");
                    containerRistoranti.getChildren().add(err);
                    return;
                }
            }

            String responseRistoranti = apiClient.sendRequest("GET_OWNER_RESTAURANT:" + idGestore);
            if (responseRistoranti != null && responseRistoranti.startsWith("GET_OWNER_RESTAURANT_OK:")) {
                String data = responseRistoranti.substring("GET_OWNER_RESTAURANT_OK:".length());
                if (!data.isEmpty()) {
                    String[] rows = data.split(";");
                    for (String row : rows) {
                        Map<String, String> rowData = ResponseParser.parseRowData(row);
                        String id = rowData.getOrDefault("id_ristorante", "");
                        String nome = rowData.getOrDefault("nome", "");
                        String citta = rowData.getOrDefault("citta", "");
                        double media = 0;
                        int numRec = 0;
                        try {
                            media = Double.parseDouble(rowData.getOrDefault("media_stelle", "0"));
                        } catch (NumberFormatException e) {
                        }
                        try {
                            numRec = Integer.parseInt(rowData.getOrDefault("num_recensioni", "0"));
                        } catch (NumberFormatException e) {
                        }
                        ristoranti.add(new RestaurantSummary(id, nome, citta, media, numRec));
                    }
                }
            }

            String responseRecensioni = apiClient.sendRequest("GET_RESTAURANT_REVIEWS:" + idGestore);
            if (responseRecensioni != null && responseRecensioni.startsWith("GET_RESTAURANT_REVIEWS_OK:")) {
                String data = responseRecensioni.substring("GET_RESTAURANT_REVIEWS_OK:".length());
                if (!data.isEmpty()) {
                    String[] rows = data.split(";");
                    for (String row : rows) {
                        Map<String, String> rowData = ResponseParser.parseRowData(row);
                        String idR = rowData.getOrDefault("id_ristorante", "");
                        int idRec = 0;
                        int stelle = 0;
                        try {
                            idRec = Integer.parseInt(rowData.getOrDefault("id_recensione", "0"));
                        } catch (NumberFormatException e) {
                        }
                        try {
                            stelle = Integer.parseInt(rowData.getOrDefault("stelle", "0"));
                        } catch (NumberFormatException e) {
                        }
                        String testo = rowData.getOrDefault("testo", "");
                        String dataRec = rowData.getOrDefault("data_recensione", "");
                        String autoreNome = rowData.getOrDefault("autore_nome", "");
                        String autoreCognome = rowData.getOrDefault("autore_cognome", "");
                        boolean giaRisposto = "1".equals(rowData.getOrDefault("gia_risposto", "0")) || "true".equalsIgnoreCase(rowData.getOrDefault("gia_risposto", "false"));
                        String risposta = rowData.getOrDefault("risposta", "");
                        String dataRisposta = rowData.getOrDefault("data_risposta", "");

                        ReviewItem item = new ReviewItem(idRec, stelle, testo, dataRec, autoreNome, autoreCognome, giaRisposto,
                                risposta, dataRisposta);
                        reviewsByRistorante.computeIfAbsent(idR, k -> new ArrayList<>()).add(item);
                    }
                }
            }

            if (ristoranti.isEmpty()) {
                Label empty = new Label(
                        "Non hai ancora ristoranti registrati. Crea il tuo primo ristorante dal pannello.");
                empty.setStyle("-fx-text-fill: #6B6B6B;");
                containerRistoranti.getChildren().add(empty);
            } else {
                for (RestaurantSummary r : ristoranti) {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("owner-restaurant-row");
                    row.setMaxWidth(Double.MAX_VALUE);
                    row.setPrefWidth(760);

                    Label nome = new Label(r.getNome());
                    nome.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #1B4332;");

                    Label meta = new Label(
                            r.getCitta() + " · " + String.format("%.1f ★ (%d)", r.getMediaStelle(), r.getNumRecensioni()));
                    meta.setStyle("-fx-text-fill: #4A6B57; -fx-font-size: 12;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button apri = new Button("Apri");
                    apri.getStyleClass().add("btn-outline");
                    apri.setMinWidth(82);

                    apri.setOnAction(ev -> openRestaurant(r));

                    row.getChildren().addAll(nome, meta, spacer, apri);
                    containerRistoranti.getChildren().add(row);
                }
            }

        } catch (IOException e) {
            Label err = new Label("Errore nel caricamento dei ristoranti/recensioni. Controlla il server.");
            err.setStyle("-fx-text-fill: #C0392B;");
            containerRistoranti.getChildren().add(err);
        }
    }

    private void openRestaurant(RestaurantSummary r) {
        selectedRestaurantId = r.getId();
        selectedRestaurantName = r.getNome();

        dashNomeLabel.setText(r.getNome());
        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    return;
                }
            }

            String responseRistorante = apiClient.sendRequest("GET_RISTORANTE_DETAILS:" + selectedRestaurantId);
            if (responseRistorante != null && responseRistorante.startsWith("GET_RISTORANTE_DETAILS_OK:")) {
                String data = responseRistorante.substring("GET_RISTORANTE_DETAILS_OK:".length());
                Map<String, String> rowData = ResponseParser.parseRowData(data);
                nomeField.setText(rowData.getOrDefault("nome", ""));
                cittaField.setText(rowData.getOrDefault("citta", ""));
                nazioneField.setText(rowData.getOrDefault("nazione", ""));
                indirizzoField.setText(rowData.getOrDefault("indirizzo", ""));
                try {
                    latField.setText(String.valueOf(Double.parseDouble(rowData.getOrDefault("latitudine", "0"))));
                } catch (NumberFormatException ex) {
                    latField.setText("0");
                }
                try {
                    lonField.setText(String.valueOf(Double.parseDouble(rowData.getOrDefault("longitudine", "0"))));
                } catch (NumberFormatException ex) {
                    lonField.setText("0");
                }
                tipoCucinaField.setText(rowData.getOrDefault("tipologia_cucina", ""));
                boolean delivery = "1".equals(rowData.getOrDefault("delivery", "0")) || "true".equalsIgnoreCase(rowData.getOrDefault("delivery", "false"));
                boolean pren = "1".equals(rowData.getOrDefault("prenotazione_online", "0")) || "true".equalsIgnoreCase(rowData.getOrDefault("prenotazione_online", "false"));
                deliveryCheck.setSelected(delivery);
                prenotazioneCheck.setSelected(pren);
                int prezzoMedio = 0;
                try {
                    prezzoMedio = Integer.parseInt(rowData.getOrDefault("prezzo_medio", "0"));
                } catch (NumberFormatException ex) {
                }
                String prezzoStr = String.valueOf(prezzoMedio);
                if (!fasciaCombo.getItems().contains(prezzoStr)) {
                    fasciaCombo.getItems().add(prezzoStr);
                }
                fasciaCombo.getSelectionModel().select(prezzoStr);
            }

            dashOwnerLabel.setText(String.format("Gestore ID: %d", idGestore));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        kpiNumRecensioniLabel.setText(String.valueOf(r.getNumRecensioni()));
        kpiMediaStelleLabel.setText(String.format("%.1f", r.getMediaStelle()));
        int senza = 0;
        List<ReviewItem> list = reviewsByRistorante.getOrDefault(r.getId(), Collections.emptyList());
        for (ReviewItem it : list)
            if (!it.isGiaRisposto())
                senza++;
        kpiSenzaRispostaLabel.setText(String.valueOf(senza));

        populateReviewsContainer(list);
    }

    private void populateReviewsContainer(List<ReviewItem> reviews) {
        containerRecensioni.getChildren().clear();

        if (reviews.isEmpty()) {
            Label none = new Label("Nessuna recensione per questo ristorante.");
            none.setStyle("-fx-text-fill: #6B6B6B;");
            containerRecensioni.getChildren().add(none);
            return;
        }

        for (ReviewItem r : reviews) {
            VBox card = new VBox(8);
            card.getStyleClass().add("rec-card");

            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);

            Label avatar = new Label(initialsFromText(r.getTesto()));
            avatar.getStyleClass().add("avatar-small");
            avatar.setAlignment(Pos.CENTER);

            VBox meta = new VBox(2);
            String nomeAutore = (r.getAutoreNome() != null ? r.getAutoreNome() : "") + " "
                    + (r.getAutoreCognome() != null ? r.getAutoreCognome() : "");
            Label username = new Label(nomeAutore.trim().isEmpty() ? "Cliente" : nomeAutore.trim());
            username.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
            Label data = new Label(r.getData());
            data.setStyle("-fx-text-fill: #4A6B57; -fx-font-size: 11;");
            meta.getChildren().addAll(username, data);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label stelle = new Label(String.join("", Collections.nCopies(r.getStelle(), "★")));
            stelle.setStyle("-fx-text-fill: #F6A623; -fx-font-size: 13;");

            Label badgeStatus = new Label(r.isGiaRisposto() ? "✅ Risposto" : "⏳ Senza risposta");
            badgeStatus.getStyleClass().addAll("badge-status", r.isGiaRisposto() ? "status-ok" : "status-no");

            header.getChildren().addAll(avatar, meta, spacer, stelle, badgeStatus);

            Label testo = new Label(r.getTesto());
            testo.setWrapText(true);
            testo.setStyle("-fx-font-size: 13; -fx-text-fill: #1B2E24; -fx-line-spacing: 1.4;");

            card.getChildren().addAll(header, testo);

            if (r.isGiaRisposto() && r.getRisposta() != null) {
                VBox boxRisposta = new VBox(4);
                boxRisposta.getStyleClass().add("risposta-box");

                Label titoloRisposta = new Label(
                        "👨‍🍳 Tua risposta — " + (r.getDataRisposta() == null ? "" : r.getDataRisposta()));
                titoloRisposta.setStyle("-fx-font-weight: bold; -fx-font-size: 11; -fx-text-fill: #1B4332;");
                Label testoRisposta = new Label(r.getRisposta());
                testoRisposta.setStyle("-fx-font-size: 12; -fx-text-fill: #4A6B57;");

                boxRisposta.getChildren().addAll(titoloRisposta, testoRisposta);
                card.getChildren().add(boxRisposta);
            }

            HBox formRisposta = new HBox(8);
            formRisposta.setAlignment(Pos.BOTTOM_LEFT);

            TextArea inputRisposta = new TextArea();
            inputRisposta.setPromptText(r.isGiaRisposto() ? "Modifica la risposta..." : "Scrivi una risposta pubblica...");
            inputRisposta.setPrefHeight(50);
            HBox.setHgrow(inputRisposta, Priority.ALWAYS);
            inputRisposta.getStyleClass().add("home-text-field");
            if (r.isGiaRisposto() && r.getRisposta() != null)
                inputRisposta.setText(r.getRisposta());

            Button btnInvia = new Button(r.isGiaRisposto() ? "✏️ Modifica" : "📤 Rispondi");
            btnInvia.getStyleClass().add("btn-primary");
            btnInvia.setStyle("-fx-padding: 6 14; -fx-font-size: 12;");

            btnInvia.setOnAction(ev -> {
                String testoRisposta = inputRisposta.getText().trim();
                if (testoRisposta.isEmpty()) {
                    Alert a = new Alert(Alert.AlertType.WARNING, "La risposta non può essere vuota.", ButtonType.OK);
                    a.showAndWait();
                    return;
                }
                rispondiRecensione(r.getIdRecensione(), testoRisposta);
            });

            formRisposta.getChildren().addAll(inputRisposta, btnInvia);
            card.getChildren().add(formRisposta);

            containerRecensioni.getChildren().add(card);
        }
    }

    private void salvaRistorante() {
        if (selectedRestaurantId == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Seleziona prima un ristorante dalla lista.", ButtonType.OK);
            a.showAndWait();
            return;
        }
        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Errore di connessione al server.", ButtonType.OK);
                    a.showAndWait();
                    return;
                }
            }

            String nome = nomeField.getText().trim();
            String citta = cittaField.getText().trim();
            String nazione = nazioneField.getText().trim();
            String indirizzo = indirizzoField.getText().trim();
            int prezzoMedio = 0;
            String fascia = fasciaCombo.getSelectionModel().getSelectedItem();
            if (fascia != null && !fascia.isBlank()) {
                try {
                    prezzoMedio = Integer.parseInt(fascia);
                } catch (NumberFormatException ex) {
                    prezzoMedio = 0;
                }
            }
            boolean delivery = deliveryCheck.isSelected();
            boolean pren = prenotazioneCheck.isSelected();
            String tipoCuc = tipoCucinaField.getText().trim();

            String response = apiClient.sendRequest("UPDATE_RESTAURANT:" + selectedRestaurantId + ":" + idGestore + ":" +
                    nome + ":" + citta + ":" + indirizzo + ":" + tipoCuc + ":" + prezzoMedio + ":" +
                    (delivery ? "1" : "0") + ":" + (pren ? "1" : "0"));

            if (response != null && response.startsWith("UPDATE_RESTAURANT_OK:")) {
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Ristorante aggiornato con successo.", ButtonType.OK);
                a.showAndWait();
                caricaRistorantiERecensioni();
                Optional<RestaurantSummary> opt = ristoranti.stream().filter(x -> x.getId().equals(selectedRestaurantId))
                        .findFirst();
                opt.ifPresent(this::openRestaurant);
            } else if (response != null && response.startsWith("UPDATE_RESTAURANT_FAIL:")) {
                Alert a = new Alert(Alert.AlertType.ERROR, "Non è stato possibile aggiornare il ristorante.",
                        ButtonType.OK);
                a.showAndWait();
            }
        } catch (IOException e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Errore durante l'aggiornamento del ristorante.", ButtonType.OK);
            a.showAndWait();
        }
    }

    private void eliminaRistorante() {
        if (selectedRestaurantId == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Seleziona prima un ristorante dalla lista.", ButtonType.OK);
            a.showAndWait();
            return;
        }
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("Conferma eliminazione");
        conferma.setHeaderText("Sei sicuro di voler eliminare questo ristorante?");
        conferma.setContentText("Questa azione è irreversibile. Tutti i dati del ristorante saranno cancellati.");
        Optional<ButtonType> result = conferma.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (!apiClient.isConnected()) {
                    if (!apiClient.ensureConnected()) {
                        Alert a = new Alert(Alert.AlertType.ERROR, "Errore di connessione al server.", ButtonType.OK);
                        a.showAndWait();
                        return;
                    }
                }

                String response = apiClient.sendRequest("DELETE_RESTAURANT:" + selectedRestaurantId + ":" + idGestore);

                if (response != null && response.startsWith("DELETE_RESTAURANT_OK:")) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Ristorante eliminato con successo.",
                            ButtonType.OK);
                    a.showAndWait();
                    selectedRestaurantId = null;
                    selectedRestaurantName = null;
                    caricaRistorantiERecensioni();
                    containerRecensioni.getChildren().clear();
                    nomeField.clear();
                    cittaField.clear();
                    nazioneField.clear();
                    indirizzoField.clear();
                    latField.clear();
                    lonField.clear();
                    tipoCucinaField.clear();
                    fasciaCombo.getSelectionModel().clearSelection();
                    deliveryCheck.setSelected(false);
                    prenotazioneCheck.setSelected(false);
                } else if (response != null && response.startsWith("DELETE_RESTAURANT_FAIL:")) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Non è stato possibile eliminare il ristorante.",
                            ButtonType.OK);
                    a.showAndWait();
                }
            } catch (IOException e) {
                Alert a = new Alert(Alert.AlertType.ERROR, "Errore durante l'eliminazione del ristorante.",
                        ButtonType.OK);
                a.showAndWait();
            }
        }
    }

    private void vediSchedaRistorante() {
        if (selectedRestaurantId == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Seleziona prima un ristorante dalla lista.", ButtonType.OK);
            a.showAndWait();
            return;
        }
        // Placeholder: in futuro si navigherà alla scheda pubblica del ristorante
        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Ristorante: " + selectedRestaurantName + "\n" +
                        "ID: " + selectedRestaurantId + "\n\n" +
                        "La scheda pubblica di questo ristorante sarà disponibile sulla pagina principale di ricerca dei clienti.",
                ButtonType.OK);
        a.setTitle("Scheda Pubblica");
        a.setHeaderText("Visualizza scheda pubblica");
        a.showAndWait();
    }

    // Utility
    private String initialsFromText(String testo) {
        if (testo == null || testo.isBlank())
            return "??";
        String[] parts = testo.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    private void rispondiRecensione(int idRecensione, String testoRisposta) {
        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Errore di connessione al server.", ButtonType.OK);
                    a.showAndWait();
                    return;
                }
            }

            String response = apiClient.sendRequest("REPLY_REVIEW:" + idRecensione + ":" + idGestore + ":" + testoRisposta);

            if (response != null && response.startsWith("REPLY_REVIEW_OK:")) {
                caricaRistorantiERecensioni();
                Optional<RestaurantSummary> opt = ristoranti.stream()
                        .filter(x -> x.getId().equals(selectedRestaurantId)).findFirst();
                opt.ifPresent(this::openRestaurant);
            } else if (response != null && response.startsWith("REPLY_REVIEW_FAIL:")) {
                Alert a = new Alert(Alert.AlertType.ERROR, "Errore nell'inserimento della risposta.",
                        ButtonType.OK);
                a.showAndWait();
            }
        } catch (IOException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Errore server: impossibile salvare la risposta.",
                    ButtonType.OK);
            a.showAndWait();
        }
    }

    @FXML
    private void handleGoToProfile(ActionEvent event) {
        Navigator.getInstance().navigateToProfile();
    }

    @FXML
    private void handleGoToHome(ActionEvent event) {
        Navigator.getInstance().navigateToHomeIntelligent();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        navigator.logout();
        navigator.navigateTo("login-view.fxml", "Accedi");
    }

    @FXML
    private void handleNuovoRistorante(ActionEvent event) {
        navigator.navigateTo("new-restaurant-view.fxml", "Crea Nuovo Ristorante");
    }
}
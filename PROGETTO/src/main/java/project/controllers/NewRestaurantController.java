package project.controllers;

import project.client.services.ServerApiClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NewRestaurantController {

    @FXML private Text txtInizialiGestore;
    @FXML private Text txtNomeGestore;
    @FXML private Text txtEmailGestore;
    @FXML private Text lblKpiRistoranti;

    @FXML private TextField txtNome;
    @FXML private ComboBox<String> comboCucina;
    @FXML private TextField txtPrezzoMedio;
    @FXML private CheckBox chkDelivery;
    @FXML private CheckBox chkPrenotazione;

    @FXML private Label star1;
    @FXML private Label star2;
    @FXML private Label star3;
    @FXML private Label star4;
    @FXML private Label star5;

    @FXML private TextField txtNazione;
    @FXML private TextField txtCitta;
    @FXML private TextField txtIndirizzo;
    @FXML private TextField txtLatitudine;
    @FXML private TextField txtLongitudine;
    @FXML private Text lblCoordinatePreview;

    private Navigator navigator;
    private ServerApiClient apiClient;
    private int idGestoreLoggato;
    private int stelleSelezionate = 0;

    @FXML
    public void initialize() {
        this.navigator = Navigator.getInstance();
        this.apiClient = new ServerApiClient();
        this.idGestoreLoggato = navigator.getIdUtenteLoggato();

        if (idGestoreLoggato == -1) {
            mostraAllerta("Errore Sessione", "Devi prima effettuare il login per aggiungere un ristorante.", Alert.AlertType.ERROR);
            navigator.navigateTo("login-view.fxml", "Accedi");
            return;
        }

        caricaDatiGestore();

        txtCitta.textProperty().addListener((obs, oldVal, newVal) -> calcolaCoordinateAutomatiche());
        txtIndirizzo.textProperty().addListener((obs, oldVal, newVal) -> calcolaCoordinateAutomatiche());
    }


    private void caricaDatiGestore() {
        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    mostraAllerta("Errore di connessione", "Impossibile connettersi al server", Alert.AlertType.ERROR);
                    return;
                }
            }

            String response = apiClient.sendRequest("GET_USER_PROFILE:" + idGestoreLoggato);
            if (response != null && response.startsWith("GET_USER_PROFILE_OK:")) {
                String data = response.substring("GET_USER_PROFILE_OK:".length());
                if (!data.isEmpty()) {
                    Map<String, String> rowData = parseRowData(data);
                    String nome = rowData.getOrDefault("nome", "");
                    String cognome = rowData.getOrDefault("cognome", "");
                    String email = rowData.getOrDefault("email", "");

                    txtNomeGestore.setText(nome + " " + cognome);
                    txtEmailGestore.setText(email);

                    String iniziali = "";
                    if (nome != null && !nome.isEmpty()) iniziali += nome.charAt(0);
                    if (cognome != null && !cognome.isEmpty()) iniziali += cognome.charAt(0);
                    txtInizialiGestore.setText(iniziali.toUpperCase());
                }
            }

            String countResponse = apiClient.sendRequest("GET_OWNER_RESTAURANT:" + idGestoreLoggato);
            int count = 0;
            if (countResponse != null && countResponse.startsWith("GET_OWNER_RESTAURANT_OK:")) {
                String data = countResponse.substring("GET_OWNER_RESTAURANT_OK:".length());
                if (!data.isEmpty()) {
                    String[] rows = data.split(";");
                    count = rows.length;
                }
            }
            lblKpiRistoranti.setText(String.valueOf(count));

        } catch (IOException e) {
            mostraAllerta("Errore di connessione", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void calcolaCoordinateAutomatiche() {
        String citta = txtCitta.getText().trim();
        String indirizzo = txtIndirizzo.getText().trim();

        if (citta.isEmpty() || indirizzo.isEmpty()) {
            lblCoordinatePreview.setText("Lat: —, Long: —");
            txtLatitudine.clear();
            txtLongitudine.clear();
            return;
        }

        int hash = (citta + indirizzo).hashCode();
        double latSimulata = 45.4642 + (Math.abs(hash % 100) / 10000.0);
        double lonSimulata = 9.1900 + (Math.abs(hash % 100) / 10000.0);

        txtLatitudine.setText(String.format("%.4f", latSimulata).replace(",", "."));
        txtLongitudine.setText(String.format("%.4f", lonSimulata).replace(",", "."));
        lblCoordinatePreview.setText(String.format("Lat: %.4f, Long: %.4f", latSimulata, lonSimulata));
    }


    @FXML
    void handleCreaRistorante(ActionEvent event) {
        String nome = txtNome.getText().trim();
        String nazione = txtNazione.getText().trim();
        String citta = txtCitta.getText().trim();
        String indirizzo = txtIndirizzo.getText().trim();
        String cucinaConEmoji = comboCucina.getValue();
        String prezzoText = txtPrezzoMedio.getText().trim();

        if (nome.isEmpty() || nazione.isEmpty() || citta.isEmpty() || indirizzo.isEmpty() || cucinaConEmoji == null || prezzoText.isEmpty()) {
            mostraAllerta("Campi vuoti", "Tutti i campi obbligatori devono essere compilati.", Alert.AlertType.WARNING);
            return;
        }

        int prezzoMedio;
        try {
            prezzoMedio = Integer.parseInt(prezzoText);
            if (prezzoMedio <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostraAllerta("Dato non valido", "Inserisci un prezzo medio valido maggiore di zero.", Alert.AlertType.WARNING);
            return;
        }

        double lat = 0.0;
        double lon = 0.0;
        try {
            lat = Double.parseDouble(txtLatitudine.getText().trim().replace(",", "."));
            lon = Double.parseDouble(txtLongitudine.getText().trim().replace(",", "."));
        } catch (Exception ex) {
            lat = 45.4642;
            lon = 9.1900;
        }

        String cucinaPura = cucinaConEmoji.replaceAll("[^a-zA-Z\\s/]", "").trim();

        boolean delivery = chkDelivery.isSelected();
        boolean prenotazioneOnline = chkPrenotazione.isSelected();

        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    mostraAllerta("Errore di connessione", "Impossibile connettersi al server", Alert.AlertType.ERROR);
                    return;
                }
            }

            String response = apiClient.sendRequest("CREATE_RESTAURANT:" + idGestoreLoggato + ":" + 
                    nome + ":" + nazione + ":" + citta + ":" + indirizzo + ":" + cucinaPura + ":" + 
                    prezzoMedio + ":" + delivery + ":" + prenotazioneOnline);

            if (response != null && response.startsWith("CREATE_RESTAURANT_OK:")) {
                mostraAllerta("Ristorante Creato! 🎉", "Il ristorante '" + nome + "' è stato registrato correttamente.", Alert.AlertType.INFORMATION);
                handleAnnulla(null);
            } else if (response != null && response.startsWith("CREATE_RESTAURANT_FAIL:")) {
                mostraAllerta("Errore", response.substring("CREATE_RESTAURANT_FAIL:".length()), Alert.AlertType.ERROR);
            } else {
                mostraAllerta("Errore", "Si è verificato un errore durante la registrazione del ristorante.", Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            mostraAllerta("Errore di connessione", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Ritorna alla Dashboard o alla pagina precedente
     */
    @FXML
    void handleAnnulla(ActionEvent event) {
        navigator.navigateTo("owner-dashboard-view.fxml", "Dashboard Proprietario");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        navigator.logout();
        navigator.navigateTo("home-view.fxml", "Trova il tuo ristorante");
    }

    @FXML
    private void handleGoToProfile(ActionEvent event) {
        navigator.navigateToProfile();
    }

    @FXML
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        navigator.navigateToHomeIntelligent();
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

    // =========================================================
    //  GESTIONE RATING STELLATO CLICCABILE (INTERFACCIA DINAMICA)
    // =========================================================

    private void aggiornaStelleVisive(int count) {
        stelleSelezionate = count;
        Label[] stelle = {star1, star2, star3, star4, star5};
        for (int i = 0; i < 5; i++) {
            if (i < count) {
                stelle[i].setStyle("-fx-text-fill: #FFB703; -fx-font-size: 20px; -fx-cursor: hand;");
            } else {
                stelle[i].setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 20px; -fx-cursor: hand;");
            }
        }
    }

    @FXML private void handleStar1() { aggiornaStelleVisive(1); }
    @FXML private void handleStar2() { aggiornaStelleVisive(2); }
    @FXML private void handleStar3() { aggiornaStelleVisive(3); }
    @FXML private void handleStar4() { aggiornaStelleVisive(4); }
    @FXML private void handleStar5() { aggiornaStelleVisive(5); }

    private void mostraAllerta(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}


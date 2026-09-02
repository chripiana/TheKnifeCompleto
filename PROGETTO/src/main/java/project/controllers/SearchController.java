package project.controllers;

import project.client.services.ServerApiClient;
import project.shared.util.ResponseParser;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.util.Duration;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchController {

    // === API CLIENT ===
    private ServerApiClient apiClient;

    @FXML
    private TextField searchInlineField; // Barra della Navbar superiore
    @FXML
    private VBox containerRisultati; // Contenitore verticale per le card
    @FXML
    private HBox containerPaginazione; // Contenitore per i bottoni 1, 2, 3...

    // Pannello laterale sinistro allineato allo schema definitivo
    @FXML
    private TextField filterCitta;
    @FXML
    private TextField filterPrezzo;
    @FXML
    private ComboBox<String> filterStelle;
    @FXML
    private ComboBox<String> filterOrdine;
    @FXML
    private CheckBox filterDelivery;
    @FXML
    private CheckBox filterPrenotazione;
    @FXML
    private CheckBox filterAperto;
    @FXML
    private ComboBox<Integer> nearbyRadiusCombo;

    private final List<RistoranteOggetto> tuttiIRistoranti = new ArrayList<>();
    private int paginaCorrente = 1;
    private static final int ELEMENTI_PER_PAGINA = 10;
    private Popup toastPopup;
    private PauseTransition toastTimer;


    public static class RistoranteOggetto {
        public String id, nome, citta, nazione, indirizzo, cucina;
        public int prezzo, stelleIntere, numRecensioni;
        public double mediaStelleReale;
        public double lat, lon, distanzaKm;
        public boolean delivery, prenotazioneOnline;

        public RistoranteOggetto(ResultSet rs) throws SQLException {
            this.id = rs.getString("id_ristorante");
            this.nome = rs.getString("nome");
            this.citta = rs.getString("citta");
            this.nazione = rs.getString("nazione");
            this.indirizzo = rs.getString("indirizzo");
            this.cucina = rs.getString("tipologia_cucina");
            this.prezzo = rs.getInt("prezzo_medio");
            this.lat = rs.getDouble("latitudine");
            this.lon = rs.getDouble("longitudine");
            this.delivery = rs.getBoolean("delivery");
            this.prenotazioneOnline = true;
            try {
                this.distanzaKm = parseDouble(rs.getString("distanza_km"));
            } catch (Exception e) {
                this.distanzaKm = 0.0;
            }

            double mediaStelle = 0;
            try {
                mediaStelle = rs.getDouble("media_stelle");
            } catch (Exception e) {
                try {
                    mediaStelle = rs.getInt("stellato");
                } catch (Exception ex) {
                }
            }
            this.mediaStelleReale = mediaStelle;
            this.stelleIntere = (int) Math.round(mediaStelle);

            int nRec = 0;
            try {
                nRec = rs.getInt("num_recensioni");
            } catch (Exception e) {
                /* colonna non presente in questa query */ }
            this.numRecensioni = nRec;
        }

        public RistoranteOggetto(java.util.Map<String, String> data) {
            this.id = data.getOrDefault("id_ristorante", "");
            this.nome = data.getOrDefault("nome", "");
            this.citta = data.getOrDefault("citta", "");
            this.nazione = data.getOrDefault("nazione", "");
            this.indirizzo = data.getOrDefault("indirizzo", "");
            this.cucina = data.getOrDefault("tipologia_cucina", "Internazionale");
            this.prezzo = parseInt(data.getOrDefault("prezzo_medio", "0"));
            this.lat = parseDouble(data.getOrDefault("latitudine", "0"));
            this.lon = parseDouble(data.getOrDefault("longitudine", "0"));
            this.delivery = parseBoolean(data.getOrDefault("delivery", "false"));
            this.prenotazioneOnline = true;
            this.distanzaKm = parseDouble(data.getOrDefault("distanza_km", "0"));

            double mediaStelle = parseDouble(data.getOrDefault("media_stelle", "0"));
            this.mediaStelleReale = mediaStelle;
            this.stelleIntere = (int) Math.round(mediaStelle);

            this.numRecensioni = parseInt(data.getOrDefault("num_recensioni", "0"));
        }

        private static int parseInt(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

            private static double parseDouble(String value) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        private static boolean parseBoolean(String value) {
            return "true".equalsIgnoreCase(value) || "1".equals(value);
        }
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @FXML
    public void initialize() {
        apiClient = new ServerApiClient();
        if (filterStelle != null) {
            filterStelle.setItems(FXCollections.observableArrayList("Tutti", "1 ★ o più", "2 ★ o più", "3 ★ o più",
                    "4 ★ o più", "5 ★"));
        }
        if (filterOrdine != null) {
            filterOrdine.setItems(FXCollections.observableArrayList(
                    "Predefinito",
                    "Prezzo: Più economico",
                    "Prezzo: Più caro",
                    "Nome: A-Z",
                    "Distanza: più vicini",
                    "Miglior valutazione",
                    "Più popolari"
            ));
        }
        if (nearbyRadiusCombo != null) {
            nearbyRadiusCombo.setItems(FXCollections.observableArrayList(5, 10, 20, 50));
            nearbyRadiusCombo.setValue(10);
        }
    }

    @FXML
    private void goToHome(ActionEvent event) {
        Navigator.getInstance().navigateToHomeIntelligent();
    }

    @FXML
    private void OnCercaPremuto() {
        if (searchInlineField == null)
            return;
        String query = searchInlineField.getText().trim();
        if (filterCitta != null)
            filterCitta.clear();
        if (filterPrezzo != null)
            filterPrezzo.clear();
        String ordine = (filterOrdine != null) ? filterOrdine.getValue() : null;
        eseguiRicercaDinamica(null, query, null, null, null, ordine);
    }

    @FXML
    private void onAggiornaFiltriPremuto() {
        String citta = (filterCitta != null) ? filterCitta.getText().trim() : null;
        String prezzoText = (filterPrezzo != null) ? filterPrezzo.getText().trim() : "";
        String stelleString = (filterStelle != null) ? filterStelle.getValue() : null;
        String ordine = (filterOrdine != null) ? filterOrdine.getValue() : null;
        Boolean deliveryFilter = (filterDelivery != null && filterDelivery.isSelected()) ? Boolean.TRUE : null;
        Boolean prenotazioneFilter = (filterPrenotazione != null && filterPrenotazione.isSelected()) ? Boolean.TRUE : null;

        String tipoCucina = (searchInlineField != null) ? searchInlineField.getText().trim() : null;
        if (tipoCucina != null && tipoCucina.isEmpty())
            tipoCucina = null;

        Integer prezzoMaxParam = null;
        if (!prezzoText.isEmpty()) {
            try {
                prezzoMaxParam = Integer.parseInt(prezzoText);
            } catch (NumberFormatException e) {
            }
        }

        Double minStelleParam = null;
        if (stelleString != null && !stelleString.isEmpty() && !stelleString.contains("Tutti")) {
            String pulito = stelleString.replaceAll("[^0-9]", "");
            if (!pulito.isEmpty())
                minStelleParam = Double.parseDouble(pulito);
        }

        eseguiRicercaDinamica(citta, tipoCucina, null, prezzoMaxParam, minStelleParam, deliveryFilter, prenotazioneFilter, ordine);
    }

    @FXML
    private void onResetFiltriPremuto() {
        if (filterCitta != null)
            filterCitta.clear();
        if (filterPrezzo != null)
            filterPrezzo.clear();
        if (filterStelle != null)
            filterStelle.setValue("Tutti");
        if (filterOrdine != null)
            filterOrdine.setValue("Predefinito");
        if (filterDelivery != null)
            filterDelivery.setSelected(false);
        if (filterPrenotazione != null)
            filterPrenotazione.setSelected(false);
        if (filterAperto != null)
            filterAperto.setSelected(false);
        if (searchInlineField != null)
            searchInlineField.clear();
        eseguiRicercaDinamica(null, null, null, null, null, null, null, null);
    }

    @FXML
    private void onNearbySearchPremuto() {
        int userId = Navigator.getInstance().getIdUtenteLoggato();
        if (userId <= 0) {
            showError("Effettua prima il login per cercare i ristoranti vicino a te.");
            return;
        }

        // WORKAROUND: Use hardcoded Milan coordinates if profile fetch fails
        // (User already has these in database: lat=45.464200, lon=9.190000)
        double lat = 45.464200;
        double lon = 9.190000;
        String domicilio = "Milan";
        int radiusKm = nearbyRadiusCombo != null && nearbyRadiusCombo.getValue() != null ? nearbyRadiusCombo.getValue() : 10;

        // Try to get actual profile coordinates, but use Milan as fallback
        try {
            if (!apiClient.isConnected()) {
                apiClient.connect();
            }
            if (apiClient.isConnected()) {
                try {
                    String profileResponse = apiClient.sendRequest("GET_USER_PROFILE:" + userId);
                    if (profileResponse != null && profileResponse.startsWith("GET_USER_PROFILE_OK:")) {
                        java.util.Map<String, String> userData = ResponseParser.parseRowData(profileResponse.substring("GET_USER_PROFILE_OK:".length()));
                        double dbLat = parseDouble(userData.getOrDefault("lat_domicilio", "0"));
                        double dbLon = parseDouble(userData.getOrDefault("lon_domicilio", "0"));
                        if (dbLat != 0 && dbLon != 0) {
                            lat = dbLat;
                            lon = dbLon;
                            domicilio = userData.getOrDefault("luogo_domicilio", "Unknown");
                        }
                    }
                } catch (IOException e) {
                    // Fallback to Milan coordinates if profile fetch fails.
                }
            }
        } catch (Exception e) {
            // Keep Milan fallback if the profile request fails.
        }

        String cityFilter = (filterCitta != null && !filterCitta.getText().trim().isEmpty()) ? filterCitta.getText().trim() : null;
        String cuisineFilter = (searchInlineField != null && !searchInlineField.getText().trim().isEmpty()) ? searchInlineField.getText().trim() : null;
        eseguiRicercaDinamica(cityFilter, cuisineFilter, null, null, null, "Distanza: più vicini", lat, lon, (double) radiusKm);
        // Note: Don't set filterOrdine here - it triggers onAggiornaFiltriPremuto which overwrites geo search results!
    }

    public void inizializzaRicercaGlobale(String testoCercato) {
        if (searchInlineField != null)
            searchInlineField.setText(testoCercato);
        eseguiRicercaDinamica(null, testoCercato, null, null, null, null);
    }

    public void inizializzaRicercaAvanzata(String citta, String prezzoMax, String stelle, String ordine) {
        if (filterCitta != null)
            filterCitta.setText(citta);
        if (filterPrezzo != null)
            filterPrezzo.setText(prezzoMax);
        if (filterStelle != null)
            filterStelle.setValue(stelle);
        if (filterOrdine != null)
            filterOrdine.setValue(ordine);

        Integer prezzoMaxParam = null;
        if (prezzoMax != null && !prezzoMax.isEmpty()) {
            try {
                prezzoMaxParam = Integer.parseInt(prezzoMax);
            } catch (Exception e) {
            }
        }

        Double minStelleParam = null;
        if (stelle != null && !stelle.isEmpty() && !stelle.contains("Tutti")) {
            String pulito = stelle.replaceAll("[^0-9]", "");
            if (!pulito.isEmpty())
                minStelleParam = Double.parseDouble(pulito);
        }

        eseguiRicercaDinamica(citta, null, null, prezzoMaxParam, minStelleParam, ordine);
    }

    private void eseguiRicercaDinamica(String citta, String tipoCucina, Integer prezzoMin, Integer prezzoMax,
            Double minStelle, String ordine) {
        eseguiRicercaDinamica(citta, tipoCucina, prezzoMin, prezzoMax, minStelle, null, null, ordine, null, null, null);
    }

    private void eseguiRicercaDinamica(String citta, String tipoCucina, Integer prezzoMin, Integer prezzoMax,
            Double minStelle, Boolean delivery, Boolean prenotazione, String ordine) {
        eseguiRicercaDinamica(citta, tipoCucina, prezzoMin, prezzoMax, minStelle, delivery, prenotazione, ordine, null, null, null);
    }

    private void eseguiRicercaDinamica(String citta, String tipoCucina, Integer prezzoMin, Integer prezzoMax,
            Double minStelle, String ordine, Double lat, Double lon, Double radiusKm) {
        eseguiRicercaDinamica(citta, tipoCucina, prezzoMin, prezzoMax, minStelle, null, null, ordine, lat, lon, radiusKm);
    }

    private void eseguiRicercaDinamica(String citta, String tipoCucina, Integer prezzoMin, Integer prezzoMax,
            Double minStelle, Boolean delivery, Boolean prenotazione, String ordine,
            Double lat, Double lon, Double radiusKm) {
        // preserve current results in case of failure so UI doesn't disappear
        java.util.List<RistoranteOggetto> backup = new ArrayList<>(tuttiIRistoranti);
        tuttiIRistoranti.clear();
        paginaCorrente = 1;
        if (!apiClient.isConnected()) {
            if (!apiClient.ensureConnected()) {
                System.err.println("[SearchController] Impossibile connettersi al server");
                tuttiIRistoranti.clear();
                tuttiIRistoranti.addAll(backup);
                aggiornaInterfacciaVisiva();
                showError("Impossibile connettersi al server.");
                return;
            }
        }

        String request = "SEARCH_RISTORANTI:" +
                (citta != null ? citta : "") + ":" +
                (tipoCucina != null ? tipoCucina : "") + ":" +
                (prezzoMin != null ? prezzoMin : "") + ":" +
                (prezzoMax != null ? prezzoMax : "") + ":" +
                (delivery != null ? delivery.toString() : "") + ":" +
                (prenotazione != null ? prenotazione.toString() : "") + ":" +
                (minStelle != null ? minStelle.toString() : "") + ":" +
                (lat != null ? lat : "") + ":" +
                (lon != null ? lon : "") + ":" +
                (radiusKm != null ? radiusKm : "");

        String response = null;
        try {
            response = apiClient.sendRequest(request);
        } catch (IOException e) {
            System.err.println("[SearchController] Errore invio richiesta: " + e.getMessage());
            tuttiIRistoranti.clear();
            tuttiIRistoranti.addAll(backup);
            aggiornaInterfacciaVisiva();
            showError("Errore di connessione: " + e.getMessage());
            return;
        }

        if (response == null) {
            System.err.println("[SearchController] Nessuna risposta dal server");
            tuttiIRistoranti.clear();
            tuttiIRistoranti.addAll(backup);
            aggiornaInterfacciaVisiva();
            showError("Nessuna risposta dal server.");
            return;
        }

        if (response.startsWith("SEARCH_RISTORANTI_OK:")) {
            String data = response.substring("SEARCH_RISTORANTI_OK:".length());

            if (!data.isEmpty()) {
                String[] rows = data.split(";");
                for (String row : rows) {
                    if (row.trim().isEmpty()) continue;
                    try {
                        java.util.Map<String, String> rowData = ResponseParser.parseRowData(row);
                        RistoranteOggetto ristorante = new RistoranteOggetto(rowData);
                        tuttiIRistoranti.add(ristorante);
                    } catch (Exception e) {
                        System.err.println("[SearchController] Errore parsing riga: " + e.getMessage());
                    }
                }
            }
        } else if (response.startsWith("SEARCH_RISTORANTI_FAIL:")) {
            System.err.println("[SearchController] Server returned error: " + response.substring("SEARCH_RISTORANTI_FAIL:".length()));
            tuttiIRistoranti.clear();
            tuttiIRistoranti.addAll(backup);
        } else {
            System.err.println("[SearchController] Unexpected response format: " + response.substring(0, Math.min(60, response.length())));
        }

        applicaOrdinamento(ordine);
        Platform.runLater(() -> aggiornaInterfacciaVisiva());
    }

    private void applicaOrdinamento(String ordine) {
        if (ordine == null || ordine.isEmpty() || ordine.contains("Predefinito")) {
            if (tuttiIRistoranti.stream().anyMatch(r -> r.distanzaKm > 0)) {
                tuttiIRistoranti.sort((a, b) -> Double.compare(a.distanzaKm, b.distanzaKm));
            }
            return;
        }

        if (ordine.contains("economico")) {
            tuttiIRistoranti.sort((a, b) -> Integer.compare(a.prezzo, b.prezzo));
        } else if (ordine.contains("caro") || ordine.contains("esclusivo")) {
            tuttiIRistoranti.sort((a, b) -> Integer.compare(b.prezzo, a.prezzo));
        } else if (ordine.contains("A-Z") || ordine.contains("Alfabetico") || ordine.contains("Nome")) {
            tuttiIRistoranti.sort((a, b) -> a.nome.compareToIgnoreCase(b.nome));
        } else if (ordine.contains("valutazione") || ordine.contains("Miglior")) {
            tuttiIRistoranti.sort((a, b) -> {
                int byRating = Double.compare(b.mediaStelleReale, a.mediaStelleReale);
                if (byRating != 0) return byRating;
                return Integer.compare(b.numRecensioni, a.numRecensioni);
            });
        } else if (ordine.contains("popolari")) {
            tuttiIRistoranti.sort((a, b) -> {
                int byReviews = Integer.compare(b.numRecensioni, a.numRecensioni);
                if (byReviews != 0) return byReviews;
                return Double.compare(b.mediaStelleReale, a.mediaStelleReale);
            });
        } else if (ordine.contains("Distanza") || ordine.contains("vicini")) {
            tuttiIRistoranti.sort((a, b) -> Double.compare(a.distanzaKm, b.distanzaKm));
        }
    }

    private String cittaParam(String c) {
        return (c == null || c.isEmpty()) ? null : c;
    }

    private String cucinaParam(String c) {
        return (c == null || c.isEmpty()) ? null : c;
    }

    private void handleTogglePreferito(RistoranteOggetto r, Button btnFav) {
        try {
            int idUtenteLoggato = Navigator.getInstance().getIdUtenteLoggato();

            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    showError("Impossibile connettersi al server");
                    return;
                }
            }

            // Controlla se è già preferito
            String checkResponse = apiClient.sendRequest("IS_PREFERITO:" + idUtenteLoggato + ":" + r.id);

            if (checkResponse == null) {
                showError("Nessuna risposta dal server");
                return;
            }
            if (checkResponse.startsWith("ERROR:Sessione")) {
                Navigator.getInstance().logout();
                Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
                showError("Sessione scaduta", "Effettua nuovamente il login per continuare.");
                return;
            }

            if (checkResponse.startsWith("IS_PREFERITO_OK:")) {
                boolean isPreferito = checkResponse.contains("true");

                if (isPreferito) {
                    // Rimuovi dai preferiti
                    String response = apiClient.sendRequest("REMOVE_PREFERITO:" + idUtenteLoggato + ":" + r.id);
                    if (response == null) {
                        showError("Nessuna risposta dal server");
                        return;
                    }
                    if (response.startsWith("ERROR:Sessione")) {
                        Navigator.getInstance().logout();
                        Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
                        showError("Sessione scaduta", "Effettua nuovamente il login per continuare.");
                        return;
                    }
                    if (response.startsWith("REMOVE_PREFERITO_OK:")) {
                        btnFav.setText("♡");
                        System.out.println("[PREFERITI] Rimosso: " + r.nome);
                        showToast("Rimosso dai preferiti");
                    } else {
                        showError("Errore nella rimozione del preferito");
                    }
                } else {
                    // Aggiungi ai preferiti
                    String response = apiClient.sendRequest("ADD_PREFERITO:" + idUtenteLoggato + ":" + r.id);
                    if (response == null) {
                        showError("Nessuna risposta dal server");
                        return;
                    }
                    if (response.startsWith("ERROR:Sessione")) {
                        Navigator.getInstance().logout();
                        Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
                        showError("Sessione scaduta", "Effettua nuovamente il login per continuare.");
                        return;
                    }
                    if (response.startsWith("ADD_PREFERITO_OK:")) {
                        btnFav.setText("♥");
                        System.out.println("[PREFERITI] Aggiunto: " + r.nome);
                        showToast("Aggiunto ai preferiti");
                    } else {
                        showError("Errore nell'aggiunta del preferito");
                    }
                }
            } else {
                showError("Errore nel controllo del preferito");
            }
        } catch (IOException e) {
            System.err.println("[SearchController] Errore di connessione: " + e.getMessage());
            showError("Errore di connessione al server");
        } catch (Exception e) {
            System.err.println("[SearchController] Errore durante il toggle del preferito: " + e.getMessage());
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("Impossibile salvare i preferiti");
            alert.setContentText("Assicurati di aver effettuato il login.");
            alert.showAndWait();
        }
    }

    private void showToast(String message) {
        if (message == null || message.isBlank()) return;
        if (containerRisultati == null || containerRisultati.getScene() == null) {
            showError("Info", message);
            return;
        }

        Label toast = new Label(message);
        toast.setStyle("-fx-background-color: rgba(15,23,42,0.92); -fx-text-fill: white; -fx-padding: 10 16; -fx-background-radius: 10; -fx-font-weight: bold; -fx-font-size: 12px;");

        if (toastPopup != null) {
            toastPopup.hide();
        }

        toastPopup = new Popup();
        toastPopup.getContent().add(toast);

        var bounds = containerRisultati.getScene().getRoot().localToScreen(containerRisultati.getBoundsInLocal());
        double x = bounds.getMinX() + Math.max(0, (bounds.getWidth() - 180) / 2);
        double y = bounds.getMinY() + 18;
        toastPopup.setX(x);
        toastPopup.setY(y);
        toastPopup.show(containerRisultati.getScene().getWindow());

        if (toastTimer != null) {
            toastTimer.stop();
        }
        toastTimer = new PauseTransition(Duration.seconds(2.2));
        toastTimer.setOnFinished(e -> toastPopup.hide());
        toastTimer.play();
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title == null || title.isEmpty() ? "Errore" : title);
        alert.setContentText(message == null ? "" : message);
        alert.showAndWait();
    }

    private void aggiornaInterfacciaVisiva() {
        System.out.println("[SearchController] aggiornaInterfacciaVisiva called. Ristoranti: " + tuttiIRistoranti.size());
        System.out.println("[SearchController]   containerRisultati is: " + (containerRisultati == null ? "NULL" : "OK"));
         
        if (containerRisultati == null) {
            System.err.println("[SearchController] ERROR: containerRisultati is NULL! UI won't update");
            return;
        }
         
        containerRisultati.getChildren().clear();

        if (tuttiIRistoranti.isEmpty()) {
            VBox boxVuoto = new VBox();
            boxVuoto.setStyle("-fx-padding: 40; -fx-alignment: center;");
            Text msg = new Text("❌ Nessun ristorante trovato con i criteri inseriti.");
            msg.setStyle("-fx-fill: #94A3B8; -fx-font-size: 15; -fx-font-style: italic;");
            boxVuoto.getChildren().add(msg);
            containerRisultati.getChildren().add(boxVuoto);
            if (containerPaginazione != null)
                containerPaginazione.getChildren().clear();
            System.out.println("[SearchController] No results found message displayed");
            return;
        }

        int indiceInizio = (paginaCorrente - 1) * ELEMENTI_PER_PAGINA;
        int indiceFine = Math.min(indiceInizio + ELEMENTI_PER_PAGINA, tuttiIRistoranti.size());

        for (int i = indiceInizio; i < indiceFine; i++) {
            RistoranteOggetto r = tuttiIRistoranti.get(i);

            // ==========================================
            // CREAZIONE CARD PRINCIPALE (HBox)
            // ==========================================
            HBox card = new HBox(18);
            card.getStyleClass().add("restaurant-card");
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(18));

            // --- 1. THUMBNAIL (Icona e Sfondo basati sulla cucina) ---
            VBox thumb = new VBox();
            thumb.setAlignment(Pos.CENTER);
            thumb.setMinSize(88, 88);
            thumb.setMaxSize(88, 88);

            String tipoCucina = r.cucina != null ? r.cucina : "Internazionale";
            String classeThumb = getThumbClass(tipoCucina);
            String iconaThumb = getThumbIcon(tipoCucina);

            thumb.getStyleClass().addAll("restaurant-card-thumb", classeThumb);
            Label lblIcona = new Label(iconaThumb);
            lblIcona.getStyleClass().add("restaurant-card-thumb-icon");
            thumb.getChildren().add(lblIcona);

            // --- 2. CORPO CENTRALE DEI DETTAGLI (VBox) ---
            VBox dettagliBox = new VBox(8);
            HBox.setHgrow(dettagliBox, Priority.ALWAYS);

            // Riga 1: Nome, Badge Consigliato (se >= 4.5 stelle), Prezzo
            HBox row1 = new HBox(10);
            row1.setAlignment(Pos.CENTER_LEFT);
            Label lblNome = new Label(r.nome);
            lblNome.getStyleClass().add("restaurant-card-name");

            row1.getChildren().add(lblNome);

            if (r.stelleIntere >= 4) {
                Label badgeConsigliato = new Label("★ Consigliato");
                badgeConsigliato.getStyleClass().add("badge-featured");
                row1.getChildren().add(badgeConsigliato);
            }

            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);
            Label lblPrezzo = new Label("💰 Spesa Media: " + r.prezzo + "€");
            lblPrezzo.getStyleClass().add("restaurant-card-price-badge");
            row1.getChildren().addAll(spacer1, lblPrezzo);

            // Riga 2: Gestore, Cucina
            HBox row2 = new HBox(8);
            row2.setAlignment(Pos.CENTER_LEFT);
            Label lblOwner = new Label("👤 TheKnife Partner"); // Si può agganciare al DB
            lblOwner.getStyleClass().add("restaurant-card-owner");
            Label dot1 = new Label("•");
            dot1.getStyleClass().add("restaurant-card-dot");
            Label lblCucinaChip = new Label("Cucina " + tipoCucina);
            lblCucinaChip.getStyleClass().add("restaurant-card-cuisine-chip");
            row2.getChildren().addAll(lblOwner, dot1, lblCucinaChip);

            // Riga 3: Posizione
            HBox row3 = new HBox(8);
            row3.setAlignment(Pos.CENTER_LEFT);
            Label lblMeta = new Label("📍 " + r.citta + " (" + r.nazione + ")");
            lblMeta.getStyleClass().add("restaurant-card-meta");
            row3.getChildren().addAll(lblMeta);

            // Riga 4: Stelle, Recensioni, Tags
            HBox row4 = new HBox(4);
            row4.setAlignment(Pos.CENTER_LEFT);
            row4.getStyleClass().add("rating-stars-row");

            // Generazione dinamica delle stelle
            for (int j = 1; j <= 5; j++) {
                Label star = new Label("★");
                star.getStyleClass().add(j <= r.stelleIntere ? "star-filled" : "star-empty");
                row4.getChildren().add(star);
            }

            Label lblAvg = new Label(r.numRecensioni > 0 ? String.format("%.1f", r.mediaStelleReale) : "Nuovo");
            // Media
            lblAvg.getStyleClass().add(r.stelleIntere > 0 ? "rating-avg-num" : "rating-avg-num-muted");
            row4.getChildren().add(lblAvg);

            Label lblNumRec = new Label(r.numRecensioni > 0 ? "(" + r.numRecensioni + ")" : "");
            lblNumRec.getStyleClass().add("rating-count");
            row4.getChildren().add(lblNumRec);

            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            row4.getChildren().add(spacer2);

            if (r.delivery) {
                Label tagDelivery = new Label("🛵 Delivery");
                tagDelivery.getStyleClass().add("restaurant-card-tag");
                row4.getChildren().add(tagDelivery);
            }
            if (r.prenotazioneOnline) {
                Label tagPrenotabile = new Label("📅 Prenotabile");
                tagPrenotabile.getStyleClass().add("restaurant-card-tag");
                row4.getChildren().add(tagPrenotabile);
            }

            dettagliBox.getChildren().addAll(row1, row2, row3, row4);

            // --- 3. BOTTONI AZIONE LATERALI ---
            // --- 3. BOTTONI AZIONE LATERALI ---
            VBox azioniBox = new VBox(8);
            azioniBox.setAlignment(Pos.CENTER);

            Button btnFav = new Button("♡"); // Stato di default vuoto
            btnFav.getStyleClass().add("btn-card-fav");

            // 1. IMPOSTA LO STATO INIZIALE via API
            try {
                int idUtenteLoggato = Navigator.getInstance().getIdUtenteLoggato();
                if (!apiClient.isConnected()) {
                    if (!apiClient.ensureConnected()) {
                        // Resto con il default
                    } else {
                        String response = apiClient.sendRequest("IS_PREFERITO:" + idUtenteLoggato + ":" + r.id);
                        if (response.startsWith("IS_PREFERITO_OK:") && response.contains("true")) {
                            btnFav.setText("♥"); // Cuore pieno se è già nei preferiti
                        }
                    }
                } else {
                    String response = apiClient.sendRequest("IS_PREFERITO:" + idUtenteLoggato + ":" + r.id);
                    if (response.startsWith("IS_PREFERITO_OK:") && response.contains("true")) {
                        btnFav.setText("♥"); // Cuore pieno se è già nei preferiti
                    }
                }
            } catch (Exception e) {
                // Gestione silenziosa (es. se l'utente non è loggato, il cuore resta vuoto "♡")
            }

            // 2. COLLEGA IL METODO AL CLICK DEL BOTTONE
            btnFav.setOnAction(e -> {
                // Consuma l'evento in modo che il click sul bottone non attivi erroneamente il
                // click sull'intera card
                e.consume();
                handleTogglePreferito(r, btnFav);
            });

            Button btnDettagli = new Button("Vedi dettagli");
            btnDettagli.getStyleClass().add("btn-card-cta");

            // Navigazione ai dettagli
            btnDettagli.setOnAction(e -> navigaADettagli(r));
            // L'intera card è cliccabile
            card.setOnMouseClicked(e -> navigaADettagli(r));
            card.setStyle("-fx-cursor: hand;");

            azioniBox.getChildren().addAll(btnFav, btnDettagli);

            // Assemblaggio finale della card
            card.getChildren().addAll(thumb, dettagliBox, azioniBox);

            // Effetto Hover dinamico (aggiungi una classe hover nel tuo CSS se preferisci)
            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F8FAFC; -fx-cursor: hand;"));
            card.setOnMouseExited(e -> card.setStyle(""));

            containerRisultati.getChildren().add(card);
        }
        disegnaBarraPaginazione();
    }

    // ==========================================
    // METODI DI SUPPORTO
    // ==========================================

    // 1. Gestisce il click sulla card del ristorante rimandando al Navigator
    // modificato
    private void navigaADettagli(RistoranteOggetto r) {
        System.out.println("[NAVIGAZIONE] Richiesta apertura dettagli per: " + r.nome);
        Navigator.getInstance().navigateToRestaurantDetails(r);
    }

    // Mappa capillarmente la stringa del DB a una classe CSS con colori dedicati
    private String getThumbClass(String cucina) {
        if (cucina == null)
            return "thumb-altro";
        String lower = cucina.toLowerCase();

        // 1. Italiana & Pizza
        if (lower.contains("italian") || lower.contains("pizza") || lower.contains("primi") || lower.contains("pasta"))
            return "thumb-italiana";

        // 2. Asiatica & Etnica Orientale
        if (lower.contains("giapponese") || lower.contains("sushi") || lower.contains("asian")
                || lower.contains("cinese") || lower.contains("coreano"))
            return "thumb-giapponese";
        if (lower.contains("thai") || lower.contains("poke") || lower.contains("hawaiian")
                || lower.contains("vietnamita") || lower.contains("ramen"))
            return "thumb-poke";
        if (lower.contains("indian") || lower.contains("curry"))
            return "thumb-indiana";

        // 3. Green & Healthy
        if (lower.contains("veg") || lower.contains("insalata") || lower.contains("salad")
                || lower.contains("salutista"))
            return "thumb-vegetariana";
        if (lower.contains("mediterranea") || lower.contains("mediterranean"))
            return "thumb-mediterranea";

        // 4. Europee Specifiche
        if (lower.contains("greco") || lower.contains("greca") || lower.contains("greek"))
            return "thumb-greca";
        if (lower.contains("spagnol") || lower.contains("tapas") || lower.contains("paella"))
            return "thumb-spagnola";
        if (lower.contains("francese") || lower.contains("french"))
            return "thumb-francese";
        if (lower.contains("kebab") || lower.contains("turco") || lower.contains("arabo")
                || lower.contains("medio orientale") || lower.contains("libanese"))
            return "thumb-arabo";

        // 5. Ciccia, Pesce & Grill
        if (lower.contains("meat") || lower.contains("steak") || lower.contains("carne") || lower.contains("bbq")
                || lower.contains("grill"))
            return "thumb-carne";
        if (lower.contains("pesce") || lower.contains("seafood") || lower.contains("fish") || lower.contains("mare")
                || lower.contains("crostacei"))
            return "thumb-pesce";
        if (lower.contains("messicano") || lower.contains("mexican") || lower.contains("taco")
                || lower.contains("piccante") || lower.contains("burrito"))
            return "thumb-messicana";

        // 6. Fast Food & Paninoteche
        if (lower.contains("burger") || lower.contains("fast food") || lower.contains("americano")
                || lower.contains("american") || lower.contains("patatine") || lower.contains("hot dog"))
            return "thumb-fastfood";
        if (lower.contains("street") || lower.contains("panin") || lower.contains("piadina") || lower.contains("toast"))
            return "thumb-streetfood";

        // 7. Caffetteria, Dolci & Bar
        if (lower.contains("dolci") || lower.contains("dessert") || lower.contains("pasticceria")
                || lower.contains("gelato") || lower.contains("crepe"))
            return "thumb-dolci";
        if (lower.contains("brunch") || lower.contains("colazione") || lower.contains("caff")
                || lower.contains("bakery"))
            return "thumb-brunch";
        if (lower.contains("pub") || lower.contains("birra") || lower.contains("cocktail") || lower.contains("bar")
                || lower.contains("enoteca") || lower.contains("vino"))
            return "thumb-pub";

        return "thumb-altro";
    }


    private String getThumbIcon(String cucina) {
        if (cucina == null)
            return "👨‍🍳";
        String lower = cucina.toLowerCase();

        // Italiana & Pizza
        if (lower.contains("pizza"))
            return "🍕";
        if (lower.contains("italian") || lower.contains("primi") || lower.contains("pasta"))
            return "🍝";

        // Asiatica & Etnica Orientale
        if (lower.contains("sushi") || lower.contains("giapponese"))
            return "🍣";
        if (lower.contains("cinese") || lower.contains("asian") || lower.contains("ramen") || lower.contains("coreano"))
            return "🍜";
        if (lower.contains("thai") || lower.contains("vietnamita"))
            return "🥡";
        if (lower.contains("poke") || lower.contains("hawaiian"))
            return "🥣";
        if (lower.contains("indian") || lower.contains("curry"))
            return "🍛";

        // Green, Healthy & Mediterranea
        if (lower.contains("veg") || lower.contains("insalata") || lower.contains("salad")
                || lower.contains("mediterranea") || lower.contains("mediterranean"))
            return "🥗";

        // Carne & Grill
        if (lower.contains("steak") || lower.contains("churrasco") || lower.contains("grill"))
            return "🥩";
        if (lower.contains("meat") || lower.contains("carne") || lower.contains("bbq"))
            return "🍖";

        // Pesce
        if (lower.contains("seafood") || lower.contains("crostacei") || lower.contains("aragosta"))
            return "🦞";
        if (lower.contains("pesce") || lower.contains("fish") || lower.contains("mare"))
            return "🐟";

        // Internazionali e Country Specific
        if (lower.contains("messicano") || lower.contains("mexican") || lower.contains("taco")
                || lower.contains("burrito"))
            return "🌮";
        if (lower.contains("piccante"))
            return "🌶️";
        if (lower.contains("kebab") || lower.contains("turco") || lower.contains("arabo")
                || lower.contains("medio orientale") || lower.contains("libanese"))
            return "🥙";
        if (lower.contains("spagnol") || lower.contains("paella") || lower.contains("tapas"))
            return "🥘";
        if (lower.contains("francese") || lower.contains("french"))
            return "🥐";
        if (lower.contains("greco") || lower.contains("greca") || lower.contains("greek"))
            return "🫒";

        // Fast Food & Street Food
        if (lower.contains("burger"))
            return "🍔";
        if (lower.contains("fast food") || lower.contains("americano") || lower.contains("patatine"))
            return "🍟";
        if (lower.contains("hot dog"))
            return "🌭";
        if (lower.contains("street") || lower.contains("panin") || lower.contains("piadina") || lower.contains("toast"))
            return "🥪";

        // Dolci & Caffetteria
        if (lower.contains("gelato"))
            return "🍦";
        if (lower.contains("dolci") || lower.contains("dessert") || lower.contains("pasticceria"))
            return "🍰";
        if (lower.contains("brunch") || lower.contains("colazione") || lower.contains("bakery")
                || lower.contains("crepe"))
            return "🥐";
        if (lower.contains("caff"))
            return "☕";

        // Drink & Pub
        if (lower.contains("pub") || lower.contains("birra"))
            return "🍺";
        if (lower.contains("cocktail") || lower.contains("bar") || lower.contains("enoteca") || lower.contains("vino"))
            return "🍷";

        return "👨‍🍳"; // Estremo ripiego: il cuoco al posto del piatto piatto 🍽️
    }

    private void disegnaBarraPaginazione() {
        if (containerPaginazione == null)
            return;
        containerPaginazione.getChildren().clear();

        int totalePagine = (int) Math.ceil((double) tuttiIRistoranti.size() / ELEMENTI_PER_PAGINA);
        if (totalePagine <= 1)
            return;

        int startPage = Math.max(1, paginaCorrente - 2);
        int endPage = Math.min(totalePagine, paginaCorrente + 2);

        if (paginaCorrente > 1) {
            Button btnPrecedente = new Button("←");
            btnPrecedente.getStyleClass().add("pagination-button-inactive");
            btnPrecedente.setOnAction(e -> {
                paginaCorrente--;
                aggiornaInterfacciaVisiva();
            });
            containerPaginazione.getChildren().add(btnPrecedente);
        }

        for (int p = startPage; p <= endPage; p++) {
            final int numeroPagina = p;
            Button btnPagina = new Button(String.valueOf(p));
            if (p == paginaCorrente) {
                btnPagina.getStyleClass().add("pagination-button-active");
            } else {
                btnPagina.getStyleClass().add("pagination-button-inactive");
            }
            btnPagina.setOnAction(e -> {
                paginaCorrente = numeroPagina;
                aggiornaInterfacciaVisiva();
            });
            containerPaginazione.getChildren().add(btnPagina);
        }

        if (paginaCorrente < totalePagine) {
            Button btnSuccessiva = new Button("→");
            btnSuccessiva.getStyleClass().add("pagination-button-inactive");
            btnSuccessiva.setOnAction(e -> {
                paginaCorrente++;
                aggiornaInterfacciaVisiva();
            });
            containerPaginazione.getChildren().add(btnSuccessiva);
        }
    }

    @FXML
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        project.controllers.Navigator.getInstance().navigateToHomeIntelligent();
    }

    @FXML
    private void handleGoToProfile(javafx.event.ActionEvent event) {
        project.controllers.Navigator.getInstance().navigateToProfile();
    }

}
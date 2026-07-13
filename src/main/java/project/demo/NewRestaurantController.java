package project.demo;

import db.TheKnifeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    private TheKnifeDAO dao;
    private int idGestoreLoggato;
    private int stelleSelezionate = 0;

    @FXML
    public void initialize() {
        this.navigator = Navigator.getInstance();
        this.dao = navigator.getDao();
        this.idGestoreLoggato = navigator.getIdUtenteLoggato();

        // Controllo di sicurezza: se non loggato, rimanda ad Accedi
        if (idGestoreLoggato == -1) {
            mostraAllerta("Errore Sessione", "Devi prima effettuare il login per aggiungere un ristorante.", Alert.AlertType.ERROR);
            navigator.navigateTo("login-view.fxml", "Accedi");
            return;
        }

        // Carica i dati anagrafici del Gestore
        caricaDatiGestore();

        // Listener reattivo per calcolare le coordinate simulate all'inserimento dell'indirizzo
        txtCitta.textProperty().addListener((obs, oldVal, newVal) -> calcolaCoordinateAutomatiche());
        txtIndirizzo.textProperty().addListener((obs, oldVal, newVal) -> calcolaCoordinateAutomatiche());
    }

    /**
     * Carica i dettagli del Gestore correntemente loggato
     */
    private void caricaDatiGestore() {
        try {
            ResultSet rs = dao.getDatiUtente(idGestoreLoggato);
            if (rs != null && rs.next()) {
                String nome = rs.getString("nome");
                String cognome = rs.getString("cognome");
                String email = rs.getString("email");

                txtNomeGestore.setText(nome + " " + cognome);
                txtEmailGestore.setText(email);

                String iniziali = "";
                if (nome != null && !nome.isEmpty()) iniziali += nome.charAt(0);
                if (cognome != null && !cognome.isEmpty()) iniziali += cognome.charAt(0);
                txtInizialiGestore.setText(iniziali.toUpperCase());
            }
            if (rs != null) rs.close();

            // Recupera il numero di ristoranti già posseduti dal gestore
            ResultSet rsCount = dao.visualizzaRiepilogo(idGestoreLoggato);
            int count = 0;
            while (rsCount != null && rsCount.next()) {
                count++;
            }
            lblKpiRistoranti.setText(String.valueOf(count));
            if (rsCount != null) rsCount.close();

        } catch (SQLException e) {
            System.err.println("[NEW RESTAURANT] Errore nel recupero dei dettagli del gestore.");
            e.printStackTrace();
        }
    }

    /**
     * Calcola deterministicamente le coordinate di latitudine e longitudine simulate
     * per evitare che il database memorizzi valori nulli o non validi.
     */
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

    /**
     * Salva il nuovo ristorante su PostgreSQL
     */
    @FXML
    void handleCreaRistorante(ActionEvent event) {
        String nome = txtNome.getText().trim();
        String nazione = txtNazione.getText().trim();
        String citta = txtCitta.getText().trim();
        String indirizzo = txtIndirizzo.getText().trim();
        String cucinaConEmoji = comboCucina.getValue();
        String prezzoText = txtPrezzoMedio.getText().trim();

        // Validazione minima
        if (nome.isEmpty() || nazione.isEmpty() || citta.isEmpty() || indirizzo.isEmpty() || cucinaConEmoji == null || prezzoText.isEmpty()) {
            mostraAllerta("Campi vuoti", "Tutti i campi obbligatori devono essere compilati.", Alert.AlertType.WARNING);
            return;
        }

        double prezzoMedio;
        try {
            prezzoMedio = Double.parseDouble(prezzoText);
            if (prezzoMedio <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostraAllerta("Dato non valido", "Inserisci un prezzo medio valido maggiore di zero.", Alert.AlertType.WARNING);
            return;
        }

        // Calcola la fascia di prezzo in base al prezzo medio inserito
        int fasciaPrezzo = 1;
        if (prezzoMedio > 30 && prezzoMedio <= 60) fasciaPrezzo = 2;
        else if (prezzoMedio > 60) fasciaPrezzo = 3;

        double lat = 0.0;
        double lon = 0.0;
        try {
            lat = Double.parseDouble(txtLatitudine.getText().trim().replace(",", "."));
            lon = Double.parseDouble(txtLongitudine.getText().trim().replace(",", "."));
        } catch (Exception ex) {
            // Se non valide o vuote, usa dei fallback predefiniti
            lat = 45.4642;
            lon = 9.1900;
        }

        // Rimuove l'emoji iniziale dalla stringa selezionata per salvare solo il testo pulito
        String cucinaPura = cucinaConEmoji.replaceAll("[^a-zA-Z\\s/]", "").trim();

        boolean delivery = chkDelivery.isSelected();
        boolean prenotazioneOnline = chkPrenotazione.isSelected();

        try {
            // Esegue l'inserimento effettivo nel database utilizzando il metodo del tuo DAO
            int righeInserite = dao.aggiungiRistorante(
                    nome,
                    nazione,
                    citta,
                    indirizzo,
                    lat,
                    lon,
                    fasciaPrezzo,
                    delivery,
                    prenotazioneOnline,
                    cucinaPura,
                    idGestoreLoggato
            );

            if (righeInserite > 0) {
                mostraAllerta("Ristorante Creato! 🎉", "Il ristorante '" + nome + "' è stato registrato correttamente.", Alert.AlertType.INFORMATION);
                // Naviga indietro
                handleAnnulla(null);
            } else {
                mostraAllerta("Errore", "Si è verificato un errore durante la registrazione del ristorante.", Alert.AlertType.ERROR);
            }

        } catch (SQLException e) {
            System.err.println("[NEW RESTAURANT] Errore durante l'inserimento nel database.");
            e.printStackTrace();
            mostraAllerta("Errore Database", "Errore nel salvataggio su PostgreSQL: " + e.getMessage(), Alert.AlertType.ERROR);
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
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        navigator.navigateToHomeIntelligent();
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
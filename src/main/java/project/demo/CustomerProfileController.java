package project.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.sql.ResultSet;
import java.sql.SQLException;
import db.TheKnifeDAO;

public class CustomerProfileController {

    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private DatePicker dataNascitaPicker;
    @FXML private TextField domicilioField;
    @FXML private PasswordField passwordField;
    @FXML private Button btnSalvaModifiche;

    private Navigator navigator;
    private TheKnifeDAO dao;
    private int idUtenteLoggato;

    @FXML
    public void initialize() {
        // Recuperiamo l'istanza del navigatore e del DAO di sessione
        this.navigator = Navigator.getInstance();
        this.dao = navigator.getDao();
        this.idUtenteLoggato = navigator.getIdUtenteLoggato();

        // Se l'utente è regolarmente loggato, carichiamo i suoi dati reali
        if (idUtenteLoggato != -1) {
            caricaDatiProfilo();
        } else {
            mostraAllerta("Errore di sessione", "Nessun utente loggato. Effettua prima l'accesso.", Alert.AlertType.ERROR);
            navigator.navigateTo("login-view.fxml", "Accedi");
        }
    }

    /**
     * Carica dinamicamente i dati dell'utente corretto prelevandoli dal database
     */
    /**
     * Carica i dati del profilo dal database e popola i campi della vista.
     */
    private void caricaDatiProfilo() {
        try {
            // Esegue la query tramite il DAO per ottenere i dati dell'utente loggato
            ResultSet rs = dao.getDatiUtente(idUtenteLoggato);

            if (rs != null && rs.next()) {
                nomeField.setText(rs.getString("nome"));
                cognomeField.setText(rs.getString("cognome"));

                // Gestione della data di nascita
                java.sql.Date dataSql = rs.getDate("data_nascita");
                if (dataSql != null) {
                    dataNascitaPicker.setValue(dataSql.toLocalDate());
                } else {
                    dataNascitaPicker.setValue(null);
                }

                // CORREZIONE RIGA 53: Cambiato da "domicilio" a "luogo_domicilio"
                String indirizzoDomicilio = rs.getString("luogo_domicilio");
                domicilioField.setText(indirizzoDomicilio != null ? indirizzoDomicilio : "");
            }

        } catch (SQLException e) {
            System.err.println("[PROFILO] Errore nel caricamento dei dati dal DB.");
            e.printStackTrace();
            mostraAllerta("Errore di Caricamento", "Impossibile recuperare i dati del tuo profilo dal server.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleSalvaModifiche(ActionEvent event) {
        String nome = nomeField.getText().trim();
        String cognome = cognomeField.getText().trim();
        LocalDate dataNascita = dataNascitaPicker.getValue();
        String domicilio = domicilioField.getText().trim();
        String nuovaPassword = passwordField.getText().trim();

        // Validazione minima dei campi obbligatori
        if (nome.isEmpty() || cognome.isEmpty()) {
            mostraAllerta("Campi mancanti", "Nome e Cognome sono obbligatori.", Alert.AlertType.WARNING);
            return;
        }

        try {
            boolean successo;

            // Convertiamo la data in java.sql.Date solo se è stata selezionata
            java.sql.Date dataSql = (dataNascita != null ? java.sql.Date.valueOf(dataNascita) : null);

            // Se l'utente ha scritto qualcosa nel campo password, aggiorniamo anche quella
            if (!nuovaPassword.isEmpty()) {
                // NOTA: Se usi un sistema di hashing (es. MD5/SHA-256) per la registrazione,
                // ricordati di convertire in hash la stringa 'nuovaPassword' prima di passarla qui!
                successo = dao.updateProfiloUtenteConPassword(
                        idUtenteLoggato,
                        nome,
                        cognome,
                        dataSql,
                        domicilio,
                        0.0, // lat_domicilio (segnaposto)
                        0.0, // lon_domicilio (segnaposto)
                        nuovaPassword
                );
            } else {
                // Altrimenti aggiorniamo solo i dati anagrafici lasciando invariata la password attuale
                successo = dao.updateProfiloUtente(
                        idUtenteLoggato,
                        nome,
                        cognome,
                        dataSql,
                        domicilio,
                        0.0, // lat_domicilio (segnaposto)
                        0.0  // lon_domicilio (segnaposto)
                );
            }

            if (successo) {
                mostraAllerta("Profilo Aggiornato", "Le modifiche sono state salvate con successo nel database!", Alert.AlertType.INFORMATION);
                passwordField.clear(); // Svuota il campo di input password per sicurezza
            } else {
                mostraAllerta("Errore di aggiornamento", "Non è stato possibile salvare le modifiche.", Alert.AlertType.ERROR);
            }

        } catch (SQLException e) {
            System.err.println("[PROFILO] Errore durante il salvataggio su DB.");
            e.printStackTrace();
            mostraAllerta("Errore del Server", "Si è verificato un errore nel salvataggio dei dati.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        System.out.println("Esecuzione del logout utente. Ritorno alla schermata di Login...");
        // Resetta la sessione globale sul Navigator
        navigator.setIdUtenteLoggato(-1);
        navigator.setRuoloUtenteLoggato(null);
        navigator.navigateTo("login-view.fxml", "Accedi");
    }

    @FXML
    void handleCercaRistoranti(ActionEvent event) {
        navigator.navigateTo("search-view-logged.fxml", "Cerca Ristoranti");
    }

    @FXML
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        navigator.navigateToHomeIntelligent();
    }

    /**
     * Utility rapida per mostrare finestre di dialogo pop-up (Informativa, Errore, Avviso)
     */
    private void mostraAllerta(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    @FXML
    void handleVediPreferiti(ActionEvent event) {
        navigator.navigateTo("favorites-view.fxml", "I Miei Preferiti");
    }

    @FXML
    void handleVediRecensioni(ActionEvent event) {
        navigator.navigateTo("reviews-view.fxml", "Le Mie Recensioni");
    }
}
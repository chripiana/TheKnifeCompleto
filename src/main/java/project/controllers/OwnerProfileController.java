package project.controllers;

import project.client.services.ServerApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * OwnerProfileController
 *
 * Controller per la gestione del profilo dell'utente proprietario/gestore.
 * Fornisce le stesse funzionalità del profilo cliente con adattamenti della
 * UI per il ruolo gestore (navbar diverse).
 **/
/**
 * OwnerProfileController
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
public class OwnerProfileController {
    /** Campo nome (bindato da FXML).*/
    @FXML private TextField nomeField;
    /** Campo cognome (bindato da FXML).*/
    @FXML private TextField cognomeField;
    /** DatePicker data di nascita (bindato da FXML).*/
    @FXML private DatePicker dataNascitaPicker;
    /** Campo domicilio (bindato da FXML).*/
    @FXML private TextField domicilioField;
    /** Campo password per eventuale modifica.*/
    @FXML private PasswordField passwordField;
    /** Bottone per salvare modifiche.*/
    @FXML private Button btnSalvaModifiche;

    /** Navbar per utente cliente (visibile solo se ruolo CLIENTE).*/
    @FXML private HBox navbarCliente;
    /** Navbar per gestore (visibile solo se ruolo GESTORE).*/
    @FXML private HBox navbarGestore;
    /** Titolo della pagina adattabile in base al ruolo.*/
    @FXML private Text titoloPagina;

    /** Navigator per navigazione.*/
    private Navigator navigator;
    /** Client di rete.*/
    private ServerApiClient apiClient;
    /** Id utente loggato.*/
    private int idUtenteLoggato;

    @FXML
/**
 * Method: initialize
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void initialize() {
        this.navigator = Navigator.getInstance();
        this.apiClient = new ServerApiClient();
        this.idUtenteLoggato = navigator.getIdUtenteLoggato();

        if (idUtenteLoggato != -1) {
            configuraInterfacciaPerRuolo();
            caricaDatiProfilo();
        } else {
            mostraAllerta("Errore di sessione", "Nessun utente loggato. Effettua prima l'accesso.", Alert.AlertType.ERROR);
            navigator.navigateTo("login-view.fxml", "Accedi");
        }
    }

/**
 * Method: configuraInterfacciaPerRuolo
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void configuraInterfacciaPerRuolo() {
        String ruolo = navigator.getRuoloUtenteLoggato();
        if ("GESTORE".equalsIgnoreCase(ruolo)) {
            navbarCliente.setVisible(false);
            navbarCliente.setManaged(false);

            navbarGestore.setVisible(true);
            navbarGestore.setManaged(true);

            if (titoloPagina != null) {
                titoloPagina.setText("Profilo Ristoratore");
            }
        } else {
            navbarCliente.setVisible(true);
            navbarCliente.setManaged(true);

            navbarGestore.setVisible(false);
            navbarGestore.setManaged(false);
        }
    }

/**
 * Method: caricaDatiProfilo
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void caricaDatiProfilo() {
        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    mostraAllerta("Errore di connessione", "Impossibile connettersi al server", Alert.AlertType.ERROR);
                    return;
                }
            }

            String response = apiClient.sendRequest("GET_USER_PROFILE:" + idUtenteLoggato);

            if (response != null && response.startsWith("GET_USER_PROFILE_OK:")) {
                String data = response.substring("GET_USER_PROFILE_OK:".length());
                if (!data.isEmpty()) {
                    Map<String, String> rowData = parseRowData(data);
                    
                    nomeField.setText(rowData.getOrDefault("nome", ""));
                    cognomeField.setText(rowData.getOrDefault("cognome", ""));

                    String dataNascitaStr = rowData.getOrDefault("data_nascita", "");
                    if (!dataNascitaStr.isEmpty()) {
                        try {
                            dataNascitaPicker.setValue(LocalDate.parse(dataNascitaStr));
                        } catch (Exception e) {
                            dataNascitaPicker.setValue(null);
                        }
                    } else {
                        dataNascitaPicker.setValue(null);
                    }

                    String luogo = rowData.getOrDefault("luogo_domicilio", "");
                    domicilioField.setText(luogo != null ? luogo : "");
                }
            } else if (response != null && response.startsWith("GET_USER_PROFILE_FAIL:")) {
                mostraAllerta("Errore", response.substring("GET_USER_PROFILE_FAIL:".length()), Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            mostraAllerta("Errore di connessione", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleSalvaModifiche(ActionEvent event) {
        String nome = nomeField.getText().trim();
        String cognome = cognomeField.getText().trim();
        LocalDate dataNascita = dataNascitaPicker.getValue();
        String domicilio = domicilioField.getText().trim();
        String nuovaPassword = passwordField.getText().trim();

        if (nome.isEmpty() || cognome.isEmpty()) {
            mostraAllerta("Campi mancanti", "Nome e Cognome sono obbligatori.", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    mostraAllerta("Errore di connessione", "Impossibile connettersi al server", Alert.AlertType.ERROR);
                    return;
                }
            }

            String dataNascitaStr = (dataNascita != null ? dataNascita.toString() : "");

            if (!nuovaPassword.isEmpty()) {
                String changePassResponse = apiClient.sendRequest("CHANGE_PASSWORD:" + idUtenteLoggato + ":" + 
                        ":" + nuovaPassword);
                
                if (changePassResponse == null || !changePassResponse.startsWith("CHANGE_PASSWORD_OK:")) {
                    mostraAllerta("Errore", "Impossibile cambiare la password", Alert.AlertType.ERROR);
                    return;
                }
            }

            String updateResponse = apiClient.sendRequest("UPDATE_USER_PROFILE:" + idUtenteLoggato + ":" + 
                    nome + ":" + cognome + ":" + ":" + dataNascitaStr + ":" + domicilio + ":0:0");

            if (updateResponse != null && updateResponse.startsWith("UPDATE_USER_PROFILE_OK:")) {
                mostraAllerta("Profilo Aggiornato", "Le modifiche sono state salvate con successo!", Alert.AlertType.INFORMATION);
                passwordField.clear();
            } else if (updateResponse != null && updateResponse.startsWith("UPDATE_USER_PROFILE_FAIL:")) {
                mostraAllerta("Errore", updateResponse.substring("UPDATE_USER_PROFILE_FAIL:".length()), Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            mostraAllerta("Errore di connessione", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleTornaIndietroGestore(ActionEvent event) {
        navigator.navigateTo("owner-dashboard-view.fxml", "Dashboard Ristoratore");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        System.out.println("Esecuzione del logout utente. Ritorno alla schermata di Login...");
        navigator.setIdUtenteLoggato(-1);
        navigator.setRuoloUtenteLoggato(null);
        navigator.navigateTo("login-view.fxml", "Accedi");
    }

    @FXML
    void handleCercaRistoranti(ActionEvent event) {
        navigator.navigateTo("search-view-logged.fxml", "Cerca Ristoranti");
    }

    @FXML
/**
 * Method: handleGoToProfile
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleGoToProfile(ActionEvent event) {
        navigator.navigateToProfile();
    }

    @FXML
/**
 * Method: handleGoToHome
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
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

/**
 * Method: mostraAllerta
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
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

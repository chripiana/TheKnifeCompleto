package project.controllers;

import project.client.services.ServerApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller per la gestione del profilo cliente nell'interfaccia JavaFX.
 *
 * Responsabilità:
 * - caricare i dati del profilo utente dal server
 * - permettere la modifica dei campi profilo e l'aggiornamento remoto
 * - fornire feedback all'utente tramite alert
 *
 * Dipendenze principali: ServerApiClient per comunicazione con il server,
 * Navigator per operazioni di navigazione tra viste.
 *
 * Motivazione delle scelte:
 * - Logica di I/O delegata al ServerApiClient per separare UI e networking
 * - Validazioni minime lato client: lasciare controlli più robusti al server
 **/
/**
 * CustomerProfileController
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
public class CustomerProfileController {

    /**
     * Campo testo per il nome dell'utente (bindato dalla view FXML)*/
/**
 * Field: nomeField
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private TextField nomeField;

    /**
     * Campo testo per il cognome dell'utente (bindato dalla view FXML)*/
/**
 * Field: cognomeField
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private TextField cognomeField;

    /**
     * DatePicker per la data di nascita (bindato dalla view FXML)*/
/**
 * Field: dataNascitaPicker
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private DatePicker dataNascitaPicker;

    /**
     * Campo testo per il domicilio dell'utente*/
/**
 * Field: domicilioField
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private TextField domicilioField;

    /**
     * Campo per l'inserimento della nuova password (se richiesta)*/
/**
 * Field: passwordField
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private PasswordField passwordField;

    /**
     * Bottone che attiva il salvataggio delle modifiche*/
/**
 * Field: btnSalvaModifiche
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    @FXML private Button btnSalvaModifiche;

    /**
     * Istanza condivisa del Navigator per la navigazione tra viste*/
/**
 * Field: navigator
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Navigator navigator;

    /**
     * Client di rete per comunicare con il server*/
/**
 * Field: apiClient
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private ServerApiClient apiClient;

    /**
     * Id dell'utente loggato (ottenuto dal Navigator)*/
/**
 * Field: idUtenteLoggato
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private int idUtenteLoggato;

    /**
     * Metodo di inizializzazione invocato da JavaFX dopo il caricamento della view.
     * - istanzia navigator e apiClient
     * - recupera l'id utente loggato e carica il profilo se presente*/
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
            caricaDatiProfilo();
        } else {
            mostraAllerta("Errore di sessione", "Nessun utente loggato. Effettua prima l'accesso.", Alert.AlertType.ERROR);
            navigator.navigateTo("login-view.fxml", "Accedi");
        }
    }

    /**
     * Recupera i dati del profilo dell'utente loggato dal server e popola i
     * campi della view. Esegue controlli di connettività e mostra alert in
     * caso di errore.*/
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

    /**
     * Handler invocato dal bottone "Salva Modifiche". Esegue validazioni
     * basilari sui campi, invia eventuale richiesta di cambio password e
     * quindi aggiorna il profilo utente sul server.*/
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

    /**
     * Navigazione alla Home intelligente (scelta della view in base al ruolo).*/
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

    /**
     * Parser semplice per convertire una riga di risposta del server nel formato
     * key=value|key2=value2 in una mappa. Non fa validazioni avanzate,
     * assume che il server rispetti il formato atteso.*/
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
     * Mostra un alert modale semplice all'utente.*/
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

    /**
     * Esegue il logout locale resettando lo stato nel Navigator e tornando
     * alla schermata di login.*/
    @FXML
    void handleLogout(ActionEvent event) {
        System.out.println("Esecuzione del logout utente. Ritorno alla schermata di Login...");
        navigator.setIdUtenteLoggato(-1);
        navigator.setRuoloUtenteLoggato(null);
        navigator.navigateTo("login-view.fxml", "Accedi");
    }
}
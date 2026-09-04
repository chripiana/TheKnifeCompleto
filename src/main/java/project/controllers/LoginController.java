package project.controllers;

import project.client.services.ServerApiClient;
import project.shared.validation.RequestValidator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.animation.PauseTransition;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * LoginController
 *
 * Controller che gestisce il flusso di autenticazione e registrazione degli
 * utenti. Si occupa di validare input minimo lato client, inviare le
 * richieste al ServerApiClient e instradare l'utente alla vista corretta
 * in base al ruolo restituito dal server.
 **/
/**
 * LoginController
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
public class LoginController {

    /** Client per comunicazione con il server.*/
    private ServerApiClient apiClient;

    // === ELEMENTI GRAFICI LOGIN ===
    /** Campo username/email per il login (bindato da FXML).*/
    @FXML private TextField loginUsername; // Corrisponde all'email nel tuo DB
    /** Campo password per il login (bindato da FXML).*/
    @FXML private PasswordField loginPassword;
    /** Label per mostrare errori di login (bindato da FXML).*/
    @FXML private Label loginErrorLabel;

    // === ELEMENTI GRAFICI REGISTRAZIONE ===
    /** Campo nome durante la registrazione.*/
    @FXML private TextField regNome;
    /** Campo cognome durante la registrazione.*/
    @FXML private TextField regCognome;
    /** Campo email durante la registrazione.*/
    @FXML private TextField regEmail;
    /** Campo password durante la registrazione.*/
    @FXML private PasswordField regPassword;
    /** Campo luogo di domicilio durante la registrazione.*/
    @FXML private TextField regLuogoDomicilio;
    /** DatePicker per la data di nascita (registrazione).*/
    @FXML private DatePicker regDataNascita;
    /** Gruppo toggle che seleziona il tipo di account (cliente/gestore).*/
    @FXML private ToggleGroup tipoAccountGroup;
    /** ToggleButton che rappresenta la scelta 'ristoratore'.*/
    @FXML private ToggleButton btnRistoratore;
    /** Label per mostrare errori durante la registrazione.*/
    @FXML private Label regErrorLabel;

    /**
     * Inizializzazione del controller — crea i client necessari.*/
    @FXML
/**
 * Method: initialize
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void initialize() {
        apiClient = new ServerApiClient();
    }

    @FXML
/**
 * Method: handleLogin
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleLogin() {
        String email = loginUsername.getText().trim();
        String passwordInserita = loginPassword.getText();

        if (email.isEmpty() || passwordInserita.isEmpty()) {
            showLoginError("Inserisci email e password!");
            return;
        }

        // Validazione email
        if (!isValidEmail(email)) {
            showLoginError("Inserisci un'email valida (es: user@example.com)!");
            return;
        }

        try {
            // Verifica connessione al server
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    showLoginError("Impossibile connettersi al server. Riprova più tardi.");
                    return;
                }
            }

            // Invia richiesta di login
            String response = apiClient.sendRequest("LOGIN:" + email + ":" + passwordInserita);

            if (response == null) {
                showLoginError("Nessuna risposta dal server.");
                return;
            }
            if (response.startsWith("LOGIN_OK:")) {
                // Estrai l'ID utente e il ruolo dalla risposta
                String[] parts = response.substring("LOGIN_OK:".length()).split(":");
                if (parts.length >= 2) {
                    int idUtente = Integer.parseInt(parts[0]);
                    String ruolo = parts[1];

                    Navigator.getInstance().setIdUtenteLoggato(idUtente);
                    Navigator.getInstance().setRuoloUtenteLoggato(ruolo);

                    loginErrorLabel.setVisible(false);
                    loginErrorLabel.setManaged(false);

                    if ("CLIENTE".equalsIgnoreCase(ruolo)) {
                        Navigator.getInstance().navigateTo("home-view-logged.fxml", "Home Cliente");
                    } else if ("GESTORE".equalsIgnoreCase(ruolo)) {
                        Navigator.getInstance().navigateTo("home-view-owner.fxml", "Home Ristoratore");
                    }
                } else {
                    showLoginError("Risposta del server non valida.");
                }
            } else if (response.startsWith("LOGIN_FAIL:")) {
                String errMsg = response.substring("LOGIN_FAIL:".length());
                showLoginError(errMsg.isEmpty() ? "Email o password non corretti!" : errMsg);
            } else {
                showLoginError("Errore del server: risposta non riconosciuta.");
            }

        } catch (IOException e) {
            showLoginError("Errore di connessione al server.");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            showLoginError("Errore nel parsing della risposta del server.");
            e.printStackTrace();
        }
    }


    @FXML
/**
 * Method: handleRegister
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleRegister() {
        String nome = regNome.getText().trim();
        String cognome = regCognome.getText().trim();
        String email = regEmail.getText().trim();
        String password = regPassword.getText();
        String luogoDomicilio = regLuogoDomicilio.getText().trim();
        LocalDate localDate = regDataNascita.getValue();

        if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() || password.isEmpty() || luogoDomicilio.isEmpty() || localDate == null) {
            showRegError("Tutti i campi sono obbligatori!");
            return;
        }

        if (!isValidEmail(email)) {
            showRegError("Inserisci un'email valida (es: user@example.com)!");
            return;
        }

        if (!RequestValidator.isValidPassword(password)) {
            showRegError("La password deve contenere almeno 7 caratteri, una maiuscola, una minuscola e un numero.");
            return;
        }

        if (!localDate.isBefore(LocalDate.now())) {
            showRegError("La data di nascita deve essere precedente ad oggi.");
            return;
        }

        String ruolo = "cliente";
        if (tipoAccountGroup.getSelectedToggle() == btnRistoratore) {
            ruolo = "gestore";
        }

        Date dataNascita = Date.valueOf(localDate);

        double latDomicilio = 45.4642;
        double lonDomicilio = 9.1900;

            // Verifica connessione al server
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    showRegError("Impossibile connettersi al server. Riprova più tardi.");
                    return;
                }
            }

            // Invia richiesta di registrazione
            String regData = "REGISTER:" + nome + ":" + cognome + ":" + email + ":" + password + ":" +
                    dataNascita + ":" + luogoDomicilio + ":" + latDomicilio + ":" + lonDomicilio + ":" + ruolo;
            String response = null;
            try {
                response = apiClient.sendRequest(regData);
            } catch (IOException e) {
                showRegError("Errore di connessione al server.");
                e.printStackTrace();
                return;
            }

            if (response == null) {
                showRegError("Nessuna risposta dal server.");
                return;
            }

            if (response.startsWith("REGISTER_OK:")) {
                regErrorLabel.setStyle("-fx-text-fill: #2D6A4F;");
                showRegError("Registrazione completata! Accedi ora con le tue credenziali.");

                regNome.clear(); regCognome.clear(); regEmail.clear();
                regPassword.clear(); regLuogoDomicilio.clear(); regDataNascita.setValue(null);

                PauseTransition pause = new PauseTransition(javafx.util.Duration.seconds(1));
                pause.setOnFinished(e -> {
                    loginUsername.requestFocus();
                    loginUsername.setText(email);
                });
                pause.play();
            } else if (response.startsWith("REGISTER_FAIL:")) {
                String errMsg = response.substring("REGISTER_FAIL:".length());
                showRegError(errMsg.isEmpty() ? "Errore durante la registrazione." : errMsg);
            } else {
                showRegError("Errore del server: risposta non riconosciuta.");
            }
    }

    /**
     * Gestisce il pulsante della barra in alto per tornare indietro.
     */
    @FXML
/**
 * Method: goToHome
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void goToHome() {
        Navigator.getInstance().navigateTo("home-view.fxml", "Home Page");
    }

/**
 * Method: showLoginError
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void showLoginError(String msg) {
        loginErrorLabel.setText(msg);
        loginErrorLabel.setVisible(true);
        loginErrorLabel.setManaged(true);
    }

/**
 * Method: showRegError
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void showRegError(String msg) {
        regErrorLabel.setText(msg);
        regErrorLabel.setVisible(true);
        regErrorLabel.setManaged(true);
    }

    @FXML
/**
 * Method: handleGoToHome
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        project.controllers.Navigator.getInstance().navigateToHome();
    }

    /**
     * Valida il formato dell'email
     */
/**
 * Method: isValidEmail
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }
}

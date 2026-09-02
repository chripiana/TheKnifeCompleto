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

public class LoginController {

    // === API CLIENT ===
    private ServerApiClient apiClient;

    // === ELEMENTI GRAFICI LOGIN ===
    @FXML private TextField loginUsername; // Corrisponde all'email nel tuo DB
    @FXML private PasswordField loginPassword;
    @FXML private Label loginErrorLabel;

    // === ELEMENTI GRAFICI REGISTRAZIONE ===
    @FXML private TextField regNome;
    @FXML private TextField regCognome;
    @FXML private TextField regEmail;
    @FXML private PasswordField regPassword;
    @FXML private TextField regLuogoDomicilio;
    @FXML private DatePicker regDataNascita;
    @FXML private ToggleGroup tipoAccountGroup;
    @FXML private ToggleButton btnRistoratore;
    @FXML private Label regErrorLabel;

    @FXML
    public void initialize() {
        apiClient = new ServerApiClient();
    }

    @FXML
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
    private void goToHome() {
        Navigator.getInstance().navigateTo("home-view.fxml", "Home Page");
    }

    private void showLoginError(String msg) {
        loginErrorLabel.setText(msg);
        loginErrorLabel.setVisible(true);
        loginErrorLabel.setManaged(true);
    }

    private void showRegError(String msg) {
        regErrorLabel.setText(msg);
        regErrorLabel.setVisible(true);
        regErrorLabel.setManaged(true);
    }

    @FXML
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        project.controllers.Navigator.getInstance().navigateToHome();
    }

    /**
     * Valida il formato dell'email
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }
}

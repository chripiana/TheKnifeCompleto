package project.demo;

import db.TheKnifeDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class LoginController {

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

    /**
     * Gestisce l'autenticazione sulla colonna di sinistra.
     */
    @FXML
    private void handleLogin() {
        String email = loginUsername.getText().trim();
        String passwordInserita = loginPassword.getText();

        if (email.isEmpty() || passwordInserita.isEmpty()) {
            showLoginError("Inserisci email e password!");
            return;
        }

        TheKnifeDAO dao = Navigator.getInstance().getDao();

        try (ResultSet rs = dao.login(email)) {
            if (rs.next()) {
                String hashSalvato = rs.getString("password_hash");
                int idUtente = rs.getInt("id_utente");
                String ruolo = rs.getString("ruolo");

                // NOTA: Qui andrà integrato il controllo dell'hash (es. BCrypt).
                // Per ora assumiamo la password testuale valida.
                Navigator.getInstance().setIdUtenteLoggato(idUtente);
                Navigator.getInstance().setRuoloUtenteLoggato(ruolo);

                loginErrorLabel.setVisible(false);
                loginErrorLabel.setManaged(false);

                // Smistamento pagine in base al ruolo presente nel database
                if ("CLIENTE".equalsIgnoreCase(ruolo)) {
                    Navigator.getInstance().navigateTo("home-view-logged.fxml", "Home Cliente");
                } else {
                    Navigator.getInstance().navigateTo("owner-dashboard.fxml", "Dashboard Ristoratore");
                }
            } else {
                showLoginError("Email non trovata!");
            }
        } catch (SQLException e) {
            showLoginError("Errore di connessione al database.");
            e.printStackTrace();
        }
    }

    /**
     * Gestisce l'inserimento nel DB sulla colonna di destra.
     */
    @FXML
    private void handleRegister() {
        String nome = regNome.getText().trim();
        String cognome = regCognome.getText().trim();
        String email = regEmail.getText().trim();
        String password = regPassword.getText();
        String luogoDomicilio = regLuogoDomicilio.getText().trim();
        LocalDate localDate = regDataNascita.getValue();

        // 1. Verifica campi obbligatori
        if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() || password.isEmpty() || luogoDomicilio.isEmpty() || localDate == null) {
            showRegError("Tutti i campi sono obbligatori!");
            return;
        }

        // 2. Lettura del ruolo dai ToggleButton
        String ruolo = "cliente";
        if (tipoAccountGroup.getSelectedToggle() == btnRistoratore) {
            ruolo = "gestore";
        }

        // 3. Conversione data per PostgreSQL
        Date dataNascita = Date.valueOf(localDate);

        // 4. Coordinate di default (Mock temporaneo in attesa di Geocoding)
        double latDomicilio = 45.4642;
        double lonDomicilio = 9.1900;

        TheKnifeDAO dao = Navigator.getInstance().getDao();

        try {
            // Esecuzione della query sfruttando il metodo esatto del tuo DAO
            int righeInserite = dao.registrazione(
                    nome, cognome, email, password, dataNascita,
                    luogoDomicilio, latDomicilio, lonDomicilio, ruolo
            );

            if (righeInserite > 0) {
                regErrorLabel.setStyle("-fx-text-fill: #2D6A4F;"); // Cambia colore in verde per successo
                showRegError("Registrazione completata! Ora puoi effettuare il login.");

                // Pulisce i campi di registrazione
                regNome.clear(); regCognome.clear(); regEmail.clear();
                regPassword.clear(); regLuogoDomicilio.clear(); regDataNascita.setValue(null);
            } else {
                showRegError("Errore durante la registrazione.");
            }

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                showRegError("Questa email è già registrata!");
            } else {
                showRegError("Errore di salvataggio nel database.");
                e.printStackTrace();
            }
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
        project.demo.Navigator.getInstance().navigateToHome();
    }
}
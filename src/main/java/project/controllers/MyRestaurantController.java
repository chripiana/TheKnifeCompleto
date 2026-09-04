
package project.controllers;

import project.client.services.ServerApiClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MyRestaurantController
 *
 * Controller che gestisce la vista dei ristoranti posseduti dal gestore.
 * Fornisce funzionalità per:
 * - elencare i ristoranti dell'utente (GET_OWNER_RESTAURANT)
 * - aprire dialog di modifica per ogni ristorante
 * - eliminare ristoranti
 *
 * Scelte di design:
 * - UI generata dinamicamente per ciascuna card per mantenere il FXML
 *   generale semplice e riutilizzabile. Le chiamate di rete sono delegate
 *   a ServerApiClient.
 **/
/**
 * MyRestaurantController
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
public class MyRestaurantController {

    /** Contenitore delle card ristorante.*/
    @FXML
    private FlowPane containerRistoranti;
    /** Box mostrato quando non ci sono ristoranti.*/
    @FXML
    private VBox emptyRistorantiBox;
    /** Label che mostra il numero totale di ristoranti.*/
    @FXML
    private Label lblNumRistoranti;

    /** Navigator per la navigazione tra view.*/
    private Navigator navigator;
    /** Client di rete per comunicazione con il server.*/
    private ServerApiClient apiClient;
    /** Id del gestore loggato, usato per richieste a server.*/
    private int idGestoreLoggato;

    /**
     * Inizializzazione del controller — recupera l'id gestore e carica i dati.*/
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
        this.idGestoreLoggato = navigator.getIdUtenteLoggato();

        if (idGestoreLoggato != -1) {
            caricaRistoranti();
        } else {
            navigator.navigateTo("login-view.fxml", "Accedi");
        }
    }

/**
 * Method: caricaRistoranti
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void caricaRistoranti() {
        containerRistoranti.getChildren().clear();
        int totale = 0;

        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    mostraErrore("Errore di connessione", "Impossibile connettersi al server");
                    return;
                }
            }

            String response = apiClient.sendRequest("GET_OWNER_RESTAURANT:" + idGestoreLoggato);

            if (response != null && response.startsWith("GET_OWNER_RESTAURANT_OK:")) {
                String data = response.substring("GET_OWNER_RESTAURANT_OK:".length());
                if (!data.isEmpty()) {
                    String[] rows = data.split(";");
                    for (String row : rows) {
                        totale++;
                        Map<String, String> rowData = parseRowData(row);

                        String idRistorante = rowData.getOrDefault("id_ristorante", "");
                        String nome = rowData.getOrDefault("nome", "");
                        String citta = rowData.getOrDefault("citta", "");
                        double mediaStelle = 0;
                        int numRecensioni = 0;
                        
                        try {
                            mediaStelle = Double.parseDouble(rowData.getOrDefault("media_stelle", "0"));
                        } catch (NumberFormatException e) {
                        }
                        try {
                            numRecensioni = Integer.parseInt(rowData.getOrDefault("num_recensioni", "0"));
                        } catch (NumberFormatException e) {
                        }

                        containerRistoranti.getChildren()
                                .add(creaCardRistorante(idRistorante, nome, citta, mediaStelle, numRecensioni));
                    }
                }
            } else if (response != null && response.startsWith("GET_OWNER_RESTAURANT_FAIL:")) {
                mostraErrore("Errore", response.substring("GET_OWNER_RESTAURANT_FAIL:".length()));
            }
        } catch (IOException e) {
            mostraErrore("Errore di connessione", e.getMessage());
        }

        lblNumRistoranti.setText(String.valueOf(totale));

        boolean haRistoranti = totale > 0;
        containerRistoranti.setVisible(haRistoranti);
        containerRistoranti.setManaged(haRistoranti);
        emptyRistorantiBox.setVisible(!haRistoranti);
        emptyRistorantiBox.setManaged(!haRistoranti);
    }

    private VBox creaCardRistorante(String idRistorante, String nome, String citta, double mediaStelle,
            int numRecensioni) {
        VBox card = new VBox(10);
        card.getStyleClass().add("owner-restaurant-card");
        card.setPrefWidth(300);
        card.setMinWidth(280);
        card.setMaxWidth(300);

        Label lblNome = new Label(nome);
        lblNome.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1B4332;");

        Label lblCitta = new Label("📍 " + citta);
        lblCitta.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label lblRecensioni;
        if (numRecensioni == 0) {
            lblRecensioni = new Label("Nessuna recensione");
            lblRecensioni.setStyle("-fx-font-size: 13px; -fx-font-style: italic; -fx-text-fill: #94A3B8;");
        } else {
            String stelle = String.format("★ %.1f  •  %d recension%s", mediaStelle, numRecensioni,
                    numRecensioni == 1 ? "e" : "i");
            lblRecensioni = new Label(stelle);
            lblRecensioni.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");
        }

        HBox hboxAzioni = new HBox(10);
        Button btnModifica = new Button("✏️ Modifica");
        btnModifica.getStyleClass().add("btn-outline");
        btnModifica.setOnAction(e -> apriDialogModifica(idRistorante));

        Button btnElimina = new Button("🗑️ Elimina");
        btnElimina.getStyleClass().add("btn-danger");
        btnElimina.setOnAction(e -> confermaEliminazione(idRistorante, nome));

        hboxAzioni.getChildren().addAll(btnModifica, btnElimina);

        card.getChildren().addAll(lblNome, lblCitta, lblRecensioni, hboxAzioni);
        return card;
    }

/**
 * Method: apriDialogModifica
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void apriDialogModifica(String idRistorante) {
        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    mostraErrore("Errore di connessione", "Impossibile connettersi al server");
                    return;
                }
            }

            String response = apiClient.sendRequest("GET_RISTORANTE_DETAILS:" + idRistorante);

            if (response == null || !response.startsWith("GET_RISTORANTE_DETAILS_OK:")) {
                mostraErrore("Impossibile trovare i dati del ristorante.");
                return;
            }

            String data = response.substring("GET_RISTORANTE_DETAILS_OK:".length());
            Map<String, String> rowData = parseRowData(data);

            String nomeAttuale = rowData.getOrDefault("nome", "");
            String cittaAttuale = rowData.getOrDefault("citta", "");
            String indirizzoAttuale = rowData.getOrDefault("indirizzo", "");
            String cucinaAttuale = rowData.getOrDefault("tipologia_cucina", "");
            int prezzoAttuale = 0;
            try {
                prezzoAttuale = Integer.parseInt(rowData.getOrDefault("prezzo_medio", "0"));
            } catch (NumberFormatException e) {
            }
            boolean deliveryAttuale = "1".equals(rowData.getOrDefault("delivery", "0")) || "true".equalsIgnoreCase(rowData.getOrDefault("delivery", "false"));
            boolean prenotazioneAttuale = "1".equals(rowData.getOrDefault("prenotazione_online", "0")) || "true".equalsIgnoreCase(rowData.getOrDefault("prenotazione_online", "false"));

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifica Ristorante");
            dialog.setHeaderText("Aggiorna le specifiche di \"" + nomeAttuale + "\"");

            ButtonType btnSalvaType = new ButtonType("Salva", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnSalvaType, ButtonType.CANCEL);
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(12);
            grid.setPadding(new Insets(20, 20, 10, 20));
            ColumnConstraints c1 = new ColumnConstraints();
            c1.setPrefWidth(130);
            grid.getColumnConstraints().add(c1);

            TextField txtNome = new TextField(nomeAttuale);
            TextField txtCitta = new TextField(cittaAttuale);
            TextField txtIndirizzo = new TextField(indirizzoAttuale);
            TextField txtCucina = new TextField(cucinaAttuale);
            TextField txtPrezzo = new TextField(String.valueOf(prezzoAttuale));
            CheckBox chkDelivery = new CheckBox("Delivery disponibile");
            chkDelivery.setSelected(deliveryAttuale);
            CheckBox chkPrenotazione = new CheckBox("Prenotazione online");
            chkPrenotazione.setSelected(prenotazioneAttuale);

            grid.addRow(0, new Label("Nome"), txtNome);
            grid.addRow(1, new Label("Città"), txtCitta);
            grid.addRow(2, new Label("Indirizzo"), txtIndirizzo);
            grid.addRow(3, new Label("Tipo cucina"), txtCucina);
            grid.addRow(4, new Label("Prezzo medio (€)"), txtPrezzo);
            grid.add(chkDelivery, 1, 5);
            grid.add(chkPrenotazione, 1, 6);

            dialog.getDialogPane().setContent(grid);

            Optional<ButtonType> risultato = dialog.showAndWait();
            if (risultato.isPresent() && risultato.get() == btnSalvaType) {
                int nuovoPrezzo;
                try {
                    nuovoPrezzo = Integer.parseInt(txtPrezzo.getText().trim());
                } catch (NumberFormatException ex) {
                    mostraErrore("Il prezzo medio deve essere un numero valido.");
                    return;
                }

                if (txtNome.getText().isBlank() || txtCitta.getText().isBlank()) {
                    mostraErrore("Nome e città non possono essere vuoti.");
                    return;
                }

                modificaRistorante(idRistorante, txtNome.getText().trim(), txtCitta.getText().trim(),
                        txtIndirizzo.getText().trim(), txtCucina.getText().trim(), nuovoPrezzo,
                        chkDelivery.isSelected(), chkPrenotazione.isSelected());
            }
        } catch (IOException e) {
            mostraErrore("Errore durante il caricamento dei dati del ristorante.");
        }
    }

    private void modificaRistorante(String idRistorante, String nome, String citta, String indirizzo,
            String cucina, int prezzo, boolean delivery, boolean prenotazione) {
        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    mostraErrore("Errore di connessione", "Impossibile connettersi al server");
                    return;
                }
            }

            String response = apiClient.sendRequest("UPDATE_RESTAURANT:" + idRistorante + ":" + idGestoreLoggato + ":" +
                    nome + ":" + citta + ":" + indirizzo + ":" + cucina + ":" + prezzo + ":" +
                    (delivery ? "1" : "0") + ":" + (prenotazione ? "1" : "0"));

            if (response != null && response.startsWith("UPDATE_RESTAURANT_OK:")) {
                caricaRistoranti();
            } else if (response != null && response.startsWith("UPDATE_RESTAURANT_FAIL:")) {
                mostraErrore("Nessuna modifica salvata. Riprova.");
            }
        } catch (IOException e) {
            mostraErrore("Errore durante l'aggiornamento del ristorante.");
        }
    }

/**
 * Method: confermaEliminazione
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void confermaEliminazione(String idRistorante, String nome) {
        Alert conferma = new Alert(AlertType.CONFIRMATION);
        conferma.setTitle("Elimina Ristorante");
        conferma.setHeaderText("Vuoi eliminare \"" + nome + "\"?");
        conferma.setContentText("Questa azione è irreversibile e rimuoverà anche le recensioni associate.");

        Optional<ButtonType> risultato = conferma.showAndWait();
        if (risultato.isPresent() && risultato.get() == ButtonType.OK) {
            try {
                if (!apiClient.isConnected()) {
                    if (!apiClient.ensureConnected()) {
                        mostraErrore("Errore di connessione", "Impossibile connettersi al server");
                        return;
                    }
                }

                String response = apiClient.sendRequest("DELETE_RESTAURANT:" + idRistorante + ":" + idGestoreLoggato);

                if (response != null && response.startsWith("DELETE_RESTAURANT_OK:")) {
                    caricaRistoranti();
                } else if (response != null && response.startsWith("DELETE_RESTAURANT_FAIL:")) {
                    mostraErrore("Impossibile eliminare il ristorante.");
                }
            } catch (IOException e) {
                mostraErrore("Errore durante l'eliminazione del ristorante.");
            }
        }
    }

/**
 * Method: mostraErrore
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void mostraErrore(String title, String messaggio) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

/**
 * Method: mostraErrore
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void mostraErrore(String messaggio) {
        mostraErrore("Errore", messaggio);
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

    @FXML
    void handleNuovoRistorante(ActionEvent event) {
        navigator.navigateTo("new-restaurant-view.fxml", "Nuovo Ristorante");
    }

    @FXML
    void handleRecensioni(ActionEvent event) {
        navigator.navigateTo("owner-dashboard-view.fxml", "Gestisci Ristoranti");
    }

    @FXML
    void handleProfilo(ActionEvent event) {
        navigator.navigateTo("owner-dashboard-view.fxml", "Dashboard Proprietario");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        navigator.logout();
        navigator.navigateTo("login-view.fxml", "Accedi");
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
}
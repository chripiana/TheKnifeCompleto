package project.controllers;

import project.client.services.ServerApiClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
/**
 * RestaurantDetailsController
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

public class RestaurantDetailsController {

    // Elementi dell'HBox Hero e informazioni principali
    @FXML
/**
 * Field: lblBreadcrumb
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblBreadcrumb;
    @FXML
/**
 * Field: lblNomeRistorante
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblNomeRistorante;
    @FXML
/**
 * Field: lblCitta
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblCitta;
    @FXML
/**
 * Field: lblCucina
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblCucina;
    @FXML
/**
 * Field: lblPrezzo
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblPrezzo;
    @FXML
/**
 * Field: hboxHero
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private HBox hboxHero;
    @FXML
/**
 * Field: lblAvatarRistorante
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblAvatarRistorante;

    // Riferimenti alla navbar (resta sempre verde, come tutte le altre pagine:
    // nessuna colorazione dinamica)
    @FXML
/**
 * Field: hboxNavbar
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private HBox hboxNavbar;
    @FXML
/**
 * Field: lblLogoThe
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblLogoThe;
    @FXML
/**
 * Field: lblLogoKnife
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblLogoKnife;
    @FXML
/**
 * Field: btnTornaRisultati
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Button btnTornaRisultati;

    // Allineato all'fx:id dell'FXML per evitare disallineamenti di mancato
    // aggiornamento delle recensioni
    @FXML
/**
 * Field: lblStelleMedia
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblStelleMedia;
    @FXML
/**
 * Field: lblNumRecensioni
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblNumRecensioni;

    // Card "VALUTAZIONI": numero grande, stelle e distribuzione 1★-5★ (presenti in
    // entrambe le viste,
    // nella vista loggata questi stessi fx:id sostituiscono anche
    // lblStelleMedia/lblNumRecensioni nell'hero)
    @FXML
/**
 * Field: lblRatingBigNumber
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblRatingBigNumber;
    @FXML
/**
 * Field: lblStarsDisplayBig
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblStarsDisplayBig;
    @FXML
/**
 * Field: lblNumRecensioniBox
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblNumRecensioniBox;
    @FXML
/**
 * Field: barStelle1
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private ProgressBar barStelle1;
    @FXML
/**
 * Field: barStelle2
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private ProgressBar barStelle2;
    @FXML
/**
 * Field: barStelle3
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private ProgressBar barStelle3;
    @FXML
/**
 * Field: barStelle4
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private ProgressBar barStelle4;
    @FXML
/**
 * Field: barStelle5
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private ProgressBar barStelle5;
    @FXML
/**
 * Field: lblPctStelle1
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblPctStelle1;
    @FXML
/**
 * Field: lblPctStelle2
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblPctStelle2;
    @FXML
/**
 * Field: lblPctStelle3
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblPctStelle3;
    @FXML
/**
 * Field: lblPctStelle4
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblPctStelle4;
    @FXML
/**
 * Field: lblPctStelle5
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblPctStelle5;

    // Scheda Informazioni Generali
    @FXML
/**
 * Field: lblNazione
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblNazione;
    @FXML
/**
 * Field: lblCittaVal
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblCittaVal;
    @FXML
/**
 * Field: lblIndirizzo
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblIndirizzo;
    @FXML
/**
 * Field: lblTipologiaCucinaBox
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblTipologiaCucinaBox;
    @FXML
/**
 * Field: lblPrezzoMedioBox
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblPrezzoMedioBox;

    // Mappa e Posizione
    @FXML
/**
 * Field: lblMappaTestoIndirizzo
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblMappaTestoIndirizzo;
    @FXML
/**
 * Field: lblLatitudine
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblLatitudine;
    @FXML
/**
 * Field: lblLongitudine
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblLongitudine;
    @FXML
/**
 * Field: btnPrenotaOra
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Button btnPrenotaOra;
    @FXML
/**
 * Field: btnAggiungiPreferiti
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Button btnAggiungiPreferiti;

    // Componenti interattivi per lasciare una recensione
    @FXML
/**
 * Field: starRec1
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label starRec1;
    @FXML
/**
 * Field: starRec2
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label starRec2;
    @FXML
/**
 * Field: starRec3
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label starRec3;
    @FXML
/**
 * Field: starRec4
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label starRec4;
    @FXML
/**
 * Field: starRec5
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label starRec5;
    @FXML
/**
 * Field: recensioneTextArea
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private TextArea recensioneTextArea;
    @FXML
/**
 * Field: btnPubblicaRecensione
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Button btnPubblicaRecensione;

    // Riepilogo Laterale Destro
    @FXML
/**
 * Field: lblRiepilogoStelle
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblRiepilogoStelle;
    @FXML
/**
 * Field: lblRiepilogoPrezzo
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblRiepilogoPrezzo;
    @FXML
/**
 * Field: lblRiepilogoCucina
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private Label lblRiepilogoCucina;

    private SearchController.RistoranteOggetto ristoranteCorrente;
/**
 * Field: apiClient
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private ServerApiClient apiClient;
/**
 * Field: votoSelezionato
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private int votoSelezionato = 0;

    public RestaurantDetailsController() {
        this.apiClient = new ServerApiClient();
    }

    /**
     * Cambia visivamente il colore delle stelle da grigio a oro quando l'utente
     * clicca.
     */
/**
 * Method: aggiornaVisualizzazioneStelleInput
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void aggiornaVisualizzazioneStelleInput(int voto) {
        this.votoSelezionato = voto;
        Label[] stelle = { starRec1, starRec2, starRec3, starRec4, starRec5 };

        for (int i = 0; i < stelle.length; i++) {
            if (stelle[i] != null) {
                if (i < voto) {
                    // Imposta le stelle selezionate color Oro lucido
                    stelle[i].setStyle("-fx-cursor: hand; -fx-font-size: 22px; -fx-text-fill: #FFD700;");
                } else {
                    // Mantiene o ripristina le restanti stelle in Grigio disattivato
                    stelle[i].setStyle("-fx-cursor: hand; -fx-font-size: 22px; -fx-text-fill: #CBD5E1;");
                }
            }
        }
        System.out.println("[INTERAZIONE] Valutazione impostata a: " + voto + " stelle.");
    }

    @FXML
/**
 * Method: handleStarRec1
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleStarRec1(MouseEvent event) {
        aggiornaVisualizzazioneStelleInput(1);
    }

    @FXML
/**
 * Method: handleStarRec2
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleStarRec2(MouseEvent event) {
        aggiornaVisualizzazioneStelleInput(2);
    }

    @FXML
/**
 * Method: handleStarRec3
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleStarRec3(MouseEvent event) {
        aggiornaVisualizzazioneStelleInput(3);
    }

    @FXML
/**
 * Method: handleStarRec4
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleStarRec4(MouseEvent event) {
        aggiornaVisualizzazioneStelleInput(4);
    }

    @FXML
/**
 * Method: handleStarRec5
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleStarRec5(MouseEvent event) {
        aggiornaVisualizzazioneStelleInput(5);
    }

    @FXML
    private void handlePubblicaRecensione(ActionEvent event) throws IOException {
        if (ristoranteCorrente == null) {
            System.err.println("[ERRORE] Nessun ristorante selezionato corrente.");
            return;
        }

        int idUtenteLoggato = Navigator.getInstance().getIdUtenteLoggato();

        if (idUtenteLoggato == -1) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Accesso Richiesto");
            alert.setHeaderText("Impossibile pubblicare la recensione");
            alert.setContentText("Devi effettuare il login con il tuo account prima di poter recensire un ristorante.");
            alert.showAndWait();
            return;
        }

        if (votoSelezionato == 0) {
            System.out.println("[ATTENZIONE] Seleziona almeno una stella prima di pubblicare.");
            return;
        }

        String testo = recensioneTextArea != null ? recensioneTextArea.getText() : "";
        System.out.println("[RECENSIONE] Inserimento per il ristorante ID: " + ristoranteCorrente.id);
        System.out.println("[RECENSIONE] " + votoSelezionato + " stelle. Utente ID: " + idUtenteLoggato);

        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore di connessione");
                    alert.setContentText("Impossibile connettersi al server");
                    alert.showAndWait();
                    return;
                }
            }

            String response = apiClient.sendRequest("ADD_REVIEW:" + ristoranteCorrente.id + ":" + idUtenteLoggato + ":" +
                    votoSelezionato + ":" + testo);

            if (response != null && response.startsWith("ADD_REVIEW_OK:")) {
                if (recensioneTextArea != null)
                    recensioneTextArea.clear();
                votoSelezionato = 0;
                aggiornaVisualizzazioneStelleInput(0);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Successo");
                alert.setContentText("Recensione pubblicata con successo!");
                alert.showAndWait();
                aggiornaValutazioniDaDB();
            } else if (response != null && response.startsWith("ADD_REVIEW_FAIL:")) {
                String errorMsg = response.substring("ADD_REVIEW_FAIL:".length());
                if (errorMsg.contains("UNIQUE")) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Errore di inserimento");
                    alert.setHeaderText("Hai già recensito questo ristorante");
                    alert.setContentText("Non è possibile lasciare più di una recensione per lo stesso locale.");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setContentText(errorMsg);
                    alert.showAndWait();
                }
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore di connessione");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
/**
 * Method: handlePrenotaOra
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handlePrenotaOra(ActionEvent event) {
        if (ristoranteCorrente == null) {
            return;
        }

        int idUtente = Navigator.getInstance().getIdUtenteLoggato();
        if (idUtente == -1) {
            showStyledReservationStage("Accesso richiesto",
                    "Per prenotare un tavolo devi essere registrato e autenticato.",
                    "Effettua il login",
                    "login-view.fxml");
            Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
            return;
        }

        showReservationFormStage(idUtente);
    }

/**
 * Method: showReservationFormStage
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void showReservationFormStage(int idUtente) {
        Stage ownerStage = btnPrenotaOra != null && btnPrenotaOra.getScene() != null ?
                (Stage) btnPrenotaOra.getScene().getWindow() : null;

        Stage stage = new Stage();
        if (ownerStage != null) {
            stage.initOwner(ownerStage);
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        stage.setTitle("Prenota un tavolo");

        VBox root = new VBox(18);
        root.getStyleClass().add("reservation-stage-shell");

        VBox card = new VBox(18);
        card.getStyleClass().add("reservation-stage-card");

        Label title = new Label("Prenota un tavolo");
        title.getStyleClass().add("reservation-stage-title");

        Label subtitle = new Label("Prenotazione per: " + ristoranteCorrente.nome);
        subtitle.getStyleClass().add("reservation-stage-subtitle");

        DatePicker datePicker = new DatePicker(java.time.LocalDate.now().plusDays(1));
        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll("12:00", "12:30", "13:00", "13:30", "19:00", "19:30", "20:00", "20:30", "21:00", "21:30");
        timeCombo.setValue("20:00");

        Spinner<Integer> guestsSpinner = new Spinner<>(1, 20, 2);
        TextArea noteArea = new TextArea();
        noteArea.setPromptText("Eventuali note (es. allergeni, accessibilità, ecc.)");
        noteArea.setPrefRowCount(5);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("reservation-form-grid");
        grid.setPadding(new javafx.geometry.Insets(8, 0, 0, 0));
        grid.add(new Label("Data:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Ora:"), 0, 1);
        grid.add(timeCombo, 1, 1);
        grid.add(new Label("Persone:"), 0, 2);
        grid.add(guestsSpinner, 1, 2);
        grid.add(new Label("Note:"), 0, 3);
        grid.add(noteArea, 1, 3);

        HBox actions = new HBox(12);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Annulla");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #D7E5DD; -fx-border-radius: 10px; -fx-padding: 10 16; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> stage.close());

        Button confirmBtn = new Button("Conferma prenotazione");
        confirmBtn.getStyleClass().add("btn-primary");
        confirmBtn.setOnAction(e -> {
            try {
                if (datePicker.getValue() == null) {
                    showStyledReservationStage("Data mancante", "Seleziona una data valida per la prenotazione.", "Riprova", null);
                    return;
                }

                if (!apiClient.isConnected()) {
                    if (!apiClient.ensureConnected()) {
                        showStyledReservationStage("Connessione assente", "Impossibile contattare il server.", "Riprova più tardi", null);
                        return;
                    }
                }

                String selectedDate = datePicker.getValue().toString();
                String selectedTime = timeCombo.getValue() == null ? "20:00" : timeCombo.getValue().replace(":", "");
                String note = noteArea.getText() == null ? "" : noteArea.getText().replace(";", ",");
                String response = apiClient.sendRequest(
                        "CREATE_RESERVATION:" + idUtente + ":" + ristoranteCorrente.id + ":" + selectedDate + ":"
                                + selectedTime + ":" + guestsSpinner.getValue() + ":" + note
                );

                if (response != null && response.startsWith("CREATE_RESERVATION_OK:")) {
                    String codice = response.substring("CREATE_RESERVATION_OK:".length());
                    stage.close();
                    showSuccessReservationStage(codice);
                } else {
                    showStyledReservationStage("Prenotazione non completata",
                            response != null ? response.replace("CREATE_RESERVATION_FAIL:", "") : "Contatta il supporto.",
                            "Riprova", null);
                }
            } catch (Exception ex) {
                showStyledReservationStage("Errore durante la prenotazione", ex.getMessage(), "Riprova", null);
            }
        });

        actions.getChildren().addAll(cancelBtn, confirmBtn);
        card.getChildren().addAll(title, subtitle, grid, actions);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 760, 600);
        URL css = getClass().getResource("/project/controllers/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

/**
 * Method: showSuccessReservationStage
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void showSuccessReservationStage(String codice) {
        Stage stage = new Stage();
        stage.setTitle("Prenotazione inviata");

        VBox root = new VBox(18);
        root.getStyleClass().add("reservation-stage-shell");

        VBox card = new VBox(20);
        card.getStyleClass().add("reservation-stage-card");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label badge = new Label("In attesa");
        badge.setStyle("-fx-background-color: #FFF3CD; -fx-text-fill: #8A6D00; -fx-padding: 8 14 8 14; -fx-background-radius: 999px; -fx-font-weight: bold;");

        Label title = new Label("Prenotazione ricevuta");
        title.getStyleClass().add("reservation-stage-title");

        Label subtitle = new Label("La tua richiesta è stata inviata al ristorante. Potrai seguire lo stato nelle tue prenotazioni.");
        subtitle.getStyleClass().add("reservation-stage-subtitle");
        subtitle.setWrapText(true);

        Label code = new Label("Codice prenotazione: " + codice);
        code.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #163d2f;");

        HBox actions = new HBox(12);
        actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button viewBtn = new Button("Vedi prenotazioni");
        viewBtn.getStyleClass().add("btn-primary");
        viewBtn.setOnAction(e -> {
            stage.close();
            Navigator.getInstance().navigateToReservations();
        });

        Button closeBtn = new Button("Chiudi");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #D7E5DD; -fx-border-radius: 10px; -fx-padding: 10 16; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());

        actions.getChildren().addAll(viewBtn, closeBtn);
        card.getChildren().addAll(badge, title, subtitle, code, actions);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 640, 480);
        URL css = getClass().getResource("/project/controllers/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

/**
 * Method: showStyledReservationStage
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void showStyledReservationStage(String titleText, String message, String actionText, String redirectPage) {
        Stage stage = new Stage();
        stage.setTitle(titleText);

        VBox root = new VBox(18);
        root.getStyleClass().add("reservation-stage-shell");

        VBox card = new VBox(18);
        card.getStyleClass().add("reservation-stage-card");

        Label title = new Label(titleText);
        title.getStyleClass().add("reservation-stage-title");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4A6B57;");

        Button action = new Button(actionText);
        action.getStyleClass().add("btn-primary");
        action.setOnAction(e -> {
            stage.close();
            if (redirectPage != null && !redirectPage.isBlank()) {
                Navigator.getInstance().navigateTo(redirectPage, "Accedi");
            }
        });

        card.getChildren().addAll(title, messageLabel, action);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 560, 300);
        URL css = getClass().getResource("/project/controllers/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @FXML
/**
 * Method: handleCerca
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleCerca(ActionEvent event) {
        Navigator.getInstance().navigateTo("search-view-logged.fxml", "Cerca Ristoranti");
    }

    @FXML
/**
 * Method: handlePreferiti
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handlePreferiti(ActionEvent event) {
        Navigator.getInstance().navigateTo("favorites-view.fxml", "I Miei Preferiti");
    }

    @FXML
/**
 * Method: handleTogglePreferiti
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleTogglePreferiti(ActionEvent event) {
        togglePreferito();
    }

    @FXML
/**
 * Method: handleTogglePreferito
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleTogglePreferito(ActionEvent event) {
        togglePreferito();
    }

    @FXML
/**
 * Method: handleRecensioni
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleRecensioni(ActionEvent event) {
        Navigator.getInstance().navigateTo("reviews-view.fxml", "Le Mie Recensioni");
    }

/**
 * Method: togglePreferito
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void togglePreferito() {
        if (ristoranteCorrente == null) {
            return;
        }

        int idUtenteLoggato = Navigator.getInstance().getIdUtenteLoggato();
        if (idUtenteLoggato == -1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Accesso richiesto");
            alert.setHeaderText("Devi effettuare il login");
            alert.setContentText("Per aggiungere un ristorante ai preferiti devi essere autenticato.");
            alert.showAndWait();
            Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
            return;
        }

        try {
            if (!apiClient.isConnected() && !apiClient.ensureConnected()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore di connessione");
                alert.setHeaderText("Impossibile connettersi al server");
                alert.setContentText("Controlla la connessione e riprova.");
                alert.showAndWait();
                return;
            }

            String checkResponse = apiClient.sendRequest("IS_PREFERITO:" + idUtenteLoggato + ":" + ristoranteCorrente.id);
            if (checkResponse == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setContentText("Nessuna risposta dal server.");
                alert.showAndWait();
                return;
            }

            boolean isPreferito = checkResponse.startsWith("IS_PREFERITO_OK:") && checkResponse.contains("true");
            String response;
            if (isPreferito) {
                response = apiClient.sendRequest("REMOVE_PREFERITO:" + idUtenteLoggato + ":" + ristoranteCorrente.id);
                if (response != null && response.startsWith("REMOVE_PREFERITO_OK:")) {
                    if (btnAggiungiPreferiti != null) {
                        btnAggiungiPreferiti.setText("♡  Aggiungi ai Preferiti");
                    }
                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setTitle("Preferiti aggiornati");
                    ok.setHeaderText("Ristorante rimosso dai preferiti");
                    ok.setContentText(ristoranteCorrente.nome + " non è più tra i tuoi preferiti.");
                    ok.showAndWait();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Errore");
                    err.setHeaderText("Impossibile rimuovere il preferito");
                    err.setContentText(response != null ? response.replace("REMOVE_PREFERITO_FAIL:", "") : "Contatta il supporto.");
                    err.showAndWait();
                }
            } else {
                response = apiClient.sendRequest("ADD_PREFERITO:" + idUtenteLoggato + ":" + ristoranteCorrente.id);
                if (response != null && response.startsWith("ADD_PREFERITO_OK:")) {
                    if (btnAggiungiPreferiti != null) {
                        btnAggiungiPreferiti.setText("♥  Nei tuoi preferiti");
                    }
                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setTitle("Preferiti aggiornati");
                    ok.setHeaderText("Ristorante aggiunto ai preferiti");
                    ok.setContentText(ristoranteCorrente.nome + " è stato salvato tra i tuoi preferiti.");
                    ok.showAndWait();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Errore");
                    err.setHeaderText("Impossibile aggiungere il preferito");
                    err.setContentText(response != null ? response.replace("ADD_PREFERITO_FAIL:", "") : "Contatta il supporto.");
                    err.showAndWait();
                }
            }
        } catch (IOException e) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Errore di connessione");
            err.setHeaderText("Problema durante il salvataggio preferiti");
            err.setContentText(e.getMessage());
            err.showAndWait();
        }
    }

    @FXML
/**
 * Method: handleProfilo
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleProfilo(ActionEvent event) {
        Navigator.getInstance().navigateToProfile();
    }

    @FXML
/**
 * Method: handleLogout
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleLogout(ActionEvent event) {
        Navigator.getInstance().logout();
        Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
    }

    @FXML
/**
 * Method: handleTornaRisultati
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleTornaRisultati(ActionEvent event) {
        System.out.println("[NAVIGAZIONE] Ritorno ai risultati di ricerca.");
        Navigator.getInstance().backToSearchResults();
    }

    @FXML
/**
 * Method: handleGoToHome
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleGoToHome(MouseEvent event) {
        System.out.println("[NAVIGAZIONE] Ritorno alla Home principale.");
        Navigator.getInstance().navigateToHomeIntelligent();
    }

/**
 * Method: caricaDatiRistorante
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void caricaDatiRistorante(SearchController.RistoranteOggetto r) {
        if (r == null)
            return;
        this.ristoranteCorrente = r;

        if (lblBreadcrumb != null)
            lblBreadcrumb.setText("Home  ›  " + r.citta + "  ›  " + r.nome);
        if (lblNomeRistorante != null)
            lblNomeRistorante.setText(r.nome);
        if (lblCitta != null)
            lblCitta.setText("📍 " + r.indirizzo + ", " + r.citta + ", " + r.nazione);
        if (lblCucina != null)
            lblCucina.setText(getEmojiCucina(r.cucina) + " Cucina " + r.cucina);
        if (lblPrezzo != null)
            lblPrezzo.setText("€".repeat(Math.max(1, Math.min(3, r.prezzo / 25))) + "  ·  " + r.prezzo + "€ p.p.");

        // Configura gli sfondi grafici corretti
        impostaStileCucinaDinamico(r.cucina);

        // Compilazione della scheda dettagliata delle informazioni
        if (lblNazione != null)
            lblNazione.setText(r.nazione);
        if (lblCittaVal != null)
            lblCittaVal.setText(r.citta);
        if (lblIndirizzo != null)
            lblIndirizzo.setText(r.indirizzo);
        if (lblTipologiaCucinaBox != null)
            lblTipologiaCucinaBox.setText(r.cucina);
        if (lblPrezzoMedioBox != null)
            lblPrezzoMedioBox.setText(r.prezzo + " € a persona");

        // Compilazione del box riepilogo laterale destro
        if (lblRiepilogoPrezzo != null)
            lblRiepilogoPrezzo.setText(r.prezzo + " €");
        if (lblRiepilogoCucina != null)
            lblRiepilogoCucina.setText(r.cucina);

        if (lblMappaTestoIndirizzo != null)
            lblMappaTestoIndirizzo.setText(r.indirizzo + " — " + r.citta + ", " + r.nazione);
        if (lblLatitudine != null)
            lblLatitudine.setText("Lat: " + (r.lat != 0.0 ? r.lat + "°" : "45.4642°"));
        if (lblLongitudine != null)
            lblLongitudine.setText("Lng: " + (r.lon != 0.0 ? r.lon + "°" : "9.1900°"));

        aggiornaValutazioniDaDB();

        if (btnPrenotaOra != null) {
            if (r.prenotazioneOnline) {
                btnPrenotaOra.setDisable(false);
                btnPrenotaOra.setText("Prenota Ora");
            } else {
                btnPrenotaOra.setDisable(true);
                btnPrenotaOra.setText("Prenotazione non disponibile");
            }
        }

        aggiornaStatoPreferiti();
    }

/**
 * Method: aggiornaStatoPreferiti
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void aggiornaStatoPreferiti() {
        if (btnAggiungiPreferiti == null || ristoranteCorrente == null) {
            return;
        }

        int userId = Navigator.getInstance().getIdUtenteLoggato();
        if (userId == -1) {
            btnAggiungiPreferiti.setText("♡  Aggiungi ai Preferiti");
            return;
        }

        try {
            if (!apiClient.isConnected() && !apiClient.ensureConnected()) {
                btnAggiungiPreferiti.setText("♡  Aggiungi ai Preferiti");
                return;
            }

            String response = apiClient.sendRequest("IS_PREFERITO:" + userId + ":" + ristoranteCorrente.id);
            if (response != null && response.startsWith("IS_PREFERITO_OK:")) {
                boolean isPreferito = response.contains("true");
                btnAggiungiPreferiti.setText(isPreferito ? "♥  Nei tuoi preferiti" : "♡  Aggiungi ai Preferiti");
            }
        } catch (IOException e) {
            btnAggiungiPreferiti.setText("♡  Aggiungi ai Preferiti");
        }
    }

/**
 * Method: impostaStileCucinaDinamico
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void impostaStileCucinaDinamico(String cucina) {
        if (cucina == null)
            cucina = "";
        String lower = cucina.toLowerCase().trim();

        // Determina l'emoji corretta tramite la lista completa delle categorie
        String emoji = getEmojiCucina(lower);
        if (lblAvatarRistorante != null) {
            lblAvatarRistorante.setText(emoji);
        }

        // Colore di sfondo associato alla cucina, usato solo per l'hero (la navbar
        // resta verde fissa)
        String coloreSfondo = getCucinaColor(lower);

        if (hboxHero != null) {
            // Rimuove i vecchi stili associati
            hboxHero.getStyleClass().removeAll(
                    "hero-italiana", "hero-giapponese", "hero-vegetariana", "hero-mediterranea",
                    "hero-carne", "hero-messicana", "hero-pesce", "hero-poke", "hero-indiana", "hero-altro");

            // Inietta lo stile inline preservando la struttura morbida
            String stileBase = "-fx-background-radius: 16px; -fx-padding: 32px;";
            hboxHero.setStyle(stileBase + " -fx-background-color: " + coloreSfondo + ";");
        }

        // La navbar della pagina dettagli resta verde come in tutte le altre pagine
        // dell'app:
        // reset esplicito di qualsiasi stile inline residuo, così dipende solo dalla
        // classe CSS "navbar".
        if (hboxNavbar != null) {
            hboxNavbar.setStyle("");
        }
    }

    /**
     * Determina il colore di sfondo associato alla tipologia di cucina (Palette Pastello Accesa).
     * Tutti i colori mantengono una luminosità media per garantire ottima leggibilità
     * sia con testo BIANCO che con testo NERO.
     */
/**
 * Method: getCucinaColor
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private String getCucinaColor(String lower) {
        if (lower.contains("pizza"))
            return "#FF7B6B"; // Corallo pastello acceso
        if (lower.contains("italian") || lower.contains("primi") || lower.contains("pasta"))
            return "#85C88A"; // Verde salvia / pistacchio acceso
        if (lower.contains("sushi") || lower.contains("giapponese"))
            return "#95B8D1"; // Azzurro polvere / carta da zucchero
        if (lower.contains("cinese") || lower.contains("asian") || lower.contains("ramen") || lower.contains("coreano")
                || lower.contains("thai") || lower.contains("vietnamita"))
            return "#E86A75"; // Rosso fragola pastello
        if (lower.contains("poke") || lower.contains("hawaiian") || lower.contains("indian") || lower.contains("curry"))
            return "#F4A259"; // Mango / Zafferano pastello
        if (lower.contains("veg") || lower.contains("insalata") || lower.contains("salad")
                || lower.contains("mediterranea") || lower.contains("mediterranean"))
            return "#52B788"; // Verde menta / smeraldo pastello
        if (lower.contains("steak") || lower.contains("churrasco") || lower.contains("grill") || lower.contains("meat")
                || lower.contains("carne") || lower.contains("bbq"))
            return "#D85A7fff"; // Lampone / terracotta rosato
        if (lower.contains("seafood") || lower.contains("crostacei") || lower.contains("aragosta")
                || lower.contains("pesce") || lower.contains("fish") || lower.contains("mare"))
            return "#4EA8DE"; // Blu oceano pastello
        if (lower.contains("messicano") || lower.contains("mexican") || lower.contains("taco")
                || lower.contains("burrito"))
            return "#C87D55"; // Cannella / Terracotta calda
        if (lower.contains("kebab") || lower.contains("turco") || lower.contains("arabo")
                || lower.contains("medio orientale") || lower.contains("libanese"))
            return "#B58A63"; // Nocciola / Sabbia calda
        if (lower.contains("francese") || lower.contains("french"))
            return "#7289DA"; // Lavanda / Periwinkle acceso
        if (lower.contains("greco") || lower.contains("greca") || lower.contains("greek"))
            return "#6BBF8C"; // Olivo chiaro mediterraneo
        if (lower.contains("burger") || lower.contains("fast food") || lower.contains("americano")
                || lower.contains("patatine") || lower.contains("hot dog") || lower.contains("street")
                || lower.contains("panin") || lower.contains("piadina") || lower.contains("toast"))
            return "#E08955"; // Caramello / Toast ambrato
        if (lower.contains("dolci") || lower.contains("dessert") || lower.contains("pasticceria")
                || lower.contains("gelato") || lower.contains("brunch") || lower.contains("colazione")
                || lower.contains("bakery") || lower.contains("crepe"))
            return "#FF85A1"; // Rosa panna / Pasticceria
        if (lower.contains("pub") || lower.contains("birra") || lower.contains("cocktail") || lower.contains("bar")
                || lower.contains("enoteca") || lower.contains("vino") || lower.contains("caff"))
            return "#8377D1"; // Viola mirtillo pastello
        return "#52B788"; // Default: Menta TheKnife
    }
/**
 * Method: getEmojiCucina
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private String getEmojiCucina(String lower) {
        if (lower == null || lower.isEmpty())
            return "👨‍🍳";

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

        return "👨‍🍳";
    }

/**
 * Method: aggiornaValutazioniDaDB
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void aggiornaValutazioniDaDB() {
        if (ristoranteCorrente == null)
            return;
        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    return;
                }
            }

            String responseStats = apiClient.sendRequest("GET_REVIEW_STATS:" + ristoranteCorrente.id);
            if (responseStats != null && responseStats.startsWith("GET_REVIEW_STATS_OK:")) {
                String data = responseStats.substring("GET_REVIEW_STATS_OK:".length());
                Map<String, String> rowData = parseRowData(data);

                double media = 0;
                int totale = 0;
                try {
                    media = Double.parseDouble(rowData.getOrDefault("media_stelle", "0"));
                } catch (NumberFormatException e) {
                }
                try {
                    totale = Integer.parseInt(rowData.getOrDefault("num_recensioni", "0"));
                } catch (NumberFormatException e) {
                }

                String mediaFormattata = String.format("%.1f ★", media);
                if (lblStelleMedia != null)
                    lblStelleMedia.setText(mediaFormattata);
                if (lblRiepilogoStelle != null)
                    lblRiepilogoStelle.setText(mediaFormattata);
                if (lblNumRecensioni != null)
                    lblNumRecensioni.setText(totale + " recensioni");

                if (lblRatingBigNumber != null)
                    lblRatingBigNumber.setText(String.format("%.1f", media));
                if (lblStarsDisplayBig != null)
                    lblStarsDisplayBig.setText(costruisciStelleTesto(media));
                if (lblNumRecensioniBox != null)
                    lblNumRecensioniBox.setText(totale + " recensioni");

                aggiornaDistribuzioneStelle(totale);
            }
        } catch (IOException e) {
            System.err.println("[DETTAGLI] Errore nel recupero dati recensione dal server.");
        }
    }

    /**
     * Costruisce la stringa di 5 caratteri (★ piene / ☆ vuote) corrispondente alla
     * media arrotondata.
     */
/**
 * Method: costruisciStelleTesto
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private String costruisciStelleTesto(double media) {
        int piene = (int) Math.round(media);
        piene = Math.max(0, Math.min(5, piene));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++)
            sb.append(i < piene ? "★" : "☆");
        return sb.toString();
    }

    /**
     * Recupera dal server quante recensioni ci sono per ogni valore di stelle (1-5) e
     * aggiorna le progress bar e le percentuali della card "VALUTAZIONI".
     */
/**
 * Method: aggiornaDistribuzioneStelle
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void aggiornaDistribuzioneStelle(int totaleRecensioni) {
        if (ristoranteCorrente == null)
            return;

        int[] conteggi = new int[6];
        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    return;
                }
            }

            String response = apiClient.sendRequest("GET_STAR_DISTRIBUTION:" + ristoranteCorrente.id);
            if (response != null && response.startsWith("GET_STAR_DISTRIBUTION_OK:")) {
                String data = response.substring("GET_STAR_DISTRIBUTION_OK:".length());
                if (!data.isEmpty()) {
                    String[] rows = data.split(";");
                    for (String row : rows) {
                        Map<String, String> rowData = parseRowData(row);
                        int stelle = 0;
                        int conteggio = 0;
                        try {
                            stelle = Integer.parseInt(rowData.getOrDefault("stelle", "0"));
                        } catch (NumberFormatException e) {
                        }
                        try {
                            conteggio = Integer.parseInt(rowData.getOrDefault("conteggio", "0"));
                        } catch (NumberFormatException e) {
                        }
                        if (stelle >= 1 && stelle <= 5)
                            conteggi[stelle] = conteggio;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[DETTAGLI] Errore nel recupero della distribuzione delle stelle.");
            return;
        }

        aggiornaBarraStella(barStelle5, lblPctStelle5, conteggi[5], totaleRecensioni);
        aggiornaBarraStella(barStelle4, lblPctStelle4, conteggi[4], totaleRecensioni);
        aggiornaBarraStella(barStelle3, lblPctStelle3, conteggi[3], totaleRecensioni);
        aggiornaBarraStella(barStelle2, lblPctStelle2, conteggi[2], totaleRecensioni);
        aggiornaBarraStella(barStelle1, lblPctStelle1, conteggi[1], totaleRecensioni);
    }

/**
 * Method: aggiornaBarraStella
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void aggiornaBarraStella(ProgressBar barra, Label percentualeLabel, int conteggio, int totale) {
        double percentuale = (totale > 0) ? (double) conteggio / totale : 0.0;
        if (barra != null)
            barra.setProgress(percentuale);
        if (percentualeLabel != null)
            percentualeLabel.setText(Math.round(percentuale * 100) + "%");
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
}
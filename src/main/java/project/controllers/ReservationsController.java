package project.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import project.client.services.ServerApiClient;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ReservationsController
 *
 * Controller che gestisce la vista delle prenotazioni dell'utente.
 * Funzionalità:
 * - caricare le prenotazioni dell'utente (GET_USER_RESERVATIONS)
 * - consentire modifica/cancellazione di prenotazioni
 *
 * Nota sulla paginazione: usa currentPage e pageSize per limitare il carico
 * della lista quando ci sono molte prenotazioni.
 **/
/**
 * ReservationsController
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
public class ReservationsController {

    /** Lista grafica delle prenotazioni (bindata da FXML).*/
    @FXML private ListView<ReservationEntry> reservationsListView;
    /** DatePicker per selezionare data di prenotazione.*/
    @FXML private DatePicker reservationDatePicker;
    /** ComboBox con orari disponibili.*/
    @FXML private ComboBox<String> reservationTimeCombo;
    /** Spinner per il numero di persone.*/
    @FXML private Spinner<Integer> reservationGuestsSpinner;
    /** Area per note aggiuntive sulla prenotazione.*/
    @FXML private TextArea reservationNotesArea;
    /** Label per mostrare messaggi di stato relativi alle prenotazioni.*/
    @FXML private Label reservationStatusLabel;
    /** Pulsanti per salvare o cancellare la prenotazione.*/
    @FXML private Button saveReservationButton;
    @FXML private Button deleteReservationButton;

    /** Client di rete condiviso nel controller.*/
    private final ServerApiClient apiClient = new ServerApiClient();
    /** Modello osservabile delle prenotazioni visualizzate nella ListView.*/
    private final ObservableList<ReservationEntry> reservations = FXCollections.observableArrayList();
    /** Prenotazione selezionata dall'utente nella lista.*/
    private ReservationEntry selectedReservation;
    /** Numero di pagina corrente per la paginazione lato client.*/
    private int currentPage = 0;
    /** Dimensione della pagina (numero di elementi per richiesta).*/
    private final int pageSize = 50; // default page size for reservations

    @FXML
/**
 * Method: initialize
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void initialize() {
        reservationGuestsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        reservationTimeCombo.setItems(FXCollections.observableArrayList(buildTimeOptions()));
        reservationTimeCombo.getSelectionModel().select("20:00");

        reservationsListView.setItems(reservations);
        reservationsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
/**
 * Method: updateItem
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
            protected void updateItem(ReservationEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(buildReservationCard(item));
                    setText(null);
                }
            }
        });

        reservationsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                selectedReservation = newValue;
                fillForm(newValue);
            }
        });

        loadReservations();
    }

    @FXML
/**
 * Method: handleBack
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleBack() {
        Navigator.getInstance().navigateToHomeIntelligent();
    }

    @FXML
/**
 * Method: handleGoToHome
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleGoToHome() {
        Navigator.getInstance().navigateToHomeIntelligent();
    }

    @FXML
/**
 * Method: handleGoToProfile
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleGoToProfile() {
        Navigator.getInstance().navigateToProfile();
    }

    @FXML
/**
 * Method: handleRefresh
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleRefresh() {
        loadReservations();
    }

    @FXML
/**
 * Method: handleSaveReservation
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleSaveReservation() {
        if (selectedReservation == null) {
            showStatus("Seleziona una prenotazione da modificare", true);
            return;
        }

        LocalDate date = reservationDatePicker.getValue();
        String time = reservationTimeCombo.getValue();
        int guests = reservationGuestsSpinner.getValue();
        String note = reservationNotesArea.getText() == null ? "" : reservationNotesArea.getText().replace(";", ",");

        if (date == null || time == null || time.isBlank()) {
            showStatus("Inserisci data e ora valide", true);
            return;
        }

        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    showStatus("Connessione al server non disponibile", true);
                    return;
                }
            }

            String rawTime = time.replace(":", "");
            String response = apiClient.sendRequest(
                    "UPDATE_RESERVATION:" + selectedReservation.id + ":" + Navigator.getInstance().getIdUtenteLoggato() + ":"
                            + date + ":" + rawTime + ":" + guests + ":" + note
            );

            if (response != null && response.startsWith("UPDATE_RESERVATION_OK:")) {
                showStatus("Prenotazione aggiornata correttamente", false);
                loadReservations();
            } else if (response != null && response.startsWith("ERROR:Sessione")) {
                Navigator.getInstance().logout();
                Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
            } else {
                showStatus(response != null ? response.replace("UPDATE_RESERVATION_FAIL:", "") : "Aggiornamento non riuscito", true);
            }
        } catch (IOException e) {
            showStatus("Errore durante l'aggiornamento: " + e.getMessage(), true);
        }
    }

    @FXML
/**
 * Method: handleDeleteReservation
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void handleDeleteReservation() {
        if (selectedReservation == null) {
            showStatus("Seleziona una prenotazione da cancellare", true);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Conferma cancellazione");
        confirmation.setHeaderText("Eliminare la prenotazione " + selectedReservation.code + "?");
        confirmation.setContentText("L'operazione non può essere annullata.");

        confirmation.showAndWait().ifPresent(button -> {
            if (button == ButtonType.OK) {
                try {
                    if (!apiClient.isConnected()) {
                        if (!apiClient.ensureConnected()) {
                            showStatus("Connessione al server non disponibile", true);
                            return;
                        }
                    }

                    String response = apiClient.sendRequest(
                            "DELETE_RESERVATION:" + selectedReservation.id + ":" + Navigator.getInstance().getIdUtenteLoggato()
                    );

                    if (response != null && response.startsWith("DELETE_RESERVATION_OK:")) {
                        showStatus("Prenotazione eliminata", false);
                        loadReservations();
                    } else if (response != null && response.startsWith("ERROR:Sessione")) {
                        Navigator.getInstance().logout();
                        Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
                    } else {
                        showStatus(response != null ? response.replace("DELETE_RESERVATION_FAIL:", "") : "Cancellazione non riuscita", true);
                    }
                } catch (IOException e) {
                    showStatus("Errore durante la cancellazione: " + e.getMessage(), true);
                }
            }
        });
    }

/**
 * Method: loadReservations
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void loadReservations() {
        int userId = Navigator.getInstance().getIdUtenteLoggato();
        if (userId <= 0) {
            showStatus("Effettua il login per vedere le prenotazioni", true);
            Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
            return;
        }

        try {
            if (!apiClient.isConnected()) {
                if (!apiClient.ensureConnected()) {
                    showStatus("Connessione al server non disponibile", true);
                    return;
                }
            }

            String response = apiClient.sendRequest("GET_USER_RESERVATIONS:" + userId + ":" + currentPage + ":" + pageSize);
            if (response != null && response.startsWith("GET_USER_RESERVATIONS_OK:")) {
                String payload = response.substring("GET_USER_RESERVATIONS_OK:".length());
                // strip possible MORE marker appended by server
                if (payload.endsWith("|MORE")) payload = payload.substring(0, payload.length() - 5);
                parseReservations(payload);
            } else if (response != null && response.startsWith("ERROR:Sessione")) {
                // session invalid -> force logout
                Navigator.getInstance().logout();
                Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
            } else {
                reservations.clear();
                showStatus("Nessuna prenotazione trovata", false);
            }
        } catch (IOException e) {
            showStatus("Errore nel caricamento: " + e.getMessage(), true);
        }
    }

/**
 * Method: parseReservations
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void parseReservations(String payload) {
        reservations.clear();
        if (payload == null || payload.isBlank() || "0".equals(payload.trim())) {
            selectedReservation = null;
            clearForm();
            showStatus("Non hai ancora prenotazioni", false);
            return;
        }

        for (String entry : payload.split(";")) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            String[] parts = entry.split("\\|", -1);
            if (parts.length < 9) {
                continue;
            }
            try {
                ReservationEntry reservation = new ReservationEntry();
                reservation.id = Integer.parseInt(parts[0].trim());
                reservation.restaurantId = parts[1].trim();
                reservation.restaurantName = parts[2].trim();
                reservation.date = parts[3].trim();
                reservation.time = parts[4].trim();
                reservation.people = Integer.parseInt(parts[5].trim());
                reservation.code = parts[6].trim();
                reservation.note = parts[7].trim();
                reservation.state = parts[8].trim();
                reservations.add(reservation);
            } catch (NumberFormatException ignored) {
                // ignora record malformato
            }
        }

        if (!reservations.isEmpty()) {
            reservationsListView.getSelectionModel().selectFirst();
            showStatus("Hai " + reservations.size() + " prenotazione/i", false);
        } else {
            clearForm();
            showStatus("Nessuna prenotazione trovata", false);
        }
    }

/**
 * Method: fillForm
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void fillForm(ReservationEntry reservation) {
        if (reservation == null) {
            clearForm();
            return;
        }
        reservationDatePicker.setValue(LocalDate.parse(reservation.date));
        reservationTimeCombo.setValue(reservation.time);
        reservationGuestsSpinner.getValueFactory().setValue(reservation.people);
        reservationNotesArea.setText(reservation.note);
    }

/**
 * Method: clearForm
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void clearForm() {
        reservationDatePicker.setValue(LocalDate.now());
        reservationTimeCombo.getSelectionModel().select("20:00");
        reservationGuestsSpinner.getValueFactory().setValue(2);
        reservationNotesArea.clear();
    }

/**
 * Method: buildTimeOptions
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private List<String> buildTimeOptions() {
        // Safe time options builder: cap iterations to avoid infinite loops causing OOM
        List<String> options = new ArrayList<>();
        LocalTime slot = LocalTime.of(12, 0);
        LocalTime end = LocalTime.of(23, 30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        // maximum 48 slots (24 hours / 30min) as a safety cap
        int maxIterations = 48;
        int i = 0;
        while (!slot.isAfter(end) && i < maxIterations) {
            options.add(slot.format(formatter));
            slot = slot.plusMinutes(30);
            i++;
        }
        return options;
    }

/**
 * Method: buildReservationCard
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private VBox buildReservationCard(ReservationEntry item) {
        VBox card = new VBox(8);
        card.getStyleClass().add("reservation-card");
        card.setMinHeight(120);

        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label restaurant = new Label(item.restaurantName);
        restaurant.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #163d2f;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label status = new Label(getReservationStatusLabel(item.state));
        status.getStyleClass().addAll("reservation-status-pill", getReservationStatusStyleClass(item.state));

        header.getChildren().addAll(restaurant, spacer, status);

        Label meta = new Label(item.date + " • " + item.time + " • " + item.people + " persone");
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #4A6B57;");

        Label code = new Label("Codice: " + item.code);
        code.setStyle("-fx-font-size: 12px; -fx-text-fill: #1B4332; -fx-font-weight: bold;");

        String noteText = item.note == null || item.note.isBlank() ? "Nessuna nota aggiunta" : item.note;
        Label note = new Label(noteText);
        note.setWrapText(true);
        note.setStyle("-fx-font-size: 12px; -fx-text-fill: #526B62; -fx-opacity: 0.95;");

        card.getChildren().addAll(header, meta, code, note);
        return card;
    }

/**
 * Method: getReservationStatusLabel
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private String getReservationStatusLabel(String state) {
        String normalized = normalizeReservationState(state);
        switch (normalized) {
            case "accettata": return "Accettata";
            case "rifiutata": return "Rifiutata";
            default: return "In attesa";
        }
    }

/**
 * Method: getReservationStatusStyleClass
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private String getReservationStatusStyleClass(String state) {
        String normalized = normalizeReservationState(state);
        switch (normalized) {
            case "accettata": return "status-accepted";
            case "rifiutata": return "status-rejected";
            default: return "status-pending";
        }
    }

/**
 * Method: normalizeReservationState
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private String normalizeReservationState(String state) {
        if (state == null) return "attiva";
        String normalized = state.trim().toLowerCase();
        if (normalized.contains("accett")) return "accettata";
        if (normalized.contains("rifiut") || normalized.contains("respinta")) return "rifiutata";
        if (normalized.contains("attiv") || normalized.contains("in attesa") || normalized.contains("modif")) return "attiva";
        return normalized;
    }

/**
 * Method: showStatus
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void showStatus(String message, boolean error) {
        if (reservationStatusLabel == null) {
            return;
        }
        reservationStatusLabel.setText(message);
        reservationStatusLabel.setStyle(error ? "-fx-text-fill: #c0392b;" : "-fx-text-fill: #2e7d32;");
    }

    public static class ReservationEntry {
/**
 * Field: id
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
        private int id;
/**
 * Field: restaurantId
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
        private String restaurantId;
/**
 * Field: restaurantName
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
        private String restaurantName;
/**
 * Field: date
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
        private String date;
/**
 * Field: time
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
        private String time;
/**
 * Field: people
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
        private int people;
/**
 * Field: code
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
        private String code;
/**
 * Field: note
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
        private String note;
/**
 * Field: state
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
        private String state;
    }
}

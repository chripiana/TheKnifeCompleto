package project.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import project.client.services.ServerApiClient;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReservationsController {

    @FXML private ListView<ReservationEntry> reservationsListView;
    @FXML private DatePicker reservationDatePicker;
    @FXML private ComboBox<String> reservationTimeCombo;
    @FXML private Spinner<Integer> reservationGuestsSpinner;
    @FXML private TextArea reservationNotesArea;
    @FXML private Label reservationStatusLabel;
    @FXML private Button saveReservationButton;
    @FXML private Button deleteReservationButton;

    private final ServerApiClient apiClient = new ServerApiClient();
    private final ObservableList<ReservationEntry> reservations = FXCollections.observableArrayList();
    private ReservationEntry selectedReservation;
    private int currentPage = 0;
    private final int pageSize = 50; // default page size for reservations

    @FXML
    public void initialize() {
        reservationGuestsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        reservationTimeCombo.setItems(FXCollections.observableArrayList(buildTimeOptions()));
        reservationTimeCombo.getSelectionModel().select("20:00");

        reservationsListView.setItems(reservations);
        reservationsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(ReservationEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.code + " • " + item.restaurantName + " • " + item.date + " " + item.time);
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
    private void handleBack() {
        Navigator.getInstance().navigateToHomeIntelligent();
    }

    @FXML
    private void handleGoToHome() {
        Navigator.getInstance().navigateToHomeIntelligent();
    }

    @FXML
    private void handleGoToProfile() {
        Navigator.getInstance().navigateToProfile();
    }

    @FXML
    private void handleRefresh() {
        loadReservations();
    }

    @FXML
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

    private void clearForm() {
        reservationDatePicker.setValue(LocalDate.now());
        reservationTimeCombo.getSelectionModel().select("20:00");
        reservationGuestsSpinner.getValueFactory().setValue(2);
        reservationNotesArea.clear();
    }

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

    private void showStatus(String message, boolean error) {
        if (reservationStatusLabel == null) {
            return;
        }
        reservationStatusLabel.setText(message);
        reservationStatusLabel.setStyle(error ? "-fx-text-fill: #c0392b;" : "-fx-text-fill: #2e7d32;");
    }

    public static class ReservationEntry {
        private int id;
        private String restaurantId;
        private String restaurantName;
        private String date;
        private String time;
        private int people;
        private String code;
        private String note;
        private String state;
    }
}

package project.demo;

import db.TheKnifeDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class OwnerDashboardController {

    @FXML
    private VBox containerRecensioni;
    @FXML
    private VBox containerRistoranti;

    @FXML
    private Label dashNomeLabel;
    @FXML
    private Label dashOwnerLabel;
    @FXML
    private Label dashAddressLabel;

    @FXML
    private Label kpiNumRecensioniLabel;
    @FXML
    private Label kpiMediaStelleLabel;
    @FXML
    private Label kpiSenzaRispostaLabel;

    // Form fields
    @FXML
    private TextField nomeField;
    @FXML
    private TextField cittaField;
    @FXML
    private TextField nazioneField;
    @FXML
    private TextField indirizzoField;
    @FXML
    private TextField latField;
    @FXML
    private TextField lonField;
    @FXML
    private ComboBox<String> fasciaCombo;
    @FXML
    private TextField tipoCucinaField;
    @FXML
    private CheckBox deliveryCheck;
    @FXML
    private CheckBox prenotazioneCheck;
    @FXML
    private Button btnSalvaRistorante;
    @FXML
    private Button btnEliminaRistorante;
    @FXML
    private Button btnVediScheda;

    private Navigator navigator;
    private TheKnifeDAO dao;
    private int idGestore;

    // In-memory structures
    private List<RistoranteSummary> ristoranti = new ArrayList<>();
    private Map<String, List<ReviewItem>> reviewsByRistorante = new HashMap<>();
    private String selectedRestaurantId = null;
    private String selectedRestaurantName = null;

    @FXML
    public void initialize() {
        this.navigator = Navigator.getInstance();
        this.dao = navigator.getDao();
        this.idGestore = navigator.getIdUtenteLoggato();

        // Popolo combo con valori numerici standard (usiamo stringhe, useremo il valore
        // esatto del DB quando apriamo un ristorante)
        if (fasciaCombo != null) {
            fasciaCombo.setItems(FXCollections.observableArrayList("15", "30", "45", "60", "80"));
        }

        if (idGestore == -1) {
            // Nessun gestore loggato
            navigator.navigateTo("login-view.fxml", "Accedi");
            return;
        }

        Platform.runLater(this::caricaRistorantiERecensioni);

        // Handlers form
        if (btnSalvaRistorante != null) {
            btnSalvaRistorante.setOnAction(ev -> salvaRistorante());
        }
        if (btnEliminaRistorante != null) {
            btnEliminaRistorante.setOnAction(ev -> eliminaRistorante());
        }
        if (btnVediScheda != null) {
            btnVediScheda.setOnAction(ev -> vediSchedaRistorante());
        }
    }

    private void caricaRistorantiERecensioni() {
        containerRistoranti.getChildren().clear();
        containerRecensioni.getChildren().clear();
        ristoranti.clear();
        reviewsByRistorante.clear();

        try {
            // 1) riepilogo ristoranti
            ResultSet rs = dao.visualizzaRiepilogo(idGestore);
            while (rs != null && rs.next()) {
                String id = rs.getString("id_ristorante");
                String nome = rs.getString("nome");
                String citta = rs.getString("citta");
                double media = rs.getDouble("media_stelle");
                int numRec = rs.getInt("num_recensioni");
                ristoranti.add(new RistoranteSummary(id, nome, citta, media, numRec));
            }
            if (rs != null)
                rs.close();

            // 2) recensioni del gestore (raggruppo per id_ristorante)
            ResultSet rs2 = dao.visualizzaRecensioniGestore(idGestore);
            while (rs2 != null && rs2.next()) {
                String idR = rs2.getString("id_ristorante");
                String nomeR = rs2.getString("nome");
                int idRec = rs2.getInt("id_recensione");
                int stelle = rs2.getInt("stelle");
                String testo = rs2.getString("testo");
                String data = rs2.getString("data_recensione");
                String autoreNome = rs2.getString("autore_nome");
                String autoreCognome = rs2.getString("autore_cognome");
                boolean gia_risposto = rs2.getBoolean("gia_risposto");
                String risposta = rs2.getString("risposta");
                String dataRisposta = rs2.getString("data_risposta");

                ReviewItem item = new ReviewItem(idRec, stelle, testo, data, autoreNome, autoreCognome, gia_risposto,
                        risposta, dataRisposta);
                reviewsByRistorante.computeIfAbsent(idR, k -> new ArrayList<>()).add(item);
            }
            if (rs2 != null)
                rs2.close();

            // 3) render lista ristoranti
            if (ristoranti.isEmpty()) {
                Label empty = new Label(
                        "Non hai ancora ristoranti registrati. Crea il tuo primo ristorante dal pannello.");
                empty.setStyle("-fx-text-fill: #6B6B6B;");
                containerRistoranti.getChildren().add(empty);
            } else {
                for (RistoranteSummary r : ristoranti) {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);

                    Label nome = new Label(r.nome);
                    nome.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

                    Label meta = new Label(
                            r.citta + " · " + String.format("%.1f ★ (%d)", r.mediaStelle, r.numRecensioni));
                    meta.setStyle("-fx-text-fill: #4A6B57; -fx-font-size: 12;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button apri = new Button("Apri");
                    apri.getStyleClass().add("btn-outline");

                    apri.setOnAction(ev -> openRestaurant(r));

                    row.getChildren().addAll(nome, meta, spacer, apri);
                    containerRistoranti.getChildren().add(row);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Label err = new Label("Errore nel caricamento dei ristoranti/recensioni. Controlla il server.");
            err.setStyle("-fx-text-fill: #C0392B;");
            containerRistoranti.getChildren().add(err);
        }
    }

    private void openRestaurant(RistoranteSummary r) {
        selectedRestaurantId = r.id;
        selectedRestaurantName = r.nome;

        // Aggiorno hero
        dashNomeLabel.setText(r.nome);
        // owner: proviamo a leggere i dati dell'utente
        try {
            var rsUser = dao.getDatiUtente(idGestore);
            if (rsUser != null && rsUser.next()) {
                String nomeU = rsUser.getString("nome");
                String cogn = rsUser.getString("cognome");
                String email = rsUser.getString("email");
                dashOwnerLabel.setText((nomeU != null ? nomeU : "") + " " + (cogn != null ? cogn : "") + " — "
                        + (email != null ? email : ""));
            }
            if (rsUser != null)
                rsUser.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Riempio form con getRistorantePerId
        try {
            ResultSet rs = dao.getRistorantePerId(selectedRestaurantId, idGestore);
            if (rs != null && rs.next()) {
                nomeField.setText(rs.getString("nome"));
                cittaField.setText(rs.getString("citta"));
                nazioneField.setText(rs.getString("nazione"));
                indirizzoField.setText(rs.getString("indirizzo"));
                latField.setText(String.valueOf(rs.getDouble("latitudine")));
                lonField.setText(String.valueOf(rs.getDouble("longitudine")));
                tipoCucinaField.setText(rs.getString("tipologia_cucina"));
                boolean delivery = rs.getBoolean("delivery");
                boolean pren = rs.getBoolean("prenotazione_online");
                deliveryCheck.setSelected(delivery);
                prenotazioneCheck.setSelected(pren);
                // prezzo_medio: selezioniamo il valore esatto (se non presente, aggiungiamo)
                int prezzoMedio = rs.getInt("prezzo_medio");
                String prezzoStr = String.valueOf(prezzoMedio);
                if (!fasciaCombo.getItems().contains(prezzoStr)) {
                    fasciaCombo.getItems().add(prezzoStr);
                }
                fasciaCombo.getSelectionModel().select(prezzoStr);
            }
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Aggiorno KPI
        kpiNumRecensioniLabel.setText(String.valueOf(r.numRecensioni));
        kpiMediaStelleLabel.setText(String.format("%.1f", r.mediaStelle));
        int senza = 0;
        List<ReviewItem> list = reviewsByRistorante.getOrDefault(r.id, Collections.emptyList());
        for (ReviewItem it : list)
            if (!it.giaRisposto)
                senza++;
        kpiSenzaRispostaLabel.setText(String.valueOf(senza));

        // Popolo containerRecensioni con le recensioni del ristorante selezionato
        populateReviewsContainer(list);
    }

    private void populateReviewsContainer(List<ReviewItem> reviews) {
        containerRecensioni.getChildren().clear();

        if (reviews.isEmpty()) {
            Label none = new Label("Nessuna recensione per questo ristorante.");
            none.setStyle("-fx-text-fill: #6B6B6B;");
            containerRecensioni.getChildren().add(none);
            return;
        }

        for (ReviewItem r : reviews) {
            VBox card = new VBox(8);
            card.getStyleClass().add("rec-card");

            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);

            Label avatar = new Label(initialsFromText(r.testo));
            avatar.getStyleClass().add("avatar-small");
            avatar.setAlignment(Pos.CENTER);

            VBox meta = new VBox(2);
            String nomeAutore = (r.autoreNome != null ? r.autoreNome : "") + " "
                    + (r.autoreCognome != null ? r.autoreCognome : "");
            Label username = new Label(nomeAutore.trim().isEmpty() ? "Cliente" : nomeAutore.trim());
            username.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
            Label data = new Label(r.data);
            data.setStyle("-fx-text-fill: #4A6B57; -fx-font-size: 11;");
            meta.getChildren().addAll(username, data);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label stelle = new Label(String.join("", Collections.nCopies(r.stelle, "★")));
            stelle.setStyle("-fx-text-fill: #F6A623; -fx-font-size: 13;");

            Label badgeStatus = new Label(r.giaRisposto ? "✅ Risposto" : "⏳ Senza risposta");
            badgeStatus.getStyleClass().addAll("badge-status", r.giaRisposto ? "status-ok" : "status-no");

            header.getChildren().addAll(avatar, meta, spacer, stelle, badgeStatus);

            Label testo = new Label(r.testo);
            testo.setWrapText(true);
            testo.setStyle("-fx-font-size: 13; -fx-text-fill: #1B2E24; -fx-line-spacing: 1.4;");

            card.getChildren().addAll(header, testo);

            if (r.giaRisposto && r.risposta != null) {
                VBox boxRisposta = new VBox(4);
                boxRisposta.getStyleClass().add("risposta-box");

                Label titoloRisposta = new Label(
                        "👨‍🍳 Tua risposta — " + (r.dataRisposta == null ? "" : r.dataRisposta));
                titoloRisposta.setStyle("-fx-font-weight: bold; -fx-font-size: 11; -fx-text-fill: #1B4332;");
                Label testoRisposta = new Label(r.risposta);
                testoRisposta.setStyle("-fx-font-size: 12; -fx-text-fill: #4A6B57;");

                boxRisposta.getChildren().addAll(titoloRisposta, testoRisposta);
                card.getChildren().add(boxRisposta);
            }

            // Form risposta
            HBox formRisposta = new HBox(8);
            formRisposta.setAlignment(Pos.BOTTOM_LEFT);

            TextArea inputRisposta = new TextArea();
            inputRisposta.setPromptText(r.giaRisposto ? "Modifica la risposta..." : "Scrivi una risposta pubblica...");
            inputRisposta.setPrefHeight(50);
            HBox.setHgrow(inputRisposta, Priority.ALWAYS);
            inputRisposta.getStyleClass().add("home-text-field");
            if (r.giaRisposto && r.risposta != null)
                inputRisposta.setText(r.risposta);

            Button btnInvia = new Button(r.giaRisposto ? "✏️ Modifica" : "📤 Rispondi");
            btnInvia.getStyleClass().add("btn-primary");
            btnInvia.setStyle("-fx-padding: 6 14; -fx-font-size: 12;");

            btnInvia.setOnAction(ev -> {
                String testoRisposta = inputRisposta.getText().trim();
                if (testoRisposta.isEmpty()) {
                    Alert a = new Alert(Alert.AlertType.WARNING, "La risposta non può essere vuota.", ButtonType.OK);
                    a.showAndWait();
                    return;
                }
                try {
                    int updated = dao.rispostaRecensione(r.idRecensione, idGestore, testoRisposta);
                    if (updated > 0) {
                        // ricarica dati
                        caricaRistorantiERecensioni();
                        // riapri lo stesso ristorante per aggiornare la vista
                        Optional<RistoranteSummary> opt = ristoranti.stream()
                                .filter(x -> x.id.equals(selectedRestaurantId)).findFirst();
                        opt.ifPresent(this::openRestaurant);
                    } else {
                        Alert a = new Alert(Alert.AlertType.ERROR, "Errore nell'inserimento della risposta.",
                                ButtonType.OK);
                        a.showAndWait();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert a = new Alert(Alert.AlertType.ERROR, "Errore server: impossibile salvare la risposta.",
                            ButtonType.OK);
                    a.showAndWait();
                }
            });

            formRisposta.getChildren().addAll(inputRisposta, btnInvia);
            card.getChildren().add(formRisposta);

            containerRecensioni.getChildren().add(card);
        }
    }

    private void salvaRistorante() {
        if (selectedRestaurantId == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Seleziona prima un ristorante dalla lista.", ButtonType.OK);
            a.showAndWait();
            return;
        }
        try {
            String nome = nomeField.getText().trim();
            String citta = cittaField.getText().trim();
            String nazione = nazioneField.getText().trim();
            String indirizzo = indirizzoField.getText().trim();
            int prezzoMedio = 0;
            String fascia = fasciaCombo.getSelectionModel().getSelectedItem();
            if (fascia != null && !fascia.isBlank()) {
                try {
                    prezzoMedio = Integer.parseInt(fascia);
                } catch (NumberFormatException ex) {
                    // fallback al valore 0 se non numerico
                    prezzoMedio = 0;
                }
            }
            boolean delivery = deliveryCheck.isSelected();
            boolean pren = prenotazioneCheck.isSelected();
            String tipoCuc = tipoCucinaField.getText().trim();

            boolean ok = dao.modificaRistorante(selectedRestaurantId, idGestore, nome, citta, indirizzo, prezzoMedio,
                    delivery, pren, tipoCuc);
            if (ok) {
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Ristorante aggiornato con successo.", ButtonType.OK);
                a.showAndWait();
                caricaRistorantiERecensioni();
                // riapri
                Optional<RistoranteSummary> opt = ristoranti.stream().filter(x -> x.id.equals(selectedRestaurantId))
                        .findFirst();
                opt.ifPresent(this::openRestaurant);
            } else {
                Alert a = new Alert(Alert.AlertType.ERROR, "Non è stato possibile aggiornare il ristorante.",
                        ButtonType.OK);
                a.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Errore durante l'aggiornamento del ristorante.", ButtonType.OK);
            a.showAndWait();
        }
    }

    private void eliminaRistorante() {
        if (selectedRestaurantId == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Seleziona prima un ristorante dalla lista.", ButtonType.OK);
            a.showAndWait();
            return;
        }
        // Modale di conferma
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("Conferma eliminazione");
        conferma.setHeaderText("Sei sicuro di voler eliminare questo ristorante?");
        conferma.setContentText("Questa azione è irreversibile. Tutti i dati del ristorante saranno cancellati.");
        Optional<ButtonType> result = conferma.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean ok = dao.eliminaRistorante(selectedRestaurantId, idGestore);
                if (ok) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Ristorante eliminato con successo.",
                            ButtonType.OK);
                    a.showAndWait();
                    selectedRestaurantId = null;
                    selectedRestaurantName = null;
                    caricaRistorantiERecensioni();
                    containerRecensioni.getChildren().clear();
                    // pulisco form
                    nomeField.clear();
                    cittaField.clear();
                    nazioneField.clear();
                    indirizzoField.clear();
                    latField.clear();
                    lonField.clear();
                    tipoCucinaField.clear();
                    fasciaCombo.getSelectionModel().clearSelection();
                    deliveryCheck.setSelected(false);
                    prenotazioneCheck.setSelected(false);
                } else {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Non è stato possibile eliminare il ristorante.",
                            ButtonType.OK);
                    a.showAndWait();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR, "Errore durante l'eliminazione del ristorante.",
                        ButtonType.OK);
                a.showAndWait();
            }
        }
    }

    private void vediSchedaRistorante() {
        if (selectedRestaurantId == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Seleziona prima un ristorante dalla lista.", ButtonType.OK);
            a.showAndWait();
            return;
        }
        // Placeholder: in futuro si navigherà alla scheda pubblica del ristorante
        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Ristorante: " + selectedRestaurantName + "\n" +
                        "ID: " + selectedRestaurantId + "\n\n" +
                        "La scheda pubblica di questo ristorante sarà disponibile sulla pagina principale di ricerca dei clienti.",
                ButtonType.OK);
        a.setTitle("Scheda Pubblica");
        a.setHeaderText("Visualizza scheda pubblica");
        a.showAndWait();
    }

    // Utility
    private String initialsFromText(String testo) {
        if (testo == null || testo.isBlank())
            return "??";
        String[] parts = testo.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    private static class RistoranteSummary {
        final String id;
        final String nome;
        final String citta;
        final double mediaStelle;
        final int numRecensioni;

        RistoranteSummary(String id, String nome, String citta, double media, int numRec) {
            this.id = id;
            this.nome = nome;
            this.citta = citta;
            this.mediaStelle = media;
            this.numRecensioni = numRec;
        }
    }

    private static class ReviewItem {
        final int idRecensione;
        final int stelle;
        final String testo;
        final String data;
        final String autoreNome;
        final String autoreCognome;
        final boolean giaRisposto;
        final String risposta;
        final String dataRisposta;

        ReviewItem(int idRecensione, int stelle, String testo, String data, String autoreNome, String autoreCognome,
                boolean giaRisposto, String risposta, String dataRisposta) {
            this.idRecensione = idRecensione;
            this.stelle = stelle;
            this.testo = testo;
            this.data = data;
            this.autoreNome = autoreNome;
            this.autoreCognome = autoreCognome;
            this.giaRisposto = giaRisposto;
            this.risposta = risposta;
            this.dataRisposta = dataRisposta;
        }
    }

    @FXML
    private void handleGoToHome(ActionEvent event) {
        Navigator.getInstance().navigateToHomeIntelligent();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        navigator.logout();
        navigator.navigateTo("login-view.fxml", "Accedi");
    }

    @FXML
    private void handleNuovoRistorante(ActionEvent event) {
        navigator.navigateTo("new-restaurant-view.fxml", "Crea Nuovo Ristorante");
    }
}
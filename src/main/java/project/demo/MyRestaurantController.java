
package project.demo;

import db.TheKnifeDAO;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class MyRestaurantController {

    @FXML
    private FlowPane containerRistoranti;
    @FXML
    private VBox emptyRistorantiBox;
    @FXML
    private Label lblNumRistoranti;

    private Navigator navigator;
    private TheKnifeDAO dao;
    private int idGestoreLoggato;

    @FXML
    public void initialize() {
        this.navigator = Navigator.getInstance();
        this.dao = navigator.getDao();
        this.idGestoreLoggato = navigator.getIdUtenteLoggato();

        if (idGestoreLoggato != -1) {
            caricaRistoranti();
        } else {
            navigator.navigateTo("login-view.fxml", "Accedi");
        }
    }

    /**
     * Carica dinamicamente dal database tutti i ristoranti del gestore loggato,
     * con statistiche di recensione annesse (media stelle e numero recensioni).
     */
    private void caricaRistoranti() {
        containerRistoranti.getChildren().clear();
        int totale = 0;

        try {
            ResultSet rs = dao.visualizzaRiepilogo(idGestoreLoggato);

            while (rs != null && rs.next()) {
                totale++;

                String idRistorante = rs.getString("id_ristorante");
                String nome = rs.getString("nome");
                String citta = rs.getString("citta");
                double mediaStelle = rs.getDouble("media_stelle");
                int numRecensioni = rs.getInt("num_recensioni");

                containerRistoranti.getChildren()
                        .add(creaCardRistorante(idRistorante, nome, citta, mediaStelle, numRecensioni));
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 18; -fx-pref-width: 300; " +
                "-fx-border-color: #EDF1EE; -fx-border-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(27,67,50,0.06), 10, 0, 0, 3);");

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
     * Apre una finestra di dialogo per modificare le specifiche di un ristorante
     * (nome, città, indirizzo, tipologia cucina, prezzo medio, delivery,
     * prenotazione online).
     */
    private void apriDialogModifica(String idRistorante) {
        try {
            ResultSet rs = dao.getRistorantePerId(idRistorante, idGestoreLoggato);
            if (rs == null || !rs.next()) {
                mostraErrore("Impossibile trovare i dati del ristorante.");
                return;
            }

            String nomeAttuale = rs.getString("nome");
            String cittaAttuale = rs.getString("citta");
            String indirizzoAttuale = rs.getString("indirizzo");
            String cucinaAttuale = rs.getString("tipologia_cucina");
            int prezzoAttuale = rs.getInt("prezzo_medio");
            boolean deliveryAttuale = rs.getBoolean("delivery");
            boolean prenotazioneAttuale = rs.getBoolean("prenotazione_online");

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

                boolean ok = dao.modificaRistorante(idRistorante, idGestoreLoggato,
                        txtNome.getText().trim(), txtCitta.getText().trim(), txtIndirizzo.getText().trim(),
                        nuovoPrezzo, chkDelivery.isSelected(), chkPrenotazione.isSelected(),
                        txtCucina.getText().trim());

                if (ok) {
                    caricaRistoranti();
                } else {
                    mostraErrore("Nessuna modifica salvata. Riprova.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostraErrore("Errore durante il caricamento dei dati del ristorante.");
        }
    }

    private void confermaEliminazione(String idRistorante, String nome) {
        Alert conferma = new Alert(AlertType.CONFIRMATION);
        conferma.setTitle("Elimina Ristorante");
        conferma.setHeaderText("Vuoi eliminare \"" + nome + "\"?");
        conferma.setContentText("Questa azione è irreversibile e rimuoverà anche le recensioni associate.");

        Optional<ButtonType> risultato = conferma.showAndWait();
        if (risultato.isPresent() && risultato.get() == ButtonType.OK) {
            try {
                boolean ok = dao.eliminaRistorante(idRistorante, idGestoreLoggato);
                if (ok) {
                    caricaRistoranti();
                } else {
                    mostraErrore("Impossibile eliminare il ristorante.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                mostraErrore("Errore durante l'eliminazione del ristorante.");
            }
        }
    }

    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
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
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        navigator.navigateToHomeIntelligent();
    }
}
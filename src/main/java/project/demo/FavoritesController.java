package project.demo;

import db.TheKnifeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FavoritesController {

    @FXML private FlowPane containerPreferiti;
    @FXML private VBox emptyFavoritesBox;

    private Navigator navigator;
    private TheKnifeDAO dao;
    private int idUtenteLoggato;

    @FXML
    public void initialize() {
        this.navigator = Navigator.getInstance();
        this.dao = navigator.getDao();
        this.idUtenteLoggato = navigator.getIdUtenteLoggato();

        if (idUtenteLoggato != -1) {
            caricaPreferiti();
        } else {
            navigator.navigateTo("login-view.fxml", "Accedi");
        }
    }

    private void caricaPreferiti() {
        containerPreferiti.getChildren().clear();
        boolean haPreferiti = false;

        try {
            ResultSet rs = dao.getPreferitiUtente(idUtenteLoggato);

            while (rs != null && rs.next()) {
                haPreferiti = true;
                // Modifica queste righe all'interno del ciclo while (rs.next())
                String nome = rs.getString("nome");
                String citta = rs.getString("citta");
                String cucina = rs.getString("tipologia_cucina"); // <-- Modificato da "cucina"
                double prezzo = rs.getDouble("prezzo_medio");    // <-- Modificato da "prezzo"

                // Costruiamo la Card graficamente in modo dinamico
                VBox card = new VBox(10);
                card.getStyleClass().add("card-favorites");

                Label lblNome = new Label(nome);
                lblNome.getStyleClass().add("label-card-title");

                Label lblDettagli = new Label("📍 " + citta + "  •  🍳 " + cucina);
                lblDettagli.getStyleClass().add("label-card-details");

                Label lblPrezzo = new Label("💰 Prezzo medio: " + prezzo + " €");
                lblPrezzo.getStyleClass().add("label-card-price");

                card.getChildren().addAll(lblNome, lblDettagli, lblPrezzo);
                containerPreferiti.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Se non ci sono preferiti, nascondi il contenitore e mostra la box di avviso negativo
        if (haPreferiti) {
            containerPreferiti.setVisible(true);
            containerPreferiti.setManaged(true);
            emptyFavoritesBox.setVisible(false);
            emptyFavoritesBox.setManaged(false);
        } else {
            containerPreferiti.setVisible(false);
            containerPreferiti.setManaged(false);
            emptyFavoritesBox.setVisible(true);
            emptyFavoritesBox.setManaged(true);
        }
    }

    @FXML void handleCerca(ActionEvent event) { navigator.navigateTo("search-view-logged.fxml", "Cerca Ristoranti"); }
    @FXML void handleRecensioni(ActionEvent event) { navigator.navigateTo("reviews-view.fxml", "Le Mie Recensioni"); }
    @FXML void handleProfilo(ActionEvent event) { navigator.navigateToProfile(); }
    @FXML void handleLogout(ActionEvent event) {
        navigator.logout();
        navigator.navigateTo("login-view.fxml", "Accedi");
    }
    @FXML void handleVaiAllaRicerca(ActionEvent event) { handleCerca(event); }
    @FXML private void handleGoToHome(javafx.scene.input.MouseEvent event) { navigator.navigateToHomeIntelligent(); }
}
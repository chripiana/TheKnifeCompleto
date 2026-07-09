package project.demo;

import db.TheKnifeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReviewsController {

    @FXML private VBox containerRecensioni;
    @FXML private VBox emptyReviewsBox;

    private Navigator navigator;
    private TheKnifeDAO dao;
    private int idUtenteLoggato;

    @FXML
    public void initialize() {
        this.navigator = Navigator.getInstance();
        this.dao = navigator.getDao();
        this.idUtenteLoggato = navigator.getIdUtenteLoggato();

        if (idUtenteLoggato != -1) {
            caricaRecensioni();
        } else {
            navigator.navigateTo("login-view.fxml", "Accedi");
        }
    }

    private void caricaRecensioni() {
        containerRecensioni.getChildren().clear();
        boolean haRecensioni = false;

        try {
            ResultSet rs = dao.getRecensioniUtente(idUtenteLoggato);

            while (rs != null && rs.next()) {
                haRecensioni = true;
                String ristorante = rs.getString("nome_ristorante");
                String testo = rs.getString("testo");
                int stelle = rs.getInt("stelle");
                java.sql.Date data = rs.getDate("data_recensione");

                // Creazione dinamica del box recensione
                VBox card = new VBox(8);
                card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2);");

                HBox header = new HBox(12);
                header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label lblRisto = new Label(ristorante);
                lblRisto.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1B4332;");

                // Generazione dinamica delle stelle grafiche (es: ★★★★☆)
                StringBuilder stelleStr = new StringBuilder();
                for (int i = 0; i < 5; i++) {
                    stelleStr.append(i < stelle ? "★" : "☆");
                }
                Label lblStelle = new Label(stelleStr.toString());
                lblStelle.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFB703;");

                Label lblData = new Label(data != null ? data.toString() : "");
                lblData.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                header.getChildren().addAll(lblRisto, lblStelle, spacer, lblData);

                Label lblTesto = new Label(testo);
                lblTesto.setWrapText(true);
                lblTesto.setStyle("-fx-font-size: 14px; -fx-text-fill: #444444; -fx-font-style: italic;");

                card.getChildren().addAll(header, lblTesto);
                containerRecensioni.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Se non ci sono recensioni, mostra la schermata di notifica negativa
        if (haRecensioni) {
            containerRecensioni.setVisible(true);
            containerRecensioni.setManaged(true);
            emptyReviewsBox.setVisible(false);
            emptyReviewsBox.setManaged(false);
        } else {
            containerRecensioni.setVisible(false);
            containerRecensioni.setManaged(false);
            emptyReviewsBox.setVisible(true);
            emptyReviewsBox.setManaged(true);
        }
    }

    @FXML void handleCerca(ActionEvent event) { navigator.navigateTo("search-view-logged.fxml", "Cerca Ristoranti"); }
    @FXML void handlePreferiti(ActionEvent event) { navigator.navigateTo("favorites-view.fxml", "I Miei Preferiti"); }
    @FXML void handleProfilo(ActionEvent event) { navigator.navigateTo("customer-profile-view.fxml", "Il Mio Profilo"); }
    @FXML void handleLogout(ActionEvent event) {
        navigator.setIdUtenteLoggato(-1);
        navigator.navigateTo("login-view.fxml", "Accedi");
    }
    @FXML void handleVaiAllaRicerca(ActionEvent event) { handleCerca(event); }
    @FXML private void handleGoToHome(javafx.scene.input.MouseEvent event) { navigator.navigateToHome(); }
}
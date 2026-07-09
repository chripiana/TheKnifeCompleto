package project.demo;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;

public class OwnerDashboardController {

    @FXML private VBox containerRecensioni;

    private final ObservableList<Recensione> listaRecensioni = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        caricaDatiEsempio();

        Platform.runLater(() -> {
            if (containerRecensioni != null) {
                renderizzaRecensioni();
            }
        });
    }

    private void renderizzaRecensioni() {
        containerRecensioni.getChildren().clear();

        for (Recensione rec : listaRecensioni) {
            VBox card = new VBox(12);
            card.getStyleClass().add("rec-card");

            // Header recensione
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label avatar = new Label(rec.iniziali);
            avatar.getStyleClass().add("avatar-small");
            avatar.setAlignment(Pos.CENTER);

            VBox meta = new VBox(2);
            Label username = new Label(rec.username);
            username.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
            Label data = new Label(rec.data);
            data.setStyle("-fx-text-fill: #4A6B57; -fx-font-size: 11;");
            meta.getChildren().addAll(username, data);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label stelle = new Label(rec.stelle);
            stelle.setStyle("-fx-text-fill: #F6A623; -fx-font-size: 13;");

            Label badgeStatus = new Label(rec.risposta != null ? "✅ Risposto" : "⏳ Senza risposta");
            badgeStatus.getStyleClass().addAll("badge-status", rec.risposta != null ? "status-ok" : "status-no");

            header.getChildren().addAll(avatar, meta, spacer, stelle, badgeStatus);

            // Testo recensione
            Label testo = new Label(rec.testo);
            testo.setWrapText(true);
            testo.setStyle("-fx-font-size: 13; -fx-text-fill: #1B2E24; -fx-line-spacing: 1.4;");

            card.getChildren().addAll(header, testo);

            // Box Risposta se presente
            if (rec.risposta != null) {
                VBox boxRisposta = new VBox(4);
                boxRisposta.getStyleClass().add("risposta-box");

                Label titoloRisposta = new Label("👨‍🍳 Tua risposta — " + rec.dataRisposta);
                titoloRisposta.setStyle("-fx-font-weight: bold; -fx-font-size: 11; -fx-text-fill: #1B4332;");
                Label testoRisposta = new Label(rec.risposta);
                testoRisposta.setStyle("-fx-font-size: 12; -fx-text-fill: #4A6B57;");

                boxRisposta.getChildren().addAll(titoloRisposta, testoRisposta);
                card.getChildren().add(boxRisposta);
            }

            // Form operativo di risposta/modifica
            HBox formRisposta = new HBox(10);
            formRisposta.setAlignment(Pos.BOTTOM_LEFT);

            TextArea inputRisposta = new TextArea();
            inputRisposta.setPromptText(rec.risposta != null ? "Modifica la risposta..." : "Scrivi una risposta pubblica...");
            inputRisposta.setPrefHeight(50);
            HBox.setHgrow(inputRisposta, Priority.ALWAYS);
            inputRisposta.getStyleClass().add("home-text-field");

            Button btnInvia = new Button(rec.risposta != null ? "✏️ Modifica" : "📤 Rispondi");
            btnInvia.getStyleClass().add("btn-primary");
            btnInvia.setStyle("-fx-padding: 6 14; -fx-font-size: 12;");

            formRisposta.getChildren().addAll(inputRisposta, btnInvia);
            card.getChildren().add(formRisposta);

            containerRecensioni.getChildren().add(card);
        }
    }

    private void caricaDatiEsempio() {
        listaRecensioni.add(new Recensione("MC", "marco_cuoco", "14 aprile 2025", "★★★★★",
                "Esperienza fantastica! La pasta fatta in casa era eccellente, il servizio impeccabile. Tornerò sicuramente.", null, null));

        listaRecensioni.add(new Recensione("LB", "laura.bianchi", "2 marzo 2025", "★★★★☆",
                "Ottimo cibo, prezzi onesti per Milano. L'ambiente è accogliente e familiare.",
                "Grazie Laura! Per evitare l'attesa nel weekend, utilizza il sistema di prenotazione online.", "5 marzo 2025"));
    }

    private static class Recensione {
        String iniziali, username, data, stelle, testo, risposta, dataRisposta;
        public Recensione(String i, String u, String d, String s, String t, String r, String dr) {
            iniziali=i; username=u; data=d; stelle=s; testo=t; risposta=r; dataRisposta=dr;
        }
    }

    @FXML
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        project.demo.Navigator.getInstance().navigateToHome();
    }
}
package project.demo;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import db.TheKnifeDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchController {

    @FXML private TextField searchInlineField; // Barra della Navbar superiore
    @FXML private VBox containerRisultati;    // Contenitore verticale per le card
    @FXML private HBox containerPaginazione;  // Contenitore per i bottoni 1, 2, 3...

    // Pannello laterale sinistro allineato allo schema definitivo
    @FXML private TextField filterCitta;
    @FXML private TextField filterPrezzo;
    @FXML private ComboBox<String> filterStelle;
    @FXML private ComboBox<String> filterOrdine;

    private final List<RistoranteOggetto> tuttiIRistoranti = new ArrayList<>();
    private int paginaCorrente = 1;
    private static final int ELEMENTI_PER_PAGINA = 10;

    // Classe interna di supporto per i record del DB
    public static class RistoranteOggetto {
        public String id, nome, citta, nazione, indirizzo, cucina;
        public int prezzo, stelleIntere;
        public double lat, lon;
        public boolean delivery, prenotazioneOnline;

        public RistoranteOggetto(ResultSet rs) throws SQLException {
            this.id = rs.getString("id_ristorante");
            this.nome = rs.getString("nome");
            this.citta = rs.getString("citta");
            this.nazione = rs.getString("nazione");
            this.indirizzo = rs.getString("indirizzo");
            this.cucina = rs.getString("tipologia_cucina");
            this.prezzo = rs.getInt("prezzo_medio");
            this.lat = rs.getDouble("latitudine");
            this.lon = rs.getDouble("longitudine");
            this.delivery = rs.getBoolean("delivery");
            this.prenotazioneOnline = rs.getBoolean("prenotazione_online");

            double mediaStelle = 0;
            try { mediaStelle = rs.getDouble("media_stelle"); } catch (Exception e) {
                try { mediaStelle = rs.getInt("stellato"); } catch (Exception ex) {}
            }
            this.stelleIntere = (int) Math.round(mediaStelle);
        }
    }

    @FXML
    public void initialize() {
        if (filterStelle != null) {
            filterStelle.setItems(FXCollections.observableArrayList("Tutti", "1 ★ o più", "2 ★ o più", "3 ★ o più", "4 ★ o più", "5 ★"));
        }
        if (filterOrdine != null) {
            filterOrdine.setItems(FXCollections.observableArrayList("Predefinito", "Prezzo: Più economico", "Prezzo: Più caro", "Nome: A-Z"));
        }
    }

    @FXML
    private void goToHome(ActionEvent event) {
        Navigator.getInstance().navigateTo("home-view.fxml", "Home");
    }

    @FXML
    private void OnCercaPremuto() {
        if (searchInlineField == null) return;
        String query = searchInlineField.getText().trim();
        if (filterCitta != null) filterCitta.clear();
        if (filterPrezzo != null) filterPrezzo.clear();
        eseguiRicercaDinamica(null, query, null, null, null);
    }

    @FXML
    private void onAggiornaFiltriPremuto() {
        String citta = (filterCitta != null) ? filterCitta.getText().trim() : null;
        String prezzoText = (filterPrezzo != null) ? filterPrezzo.getText().trim() : "";
        String stelleString = (filterStelle != null) ? filterStelle.getValue() : null;

        String tipoCucina = (searchInlineField != null) ? searchInlineField.getText().trim() : null;
        if (tipoCucina != null && tipoCucina.isEmpty()) tipoCucina = null;

        Integer prezzoMaxParam = null;
        if (!prezzoText.isEmpty()) {
            try { prezzoMaxParam = Integer.parseInt(prezzoText); } catch (NumberFormatException e) {}
        }

        Double minStelleParam = null;
        if (stelleString != null && !stelleString.isEmpty() && !stelleString.contains("Tutti")) {
            String pulito = stelleString.replaceAll("[^0-9]", "");
            if (!pulito.isEmpty()) minStelleParam = Double.parseDouble(pulito);
        }

        eseguiRicercaDinamica(citta, tipoCucina, null, prezzoMaxParam, minStelleParam);
    }

    @FXML
    private void onResetFiltriPremuto() {
        if (filterCitta != null) filterCitta.clear();
        if (filterPrezzo != null) filterPrezzo.clear();
        if (filterStelle != null) filterStelle.setValue("Tutti");
        if (filterOrdine != null) filterOrdine.setValue("Predefinito");
        if (searchInlineField != null) searchInlineField.clear();
        eseguiRicercaDinamica(null, null, null, null, null);
    }

    public void inizializzaRicercaGlobale(String testoCercato) {
        if (searchInlineField != null) searchInlineField.setText(testoCercato);
        eseguiRicercaDinamica(null, testoCercato, null, null, null);
    }

    public void inizializzaRicercaAvanzata(String citta, String prezzoMax, String stelle, String ordine) {
        if (filterCitta != null) filterCitta.setText(citta);
        if (filterPrezzo != null) filterPrezzo.setText(prezzoMax);
        if (filterStelle != null) filterStelle.setValue(stelle);
        if (filterOrdine != null) filterOrdine.setValue(ordine);

        Integer prezzoMaxParam = null;
        if (prezzoMax != null && !prezzoMax.isEmpty()) {
            try { prezzoMaxParam = Integer.parseInt(prezzoMax); } catch(Exception e){}
        }

        Double minStelleParam = null;
        if (stelle != null && !stelle.isEmpty() && !stelle.contains("Tutti")) {
            String pulito = stelle.replaceAll("[^0-9]", "");
            if (!pulito.isEmpty()) minStelleParam = Double.parseDouble(pulito);
        }

        eseguiRicercaDinamica(citta, null, null, prezzoMaxParam, minStelleParam);
    }

    private void eseguiRicercaDinamica(String citta, String tipoCucina, Integer prezzoMin, Integer prezzoMax, Double minStelle) {
        tuttiIRistoranti.clear();
        paginaCorrente = 1;
        try {
            TheKnifeDAO dao = Navigator.getInstance().getDao();
            ResultSet rs = dao.cercaRistorante(cittaParam(citta), cucinaParam(tipoCucina), prezzoMin, prezzoMax, null, null, minStelle);
            while (rs.next()) {
                tuttiIRistoranti.add(new RistoranteOggetto(rs));
            }
            rs.close();
            aggiornaInterfacciaVisiva();
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Errore in ricerca: " + e.getMessage());
        }
    }

    private String cittaParam(String c) { return (c == null || c.isEmpty()) ? null : c; }
    private String cucinaParam(String c) { return (c == null || c.isEmpty()) ? null : c; }

    private void aggiornaInterfacciaVisiva() {
        if (containerRisultati == null) return;
        containerRisultati.getChildren().clear();

        if (tuttiIRistoranti.isEmpty()) {
            VBox boxVuoto = new VBox();
            boxVuoto.setStyle("-fx-padding: 40; -fx-alignment: center;");
            Text msg = new Text("❌ Nessun ristorante trovato con i criteri inseriti.");
            msg.setStyle("-fx-fill: #94A3B8; -fx-font-size: 15; -fx-font-style: italic;");
            boxVuoto.getChildren().add(msg);
            containerRisultati.getChildren().add(boxVuoto);
            if (containerPaginazione != null) containerPaginazione.getChildren().clear();
            return;
        }

        int indiceInizio = (paginaCorrente - 1) * ELEMENTI_PER_PAGINA;
        int indiceFine = Math.min(indiceInizio + ELEMENTI_PER_PAGINA, tuttiIRistoranti.size());

        for (int i = indiceInizio; i < indiceFine; i++) {
            RistoranteOggetto r = tuttiIRistoranti.get(i);

            // ==========================================
            // CREAZIONE CARD PRINCIPALE (HBox)
            // ==========================================
            HBox card = new HBox(18);
            card.getStyleClass().add("restaurant-card");
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(18));

            // --- 1. THUMBNAIL (Icona e Sfondo basati sulla cucina) ---
            VBox thumb = new VBox();
            thumb.setAlignment(Pos.CENTER);
            thumb.setMinSize(88, 88);
            thumb.setMaxSize(88, 88);

            String tipoCucina = r.cucina != null ? r.cucina : "Internazionale";
            String classeThumb = getThumbClass(tipoCucina);
            String iconaThumb = getThumbIcon(tipoCucina);

            thumb.getStyleClass().addAll("restaurant-card-thumb", classeThumb);
            Label lblIcona = new Label(iconaThumb);
            lblIcona.getStyleClass().add("restaurant-card-thumb-icon");
            thumb.getChildren().add(lblIcona);

            // --- 2. CORPO CENTRALE DEI DETTAGLI (VBox) ---
            VBox dettagliBox = new VBox(8);
            HBox.setHgrow(dettagliBox, Priority.ALWAYS);

            // Riga 1: Nome, Badge Consigliato (se >= 4.5 stelle), Prezzo
            HBox row1 = new HBox(10);
            row1.setAlignment(Pos.CENTER_LEFT);
            Label lblNome = new Label(r.nome);
            lblNome.getStyleClass().add("restaurant-card-name");

            row1.getChildren().add(lblNome);

            if (r.stelleIntere >= 4) {
                Label badgeConsigliato = new Label("★ Consigliato");
                badgeConsigliato.getStyleClass().add("badge-featured");
                row1.getChildren().add(badgeConsigliato);
            }

            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);
            Label lblPrezzo = new Label("💰 Spesa Media: " + r.prezzo + "€");
            lblPrezzo.getStyleClass().add("restaurant-card-price-badge");
            row1.getChildren().addAll(spacer1, lblPrezzo);

            // Riga 2: Gestore, Cucina
            HBox row2 = new HBox(8);
            row2.setAlignment(Pos.CENTER_LEFT);
            Label lblOwner = new Label("👤 TheKnife Partner"); // Si può agganciare al DB
            lblOwner.getStyleClass().add("restaurant-card-owner");
            Label dot1 = new Label("•");
            dot1.getStyleClass().add("restaurant-card-dot");
            Label lblCucinaChip = new Label("Cucina " + tipoCucina);
            lblCucinaChip.getStyleClass().add("restaurant-card-cuisine-chip");
            row2.getChildren().addAll(lblOwner, dot1, lblCucinaChip);

            // Riga 3: Posizione
            HBox row3 = new HBox(8);
            row3.setAlignment(Pos.CENTER_LEFT);
            Label lblMeta = new Label("📍 " + r.citta + " (" + r.nazione + ")");
            lblMeta.getStyleClass().add("restaurant-card-meta");
            row3.getChildren().addAll(lblMeta);

            // Riga 4: Stelle, Recensioni, Tags
            HBox row4 = new HBox(4);
            row4.setAlignment(Pos.CENTER_LEFT);
            row4.getStyleClass().add("rating-stars-row");

            // Generazione dinamica delle stelle
            for (int j = 1; j <= 5; j++) {
                Label star = new Label("★");
                star.getStyleClass().add(j <= r.stelleIntere ? "star-filled" : "star-empty");
                row4.getChildren().add(star);
            }

            Label lblAvg = new Label(String.valueOf((double)r.stelleIntere)); // Media
            lblAvg.getStyleClass().add(r.stelleIntere > 0 ? "rating-avg-num" : "rating-avg-num-muted");
            row4.getChildren().add(lblAvg);

            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            row4.getChildren().add(spacer2);

            if (r.delivery) {
                Label tagDelivery = new Label("🛵 Delivery");
                tagDelivery.getStyleClass().add("restaurant-card-tag");
                row4.getChildren().add(tagDelivery);
            }
            if (r.prenotazioneOnline) {
                Label tagPrenotabile = new Label("📅 Prenotabile");
                tagPrenotabile.getStyleClass().add("restaurant-card-tag");
                row4.getChildren().add(tagPrenotabile);
            }

            dettagliBox.getChildren().addAll(row1, row2, row3, row4);

            // --- 3. BOTTONI AZIONE LATERALI ---
            VBox azioniBox = new VBox(8);
            azioniBox.setAlignment(Pos.CENTER);

            Button btnFav = new Button("♡");
            btnFav.getStyleClass().add("btn-card-fav");

            Button btnDettagli = new Button("Vedi dettagli");
            btnDettagli.getStyleClass().add("btn-card-cta");

            // Navigazione ai dettagli
            btnDettagli.setOnAction(e -> navigaADettagli(r));
            // L'intera card è cliccabile
            card.setOnMouseClicked(e -> navigaADettagli(r));
            card.setStyle("-fx-cursor: hand;");

            azioniBox.getChildren().addAll(btnFav, btnDettagli);

            // Assemblaggio finale della card
            card.getChildren().addAll(thumb, dettagliBox, azioniBox);

            // Effetto Hover dinamico (aggiungi una classe hover nel tuo CSS se preferisci)
            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F8FAFC; -fx-cursor: hand;"));
            card.setOnMouseExited(e -> card.setStyle(""));

            containerRisultati.getChildren().add(card);
        }
        disegnaBarraPaginazione();
    }

// ==========================================
// METODI DI SUPPORTO
// ==========================================

    // 1. Gestisce il click sulla card del ristorante rimandando al Navigator modificato
    private void navigaADettagli(RistoranteOggetto r) {
        System.out.println("[NAVIGAZIONE] Richiesta apertura dettagli per: " + r.nome);
        Navigator.getInstance().navigateToRestaurantDetails(r);
    }

    // Mappa la stringa del DB a una classe CSS
    private String getThumbClass(String cucina) {
        String lower = cucina.toLowerCase();
        if (lower.contains("italian")) return "thumb-italiana";
        if (lower.contains("giapponese") || lower.contains("sushi") || lower.contains("asian")) return "thumb-giapponese";
        if (lower.contains("veg")) return "thumb-vegetariana";
        return "thumb-default";
    }

    // Mappa la stringa del DB a un'emoji
    private String getThumbIcon(String cucina) {
        String lower = cucina.toLowerCase();
        if (lower.contains("italian")) return "🍝";
        if (lower.contains("giapponese") || lower.contains("sushi")) return "🍣";
        if (lower.contains("asian")) return "🍜";
        if (lower.contains("veg")) return "🥗";
        if (lower.contains("meat") || lower.contains("steak")) return "🥩";
        if (lower.contains("pizza")) return "🍕";
        return "🍽️";
    }

    private void disegnaBarraPaginazione() {
        if (containerPaginazione == null) return;
        containerPaginazione.getChildren().clear();

        int totalePagine = (int) Math.ceil((double) tuttiIRistoranti.size() / ELEMENTI_PER_PAGINA);
        if (totalePagine <= 1) return;

        int startPage = Math.max(1, paginaCorrente - 2);
        int endPage = Math.min(totalePagine, paginaCorrente + 2);

        if (paginaCorrente > 1) {
            Button btnPrecedente = new Button("←");
            btnPrecedente.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #1B4332; -fx-cursor: hand;");
            btnPrecedente.setOnAction(e -> { paginaCorrente--; aggiornaInterfacciaVisiva(); });
            containerPaginazione.getChildren().add(btnPrecedente);
        }

        for (int p = startPage; p <= endPage; p++) {
            final int numeroPagina = p;
            Button btnPagina = new Button(String.valueOf(p));
            if (p == paginaCorrente) {
                btnPagina.setStyle("-fx-background-color: #2D6A4F; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                btnPagina.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-text-fill: #64748B; -fx-cursor: hand;");
            }
            btnPagina.setOnAction(e -> { paginaCorrente = numeroPagina; aggiornaInterfacciaVisiva(); });
            containerPaginazione.getChildren().add(btnPagina);
        }

        if (paginaCorrente < totalePagine) {
            Button btnSuccessiva = new Button("→");
            btnSuccessiva.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #1B4332; -fx-cursor: hand;");
            btnSuccessiva.setOnAction(e -> { paginaCorrente++; aggiornaInterfacciaVisiva(); });
            containerPaginazione.getChildren().add(btnSuccessiva);
        }
    }

    @FXML
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        project.demo.Navigator.getInstance().navigateToHome();
    }
}
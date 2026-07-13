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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchController {

    @FXML
    private TextField searchInlineField; // Barra della Navbar superiore
    @FXML
    private VBox containerRisultati; // Contenitore verticale per le card
    @FXML
    private HBox containerPaginazione; // Contenitore per i bottoni 1, 2, 3...

    // Pannello laterale sinistro allineato allo schema definitivo
    @FXML
    private TextField filterCitta;
    @FXML
    private TextField filterPrezzo;
    @FXML
    private ComboBox<String> filterStelle;
    @FXML
    private ComboBox<String> filterOrdine;

    private final List<RistoranteOggetto> tuttiIRistoranti = new ArrayList<>();
    private int paginaCorrente = 1;
    private static final int ELEMENTI_PER_PAGINA = 10;

    // Classe interna di supporto per i record del DB
    // Classe interna di supporto per i record del DB
    public static class RistoranteOggetto {
        public String id, nome, citta, nazione, indirizzo, cucina;
        public int prezzo, stelleIntere, numRecensioni;
        public double mediaStelleReale;
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
            try {
                mediaStelle = rs.getDouble("media_stelle");
            } catch (Exception e) {
                try {
                    mediaStelle = rs.getInt("stellato");
                } catch (Exception ex) {
                }
            }
            this.mediaStelleReale = mediaStelle;
            this.stelleIntere = (int) Math.round(mediaStelle);

            int nRec = 0;
            try {
                nRec = rs.getInt("num_recensioni");
            } catch (Exception e) {
                /* colonna non presente in questa query */ }
            this.numRecensioni = nRec;
        }
    }

    @FXML
    public void initialize() {
        if (filterStelle != null) {
            filterStelle.setItems(FXCollections.observableArrayList("Tutti", "1 ★ o più", "2 ★ o più", "3 ★ o più",
                    "4 ★ o più", "5 ★"));
        }
        if (filterOrdine != null) {
            filterOrdine.setItems(FXCollections.observableArrayList("Predefinito", "Prezzo: Più economico",
                    "Prezzo: Più caro", "Nome: A-Z"));
        }
    }

    @FXML
    private void goToHome(ActionEvent event) {
        Navigator.getInstance().navigateToHomeIntelligent();
    }

    @FXML
    private void OnCercaPremuto() {
        if (searchInlineField == null)
            return;
        String query = searchInlineField.getText().trim();
        if (filterCitta != null)
            filterCitta.clear();
        if (filterPrezzo != null)
            filterPrezzo.clear();
        String ordine = (filterOrdine != null) ? filterOrdine.getValue() : null;
        eseguiRicercaDinamica(null, query, null, null, null, ordine);
    }

    @FXML
    private void onAggiornaFiltriPremuto() {
        String citta = (filterCitta != null) ? filterCitta.getText().trim() : null;
        String prezzoText = (filterPrezzo != null) ? filterPrezzo.getText().trim() : "";
        String stelleString = (filterStelle != null) ? filterStelle.getValue() : null;
        String ordine = (filterOrdine != null) ? filterOrdine.getValue() : null;

        String tipoCucina = (searchInlineField != null) ? searchInlineField.getText().trim() : null;
        if (tipoCucina != null && tipoCucina.isEmpty())
            tipoCucina = null;

        Integer prezzoMaxParam = null;
        if (!prezzoText.isEmpty()) {
            try {
                prezzoMaxParam = Integer.parseInt(prezzoText);
            } catch (NumberFormatException e) {
            }
        }

        Double minStelleParam = null;
        if (stelleString != null && !stelleString.isEmpty() && !stelleString.contains("Tutti")) {
            String pulito = stelleString.replaceAll("[^0-9]", "");
            if (!pulito.isEmpty())
                minStelleParam = Double.parseDouble(pulito);
        }

        eseguiRicercaDinamica(citta, tipoCucina, null, prezzoMaxParam, minStelleParam, ordine);
    }

    @FXML
    private void onResetFiltriPremuto() {
        if (filterCitta != null)
            filterCitta.clear();
        if (filterPrezzo != null)
            filterPrezzo.clear();
        if (filterStelle != null)
            filterStelle.setValue("Tutti");
        if (filterOrdine != null)
            filterOrdine.setValue("Predefinito");
        if (searchInlineField != null)
            searchInlineField.clear();
        eseguiRicercaDinamica(null, null, null, null, null, null);
    }

    public void inizializzaRicercaGlobale(String testoCercato) {
        if (searchInlineField != null)
            searchInlineField.setText(testoCercato);
        eseguiRicercaDinamica(null, testoCercato, null, null, null, null);
    }

    public void inizializzaRicercaAvanzata(String citta, String prezzoMax, String stelle, String ordine) {
        if (filterCitta != null)
            filterCitta.setText(citta);
        if (filterPrezzo != null)
            filterPrezzo.setText(prezzoMax);
        if (filterStelle != null)
            filterStelle.setValue(stelle);
        if (filterOrdine != null)
            filterOrdine.setValue(ordine);

        Integer prezzoMaxParam = null;
        if (prezzoMax != null && !prezzoMax.isEmpty()) {
            try {
                prezzoMaxParam = Integer.parseInt(prezzoMax);
            } catch (Exception e) {
            }
        }

        Double minStelleParam = null;
        if (stelle != null && !stelle.isEmpty() && !stelle.contains("Tutti")) {
            String pulito = stelle.replaceAll("[^0-9]", "");
            if (!pulito.isEmpty())
                minStelleParam = Double.parseDouble(pulito);
        }

        eseguiRicercaDinamica(citta, null, null, prezzoMaxParam, minStelleParam, ordine);
    }

    private void eseguiRicercaDinamica(String citta, String tipoCucina, Integer prezzoMin, Integer prezzoMax,
            Double minStelle, String ordine) {
        tuttiIRistoranti.clear();
        paginaCorrente = 1;
        try {
            TheKnifeDAO dao = Navigator.getInstance().getDao();
            ResultSet rs = dao.cercaRistorante(cittaParam(citta), cucinaParam(tipoCucina), prezzoMin, prezzoMax, null,
                    null, minStelle);
            while (rs.next()) {
                tuttiIRistoranti.add(new RistoranteOggetto(rs));
            }
            rs.close();
            applicaOrdinamento(ordine);
            aggiornaInterfacciaVisiva();
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Errore in ricerca: " + e.getMessage());
        }
    }

    /**
     * FIX: applica realmente l'ordinamento scelto dall'utente (prima veniva
     * solo mostrato nella ComboBox ma mai usato per riordinare i risultati).
     * Confronto con .contains(...) perché home-view.fxml e search-view.fxml
     * usano etichette leggermente diverse per le stesse opzioni.
     */
    private void applicaOrdinamento(String ordine) {
        if (ordine == null || ordine.isEmpty() || ordine.contains("Predefinito"))
            return;

        if (ordine.contains("economico")) {
            tuttiIRistoranti.sort((a, b) -> Integer.compare(a.prezzo, b.prezzo));
        } else if (ordine.contains("caro") || ordine.contains("esclusivo")) {
            tuttiIRistoranti.sort((a, b) -> Integer.compare(b.prezzo, a.prezzo));
        } else if (ordine.contains("A-Z") || ordine.contains("Alfabetico")) {
            tuttiIRistoranti.sort((a, b) -> a.nome.compareToIgnoreCase(b.nome));
        } else if (ordine.contains("Stelle") || ordine.contains("Valutazione")) {
            tuttiIRistoranti.sort((a, b) -> Integer.compare(b.stelleIntere, a.stelleIntere));
        }
        // altre opzioni non riconosciute -> lascia l'ordine di default (media_stelle
        // DESC dal DB)
    }

    private String cittaParam(String c) {
        return (c == null || c.isEmpty()) ? null : c;
    }

    private String cucinaParam(String c) {
        return (c == null || c.isEmpty()) ? null : c;
    }

    private void handleTogglePreferito(RistoranteOggetto r, Button btnFav) {
        try {
            TheKnifeDAO dao = Navigator.getInstance().getDao();

            // NOTA: Sostituisci questa riga con il tuo metodo reale per ottenere l'ID
            // dell'utente loggato
            // Ad esempio: Navigator.getInstance().getUtenteLoggato().getId() o simile.
            int idUtenteLoggato = Navigator.getInstance().getIdUtenteLoggato();

            if (dao.isPreferito(idUtenteLoggato, r.id)) {
                // Se è già preferito, lo rimuoviamo
                dao.rimuoviPreferito(idUtenteLoggato, r.id);
                btnFav.setText("♡");
                System.out.println("[PREFERITI] Rimosso: " + r.nome);
            } else {
                // Se non è preferito, lo aggiungiamo
                dao.aggiungiPreferito(idUtenteLoggato, r.id);
                btnFav.setText("♥");
                System.out.println("[PREFERITI] Aggiunto: " + r.nome);
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Errore durante il toggle del preferito: " + e.getMessage());

            // Opzionale: Mostra un alert di errore se l'utente non è loggato
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("Impossibile salvare i preferiti");
            alert.setContentText("Assicurati di aver effettuato il login.");
            alert.showAndWait();
        }
    }

    private void aggiornaInterfacciaVisiva() {
        if (containerRisultati == null)
            return;
        containerRisultati.getChildren().clear();

        if (tuttiIRistoranti.isEmpty()) {
            VBox boxVuoto = new VBox();
            boxVuoto.setStyle("-fx-padding: 40; -fx-alignment: center;");
            Text msg = new Text("❌ Nessun ristorante trovato con i criteri inseriti.");
            msg.setStyle("-fx-fill: #94A3B8; -fx-font-size: 15; -fx-font-style: italic;");
            boxVuoto.getChildren().add(msg);
            containerRisultati.getChildren().add(boxVuoto);
            if (containerPaginazione != null)
                containerPaginazione.getChildren().clear();
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

            Label lblAvg = new Label(r.numRecensioni > 0 ? String.format("%.1f", r.mediaStelleReale) : "Nuovo");
            // Media
            lblAvg.getStyleClass().add(r.stelleIntere > 0 ? "rating-avg-num" : "rating-avg-num-muted");
            row4.getChildren().add(lblAvg);

            Label lblNumRec = new Label(r.numRecensioni > 0 ? "(" + r.numRecensioni + ")" : "");
            lblNumRec.getStyleClass().add("rating-count");
            row4.getChildren().add(lblNumRec);

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
            // --- 3. BOTTONI AZIONE LATERALI ---
            VBox azioniBox = new VBox(8);
            azioniBox.setAlignment(Pos.CENTER);

            Button btnFav = new Button("♡"); // Stato di default vuoto
            btnFav.getStyleClass().add("btn-card-fav");

            // 1. IMPOSTA LO STATO INIZIALE (Se l'utente è loggato, mostra il cuore pieno se
            // è già tra i preferiti)
            try {
                TheKnifeDAO dao = Navigator.getInstance().getDao();
                int idUtenteLoggato = Navigator.getInstance().getIdUtenteLoggato(); // Adatta alla tua sessione

                if (dao.isPreferito(idUtenteLoggato, r.id)) {
                    btnFav.setText("♥"); // Cuore pieno se è già nei preferiti
                }
            } catch (Exception e) {
                // Gestione silenziosa (es. se l'utente non è loggato, il cuore resta vuoto "♡")
            }

            // 2. COLLEGA IL METODO AL CLICK DEL BOTTONE
            btnFav.setOnAction(e -> {
                // Consuma l'evento in modo che il click sul bottone non attivi erroneamente il
                // click sull'intera card
                e.consume();
                handleTogglePreferito(r, btnFav);
            });

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

    // 1. Gestisce il click sulla card del ristorante rimandando al Navigator
    // modificato
    private void navigaADettagli(RistoranteOggetto r) {
        System.out.println("[NAVIGAZIONE] Richiesta apertura dettagli per: " + r.nome);
        Navigator.getInstance().navigateToRestaurantDetails(r);
    }

    // Mappa capillarmente la stringa del DB a una classe CSS con colori dedicati
    private String getThumbClass(String cucina) {
        if (cucina == null)
            return "thumb-altro";
        String lower = cucina.toLowerCase();

        // 1. Italiana & Pizza
        if (lower.contains("italian") || lower.contains("pizza") || lower.contains("primi") || lower.contains("pasta"))
            return "thumb-italiana";

        // 2. Asiatica & Etnica Orientale
        if (lower.contains("giapponese") || lower.contains("sushi") || lower.contains("asian")
                || lower.contains("cinese") || lower.contains("coreano"))
            return "thumb-giapponese";
        if (lower.contains("thai") || lower.contains("poke") || lower.contains("hawaiian")
                || lower.contains("vietnamita") || lower.contains("ramen"))
            return "thumb-poke";
        if (lower.contains("indian") || lower.contains("curry"))
            return "thumb-indiana";

        // 3. Green & Healthy
        if (lower.contains("veg") || lower.contains("insalata") || lower.contains("salad")
                || lower.contains("salutista"))
            return "thumb-vegetariana";
        if (lower.contains("mediterranea") || lower.contains("mediterranean"))
            return "thumb-mediterranea";

        // 4. Europee Specifiche
        if (lower.contains("greco") || lower.contains("greca") || lower.contains("greek"))
            return "thumb-greca";
        if (lower.contains("spagnol") || lower.contains("tapas") || lower.contains("paella"))
            return "thumb-spagnola";
        if (lower.contains("francese") || lower.contains("french"))
            return "thumb-francese";
        if (lower.contains("kebab") || lower.contains("turco") || lower.contains("arabo")
                || lower.contains("medio orientale") || lower.contains("libanese"))
            return "thumb-arabo";

        // 5. Ciccia, Pesce & Grill
        if (lower.contains("meat") || lower.contains("steak") || lower.contains("carne") || lower.contains("bbq")
                || lower.contains("grill"))
            return "thumb-carne";
        if (lower.contains("pesce") || lower.contains("seafood") || lower.contains("fish") || lower.contains("mare")
                || lower.contains("crostacei"))
            return "thumb-pesce";
        if (lower.contains("messicano") || lower.contains("mexican") || lower.contains("taco")
                || lower.contains("piccante") || lower.contains("burrito"))
            return "thumb-messicana";

        // 6. Fast Food & Paninoteche
        if (lower.contains("burger") || lower.contains("fast food") || lower.contains("americano")
                || lower.contains("american") || lower.contains("patatine") || lower.contains("hot dog"))
            return "thumb-fastfood";
        if (lower.contains("street") || lower.contains("panin") || lower.contains("piadina") || lower.contains("toast"))
            return "thumb-streetfood";

        // 7. Caffetteria, Dolci & Bar
        if (lower.contains("dolci") || lower.contains("dessert") || lower.contains("pasticceria")
                || lower.contains("gelato") || lower.contains("crepe"))
            return "thumb-dolci";
        if (lower.contains("brunch") || lower.contains("colazione") || lower.contains("caff")
                || lower.contains("bakery"))
            return "thumb-brunch";
        if (lower.contains("pub") || lower.contains("birra") || lower.contains("cocktail") || lower.contains("bar")
                || lower.contains("enoteca") || lower.contains("vino"))
            return "thumb-pub";

        return "thumb-altro";
    }

    // Associa un'emoji ad hoc ad ogni sfumatura di cucina, minimizzando i simboli
    // generici
    private String getThumbIcon(String cucina) {
        if (cucina == null)
            return "👨‍🍳";
        String lower = cucina.toLowerCase();

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

        return "👨‍🍳"; // Estremo ripiego: il cuoco al posto del piatto piatto 🍽️
    }

    private void disegnaBarraPaginazione() {
        if (containerPaginazione == null)
            return;
        containerPaginazione.getChildren().clear();

        int totalePagine = (int) Math.ceil((double) tuttiIRistoranti.size() / ELEMENTI_PER_PAGINA);
        if (totalePagine <= 1)
            return;

        int startPage = Math.max(1, paginaCorrente - 2);
        int endPage = Math.min(totalePagine, paginaCorrente + 2);

        if (paginaCorrente > 1) {
            Button btnPrecedente = new Button("←");
            btnPrecedente.getStyleClass().add("pagination-button-inactive");
            btnPrecedente.setOnAction(e -> {
                paginaCorrente--;
                aggiornaInterfacciaVisiva();
            });
            containerPaginazione.getChildren().add(btnPrecedente);
        }

        for (int p = startPage; p <= endPage; p++) {
            final int numeroPagina = p;
            Button btnPagina = new Button(String.valueOf(p));
            if (p == paginaCorrente) {
                btnPagina.getStyleClass().add("pagination-button-active");
            } else {
                btnPagina.getStyleClass().add("pagination-button-inactive");
            }
            btnPagina.setOnAction(e -> {
                paginaCorrente = numeroPagina;
                aggiornaInterfacciaVisiva();
            });
            containerPaginazione.getChildren().add(btnPagina);
        }

        if (paginaCorrente < totalePagine) {
            Button btnSuccessiva = new Button("→");
            btnSuccessiva.getStyleClass().add("pagination-button-inactive");
            btnSuccessiva.setOnAction(e -> {
                paginaCorrente++;
                aggiornaInterfacciaVisiva();
            });
            containerPaginazione.getChildren().add(btnSuccessiva);
        }
    }

    @FXML
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        project.demo.Navigator.getInstance().navigateToHomeIntelligent();
    }

    @FXML
    private void handleGoToProfile(javafx.scene.input.MouseEvent event) {
        project.demo.Navigator.getInstance().navigateToProfile();
    }

}
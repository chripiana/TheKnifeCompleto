package project.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.event.ActionEvent;

public class RestaurantDetailsController {

    // --- ELEMENTI NAVBAR & BREADCRUMB ---
    @FXML private Label lblBreadcrumb;

    // --- HERO PRINCIPALE ---
    @FXML private Label lblNomeRistorante;
    @FXML private Label lblCittaHero;
    @FXML private Label lblCucinaHero;
    @FXML private Label lblPrezzoHero;
    @FXML private Label lblStelleMediaHero;
    @FXML private Label lblEmojiCucina;

    // --- INFORMAZIONI GENERALI ---
    @FXML private Label lblNazione;
    @FXML private Label lblCittaVal;
    @FXML private Label lblIndirizzo;
    @FXML private Label lblTipologiaCucinaBox;
    @FXML private Label lblPrezzoMedioBox;

    // --- POSIZIONE & MAPPA ---
    @FXML private Label lblMappaTestoIndirizzo;
    @FXML private Label lblLatitudine;
    @FXML private Label lblLongitudine;

    // --- RIEPILOGO LATERALE (DESTRA) ---
    @FXML private Label lblRiepilogoStelle;
    @FXML private Label lblRiepilogoPrezzo;
    @FXML private Label lblRiepilogoCucina;

    // --- AZIONI ---
    @FXML private TextArea recensioneTextArea;
    @FXML private Button btnPrenotaOra;

    /**
     * Popola dinamicamente tutti i campi della vista con i dati del ristorante selezionato
     */
    public void caricaDatiRistorante(SearchController.RistoranteOggetto r) {
        if (r == null) return;

        // 1. Navbar & Breadcrumb
        if (lblBreadcrumb != null) {
            lblBreadcrumb.setText("Home  ›  " + r.citta + "  ›  " + r.nome);
        }

        // 2. Hero Principale
        if (lblNomeRistorante != null) lblNomeRistorante.setText(r.nome);
        if (lblCittaHero != null) lblCittaHero.setText("📍 " + r.indirizzo + ", " + r.citta + ", " + r.nazione);
        if (lblCucinaHero != null) lblCucinaHero.setText(getEmojiCucina(r.cucina) + " Cucina " + r.cucina);
        if (lblPrezzoHero != null) lblPrezzoHero.setText("€" .repeat(Math.max(1, Math.min(3, r.prezzo / 25))) + "  ·  " + r.prezzo + "€ p.p.");
        if (lblEmojiCucina != null) lblEmojiCucina.setText(getEmojiCucina(r.cucina));

        // Generazione visiva delle stelle (es. ★★★★☆)
        String stelleGrafiche = "★".repeat(r.stelleIntere) + "☆".repeat(5 - r.stelleIntere);
        if (lblStelleMediaHero != null) {
            lblStelleMediaHero.setText(stelleGrafiche + "  " + String.format("%.1f", (double) r.stelleIntere));
        }

        // 3. Box Informazioni Generali
        if (lblNazione != null) lblNazione.setText(r.nazione);
        if (lblCittaVal != null) lblCittaVal.setText(r.citta);
        if (lblIndirizzo != null) lblIndirizzo.setText(r.indirizzo);
        if (lblTipologiaCucinaBox != null) lblTipologiaCucinaBox.setText(r.cucina);
        if (lblPrezzoMedioBox != null) lblPrezzoMedioBox.setText(r.prezzo + " € a persona");

        // 4. Box Mappa e Coordinate
        if (lblMappaTestoIndirizzo != null) lblMappaTestoIndirizzo.setText(r.indirizzo + " — " + r.citta + ", " + r.nazione);
        if (lblLatitudine != null) lblLatitudine.setText("Lat: " + (r.lat != 0.0 ? r.lat + "°" : "45.4642°"));
        if (lblLongitudine != null) lblLongitudine.setText("Lng: " + (r.lon != 0.0 ? r.lon + "°" : "9.1900°"));

        // 5. Box Riepilogo Laterale Destro
        if (lblRiepilogoStelle != null) lblRiepilogoStelle.setText(String.format("%.1f", (double) r.stelleIntere) + " / 5");
        if (lblRiepilogoPrezzo != null) lblRiepilogoPrezzo.setText(r.prezzo + " €");
        if (lblRiepilogoCucina != null) lblRiepilogoCucina.setText(r.cucina);

        // 6. Gestione pulsante di prenotazione (Abilitato solo se il ristorante supporta la prenotazione online)
        if (btnPrenotaOra != null) {
            if (r.prenotazioneOnline) {
                btnPrenotaOra.setDisable(false);
                btnPrenotaOra.setText("Prenota Ora");
            } else {
                btnPrenotaOra.setDisable(true);
                btnPrenotaOra.setText("Prenotazione non disponibile");
            }
        }

        System.out.println("[DINAMICO] Tutti i dettagli caricati con successo per: " + r.nome);
    }

    /**
     * Helper per associare un'emoji alla tipologia di cucina
     */
    private String getEmojiCucina(String cucina) {
        if (cucina == null) return "🍽️";
        String lower = cucina.toLowerCase();
        if (lower.contains("ital")) return "🍝";
        if (lower.contains("giapp") || lower.contains("sush")) return "🍣";
        if (lower.contains("asia") || lower.contains("cin")) return "¼";
        if (lower.contains("veg")) return "🥗";
        if (lower.contains("carne") || lower.contains("steak")) return "🥩";
        if (lower.contains("pizz")) return "🍕";
        return "🍽️";
    }

    @FXML
    void handleTornaRisultati(ActionEvent event) {
        // Chiama il metodo intelligente che preserva la ricerca ed evita il reset della pagina
        Navigator.getInstance().backToSearchResults();
    }

    @FXML
    void handlePubblicaRecensione(ActionEvent event) {
        if (recensioneTextArea != null) {
            String testoRecensione = recensioneTextArea.getText();
            if (testoRecensione.trim().isEmpty()) {
                System.out.println("Errore: impossibile pubblicare una recensione vuota.");
                return;
            }
            System.out.println("Pubblicazione nuova recensione: " + testoRecensione);
            recensioneTextArea.clear();
        }
    }

    @FXML
    void handlePrenotaOra(ActionEvent event) {
        System.out.println("Apertura finestra di prenotazione...");
    }

    @FXML
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        project.demo.Navigator.getInstance().navigateToHome();
    }
}
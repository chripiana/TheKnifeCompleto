package project.demo;

import db.TheKnifeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RestaurantDetailsController {

    // Elementi dell'HBox Hero e informazioni principali
    @FXML
    private Label lblBreadcrumb;
    @FXML
    private Label lblNomeRistorante;
    @FXML
    private Label lblCitta;
    @FXML
    private Label lblCucina;
    @FXML
    private Label lblPrezzo;
    @FXML
    private HBox hboxHero;
    @FXML
    private Label lblAvatarRistorante;

    // Riferimenti alla navbar (resta sempre verde, come tutte le altre pagine:
    // nessuna colorazione dinamica)
    @FXML
    private HBox hboxNavbar;
    @FXML
    private Label lblLogoThe;
    @FXML
    private Label lblLogoKnife;
    @FXML
    private Button btnTornaRisultati;

    // Allineato all'fx:id dell'FXML per evitare disallineamenti di mancato
    // aggiornamento delle recensioni
    @FXML
    private Label lblStelleMedia;
    @FXML
    private Label lblNumRecensioni;

    // Card "VALUTAZIONI": numero grande, stelle e distribuzione 1★-5★ (presenti in
    // entrambe le viste,
    // nella vista loggata questi stessi fx:id sostituiscono anche
    // lblStelleMedia/lblNumRecensioni nell'hero)
    @FXML
    private Label lblRatingBigNumber;
    @FXML
    private Label lblStarsDisplayBig;
    @FXML
    private Label lblNumRecensioniBox;
    @FXML
    private ProgressBar barStelle1;
    @FXML
    private ProgressBar barStelle2;
    @FXML
    private ProgressBar barStelle3;
    @FXML
    private ProgressBar barStelle4;
    @FXML
    private ProgressBar barStelle5;
    @FXML
    private Label lblPctStelle1;
    @FXML
    private Label lblPctStelle2;
    @FXML
    private Label lblPctStelle3;
    @FXML
    private Label lblPctStelle4;
    @FXML
    private Label lblPctStelle5;

    // Scheda Informazioni Generali
    @FXML
    private Label lblNazione;
    @FXML
    private Label lblCittaVal;
    @FXML
    private Label lblIndirizzo;
    @FXML
    private Label lblTipologiaCucinaBox;
    @FXML
    private Label lblPrezzoMedioBox;

    // Mappa e Posizione
    @FXML
    private Label lblMappaTestoIndirizzo;
    @FXML
    private Label lblLatitudine;
    @FXML
    private Label lblLongitudine;
    @FXML
    private Button btnPrenotaOra;

    // Componenti interattivi per lasciare una recensione
    @FXML
    private Label starRec1;
    @FXML
    private Label starRec2;
    @FXML
    private Label starRec3;
    @FXML
    private Label starRec4;
    @FXML
    private Label starRec5;
    @FXML
    private TextArea recensioneTextArea;
    @FXML
    private Button btnPubblicaRecensione;

    // Riepilogo Laterale Destro
    @FXML
    private Label lblRiepilogoStelle;
    @FXML
    private Label lblRiepilogoPrezzo;
    @FXML
    private Label lblRiepilogoCucina;

    private SearchController.RistoranteOggetto ristoranteCorrente;
    private TheKnifeDAO dao;
    private int votoSelezionato = 0; // Memorizza il punteggio cliccato dall'utente

    public void setDao(TheKnifeDAO dao) {
        this.dao = dao;
    }

    /**
     * Cambia visivamente il colore delle stelle da grigio a oro quando l'utente
     * clicca.
     */
    private void aggiornaVisualizzazioneStelleInput(int voto) {
        this.votoSelezionato = voto;
        Label[] stelle = { starRec1, starRec2, starRec3, starRec4, starRec5 };

        for (int i = 0; i < stelle.length; i++) {
            if (stelle[i] != null) {
                if (i < voto) {
                    // Imposta le stelle selezionate color Oro lucido
                    stelle[i].setStyle("-fx-cursor: hand; -fx-font-size: 22px; -fx-text-fill: #FFD700;");
                } else {
                    // Mantiene o ripristina le restanti stelle in Grigio disattivato
                    stelle[i].setStyle("-fx-cursor: hand; -fx-font-size: 22px; -fx-text-fill: #CBD5E1;");
                }
            }
        }
        System.out.println("[INTERAZIONE] Valutazione impostata a: " + voto + " stelle.");
    }

    @FXML
    private void handleStarRec1(MouseEvent event) {
        aggiornaVisualizzazioneStelleInput(1);
    }

    @FXML
    private void handleStarRec2(MouseEvent event) {
        aggiornaVisualizzazioneStelleInput(2);
    }

    @FXML
    private void handleStarRec3(MouseEvent event) {
        aggiornaVisualizzazioneStelleInput(3);
    }

    @FXML
    private void handleStarRec4(MouseEvent event) {
        aggiornaVisualizzazioneStelleInput(4);
    }

    @FXML
    private void handleStarRec5(MouseEvent event) {
        aggiornaVisualizzazioneStelleInput(5);
    }

    @FXML
    private void handlePubblicaRecensione(ActionEvent event) throws SQLException {
        // 1. Controllo di sicurezza: impedisce il crash se il DAO non è stato iniettato
        if (dao == null) {
            System.err.println(
                    "[ERRORE CRITICO] Impossibile pubblicare: TheKnifeDAO è null. Verificare il caricamento del controller.");
            return;
        }

        // 2. Controllo di sicurezza: verifica che ci sia un ristorante valido
        // correntemente caricato
        if (ristoranteCorrente == null) {
            System.err.println("[ERRORE] Nessun ristorante selezionato corrente.");
            return;
        }

        // 3. RECUPERO SESSIONE CORRETTA: Preleva l'ID utente dal Navigator invece del
        // valore fisso -1
        int idUtenteLoggato = Navigator.getInstance().getIdUtenteLoggato();

        // 4. BLOCCO UTENTE NON LOGGATO: Evita la violazione della Foreign Key nel
        // database
        if (idUtenteLoggato == -1) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Accesso Richiesto");
            alert.setHeaderText("Impossibile pubblicare la recensione");
            alert.setContentText("Devi effettuare il login con il tuo account prima di poter recensire un ristorante.");
            alert.showAndWait();
            return;
        }

        // 5. Verifica della valutazione minima
        if (votoSelezionato == 0) {
            System.out.println("[ATTENZIONE] Seleziona almeno una stella prima di pubblicare.");
            return;
        }

        String testo = recensioneTextArea != null ? recensioneTextArea.getText() : "";
        System.out.println("[RECENSIONE] Inserimento nel DB per il ristorante ID: " + ristoranteCorrente.id);
        System.out.println(
                "[RECENSIONE] Inserimento nel DB: " + votoSelezionato + " stelle. Utente ID: " + idUtenteLoggato);

        // Opzionale: Pulisce l'area di testo e resetta le stelle dopo l'inserimento
        // riuscito
        try {
            if (recensioneTextArea != null)
                recensioneTextArea.clear();
            dao.aggiungiRecensione(ristoranteCorrente.id, idUtenteLoggato, votoSelezionato, testo);
        } catch (SQLException e) {
            // "23505" è il codice d'errore standard di PostgreSQL per la violazione di un
            // vincolo UNIQUE
            if ("23505".equals(e.getSQLState())) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Errore di inserimento");
                alert.setHeaderText("Hai già recensito questo ristorante");
                alert.setContentText("Non è possibile lasciare più di una recensione per lo stesso locale.");
                alert.showAndWait();
            } else {
                // Gestisci altri tipi di errori SQL imprevisti
                e.printStackTrace();
            }
        }
        aggiornaValutazioniDaDB();
    }

    @FXML
    private void handlePrenotaOra(ActionEvent event) {
        System.out.println("[PRENOTAZIONE] Apertura del flusso per: "
                + (ristoranteCorrente != null ? ristoranteCorrente.nome : "Ristorante"));
    }

    @FXML
    private void handleCerca(ActionEvent event) {
        Navigator.getInstance().navigateTo("search-view-logged.fxml", "Cerca Ristoranti");
    }

    @FXML
    private void handlePreferiti(ActionEvent event) {
        Navigator.getInstance().navigateTo("favorites-view.fxml", "I Miei Preferiti");
    }

    @FXML
    private void handleRecensioni(ActionEvent event) {
        Navigator.getInstance().navigateTo("reviews-view.fxml", "Le Mie Recensioni");
    }

    @FXML
    private void handleProfilo(ActionEvent event) {
        Navigator.getInstance().navigateToProfile();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Navigator.getInstance().logout();
        Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
    }

    @FXML
    private void handleTornaRisultati(ActionEvent event) {
        System.out.println("[NAVIGAZIONE] Ritorno ai risultati di ricerca.");
        Navigator.getInstance().backToSearchResults();
    }

    @FXML
    private void handleGoToHome(MouseEvent event) {
        System.out.println("[NAVIGAZIONE] Ritorno alla Home principale.");
        Navigator.getInstance().navigateToHomeIntelligent();
    }

    public void caricaDatiRistorante(SearchController.RistoranteOggetto r) {
        if (r == null)
            return;
        this.ristoranteCorrente = r;

        if (lblBreadcrumb != null)
            lblBreadcrumb.setText("Home  ›  " + r.citta + "  ›  " + r.nome);
        if (lblNomeRistorante != null)
            lblNomeRistorante.setText(r.nome);
        if (lblCitta != null)
            lblCitta.setText("📍 " + r.indirizzo + ", " + r.citta + ", " + r.nazione);
        if (lblCucina != null)
            lblCucina.setText(getEmojiCucina(r.cucina) + " Cucina " + r.cucina);
        if (lblPrezzo != null)
            lblPrezzo.setText("€".repeat(Math.max(1, Math.min(3, r.prezzo / 25))) + "  ·  " + r.prezzo + "€ p.p.");

        // Configura gli sfondi grafici corretti
        impostaStileCucinaDinamico(r.cucina);

        // Compilazione della scheda dettagliata delle informazioni
        if (lblNazione != null)
            lblNazione.setText(r.nazione);
        if (lblCittaVal != null)
            lblCittaVal.setText(r.citta);
        if (lblIndirizzo != null)
            lblIndirizzo.setText(r.indirizzo);
        if (lblTipologiaCucinaBox != null)
            lblTipologiaCucinaBox.setText(r.cucina);
        if (lblPrezzoMedioBox != null)
            lblPrezzoMedioBox.setText(r.prezzo + " € a persona");

        // Compilazione del box riepilogo laterale destro
        if (lblRiepilogoPrezzo != null)
            lblRiepilogoPrezzo.setText(r.prezzo + " €");
        if (lblRiepilogoCucina != null)
            lblRiepilogoCucina.setText(r.cucina);

        if (lblMappaTestoIndirizzo != null)
            lblMappaTestoIndirizzo.setText(r.indirizzo + " — " + r.citta + ", " + r.nazione);
        if (lblLatitudine != null)
            lblLatitudine.setText("Lat: " + (r.lat != 0.0 ? r.lat + "°" : "45.4642°"));
        if (lblLongitudine != null)
            lblLongitudine.setText("Lng: " + (r.lon != 0.0 ? r.lon + "°" : "9.1900°"));

        aggiornaValutazioniDaDB();

        if (btnPrenotaOra != null) {
            if (r.prenotazioneOnline) {
                btnPrenotaOra.setDisable(false);
                btnPrenotaOra.setText("Prenota Ora");
            } else {
                btnPrenotaOra.setDisable(true);
                btnPrenotaOra.setText("Prenotazione non disponibile");
            }
        }
    }

    public void impostaStileCucinaDinamico(String cucina) {
        if (cucina == null)
            cucina = "";
        String lower = cucina.toLowerCase().trim();

        // Determina l'emoji corretta tramite la lista completa delle categorie
        String emoji = getEmojiCucina(lower);
        if (lblAvatarRistorante != null) {
            lblAvatarRistorante.setText(emoji);
        }

        // Colore di sfondo associato alla cucina, usato solo per l'hero (la navbar
        // resta verde fissa)
        String coloreSfondo = getCucinaColor(lower);

        if (hboxHero != null) {
            // Rimuove i vecchi stili associati
            hboxHero.getStyleClass().removeAll(
                    "hero-italiana", "hero-giapponese", "hero-vegetariana", "hero-mediterranea",
                    "hero-carne", "hero-messicana", "hero-pesce", "hero-poke", "hero-indiana", "hero-altro");

            // Inietta lo stile inline preservando la struttura morbida
            String stileBase = "-fx-background-radius: 16px; -fx-padding: 32px;";
            hboxHero.setStyle(stileBase + " -fx-background-color: " + coloreSfondo + ";");
        }

        // La navbar della pagina dettagli resta verde come in tutte le altre pagine
        // dell'app:
        // reset esplicito di qualsiasi stile inline residuo, così dipende solo dalla
        // classe CSS "navbar".
        if (hboxNavbar != null) {
            hboxNavbar.setStyle("");
        }
    }

    /**
     * Determina il colore di sfondo associato alla tipologia di cucina, usato per
     * l'hero.
     */
    private String getCucinaColor(String lower) {
        if (lower.contains("pizza"))
            return "#E76F51"; // Arancione crosta di pizza
        if (lower.contains("italian") || lower.contains("primi") || lower.contains("pasta"))
            return "#A7C957"; // Verde oliva/italiano morbido
        if (lower.contains("sushi") || lower.contains("giapponese"))
            return "#EAEAEA"; // Bianco/Grigio chiarissimo (sushi/riso)
        if (lower.contains("cinese") || lower.contains("asian") || lower.contains("ramen") || lower.contains("coreano")
                || lower.contains("thai") || lower.contains("vietnamita"))
            return "#D9381E"; // Rosso Oriente
        if (lower.contains("poke") || lower.contains("hawaiian") || lower.contains("indian") || lower.contains("curry"))
            return "#E9C46A"; // Giallo ocra / curry / esotico
        if (lower.contains("veg") || lower.contains("insalata") || lower.contains("salad")
                || lower.contains("mediterranea") || lower.contains("mediterranean"))
            return "#2D6A4F"; // Verde scuro Healthy
        if (lower.contains("steak") || lower.contains("churrasco") || lower.contains("grill") || lower.contains("meat")
                || lower.contains("carne") || lower.contains("bbq"))
            return "#9E1B1B"; // Rosso scuro/carne intenso
        if (lower.contains("seafood") || lower.contains("crostacei") || lower.contains("aragosta")
                || lower.contains("pesce") || lower.contains("fish") || lower.contains("mare"))
            return "#2A9D8F"; // Azzurro mare / Ottanio
        if (lower.contains("messicano") || lower.contains("mexican") || lower.contains("taco")
                || lower.contains("burrito"))
            return "#6C584C"; // Marroncino terra / messicano
        if (lower.contains("kebab") || lower.contains("turco") || lower.contains("arabo")
                || lower.contains("medio orientale") || lower.contains("libanese"))
            return "#8C7853"; // Avana / Medio oriente
        if (lower.contains("francese") || lower.contains("french"))
            return "#5B7C99"; // Blu ardesia elegante alla francese
        if (lower.contains("greco") || lower.contains("greca") || lower.contains("greek"))
            return "#6B8E4E"; // Verde oliva mediterraneo
        if (lower.contains("burger") || lower.contains("fast food") || lower.contains("americano")
                || lower.contains("patatine") || lower.contains("hot dog") || lower.contains("street")
                || lower.contains("panin") || lower.contains("piadina") || lower.contains("toast"))
            return "#BC6C25"; // Marrone Street Food / Pane tostato
        if (lower.contains("dolci") || lower.contains("dessert") || lower.contains("pasticceria")
                || lower.contains("gelato") || lower.contains("brunch") || lower.contains("colazione")
                || lower.contains("bakery") || lower.contains("crepe"))
            return "#F4A261"; // Arancio pastello / Tortora dolce
        if (lower.contains("pub") || lower.contains("birra") || lower.contains("cocktail") || lower.contains("bar")
                || lower.contains("enoteca") || lower.contains("vino") || lower.contains("caff"))
            return "#3D348B"; // Viola/Blu scuro lounge bar
        return "#40916C"; // Default: Verde TheKnife
    }

    private String getEmojiCucina(String lower) {
        if (lower == null || lower.isEmpty())
            return "👨‍🍳";

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

        return "👨‍🍳";
    }

    private void aggiornaValutazioniDaDB() {
        if (dao == null || ristoranteCorrente == null)
            return;
        try {
            ResultSet rs = dao.getStatisticheRecensioni(ristoranteCorrente.id);
            if (rs.next()) {
                double media = rs.getDouble("media_stelle");
                int totale = rs.getInt("num_recensioni");

                String mediaFormattata = String.format("%.1f ★", media);
                if (lblStelleMedia != null)
                    lblStelleMedia.setText(mediaFormattata);
                if (lblRiepilogoStelle != null)
                    lblRiepilogoStelle.setText(mediaFormattata);
                if (lblNumRecensioni != null)
                    lblNumRecensioni.setText(totale + " recensioni");

                // Card "VALUTAZIONI" (numero grande + stelle + conteggio) e, nella vista
                // loggata,
                // anche l'hero in alto usa questi stessi fx:id
                if (lblRatingBigNumber != null)
                    lblRatingBigNumber.setText(String.format("%.1f", media));
                if (lblStarsDisplayBig != null)
                    lblStarsDisplayBig.setText(costruisciStelleTesto(media));
                if (lblNumRecensioniBox != null)
                    lblNumRecensioniBox.setText(totale + " recensioni");

                aggiornaDistribuzioneStelle(totale);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("[DETTAGLI] Errore nel recupero dati recensione dal DB.");
        }
    }

    /**
     * Costruisce la stringa di 5 caratteri (★ piene / ☆ vuote) corrispondente alla
     * media arrotondata.
     */
    private String costruisciStelleTesto(double media) {
        int piene = (int) Math.round(media);
        piene = Math.max(0, Math.min(5, piene));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++)
            sb.append(i < piene ? "★" : "☆");
        return sb.toString();
    }

    /**
     * Recupera dal DB quante recensioni ci sono per ogni valore di stelle (1-5) e
     * aggiorna
     * le progress bar e le percentuali della card "VALUTAZIONI".
     */
    private void aggiornaDistribuzioneStelle(int totaleRecensioni) {
        if (dao == null || ristoranteCorrente == null)
            return;

        int[] conteggi = new int[6]; // indici 1..5 utilizzati, 0 non usato
        try {
            ResultSet rs = dao.getDistribuzioneStelle(ristoranteCorrente.id);
            while (rs.next()) {
                int stelle = rs.getInt("stelle");
                int conteggio = rs.getInt("conteggio");
                if (stelle >= 1 && stelle <= 5)
                    conteggi[stelle] = conteggio;
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("[DETTAGLI] Errore nel recupero della distribuzione delle stelle.");
            return;
        }

        aggiornaBarraStella(barStelle5, lblPctStelle5, conteggi[5], totaleRecensioni);
        aggiornaBarraStella(barStelle4, lblPctStelle4, conteggi[4], totaleRecensioni);
        aggiornaBarraStella(barStelle3, lblPctStelle3, conteggi[3], totaleRecensioni);
        aggiornaBarraStella(barStelle2, lblPctStelle2, conteggi[2], totaleRecensioni);
        aggiornaBarraStella(barStelle1, lblPctStelle1, conteggi[1], totaleRecensioni);
    }

    private void aggiornaBarraStella(ProgressBar barra, Label percentualeLabel, int conteggio, int totale) {
        double percentuale = (totale > 0) ? (double) conteggio / totale : 0.0;
        if (barra != null)
            barra.setProgress(percentuale);
        if (percentualeLabel != null)
            percentualeLabel.setText(Math.round(percentuale * 100) + "%");
    }
}
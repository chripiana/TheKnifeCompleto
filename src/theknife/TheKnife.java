package theknife;

// File: src/theknife/TheKnife.java
/**
 * Progetto TheKnife - Piattaforma per la ricerca di ristoranti
 * Università degli Studi dell'Insubria
 * Corso: Laboratorio Interdisciplinare A
 * A.A. 2024/2025
 *
 * @author Pianarosa Christian
 * @matricola [Numero Matricola]
 * @sede CO
 */
import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe principale dell'applicazione TheKnife
 */
public class TheKnife {
    private static GestoreRistoranti gestoreRistoranti;
    private static GestoreUtenti gestoreUtenti;
    private static Scanner scanner;
    private static UtenteCorrente utenteCorrente;

    public static void main(String[] args) {
        System.out.println("=== Benvenuto in TheKnife ===");

        // Inizializzazione
        gestoreRistoranti = new GestoreRistoranti();
        gestoreUtenti = new GestoreUtenti();
        scanner = new Scanner(System.in);
        utenteCorrente = new UtenteCorrente();

        // Caricamento dati
        caricaDati();

        // Menu principale
        menuPrincipale();

        // Salvataggio dati prima dell'uscita
        salvaDati();
        scanner.close();
    }

    /**
     * Carica i dati da file
     */
    private static void caricaDati() {
        try {
            gestoreRistoranti.caricaRistoranti();
            gestoreUtenti.caricaUtenti();
            System.out.println("Dati caricati con successo!");
        } catch (Exception e) {
            System.out.println("Errore nel caricamento dei dati: " + e.getMessage());
            System.out.println("Continuazione con dati vuoti...");
        }
    }

    /**
     * Salva i dati su file
     */
    private static void salvaDati() {
        try {
            gestoreRistoranti.salvaRistoranti();
            gestoreUtenti.salvaUtenti();
            System.out.println("Dati salvati con successo!");
        } catch (Exception e) {
            System.out.println("Errore nel salvataggio dei dati: " + e.getMessage());
        }
    }

    /**
     * Menu principale dell'applicazione
     */
    private static void menuPrincipale() {
        while (true) {
            System.out.println("\n=== MENU PRINCIPALE ===");
            System.out.println("1. Login");
            System.out.println("2. Registrazione");
            System.out.println("3. Continua come Guest");
            System.out.println("0. Esci");
            System.out.print("Scelta: ");

            int scelta = leggiIntero();

            switch (scelta) {
                case 1:
                    login();
                    break;
                case 2:
                    registrazione();
                    break;
                case 3:
                    menuGuest();
                    break;
                case 0:
                    System.out.println("Arrivederci!");
                    return;
                default:
                    System.out.println("Scelta non valida!");
            }
        }
    }

    /**
     * Gestisce il login dell'utente
     */
    private static void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Utente utente = gestoreUtenti.autenticaUtente(username, password);
        if (utente != null) {
            utenteCorrente.setUtente(utente);
            System.out.println("Login effettuato con successo!");

            if (utente.getRuolo() == TipoUtente.CLIENTE) {
                menuCliente();
            } else {
                menuRistoratore();
            }
        } else {
            System.out.println("Credenziali non valide!");
        }
    }

    /**
     * Gestisce la registrazione di un nuovo utente
     */
    private static void registrazione() {
        System.out.println("\n=== REGISTRAZIONE ===");

        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("Cognome: ");
        String cognome = scanner.nextLine();

        System.out.print("Username: ");
        String username = scanner.nextLine();

        if (gestoreUtenti.esisteUsername(username)) {
            System.out.println("Username già esistente!");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Data di nascita (gg/mm/aaaa) - opzionale: ");
        String dataNascitaStr = scanner.nextLine();
        LocalDate dataNascita = null;
        if (!dataNascitaStr.isEmpty()) {
            try {
                dataNascita = LocalDate.parse(dataNascitaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e) {
                System.out.println("Data non valida, procedendo senza data di nascita");
            }
        }

        System.out.print("Luogo di domicilio: ");
        String luogoDomicilio = scanner.nextLine();

        System.out.println("Tipo di utente:");
        System.out.println("1. Cliente");
        System.out.println("2. Ristoratore");
        System.out.print("Scelta: ");
        int tipoScelta = leggiIntero();

        TipoUtente ruolo = (tipoScelta == 2) ? TipoUtente.RISTORATORE : TipoUtente.CLIENTE;

        Utente nuovoUtente = new Utente(nome, cognome, username, password, dataNascita, luogoDomicilio, ruolo);
        gestoreUtenti.registraUtente(nuovoUtente);

        System.out.println("Registrazione completata con successo!");
    }

    /**
     * Menu per utenti guest
     */
    private static void menuGuest() {
        System.out.print("Inserisci il nome del luogo per la ricerca: ");
        String luogo = scanner.nextLine();

        while (true) {
            System.out.println("\n=== MENU GUEST ===");
            System.out.println("1. Cerca ristoranti");
            System.out.println("2. Visualizza ristoranti vicini a " + luogo);
            System.out.println("0. Torna al menu principale");
            System.out.print("Scelta: ");

            int scelta = leggiIntero();

            switch (scelta) {
                case 1:
                    cercaRistoranti(luogo);
                    break;
                case 2:
                    visualizzaRistorantiVicini(luogo);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Scelta non valida!");
            }
        }
    }

    /**
     * Menu per clienti registrati
     */
    private static void menuCliente() {
        while (true) {
            System.out.println("\n=== MENU CLIENTE ===");
            System.out.println("1. Cerca ristoranti");
            System.out.println("2. Visualizza ristoranti vicini");
            System.out.println("3. Visualizza preferiti");
            System.out.println("4. Visualizza le mie recensioni");
            System.out.println("5. Logout");
            System.out.print("Scelta: ");

            int scelta = leggiIntero();

            switch (scelta) {
                case 1:
                    cercaRistoranti(utenteCorrente.getUtente().getLuogoDomicilio());
                    break;
                case 2:
                    visualizzaRistorantiVicini(utenteCorrente.getUtente().getLuogoDomicilio());
                    break;
                case 3:
                    visualizzaPreferiti();
                    break;
                case 4:
                    visualizzaMieRecensioni();
                    break;
                case 5:
                    logout();
                    return;
                default:
                    System.out.println("Scelta non valida!");
            }
        }
    }

    /**
     * Menu per ristoratori registrati
     */
    private static void menuRistoratore() {
        while (true) {
            System.out.println("\n=== MENU RISTORATORE ===");
            System.out.println("1. Aggiungi ristorante");
            System.out.println("2. Visualizza i miei ristoranti");
            System.out.println("3. Visualizza recensioni dei miei ristoranti");
            System.out.println("4. Rispondi alle recensioni");
            System.out.println("5. Logout");
            System.out.print("Scelta: ");

            int scelta = leggiIntero();

            switch (scelta) {
                case 1:
                    aggiungiRistorante();
                    break;
                case 2:
                    visualizzaMieiRistoranti();
                    break;
                case 3:
                    visualizzaRecensioniMieiRistoranti();
                    break;
                case 4:
                    rispondiAlleRecensioni();
                    break;
                case 5:
                    logout();
                    return;
                default:
                    System.out.println("Scelta non valida!");
            }
        }
    }

    /**
     * Cerca ristoranti con filtri
     */
    private static void cercaRistoranti(String luogoBase) {
        System.out.println("\n=== RICERCA RISTORANTI ===");

        FiltroRicerca filtro = new FiltroRicerca();
        filtro.setLuogo(luogoBase);

        System.out.print("Tipo di cucina (opzionale): ");
        String tipoCucina = scanner.nextLine();
        if (!tipoCucina.isEmpty()) {
            filtro.setTipoCucina(tipoCucina);
        }

        System.out.print("Prezzo massimo (opzionale): ");
        String prezzoMaxStr = scanner.nextLine();
        if (!prezzoMaxStr.isEmpty()) {
            try {
                filtro.setPrezzoMax(Double.parseDouble(prezzoMaxStr));
            } catch (NumberFormatException e) {
                System.out.println("Prezzo non valido, ignorato");
            }
        }

        System.out.print("Delivery disponibile? (s/n, opzionale): ");
        String deliveryStr = scanner.nextLine();
        if (!deliveryStr.isEmpty()) {
            filtro.setDelivery(deliveryStr.toLowerCase().startsWith("s"));
        }

        System.out.print("Prenotazione online disponibile? (s/n, opzionale): ");
        String prenotazioneStr = scanner.nextLine();
        if (!prenotazioneStr.isEmpty()) {
            filtro.setPrenotazioneOnline(prenotazioneStr.toLowerCase().startsWith("s"));
        }

        List<Ristorante> risultati = gestoreRistoranti.cercaRistoranti(filtro);

        if (risultati.isEmpty()) {
            System.out.println("Nessun ristorante trovato con i criteri specificati.");
        } else {
            System.out.println("\n=== RISULTATI RICERCA ===");
            for (int i = 0; i < risultati.size(); i++) {
                Ristorante r = risultati.get(i);
                System.out.printf("%d. %s - %s, %s (%.2f€, %.1f⭐)\n",
                        i + 1, r.getNome(), r.getCitta(), r.getTipoCucina(),
                        r.getFasciaPrezzo(), r.getMediaStelle());
            }

            System.out.print("\nSeleziona un ristorante (0 per tornare): ");
            int scelta = leggiIntero();
            if (scelta > 0 && scelta <= risultati.size()) {
                visualizzaRistorante(risultati.get(scelta - 1));
            }
        }
    }

    /**
     * Visualizza i ristoranti vicini a un luogo
     */
    private static void visualizzaRistorantiVicini(String luogo) {
        List<Ristorante> vicini = gestoreRistoranti.getRistorantiVicini(luogo);

        if (vicini.isEmpty()) {
            System.out.println("Nessun ristorante trovato vicino a " + luogo);
        } else {
            System.out.println("\n=== RISTORANTI VICINI A " + luogo.toUpperCase() + " ===");
            for (int i = 0; i < vicini.size(); i++) {
                Ristorante r = vicini.get(i);
                System.out.printf("%d. %s - %s (%.2f€, %.1f⭐)\n",
                        i + 1, r.getNome(), r.getIndirizzo(), r.getFasciaPrezzo(), r.getMediaStelle());
            }

            System.out.print("\nSeleziona un ristorante (0 per tornare): ");
            int scelta = leggiIntero();
            if (scelta > 0 && scelta <= vicini.size()) {
                visualizzaRistorante(vicini.get(scelta - 1));
            }
        }
    }

    /**
     * Visualizza i dettagli di un ristorante
     */
    private static void visualizzaRistorante(Ristorante ristorante) {
        System.out.println("\n=== DETTAGLI RISTORANTE ===");
        System.out.println("Nome: " + ristorante.getNome());
        System.out.println("Indirizzo: " + ristorante.getIndirizzo());
        System.out.println("Città: " + ristorante.getCitta());
        System.out.println("Nazione: " + ristorante.getNazione());
        System.out.println("Tipo di cucina: " + ristorante.getTipoCucina());
        System.out.println("Fascia di prezzo: " + ristorante.getFasciaPrezzo() + "€");
        System.out.println("Delivery: " + (ristorante.isDelivery() ? "Sì" : "No"));
        System.out.println("Prenotazione online: " + (ristorante.isPrenotazioneOnline() ? "Sì" : "No"));
        System.out.println("Valutazione: " + ristorante.getMediaStelle() + "⭐ (" + ristorante.getNumeroRecensioni() + " recensioni)");

        // Mostra recensioni
        visualizzaRecensioni(ristorante);

        if (utenteCorrente.isLoggato() && utenteCorrente.getUtente().getRuolo() == TipoUtente.CLIENTE) {
            menuAzioniCliente(ristorante);
        }
    }

    /**
     * Menu delle azioni disponibili per un cliente su un ristorante
     */
    private static void menuAzioniCliente(Ristorante ristorante) {
        System.out.println("\n=== AZIONI DISPONIBILI ===");
        System.out.println("1. Aggiungi ai preferiti");
        System.out.println("2. Rimuovi dai preferiti");
        System.out.println("3. Scrivi recensione");
        System.out.println("4. Modifica mia recensione");
        System.out.println("5. Elimina mia recensione");
        System.out.println("0. Torna indietro");
        System.out.print("Scelta: ");

        int scelta = leggiIntero();

        switch (scelta) {
            case 1:
                aggiungiPreferito(ristorante);
                break;
            case 2:
                rimuoviPreferito(ristorante);
                break;
            case 3:
                aggiungiRecensione(ristorante);
                break;
            case 4:
                modificaRecensione(ristorante);
                break;
            case 5:
                eliminaRecensione(ristorante);
                break;
        }
    }

    /**
     * Visualizza le recensioni di un ristorante
     */
    private static void visualizzaRecensioni(Ristorante ristorante) {
        List<Recensione> recensioni = ristorante.getRecensioni();

        if (recensioni.isEmpty()) {
            System.out.println("\nNessuna recensione disponibile.");
        } else {
            System.out.println("\n=== RECENSIONI ===");
            for (Recensione recensione : recensioni) {
                System.out.println("Utente: " + recensione.getUsername());
                System.out.println("Stelle: " + "⭐".repeat(recensione.getStelle()));
                System.out.println("Commento: " + recensione.getTesto());
                System.out.println("Data: " + recensione.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                if (recensione.getRisposta() != null) {
                    System.out.println("Risposta del ristoratore: " + recensione.getRisposta());
                }
                System.out.println("---");
            }
        }
    }

    // Metodi per le funzionalità cliente
    private static void aggiungiPreferito(Ristorante ristorante) {
        utenteCorrente.getUtente().aggiungiPreferito(ristorante.getId());
        System.out.println("Ristorante aggiunto ai preferiti!");
    }

    private static void rimuoviPreferito(Ristorante ristorante) {
        utenteCorrente.getUtente().rimuoviPreferito(ristorante.getId());
        System.out.println("Ristorante rimosso dai preferiti!");
    }

    private static void aggiungiRecensione(Ristorante ristorante) {
        System.out.print("Numero di stelle (1-5): ");
        int stelle = leggiIntero();
        if (stelle < 1 || stelle > 5) {
            System.out.println("Numero di stelle non valido!");
            return;
        }

        System.out.print("Scrivi la tua recensione: ");
        String testo = scanner.nextLine();

        Recensione recensione = new Recensione(
                utenteCorrente.getUtente().getUsername(),
                stelle,
                testo,
                LocalDate.now()
        );

        ristorante.aggiungiRecensione(recensione);
        System.out.println("Recensione aggiunta con successo!");
    }

    private static void modificaRecensione(Ristorante ristorante) {
        Recensione miaRecensione = ristorante.getRecensione(utenteCorrente.getUtente().getUsername());
        if (miaRecensione == null) {
            System.out.println("Non hai ancora scritto una recensione per questo ristorante!");
            return;
        }

        System.out.println("Recensione attuale: " + miaRecensione.getTesto());
        System.out.println("Stelle attuali: " + miaRecensione.getStelle());

        System.out.print("Nuovo numero di stelle (1-5): ");
        int nuoveStelle = leggiIntero();
        if (nuoveStelle < 1 || nuoveStelle > 5) {
            System.out.println("Numero di stelle non valido!");
            return;
        }

        System.out.print("Nuovo testo della recensione: ");
        String nuovoTesto = scanner.nextLine();

        miaRecensione.setStelle(nuoveStelle);
        miaRecensione.setTesto(nuovoTesto);

        System.out.println("Recensione modificata con successo!");
    }

    private static void eliminaRecensione(Ristorante ristorante) {
        if (ristorante.rimuoviRecensione(utenteCorrente.getUtente().getUsername())) {
            System.out.println("Recensione eliminata con successo!");
        } else {
            System.out.println("Non hai recensioni da eliminare per questo ristorante!");
        }
    }

    private static void visualizzaPreferiti() {
        List<String> preferiti = utenteCorrente.getUtente().getPreferiti();

        if (preferiti.isEmpty()) {
            System.out.println("Non hai ristoranti preferiti.");
        } else {
            System.out.println("\n=== I TUOI PREFERITI ===");
            for (String idRistorante : preferiti) {
                Ristorante ristorante = gestoreRistoranti.getRistoranteById(idRistorante);
                if (ristorante != null) {
                    System.out.printf("%s - %s, %s (%.2f€, %.1f⭐)\n",
                            ristorante.getNome(), ristorante.getCitta(), ristorante.getTipoCucina(),
                            ristorante.getFasciaPrezzo(), ristorante.getMediaStelle());
                }
            }
        }
    }

    private static void visualizzaMieRecensioni() {
        List<Ristorante> conRecensioni = gestoreRistoranti.getRistorantiConRecensioneDi(utenteCorrente.getUtente().getUsername());

        if (conRecensioni.isEmpty()) {
            System.out.println("Non hai ancora scritto recensioni.");
        } else {
            System.out.println("\n=== LE TUE RECENSIONI ===");
            for (Ristorante ristorante : conRecensioni) {
                Recensione recensione = ristorante.getRecensione(utenteCorrente.getUtente().getUsername());
                if (recensione != null) {
                    System.out.println("Ristorante: " + ristorante.getNome());
                    System.out.println("Stelle: " + "⭐".repeat(recensione.getStelle()));
                    System.out.println("Commento: " + recensione.getTesto());
                    System.out.println("Data: " + recensione.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    System.out.println("---");
                }
            }
        }
    }

    // Metodi per le funzionalità ristoratore
    private static void aggiungiRistorante() {
        System.out.println("\n=== AGGIUNGI RISTORANTE ===");

        System.out.print("Nome del ristorante: ");
        String nome = scanner.nextLine();

        System.out.print("Nazione: ");
        String nazione = scanner.nextLine();

        System.out.print("Città: ");
        String citta = scanner.nextLine();

        System.out.print("Indirizzo: ");
        String indirizzo = scanner.nextLine();

        System.out.print("Latitudine: ");
        double latitudine = leggiDouble();

        System.out.print("Longitudine: ");
        double longitudine = leggiDouble();

        System.out.print("Fascia di prezzo (€): ");
        double fasciaPrezzo = leggiDouble();

        System.out.print("Delivery disponibile? (s/n): ");
        boolean delivery = scanner.nextLine().toLowerCase().startsWith("s");

        System.out.print("Prenotazione online disponibile? (s/n): ");
        boolean prenotazioneOnline = scanner.nextLine().toLowerCase().startsWith("s");

        System.out.print("Tipo di cucina: ");
        String tipoCucina = scanner.nextLine();

        Ristorante nuovoRistorante = new Ristorante(
                nome, nazione, citta, indirizzo, latitudine, longitudine,
                fasciaPrezzo, delivery, prenotazioneOnline, tipoCucina
        );

        nuovoRistorante.setProprietario(utenteCorrente.getUtente().getUsername());
        gestoreRistoranti.aggiungiRistorante(nuovoRistorante);

        System.out.println("Ristorante aggiunto con successo!");
    }

    private static void visualizzaMieiRistoranti() {
        List<Ristorante> mieiRistoranti = gestoreRistoranti.getRistorantiDi(utenteCorrente.getUtente().getUsername());

        if (mieiRistoranti.isEmpty()) {
            System.out.println("Non hai ancora aggiunto ristoranti.");
        } else {
            System.out.println("\n=== I TUOI RISTORANTI ===");
            for (Ristorante ristorante : mieiRistoranti) {
                System.out.printf("%s - %s, %s (%.2f€, %.1f⭐, %d recensioni)\n",
                        ristorante.getNome(), ristorante.getCitta(), ristorante.getTipoCucina(),
                        ristorante.getFasciaPrezzo(), ristorante.getMediaStelle(), ristorante.getNumeroRecensioni());
            }
        }
    }

    private static void visualizzaRecensioniMieiRistoranti() {
        List<Ristorante> mieiRistoranti = gestoreRistoranti.getRistorantiDi(utenteCorrente.getUtente().getUsername());

        if (mieiRistoranti.isEmpty()) {
            System.out.println("Non hai ancora aggiunto ristoranti.");
            return;
        }

        System.out.println("\n=== RECENSIONI DEI TUOI RISTORANTI ===");
        for (Ristorante ristorante : mieiRistoranti) {
            System.out.println("\n--- " + ristorante.getNome() + " ---");
            if (ristorante.getRecensioni().isEmpty()) {
                System.out.println("Nessuna recensione ancora.");
            } else {
                for (Recensione recensione : ristorante.getRecensioni()) {
                    System.out.println("Utente: " + recensione.getUsername());
                    System.out.println("Stelle: " + "⭐".repeat(recensione.getStelle()));
                    System.out.println("Commento: " + recensione.getTesto());
                    System.out.println("Data: " + recensione.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                    if (recensione.getRisposta() != null) {
                        System.out.println("Tua risposta: " + recensione.getRisposta());
                    } else {
                        System.out.println("(Nessuna risposta)");
                    }
                    System.out.println("---");
                }
            }
        }
    }

    private static void rispondiAlleRecensioni() {
        List<Ristorante> mieiRistoranti = gestoreRistoranti.getRistorantiDi(utenteCorrente.getUtente().getUsername());

        if (mieiRistoranti.isEmpty()) {
            System.out.println("Non hai ancora aggiunto ristoranti.");
            return;
        }

        System.out.println("\n=== RISPONDI ALLE RECENSIONI ===");

        for (Ristorante ristorante : mieiRistoranti) {
            List<Recensione> recensioniSenzaRisposta = ristorante.getRecensioni().stream()
                    .filter(r -> r.getRisposta() == null)
                    .toList();

            if (!recensioniSenzaRisposta.isEmpty()) {
                System.out.println("\n--- " + ristorante.getNome() + " ---");

                for (Recensione recensione : recensioniSenzaRisposta) {
                    System.out.println("Recensione di " + recensione.getUsername());
                    System.out.println("Stelle: " + "⭐".repeat(recensione.getStelle()));
                    System.out.println("Commento: " + recensione.getTesto());
                    System.out.println("Data: " + recensione.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                    System.out.print("Vuoi rispondere? (s/n): ");
                    if (scanner.nextLine().toLowerCase().startsWith("s")) {
                        System.out.print("Scrivi la tua risposta: ");
                        String risposta = scanner.nextLine();
                        recensione.setRisposta(risposta);
                        System.out.println("Risposta aggiunta!");
                    }
                    System.out.println("---");
                }
            }
        }
    }

    private static void logout() {
        utenteCorrente.logout();
        System.out.println("Logout effettuato!");
    }

    // Metodi di utilità
    private static int leggiIntero() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double leggiDouble() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
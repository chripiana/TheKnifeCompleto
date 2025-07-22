package theknife;

// File: src/theknife/GestoreUtenti.java
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

import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe per la gestione degli utenti del sistema
 * Gestisce registrazione, autenticazione e persistenza degli utenti
 */
public class GestoreUtenti {
    private static final String FILE_UTENTI = "../data/utenti.txt";
    private static final String SEPARATORE = ";";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private List<Utente> utenti;

    /**
     * Costruttore del gestore utenti
     */
    public GestoreUtenti() {
        this.utenti = new ArrayList<>();
        // Crea la directory data se non esiste
        File dataDir = new File("../data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    /**
     * Carica gli utenti dal file
     * @throws IOException se si verifica un errore di I/O
     */
    public void caricaUtenti() throws IOException {
        File file = new File(FILE_UTENTI);
        if (!file.exists()) {
            System.out.println("File utenti non trovato. Verrà creato al primo salvataggio.");
            return;
        }

        utenti.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                try {
                    Utente utente = parseUtenteDaLinea(linea);
                    if (utente != null) {
                        utenti.add(utente);
                    }
                } catch (Exception e) {
                    System.out.println("Errore nel parsing dell'utente: " + linea);
                    System.out.println("Errore: " + e.getMessage());
                }
            }
        }

        System.out.println("Caricati " + utenti.size() + " utenti.");
    }

    /**
     * Salva gli utenti nel file
     * @throws IOException se si verifica un errore di I/O
     */
    public void salvaUtenti() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_UTENTI))) {
            for (Utente utente : utenti) {
                writer.println(formatUtentePerFile(utente));
            }
        }
        System.out.println("Salvati " + utenti.size() + " utenti.");
    }

    /**
     * Registra un nuovo utente nel sistema
     * @param utente l'utente da registrare
     * @return true se la registrazione è avvenuta con successo
     */
    public boolean registraUtente(Utente utente) {
        if (esisteUsername(utente.getUsername())) {
            return false;
        }

        // Cripta la password prima di salvare
        utente.setPassword(criptaPassword(utente.getPassword()));
        utenti.add(utente);

        return true;
    }

    /**
     * Autentica un utente
     * @param username nome utente
     * @param password password in chiaro
     * @return l'utente autenticato o null se le credenziali non sono valide
     */
    public Utente autenticaUtente(String username, String password) {
        String passwordCriptata = criptaPassword(password);

        for (Utente utente : utenti) {
            if (utente.getUsername().equals(username) &&
                    utente.getPassword().equals(passwordCriptata)) {
                return utente;
            }
        }

        return null;
    }

    /**
     * Verifica se un username esiste già
     * @param username nome utente da verificare
     * @return true se l'username esiste
     */
    public boolean esisteUsername(String username) {
        return utenti.stream().anyMatch(u -> u.getUsername().equals(username));
    }

    /**
     * Ottiene un utente per username
     * @param username nome utente
     * @return l'utente o null se non trovato
     */
    public Utente getUtenteByUsername(String username) {
        return utenti.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Ottiene tutti gli utenti
     * @return lista di tutti gli utenti
     */
    public List<Utente> getTuttiUtenti() {
        return new ArrayList<>(utenti);
    }

    /**
     * Ottiene tutti i clienti
     * @return lista dei clienti
     */
    public List<Utente> getClienti() {
        return utenti.stream()
                .filter(u -> u.getRuolo() == TipoUtente.CLIENTE)
                .toList();
    }

    /**
     * Ottiene tutti i ristoratori
     * @return lista dei ristoratori
     */
    public List<Utente> getRistoratori() {
        return utenti.stream()
                .filter(u -> u.getRuolo() == TipoUtente.RISTORATORE)
                .toList();
    }

    /**
     * Rimuove un utente dal sistema
     * @param username nome utente da rimuovere
     * @return true se l'utente è stato rimosso
     */
    public boolean rimuoviUtente(String username) {
        return utenti.removeIf(u -> u.getUsername().equals(username));
    }

    /**
     * Aggiorna le informazioni di un utente
     * @param utente utente con le informazioni aggiornate
     * @return true se l'aggiornamento è avvenuto con successo
     */
    public boolean aggiornaUtente(Utente utente) {
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getUsername().equals(utente.getUsername())) {
                utenti.set(i, utente);
                return true;
            }
        }
        return false;
    }

    /**
     * Cripta una password usando SHA-256
     * @param password password in chiaro
     * @return password criptata
     */
    private String criptaPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Errore nella crittografia della password: " + e.getMessage());
            return password; // Fallback non sicuro
        }
    }

    /**
     * Converte una linea del file in un oggetto Utente
     * @param linea linea del file da parsare
     * @return oggetto Utente o null se il parsing fallisce
     */
    private Utente parseUtenteDaLinea(String linea) {
        String[] parti = linea.split(SEPARATORE);

        if (parti.length < 6) {
            System.out.println("Formato linea non valido: " + linea);
            return null;
        }

        try {
            String nome = parti[0].trim();
            String cognome = parti[1].trim();
            String username = parti[2].trim();
            String password = parti[3].trim();

            // Data di nascita (può essere vuota)
            LocalDate dataNascita = null;
            if (parti.length > 4 && !parti[4].trim().isEmpty()) {
                try {
                    dataNascita = LocalDate.parse(parti[4].trim(), DATE_FORMATTER);
                } catch (Exception e) {
                    System.out.println("Data di nascita non valida per utente " + username);
                }
            }

            String luogoDomicilio = parti[5].trim();

            // Ruolo
            TipoUtente ruolo = TipoUtente.CLIENTE;
            if (parti.length > 6) {
                try {
                    ruolo = TipoUtente.valueOf(parti[6].trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.println("Ruolo non valido per utente " + username + ", usando CLIENTE");
                }
            }

            Utente utente = new Utente(nome, cognome, username, password, dataNascita, luogoDomicilio, ruolo);

            // Carica lista preferiti se presente
            if (parti.length > 7 && !parti[7].trim().isEmpty()) {
                String[] preferiti = parti[7].split(",");
                for (String preferito : preferiti) {
                    if (!preferito.trim().isEmpty()) {
                        utente.aggiungiPreferito(preferito.trim());
                    }
                }
            }

            return utente;

        } catch (Exception e) {
            System.out.println("Errore nel parsing dell'utente: " + e.getMessage());
            return null;
        }
    }

    /**
     * Formatta un utente per il salvataggio su file
     * @param utente utente da formattare
     * @return stringa formattata per il file
     */
    private String formatUtentePerFile(Utente utente) {
        StringBuilder sb = new StringBuilder();

        sb.append(utente.getNome()).append(SEPARATORE);
        sb.append(utente.getCognome()).append(SEPARATORE);
        sb.append(utente.getUsername()).append(SEPARATORE);
        sb.append(utente.getPassword()).append(SEPARATORE);

        // Data di nascita
        if (utente.getDataNascita() != null) {
            sb.append(utente.getDataNascita().format(DATE_FORMATTER));
        }
        sb.append(SEPARATORE);

        sb.append(utente.getLuogoDomicilio()).append(SEPARATORE);
        sb.append(utente.getRuolo().name()).append(SEPARATORE);

        // Lista preferiti
        if (!utente.getPreferiti().isEmpty()) {
            sb.append(String.join(",", utente.getPreferiti()));
        }

        return sb.toString();
    }

    /**
     * Restituisce statistiche degli utenti
     * @return mappa con le statistiche
     */
    public Map<String, Integer> getStatistiche() {
        Map<String, Integer> stats = new HashMap<>();

        stats.put("totale", utenti.size());
        stats.put("clienti", (int) utenti.stream().filter(u -> u.getRuolo() == TipoUtente.CLIENTE).count());
        stats.put("ristoratori", (int) utenti.stream().filter(u -> u.getRuolo() == TipoUtente.RISTORATORE).count());

        return stats;
    }

    /**
     * Verifica l'integrità dei dati degli utenti
     * @return true se tutti i dati sono validi
     */
    public boolean verificaIntegrita() {
        for (Utente utente : utenti) {
            if (utente.getUsername() == null || utente.getUsername().trim().isEmpty() ||
                    utente.getPassword() == null || utente.getPassword().trim().isEmpty() ||
                    utente.getNome() == null || utente.getNome().trim().isEmpty() ||
                    utente.getCognome() == null || utente.getCognome().trim().isEmpty()) {

                System.out.println("Utente con dati incompleti: " + utente.getUsername());
                return false;
            }
        }
        return true;
    }

    /**
     * Pulisce i dati degli utenti rimuovendo quelli con informazioni incomplete
     */
    public void pulisciDati() {
        utenti.removeIf(utente ->
                utente.getUsername() == null || utente.getUsername().trim().isEmpty() ||
                        utente.getPassword() == null || utente.getPassword().trim().isEmpty() ||
                        utente.getNome() == null || utente.getNome().trim().isEmpty() ||
                        utente.getCognome() == null || utente.getCognome().trim().isEmpty()
        );
    }
}
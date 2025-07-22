package theknife;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Utente {
    private String nome;
    private String cognome;
    private String username;
    private String password;
    private LocalDate dataNascita;
    private String luogoDomicilio;
    private TipoUtente ruolo;
    private List<String> preferiti;

    public Utente(String nome, String cognome, String username, String password,
                  LocalDate dataNascita, String luogoDomicilio, TipoUtente ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.password = password;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
        this.ruolo = ruolo;
        this.preferiti = new ArrayList<>();
    }

    // Costruttore per il caricamento da file
    public Utente(String nome, String cognome, String username, String password,
                  LocalDate dataNascita, String luogoDomicilio, TipoUtente ruolo, boolean passwordGiaCifrata) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.password = password;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
        this.ruolo = ruolo;
        this.preferiti = new ArrayList<>();
    }

    public boolean verificaPassword(String password) {
        return this.password.equals(password);
    }

    public void aggiungiPreferito(String idRistorante) {
        if (!preferiti.contains(idRistorante)) {
            preferiti.add(idRistorante);
        }
    }

    public void rimuoviPreferito(String idRistorante) {
        preferiti.remove(idRistorante);
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDate getDataNascita() { return dataNascita; }
    public void setDataNascita(LocalDate dataNascita) { this.dataNascita = dataNascita; }

    public String getLuogoDomicilio() { return luogoDomicilio; }
    public void setLuogoDomicilio(String luogoDomicilio) { this.luogoDomicilio = luogoDomicilio; }

    public TipoUtente getRuolo() { return ruolo; }
    public void setRuolo(TipoUtente ruolo) { this.ruolo = ruolo; }

    public List<String> getPreferiti() { return new ArrayList<>(preferiti); }
    public void setPreferiti(List<String> preferiti) { this.preferiti = new ArrayList<>(preferiti); }

    @Override
    public String toString() {
        String dataNascitaStr = dataNascita != null ? dataNascita.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        String preferitiStr = String.join(",", preferiti);

        return String.format("%s|%s|%s|%s|%s|%s|%s|%s",
                nome, cognome, username, password, dataNascitaStr,
                luogoDomicilio, ruolo.toString(), preferitiStr);
    }

    public static Utente fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 7) return null;

        try {
            LocalDate dataNascita = null;
            if (!parts[4].isEmpty()) {
                dataNascita = LocalDate.parse(parts[4], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            TipoUtente ruolo = TipoUtente.valueOf(parts[6]);

            Utente utente = new Utente(parts[0], parts[1], parts[2], parts[3],
                    dataNascita, parts[5], ruolo, true);

            if (parts.length > 7 && !parts[7].isEmpty()) {
                List<String> preferiti = Arrays.asList(parts[7].split(","));
                utente.setPreferiti(preferiti);
            }

            return utente;
        } catch (Exception e) {
            return null;
        }
    }
}
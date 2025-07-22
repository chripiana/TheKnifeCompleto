package theknife;

import java.util.*;

public class Ristorante {
    private String id;
    private String nome;
    private String nazione;
    private String citta;
    private String indirizzo;
    private double latitudine;
    private double longitudine;
    private double fasciaPrezzo;
    private boolean delivery;
    private boolean prenotazioneOnline;
    private String tipoCucina;
    private String proprietario;
    private List<Recensione> recensioni;

    public Ristorante(String nome, String nazione, String citta, String indirizzo,
                      double latitudine, double longitudine, double fasciaPrezzo,
                      boolean delivery, boolean prenotazioneOnline, String tipoCucina) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome != null ? nome : "";
        this.nazione = nazione != null ? nazione : "";
        this.citta = citta != null ? citta : "";
        this.indirizzo = indirizzo != null ? indirizzo : "";
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.fasciaPrezzo = fasciaPrezzo;
        this.delivery = delivery;
        this.prenotazioneOnline = prenotazioneOnline;
        this.tipoCucina = tipoCucina != null ? tipoCucina : "";
        this.recensioni = new ArrayList<>();
    }

    // Costruttore per il caricamento da file
    public Ristorante(String id, String nome, String nazione, String citta, String indirizzo,
                      double latitudine, double longitudine, double fasciaPrezzo,
                      boolean delivery, boolean prenotazioneOnline, String tipoCucina) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.nome = nome != null ? nome : "";
        this.nazione = nazione != null ? nazione : "";
        this.citta = citta != null ? citta : "";
        this.indirizzo = indirizzo != null ? indirizzo : "";
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.fasciaPrezzo = fasciaPrezzo;
        this.delivery = delivery;
        this.prenotazioneOnline = prenotazioneOnline;
        this.tipoCucina = tipoCucina != null ? tipoCucina : "";
        this.recensioni = new ArrayList<>();
    }

    public void aggiungiRecensione(Recensione recensione) {
        if (recensione == null) return;

        // Rimuovi eventuale recensione precedente dello stesso utente
        recensioni.removeIf(r -> r.getUsername().equals(recensione.getUsername()));
        recensioni.add(recensione);
    }

    public boolean rimuoviRecensione(String username) {
        if (username == null) return false;
        return recensioni.removeIf(r -> r.getUsername().equals(username));
    }

    public Recensione getRecensione(String username) {
        if (username == null) return null;

        return recensioni.stream()
                .filter(r -> r.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public double getMediaStelle() {
        if (recensioni.isEmpty()) return 0.0;
        return recensioni.stream()
                .mapToInt(Recensione::getStelle)
                .average()
                .orElse(0.0);
    }

    public int getNumeroRecensioni() {
        return recensioni.size();
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome != null ? nome : ""; }

    public String getNazione() { return nazione; }
    public void setNazione(String nazione) { this.nazione = nazione != null ? nazione : ""; }

    public String getCitta() { return citta; }
    public void setCitta(String citta) { this.citta = citta != null ? citta : ""; }

    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo != null ? indirizzo : ""; }

    public double getLatitudine() { return latitudine; }
    public void setLatitudine(double latitudine) { this.latitudine = latitudine; }

    public double getLongitudine() { return longitudine; }
    public void setLongitudine(double longitudine) { this.longitudine = longitudine; }

    public double getFasciaPrezzo() { return fasciaPrezzo; }
    public void setFasciaPrezzo(double fasciaPrezzo) { this.fasciaPrezzo = fasciaPrezzo; }

    public boolean isDelivery() { return delivery; }
    public void setDelivery(boolean delivery) { this.delivery = delivery; }

    public boolean isPrenotazioneOnline() { return prenotazioneOnline; }
    public void setPrenotazioneOnline(boolean prenotazioneOnline) { this.prenotazioneOnline = prenotazioneOnline; }

    public String getTipoCucina() { return tipoCucina; }
    public void setTipoCucina(String tipoCucina) { this.tipoCucina = tipoCucina != null ? tipoCucina : ""; }

    public String getProprietario() { return proprietario; }
    public void setProprietario(String proprietario) { this.proprietario = proprietario; }

    public List<Recensione> getRecensioni() { return new ArrayList<>(recensioni); }
    public void setRecensioni(List<Recensione> recensioni) {
        this.recensioni = recensioni != null ? new ArrayList<>(recensioni) : new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("%s|%s|%s|%s|%s|%.6f|%.6f|%.2f|%s|%s|%s|%s",
                id, nome, nazione, citta, indirizzo, latitudine, longitudine,
                fasciaPrezzo, delivery, prenotazioneOnline, tipoCucina,
                proprietario != null ? proprietario : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ristorante that = (Ristorante) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
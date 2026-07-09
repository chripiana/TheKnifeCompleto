package project.demo;

public class Ristorante {
    private final String nome;
    private final String citta;
    private final String nazione;
    private final String indirizzo;
    private final double latitudine;
    private final double longitudine;
    private final String fasciaPrezzo; // e.g. "€€ — Medio (~45€)"
    private final String tipoCucina;
    private final boolean delivery;
    private final boolean prenotazioneOnline;

    public Ristorante(String nome, String citta, String nazione, String indirizzo, double latitudine, double longitudine, String fasciaPrezzo, String tipoCucina, boolean delivery, boolean prenotazioneOnline) {
        this.nome = nome;
        this.citta = citta;
        this.nazione = nazione;
        this.indirizzo = indirizzo;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.fasciaPrezzo = fasciaPrezzo;
        this.tipoCucina = tipoCucina;
        this.delivery = delivery;
        this.prenotazioneOnline = prenotazioneOnline;
    }

    // Getter e Setter standard...
    public String getNome() { return nome; }
    public String getCitta() { return citta; }
    public String getTipoCucina() { return tipoCucina; }
    public boolean isDelivery() { return delivery; }
    public boolean isPrenotazioneOnline() { return prenotazioneOnline; }
}
package project.shared.dto;

public class RestaurantSummary {
    private final String id;
    private final String nome;
    private final String citta;
    private final double mediaStelle;
    private final int numRecensioni;

    public RestaurantSummary(String id, String nome, String citta, double mediaStelle, int numRecensioni) {
        this.id = id;
        this.nome = nome;
        this.citta = citta;
        this.mediaStelle = mediaStelle;
        this.numRecensioni = numRecensioni;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCitta() {
        return citta;
    }

    public double getMediaStelle() {
        return mediaStelle;
    }

    public int getNumRecensioni() {
        return numRecensioni;
    }
}

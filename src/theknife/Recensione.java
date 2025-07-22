package theknife;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Recensione {
    private String username;
    private int stelle;
    private String testo;
    private LocalDate data;
    private String risposta;

    public Recensione(String username, int stelle, String testo, LocalDate data) {
        this.username = username;
        this.stelle = stelle;
        this.testo = testo;
        this.data = data;
    }

    // Getters e Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getStelle() { return stelle; }
    public void setStelle(int stelle) { this.stelle = stelle; }

    public String getTesto() { return testo; }
    public void setTesto(String testo) { this.testo = testo; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getRisposta() { return risposta; }
    public void setRisposta(String risposta) { this.risposta = risposta; }

    @Override
    public String toString() {
        String dataStr = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return String.format("%s|%d|%s|%s|%s",
                username, stelle, testo, dataStr, risposta != null ? risposta : "");
    }

    public static Recensione fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 4) return null;

        try {
            LocalDate data = LocalDate.parse(parts[3], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Recensione recensione = new Recensione(parts[0], Integer.parseInt(parts[1]), parts[2], data);

            if (parts.length > 4 && !parts[4].isEmpty()) {
                recensione.setRisposta(parts[4]);
            }

            return recensione;
        } catch (Exception e) {
            return null;
        }
    }
}
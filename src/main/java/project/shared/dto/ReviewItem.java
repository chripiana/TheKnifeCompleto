package project.shared.dto;

public class ReviewItem {
    private final int idRecensione;
    private final int stelle;
    private final String testo;
    private final String data;
    private final String autoreNome;
    private final String autoreCognome;
    private final boolean giaRisposto;
    private final String risposta;
    private final String dataRisposta;

    public ReviewItem(int idRecensione, int stelle, String testo, String data, String autoreNome, String autoreCognome,
                     boolean giaRisposto, String risposta, String dataRisposta) {
        this.idRecensione = idRecensione;
        this.stelle = stelle;
        this.testo = testo;
        this.data = data;
        this.autoreNome = autoreNome;
        this.autoreCognome = autoreCognome;
        this.giaRisposto = giaRisposto;
        this.risposta = risposta;
        this.dataRisposta = dataRisposta;
    }

    public int getIdRecensione() { return idRecensione; }
    public int getStelle() { return stelle; }
    public String getTesto() { return testo; }
    public String getData() { return data; }
    public String getAutoreNome() { return autoreNome; }
    public String getAutoreCognome() { return autoreCognome; }
    public boolean isGiaRisposto() { return giaRisposto; }
    public String getRisposta() { return risposta; }
    public String getDataRisposta() { return dataRisposta; }
}

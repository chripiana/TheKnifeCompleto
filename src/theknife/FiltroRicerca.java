package theknife;

public class FiltroRicerca {
    private String luogo;
    private String tipoCucina;
    private Double prezzoMin;
    private Double prezzoMax;
    private Boolean delivery;
    private Boolean prenotazioneOnline;
    private Double mediaMinStelle;

    public FiltroRicerca() {}

    // Getters e Setters
    public String getLuogo() { return luogo; }
    public void setLuogo(String luogo) { this.luogo = luogo; }

    public String getTipoCucina() { return tipoCucina; }
    public void setTipoCucina(String tipoCucina) { this.tipoCucina = tipoCucina; }

    public Double getPrezzoMin() { return prezzoMin; }
    public void setPrezzoMin(Double prezzoMin) { this.prezzoMin = prezzoMin; }

    public Double getPrezzoMax() { return prezzoMax; }
    public void setPrezzoMax(Double prezzoMax) { this.prezzoMax = prezzoMax; }

    public Boolean getDelivery() { return delivery; }
    public void setDelivery(Boolean delivery) { this.delivery = delivery; }

    public Boolean getPrenotazioneOnline() { return prenotazioneOnline; }
    public void setPrenotazioneOnline(Boolean prenotazioneOnline) { this.prenotazioneOnline = prenotazioneOnline; }

    public Double getMediaMinStelle() { return mediaMinStelle; }
    public void setMediaMinStelle(Double mediaMinStelle) { this.mediaMinStelle = mediaMinStelle; }
}
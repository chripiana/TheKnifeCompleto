package theknife;

public class UtenteCorrente {
    private Utente utente;

    public UtenteCorrente() {
        this.utente = null;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public Utente getUtente() {
        return utente;
    }

    public boolean isLoggato() {
        return utente != null;
    }

    public void logout() {
        this.utente = null;
    }
}
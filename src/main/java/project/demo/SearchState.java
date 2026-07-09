package project.demo;

public class SearchState {
    // La città è obbligatoria nel tuo metodo DAO cercaRistorante
    public static String citta = "Varese";
    public static String tipoCucina = null;
    public static Integer prezzoMin = null;
    public static Integer prezzoMax = null;
    public static Boolean delivery = null;
    public static Boolean prenotazioneOnline = null;
    public static Double minStelle = null;

    /**
     * Ripristina tutti i filtri allo stato iniziale (null) prima di una nuova ricerca.
     */
    public static void reset() {
        citta = "Varese"; // Imposta una città di default a tua scelta
        tipoCucina = null;
        prezzoMin = null;
        prezzoMax = null;
        delivery = null;
        prenotazioneOnline = null;
        minStelle = null;
    }
}
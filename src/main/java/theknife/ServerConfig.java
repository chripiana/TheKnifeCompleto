/*
 * TheKnife - Progetto JavaFX/DB per il modulo client/server.
 * Autore: Nome Cognome, Matricola 000000, sede VA
 */
package theknife;

/**
 * Rappresenta la configurazione di avvio del modulo server di TheKnife.
 *
 * @author Nome Cognome, Matricola 000000, sede VA
 */
public record ServerConfig(String host, int port, String databaseName, String user, String password,
        boolean helpRequested) {

    /**
     * Crea una configurazione leggendo gli argomenti di avvio.
     *
     * @param args argomenti da riga di comando
     * @return configurazione elaborata
     */
    public static ServerConfig fromArguments(String[] args) {
        String host = "localhost";
        int port = 5432;
        String databaseName = "theknife";
        String user = "postgres";
        String password = System.getenv().getOrDefault("THEKNIFE_DB_PASSWORD", "");
        boolean helpRequested = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host" -> host = args[++i];
                case "--port" -> port = Integer.parseInt(args[++i]);
                case "--db" -> databaseName = args[++i];
                case "--user" -> user = args[++i];
                case "--password" -> password = args[++i];
                case "--help", "-h" -> helpRequested = true;
                default -> throw new IllegalArgumentException("Argomento sconosciuto: " + args[i]);
            }
        }

        return new ServerConfig(host, port, databaseName, user, password, helpRequested);
    }
}

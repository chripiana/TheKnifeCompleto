package theknife;

import db.DatabaseManager;
import project.server.TheKnifeServer;
import project.shared.models.ServerConnection;

/**
 * Non-GUI server entry point (headless only)
 * Avoids loading JavaFX for command-line deployment
 */
public class NonGuiServerMain {
    public static void main(String[] args) {
        try {
            ServerConfig config = ServerConfig.fromArguments(args);
            if (config.helpRequested()) {
                System.out.println(
                        "Uso: java -jar serverTK.jar [--host localhost] [--port 5432] [--db theknife] [--user postgres] [--password <db_password>]\n");
                return;
            }

            ServerConnection serverConnection = new ServerConnection(
                    config.host(),
                    8080,
                    config.port(),
                    config.databaseName(),
                    config.user(),
                    config.password()
            );

            System.out.println("[serverTK] Avvio server TCP sulla porta " + serverConnection.getPort() +
                    " e connessione DB " + config.databaseName() + " su " + config.host() + ":" + config.port());

            TheKnifeServer server = new TheKnifeServer(serverConnection.getPort(), serverConnection);
            server.start();
        } catch (Exception ex) {
            System.err.println("[serverTK] Errore di avvio: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }
}

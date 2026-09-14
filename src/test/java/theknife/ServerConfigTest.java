/*
 * TheKnife - Progetto JavaFX/DB per il modulo client/server.
 * Autore: Nome Cognome, Matricola 000000, sede VA
 */
package theknife;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerConfigTest {

    @Test
    void parsesHostAndDatabaseFromArguments() {
        ServerConfig config = ServerConfig
                .fromArguments(new String[] { "--host", "db.local", "--db", "theknife_test", "--port", "5433" });

        assertEquals("db.local", config.host());
        assertEquals("theknife_test", config.databaseName());
        assertEquals(5433, config.port());
    }

    @Test
    void reportsHelpWhenRequested() {
        ServerConfig config = ServerConfig.fromArguments(new String[] { "--help" });

        assertTrue(config.helpRequested());
    }
}

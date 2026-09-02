package project.client.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import project.shared.models.ServerConnection;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per verifica della configurazione client
 */
public class ServerConnectionConfigTest {

    @BeforeEach
    public void setUp() {
        String configFile = System.getProperty("user.home") + "/.theknife/server-config.properties";
        File f = new File(configFile);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void testInitializeDefaultConfig() {
        ServerConnectionConfig.initialize();
        ServerConnection config = ServerConnectionConfig.getInstance();
        
        assertNotNull(config);
        assertEquals("localhost", config.getHost());
        assertEquals(8080, config.getPort());
        assertEquals(5432, config.getDbPort());
    }

    @Test
    public void testSaveAndLoadConfig() {
        ServerConnection config = new ServerConnection("192.168.1.1", 9090, 5433, "test_db", "admin", "pass123");
        ServerConnectionConfig.saveConfiguration(config);

        ServerConnectionConfig.initialize();
        ServerConnection loaded = ServerConnectionConfig.getInstance();

        assertEquals("192.168.1.1", loaded.getHost());
        assertEquals(9090, loaded.getPort());
        assertEquals(5432, loaded.getDbPort());
        assertEquals("theknife", loaded.getDatabaseName());
        assertEquals("postgres", loaded.getUser());
    }

    @Test
    public void testConfigFileExists() {
        ServerConnectionConfig.initialize();
        String configFile = System.getProperty("user.home") + "/.theknife/server-config.properties";
        assertTrue(Files.exists(Paths.get(configFile)), "File configurazione non trovato");
    }
}

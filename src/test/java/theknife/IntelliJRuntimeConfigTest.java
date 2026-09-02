package theknife;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IntelliJRuntimeConfigTest {

    @Test
    void moduleInfoExposesClientJavaFxController() throws IOException {
        String moduleInfo = Files.readString(Path.of("src/main/java/module-info.java"));
        assertTrue(moduleInfo.contains("opens project.client.controllers to javafx.fxml;"),
                "Il controller JavaFX del client deve essere visibile al modulo JavaFX.");
    }

    @Test
    void intellijRunConfigsUseTheCorrectModules() throws IOException {
        String serverConfig = Files.readString(Path.of(".run/ServerMain.run.xml"));
        String clientConfig = Files.readString(Path.of(".run/ClientMain.run.xml"));

        assertTrue(serverConfig.contains("theknife.NonGuiServerMain") || serverConfig.contains("theknife.ServerMain"),
                "Il run config del server deve avviare il server TCP headless o la GUI.");
        assertTrue(serverConfig.contains("theknife.app"),
                "Il run config del server deve usare il modulo corretto.");
        assertTrue(clientConfig.contains("theknife.app"),
                "Il run config del client deve usare il modulo corretto.");
    }
}

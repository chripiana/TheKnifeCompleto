package project.demo;

import javafx.application.Application;
import javafx.stage.Stage;

public class TheKnifeApp extends Application {
    @Override
    public void start(Stage stage) {
        // Inizializza il navigatore
        Navigator.getInstance().setStage(stage);

        // Carica la prima pagina (la Home pubblica)
        Navigator.getInstance().navigateTo("NotLoggedUser/home-view.fxml", "Trova il tuo ristorante");
    }

    public static void main(String[] args) {
        launch();
    }
}
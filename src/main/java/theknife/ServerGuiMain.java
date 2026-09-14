package theknife;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Server entry point with GUI (JavaFX Dashboard)
 * Allows monitoring and control of the server from a graphical interface
 */
public class ServerGuiMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/server/server-dashboard-view.fxml"));
        Scene scene = new Scene(loader.load());
        
        stage.setTitle("TheKnife Server Dashboard");
        stage.setScene(scene);
        stage.setWidth(1180);
        stage.setHeight(820);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

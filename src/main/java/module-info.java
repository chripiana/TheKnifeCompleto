module theknife.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires transitive eu.hansolo.toolbox;

    requires java.sql;
    requires java.net.http;
    requires com.zaxxer.hikari;

    opens project.controllers to javafx.fxml;
    opens project.client.controllers to javafx.fxml;
    opens project.server to javafx.fxml;
    opens theknife to javafx.graphics, javafx.fxml;

    exports theknife;
    exports project.controllers;
    exports project.server;
}

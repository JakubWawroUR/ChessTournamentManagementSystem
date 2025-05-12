module ChessProject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jdk.compiler;

    opens src.model to javafx.base;
    exports src.model;

    opens src to javafx.fxml;
    opens src.auth to javafx.fxml;
    opens src.Connection to javafx.fxml;
    opens src.Admin to javafx.fxml;
    opens src.Player to javafx.fxml;
    exports src;
    exports src.Connection;
    exports src.auth;
    exports src.Admin;
    exports src.Player;
}
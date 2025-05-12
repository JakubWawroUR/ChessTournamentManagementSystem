module ChessProject {
    requires javafx.controls;
    requires javafx.fxml;

    opens src to javafx.graphics;  // Ważne: pozwala JavaFX na dostęp do klas w pakiecie src
}
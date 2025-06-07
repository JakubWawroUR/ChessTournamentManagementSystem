module ChessProject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jdk.compiler; // To jest rzadko potrzebne w aplikacji klienckiej, możesz rozważyć usunięcie, jeśli nie kompilujesz kodu dynamicznie.

    // === WAŻNE ZMIANY ===

    // Otwórz pakiet src.model dla javafx.base, aby PropertyValueFactory mogło działać
    opens src.model to javafx.base; // <--- TEGO BRAKUJE I TO JEST PRZYCZYNA BŁĘDU
    exports src.model; // Eksportuj również pakiet modelu, jeśli jest używany poza modułem (dobra praktyka)

    // === Twoje istniejące dyrektywy ===
    opens src to javafx.fxml; // Zazwyczaj nie otwiera się całego 'src', tylko konkretne pakiety
    opens src.auth to javafx.fxml;
    opens src.Connection to javafx.fxml;
    opens src.Admin to javafx.fxml;
    opens src.Player to javafx.fxml;
    exports src;
    exports src.Connection; // Powinien być eksportowany, jeśli JDBC jest w nim
    exports src.auth;
    exports src.Admin;
    exports src.Player;
}
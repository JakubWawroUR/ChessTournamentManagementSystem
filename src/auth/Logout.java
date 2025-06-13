package src.auth;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.fxml.FXML; // Dodaj ten import
import java.io.IOException;

// Klasa abstrakcyjna lub po prostu klasa bazowa dla kontrolerów, które mają funkcję wylogowania
public class Logout { // Zmieniono z 'public abstract class Logout'
    // Metoda obsługująca wylogowanie, wywoływana z FXML (np. onMouseClicked)
    @FXML // Potrzebne, jeśli ta metoda ma być bezpośrednio wywoływana z FXML przez klasy dziedziczące
    public void logout(MouseEvent event) throws IOException {
        System.out.println("Przycisk 'Wyloguj' kliknięty (z abstrakcyjnej klasy Logout).");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/auth/LoginController.fxml")); // Upewnij się, że to jest poprawna ścieżka do Twojej głównej sceny logowania
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Logowanie do Systemu Turniejowego"); // Ustaw tytuł okna
        stage.show();
    }
}
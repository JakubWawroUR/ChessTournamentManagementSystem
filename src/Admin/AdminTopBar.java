package src.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Nadal potrzebne, jeśli inne metody AdminTopBar z tego korzystają
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene; // Nadal potrzebne
import javafx.scene.control.Alert; // Nadal potrzebne
import javafx.scene.control.ButtonType; // Nadal potrzebne
import javafx.scene.input.MouseEvent; // WAŻNE: Dodaj ten import
import javafx.scene.text.Text;
import javafx.stage.Stage;

import src.auth.Logout; // WAŻNE: Importuj klasę Logout

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// AdminTopBar dziedziczy z Logout
public class AdminTopBar extends Logout implements Initializable { // Zmieniono: extends Logout

    @FXML
    private Text headerText;

    // Pole primaryStage nie jest już absolutnie konieczne dla samej metody logout()
    // ponieważ dziedziczona metoda Logout.logout() pobiera Stage z event.getSource().
    // Jednak jeśli AdminTopBar ma inne metody, które potrzebują primaryStage (np. do zmiany tytułu głównego okna),
    // możesz je zostawić i przekazać.
    private Stage primaryStage;

    // Setter dla głównego Stage, jeśli jest używany w innych metodach AdminTopBar
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // ... (Twój istniejący kod initialize) ...
    }

    /**
     * Metoda do ustawiania tytułu na topbarze.
     */
    public void setTitle(String title) {
        if (headerText != null) {
            headerText.setText(title);
        }
    }

    // WAŻNE: Metoda logout() JUŻ NIE MUSI być zdefiniowana TUTAJ,
    // jeśli ma dokładnie taką samą sygnaturę i adnotację @FXML jak w Logout.java.
    // FXMLLoader automatycznie znajdzie ją w klasie bazowej.
    // Jeśli chcesz nadpisać jej zachowanie, możesz to zrobić tutaj.
    // Ale skoro celem jest centralizacja, to po prostu polegasz na dziedziczeniu.

    // Jeśli zdecydujesz się nie nadpisywać, możesz usunąć całą metodę logout() z AdminTopBar.java!
    /*
    @FXML
    @Override // Dodanie @Override jest dobrą praktyką, jeśli nadpisujesz
    public void logout(MouseEvent event) {
        // Jeśli chcesz dodać specyficzną logikę dla AdminTopBar PRZED lub PO wylogowaniem,
        // możesz wywołać super.logout(event);
        System.out.println("AdminTopBar: Dodatkowa logika przed wylogowaniem...");
        super.logout(event); // Wywołanie metody wylogowania z klasy bazowej Logout
        System.out.println("AdminTopBar: Dodatkowa logika po wylogowaniu (jeśli kod tutaj zostanie osiągnięty).");
    }
    */

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
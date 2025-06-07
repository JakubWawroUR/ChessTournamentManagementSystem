package src.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import src.dao.UserDAO;
import src.model.Role;
import src.model.User; // Potrzebny import dla klasy User

import java.io.IOException;
import java.sql.SQLException; // Potrzebny import dla SQLException

public class RegisterController implements SceneSwitcher {
    @FXML
    private ToggleButton RegisterButton; // Zazwyczaj używa się Button, ale ToggleButton też może być
    @FXML
    private TextField firstname;
    @FXML
    private TextField lastname;
    @FXML
    private TextField login;
    @FXML
    private PasswordField password1;
    @FXML
    private PasswordField password2;

    private UserDAO userDAO = new UserDAO(); // Deklaracja i inicjalizacja UserDAO

    @FXML
    private void handleRegister(ActionEvent event) throws IOException {
        // Sprawdzanie, czy wszystkie pola są wypełnione
        if (login.getText().isEmpty() || password1.getText().isEmpty() ||
                password2.getText().isEmpty() || firstname.getText().isEmpty() ||
                lastname.getText().isEmpty()) {
            System.out.println("Wszystkie pola muszą być wypełnione!");
            showAlert("Błąd rejestracji", "Wszystkie pola są wymagane!"); // Użycie metody showAlert
            return;
        }

        // Sprawdzanie zgodności haseł
        if (!password1.getText().equals(password2.getText())) {
            System.out.println("Hasła nie są identyczne!");
            showAlert("Błąd rejestracji", "Hasła nie są identyczne!"); // Użycie metody showAlert
            return;
        }

        // Zmieniono logikę rejestracji, aby używać registerUser z UserDAO
        try {
            // Metoda registerUser przyjmuje login, hasło, imię, nazwisko i rolę.
            // Rola jest domyślnie ustawiona na GRACZ, jak wskazuje Twój kod.
            boolean success = userDAO.registerUser(
                    login.getText(),
                    password1.getText(),
                    firstname.getText(),
                    lastname.getText(),
                    Role.GRACZ // Domyślna rola to GRACZ
            );

            if (success) {
                System.out.println("Użytkownik zarejestrowany pomyślnie!");
                showAlert("Rejestracja udana", "Konto zostało pomyślnie utworzone!"); // Użycie metody showAlert
                switchScene(event); // Przełącz na ekran logowania
            } else {
                System.out.println("Błąd: Użytkownik o podanym loginie już istnieje.");
                showAlert("Rejestracja nieudana", "Użytkownik o podanym loginie już istnieje."); // Użycie metody showAlert
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Wypisz stos wywołań w konsoli (do debugowania)
            System.out.println("Błąd podczas rejestracji użytkownika: " + e.getMessage());
            showAlert("Błąd bazy danych", "Wystąpił błąd podczas rejestracji użytkownika: " + e.getMessage()); // Użycie metody showAlert
        }
    }

    // Dodano prostą metodę do wyświetlania alertów zamiast System.out.println
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void switchScene(ActionEvent event) throws IOException {
        System.out.println("Przełączanie sceny...");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml")); // main.fxml to zakładam ekran logowania
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
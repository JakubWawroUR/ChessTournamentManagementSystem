package src.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import src.dao.UserDAO;
import src.model.Role;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterController {
    @FXML
    private ToggleButton RegisterButton;
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

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleRegister(ActionEvent event) throws IOException {
        if (login.getText().isEmpty() || password1.getText().isEmpty() ||
                password2.getText().isEmpty() || firstname.getText().isEmpty() ||
                lastname.getText().isEmpty()) {
            showAlert("Błąd rejestracji", "Wszystkie pola są wymagane!");
            return;
        }
        if (!password1.getText().equals(password2.getText())) {
            showAlert("Błąd rejestracji", "Hasła nie są identyczne!");
            return;
        }
        try {
            boolean success = userDAO.registerUser(
                    login.getText(),
                    password1.getText(),
                    firstname.getText(),
                    lastname.getText(),
                    Role.GRACZ
            );

            if (success) {
                showAlert("Rejestracja udana", "Konto zostało pomyślnie utworzone!");
                switchScene(event);
            } else {
                showAlert("Rejestracja nieudana", "Użytkownik o podanym loginie już istnieje.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Błąd bazy danych", "Wystąpił błąd podczas rejestracji użytkownika: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public void switchScene(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginController.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
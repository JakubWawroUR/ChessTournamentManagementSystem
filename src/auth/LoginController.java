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
import javafx.scene.control.ToggleButton; // Dodany import dla ToggleButton
import javafx.stage.Stage;
import src.Player.PlayerDashboard;
import src.Admin.AdminDashboard;
import src.dao.UserDAO;
import src.model.Role;
import src.model.User;
import src.model.Player;
import src.model.Admin;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController implements SceneSwitcher {
    @FXML
    private TextField loginField; // NAZWA POLA ZGODNA Z fx:id W FXML
    @FXML
    private PasswordField passwordField;
    @FXML
    private ToggleButton registerButton; // Dodane pole dla ToggleButton

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLoginButtonAction(ActionEvent event) throws IOException {
        String login = loginField.getText(); // Tutaj już nie powinno być NullPointerException
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd logowania", "Proszę wprowadzić login i hasło.");
            return;
        }

        try {
            User loggedUser = userDAO.loginUser(login, password);

            if (loggedUser != null) {
                if (loggedUser.getRole() == Role.GRACZ) {
                    Player loggedPlayer = (Player) loggedUser; // Rzutowanie jest poprawne, jak już wcześniej ustaliliśmy
                    System.out.println("Zalogowano jako Gracz: " + loggedPlayer.getLogin() + " (ID: " + loggedPlayer.getId() + ", PlayersTableId: " + loggedPlayer.getPlayersTableId() + ")");

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Player/PlayerDashboard.fxml"));
                    Parent playerDashboardParent = loader.load();
                    PlayerDashboard playerDashboardController = loader.getController();
                    playerDashboardController.initData(loggedPlayer);

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(playerDashboardParent));
                    stage.show();

                } else if (loggedUser.getRole() == Role.ADMINISTRATOR) {
                    System.out.println("Zalogowano jako Administrator: " + loggedUser.getLogin());

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Admin/AdminDashboard.fxml"));
                    Parent adminDashboardParent = loader.load();

                    AdminDashboard adminDashboard = loader.getController(); // To powinno być OK, jeśli fx:controller jest w FXML
                    Admin loggedAdmin = (Admin) loggedUser; // Rzutowanie jest poprawne
                    adminDashboard.initData(loggedAdmin);

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(adminDashboardParent));
                    stage.show();

                } else {
                    showAlert(Alert.AlertType.ERROR, "Błąd roli", "Nieznana rola użytkownika.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Błąd logowania", "Nieprawidłowy login lub hasło.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas logowania: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Dodana metoda dla przycisku "Zarejestruj"
    @FXML
    private void handleRegisterButtonAction(ActionEvent event) throws IOException {
        System.out.println("Przycisk Zarejestruj naciśnięty!");

         Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
         FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterController.fxml")); // Zmień na ścieżkę do pliku FXML rejestracji
         Parent registerParent = loader.load();
         stage.setScene(new Scene(registerParent));
         stage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void switchScene(ActionEvent event) throws IOException {
        // Implementacja przełączania scen
    }
}
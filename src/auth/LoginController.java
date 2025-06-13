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
import src.Player.PlayerDashboard;
import src.Admin.AdminDashboard; // Nadal potrzebne, bo ładujemy ten FXML
import src.dao.UserDAO;
import src.model.Role;
import src.model.User;
import src.model.Player;
import src.model.Admin;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController implements SceneSwitcher {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ToggleButton registerButton;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLoginButtonAction(ActionEvent event) throws IOException {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd logowania", "Proszę wprowadzić login i hasło.");
            return;
        }

        try {
            User loggedUser = userDAO.loginUser(login, password);

            if (loggedUser != null) {
                if (loggedUser.getRole() == Role.GRACZ) {
                    Player loggedPlayer = (Player) loggedUser;
                    System.out.println("Zalogowano jako Gracz: " + loggedPlayer.getLogin() + " (ID: " + loggedPlayer.getId() + ", PlayersTableId: " + loggedPlayer.getPlayersTableId() + ")");

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Player/PlayerDashboard.fxml"));
                    Parent playerDashboardParent = loader.load();

                    PlayerDashboard playerDashboardController = loader.getController();

                    if (playerDashboardController != null) {
                        playerDashboardController.setPlayer(loggedPlayer);
                        System.out.println("LoginController: PlayerDashboardController pobrany i dane gracza przekazane.");
                    } else {
                        System.err.println("LoginController: Błąd! PlayerDashboardController jest NULL po załadowaniu FXML. Sprawdź fx:controller w PlayerDashboard.fxml!");
                        showAlert(Alert.AlertType.ERROR, "Błąd aplikacji", "Nie można załadować panelu gracza. Skontaktuj się z administratorem.");
                        return;
                    }

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(playerDashboardParent));
                    stage.show();
                    System.out.println("LoginController: Przełączono na PlayerDashboard.");

                } else if (loggedUser.getRole() == Role.ADMINISTRATOR) {
                    System.out.println("Zalogowano jako Administrator: " + loggedUser.getLogin());

                    // *** SPRAWDŹ TUTAJ! Upewnij się, że ładujesz AdminDashboard.fxml ***
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Admin/AdminDashboard.fxml"));
                    Parent adminDashboardParent = loader.load(); // Tu ładujemy dashboard!

                    AdminDashboard adminDashboardController = loader.getController();

                    if (adminDashboardController != null) {
                        adminDashboardController.setAdmin((Admin) loggedUser); // Przekazujemy dane admina do dashboardu
                        System.out.println("LoginController: AdminDashboardController pobrany i dane administratora przekazane.");
                    } else {
                        System.err.println("LoginController: Błąd! AdminDashboard jest NULL po załadowaniu FXML. Sprawdź fx:controller w AdminDashboard.fxml!");
                        showAlert(Alert.AlertType.ERROR, "Błąd aplikacji", "Nie można załadować panelu administratora. Skontaktuj się z administratorem.");
                        return;
                    }

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(adminDashboardParent)); // Ustawiamy scenę z dashboardem
                    stage.show();
                    System.out.println("LoginController: Przełączono na AdminDashboard.");

                } else {
                    showAlert(Alert.AlertType.ERROR, "Błąd roli", "Nieznana rola użytkownika.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Błąd logowania", "Nieprawidłowy login lub hasło.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas logowania: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd ładowania widoku", "Nie można załadować widoku panelu. " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegisterButtonAction(ActionEvent event) throws IOException {
        System.out.println("Przycisk Zarejestruj naciśnięty!");

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterController.fxml"));
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
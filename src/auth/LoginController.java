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
import src.Admin.AdminDashboard; // Upewnij się, że AdminDashboard jest poprawnie zaimportowany i istnieje
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

                    // *** USUNIĘTO PROBLEMOWĄ LINIĘ: loader.setController(playerDashboardController); ***
                    // Pozostawiamy loader.load(), który sam stworzy kontroler zdefiniowany w FXML
                    Parent playerDashboardParent = loader.load(); // FXML jest ładowany, initialize() jest wywoływane tutaj

                    // *** POBIERZ KONTROLER PO ZAŁADOWANIU FXML ***
                    PlayerDashboard playerDashboardController = loader.getController();

                    // *** Ustaw dane gracza PO załadowaniu i inicjalizacji kontrolera ***
                    // Tutaj controller PlayerDashboard jest już zainicjalizowany przez FXMLLoader
                    if (playerDashboardController != null) {
                        playerDashboardController.setPlayer(loggedPlayer); // Użyj nowej metody setPlayer
                        System.out.println("LoginController: PlayerDashboardController pobrany i dane gracza przekazane.");
                    } else {
                        System.err.println("LoginController: Błąd! PlayerDashboardController jest NULL po załadowaniu FXML. Sprawdź fx:controller w PlayerDashboard.fxml!");
                    }


                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(playerDashboardParent));
                    stage.show();
                    System.out.println("LoginController: Przełączono na PlayerDashboard.");

                } else if (loggedUser.getRole() == Role.ADMINISTRATOR) {
                    System.out.println("Zalogowano jako Administrator: " + loggedUser.getLogin());

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Admin/AdminDashboard.fxml"));
                    // DLA ADMIN DASHBOARD RÓWNIEŻ ZMIEŃ:
                    // Zamiast: AdminDashboard adminDashboard = new AdminDashboard(); loader.setController(adminDashboard);
                    Parent adminDashboardParent = loader.load(); // Niech FXML sam stworzy kontroler
                    AdminDashboard adminDashboard = loader.getController(); // Pobierz kontroler po załadowaniu

                    if (adminDashboard != null) {
                        adminDashboard.setAdmin((Admin) loggedUser); // Assuming you'd have a similar setAdmin method
                    } else {
                        System.err.println("LoginController: Błąd! AdminDashboard jest NULL po załadowaniu FXML. Sprawdź fx:controller w AdminDashboard.fxml!");
                    }


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
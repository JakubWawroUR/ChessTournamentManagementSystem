package src.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import src.model.Admin;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboard implements Initializable {
    @FXML
    private Label welcomeLabel;

    // *** DODANA LINIA ***
    // Pole odpowiadające załączonemu AdminTopBar.fxml z jego fx:id
    @FXML
    private AdminTopBar adminTopBarController;

    private Admin currentAdmin;

    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        System.out.println("AdminDashboard (setAdmin): Otrzymano dane administratora: " + admin.getLogin() + " (currentAdmin ustawiony na " + (this.currentAdmin != null ? "NIE-NULL" : "NULL") + ").");
        if (welcomeLabel != null) {
            welcomeLabel.setText("Witaj, " + admin.getLogin() + "!");
        }

        // *** Zapewnij, że adminTopBarController jest zainicjalizowany i przekaż mu tytuł ***
        if (adminTopBarController != null) {
            adminTopBarController.setTitle("Panel Administratora: " + admin.getLogin());
            // Jeśli AdminTopBar potrzebuje Stage, to tutaj go przekaż (choć metoda logout w TopBar już go pobiera z eventu)
            // adminTopBarController.setPrimaryStage((Stage) welcomeLabel.getScene().getWindow()); // Tylko jeśli to jest absolutnie konieczne dla innych funkcji w TopBar
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("AdminDashboard (initialize): Metoda initialize() rozpoczyna działanie.");
        if (welcomeLabel != null) {
            welcomeLabel.setText("Witaj, Administratorze!");
        }
        // Upewnij się, że adminTopBarController zostanie ustawiony przez FXMLLoader po załadowaniu FXML
        // i możesz tutaj ustawić np. początkowy tytuł, jeśli setAdmin nie zostanie od razu wywołane
        if (adminTopBarController != null) {
            adminTopBarController.setTitle("Panel Administratora"); // Domyślny tytuł przed zalogowaniem
        }
    }

    @FXML
    public void handleManageUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminUserController.fxml"));
            Parent root = loader.load();
            if (welcomeLabel.getScene() == null) {
                System.err.println("AdminDashboard: Błąd! Scena dla welcomeLabel jest null podczas handleManageUsers.");
                showAlert("Błąd", "Widok główny nie jest jeszcze w pełni załadowany.");
                return;
            }
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Panel Administratora: Użytkownicy");
        } catch (IOException e) {
            showAlert("Błąd", "Nie można załadować panelu użytkowników");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleManageTournaments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminTournamentController.fxml"));
            Parent root = loader.load();
            if (welcomeLabel.getScene() == null) {
                System.err.println("AdminDashboard: Błąd! Scena dla welcomeLabel jest null podczas handleManageTournaments.");
                showAlert("Błąd", "Widok główny nie jest jeszcze w pełni załadowany.");
                return;
            }
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Panel Administratora: Turnieje");
        } catch (IOException e) {
            showAlert("Błąd", "Nie można załadować panelu turniejów");
            e.printStackTrace();
        }
    }

    // *** USUNIĘTA METODA ***
    // handleLogout() z AdminDashboard.java jest zbędna, ponieważ wylogowanie jest obsługiwane przez AdminTopBar.
    // @FXML
    // public void handleLogout() {
    //     try {
    //         Parent root = FXMLLoader.load(getClass().getResource("/src/auth/LoginController.fxml"));
    //         if (welcomeLabel.getScene() == null) {
    //             System.err.println("AdminDashboard: Błąd! Scena dla welcomeLabel jest null podczas handleLogout.");
    //             showAlert("Błąd", "Wystąpił błąd podczas wylogowania.");
    //             return;
    //         }
    //         Stage stage = (Stage) welcomeLabel.getScene().getWindow();
    //         stage.setScene(new Scene(root));
    //         stage.setTitle("Logowanie");
    //     } catch (IOException e) {
    //         showAlert("Błąd", "Nie można wylogować");
    //         e.printStackTrace();
    //     }
    // }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
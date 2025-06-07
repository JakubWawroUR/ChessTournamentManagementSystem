package src.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import src.model.Admin; // WAŻNE: Dodaj import dla Admin

import java.io.IOException;

public class AdminDashboard {
    @FXML
    private Label welcomeLabel;

    private Admin adminUser; // Zmieniono z User na Admin, aby przechowywać pełne dane

    /**
     * Metoda wywoływana przez LoginController do przekazania danych zalogowanego administratora.
     * @param admin Obiekt Admina, który się zalogował.
     */
    public void initData(Admin admin) {
        this.adminUser = admin;
        if (admin != null) {
            welcomeLabel.setText("Witaj, Administratorze " + admin.getLogin() + "!");
            // Tutaj możesz załadować inne dane specyficzne dla admina na pulpicie
        }
    }

    @FXML
    public void handleManageUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminUserController.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            // Opcjonalnie: jeśli AdminUserController też potrzebuje danych Admina, możesz je przekazać
            // AdminUserController userController = loader.getController();
            // userController.initData(this.adminUser);
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
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            // Opcjonalnie: jeśli AdminTournamentController też potrzebuje danych Admina
            // AdminTournamentController tournamentController = loader.getController();
            // tournamentController.initData(this.adminUser);
        } catch (IOException e) {
            showAlert("Błąd", "Nie można załadować panelu turniejów");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        try {
            // Poprawiona ścieżka do pliku main.fxml (ekran logowania)
            Parent root = FXMLLoader.load(getClass().getResource("/src/auth/main.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Logowanie");
        } catch (IOException e) {
            showAlert("Błąd", "Nie można wylogować");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
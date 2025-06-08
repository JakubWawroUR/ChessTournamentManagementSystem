package src.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; // <--- DODANY IMPORT
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import src.model.Admin;

import java.io.IOException;
import java.net.URL;         // <--- DODANY IMPORT
import java.util.ResourceBundle; // <--- DODANY IMPORT

public class AdminDashboard implements Initializable { // <--- KLUCZOWA ZMIANA: Implementacja Initializable
    @FXML
    private Label welcomeLabel;

    private Admin currentAdmin;

    // Metoda ustawiająca obiekt Admin, wywoływana z LoginController
    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        System.out.println("AdminDashboard (setAdmin): Otrzymano dane administratora: " + admin.getLogin() + " (currentAdmin ustawiony na " + (this.currentAdmin != null ? "NIE-NULL" : "NULL") + ").");
        // Tutaj możesz dodać logikę aktualizacji UI po otrzymaniu admina, np.:
        if (welcomeLabel != null) {
            welcomeLabel.setText("Witaj, " + admin.getLogin() + "!");
        }
        // Możesz też tutaj załadować domyślny widok dla administratora
        // (np. wywołać metodę, która ładuje AdminUserController.fxml)
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("AdminDashboard (initialize): Metoda initialize() rozpoczyna działanie.");
        // W initialize() możesz sprawdzić, czy currentAdmin został już ustawiony przez setAdmin()
        // (choć z obecnym przepływem, setAdmin() jest wywoływane po initialize()).
        // Logika aktualizacji UI związana z currentAdmin powinna być głównie w setAdmin() lub w dedykowanej metodzie UI update.
        if (this.currentAdmin == null) {
            System.out.println("AdminDashboard (initialize): currentAdmin jest NULL na początku initialize(). Jest to oczekiwane, jeśli setAdmin() zostanie wywołane później.");
        } else {
            System.out.println("AdminDashboard (initialize): currentAdmin jest NIE-NULL na początku initialize(): " + currentAdmin.getLogin());
            // Jeśli jakimś cudem admin jest już tu, możesz zaktualizować UI
            if (welcomeLabel != null) {
                welcomeLabel.setText("Witaj, " + currentAdmin.getLogin() + "!");
            }
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
            // userController.initData(this.currentAdmin); // Zmieniono na currentAdmin
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
            // tournamentController.initData(this.currentAdmin); // Zmieniono na currentAdmin
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
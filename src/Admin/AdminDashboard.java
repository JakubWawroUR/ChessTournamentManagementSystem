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
import src.model.User;
import src.BaseDashboardController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboard extends BaseDashboardController {
    @FXML
    private AdminTopBar adminTopBarController;
    private Admin currentAdmin;

    @Override
    public void setLoggedInUser(User user) {
        if (user instanceof Admin) {
            this.currentAdmin = (Admin) user;
            super.setWelcomeMessage("Witaj, " + currentAdmin.getLogin() + "!");
            if (adminTopBarController != null) {
                adminTopBarController.setTitle("Panel Administratora: " + currentAdmin.getLogin());
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        if (adminTopBarController != null) {
            adminTopBarController.setTitle("Panel Administratora");
        }
    }
}
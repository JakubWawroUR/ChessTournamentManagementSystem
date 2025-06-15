package src.Player;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import src.auth.Logout;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PlayerTopBar extends Logout implements Initializable {

    @FXML private Label sceneTitleLabel;
    private PlayerDashboard playerDashboardController;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (sceneTitleLabel != null) {
            sceneTitleLabel.setText("Ładowanie...");
        }
    }

    public void setPlayerDashboardController(PlayerDashboard playerDashboardController) {
        this.playerDashboardController = playerDashboardController;
    }

    public void setSceneTitle(String title) {
        if (sceneTitleLabel != null) {
            sceneTitleLabel.setText(title);
        }
    }
    @FXML
    private void handleLogout(MouseEvent event) {
        if (playerDashboardController != null) {
            playerDashboardController.cleanup();
        }
        try {
            super.logout(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package src.Player;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class PlayerNavigation {
    @FXML private javafx.scene.text.Text profileText;
    @FXML private javafx.scene.text.Text tournamentsText;
    private PlayerDashboard playerDashboardController;
    public void setPlayerDashboardController(PlayerDashboard controller) {
        this.playerDashboardController = controller;
    }

    @FXML
    private void handleProfileClicked(MouseEvent event) {
        playerDashboardController.handleShowProfile();
    }

    @FXML
    private void handleTournamentsClicked(MouseEvent event) {
        playerDashboardController.handleShowTournaments();
    }
}
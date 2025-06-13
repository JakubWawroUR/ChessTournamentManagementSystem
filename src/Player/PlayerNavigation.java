package src.Player;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class PlayerNavigation {

    // ZMIENIONO TYP POL Z org.w3c.dom.Text NA javafx.scene.text.Text
    @FXML private javafx.scene.text.Text profileText; // Poprawny typ Text
    @FXML private javafx.scene.text.Text tournamentsText; // Poprawny typ Text

    private PlayerDashboard playerDashboardController;

    public void setPlayerDashboardController(PlayerDashboard controller) {
        this.playerDashboardController = controller;
        System.out.println("PlayerNavigationController: Referencja do PlayerDashboardController USTAWIENIA: " + (controller != null));
    }

    @FXML
    private void handleProfileClicked(MouseEvent event) {
        if (playerDashboardController == null) {
            System.err.println("PlayerNavigationController: playerDashboardController jest null. Nie można przełączyć widoków.");
            return;
        }
        System.out.println("Nawigacja: Kliknięto Profil. Ładowanie widoku profilu...");
        playerDashboardController.handleShowProfile();
    }

    @FXML
    private void handleTournamentsClicked(MouseEvent event) {
        if (playerDashboardController == null) {
            System.err.println("PlayerNavigationController: playerDashboardController jest null. Nie można przełączyć widoków.");
            return;
        }
        System.out.println("Nawigacja: Kliknięto Turnieje. Ładowanie widoku turniejów...");
        playerDashboardController.handleShowTournaments();
    }
}
package src.Player;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.Node; // Dodaj import
import javafx.stage.Stage; // Dodaj import

import java.io.IOException;

public class PlayerNavigationController {

    @FXML
    private Text usersText;
    @FXML
    private Text tournamentsText;

    // Dodaj pole na referencję do PlayerDashboardController
    private PlayerDashboard playerDashboardController;

    // Metoda do ustawienia referencji na kontroler nadrzędny
    public void setPlayerDashboardController(PlayerDashboard controller) {
        this.playerDashboardController = controller;
    }

    @FXML
    public void switchScene(MouseEvent event) throws IOException {
        System.out.println("dziala");
        Object source = event.getSource();

        if (playerDashboardController == null) {
            System.err.println("PlayerNavigationController: playerDashboardController is null. Cannot switch views.");
            // Możesz tutaj wyświetlić alert lub logować błąd
            return;
        }

        if (source == tournamentsText) {
            System.out.println("Przełączam na Turnieje (przez PlayerDashboard)");
            playerDashboardController.handleShowTournaments(); // Wywołujemy metodę z PlayerDashboard
        } else if (source == usersText) {
            System.out.println("Przełączam na Profil (przez PlayerDashboard)");
            // Tutaj będziesz musiał dodać metodę w PlayerDashboard, np. handleShowProfile()
            // playerDashboardController.handleShowProfile();
            System.out.println("Opcja 'Profil' nie jest jeszcze zaimplementowana w PlayerDashboard.");
        } else {
            System.out.println("Nieznany element nawigacji.");
        }
    }
}
// src/Player/PlayerTopBar.java
package src.Player;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent; // <--- Upewnij się, że masz ten import!
import src.auth.Logout; // <--- Upewnij się, że masz ten import i klasa istnieje

import java.io.IOException; // <--- Upewnij się, że masz ten import!
import java.net.URL;
import java.util.ResourceBundle;

// Klasa MUSI DZIEDZICZYĆ Z Logout, aby super.logout(event) działało
public class PlayerTopBar extends Logout implements Initializable { // <--- DODAJ "extends Logout"

    @FXML private Label sceneTitleLabel;

    private PlayerDashboard playerDashboardController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (sceneTitleLabel != null) {
            sceneTitleLabel.setText("Ładowanie...");
        }
        System.out.println("PlayerTopBar: initialize() finished.");
    }

    public void setPlayerDashboardController(PlayerDashboard playerDashboardController) {
        this.playerDashboardController = playerDashboardController;
        System.out.println("PlayerTopBar: PlayerDashboardController set.");
    }

    public void setSceneTitle(String title) {
        if (sceneTitleLabel != null) {
            sceneTitleLabel.setText(title);
        } else {
            System.err.println("PlayerTopBar: sceneTitleLabel is null! Cannot set scene title.");
        }
    }

    /**
     * Metoda obsługująca wylogowanie użytkownika.
     * MUSI PRZYJMOWAĆ MouseEvent, aby pasowała do onMouseClicked z FXML.
     * @param event Zdarzenie myszy, które wywołało akcję.
     */
    @FXML
    private void handleLogout(MouseEvent event) { // <--- ZMIEŃ SYGNATURĘ NA TĘ WERSJĘ!
        System.out.println("PlayerTopBar: Obsługa wylogowania rozpoczyna się...");

        if (playerDashboardController != null) {
            playerDashboardController.cleanup(); // Wywołaj metodę sprzątającą w Dashboardzie
            System.out.println("PlayerTopBar: Wywołano cleanup() w PlayerDashboard.");
        }

        try {
            // Wywołaj metodę logout z klasy bazowej (Logout)
            super.logout(event); // <--- DODAJ TO WYWOŁANIE
            System.out.println("PlayerTopBar: Wywołano super.logout(event). Przekierowanie powinno nastąpić.");
        } catch (IOException e) {
            System.err.println("PlayerTopBar: Błąd podczas wylogowywania (przekierowania): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
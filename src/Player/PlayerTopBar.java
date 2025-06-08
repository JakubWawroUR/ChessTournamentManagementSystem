package src.Player;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import src.auth.Logout; // Import dla klasy Logout
import java.io.IOException;

public class PlayerTopBar extends Logout { // Poprawne dziedziczenie
    @FXML
    private Text welcomeLabel; // ZMIEŃ TO: NAZWA MUSI PASOWAĆ DO FX:ID W FXML
    @FXML
    private Text rankingLabel; // ZMIEŃ TO: NAZWA MUSI PASOWAĆ DO FX:ID W FXML
    @FXML // Dodaj @FXML do przycisku wylogowania, jeśli jest to Button, a nie Text
    private Text logoutButton; // Przykład, jeśli masz Text jako przycisk wylogowania

    private PlayerDashboard playerDashboardController; // Referencja do nadrzędnego kontrolera

    /**
     * Ustawia referencję do nadrzędnego kontrolera PlayerDashboard.
     * Ta metoda jest wywoływana przez PlayerDashboard w jego metodzie initialize().
     * @param controller Instancja PlayerDashboard.
     */
    public void setPlayerDashboardController(PlayerDashboard controller) {
        this.playerDashboardController = controller;
        System.out.println("PlayerTopBar: Otrzymano referencję do PlayerDashboardController: " + (controller != null));
    }

    /**
     * Ustawia tekst powitalny wyświetlany na górnym pasku.
     * @param text Tekst do wyświetlenia.
     */
    public void setWelcomeText(String text) {
        if (welcomeLabel != null) { // TERAZ SPRAWDZAMY 'welcomeLabel'
            welcomeLabel.setText(text);
            System.out.println("PlayerTopBar: Ustawiono welcomeText: " + text); // Ten log może zostać, jest informacyjny
        } else {
            System.err.println("PlayerTopBar: welcomeLabel jest null! Nie można ustawić tekstu."); // Komunikat błędu też powinien wskazywać 'welcomeLabel'
        }
    }

    /**
     * Ustawia tekst rankingu wyświetlany na górnym pasku.
     * @param text Tekst do wyświetlenia.
     */
    public void setRankingText(String text) {
        if (rankingLabel != null) { // TERAZ SPRAWDZAMY 'rankingLabel'
            rankingLabel.setText(text);
            System.out.println("PlayerTopBar: Ustawiono rankingText: " + text); // Ten log może zostać
        } else {
            System.err.println("PlayerTopBar: rankingLabel jest null! Nie można ustawić tekstu."); // Komunikat błędu też powinien wskazywać 'rankingLabel'
        }
    }

    /**
     * Nadpisana metoda logout z klasy bazowej Logout.
     * Jest wywoływana, gdy użytkownik kliknie przycisk wylogowania w TopBarze.
     * Najpierw wywołuje czyszczenie zasobów w PlayerDashboard, a następnie przełącza scenę.
     * @param event Zdarzenie myszy.
     * @throws IOException Jeśli wystąpi błąd podczas ładowania FXML.
     */
    @Override
    @FXML // Ważne: FXML będzie wywoływał tę metodę!
    public void logout(MouseEvent event) throws IOException {
        // Najpierw poproś PlayerDashboard o posprzątanie (zatrzymanie timera)
        if (playerDashboardController != null) {
            System.out.println("PlayerTopBar: Wywołuję cleanup() na PlayerDashboard przed wylogowaniem.");
            playerDashboardController.cleanup();
        } else {
            System.err.println("PlayerTopBar: Brak referencji do PlayerDashboardController. Nie można wywołać cleanup().");
        }

        // Następnie wywołaj oryginalną logikę wylogowania z klasy bazowej Logout
        super.logout(event);
        System.out.println("PlayerTopBar: Wywołano super.logout() i przełączono scenę.");
    }
}
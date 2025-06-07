package src.Player;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import src.model.Player;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class PlayerDashboard implements Initializable {
    @FXML
    private Label welcomeLabel; // musi być w PlayerTopBar.fxml i mieć fx:id="welcomeLabel"

    @FXML
    private Label rankingLabel; // również z PlayerTopBar.fxml lub innym miejscem

    @FXML
    private AnchorPane contentPane; // kontener na dynamiczne widoki

    @FXML
    private VBox playerNavigationPanel; // zgodnie z root PlayerNavigationPanel.fxml - jeśli root jest VBox

    private Player currentPlayer;
    private Timeline debugTimer;

    public void initData(Player player) {
        this.currentPlayer = player;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Witaj, " + player.getFirstname() + " " + player.getLastname());
        } else {
            System.err.println("welcomeLabel is null in initData!");
        }

        if (rankingLabel != null) {
            rankingLabel.setText("Twój ranking: " + player.getRanking());
        }

        // --- ZMIANA TUTAJ: USUNIĘTO wywołanie handleShowTournaments() ---
        // Usunięto: handleShowTournaments();
        // contentPane jest już ustawiony w PlayerDashboard.fxml z tekstem "WITAJ".
        // Widoki będą ładowane dynamicznie po kliknięciu przycisków w PlayerNavigationPanel.

        startDebugTimer();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (playerNavigationPanel != null) {
            Object ctrl = playerNavigationPanel.getProperties().get("fx:controller");
            if (ctrl instanceof PlayerNavigationController navCtrl) {
                navCtrl.setPlayerDashboardController(this);
                System.out.println("PlayerDashboard: PlayerNavigationController ustawiony.");
            } else {
                // To może oznaczać, że PlayerNavigationPanel.fxml nie ma fx:controller ustawionego
                // lub został załadowany w sposób, który nie pozwala na pobranie kontrolera.
                System.err.println("Nie udało się pobrać PlayerNavigationController z fx:include. " +
                        "Sprawdź, czy PlayerNavigationPanel.fxml ma fx:controller.");
            }
        } else {
            // To może oznaczać, że fx:id="playerNavigationPanel" nie zostało poprawnie przypisane
            // w PlayerDashboard.fxml lub ścieżka do PlayerNavigationPanel.fxml jest błędna.
            System.err.println("playerNavigationPanel jest null w initialize(). Sprawdź fx:include w FXML.");
        }
    }

    @FXML
    public void handleShowTournaments() {
        if (currentPlayer == null) {
            System.err.println("currentPlayer jest NULL w handleShowTournaments");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Player/PlayerTournaments.fxml"));
            Parent tournamentsView = loader.load();
            PlayerTournaments tournamentsController = loader.getController();
            tournamentsController.initData(currentPlayer);

            contentPane.getChildren().setAll(tournamentsView);
            // Ustawianie kotwic dla rozciągnięcia widoku na cały kontener
            AnchorPane.setTopAnchor(tournamentsView, 0.0);
            AnchorPane.setBottomAnchor(tournamentsView, 0.0);
            AnchorPane.setLeftAnchor(tournamentsView, 0.0);
            AnchorPane.setRightAnchor(tournamentsView, 0.0);

        } catch (IOException e) {
            System.err.println("Błąd podczas ładowania PlayerTournaments.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Możesz dodać więcej metod handleShowX(), np.
    // @FXML
    // public void handleShowProfile() {
    //     if (currentPlayer == null) {
    //         System.err.println("currentPlayer jest NULL w handleShowProfile");
    //         return;
    //     }
    //     try {
    //         FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Player/PlayerProfile.fxml"));
    //         Parent profileView = loader.load();
    //         PlayerProfileController profileController = loader.getController();
    //         profileController.initData(currentPlayer); // Załóżmy, że masz taki kontroler
    //         contentPane.getChildren().setAll(profileView);
    //         AnchorPane.setTopAnchor(profileView, 0.0);
    //         AnchorPane.setBottomAnchor(profileView, 0.0);
    //         AnchorPane.setLeftAnchor(profileView, 0.0);
    //         AnchorPane.setRightAnchor(profileView, 0.0);
    //     } catch (IOException e) {
    //         System.err.println("Błąd podczas ładowania PlayerProfile.fxml: " + e.getMessage());
    //         e.printStackTrace();
    //     }
    // }


    private void startDebugTimer() {
        if (debugTimer != null) debugTimer.stop();

        debugTimer = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            if (currentPlayer != null) {
                System.out.println("DEBUG: Player: " + currentPlayer.getFirstname() + " " + currentPlayer.getLastname() +
                        ", Ranking: " + currentPlayer.getRanking());
            } else {
                System.out.println("DEBUG: currentPlayer is null");
            }
        }));
        debugTimer.setCycleCount(Timeline.INDEFINITE);
        debugTimer.play();
    }
}
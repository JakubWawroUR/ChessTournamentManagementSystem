package src.Player;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import src.model.Player;
import src.model.Tournament;
import src.BaseDashboardController;
import src.model.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.Timeline;

public class PlayerDashboard extends BaseDashboardController {
    @FXML private AnchorPane contentPane;
    @FXML private PlayerNavigation playerNavigationPanelController;
    @FXML private PlayerTopBar playerTopBarController;
    private Player currentPlayer;
    private Timeline debugTimer;
    private boolean initialized = false;

    @Override
    public void setLoggedInUser(User user) {
        if (user instanceof Player) {
            this.currentPlayer = (Player) user;
            super.setWelcomeMessage("Witaj, " + currentPlayer.getFirstName() + "!");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);


        if (playerNavigationPanelController != null) {
            this.playerNavigationPanelController.setPlayerDashboardController(this);
        }

        if (playerTopBarController != null) {
            this.playerTopBarController.setPlayerDashboardController(this);
            playerTopBarController.setSceneTitle("Panel Gracza");
        }
        initialized = true;
    }

    @FXML
    public void handleShowTournaments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Player/PlayerTournaments.fxml"));
            Parent tournamentsView = loader.load();
            Object controller = loader.getController();
            if (controller instanceof PlayerTournaments tournamentsController) {
                tournamentsController.initData(currentPlayer);
                tournamentsController.setPlayerDashboardController(this);
            }
            if (playerTopBarController != null) {
                playerTopBarController.setSceneTitle("Turnieje");
            }
            contentPane.getChildren().setAll(tournamentsView);
            AnchorPane.setTopAnchor(tournamentsView, 0.0);
            AnchorPane.setBottomAnchor(tournamentsView, 0.0);
            AnchorPane.setLeftAnchor(tournamentsView, 0.0);
            AnchorPane.setRightAnchor(tournamentsView, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleShowTournamentInfo(Tournament tournament) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Player/PlayerTournamentInfo.fxml"));
            Parent tournamentInfoView = loader.load();
            Object controller = loader.getController();
            if (controller instanceof PlayerTournamentInfo tournamentInfoController) {
                tournamentInfoController.initData(tournament, currentPlayer);
                tournamentInfoController.setPlayerDashboardController(this);
            }
            if (playerTopBarController != null) {
                playerTopBarController.setSceneTitle("Turniej: " + tournament.getName());
            }
            contentPane.getChildren().setAll(tournamentInfoView);
            AnchorPane.setTopAnchor(tournamentInfoView, 0.0);
            AnchorPane.setBottomAnchor(tournamentInfoView, 0.0);
            AnchorPane.setLeftAnchor(tournamentInfoView, 0.0);
            AnchorPane.setRightAnchor(tournamentInfoView, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleShowProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Player/PlayerProfile.fxml"));
            Parent profileView = loader.load();
            Object controller = loader.getController();
            if (controller instanceof PlayerProfile profileController) {
                profileController.initData(currentPlayer);
                profileController.setPlayerDashboardController(this);
                if (playerTopBarController != null) {
                    playerTopBarController.setSceneTitle("Profil Gracza");
                }
            }
            contentPane.getChildren().setAll(profileView);
            AnchorPane.setTopAnchor(profileView, 0.0);
            AnchorPane.setBottomAnchor(profileView, 0.0);
            AnchorPane.setLeftAnchor(profileView, 0.0);
            AnchorPane.setRightAnchor(profileView, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void cleanup() {
        if (debugTimer != null) {
            debugTimer.stop();
        }
    }
}
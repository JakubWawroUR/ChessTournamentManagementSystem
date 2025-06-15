package src.Admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import src.model.Game;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminResolveGame implements Initializable {
    @FXML private AnchorPane mainBox;
    @FXML private Label matchInfoLabel;
    @FXML private Label instructionLabel;
    @FXML private Button player1WinButton;
    @FXML private Button player2WinButton;
    @FXML private Button drawButton;

    private Game game;
    private Integer winnerPlayerId = null;
    private String winnerPlayerName = null;

    private Stage dialogStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (instructionLabel != null) {
            instructionLabel.setText("Rozstrzygnij spotkanie");
        }
    }

    public void initData(Game game, Stage dialogStage) {
        this.game = game;
        this.dialogStage = dialogStage;

        if (game != null) {
            if (player1WinButton != null) {
                player1WinButton.setText(game.getPlayer1Name() + " wygrywa");
            }
            if (player2WinButton != null) {
                player2WinButton.setText(game.getPlayer2Name() + " wygrywa");
            }
            if (drawButton != null) {
                drawButton.setText("Remis");
            }
            if (matchInfoLabel != null) {
                matchInfoLabel.setText("Mecz: " + game.getPlayer1Name() + " vs " + game.getPlayer2Name());
            }
        }
        this.winnerPlayerName = null;
    }
    @FXML
    private void handlePlayer1Win() {
        if (game != null) {
            this.winnerPlayerId = game.getPlayer1Id();
            this.winnerPlayerName = game.getPlayer1Name();
            if (dialogStage != null) {
                dialogStage.close();
            }
        }
    }
    @FXML
    private void handlePlayer2Win() {
        if (game != null) {
            this.winnerPlayerId = game.getPlayer2Id();
            this.winnerPlayerName = game.getPlayer2Name();
            if (dialogStage != null) {
                dialogStage.close();
            }
        }
    }
    @FXML
    private void handleDraw() {
        this.winnerPlayerId = null;
        this.winnerPlayerName = "Remis";
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    @FXML
    private void handleCancel() {
        this.winnerPlayerId = null;
        this.winnerPlayerName = null;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    public Integer getWinnerPlayerId() {
        return winnerPlayerId;
    }

    public String getWinnerPlayerName() {
        return winnerPlayerName;
    }
}
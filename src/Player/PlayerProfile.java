package src.Player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import src.model.Game;
import src.model.Player;
import src.dao.PlayerDAO;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.List;

public class PlayerProfile implements Initializable {

    @FXML private AnchorPane contentPane;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label rankingLabel;
    @FXML private Label recordLabel;
    @FXML private TableView<Game> matchHistoryTable;
    @FXML private TableColumn<Game, String> tournamentNameColumn;
    @FXML private TableColumn<Game, String> opponentColumn;
    @FXML private TableColumn<Game, String> resultColumn;

    private Player currentPlayer;
    private PlayerDAO playerDAO;
    private ObservableList<Game> playerGamesList;
    private PlayerDashboard playerDashboardController;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playerDAO = new PlayerDAO();
        playerGamesList = FXCollections.observableArrayList();
        tournamentNameColumn.setCellValueFactory(new PropertyValueFactory<>("tournamentName"));
        opponentColumn.setCellValueFactory(new PropertyValueFactory<>("opponentName"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("individualResultDisplay"));
        matchHistoryTable.setItems(playerGamesList);

        if (firstNameLabel != null) firstNameLabel.setText("");
        if (lastNameLabel != null) lastNameLabel.setText("");
        if (rankingLabel != null) rankingLabel.setText("");
        if (recordLabel != null) recordLabel.setText("");
    }
    public void initData(Player player) {
        this.currentPlayer = player;
        if (currentPlayer != null) {
            if (firstNameLabel != null) firstNameLabel.setText("Imię: " + currentPlayer.getFirstName());
            if (lastNameLabel != null) lastNameLabel.setText("Nazwisko: " + currentPlayer.getLastName());
            if (rankingLabel != null) rankingLabel.setText("Ranking: " + currentPlayer.getRanking());
            if (recordLabel != null) recordLabel.setText("W/D/L: " + currentPlayer.getRecord());
            loadPlayerMatchHistory();
        }
    }
    private void loadPlayerMatchHistory() {
        try {
            List<Game> games = playerDAO.getAllGamesForPlayer(currentPlayer.getPlayerId());
            playerGamesList.setAll(games);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void setPlayerDashboardController(PlayerDashboard controller) {
        this.playerDashboardController = controller;
    }
}
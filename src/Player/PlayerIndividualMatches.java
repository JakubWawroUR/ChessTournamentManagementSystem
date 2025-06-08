package src.Player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import src.dao.TournamentDAO;
import src.model.Game;
import src.model.Player;
import src.model.Tournament;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PlayerIndividualMatches implements Initializable {

    @FXML private Label playerNameLabel;
    @FXML private Label tournamentNameLabel;
    @FXML private TableView<Game> individualMatchesTable;
    @FXML private TableColumn<Game, Integer> gameNumberColumn;
    @FXML private TableColumn<Game, String> opponentColumn;
    @FXML private TableColumn<Game, String> resultColumn;

    private TournamentDAO tournamentDAO;
    private Player currentPlayer;
    private Tournament currentTournament;
    private ObservableList<Game> gameList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tournamentDAO = new TournamentDAO();
        gameList = FXCollections.observableArrayList();

        gameNumberColumn.setCellValueFactory(new PropertyValueFactory<>("gameNumber"));
        opponentColumn.setCellValueFactory(new PropertyValueFactory<>("opponentName"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("individualResultDisplay"));

        individualMatchesTable.setItems(gameList);
    }

    /**
     * Metoda inicjalizująca dane w kontrolerze, wywoływana z PlayerTournamentInfo.
     * @param player Gracz, którego mecze mają być wyświetlone.
     * @param tournament Turniej, w którym odbyły się mecze.
     */
    public void initData(Player player, Tournament tournament) {
        this.currentPlayer = player;
        this.currentTournament = tournament;
        if (currentPlayer != null && currentTournament != null) {
            playerNameLabel.setText(currentPlayer.getFirstName() + " " + currentPlayer.getLastName());
            tournamentNameLabel.setText(currentTournament.getName());
            loadPlayerMatches();
        }
    }

    private void loadPlayerMatches() {
        if (currentPlayer == null || currentTournament == null) {
            System.err.println("Błąd: Brak danych gracza lub turnieju do załadowania meczów.");
            return;
        }
        try {
            // TUTAJ ZMIANA: Użyj getAllGamesForTournament zamiast getGamesForTournament
            List<Game> allTournamentGames = tournamentDAO.getAllGamesForTournament(currentTournament.getId());
            List<Game> playerGames = new ArrayList<>();
            int gameCount = 1; // Licznik meczów dla tego gracza

            for (Game game : allTournamentGames) {
                if (game.getPlayer1Id() == currentPlayer.getPlayersTableId() ||
                        game.getPlayer2Id() == currentPlayer.getPlayersTableId()) {

                    game.setGameNumber(gameCount++);

                    if (game.getPlayer1Id() == currentPlayer.getPlayersTableId()) {
                        game.setOpponentName(game.getPlayer2Name());
                    } else {
                        game.setOpponentName(game.getPlayer1Name());
                    }

                    if (game.getWinnerId() == null) {
                        game.setIndividualResultDisplay("Remis");
                    } else if (game.getWinnerId() == currentPlayer.getPlayersTableId()) {
                        game.setIndividualResultDisplay("Wygrana");
                    } else {
                        game.setIndividualResultDisplay("Przegrana");
                    }
                    playerGames.add(game);
                }
            }
            gameList.setAll(playerGames);
            System.out.println("Załadowano " + playerGames.size() + " meczów dla gracza " + currentPlayer.getFirstName() + " " + currentPlayer.getLastName() + " w turnieju ID: " + currentTournament.getId());
        } catch (SQLException e) {
            System.err.println("Błąd podczas ładowania meczów dla gracza " + currentPlayer.getFirstName() + " w turnieju ID " + currentTournament.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obsługuje kliknięcie przycisku "Zamknij".
     * @param event Zdarzenie akcji.
     */
    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
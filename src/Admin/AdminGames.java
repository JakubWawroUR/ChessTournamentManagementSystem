package src.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.dao.TournamentDAO;
import src.model.Game;
import src.model.Tournament;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AdminGames implements Initializable {
    @FXML private Label tournamentNameLabel;
    @FXML private TableView<Game> matchesTable;
    @FXML private TableColumn<Game, Integer> idColumn;
    @FXML private TableColumn<Game, Integer> gameNumberColumn;
    @FXML private TableColumn<Game, String> player1Column;
    @FXML private TableColumn<Game, String> player2Column;
    @FXML private TableColumn<Game, String> winnerColumn;
    @FXML private TableColumn<Game, Void> actionsColumn;
    private Tournament selectedTournament;
    private ObservableList<Game> gameList;
    private TournamentDAO tournamentDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tournamentDAO = new TournamentDAO();
        gameList = FXCollections.observableArrayList();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        gameNumberColumn.setCellValueFactory(new PropertyValueFactory<>("gameNumber"));
        player1Column.setCellValueFactory(new PropertyValueFactory<>("player1Name"));
        player2Column.setCellValueFactory(new PropertyValueFactory<>("player2Name"));
        winnerColumn.setCellValueFactory(new PropertyValueFactory<>("winnerName"));
        setupActionsColumn();
        matchesTable.setItems(gameList);
    }

    public void setTournament(Tournament tournament) {
        this.selectedTournament = tournament;
        tournamentNameLabel.setText("Mecze turnieju: " + tournament.getName() + " (Status: " + tournament.getStatus() + ")");
        loadMatchesData();
        matchesTable.refresh();
    }

    private void loadMatchesData() {
        if (selectedTournament != null) {
            try {
                List<Game> games = tournamentDAO.getAllGamesForTournament(selectedTournament.getId());
                gameList.setAll(games);
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Błąd ładowania", "Nie udało się załadować meczów turnieju: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<Game, Void>() {
            private final Button resolveButton = new Button("Rozstrzygnij");
            private final HBox pane = new HBox(5, resolveButton);

            {
                pane.setAlignment(Pos.CENTER);
                resolveButton.setOnAction(event -> {
                    Game game = getTableView().getItems().get(getIndex());
                    handleResolveGame(game);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    if (selectedTournament != null && selectedTournament.getStatus().equals("ZAKOŃCZONY")) {
                        resolveButton.setDisable(true);
                        resolveButton.setText("Zakończony");
                    } else {
                        resolveButton.setDisable(false);
                        resolveButton.setText("Rozstrzygnij");
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    private void handleResolveGame(Game game) {
        if (selectedTournament != null && selectedTournament.getStatus().equals("ZAKOŃCZONY")) {
            showAlert(AlertType.WARNING, "Turniej Zakończony", "Nie można rozstrzygać meczów w zakończonym turnieju.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminResolveGame.fxml"));
            Parent root = loader.load();
            AdminResolveGame adminResolveGame = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Rozstrzygnij mecz");
            dialogStage.setAlwaysOnTop(true);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(matchesTable.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            adminResolveGame.initData(game, dialogStage);
            dialogStage.showAndWait();
            Integer winnerId = adminResolveGame.getWinnerPlayerId();
            String winnerName = adminResolveGame.getWinnerPlayerName();
            if (winnerId != null || (winnerId == null && winnerName != null && winnerName.equals("Remis"))) {
                try {
                    tournamentDAO.updateGameResult(game.getId(), winnerId);
                    game.setWinnerId(winnerId);
                    game.setWinnerName(winnerName);
                    matchesTable.refresh();

                    showAlert(AlertType.INFORMATION, "Sukces", "Wynik meczu został rozstrzygnięty.");
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Błąd bazy danych", "Nie udało się zaktualizować wyniku meczu: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Rozstrzygnięcie meczu anulowane.");
            }
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Błąd", "Nie można załadować okna rozstrzygania meczu.");
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Wystąpił nieoczekiwany błąd", "Szczegóły: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
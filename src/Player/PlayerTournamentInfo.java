package src.Player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.model.Player;
import src.model.Tournament;
import src.dao.TournamentDAO;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class PlayerTournamentInfo implements Initializable {

    @FXML private Label tournamentNameLabel;
    @FXML private Label tournamentSlotsLabel;
    @FXML private Label tournamentStartDateLabel;
    @FXML private Label tournamentEndDateLabel;
    @FXML private Label currentPlayerStatusLabel;
    @FXML private TableView<Player> participantsTable;
    @FXML private TableColumn<Player, Integer> participantNumberColumn;
    @FXML private TableColumn<Player, String> firstNameColumn;
    @FXML private TableColumn<Player, String> lastNameColumn;
    @FXML private TableColumn<Player, Integer> rankingColumn;
    @FXML private TableColumn<Player, String> recordColumn;
    @FXML private TableColumn<Player, Void> viewMatchesColumn;

    private Tournament selectedTournament;
    private Player currentPlayer;
    private TournamentDAO tournamentDAO;
    private PlayerDashboard playerDashboardController;
    private ObservableList<Player> participantList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tournamentDAO = new TournamentDAO();
        participantList = FXCollections.observableArrayList();
        participantNumberColumn.setCellValueFactory(new PropertyValueFactory<>("displayNumber"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        rankingColumn.setCellValueFactory(new PropertyValueFactory<>("ranking"));
        recordColumn.setCellValueFactory(new PropertyValueFactory<>("record"));
        viewMatchesColumn.setCellFactory(param -> new TableCell<Player, Void>() {
            private final Button viewButton = new Button("Pokaż Mecze");

            {
                viewButton.setOnAction(event -> {
                    Player player = getTableView().getItems().get(getIndex());
                    handleShowPlayerMatches(player, selectedTournament);
                });
                viewButton.setStyle("-fx-font-size: 10px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
        participantsTable.setItems(participantList);
    }
    public void initData(Tournament tournament, Player currentPlayer) {
        this.selectedTournament = tournament;
        this.currentPlayer = currentPlayer;
        if (selectedTournament != null) {
            tournamentNameLabel.setText(selectedTournament.getName());
            tournamentSlotsLabel.setText(selectedTournament.getFreeSlots() + " / " + selectedTournament.getMaxSlots());
            tournamentStartDateLabel.setText(selectedTournament.getStartDate());
            tournamentEndDateLabel.setText(selectedTournament.getEndDate());
            try {
                if (tournamentDAO.isPlayerRegisteredForTournament(selectedTournament.getId(), currentPlayer.getPlayerId())) {
                    currentPlayerStatusLabel.setText("Zapisany");
                } else {
                    currentPlayerStatusLabel.setText("Niezapisany");
                }
            } catch (SQLException e) {
                System.err.println("Błąd podczas sprawdzania statusu gracza: " + e.getMessage());
                currentPlayerStatusLabel.setText("Błąd");
                e.printStackTrace();
            }
            loadTournamentParticipants(selectedTournament.getId());
        } else {
            tournamentNameLabel.setText("[Brak wybranego turnieju]");
        }
    }
    public void setPlayerDashboardController(PlayerDashboard controller) {
        this.playerDashboardController = controller;
        System.out.println("PlayerTournamentInfo: Referencja do PlayerDashboardController ustawiona.");
    }
    private void loadTournamentParticipants(int tournamentId) {
        try {
            List<Player> participants = tournamentDAO.getRegisteredPlayersWithRecordsForTournament(tournamentId);
            for (int i = 0; i < participants.size(); i++) {
                participants.get(i).setDisplayNumber(i + 1);
            }
            participantList.setAll(participants);
        }
        catch (SQLException e) {
            System.err.println("Błąd podczas ładowania uczestników turnieju ID " + tournamentId + ": " + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleShowPlayerMatches(Player player, Tournament tournament) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Player/PlayerIndividualMatches.fxml"));
            Parent root = loader.load();
            PlayerIndividualMatches matchesController = loader.getController();
            matchesController.initData(player, tournament);
            Stage stage = new Stage();
            stage.setTitle("Mecze gracza " + player.getFirstName() + " " + player.getLastName() + " w turnieju: " + tournament.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
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
// import java.util.function.Consumer; // Już niepotrzebny, jeśli go nie używasz jawnie

public class PlayerTournamentInfo implements Initializable {

    // --- Elementy FXML ---
    @FXML private Label tournamentNameLabel;
    @FXML private Label tournamentSlotsLabel;
    @FXML private Label tournamentStartDateLabel;
    @FXML private Label tournamentEndDateLabel;
    @FXML private Label currentPlayerStatusLabel;

    @FXML private TableView<Player> participantsTable;
    // Kolumny dla tabeli uczestników
    @FXML private TableColumn<Player, Integer> participantNumberColumn;
    @FXML private TableColumn<Player, String> firstNameColumn;
    @FXML private TableColumn<Player, String> lastNameColumn;
    @FXML private TableColumn<Player, Integer> rankingColumn;
    @FXML private TableColumn<Player, String> recordColumn;
    @FXML private TableColumn<Player, Void> viewMatchesColumn; // NOWA KOLUMNA AKCJI

    // --- Pola logiki biznesowej ---
    private Tournament selectedTournament;
    private Player currentPlayer;
    private TournamentDAO tournamentDAO;

    // Referencja do kontrolera nadrzędnego (PlayerDashboard)
    private PlayerDashboard playerDashboardController;

    private ObservableList<Player> participantList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tournamentDAO = new TournamentDAO();
        participantList = FXCollections.observableArrayList();

        participantNumberColumn.setCellValueFactory(new PropertyValueFactory<>("displayNumber"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName")); // POPRAWIONA LINIA
        rankingColumn.setCellValueFactory(new PropertyValueFactory<>("ranking"));
        recordColumn.setCellValueFactory(new PropertyValueFactory<>("record"));

        // KONFIGURACJA NOWEJ KOLUMNY Z PRZYCISKIEM
        viewMatchesColumn.setCellFactory(param -> new TableCell<Player, Void>() {
            private final Button viewButton = new Button("Pokaż Mecze");

            {
                viewButton.setOnAction(event -> {
                    Player player = getTableView().getItems().get(getIndex());
                    handleShowPlayerMatches(player, selectedTournament); // Wywołaj nową metodę
                });
                viewButton.setStyle("-fx-font-size: 10px;"); // Mniejsza czcionka dla przycisku
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

    /**
     * Metoda do inicjalizacji danych turnieju i bieżącego gracza.
     * Wywoływana przez PlayerDashboard.
     * @param tournament Obiekt Tournament z danymi do wyświetlenia.
     * @param currentPlayer Obiekt Player reprezentujący aktualnie zalogowanego gracza.
     */
    public void initData(Tournament tournament, Player currentPlayer) {
        this.selectedTournament = tournament;
        this.currentPlayer = currentPlayer;
        if (selectedTournament != null) {
            tournamentNameLabel.setText(selectedTournament.getName());
            tournamentSlotsLabel.setText(selectedTournament.getFreeSlots() + " / " + selectedTournament.getMaxSlots());
            tournamentStartDateLabel.setText(selectedTournament.getStartDate());
            tournamentEndDateLabel.setText(selectedTournament.getEndDate());

            try {
                if (tournamentDAO.isPlayerRegisteredForTournament(selectedTournament.getId(), currentPlayer.getPlayersTableId())) {
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
            // ... ustaw pozostałe etykiety na brak danych
        }
    }

    // Setter dla referencji do PlayerDashboard
    public void setPlayerDashboardController(PlayerDashboard controller) {
        this.playerDashboardController = controller;
        System.out.println("PlayerTournamentInfo: Referencja do PlayerDashboardController ustawiona.");
    }

    /**
     * Ładuje listę graczy zarejestrowanych w danym turnieju do tabeli, wraz z ich rekordami.
     * @param tournamentId ID turnieju, dla którego ma być pobrana lista graczy.
     */
    private void loadTournamentParticipants(int tournamentId) {
        try {
            List<Player> participants = tournamentDAO.getRegisteredPlayersWithRecordsForTournament(tournamentId);
            for (int i = 0; i < participants.size(); i++) {
                participants.get(i).setDisplayNumber(i + 1);
            }
            participantList.setAll(participants);
            System.out.println("Załadowano " + participants.size() + " uczestników (z rekordami) dla turnieju ID: " + tournamentId);
        }
        catch (SQLException e) { // Zmieniono na SQLException, bo tak rzuca TournamentDAO
            System.err.println("Błąd podczas ładowania uczestników turnieju ID " + tournamentId + ": " + e.getMessage());
            e.printStackTrace();
            // Możesz dodać alert dla użytkownika
        }
        catch (Exception e) { // Zachowaj ogólny catch, jeśli są inne potencjalne wyjątki, ale SQLException jest specyficzny dla DAO
            System.err.println("Ogólny błąd podczas ładowania uczestników turnieju ID " + tournamentId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Obsługuje akcję kliknięcia przycisku "Pokaż Mecze" dla konkretnego gracza.
     * Otwiera nowe okno lub ładuje nowy widok z listą meczów dla tego gracza w wybranym turnieju.
     * @param player Gracz, którego mecze chcemy wyświetlić.
     * @param tournament Turniej, w którym odbyły się mecze.
     */
    private void handleShowPlayerMatches(Player player, Tournament tournament) {
        if (player == null || tournament == null) {
            System.out.println("Nie wybrano gracza ani turnieju do wyświetlenia meczów.");
            return;
        }

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
            System.err.println("Błąd podczas ładowania widoku meczów gracza: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
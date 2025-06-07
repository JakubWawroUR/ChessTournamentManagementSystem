package src.Player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import src.dao.PlayerDAO; // Nowy import
import src.dao.TournamentDAO;
import src.model.Player;
import src.model.Tournament;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PlayerTournaments implements Initializable {

    @FXML private TableView<Tournament> tournamentTable;
    @FXML private TableColumn<Tournament, String> nameColumn;
    @FXML private TableColumn<Tournament, String> slotsInfoColumn;
    @FXML private TableColumn<Tournament, String> startDateColumn;
    @FXML private TableColumn<Tournament, String> endDateColumn;
    @FXML private TableColumn<Tournament, Void> operationsColumn;

    private ObservableList<Tournament> tournamentList;
    private TournamentDAO tournamentDAO;
    private PlayerDAO playerDAO; // Dodano PlayerDAO
    private Player currentPlayer;

    // Metoda do inicjalizacji danych gracza (wywoływana z PlayerDashboard)
    public void initData(Player player) {
        this.currentPlayer = player;
        if (this.currentPlayer != null) {
            System.out.println("PlayerTournaments: initData - Gracz ID (users.idusers): " + this.currentPlayer.getId());
            System.out.println("PlayerTournaments: initData - Gracz ID (players.id): " + this.currentPlayer.getPlayersTableId());

            // Ta logika jest już teraz głównie obsługiwana w UserDAO.getPlayerByLoginAndPassword
            // ale zostawiamy tu, jako fallback lub dla debugowania
            if (this.currentPlayer.getPlayersTableId() == 0) { // Jeśli playersTableId nie jest ustawione (np. jeśli użytkownik był tylko Userem, a teraz jest Graczem)
                try {
                    Integer playersId = playerDAO.getPlayersTableIdByUserId(this.currentPlayer.getId());
                    if (playersId != null) {
                        this.currentPlayer.setPlayersTableId(playersId);
                        System.out.println("PlayerTournaments: playersTableId zostało ustawione na: " + playersId);
                    } else {
                        System.err.println("Błąd: Zalogowany użytkownik (ID: " + this.currentPlayer.getId() + ") nie posiada rekordu w tabeli 'players' (prawdopodobnie nie jest graczem).");
                        // W tej sytuacji przycisk "Dołącz" zostanie wyłączony przez setupOperationsColumn
                        showAlert(Alert.AlertType.WARNING, "Błąd danych gracza", "Twoje dane gracza są niekompletne. Nie możesz dołączać do turniejów.");
                    }
                } catch (SQLException e) {
                    System.err.println("Błąd podczas inicjalizacji playersTableId w PlayerTournaments: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Błąd bazy danych", "Problem z inicjalizacją danych gracza.");
                }
            }
        } else {
            System.out.println("PlayerTournaments: initData - currentPlayer jest NULL!");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tournamentDAO = new TournamentDAO();
        playerDAO = new PlayerDAO(); // Inicjalizacja PlayerDAO

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        slotsInfoColumn.setCellValueFactory(new PropertyValueFactory<>("slotsInfo"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        setupOperationsColumn();

        tournamentList = FXCollections.observableArrayList();
        loadTournamentDataFromDatabase();
        tournamentTable.setItems(tournamentList);
    }

    private void setupOperationsColumn() {
        operationsColumn.setCellFactory(param -> new TableCell<Tournament, Void>() {
            private final Button joinButton = new Button("Dołącz");
            private final HBox pane = new HBox(5, joinButton);

            {
                pane.setAlignment(Pos.CENTER);

                joinButton.setOnAction(event -> {
                    Tournament tournament = getTableView().getItems().get(getIndex());
                    handleJoinTournament(tournament);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Tournament tournament = getTableView().getItems().get(getIndex());
                    LocalDate endDate = LocalDate.parse(tournament.getEndDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                    LocalDate startDate = LocalDate.parse(tournament.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                    LocalDate today = LocalDate.now();

                    boolean isEnded = today.isAfter(endDate);
                    boolean isStarted = today.isAfter(startDate);
                    boolean hasFreeSlots = tournament.getFreeSlots() > 0;
                    boolean isRegistered = false;

                    // WAŻNA ZMIANA: Sprawdzamy, czy currentPlayer i playersTableId są poprawne, zanim sprawdzimy rejestrację
                    if (currentPlayer != null && currentPlayer.getPlayersTableId() != 0) {
                        try {
                            isRegistered = tournamentDAO.isPlayerRegisteredForTournament(tournament.getId(), currentPlayer.getPlayersTableId());
                        } catch (SQLException e) {
                            System.err.println("Błąd podczas sprawdzania rejestracji: " + e.getMessage());
                            isRegistered = false;
                        }
                    } else {
                        // Jeśli currentPlayer nie jest zainicjalizowany lub playersTableId jest 0,
                        // to nie możemy sprawdzić rejestracji, a przycisk "Dołącz" powinien być wyłączony.
                        joinButton.setDisable(true);
                        joinButton.setText("Błąd gracza"); // Możesz tu dać inny tekst
                        setGraphic(pane);
                        return; // Wychodzimy, aby nie renderować dalej
                    }


                    if (isEnded || isRegistered || !hasFreeSlots) {
                        joinButton.setDisable(true);
                        if (isEnded) {
                            joinButton.setText("Zakończony");
                        } else if (isRegistered) {
                            joinButton.setText("Zapisany");
                        } else if (!hasFreeSlots) {
                            joinButton.setText("Brak miejsc");
                        }
                    } else if (isStarted) {
                        joinButton.setDisable(true);
                        joinButton.setText("W trakcie");
                    }
                    else {
                        joinButton.setDisable(false);
                        joinButton.setText("Dołącz");
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    /**
     * Obsługuje logikę dołączania gracza do turnieju.
     * @param tournament Turniej, do którego gracz chce dołączyć.
     */
    private void handleJoinTournament(Tournament tournament) {
        // WAŻNA ZMIANA: Sprawdzamy, czy currentPlayer i playersTableId są poprawne
        if (currentPlayer == null || currentPlayer.getPlayersTableId() == 0) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie znaleziono pełnych danych gracza. Proszę się zalogować ponownie lub upewnij się, że masz przypisany profil gracza.");
            return;
        }

        if (tournament.getFreeSlots() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Brak miejsc", "W turnieju '" + tournament.getName() + "' nie ma już wolnych miejsc.");
            return;
        }

        LocalDate startDate = LocalDate.parse(tournament.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate today = LocalDate.now();
        if (today.isAfter(startDate)) {
            showAlert(Alert.AlertType.WARNING, "Turniej rozpoczęty", "Nie możesz dołączyć do turnieju, który już się rozpoczął.");
            return;
        }

        try {
            // WAŻNA ZMIANA: Sprawdzamy, czy gracz jest już zapisany, używając playersTableId
            boolean isAlreadyRegistered = tournamentDAO.isPlayerRegisteredForTournament(tournament.getId(), currentPlayer.getPlayersTableId());
            if (isAlreadyRegistered) {
                showAlert(Alert.AlertType.INFORMATION, "Już zapisany", "Jesteś już zapisany na turniej '" + tournament.getName() + "'.");
                return;
            }

            // 1. Dodaj rekord do tabeli tournament_players
            // TUTAJ JEST NAJWAŻNIEJSZA ZMIANA: Przekazujemy playersTableId, nie idusers
            tournamentDAO.addPlayerToTournament(tournament.getId(), currentPlayer.getPlayersTableId());

            // 2. Zmniejsz liczbę wolnych miejsc w tabeli tournaments
            tournamentDAO.updateFreeSlots(tournament.getId(), -1);

            // 3. Zaktualizuj obiekt Tournament w ObservableList
            tournament.setFreeSlots(tournament.getFreeSlots() - 1);
            tournamentTable.refresh();

            showAlert(Alert.AlertType.INFORMATION, "Sukces!", "Pomyślnie dołączyłeś do turnieju '" + tournament.getName() + "'.");
            System.out.println("Gracz (players.id): " + currentPlayer.getPlayersTableId() + " dołączył do turnieju " + tournament.getId());

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas dołączania do turnieju: " + e.getMessage());
            System.err.println("Błąd dołączania gracza do turnieju: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadTournamentDataFromDatabase() {
        System.out.println("PlayerTournamentsController: Rozpoczynam ładowanie danych turniejów z bazy.");
        try {
            List<Tournament> tournaments = tournamentDAO.getAllTournaments();
            System.out.println("PlayerTournamentsController: Pobrana lista z DAO ma " + tournaments.size() + " elementów.");
            tournamentList.setAll(tournaments);
            System.out.println("PlayerTournamentsController: ObservableList ma teraz " + tournamentList.size() + " elementów.");

        } catch (Exception e) {
            System.err.println("Wystąpił błąd podczas ładowania danych turniejów z bazy danych:");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd ładowania danych", "Nie udało się załadować danych turniejów z bazy danych. Sprawdź połączenie.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) { // Zmieniono nazwę argumentu z 'String' na 'message'
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package src.Player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Importuj FXMLLoader
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent; // Importuj Parent
import javafx.scene.Scene; // Importuj Scene
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow; // Importuj TableRow
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent; // Importuj MouseEvent
import javafx.scene.layout.HBox;
import javafx.stage.Stage; // Importuj Stage
import src.dao.PlayerDAO;
import src.dao.TournamentDAO;
import src.model.Player;
import src.model.Tournament;
import src.Player.PlayerTournamentInfo;

import java.io.IOException; // Importuj IOException
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
    private PlayerDAO playerDAO;
    private Player currentPlayer;

    public void initData(Player player) {
        this.currentPlayer = player;
        if (this.currentPlayer != null) {
            System.out.println("PlayerTournaments: initData - Gracz ID (users.idusers): " + this.currentPlayer.getId());
            System.out.println("PlayerTournaments: initData - Gracz ID (players.id): " + this.currentPlayer.getPlayersTableId());

            if (this.currentPlayer.getPlayersTableId() == 0) {
                try {
                    Integer playersId = playerDAO.getPlayersTableIdByUserId(this.currentPlayer.getId());
                    if (playersId != null) {
                        this.currentPlayer.setPlayersTableId(playersId);
                        System.out.println("PlayerTournaments: playersTableId zostało ustawione na: " + playersId);
                    } else {
                        System.err.println("Błąd: Zalogowany użytkownik (ID: " + this.currentPlayer.getId() + ") nie posiada rekordu w tabeli 'players' (prawdopodobnie nie jest graczem).");
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
        playerDAO = new PlayerDAO();

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        slotsInfoColumn.setCellValueFactory(new PropertyValueFactory<>("slotsInfo"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        setupOperationsColumn();

        tournamentList = FXCollections.observableArrayList();
        loadTournamentDataFromDatabase();
        tournamentTable.setItems(tournamentList);

        // --- DODANA LOGIKA DLA KLIKNIĘĆ W WIERSZ ---
        tournamentTable.setRowFactory(tv -> {
            TableRow<Tournament> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                // Sprawdzamy, czy to podwójne kliknięcie i czy wiersz nie jest pusty
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Tournament selectedTournament = row.getItem();
                    System.out.println("Wybrano turniej: " + selectedTournament.getName());
                    // Przełączamy scenę na PlayerTournamentInfo, przekazując dane turnieju
                    switchToPlayerTournamentInfo(selectedTournament);
                }
            });
            return row;
        });
        // --- KONIEC DODANEJ LOGIKI ---
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

                    if (currentPlayer != null && currentPlayer.getPlayersTableId() != 0) {
                        try {
                            isRegistered = tournamentDAO.isPlayerRegisteredForTournament(tournament.getId(), currentPlayer.getPlayersTableId());
                        } catch (SQLException e) {
                            System.err.println("Błąd podczas sprawdzania rejestracji: " + e.getMessage());
                            isRegistered = false;
                        }
                    } else {
                        joinButton.setDisable(true);
                        joinButton.setText("Błąd gracza");
                        setGraphic(pane);
                        return;
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

    private void handleJoinTournament(Tournament tournament) {
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
            boolean isAlreadyRegistered = tournamentDAO.isPlayerRegisteredForTournament(tournament.getId(), currentPlayer.getPlayersTableId());
            if (isAlreadyRegistered) {
                showAlert(Alert.AlertType.INFORMATION, "Już zapisany", "Jesteś już zapisany na turniej '" + tournament.getName() + "'.");
                return;
            }

            tournamentDAO.addPlayerToTournament(tournament.getId(), currentPlayer.getPlayersTableId());
            tournamentDAO.updateFreeSlots(tournament.getId(), -1);

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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- NOWA METODA DO PRZEŁĄCZANIA SCENY NA PlayerTournamentInfo ---
    private void switchToPlayerTournamentInfo(Tournament tournament) {
        try {
            // Upewnij się, że ścieżka do Twojego FXML pliku PlayerTournamentInfo jest poprawna!
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PlayerTournamentInfo.fxml"));
            Parent root = loader.load();

            // Pobierz kontroler nowej sceny
            PlayerTournamentInfo controller = loader.getController();

            // Sprawdź, czy kontroler został poprawnie załadowany i przekaż mu dane
            if (controller != null) {
                // Przekazujemy zarówno obiekt Tournament, jak i bieżącego gracza (currentPlayer)
                // Kontroler PlayerTournamentInfoController musi mieć metodę `initData`
                controller.initData(tournament, currentPlayer);
            }

            // Ustaw nową scenę na istniejącym Stage (oknie)
            Stage stage = (Stage) tournamentTable.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd ładowania sceny", "Nie udało się załadować sceny szczegółów turnieju. Sprawdź plik FXML i kontroler PlayerTournamentInfo.");
        }
    }
}
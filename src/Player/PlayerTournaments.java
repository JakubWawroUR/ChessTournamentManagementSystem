package src.Player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import src.dao.TournamentDAO;
import src.model.Player;
import src.model.Tournament;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseButton; // DODANY IMPORT

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class PlayerTournaments implements Initializable {

    @FXML private TableView<Tournament> tournamentsTable;
    @FXML private TableColumn<Tournament, Integer> idColumn;
    @FXML private TableColumn<Tournament, String> nameColumn;
    @FXML private TableColumn<Tournament, String> startDateColumn;
    @FXML private TableColumn<Tournament, String> endDateColumn;
    @FXML private TableColumn<Tournament, Integer> freeSlotsColumn;
    @FXML private TableColumn<Tournament, Integer> maxSlotsColumn;
    @FXML private TableColumn<Tournament, Void> registerColumn; // Kolumna dla przycisku

    private TournamentDAO tournamentDAO;
    private ObservableList<Tournament> tournamentList;
    private Player currentPlayer;

    // Referencja do kontrolera nadrzędnego (PlayerDashboard)
    private PlayerDashboard playerDashboardController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tournamentDAO = new TournamentDAO();
        tournamentList = FXCollections.observableArrayList();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        freeSlotsColumn.setCellValueFactory(new PropertyValueFactory<>("freeSlots"));
        maxSlotsColumn.setCellValueFactory(new PropertyValueFactory<>("maxSlots"));

        // KONFIGURACJA KOLUMNY Z PRZYCISKIEM ZAREJESTRUJ SIĘ
        registerColumn.setCellFactory(param -> new TableCell<Tournament, Void>() {
            private final Button registerButton = new Button("Zarejestruj się");

            {
                registerButton.setOnAction(event -> {
                    Tournament tournament = getTableView().getItems().get(getIndex());
                    handleRegisterForTournament(tournament); // Wywołaj metodę rejestracji dla konkretnego turnieju
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Tournament currentTournament = getTableView().getItems().get(getIndex());
                    if (currentTournament != null && currentPlayer != null) {
                        try {
                            boolean isRegistered = tournamentDAO.isPlayerRegisteredForTournament(currentTournament.getId(), currentPlayer.getPlayersTableId());
                            if (isRegistered || currentTournament.getFreeSlots() <= 0) {
                                registerButton.setDisable(true); // Wyłącz przycisk
                                if (isRegistered) {
                                    registerButton.setText("Zarejestrowany");
                                } else if (currentTournament.getFreeSlots() <= 0) {
                                    registerButton.setText("Brak miejsc");
                                }
                            } else {
                                registerButton.setDisable(false); // Włącz przycisk
                                registerButton.setText("Zarejestruj się");
                            }
                        } catch (SQLException e) {
                            System.err.println("Błąd sprawdzania statusu rejestracji: " + e.getMessage());
                            registerButton.setDisable(true); // Wyłącz w przypadku błędu
                            registerButton.setText("Błąd");
                        }
                    } else {
                        registerButton.setDisable(true); // Wyłącz, jeśli dane niekompletne
                        setGraphic(null); // Nie pokazuj przycisku
                    }
                    setGraphic(registerButton);
                }
            }
        });
        // KONIEC KONFIGURACJI KOLUMNY Z PRZYCISKIEM

        tournamentsTable.setItems(tournamentList);

        loadTournaments(); // Początkowe ładowanie turniejów

        // *** PRZYWRÓCONA LOGIKA PODWÓJNEGO KLIKNIĘCIA DLA SZCZEGÓŁÓW TURNIEJU ***
        tournamentsTable.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                Tournament selectedTournament = tournamentsTable.getSelectionModel().getSelectedItem();
                if (selectedTournament != null) {
                    if (playerDashboardController != null) {
                        System.out.println("PlayerTournaments: Podwójne kliknięcie. Przekazuję żądanie wyświetlenia informacji o turnieju do PlayerDashboard dla turnieju: " + selectedTournament.getName());
                        playerDashboardController.handleShowTournamentInfo(selectedTournament);
                    } else {
                        System.err.println("PlayerTournaments: playerDashboardController jest NULL. Nie można wyświetlić informacji o turnieju po podwójnym kliknięciu.");
                    }
                } else {
                    System.out.println("PlayerTournaments: Nie wybrano żadnego turnieju do wyświetlenia informacji (podwójne kliknięcie).");
                }
            }
        });
        // *** KONIEC PRZYWRÓCONEJ LOGIKI ***
    }

    /**
     * Metoda do inicjalizacji danych gracza. Wywoływana przez PlayerDashboard.
     * @param player Obiekt gracza zalogowanego w tej sesji.
     */
    public void initData(Player player) {
        this.currentPlayer = player;
        System.out.println("PlayerTournaments: Dane gracza otrzymane: " + player.getFirstName());
        loadTournaments(); // Załaduj turnieje po otrzymaniu danych gracza, aby zaktualizować status rejestracji
    }

    // Setter dla referencji do PlayerDashboard
    public void setPlayerDashboardController(PlayerDashboard controller) {
        this.playerDashboardController = controller;
        System.out.println("PlayerTournaments: Referencja do PlayerDashboardController ustawiona.");
    }


    private void loadTournaments() {
        List<Tournament> tournaments = tournamentDAO.getAllTournaments();
        tournamentList.setAll(tournaments);
        System.out.println("PlayerTournaments: Załadowano " + tournaments.size() + " turniejów.");
        tournamentsTable.refresh(); // Odśwież tabelę, aby zaktualizować stan przycisków
    }

    /**
     * Obsługuje rejestrację w turnieju. Wywoływana z komórki tabeli.
     * @param selectedTournament Turniej, w którym gracz chce się zarejestrować.
     */
    private void handleRegisterForTournament(Tournament selectedTournament) {
        if (selectedTournament == null) {
            System.out.println("PlayerTournaments: Nie wybrano turnieju do rejestracji.");
            // Możesz wyświetlić alert
            return;
        }

        if (currentPlayer == null) {
            System.err.println("PlayerTournaments: Brak danych bieżącego gracza. Nie można zarejestrować.");
            // Możesz wyświetlić alert
            return;
        }

        try {
            if (tournamentDAO.isPlayerRegisteredForTournament(selectedTournament.getId(), currentPlayer.getPlayersTableId())) {
                System.out.println("PlayerTournaments: Gracz jest już zarejestrowany w tym turnieju.");
                // Możesz pokazać alert
            } else if (selectedTournament.getFreeSlots() <= 0) {
                System.out.println("PlayerTournaments: Brak wolnych miejsc w turnieju.");
                // Możesz pokazać alert
            } else {
                tournamentDAO.addPlayerToTournament(selectedTournament.getId(), currentPlayer.getPlayersTableId());
                tournamentDAO.updateFreeSlots(selectedTournament.getId(), -1); // Zmniejsz wolne miejsca
                System.out.println("PlayerTournaments: Pomyślnie zarejestrowano gracza w turnieju.");
                loadTournaments(); // Odśwież listę turniejów (zaktualizuje przyciski)
                // Możesz pokazać alert sukcesu
            }
        } catch (Exception e) {
            System.err.println("PlayerTournaments: Błąd podczas rejestracji w turnieju: " + e.getMessage());
            e.printStackTrace();
            // Pokaż alert błędu
        }
    }
}
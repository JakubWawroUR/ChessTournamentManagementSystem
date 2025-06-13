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
import javafx.scene.input.MouseButton;
import javafx.scene.control.Alert; // Dodany import dla Alert
import javafx.scene.control.ButtonType; // Dodany import dla ButtonType

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PlayerTournaments implements Initializable {

    @FXML private TableView<Tournament> tournamentsTable;
    @FXML private TableColumn<Tournament, Integer> idColumn;
    @FXML private TableColumn<Tournament, String> nameColumn;
    @FXML private TableColumn<Tournament, String> startDateColumn;
    @FXML private TableColumn<Tournament, String> endDateColumn;
    @FXML private TableColumn<Tournament, Integer> freeSlotsColumn;
    @FXML private TableColumn<Tournament, Integer> maxSlotsColumn;
    @FXML private TableColumn<Tournament, String> statusColumn; // Dodana kolumna statusu
    @FXML private TableColumn<Tournament, Void> registerColumn;

    private TournamentDAO tournamentDAO;
    private ObservableList<Tournament> tournamentList;
    private Player currentPlayer;

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
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status")); // Ustawienie dla kolumny statusu

        registerColumn.setCellFactory(param -> new TableCell<Tournament, Void>() {
            private final Button registerButton = new Button("Zarejestruj się");

            {
                registerButton.setOnAction(event -> {
                    Tournament tournament = getTableView().getItems().get(getIndex());
                    handleRegisterForTournament(tournament);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Tournament currentTournament = getTableView().getItems().get(getIndex());
                    // Zabezpieczenie przed NullPointerException, jeśli currentPlayer jest jeszcze null
                    if (currentTournament != null && currentPlayer != null) {
                        try {
                            boolean isRegistered = tournamentDAO.isPlayerRegisteredForTournament(currentTournament.getId(), currentPlayer.getPlayersTableId());
                            if (isRegistered) {
                                registerButton.setDisable(true);
                                registerButton.setText("Zarejestrowany");
                            } else if (currentTournament.getFreeSlots() <= 0) {
                                registerButton.setDisable(true);
                                registerButton.setText("Brak miejsc");
                            } else if (!currentTournament.getStatus().equals("OTWARTY")) { // Dodane sprawdzenie statusu
                                registerButton.setDisable(true);
                                registerButton.setText("Zamknięty");
                            }
                            else {
                                registerButton.setDisable(false);
                                registerButton.setText("Zarejestruj się");
                            }
                        } catch (SQLException e) {
                            System.err.println("Błąd sprawdzania statusu rejestracji: " + e.getMessage());
                            registerButton.setDisable(true);
                            registerButton.setText("Błąd");
                            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie można sprawdzić statusu rejestracji. " + e.getMessage());
                        }
                    } else {
                        registerButton.setDisable(true); // Wyłącz, jeśli brak danych gracza lub turnieju
                        registerButton.setText("Brak danych");
                    }
                    setGraphic(registerButton);
                }
            }
        });

        tournamentsTable.setItems(tournamentList);
        loadTournaments(); // Początkowe ładowanie turniejów
        // Ważne: Po initialize, initData(player) wywoła loadTournaments() ponownie
        // Aby upewnić się, że przyciski są poprawne, możesz potrzebować odświeżenia tabeli po ustawieniu currentPlayer.

        tournamentsTable.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                Tournament selectedTournament = tournamentsTable.getSelectionModel().getSelectedItem();
                if (selectedTournament != null) {
                    if (playerDashboardController != null) {
                        System.out.println("PlayerTournaments: Podwójne kliknięcie. Przekazuję żądanie wyświetlenia informacji o turnieju do PlayerDashboard dla turnieju: " + selectedTournament.getName());
                        playerDashboardController.handleShowTournamentInfo(selectedTournament);
                    } else {
                        System.err.println("PlayerTournaments: playerDashboardController jest NULL. Nie można wyświetlić informacji o turnieju po podwójnym kliknięciu.");
                        showAlert(Alert.AlertType.ERROR, "Błąd", "Nie można wyświetlić szczegółów turnieju. Błąd wewnętrzny aplikacji.");
                    }
                } else {
                    System.out.println("PlayerTournaments: Nie wybrano żadnego turnieju do wyświetlenia informacji (podwójne kliknięcie).");
                }
            }
        });
    }

    /**
     * Metoda do inicjalizacji danych gracza. Wywoływana przez PlayerDashboard.
     * @param player Obiekt gracza zalogowanego w tej sesji.
     */
    public void initData(Player player) {
        this.currentPlayer = player;
        System.out.println("PlayerTournaments: Dane gracza otrzymane: " + player.getFirstName());
        // Odśwież turnieje, aby przyciski rejestracji odzwierciedlały status bieżącego gracza
        loadTournaments();
    }

    // Setter dla referencji do PlayerDashboard
    public void setPlayerDashboardController(PlayerDashboard controller) {
        this.playerDashboardController = controller;
        System.out.println("PlayerTournaments: Referencja do PlayerDashboardController ustawiona.");
    }

    private void loadTournaments() {
        try {
            List<Tournament> tournaments = tournamentDAO.getAllTournaments();
            tournamentList.setAll(tournaments);
            System.out.println("PlayerTournaments: Załadowano " + tournaments.size() + " turniejów.");
            tournamentsTable.refresh(); // Odśwież tabelę, aby zaktualizować stan przycisków
        } catch (Exception e) {
            System.err.println("PlayerTournaments: Błąd podczas ładowania turniejów: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd ładowania", "Nie udało się załadować listy turniejów.");
        }
    }

    /**
     * Obsługuje rejestrację w turnieju. Wywoływana z komórki tabeli.
     * @param selectedTournament Turniej, w którym gracz chce się zarejestrować.
     */
    private void handleRegisterForTournament(Tournament selectedTournament) {
        if (selectedTournament == null || currentPlayer == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd rejestracji", "Brak danych turnieju lub gracza. Spróbuj ponownie.");
            return;
        }

        // Dodano potwierdzenie przed rejestracją
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Potwierdź rejestrację");
        confirmAlert.setHeaderText("Rejestracja do turnieju: " + selectedTournament.getName());
        confirmAlert.setContentText("Czy na pewno chcesz zarejestrować się w tym turnieju?");
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Ta metoda już zawiera logikę sprawdzania statusu turnieju, wolnych miejsc i czy gracz już jest zapisany
                Tournament updatedTournament = tournamentDAO.addPlayerToTournament(selectedTournament.getId(), currentPlayer.getPlayersTableId());

                // Po pomyślnej rejestracji odśwież widok, aby pokazać zaktualizowane dane (wolne miejsca, status)
                // Możesz zaktualizować tylko ten jeden turniej w ObservableList zamiast ładować wszystkie od nowa
                for (int i = 0; i < tournamentList.size(); i++) {
                    if (tournamentList.get(i).getId() == updatedTournament.getId()) {
                        tournamentList.set(i, updatedTournament);
                        break;
                    }
                }
                tournamentsTable.refresh(); // Odśwież tabelę

                showAlert(Alert.AlertType.INFORMATION, "Sukces", "Pomyślnie zarejestrowano w turnieju '" + selectedTournament.getName() + "'!");
            } catch (IllegalStateException e) {
                // Obsługa specyficznych błędów biznesowych (turniej zamknięty, pełny, gracz już zapisany)
                showAlert(Alert.AlertType.WARNING, "Nie można zarejestrować", e.getMessage());
                loadTournaments(); // Odśwież listę, aby przyciski odzwierciedlały aktualny stan
            } catch (SQLException e) {
                // Obsługa błędów bazy danych
                showAlert(Alert.AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas rejestracji: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                // Ogólna obsługa innych nieprzewidzianych błędów
                showAlert(Alert.AlertType.ERROR, "Wystąpił błąd", "Nieoczekiwany błąd podczas rejestracji: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
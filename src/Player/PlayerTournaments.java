package src.Player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import src.dao.TournamentDAO;
import src.model.Player;
import src.model.Tournament;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

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
    @FXML private TableColumn<Tournament, String> statusColumn;
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
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
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
                    if (currentTournament != null && currentPlayer != null) {
                        try {
                            boolean isRegistered = tournamentDAO.isPlayerRegisteredForTournament(currentTournament.getId(), currentPlayer.getPlayerId());
                            if (isRegistered) {
                                registerButton.setDisable(true);
                                registerButton.setText("Zarejestrowany");
                            } else if (currentTournament.getFreeSlots() <= 0) {
                                registerButton.setDisable(true);
                                registerButton.setText("Brak miejsc");
                            } else if (!currentTournament.getStatus().equals("OTWARTY")) {
                                registerButton.setDisable(true);
                                registerButton.setText("Zamknięty");
                            }
                            else {
                                registerButton.setDisable(false);
                                registerButton.setText("Zarejestruj się");
                            }
                        } catch (SQLException e) {
                            registerButton.setDisable(true);
                            registerButton.setText("Błąd");
                            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie można sprawdzić statusu rejestracji. " + e.getMessage());
                        }
                    } else {
                        registerButton.setDisable(true);
                        registerButton.setText("Brak danych");
                    }
                    setGraphic(registerButton);
                }
            }
        });

        tournamentsTable.setItems(tournamentList);
        loadTournaments();

        tournamentsTable.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                Tournament selectedTournament = tournamentsTable.getSelectionModel().getSelectedItem();
                if (selectedTournament != null) {
                    if (playerDashboardController != null) {
                        playerDashboardController.handleShowTournamentInfo(selectedTournament);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Błąd", "Nie można wyświetlić szczegółów turnieju. Błąd wewnętrzny aplikacji.");
                    }
                }
            }
        });
    }

    public void initData(Player player) {
        this.currentPlayer = player;
        loadTournaments();
    }

    public void setPlayerDashboardController(PlayerDashboard controller) {
        this.playerDashboardController = controller;
    }

    private void loadTournaments() {
        try {
            List<Tournament> tournaments = tournamentDAO.getAllTournaments();
            tournamentList.setAll(tournaments);
            tournamentsTable.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd ładowania", "Nie udało się załadować listy turniejów.");
        }
    }
    private void handleRegisterForTournament(Tournament selectedTournament) {
        if (selectedTournament == null || currentPlayer == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd rejestracji", "Brak danych turnieju lub gracza. Spróbuj ponownie.");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Potwierdź rejestrację");
        confirmAlert.setHeaderText("Rejestracja do turnieju: " + selectedTournament.getName());
        confirmAlert.setContentText("Czy na pewno chcesz zarejestrować się w tym turnieju?");
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                tournamentDAO.addPlayerToTournament(selectedTournament.getId(), currentPlayer.getPlayerId());
                loadTournaments();
                showAlert(Alert.AlertType.INFORMATION, "Sukces", "Pomyślnie zarejestrowano w turnieju '" + selectedTournament.getName() + "'!");
            } catch (IllegalStateException e) {
                showAlert(Alert.AlertType.WARNING, "Nie można zarejestrować", e.getMessage());
                loadTournaments();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas rejestracji: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
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
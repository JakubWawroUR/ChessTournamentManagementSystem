package src.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.dao.TournamentDAO;
import src.model.Tournament;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminTournamentController implements Initializable {

    @FXML private TableView<Tournament> tournamentTable;
    @FXML private TableColumn<Tournament, Integer> idColumn;
    @FXML private TableColumn<Tournament, String> tournamentNameColumn;
    @FXML private TableColumn<Tournament, String> startDateColumn;
    @FXML private TableColumn<Tournament, String> endDateColumn;
    @FXML private TableColumn<Tournament, Integer> maxSlotsColumn;
    @FXML private TableColumn<Tournament, Integer> freeSlotsColumn;
    @FXML private TableColumn<Tournament, String> statusColumn;
    @FXML private TableColumn<Tournament, Void> actionsColumn;
    @FXML private TextField tournamentNameField;
    @FXML private TextField maxSlotsField;
    @FXML private Button addTournamentButton;
    @FXML private Button updateTournamentButton;
    @FXML private Button cancelEditButton;
    @FXML private Button endTournamentButton;
    @FXML private TextField tournamentIdField;

    private ObservableList<Tournament> tournamentList;
    private TournamentDAO tournamentDAO;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.tournamentDAO = new TournamentDAO();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        tournamentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        maxSlotsColumn.setCellValueFactory(new PropertyValueFactory<>("maxSlots"));
        freeSlotsColumn.setCellValueFactory(new PropertyValueFactory<>("freeSlots"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        setupActionsColumn();
        tournamentList = FXCollections.observableArrayList();
        loadTournamentDataFromDatabase();
        tournamentTable.setItems(tournamentList);
        tournamentTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tournamentTable.getSelectionModel().getSelectedItem() != null) {
                Tournament selectedTournament = tournamentTable.getSelectionModel().getSelectedItem();
                handleShowTournamentMatches(selectedTournament);
            }
        });
        tournamentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                boolean isClosable = newSelection.getStatus().equals("ZAMKNIĘTY") || newSelection.getStatus().equals("W TRAKCIE");
                endTournamentButton.setVisible(isClosable);
                endTournamentButton.setManaged(isClosable);
            } else {
                endTournamentButton.setVisible(false);
                endTournamentButton.setManaged(false);
            }
        });
        resetForm();
    }

    private void handleShowTournamentMatches(Tournament tournament) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminGames.fxml"));
            Parent root = loader.load();
            AdminGames gamesController = loader.getController();
            gamesController.setTournament(tournament);
            Stage stage = new Stage();
            stage.setTitle("Mecze turnieju: " + tournament.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Błąd", "Nie udało się załadować widoku meczów: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<Tournament, Void>() {
            private final Button editButton = new Button("Edytuj");
            private final Button deleteButton = new Button("Usuń");
            private final HBox pane = new HBox(5, editButton, deleteButton);
            {
                pane.setAlignment(Pos.CENTER);
                editButton.setOnAction(event -> {
                    Tournament tournament = getTableView().getItems().get(getIndex());
                    handleEditRequest(tournament);
                });
                deleteButton.setOnAction(event -> {
                    Tournament tournament = getTableView().getItems().get(getIndex());
                    handleDeleteTournament(tournament);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void handleEditRequest(Tournament tournament) {
        tournamentIdField.setText(String.valueOf(tournament.getId()));
        tournamentNameField.setText(tournament.getName());
        maxSlotsField.setText(String.valueOf(tournament.getMaxSlots()));
        addTournamentButton.setVisible(false);
        addTournamentButton.setManaged(false);
        updateTournamentButton.setVisible(true);
        updateTournamentButton.setManaged(true);
        if (cancelEditButton != null) {
            cancelEditButton.setVisible(true);
            cancelEditButton.setManaged(true);
        }
        endTournamentButton.setVisible(false);
        endTournamentButton.setManaged(false);
        showAlert(AlertType.INFORMATION, "Edycja turnieju", "Edytujesz turniej: " + tournament.getName());
    }

    @FXML
    private void handleUpdateTournament() {
        if (tournamentIdField.getText().isEmpty()) {
            showAlert(AlertType.ERROR, "Błąd edycji", "Nie wybrano turnieju do edycji. Proszę wybrać turniej z tabeli.");
            resetForm();
            return;
        }

        int id = Integer.parseInt(tournamentIdField.getText());
        String name = tournamentNameField.getText();
        int newMaxSlots;
        try {
            newMaxSlots = Integer.parseInt(maxSlotsField.getText());
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Błąd edycji", "Pole 'Maksymalna liczba miejsc' musi być liczbą całkowitą.");
            return;
        }
        if (name.isEmpty()) {
            showAlert(AlertType.ERROR, "Błąd edycji", "Proszę wypełnić nazwę turnieju.");
            return;
        }
        try {
            Tournament existingTournament = tournamentList.stream()
                    .filter(t -> t.getId() == id)
                    .findFirst()
                    .orElse(null);

            if (existingTournament == null) {
                showAlert(AlertType.ERROR, "Błąd edycji", "Nie znaleziono turnieju o podanym ID do aktualizacji.");
                return;
            }
            String currentStartDate = existingTournament.getStartDate();
            String currentEndDate = existingTournament.getEndDate();
            String currentStatus = existingTournament.getStatus();
            int currentFreeSlots = existingTournament.getFreeSlots();
            int currentMaxSlots = existingTournament.getMaxSlots();
            int registeredPlayersCount = currentMaxSlots - currentFreeSlots;
            if (newMaxSlots < registeredPlayersCount) {
                showAlert(AlertType.ERROR, "Błąd edycji",
                        "Nowa maksymalna liczba miejsc (" + newMaxSlots + ") nie może być mniejsza niż obecna liczba zarejestrowanych graczy (" + registeredPlayersCount + ").");
                return;
            }
            int newFreeSlots;
            String newStatus = currentStatus;
            if (newMaxSlots < currentMaxSlots) {
                newFreeSlots = newMaxSlots - registeredPlayersCount;
                if (newFreeSlots < 0) newFreeSlots = 0;
                if (newFreeSlots == 0 && newStatus.equals("OTWARTY")) {
                    newStatus = "ZAMKNIĘTY";
                }
            } else {
                newFreeSlots = currentFreeSlots + (newMaxSlots - currentMaxSlots);
                if (newStatus.equals("ZAMKNIĘTY") && newFreeSlots > 0) {
                    newStatus = "OTWARTY";
                }
            }
            newFreeSlots = Math.min(newFreeSlots, newMaxSlots);
            newFreeSlots = Math.max(0, newFreeSlots);
            Tournament updatedTournament = new Tournament(id, name, currentStartDate, currentEndDate, newMaxSlots, newFreeSlots, newStatus);
            tournamentDAO.updateTournament(updatedTournament);
            for (int i = 0; i < tournamentList.size(); i++) {
                if (tournamentList.get(i).getId() == id) {
                    tournamentList.set(i, updatedTournament);
                    break;
                }
            }
            tournamentTable.refresh();
            showAlert(AlertType.INFORMATION, "Sukces", "Turniej '" + name + "' został pomyślnie zaktualizowany.");
            resetForm();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas aktualizacji turnieju: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteTournament(Tournament tournament) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Potwierdź usunięcie");
        alert.setHeaderText("Czy na pewno chcesz usunąć turniej?");
        alert.setContentText("Usunięcie turnieju '" + tournament.getName() + "' (ID: " + tournament.getId() + ") jest nieodwracalne.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                tournamentDAO.deleteTournament(tournament.getId());
                tournamentList.remove(tournament);
                tournamentTable.refresh();
                showAlert(AlertType.INFORMATION, "Sukces", "Turniej '" + tournament.getName() + "' został pomyślnie usunięty.");
                resetForm();
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas usuwania turnieju: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAddTournament() {
        String name = tournamentNameField.getText();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(3);

        int maxSlots;

        try {
            maxSlots = Integer.parseInt(maxSlotsField.getText());
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Błąd dodawania", "Pole 'Maksymalna liczba miejsc' musi być liczbą całkowitą.");
            return;
        }

        if (name.isEmpty()) {
            showAlert(AlertType.ERROR, "Błąd dodawania", "Proszę wypełnić nazwę turnieju.");
            return;
        }
        String formattedStartDate = startDate.format(DATE_FORMATTER);
        String formattedEndDate = endDate.format(DATE_FORMATTER);

        try {
            Tournament newTournament = new Tournament(name, formattedStartDate, formattedEndDate, maxSlots);
            tournamentDAO.addTournament(newTournament);

            tournamentList.add(newTournament);
            tournamentTable.refresh();

            showAlert(AlertType.INFORMATION, "Sukces", "Turniej '" + name + "' został pomyślnie dodany.");
            System.out.println("Turniej '" + name + "' został pomyślnie dodany.");
            resetForm();

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas dodawania turnieju do bazy danych: " + e.getMessage());
            System.err.println("Błąd podczas dodawania turnieju: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearForm() {
        resetForm();
        showAlert(AlertType.INFORMATION, "Formularz wyczyszczony", "Formularz został wyczyszczony.");
    }

    private void resetForm() {
        tournamentIdField.clear();
        tournamentNameField.clear();
        maxSlotsField.clear();
        addTournamentButton.setVisible(true);
        addTournamentButton.setManaged(true);
        updateTournamentButton.setVisible(false);
        updateTournamentButton.setManaged(false);
        if (cancelEditButton != null) {
            cancelEditButton.setVisible(false);
            cancelEditButton.setManaged(false);
        }
        endTournamentButton.setVisible(false);
        endTournamentButton.setManaged(false);
    }

    private void loadTournamentDataFromDatabase() {
        try {
            List<Tournament> tournaments = tournamentDAO.getAllTournaments();
            tournamentList.setAll(tournaments);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Błąd ładowania danych", "Nie udało się załadować danych turniejów z bazy danych. Sprawdź połączenie.");
        }
    }

    @FXML
    private void handleEndTournament() {
        Tournament selectedTournament = tournamentTable.getSelectionModel().getSelectedItem();
        if (selectedTournament == null) {
            showAlert(AlertType.ERROR, "Błąd", "Proszę wybrać turniej do zakończenia.");
            return;
        }

        if (!selectedTournament.getStatus().equals("ZAMKNIĘTY") && !selectedTournament.getStatus().equals("W TRAKCIE")) {
            showAlert(AlertType.WARNING, "Błąd", "Możesz zakończyć tylko turniej, który jest 'ZAMKNIĘTY' lub 'W TRAKCIE'. Aktualny status: " + selectedTournament.getStatus());
            return;
        }

        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Potwierdź zakończenie turnieju");
        confirmation.setHeaderText("Czy na pewno chcesz zakończyć turniej '" + selectedTournament.getName() + "'?");
        confirmation.setContentText("Po zakończeniu turnieju, jego status zostanie zmieniony na 'ZAKOŃCZONY' i nie będzie można już zmieniać wyników meczów ani dodawać graczy.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                tournamentDAO.updateTournamentStatus(selectedTournament.getId(), "ZAKOŃCZONY");
                selectedTournament.setStatus("ZAKOŃCZONY");
                tournamentDAO.calculateAndApplyTournamentStats(selectedTournament.getId());

                tournamentTable.refresh();
                resetForm();

                showAlert(AlertType.INFORMATION, "Sukces", "Turniej '" + selectedTournament.getName() + "' został pomyślnie zakończony.");
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas zakończenia turnieju: " + e.getMessage());
                e.printStackTrace();
            }
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
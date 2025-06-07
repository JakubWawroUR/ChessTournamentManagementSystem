package src.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import src.dao.TournamentDAO;
import src.model.Tournament;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminTournamentController implements Initializable {

    // Poprawiono z userTable na tournamentTable
    @FXML private TableView<Tournament> tournamentTable;
    @FXML private TableColumn<Tournament, Integer> idColumn;
    @FXML private TableColumn<Tournament, String> tournamentNameColumn; // Pozostawiono tournamentNameColumn jak w FXML
    @FXML private TableColumn<Tournament, String> startDateColumn;
    @FXML private TableColumn<Tournament, String> endDateColumn;
    @FXML private TableColumn<Tournament, Integer> maxSlotsColumn; // Dodano
    @FXML private TableColumn<Tournament, Integer> freeSlotsColumn; // Dodano
    @FXML private TableColumn<Tournament, Void> actionsColumn;

    // Pola FXML dla formularza dodawania/edycji
    @FXML private TextField tournamentNameField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField maxSlotsField; // Dodano
    @FXML private TextField freeSlotsField; // Dodano
    @FXML private Button addTournamentButton;
    @FXML private Button updateTournamentButton;
    @FXML private Button cancelEditButton; // Będziesz potrzebować tego przycisku w FXML
    @FXML private TextField tournamentIdField;

    private ObservableList<Tournament> tournamentList;
    private TournamentDAO tournamentDAO;

    // Formatter do konwersji String <-> LocalDate
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tournamentDAO = new TournamentDAO();

        // Ustawienie PropertyValueFactory dla kolumn tabeli
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        tournamentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name")); // Zmieniono z tournamentName
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        maxSlotsColumn.setCellValueFactory(new PropertyValueFactory<>("maxSlots")); // Dodano
        freeSlotsColumn.setCellValueFactory(new PropertyValueFactory<>("freeSlots")); // Dodano

        setupActionsColumn();

        tournamentList = FXCollections.observableArrayList();
        loadTournamentDataFromDatabase();
        tournamentTable.setItems(tournamentList);

        // Początkowy stan formularza (tryb dodawania)
        resetForm();
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
        // Konwersja String na LocalDate
        startDatePicker.setValue(LocalDate.parse(tournament.getStartDate(), DATE_FORMATTER));
        endDatePicker.setValue(LocalDate.parse(tournament.getEndDate(), DATE_FORMATTER));
        maxSlotsField.setText(String.valueOf(tournament.getMaxSlots()));
        freeSlotsField.setText(String.valueOf(tournament.getFreeSlots()));

        addTournamentButton.setVisible(false);
        addTournamentButton.setManaged(false);
        updateTournamentButton.setVisible(true);
        updateTournamentButton.setManaged(true);
        // Upewnij się, że cancelEditButton jest widoczny i zarządzany
        if (cancelEditButton != null) {
            cancelEditButton.setVisible(true);
            cancelEditButton.setManaged(true);
        }

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
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        int maxSlots;
        int freeSlots;

        // Walidacja liczb
        try {
            maxSlots = Integer.parseInt(maxSlotsField.getText());
            freeSlots = Integer.parseInt(freeSlotsField.getText());
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Błąd edycji", "Pola 'Maksymalna liczba miejsc' i 'Wolne miejsca' muszą być liczbami całkowitymi.");
            return;
        }

        if (name.isEmpty() || startDate == null || endDate == null) {
            showAlert(AlertType.ERROR, "Błąd edycji", "Proszę wypełnić wszystkie pola.");
            return;
        }
        if (endDate.isBefore(startDate)) {
            showAlert(AlertType.ERROR, "Błąd edycji", "Data zakończenia nie może być wcześniejsza niż data rozpoczęcia.");
            return;
        }
        if (freeSlots > maxSlots) {
            showAlert(AlertType.ERROR, "Błąd edycji", "Wolne miejsca nie mogą być większe niż maksymalna liczba miejsc.");
            return;
        }
        if (freeSlots < 0 || maxSlots < 0) {
            showAlert(AlertType.ERROR, "Błąd edycji", "Liczba miejsc nie może być ujemna.");
            return;
        }

        String formattedStartDate = startDate.format(DATE_FORMATTER);
        String formattedEndDate = endDate.format(DATE_FORMATTER);

        try {
            Tournament updatedTournament = new Tournament(id, name, formattedStartDate, formattedEndDate, maxSlots, freeSlots);
            tournamentDAO.updateTournament(updatedTournament);

            for (int i = 0; i < tournamentList.size(); i++) {
                if (tournamentList.get(i).getId() == id) {
                    tournamentList.set(i, updatedTournament);
                    break;
                }
            }

            showAlert(AlertType.INFORMATION, "Sukces", "Turniej '" + name + "' został pomyślnie zaktualizowany.");
            System.out.println("Turniej '" + name + "' został pomyślnie zaktualizowany.");
            resetForm();

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas aktualizacji turnieju: " + e.getMessage());
            System.err.println("Błąd podczas aktualizacji turnieju: " + e.getMessage());
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

                showAlert(AlertType.INFORMATION, "Sukces", "Turniej '" + tournament.getName() + "' został pomyślnie usunięty.");
                System.out.println("Turniej '" + tournament.getName() + "' został pomyślnie usunięty.");
                resetForm();

            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas usuwania turnieju: " + e.getMessage());
                System.err.println("Błąd podczas usuwania turnieju: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAddTournament() {
        String name = tournamentNameField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        int maxSlots;
        int freeSlots;

        // Walidacja liczb
        try {
            maxSlots = Integer.parseInt(maxSlotsField.getText());
            freeSlots = Integer.parseInt(freeSlotsField.getText());
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Błąd dodawania", "Pola 'Maksymalna liczba miejsc' i 'Wolne miejsca' muszą być liczbami całkowitymi.");
            return;
        }

        if (name.isEmpty() || startDate == null || endDate == null) {
            showAlert(AlertType.ERROR, "Błąd dodawania", "Proszę wypełnić wszystkie pola.");
            return;
        }
        if (endDate.isBefore(startDate)) {
            showAlert(AlertType.ERROR, "Błąd dodawania", "Data zakończenia nie może być wcześniejsza niż data rozpoczęcia.");
            return;
        }
        if (freeSlots > maxSlots) {
            showAlert(AlertType.ERROR, "Błąd dodawania", "Wolne miejsca nie mogą być większe niż maksymalna liczba miejsc.");
            return;
        }
        if (freeSlots < 0 || maxSlots < 0) {
            showAlert(AlertType.ERROR, "Błąd dodawania", "Liczba miejsc nie może być ujemna.");
            return;
        }

        String formattedStartDate = startDate.format(DATE_FORMATTER);
        String formattedEndDate = endDate.format(DATE_FORMATTER);

        try {
            Tournament newTournament = new Tournament(name, formattedStartDate, formattedEndDate, maxSlots, freeSlots);
            tournamentDAO.addTournament(newTournament);
            tournamentList.add(newTournament);

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
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        maxSlotsField.clear();
        freeSlotsField.clear();

        addTournamentButton.setVisible(true);
        addTournamentButton.setManaged(true);
        updateTournamentButton.setVisible(false);
        updateTournamentButton.setManaged(false);
        if (cancelEditButton != null) {
            cancelEditButton.setVisible(false);
            cancelEditButton.setManaged(false);
        }
    }

    private void loadTournamentDataFromDatabase() {
        System.out.println("AdminTournamentController: Rozpoczynam ładowanie danych turniejów z bazy.");
        try {
            List<Tournament> tournaments = tournamentDAO.getAllTournaments();
            System.out.println("AdminTournamentController: Pobrana lista z DAO ma " + tournaments.size() + " elementów.");
            tournamentList.setAll(tournaments);
            System.out.println("AdminTournamentController: ObservableList ma teraz " + tournamentList.size() + " elementów.");

        } catch (Exception e) {
            System.err.println("Wystąpił błąd podczas ładowania danych turniejów z bazy danych:");
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Błąd ładowania danych", "Nie udało się załadować danych turniejów z bazy danych. Sprawdź połączenie.");
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
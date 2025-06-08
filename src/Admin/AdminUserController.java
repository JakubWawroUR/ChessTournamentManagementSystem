package src.Admin;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import src.dao.UserDAO;
import src.dao.PlayerDAO;
import src.model.Role;
import src.model.User;
import src.model.Player;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminUserController implements Initializable {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> loginColumn;
    @FXML private TableColumn<User, String> passwordColumn;
    @FXML private TableColumn<User, String> firstnameColumn;
    @FXML private TableColumn<User, String> lastnameColumn;
    @FXML private TableColumn<User, Role> roleColumn;
    @FXML private TableColumn<User, Integer> rankingColumn;
    @FXML private TableColumn<User, Void> actionsColumn;

    @FXML private TextField userIdField;
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private TextField firstnameField;
    @FXML private TextField lastnameField;
    @FXML private ComboBox<Role> roleComboBox;

    @FXML private Label rankingLabel;
    @FXML private TextField rankingField;

    @FXML private Button addEditUserButton;
    @FXML private Button updateSaveUserButton;
    @FXML private Button clearFormButton;

    @FXML private Button btnShowAdmins;
    @FXML private Button btnShowPlayers;
    @FXML private Button btnShowAll;

    private UserDAO userDAO = new UserDAO();
    private PlayerDAO playerDAO = new PlayerDAO();
    private ObservableList<User> userList = FXCollections.observableArrayList(); // Inicjalizacja userList

    // Nowa zmienna do śledzenia aktywnego filtru
    private FilterType activeFilter = FilterType.PLAYERS;

    // Definicja typu wyliczeniowego dla filtrów
    private enum FilterType {
        ALL,
        PLAYERS,
        ADMINS
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Ustawienie cel dla kolumn w TableView
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        loginColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLogin()));
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        firstnameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        lastnameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        roleColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getRole()));

        rankingColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (user instanceof Player) {
                return new SimpleIntegerProperty(((Player) user).getRanking()).asObject();
            }
            return new SimpleIntegerProperty(0).asObject(); // Domyślnie 0 dla nie-graczy
        });

        // Dostosowanie wyświetlania kolumny rankingu
        rankingColumn.setCellFactory(tc -> new TableCell<User, Integer>() {
            @Override
            protected void updateItem(Integer ranking, boolean empty) {
                super.updateItem(ranking, empty);
                if (empty || getTableRow().getItem() == null || !(getTableRow().getItem() instanceof Player)) {
                    setText(null); // Brak tekstu dla pustych komórek lub nie-graczy
                } else {
                    setText(ranking.toString());
                }
            }
        });

        // Ustawienie elementów ComboBoxa ról i dodanie listenera
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        roleComboBox.valueProperty().addListener((obs, oldRole, newRole) -> {
            boolean isPlayer = (newRole == Role.GRACZ);
            rankingLabel.setVisible(isPlayer);
            rankingLabel.setManaged(isPlayer); // Kontroluj zarządzanie układem
            rankingField.setVisible(isPlayer);
            rankingField.setManaged(isPlayer); // Kontroluj zarządzanie układem
            if (!isPlayer) {
                rankingField.clear(); // Wyczyść pole rankingu, jeśli rola nie jest GRACZ
            }
        });

        setupActionsColumn(); // Konfiguracja kolumny akcji (Edytuj/Usuń)

        // Najpierw przypisz zainicjalizowaną userList do TableView
        userTable.setItems(userList);
        // Ładuje tylko graczy przy starcie i ustawia aktywny filtr
        loadUsersByRole(Role.GRACZ);
        activeFilter = FilterType.PLAYERS;

        // Ustawienie akcji dla przycisków i aktualizacja aktywnego filtra
        addEditUserButton.setOnAction(event -> handleAddUser());
        updateSaveUserButton.setOnAction(event -> handleUpdateUser());
        clearFormButton.setOnAction(event -> resetForm());

        btnShowAdmins.setOnAction(event -> {
            loadUsersByRole(Role.ADMINISTRATOR);
            activeFilter = FilterType.ADMINS; // Zaktualizuj filtr po kliknięciu
        });
        btnShowPlayers.setOnAction(event -> {
            loadUsersByRole(Role.GRACZ);
            activeFilter = FilterType.PLAYERS; // Zaktualizuj filtr po kliknięciu
        });
        if (btnShowAll != null) {
            btnShowAll.setOnAction(event -> {
                loadAllUsers();
                activeFilter = FilterType.ALL; // Zaktualizuj filtr po kliknięciu
            });
        }

        resetForm(); // Wyczyść formularz przy starcie
    }

    private void setupActionsColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = param -> new TableCell<>() {
            private final Button editButton = new Button("Edytuj");
            private final Button deleteButton = new Button("Usuń");
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                pane.setAlignment(Pos.CENTER); // Wyśrodkuj przyciski w komórce

                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) {
                        handleEditRequest(user); // Obsługa żądania edycji
                    }
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) {
                        handleDeleteUser(user); // Obsługa żądania usunięcia
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null); // Nic nie wyświetlaj dla pustych komórek
                } else {
                    setGraphic(pane); // Wyświetl przyciski
                }
            }
        };

        actionsColumn.setCellFactory(cellFactory);
    }

    // Metoda do obsługi żądania edycji użytkownika z tabeli
    private void handleEditRequest(User user) {
        userIdField.setText(String.valueOf(user.getId()));
        loginField.setText(user.getLogin());
        passwordField.setText(user.getPassword());
        firstnameField.setText(user.getFirstName());
        lastnameField.setText(user.getLastName());
        roleComboBox.setValue(user.getRole());

        if (user.getRole() == Role.GRACZ) {
            rankingLabel.setVisible(true);
            rankingLabel.setManaged(true);
            rankingField.setVisible(true);
            rankingField.setManaged(true);
            try {
                if (user instanceof Player) {
                    rankingField.setText(String.valueOf(((Player) user).getRanking()));
                } else {
                    Integer ranking = playerDAO.getPlayerRanking(user.getId());
                    if (ranking != null) {
                        rankingField.setText(String.valueOf(ranking));
                    } else {
                        rankingField.setText("0");
                    }
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się pobrać rankingu: " + e.getMessage());
                e.printStackTrace();
                rankingField.setText("0");
            }
        } else {
            rankingLabel.setVisible(false);
            rankingLabel.setManaged(false);
            rankingField.setVisible(false);
            rankingField.setManaged(false);
            rankingField.clear();
        }

        addEditUserButton.setVisible(false);
        addEditUserButton.setManaged(false);
        updateSaveUserButton.setVisible(true);
        updateSaveUserButton.setManaged(true);
        clearFormButton.setVisible(true);
        clearFormButton.setManaged(true);

        showAlert(Alert.AlertType.INFORMATION, "Edycja użytkownika", "Edytujesz użytkownika: " + user.getLogin());
    }

    // Metoda do dodawania nowego użytkownika
    @FXML
    private void handleAddUser() {
        String login = loginField.getText();
        String password = passwordField.getText();
        String firstname = firstnameField.getText();
        String lastname = lastnameField.getText();
        Role role = roleComboBox.getValue();

        if (login.isEmpty() || password.isEmpty() || firstname.isEmpty() || lastname.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd dodawania", "Proszę wypełnić wszystkie pola.");
            return;
        }

        int ranking = 0;
        if (role == Role.GRACZ) {
            if (rankingField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Błąd dodawania", "Proszę podać ranking dla gracza.");
                return;
            }
            try {
                ranking = Integer.parseInt(rankingField.getText());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd dodawania", "Ranking musi być liczbą całkowitą.");
                return;
            }
        }

        try {
            boolean success = userDAO.registerUser(login, password, firstname, lastname, role);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Sukces", "Użytkownik '" + login + "' został pomyślnie dodany.");
                resetForm();
                refreshTable(); // Odśwież tabelę po dodaniu, zachowując filtr
            } else {
                showAlert(Alert.AlertType.WARNING, "Błąd", "Użytkownik o loginie '" + login + "' już istnieje.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas dodawania użytkownika: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metoda do aktualizacji istniejącego użytkownika
    @FXML
    private void handleUpdateUser() {
        if (userIdField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd edycji", "Nie wybrano użytkownika do edycji. Proszę wybrać użytkownika z tabeli.");
            resetForm();
            return;
        }

        int id = Integer.parseInt(userIdField.getText());
        String login = loginField.getText();
        String password = passwordField.getText();
        String firstname = firstnameField.getText();
        String lastname = lastnameField.getText();
        Role role = roleComboBox.getValue();

        if (login.isEmpty() || password.isEmpty() || firstname.isEmpty() || lastname.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd edycji", "Proszę wypełnić wszystkie pola.");
            return;
        }

        int ranking = 0;
        if (role == Role.GRACZ) {
            if (rankingField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Błąd edycji", "Proszę podać ranking dla gracza.");
                return;
            }
            try {
                ranking = Integer.parseInt(rankingField.getText());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd edycji", "Ranking musi być liczbą całkowitą.");
                return;
            }
        }

        try {
            User updatedUser = new User(id, login, password, firstname, lastname, role);
            userDAO.updateUser(updatedUser);

            if (role == Role.GRACZ) {
                Integer currentRanking = playerDAO.getPlayerRanking(id);
                if (currentRanking != null) {
                    playerDAO.updatePlayerRanking(id, ranking);
                } else {
                    playerDAO.addPlayerDetails(id, ranking);
                }
            } else {
                try {
                    playerDAO.deletePlayerDetails(id);
                } catch (SQLException e) {
                    System.err.println("Błąd podczas usuwania danych gracza (może nie istniały): " + e.getMessage());
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Użytkownik '" + login + "' został pomyślnie zaktualizowany.");
            resetForm();
            refreshTable(); // Odśwież tabelę po aktualizacji, zachowując filtr
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas aktualizacji użytkownika: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metoda do usuwania użytkownika
    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdź usunięcie");
        alert.setHeaderText("Czy na pewno chcesz usunąć użytkownika?");
        alert.setContentText("Usunięcie użytkownika '" + user.getLogin() + "' (ID: " + user.getId() + ") jest nieodwracalne.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                playerDAO.deletePlayerDetails(user.getId());
                userDAO.deleteUser(user.getId());

                showAlert(Alert.AlertType.INFORMATION, "Sukces", "Użytkownik '" + user.getLogin() + "' został pomyślnie usunięty.");
                resetForm();
                refreshTable(); // Odśwież tabelę po usunięciu, zachowując filtr
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas usuwania użytkownika: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Metoda do resetowania/czyszczenia formularza
    @FXML
    private void resetForm() {
        userIdField.clear();
        loginField.clear();
        passwordField.clear();
        firstnameField.clear();
        lastnameField.clear();
        roleComboBox.setValue(null);

        rankingLabel.setVisible(false);
        rankingLabel.setManaged(false);
        rankingField.setVisible(false);
        rankingField.setManaged(false);
        rankingField.clear();

        addEditUserButton.setText("Dodaj Użytkownika");
        addEditUserButton.setVisible(true);
        addEditUserButton.setManaged(true);
        updateSaveUserButton.setVisible(false);
        updateSaveUserButton.setManaged(false);
        clearFormButton.setVisible(false);
        clearFormButton.setManaged(false);
    }

    // Metoda do ładowania wszystkich użytkowników z bazy danych do tabeli
    private void loadAllUsers() {
        System.out.println("Ładowanie wszystkich użytkowników...");
        try {
            List<User> users = userDAO.getAllUsers();
            userList.setAll(users);
            userTable.refresh();
            System.out.println("Załadowano " + users.size() + " użytkowników.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd ładowania", "Nie udało się załadować wszystkich użytkowników: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metoda do ładowania użytkowników według określonej roli (Admin/Gracz)
    private void loadUsersByRole(Role role) {
        System.out.println("Ładowanie użytkowników z rolą: " + role.toString());
        try {
            List<User> allUsers = userDAO.getAllUsers();
            List<User> filteredUsers = allUsers.stream()
                    .filter(user -> user.getRole() == role)
                    .collect(Collectors.toList());
            userList.setAll(filteredUsers);
            userTable.refresh();
            System.out.println("Załadowano " + filteredUsers.size() + " użytkowników z rolą " + role.toString() + ".");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd ładowania", "Nie udało się załadować użytkowników z rolą " + role.toString() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metoda pomocnicza do odświeżania tabeli z uwzględnieniem aktywnego filtra
    private void refreshTable() {
        System.out.println("Odświeżanie tabeli, aktywny filtr: " + activeFilter);
        switch (activeFilter) {
            case ADMINS:
                loadUsersByRole(Role.ADMINISTRATOR);
                break;
            case PLAYERS:
                loadUsersByRole(Role.GRACZ);
                break;
            case ALL:
            default: // Domyślnie lub jeśli FilterType.ALL
                loadAllUsers();
                break;
        }
    }

    // Metoda pomocnicza do wyświetlania alertów
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
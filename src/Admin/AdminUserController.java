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
    private UserDAO userDAO = new UserDAO();
    private PlayerDAO playerDAO = new PlayerDAO();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    private FilterType activeFilter = FilterType.PLAYERS;

    private enum FilterType {
        ALL,
        PLAYERS,
        ADMINS
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

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
            return new SimpleIntegerProperty(0).asObject();
        });

        rankingColumn.setCellFactory(tc -> new TableCell<User, Integer>() {
            @Override
            protected void updateItem(Integer ranking, boolean empty) {
                super.updateItem(ranking, empty);
                if (empty || getTableRow().getItem() == null || !(getTableRow().getItem() instanceof Player)) {
                    setText(null);
                } else {
                    setText(ranking.toString());
                }
            }
        });

        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        roleComboBox.valueProperty().addListener((obs, oldRole, newRole) -> {
            boolean isPlayer = (newRole == Role.GRACZ);
            rankingLabel.setVisible(isPlayer);
            rankingLabel.setManaged(isPlayer);
            rankingField.setVisible(isPlayer);
            rankingField.setManaged(isPlayer);
            if (!isPlayer) {
                rankingField.clear();
            }
        });

        setupActionsColumn();

        userTable.setItems(userList);
        loadUsersByRole(Role.GRACZ);
        activeFilter = FilterType.PLAYERS;

        addEditUserButton.setOnAction(event -> handleAddUser());
        updateSaveUserButton.setOnAction(event -> handleUpdateUser());
        clearFormButton.setOnAction(event -> resetForm());

        btnShowAdmins.setOnAction(event -> {
            loadUsersByRole(Role.ADMINISTRATOR);
            activeFilter = FilterType.ADMINS;
        });
        btnShowPlayers.setOnAction(event -> {
            loadUsersByRole(Role.GRACZ);
            activeFilter = FilterType.PLAYERS;
        });


        resetForm();
    }

    private void setupActionsColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = param -> new TableCell<>() {
            private final Button editButton = new Button("Edytuj");
            private final Button deleteButton = new Button("Usuń");
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                pane.setAlignment(Pos.CENTER);

                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) {
                        handleEditRequest(user);
                    }
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) {
                        handleDeleteUser(user);
                    }
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
        };

        actionsColumn.setCellFactory(cellFactory);
    }

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
                refreshTable();
            } else {
                showAlert(Alert.AlertType.WARNING, "Błąd", "Użytkownik o loginie '" + login + "' już istnieje.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas dodawania użytkownika: " + e.getMessage());
            e.printStackTrace();
        }
    }


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
                Integer playerTableId = playerDAO.getPlayersTableIdByUserId(id);
                if (playerTableId != null) {
                    playerDAO.updatePlayerRanking(playerTableId, ranking);
                } else {
                    playerDAO.addPlayerDetails(id, ranking);
                }
            } else {
                try {
                    Integer playerTableIdToDelete = playerDAO.getPlayersTableIdByUserId(id);
                    if (playerTableIdToDelete != null) {
                        playerDAO.deletePlayerDetails(playerTableIdToDelete);
                    }
                } catch (SQLException e) {
                    System.err.println("Błąd podczas usuwania danych gracza (może nie istniały): " + e.getMessage());
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Użytkownik '" + login + "' został pomyślnie zaktualizowany.");
            resetForm();
            refreshTable();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas aktualizacji użytkownika: " + e.getMessage());
            e.printStackTrace();
        }
    }
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
                refreshTable();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd bazy danych", "Wystąpił błąd podczas usuwania użytkownika: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

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

    private void loadAllUsers() {
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

    private void loadUsersByRole(Role role) {
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

    private void refreshTable() {
        switch (activeFilter) {
            case ADMINS:
                loadUsersByRole(Role.ADMINISTRATOR);
                break;
            case PLAYERS:
                loadUsersByRole(Role.GRACZ);
                break;
            case ALL:
            default:
                loadAllUsers();
                break;
        }
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
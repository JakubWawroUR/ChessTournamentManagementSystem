// W pliku src/Player/PlayerProfile.java

package src.Player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import src.model.Game;
import src.model.Player;
import src.dao.PlayerDAO;
import src.model.Tournament; // Dodane, jeśli używasz go w PlayerProfile (niekoniecznie)

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.List; // Dodaj ten import

public class PlayerProfile implements Initializable {

    @FXML private AnchorPane contentPane;

    // --- Labels for player info ---
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label rankingLabel;
    @FXML private Label recordLabel;

    // --- Table for match history ---
    @FXML private TableView<Game> matchHistoryTable;
    @FXML private TableColumn<Game, String> tournamentNameColumn;
    @FXML private TableColumn<Game, String> opponentColumn;
    @FXML private TableColumn<Game, String> resultColumn;

    private Player currentPlayer;
    private PlayerDAO playerDAO;

    private ObservableList<Game> playerGamesList;

    // Referencja do PlayerDashboard (jeśli chcesz, aby PlayerProfile mógł wracać/odświeżać Dashboard)
    private PlayerDashboard playerDashboardController;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playerDAO = new PlayerDAO();
        playerGamesList = FXCollections.observableArrayList();

        tournamentNameColumn.setCellValueFactory(new PropertyValueFactory<>("tournamentName"));
        opponentColumn.setCellValueFactory(new PropertyValueFactory<>("opponentName"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("individualResultDisplay"));

        matchHistoryTable.setItems(playerGamesList);

        if (firstNameLabel != null) firstNameLabel.setText("");
        if (lastNameLabel != null) lastNameLabel.setText("");
        if (rankingLabel != null) rankingLabel.setText("");
        if (recordLabel != null) recordLabel.setText("");
    }

    /**
     * Metoda do inicjalizacji danych gracza po załadowaniu kontrolera.
     * Ta metoda powinna być wywołana z miejsca, które ładuje PlayerProfile.fxml (np. PlayerDashboard).
     * @param player Obiekt Player, którego profil ma być wyświetlony.
     */
    public void initData(Player player) { // Upewnij się, że ta metoda jest zdefiniowana
        this.currentPlayer = player;
        if (currentPlayer != null) {
            if (firstNameLabel != null) firstNameLabel.setText("Imię: " + currentPlayer.getFirstName());
            if (lastNameLabel != null) lastNameLabel.setText("Nazwisko: " + currentPlayer.getLastName());
            if (rankingLabel != null) rankingLabel.setText("Ranking: " + currentPlayer.getRanking());

            // Upewnij się, że calculatePoints() i getRecord() są wywołane, jeśli potrzebujesz ich aktualizacji
            // Te metody powinny być w klasie Player

            if (recordLabel != null) recordLabel.setText("W/D/L: " + currentPlayer.getRecord());

            loadPlayerMatchHistory();
        } else {
            System.err.println("Błąd: Obiekt Player w PlayerProfile jest null.");
        }
    }

    /**
     * Ładuje historię meczów dla bieżącego gracza z bazy danych.
     */
    private void loadPlayerMatchHistory() {
        if (currentPlayer == null) {
            System.err.println("Błąd: Nie można załadować historii meczów. Brak obiektu Player.");
            return;
        }
        try {
            // Upewnij się, że getPlayersTableId() istnieje w klasie Player
            List<Game> games = playerDAO.getAllGamesForPlayer(currentPlayer.getPlayersTableId());
            playerGamesList.setAll(games);
            System.out.println("Załadowano " + games.size() + " meczów dla gracza " + currentPlayer.getLogin());
        } catch (SQLException e) {
            System.err.println("Błąd podczas ładowania historii meczów dla gracza " + currentPlayer.getLogin() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Setter dla referencji do PlayerDashboard.
     * Umożliwia komunikację z głównym kontrolerem dashboardu.
     */
    public void setPlayerDashboardController(PlayerDashboard controller) {
        this.playerDashboardController = controller;
    }

    // Opcjonalne: Metody do obsługi odświeżania danych w razie potrzeby (np. po edycji profilu)
    public void refreshProfileData() {
        if (currentPlayer != null) {
            initData(currentPlayer); // Proste ponowne wywołanie initData
        }
    }
}
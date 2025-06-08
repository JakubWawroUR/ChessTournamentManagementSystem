package src.Player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn; // Import dla TableColumn
import javafx.scene.control.TableView;   // Import dla TableView
import javafx.scene.control.cell.PropertyValueFactory; // Import dla PropertyValueFactory
import src.model.Player;
import src.model.Tournament;
import src.dao.TournamentDAO;
import src.dao.PlayerDAO; // Dodaj import dla PlayerDAO!

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class PlayerTournamentInfo implements Initializable {

    @FXML private Label tournamentNameLabel;
    @FXML private Label tournamentDatesLabel; // Ta etykieta nie jest już używana bezpośrednio, ale zostawiam ją
    @FXML private Label tournamentSlotsLabel;
    @FXML private Label currentPlayerStatusLabel;

    @FXML private Label tournamentStartDateLabel;
    @FXML private Label tournamentEndDateLabel;

    // --- NOWE ELEMENTY DLA TABELI UCZESTNIKÓW ---
    @FXML private TableView<Player> participantsTable; // Tabela dla graczy
    @FXML private TableColumn<Player, String> firstNameColumn; // Kolumna dla imienia
    @FXML private TableColumn<Player, String> lastNameColumn;  // Kolumna dla nazwiska
    @FXML private TableColumn<Player, Integer> rankingColumn; // Kolumna dla rankingu (zmieniono na Integer)
    // --- KONIEC NOWYCH ELEMENTÓW ---

    private Tournament selectedTournament;
    private Player currentPlayer;
    private TournamentDAO tournamentDAO;
    private PlayerDAO playerDAO; // Deklaracja PlayerDAO

    // --- NOWA OBSERWOWALNA LISTA DLA UCZESTNIKÓW ---
    private ObservableList<Player> participantList;
    // --- KONIEC NOWEJ LISTY ---


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tournamentDAO = new TournamentDAO();
        playerDAO = new PlayerDAO(); // Inicjalizacja PlayerDAO

        // Inicjalizacja kolumn dla tabeli uczestników
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName")); // Zakładamy, że Player ma getFirstName()
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));   // Zakładamy, że Player ma getLastName()
        rankingColumn.setCellValueFactory(new PropertyValueFactory<>("ranking"));     // Zakładamy, że Player ma getRanking()

        participantList = FXCollections.observableArrayList();
        participantsTable.setItems(participantList); // Ustawienie listy na tabeli
    }

    /**
     * Metoda wywoływana przez PlayerTournamentsController w celu przekazania danych.
     * @param tournament Wybrany turniej.
     * @param player Bieżący zalogowany gracz.
     */
    public void initData(Tournament tournament, Player player) {
        this.selectedTournament = tournament;
        this.currentPlayer = player;

        if (selectedTournament != null) {
            tournamentNameLabel.setText(selectedTournament.getName());
            tournamentSlotsLabel.setText(selectedTournament.getFreeSlots() + " / " + selectedTournament.getMaxSlots());
            tournamentStartDateLabel.setText(selectedTournament.getStartDate());
            tournamentEndDateLabel.setText(selectedTournament.getEndDate());

            if (currentPlayer != null && currentPlayer.getPlayersTableId() != 0) {
                try {
                    boolean isRegistered = tournamentDAO.isPlayerRegisteredForTournament(selectedTournament.getId(), currentPlayer.getPlayersTableId());
                    if (isRegistered) {
                        currentPlayerStatusLabel.setText("Jesteś zapisany!");
                    } else {
                        currentPlayerStatusLabel.setText("Nie jesteś zapisany.");
                    }
                } catch (SQLException e) {
                    System.err.println("Błąd podczas sprawdzania rejestracji w PlayerTournamentInfoController: " + e.getMessage());
                    currentPlayerStatusLabel.setText("Błąd wczytywania statusu.");
                }
            } else {
                currentPlayerStatusLabel.setText("Brak danych gracza.");
            }

            // --- ŁADOWANIE DANYCH UCZESTNIKÓW ---
            loadTournamentParticipants(selectedTournament.getId());
            // --- KONIEC ŁADOWANIA DANYCH UCZESTNIKÓW ---

        } else {
            tournamentNameLabel.setText("Brak danych turnieju.");
            tournamentSlotsLabel.setText("");
            tournamentStartDateLabel.setText("");
            tournamentEndDateLabel.setText("");
            currentPlayerStatusLabel.setText("Brak danych.");
            participantList.clear(); // Wyczyść tabelę uczestników
        }
    }

    /**
     * Ładuje listę graczy zarejestrowanych w danym turnieju do tabeli.
     * @param tournamentId ID turnieju, dla którego ma być pobrana lista graczy.
     */
    private void loadTournamentParticipants(int tournamentId) {
        try {
            // Zakładamy, że PlayerDAO ma metodę getPlayersInTournament(int tournamentId)
            List<Player> participants = playerDAO.getPlayersInTournament(tournamentId);
            participantList.setAll(participants);
            System.out.println("Załadowano " + participants.size() + " uczestników dla turnieju ID: " + tournamentId);
        } catch (SQLException e) {
            System.err.println("Błąd podczas ładowania uczestników turnieju ID " + tournamentId + ": " + e.getMessage());
            e.printStackTrace();
            // Możesz tu wyświetlić alert, jeśli chcesz poinformować użytkownika o błędzie
        }
    }
}
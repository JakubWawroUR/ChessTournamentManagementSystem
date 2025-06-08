package src.dao;

import src.Connection.JDBC;
import src.model.Game; // Dodaj import dla klasy Game
import src.model.Player;
import src.model.Role;
import src.model.Tournament;
import src.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TournamentDAO {

    /**
     * Pobiera wszystkie turnieje z bazy danych.
     *
     * @return Lista obiektów Tournament.
     */
    public List<Tournament> getAllTournaments() {
        List<Tournament> tournaments = new ArrayList<>();
        String query = "SELECT id, name, start_date, end_date, max_slots, free_slots FROM tournaments ORDER BY start_date DESC";

        System.out.println("TournamentDAO: Wykonuję zapytanie SELECT: " + query);

        try (Connection conn = JDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int rowCount = 0;
            while (rs.next()) {
                Tournament tournament = new Tournament(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getInt("max_slots"),
                        rs.getInt("free_slots")
                );
                tournaments.add(tournament);
                rowCount++;
                System.out.println("TournamentDAO: Pobrany turniej: ID=" + tournament.getId() + ", Nazwa=" + tournament.getName() +
                        ", Start=" + tournament.getStartDate() + ", Koniec=" + tournament.getEndDate() +
                        ", Max=" + tournament.getMaxSlots() + ", Wolne=" + tournament.getFreeSlots());
            }
            System.out.println("TournamentDAO: Pomyślnie pobrano " + rowCount + " turniejów z bazy.");

        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania wszystkich turniejów: " + e.getMessage());
            e.printStackTrace();
        }

        return tournaments;
    }

    /**
     * Dodaje nowy turniej do bazy danych.
     *
     * @param tournament Obiekt Tournament do dodania.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void addTournament(Tournament tournament) throws SQLException {
        String query = "INSERT INTO tournaments (name, start_date, end_date, max_slots, free_slots) VALUES (?, ?, ?, ?, ?)";

        System.out.println("TournamentDAO: Wykonuję zapytanie INSERT dla turnieju: " + tournament.getName());

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getStartDate());
            ps.setString(3, tournament.getEndDate());
            ps.setInt(4, tournament.getMaxSlots());
            ps.setInt(5, tournament.getFreeSlots());

            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas INSERT.");

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tournament.setId(generatedKeys.getInt(1));
                    System.out.println("TournamentDAO: Nowe ID turnieju: " + tournament.getId());
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas dodawania turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Aktualizuje istniejący turniej w bazie danych na podstawie jego ID.
     *
     * @param tournament Obiekt Tournament z zaktualizowanymi danymi (musi zawierać ID).
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void updateTournament(Tournament tournament) throws SQLException {
        String query = "UPDATE tournaments SET name = ?, start_date = ?, end_date = ?, max_slots = ?, free_slots = ? WHERE id = ?";

        System.out.println("TournamentDAO: Wykonuję zapytanie UPDATE dla turnieju ID: " + tournament.getId() +
                ", Nazwa: " + tournament.getName() +
                ", Start: " + tournament.getStartDate() +
                ", Koniec: " + tournament.getEndDate() +
                ", Max Miejsc: " + tournament.getMaxSlots() +
                ", Wolne Miejsca: " + tournament.getFreeSlots());

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getStartDate());
            ps.setString(3, tournament.getEndDate());
            ps.setInt(4, tournament.getMaxSlots());
            ps.setInt(5, tournament.getFreeSlots());
            ps.setInt(6, tournament.getId());

            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas UPDATE.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas aktualizacji turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Usuwa turniej z bazy danych po ID.
     *
     * @param id ID turnieju do usunięcia.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void deleteTournament(int id) throws SQLException {
        String query = "DELETE FROM tournaments WHERE id = ?";

        System.out.println("TournamentDAO: Wykonuję zapytanie DELETE dla turnieju ID: " + id);

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);

            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas DELETE.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas usuwania turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Znajduje turniej po jego ID.
     *
     * @param id ID turnieju.
     * @return Obiekt Tournament, jeśli znaleziono, w przeciwnym razie null.
     */
    public Tournament getTournamentById(int id) {
        String query = "SELECT id, name, start_date, end_date, max_slots, free_slots FROM tournaments WHERE id = ?";

        System.out.println("TournamentDAO: Wykonuję zapytanie SELECT by ID dla turnieju ID: " + id);

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("TournamentDAO: Znaleziono turniej o ID: " + id);
                    return new Tournament(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("start_date"),
                            rs.getString("end_date"),
                            rs.getInt("max_slots"),
                            rs.getInt("free_slots")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania turnieju o ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("TournamentDAO: Nie znaleziono turnieju o ID: " + id);
        return null;
    }

    /**
     * Dodaje gracza do turnieju.
     * @param tournamentId ID turnieju.
     * @param playerId ID gracza (z tabeli 'players').
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void addPlayerToTournament(int tournamentId, int playerId) throws SQLException {
        String query = "INSERT INTO tournament_players (tournament_id, player_id) VALUES (?, ?)";
        System.out.println("TournamentDAO: Dodaję gracza ID " + playerId + " do turnieju ID " + tournamentId);
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, playerId);
            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Dodano " + affectedRows + " wiersz do tournament_players.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas dodawania gracza do turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Sprawdza, czy gracz jest już zapisany na dany turniej.
     * @param tournamentId ID turnieju.
     * @param playerId ID gracza (z tabeli 'players').
     * @return true jeśli gracz jest zapisany, false w przeciwnym razie.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public boolean isPlayerRegisteredForTournament(int tournamentId, int playerId) throws SQLException {
        String query = "SELECT COUNT(*) FROM tournament_players WHERE tournament_id = ? AND player_id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas sprawdzania rejestracji gracza: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return false;
    }

    /**
     * Zwiększa lub zmniejsza liczbę wolnych miejsc w turnieju.
     * @param tournamentId ID turnieju.
     * @param changeValue Wartość zmiany (np. -1 dla dołączenia, +1 dla opuszczenia).
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void updateFreeSlots(int tournamentId, int changeValue) throws SQLException {
        String query = "UPDATE tournaments SET free_slots = free_slots + ? WHERE id = ?";
        System.out.println("TournamentDAO: Aktualizuję wolne miejsca dla turnieju ID " + tournamentId + ", zmiana: " + changeValue);
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, changeValue);
            ps.setInt(2, tournamentId);
            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas aktualizacji wolnych miejsc.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas aktualizacji wolnych miejsc w turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Pobiera graczy zarejestrowanych w danym turnieju wraz z ich rekordami (wygrane/remisy/przegrane)
     * w ramach tego turnieju.
     *
     * @param tournamentId ID turnieju.
     * @return Lista obiektów Player zarejestrowanych w turnieju z ich rekordami.
     */
    public List<Player> getRegisteredPlayersWithRecordsForTournament(int tournamentId) throws SQLException {
        List<Player> participants = new ArrayList<>();
        String sql = "SELECT " +
                "u.idusers, " +
                "u.login, " +
                "u.password, " +
                "u.firstname, " +
                "u.lastname, " +
                "p.ranking, " +
                "p.id AS players_table_id " +
                "FROM tournament_players tp " +
                "JOIN players p ON tp.player_id = p.id " +
                "JOIN users u ON p.user_id = u.idusers " +
                "WHERE tp.tournament_id = ?";

        System.out.println("TournamentDAO: Wykonuję zapytanie SELECT graczy (z rekordami) dla turnieju ID: " + tournamentId);

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tournamentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int playerTableId = rs.getInt("players_table_id");
                Player player = new Player(
                        rs.getInt("idusers"),
                        rs.getString("login"),
                        rs.getString("password"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        Role.GRACZ,
                        playerTableId,
                        rs.getInt("ranking")
                );

                int wins = 0;
                int draws = 0;
                int losses = 0;

                String gamesSql = "SELECT player1_id, player2_id, winner_id FROM games WHERE tournament_id = ? AND (player1_id = ? OR player2_id = ?)";
                try (PreparedStatement gamesPstmt = conn.prepareStatement(gamesSql)) {
                    gamesPstmt.setInt(1, tournamentId);
                    gamesPstmt.setInt(2, playerTableId);
                    gamesPstmt.setInt(3, playerTableId);
                    ResultSet gamesRs = gamesPstmt.executeQuery();

                    while (gamesRs.next()) {
                        Integer winnerId = (Integer) gamesRs.getObject("winner_id"); // Użyj getObject do pobrania Integer

                        if (winnerId == null) {
                            draws++;
                        } else if (winnerId == playerTableId) {
                            wins++;
                        } else {
                            losses++;
                        }
                    }
                }

                player.setWins(wins);
                player.setDraws(draws);
                player.setLosses(losses);

                participants.add(player);
                System.out.println("TournamentDAO: Pobrany gracz z rekordem: " + player.getFirstName() + " " + player.getLastName() +
                        " (ID: " + player.getPlayersTableId() + ") Rekord: " + player.getRecord());
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania graczy z rekordami dla turnieju ID " + tournamentId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return participants;
    }

    /**
     * Pobiera wszystkie mecze dla danego turnieju, wraz z nazwami graczy.
     * Ta metoda jest ogólna i zwraca wszystkie mecze w turnieju.
     * Logika filtrowania dla konkretnego gracza będzie realizowana w kontrolerze.
     *
     * @param tournamentId ID turnieju, dla którego mają być pobrane mecze.
     * @return Lista obiektów Game.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public List<Game> getAllGamesForTournament(int tournamentId) throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT g.id, g.tournament_id, g.player1_id, g.player2_id, g.winner_id, g.game_number, " +
                "u1.firstname AS p1_firstname, u1.lastname AS p1_lastname, " +
                "u2.firstname AS p2_firstname, u2.lastname AS p2_lastname, " +
                "uw.firstname AS w_firstname, uw.lastname AS w_lastname " +
                "FROM games g " +
                "JOIN players p1 ON g.player1_id = p1.id " +
                "JOIN users u1 ON p1.user_id = u1.idusers " +
                "JOIN players p2 ON g.player2_id = p2.id " +
                "JOIN users u2 ON p2.user_id = u2.idusers " +
                "LEFT JOIN players pw ON g.winner_id = pw.id " + // LEFT JOIN, bo winner_id może być NULL
                "LEFT JOIN users uw ON pw.user_id = uw.idusers " + // LEFT JOIN, bo winner_id może być NULL
                "WHERE g.tournament_id = ? " +
                "ORDER BY g.game_number ASC";

        System.out.println("TournamentDAO: Wykonuję zapytanie SELECT wszystkich meczów dla turnieju ID: " + tournamentId);

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tournamentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Game game = new Game(
                        rs.getInt("id"),
                        rs.getInt("tournament_id"),
                        rs.getInt("player1_id"),
                        rs.getInt("player2_id"),
                        (Integer) rs.getObject("winner_id"), // Użyj getObject do obsługi NULL
                        rs.getInt("game_number")
                );

                game.setPlayer1Name(rs.getString("p1_firstname") + " " + rs.getString("p1_lastname"));
                game.setPlayer2Name(rs.getString("p2_firstname") + " " + rs.getString("p2_lastname"));

                // Obsługa zwycięzcy/remisu
                String winnerFirstname = rs.getString("w_firstname");
                String winnerLastname = rs.getString("w_lastname");
                if (game.getWinnerId() == null) {
                    game.setWinnerName("Remis");
                } else if (winnerFirstname != null && winnerLastname != null) {
                    game.setWinnerName(winnerFirstname + " " + winnerLastname);
                } else {
                    game.setWinnerName("Nieznany"); // Na wypadek błędu danych
                }

                games.add(game);
                // System.out.println("TournamentDAO: Pobrany mecz: " + game.getResultDisplay()); // Możesz włączyć dla debugowania
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania wszystkich meczów dla turnieju ID " + tournamentId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return games;
    }
}
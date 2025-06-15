package src.dao;

import src.Connection.JDBC;
import src.model.Game;
import src.model.Player;
import src.model.Role;
import src.model.Tournament;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TournamentDAO {


    public List<Tournament> getAllTournaments() {
        List<Tournament> tournaments = new ArrayList<>();
        String query = "SELECT id, name, start_date, end_date, max_slots, free_slots, status FROM tournaments ORDER BY start_date DESC";
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
                        rs.getInt("free_slots"),
                        rs.getString("status")
                );
                tournaments.add(tournament);
                rowCount++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tournaments;
    }

    public void addTournament(Tournament tournament) throws SQLException {
        String query = "INSERT INTO tournaments (name, start_date, end_date, max_slots, free_slots, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getStartDate());
            ps.setString(3, tournament.getEndDate());
            ps.setInt(4, tournament.getMaxSlots());
            ps.setInt(5, tournament.getFreeSlots());
            ps.setString(6, tournament.getStatus());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Tworzenie turnieju nie powiodło się");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tournament.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Tworzenie turnieju nie powiodło się");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void updateTournament(Tournament tournament) throws SQLException {
        String query = "UPDATE tournaments SET name = ?, start_date = ?, end_date = ?, max_slots = ?, free_slots = ?, status = ? WHERE id = ?";

        try (Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getStartDate());
            ps.setString(3, tournament.getEndDate());
            ps.setInt(4, tournament.getMaxSlots());
            ps.setInt(5, tournament.getFreeSlots());
            ps.setString(6, tournament.getStatus());
            ps.setInt(7, tournament.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void deleteTournament(int id) throws SQLException {
        String query = "DELETE FROM tournaments WHERE id = ?";
        try (Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Tournament getTournamentById(int id) {
        String query = "SELECT id, name, start_date, end_date, max_slots, free_slots, status FROM tournaments WHERE id = ?";
        try (Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Tournament(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("start_date"),
                            rs.getString("end_date"),
                            rs.getInt("max_slots"),
                            rs.getInt("free_slots"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Tournament addPlayerToTournament(int tournamentId, int playerId) throws SQLException, IllegalStateException {
        Tournament tournament = getTournamentById(tournamentId);
        if (!tournament.getStatus().equals("OTWARTY")) {
            throw new IllegalStateException("Nie można dołączyć do turnieju, którego status to: " + tournament.getStatus());
        }
        if (isPlayerRegisteredForTournament(tournamentId, playerId)) {
            throw new IllegalStateException("Gracz o ID " + playerId + " jest już zapisany na turniej ID " + tournamentId + ".");
        }
        if (tournament.getFreeSlots() <= 0) {
            throw new IllegalStateException("Turniej o ID " + tournamentId + " jest już pełny.");
        }

        String query = "INSERT INTO tournament_players (tournament_id, player_id) VALUES (?, ?)";
        try (Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, playerId);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Rejestracja gracza w turnieju nie powiodła się, brak wpływu na wiersze.");
            }

            Tournament updatedTournament = updateFreeSlotsAndCheckStatus(tournamentId, -1);

            return updatedTournament;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Tournament updateFreeSlotsAndCheckStatus(int tournamentId, int changeValue) throws SQLException, IllegalStateException {
        String updateSlotsQuery = "UPDATE tournaments SET free_slots = free_slots + ? WHERE id = ?";
        try (Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement(updateSlotsQuery)) {
            ps.setInt(1, changeValue);
            ps.setInt(2, tournamentId);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return null;
            }
        }
        Tournament updatedTournament = getTournamentById(tournamentId);

        if (updatedTournament != null) {
            if (updatedTournament.getFreeSlots() <= 0 && updatedTournament.getStatus().equals("OTWARTY") && changeValue < 0) {
                updateTournamentStatus(tournamentId, "ZAMKNIĘTY");
                updatedTournament.setStatus("ZAMKNIĘTY");

                try {
                    String checkGamesQuery = "SELECT COUNT(*) FROM games WHERE tournament_id = ?";
                    try(Connection conn = JDBC.getConnection(); PreparedStatement ps = conn.prepareStatement(checkGamesQuery)) {
                        ps.setInt(1, tournamentId);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                            generateGamesForTournament(tournamentId);
                        }
                    }
                } catch (IllegalStateException e) {
                    System.err.println("Ostrzeżenie: Gry dla turnieju ID " + tournamentId + " już istniały lub nie spełniono warunków generowania. " + e.getMessage());
                } catch (SQLException e) {
                    System.err.println("Błąd podczas sprawdzania/generowania gier: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            else if (updatedTournament.getFreeSlots() > 0 && updatedTournament.getStatus().equals("ZAMKNIĘTY") && changeValue > 0) {
                updateTournamentStatus(tournamentId, "OTWARTY");
                updatedTournament.setStatus("OTWARTY");
            }
        }
        return updatedTournament;
    }

    public void updateTournamentStatus(int tournamentId, String newStatus) throws SQLException {
        String query = "UPDATE tournaments SET status = ? WHERE id = ?";
        try (Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, newStatus);
            ps.setInt(2, tournamentId);
            ps.executeUpdate();
        }
    }


    public void generateGamesForTournament(int tournamentId) throws SQLException, IllegalStateException {
        String checkGamesQuery = "SELECT COUNT(*) FROM games WHERE tournament_id = ?";
        try (Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement(checkGamesQuery)) {
            ps.setInt(1, tournamentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new IllegalStateException("Mecze dla tego turnieju już zostały wygenerowane.");
            }
        }
        List<Player> registeredPlayers = getRegisteredPlayersWithRecordsForTournament(tournamentId);

        if (registeredPlayers.size() < 2) {
            throw new IllegalStateException("Aby wygenerować mecze, w turnieju musi być co najmniej dwóch graczy.");
        }
        String insertGameQuery = "INSERT INTO games (tournament_id, player1_id, player2_id, game_number) VALUES (?, ?, ?, ?)";
        try (Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement(insertGameQuery)) {
            int gameNumber = 1;
            for (int i = 0; i < registeredPlayers.size(); i++) {
                for (int j = i + 1; j < registeredPlayers.size(); j++) {
                    Player player1 = registeredPlayers.get(i);
                    Player player2 = registeredPlayers.get(j);
                    ps.setInt(1, tournamentId);
                    ps.setInt(2, player1.getPlayerId());
                    ps.setInt(3, player2.getPlayerId());
                    ps.setInt(4, gameNumber);
                    ps.addBatch();
                    gameNumber++;
                }
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

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
            e.printStackTrace();
            throw e;
        }
        return false;
    }

    public List<Player> getRegisteredPlayersWithRecordsForTournament(int tournamentId) throws SQLException {
        List<Player> participants = new ArrayList<>();
        String sql = "SELECT " +
                "u.idusers, " +
                "u.login, " +
                "u.password, " +
                "u.firstname, " +
                "u.lastname, " +
                "u.role, " +
                "p.ranking, " +
                "p.id AS players_table_id, " +
                "p.wins AS total_wins, " +
                "p.draws AS total_draws, " +
                "p.losses AS total_losses, " +
                "p.games_played AS total_games_played " +
                "FROM tournament_players tp " +
                "JOIN players p ON tp.player_id = p.id " +
                "JOIN users u ON p.user_id = u.idusers " +
                "WHERE tp.tournament_id = ?";
        Connection conn = null;
        try {
            conn = JDBC.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, tournamentId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int idusers = rs.getInt("idusers");
                    String login = rs.getString("login");
                    String password = rs.getString("password");
                    String firstname = rs.getString("firstname");
                    String lastname = rs.getString("lastname");
                    Role role = Role.valueOf(rs.getString("role").toUpperCase());
                    int playerTableId = rs.getInt("players_table_id");
                    int ranking = rs.getInt("ranking");
                    int totalWins = rs.getInt("total_wins");
                    int totalDraws = rs.getInt("total_draws");
                    int totalLosses = rs.getInt("total_losses");
                    int totalGamesPlayed = rs.getInt("total_games_played");

                    Player player = new Player(
                            idusers,
                            login,
                            password,
                            firstname,
                            lastname,
                            role,
                            playerTableId,
                            ranking,
                            totalWins,
                            totalDraws,
                            totalLosses,
                            totalGamesPlayed
                    );

                    int tournamentWins = 0;
                    int tournamentDraws = 0;
                    int tournamentLosses = 0;

                    String gamesSql = "SELECT player1_id, player2_id, winner_id FROM games WHERE tournament_id = ? AND (player1_id = ? OR player2_id = ?)";
                    try (PreparedStatement gamesPstmt = conn.prepareStatement(gamesSql)) {
                        gamesPstmt.setInt(1, tournamentId);
                        gamesPstmt.setInt(2, playerTableId);
                        gamesPstmt.setInt(3, playerTableId);
                        ResultSet gamesRs = gamesPstmt.executeQuery();

                        while (gamesRs.next()) {
                            Integer winnerId = (Integer) gamesRs.getObject("winner_id");

                            if (winnerId == null) {
                                tournamentDraws++;
                            } else if (winnerId == playerTableId) {
                                tournamentWins++;
                            } else {
                                tournamentLosses++;
                            }
                        }
                    }
                    player.setWins(tournamentWins);
                    player.setDraws(tournamentDraws);
                    player.setLosses(tournamentLosses);
                    player.setGamesPlayed(tournamentWins + tournamentDraws + tournamentLosses);

                    participants.add(player);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return participants;
    }

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
                "LEFT JOIN players pw ON g.winner_id = pw.id " +
                "LEFT JOIN users uw ON pw.user_id = uw.idusers " +
                "WHERE g.tournament_id = ? " +
                "ORDER BY g.game_number ASC";

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
                        (Integer) rs.getObject("winner_id"),
                        rs.getInt("game_number")
                );

                game.setPlayer1Name(rs.getString("p1_firstname") + " " + rs.getString("p1_lastname"));
                game.setPlayer2Name(rs.getString("p2_firstname") + " " + rs.getString("p2_lastname"));

                String winnerFirstname = rs.getString("w_firstname");
                String winnerLastname = rs.getString("w_lastname");
                if (game.getWinnerId() == null) {
                    game.setWinnerName("Remis");
                } else if (winnerFirstname != null && winnerLastname != null) {
                    game.setWinnerName(winnerFirstname + " " + winnerLastname);
                } else {
                    game.setWinnerName("Nieznany");
                }

                games.add(game);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return games;
    }
    public void updateGameResult(int gameId, Integer winnerPlayerId) throws SQLException {
        String query = "UPDATE games SET winner_id = ? WHERE id = ?";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            if (winnerPlayerId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, winnerPlayerId);
            }
            ps.setInt(2, gameId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
    public void calculateAndApplyTournamentStats(int tournamentId) throws SQLException {
        Connection conn = null;
        try {
            conn = JDBC.getConnection();
            conn.setAutoCommit(false);
            Map<Integer, Map<String, Integer>> playerTournamentStats = new HashMap<>();
            String playersInTournamentQuery = "SELECT player_id FROM tournament_players WHERE tournament_id = ?";
            try(PreparedStatement ps = conn.prepareStatement(playersInTournamentQuery)) {
                ps.setInt(1, tournamentId);
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        playerTournamentStats.put(rs.getInt("player_id"), new HashMap<>(Map.of("wins", 0, "draws", 0, "losses", 0, "games_played", 0)));
                    }
                }
            }
            String gamesQuery = "SELECT player1_id, player2_id, winner_id FROM games WHERE tournament_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(gamesQuery)) {
                ps.setInt(1, tournamentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int player1Id = rs.getInt("player1_id");
                        int player2Id = rs.getInt("player2_id");
                        Integer winnerId = (Integer) rs.getObject("winner_id");
                        Map<String, Integer> p1Stats = playerTournamentStats.get(player1Id);
                        Map<String, Integer> p2Stats = playerTournamentStats.get(player2Id);
                        if (p1Stats != null && p2Stats != null) {
                            if (winnerId == null) {
                                p1Stats.put("draws", p1Stats.get("draws") + 1);
                                p2Stats.put("draws", p2Stats.get("draws") + 1);
                            } else if (winnerId == player1Id) {
                                p1Stats.put("wins", p1Stats.get("wins") + 1);
                                p2Stats.put("losses", p2Stats.get("losses") + 1);
                            } else {
                                p2Stats.put("wins", p2Stats.get("wins") + 1);
                                p1Stats.put("losses", p1Stats.get("losses") + 1);
                            }
                            p1Stats.put("games_played", p1Stats.get("games_played") + 1);
                            p2Stats.put("games_played", p2Stats.get("games_played") + 1);
                        } else {
                            System.err.println("Ostrzeżenie: Znaleziono grę dla turnieju ID " + tournamentId + " z nieoczekiwanym graczem (ID " + player1Id + " lub " + player2Id + "), który nie jest zarejestrowany w tym turnieju.");
                        }
                    }
                }
            }
            String updatePlayerStatsQuery = "UPDATE players SET wins = wins + ?, draws = draws + ?, losses = losses + ?, games_played = games_played + ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updatePlayerStatsQuery)) {
                for (Map.Entry<Integer, Map<String, Integer>> entry : playerTournamentStats.entrySet()) {
                    Integer playerId = entry.getKey();
                    Map<String, Integer> stats = entry.getValue();
                    ps.setInt(1, stats.get("wins"));
                    ps.setInt(2, stats.get("draws"));
                    ps.setInt(3, stats.get("losses"));
                    ps.setInt(4, stats.get("games_played"));
                    ps.setInt(5, playerId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Błąd rollbacku: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Błąd podczas zamykania połączenia: " + closeEx.getMessage());
                }
            }
        }
    }
}
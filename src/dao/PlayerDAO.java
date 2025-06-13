// src/dao/PlayerDAO.java

package src.dao;

import src.Connection.JDBC;
import src.model.Game;
import src.model.Player;
import src.model.Role; // Upewnij się, że Role jest odpowiednio zdefiniowane
// import src.model.User; // Zwykle niepotrzebne, jeśli Player dziedziczy po User i UserDAO jest oddzielne

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {

    private static final int DEFAULT_RANKING = 1000; // Domyślny ranking dla nowego gracza

    /**
     * Dodaje szczegóły gracza do tabeli 'players'.
     * Używana w transakcjach, gdy połączenie jest już otwarte.
     * Inicjuje wins, draws, losses, games_played na 0.
     *
     * @param userId ID użytkownika z tabeli 'users'.
     * @param ranking Początkowy ranking gracza.
     * @param existingConn Istniejące połączenie SQL.
     * @throws SQLException W przypadku błędu bazy danych.
     */
    public void addPlayerDetails(int userId, int ranking, Connection existingConn) throws SQLException {
        String insertPlayerQuery = "INSERT INTO players (user_id, ranking, wins, draws, losses, games_played) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = existingConn.prepareStatement(insertPlayerQuery)) {
            ps.setInt(1, userId);
            ps.setInt(2, ranking);
            ps.setInt(3, 0); // wins
            ps.setInt(4, 0); // draws
            ps.setInt(5, 0); // losses
            ps.setInt(6, 0); // games_played
            ps.executeUpdate();
        }
    }

    /**
     * Dodaje szczegóły gracza do tabeli 'players'.
     * Otwiera własne połączenie z bazą danych.
     * Używana, gdy gracz jest dodawany poza istniejącą transakcją.
     *
     * @param userId ID użytkownika z tabeli 'users'.
     * @param ranking Początkowy ranking gracza.
     * @throws SQLException W przypadku błędu bazy danych.
     */
    public void addPlayerDetails(int userId, int ranking) throws SQLException {
        try (Connection conn = JDBC.getConnection()) {
            addPlayerDetails(userId, ranking, conn); // Wywołuje wersję z Connection
        }
    }

    /**
     * Pobiera ID z tabeli 'players' na podstawie ID użytkownika z tabeli 'users'.
     *
     * @param userId ID użytkownika.
     * @return ID gracza z tabeli 'players' lub null, jeśli nie znaleziono.
     * @throws SQLException W przypadku błędu bazy danych.
     */
    public Integer getPlayersTableIdByUserId(int userId) throws SQLException {
        String query = "SELECT id FROM players WHERE user_id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }

    /**
     * Pobiera pełny obiekt Player na podstawie ID użytkownika (User.id).
     * Łączy dane z tabel 'users' i 'players'.
     *
     * @param userId ID użytkownika (z tabeli 'users').
     * @return Obiekt Player z pełnymi danymi lub null, jeśli nie znaleziono.
     * @throws SQLException W przypadku błędu bazy danych.
     */
    public Player getPlayerByUserId(int userId) throws SQLException {
        String sql = "SELECT p.id AS player_id, p.ranking, p.wins, p.draws, p.losses, p.games_played, " +
                "u.idusers, u.login, u.password, u.firstname, u.lastname, u.role " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.idusers " +
                "WHERE u.idusers = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Player(
                            rs.getInt("idusers"),          // 1. User ID (dla superclass User)
                            rs.getString("login"),         // 2. Login
                            rs.getString("password"),      // 3. Password
                            rs.getString("firstname"),     // 4. First Name
                            rs.getString("lastname"),      // 5. Last Name
                            Role.valueOf(rs.getString("role").toUpperCase()), // 6. Role
                            rs.getInt("player_id"),        // 7. Player ID (z tabeli players)
                            rs.getInt("ranking"),          // 8. Ranking
                            rs.getInt("wins"),             // 9. Wins
                            rs.getInt("draws"),            // 10. Draws
                            rs.getInt("losses"),           // 11. Losses
                            rs.getInt("games_played")      // 12. Games Played
                    );
                }
            }
        }
        return null;
    }

    /**
     * Pobiera pełny obiekt Player na podstawie ID gracza (z tabeli 'players').
     * Łączy dane z tabel 'users' i 'players'.
     *
     * @param playersTableId ID gracza (z tabeli 'players').
     * @return Obiekt Player z pełnymi danymi lub null, jeśli nie znaleziono.
     * @throws SQLException W przypadku błędu bazy danych.
     */
    public Player getPlayerByPlayerId(int playersTableId) throws SQLException {
        String sql = "SELECT p.id AS player_id, p.ranking, p.wins, p.draws, p.losses, p.games_played, " +
                "u.idusers, u.login, u.password, u.firstname, u.lastname, u.role " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.idusers " +
                "WHERE p.id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playersTableId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Player(
                            rs.getInt("idusers"),          // 1. User ID (dla superclass User)
                            rs.getString("login"),         // 2. Login
                            rs.getString("password"),      // 3. Password
                            rs.getString("firstname"),     // 4. First Name
                            rs.getString("lastname"),      // 5. Last Name
                            Role.valueOf(rs.getString("role").toUpperCase()), // 6. Role
                            rs.getInt("player_id"),        // 7. Player ID (z tabeli players)
                            rs.getInt("ranking"),          // 8. Ranking
                            rs.getInt("wins"),             // 9. Wins
                            rs.getInt("draws"),            // 10. Draws
                            rs.getInt("losses"),           // 11. Losses
                            rs.getInt("games_played")      // 12. Games Played
                    );
                }
            }
        }
        return null;
    }

    /**
     * Pobiera ranking gracza na podstawie ID z tabeli 'players'.
     *
     * @param playerId ID gracza z tabeli 'players'.
     * @return Ranking gracza lub null, jeśli nie znaleziono.
     * @throws SQLException W przypadku błędu bazy danych.
     */
    public Integer getPlayerRanking(int playerId) throws SQLException {
        String query = "SELECT ranking FROM players WHERE id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int ranking = rs.getInt("ranking");
                    if (rs.wasNull()) {
                        return null;
                    }
                    return ranking;
                }
            }
        }
        return null;
    }

    /**
     * Aktualizuje ranking gracza w tabeli 'players'.
     *
     * @param playerId ID gracza z tabeli 'players'.
     * @param newRanking Nowy ranking do ustawienia.
     * @throws SQLException W przypadku błędu bazy danych.
     */
    public void updatePlayerRanking(int playerId, int newRanking) throws SQLException {
        String query = "UPDATE players SET ranking = ? WHERE id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, newRanking);
            ps.setInt(2, playerId);
            ps.executeUpdate();
        }
    }

    /**
     * Aktualizuje statystyki W/D/L i liczbę gier dla gracza w tabeli 'players'.
     * Wywoływana po każdym meczu.
     *
     * @param player Obiekt Player z zaktualizowanymi statystykami.
     * @throws SQLException W przypadku błędu bazy danych.
     */
    public void updatePlayerStatistics(Player player) throws SQLException {
        String sql = "UPDATE players SET wins = ?, draws = ?, losses = ?, games_played = ? WHERE id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, player.getWins());
            pstmt.setInt(2, player.getDraws());
            pstmt.setInt(3, player.getLosses());
            pstmt.setInt(4, player.getGamesPlayed());
            pstmt.setInt(5, player.getPlayersTableId()); // Używamy ID z tabeli 'players'
            pstmt.executeUpdate();
        }
    }

    /**
     * Usuwa szczegóły gracza z tabeli 'players'.
     *
     * @param playerId ID gracza z tabeli 'players'.
     * @throws SQLException W przypadku błędu bazy danych.
     */
    public void deletePlayerDetails(int playerId) throws SQLException {
        String query = "DELETE FROM players WHERE id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playerId);
            ps.executeUpdate();
        }
    }

    /**
     * Pobiera graczy zarejestrowanych w danym turnieju wraz z ich pełnymi rekordami.
     * Łączy dane z tabel 'users', 'players' i 'tournament_players'.
     *
     * @param tournamentId ID turnieju.
     * @return Lista obiektów Player zarejestrowanych w turnieju.
     * @throws SQLException W przypadku błędu bazy danych.
     */
    public List<Player> getPlayersInTournament(int tournamentId) throws SQLException {
        List<Player> players = new ArrayList<>();
        String query = "SELECT p.id AS player_id, p.ranking, p.wins, p.draws, p.losses, p.games_played, " +
                "u.idusers, u.login, u.password, u.firstname, u.lastname, u.role " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.idusers " +
                "JOIN tournament_players tp ON p.id = tp.player_id " +
                "WHERE tp.tournament_id = ?";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, tournamentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Upewnij się, że ten konstruktor Player jest dostępny i ma poprawną sygnaturę
                Player player = new Player(
                        rs.getInt("idusers"),          // 1. User ID (dla superclass User)
                        rs.getString("login"),         // 2. Login
                        rs.getString("password"),      // 3. Password
                        rs.getString("firstname"),     // 4. First Name
                        rs.getString("lastname"),      // 5. Last Name
                        Role.valueOf(rs.getString("role").toUpperCase()), // 6. Role
                        rs.getInt("player_id"),        // 7. Player ID (z tabeli players)
                        rs.getInt("ranking"),          // 8. Ranking
                        rs.getInt("wins"),             // 9. Wins
                        rs.getInt("draws"),            // 10. Draws
                        rs.getInt("losses"),           // 11. Losses
                        rs.getInt("games_played")      // 12. Games Played
                );
                players.add(player);
            }
        }
        return players;
    }

    /**
     * Pobiera wszystkie mecze rozegrane przez konkretnego gracza we wszystkich turniejach.
     * Dodaje nazwy turniejów i przeciwników do obiektów Game.
     *
     * @param playerTableId ID gracza z tabeli `players` (nie `users_idusers`).
     * @return Lista obiektów Game reprezentujących mecze gracza.
     * @throws SQLException W przypadku błędu bazy danych.
     */
    public List<Game> getAllGamesForPlayer(int playerTableId) throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT g.id, g.tournament_id, g.player1_id, g.player2_id, g.winner_id, g.game_number, " +
                "t.name AS tournament_name, " +
                "u1.firstname AS p1_firstname, u1.lastname AS p1_lastname, " +
                "u2.firstname AS p2_firstname, u2.lastname AS p2_lastname, " +
                "uw.firstname AS w_firstname, uw.lastname AS w_lastname " +
                "FROM games g " +
                "JOIN tournaments t ON g.tournament_id = t.id " +
                "JOIN players p1 ON g.player1_id = p1.id " +
                "JOIN users u1 ON p1.user_id = u1.idusers " +
                "JOIN players p2 ON g.player2_id = p2.id " +
                "JOIN users u2 ON p2.user_id = u2.idusers " +
                "LEFT JOIN players pw ON g.winner_id = pw.id " +
                "LEFT JOIN users uw ON pw.user_id = uw.idusers " +
                "WHERE g.player1_id = ? OR g.player2_id = ? " +
                "ORDER BY t.start_date DESC, g.game_number ASC";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerTableId);
            pstmt.setInt(2, playerTableId);
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

                game.setTournamentName(rs.getString("tournament_name"));

                if (game.getPlayer1Id() == playerTableId) {
                    game.setOpponentName(rs.getString("p2_firstname") + " " + rs.getString("p2_lastname"));
                } else {
                    game.setOpponentName(rs.getString("p1_firstname") + " " + rs.getString("p1_lastname"));
                }

                Integer winnerId = game.getWinnerId();
                if (winnerId == null) {
                    game.setIndividualResultDisplay("Remis");
                } else if (winnerId == playerTableId) {
                    game.setIndividualResultDisplay("Wygrana");
                } else {
                    game.setIndividualResultDisplay("Przegrana");
                }

                games.add(game);
            }
        }
        return games;
    }
}
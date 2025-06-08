// W pliku src/dao/PlayerDAO.java
package src.dao;

import src.Connection.JDBC; // Upewnij się, że JDBC jest Twoją klasą do zarządzania połączeniem
import src.model.Player;
import src.model.Role;
import src.model.User; // Importuj klasę User, jeśli dane imienia i nazwiska pochodzą z tabeli users

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {

    // To jest jedyna wersja addPlayerDetails, która powinna być używana w transakcjach
    public void addPlayerDetails(int userId, int ranking, Connection existingConn) throws SQLException {
        String insertPlayerQuery = "INSERT INTO players (user_id, ranking) VALUES (?, ?)";
        try (PreparedStatement ps = existingConn.prepareStatement(insertPlayerQuery)) {
            ps.setInt(1, userId);
            ps.setInt(2, ranking);
            ps.executeUpdate();
        }
    }

    // WAŻNE: Dodaj drugą wersję addPlayerDetails BEZ argumentu Connection,
    // która otwiera własne połączenie. Będzie to potrzebne w loginUser(),
    // jeśli użytkownik nie ma rekordu w players i jest dodawany poza transakcją.
    public void addPlayerDetails(int userId, int ranking) throws SQLException {
        try (Connection conn = JDBC.getConnection()) {
            addPlayerDetails(userId, ranking, conn); // Wywołuje wersję z Connection
        }
    }

    public Integer getPlayersTableIdByUserId(int userId) throws SQLException {
        String query = "SELECT idplayers FROM players WHERE users_idusers = ?"; // Pamiętaj o poprawnych nazwach kolumn z bazy
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idplayers"); // Pamiętaj o poprawnych nazwach kolumn z bazy
                }
            }
        }
        return null;
    }

    public Integer getPlayerRanking(int playerId) throws SQLException { // Zmieniono parametr na playerId
        String query = "SELECT ranking FROM players WHERE idplayers = ?"; // Zmieniono warunek na idplayers
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

    public void updatePlayerRanking(int playerId, int newRanking) throws SQLException { // Zmieniono parametr na playerId
        String query = "UPDATE players SET ranking = ? WHERE idplayers = ?"; // Zmieniono warunek na idplayers
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, newRanking);
            ps.setInt(2, playerId);
            ps.executeUpdate();
        }
    }

    public void deletePlayerDetails(int playerId) throws SQLException { // Zmieniono parametr na playerId
        String query = "DELETE FROM players WHERE idplayers = ?"; // Zmieniono warunek na idplayers
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playerId);
            ps.executeUpdate();
        }
    }

    public List<Player> getPlayersInTournament(int tournamentId) throws SQLException {
        List<Player> players = new ArrayList<>();
        // Poprawione zapytanie SQL:
        // 1. Zmieniono 'p.idplayers' na 'p.id' (zakładając, że klucz główny tabeli 'players' to 'id')
        // 2. Zmieniono 'p.users_idusers' na 'p.user_id' (zakładając 'user_id' w tabeli players/ranking)
        // 3. Zmieniono 'tp.players_idplayers' na 'tp.player_id' (potwierdzone zdjęciem tournament_players)
        // 4. Zmieniono 'tp.tournaments_idtournaments' na 'tp.tournament_id' (potwierdzone zdjęciem tournament_players)
        // 5. Zmieniono 'first_name', 'last_name' na 'firstname', 'lastname' (potwierdzone zdjęciem users)
        String query = "SELECT p.id, u.idusers, u.login, u.password, u.firstname, u.lastname, u.role, p.ranking " +
                "FROM players p " + // Alias 'p' dla tabeli 'players' (lub 'ranking', jeśli to ta sama tabela)
                "JOIN users u ON p.user_id = u.idusers " + // Połączenie gracza z użytkownikiem
                "JOIN tournament_players tp ON p.id = tp.player_id " + // Połączenie gracza z turniejem
                "WHERE tp.tournament_id = ?"; // Filtracja po ID turnieju

        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, tournamentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("idusers");
                String login = rs.getString("login");
                String password = rs.getString("password");
                String firstName = rs.getString("firstname"); // Poprawiono
                String lastName = rs.getString("lastname");   // Poprawiono
                Role role = Role.valueOf(rs.getString("role").toUpperCase());

                // Tutaj pobieramy ID z tabeli `players` (alias 'p'), które teraz nazywa się 'id'
                int playersTableId = rs.getInt("id"); // Użyj "id", jeśli to klucz główny tabeli "players"
                int ranking = rs.getInt("ranking");

                // Upewnij się, że konstruktor Player jest w stanie przyjąć te wszystkie argumenty
                Player player = new Player(userId, login, password, firstName, lastName, role, playersTableId, ranking);
                players.add(player);
            }
        }
        return players;
    }
    // --- KONIEC NOWEJ METODY ---
}
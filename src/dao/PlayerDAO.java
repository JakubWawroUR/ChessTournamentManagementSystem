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
        // Zaktualizowane zapytanie SQL - pobieramy wszystkie dane potrzebne do konstruktora Player
        String query = "SELECT p.idplayers, u.idusers, u.login, u.password, u.first_name, u.last_name, u.role, p.ranking " +
                "FROM players p " +
                "JOIN users u ON p.users_idusers = u.idusers " + // Zakładam, że players.users_idusers łączy się z users.idusers
                "JOIN tournament_players tp ON p.idplayers = tp.players_idplayers " +
                "WHERE tp.tournaments_idtournaments = ?";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, tournamentId); // Ustawienie ID turnieju
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Pobieranie danych z ResultSet
                int userId = rs.getInt("idusers");
                String login = rs.getString("login");
                String password = rs.getString("password");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                // Konwersja Stringa roli z bazy danych na enum Role
                Role role = Role.valueOf(rs.getString("role").toUpperCase()); // Zapewnij, że nazwy ról w DB pasują do nazw enum (np. "GRACZ")

                int playersTableId = rs.getInt("idplayers"); // ID gracza z tabeli players
                int ranking = rs.getInt("ranking");

                // Tworzenie obiektu Player przy użyciu pełnego konstruktora
                Player player = new Player(userId, login, password, firstName, lastName, role, playersTableId, ranking);
                players.add(player); // Dodanie gracza do listy
            }
        }
        return players;
    }
    // --- KONIEC NOWEJ METODY ---
}
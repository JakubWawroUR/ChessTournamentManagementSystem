package src.dao;

import src.Connection.JDBC; // Upewnij się, że to jest poprawna ścieżka do Twojej klasy JDBC
import src.model.Player;
import src.model.User; // Może być potrzebny, jeśli PlayerDAO używa User (np. do pobierania login/password w getPlayerDetailsByUserId)
import src.model.Role; // Będzie potrzebny do stworzenia obiektu Player z pełnymi danymi (jeśli Player ma rolę)

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Potrzebne dla Statement.RETURN_GENERATED_KEYS

public class PlayerDAO {

    /**
     * Dodaje nowe szczegóły gracza do tabeli 'players'.
     * Jest wywoływana podczas rejestracji nowego użytkownika z rolą GRACZ.
     * @param userId ID użytkownika z tabeli 'users', do którego przypisany jest gracz.
     * @param ranking Początkowy ranking gracza.
     * @return true, jeśli dodanie powiodło się, false w przeciwnym razie.
     * @throws SQLException jeśli wystąpi błąd SQL.
     */
    public boolean addPlayerDetails(int userId, int ranking) throws SQLException {
        String query = "INSERT INTO players (user_id, ranking) VALUES (?, ?)";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, ranking);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Pobiera pełny obiekt Player na podstawie ID użytkownika (user_id z tabeli users).
     * Do stworzenia obiektu Player potrzebne są dane z tabeli users (login, password, firstname, lastname)
     * oraz dane z tabeli players (id gracza w tabeli players, ranking).
     * @param userId ID użytkownika z tabeli 'users'.
     * @return Obiekt Player, jeśli znaleziono, w przeciwnym razie null.
     * @throws SQLException jeśli wystąpi błąd SQL.
     */
    public Player getPlayerDetailsByUserId(int userId) throws SQLException {
        String query = "SELECT p.idplayers AS players_table_id, p.user_id, p.ranking, " + // Zmieniono p.id na p.idplayers dla spójności
                "u.login, u.password, u.firstname, u.lastname, u.role " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.idusers " +
                "WHERE p.user_id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int playersTableId = rs.getInt("players_table_id");
                    int userIdFromDB = rs.getInt("user_id");
                    int ranking = rs.getInt("ranking");
                    String login = rs.getString("login");
                    String password = rs.getString("password");
                    String firstname = rs.getString("firstname");
                    String lastname = rs.getString("lastname");
                    // *** TUTAJ JEST ZMIANA ***
                    Role role = Role.valueOf(rs.getString("role")); // Pobieramy rolę

                    // *** I TUTAJ JEST ZMIANA W WYWOŁANIU KONSTRUKTORA ***
                    return new Player(userIdFromDB, login, password, firstname, lastname, role, playersTableId, ranking);
                }
            }
        }
        return null;
    }

    /**
     * Pobiera ranking gracza na podstawie jego ID użytkownika (user_id).
     * @param userId ID użytkownika z tabeli 'users'.
     * @return Ranking gracza (Integer), lub null jeśli nie znaleziono lub gracz nie ma rankingu.
     * @throws SQLException jeśli wystąpi błąd SQL.
     */
    public Integer getPlayerRanking(int userId) throws SQLException {
        String query = "SELECT ranking FROM players WHERE user_id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ranking");
                }
            }
        }
        return null; // Zwróć null, jeśli gracz nie istnieje w tabeli players
    }

    /**
     * Pobiera ID gracza z tabeli 'players' na podstawie ID użytkownika (user_id).
     * Jest używana np. po dodaniu nowego gracza, aby uzyskać jego players_table_id.
     * @param userId ID użytkownika z tabeli 'users'.
     * @return ID gracza z tabeli 'players' (kolumna 'id'), lub null jeśli nie znaleziono.
     * @throws SQLException jeśli wystąpi błąd SQL.
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
     * Aktualizuje ranking gracza na podstawie jego user_id.
     * @param userId ID użytkownika z tabeli 'users'.
     * @param newRanking Nowy ranking do ustawienia.
     * @return true jeśli aktualizacja powiodła się, false w przeciwnym razie.
     * @throws SQLException jeśli wystąpi błąd SQL.
     */
    public boolean updatePlayerRanking(int userId, int newRanking) throws SQLException {
        String query = "UPDATE players SET ranking = ? WHERE user_id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, newRanking);
            ps.setInt(2, userId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Usuwa dane gracza z tabeli 'players' na podstawie user_id.
     * Jest używana, gdy np. rola użytkownika zmienia się z gracza na inną,
     * lub gdy użytkownik jest usuwany.
     * @param userId ID użytkownika z tabeli 'users'.
     * @return true jeśli usunięcie powiodło się, false w przeciwnym razie.
     * @throws SQLException jeśli wystąpi błąd SQL.
     */
    public boolean deletePlayerDetails(int userId) throws SQLException {
        String query = "DELETE FROM players WHERE user_id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Możesz dodać inne metody, np. do pobierania listy wszystkich graczy,
    // jeśli będzie to potrzebne w innej części aplikacji.
    // Przykład:
    /*
    public ObservableList<Player> getAllPlayers() throws SQLException {
        ObservableList<Player> playerList = FXCollections.observableArrayList();
        String query = "SELECT p.id AS players_table_id, p.user_id, p.ranking, " +
                       "u.login, u.password, u.firstname, u.lastname, u.role " +
                       "FROM players p JOIN users u ON p.user_id = u.idusers";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int playersTableId = rs.getInt("players_table_id");
                int userId = rs.getInt("user_id");
                int ranking = rs.getInt("ranking");
                String login = rs.getString("login");
                String password = rs.getString("password");
                String firstname = rs.getString("firstname");
                String lastname = rs.getString("lastname");
                playerList.add(new Player(userId, login, password, firstname, lastname, ranking, playersTableId));
            }
        }
        return playerList;
    }
    */
}
package src.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import src.Connection.JDBC;
import src.model.Admin;
import src.model.Player;
import src.model.Role;
import src.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    // Metoda do rejestracji nowego użytkownika (może być graczem)
    public boolean registerUser(String login, String password, String firstname, String lastname, Role role) throws SQLException {
        try (Connection conn = JDBC.getConnection()) {
            conn.setAutoCommit(false);

            if (isUserExists(login, conn)) {
                conn.rollback();
                return false;
            }

            String insertUserQuery = "INSERT INTO users (login, password, firstname, lastname, role) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, login);
                ps.setString(2, password);
                ps.setString(3, firstname);
                ps.setString(4, lastname);
                ps.setString(5, role.name());

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    throw new SQLException("Tworzenie użytkownika nie powiodło się, brak wpływu na wiersze.");
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        if (role == Role.GRACZ) {
                            PlayerDAO playerDAO = new PlayerDAO(); // Tworzenie nowej instancji PlayerDAO
                            playerDAO.addPlayerDetails(userId, 1000); // Domyślny ranking
                            System.out.println("Nowy gracz (user_id: " + userId + ") dodany do tabeli players.");
                        }
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        throw new SQLException("Tworzenie użytkownika nie powiodło się, brak wygenerowanego ID.");
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Metoda sprawdzająca, czy użytkownik istnieje (przeciążona dla transakcji)
    private boolean isUserExists(String login, Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE login = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Metoda do logowania i pobierania pełnego obiektu User.
    public User loginUser(String login, String password) throws SQLException {
        String query = "SELECT u.idusers, u.login, u.password, u.firstname, u.lastname, u.role, p.id AS players_table_id, p.ranking " +
                "FROM users u " +
                "LEFT JOIN players p ON u.idusers = p.user_id " +
                "WHERE u.login = ? AND u.password = ?";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, login);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idusers = rs.getInt("idusers");
                    String userLogin = rs.getString("login");
                    String userPassword = rs.getString("password");
                    String userFirstname = rs.getString("firstname");
                    String userLastname = rs.getString("lastname");
                    String roleString = rs.getString("role");

                    Role userRole = Role.valueOf(roleString);

                    if (userRole == Role.GRACZ) {
                        int playersTableId = rs.getInt("players_table_id");
                        boolean isPlayersTableIdNull = rs.wasNull(); // Sprawdź, czy players_table_id było NULL

                        // ZMIANA TUTAJ: Zmieniono 'int ranking' na 'Integer ranking'
                        Integer ranking = rs.getInt("ranking"); // Pobierz ranking
                        if (rs.wasNull()) { // Sprawdź, czy ranking (ostatnio pobrana wartość) było NULL
                            ranking = null; // Jeśli tak, przypisz null do obiektu Integer
                        }

                        if (isPlayersTableIdNull || playersTableId == 0) {
                            System.out.println("Zalogowany użytkownik (ID: " + idusers + ", Rola: " + userRole + ") nie posiada rekordu w tabeli 'players'. Dodaję automatycznie.");
                            PlayerDAO playerDAO = new PlayerDAO();
                            playerDAO.addPlayerDetails(idusers, 1000); // Domyślny ranking
                            Integer newPlayersTableId = playerDAO.getPlayersTableIdByUserId(idusers);
                            if (newPlayersTableId != null) {
                                playersTableId = newPlayersTableId;
                                System.out.println("Dodano i ustawiono players.id: " + playersTableId);
                            } else {
                                System.err.println("Krytyczny błąd: Nie udało się pobrać nowo utworzonego players.id dla user_id: " + idusers);
                                return null;
                            }
                            // Ranking powinien teraz istnieć w bazie, więc pobierz go ponownie
                            ranking = playerDAO.getPlayerRanking(idusers);
                            if (ranking == null) { // Fallback, jeśli getPlayerRanking zwróci null (co nie powinno się zdarzyć po dodaniu)
                                ranking = 1000;
                            }
                        } else {
                            // Jeśli rekord gracza istnieje, ale ranking z bazy był NULL, przypisz domyślny
                            if (ranking == null) {
                                ranking = 1000;
                            }
                        }
                        // Upewnij się, że 'ranking' nie jest null przed przekazaniem do konstruktora Player,
                        // ponieważ konstruktor Player najprawdopodobniej oczekuje 'int' (prymitywu).
                        // Jeśli 'ranking' jest Integer, Java automatycznie go rozpakuje (unbox).
                        // Jeśli jest null podczas rozpakowywania, dostaniesz NullPointerException.
                        // Dlatego ważne jest, aby wcześniej go zainicjalizować, jeśli był null.
                        return new Player(idusers, userLogin, userPassword, userFirstname, userLastname, userRole, playersTableId, ranking.intValue()); // Użyj .intValue() aby być pewnym
                    } else if (userRole == Role.ADMINISTRATOR) {
                        return new Admin(idusers, userLogin, userPassword, userFirstname, userLastname, userRole);
                    } else {
                        return new User(idusers, userLogin, userPassword, userFirstname, userLastname, userRole);
                    }
                }
            }
        }
        return null;
    }

    // Metoda do pobierania listy wszystkich użytkowników
    public ObservableList<User> getAllUsers() throws SQLException {
        ObservableList<User> userList = FXCollections.observableArrayList();
        // Zmodyfikowano zapytanie: 'p.idplayers' zmieniono na 'p.id'
        String query = "SELECT u.idusers, u.login, u.password, u.firstname, u.lastname, u.role, p.id AS players_table_id, p.ranking " +
                "FROM users u " +
                "LEFT JOIN players p ON u.idusers = p.user_id";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idusers = rs.getInt("idusers");
                String login = rs.getString("login");
                String password = rs.getString("password");
                String firstname = rs.getString("firstname");
                String lastname = rs.getString("lastname");
                String roleString = rs.getString("role");

                Role role = Role.valueOf(roleString);

                if (role == Role.GRACZ) {
                    int playersTableId = rs.getInt("players_table_id");
                    int ranking = rs.getInt("ranking");

                    if (rs.wasNull() && playersTableId == 0 && ranking == 0) {
                        System.err.println("Ostrzeżenie: Gracz (ID: " + idusers + ") istnieje w 'users', ale brakuje rekordu w 'players'.");
                        userList.add(new User(idusers, login, password, firstname, lastname, role));
                    } else {
                        userList.add(new Player(idusers, login, password, firstname, lastname, role, playersTableId, ranking));
                    }
                } else if (role == Role.ADMINISTRATOR) {
                    userList.add(new Admin(idusers, login, password, firstname, lastname, role));
                } else {
                    userList.add(new User(idusers, login, password, firstname, lastname, role));
                }
            }
        }
        return userList;
    }

    // Metoda do aktualizacji użytkownika
    public boolean updateUser(User user) throws SQLException {
        String query = "UPDATE users SET login = ?, password = ?, firstname = ?, lastname = ?, role = ? WHERE idusers = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getLastname());
            ps.setString(5, user.getRole().name());
            ps.setInt(6, user.getId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Metoda do usuwania użytkownika
    public boolean deleteUser(int userId) throws SQLException {
        String getUserRoleQuery = "SELECT role FROM users WHERE idusers = ?";
        String deletePlayerQuery = "DELETE FROM players WHERE user_id = ?";
        String deleteUserQuery = "DELETE FROM users WHERE idusers = ?";

        try (Connection conn = JDBC.getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement psRole = null;
            PreparedStatement psDeletePlayer = null;
            PreparedStatement psDeleteUser = null;
            ResultSet rs = null;

            try {
                psRole = conn.prepareStatement(getUserRoleQuery);
                psRole.setInt(1, userId);
                rs = psRole.executeQuery();
                if (rs.next()) {
                    Role role = Role.valueOf(rs.getString("role"));
                    if (role == Role.GRACZ) {
                        psDeletePlayer = conn.prepareStatement(deletePlayerQuery);
                        psDeletePlayer.setInt(1, userId);
                        psDeletePlayer.executeUpdate();
                        System.out.println("Usunięto gracza o user_id: " + userId + " z tabeli players.");
                    }
                }

                psDeleteUser = conn.prepareStatement(deleteUserQuery);
                psDeleteUser.setInt(1, userId);
                int affectedRows = psDeleteUser.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                if (rs != null) rs.close();
                if (psRole != null) psRole.close();
                if (psDeletePlayer != null) psDeletePlayer.close();
                if (psDeleteUser != null) psDeleteUser.close();
            }
        }
    }
}
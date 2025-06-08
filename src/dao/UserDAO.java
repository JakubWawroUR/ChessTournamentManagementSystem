// W pliku src/dao/UserDAO.java
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
        try (Connection conn = JDBC.getConnection()) { // Zamykane automatycznie
            conn.setAutoCommit(false); // Rozpocznij transakcję

            if (isUserExists(login, conn)) {
                conn.rollback(); // Wycofaj transakcję, jeśli login już istnieje
                return false;
            }

            String insertUserQuery = "INSERT INTO users (login, password, firstname, lastname, role) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, login);
                ps.setString(2, password); // Pamiętaj o haszowaniu haseł w prawdziwej aplikacji!
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
                            PlayerDAO playerDAO = new PlayerDAO();
                            // ZMIANA TUTAJ: Przekazujemy istniejące połączenie 'conn'
                            playerDAO.addPlayerDetails(userId, 1000, conn); // Domyślny ranking
                            System.out.println("Nowy gracz (user_id: " + userId + ") dodany do tabeli players.");
                        }
                        conn.commit(); // Zatwierdź transakcję, jeśli wszystko poszło ok
                        return true;
                    } else {
                        conn.rollback(); // Wycofaj, jeśli brak ID
                        throw new SQLException("Tworzenie użytkownika nie powiodło się, brak wygenerowanego ID.");
                    }
                }
            } catch (SQLException e) {
                conn.rollback(); // Wycofaj transakcję w przypadku błędu
                throw e; // Rzuć wyjątek, aby kontroler go obsłużył
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

                        Integer ranking = rs.getInt("ranking"); // Pobierz ranking
                        if (rs.wasNull()) {
                            ranking = null;
                        }

                        // Jeśli rekord gracza nie istnieje w tabeli 'players', dodaj go
                        // Sprawdź, czy playersTableId jest 0 lub null, bo to wskazuje na brak rekordu w 'players'
                        if (isPlayersTableIdNull || playersTableId == 0) { // playersTableId == 0 dla int oznacza brak wartości
                            System.out.println("Zalogowany użytkownik (ID: " + idusers + ", Rola: " + userRole + ") nie posiada rekordu w tabeli 'players'. Dodaję automatycznie.");
                            PlayerDAO playerDAO = new PlayerDAO();
                            // Używamy samodzielnego połączenia dla tej operacji, bo loginUser nie jest transakcyjny
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
                            if (ranking == null) { // Fallback, jeśli getPlayerRanking zwróci null
                                ranking = 1000;
                            }
                        } else {
                            // Jeśli rekord gracza istnieje, ale ranking z bazy był NULL, przypisz domyślny
                            if (ranking == null) {
                                ranking = 1000;
                            }
                        }
                        return new Player(idusers, userLogin, userPassword, userFirstname, userLastname, userRole, playersTableId, ranking.intValue());
                    } else if (userRole == Role.ADMINISTRATOR) {
                        return new Admin(idusers, userLogin, userPassword, userFirstname, userLastname, userRole);
                    } else {
                        return new User(idusers, userLogin, userPassword, userFirstname, userLastname, userRole);
                    }
                }
            }
        }
        return null; // Nie znaleziono użytkownika
    }

    // Metoda do pobierania listy wszystkich użytkowników
    public ObservableList<User> getAllUsers() throws SQLException {
        ObservableList<User> userList = FXCollections.observableArrayList();
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
                    // Używamy rs.wasNull() dla obu, bo ranking też może być null
                    boolean isPlayersTableIdNull = rs.wasNull();
                    int ranking = rs.getInt("ranking"); // Próba pobrania jako int
                    boolean isRankingNull = rs.wasNull(); // Sprawdzenie, czy faktycznie było null

                    if (isPlayersTableIdNull || playersTableId == 0) {
                        System.err.println("Ostrzeżenie: Gracz (ID: " + idusers + ", Login: " + login + ") istnieje w 'users', ale brakuje rekordu w 'players'.");
                        // Zwróć podstawowy obiekt User, jeśli nie ma pełnych danych gracza
                        userList.add(new User(idusers, login, password, firstname, lastname, role));
                    } else {
                        // Jeśli ranking był null w bazie, ustaw domyślną wartość (np. 1000)
                        if (isRankingNull) {
                            ranking = 1000;
                        }
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
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
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
            conn.setAutoCommit(false); // Rozpocznij transakcję

            // Pobierz rolę użytkownika
            String roleString = null;
            try (PreparedStatement psRole = conn.prepareStatement(getUserRoleQuery)) {
                psRole.setInt(1, userId);
                try (ResultSet rs = psRole.executeQuery()) {
                    if (rs.next()) {
                        roleString = rs.getString("role");
                    }
                }
            }

            // Jeśli to gracz, usuń wpis z tabeli 'players'
            if (roleString != null && Role.valueOf(roleString) == Role.GRACZ) {
                try (PreparedStatement psDeletePlayer = conn.prepareStatement(deletePlayerQuery)) {
                    psDeletePlayer.setInt(1, userId);
                    psDeletePlayer.executeUpdate();
                    System.out.println("Usunięto gracza o user_id: " + userId + " z tabeli players.");
                }
            }

            // Usuń użytkownika z tabeli 'users'
            try (PreparedStatement psDeleteUser = conn.prepareStatement(deleteUserQuery)) {
                psDeleteUser.setInt(1, userId);
                int affectedRows = psDeleteUser.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit(); // Zatwierdź transakcję
                    return true;
                } else {
                    conn.rollback(); // Wycofaj, jeśli użytkownik nie został usunięty
                    return false;
                }
            }
        } catch (SQLException e) {
            // Wycofaj transakcję w przypadku wyjątku
            throw e; // Przekaż wyjątek dalej
        }
    }
}
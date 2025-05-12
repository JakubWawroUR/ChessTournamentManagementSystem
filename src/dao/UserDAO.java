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

    private static final int DEFAULT_RANKING = 1000;
    private static final int DEFAULT_WINS = 0;
    private static final int DEFAULT_DRAWS = 0;
    private static final int DEFAULT_LOSSES = 0;
    private static final int DEFAULT_GAMES_PLAYED = 0;

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
                    throw new SQLException("Tworzenie użytkownika nie powiodło się.");
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        if (role == Role.GRACZ) {
                            PlayerDAO playerDAO = new PlayerDAO();
                            playerDAO.addPlayerDetails(userId, DEFAULT_RANKING, conn);
                        }
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        throw new SQLException("Błąd w nadaniu wartości gracza");
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
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
    public User loginUser(String login, String password) throws SQLException {
        String query = "SELECT u.idusers, u.login, u.password, u.firstname, u.lastname, u.role, " +
                "p.id AS players_table_id, p.ranking, p.wins, p.draws, p.losses, p.games_played " +
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
                        boolean isPlayersTableIdNull = rs.wasNull();
                        int ranking = rs.getInt("ranking");
                        boolean isRankingNull = rs.wasNull();
                        int wins = rs.getInt("wins");
                        boolean isWinsNull = rs.wasNull();
                        int draws = rs.getInt("draws");
                        boolean isDrawsNull = rs.wasNull();
                        int losses = rs.getInt("losses");
                        boolean isLossesNull = rs.wasNull();
                        int gamesPlayed = rs.getInt("games_played");
                        boolean isGamesPlayedNull = rs.wasNull();
                        if (isPlayersTableIdNull || playersTableId == 0) {
                            PlayerDAO playerDAO = new PlayerDAO();
                            playerDAO.addPlayerDetails(idusers, DEFAULT_RANKING);
                            Integer newPlayersTableId = playerDAO.getPlayersTableIdByUserId(idusers);
                            if (newPlayersTableId != null) {
                                playersTableId = newPlayersTableId;
                                ranking = DEFAULT_RANKING;
                                wins = DEFAULT_WINS;
                                draws = DEFAULT_DRAWS;
                                losses = DEFAULT_LOSSES;
                                gamesPlayed = DEFAULT_GAMES_PLAYED;
                            } else {
                                return null;
                            }
                        } else {
                            if (isRankingNull) ranking = DEFAULT_RANKING;
                            if (isWinsNull) wins = DEFAULT_WINS;
                            if (isDrawsNull) draws = DEFAULT_DRAWS;
                            if (isLossesNull) losses = DEFAULT_LOSSES;
                            if (isGamesPlayedNull) gamesPlayed = DEFAULT_GAMES_PLAYED;
                        }
                        return new Player(idusers, userLogin, userPassword, userFirstname, userLastname, userRole,
                                playersTableId, ranking, wins, draws, losses, gamesPlayed);
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
    public ObservableList<User> getAllUsers() throws SQLException {
        ObservableList<User> userList = FXCollections.observableArrayList();
        String query = "SELECT u.idusers, u.login, u.password, u.firstname, u.lastname, u.role, " +
                "p.id AS players_table_id, p.ranking, p.wins, p.draws, p.losses, p.games_played " +
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
                    boolean isPlayersTableIdNull = rs.wasNull();
                    int ranking = rs.getInt("ranking");
                    boolean isRankingNull = rs.wasNull();
                    int wins = rs.getInt("wins");
                    boolean isWinsNull = rs.wasNull();
                    int draws = rs.getInt("draws");
                    boolean isDrawsNull = rs.wasNull();
                    int losses = rs.getInt("losses");
                    boolean isLossesNull = rs.wasNull();
                    int gamesPlayed = rs.getInt("games_played");
                    boolean isGamesPlayedNull = rs.wasNull();
                    if (isPlayersTableIdNull || playersTableId == 0) {
                        userList.add(new User(idusers, login, password, firstname, lastname, role));
                    } else {
                        if (isRankingNull) ranking = DEFAULT_RANKING;
                        if (isWinsNull) wins = DEFAULT_WINS;
                        if (isDrawsNull) draws = DEFAULT_DRAWS;
                        if (isLossesNull) losses = DEFAULT_LOSSES;
                        if (isGamesPlayedNull) gamesPlayed = DEFAULT_GAMES_PLAYED;

                        userList.add(new Player(idusers, login, password, firstname, lastname, role,
                                playersTableId, ranking, wins, draws, losses, gamesPlayed));
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

    public boolean deleteUser(int userId) throws SQLException {
        String getUserRoleQuery = "SELECT role FROM users WHERE idusers = ?";
        String deletePlayerQuery = "DELETE FROM players WHERE user_id = ?";
        String deleteUserQuery = "DELETE FROM users WHERE idusers = ?";
        try (Connection conn = JDBC.getConnection()) {
            conn.setAutoCommit(false);
            String roleString = null;
            try (PreparedStatement psRole = conn.prepareStatement(getUserRoleQuery)) {
                psRole.setInt(1, userId);
                try (ResultSet rs = psRole.executeQuery()) {
                    if (rs.next()) {
                        roleString = rs.getString("role");
                    }
                }
            }
            if (roleString != null && Role.valueOf(roleString) == Role.GRACZ) {
                try (PreparedStatement psDeletePlayer = conn.prepareStatement(deletePlayerQuery)) {
                    psDeletePlayer.setInt(1, userId);
                    psDeletePlayer.executeUpdate();
                }
            }
            try (PreparedStatement psDeleteUser = conn.prepareStatement(deleteUserQuery)) {
                psDeleteUser.setInt(1, userId);
                int affectedRows = psDeleteUser.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            throw e;
        }
    }
}
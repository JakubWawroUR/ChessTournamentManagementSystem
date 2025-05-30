package src.dao;

import src.Connection.JDBC;
import src.model.Role;
import src.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM USERS";
        try (Connection conn = JDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                User user = new User(
                        rs.getInt("idusers"),
                        rs.getString("login"),
                        rs.getString("password"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        Role.valueOf(rs.getString("role").toUpperCase())
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    public boolean checkUserExists(String login,String password){
        String query = "SELECT 1 FROM USERS WHERE login = ? AND password = ? ";
        try(Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)){
            ps.setString(1,login);
            ps.setString(2,password);
            try(ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public void addUser(User user) throws SQLException {
        String query = "INSERT INTO USERS (login, password, firstname, lastname) VALUES (?, ?, ?, ?)";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getLastname());
            ps.executeUpdate();

            // Pobierz wygenerowane ID
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
}

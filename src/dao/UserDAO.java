package src.dao;

import src.Connection.JDBC;
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
                        rs.getString("role")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    public boolean checkUserExists(String login,String password) {
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
}

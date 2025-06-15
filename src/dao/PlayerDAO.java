package src.dao;

import src.Connection.JDBC;
import src.model.Game;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {


    public void addPlayerDetails(int userId, int ranking, Connection existingConn) throws SQLException {
        String insertPlayerQuery = "INSERT INTO players (user_id, ranking, wins, draws, losses, games_played) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = existingConn.prepareStatement(insertPlayerQuery)) {
            ps.setInt(1, userId);
            ps.setInt(2, ranking);
            ps.setInt(3, 0); 
            ps.setInt(4, 0); 
            ps.setInt(5, 0); 
            ps.setInt(6, 0); 
            ps.executeUpdate();
        }
    }

    public void addPlayerDetails(int userId, int ranking) throws SQLException {
        try (Connection conn = JDBC.getConnection()) {
            addPlayerDetails(userId, ranking, conn);
        }
    }

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


    public void updatePlayerRanking(int playerId, int newRanking) throws SQLException {
        String query = "UPDATE players SET ranking = ? WHERE id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, newRanking);
            ps.setInt(2, playerId);
            ps.executeUpdate();
        }
    }

    public void deletePlayerDetails(int playerId) throws SQLException {
        String query = "DELETE FROM players WHERE id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playerId);
            ps.executeUpdate();
        }
    }

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
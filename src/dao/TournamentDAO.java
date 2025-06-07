package src.dao;

import src.Connection.JDBC;
import src.model.Tournament;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournamentDAO {

    /**
     * Pobiera wszystkie turnieje z bazy danych.
     *
     * @return Lista obiektów Tournament.
     */
    public List<Tournament> getAllTournaments() {
        List<Tournament> tournaments = new ArrayList<>();
        // Zaktualizowano zapytanie SQL o nowe kolumny
        String query = "SELECT id, name, start_date, end_date, max_slots, free_slots FROM TOURNAMENTS ORDER BY start_date DESC";

        System.out.println("TournamentDAO: Wykonuję zapytanie SELECT: " + query);

        try (Connection conn = JDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int rowCount = 0;
            while (rs.next()) {
                // Odczytywanie wszystkich 6 pól
                Tournament tournament = new Tournament(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getInt("max_slots"),   // Nowe pole
                        rs.getInt("free_slots")    // Nowe pole
                );
                tournaments.add(tournament);
                rowCount++;
                System.out.println("TournamentDAO: Pobrany turniej: ID=" + tournament.getId() + ", Nazwa=" + tournament.getName() +
                        ", Start=" + tournament.getStartDate() + ", Koniec=" + tournament.getEndDate() +
                        ", Max=" + tournament.getMaxSlots() + ", Wolne=" + tournament.getFreeSlots());
            }
            System.out.println("TournamentDAO: Pomyślnie pobrano " + rowCount + " turniejów z bazy.");

        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania wszystkich turniejów: " + e.getMessage());
            e.printStackTrace();
        }

        return tournaments;
    }

    /**
     * Dodaje nowy turniej do bazy danych.
     *
     * @param tournament Obiekt Tournament do dodania.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void addTournament(Tournament tournament) throws SQLException {
        // Zaktualizowano zapytanie INSERT o nowe kolumny
        String query = "INSERT INTO TOURNAMENTS (name, start_date, end_date, max_slots, free_slots) VALUES (?, ?, ?, ?, ?)";

        System.out.println("TournamentDAO: Wykonuję zapytanie INSERT dla turnieju: " + tournament.getName());

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getStartDate());
            ps.setString(3, tournament.getEndDate());
            ps.setInt(4, tournament.getMaxSlots());
            ps.setInt(5, tournament.getFreeSlots());

            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas INSERT.");

            // Pobierz wygenerowane id i ustaw w obiekcie tournament
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tournament.setId(generatedKeys.getInt(1));
                    System.out.println("TournamentDAO: Nowe ID turnieju: " + tournament.getId());
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas dodawania turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Aktualizuje istniejący turniej w bazie danych na podstawie jego ID.
     *
     * @param tournament Obiekt Tournament z zaktualizowanymi danymi (musi zawierać ID).
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void updateTournament(Tournament tournament) throws SQLException {
        // Zaktualizowano zapytanie UPDATE o nowe kolumny
        String query = "UPDATE TOURNAMENTS SET name = ?, start_date = ?, end_date = ?, max_slots = ?, free_slots = ? WHERE id = ?";

        System.out.println("TournamentDAO: Wykonuję zapytanie UPDATE dla turnieju ID: " + tournament.getId() +
                ", Nazwa: " + tournament.getName() +
                ", Start: " + tournament.getStartDate() +
                ", Koniec: " + tournament.getEndDate() +
                ", Max Miejsc: " + tournament.getMaxSlots() +
                ", Wolne Miejsca: " + tournament.getFreeSlots()); // Dodano szczegóły dla diagnostyki

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getStartDate());
            ps.setString(3, tournament.getEndDate());
            ps.setInt(4, tournament.getMaxSlots());
            ps.setInt(5, tournament.getFreeSlots());
            ps.setInt(6, tournament.getId());

            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas UPDATE.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas aktualizacji turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Usuwa turniej z bazy danych po ID.
     *
     * @param id ID turnieju do usunięcia.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void deleteTournament(int id) throws SQLException {
        String query = "DELETE FROM TOURNAMENTS WHERE id = ?";

        System.out.println("TournamentDAO: Wykonuję zapytanie DELETE dla turnieju ID: " + id);

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);

            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas DELETE.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas usuwania turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Znajduje turniej po jego ID.
     *
     * @param id ID turnieju.
     * @return Obiekt Tournament, jeśli znaleziono, w przeciwnym razie null.
     */
    public Tournament getTournamentById(int id) {
        // Zaktualizowano zapytanie SELECT by ID o nowe kolumny
        String query = "SELECT id, name, start_date, end_date, max_slots, free_slots FROM TOURNAMENTS WHERE id = ?";

        System.out.println("TournamentDAO: Wykonuję zapytanie SELECT by ID dla turnieju ID: " + id);

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("TournamentDAO: Znaleziono turniej o ID: " + id);
                    return new Tournament(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("start_date"),
                            rs.getString("end_date"),
                            rs.getInt("max_slots"),
                            rs.getInt("free_slots")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania turnieju o ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("TournamentDAO: Nie znaleziono turnieju o ID: " + id);
        return null;
    }

    // Nowe metody do zarządzania dołączaniem graczy do turniejów

    /**
     * Dodaje gracza do turnieju.
     * @param tournamentId ID turnieju.
     * @param playerId ID gracza.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void addPlayerToTournament(int tournamentId, int playerId) throws SQLException {
        String query = "INSERT INTO chess_schema.tournament_players (tournament_id, player_id) VALUES (?, ?)"; //
        System.out.println("TournamentDAO: Dodaję gracza ID " + playerId + " do turnieju ID " + tournamentId);
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, playerId);
            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Dodano " + affectedRows + " wiersz do tournament_players.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas dodawania gracza do turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Sprawdza, czy gracz jest już zapisany na dany turniej.
     * @param tournamentId ID turnieju.
     * @param playerId ID gracza.
     * @return true jeśli gracz jest zapisany, false w przeciwnym razie.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public boolean isPlayerRegisteredForTournament(int tournamentId, int playerId) throws SQLException {
        String query = "SELECT COUNT(*) FROM chess_schema.tournament_players WHERE tournament_id = ? AND player_id = ?"; //
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas sprawdzania rejestracji gracza: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return false;
    }


    /**
     * Zwiększa lub zmniejsza liczbę wolnych miejsc w turnieju.
     * @param tournamentId ID turnieju.
     * @param changeValue Wartość zmiany (np. -1 dla dołączenia, +1 dla opuszczenia).
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void updateFreeSlots(int tournamentId, int changeValue) throws SQLException {
        String query = "UPDATE TOURNAMENTS SET free_slots = free_slots + ? WHERE id = ?";
        System.out.println("TournamentDAO: Aktualizuję wolne miejsca dla turnieju ID " + tournamentId + ", zmiana: " + changeValue);
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, changeValue);
            ps.setInt(2, tournamentId);
            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas aktualizacji wolnych miejsc.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas aktualizacji wolnych miejsc w turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
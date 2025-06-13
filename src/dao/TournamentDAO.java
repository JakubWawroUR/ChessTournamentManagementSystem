package src.dao;

import src.Connection.JDBC;
import src.model.Game;
import src.model.Player;
import src.model.Role;
import src.model.Tournament;
import src.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.CallableStatement;
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
        // DODANO 'status' do zapytania SELECT
        String query = "SELECT id, name, start_date, end_date, max_slots, free_slots, status FROM tournaments ORDER BY start_date DESC";

        System.out.println("TournamentDAO: Wykonuję zapytanie SELECT: " + query);

        try (Connection conn = JDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int rowCount = 0;
            while (rs.next()) {
                Tournament tournament = new Tournament(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getInt("max_slots"),
                        rs.getInt("free_slots"),
                        rs.getString("status") // POBIERZ STATUS
                );
                tournaments.add(tournament);
                rowCount++;
                System.out.println("TournamentDAO: Pobrany turniej: ID=" + tournament.getId() + ", Nazwa=" + tournament.getName() +
                        ", Start=" + tournament.getStartDate() + ", Koniec=" + tournament.getEndDate() +
                        ", Max=" + tournament.getMaxSlots() + ", Wolne=" + tournament.getFreeSlots() +
                        ", Status=" + tournament.getStatus());
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
     * Wygenerowane ID zostanie ustawione z powrotem na obiekcie Tournament.
     *
     * @param tournament Obiekt Tournament do dodania.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void addTournament(Tournament tournament) throws SQLException {
        // DODANO 'status' do INSERT
        String query = "INSERT INTO tournaments (name, start_date, end_date, max_slots, free_slots, status) VALUES (?, ?, ?, ?, ?, ?)";

        System.out.println("TournamentDAO: Wykonuję zapytanie INSERT dla turnieju: " + tournament.getName());

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getStartDate());
            ps.setString(3, tournament.getEndDate());
            ps.setInt(4, tournament.getMaxSlots());
            ps.setInt(5, tournament.getFreeSlots());
            ps.setString(6, tournament.getStatus()); // Ustaw status

            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas INSERT.");

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tournament.setId(generatedKeys.getInt(1));
                    System.out.println("TournamentDAO: Nowe ID turnieju: " + tournament.getId());
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas dodawania turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rzuć ponownie, aby metoda wywołująca mogła obsłużyć
        }
    }

    /**
     * Aktualizuje istniejący turniej w bazie danych na podstawie jego ID.
     *
     * @param tournament Obiekt Tournament z zaktualizowanymi danymi (musi zawierać ID).
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void updateTournament(Tournament tournament) throws SQLException {
        // DODANO 'status' do UPDATE
        String query = "UPDATE tournaments SET name = ?, start_date = ?, end_date = ?, max_slots = ?, free_slots = ?, status = ? WHERE id = ?";

        System.out.println("TournamentDAO: Wykonuję zapytanie UPDATE dla turnieju ID: " + tournament.getId() +
                ", Nazwa: " + tournament.getName() +
                ", Start: " + tournament.getStartDate() +
                ", Koniec: " + tournament.getEndDate() +
                ", Maksymalne Miejsca: " + tournament.getMaxSlots() +
                ", Wolne Miejsca: " + tournament.getFreeSlots() +
                ", Status: " + tournament.getStatus()); // Log status

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getStartDate());
            ps.setString(3, tournament.getEndDate());
            ps.setInt(4, tournament.getMaxSlots());
            ps.setInt(5, tournament.getFreeSlots());
            ps.setString(6, tournament.getStatus()); // Ustaw status
            ps.setInt(7, tournament.getId());

            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas UPDATE.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas aktualizacji turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rzuć ponownie, aby metoda wywołująca mogła obsłużyć
        }
    }

    /**
     * Usuwa turniej z bazy danych po ID.
     *
     * @param id The ID of the tournament to delete.
     * @throws SQLException If a SQL error occurs.
     */
    public void deleteTournament(int id) throws SQLException {
        String query = "DELETE FROM tournaments WHERE id = ?";

        System.out.println("TournamentDAO: Wykonuję zapytanie DELETE dla turnieju ID: " + id);

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);

            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas DELETE.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas usuwania turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rzuć ponownie, aby metoda wywołująca mogła obsłużyć
        }
    }

    /**
     * Znajduje turniej po jego ID.
     *
     * @param id ID turnieju.
     * @return Obiekt Tournament, jeśli znaleziono, w przeciwnym razie null.
     */
    public Tournament getTournamentById(int id) {
        // DODANO 'status' do SELECT
        String query = "SELECT id, name, start_date, end_date, max_slots, free_slots, status FROM tournaments WHERE id = ?";

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
                            rs.getInt("free_slots"),
                            rs.getString("status") // POBIERZ STATUS
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

    /**
     * Dodaje gracza do turnieju.
     * Po dodaniu gracza aktualizuje wolne miejsca i sprawdza status turnieju.
     *
     * @param tournamentId ID turnieju.
     * @param playerId ID gracza (z tabeli 'players').
     * @return Zaktualizowany obiekt Tournament po dołączeniu gracza.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     * @throws IllegalStateException Jeśli gracz jest już zapisany lub turniej jest zamknięty/pełny.
     */
    public Tournament addPlayerToTournament(int tournamentId, int playerId) throws SQLException, IllegalStateException {
        Tournament tournament = getTournamentById(tournamentId);
        if (tournament == null) {
            throw new IllegalArgumentException("Turniej o ID " + tournamentId + " nie istnieje.");
        }
        if (!tournament.getStatus().equals("OTWARTY")) {
            throw new IllegalStateException("Nie można dołączyć do turnieju, którego status to: " + tournament.getStatus());
        }
        if (isPlayerRegisteredForTournament(tournamentId, playerId)) {
            throw new IllegalStateException("Gracz o ID " + playerId + " jest już zapisany na turniej ID " + tournamentId + ".");
        }
        if (tournament.getFreeSlots() <= 0) { // Ponowna weryfikacja, choć status OTWARTY powinien to też obejmować
            throw new IllegalStateException("Turniej o ID " + tournamentId + " jest już pełny.");
        }

        String query = "INSERT INTO tournament_players (tournament_id, player_id) VALUES (?, ?)";
        System.out.println("TournamentDAO: Dodaję gracza ID " + playerId + " do turnieju ID " + tournamentId);
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, playerId);
            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Dodano " + affectedRows + " wiersz do tournament_players.");

            // Po pomyślnym dodaniu gracza, aktualizuj wolne miejsca i sprawdź status
            // Metoda updateFreeSlotsAndCheckStatus zwróci zaktualizowany obiekt turnieju
            Tournament updatedTournament = updateFreeSlotsAndCheckStatus(tournamentId, -1);
            if (updatedTournament != null) {
                System.out.println("TournamentDAO: Zaktualizowany turniej po dodaniu gracza: " + updatedTournament.getFreeSlots() + " wolnych miejsc, status: " + updatedTournament.getStatus());
            }
            return updatedTournament; // Zwróć zaktualizowany obiekt turnieju
        } catch (SQLException e) {
            System.err.println("Błąd podczas dodawania gracza do turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * Zwiększa lub zmniejsza liczbę wolnych miejsc w turnieju i sprawdza, czy turniej został zapełniony.
     * Jeśli free_slots spadnie do 0 (lub poniżej), status turnieju jest zmieniany na "ZAMKNIĘTY"
     * i generowane są mecze (jeśli jeszcze ich nie ma).
     *
     * @param tournamentId ID turnieju.
     * @param changeValue Wartość zmiany (np. -1 dla dołączenia gracza, +1 dla usunięcia).
     * @return Zaktualizowany obiekt Tournament (lub null, jeśli nie znaleziono).
     * @throws SQLException If a SQL error occurs.
     * @throws IllegalStateException If games already generated (from generateGamesForTournament).
     */
    public Tournament updateFreeSlotsAndCheckStatus(int tournamentId, int changeValue) throws SQLException, IllegalStateException {
        // 1. Zaktualizuj free_slots
        String updateSlotsQuery = "UPDATE tournaments SET free_slots = free_slots + ? WHERE id = ?";
        System.out.println("TournamentDAO: Aktualizuję wolne miejsca dla turnieju ID " + tournamentId + ", zmiana: " + changeValue);
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSlotsQuery)) {
            ps.setInt(1, changeValue);
            ps.setInt(2, tournamentId);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("TournamentDAO: Nie znaleziono turnieju do aktualizacji wolnych miejsc (ID: " + tournamentId + ").");
                return null;
            }
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas aktualizacji wolnych miejsc.");
        }

        // 2. Pobierz zaktualizowany turniej, aby sprawdzić jego status i free_slots
        Tournament updatedTournament = getTournamentById(tournamentId);

        if (updatedTournament != null) {
            // 3. Sprawdź, czy turniej jest pełny i jego status jest nadal "OTWARTY"
            // Upewnij się, że to nie jest usunięcie gracza z pełnego turnieju, które miałoby go znowu otworzyć
            // (jeśli changeValue jest dodatnie, czyli gracz opuszcza, nie generujemy meczów)
            if (updatedTournament.getFreeSlots() <= 0 && updatedTournament.getStatus().equals("OTWARTY") && changeValue < 0) {
                System.out.println("TournamentDAO: Turniej ID " + tournamentId + " został zapełniony. Zmieniam status na ZAMKNIĘTY i generuję mecze.");
                updateTournamentStatus(tournamentId, "ZAMKNIĘTY"); // Zmień status na ZAMKNIĘTY
                updatedTournament.setStatus("ZAMKNIĘTY"); // Zaktualizuj obiekt w pamięci

                // Generuj mecze automatycznie, ale tylko jeśli jeszcze ich nie ma
                try {
                    generateGamesForTournament(tournamentId);
                } catch (IllegalStateException e) {
                    System.out.println("TournamentDAO: Ostrzeżenie podczas generowania meczów: " + e.getMessage());
                    // Możesz zdecydować, czy rzucić wyjątek, czy tylko zalogować
                    // Jeśli mecze już są, to po prostu kontynuujemy.
                }
            }
            // Opcjonalnie: jeśli free_slots > 0 i status jest "ZAMKNIĘTY", możesz zmienić na "OTWARTY"
            // np. gdy gracz opuścił pełny turniej
            else if (updatedTournament.getFreeSlots() > 0 && updatedTournament.getStatus().equals("ZAMKNIĘTY") && changeValue > 0) {
                System.out.println("TournamentDAO: Turniej ID " + tournamentId + " ma wolne miejsca. Zmieniam status na OTWARTY.");
                updateTournamentStatus(tournamentId, "OTWARTY");
                updatedTournament.setStatus("OTWARTY");
            }
        }
        return updatedTournament;
    }

    /**
     * Zmienia status turnieju w bazie danych.
     * @param tournamentId ID turnieju.
     * @param newStatus Nowy status (np. "OTWARTY", "ZAMKNIĘTY", "W TRAKCIE", "ZAKOŃCZONY").
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void updateTournamentStatus(int tournamentId, String newStatus) throws SQLException {
        String query = "UPDATE tournaments SET status = ? WHERE id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, newStatus);
            ps.setInt(2, tournamentId);
            ps.executeUpdate();
            System.out.println("TournamentDAO: Zmieniono status turnieju ID " + tournamentId + " na: " + newStatus);
        }
    }

    /**
     * Generuje mecze dla danego turnieju na podstawie zarejestrowanych graczy.
     * Implementuje prosty system "każdy z każdym" (round-robin).
     *
     * @param tournamentId ID turnieju.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     * @throws IllegalStateException Jeśli brak graczy lub mecze już istnieją.
     */
    public void generateGamesForTournament(int tournamentId) throws SQLException, IllegalStateException {
        // 1. Sprawdź, czy turniej ma już jakieś mecze (aby nie generować podwójnie)
        String checkGamesQuery = "SELECT COUNT(*) FROM games WHERE tournament_id = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkGamesQuery)) {
            ps.setInt(1, tournamentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new IllegalStateException("Mecze dla tego turnieju już zostały wygenerowane.");
            }
        }

        // 2. Pobierz wszystkich graczy zarejestrowanych w tym turnieju
        // Upewnij się, że getRegisteredPlayersWithRecordsForTournament jest w tej samej klasie
        List<Player> registeredPlayers = getRegisteredPlayersWithRecordsForTournament(tournamentId);

        if (registeredPlayers.size() < 2) {
            throw new IllegalStateException("Aby wygenerować mecze, w turnieju musi być co najmniej dwóch graczy.");
        }

        String insertGameQuery = "INSERT INTO games (tournament_id, player1_id, player2_id, game_number) VALUES (?, ?, ?, ?)";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertGameQuery)) {

            int gameNumber = 1;
            // Generowanie par każdy z każdym
            for (int i = 0; i < registeredPlayers.size(); i++) {
                for (int j = i + 1; j < registeredPlayers.size(); j++) {
                    Player player1 = registeredPlayers.get(i);
                    Player player2 = registeredPlayers.get(j);

                    ps.setInt(1, tournamentId);
                    ps.setInt(2, player1.getPlayersTableId()); // Użyj ID z tabeli 'players'
                    ps.setInt(3, player2.getPlayersTableId()); // Użyj ID z tabeli 'players'
                    ps.setInt(4, gameNumber);
                    ps.addBatch(); // Dodaj do partii

                    gameNumber++;
                }
            }
            ps.executeBatch(); // Wykonaj wszystkie wstawienia
            System.out.println("TournamentDAO: Wygenerowano " + (gameNumber - 1) + " meczów dla turnieju ID: " + tournamentId);

        } catch (SQLException e) {
            System.err.println("Błąd podczas generowania meczów dla turnieju: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Sprawdza, czy gracz jest już zapisany na dany turniej.
     * @param tournamentId ID turnieju.
     * @param playerId ID gracza (z tabeli 'players').
     * @return true jeśli gracz jest zapisany, false w przeciwnym razie.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public boolean isPlayerRegisteredForTournament(int tournamentId, int playerId) throws SQLException {
        String query = "SELECT COUNT(*) FROM tournament_players WHERE tournament_id = ? AND player_id = ?";
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

    // Metoda getRegisteredPlayersWithRecordsForTournament (już wcześniej podana)
    // ... (pozostałe metody) ...

    /**
     * Pobiera graczy zarejestrowanych w danym turnieju wraz z ich rekordami (wygrane/remisy/przegrane)
     * w ramach tego turnieju.
     *
     * @param tournamentId ID turnieju.
     * @return Lista obiektów Player zarejestrowanych w turnieju z ich rekordami.
     */
    public List<Player> getRegisteredPlayersWithRecordsForTournament(int tournamentId) throws SQLException {
        List<Player> participants = new ArrayList<>();
        String sql = "SELECT " +
                "u.idusers, " +
                "u.login, " +
                "u.password, " +
                "u.firstname, " +
                "u.lastname, " +
                "u.role, " +
                "p.ranking, " +
                "p.id AS players_table_id, " +
                "p.wins AS total_wins, " +
                "p.draws AS total_draws, " +
                "p.losses AS total_losses, " +
                "p.games_played AS total_games_played " +
                "FROM tournament_players tp " +
                "JOIN players p ON tp.player_id = p.id " +
                "JOIN users u ON p.user_id = u.idusers " +
                "WHERE tp.tournament_id = ?";

        System.out.println("TournamentDAO: Wykonuję zapytanie SELECT graczy (z rekordami) dla turnieju ID: " + tournamentId);

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tournamentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int idusers = rs.getInt("idusers");
                String login = rs.getString("login");
                String password = rs.getString("password");
                String firstname = rs.getString("firstname");
                String lastname = rs.getString("lastname");
                Role role = Role.valueOf(rs.getString("role").toUpperCase());

                int playerTableId = rs.getInt("players_table_id");
                int ranking = rs.getInt("ranking");

                int totalWins = rs.getInt("total_wins");
                int totalDraws = rs.getInt("total_draws");
                int totalLosses = rs.getInt("total_losses");
                int totalGamesPlayed = rs.getInt("total_games_played");

                Player player = new Player(
                        idusers,
                        login,
                        password,
                        firstname,
                        lastname,
                        role,
                        playerTableId,
                        ranking,
                        totalWins,
                        totalDraws,
                        totalLosses,
                        totalGamesPlayed
                );

                int tournamentWins = 0;
                int tournamentDraws = 0;
                int tournamentLosses = 0;

                String gamesSql = "SELECT player1_id, player2_id, winner_id FROM games WHERE tournament_id = ? AND (player1_id = ? OR player2_id = ?)";
                try (PreparedStatement gamesPstmt = conn.prepareStatement(gamesSql)) {
                    gamesPstmt.setInt(1, tournamentId);
                    gamesPstmt.setInt(2, playerTableId);
                    gamesPstmt.setInt(3, playerTableId);
                    ResultSet gamesRs = gamesPstmt.executeQuery();

                    while (gamesRs.next()) {
                        Integer winnerId = (Integer) gamesRs.getObject("winner_id");

                        if (winnerId == null) {
                            tournamentDraws++;
                        } else if (winnerId == playerTableId) {
                            tournamentWins++;
                        } else {
                            tournamentLosses++;
                        }
                    }
                }

                player.setWins(tournamentWins);
                player.setDraws(tournamentDraws);
                player.setLosses(tournamentLosses);
                player.setGamesPlayed(tournamentWins + tournamentDraws + tournamentLosses);

                participants.add(player);
                System.out.println("TournamentDAO: Pobrany gracz z rekordem turniejowym: " + player.getFirstName() + " " + player.getLastName() +
                        " (ID: " + player.getPlayersTableId() + ") Rekord: " + player.getRecord());
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania graczy z rekordami dla turnieju ID " + tournamentId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return participants;
    }

    /**
     * Pobiera wszystkie mecze dla danego turnieju, wraz z nazwami graczy.
     *
     * @param tournamentId ID turnieju, dla którego mają być pobrane mecze.
     * @return Lista obiektów Game.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public List<Game> getAllGamesForTournament(int tournamentId) throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT g.id, g.tournament_id, g.player1_id, g.player2_id, g.winner_id, g.game_number, " +
                "u1.firstname AS p1_firstname, u1.lastname AS p1_lastname, " +
                "u2.firstname AS p2_firstname, u2.lastname AS p2_lastname, " +
                "uw.firstname AS w_firstname, uw.lastname AS w_lastname " +
                "FROM games g " +
                "JOIN players p1 ON g.player1_id = p1.id " +
                "JOIN users u1 ON p1.user_id = u1.idusers " +
                "JOIN players p2 ON g.player2_id = p2.id " +
                "JOIN users u2 ON p2.user_id = u2.idusers " +
                "LEFT JOIN players pw ON g.winner_id = pw.id " +
                "LEFT JOIN users uw ON pw.user_id = uw.idusers " +
                "WHERE g.tournament_id = ? " +
                "ORDER BY g.game_number ASC";

        System.out.println("TournamentDAO: Wykonuję zapytanie SELECT wszystkich meczów dla turnieju ID: " + tournamentId);

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tournamentId);
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

                game.setPlayer1Name(rs.getString("p1_firstname") + " " + rs.getString("p1_lastname"));
                game.setPlayer2Name(rs.getString("p2_firstname") + " " + rs.getString("p2_lastname"));

                String winnerFirstname = rs.getString("w_firstname");
                String winnerLastname = rs.getString("w_lastname");
                if (game.getWinnerId() == null) {
                    game.setWinnerName("Remis");
                } else if (winnerFirstname != null && winnerLastname != null) {
                    game.setWinnerName(winnerFirstname + " " + winnerLastname);
                } else {
                    game.setWinnerName("Nieznany");
                }

                games.add(game);
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania wszystkich meczów dla turnieju ID " + tournamentId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return games;
    }

    public void updateGameResult(int gameId, Integer winnerPlayerId) throws SQLException {
        String query = "UPDATE games SET winner_id = ? WHERE id = ?";
        System.out.println("TournamentDAO: Aktualizuję wynik meczu ID: " + gameId + ", Zwycięzca ID: " + winnerPlayerId);

        try (Connection conn = JDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            if (winnerPlayerId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, winnerPlayerId);
            }
            ps.setInt(2, gameId);

            int affectedRows = ps.executeUpdate();
            System.out.println("TournamentDAO: Zmodyfikowano " + affectedRows + " wierszy podczas aktualizacji wyniku meczu.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas aktualizacji wyniku meczu: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Opcjonalna metoda do wywołania procedury składowanej aktualizującej statystyki graczy.
     *
     * @param gameId ID meczu, którego wynik został zmieniony.
     * @param oldWinnerId Poprzednie ID zwycięzcy (może być null).
     * @param newWinnerId Nowe ID zwycięzcy (może być null).
     * @param player1Id ID pierwszego gracza w meczu.
     * @param player2Id ID drugiego gracza w meczu.
     * @throws SQLException Jeśli wystąpi błąd SQL.
     */
    public void callUpdatePlayerStatsProcedure(int gameId, Integer oldWinnerId, Integer newWinnerId, int player1Id, int player2Id) throws SQLException {
        String call = "{CALL UpdatePlayerStatsAfterGame(?, ?, ?, ?, ?)}";

        try (Connection conn = JDBC.getConnection();
             CallableStatement cs = conn.prepareCall(call)) {

            cs.setInt(1, gameId);
            if (oldWinnerId == null) {
                cs.setNull(2, java.sql.Types.INTEGER);
            } else {
                cs.setInt(2, oldWinnerId);
            }
            if (newWinnerId == null) {
                cs.setNull(3, java.sql.Types.INTEGER);
            } else {
                cs.setInt(3, newWinnerId);
            }
            cs.setInt(4, player1Id);
            cs.setInt(5, player2Id);

            cs.execute();
            System.out.println("TournamentDAO: Wywołano procedurę aktualizacji statystyk graczy.");

        } catch (SQLException e) {
            System.err.println("Błąd podczas wywoływania procedury aktualizacji statystyk graczy: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
package src.model;

public class Game {
    private int id;
    private int tournamentId;
    private int player1Id;
    private int player2Id;
    private Integer winnerId; // Użyj Integer, aby obsłużyć NULL dla remisu
    private int gameNumber; // Numer meczu (globalny w turnieju lub LP w widoku)

    // Do wyświetlania w tabeli, przechowujmy nazwy graczy
    private String player1Name; // Imię i Nazwisko Gracza 1
    private String player2Name; // Imię i Nazwisko Gracza 2
    private String winnerName; // Imię i Nazwisko zwycięzcy (lub "Remis")

    // NOWE POLA DLA WIDOKU INDYWIDUALNEGO GRACZA
    private String opponentName;        // Imię i nazwisko przeciwnika w danym meczu
    private String individualResultDisplay; // Sformatowany wynik z perspektywy gracza (Wygrana/Remis/Przegrana)

    public Game(int id, int tournamentId, int player1Id, int player2Id, Integer winnerId, int gameNumber) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.winnerId = winnerId;
        this.gameNumber = gameNumber;
    }

    // --- Gettery i Settery ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(int tournamentId) {
        this.tournamentId = tournamentId;
    }

    public int getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(int player1Id) {
        this.player1Id = player1Id;
    }

    public int getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(int player2Id) {
        this.player2Id = player2Id;
    }

    public Integer getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Integer winnerId) {
        this.winnerId = winnerId;
    }

    public int getGameNumber() {
        return gameNumber;
    }

    public void setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    // --- NOWE GETTERY I SETTERY ---
    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public String getIndividualResultDisplay() {
        return individualResultDisplay;
    }

    public void setIndividualResultDisplay(String individualResultDisplay) {
        this.individualResultDisplay = individualResultDisplay;
    }
    // --- KONIEC NOWYCH GETTERÓW I SETTERÓW ---

    // Metoda pomocnicza do formatowania ogólnego wyniku meczu (dla widoku wszystkich meczów turnieju, jeśli byś go przywrócił)
    public String getResultDisplay() {
        if (player1Name == null || player2Name == null) {
            return "N/A";
        }
        if (winnerId == null) {
            return player1Name + " vs " + player2Name + " (Remis)";
        } else if (winnerId == player1Id) {
            return player1Name + " vs " + player2Name + " (Zwycięzca: " + player1Name + ")";
        } else if (winnerId == player2Id) {
            return player1Name + " vs " + player2Name + " (Zwycięzca: " + player2Name + ")";
        }
        return "N/A";
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", tournamentId=" + tournamentId +
                ", player1Id=" + player1Id +
                ", player2Id=" + player2Id +
                ", winnerId=" + winnerId +
                ", gameNumber=" + gameNumber +
                ", player1Name='" + player1Name + '\'' +
                ", player2Name='" + player2Name + '\'' +
                ", winnerName='" + winnerName + '\'' +
                ", opponentName='" + opponentName + '\'' + // Dodano
                ", individualResultDisplay='" + individualResultDisplay + '\'' + // Dodano
                '}';
    }
}
package src.model;

public class Game {
    private int id;
    private int tournamentId;
    private int gameNumber;
    private int player1Id;
    private int player2Id;
    private Integer winnerId;

    private String player1Name;
    private String player2Name;
    private String winnerName; 
    private String tournamentName;
    private String opponentName;
    private String individualResultDisplay;


    public Game(int id, int tournamentId, int gameNumber, int player1Id, int player2Id, Integer winnerId) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.gameNumber = gameNumber;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.winnerId = winnerId;
    }

    public Game(int id, int tournamentId, int player1Id, int player2Id, Integer winnerId, int gameNumber) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.winnerId = winnerId;
        this.gameNumber = gameNumber;
    }


    public int getId() { return id; }
    public int getTournamentId() { return tournamentId; }
    public int getGameNumber() { return gameNumber; }
    public int getPlayer1Id() { return player1Id; }
    public int getPlayer2Id() { return player2Id; }
    public Integer getWinnerId() { return winnerId; }

    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }
    public String getWinnerName() { return winnerName; }
    public String getTournamentName() { return tournamentName; }
    public String getOpponentName() { return opponentName; }
    public String getIndividualResultDisplay() { return individualResultDisplay; }

    public void setId(int id) { this.id = id; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }
    public void setGameNumber(int gameNumber) { this.gameNumber = gameNumber; }
    public void setPlayer1Id(int player1Id) { this.player1Id = player1Id; }
    public void setPlayer2Id(int player2Id) { this.player2Id = player2Id; }
    public void setWinnerId(Integer winnerId) { this.winnerId = winnerId; }

    public void setPlayer1Name(String player1Name) { this.player1Name = player1Name; }
    public void setPlayer2Name(String player2Name) { this.player2Name = player2Name; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }
    public void setOpponentName(String opponentName) { this.opponentName = opponentName; }
    public void setIndividualResultDisplay(String individualResultDisplay) { this.individualResultDisplay = individualResultDisplay; }
}
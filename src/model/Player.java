package src.model;

public class Player extends User {
    private int playerId;
    private int ranking;
    private int displayNumber;
    private int wins;
    private int draws;
    private int losses;
    private int gamesPlayed;

    public Player(int id, String login, String password, String firstname, String lastname, Role role,
                  int playerId,
                  int ranking, int wins, int draws, int losses, int gamesPlayed) {
        super(id, login, password, firstname, lastname, role);

        this.playerId = playerId;
        this.ranking = ranking;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        this.gamesPlayed = gamesPlayed;
        this.displayNumber = 0;
    }

    public Player(String login, String password, String firstname, String lastname, int ranking) {
        super(login, password, firstname, lastname, Role.GRACZ);
        this.ranking = ranking;
        this.playerId = 0;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
        this.gamesPlayed = 0;
        this.displayNumber = 0;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public int getDisplayNumber() {
        return displayNumber;
    }

    public void setDisplayNumber(int displayNumber) {
        this.displayNumber = displayNumber;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public String getRecord() {
        return wins + "W / " + draws + "D / " + losses + "L";
    }
}
// src/model/Player.java

package src.model;

public class Player extends User {
    private int playersTableId; // ID z tabeli 'players' (kolumna 'id' w tabeli players)
    private int ranking;        // Ranking gracza
    private int displayNumber;  // Nowe pole do przechowywania kolejnego numeru dla wyświetlania w tabeli

    // --- POLA DLA REKORDU W TURNIEJU (TERAZ JUŻ Z GAMES_PLAYED) ---
    private int wins;
    private int draws;
    private int losses;
    private int gamesPlayed; // Dodane pole
    // --- KONIEC PÓL REKORDU ---


    // *** KLUCZOWY KONSTRUKTOR Z BAZY DANYCH ***
    // Używany, gdy pobieramy gracza z połączonych tabel users i players.
    // Argumenty:
    // id: to jest id z tabeli users (User.id)
    // login, password, firstname, lastname, role: dziedziczone z User
    // playersTableId: id z tabeli players
    // ranking, wins, draws, losses, gamesPlayed: z tabeli players
    public Player(int id, String login, String password, String firstname, String lastname, Role role,
                  int playersTableId, int ranking, int wins, int draws, int losses, int gamesPlayed) {
        // Wywołanie konstruktora klasy bazowej (User)
        super(id, login, password, firstname, lastname, role);

        // Inicjalizacja pól specyficznych dla Player
        this.playersTableId = playersTableId;
        this.ranking = ranking;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        this.gamesPlayed = gamesPlayed; // Inicjalizacja gamesPlayed
        this.displayNumber = 0; // Domyślna wartość, będzie ustawiana w kontrolerze UI
    }

    // Konstruktor dla NOWEGO gracza, który nie ma jeszcze ID z bazy danych.
    // Domyślne wartości dla playersTableId, wins, draws, losses, gamesPlayed.
    public Player(String login, String password, String firstname, String lastname, int ranking) {
        // Nowy gracz domyślnie ma rolę GRACZ (jeśli to jest Twoja ENUM-owa wartość)
        super(login, password, firstname, lastname, Role.GRACZ); // Upewnij się, że Role.GRACZ istnieje!
        this.ranking = ranking;
        this.playersTableId = 0; // Tymczasowa wartość, zostanie ustawiona po zapisaniu do bazy
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
        this.gamesPlayed = 0; // Inicjalizacja gamesPlayed
        this.displayNumber = 0; // Domyślna wartość
    }

    // --- Gettery i Settery (specyficzne dla Player) ---
    public int getPlayersTableId() {
        return playersTableId;
    }

    public void setPlayersTableId(int playersTableId) {
        this.playersTableId = playersTableId;
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

    public int getGamesPlayed() { // Getter dla gamesPlayed
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) { // Setter dla gamesPlayed
        this.gamesPlayed = gamesPlayed;
    }

    // Metoda pomocnicza do wyświetlania rekordu w formacie W/D/L
    public String getRecord() {
        return wins + "W / " + draws + "D / " + losses + "L"; // Zmieniono format na spójny
    }

    @Override
    public String toString() {
        return "Player{" +
                "idUsers=" + getId() + // Dziedziczone z User
                ", login='" + getLogin() + '\'' + // Dziedziczone z User
                ", password='" + getPassword() + '\'' + // Dziedziczone z User
                ", firstName='" + getFirstName() + '\'' + // Dziedziczone z User
                ", lastName='" + getLastName() + '\'' + // Dziedziczone z User
                ", role=" + getRole() + // Dziedziczone z User
                ", playersTableId=" + playersTableId +
                ", ranking=" + ranking +
                ", displayNumber=" + displayNumber +
                ", wins=" + wins +
                ", draws=" + draws +
                ", losses=" + losses +
                ", gamesPlayed=" + gamesPlayed + // Dodano do toString
                '}';
    }
}
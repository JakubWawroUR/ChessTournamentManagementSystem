package src.model;

// Klasa Player dziedziczy po User, więc ma wszystkie pola User (id, login, password, firstname, lastname, role)
public class Player extends User {
    private int playersTableId; // ID z tabeli 'players' (kolumna 'id' w tabeli players)
    private int ranking;        // Ranking gracza
    private int displayNumber;  // Nowe pole do przechowywania kolejnego numeru dla wyświetlania w tabeli

    // --- NOWE POLA DLA REKORDU W TURNIEJU ---
    private int wins;
    private int draws;
    private int losses;
    // --- KONIEC NOWYCH PÓL ---

    // *** KLUCZOWY KONSTRUKTOR ***
    // Ten konstruktor powinien być używany do tworzenia obiektów Player
    // pobranych z bazy danych.
    public Player(int id, String login, String password, String firstname, String lastname, Role role, int playersTableId, int ranking) {
        // Wywołanie konstruktora klasy bazowej (User) z przekazaniem roli
        super(id, login, password, firstname, lastname, role);
        this.playersTableId = playersTableId;
        this.ranking = ranking;
        this.displayNumber = 0; // Domyślna wartość, będzie ustawiana w kontrolerze
        // Inicjalizacja nowych pól rekordu
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
    }

    // Konstruktor dla nowego gracza, który nie ma jeszcze ID z bazy danych
    // (ani z tabeli 'users', ani z tabeli 'players').
    // Używany jest, gdy gracz jest tworzony w aplikacji przed pierwszym zapisaniem do bazy.
    public Player(String login, String password, String firstname, String lastname, int ranking) {
        super(login, password, firstname, lastname, Role.GRACZ); // Domyślnie nowo tworzony jest GRACZEM
        this.ranking = ranking;
        this.playersTableId = 0; // Tymczasowa wartość, zostanie ustawiona po zapisaniu do bazy
        this.displayNumber = 0; // Domyślna wartość, będzie ustawiana w kontrolerze
        // Inicjalizacja nowych pól rekordu
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
    }

    // Gettery i Settery specyficzne dla klasy Player
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

    // Getter i Setter dla displayNumber
    public int getDisplayNumber() {
        return displayNumber;
    }

    public void setDisplayNumber(int displayNumber) {
        this.displayNumber = displayNumber;
    }

    // --- NOWE GETTERY I SETTERY DLA PÓL REKORDU ---
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

    // Metoda pomocnicza do wyświetlania rekordu w formacie W/D/L
    public String getRecord() {
        return wins + "/" + draws + "/" + losses;
    }
    // --- KONIEC NOWYCH GETTERÓW I SETTERÓW ---

    // Opcjonalnie: nadpisanie metody toString() dla łatwiejszego debugowania
    @Override
    public String toString() {
        return "Player{" +
                "idUsers=" + getId() + // dziedziczone z User
                ", login='" + getLogin() + '\'' + // dziedziczone z User
                ", role=" + getRole() + // dziedziczone z User
                ", playersTableId=" + playersTableId +
                ", ranking=" + ranking +
                ", displayNumber=" + displayNumber +
                ", wins=" + wins + // Dodano do toString
                ", draws=" + draws + // Dodano do toString
                ", losses=" + losses + // Dodano do toString
                '}';
    }
}
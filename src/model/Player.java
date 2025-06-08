package src.model;

// Klasa Player dziedziczy po User, więc ma wszystkie pola User (id, login, password, firstname, lastname, role)
public class Player extends User {
    private int playersTableId; // ID z tabeli 'players' (kolumna 'idplayers' w Twojej bazie danych, jeśli tak się nazywa)
    private int ranking;        // Ranking gracza

    // *** KLUCZOWY KONSTRUKTOR ***
    // To jest konstruktor, którego UserDAO oczekuje w metodach loginUser i getAllUsers.
    // Przyjmuje 'role' jako jawny parametr.
    public Player(int id, String login, String password, String firstname, String lastname, Role role, int playersTableId, int ranking) {
        // Wywołanie konstruktora klasy bazowej (User) z przekazaniem roli
        super(id, login, password, firstname, lastname, role);
        this.playersTableId = playersTableId;
        this.ranking = ranking;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    // Konstruktor dla nowego gracza, który nie ma jeszcze ID z bazy danych
    // (ani z tabeli 'users', ani z tabeli 'players').
    // Używany jest, gdy gracz jest tworzony w aplikacji przed pierwszym zapisaniem do bazy.
    public Player(String login, String password, String firstname, String lastname, int ranking) {
        // ID użytkownika (z tabeli 'users') będzie auto-generowane przez bazę.
        // ID gracza (z tabeli 'players') będzie auto-generowane przez bazę po dodaniu do tabeli 'players'.
        super(login, password, firstname, lastname, Role.GRACZ); // Domyślnie nowo tworzony jest GRACZEM
        this.ranking = ranking;
        this.playersTableId = 0; // Tymczasowa wartość, zostanie ustawiona po zapisaniu do bazy
    }


    // --- WAŻNA UWAGA ---
    // Poniższy konstruktor (z 7 parametrami, bez 'Role' jako jawnego parametru)
    // był problemem, gdy UserDAO próbowało przekazać 'Role'.
    // Jeśli Twoja aplikacja wymaga konstruktora, który domyślnie zakłada, że rola to GRACZ,
    // a nie przyjmuje 'Role' jako parametr, to POWINIEN wyglądać tak:
    /*
    public Player(int id, String login, String password, String firstname, String lastname, int ranking, int playersTableId) {
        // Zakładamy, że każdy obiekt stworzony tym konstruktorem to GRACZ
        super(id, login, password, firstname, lastname, Role.GRACZ);
        this.ranking = ranking;
        this.playersTableId = playersTableId;
    }
    */
    // Jednakże, ponieważ UserDAO już poprawnie przekazuje 'Role',
    // ten konstruktor nie jest już główną przyczyną błędu.
    // Możesz go zachować, jeśli jest wywoływany gdzieś indziej,
    // ale **głównym problemem był brak 8-parametrowego konstruktora z Role.**


    // Gettery i Settery specyficzne dla klasy Player
    public int getPlayersTableId() {
        return playersTableId;
    }
    public String getFirstName() {
        return firstname;
    }
    public String getLastName() {
        return lastname;
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

    // Opcjonalnie: nadpisanie metody toString() dla łatwiejszego debugowania
    @Override
    public String toString() {
        return "Player{" +
                "idUsers=" + getId() + // dziedziczone z User
                ", login='" + getLogin() + '\'' + // dziedziczone z User
                ", role=" + getRole() + '\'' + // dziedziczone z User
                ", playersTableId=" + playersTableId +
                ", ranking=" + ranking +
                '}';
    }
}